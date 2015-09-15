###################################################################################
## FILE       : Makefile.am
## DATE       : Mar 17, 2011
## PROJECT    : SPELL
## DESCRIPTION: Automake rules
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## SPELL is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## SPELL is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with SPELL. If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################
include ${top_srcdir}/src/common.mk

DEST_DIR=$(top_builddir)/src/spel-gui/
SRC_DIR=$(top_srcdir)/src/spel-gui

prepare:
	if [ ! -d ${WITH_ECLIPSE} ]; \
        then \
            echo "FATAL: cannot find eclipse installation at ${WITH_ECLIPSE}";\
            exit -1; \
        fi
	
types-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.types ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

core-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.core ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

language-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.language ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

procs-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.procs ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

print-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.print ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

gui-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

clock-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.clock ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

codeview-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.codeview ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

textview-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.textview ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

shellview-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.shellview ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

commskt-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.commsocket ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

shell-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.shell ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

preferences-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.preferences ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

preferences-gui-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.preferences.ui ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

wvariables-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.watchvariables ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

outline-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.outline ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

doc-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.documentation ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

htree-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.htree ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

replay-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.replay ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

shared-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.shared ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

sampleview-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.sampleview ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

all-local: prepare \
     types-plugin \
	   core-plugin \
	   language-plugin \
	   preferences-plugin \
	   procs-plugin \
	   preferences-gui-plugin \
	   gui-plugin \
	   print-plugin \
	   clock-plugin \
	   codeview-plugin \
	   textview-plugin \
     shellview-plugin \
     sampleview-plugin \
	   commskt-plugin \
	   shell-plugin \
     wvariables-plugin \
     outline-plugin \
     doc-plugin \
     replay-plugin \
     shared-plugin \
     htree-plugin 

clean-local:
	rm -rf ${DEST_DIR}/lib_plugins
	rm -rf ${DEST_DIR}/obj
	rm -f ${DEST_DIR}/*.log
	rm -f ${DEST_DIR}/*.jar

install-exec-hook:
	mkdir -p ${exec_prefix}/spel-gui
if HAVE_RSYNC
	rsync -av --exclude=.svn ${SRC_DIR}/bin/* ${exec_prefix}/spel-gui/
	rsync -av ${DEST_DIR}/*.jar ${exec_prefix}/spel-gui/plugins/
else
	cp -rvf ${SRC_DIR}/bin ${exec_prefix}/spel-gui
	find ${exec_prefix}/spel-gui -name ".svn" | xargs rm -rf
	cp -vf ${DEST_DIR}/*.jar ${exec_prefix}/spel-gui/plugins/.
endif

