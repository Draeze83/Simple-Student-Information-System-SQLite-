package com.sis.service;

import com.sis.model.Student;

import java.util.regex.Pattern;

/**
 * Validates {@link Student} field values before they reach the DAO layer.
 */
public class StudentValidator {

    // YYYY-NNNN  e.g. 2024-0001
    private static final Pattern ID_PATTERN =
            Pattern.compile("^\\d{4}-\\d{4}$");

    private static final int MAX_NAME_LENGTH = 60;
    private static final int MIN_YEAR        = 1;
    private static final int MAX_YEAR        = 5;

    private StudentValidator() {}

    public static void validate(Student s) {
        validateId(s.getId());
        validateName("First name", s.getFirstname());
        validateName("Last name",  s.getLastname());
        validateYear(s.getYear());
        validateGender(s.getGender());
        validateProgramId(s.getProgramId());
    }

    // ---------------------------------------------------------------- helpers

    private static void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        if (!ID_PATTERN.matcher(id.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Student ID must follow the format YYYY-NNNN " +
                    "(e.g. 2024-0001). Got: \"" + id.trim() + "\".");
        }
    }

    private static void validateName(String label, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank.");
        }
        if (value.trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    label + " must not exceed " + MAX_NAME_LENGTH +
                    " characters (got " + value.trim().length() + ").");
        }
    }

    private static void validateYear(int year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException(
                    "Year level must be between " + MIN_YEAR +
                    " and " + MAX_YEAR + " (got " + year + ").");
        }
    }

    private static void validateGender(String gender) {
        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("Gender must not be blank.");
        }
    }

    private static void validateProgramId(int programId) {
        if (programId <= 0) {
            throw new IllegalArgumentException("A valid program must be selected.");
        }
    }
}
