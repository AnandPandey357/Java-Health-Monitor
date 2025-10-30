#!/bin/bash

# Java Application Health Monitor - Start Script
# This script starts the health monitoring system with various configuration options

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
JAR_NAME="java-health-monitor-1.0.0-shaded.jar"
JAR_PATH="$PROJECT_DIR/target/$JAR_NAME"
LOGS_DIR="$PROJECT_DIR/logs"
REPORTS_DIR="$PROJECT_DIR/reports"

# Color codes for output formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default configuration
DEFAULT_MODE="interactive"
DEFAULT_INTERVAL=30
DEFAULT_SERVICES_FILE="$PROJECT_DIR/monitored-services.txt"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE} Java Application Health Monitor${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Function to display usage information
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -m, --mode MODE          Monitoring mode: interactive, continuous, once, report"
    echo "  -i, --interval SECONDS   Monitoring interval in seconds (default: 30)"
    echo "  -s, --services FILE      File containing services to monitor"
    echo "  -o, --output PATH        Output path for reports"
    echo "  -j, --java-home PATH     Java home directory"
    echo "  -d, --daemon             Run as daemon (background process)"
    echo "  -p, --pid-file FILE      PID file for daemon mode"
    echo "  -l, --log-level LEVEL    Log level: DEBUG, INFO, WARN, ERROR"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Modes:"
    echo "  interactive    - Interactive command-line interface (default)"
    echo "  continuous     - Continuous monitoring until stopped"
    echo "  once          - Single metrics collection and exit"
    echo "  report        - Generate report and exit"
    echo ""
    echo "Examples:"
    echo "  $0                                     # Start interactive mode"
    echo "  $0 -m continuous -i 60                # Continuous monitoring every 60 seconds"
    echo "  $0 -m once                            # Single metrics collection"
    echo "  $0 -m report -o ./my-report.json      # Generate report"
    echo "  $0 -d -p /var/run/health-monitor.pid  # Run as daemon"
}

# Function to check if Java is available
check_java() {
    if [ -n "$JAVA_HOME_PATH" ]; then
        JAVA_CMD="$JAVA_HOME_PATH/bin/java"
    else
        JAVA_CMD="java"
    fi
    
    if ! command -v "$JAVA_CMD" &> /dev/null; then
        print_error "Java not found. Please install Java 11 or later or set JAVA_HOME."
        exit 1
    fi
    
    # Check Java version
    JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | grep -oP 'version "(\d+)' | grep -oP '\d+')
    if [ "$JAVA_VERSION" -lt 11 ]; then
        print_error "Java 11 or later is required. Found Java $JAVA_VERSION."
        exit 1
    fi
    
    print_status "Using Java $JAVA_VERSION from: $JAVA_CMD"
}

# Function to build the project if JAR doesn't exist
build_project() {
    if [ ! -f "$JAR_PATH" ]; then
        print_warning "JAR file not found. Building project..."
        
        cd "$PROJECT_DIR" || exit 1
        
        if command -v mvn &> /dev/null; then
            print_status "Building with Maven..."
            mvn clean package -q
        else
            print_error "Maven not found. Please install Maven or build the project manually."
            exit 1
        fi
        
        if [ ! -f "$JAR_PATH" ]; then
            print_error "Build failed. JAR file not created."
            exit 1
        fi
        
        print_status "Build completed successfully."
    fi
}

# Function to create necessary directories
setup_directories() {
    mkdir -p "$LOGS_DIR"
    mkdir -p "$REPORTS_DIR"
    print_status "Created directories: logs, reports"
}

# Function to create default services file if it doesn't exist
create_default_services() {
    if [ ! -f "$DEFAULT_SERVICES_FILE" ]; then
        print_status "Creating default services configuration..."
        cat > "$DEFAULT_SERVICES_FILE" << EOF
# Monitored Services Configuration
# Format: SERVICE_NAME,URL,EXPECTED_STATUS_CODE
# Lines starting with # are comments

Google,https://www.google.com,200
GitHub API,https://api.github.com,200
JSONPlaceholder,https://jsonplaceholder.typicode.com/posts/1,200

# Add your own services here:
# My Service,http://localhost:8080/health,200
# Database API,http://mydb:5432/ping,200
EOF
        print_status "Default services file created: $DEFAULT_SERVICES_FILE"
        print_status "Edit this file to add your own services to monitor."
    fi
}

# Function to load services from file
load_services() {
    local services_file="$1"
    local services_args=""
    
    if [ -f "$services_file" ]; then
        print_status "Loading services from: $services_file"
        
        # Read services file and create JVM arguments
        while IFS=',' read -r name url status_code || [ -n "$name" ]; do
            # Skip comments and empty lines
            if [[ "$name" =~ ^[[:space:]]*# ]] || [[ -z "$name" ]]; then
                continue
            fi
            
            # Trim whitespace
            name=$(echo "$name" | xargs)
            url=$(echo "$url" | xargs)
            status_code=$(echo "$status_code" | xargs)
            
            if [ -n "$name" ] && [ -n "$url" ]; then
                if [ -z "$status_code" ]; then
                    status_code="200"
                fi
                services_args="$services_args -Dservice.$name.url=$url -Dservice.$name.status=$status_code"
            fi
        done < "$services_file"
    fi
    
    echo "$services_args"
}

# Function to start monitoring in daemon mode
start_daemon() {
    local pid_file="$1"
    local java_args="$2"
    
    print_status "Starting health monitor as daemon..."
    
    # Start the Java process in background
    nohup "$JAVA_CMD" $java_args > "$LOGS_DIR/health-monitor.log" 2>&1 &
    local pid=$!
    
    # Save PID to file
    echo $pid > "$pid_file"
    print_status "Health monitor started with PID: $pid"
    print_status "PID saved to: $pid_file"
    print_status "Logs: $LOGS_DIR/health-monitor.log"
}

# Function to check if daemon is running
check_daemon() {
    local pid_file="$1"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p "$pid" > /dev/null 2>&1; then
            return 0  # Running
        else
            rm -f "$pid_file"  # Clean up stale PID file
            return 1  # Not running
        fi
    else
        return 1  # Not running
    fi
}

# Function to stop daemon
stop_daemon() {
    local pid_file="$1"
    
    if check_daemon "$pid_file"; then
        local pid=$(cat "$pid_file")
        print_status "Stopping health monitor (PID: $pid)..."
        
        kill "$pid"
        
        # Wait for process to stop
        for i in {1..10}; do
            if ! ps -p "$pid" > /dev/null 2>&1; then
                rm -f "$pid_file"
                print_status "Health monitor stopped successfully."
                return 0
            fi
            sleep 1
        done
        
        # Force kill if still running
        print_warning "Process didn't stop gracefully. Force killing..."
        kill -9 "$pid"
        rm -f "$pid_file"
        print_status "Health monitor force stopped."
    else
        print_warning "Health monitor is not running."
    fi
}

# Main function
main() {
    # Parse command line arguments
    local mode="$DEFAULT_MODE"
    local interval="$DEFAULT_INTERVAL"
    local services_file="$DEFAULT_SERVICES_FILE"
    local output_path=""
    local daemon_mode=false
    local pid_file="/var/run/health-monitor.pid"
    local log_level="INFO"
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            -m|--mode)
                mode="$2"
                shift 2
                ;;
            -i|--interval)
                interval="$2"
                shift 2
                ;;
            -s|--services)
                services_file="$2"
                shift 2
                ;;
            -o|--output)
                output_path="$2"
                shift 2
                ;;
            -j|--java-home)
                JAVA_HOME_PATH="$2"
                shift 2
                ;;
            -d|--daemon)
                daemon_mode=true
                shift
                ;;
            -p|--pid-file)
                pid_file="$2"
                shift 2
                ;;
            -l|--log-level)
                log_level="$2"
                shift 2
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            stop)
                stop_daemon "$pid_file"
                exit 0
                ;;
            status)
                if check_daemon "$pid_file"; then
                    local pid=$(cat "$pid_file")
                    print_status "Health monitor is running with PID: $pid"
                else
                    print_status "Health monitor is not running."
                fi
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    print_header
    
    # Validate mode
    case $mode in
        interactive|continuous|once|report)
            ;;
        *)
            print_error "Invalid mode: $mode"
            show_usage
            exit 1
            ;;
    esac
    
    # Check prerequisites
    check_java
    build_project
    setup_directories
    create_default_services
    
    # Load services configuration
    local services_args=$(load_services "$services_file")
    
    # Build Java command arguments
    local java_args="-jar $JAR_PATH"
    
    # Add mode-specific arguments
    case $mode in
        continuous)
            java_args="$java_args --continuous"
            ;;
        once)
            java_args="$java_args --once"
            ;;
        report)
            if [ -n "$output_path" ]; then
                java_args="$java_args --report $output_path"
            else
                java_args="$java_args --report $REPORTS_DIR/health-report-$(date +%Y%m%d-%H%M%S).json"
            fi
            ;;
    esac
    
    # Add services configuration
    java_args="$java_args $services_args"
    
    # Add system properties
    java_args="-Dmonitoring.interval=$interval -Dlog.level=$log_level $java_args"
    
    print_status "Mode: $mode"
    print_status "Interval: $interval seconds"
    print_status "Services file: $services_file"
    
    # Start monitoring
    if [ "$daemon_mode" = true ]; then
        if [ "$mode" = "interactive" ]; then
            print_error "Interactive mode cannot be run as daemon. Use continuous mode instead."
            exit 1
        fi
        
        if check_daemon "$pid_file"; then
            print_error "Health monitor is already running."
            exit 1
        fi
        
        start_daemon "$pid_file" "$java_args"
    else
        print_status "Starting health monitor..."
        print_status "Command: $JAVA_CMD $java_args"
        
        # Execute the Java application
        "$JAVA_CMD" $java_args
    fi
}

# Handle script interruption
trap 'print_warning "Script interrupted. Cleaning up..."; exit 130' INT TERM

# Execute main function with all arguments
main "$@"