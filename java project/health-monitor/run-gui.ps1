# Java Application Health Monitor - GUI Launcher (PowerShell)
# This script compiles and runs the health monitoring GUI

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Java Application Health Monitor - GUI Version" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# Check if Java is available
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java not found. Please install Java 11 or later." -ForegroundColor Red
    Write-Host "Download from: https://adoptium.net/" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Set the project directory
$PROJECT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $PROJECT_DIR

# Create target directories if they don't exist
if (-not (Test-Path "target\classes")) {
    New-Item -ItemType Directory -Path "target\classes" -Force | Out-Null
}

Write-Host "`nCompiling Java source files..." -ForegroundColor Yellow
Write-Host "--------------------------------" -ForegroundColor Yellow

# Compile monitor classes first (dependencies)
Write-Host "Compiling SimpleJVMMonitor..." -ForegroundColor Gray
javac -d target\classes -encoding UTF-8 src\monitor\SimpleJVMMonitor.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to compile SimpleJVMMonitor.java" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Compile GUI panel components (without ConfigurationDialog first)
Write-Host "Compiling GUI panels..." -ForegroundColor Gray
javac -d target\classes -encoding UTF-8 -cp target\classes src\gui\ServiceMonitorPanel.java src\gui\ReportsPanel.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to compile GUI panel components" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Compile main GUI class and ConfigurationDialog together
Write-Host "Compiling main GUI components..." -ForegroundColor Gray
javac -d target\classes -encoding UTF-8 -cp target\classes src\gui\HealthMonitorGUI.java src\gui\ConfigurationDialog.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to compile main GUI components" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "`nCompilation successful!" -ForegroundColor Green
Write-Host "--------------------------------" -ForegroundColor Yellow

# Launch the GUI application
Write-Host "`nStarting Health Monitor GUI..." -ForegroundColor Cyan
Write-Host ""
java -cp target\classes gui.HealthMonitorGUI

Write-Host "`nGUI application closed." -ForegroundColor Yellow
Read-Host "Press Enter to exit"
