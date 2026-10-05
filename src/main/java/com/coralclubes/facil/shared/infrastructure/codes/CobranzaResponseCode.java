package com.coralclubes.facil.shared.infrastructure.codes;

import com.coralclubes.responses.BaseResponseCode;

public enum CobranzaResponseCode implements BaseResponseCode {
    SUPERATED_MAX_DISCOUNT("SUP_DISC", "El descuento aplicado supera el máximo permitido.", 400);

    private final String code;
    private final String message;
    private final int status;

    CobranzaResponseCode(String code, String message, int status) {
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
