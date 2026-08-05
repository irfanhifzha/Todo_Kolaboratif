@echo off
REM Runs the app without Maven. Run compile.bat first.

setlocal

REM Change to the directory containing this script
cd /d "%~dp0"

java -cp "out;lib\sqlite-jdbc-3.36.0.3.jar" com.classapp.Main

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    exit /b %errorlevel%
)