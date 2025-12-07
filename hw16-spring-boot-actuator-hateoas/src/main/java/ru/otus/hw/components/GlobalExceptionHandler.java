package ru.otus.hw.components;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.otus.hw.exceptions.BusinessException;
import ru.otus.hw.exceptions.ErrorCode;

import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorCodeHttpStatusMapper statusMapper;

    private final ErrorResponseNegotiator negotiator;

    private final ValidationErrorExtractor valExtractor;

    @ExceptionHandler(BusinessException.class)
    public Object handleBusiness(BusinessException ex, HttpServletRequest req) {
        HttpStatus status = statusMapper.toStatus(ex.getCode());
        logAtLevel(ex, status);

        var params = ErrorResponseParams.builder(status)
                .title(ex.getCode().name())
                .detail(ex.getMessage())
                .props(Map.of("code", ex.getCode().name()))
                .build();

        return negotiator.respond(req, params);
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public Object handleBinding(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        var br = (ex instanceof MethodArgumentNotValidException manve)
                ? manve.getBindingResult()
                : ((BindException) ex).getBindingResult();

        var params = p(
                status,
                "error/400",
                ErrorCode.VALIDATION.name(),
                "Validation failed",
                Map.of(
                        "code", ErrorCode.VALIDATION.name(),
                        "errors", valExtractor.toList(br)
                )
        );
        return negotiator.respond(req, params);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            org.springframework.beans.TypeMismatchException.class
    })
    public Object handleTypeMismatch(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Invalid parameter value";
        if (ex instanceof MethodArgumentTypeMismatchException mm) {
            String name = mm.getName();
            String value = String.valueOf(mm.getValue());
            String required = (mm.getRequiredType() != null) ? mm.getRequiredType().getSimpleName() : "required type";
            msg = "Parameter '%s' has invalid value '%s' (expected %s)".formatted(name, value, required);
        }

        var params = p(
                status,
                "error/400",
                ErrorCode.VALIDATION.name(),
                msg,
                Map.of("code", ErrorCode.VALIDATION.name())
        );
        return negotiator.respond(req, params);
    }


    @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
    public Object handleNotFound404(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String msg = (ex instanceof NoHandlerFoundException nhf)
                ? "Page not found: " + nhf.getRequestURL()
                : (ex instanceof NoResourceFoundException nrf
                ? "Resource not found: " + nrf.getResourcePath()
                : "Not found");
        log.warn("404 Not Found: {}", msg);

        var params = p(
                status,
                "error/404",
                ErrorCode.NOT_FOUND.name(),
                msg,
                Map.of("code", ErrorCode.NOT_FOUND.name())
        );
        return negotiator.respond(req, params);
    }


    @ExceptionHandler(Exception.class)
    public Object handleAny(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        var params = p(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "error/500",
                "INTERNAL_SERVER_ERROR",
                "Internal server error",
                Map.of()
        );
        return negotiator.respond(req, params);
    }


    private ErrorResponseParams p(HttpStatus status, String view, String title, String detail, Map<String, ?> props) {
        return ErrorResponseParams.builder(status)
                .view(view)
                .title(title)
                .detail(detail)
                .props(props == null ? Map.of() : props)
                .build();
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
}