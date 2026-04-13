package com.sis.service;

import com.sis.model.College;

import java.util.regex.Pattern;

/**
 * Validates {@link College} field values before they reach the DAO layer.
 * Rules mirror what {@code CollegeDialog} already enforces in the UI,
 * giving the service layer a defence-in-depth second check.
 */
public class CollegeValidator {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9]{1,10}$");
    private static final int MAX_NAME_LENGTH = 100;

    private CollegeValidator() {}

    public static void validate(College c) {
        validateCode(c.getCode());
        validateName(c.getName());
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("College code is required.");
        String trimmed = code.trim();
        if (!CODE_PATTERN.matcher(trimmed).matches())
            throw new IllegalArgumentException(
                    "College code must be 1–10 uppercase letters/digits (got \"" + trimmed + "\").");
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("College name is required.");
        if (name.trim().length() > MAX_NAME_LENGTH)
            throw new IllegalArgumentException(
                    "College name must not exceed " + MAX_NAME_LENGTH + " characters.");
    }
}
