@echo off
echo === SIS – Run Unit Tests (Windows) ===
echo.

pushd "%~dp0" >nul 2>&1

REM ── Verify JUnit JARs ────────────────────────────────────────────────────────
if not exist "lib\test\junit-platform-console-standalone-1.10.2.jar" (
    echo ERROR: lib\test\junit-platform-console-standalone-1.10.2.jar not found.
    echo   Download from:
    echo   https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar
    pause & exit /b 1
)

set MAIN_CP=lib\sqlite-jdbc-3.45.3.0.jar;lib\slf4j-api-1.7.36.jar;lib\logback-classic-1.2.12.jar;lib\logback-core-1.2.12.jar
set TEST_JAR=lib\test\junit-platform-console-standalone-1.10.2.jar
set CP=%MAIN_CP%;%TEST_JAR%

echo [1/3] Compiling main sources...
if exist out-test rmdir /s /q out-test
mkdir out-test
for /r src\main\java %%f in (*.java) do echo %%f >> sources-test.txt
javac -cp "%CP%" -d out-test @sources-test.txt
if errorlevel 1 ( echo Compile failed (main). & pause & exit /b 1 )

echo [2/3] Compiling test sources...
for /r src\test\java %%f in (*.java) do echo %%f >> sources-test.txt
javac -cp "out-test;%CP%" -d out-test src\test\java\com\sis\service\*.java
if errorlevel 1 ( echo Compile failed (tests). & pause & exit /b 1 )

echo [3/3] Running tests...
java -cp "out-test;%CP%" ^
  org.junit.platform.console.standalone.ConsoleLauncher ^
  --scan-class-path=out-test ^
  --include-package=com.sis.service

del sources-test.txt 2>nul
popd >nul 2>&1
pause
