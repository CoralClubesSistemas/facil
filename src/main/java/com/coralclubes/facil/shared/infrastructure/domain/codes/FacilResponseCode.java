package com.coralclubes.facil.shared.infrastructure.domain.codes;

import com.coralclubes.responses.BaseResponseCode;

public enum FacilResponseCode implements BaseResponseCode {
    GENERIC_ERROR("FACIL-0001", "An unexpected error occurred", 500),
    RESOURCE_NOT_FOUND("FACIL-0002", "The requested resource was not found", 404),
    INVALID_REQUEST("FACIL-0003", "The request is invalid", 400);

    private final String code;
    private final String message;
    private final int status;

    FacilResponseCode(String code, String message, int status) {
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
