#!/bin/bash

HE_LOG_LOCATION="${HE_LOG_LOCATION:="/logs/"}"
HE_LOG="$HE_LOG_LOCATION/eric-cnam-onboarding.log"

export HE_LOG


cmd_wrapper(){
    # wrapper function works with two positional arguments
    # first one is the command to execute and second for
    # managing script termination.
    # First argument would be parsed with IFS to determine
    # its stdout, stderr and rc_code accordingly and for
    # wrapping up with specific msg format.
    # Second argument can by only "skip_rc" for omit raising an script exit
    local cmd="$1"
    local rc="$2"
    if [[ ! -z $rc ]] && [[ $rc != "skip_rc" ]]; then
        echo "Wrapper format is incorrect. Usage: 'cmd_wrapper \"command\" - for regular flows or cmd_wrapper \"command\" \"skip_rc\" - for skipping possible return code"
        exit 1
    fi
    {
        IFS=$'\n' read -r -d '' std_err;
        IFS= read -r -d '' return_code;
        IFS=$'\n' read -r -d '' std_out;
    } < <((printf '\0%s\n\0' "$($cmd; printf '\0%d' "${?}" 1>&2)" 1>&2) 2>&1)
    msg="{\"timestamp\":\"$(date +"%G-%m-%dT%T.%3N%:z")\",\"version\":1.0.0,`
          `\"message\":\"$std_out,err:$std_err,rc:$return_code,cmd:$(replace_sensitive_data "$cmd")\",`
          `\"thread\":\"$(basename "$0")\",`
          `\"service_id\":\"unknown\",`
          `\"severity\":\"info\"}"
    if [[ $return_code -gt 0 ]]
    then
        if [[ $rc == "skip_rc" ]]
        then
            echo "$msg" | tee -a "$HE_LOG"
            return 1
        else
            echo "$msg" | tee -a "$HE_LOG"
            exit "$return_code"
        fi
    else
        echo "$msg" | tee -a "$HE_LOG"
        return 0
    fi
}


replace_sensitive_data(){
    local msg="$1"
    msg=$(echo $msg | sed "s/password [^[:space:]]*/password xxxxxxxx/g;s/password=[^[:space:]]*/password=******/g")
    msg=$(echo $msg | sed "s/username [^[:space:]]*/username xxxxxxxx/g;s/username=[^[:space:]]*/username=******/g")
    echo $msg
}


oci_registry_connect(){
    if [ -z $OCI_REGISTRY_URL ] || [ -z $OCI_REGISTRY_USER ] || [ -z $OCI_REGISTRY_PASS ]; then
        cmd_wrapper "echo OCI registry credentials are not present"
    else
        cmd_wrapper "oras login $OCI_REGISTRY_URL --username $OCI_REGISTRY_USER --password $OCI_REGISTRY_PASS"
        cmd_wrapper "helm registry login $OCI_REGISTRY_URL --username $OCI_REGISTRY_USER --password $OCI_REGISTRY_PASS"

    fi
}


main(){
    cmd_wrapper "echo Start Docker entrypoint"
    oci_registry_connect
    exec "$@"
}

main "$@"

