package com.sis.service;

import com.sis.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StudentValidator")
class StudentValidatorTest {

    private Student valid;

    @BeforeEach
    void setUp() {
        valid = new Student();
        valid.setId("2024-0001");
        valid.setFirstname("Maria");
        valid.setLastname("Santos");
        valid.setProgramId(1);
        valid.setYear(1);
        valid.setGender("Female");
    }

    // ---------------------------------------------------------------- happy path

    @Test
    @DisplayName("valid student passes without exception")
    void validStudent_noException() {
        assertDoesNotThrow(() -> StudentValidator.validate(valid));
    }

    // ---------------------------------------------------------------- ID format

    @ParameterizedTest
    @ValueSource(strings = {"2024-0001", "2019-9999", "2000-0000"})
    @DisplayName("valid IDs are accepted")
    void id_valid(String id) {
        valid.setId(id);
        assertDoesNotThrow(() -> StudentValidator.validate(valid));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "24-0001", "2024-001", "2024-00001",
                            "ABCD-0001", "2024_0001", "2024-000A"})
    @DisplayName("malformed IDs are rejected")
    void id_invalid(String id) {
        valid.setId(id);
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
        assertTrue(ex.getMessage().toLowerCase().contains("id"));
    }

    @Test
    @DisplayName("null ID is rejected")
    void id_null() {
        valid.setId(null);
        assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
    }

    // ---------------------------------------------------------------- first name

    @Test
    @DisplayName("blank first name is rejected")
    void firstName_blank() {
        valid.setFirstname("  ");
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
        assertTrue(ex.getMessage().toLowerCase().contains("first"));
    }

    @Test
    @DisplayName("first name exceeding 60 chars is rejected")
    void firstName_tooLong() {
        valid.setFirstname("A".repeat(61));
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
        assertTrue(ex.getMessage().toLowerCase().contains("first"));
    }

    @Test
    @DisplayName("first name of exactly 60 chars is accepted")
    void firstName_maxLength() {
        valid.setFirstname("A".repeat(60));
        assertDoesNotThrow(() -> StudentValidator.validate(valid));
    }

    // ---------------------------------------------------------------- last name

    @Test
    @DisplayName("blank last name is rejected")
    void lastName_blank() {
        valid.setLastname("");
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
        assertTrue(ex.getMessage().toLowerCase().contains("last"));
    }

    @Test
    @DisplayName("last name exceeding 60 chars is rejected")
    void lastName_tooLong() {
        valid.setLastname("B".repeat(61));
        assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
    }

    // ---------------------------------------------------------------- year level

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("year levels 1-5 are accepted")
    void year_valid(int year) {
        valid.setYear(year);
        assertDoesNotThrow(() -> StudentValidator.validate(valid));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 100})
    @DisplayName("year levels outside 1-5 are rejected")
    void year_outOfRange(int year) {
        valid.setYear(year);
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
        assertTrue(ex.getMessage().toLowerCase().contains("year"));
    }

    // ---------------------------------------------------------------- gender

    @Test
    @DisplayName("blank gender is rejected")
    void gender_blank() {
        valid.setGender("  ");
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
        assertTrue(ex.getMessage().toLowerCase().contains("gender"));
    }

    // ---------------------------------------------------------------- program ID

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -999})
    @DisplayName("invalid program IDs are rejected")
    void programId_invalid(int pid) {
        valid.setProgramId(pid);
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> StudentValidator.validate(valid));
        assertTrue(ex.getMessage().toLowerCase().contains("program"));
    }

    @Test
    @DisplayName("positive program ID is accepted")
    void programId_valid() {
        valid.setProgramId(42);
        assertDoesNotThrow(() -> StudentValidator.validate(valid));
    }
}
