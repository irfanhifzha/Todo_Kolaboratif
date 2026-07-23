@echo off
setlocal

REM Create output directory if it doesn't exist
if not exist out mkdir out

REM Compile Java source files
dir /s /b src\*.java > sources.txt
javac -cp "lib\sqlite-jdbc-3.53.2.0.jar" -d out @sources.txt

if errorlevel 1 (
    del sources.txt
    exit /b %errorlevel%
)

del sources.txt

REM Run the application
java -cp "out;lib\sqlite-jdbc-3.53.2.0.jar" com.classapp.Main

endlocal