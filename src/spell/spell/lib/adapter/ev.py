###################################################################################
## MODULE     : spell.lib.adapter.ev
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Event interface for drivers
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
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lib.registry import REGISTRY
from spell.lang.constants import *
from spell.lang.modifiers import *
from interface import Interface

#*******************************************************************************
# Local imports
#*******************************************************************************
from config import Configurable

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['EvInterface,EvView']

INTERFACE_DEFAULTS = { OnFailure:ABORT | SKIP | REPEAT,
                       Severity:INFORMATION,
                       Scope:SCOPE_PROC,
                       Mode: TIME_MODE_LIVE,
                       PromptUser:True,
                       Time: ""}

###############################################################################
class EvView(object):

    def notifyEvent(self, *args):
        raise NotImplemented()

###############################################################################
class EvInterface(Configurable, Interface):
    
    """
    DESCRIPTION:
        Event management library interface
    """
    __ctxName = None
    
    #==========================================================================
    def __init__(self):
        Interface.__init__(self, "EV")
        Configurable.__init__(self)
        self.__ctxName = None
        LOG("Created")
    
    #===========================================================================
    def refreshConfig(self):
        ctxConfig = self.getContextConfig()
        languageDefaults = ctxConfig.getInterfaceConfig(self.getInterfaceName())
        if languageDefaults:
            INTERFACE_DEFAULTS.update(languageDefaults)
        self.setConfig( INTERFACE_DEFAULTS )
        LOG("Configuration loaded", level = LOG_CNFG )

    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        LOG("Setup EV adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()

    #==========================================================================
    def cleanup(self):
        LOG("Cleanup EV adapter interface")
        
    #==========================================================================
    def raiseEvent(self, *args, **kargs ):
        """
        ------------------------------------------------------------------------
        Description
        
        Raise an event on the GCS.

        ------------------------------------------------------------------------
        Syntax #1:
            EV.raiseEvent( "message" )
            
            Raises a message on the GCS with severity INFORMATION and scope
            SCOPE_PROC.

        ------------------------------------------------------------------------
        Syntax #2:
            EV.raiseEvent( "message", {config} )
            
            Raises a message on the GCS with specific configuration. The 
            configuration determines the type of event (see below). 
            
        ------------------------------------------------------------------------
        Configuration
            
            Possible configuration modifiers are
             
                Severity: INFORMATION, WARNING, ERROR, FATAL
                Scope   : SCOPE_PROC - Procedure scope
                          SCOPE_SYS  - System scope
                          SCOPE_CFG  - Configuration scope
                          
            Default value for severity is INFORMATION. Default value for scope
            is SCOPE_PROC.
            
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. Severity) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            raiseEvent( param, { Severity:INFORMATION } )    is the same as
            raiseEvent( param, severity = INFORMATION ) 
        ------------------------------------------------------------------------
        """
        if (len(args)==0 and len(kargs)==0) or\
           (len(args)==0 and not kargs.has_key('message')) or \
           (len(args)>0 and type(args[0])!=str):
            raise SyntaxException("Expected an event message")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        if len(args)==0:
            message = kargs.get('message')
        else:
            message = args[0] 
        self._raiseEvent( message, useConfig )
     
    #==========================================================================
    def _raiseEvent(self, message, config = {} ):
        raise DriverException("Service not available on this driver")
     
    #==========================================================================
    def registerForEvents(self, *args, **kargs ):
        """
        Syntax #1:
            EV.registerForEvents( <EvView> ) 
        
            Register the given event view for live events coming from GCS
        
        Syntax #2:
            EV.registerForEvents( <EvView>, {Mode:<time mode>, 
                                             Time:<datetime>} )

            EV.registerForEvents( <EvView>, mode = <time mode>, 
                                            time = <datetime> )
            
            Register the given event view for retrieving live or historical events
            from the GCS. 

        ------------------------------------------------------------------------
        Configuration
            
            Possible configuration modifiers are
            
                Mode: TIME_MODE_LIVE, TIME_MODE_FWD or TIME_MODE_BWD
                Time: Date-time string like "20/01/2008 10:34:00"
                      If the date-time information is not complete, it is 
                      assumed that the time is relative, not absolute.
                      
        ------------------------------------------------------------------------
        NOTES
        
        Notice that all configuration parameters (modifiers) can be passed in
        two ways:
        
            a) { ModifierName:ModifierValue, ModifierName:ModifierValue }
            
            b) modifiername = ModifierValue, modifiername = ModifierValue
        
        In the first case modifier names are written with leading capital letters
        (e.g. Severity) and they must be passed within a dictionary {}.
        
        In the second case, modifier names are written in lowercase, separated
        by commas and the value is assigned with '='.
        
        Examples:
        
            registerForEvents( param, { Mode:TIME_MODE_LIVE } )    is the same as
            registerForEvents( param, mode = TIME_MODE_LIVE ) 
        ------------------------------------------------------------------------
        """
        if (len(args)==0 and len(kargs)==0) or\
           (len(args)==0 and not kargs.has_key('view')) or \
           (len(args)>0 and not isinstance(args[0], EvView)):
            raise SyntaxException("Expected an event view")
        
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        if len(args)==0:
            view = kargs.get('view')
        else:
            view = args[0] 
        self._registerForEvents( view, useConfig )
    
    #==========================================================================
    def _registerForEvents(self, view, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False
    
    #==========================================================================
    def unregisterForEvents(self):
        """
        Syntax #1:
            EV.unregisterForEvents()
            
            Stop receiving events from GCS
        """
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return False

    #==========================================================================
    def pullEvents(self):
        """
        Syntax #1:
            EV.pullEvents()
            
            Receive next available historical event. Returns True if there
            are more events available, False otherwise 
        """
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return None
            
