@echo off
REM Java Application Health Monitor - Windows Batch File
REM This script runs the health monitoring system

echo ================================================
echo Java Application Health Monitor - Windows Version
echo ================================================

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found. Please install Java 11 or later.
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Set the project directory
set PROJECT_DIR=%~dp0
cd /d "%PROJECT_DIR%"

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

REM Display menu
echo.
echo Select monitoring mode:
echo 1. Single metrics collection
echo 2. Continuous monitoring (30 seconds interval)
echo 3. Continuous monitoring (custom interval)
echo 4. Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto single
if "%choice%"=="2" goto continuous_30
if "%choice%"=="3" goto continuous_custom
if "%choice%"=="4" goto exit
goto invalid_choice

:single
echo.
echo Running single metrics collection...
java -cp target\classes monitor.SimpleJVMMonitor
goto end

:continuous_30
echo.
echo Starting continuous monitoring (30 seconds interval)...
echo Press Ctrl+C to stop
java -cp target\classes monitor.SimpleJVMMonitor 30
goto end

:continuous_custom
echo.
set /p interval="Enter monitoring interval in seconds: "
echo Starting continuous monitoring (%interval% seconds interval)...
echo Press Ctrl+C to stop
java -cp target\classes monitor.SimpleJVMMonitor %interval%
goto end

:invalid_choice
echo Invalid choice. Please select 1-4.
pause
exit /b 1

:exit
echo Exiting...
goto end

:end
echo.
echo Monitoring session ended.
pause