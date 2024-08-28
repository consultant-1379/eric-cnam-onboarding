#!/bin/env bash
#
#  clear    - remove sslcainfo and sslverify configuration
#  insecure - set sslverify true
#  secure   - set sslcainfo key 
#  show     - show current git configuration
#
#  without arguments - check local git repo connection

DIRECTORY="$( cd "$(dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd)"
KEY_NAME="git_repo_selfsigned.key"
KEY_NAME_PATH="$DIRECTORY/$KEY_NAME"

error(){
    local message="$1"
    local red="\033[1;31m"
    local no_color="\033[0m"
    echo -e "${red}[Error] $message ${no_color}" >&2
    exit 1
}


info(){
    local message="$1"
    echo -e "[INFO] $message"
}


source "$DIRECTORY/.env"

if [[ -z "$GITREPO_PORT" ]]
  then
    error "Failed to parse ports"
fi

GITREPO_READY=$(curl --silent localhost:"$GITREPO_PORT"/ping)

if [[ $GITREPO_READY != "pong" ]]
  then
    error "Failed to connect to local git repo. Start it first"
fi

case "$1" in
    clear)
      info "Clear all configuration related to local git operation"
      git config --global --unset http.sslVerify
      git config --global --unset http.sslCAInfo
      ;;
    insecure)
      info "Activate insecure git connection"
      git config --global --unset http.sslCAInfo
      git config --global http.sslVerify false
      ;;
    secure)
      info "Activate secured git connection by adding it configuration"
      \rm -rf "$KEY_NAME_PATH"
      openssl s_client -showcerts -connect localhost:"$GITREPO_PORT" </dev/null 2>/dev/null | openssl x509 -outform PEM > "$KEY_NAME_PATH"
      git config --global --unset http.sslCAInfo
      git config --global http.sslVerify true
      git config --global http.sslCAInfo "$KEY_NAME_PATH"
      ;;
    show)
      info "Print git global configuration"
      git config --global --list
      ;;
    *?)
      error "Usage: $(basename "$0") [clear] [insecure] [secure] [show]"
      ;;
esac

exit 0
