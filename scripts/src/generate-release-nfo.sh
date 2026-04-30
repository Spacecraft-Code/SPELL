#!/bin/bash
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

if [ -f "/etc/centos-release" ]; then
    PLATFORM=$(cat /etc/centos-release)
elif [ `which lsb_release` ]; then
    PLATFORM=$(lsb_release -a | grep Description | cut -d: -f2 | tr -d '\t')
elif [ -f  "/etc/lsb_release" ]; then
    PLATFORM=$(cat /etc/lsb-release | grep -Po '.*DISTRIB_DESCRIPTION="\K([^"]*)')
else
    PLATFORM="Unknown"
fi

if [ -z "${VERSION_TAG}" ]; then
    REVISION=$(git describe --tags --always --long 2> /dev/null)
else
    REVISION="${VERSION_TAG}"
fi

echo "\
---------
CHANGELOG
---------

$(cat $1 2> /dev/null)

================================================================================
Generation date   : $(date +%Y-%m-%d)
Revision          : $REVISION
Version           : $2
Build host        : $(hostname)
Build user        : $(whoami)
Platform          : $PLATFORM
Installation path : $3
GCC               : $(gcc --version | head -1)
Configure log     :

$(head -8 config.log)
================================================================================\
"
