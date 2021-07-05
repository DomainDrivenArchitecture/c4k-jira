#!/bin/bash

function main() {

    upgradeSystem
    apt-get -qqy install curl openjdk-11-jre-headless xmlstarlet > /dev/null
    
    adduser --system --disabled-login --home ${JIRA_HOME} --uid 901 --group jira 

    mkdir -p /app
    
    cd /tmp
    curl --location ${DOWNLOAD_URL}/atlassian-jira-software-${JIRA_RELEASE}.tar.gz \
        -o atlassian-jira-software-${JIRA_RELEASE}.tar.gz
    tar -xvf atlassian-jira-software-${JIRA_RELEASE}.tar.gz -C /tmp/
    mv /tmp/atlassian-jira-software-${JIRA_RELEASE}-standalone \
        /opt/atlassian-jira-software-standalone
    chown -R jira:jira /opt/atlassian-jira-software-standalone

    install -ojira -gjira -m744 /tmp/resources/entrypoint.sh /app/entrypoint.sh
    install -ojira -gjira -m744 /tmp/resources/setenv.sh /opt/atlassian-jira-software-standalone/bin/setenv.sh
    install -ojira -gjira -m660 /tmp/resources/dbconfig.xml /app/dbconfig.xml
    install -ojira -gjira -m660 /tmp/resources/server.xml /opt/atlassian-jira-software-standalone/conf/server.xml
    install -ojira -gjira -m660 /tmp/resources/logging.properties /opt/atlassian-jira-software-standalone/conf/logging.properties

    cleanupDocker
}

source /tmp/resources/install_functions.sh
main