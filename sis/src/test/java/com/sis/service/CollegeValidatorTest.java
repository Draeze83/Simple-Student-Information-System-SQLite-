package com.sis.service;

import com.sis.model.College;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CollegeValidator")
class CollegeValidatorTest {

    private College college(String code, String name) {
        return new College(code, name);
    }

    // ---------------------------------------------------------------- happy path

    @Test
    @DisplayName("valid college passes without exception")
    void valid() {
        assertDoesNotThrow(() ->
                CollegeValidator.validate(college("CASS", "College of Arts & Social Sciences")));
    }

    // ---------------------------------------------------------------- code

    @ParameterizedTest
    @ValueSource(strings = {"A", "COE", "CBAA", "CCS1", "ABCDEFGHIJ"})
    @DisplayName("valid codes (1-10 uppercase alphanumeric) are accepted")
    void code_valid(String code) {
        assertDoesNotThrow(() -> CollegeValidator.validate(college(code, "Name")));
    }

    @Test
    @DisplayName("code longer than 10 chars is rejected")
    void code_tooLong() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CollegeValidator.validate(college("ABCDEFGHIJK", "Name")));
        assertTrue(ex.getMessage().toLowerCase().contains("code"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("blank code is rejected")
    void code_blank(String code) {
        assertThrows(IllegalArgumentException.class,
                () -> CollegeValidator.validate(college(code, "Name")));
    }

    @Test
    @DisplayName("null code is rejected")
    void code_null() {
        assertThrows(IllegalArgumentException.class,
                () -> CollegeValidator.validate(college(null, "Name")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"col lege", "COE!", "C-O-E", "coe"})
    @DisplayName("codes with spaces, symbols, or lowercase are rejected")
    void code_invalidChars(String code) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CollegeValidator.validate(college(code, "Name")));
        assertTrue(ex.getMessage().toLowerCase().contains("code"));
    }

    // ---------------------------------------------------------------- name

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("blank name is rejected")
    void name_blank(String name) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CollegeValidator.validate(college("COE", name)));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    @DisplayName("name of exactly 100 chars is accepted")
    void name_maxLength() {
        assertDoesNotThrow(() ->
                CollegeValidator.validate(college("COE", "A".repeat(100))));
    }

    @Test
    @DisplayName("name exceeding 100 chars is rejected")
    void name_tooLong() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CollegeValidator.validate(college("COE", "A".repeat(101))));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    @DisplayName("null name is rejected")
    void name_null() {
        assertThrows(IllegalArgumentException.class,
                () -> CollegeValidator.validate(college("COE", null)));
    }
}
