@echo off
REM =============================================================================
REM  build.bat  –  Student Information System (Windows build wrapper)
REM  Delegates all compilation and packaging to Maven.
REM  Use:  build.bat          → compile + test + package
REM        build.bat -DskipTests → skip tests (faster)
REM =============================================================================
setlocal

pushd "%~dp0" >nul 2>&1

REM ── Locate mvn or mvnw ───────────────────────────────────────────────────────
where mvn >nul 2>&1
if %errorlevel% == 0 (
    set MVN=mvn
    goto :run
)
if exist "mvnw.cmd" (
    set MVN=mvnw.cmd
    goto :run
)
if exist "%USERPROFILE%\.maven" (
    set "MVN="
    for /f "delims=" %%D in ('dir /b /ad "%USERPROFILE%\.maven" 2^>nul') do (
        if not defined MVN (
            if exist "%USERPROFILE%\.maven\%%D\bin\mvn.cmd" set "MVN=%USERPROFILE%\.maven\%%D\bin\mvn.cmd"
            if exist "%USERPROFILE%\.maven\%%D\bin\mvn" if not defined MVN set "MVN=%USERPROFILE%\.maven\%%D\bin\mvn"
        )
    )
)
if defined MVN goto :run

echo ERROR: Maven (mvn) not found on PATH and no mvnw.cmd wrapper present.
echo        Install Maven from https://maven.apache.org/download.cgi
echo        or add it to your PATH.
echo        If Maven is installed locally, put it under %%USERPROFILE%%\.maven\<version>\bin\mvn
pause & popd >nul 2>&1 & exit /b 1

:run
echo === Student Information System – Maven Build (Windows) ===
echo Using: %MVN%
echo.

%MVN% clean package %*

if %errorlevel% neq 0 (
    echo.
    echo Build FAILED. See output above for details.
    pause & popd >nul 2>&1 & exit /b 1
)

echo.
echo Build complete!
echo   JAR:              target\student-information-system-1.0.0-shaded.jar
echo   Run:              java -jar target\student-information-system-1.0.0-shaded.jar
echo   Override DB path: java -Ddb.path=C:\data\sis.db -jar target\student-information-system-1.0.0-shaded.jar
echo   Logs directory:   .\logs\sis.log  (created on first run)
echo.
pause

popd >nul 2>&1
endlocal