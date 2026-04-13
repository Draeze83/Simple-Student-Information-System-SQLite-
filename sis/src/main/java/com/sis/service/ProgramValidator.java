package com.sis.service;

import com.sis.model.Program;

import java.util.regex.Pattern;

/**
 * Validates {@link Program} field values before they reach the DAO layer.
 */
public class ProgramValidator {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9]{1,12}$");
    private static final int MAX_NAME_LENGTH        = 120;
    private static final int MAX_COLLEGE_CODE_LEN   = 10;

    private ProgramValidator() {}

    public static void validate(Program p) {
        validateCode(p.getCode());
        validateName(p.getName());
        validateCollegeCode(p.getCollegeCode());
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("Program code is required.");
        String upper = code.trim().toUpperCase();
        if (!CODE_PATTERN.matcher(upper).matches())
            throw new IllegalArgumentException(
                    "Program code must be 1–12 uppercase letters/digits (got \"" + upper + "\").");
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Program name is required.");
        if (name.trim().length() > MAX_NAME_LENGTH)
            throw new IllegalArgumentException(
                    "Program name must not exceed " + MAX_NAME_LENGTH + " characters.");
    }

    private static void validateCollegeCode(String collegeCode) {
        if (collegeCode == null || collegeCode.isBlank())
            throw new IllegalArgumentException("A college must be selected for this program.");
        if (collegeCode.trim().length() > MAX_COLLEGE_CODE_LEN)
            throw new IllegalArgumentException("College code is invalid.");
    }
}
