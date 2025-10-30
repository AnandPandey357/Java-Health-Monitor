# Java Application Health Monitor - PowerShell Script
# This script provides an easy way to run the health monitor on Windows

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Java Application Health Monitor - PowerShell" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# Set the project directory
$ProjectDir = $PSScriptRoot
Set-Location $ProjectDir

Write-Host "Project directory: $ProjectDir" -ForegroundColor Green

# Check if Java is available
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java not found. Please install Java 11 or later." -ForegroundColor Red
    Write-Host "Download from: https://adoptium.net/" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if classes are compiled
if (-not (Test-Path "target\classes\monitor\SimpleJVMMonitor.class")) {
    Write-Host "Compiling Java classes..." -ForegroundColor Yellow
    
    if (-not (Test-Path "target\classes")) {
        New-Item -ItemType Directory -Path "target\classes" -Force | Out-Null
    }
    
    javac -d target\classes src\monitor\SimpleJVMMonitor.java
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Compilation failed." -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
    
    Write-Host "Compilation successful!" -ForegroundColor Green
}

# Display menu
do {
    Write-Host ""
    Write-Host "Select monitoring mode:" -ForegroundColor Cyan
    Write-Host "1. Single metrics collection" -ForegroundColor White
    Write-Host "2. Continuous monitoring (30 seconds interval)" -ForegroundColor White
    Write-Host "3. Continuous monitoring (custom interval)" -ForegroundColor White
    Write-Host "4. Exit" -ForegroundColor White
    Write-Host ""
    
    $choice = Read-Host "Enter your choice (1-4)"
    
    switch ($choice) {
        "1" {
            Write-Host ""
            Write-Host "Running single metrics collection..." -ForegroundColor Yellow
            java -cp target\classes monitor.SimpleJVMMonitor
            break
        }
        "2" {
            Write-Host ""
            Write-Host "Starting continuous monitoring (30 seconds interval)..." -ForegroundColor Yellow
            Write-Host "Press Ctrl+C to stop" -ForegroundColor Red
            java -cp target\classes monitor.SimpleJVMMonitor 30
            break
        }
        "3" {
            Write-Host ""
            $interval = Read-Host "Enter monitoring interval in seconds"
            Write-Host "Starting continuous monitoring ($interval seconds interval)..." -ForegroundColor Yellow
            Write-Host "Press Ctrl+C to stop" -ForegroundColor Red
            java -cp target\classes monitor.SimpleJVMMonitor $interval
            break
        }
        "4" {
            Write-Host "Exiting..." -ForegroundColor Green
            exit 0
        }
        default {
            Write-Host "Invalid choice. Please select 1-4." -ForegroundColor Red
        }
    }
} while ($choice -ne "4")

Write-Host ""
Write-Host "Monitoring session ended." -ForegroundColor Green
Read-Host "Press Enter to exit"