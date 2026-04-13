package com.sis.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DatabaseSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final DatabaseManager db;

    DatabaseSeeder(DatabaseManager db) {
        this.db = db;
    }

    void seedIfEmpty() {
        try {
            if (isFullySeeded()) {
                log.info("Database already seeded – skipping");
                return;
            }
        } catch (SQLException e) {
            log.error("Seed check query failed", e);
            throw new RuntimeException("Seed check failed", e);
        }
        log.info("Seeding database with initial data");
        insertColleges();
        insertPrograms();
        insertStudents();
        log.info("Database seeding complete");
    }

    private boolean isFullySeeded() throws SQLException {
        return countRows("college") > 0 && countRows("program") > 0 && countRows("student") > 0;
    }

    private int countRows(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ---------------------------------------------------------------- colleges

    private void insertColleges() {
        log.info("Seeding colleges");
        String[][] data = {
            {"CASS", "College of Arts & Social Sciences"},
            {"COE",  "College of Engineering"},
            {"CSM",  "College of Science & Mathematics"},
            {"CED",  "College of Education"},
            {"CBAA", "College of Business Administration & Accountancy"},
            {"CCS",  "College of Computer Studies"},
            {"CHS",  "College of Health Sciences"}
        };
        String sql = "INSERT OR IGNORE INTO college(code,name) VALUES(?,?)";
        Connection c = null;
        try {
            c = db.getConnection();
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (String[] row : data) {
                    ps.setString(1, row[0]);
                    ps.setString(2, row[1]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            log.info("Colleges seeded successfully");
        } catch (SQLException e) {
            if (c != null) { try { c.rollback(); } catch (SQLException ignored) {} }
            log.error("College seed failed", e);
            throw new RuntimeException("College seed failed", e);
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); } catch (SQLException ignored) {}
                try { c.close();             } catch (SQLException ignored) {}
            }
        }
    }

    // ---------------------------------------------------------------- programs

    private void insertPrograms() {
        log.info("Seeding programs");
        // [code, name, college_code]  – duplicated codes are intentional
        String[][] data = {
            // CASS
            {"GEP",      "General Education Program",                                             "CASS"},
            {"BAE",      "Bachelor of Arts in English",                                           "CASS"},
            {"BSPSYCH",  "Bachelor of Science in Psychology",                                     "CASS"},
            {"BAFIL",    "Bachelor of Arts in Filipino",                                          "CASS"},
            {"BAHISTO",  "Bachelor of Arts in History",                                           "CASS"},
            {"BSPOLSCI", "Bachelor of Arts in Political Science",                                 "CASS"},
            // COE
            {"DCET",     "Diploma in Chemical Engineering Technology",                            "COE"},
            {"BSCE",     "Bachelor of Science in Civil Engineering",                              "COE"},
            {"BSCE",     "Bachelor of Science in Ceramics Engineering",                           "COE"},
            {"BSCHEMENG","Bachelor of Science in Chemical Engineering",                           "COE"},
            {"BSCE",     "Bachelor of Science in Computer Engineering",                           "COE"},
            {"BSECE",    "Bachelor of Science in Electronics & Communications Engineering",       "COE"},
            {"BSEE",     "Bachelor of Science in Electrical Engineering",                         "COE"},
            {"BSME",     "Bachelor of Science in Mining Engineering",                             "COE"},
            {"BSEET",    "Bachelor of Science in Environmental Engineering Technology",           "COE"},
            {"BSMECHENG","Bachelor of Science in Mechanical Engineering",                         "COE"},
            {"BSMETENG", "Bachelor of Science in Metallurgical Engineering",                      "COE"},
            // CSM
            {"BSBIO",    "Bachelor of Science in Biology (Botany)",                               "CSM"},
            {"BSCHEM",   "Bachelor of Science in Chemistry",                                      "CSM"},
            {"BSMAT",    "Bachelor of Science in Mathematics",                                    "CSM"},
            {"BSPHYS",   "Bachelor of Science in Physics",                                        "CSM"},
            {"BSBIO",    "Bachelor of Science in Biology (Zoology)",                              "CSM"},
            {"BSBIO",    "Bachelor of Science in Biology (Marine)",                               "CSM"},
            {"BSBIO",    "Bachelor of Science in Biology (General)",                              "CSM"},
            {"BSSTAT",   "Bachelor of Science in Statistics",                                     "CSM"},
            // CED
            {"BSE",      "Bachelor of Secondary Education (Biology)",                             "CED"},
            {"BSIE",     "Bachelor of Science in Industrial Education (Drafting)",                "CED"},
            {"BSE",      "Bachelor of Secondary Education (Chemistry)",                           "CED"},
            {"BSE",      "Bachelor of Secondary Education (Physics)",                             "CED"},
            {"BSE",      "Bachelor of Secondary Education (Mathematics)",                         "CED"},
            {"BSE",      "Bachelor of Secondary Education (MAPEH)",                               "CED"},
            {"CPT",      "Certificate Program for Teachers",                                      "CED"},
            {"BSE",      "Bachelor of Secondary Education (TLE)",                                 "CED"},
            {"BSE",      "Bachelor of Secondary Education (General Science)",                     "CED"},
            {"BEE",      "Bachelor of Elementary Education (English)",                            "CED"},
            {"BEE",      "Bachelor of Elementary Education (Science and Health)",                 "CED"},
            {"BSTT",     "Bachelor of Science in Technology Teacher Education (Industrial Tech)", "CED"},
            {"BSTT",     "Bachelor of Science in Technology Teacher Education (Drafting Tech)",   "CED"},
            // CBAA
            {"BSBA",     "Bachelor of Science in Business Administration (Business Economics)",   "CBAA"},
            {"BSBA",     "Bachelor of Science in Business Administration (Economics)",            "CBAA"},
            {"BSBA",     "Bachelor of Science in Business Administration (Entrepreneurial Marketing)","CBAA"},
            {"BSHRM",    "Bachelor of Science in Hotel and Restaurant Management",                "CBAA"},
            {"BSA",      "Bachelor of Science in Accountancy",                                    "CBAA"},
            // CCS
            {"DEET",     "Diploma in Electronics Engineering Tech (Computer Electronics)",        "CCS"},
            {"BSIS",     "Bachelor of Science in Information Systems",                            "CCS"},
            {"BSIT",     "Bachelor of Science in Information Technology",                         "CCS"},
            {"DET",      "Diploma in Electronics Technology",                                     "CCS"},
            {"DEE",      "Diploma in Electronics Engineering Tech (Communication Electronics)",   "CCS"},
            {"BSCS",     "Bachelor of Science in Computer Science",                               "CCS"},
            {"BSECT",    "Bachelor of Science in Electronics and Computer Technology (Embedded Systems)",      "CCS"},
            {"BSECT",    "Bachelor of Science in Electronics and Computer Technology (Communications System)", "CCS"},
            // CHS
            {"BSN",      "Bachelor of Science in Nursing",                                        "CHS"}
        };
        String sql = "INSERT OR IGNORE INTO program(code,name,college_code) VALUES(?,?,?)";
        Connection c = null;
        try {
            c = db.getConnection();
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (String[] row : data) {
                    ps.setString(1, row[0]);
                    ps.setString(2, row[1]);
                    ps.setString(3, row[2]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            log.info("Programs seeded successfully");
        } catch (SQLException e) {
            if (c != null) { try { c.rollback(); } catch (SQLException ignored) {} }
            log.error("Program seed failed", e);
            throw new RuntimeException("Program seed failed", e);
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); } catch (SQLException ignored) {}
                try { c.close();             } catch (SQLException ignored) {}
            }
        }
    }

    // ---------------------------------------------------------------- students

    private void insertStudents() {
        log.info("Seeding students (5100 records)");
        // ── name pools ────────────────────────────────────────────────────
        String[] male = {
            "Juan","Jose","Miguel","Antonio","Ramon","Eduardo","Carlos","Roberto",
            "Francisco","Pedro","Fernando","Luis","Jorge","Manuel","Ricardo","Andres",
            "Rodolfo","Victor","Ernesto","Mario","Dante","Angelo","Christian","Mark",
            "John","James","Michael","Daniel","David","Joseph","Paul","Peter","Kenneth",
            "Patrick","Jonathan","Ryan","Kevin","Gabriel","Rafael","Lorenzo","Alejandro",
            "Sebastian","Ignacio","Leonardo","Marcos","Felix","Rogelio","Reynaldo",
            "Arnel","Boyet","Danilo","Efren","Feliciano","Gideon","Hernani","Ildefonso",
            "Jacinto","Kalisto","Lamberto","Maynard","Noel","Octavio","Prudencio"
        };
        String[] female = {
            "Maria","Ana","Rosa","Elena","Carmen","Luz","Gloria","Marisol","Cristina",
            "Angelica","Jennifer","Jessica","Michelle","Karen","Christine","Patricia",
            "Elizabeth","Stephanie","Nicole","Katherine","Sarah","Amanda","Melissa",
            "Rebecca","Angela","Sandra","Linda","Sharon","Grace","Faith","Hope",
            "Charity","Joy","Pearl","Ruby","Jade","Crystal","Diana","Theresa",
            "Florencia","Milagros","Carmelita","Remedios","Corazon","Natividad",
            "Estrella","Rosario","Dolores","Concepcion","Maricel","Lourdes","Nenita",
            "Ofelia","Priscilla","Quirina","Salvacion","Tessie","Ursula","Vilma"
        };
        String[] last = {
            "Santos","Reyes","Cruz","Garcia","Ramos","Flores","Gonzales","Torres",
            "Rivera","Villanueva","Mendoza","Bautista","Aquino","Hernandez","Domingo",
            "Castro","Morales","Lopez","Padilla","Soriano","Aguilar","Navarro","Diaz",
            "Ocampo","Pascual","Salazar","Fernandez","Guevarra","Lim","Tan","Ong",
            "Sy","Go","Co","Chua","Lee","Yap","Chan","Wong","Ang","Dela Cruz",
            "Delos Santos","Macaraeg","Manalo","Buenaventura","Magno","Santiago",
            "Bernardo","Castillo","Alcantara","Velasquez","Concepcion","Enriquez",
            "Evangelista","Hidalgo","Inocencio","Jimenez","Lacson","Medina","Nunez",
            "Oliva","Pineda","Quijano","Roque","Suarez","Trinidad","Umali","Valencia",
            "Vergara","Villarin","Yumul","Zuniga","Bacud","Caluag","Dalogdog",
            "Estoesta","Fabula","Galvez","Hamor","Ilustre","Jamero","Kintanar",
            "Lucero","Macatangay","Nabong","Ogsimer","Pacis","Rabino","Sabado",
            "Tabayoyong","Ulanday","Vidal","Wagas","Xeres","Ybasco","Zamora"
        };

        // ── fetch program IDs ─────────────────────────────────────────────
        List<Integer> programIds = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement  st = c.createStatement();
             ResultSet  rs = st.executeQuery("SELECT id FROM program")) {
            while (rs.next()) programIds.add(rs.getInt(1));
        } catch (SQLException e) {
            log.error("Failed to fetch program IDs for student seeding", e);
            throw new RuntimeException("Program fetch failed", e);
        }
        int[] pids = programIds.stream().mapToInt(i -> i).toArray();

        // ── generate 5,100 students ───────────────────────────────────────
        Random rng = new Random(2024L);
        int[] yearSeq = new int[7]; // index 1-6 → 2019-2024
        int BASE = 2019;

        String sql = "INSERT OR IGNORE INTO student(id,firstname,lastname,program_id,year,gender) VALUES(?,?,?,?,?,?)";
        Connection c = null;
        try {
            c = db.getConnection();
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < 5100; i++) {
                    int enrollYear = BASE + rng.nextInt(6);          
                    int idx = enrollYear - BASE + 1;
                    yearSeq[idx]++;
                    String sid = String.format("%d-%04d", enrollYear, yearSeq[idx]);

                    String gender = rng.nextBoolean() ? "Male" : "Female";
                    String fname  = gender.equals("Male")
                            ? male[rng.nextInt(male.length)]
                            : female[rng.nextInt(female.length)];
                    String lname  = last[rng.nextInt(last.length)];
                    int pid       = pids[rng.nextInt(pids.length)];
                    int yr        = 1 + rng.nextInt(5);

                    ps.setString(1, sid);
                    ps.setString(2, fname);
                    ps.setString(3, lname);
                    ps.setInt(4, pid);
                    ps.setInt(5, yr);
                    ps.setString(6, gender);
                    ps.addBatch();

                    if ((i + 1) % 500 == 0) ps.executeBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            log.info("Students seeded successfully");
        } catch (SQLException e) {
            if (c != null) { try { c.rollback(); } catch (SQLException ignored) {} }
            log.error("Student seed failed", e);
            throw new RuntimeException("Student seed failed", e);
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); } catch (SQLException ignored) {}
                try { c.close();             } catch (SQLException ignored) {}
            }
        }
    }
}