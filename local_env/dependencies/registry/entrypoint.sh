#!/bin/bash

set -o errexit

info(){
    local message="$1"
    local green="\033[1;32m"
    local no_color="\033[0m"
    echo -e "${green}[INFO] $message ${no_color}"
}

info "Local registry is available: http://localhost:$PORT"

registry serve /etc/docker/registry/config.yml
