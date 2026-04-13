package com.sis.service;

import com.sis.dao.ProgramDAO;
import com.sis.dao.StudentDAO;
import com.sis.model.Program;
import com.sis.model.Student;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service layer for student-related operations.
 * Sits between the UI ({@code StudentPanel}) and the DAO layer.
 */
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentDAO studentDAO = new StudentDAO();
    private final ProgramDAO programDAO = new ProgramDAO();

    // ----------------------------------------------------------------- LIST

    public List<Student> getStudents(int page, int pageSize,
                                     String sortKey, boolean ascending,
                                     String search) {
        try {
            return studentDAO.list(page, pageSize, sortKey, ascending, search);
        } catch (SQLException e) {
            log.error("Failed to list students (page={}, search='{}')", page, search, e);
            throw new ServiceException("Failed to load student data.", e);
        }
    }

    public int countStudents(String search) {
        try {
            return studentDAO.count(search);
        } catch (SQLException e) {
            log.error("Failed to count students (search='{}')", search, e);
            throw new ServiceException("Failed to count student records.", e);
        }
    }

    // ------------------------------------------------------------------ READ

    public Student getStudent(String id) {
        try {
            return studentDAO.read(id);
        } catch (SQLException e) {
            log.error("Failed to read student id='{}'", id, e);
            throw new ServiceException("Failed to load student record.", e);
        }
    }

    // ----------------------------------------------------------------- CREATE

    public void addStudent(Student s) {
        StudentValidator.validate(s);   // IllegalArgumentException propagates as-is
        try {
            studentDAO.create(s);
            log.info("Student created: id='{}', name='{}'", s.getId(), s.getFullName());
        } catch (SQLException e) {
            log.error("Failed to create student id='{}'", s.getId(), e);
            throw new ServiceException(translateWriteError(e, "save"), e);
        }
    }

    // ----------------------------------------------------------------- UPDATE

    public void updateStudent(Student s) {
        StudentValidator.validate(s);   // IllegalArgumentException propagates as-is
        try {
            studentDAO.update(s);
            log.info("Student updated: id='{}', name='{}'", s.getId(), s.getFullName());
        } catch (SQLException e) {
            log.error("Failed to update student id='{}'", s.getId(), e);
            throw new ServiceException(translateWriteError(e, "update"), e);
        }
    }

    // ----------------------------------------------------------------- DELETE

    public void deleteStudent(String id) {
        try {
            studentDAO.delete(id);
            log.info("Student deleted: id='{}'", id);
        } catch (SQLException e) {
            log.error("Failed to delete student id='{}'", id, e);
            String msg = e.getMessage();
            if (msg != null && msg.contains("not found")) {
                throw new ServiceException(
                        "Student not found – it may have been deleted by another user.", e);
            }
            throw new ServiceException("Failed to delete the student record.", e);
        }
    }

    // --------------------------------------------------------------- HELPERS

    public String generateNextId(int enrollYear) {
        try {
            return studentDAO.generateNextId(enrollYear);
        } catch (SQLException e) {
            log.error("Failed to generate student ID for year {}", enrollYear, e);
            throw new ServiceException("Failed to generate student ID.", e);
        }
    }

    public List<Program> getPrograms() {
        try {
            return programDAO.findAll();
        } catch (SQLException e) {
            log.error("Failed to load program list", e);
            throw new ServiceException("Failed to load program list.", e);
        }
    }

    // --------------------------------------------------------- error helpers

    private static String translateWriteError(SQLException e, String verb) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("UNIQUE")) {
            return "A student with that ID already exists.";
        }
        if (msg != null && msg.contains("FOREIGN KEY")) {
            return "The selected program no longer exists.";
        }
        if (msg != null && msg.contains("not found")) {
            return "Student not found – it may have been deleted by another user.";
        }
        return "Failed to " + verb + " the student record.";
    }
}
