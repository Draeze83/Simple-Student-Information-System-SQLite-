package com.sis.service;

import com.sis.dao.CollegeDAO;
import com.sis.model.College;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for college operations.
 * Validates input, wraps DAO calls, translates {@link SQLException} into
 * user-safe {@link ServiceException}s, and logs every failure and mutation.
 */
public class CollegeService {

    private static final Logger log = LoggerFactory.getLogger(CollegeService.class);
    private final CollegeDAO dao = new CollegeDAO();

    // ------------------------------------------------------------------ LIST

    public List<College> getColleges(int page, int pageSize,
                                     String sortCol, boolean ascending,
                                     String search) {
        try {
            return dao.list(page, pageSize, sortCol, ascending, search);
        } catch (SQLException e) {
            log.error("Failed to list colleges (page={}, search='{}')", page, search, e);
            throw new ServiceException("Failed to load college data.", e);
        }
    }

    public int countColleges(String search) {
        try {
            return dao.count(search);
        } catch (SQLException e) {
            log.error("Failed to count colleges (search='{}')", search, e);
            throw new ServiceException("Failed to count college records.", e);
        }
    }

    // ------------------------------------------------------------------ READ

    public College getCollege(String code) {
        try {
            return dao.read(code);
        } catch (SQLException e) {
            log.error("Failed to read college code='{}'", code, e);
            throw new ServiceException("Failed to load college record.", e);
        }
    }

    /** Returns all colleges for combo-boxes. */
    public List<College> getAllColleges() {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            log.error("Failed to load all colleges", e);
            throw new ServiceException("Failed to load college list.", e);
        }
    }

    // ----------------------------------------------------------------- CREATE

    public void addCollege(College c) {
        CollegeValidator.validate(c);
        try {
            dao.create(c);
            log.info("College created: code='{}', name='{}'", c.getCode(), c.getName());
        } catch (SQLException e) {
            log.error("Failed to create college code='{}'", c.getCode(), e);
            throw new ServiceException(translateWriteError(e, "save"), e);
        }
    }

    // ----------------------------------------------------------------- UPDATE

    public void updateCollege(String originalCode, College updated) {
        CollegeValidator.validate(updated);
        try {
            dao.update(originalCode, updated);
            log.info("College updated: originalCode='{}' → new code='{}', name='{}'",
                    originalCode, updated.getCode(), updated.getName());
        } catch (SQLException e) {
            log.error("Failed to update college code='{}'", originalCode, e);
            throw new ServiceException(translateWriteError(e, "update"), e);
        }
    }

    // ----------------------------------------------------------------- DELETE

    public void deleteCollege(String code) {
        try {
            dao.delete(code);
            log.info("College deleted: code='{}'", code);
        } catch (SQLException e) {
            log.error("Failed to delete college code='{}'", code, e);
            String msg = e.getMessage();
            if (msg != null && msg.contains("FOREIGN KEY"))
                throw new ServiceException("A program is still linked to this college.", e);
            if (msg != null && msg.contains("not found"))
                throw new ServiceException(
                        "College not found – it may have been deleted by another user.", e);
            throw new ServiceException("Failed to delete the college record.", e);
        }
    }

    // --------------------------------------------------------------- helpers

    private static String translateWriteError(SQLException e, String verb) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("UNIQUE"))
            return "A college with that code or name already exists.";
        if (msg != null && msg.contains("not found"))
            return "College not found – it may have been deleted by another user.";
        return "Failed to " + verb + " the college record.";
    }
}
