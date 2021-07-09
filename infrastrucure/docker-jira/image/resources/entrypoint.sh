#!/bin/bash

function main() {
    local fqdn_value=file_env "FQDN"
    local db_username_value=file_env "DB_USERNAME"
    local db_username_value=file_env "DB_PASSWORD"

    xmlstarlet ed -L -u "/Server/Service/Connector[@proxyName='{subdomain}.{domain}.com']/@proxyName" \
        -v "$fqdn_value" /opt/atlassian-jira-software-standalone/conf/server.xml
    xmlstarlet ed -L -u "/jira-database-config/jdbc-datasource/username" \
        -v "$db_username_value" /app/dbconfig.xml
    xmlstarlet ed -L -u "/jira-database-config/jdbc-datasource/password" \
        -v "$db_username_value" /app/dbconfig.xml

    install -ojira -gjira -m660 /app/dbconfig.xml /var/jira/dbconfig.xml
    /opt/atlassian-jira-software-standalone/bin/setenv.sh run
    /opt/atlassian-jira-software-standalone/bin/start-jira.sh run
}


# usage: file_env VAR [DEFAULT]
#    ie: file_env 'XYZ_DB_PASSWORD' 'example'
# (will allow for "$XYZ_DB_PASSWORD_FILE" to fill in the value of
#  "$XYZ_DB_PASSWORD" from a file, especially for Docker's secrets feature)
function file_env() {
    local var="$1"
    local fileVar="${var}_FILE"
    local def="${2:-}"
    local varValue=$(env | grep -E "^${var}=" | sed -E -e "s/^${var}=//")
    local fileVarValue=$(env | grep -E "^${fileVar}=" | sed -E -e "s/^${fileVar}=//")
    if [ -n "${varValue}" ] && [ -n "${fileVarValue}" ]; then
        echo >&2 "error: both $var and $fileVar are set (but are exclusive)"
        exit 1
    fi
    if [ -n "${varValue}" ]; then
        export "$var"="${varValue}"
    elif [ -n "${fileVarValue}" ]; then
        export "$var"="$(cat "${fileVarValue}")"
    elif [ -n "${def}" ]; then
        export "$var"="$def"
    fi
    unset "$fileVar"
}

main
