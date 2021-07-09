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

## License

Copyright © 2021 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)