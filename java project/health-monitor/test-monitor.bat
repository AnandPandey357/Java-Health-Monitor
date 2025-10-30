@echo off
REM Simple test script for Java Health Monitor

echo ================================================
echo Java Application Health Monitor - Test
echo ================================================

REM Set the project directory
set PROJECT_DIR=%~dp0
cd /d "%PROJECT_DIR%"

echo Current directory: %CD%

REM Check if classes are compiled
if not exist "target\classes\monitor\SimpleJVMMonitor.class" (
    echo Compiling Java classes...
    mkdir target\classes 2>nul
    javac -d target\classes src\monitor\SimpleJVMMonitor.java
    if %errorlevel% neq 0 (
        echo ERROR: Compilation failed.
        pause
        exit /b 1
    )
    echo Compilation successful!
)

echo.
echo Running single metrics collection...
java -cp target\classes monitor.SimpleJVMMonitor

echo.
echo Test completed successfully!
pause