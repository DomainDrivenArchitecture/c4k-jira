#!/bin/bash

set -Eeo pipefail

function main() {

    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY

    # Restore latest snapshot into /var/backups/restic-restore
    rm -rf /var/backups/restic-restore
    restore-directory '/var/backups/restic-restore'

    # Restore data dir backup
    rm -rf /var/backups/data/*
    cp -a /var/backups/restic-restore/data/* /var/backups/data

    # /opt/atlassian-jira-software-standalone/bin/start-jira.sh
}

source /usr/local/lib/functions.sh
source /usr/local/lib/file-functions.sh
main

