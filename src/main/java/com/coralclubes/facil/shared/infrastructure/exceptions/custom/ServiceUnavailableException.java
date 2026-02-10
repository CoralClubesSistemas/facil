package com.coralclubes.facil.shared.infrastructure.exceptions.custom;

import com.coralclubes.BaseException;
import com.coralclubes.responses.codes.GeneralResponseCode;

public class ServiceUnavailableException extends BaseException {
    public ServiceUnavailableException(String message) {
        super(GeneralResponseCode.SERVICE_UNAVAILABLE, message);
    }
}
