package com.coralclubes.facil.shared.infrastructure.exceptions.custom;

import com.coralclubes.BaseException;
import com.coralclubes.responses.codes.AuthResponseCode;

public class NoPermissionsException extends BaseException {
    public NoPermissionsException(String message) {
        super(AuthResponseCode.ACCOUNT_LOCKED, message);
    }
}
