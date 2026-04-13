# Student Information System

## Description

The Student Information System (SIS) is a desktop application built in Java that allows a user to manage records for students, academic programs, and colleges. It provides a graphical interface through which users can create, view, update, delete, and list records across all three of these categories. The application is built around a local SQLite database, and the database comes pre-populated with seven colleges, over thirty programs, and 5,100 student records on first run.

The system is intended for use by administrative staff who need to manage enrollment data without the complexity of a web-based or server-dependent solution. Because everything runs locally as a single executable file, no internet connection, web server, or separate database server is required.

---

## Requirements

Before building or running the application, ensure the following software is installed on your machine.

**Java Development Kit (JDK) 11 or higher**
The application is compiled and runs on Java 11. You can download it from https://adoptium.net or install it through your operating system's package manager.

To verify your installation, run:
```
java -version
```

**Apache Maven 3.6 or higher**
Maven is the build tool used to compile the source code, run tests, and package everything into a single runnable file. You can download it from https://maven.apache.org/download.cgi.

To verify your installation, run:
```
mvn -version
```

No other software needs to be installed. The database engine (SQLite) and all other required libraries are bundled directly into the project.

---

## How to Build

Navigate to the `sis` folder inside the project directory. This is where the `pom.xml` build file and source code are located.

**On Linux or macOS:**
```bash
cd sis
chmod +x build.sh
./build.sh
```

**On Windows:**
```
cd sis
build.bat
```

Alternatively, you can run Maven directly:
```
mvn clean package
```

If you want to skip the automated tests to build faster:
```
mvn clean package -DskipTests
```

After a successful build, the runnable file will appear at:
```
sis/target/student-information-system-1.0.0-shaded.jar
```

---

## How to Run

Once the build is complete, run the application with the following command from inside the `sis` folder:

```
java -jar target/student-information-system-1.0.0-shaded.jar
```

Alternatively, the project can be run directly from an IDE by executing the `run.bat` file located in the `sis` folder.

The application will open a window with three tabs: Students, Programs, and Colleges. On the very first run, the database will be created and populated automatically. Subsequent runs will detect the existing data and skip the seeding step.

---

## Dependencies

All dependencies are declared in `pom.xml` and are automatically downloaded by Maven during the build. They are bundled into the final JAR so you do not need to install them separately.

| Library | Version | Purpose |
|---|---|---|
| sqlite-jdbc | 3.45.3.0 | Connects the application to the SQLite database |
| logback-classic | 1.2.12 | Handles application logging to files |
| logback-core | 1.2.12 | Core logging framework used by logback-classic |
| slf4j-api | 1.7.36 | Logging interface used throughout the code |

**Test dependency (used only during build, not included in the final JAR):**

| Library | Version | Purpose |
|---|---|---|
| junit-platform-console-standalone | 1.10.2 | Runs the automated test suite during the Maven build |

---

## Application Features

### Student Management
- Add a new student record, including first name, last name, year level (1 through 5), gender, and the program they are enrolled in.
- Student IDs are generated automatically in the format `YYYY-NNNN`, where `YYYY` is the enrollment year and `NNNN` is a sequential number unique to that year.
- Edit any existing student record.
- Delete a student record.
- Search students by ID, first name, last name, program name, program code, or college name. The search runs across all of these fields at once.
- Sort the student list by any column.
- Navigate through the list using pagination controls (first, previous, next, last page).

### Program Management
- Add a new academic program, including a program code, full name, and the college it belongs to.
- Edit existing program records.
- Delete a program, provided no students are currently enrolled in it.
- Search programs by program code, program name, or college name.
- Sort and paginate the program list.

### College Management
- Add a new college with a code and full name.
- Edit existing college records.
- Delete a college, provided no programs are still linked to it.
- Search colleges by code or name.
- Sort and paginate the college list.

---

## Security Measures

Although the system is designed as a local desktop application, it includes several basic security and reliability measures:

**SQL Injection Prevention**
All database queries use prepared statements with parameter binding. User input is never inserted directly into a query string, which means malicious input cannot alter the structure of a SQL query.

**Sort Column Validation**
Sorting is one area where dynamic SQL is unavoidable, since the column name must be embedded in the query. To address this, each data access class maintains an explicit list of allowed column names. Any sort request that does not match a name on that list is rejected before the query runs.

**Input Validation**
Both the dialog forms and the service layer validate data independently. This means input is checked before it reaches the database, and the rules are enforced even if the user interface is bypassed programmatically. Specific rules include: student IDs must match the `YYYY-NNNN` format; college codes may only contain uppercase letters and digits and must not exceed ten characters; program codes follow similar rules; names have maximum character lengths; year level must be between 1 and 5; gender must be one of Male, Female, or Other.

**Referential Integrity**
The database schema uses foreign keys to prevent orphaned records. You cannot delete a college that still has programs, and you cannot delete a program that still has enrolled students. These constraints are enforced at the database level, not just in the application.

**Database Safety Settings**
When connecting to SQLite, the application enables Write-Ahead Logging (WAL) mode and sets synchronous writes to NORMAL. This configuration protects the database file from corruption in the event of an unexpected shutdown, while still providing acceptable performance.

**Connection Pool Timeout**
The application maintains a small pool of reusable database connections. If all connections are in use and a new request cannot be fulfilled within five seconds, the operation fails with a clear error rather than waiting indefinitely.

---

## Logs

The application writes logs to a `logs/` folder in the same directory where it is run. The current log file is named `sis.log`, and older logs are automatically renamed with the date. Logs record startup events, all successful and failed create, update, and delete operations, and any unexpected errors.

---

## Automated Tests

The project includes 99 automated tests covering service logic and input validation across all three entity types. The project includes unit tests for service and validation layers. These tests ensure that business logic behaves as expected and that input validation rules are properly enforced.

Tests can be executed using:

```
mvn test
```

| Test Class | Tests |
|---|---|
| CollegeServiceTest | 10 |
| CollegeValidatorTest | 19 |
| ProgramServiceTest | 10 |
| ProgramValidatorTest | 18 |
| StudentServiceTest | 10 |
| StudentValidatorTest | 32 |

---

## Project Structure and Architecture

```
sis/
├── src/
│   ├── main/
│   │   ├── java/com/sis/
│   │   │   ├── Main.java                    Application entry point
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java           Reads config.properties (e.g. db.path)
│   │   │   ├── db/
│   │   │   │   ├── DatabaseManager.java     Singleton that owns the database lifecycle
│   │   │   │   ├── ConnectionPool.java      Pool of three reusable JDBC connections
│   │   │   │   ├── PooledConnection.java    Wrapper that returns a connection to the pool on close
│   │   │   │   ├── SchemaInitializer.java   Creates tables and indexes on startup (safe to rerun)
│   │   │   │   └── DatabaseSeeder.java      Populates initial college, program, and student data
│   │   │   ├── model/
│   │   │   │   ├── College.java             Data class for a college record
│   │   │   │   ├── Program.java             Data class for a program record
│   │   │   │   └── Student.java             Data class for a student record
│   │   │   ├── dao/
│   │   │   │   ├── CollegeDAO.java          Database operations for colleges
│   │   │   │   ├── ProgramDAO.java          Database operations for programs
│   │   │   │   └── StudentDAO.java          Database operations for students
│   │   │   ├── service/
│   │   │   │   ├── CollegeService.java      Business logic for college operations
│   │   │   │   ├── ProgramService.java      Business logic for program operations
│   │   │   │   ├── StudentService.java      Business logic for student operations
│   │   │   │   ├── CollegeValidator.java    Input rules for college data
│   │   │   │   ├── ProgramValidator.java    Input rules for program data
│   │   │   │   ├── StudentValidator.java    Input rules for student data
│   │   │   │   └── ServiceException.java    Custom exception for business logic errors
│   │   │   └── ui/
│   │   │       ├── MainFrame.java           Main window with tabbed layout
│   │   │       ├── StudentPanel.java        Student list, search, sort, pagination, and buttons
│   │   │       ├── ProgramPanel.java        Program list, search, sort, pagination, and buttons
│   │   │       ├── CollegePanel.java        College list, search, sort, pagination, and buttons
│   │   │       ├── ThemeManager.java        Applies consistent colors to the UI
│   │   │       └── dialog/
│   │   │           ├── StudentDialog.java   Form for adding and editing students
│   │   │           ├── ProgramDialog.java   Form for adding and editing programs
│   │   │           └── CollegeDialog.java   Form for adding and editing colleges
│   │   └── resources/
│   │       ├── config.properties            Default database path setting
│   │       └── logback.xml                  Logging configuration
│   └── test/
│       └── java/com/sis/service/            Unit tests for services and validators
├── lib/                                     Runtime JAR dependencies
├── libtest/                                 Test-only JAR dependencies
├── logs/                                    Log files written at runtime
├── build.sh                                 Build script for Linux and macOS
├── build.bat                                Build script for Windows
└── pom.xml                                  Maven build and dependency configuration
```

### Why the Code Is Structured This Way

The project follows a layered architecture, meaning the code is divided into groups where each group has a single responsibility and communicates only with the group directly below it.

**Configuration layer** (`config`) sits at the top and simply reads the settings file. Keeping configuration separate means the database path or other settings can be changed without touching any other code.

**Database infrastructure layer** (`db`) handles everything related to connecting to SQLite: opening the connection, setting up the schema, filling initial data, and managing the connection pool. Grouping these together means the rest of the application does not need to know or care how connections are managed.

**Model layer** (`model`) contains plain data classes with no logic. These serve as simple containers for carrying data between layers. Having dedicated model classes avoids passing raw arrays or maps around, which would make the code harder to read and maintain.

**Data access layer** (`dao`) is the only part of the application that writes SQL. Each class handles one table. This separation means that if the database structure ever changes, only the corresponding DAO class needs to be updated, and the rest of the application is unaffected.

**Service layer** (`service`) sits above the DAO layer and is responsible for business rules: validating input, calling the DAO, catching database errors, and translating them into plain messages suitable for display. Separating this from the DAO means the same validation and logic applies regardless of whether a record is being created from the user interface or from an automated test.

**User interface layer** (`ui`) only knows about the service layer. It collects input from the user, passes it to a service, and displays the result. Because the UI does not talk to the database directly, it can be replaced or redesigned without changing any business logic or data access code.

This structure also makes testing straightforward. This kind of structure was chosen to improve maintainability, readability, and scalability. By separating responsibilities, changes in one part of the system (such as the database or UI) can be made with minimal impact on other components.  
