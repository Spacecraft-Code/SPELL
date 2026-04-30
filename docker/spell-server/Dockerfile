ARG BUILD_IMAGE=prengnexus1.idmz.eng.ses/spell/spell-cots-ubuntu-build:v1.0.0
ARG RUNTIME_IMAGE=prengnexus1.idmz.eng.ses/spell/spell-cots-ubuntu-build:v1.0.0
ARG VERSION_TAG=0.0.1

FROM ${BUILD_IMAGE} as builder
ARG VERSION_TAG

USER root
RUN apt-get update && apt-get install -y libbackward-cpp-dev libdw-dev \
  && rm -rf /var/cache/apt/archives /var/lib/apt/lists/*

USER spell
RUN mkdir -p /home/spell/SPELL_BUILD
WORKDIR /home/spell/SPELL_BUILD

COPY --chown=spell . /home/spell/SPELL_BUILD
RUN mkdir -p /home/spell/SPELL && chown spell /home/spell/SPELL
ENV SPELL_COTS=/home/spell/SPELL_COTS
ENV LD_LIBRARY_PATH=$SPELL_HOME/lib:$SPELL_DRIVER/lib:$SPELL_COTS/lib

RUN cd /home/spell/SPELL_BUILD && ./build_spell_ubuntu.sh /home/spell/SPELL "none" "-DBACKWARD_HAS_DW=1 -DWITH_BACKTRACE -DWITH_DEBUG -DWITH_KUBERNETES"

FROM ${RUNTIME_IMAGE}
USER spell
COPY --chown=spell --from=builder /home/spell/SPELL /home/spell/SPELL

ENV SPELL_HOME=/home/spell/SPELL
ENV SPELL_CONFIG=/home/spell/SPELL_CONFIG
ENV SPELL_DATA=/home/spell/procs/SPELL_PROCS
ENV SPELL_LOG=/home/spell/procs/SPELL_LOG
ENV PATH=$SPELL_HOME/bin:$SPELL_COTS/bin:$PATH
ENV SPELL_DRIVER=/home/spell/SPELL_DRIVER/SPELL
ENV LD_LIBRARY_PATH=$SPELL_HOME/lib:$SPELL_DRIVER/lib:$SPELL_COTS/lib

WORKDIR /home/spell
RUN ln -sf /home/spell/procs/SPACECRAFT SPACECRAFT && ln -sf /home/spell/procs/SPELL_PROCS SPELL_PROCS

USER root
COPY --from=builder  /home/spell/SPELL_BUILD/drivers/scorpio/services /services
RUN cat /services >> /etc/services && rm /services
USER spell

ENTRYPOINT ["SPELL-Server-Docker"]
