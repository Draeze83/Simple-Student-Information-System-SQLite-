package com.sis.service;

import com.sis.model.College;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CollegeService}.
 *
 * <p>These tests exercise the full service → DAO → SQLite stack against an
 * in-memory database (configured via the {@code db.path} system property).
 * Each test class gets a fresh database so tests are fully isolated.
 */
@DisplayName("CollegeService")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CollegeServiceTest extends ServiceIntegrationBase {

    // Instantiated in @BeforeEach (after DatabaseManager is initialized)
    private CollegeService service;

    @BeforeEach
    void createService() {
        service = new CollegeService();
        clearAllData();
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

    // ---------------------------------------------------------------- CREATE

    @Test
    @Order(1)
    @DisplayName("addCollege — valid college is persisted and readable")
    void addCollege_valid() {
        College c = new College("TST", "Test College of Science");
        service.addCollege(c);

        College found = service.getCollege("TST");
        assertNotNull(found);
        assertEquals("TST",                      found.getCode());
        assertEquals("Test College of Science",  found.getName());
    }

    @Test
    @Order(2)
    @DisplayName("addCollege — invalid code throws IllegalArgumentException (validation path)")
    void addCollege_invalidCode() {
        College bad = new College("bad code!", "Some Name");
        assertThrows(IllegalArgumentException.class, () -> service.addCollege(bad));
    }

    @Test
    @Order(3)
    @DisplayName("addCollege — duplicate code throws ServiceException (DB path)")
    void addCollege_duplicateCode() {
        service.addCollege(new College("DUP", "Dup College One"));
        assertThrows(ServiceException.class,
                () -> service.addCollege(new College("DUP", "Dup College Two")));
    }

    // ---------------------------------------------------------------- UPDATE

    @Test
    @Order(4)
    @DisplayName("updateCollege — name change is persisted")
    void updateCollege_nameChange() {
        service.addCollege(new College("UPD", "Original Name"));
        service.updateCollege("UPD", new College("UPD", "Updated Name"));

        College found = service.getCollege("UPD");
        assertNotNull(found);
        assertEquals("Updated Name", found.getName());
    }

    @Test
    @Order(5)
    @DisplayName("updateCollege — code change cascades (FK ON UPDATE CASCADE)")
    void updateCollege_codeChange() {
        service.addCollege(new College("OLD", "Old Code College"));
        service.updateCollege("OLD", new College("NEW", "Old Code College"));

        assertNull(service.getCollege("OLD"), "Old code should no longer exist");
        assertNotNull(service.getCollege("NEW"), "New code should be queryable");
    }

    // ---------------------------------------------------------------- DELETE

    @Test
    @Order(6)
    @DisplayName("deleteCollege — removed college is no longer found")
    void deleteCollege_removed() {
        service.addCollege(new College("DEL", "Delete Me"));
        service.deleteCollege("DEL");
        assertNull(service.getCollege("DEL"));
    }

    @Test
    @Order(7)
    @DisplayName("deleteCollege — non-existent code throws ServiceException")
    void deleteCollege_notFound() {
        assertThrows(ServiceException.class, () -> service.deleteCollege("NONE"));
    }

    // ---------------------------------------------------------------- LIST / COUNT

    @Test
    @Order(8)
    @DisplayName("getColleges / countColleges — pagination and search work correctly")
    void getColleges_paginationAndSearch() {
        service.addCollege(new College("A1", "Alpha Institution"));
        service.addCollege(new College("B1", "Beta School"));
        service.addCollege(new College("C1", "Alpha Community College"));

        // Search for "Alpha" — should return 2 matching colleges
        int count = service.countColleges("Alpha");
        assertEquals(2, count);

        List<College> page = service.getColleges(0, 10, "code", true, "Alpha");
        assertEquals(2, page.size());
        assertTrue(page.stream().allMatch(c -> c.getName().contains("Alpha")));
    }

    @Test
    @Order(9)
    @DisplayName("getAllColleges — returns all persisted colleges")
    void getAllColleges() {
        service.addCollege(new College("X1", "X College"));
        service.addCollege(new College("Y1", "Y College"));

        List<College> all = service.getAllColleges();
        assertTrue(all.size() >= 2);
    }

    @Test
    @Order(10)
    @DisplayName("getColleges — sort direction (ASC vs DESC) is honoured")
    void getColleges_sortDirection() {
        service.addCollege(new College("AAA", "Aardvark Academy"));
        service.addCollege(new College("MMM", "Midland University"));
        service.addCollege(new College("ZZZ", "Zenith College"));

        List<College> asc  = service.getColleges(0, 10, "code", true,  "");
        List<College> desc = service.getColleges(0, 10, "code", false, "");

        // Ascending: AAA first
        assertEquals("AAA", asc.get(0).getCode());
        assertEquals("ZZZ", asc.get(asc.size() - 1).getCode());

        // Descending: ZZZ first
        assertEquals("ZZZ", desc.get(0).getCode());
        assertEquals("AAA", desc.get(desc.size() - 1).getCode());
    }
}
