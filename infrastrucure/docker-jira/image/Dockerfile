FROM ubuntu:20.04

ENV JIRA_HOME="/var/jira" \
    DOWNLOAD_URL="https://product-downloads.atlassian.com/software/jira/downloads" \
    JIRA_RELEASE="8.8.0"
ADD resources /tmp/resources
RUN /tmp/resources/install.sh

USER 901:901
