ARG UBUNTU_VERSION=22.04
ARG NEXUS_REPO=docker-weu.spo.internal.ses.com/

FROM ${NEXUS_REPO}ubuntu:${UBUNTU_VERSION} as base

# permissions
ARG CONTAINER_UID=1001
ARG CONTAINER_GID=1000

COPY ./go-cron.tar.gz /tmp/go-cron.tar.gz

# install dev tools
RUN export CONTAINER_USER=logrotate && \
    export CONTAINER_GROUP=logrotate && \
    addgroup --group --gid $CONTAINER_GID logrotate && \
    adduser --uid $CONTAINER_UID --ingroup logrotate --home /usr/bin/logrotate.d --shell /bin/bash --system logrotate && \
    apt-get update && apt-get install -y  \
      tini \
      tar \
      gzip \
      tzdata \
      logrotate && \
    mkdir -p /usr/bin/logrotate.d && \
    tar xvf /tmp/go-cron.tar.gz -C /usr/bin && \
    rm -rf /var/cache/apt/archives /var/lib/apt/lists/*

COPY docker-entrypoint.sh /usr/bin/logrotate.d/docker-entrypoint.sh
COPY logrotate.conf /usr/bin/logrotate.d/logrotate.conf

# environment variable for this container
ENV LOGROTATE_INTERVAL= \
    LOG_PATHS= 


ENTRYPOINT ["/bin/tini","--","/usr/bin/logrotate.d/docker-entrypoint.sh"]
VOLUME ["/logrotate-status"]
CMD ["cron"]