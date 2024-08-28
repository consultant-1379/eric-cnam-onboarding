#!/bin/bash

set -o errexit

GIT_PROJECT_ROOT="/var/lib/git"
GIT_INITIAL_ROOT="/tmp/local-git-repo"
GIT_USER="git"
GIT_GROUP="git"


note(){
    local message="$1"
    local green="\033[1;32m"
    local no_color="\033[0m"
    echo -e "${green}[INFO] $message ${no_color}"
}


info(){
    local message="$1"
    echo -e "[INFO] $message"
}


initialize_initial_repository(){
    \rm -rf $GIT_INITIAL_ROOT && mkdir -p $GIT_INITIAL_ROOT
    pushd $GIT_INITIAL_ROOT >/dev/null
    git init &>/dev/null
    git update-server-info
    echo "Initail message" > README.md >/dev/null
    git add README.md >/dev/null
    git commit -m "first commit" >/dev/null
    git clone --bare $GIT_INITIAL_ROOT $GIT_PROJECT_ROOT/"$(basename $GIT_INITIAL_ROOT)".git &>/dev/null
    popd >/dev/null
    info "Local git repository started"
    note "You can clone via HTTP:  http://localhost:$PORT/$(basename $GIT_INITIAL_ROOT).git"
    note "You can clone via HTTPS: https://localhost:$PORT/$(basename $GIT_INITIAL_ROOT).git"

}


initialize_service(){
    if [[ $(stat -c %A ${GIT_PROJECT_ROOT}) != "drwxrwxr-x" ]]
    then
        chown -R $GIT_USER:$GIT_GROUP $GIT_PROJECT_ROOT
        chmod -R 775 $GIT_PROJECT_ROOT
    fi
    /usr/bin/spawn-fcgi -s "/var/run/fcgiwrap.socket" \
                        -F 2 \
                        -u nginx \
                        -g nginx \
                        -U nginx \
                        -G $GIT_GROUP -- \
                        "/usr/bin/fcgiwrap"
    exec nginx
}


generate_pass(){
    if [[ $AUTH = "true" ]]
    then
        info "Base authentifaction was chosen. Generating credentials for user: $USER"
        htpasswd -cb /etc/nginx/hwpasswd "$USER" "$PASS"
        sed -i '/auth_basic*/s/^#//g' /etc/nginx/sites-enabled/git-repo
    else
        info "No password authentification chosen"
    fi
}


main() {
    generate_pass
    initialize_initial_repository
    initialize_service
}

main

