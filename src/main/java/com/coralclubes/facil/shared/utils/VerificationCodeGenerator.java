package com.coralclubes.facil.shared.utils;

import java.security.SecureRandom;

public final class VerificationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String NUMERIC_CHARACTERS = "0123456789";
    private static final String ALPHABETIC_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC_CHARACTERS = ALPHABETIC_CHARACTERS + NUMERIC_CHARACTERS;

    private VerificationCodeGenerator() {
        // Utility class
    }

    public static String generateAlphabeticCode(int length) {
        return generateCode(length, ALPHABETIC_CHARACTERS);
    }

    public static String generateNumericCode(int length) {
        return generateCode(length, NUMERIC_CHARACTERS);
    }

    public static String generateAlphanumericCode(int length) {
        return generateCode(length, ALPHANUMERIC_CHARACTERS);
    }

    private static String generateCode(int length, String characters) {
        if (length <= 0) {
            throw new IllegalArgumentException("La longitud debe ser mayor a 0");
        }

        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(RANDOM.nextInt(characters.length())));
        }

        return code.toString();
    }
}