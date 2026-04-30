################################################################################
#
#  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
#
#  This file is part of SPELL.
#
#  SPELL is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  SPELL is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with SPELL. If not, see <http://www.gnu.org/licenses/>.
#
################################################################################

include ${top_srcdir}/common.mk

bin_PROGRAMS = SPELL-Context

SPELL_Context_SOURCES = src/SPELLcontextMain.C

SPELL_Context_CPPFLAGS = ${AM_CPPFLAGS} ${PYTHON2_CPPFLAGS}

SPELL_Context_LDFLAGS = ${AM_LDFLAGS} ${PYTHON2_LDFLAGS} ${PYTHON2_EXTRA_LIBS} ${PYTHON2_EXTRA_LDFLAGS} ${RELEASE_LDFLAG}

SPELL_Context_LDADD = ${top_builddir}/lib/SPELL_CTX/libSPELL_CTX_v2.la   \
                      ${top_builddir}/lib/SPELL_SDB/libSPELL_SDB_v2.la   \
                      ${top_builddir}/lib/SPELL_WRP/libSPELL_WRP_v2.la   \
                      ${top_builddir}/lib/SPELL_IPC/libSPELL_IPC_v2.la   \
                      ${top_builddir}/lib/SPELL_PRC/libSPELL_PRC_v2.la   \
                      ${top_builddir}/lib/SPELL_PRD/libSPELL_PRD_v2.la   \
                      ${top_builddir}/lib/SPELL_SYN/libSPELL_SYN_v2.la   \
                      ${top_builddir}/lib/SPELL_CFG/libSPELL_CFG_v2.la   \
                      ${top_builddir}/lib/SPELL_UTIL/libSPELL_UTIL_v2.la
