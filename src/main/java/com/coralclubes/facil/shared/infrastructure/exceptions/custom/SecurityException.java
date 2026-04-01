package com.coralclubes.facil.shared.infrastructure.exceptions.custom;

import com.coralclubes.BaseException;
import com.coralclubes.responses.codes.AuthResponseCode;

public class SecurityException extends BaseException {
    public SecurityException(String message) {
        super(AuthResponseCode.TOKEN_INVALID, message);
    }
}
