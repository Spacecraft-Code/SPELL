ARG BUILD_IMAGE=prengnexus1.idmz.eng.ses/spell/spell-cots-ubuntu-build:v1.0.0
ARG RUNTIME_IMAGE=prengnexus1.idmz.eng.ses/spell/spell-cots-ubuntu-build:v1.0.0

FROM ${BUILD_IMAGE} as builder
USER spell
WORKDIR /home/spell/SPELL_BUILD

COPY --chown=spell . .
RUN mkdir -p /home/spell/SPELL && chown spell:spell /home/spell/SPELL
ENV SPELL_COTS=/home/spell/SPELL_COTS

RUN ./build_driver.sh /home/spell/SPELL example "" "-DWITH_DEBUG -DWITH_KUBERNETES"

FROM ${RUNTIME_IMAGE}

COPY --chown=spell --from=builder /home/spell/SPELL /home/spell/SPELL

ENV SPELL_HOME=/home/spell/SPELL
ENV SPELL_CONFIG=/home/spell/SPELL_CONFIG
ENV SPELL_DATA=/home/spell/procs/SPELL_PROCS
ENV SPELL_LOG=/home/spell/procs/SPELL_LOG
ENV PATH=$SPELL_HOME/bin:$SPELL_COTS/bin:$PATH
ENV SPELL_DRIVER=/home/spell/SPELL_DRIVER/SPELL
ENV LD_LIBRARY_PATH=$SPELL_HOME/lib:$SPELL_DRIVER/lib:$SPELL_COTS/lib

USER spell
