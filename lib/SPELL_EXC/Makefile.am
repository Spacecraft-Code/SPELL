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

include ${top_srcdir}/common.mk

#libSPELL_EXC_la_CPPFLAGS = ${AM_CPPFLAGS} -DWITH_DEBUG

lib_LTLIBRARIES= libSPELL_EXC.la
libSPELL_EXC_la_LDFLAGS = -version-info 3:0:0
libSPELL_EXC_la_SOURCES = \
                src/SPELLgoto.C \
                src/SPELLbreakpoint.C \
                src/SPELLbreakpointType.C \
                src/SPELLbytecode.C \
                src/SPELLastAnalyzer.C \
                src/SPELLexecutor.C \
                src/SPELLexecutorImpl.C \
                src/SPELLimportChecker.C \
                src/SPELLexecutorPython.C \
                src/SPELLcallstack.C \
                src/SPELLexecutorUtils.C \
                src/SPELLinterpreter.C \
                src/SPELLgotoPython.C \
                src/SPELLstepPython.C \
                src/SPELLscheduler.C \
                src/SPELLchildManager.C \
                src/SPELLframeManager.C \
                src/SPELLlnotab.C \
                src/SPELLcontroller.C \
                src/SPELLcodeTreeNode.C \
                src/SPELLcodeTreeLine.C \
                src/SPELLexecutorConfiguration.C \
                src/SPELLexecutionModel.C \
                src/SPELLexecutionTrace.C \
                src/SPELLvariableMonitor.C \
                src/SPELLvariableManager.C \
                src/SPELLcommandMailbox.C
                
 
