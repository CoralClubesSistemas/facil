package com.coralclubes.facil.shared.infrastructure.codes;

import com.coralclubes.responses.BaseResponseCode;

public enum JwtResponseCode implements BaseResponseCode {
    JWT_SUCCESS("JWT_SUCCESS", "JWT generado exitosamente", 200),
    JWT_INVALID("JWT_INVALID", "JWT inválido", 401),
    JWT_EXPIRED("JWT_EXPIRED", "JWT expirado", 401),
    JWT_MISSING("JWT_MISSING", "JWT faltante", 400),

    JWT_REFRESH_SUCCESS("JWT_REFRESH_SUCCESS", "JWT refrescado exitosamente", 200);

    private final String code;
    private final String message;
    private final Integer status;

    JwtResponseCode(String code, String message, Integer status) {
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
