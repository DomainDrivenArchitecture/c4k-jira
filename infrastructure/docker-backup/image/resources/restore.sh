#!/bin/bash

set -Eeo pipefail

function main() {

    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY

    # Restore latest snapshot into /var/backups/restore
    rm -rf /var/backups/restore
    restore-directory '/var/backups/restore'

    cp /var/backups/restore/export/*.zip /var/backups/import/
    chown 901:901 /var/backups/import/*.zip
}

source /usr/local/lib/functions.sh
source /usr/local/lib/file-functions.sh
main

