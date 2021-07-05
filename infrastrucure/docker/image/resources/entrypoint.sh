#!/bin/bash

xmlstarlet ed -L -u "/Server/Service/Connector[@proxyName='{subdomain}.{domain}.com']/@proxyName" \
    -v "$1" /opt/atlassian-jira-software-standalone/conf/server.xml
xmlstarlet ed -L -u "/jira-database-config/jdbc-datasource/username" \
    -v "$2" /app/dbconfig.xml
xmlstarlet ed -L -u "/jira-database-config/jdbc-datasource/password" \
    -v "$3" /app/dbconfig.xml

install -ojira -gjira -m660 /app/dbconfig.xml /var/jira/dbconfig.xml
/opt/atlassian-jira-software-standalone/bin/setenv.sh run
/opt/atlassian-jira-software-standalone/bin/start-jira.sh run
