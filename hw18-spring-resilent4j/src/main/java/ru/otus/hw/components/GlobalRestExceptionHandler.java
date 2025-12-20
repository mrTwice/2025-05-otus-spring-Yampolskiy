package ru.otus.hw.components;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
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

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ProblemDetail> handleBinding(Exception ex) {
        BindingResult br = (ex instanceof MethodArgumentNotValidException manve)
                ? manve.getBindingResult()
                : ((BindException) ex).getBindingResult();

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail body = pdf.create(status, ErrorCode.VALIDATION.name(), "Validation failed",
                Map.of("code", ErrorCode.VALIDATION.name(), "errors", valExtractor.toList(br)));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            org.springframework.beans.TypeMismatchException.class
    })
    public ResponseEntity<ProblemDetail> handleTypeMismatch(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Invalid parameter value";
        if (ex instanceof MethodArgumentTypeMismatchException mm) {
            String name = mm.getName();
            String value = String.valueOf(mm.getValue());
            String required = (mm.getRequiredType() != null) ? mm.getRequiredType().getSimpleName() : "required type";
            msg = "Parameter '%s' has invalid value '%s' (expected %s)".formatted(name, value, required);
        }
        ProblemDetail body = pdf.create(status, ErrorCode.VALIDATION.name(), msg,
                Map.of("code", ErrorCode.VALIDATION.name()));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound404(NoHandlerFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemDetail body = pdf.create(status, ErrorCode.NOT_FOUND.name(),
                "Page not found: " + ex.getRequestURL(),
                Map.of("code", ErrorCode.NOT_FOUND.name()));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAny(Exception ex) {
        log.error("Unhandled exception", ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail body = pdf.create(status, "INTERNAL_SERVER_ERROR", "Internal server error");
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleHibernateConstraint(ConstraintViolationException ex) {

        String constraintName = ex.getConstraintName();
        String sqlState = (ex.getSQLException() != null) ? ex.getSQLException().getSQLState() : null;
        String rawMsg = rootMessage(ex);

        return buildConstraintProblem(constraintName, sqlState, rawMsg);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof ConstraintViolationException hce) {
            return handleHibernateConstraint(hce);
        }

        return conflict(ErrorCode.CONFLICT.name(), rootMessage(ex));
    }


    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ProblemDetail> handlePersistence(PersistenceException ex) {
        Throwable root = rootCause(ex);
        if (root instanceof ConstraintViolationException hce) {
            return handleHibernateConstraint(hce);
        }
        return conflict(ErrorCode.CONFLICT.name(), rootMessage(ex));
    }

    @ExceptionHandler({
            OptimisticLockException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ProblemDetail> handleOptimistic(Exception ex) {
        return conflict(ErrorCode.CONFLICT.name(), "Конфликт версий (объект был изменён конкурентно)");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleStatic404(NoResourceFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemDetail body = pdf.create(status, ErrorCode.NOT_FOUND.name(),
                "Page not found: " + ex.getResourcePath(),
                Map.of("code", ErrorCode.NOT_FOUND.name()));
        return ResponseEntity.status(status).body(body);
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

    private ResponseEntity<ProblemDetail> buildConstraintProblem(
            String constraintName, String sqlState, String rawMsg) {

        HttpStatus status = HttpStatus.CONFLICT;
        String title = resolveTitle(constraintName, sqlState, rawMsg);
        String detail = resolveDetail(constraintName, rawMsg, title);

        ProblemDetail body = pdf.create(status, title, detail, Map.of("code", title));
        return ResponseEntity.status(status).body(body);
    }

    private String resolveTitle(String constraintName, String sqlState, String rawMsg) {
        String upperMsg = rawMsg == null ? "" : rawMsg.toUpperCase();
        String upperCn  = constraintName == null ? "" : constraintName.toUpperCase();

        if ("23505".equals(sqlState)
                || containsIgnoreCase(rawMsg, "unique")
                || upperMsg.contains("UQ_")
                || upperCn.contains("UQ_")
                || upperCn.contains("UNIQUE")) {
            return ErrorCode.DUPLICATE.name();
        }
        return ErrorCode.CONFLICT.name();
    }

    private String resolveDetail(String constraintName, String rawMsg, String currentTitle) {
        String cnUpper = (constraintName == null) ? null : constraintName.toUpperCase();
        String rawUpper = (rawMsg == null) ? "" : rawMsg.toUpperCase();

        if (cnUpper != null) {
            return detailByConstraintName(cnUpper, currentTitle, rawMsg);
        }
        return detailByMessage(rawUpper, currentTitle, rawMsg);
    }

    private String detailByConstraintName(String cnUpper, String title, String rawMsg) {
        if (cnUpper.contains("UQ_BOOKS_AUTHOR_TITLE")) {
            return "Книга с таким названием у этого автора уже существует";
        }
        if (cnUpper.contains("FK_BOOKS_AUTHOR_NO_CASCADE")) {
            return "Автор не найден или не может быть использован";
        }
        if (cnUpper.contains("FK_BG_GENRE_NO_CASCADE")) {
            return "Один или несколько жанров не найдены";
        }
        return rawMsg;
    }

    private String detailByMessage(String rawUpper, String title, String rawMsg) {
        if (rawUpper.contains("UQ_BOOKS_AUTHOR_TITLE")) {
            return "Книга с таким названием у этого автора уже существует";
        }
        if (ErrorCode.DUPLICATE.name().equals(title)) {
            return "Нарушено уникальное ограничение";
        }
        return (rawMsg == null || rawMsg.isBlank()) ? "Нарушение ограничения" : rawMsg;
    }


    private ResponseEntity<ProblemDetail> conflict(String title, String detail) {
        ProblemDetail body = pdf.create(HttpStatus.CONFLICT, title, detail, Map.of("code", title));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }

    private static String rootMessage(Throwable t) {
        Throwable r = rootCause(t);
        String msg = r.getMessage();
        return (msg != null && !msg.isBlank()) ? msg : t.toString();
    }

    private static boolean containsIgnoreCase(String s, String needle) {
        return s != null && needle != null && s.toLowerCase().contains(needle.toLowerCase());
    }
}
