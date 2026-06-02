package com.coralclubes.facil.shared.infrastructure.codes;

import com.coralclubes.responses.BaseResponseCode;

public enum LoginResponseCode implements BaseResponseCode {
    LOGIN_AUTHORIZATION_SUCCESS("LOGIN_AUTHORIZATION_SUCCESS", "Authorization successful", 200),
    LOGIN_NOT_PERMITIONS("LOGIN_NOT_PERMITIONS", "The user does not have permissions for this action", 403),
    LOGIN_MODULES_CONSTRAINT("LOGIN_MODULES_CONSTRAINT", "Los modulos del usuario han sido construidos correctamente", 200),
    NO_WEB_REGISTRATION("NO_WEB_REGISTRATION", "El usuario no tiene registro web", 400),
    LOGIN_AUTHORIZATION_FAILURE("LOGIN_AUTHORIZATION_FAILURE", "Authorization failed", 401);

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
