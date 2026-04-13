package com.sis.service;

import com.sis.model.College;
import com.sis.model.Program;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link ProgramService}.
 */
@DisplayName("ProgramService")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProgramServiceTest extends ServiceIntegrationBase {

    // Instantiated in @BeforeEach (after DatabaseManager is initialized)
    private CollegeService collegeService;
    private ProgramService programService;

    /** A college that exists for all tests in this class. */
    private static final College TEST_COLLEGE = new College("CCS", "College of Computer Studies");

    @BeforeEach
    void seedCollege() {
        collegeService = new CollegeService();
        programService = new ProgramService();
        clearAllData();
        collegeService.addCollege(TEST_COLLEGE);
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

    private Program buildProgram(String code, String name) {
        Program p = new Program();
        p.setCode(code);
        p.setName(name);
        p.setCollegeCode("CCS");
        return p;
    }

    // ---------------------------------------------------------------- CREATE

    @Test
    @Order(1)
    @DisplayName("addProgram — valid program is persisted and readable via list")
    void addProgram_valid() {
        programService.addProgram(buildProgram("BSCS", "Bachelor of Science in Computer Science"));

        List<Program> all = programService.getAllPrograms();
        assertEquals(1, all.size());
        assertEquals("BSCS", all.get(0).getCode());
    }

    @Test
    @Order(2)
    @DisplayName("addProgram — invalid code throws IllegalArgumentException")
    void addProgram_invalidCode() {
        Program bad = buildProgram("bad code!", "Some Program");
        assertThrows(IllegalArgumentException.class, () -> programService.addProgram(bad));
    }

    @Test
    @Order(3)
    @DisplayName("addProgram — duplicate name throws ServiceException")
    void addProgram_duplicateName() {
        programService.addProgram(buildProgram("BSCS", "Bachelor of Computer Science"));
        assertThrows(ServiceException.class,
                () -> programService.addProgram(buildProgram("BSIT", "Bachelor of Computer Science")));
    }

    @Test
    @Order(4)
    @DisplayName("addProgram — unknown college code throws ServiceException (FK violation)")
    void addProgram_unknownCollege() {
        Program p = buildProgram("BSBAD", "Bad Program");
        p.setCollegeCode("NONE");
        assertThrows(ServiceException.class, () -> programService.addProgram(p));
    }

    // ---------------------------------------------------------------- UPDATE

    @Test
    @Order(5)
    @DisplayName("updateProgram — name and code changes are persisted")
    void updateProgram_changes() {
        programService.addProgram(buildProgram("OLD", "Old Program Name"));
        List<Program> all = programService.getAllPrograms();
        Program p = all.get(0);
        p.setCode("NEW");
        p.setName("New Program Name");

        programService.updateProgram(p);

        List<Program> updated = programService.getAllPrograms();
        assertEquals(1, updated.size());
        assertEquals("NEW", updated.get(0).getCode());
        assertEquals("New Program Name", updated.get(0).getName());
    }

    // ---------------------------------------------------------------- DELETE

    @Test
    @Order(6)
    @DisplayName("deleteProgram — program is removed")
    void deleteProgram_removed() {
        programService.addProgram(buildProgram("DEL", "Delete Me Program"));
        int id = programService.getAllPrograms().get(0).getId();

        programService.deleteProgram(id);

        assertTrue(programService.getAllPrograms().isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("deleteProgram — non-existent id throws ServiceException")
    void deleteProgram_notFound() {
        assertThrows(ServiceException.class, () -> programService.deleteProgram(9999));
    }

    // ---------------------------------------------------------------- LIST / COUNT

    @Test
    @Order(8)
    @DisplayName("getPrograms / countPrograms — search filters correctly")
    void getPrograms_search() {
        // Use a college name that doesn't contain the search terms
        // so search results only match program names
        collegeService = new CollegeService();
        programService = new ProgramService();
        clearAllData();
        collegeService.addCollege(new College("SCI", "School of Sciences"));

        Program cs = new Program();
        cs.setCode("BSCS"); cs.setName("Bachelor of Science in Computing"); cs.setCollegeCode("SCI");
        programService.addProgram(cs);

        Program it = new Program();
        it.setCode("BSIT"); it.setName("Bachelor of Information Technology"); it.setCollegeCode("SCI");
        programService.addProgram(it);

        Program math = new Program();
        math.setCode("BSMATH"); math.setName("Bachelor of Science in Mathematics"); math.setCollegeCode("SCI");
        programService.addProgram(math);

        // "Computing" appears only in BSCS program name
        int count = programService.countPrograms("Computing");
        assertEquals(1, count);

        List<Program> page = programService.getPrograms(0, 10, "p.name", true, "Computing");
        assertEquals(1, page.size());
        assertTrue(page.get(0).getName().contains("Computing"));
    }

    @Test
    @Order(9)
    @DisplayName("getPrograms — sort by p.code ascending")
    void getPrograms_sort() {
        programService.addProgram(buildProgram("ZZ", "Z Program"));
        programService.addProgram(buildProgram("AA", "A Program"));

        List<Program> sorted = programService.getPrograms(0, 10, "p.code", true, "");
        assertEquals("AA", sorted.get(0).getCode());
        assertEquals("ZZ", sorted.get(1).getCode());
    }

    @Test
    @Order(10)
    @DisplayName("getProgram — read by ID returns correct record with joined college name")
    void getProgram_readById() {
        programService.addProgram(buildProgram("BSCS", "Bachelor of Science in Computer Science"));
        int id = programService.getAllPrograms().get(0).getId();

        Program found = programService.getProgram(id);

        assertNotNull(found);
        assertEquals("BSCS",                                    found.getCode());
        assertEquals("Bachelor of Science in Computer Science", found.getName());
        assertEquals("CCS",                                     found.getCollegeCode());
        // College name comes from the JOIN — verifies DAO query is correct
        assertEquals(TEST_COLLEGE.getName(),                     found.getCollegeName());
    }
}
