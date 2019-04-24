FROM centos:7

VOLUME /repo

COPY *.rpm /repo/
COPY tempneo4j.repo /tmp/
COPY entrypoint.sh /tmp/

RUN yum --assumeyes install ${TEST_JAVA} createrepo bc && \
    mv /tmp/tempneo4j.repo /etc/yum.repos.d/

ENTRYPOINT [ "/tmp/entrypoint.sh", "${TEST_JAVA}" ]
