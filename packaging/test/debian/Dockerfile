FROM debian:stretch
ENV DEBIAN_FRONTEND noninteractive

COPY ${DEBFILE} /tmp/

RUN apt-get update -qq && \
    apt-get install -y --no-install-recommends /tmp/${DEBFILE}

ENTRYPOINT ["/usr/bin/cypher-shell"]
