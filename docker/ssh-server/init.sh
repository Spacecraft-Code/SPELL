#!/bin/bash
#

#LDAP_PASSWORD=

[ -z "${LDAP_PASSWORD}" ] && LDAP_PASSWORD=$(cat /etc/ldap_password)
[ -z "${LDAP_URL}" ] && LDAP_URL=$(cat /etc/ldap_url)
[ -z "${LDAP_URL}" ] && LDAP_URL="ldaps://beacdc1.xeneng.ses"
[ -z "${SSH_PORT}" ] && SSH_PORT=8422
[ -z "${LDAP_BIND_DN}" ] && LDAP_BIND_DN="CN=ldap,OU=G_SYS,OU=ses-users,DC=xeneng,DC=ses"
[ -z "${LDAP_SEARCH_BASE}" ] && LDAP_SEARCH_BASE="OU=ses-users,DC=xeneng,DC=ses"

[ -f /etc/ssh/ssh_host_rsa_key ] || ssh-keygen -f /etc/ssh/ssh_host_rsa_key -q -N ''

mkdir -p /var/lib/sss/db/
cat > /etc/sssd/sssd.conf <<EOF
[sssd]
config_file_version = 2
services = nss, pam, ssh
domains = xeneng.ses

[domain/xeneng.ses]
id_provider = ldap
auth_provider = ldap


override_gid = 1000

ldap_group_nesting_level = 1
ldap_deref_threshold = 2
ldap_referrals = False
ldap_uri = ${LDAP_URL}
cache_credentials = True
ldap_default_bind_dn = ${LDAP_BIND_DN}
ldap_default_authtok = ${LDAP_PASSWORD}
ldap_search_base = ${LDAP_SEARCH_BASE}
ldap_schema = ad
ldap_tls_reqcert = never
ldap_id_mapping = True
enumerate = False

[ssh]

[pam]
offline_credentials_expiration = 3

[nss]
filter_users = root,spell
filter_groups = root,spell
homedir_substring = /home
override_homedir = /home/%u

EOF
chmod 0600 /etc/sssd/sssd.conf
pkill -9 sssd
sssd -D --logger=files

cat > /sshd_config <<EOF
#	$OpenBSD: sshd_config,v 1.93 2014/01/10 05:59:19 djm Exp $

# This is the sshd server system-wide configuration file.  See
# sshd_config(5) for more information.

# This sshd was compiled with PATH=/usr/local/bin:/usr/bin

# The strategy used for options in the default sshd_config shipped with
# OpenSSH is to specify options with their default value where
# possible, but leave them commented.  Uncommented options override the
# default value.

# If you want to change the port on a SELinux system, you have to tell
# SELinux about this change.
# semanage port -a -t ssh_port_t -p tcp #PORTNUMBER
#
Port ${SSH_PORT}
#AddressFamily any
#ListenAddress 
#ListenAddress ::

# The default requires explicit activation of protocol 1
#Protocol 2

# HostKey for protocol version 1
#HostKey /etc/ssh/ssh_host_key
# HostKeys for protocol version 2
HostKey /etc/ssh/ssh_host_rsa_key
#HostKey /etc/ssh/ssh_host_dsa_key
#HostKey /etc/ssh/ssh_host_ecdsa_key
#HostKey /etc/ssh/ssh_host_ed25519_key

# Lifetime and size of ephemeral version 1 server key
#KeyRegenerationInterval 1h
#ServerKeyBits 1024

# Ciphers and keying
#RekeyLimit default none

# Logging
# obsoletes QuietMode and FascistLogging
#SyslogFacility AUTH
#SyslogFacility AUTHPRIV
LogLevel DEBUG3

# Authentication:

#LoginGraceTime 2m
PermitRootLogin no
#StrictModes yes
#MaxAuthTries 6
#MaxSessions 10

#RSAAuthentication yes
#PubkeyAuthentication yes

# The default is to check both .ssh/authorized_keys and .ssh/authorized_keys2
# but this is overridden so installations will only check .ssh/authorized_keys
AuthorizedKeysFile	.ssh/authorized_keys

#AuthorizedPrincipalsFile none

#AuthorizedKeysCommand none
#AuthorizedKeysCommandUser nobody

# For this to work you will also need host keys in /etc/ssh/ssh_known_hosts
#RhostsRSAAuthentication no
# similar for protocol version 2
#HostbasedAuthentication no
# Change to yes if you don't trust ~/.ssh/known_hosts for
# RhostsRSAAuthentication and HostbasedAuthentication
#IgnoreUserKnownHosts no
# Don't read the user's ~/.rhosts and ~/.shosts files
#IgnoreRhosts yes

# To disable tunneled clear text passwords, change to no here!
#PasswordAuthentication yes
#PermitEmptyPasswords no
PasswordAuthentication yes

# Change to no to disable s/key passwords
#ChallengeResponseAuthentication yes
ChallengeResponseAuthentication no

# Kerberos options
#KerberosAuthentication no
#KerberosOrLocalPasswd yes
#KerberosTicketCleanup yes
#KerberosGetAFSToken no
#KerberosUseKuserok yes

# GSSAPI options
GSSAPIAuthentication yes
GSSAPICleanupCredentials no
#GSSAPIStrictAcceptorCheck yes
#GSSAPIKeyExchange no
#GSSAPIEnablek5users no

# Set this to 'yes' to enable PAM authentication, account processing,
# and session processing. If this is enabled, PAM authentication will
# be allowed through the ChallengeResponseAuthentication and
# PasswordAuthentication.  Depending on your PAM configuration,
# PAM authentication via ChallengeResponseAuthentication may bypass
# the setting of "PermitRootLogin without-password".
# If you just want the PAM account and session checks to run without
# PAM authentication, then enable this but set PasswordAuthentication
# and ChallengeResponseAuthentication to 'no'.
# WARNING: 'UsePAM no' is not supported in Red Hat Enterprise Linux and may cause several
# problems.
UsePAM yes

#AllowAgentForwarding yes
#AllowTcpForwarding yes
#GatewayPorts no
X11Forwarding yes
#X11DisplayOffset 10
#X11UseLocalhost yes
#PermitTTY yes
#PrintMotd yes
#PrintLastLog yes
#TCPKeepAlive yes
#UseLogin no
UsePrivilegeSeparation sandbox		# Default for new installations.
#PermitUserEnvironment no
#Compression delayed
#ClientAliveInterval 0
#ClientAliveCountMax 3
#ShowPatchLevel no
#UseDNS yes
#PidFile /var/run/sshd.pid
MaxStartups 100
#PermitTunnel no
#ChrootDirectory none
#VersionAddendum none

# no default banner path
#Banner none

# Accept locale-related environment variables
AcceptEnv LANG LC_CTYPE LC_NUMERIC LC_TIME LC_COLLATE LC_MONETARY LC_MESSAGES
AcceptEnv LC_PAPER LC_NAME LC_ADDRESS LC_TELEPHONE LC_MEASUREMENT
AcceptEnv LC_IDENTIFICATION LC_ALL LANGUAGE
AcceptEnv XMODIFIERS

# override default of no subsystems
Subsystem	sftp	/usr/libexec/openssh/sftp-server

# Example of overriding settings on a per-user basis
#Match User anoncvs
#	X11Forwarding no
#	AllowTcpForwarding no
#	PermitTTY no
#	ForceCommand cvs server

#Legacy changes
KexAlgorithms +diffie-hellman-group1-sha1
Ciphers +aes128-cbc
HostKeyAlgorithms +ssh-rsa,ssh-dss
EOF



pkill -9 sshd
/usr/sbin/sshd -e -E /tmp/ssh.log -f /sshd_config 

ln -sf /home/spell /home/spell/procs

touch /home/spell/users
chmod 644 /home/spell/users
mkdir /home/spell/groups
chmod 755 /home/spell/groups

watch -n0.1 "netstat -e | grep localhost | awk '{
      p=index(\$4,\":\")
      cmd=\"id -G -n \" \$7 \" > /home/spell/groups/\" \$7 \" &\"
      system(cmd)
      print substr(\$4,p+1) \"=\" \$7
    }' > /home/spell/users"
# id -G -n \$7