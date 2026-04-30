ARG UBUNTU_VERSION=22.04

FROM docker-weu.spo.internal.ses.com/ubuntu:${UBUNTU_VERSION} as base

RUN apt-get update && apt-get install -y openssh-server net-tools sssd nscd libnss-ldap libpam-ldap ldap-auth-config libpam-sss libnss-sss sssd-tools \
  && rm -rf /var/cache/apt/archives /var/lib/apt/lists/*
RUN apt-get update && apt-get install -y rxvt-unicode \
  && rm -rf /var/cache/apt/archives /var/lib/apt/lists/*

RUN  groupadd spell && useradd -rm -d /home/spell -s /sbin/nologin -g spell -G spell -u 1001 spell

RUN mkdir -p /var/run/sshd
ADD init.sh /init.sh
RUN chmod u+x /init.sh


EXPOSE 8422
RUN chmod u+rwx -R /home/ && mkdir -p /usr/libexec/openssh/ && ln -s /usr/lib/sftp-server /usr/libexec/openssh/sftp-server && ln -s /usr/lib/sftp-server /bin/sftp-server && chmod g+rx -R /home/ && touch /etc/services && pam-auth-update --enable mkhomedir

ENV TERM=xterm

CMD /init.sh