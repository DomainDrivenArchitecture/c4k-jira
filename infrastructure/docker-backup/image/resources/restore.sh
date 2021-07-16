#!/bin/bash

set -Eeo pipefail

function main() {

    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY

    file_env POSTGRES_DB
    file_env POSTGRES_PASSWORD
    file_env POSTGRES_USER

    # Restore latest snapshot into /var/backups/restic-restore
    rm -rf /var/backups/restic-restore
    restore-directory '/var/backups/restic-restore'

    # Restore data dir backup
    rm -rf /var/backups/data/*
    cp -a /var/backups/restic-restore/data/* /var/backups/data

    # Restore db
    drop-create-db
    #restore-roles
    restore-db
}

source /usr/local/lib/functions.sh
source /usr/local/lib/file-functions.sh
source /usr/local/lib/pg-functions.sh
main

