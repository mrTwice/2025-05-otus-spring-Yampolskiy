package ru.otus.java.springframework.yampolskiy.exceptions;

public class CommentForbiddenException extends BusinessException {
    public CommentForbiddenException(String message) {
        super(ErrorCode.COMMENT_FORBIDDEN, message);
    }

    public CommentForbiddenException(String message, Throwable cause) {
        super(ErrorCode.COMMENT_FORBIDDEN, message, cause);
    }
}
