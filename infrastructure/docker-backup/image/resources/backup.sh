#!/bin/bash

set -o pipefail

function main() {
    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY
    file_env RESTIC_DAYS_TO_KEEP 14

    backup-fs-from-directory '/var/backups/' 'export/'
}

source /usr/local/lib/functions.sh
source /usr/local/lib/file-functions.sh

main
