package com.coralclubes.facil.shared.infrastructure.codes;

import com.coralclubes.responses.BaseResponseCode;

public enum LoginResponseCode implements BaseResponseCode {
    LOGIN_SUCCESS("LOGIN_SUCCESS", "Login successful", 200),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid username or password", 401),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Account is locked", 403),
    INTERNAL_ERROR("INTERNAL_ERROR", "An internal error occurred", 500),

    LOGIN_MODULES_CONSTRAINT("LOGIN_MODULES_CONSTRAINT", "El usuario no tiene modulos asignados", 400),

    LOGIN_AUTHORIZATION_SUCCESS("LOGIN_AUTHORIZATION_SUCCESS", "Authorization successful", 200),
    LOGIN_NOT_PERMITIONS("LOGIN_NOT_PERMITIONS", "The user does not have permissions for this action", 403);

    private final String code;
    private final String message;
    private final Integer status;

    LoginResponseCode(String code, String message, Integer status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getStatus() {
        return status;
    }
}
