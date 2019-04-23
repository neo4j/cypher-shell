FROM centos:7

VOLUME /repo

#COPY ${CYPHER_SHELL_FILE} /repo/
#COPY ${JAVA_ADAPTER_FILE} /repo/
COPY *.rpm /repo/
COPY tempneo4j.repo /tmp/
COPY entrypoint.sh /tmp/

RUN yum --assumeyes install ${TEST_JAVA} createrepo bc && \
    mv /tmp/tempneo4j.repo /etc/yum.repos.d/

#ENTRYPOINT [ "ls", "-l", "/repo"]
ENTRYPOINT [ "/tmp/entrypoint.sh", "${TEST_JAVA}" ]
#ENTRYPOINT [ "test $(yum --assumeno install cypher-shell | grep -c ${TEST_JAVA} | bc) == '0'" ]
