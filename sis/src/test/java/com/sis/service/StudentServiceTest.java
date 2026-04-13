package com.sis.service;

import com.sis.model.College;
import com.sis.model.Program;
import com.sis.model.Student;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link StudentService}.
 */
@DisplayName("StudentService")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentServiceTest extends ServiceIntegrationBase {

    // Instantiated in @BeforeEach (after DatabaseManager is initialized)
    private CollegeService collegeService;
    private ProgramService programService;
    private StudentService studentService;

    private int programId;

    @BeforeEach
    void seedCollegeAndProgram() {
        collegeService  = new CollegeService();
        programService  = new ProgramService();
        studentService  = new StudentService();

        // Clear seeder data so tests start from a known empty state
        clearAllData();

        collegeService.addCollege(new College("CCS", "College of Computer Studies"));
        Program p = new Program();
        p.setCode("BSCS"); p.setName("BS Computer Science"); p.setCollegeCode("CCS");
        programService.addProgram(p);
        programId = programService.getAllPrograms().get(0).getId();
    }

    /** Deletes all rows so seeded data doesn’t interfere with test expectations. */
    private static void clearAllData() {
        try (java.sql.Connection c = com.sis.db.DatabaseManager.getInstance().getConnection();
             java.sql.Statement  s = c.createStatement()) {
            s.execute("DELETE FROM student");
            s.execute("DELETE FROM program");
            s.execute("DELETE FROM college");
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to clear test data", e);
        }
    }

    private Student buildStudent(String id) {
        Student s = new Student();
        s.setId(id);
        s.setFirstname("Juan");
        s.setLastname("Santos");
        s.setProgramId(programId);
        s.setYear(1);
        s.setGender("Male");
        return s;
    }

    // ---------------------------------------------------------------- CREATE

    @Test
    @Order(1)
    @DisplayName("addStudent — valid student is persisted and readable")
    void addStudent_valid() {
        studentService.addStudent(buildStudent("2024-0001"));

        Student found = studentService.getStudent("2024-0001");
        assertNotNull(found);
        assertEquals("2024-0001", found.getId());
        assertEquals("Juan",      found.getFirstname());
        assertEquals("Santos",    found.getLastname());
    }

    @Test
    @Order(2)
    @DisplayName("addStudent — invalid ID format throws IllegalArgumentException")
    void addStudent_invalidId() {
        Student bad = buildStudent("BADIDFMT");
        assertThrows(IllegalArgumentException.class, () -> studentService.addStudent(bad));
    }

    @Test
    @Order(3)
    @DisplayName("addStudent — duplicate ID throws ServiceException")
    void addStudent_duplicateId() {
        studentService.addStudent(buildStudent("2024-0001"));
        assertThrows(ServiceException.class,
                () -> studentService.addStudent(buildStudent("2024-0001")));
    }

    @Test
    @Order(4)
    @DisplayName("addStudent — invalid programId throws IllegalArgumentException")
    void addStudent_invalidProgram() {
        Student bad = buildStudent("2024-0099");
        bad.setProgramId(0);   // <= 0 is invalid per StudentValidator
        assertThrows(IllegalArgumentException.class, () -> studentService.addStudent(bad));
    }

    // ---------------------------------------------------------------- UPDATE

    @Test
    @Order(5)
    @DisplayName("updateStudent — field changes are persisted")
    void updateStudent_changes() {
        studentService.addStudent(buildStudent("2024-0001"));

        Student s = studentService.getStudent("2024-0001");
        assertNotNull(s);
        s.setFirstname("Jose");
        s.setYear(3);
        studentService.updateStudent(s);

        Student updated = studentService.getStudent("2024-0001");
        assertNotNull(updated);
        assertEquals("Jose", updated.getFirstname());
        assertEquals(3,      updated.getYear());
    }

    // ---------------------------------------------------------------- DELETE

    @Test
    @Order(6)
    @DisplayName("deleteStudent — student is no longer findable after delete")
    void deleteStudent_removed() {
        studentService.addStudent(buildStudent("2024-0001"));
        studentService.deleteStudent("2024-0001");
        assertNull(studentService.getStudent("2024-0001"));
    }

    @Test
    @Order(7)
    @DisplayName("deleteStudent — non-existent ID throws ServiceException")
    void deleteStudent_notFound() {
        assertThrows(ServiceException.class, () -> studentService.deleteStudent("9999-9999"));
    }

    // ---------------------------------------------------------------- LIST / COUNT / GENERATE ID

    @Test
    @Order(8)
    @DisplayName("countStudents / getStudents — search filters by lastname")
    void getStudents_search() {
        studentService.addStudent(buildStudent("2024-0001"));   // Santos
        Student s2 = buildStudent("2024-0002");
        s2.setLastname("Reyes");
        studentService.addStudent(s2);

        assertEquals(1, studentService.countStudents("Reyes"));
        List<Student> page = studentService.getStudents(0, 10, "lastname", true, "Reyes");
        assertEquals(1, page.size());
        assertEquals("Reyes", page.get(0).getLastname());
    }

    @Test
    @Order(9)
    @DisplayName("generateNextId — returns YYYY-0001 when no students exist for that year")
    void generateNextId_empty() {
        String id = studentService.generateNextId(2025);
        assertEquals("2025-0001", id);
    }

    @Test
    @Order(10)
    @DisplayName("generateNextId — increments correctly after existing IDs")
    void generateNextId_increments() {
        studentService.addStudent(buildStudent("2024-0001"));
        studentService.addStudent(buildStudent("2024-0002"));

        String next = studentService.generateNextId(2024);
        assertEquals("2024-0003", next);
    }
}
