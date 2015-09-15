###################################################################################
## MODULE     : spell.lang.user
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Special functions
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

#===============================================================================
# SPELL imports
#===============================================================================
from spell.utils.log import *
from spell.lang.constants import *
from spell.lang.functions import *
from spell.lang.modifiers import *
from spell.lib.adapter.config import Configurable

#===============================================================================
# Local imports
#===============================================================================

#===============================================================================
# System imports
#===============================================================================

#===============================================================================
# Import definition
#===============================================================================
__all__ = [
           'SendAndVerifyAdjLim',
	   'BuildMemoryLoad'
          ]

################################################################################
class FunctionSequence(object):
    
    __functions = []
    tryAll = True
    
    #===========================================================================
    def __init__(self, *args):
        self.__functions = []
        self.tryAll = True
        for f in args:
            if callable(f):
                self.__functions.append(f)
                
    #===========================================================================
    def execute(self, *args, **kargs ):
        result = True
        choice = None
        for f in self.__functions:
            if not result and not self.tryAll: return False
            partialResult = f(*args,**kargs)
            if partialResult is not None:
                if type(partialResult)==list:
                    if type(partialResult[0])==bool:
                        result = result and partialResult[0]
                    else:
                        result = partialResult
                    choice = partialResult[1]
                else:
                    if type(partialResult)==bool:
                        result = result and partialResult
                    else:
                        result = partialResult
        if choice is not None:
            return [result,choice]
        else:
            return result

################################################################################
def SendAndVerifyAdjLim(*args, **kargs):
    """
    The only important statement is SendAndVerify
    If DisableAlarms fails, just display an error message
    If AdjustLimits fails, display an error message and try re-enable alarms; 
    if an error occurs while re-renabling alarms display an error message and propose to retry, 
    abort or continue
    Send operations errors are manage internally by SendAndVerify, as usual 
    (RESEND, RECHECK, ABORT, SKIP, CANCEL)
    if EnableAlarm fails display an error message and propose to retry, abort or continue
    
    """
    
    Display('Disabling alarms')
    if not DisableAlarm(*args, **kargs):
        Display('Could not disable alarms', ERROR)
        
    Display('Adjusting limits')
    
    if not AdjustLimits(*args, **kargs):
            Display('Could not adjust limits', ERROR)
            user_choice = 'RETRY'
            while user_choice <> 'CONTINUE':
                Display('Re-enabling alarms')
                if not EnableAlarm(*args, **kargs):
                    user_choice = Prompt('Could not re-enable alarms. RETRY, ABORT or CONTINUE', 
                                         [ 'RETRY', 'ABORT', 'CONTINUE' ], LIST|ALPHA)
                    if user_choice == 'ABORT':
                        Abort('Procedure aborted')
                else:
                    user_choice = 'CONTINUE'

    result = SendAndVerify(*args, **kargs)

    user_choice = 'RETRY'
    while user_choice <> 'CONTINUE':
        Display('Enabling alarms')
        if not EnableAlarm(*args, **kargs):
            user_choice = Prompt('Could not re-enable alarms. RETRY, ABORT or CONTINUE', 
                                 [ 'RETRY', 'ABORT', 'CONTINUE' ], LIST|ALPHA)
            if user_choice == 'ABORT':
                Abort('Procedure aborted')
        else:
            user_choice = 'CONTINUE'
               
    return result

    
################################################################################
def BuildMemoryLoad(*args, **kargs):
    """
    TODO
    
    Syntax 1:
    =========
                
    TODO
    
        BuildMemoryLoad( arguments )

    Modifiers:
    ==========
    
    TODO
         
        1. Modifier - Description
        
            - VALUE:           Description

           Default is Modifier:VALUE

    Result:
    =======
    
    TODO
    """
    from helpers.tchelper import BuildMemoryLoad_Helper
    helper = BuildMemoryLoad_Helper()
    helper.configure( *args, **kargs )
    return helper.execute( *args, **kargs )
    
