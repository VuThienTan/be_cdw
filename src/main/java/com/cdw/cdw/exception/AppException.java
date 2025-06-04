package com.cdw.cdw.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final String messageKey;
    private final HttpStatus httpStatus;

    public AppException(String messageKey, HttpStatus httpStatus) {
        super(messageKey);
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }


    public static AppException badRequest(String messageKey) {
        return new AppException(messageKey, HttpStatus.BAD_REQUEST);
    }

    public static AppException notFound(String messageKey) {
        return new AppException(messageKey, HttpStatus.NOT_FOUND);
    }

    public static AppException forbidden(String messageKey) {
        return new AppException(messageKey, HttpStatus.FORBIDDEN);
    }

    public static AppException unauthorized(String messageKey) {
        return new AppException(messageKey, HttpStatus.UNAUTHORIZED);
    }

    public static AppException serverError(String messageKey) {
        return new AppException(messageKey, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
