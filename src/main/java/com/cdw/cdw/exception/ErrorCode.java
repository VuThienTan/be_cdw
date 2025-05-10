package com.cdw.cdw.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "User not found", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1001, "User not existed", HttpStatus.NOT_FOUND),
    USER_EXISTED(1001, "User existed", HttpStatus.BAD_REQUEST),
    MENU_ITEM_ALREADY_EXISTS(1001, "Menu item existed", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1002, "Email already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1002, "Category not found", HttpStatus.BAD_REQUEST),

    //Valid
    INVALID_EMAIL(1003, "Invalid email", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1003, "Password must be between 8 and 20 characters", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1003, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER(1003, "Invalid phone number", HttpStatus.BAD_REQUEST),
    //Feild Empty
    USERNAME_IS_REQUIRED(1004, "Username is empty", HttpStatus.BAD_REQUEST),
    PASSWORD_IS_REQUIRED(1004, "Password is empty", HttpStatus.BAD_REQUEST),

    //Auth
    UNAUTHENTICATED(1005, "User not authenticated", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1006, "User not permission", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(1007,"token expired", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR(1008, "Internal server error", HttpStatus.BAD_REQUEST),
//    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    ;
    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

}
