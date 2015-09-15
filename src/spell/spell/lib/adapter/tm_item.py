###################################################################################
## MODULE     : spell.lib.adapter.tm_item
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telemetry item model
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

#*******************************************************************************
# Local imports
#*******************************************************************************
from spell.utils.log import *
from config import Configurable
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.utctime import *

#*******************************************************************************
# System imports
#*******************************************************************************
import sys,traceback

###############################################################################
# MODULE CONSTANTS

TM_CHECK_DEFAULTS   = {ValueFormat:ENG, Wait:True, Timeout:5}
TM_MIB_DEFAULTS     = {}
RAW_CHECK_DEFAULTS  = {ValueFormat:RAW, Wait:True, Timeout:5}
COMPARISON_DEFAULTS = {Retries:3,Wait:True,ValueFormat:ENG,Timeout:5}

###############################################################################
# IMPORT DEFINITION

__all__ = [ 'TmItemClass' ]

###############################################################################
class TmItemClass(Configurable):

    """
    DESCRIPTION:
        This class represent a TM parameter in the underlying system.
    """

    # Handle to the TM interface
    __tmClass = None
    # Name of the parameter
    __name = None
    # Description of the parameter 
    __description = None
    # Raw value
    __rawValue = None
    # Eng value
    __engValue = None
    # Status (validity)
    __status = None
    # Sample time
    __sampleTime = None

    #==========================================================================
    def __init__(self, tmClass, name, description = None):
        
        # Super constructor
        Configurable.__init__(self)
        # Initialize basics
        self.__tmClass = tmClass
        self.__name = name
        self.__description = description
        self.__rawValue = None
        self.__engValue = None
        self.__status = None
        self.__sampleTime = None
        # Default item configuration is given by the TM interface
        # TM interface shall be a Configurable as well
        self.setConfig( tmClass )

    #==========================================================================
    def name(self):
        return self.__name

    #==========================================================================
    def setName(self, name):
        self.__name = name

    #==========================================================================
    def description(self):
        return self.__description

    #==========================================================================
    def fullName(self):
        name = self.__name
        if self.__description != None and self.__description != "":
            name += ": " + self.__description
        return name    

    #==========================================================================
    def value(self, *args, **kargs ):
        """
        Syntax #1:
            <default value> = item.value()   
            
            Obtain the default TM value, using the current TM parameter config.
            If the parameter configuration has not been changed, it will match
            the default configuration given by TM interface. 
            
            Configuration establishes:
                - Which is the default value format (RAW/ENG)
                - Refresh the TM parameter: wait for updates or not (Refresh)
            
            To change the parameter configuration, use setConfig().
            
        Syntax #2:
            <default value> = item.value( {Wait:True} )
            <default value> = item.value( wait = True )
            
            Same as #1, but forcing parameter update, whatever the configuration
            is.

        Syntax #3:
            <raw/eng value> = item.value( {ValueFormat:RAW/ENG} )
            <raw/eng value> = item.value( valueformat = RAW/ENG )
            
            Same as #1, returning the RAW/ENG value, whatever the configuration
            is.

        Syntax #4:
            <raw/eng value> = item.value( valueformat = RAW/ENG, wait = True )
            ...
            
            Same as #1, but forcing parameter update, and returning the RAW/ENG
            value, whatever the configuration is.
            
        NOTES: notice that all arguments may be expressed as
        
                a) Modifier :  { Key:value }
                b) Parameter:  key = value
                
               (Pay attention to letter case). For example:
               
               '{ ValueFormat:RAW }' is the same argument as 'valueformat = RAW'
        """
        # Get the parameters to be used. Priority is: 
        #       - (1) Function arguments
        #       - (2) Item configuration
        #       - (3) TM interface configuration
        #       - (4) Hardcoded defaults
        useConfig = self.buildConfig( args, kargs, self.__tmClass.getConfig(), TM_CHECK_DEFAULTS)
    
        # Return the corresponding value        
        if useConfig.get(ValueFormat) == ENG:
            return self.__engValue
        else:
            return self.__rawValue

    #==========================================================================
    def raw(self, *args, **kargs ):
        """
        Syntax #1:
            <raw value> = item.raw()   
            
            Obtain the raw TM value, using the current TM parameter config.
            If the parameter configuration has not been changed, it will match
            the default configuration given by TM interface. 
            
            Configuration establishes:
                - Refreshing the TM parameter: wait for updates or not (Wait)
            
            To change the parameter configuration, use setConfig().
            
        Syntax #2:
            <raw value> = item.raw( True )
            <raw value> = item.raw( {Wait:True} )
            <raw value> = item.raw( wait = True )
            
            Same as #1, but forcing parameter update, whatever the configuration
            is.
        """
        # Return the corresponding value        
        return self.__rawValue

    #==========================================================================
    def status(self, *args, **kargs ):
        """
        Syntax #1:
            <True/False> = item.status()   
            
            Obtain the TM validity, using the current TM parameter config.
            If the parameter configuration has not been changed, it will match
            the default configuration given by TM interface. 
            
            Configuration establishes:
                - Refreshing the TM parameter: wait for updates or not (Wait)
            
            To change the parameter configuration, use setConfig().
            
        Syntax #2:
            <True/False> = item.status( True )
            <True/False> = item.status( {Wait:True} )
            <True/False> = item.status( wait = True )
            
            Same as #1, but forcing parameter update, whatever the configuration
            is.
        """
        # Return the status        
        return self.__status

    #==========================================================================
    def eng(self, *args, **kargs ):
        """
        Syntax #1:
            <eng value> = item.eng()   
            
            Obtain the eng TM value, using the current TM parameter config.
            If the parameter configuration has not been changed, it will match
            the default configuration given by TM interface. 
            
            Configuration establishes:
                - Refreshing the TM parameter: wait for updates or not (Wait)
            
            To change the parameter configuration, use setConfig().
            
        Syntax #2:
            <eng value> = item.eng( True )
            <eng value> = item.eng( wait = True )
            <eng value> = item.eng( {Wait:True} )
            
            Same as #1, but forcing parameter update, whatever the configuration
            is.
        """
        # Return the corresponding value        
        return self.__engValue
        
    #==========================================================================
    def time(self, *args, **kargs ):
        # Return the corresponding value    
        return self.__sampleTime

    #==========================================================================
    def _setTime(self, time):
        self.__sampleTime = time

    #==========================================================================
    def _getTime(self):
        return self.__sampleTime
        
    #==========================================================================
    def _setRaw(self, value):
        self.__rawValue = value

    #==========================================================================
    def _getRaw(self):
        return self.__rawValue
        
    #==========================================================================
    def _setEng(self, value):
        self.__engValue = value

    #==========================================================================
    def _getEng(self):
        return self.__engValue

    #==========================================================================
    def _setStatus(self, value):
        self.__status = value

    #==========================================================================
    def _getStatus(self):
        return self.__status
        
    #==========================================================================
    def refresh(self, *args, **kargs ): 
        """
        Syntax #1:
            item.wait()   
            
            Wait the current TM value, waiting for the next update if the 
            TM item or TM ifc configurations say so. 
            
            To change the parameter configuration, use setConfig().
            
        Syntax #2:
            item.refresh( True )   
            item.refresh( {Wait:True} )   
            item.refresh( wait = True )   
            
            Refresh the current TM value, waiting for the next update.
        """
        # Get the parameters to be used. Priority is: 
        #       - (1) Function arguments
        #       - (2) Item configuration
        #       - (3) TM interface configuration
        #       - (4) Hardcoded defaults
        useConfig = self.buildConfig( args, kargs, self.__tmClass.getConfig(), TM_CHECK_DEFAULTS)
        
        # Update the item value
        # If wait is given by the user, use it. Otherwise,
        # use the TM item or TM ifc configuration.
        self.__tmClass.refresh( self, config = useConfig )

    #===========================================================================
    def setLimit(self, *args, **kargs ):
        return self.__tmClass.setLimit( self, *args, **kargs )

    #===========================================================================
    def getLimit(self, *args, **kargs ):
        return self.__tmClass.getLimit( self, *args, **kargs )

    #===========================================================================
    def getLimits(self, *args, **kargs ):
        return self.__tmClass.getLimits( self, *args, **kargs )

    #===========================================================================
    def setLimits(self, *args, **kargs ):
        return self.__tmClass.setLimits( self, *args, **kargs )

    #==========================================================================
    def __getComparisonData(self, args, kargs):
        # Get the parameters to be used. Priority is: 
        #       - (1) Function arguments
        #       - (2) Item configuration
        #       - (3) TM interface configuration
        #       - (4) Hardcoded defaults
        useConfig = self.buildConfig( args, kargs, self.__tmClass.getConfig(), COMPARISON_DEFAULTS)

        # Obtain the value/item (the first one found)
        value_item = None
        for item in args:
            if not type(item) is dict:
                value_item = item
                break

        return [value_item, useConfig]

    #==========================================================================
    def __getComparisonDataB(self, args, kargs):
        # Get the parameters to be used. Priority is: 
        #       - (1) Function arguments
        #       - (2) Item configuration
        #       - (3) TM interface configuration
        #       - (4) Hardcoded defaults
        useConfig = self.buildConfig( args, kargs, self.__tmClass.getConfig(), COMPARISON_DEFAULTS)

        # Obtain the value/items (the two first found)
        count = 0
        result = []
        for item in args:
            if not type(item) is dict:
                result.append(item)
                count = count + 1
                if count == 2: break
        result.append(useConfig)

        return result
    
    #==========================================================================
    def eq(self, *args, **kargs ): 
        """
        Syntax #1:
            <boolean> = item.eq( <value>|<item> )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.

        Syntax #2:
            <boolean> = item.eq( <value>|<item> , {ValueFormat:RAW/ENG} )
            <boolean> = item.eq( <value>|<item> , valueformat =  RAW/ENG )

            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            
        Syntax #3:
            <boolean> = item.eq( <value>|<item> , retries = <int> )
            <boolean> = item.eq( <value>|<item> , {Retries:<int>} )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        Syntax #4:
            <boolean> = item.eq( <value>|<item> , valueformat = RAW/ENG, retries = <int> )
            <boolean> = item.eq( <value>|<item> , {ValueFormat:RAW/ENG, Retries:<int>} )
            
            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        NOTES:
            - If a TM item is given (<item>), the used value format during 
              comparison is the same for the current and the given item.
            - If a TM item is given and retries shall be done, the system waits 
              for BOTH parameters to be updated for each retry. 
        """
        value_item,useConfig = self.__getComparisonData(args, kargs)

        # Perform comparison. If format and/or retries are none,
        # the TM ifc will use the value_item configuration or its own 
        # configuration.
        return self.__tmClass.eq(self, value_item, config = useConfig)
        
    #==========================================================================
    def neq(self, *args, **kargs ): 
        """
        Syntax #1:
            <boolean> = item.neq( <value>|<item> )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.

        Syntax #2:
            <boolean> = item.neq( <value>|<item> , {ValueFormat:RAW/ENG} )
            <boolean> = item.neq( <value>|<item> , valueformat =  RAW/ENG )

            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            
        Syntax #3:
            <boolean> = item.neq( <value>|<item> , retries = <int> )
            <boolean> = item.neq( <value>|<item> , {Retries:<int>} )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        Syntax #4:
            <boolean> = item.neq( <value>|<item> , valueformat = RAW/ENG, retries = <int> )
            <boolean> = item.neq( <value>|<item> , {ValueFormat:RAW/ENG, Retries:<int>} )
            
            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        NOTES:
            - If a TM item is given (<item>), the used value format during 
              comparison is the same for the current and the given item.
            - If a TM item is given and retries shall be done, the system waits 
              for BOTH parameters to be updated for each retry. 
        """
        value_item,useConfig = self.__getComparisonData(args, kargs)

        # Perform comparison. If format and/or retries are none,
        # the TM ifc will use the value_item configuration or its own 
        # configuration.
        return self.__tmClass.neq(self, value_item, config = useConfig)
        
    #==========================================================================
    def lt(self, *args, **kargs ): 
        """
        Syntax #1:
            <boolean> = item.lt( <value>|<item> )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.

        Syntax #2:
            <boolean> = item.lt( <value>|<item> , {ValueFormat:RAW/ENG} )
            <boolean> = item.lt( <value>|<item> , valueformat =  RAW/ENG )

            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            
        Syntax #3:
            <boolean> = item.lt( <value>|<item> , retries = <int> )
            <boolean> = item.lt( <value>|<item> , {Retries:<int>} )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        Syntax #4:
            <boolean> = item.lt( <value>|<item> , valueformat = RAW/ENG, retries = <int> )
            <boolean> = item.lt( <value>|<item> , {ValueFormat:RAW/ENG, Retries:<int>} )
            
            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        NOTES:
            - If a TM item is given (<item>), the used value format during 
              comparison is the same for the current and the given item.
            - If a TM item is given and retries shall be done, the system waits 
              for BOTH parameters to be updated for each retry. 
        """
        value_item,useConfig = self.__getComparisonData(args, kargs)

        # Perform comparison. If format and/or retries are none,
        # the TM ifc will use the value_item configuration or its own 
        # configuration.
        return self.__tmClass.lt(self, value_item, config = useConfig)
    
    #==========================================================================
    def gt(self, *args, **kargs ): 
        """
        Syntax #1:
            <boolean> = item.gt( <value>|<item> )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.

        Syntax #2:
            <boolean> = item.gt( <value>|<item> , {ValueFormat:RAW/ENG} )
            <boolean> = item.gt( <value>|<item> , valueformat =  RAW/ENG )

            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            
        Syntax #3:
            <boolean> = item.gt( <value>|<item> , retries = <int> )
            <boolean> = item.gt( <value>|<item> , {Retries:<int>} )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        Syntax #4:
            <boolean> = item.gt( <value>|<item> , valueformat = RAW/ENG, retries = <int> )
            <boolean> = item.gt( <value>|<item> , {ValueFormat:RAW/ENG, Retries:<int>} )
            
            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        NOTES:
            - If a TM item is given (<item>), the used value format during 
              comparison is the same for the current and the given item.
            - If a TM item is given and retries shall be done, the system waits 
              for BOTH parameters to be updated for each retry. 
        """
        value_item,useConfig = self.__getComparisonData(args, kargs)

        # Perform comparison. If format and/or retries are none,
        # the TM ifc will use the value_item configuration or its own 
        # configuration.
        return self.__tmClass.gt(self, value_item, config = useConfig)
    
    #==========================================================================
    def le(self, *args, **kargs ): 
        """
        Syntax #1:
            <boolean> = item.le( <value>|<item> )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.

        Syntax #2:
            <boolean> = item.le( <value>|<item> , {ValueFormat:RAW/ENG} )
            <boolean> = item.le( <value>|<item> , valueformat =  RAW/ENG )

            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            
        Syntax #3:
            <boolean> = item.le( <value>|<item> , retries = <int> )
            <boolean> = item.le( <value>|<item> , {Retries:<int>} )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        Syntax #4:
            <boolean> = item.le( <value>|<item> , valueformat = RAW/ENG, retries = <int> )
            <boolean> = item.le( <value>|<item> , {ValueFormat:RAW/ENG, Retries:<int>} )
            
            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        NOTES:
            - If a TM item is given (<item>), the used value format during 
              comparison is the same for the current and the given item.
            - If a TM item is given and retries shall be done, the system waits 
              for BOTH parameters to be updated for each retry. 
        """
        value_item,useConfig = self.__getComparisonData(args, kargs)

        # Perform comparison. If format and/or retries are none,
        # the TM ifc will use the value_item configuration or its own 
        # configuration.
        return self.__tmClass.le(self, value_item, config = useConfig)

    #==========================================================================
    def ge(self, *args, **kargs ): 
        """
        Syntax #1:
            <boolean> = item.ge <value>|<item> )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.

        Syntax #2:
            <boolean> = item.ge <value>|<item> , {ValueFormat:RAW/ENG} )
            <boolean> = item.ge <value>|<item> , valueformat =  RAW/ENG )

            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            
        Syntax #3:
            <boolean> = item.ge <value>|<item> , retries = <int> )
            <boolean> = item.ge <value>|<item> , {Retries:<int>} )
            
            Check if defaut value of this TM parameter is equal to the given value
            or the default value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        Syntax #4:
            <boolean> = item.ge <value>|<item> , valueformat = RAW/ENG, retries = <int> )
            <boolean> = item.ge <value>|<item> , {ValueFormat:RAW/ENG, Retries:<int>} )
            
            Check if RAW/ENG value of this TM parameter is equal to the given value
            or the RAW/ENG value of the given TM parameter.
            If comparison fails, retry <int> times (waiting for updates).
            
        NOTES:
            - If a TM item is given (<item>), the used value format during 
              comparison is the same for the current and the given item.
            - If a TM item is given and retries shall be done, the system waits 
              for BOTH parameters to be updated for each retry. 
        """
        value_item,useConfig = self.__getComparisonData(args, kargs)

        # Perform comparison. If format and/or retries are none,
        # the TM ifc will use the value_item configuration or its own 
        # configuration.
        return self.__tmClass.ge(self, value_item, config = useConfig)

    #==========================================================================
    def between(self, *args, **kargs ): 
        """
        Syntax #1:
            <boolean> = item.between( <value>|<item>, <value>|<item> )
            
            Check if defaut value of this TM parameter is between the given values
            or the default values of the given TM parameters.

        Syntax #2:
            <boolean> = item.between( <value>|<item> ,<value>|<item>, valueformat = RAW/ENG )
            <boolean> = item.between( <value>|<item> ,<value>|<item>, { ValueFormat:RAW/ENG} )

            Check if RAW/ENG value of this TM parameter is between the given values
            or the RAW/ENG values of the given TM parameters.
            
        Syntax #3:
            <boolean> = item.between( <value>|<item> , <value>|<item>, retr = <int> )
            <boolean> = item.between( <value>|<item> , <value>|<item>, { Retries:<int>} )
            
            Check if defaut value of this TM parameter is between the given values
            or the default values of the given TM parameters.
            If comparison fails, retry <int> times (waiting for updates).
            
        Syntax #4:
            <boolean> = item.between( <value>|<item> ,<value>|<item>, {ValueFormat:RAW/ENG, Retries:<int>} )
            <boolean> = item.between( <value>|<item> ,<value>|<item>, valueformat = RAW/ENG, retries = <int> )
            
            Check if RAW/ENG value of this TM parameter is between the given values
            or the RAW/ENG values of the given TM parameters.
            If comparison fails, retry <int> times (waiting for updates).
            
        NOTES:
            - If TM items are given (<item>), the used value format during 
              comparison is the same for the current and the given items.
            - If TM items are given and retries shall be done, the system waits 
              for ALL parameters to be updated for each retry. 
        """
        value_item_l,value_item_g,useConfig = self.__getComparisonDataB(args, kargs)

        # Perform comparison. If format and/or retries are none,
        # the TM ifc will use the value_item configuration or its own 
        # configuration.
        return self.__tmClass.between(value_item_l, self, value_item_g, config = useConfig)

    #==========================================================================
    def not_between(self, *args, **kargs ): 
        """
        Syntax #1:
            <boolean> = item.not_between( <value>|<item>, <value>|<item> )
            
            Check if defaut value of this TM parameter is between the given values
            or the default values of the given TM parameters.

        Syntax #2:
            <boolean> = item.not_between( <value>|<item> ,<value>|<item>, valueformat = RAW/ENG )
            <boolean> = item.not_between( <value>|<item> ,<value>|<item>, { ValueFormat:RAW/ENG} )

            Check if RAW/ENG value of this TM parameter is between the given values
            or the RAW/ENG values of the given TM parameters.
            
        Syntax #3:
            <boolean> = item.not_between( <value>|<item> , <value>|<item>, retr = <int> )
            <boolean> = item.not_between( <value>|<item> , <value>|<item>, { Retries:<int>} )
            
            Check if defaut value of this TM parameter is between the given values
            or the default values of the given TM parameters.
            If comparison fails, retry <int> times (waiting for updates).
            
        Syntax #4:
            <boolean> = item.not_between( <value>|<item> ,<value>|<item>, {ValueFormat:RAW/ENG, Retries:<int>} )
            <boolean> = item.not_between( <value>|<item> ,<value>|<item>, valueformat = RAW/ENG, retries = <int> )
            
            Check if RAW/ENG value of this TM parameter is between the given values
            or the RAW/ENG values of the given TM parameters.
            If comparison fails, retry <int> times (waiting for updates).
            
        NOTES:
            - If TM items are given (<item>), the used value format during 
              comparison is the same for the current and the given items.
            - If TM items are given and retries shall be done, the system waits 
              for ALL parameters to be updated for each retry. 
        """
        value_item_l, value_item_g,useConfig = self.__getComparisonDataB(args, kargs)

        # Perform comparison. If format and/or retries are none,
        # the TM ifc will use the value_item configuration or its own 
        # configuration.
        return self.__tmClass.not_between(value_item_l, self, value_item_g, config = useConfig)

    #==========================================================================
    def verify(self, *args, **kargs ):
        """
        Syntax #1:
            <boolean> item.verify( <value>|<item>, <comp> )
            
            Perform the given comparison against the given value or TM item

        Syntax #2:
            <boolean> item.verify( <value>|<item>, <comp>, <modifiers> )
            
            Perform the given comparison against the given value or TM item,
            using the given configuration. Possible modifiers/parameters
            to be used are:
            
                - Timeout: in seconds, for TM checks
                - ValueFormat: RAW/ENG
                - Wait: True/False
                - Retries: <int>
        """
        # Get the parameters to be used. Priority is: 
        #       - (1) Function arguments
        #       - (2) Item configuration
        #       - (3) TM interface configuration
        #       - (4) Hardcoded defaults
        useConfig = self.buildConfig( args, kargs, self.__tmClass.getConfig(), COMPARISON_DEFAULTS)

        # Obtain arguments
        value_item = args[0]
        comparison = args[1]
        
        # Perform verificaton (global config for 'verify' is empty here)
        return self.__tmClass.verify( [[ self, comparison, value_item, useConfig ]] )
