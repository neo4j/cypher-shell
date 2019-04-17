FROM ubuntu:bionic

# VOLUME /repo

COPY ${CYPHER_SHELL_FILE} /repo/
COPY ${JAVA_ADAPTER_FILE} /repo/

RUN apt-get update -qq && \
    apt-get -y -qq install createrepo && \
    createrepo /repo