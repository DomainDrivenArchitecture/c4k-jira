# Backup Architecture details

![](backup.svg)

* we use restic to produce small & encrypted backups
* backup is scheduled at `schedule: "10 23 * * *"`
* Jira stores files on `/var/jira/export`, these files are backuped. By default jira produces two exports per day.
* As jira provides a full xml export postgres is not needed.

## Manual init the restic repository for the first time

1. apply backup-and-restore pod:   
   `kubectl scale deployment backup-restore --replicas=1`
1. exec into pod and execute restore pod (press tab to get your exact pod name)   
   `kubectl exec -it backup-restore-... -- /usr/local/bin/init.sh`
1. remove backup-and-restore pod:   
   `kubectl scale deployment backup-restore --replicas=0`


## Manual backup the restic repository for the first time

1.Create a jira export:    
  Jira > Settings > System -> Backup system
1. Choose a filename `backup-filename.zip`. Your file will be stored to `/var/backup/export`.
1. apply backup-and-restore pod:   
  `kubectl scale deployment backup-restore --replicas=1`
1. exec into pod and execute restore pod (press tab to get your exact pod name)   
   `kubectl exec -it backup-restore-... -- /usr/local/bin/backup.sh`
1. remove backup-and-restore pod:   
   `kubectl scale deployment backup-restore --replicas=0`


## Manual restore

1. apply backup-and-restore pod:   
  `kubectl scale deployment backup-restore --replicas=1`
1. exec into pod and execute restore pod (press tab to get your exact pod name)   
   `kubectl exec -it backup-restore-... -- /usr/local/bin/restore.sh`
1. In case of already set up server:
   1. Import one of Jira exportet backups:   
      Jira > Settings > System > Restore System
   1. Choose one of your backuped files located at `/var/jira/import/`.   
      E.g. `backup-filename.zip`.
1. In case of installation wizzard:
   1. Choose restore from backup
   1. Choose one of your backuped files located at `/var/jira/import/`.   
      E.g. `backup-filename.zip`
1. remove backup-and-restore pod:   
   `kubectl scale deployment backup-restore --replicas=0`
