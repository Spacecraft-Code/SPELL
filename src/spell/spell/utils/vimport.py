###################################################################################
## MODULE     : spell.utils.vimport
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Value importer
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with SPELL. If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################
from spell.lib.exception import DriverException
from spell.utils.log import *
from spell.lib.adapter.utctime import TIME
from spell.config.reader import *
from spell.lang.functions import bin
from spell.lang.constants import *
from spell.lang.modifiers import *

################################################################################
def SpecialType( value, orig_value ):
    vtype = None
    if type(value) == int:
        if orig_value.startswith("0x"):
            vtype = HEX
        elif orig_value.startswith("0") and not "." in orig_value:
            vtype = OCT
        elif orig_value.startswith("0b"):
            len = len(orig_value)-2
            vtype = BIN + str(len)
    elif isinstance(value,TIME):
        if value.isRel():
            vtype = RELTIME
        else:
            vtype = DATETIME
    return vtype        

################################################################################
def ImportValue( orig_value ):
    value = orig_value
    vtype = None
    try:
        value = TIME(orig_value)
        if (value.isRel()):
            vtype = RELTIME
        else:
            vtype = DATETIME
        LOG("WARNING: converting " + orig_value + " to date: " + str(value))
    except:
        try:
            value = eval(orig_value,{},{})
            vtype = SpecialType(value, orig_value)
        except:
            # Check for binary strings
            if orig_value.startswith("0b"):
                value = int(orig_value[2:],2)
                length = len(orig_value)-2
                vtype = BIN + str(length)
                LOG("Converting to binary: " + repr(orig_value))
            else:
                value = orig_value
                vtype = None
                LOG("WARNING: converting to string: " + repr(value))
    return [value,vtype]

################################################################################
def ExportValue( value, vtype ):
    if vtype == HEX:
        value = hex(value).upper()
        value = '0x' + value[2:]
    elif vtype == OCT:
        value = oct(value)
    elif vtype.startswith(BIN):
        len = int(vtype[3:])
        value = '0b' + bin(value, count = len)
    elif vtype == TIME:
        value = "T(" + str(value) + ")"
    return value
