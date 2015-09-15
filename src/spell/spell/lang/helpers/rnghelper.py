###################################################################################
## MODULE     : spell.lang.helpers.rnghelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for ranging management
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

from basehelper import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *
from spell.lib.exception import SyntaxException
from spell.lang.functions import *
from spell.lib.registry import *

################################################################################
class EnableRanging_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Enable ranging"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        pass
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip enabling ranging and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip enabling ranging and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to enable ranging again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "ENABLE")

        result = REGISTRY['RNG'].enableRanging( config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry enable ranging", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip enable ranging", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel enable ranging", {Severity:WARNING} )
        return [False, False]

################################################################################
class DisableRanging_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Disable ranging"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        pass
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip disabling ranging and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip disabling ranging and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to disable ranging again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "DISABLE")

        result = REGISTRY['RNG'].disableRanging( config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry disable ranging", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip disable ranging", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel disable ranging", {Severity:WARNING} )
        return [False, False]

################################################################################
class StartRanging_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """ 
    __bbe = []
    __antenna = []   
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Start ranging"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        
        if len(args)!=2:
            raise SyntaxException("Expected a baseband name and an antenna name")
        
        if type(args[0])!=str:
            raise SyntaxException("Expected a baseband name and an antenna name")
        if type(args[1])!=str:
            raise SyntaxException("Expected a baseband name and an antenna name")
        
        self.__bbe = args[0]
        self.__antenna = args[1]
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip start ranging and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip start ranging and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to start ranging again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "START")

        result = REGISTRY['RNG'].startRanging( self.__bbe, self.__antenna, config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry start ranging", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip start ranging", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel start  ranging", {Severity:WARNING} )
        return [False, False]

################################################################################
class StartRangingCalibration_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """ 
    __bbe = []
    __antenna = []   
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Start calibration"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        
        if len(args)==0:
            raise SyntaxException("Expected at least a baseband and an antenna name (no arguments given)")
        
        usingStrings = False
        usingLists = False
        if type(args[0])==str:
            usingStrings = True
            if len(args)!=2:
                raise SyntaxException("Expected a baseband and an antenna name (" + str(len(args)) + " arguments given)")
        elif type(args[0])==list:
            usingLists = True
        else:
            raise SyntaxException("Expected a baseband name or a list as first argument")
        
        count = 0
        for element in args:
            if usingLists and type(element)!=list:
                raise SyntaxException("Inconsistent argument type (" + str(count) + "), expected a list")
            elif usingLists and len(element)!=2:
                raise SyntaxException("Malformed argument (" + str(count) + "), expected a list of two elements")
            elif usingStrings and type(element)!=str:
                raise SyntaxException("Inconsistent argument type (" + str(count) + "), expected a string")
            count = count + 1

        if usingStrings:
            self.__bbe = args[0]
            self.__antenna = args[1]
        else:
            self.__bbe = []
            self.__antenna = []
            count = 0
            for element in args:
                if type(element[0])!=str:
                    raise SyntaxException("Expected a string as baseband name in argument list " + str(count))
                if type(element[1])!=str:
                    raise SyntaxException("Expected a string as antenna name in argument list " + str(count))
                self.__bbe.append(element[0])
                self.__antenna.append(element[1])
                count = count + 1
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip start calibration and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip start calibration and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to start calibration again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "CALIBRATION")

        result = REGISTRY['RNG'].startRangingCalibration( self.__bbe, self.__antenna, config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry start calibration", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip start calibration", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel start  calibration", {Severity:WARNING} )
        return [False, False]

################################################################################
class SetBasebandConfig_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """ 
    __bbe = None
    __param = None
    __value = None   
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Set BBE parameter"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        
        if len(args)==0:
            raise SyntaxException("Expected a baseband name, a parameter and a value")

        if type(args[0])!=str:
            raise SyntaxException("Expected a baseband name as first argument")
        
        usingList = False
        if len(args)==3:
            if type(args[1])!=str:
                raise SyntaxException("Expected a parameter name as second argument")
        elif len(args)==2:
            usingList = True
            if type(args[1])!=list:
                raise SyntaxException("Expected a list of parameter-value pairs as second argument")
        else:
            raise SyntaxException("Wrong number of arguments given")

        self.__bbe = args[0]
        
        if usingList:
            self.__param = []
            self.__value = []
            for element in args[1]:
                if type(element)!=list:
                    raise SyntaxException("Expected a list of lists as second argument")
                if len(element)!=2:
                    raise SyntaxException("Malformed parameter-value argument")
                self.__param.append(element[0])
                self.__value.append(element[1])
        else:
            self.__param = args[1]
            self.__value = args[2]
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip set baseband parameter and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip set baseband parameter and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to set baseband parameter again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "BBE_SET")

        result = REGISTRY['RNG'].setBasebandConfig( self.__bbe, self.__param, self.__value, config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry set baseband parameter", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip set baseband parameter", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel set baseband parameter", {Severity:WARNING} )
        return [False, False]

################################################################################
class GetBasebandConfig_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """ 
    __bbe = None
    __param = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Get BBE parameter"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        
        if len(args)!=2:
            raise SyntaxException("Expected a baseband name and a parameter name")

        if type(args[0])!=str:
            raise SyntaxException("Expected a baseband name as first argument")

        if type(args[1])!=str:
            raise SyntaxException("Expected a parameter name as second argument")
        
        self.__bbe = args[0]
        self.__param = args[1]
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip get baseband parameter and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip get baseband parameter and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to get baseband parameter again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "BBE_GET")

        result = REGISTRY['RNG'].getBasebandConfig( self.__bbe, self.__param, config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry get baseband parameter", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip get baseband parameter", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel get baseband parameter", {Severity:WARNING} )
        return [False, False]

################################################################################
class GetRangingStatus_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """ 
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Get status"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        
        if len(args)!=0:
            raise SyntaxException("Expected no arguments")

    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip get ranging status and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to get ranging status again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "STATUS_GET")

        result = REGISTRY['RNG'].getRangingStatus( config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry get ranging status", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip get ranging status", {Severity:WARNING} )
        return [False, None]

################################################################################
class AbortRanging_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Abort ranging"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=0:
            raise SyntaxException("Expected no arguments")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip abort ranging and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip abort ranging and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to abort ranging again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "ABORT")

        result = REGISTRY['RNG'].abortRanging( config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry abort ranging", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip abort ranging", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel abort ranging", {Severity:WARNING} )
        return [False, False]

################################################################################
class GetBasebandNames_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Get BBE names"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=0:
            raise SyntaxException("Expected no arguments")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip get baseband names and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to get baseband names again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "BBE_NAMES")

        result = REGISTRY['RNG'].getBasebandNames( config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry get baseband names", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip get baseband names", {Severity:WARNING} )
        return [False, None]

################################################################################
class GetAntennaNames_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Get antenna names"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=0:
            raise SyntaxException("Expected no arguments")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip get antenna names and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to get antenna names again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "ATN_NAMES")

        result = REGISTRY['RNG'].getAntennaNames( config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry get antenna names", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip get antenna names", {Severity:WARNING} )
        return [False, None]

################################################################################
class GetRangingPaths_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "RNG")
        self._opName = "Get ranging paths"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)!=0:
            raise SyntaxException("Expected no arguments")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip get ranging paths and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to get ranging paths again")

        # Store information for possible failures
        self.setFailureInfo("RNG", "RNG_PATHS")

        result = REGISTRY['RNG'].getRangingPaths( config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry get ranging paths", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip get ranging paths", {Severity:WARNING} )
        return [False, None]





