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

#==============================================================================
# HELPER FUNCTIONS
#==============================================================================

function check_env_variable() {
    DST=${!1}
    if [ -z $DST ]; then
        echo "$1 is not set, defaulting to $2"
        export $1=$2
    fi
    DST=${!1}
    if [ ! -d $DST ]; then
        echo "ERROR: cannot find $1 directory: $DST"
        return 1
    else
        return 0
    fi
}

#==============================================================================
# ENVIRONMENT VARIABLES FOR SPELL
#==============================================================================

echo ">>> SPELL environment setup begin"
! check_env_variable SPELL_LOG $SPELL_HOME/log && mkdir -p $SPELL_LOG

# check/set environment variables
! check_env_variable SPELL_HOME /home/spell/SPELL && return 1
! check_env_variable SPELL_DATA /home/spell/SPELL_PROCS && return 1
! check_env_variable SPELL_CONFIG /home/spell/SPELL_CONFIG && return 1
! check_env_variable SPELL_LOG $SPELL_HOME/log && return 1
! check_env_variable SPELL_COTS /home/spell/spell_cots && return 1

# parse release.nfo
RELEASENFO=$SPELL_HOME/release.nfo
[ ! -f $RELEASENFO ] && echo "ERROR: $RELEASENFO not found, check installation" && return 1
VERSION=`grep ^Version $RELEASENFO | cut -d: -f 2 | tr -d ' '`
[ -z $VERSION ] && echo "ERROR: $RELEASENFO does not contain SPELL version" && return 1

[ -z "$USER" ] && export USER=spell

echo "*** SPELL version $VERSION"
echo "*** SPELL_HOME=$SPELL_HOME"
echo "*** SPELL_DATA=$SPELL_DATA"
echo "*** SPELL_CONFIG=$SPELL_CONFIG"
echo "*** SPELL_LOG=$SPELL_LOG"
echo "*** SPELL_COTS=$SPELL_COTS"
echo "*** current User: $USER"

#==============================================================================
# SPELL ulimits
#==============================================================================

echo "*** Setting ulimits"
ulimit -c unlimited > /dev/null 2>&1
ulimit -s unlimited > /dev/null 2>&1
ulimit -u 30000     > /dev/null 2>&1
ulimit -a | grep "core file size"
ulimit -a | grep "stack size"
ulimit -a | grep "max user processes"

#==============================================================================
# SPELL COTS
#==============================================================================

export PATH=$SPELL_COTS/bin:$PATH
export LD_LIBRARY_PATH=$SPELL_COTS/lib:$SPELL_HOME/lib:$LD_LIBRARY_PATH

#==============================================================================
# PYTHON CHECKS
#==============================================================================

PYTHON=`which python`
[ -z $PYTHON ] && echo "ERROR: no python executable in PATH" && return 1

# is the python version supported?
PYVERSION=`$PYTHON -V 2>&1 | cut -d' ' -f 2`
PYREL=`echo $PYVERSION | cut -d'.' -f 1`
PYMAJOR=`echo $PYVERSION | cut -d'.' -f 2`
(( $PYREL != 2 )) && echo "ERROR: python > 2.X.Y required" && return 1
(( $PYMAJOR < 5 )) && echo "ERROR: python > 2.5.Y required" && return 1

echo "*** Using python version $PYVERSION in $PYTHON"

# is the python executable coming from COTS?
[[ "$(dirname $PYTHON)" != "$SPELL_COTS/bin" ]] \
    && echo "ERROR: python executable must be in COTS, check installation" && return 1

# is the python interpreter shared library matching?
EXC=$SPELL_HOME/bin/SPELL-Executor
[ ! -f $EXC ] && echo "ERROR: SPELL-Executor not found $SPELL_HOME/bin, check installation" && return 1
EXPECTED=`readelf -d $EXC | grep ".*NEEDED.*Shared.*libpython.*" | cut -d[ -f 2 | cut -d] -f1`
AVAILABLE=`readelf -d $PYTHON | grep ".*NEEDED.*Shared.*libpython.*" | cut -d[ -f 2 | cut -d] -f1`
[[ "$EXPECTED" != "$AVAILABLE" ]] \
    && echo "ERROR: shared library mismatch, SPELL was compiled with $EXPECTED, got $AVAILABLE" && return 1

# print python path for validation
$PYTHON -c "import sys; print sys.path"

echo ">>> SPELL environment setup and validation done"
echo
