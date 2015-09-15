#!/usr/bin/env bash
###############################################################################
#  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
#
# This file is part of SPELL.
#
# SPELL is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# SPELL is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with SPELL. If not, see <http://www.gnu.org/licenses/>.
#
# FILE: environment setup script
#
# DATE: 24/11/2008
#
###############################################################################

#==============================================================================
# MAIN ENVIRONMENT VARIABLES FOR SPELL
#==============================================================================

export LD_LIBRARY_PATH=$SPELL_HOME/lib:$LD_LIBRARY_PATH

# SPELL user data
if [[ -z "$SPELL_DATA" ]]
then
    echo "[!] WARNING: SPELL_DATA variable not defined, using default"
    export SPELL_DATA=$SPELL_HOME/data
fi
[[ ! -d $SPELL_DATA ]] && echo "ERROR: cannot find SPELL data directory: $SPELL_DATA" && exit 1
echo "SPELL data: $SPELL_DATA"

# SPELL config
if [[ -z "$SPELL_CONFIG" ]]
then
    echo "[!] WARNING: SPELL_CONFIG variable not defined, using default"
    export SPELL_CONFIG=$SPELL_HOME/config
fi
[[ ! -d $SPELL_CONFIG ]] && echo "ERROR: cannot find SPELL config directory: $SPELL_CONFIG" && exit 1
echo "SPELL config: $SPELL_CONFIG"

# SPELL runtime data
if [[ -z "$SPELL_SYS_DATA" ]]
then
    echo "[!] WARNING: SPELL_SYS_DATA variable not defined, using default"
    export SPELL_SYS_DATA=$SPELL_HOME/data
fi
[[ ! -d $SPELL_SYS_DATA ]] && echo "ERROR: cannot find SPELL runtime data directory: $SPELL_SYS_DATA" && exit 1
echo "SPELL runtime: $SPELL_SYS_DATA"

# SPELL logs
if [[ -z "$SPELL_LOG" ]]
then
    echo "[!] WARNING: SPELL_LOG variable not defined, using default"
    export SPELL_LOG=$SPELL_HOME/log
fi
[[ ! -d $SPELL_LOG ]] && mkdir $SPELL_LOG
echo "SPELL log: $SPELL_LOG"

#==============================================================================
# PYTHON SETUP
#==============================================================================

# Check python availability
PYTHON=`which python`
(( $? == 1 )) && echo "ERROR: no python available" && exit 1

# Check python version 
PYVERSION=`$PYTHON -V 2>&1 | cut -d" " -f 2`
PYREL=`echo $PYVERSION | cut -d"." -f 1`
PYMAJOR=`echo $PYVERSION | cut -d"." -f 2`

[[ "$PYREL" != "2" ]] && echo "ERROR: python > 2.Y.X required for SPELL" && exit 1
[[ "$PYMAJOR" < "5" ]] && echo "ERROR: python > 2.5.X required for SPELL" && exit 1

# Update python path
export PYTHONPATH=${PYTHONPATH}:${SPELL_HOME}/spell:${SPELL_HOME}/server:${SPELL_HOME}
export TK_LIBRARY=""
export TCL_LIBRARY=""
export PYTHONCASEOK=""

# Library path
export LD_LIBRARY_PATH=$SPELL_HOME/lib:$LD_LIBRARY_PATH

#==============================================================================
# JAVA SETUP
#==============================================================================

# Check java availability
JAVA=`which java`
(( $? == 1 )) && echo "ERROR: no java available" && exit 1

# Check java version
JVERSION=`java -version 2>&1 | grep version | awk '{print $NF}' | tr -d "\""`
JREL=`echo $JVERSION | cut -d"." -f 1`
JMAJOR=`echo $JVERSION | cut -d"." -f 2`

[[ "$JREL" != "1" ]] && echo "ERROR: java 1.6 required for SPELL" && exit 1
[[ "$JMAJOR" != "6" ]] && echo "ERROR: java 1.6 required for SPELL" && exit 1


