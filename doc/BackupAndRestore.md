# Backup Architecture details

![](backup.svg)

* we use restic to produce small & encrypted backups
* backup is scheduled at `schedule: "10 23 * * *"`
* Jira stores files on `/var/jira`, these files are backuped. If you create a jira xml backup located in /var/jira this file will also be backed up.
* postgres db is backed up as pgdump

## Manual restore

1) Scale Jira deployment down:
`kubectl scale deployment jira --replicas=0`

2) apply backup and restore pod:
`kubectl apply -f src/main/resources/backup/backup-restore.yaml`

3) exec into pod and execute restore pod
`kubectl exec -it backup-restore -- /usr/local/bin/restore.sh`

4) Scale Jira deployment up:
`kubectl scale deployment jira --replicas=1`

5) Update index of Jira:
Jira > Settings > System > Advanced > Indexing
