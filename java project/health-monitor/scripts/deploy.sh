#!/bin/bash

# Java Application Health Monitor - Deployment Script
# This script automates the deployment process for the health monitoring system

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
APP_NAME="java-health-monitor"
VERSION="1.0.0"

# Color codes for output formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default configuration
DEFAULT_DEPLOY_DIR="/opt/$APP_NAME"
DEFAULT_USER="healthmonitor"
DEFAULT_SERVICE_NAME="health-monitor"
DEFAULT_LOG_DIR="/var/log/$APP_NAME"
DEFAULT_PID_DIR="/var/run"

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
    echo -e "${BLUE}=====================================${NC}"
    echo -e "${BLUE} Java Health Monitor - Deployment${NC}"
    echo -e "${BLUE}=====================================${NC}"
}

# Function to display usage information
show_usage() {
    echo "Usage: $0 [OPTIONS] COMMAND"
    echo ""
    echo "Commands:"
    echo "  install      Install the health monitor system"
    echo "  uninstall    Remove the health monitor system"
    echo "  update       Update existing installation"
    echo "  start        Start the health monitor service"
    echo "  stop         Stop the health monitor service"
    echo "  restart      Restart the health monitor service"
    echo "  status       Check service status"
    echo ""
    echo "Options:"
    echo "  -d, --deploy-dir DIR     Deployment directory (default: $DEFAULT_DEPLOY_DIR)"
    echo "  -u, --user USER          System user for the service (default: $DEFAULT_USER)"
    echo "  -s, --service-name NAME  Systemd service name (default: $DEFAULT_SERVICE_NAME)"
    echo "  -l, --log-dir DIR        Log directory (default: $DEFAULT_LOG_DIR)"
    echo "  -j, --java-home PATH     Java home directory"
    echo "  -p, --port PORT          Default monitoring port (for firewall)"
    echo "  --no-systemd             Skip systemd service creation"
    echo "  --no-user                Skip user creation"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 install                           # Standard installation"
    echo "  $0 install -d /usr/local/monitor     # Install to custom directory"
    echo "  $0 install --no-systemd              # Install without systemd service"
    echo "  $0 update                            # Update existing installation"
    echo "  $0 uninstall                         # Complete removal"
}

# Function to check if running as root
check_root() {
    if [ "$EUID" -ne 0 ]; then
        print_error "This script must be run as root (use sudo)."
        print_status "Example: sudo $0 $@"
        exit 1
    fi
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if Java is available
    if [ -n "$JAVA_HOME_PATH" ]; then
        JAVA_CMD="$JAVA_HOME_PATH/bin/java"
    else
        JAVA_CMD="java"
    fi
    
    if ! command -v "$JAVA_CMD" &> /dev/null; then
        print_error "Java not found. Please install Java 11 or later."
        print_status "On Ubuntu/Debian: sudo apt update && sudo apt install openjdk-11-jdk"
        print_status "On CentOS/RHEL: sudo yum install java-11-openjdk-devel"
        exit 1
    fi
    
    # Check Java version
    JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | grep -oP 'version "(\d+)' | grep -oP '\d+')
    if [ "$JAVA_VERSION" -lt 11 ]; then
        print_error "Java 11 or later is required. Found Java $JAVA_VERSION."
        exit 1
    fi
    
    print_status "Java $JAVA_VERSION found: $JAVA_CMD"
    
    # Check if Maven is available for building
    if ! command -v mvn &> /dev/null; then
        print_warning "Maven not found. Assuming JAR file is already built."
    fi
    
    # Check if systemctl is available
    if ! command -v systemctl &> /dev/null && [ "$CREATE_SYSTEMD" = true ]; then
        print_warning "systemctl not found. Systemd service will not be created."
        CREATE_SYSTEMD=false
    fi
}

# Function to create system user
create_user() {
    local username="$1"
    
    if [ "$CREATE_USER" = false ]; then
        print_status "Skipping user creation (--no-user specified)"
        return 0
    fi
    
    if id "$username" &>/dev/null; then
        print_status "User '$username' already exists."
    else
        print_status "Creating system user: $username"
        useradd --system --shell /bin/false --home "$DEPLOY_DIR" --create-home "$username"
        
        if [ $? -eq 0 ]; then
            print_status "User '$username' created successfully."
        else
            print_error "Failed to create user '$username'."
            exit 1
        fi
    fi
}

# Function to build the project
build_project() {
    print_status "Building project..."
    
    cd "$PROJECT_DIR" || exit 1
    
    if command -v mvn &> /dev/null; then
        print_status "Building with Maven..."
        mvn clean package -q
        
        if [ $? -ne 0 ]; then
            print_error "Build failed."
            exit 1
        fi
        
        print_status "Build completed successfully."
    else
        JAR_FILE="$PROJECT_DIR/target/$APP_NAME-$VERSION-shaded.jar"
        if [ ! -f "$JAR_FILE" ]; then
            print_error "JAR file not found and Maven is not available."
            print_status "Please build the project manually or install Maven."
            exit 1
        fi
        print_status "Using existing JAR file: $JAR_FILE"
    fi
}

# Function to install the application
install_application() {
    print_status "Installing application to: $DEPLOY_DIR"
    
    # Create deployment directory
    mkdir -p "$DEPLOY_DIR"
    mkdir -p "$DEPLOY_DIR/bin"
    mkdir -p "$DEPLOY_DIR/config"
    mkdir -p "$DEPLOY_DIR/logs"
    mkdir -p "$DEPLOY_DIR/reports"
    mkdir -p "$LOG_DIR"
    
    # Copy JAR file
    JAR_FILE="$PROJECT_DIR/target/$APP_NAME-$VERSION-shaded.jar"
    if [ -f "$JAR_FILE" ]; then
        cp "$JAR_FILE" "$DEPLOY_DIR/bin/"
        print_status "JAR file copied to deployment directory."
    else
        print_error "JAR file not found: $JAR_FILE"
        exit 1
    fi
    
    # Copy startup script
    cp "$SCRIPT_DIR/start_monitor.sh" "$DEPLOY_DIR/bin/"
    chmod +x "$DEPLOY_DIR/bin/start_monitor.sh"
    
    # Create wrapper script
    create_wrapper_script
    
    # Create default configuration
    create_default_config
    
    # Set ownership and permissions
    chown -R "$USER_NAME:$USER_NAME" "$DEPLOY_DIR"
    chown -R "$USER_NAME:$USER_NAME" "$LOG_DIR"
    chmod -R 755 "$DEPLOY_DIR"
    chmod -R 755 "$LOG_DIR"
    
    print_status "Application installed successfully."
}

# Function to create wrapper script
create_wrapper_script() {
    local wrapper_script="/usr/local/bin/$SERVICE_NAME"
    
    print_status "Creating wrapper script: $wrapper_script"
    
    cat > "$wrapper_script" << EOF
#!/bin/bash
# Health Monitor Service Wrapper Script

DEPLOY_DIR="$DEPLOY_DIR"
USER_NAME="$USER_NAME"
JAVA_HOME_PATH="$JAVA_HOME_PATH"

case "\$1" in
    start)
        sudo -u "\$USER_NAME" "\$DEPLOY_DIR/bin/start_monitor.sh" -m continuous -d -p "$DEFAULT_PID_DIR/$SERVICE_NAME.pid"
        ;;
    stop)
        "\$DEPLOY_DIR/bin/start_monitor.sh" stop -p "$DEFAULT_PID_DIR/$SERVICE_NAME.pid"
        ;;
    status)
        "\$DEPLOY_DIR/bin/start_monitor.sh" status -p "$DEFAULT_PID_DIR/$SERVICE_NAME.pid"
        ;;
    restart)
        "\$0" stop
        sleep 2
        "\$0" start
        ;;
    interactive)
        sudo -u "\$USER_NAME" "\$DEPLOY_DIR/bin/start_monitor.sh" -m interactive
        ;;
    report)
        sudo -u "\$USER_NAME" "\$DEPLOY_DIR/bin/start_monitor.sh" -m report -o "\$DEPLOY_DIR/reports/report-\$(date +%Y%m%d-%H%M%S).json"
        ;;
    *)
        echo "Usage: \$0 {start|stop|status|restart|interactive|report}"
        exit 1
        ;;
esac
EOF
    
    chmod +x "$wrapper_script"
    print_status "Wrapper script created: $wrapper_script"
}

# Function to create default configuration
create_default_config() {
    local config_file="$DEPLOY_DIR/config/monitored-services.txt"
    
    print_status "Creating default configuration: $config_file"
    
    cat > "$config_file" << EOF
# Monitored Services Configuration
# Format: SERVICE_NAME,URL,EXPECTED_STATUS_CODE
# Lines starting with # are comments

# Example external services (safe to monitor)
Google,https://www.google.com,200
GitHub API,https://api.github.com,200
JSONPlaceholder,https://jsonplaceholder.typicode.com/posts/1,200

# Add your own services here:
# Application Health,http://localhost:8080/actuator/health,200
# Database API,http://your-db:5432/health,200
# Message Queue,http://your-mq:15672/api/health,200

# Notes:
# - Remove or comment out the example services above
# - Add your actual application endpoints
# - Use /health, /actuator/health, or similar endpoints
# - Ensure the expected status code is correct (usually 200)
EOF
    
    # Create logging configuration
    local log_config="$DEPLOY_DIR/config/logging.properties"
    cat > "$log_config" << EOF
# Logging Configuration
org.slf4j.simpleLogger.defaultLogLevel=INFO
org.slf4j.simpleLogger.log.monitor=DEBUG
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd HH:mm:ss
org.slf4j.simpleLogger.showThreadName=false
org.slf4j.simpleLogger.showLogName=true
org.slf4j.simpleLogger.showShortLogName=false
EOF
    
    print_status "Configuration files created."
}

# Function to create systemd service
create_systemd_service() {
    if [ "$CREATE_SYSTEMD" = false ]; then
        print_status "Skipping systemd service creation (--no-systemd specified)"
        return 0
    fi
    
    local service_file="/etc/systemd/system/$SERVICE_NAME.service"
    
    print_status "Creating systemd service: $service_file"
    
    cat > "$service_file" << EOF
[Unit]
Description=Java Application Health Monitor
Documentation=https://github.com/your-org/java-health-monitor
Wants=network-online.target
After=network-online.target
RequiresMountsFor=$DEPLOY_DIR

[Service]
Type=forking
User=$USER_NAME
Group=$USER_NAME
WorkingDirectory=$DEPLOY_DIR
Environment=JAVA_HOME=$JAVA_HOME_PATH
Environment=LOG_DIR=$LOG_DIR

ExecStart=$DEPLOY_DIR/bin/start_monitor.sh -m continuous -d -p $DEFAULT_PID_DIR/$SERVICE_NAME.pid -s $DEPLOY_DIR/config/monitored-services.txt
ExecStop=$DEPLOY_DIR/bin/start_monitor.sh stop -p $DEFAULT_PID_DIR/$SERVICE_NAME.pid
ExecReload=/bin/kill -HUP \$MAINPID

PIDFile=$DEFAULT_PID_DIR/$SERVICE_NAME.pid
TimeoutStartSec=30
TimeoutStopSec=10
Restart=always
RestartSec=10

# Security settings
NoNewPrivileges=yes
ProtectSystem=strict
ProtectHome=yes
ReadWritePaths=$DEPLOY_DIR $LOG_DIR $DEFAULT_PID_DIR
PrivateTmp=yes

# Resource limits
LimitNOFILE=65536
MemoryMax=1G

[Install]
WantedBy=multi-user.target
EOF
    
    # Reload systemd and enable service
    systemctl daemon-reload
    systemctl enable "$SERVICE_NAME"
    
    print_status "Systemd service created and enabled."
}

# Function to install firewall rules (optional)
setup_firewall() {
    if [ -n "$MONITOR_PORT" ]; then
        print_status "Setting up firewall rules for port $MONITOR_PORT..."
        
        if command -v ufw &> /dev/null; then
            ufw allow "$MONITOR_PORT"/tcp
            print_status "UFW rule added for port $MONITOR_PORT"
        elif command -v firewall-cmd &> /dev/null; then
            firewall-cmd --permanent --add-port="$MONITOR_PORT"/tcp
            firewall-cmd --reload
            print_status "Firewalld rule added for port $MONITOR_PORT"
        else
            print_warning "No firewall management tool found. Please configure firewall manually if needed."
        fi
    fi
}

# Function to uninstall the application
uninstall_application() {
    print_status "Uninstalling health monitor..."
    
    # Stop service if running
    if [ "$CREATE_SYSTEMD" = true ] && systemctl is-active --quiet "$SERVICE_NAME"; then
        print_status "Stopping service..."
        systemctl stop "$SERVICE_NAME"
    fi
    
    # Disable and remove systemd service
    if [ "$CREATE_SYSTEMD" = true ] && [ -f "/etc/systemd/system/$SERVICE_NAME.service" ]; then
        print_status "Removing systemd service..."
        systemctl disable "$SERVICE_NAME"
        rm -f "/etc/systemd/system/$SERVICE_NAME.service"
        systemctl daemon-reload
    fi
    
    # Remove wrapper script
    rm -f "/usr/local/bin/$SERVICE_NAME"
    
    # Remove deployment directory
    if [ -d "$DEPLOY_DIR" ]; then
        print_status "Removing deployment directory: $DEPLOY_DIR"
        rm -rf "$DEPLOY_DIR"
    fi
    
    # Remove log directory (optional - ask user)
    if [ -d "$LOG_DIR" ]; then
        read -p "Remove log directory $LOG_DIR? [y/N]: " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            rm -rf "$LOG_DIR"
            print_status "Log directory removed."
        fi
    fi
    
    # Remove user (optional - ask user)
    if id "$USER_NAME" &>/dev/null; then
        read -p "Remove system user $USER_NAME? [y/N]: " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            userdel "$USER_NAME"
            print_status "User removed."
        fi
    fi
    
    print_status "Uninstallation completed."
}

# Function to update the application
update_application() {
    print_status "Updating health monitor..."
    
    # Stop service if running
    if [ "$CREATE_SYSTEMD" = true ] && systemctl is-active --quiet "$SERVICE_NAME"; then
        print_status "Stopping service for update..."
        systemctl stop "$SERVICE_NAME"
        SERVICE_WAS_RUNNING=true
    fi
    
    # Build new version
    build_project
    
    # Update JAR file
    JAR_FILE="$PROJECT_DIR/target/$APP_NAME-$VERSION-shaded.jar"
    if [ -f "$JAR_FILE" ]; then
        cp "$JAR_FILE" "$DEPLOY_DIR/bin/"
        print_status "JAR file updated."
    else
        print_error "JAR file not found: $JAR_FILE"
        exit 1
    fi
    
    # Update startup script
    cp "$SCRIPT_DIR/start_monitor.sh" "$DEPLOY_DIR/bin/"
    chmod +x "$DEPLOY_DIR/bin/start_monitor.sh"
    
    # Set ownership
    chown -R "$USER_NAME:$USER_NAME" "$DEPLOY_DIR"
    
    # Restart service if it was running
    if [ "$SERVICE_WAS_RUNNING" = true ]; then
        print_status "Starting service after update..."
        systemctl start "$SERVICE_NAME"
    fi
    
    print_status "Update completed successfully."
}

# Function to control service
control_service() {
    local action="$1"
    
    case $action in
        start)
            if [ "$CREATE_SYSTEMD" = true ]; then
                systemctl start "$SERVICE_NAME"
                print_status "Service started."
            else
                "$SERVICE_NAME" start
            fi
            ;;
        stop)
            if [ "$CREATE_SYSTEMD" = true ]; then
                systemctl stop "$SERVICE_NAME"
                print_status "Service stopped."
            else
                "$SERVICE_NAME" stop
            fi
            ;;
        restart)
            if [ "$CREATE_SYSTEMD" = true ]; then
                systemctl restart "$SERVICE_NAME"
                print_status "Service restarted."
            else
                "$SERVICE_NAME" restart
            fi
            ;;
        status)
            if [ "$CREATE_SYSTEMD" = true ]; then
                systemctl status "$SERVICE_NAME"
            else
                "$SERVICE_NAME" status
            fi
            ;;
    esac
}

# Main function
main() {
    # Default values
    local deploy_dir="$DEFAULT_DEPLOY_DIR"
    local user_name="$DEFAULT_USER"
    local service_name="$DEFAULT_SERVICE_NAME"
    local log_dir="$DEFAULT_LOG_DIR"
    local java_home_path=""
    local monitor_port=""
    local create_systemd=true
    local create_user=true
    local command=""
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -d|--deploy-dir)
                deploy_dir="$2"
                shift 2
                ;;
            -u|--user)
                user_name="$2"
                shift 2
                ;;
            -s|--service-name)
                service_name="$2"
                shift 2
                ;;
            -l|--log-dir)
                log_dir="$2"
                shift 2
                ;;
            -j|--java-home)
                java_home_path="$2"
                shift 2
                ;;
            -p|--port)
                monitor_port="$2"
                shift 2
                ;;
            --no-systemd)
                create_systemd=false
                shift
                ;;
            --no-user)
                create_user=false
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            install|uninstall|update|start|stop|restart|status)
                command="$1"
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Set global variables
    DEPLOY_DIR="$deploy_dir"
    USER_NAME="$user_name"
    SERVICE_NAME="$service_name"
    LOG_DIR="$log_dir"
    JAVA_HOME_PATH="$java_home_path"
    MONITOR_PORT="$monitor_port"
    CREATE_SYSTEMD="$create_systemd"
    CREATE_USER="$create_user"
    
    # Validate command
    if [ -z "$command" ]; then
        print_error "No command specified."
        show_usage
        exit 1
    fi
    
    print_header
    
    # Execute command
    case $command in
        install)
            check_root
            check_prerequisites
            create_user "$USER_NAME"
            build_project
            install_application
            create_systemd_service
            setup_firewall
            print_status "Installation completed successfully!"
            print_status "Start the service with: sudo systemctl start $SERVICE_NAME"
            print_status "Or use: sudo $SERVICE_NAME start"
            ;;
        uninstall)
            check_root
            uninstall_application
            ;;
        update)
            check_root
            check_prerequisites
            update_application
            ;;
        start|stop|restart|status)
            control_service "$command"
            ;;
        *)
            print_error "Unknown command: $command"
            show_usage
            exit 1
            ;;
    esac
}

# Handle script interruption
trap 'print_warning "Script interrupted. Cleaning up..."; exit 130' INT TERM

# Execute main function with all arguments
main "$@"