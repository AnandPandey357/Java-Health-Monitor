@echo off
REM Health Monitor GUI Compilation and Launch Script
REM This script compiles all GUI components and launches the health monitor application

echo.
echo =========================================
echo   Java Health Monitor - GUI Launcher
echo =========================================
echo.

REM Change to the health monitor directory
cd /d "c:\Users\anand\Desktop\java project\health-monitor"

echo 📂 Compiling Java source files...
echo.

REM Create output directory for compiled classes
if not exist "bin" mkdir bin

REM Compile all Java files with proper classpath
echo ⚡ Compiling monitor package...
javac -d bin -cp bin src\monitor\*.java

if %ERRORLEVEL% neq 0 (
    echo ❌ Error compiling monitor package
    pause
    exit /b 1
)

echo ⚡ Compiling GUI package...
javac -d bin -cp bin src\gui\*.java

if %ERRORLEVEL% neq 0 (
    echo ❌ Error compiling GUI package
    pause
    exit /b 1
)

echo ⚡ Compiling utils package...
javac -d bin -cp bin src\utils\*.java

if %ERRORLEVEL% neq 0 (
    echo ❌ Error compiling utils package
    pause
    exit /b 1
)

echo.
echo ✅ Compilation successful!
echo.

REM Check if user wants to run CLI or GUI
echo Choose monitoring mode:
echo 1) CLI Mode (Command Line Interface)
echo 2) GUI Mode (Graphical User Interface)
echo 3) Both (CLI in background, GUI in foreground)
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo 🖥️ Starting CLI Health Monitor...
    echo.
    java -cp bin monitor.SimpleJVMMonitor
) else if "%choice%"=="2" (
    echo.
    echo 🖼️ Starting GUI Health Monitor...
    echo.
    java -cp bin gui.HealthMonitorGUI
) else if "%choice%"=="3" (
    echo.
    echo 🚀 Starting both CLI and GUI modes...
    echo.
    echo Starting CLI in background...
    start "Health Monitor CLI" java -cp bin monitor.SimpleJVMMonitor
    timeout /t 2 /nobreak > nul
    echo Starting GUI in foreground...
    java -cp bin gui.HealthMonitorGUI
) else (
    echo.
    echo ❌ Invalid choice. Starting GUI mode by default...
    echo.
    java -cp bin gui.HealthMonitorGUI
)

echo.
echo 🏁 Health Monitor has been stopped.
echo.
pause