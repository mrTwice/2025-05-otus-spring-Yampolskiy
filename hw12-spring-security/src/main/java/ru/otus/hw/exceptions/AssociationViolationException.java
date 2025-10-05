package ru.otus.hw.exceptions;

public class AssociationViolationException extends BusinessException {
    public AssociationViolationException(String message) {
        super(ErrorCode.ASSOCIATION_VIOLATION, message);
    }

    public AssociationViolationException(String message, Throwable cause) {
        super(ErrorCode.ASSOCIATION_VIOLATION, message, cause);
    }
}
