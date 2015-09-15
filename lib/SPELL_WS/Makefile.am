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

lib_LTLIBRARIES= libSPELL_WS.la

libSPELL_WS_la_CPPFLAGS = ${AM_CPPFLAGS} 
libSPELL_WS_la_LDFLAGS = -version-info 3:0:0

libSPELL_WS_la_SOURCES = \
                src/SPELLwsStorage.C \
                src/SPELLwsDataHandler.C \
                src/SPELLwsDataCodes.C \
                src/SPELLwsDataHandlerFactory.C \
                src/SPELLwsObjectDataHandler.C \
                src/SPELLwsInstanceDataHandler.C \
                src/SPELLwsCustomTypeDataHandler.C \
                src/SPELLwsTmItemDataHandler.C \
                src/SPELLwsClassDataHandler.C \
                src/SPELLwsDictDataHandler.C \
                src/SPELLwsListDataHandler.C \
                src/SPELLwsTupleDataHandler.C \
                src/SPELLwsDbDataHandler.C \
                src/SPELLwsTimeDataHandler.C \
                src/SPELLwsWorkingMode.C \
                src/SPELLwsFrame.C \
                src/SPELLwsDynamicData.C \
                src/SPELLwsStaticData.C \
                src/SPELLwsWarmStartImpl.C
 
 
