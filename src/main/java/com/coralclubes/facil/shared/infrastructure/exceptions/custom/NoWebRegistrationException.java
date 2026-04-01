package com.coralclubes.facil.shared.infrastructure.exceptions.custom;

import com.coralclubes.BaseException;
import com.coralclubes.responses.codes.AuthResponseCode;

public class NoWebRegistrationException extends BaseException {
    public NoWebRegistrationException(String message) {
        super(AuthResponseCode.ACCOUNT_NOT_VERIFIED, message);
    }
}
