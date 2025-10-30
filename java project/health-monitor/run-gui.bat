@echo off
REM Java Application Health Monitor - GUI Launcher
REM This script compiles and runs the health monitoring GUI

echo ================================================
echo Java Application Health Monitor - GUI Version
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

REM Create target directories if they don't exist
if not exist "target\classes" mkdir target\classes

echo.
echo Compiling Java source files...
echo --------------------------------

REM Compile monitor classes first (dependencies)
javac -d target\classes -encoding UTF-8 src\monitor\SimpleJVMMonitor.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile SimpleJVMMonitor.java
    pause
    exit /b 1
)

REM Compile GUI components (without ConfigurationDialog first)
javac -d target\classes -encoding UTF-8 -cp target\classes src\gui\ServiceMonitorPanel.java src\gui\ReportsPanel.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile GUI panel components
    pause
    exit /b 1
)

REM Compile main GUI class and ConfigurationDialog together
javac -d target\classes -encoding UTF-8 -cp target\classes src\gui\HealthMonitorGUI.java src\gui\ConfigurationDialog.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile main GUI components
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo --------------------------------

REM Launch the GUI application
echo.
echo Starting Health Monitor GUI...
echo.
java -cp target\classes gui.HealthMonitorGUI

echo.
echo GUI application closed.
pause
