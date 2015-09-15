###################################################################################
## MODULE     : spell.lib.exception
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Exceptions
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or
## modify it under the terms of the GNU Lesser General Public
## License as published by the Free Software Foundation, either
## version 3 of the License, or (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
## Lesser General Public License for more details.
##
## You should have received a copy of the GNU Lesser General Public
## License and GNU General Public License (to which the GNU Lesser
## General Public License refers) along with this library.
## If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################

#*******************************************************************************
# SPELL Imports
#*******************************************************************************

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************

class SpellException( BaseException ):
    message = None
    reason  = None
    def __init__(self, msg = None, reason = None ):
        BaseException.__init__(self,msg)
        if isinstance(msg,SpellException):
            self.message = msg.message
            self.reason  = msg.reason
        elif type(msg)==str:
            self.message = msg
            self.reason = reason
        if self.message is None:
            self.message = repr(self.__class__)
        if self.reason is None:
            self.reason = "unknown"
    
    def __str__(self):
        return self.message + " ( " + self.reason + " )"

    def __repr__(self):
        return self.message + " ( " + self.reason + " )"
        
class CoreException  ( SpellException ): pass 
class NotAvailable   ( SpellException ): pass
class DriverException( SpellException ): pass
class CancelException( SpellException ): pass
class VerifyException( SpellException ): pass
class SyntaxException( SpellException ): pass


#*******************************************************************************
# Customized Exceptions 
#*******************************************************************************
class Handle:
    
    code = 0
    type = None
    item = None
    
    def __init__(self, code = 0, type = None, item = None):
        self.code = code
        self.type = type
        self.item = item
 
#*******************************************************************************
# Module globals
#*******************************************************************************

