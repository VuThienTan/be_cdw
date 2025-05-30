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
    MENU_ITEM_NOT_FOUND(1002, "Menu item not found", HttpStatus.BAD_REQUEST),
    INGREDIENT_NOT_FOUND(1002, "Ingredient not found", HttpStatus.BAD_REQUEST),
    ACTIVE_CODE_NOT_FOUND(1002, "Active code not found", HttpStatus.BAD_REQUEST),

    //Valid
    INVALID_EMAIL(1003, "Invalid email", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1003, "Password must be between 8 and 20 characters", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1003, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER(1003, "Invalid phone number", HttpStatus.BAD_REQUEST),

    //Feild Empty
    USERNAME_IS_REQUIRED(1004, "Username is empty", HttpStatus.BAD_REQUEST),
    PASSWORD_IS_REQUIRED(1004, "Password is empty", HttpStatus.BAD_REQUEST),
    NAME_IS_REQUIRED(1004, "Name is empty", HttpStatus.BAD_REQUEST),
    DESCRIPTION_IS_REQUIRED(1004, "Descriptions is empty", HttpStatus.BAD_REQUEST),
    PERMISSION_IS_REQUIRED(1004, "Permissions is empty", HttpStatus.BAD_REQUEST),

    //Auth
    UNAUTHENTICATED(1005, "User not authenticated", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1006, "User not permission", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(1007, "token expired", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR(1008, "Internal server error", HttpStatus.BAD_REQUEST),
    //    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    CART_ITEM_NOT_FOUND(1001, "Cart item not found", HttpStatus.NOT_FOUND),
    MENU_ITEM_NOT_AVAILABLE(10002, "Menu item is not available", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(1002, "Invalid quantity", HttpStatus.BAD_REQUEST),

    EMAIL_SENDING_ERROR(1009, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TOKEN(1003, "Invalid or expired token", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH(1004, "Passwords do not match", HttpStatus.BAD_REQUEST),

    TIME_EXPIRED(1005, "Time expired", HttpStatus.BAD_REQUEST),
    ACCOUNT_IS_ACTIVE(1006, "Account is active", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_ACTIVE(1006, "Account isn't active", HttpStatus.BAD_REQUEST),
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
