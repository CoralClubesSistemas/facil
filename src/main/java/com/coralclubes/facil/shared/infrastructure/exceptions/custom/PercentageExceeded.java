package com.coralclubes.facil.shared.infrastructure.exceptions.custom;

import com.coralclubes.BaseException;
import com.coralclubes.facil.shared.infrastructure.codes.CobranzaResponseCode;

public class PercentageExceeded extends BaseException {
    public PercentageExceeded(String message) {
        super((CobranzaResponseCode.SUPERATED_MAX_DISCOUNT), message);
    }
}
