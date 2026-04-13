package com.sis.service;

import com.sis.dao.CollegeDAO;
import com.sis.dao.ProgramDAO;
import com.sis.model.College;
import com.sis.model.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for program operations.
 * Validates input, wraps DAO calls, translates {@link SQLException} into
 * user-safe {@link ServiceException}s, and logs every failure and mutation.
 */
public class ProgramService {

    private static final Logger log = LoggerFactory.getLogger(ProgramService.class);
    private final ProgramDAO programDAO = new ProgramDAO();
    private final CollegeDAO collegeDAO = new CollegeDAO();

    // ------------------------------------------------------------------ LIST

    public List<Program> getPrograms(int page, int pageSize,
                                     String sortCol, boolean ascending,
                                     String search) {
        try {
            return programDAO.list(page, pageSize, sortCol, ascending, search);
        } catch (SQLException e) {
            log.error("Failed to list programs (page={}, search='{}')", page, search, e);
            throw new ServiceException("Failed to load program data.", e);
        }
    }

    public int countPrograms(String search) {
        try {
            return programDAO.count(search);
        } catch (SQLException e) {
            log.error("Failed to count programs (search='{}')", search, e);
            throw new ServiceException("Failed to count program records.", e);
        }
    }

    // ------------------------------------------------------------------ READ

    public Program getProgram(int id) {
        try {
            return programDAO.read(id);
        } catch (SQLException e) {
            log.error("Failed to read program id={}", id, e);
            throw new ServiceException("Failed to load program record.", e);
        }
    }

    /** Returns all programs for combo-boxes (e.g. StudentDialog). */
    public List<Program> getAllPrograms() {
        try {
            return programDAO.findAll();
        } catch (SQLException e) {
            log.error("Failed to load all programs", e);
            throw new ServiceException("Failed to load program list.", e);
        }
    }

    /** Returns all colleges for combo-boxes in ProgramDialog. */
    public List<College> getAllColleges() {
        try {
            return collegeDAO.findAll();
        } catch (SQLException e) {
            log.error("Failed to load college list for program dialog", e);
            throw new ServiceException("Failed to load college list.", e);
        }
    }

    // ----------------------------------------------------------------- CREATE

    public void addProgram(Program p) {
        ProgramValidator.validate(p);
        try {
            programDAO.create(p);
            log.info("Program created: code='{}', name='{}'", p.getCode(), p.getName());
        } catch (SQLException e) {
            log.error("Failed to create program code='{}'", p.getCode(), e);
            throw new ServiceException(translateWriteError(e, "save"), e);
        }
    }

    // ----------------------------------------------------------------- UPDATE

    public void updateProgram(Program p) {
        ProgramValidator.validate(p);
        try {
            programDAO.update(p);
            log.info("Program updated: id={}, name='{}'", p.getId(), p.getName());
        } catch (SQLException e) {
            log.error("Failed to update program id={}", p.getId(), e);
            throw new ServiceException(translateWriteError(e, "update"), e);
        }
    }

    // ----------------------------------------------------------------- DELETE

    public void deleteProgram(int id) {
        try {
            programDAO.delete(id);
            log.info("Program deleted: id={}", id);
        } catch (SQLException e) {
            log.error("Failed to delete program id={}", id, e);
            String msg = e.getMessage();
            if (msg != null && msg.contains("FOREIGN KEY"))
                throw new ServiceException("Students are still enrolled in this program.", e);
            if (msg != null && msg.contains("not found"))
                throw new ServiceException(
                        "Program not found – it may have been deleted by another user.", e);
            throw new ServiceException("Failed to delete the program record.", e);
        }
    }

    // --------------------------------------------------------------- helpers

    private static String translateWriteError(SQLException e, String verb) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("UNIQUE"))
            return "A program with that name already exists.";
        if (msg != null && msg.contains("FOREIGN KEY"))
            return "The selected college no longer exists.";
        if (msg != null && msg.contains("not found"))
            return "Program not found – it may have been deleted by another user.";
        return "Failed to " + verb + " the program record.";
    }
}
