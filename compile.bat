@echo off
REM Compiles the project without Maven, using the sqlite-jdbc jar bundled in lib/.

setlocal
cd /d "%~dp0"

if not exist out mkdir out

dir /s /b src\main\java\*.java > sources.txt

javac -encoding UTF-8 -cp "lib\sqlite-jdbc-3.36.0.3.jar" -d out @sources.txt

if errorlevel 1 (
    del sources.txt
    echo Compilation failed.
    exit /b 1
)

del sources.txt
echo Compiled to .\out