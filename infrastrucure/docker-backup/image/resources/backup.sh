#!/bin/bash

set -o pipefail

function main() {
    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY
    file_env POSTGRES_DB
    file_env POSTGRES_PASSWORD
    file_env POSTGRES_USER
    file_env RESTIC_DAYS_TO_KEEP 14

    #backup-roles 'oc_' TODO: lookup for jira roles (Also in init.sh & restore.sh)
    backup-db-dump
    backup-fs-from-directory '/var/backups/' 'data/'
}

source /usr/local/lib/functions.sh
source /usr/local/lib/file-functions.sh

main