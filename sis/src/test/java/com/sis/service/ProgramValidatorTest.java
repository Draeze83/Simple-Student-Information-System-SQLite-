package com.sis.service;

import com.sis.model.Program;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProgramValidator")
class ProgramValidatorTest {

    private Program program(String code, String name, String collegeCode) {
        Program p = new Program();
        p.setCode(code);
        p.setName(name);
        p.setCollegeCode(collegeCode);
        return p;
    }

    private Program valid() {
        return program("BSCS", "Bachelor of Science in Computer Science", "CCS");
    }

    // ---------------------------------------------------------------- happy path

    @Test
    @DisplayName("valid program passes without exception")
    void valid_passes() {
        assertDoesNotThrow(() -> ProgramValidator.validate(valid()));
    }

    // ---------------------------------------------------------------- code

    @ParameterizedTest
    @ValueSource(strings = {"A", "BSE", "BSCHEMENG", "ABCDEFGHIJKL"})
    @DisplayName("valid codes (1-12 uppercase alphanumeric) are accepted")
    void code_valid(String code) {
        Program p = valid(); p.setCode(code);
        assertDoesNotThrow(() -> ProgramValidator.validate(p));
    }

    @Test
    @DisplayName("code longer than 12 chars is rejected")
    void code_tooLong() {
        Program p = valid(); p.setCode("ABCDEFGHIJKLM");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ProgramValidator.validate(p));
        assertTrue(ex.getMessage().toLowerCase().contains("code"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("blank code is rejected")
    void code_blank(String code) {
        Program p = valid(); p.setCode(code);
        assertThrows(IllegalArgumentException.class, () -> ProgramValidator.validate(p));
    }

    @Test
    @DisplayName("null code is rejected")
    void code_null() {
        Program p = valid(); p.setCode(null);
        assertThrows(IllegalArgumentException.class, () -> ProgramValidator.validate(p));
    }

    // ---------------------------------------------------------------- name

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("blank name is rejected")
    void name_blank(String name) {
        Program p = valid(); p.setName(name);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ProgramValidator.validate(p));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    @DisplayName("name of exactly 120 chars is accepted")
    void name_maxLength() {
        Program p = valid(); p.setName("A".repeat(120));
        assertDoesNotThrow(() -> ProgramValidator.validate(p));
    }

    @Test
    @DisplayName("name exceeding 120 chars is rejected")
    void name_tooLong() {
        Program p = valid(); p.setName("A".repeat(121));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ProgramValidator.validate(p));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    @DisplayName("null name is rejected")
    void name_null() {
        Program p = valid(); p.setName(null);
        assertThrows(IllegalArgumentException.class, () -> ProgramValidator.validate(p));
    }

    // ---------------------------------------------------------------- college code

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("blank college code is rejected")
    void collegeCode_blank(String cc) {
        Program p = valid(); p.setCollegeCode(cc);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ProgramValidator.validate(p));
        assertTrue(ex.getMessage().toLowerCase().contains("college"));
    }

    @Test
    @DisplayName("null college code is rejected")
    void collegeCode_null() {
        Program p = valid(); p.setCollegeCode(null);
        assertThrows(IllegalArgumentException.class, () -> ProgramValidator.validate(p));
    }

    @Test
    @DisplayName("valid college code is accepted")
    void collegeCode_valid() {
        Program p = valid(); p.setCollegeCode("CBAA");
        assertDoesNotThrow(() -> ProgramValidator.validate(p));
    }
}
