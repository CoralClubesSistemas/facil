package com.coralclubes.facil.shared.infrastructure.exceptions.custom;

import com.coralclubes.BaseException;
import com.coralclubes.responses.codes.AuthResponseCode;

public class UsernameNotFound extends BaseException {
    public UsernameNotFound(String message) {
        super(AuthResponseCode.USER_NOT_FOUND, message);
    }
}
