# convention 4 kubernetes: c4k-jira
[![Clojars Project](https://img.shields.io/clojars/v/org.domaindrivenarchitecture/c4k-jira.svg)](https://clojars.org/org.domaindrivenarchitecture/c4k-jira) [![pipeline status](https://gitlab.com/domaindrivenarchitecture/c4k-jira/badges/master/pipeline.svg)](https://gitlab.com/domaindrivenarchitecture/c4k-jira/-/commits/master) 

[<img src="https://domaindrivenarchitecture.org/img/delta-chat.svg" width=20 alt="DeltaChat"> chat over e-mail](mailto:buero@meissa-gmbh.de?subject=community-chat) | [<img src="https://meissa-gmbh.de/img/community/Mastodon_Logotype.svg" width=20 alt="team@social.meissa-gmbh.de"> team@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@team) | [Website & Blog](https://domaindrivenarchitecture.org)

## Purpose

c4k-jira provides a k8s deployment for jira containing:
* adjusted jira docker image
* jira
* ingress having a letsencrypt managed certificate
* postgres database

The package aims to a low load sceanrio.

## Status

This is under development.

## Try out

Click on the image to try out live in your browser:

[![Try it out](doc/tryItOut.png "Try out yourself")](https://domaindrivenarchitecture.org/pages/dda-provision/c4k-jira/)

Your input will stay in your browser. No server interaction is required.

You will also be able to try out on cli:
```
target/graalvm/c4k-jira src/test/resources/valid-config.edn src/test/resources/valid-auth.edn | kubeval -
target/graalvm/c4k-jira src/test/resources/valid-config.edn src/test/resources/valid-auth.edn | kubectl apply -f -
```

## Manual restore

1) Scale Jira deployment down:
kubectl scale deployment jira --replicas=0

2) apply backup and restore pod:
kubectl apply -f src/main/resources/backup/backup-restore.yaml

3) exec into pod and execute restore pod
kubectl exec -it backup-restore -- /usr/local/bin/restore.sh

4) Scale Jira deployment up:
kubectl scale deployment jira --replicas=1

5) Update index of Jira:
Jira > Settings > System > Advanced > Indexing
## License

Copyright © 2021 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)