AUTOMAKE_OPTIONS = foreign

if WITH_LIBS
SUBDIRS = SPELL_UTIL \
          SPELL_SYN  \
          SPELL_CFG  \
          SPELL_IPC  \
          SPELL_WRP  \
          SPELL_CIF  \
          SPELL_CIFS \
          SPELL_PRC  \
          SPELL_PRD  \
          SPELL_SDB  \
          SPELL_DTA  \
          SPELL_EXC  \
          SPELL_WS   

if WITH_EXECUTOR
SUBDIRS += SPELL_CIFS
endif

if WITH_EXECUTOR_CMD
SUBDIRS += SPELL_CIFC
endif

if WITH_CONTEXT
SUBDIRS += SPELL_CTX
endif

if WITH_LISTENER
SUBDIRS += SPELL_LST 
endif

endif
