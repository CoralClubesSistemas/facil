package com.coralclubes.facil.shared.infrastructure.exceptions.custom;

import com.coralclubes.BaseException;
import com.coralclubes.responses.codes.AuthResponseCode;

public class TokenExpiredException extends BaseException {
    public TokenExpiredException(String message) {
        super(AuthResponseCode.TOKEN_EXPIRED, message);
    }
}