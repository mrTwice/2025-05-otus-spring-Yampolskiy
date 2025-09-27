package ru.otus.hw.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mapping.MappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.otus.hw.exceptions.BusinessException;
import ru.otus.hw.exceptions.ErrorCode;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalRestExceptionHandler {

    private final ErrorCodeHttpStatusMapper statusMapper;

    private final ProblemDetailFactory pdf;

    private final ValidationErrorExtractor valExtractor;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex) {
        HttpStatus status = statusMapper.toStatus(ex.getCode());
        ProblemDetail body = pdf.create(status, ex.getCode().name(), ex.getMessage(),
                Map.of("code", ex.getCode().name()));
        logAtLevel(ex, status);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleWebFluxBinding(WebExchangeBindException ex) {
        BindingResult br = ex.getBindingResult();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail body = pdf.create(status, ErrorCode.VALIDATION.name(), "Validation failed",
                Map.of("code", ErrorCode.VALIDATION.name(), "errors", valExtractor.toList(br)));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ProblemDetail> handleBindingFallback(Exception ex) {
        BindingResult br = (ex instanceof MethodArgumentNotValidException manve)
                ? manve.getBindingResult()
                : ((BindException) ex).getBindingResult();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail body = pdf.create(status, ErrorCode.VALIDATION.name(), "Validation failed",
                Map.of("code", ErrorCode.VALIDATION.name(), "errors", valExtractor.toList(br)));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({
            ServerWebInputException.class,
            MethodArgumentTypeMismatchException.class,
            MappingException.class
    })
    public ResponseEntity<ProblemDetail> handleInput(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Invalid parameter value";
        if (ex instanceof MethodArgumentTypeMismatchException mm) {
            String name = mm.getName();
            String value = String.valueOf(mm.getValue());
            String required = (mm.getRequiredType() != null) ? mm.getRequiredType().getSimpleName() : "required type";
            msg = "Parameter '%s' has invalid value '%s' (expected %s)".formatted(name, value, required);
        } else if (ex instanceof ServerWebInputException swe && swe.getReason() != null) {
            msg = swe.getReason();
        }
        ProblemDetail body = pdf.create(status, ErrorCode.VALIDATION.name(), msg,
                Map.of("code", ErrorCode.VALIDATION.name()));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ProblemDetail> handleDuplicate(DuplicateKeyException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        String detail = resolveDuplicateDetail(ex.getMessage());
        ProblemDetail body = pdf.create(status, ErrorCode.DUPLICATE.name(), detail,
                Map.of("code", ErrorCode.DUPLICATE.name()));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimistic(OptimisticLockingFailureException ex) {
        return conflict(ErrorCode.CONFLICT.name(), "Конфликт версий (объект был изменён конкурентно)");
    }

    @ExceptionHandler({ResponseStatusException.class, ErrorResponseException.class})
    public ResponseEntity<ProblemDetail> handleResponseStatus(Exception ex) {
        HttpStatus status;
        String detail;
        if (ex instanceof ResponseStatusException rse) {
            status = toHttpStatus(rse.getStatusCode());
            detail = (rse.getReason() != null && !rse.getReason().isBlank())
                    ? rse.getReason()
                    : status.getReasonPhrase();
        } else {
            var ere = (ErrorResponseException) ex;
            status = toHttpStatus(ere.getStatusCode());
            ProblemDetail pd = ere.getBody();
            detail = pd.getDetail() != null && !pd.getDetail().isBlank()
                    ? pd.getDetail()
                    : status.getReasonPhrase();
        }

        ProblemDetail body = pdf.create(
                status,
                status.is4xxClientError() ? ErrorCode.NOT_FOUND.name() : "ERROR",
                detail,
                Map.of("code", status.is4xxClientError() ? ErrorCode.NOT_FOUND.name() : "ERROR")
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleStatic404(NoResourceFoundException ex) {
        var status = HttpStatus.NOT_FOUND;
        var body = pdf.create(status, ErrorCode.NOT_FOUND.name(),
                "Static resource not found: " + ex.getMessage(),
                Map.of("code", ErrorCode.NOT_FOUND.name()));
        log.debug("Static 404: {}", ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    private static HttpStatus toHttpStatus(HttpStatusCode code) {
        return (code instanceof HttpStatus hs) ? hs : HttpStatus.valueOf(code.value());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAny(Exception ex) {
        if (ex instanceof NoResourceFoundException nrf) {
            return handleStatic404(nrf);
        }
        log.error("Unhandled exception", ex);
        var body = pdf.create(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private void logAtLevel(BusinessException ex, HttpStatus status) {
        if (status.is4xxClientError()) {
            if (status == HttpStatus.NOT_FOUND) {
                log.warn("{}: {}", ex.getCode(), ex.getMessage());
            } else {
                log.debug("{}: {}", ex.getCode(), ex.getMessage());
            }
        } else {
            log.error("{}: {}", ex.getCode(), ex.getMessage(), ex);
        }
    }

    private ResponseEntity<ProblemDetail> conflict(String title, String detail) {
        ProblemDetail body = pdf.create(HttpStatus.CONFLICT, title, detail, Map.of("code", title));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private String resolveDuplicateDetail(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Нарушено уникальное ограничение";
        }
        String u = raw.toLowerCase();
        if (u.contains("uq_books_author_title") || u.contains("books_author_title")) {
            return "Книга с таким названием у этого автора уже существует";
        }
        if (u.contains("uq_genres_name") || u.contains("genres") && u.contains("name")) {
            return "Жанр с таким названием уже существует";
        }
        if (u.contains("authors") && u.contains("full_name")) {
            return "Автор с таким именем уже существует";
        }
        return "Нарушено уникальное ограничение";
    }
}
