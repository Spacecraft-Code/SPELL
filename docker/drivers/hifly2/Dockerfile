ARG BUILD_IMAGE=prengnexus1.idmz.eng.ses/spell/spell-cots-ubuntu-build:v1.0.0
ARG RUNTIME_IMAGE=prengnexus1.idmz.eng.ses/spell/spell-cots-ubuntu-build:v1.0.0

FROM ${BUILD_IMAGE} as builder
USER root
RUN apt-get update && apt-get install -y omniidl libomniorb4-dev && rm -rf /var/cache/apt/archives /var/lib/apt/lists/*

USER spell

RUN mkdir -p /home/spell/SPELL_BUILD
WORKDIR /home/spell/SPELL_BUILD

COPY --chown=spell . .
RUN mkdir -p /home/spell/SPELL
ENV SPELL_COTS=/home/spell/SPELL_COTS

#RUN ./build_spell.sh /home/spell/SPELL "none" "-DWITH_DEBUG -DWITH_KUBERNETES"

RUN cd drivers/hifly2 && ./generate_stubs.sh 6 && cd ../.. && ./build_driver.sh /home/spell/SPELL hifly "SPELL_SYN SPELL_UTIL SPELL_WRP" "-DWITH_DEBUG -DWITH_KUBERNETES" 

FROM ${RUNTIME_IMAGE}

USER spell
COPY --from=builder --chown=spell /home/spell/SPELL /home/spell/SPELL

ENV SPELL_HOME=/home/spell/SPELL
ENV SPELL_CONFIG=/home/spell/SPELL_CONFIG
ENV SPELL_DATA=/home/spell/procs/SPELL_PROCS
ENV SPELL_LOG=/home/spell/procs/SPELL_LOG
ENV PATH=$SPELL_HOME/bin:$SPELL_COTS/bin:$PATH
ENV SPELL_DRIVER=/home/spell/SPELL_DRIVER/SPELL
ENV LD_LIBRARY_PATH=$SPELL_HOME/lib:$SPELL_DRIVER/lib:$SPELL_COTS/lib

RUN chmod u+rwx -R /home/spell &&  chmod g+rx -R /home/spell

USER root
COPY drivers/scorpio/services /services
RUN cat /services >> /etc/services && rm /services
USER spell
