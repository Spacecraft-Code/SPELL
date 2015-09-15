###################################################################################
## MODULE     : spell.lib.adapter.tm
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telemetry interface
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
from spell.lib.registry import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from tm_item import TmItemClass
from verifier import TmVerifierClass
from config import Configurable
from constants.core import COMP_SYMBOLS
from result import TmResult
from interface import Interface
from expression import ExpressionVerifier,Expression

#*******************************************************************************
# System imports
#*******************************************************************************
import time,string,thread,sys

###############################################################################
# Module import definition

__all__ = ['TmInterface']

INTERFACE_DEFAULTS = {  OnFailure:ABORT | SKIP | RECHECK | CANCEL, 
                        ValueFormat:ENG, 
                        Timeout:15, 
                        Retries:2,
                        Tolerance:0,
                        Wait:False,
                        PromptUser:True,
                        OnFalse:PROMPT,
                        OnTrue:NOPROMPT,
                        OnSkip:True,
			            IgnoreCase:False }

EPSILON = 2.1e-5

###############################################################################
class TmInterface(Configurable, Interface):

    """
    This class provides the TM management interface. Feature methods shall
    be overriden by driver concrete interfaces.
    """

    __tmParameters = {}
    __verifiers = []
    __verifTable = []
    __verifMutex = None
    __useConfig = {}
    
    #===========================================================================
    def __init__(self):
        Interface.__init__(self, "TM")
        Configurable.__init__(self)
        self.__tmParameters = {}
        self.__verifiers = []
        self.__verifTable = []
        self.__verifMutex = thread.allocate_lock()
        self.__ctxName = None
        LOG("Created")
        
    #===========================================================================
    def __getitem__(self, key):
        # If the parameter mnemonic is composed of several words:
        words = key.split()
        mnemonic = key
        description = None
        if len(words)>1 and words[0].upper() == 'T':
            mnemonic = words[1]
            description = ' '.join(words[2:])
        else:
            mnemonic = key
            description = key

        if not mnemonic in self.__tmParameters.keys():
            LOG("Creating TM item for " + mnemonic)
            LOG("Description: " + repr(description))
            item = self._createTmItem(mnemonic,description)
            self.__tmParameters[mnemonic] = item
        else:
            item = self.__tmParameters.get(mnemonic)
        return item

    #===========================================================================
    def refreshConfig(self):
        ctxConfig = self.getContextConfig()
        languageDefaults = ctxConfig.getInterfaceConfig(self.getInterfaceName())
        if languageDefaults:
            INTERFACE_DEFAULTS.update(languageDefaults)
        self.setConfig( INTERFACE_DEFAULTS )
        LOG("Configuration loaded", level = LOG_CNFG )

    #===========================================================================
    def _createTmItem(self, mnemonic, description = ""):
        return TmItemClass(self, mnemonic, description)

    #===========================================================================
    def setup(self, ctxConfig, drvConfig):
        LOG("Setup TM adapter interface")
        self.storeConfig(ctxConfig, drvConfig)
        self.refreshConfig()

    #===========================================================================
    def cleanup(self):
        LOG("Cleanup TM adapter interface")

    #===========================================================================
    def eq(self, *args, **kargs):
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_eq
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[eq], config = useConfig )        
            
    #===========================================================================
    def neq(self, *args, **kargs):
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_neq
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[neq], config = useConfig )        
    
    #===========================================================================
    def lt(self, *args, **kargs):
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_lt
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[lt], config = useConfig )        
    
    #===========================================================================
    def le(self, *args, **kargs):
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_le
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[le], config = useConfig )        
    
    #===========================================================================
    def gt(self, *args, **kargs):
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_gt
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[gt], config = useConfig )        
    
    #===========================================================================
    def ge(self, *args, **kargs):
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        value_item = args[1]
        cFunc = self.__c_ge
        return self.__comparator(param, value_item, cFunc, COMP_SYMBOLS[ge], config = useConfig )        
    
    #===========================================================================
    def between(self, *args, **kargs):
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        lvalue_item = args[1]
        gvalue_item = args[2]
        cFunc = self.__c_btw
        return self.__bcomparator(param, lvalue_item, gvalue_item, cFunc, COMP_SYMBOLS[bw], config = useConfig)
    
    #===========================================================================
    def not_between(self, *args, **kargs):
        self.__checkComparsionArgs(args, kargs)
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        lvalue_item = args[1]
        gvalue_item = args[2]
        cFunc = self.__c_nbtw
        return self.__bcomparator(param, lvalue_item, gvalue_item, cFunc, COMP_SYMBOLS[nbw], config = useConfig)
                  
    #===========================================================================
    def refresh(self, *args, **kargs):
        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        if len(args)==0:
            for param in self.__tmParameters:
                param = self.__tmParameters[param] 
                self.__refreshItemNotify(param, useConfig)
        else:
            param = args[0]
            if type(param)==dict:
                for param in self.__tmParameters:
                    param = self.__tmParameters[param] 
                    self.__refreshItemNotify(param, useConfig)
            else:                         
                if type(param) == str: param = self.__tmParameters[param] 
                self.__refreshItemNotify(param, useConfig)

    #===========================================================================
    def inject(self, *args, **kargs ):
        if len(args)==0 and len(kargs)==0:
            raise SyntaxException("No arguments given")

        useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        result = False
        if type(args[0])==list:
            injectionList = args[0]
            for item in injectionList:
                itemConfig = {}
                itemConfig.update(useConfig)
                param = item[0]
                value = item[1]
                if type(item[-1])==dict:
                    itemConfig.update(item[-1])
                if type(param)==str:
                    param = self[param]
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), str(value), NOTIF_STATUS_PR, "Injecting value")
                result = self._injectItem( param, value, itemConfig )
                if result == True:
                    REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), str(value), NOTIF_STATUS_OK)
                else:
                    REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), str(value), NOTIF_STATUS_FL)
        else:
            # Single parameter injection
            if type(args[0])==str:
                param = self[args[0]]
            # Value
            value = args[1]
            # Update with specific configuration if any
            if type(args[-1])==dict:
                useConfig.update(args[-1])
            # Inject the parameter
            REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), str(value), NOTIF_STATUS_PR, "Injecting value")
            result = self._injectItem( param, value, useConfig )
            if result == True:
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), str(value), NOTIF_STATUS_OK)
            else:
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), str(value), NOTIF_STATUS_FL)
                
        return result

    #===========================================================================
    def verify(self, *args, **kargs ):
        if len(args)==0 and len(kargs)==0:
            raise SyntaxException("No arguments given")
        
        # Obtain global verification config
        self.__useConfig = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)

        # Input cases ----------------------------------------------------------
        numArgs = len(args)
        
        # If the user gives the steps using 'verify =' 
        if numArgs==0:
            # No steps are given as a normal parameter, but the user may be
            # using the keyword "verify" to pass them.
            if kargs.has_key('verify'):
                verificationSteps = kargs.get('verify')
                LOG("Retrieved definition from named argument")
            else:
                raise SyntaxException("Malformed verification: no steps given")
        else:
            # The user is either passing a simple verification step givin TM parameter,
            # comparison operator, and valueitem, or passing a list of verification
            # steps. In addition to this, if the user passes a config dictionary,
            # it is already processed by buildConfig(), thus we shall remove it.
            
            # Check if the last element of the tuple is a dictionary
            if type(args[-1])==dict:
                iargs = args[0:-1]
            else:
                iargs = args
            
            # Now, if the length of the remaining tuple is one, it means that
            # a single step or a list of steps are given. If the length of
            # the tuple is more than one, the user passed the verification
            # parameters without square brackets.
            numArgs = len(iargs)
            if numArgs > 1:
                # Create a list of steps, with a single step containing the
                # passed parameters
                if numArgs < 3:
                    raise SyntaxException("Malformed condition")
                verificationSteps = [ [ item for item in args ] ]
                LOG("Built single step list")
            else:
                # Get rid of the tuple and get the first element only
                iargs = iargs[0]
                # Here we have a list. To distinguish between a single step or a 
                # list of steps, just check if the first element is a list 
                # (a step) or not
                if isinstance(iargs,Expression):
                    verificationSteps = iargs
                elif type(iargs[0])==list:
                    verificationSteps = iargs
                    LOG("Using direct definition")
                else:
                    if len(iargs)<3:
                        raise SyntaxException("Malformed condition")
                    verificationSteps = [ iargs ] 
                    LOG("Built list of steps" + repr(iargs))

        if type(verificationSteps)==list:
            return self.performListVerification(verificationSteps)
        else:
            return self.performExpressionVerification(verificationSteps)

    #===========================================================================
    def performListVerification(self, verificationSteps):
        try:
            self._operationStart()
            
            # Prepare verifiers
            self.__prepareVerification(verificationSteps)
    
            # Start verifiers
            #TODO review
            self.__startVerifiers()
            
            # Wait all verifications to be finished
            self.__waitVerifiers()
            
            # Check overall result
            overallResult,errors = self.__checkVerifiers()
    
            # If there is a failure somewhere
            if not overallResult:
                self.reportVerificationResult(errors)
            
        finally:
            # Cleanup verifiers
            self.__resetVerification()
            self._operationEnd()
        
        return overallResult

    #===========================================================================
    def performExpressionVerification(self, expression ):

        try:
            self._operationStart()
            self.__resetVerification()        
            
            verifier = ExpressionVerifier(expression)
    
            verifier.prepare(self.__useConfig,self.__verifiers,self.__verifTable)
            
            overallResult,errors = verifier.evaluate()
            
    
            # If there is a failure somewhere
            if not overallResult:
                REGISTRY['CIF'].write("Telemetry expression evaluates to False", {Severity:ERROR})
                self.reportVerificationResult(errors)
            else:
                REGISTRY['CIF'].write("Telemetry expression evaluates to True" )
            
        finally:
            # cleanup expression verifier
            verifier.clear()
            del verifier
            # Cleanup list verifiers
            self.__resetVerification()
            self._operationEnd()

        return overallResult

    #===========================================================================
    def reportVerificationResult(self, errors):
        # This will hold the description of the errors
        description = ""
        
        # First, if there is a system failure somewhere, report it
        # unless PromptFailure is false
        thereIsFailure = False
        wasStopped = False
        for key,reason,withError,failed,stopped in errors:
            thereIsFailure = (thereIsFailure or withError)
            wasStopped = (wasStopped or stopped)
            description += ("  %-10s" % key.split(":")[1]) + ": " + reason 
            if withError:
                description += " (failed)\n"
            elif stopped:
                description += " (stopped)\n"
            elif failed:
                description += " (NOK)\n"
            else:
                description += " (OK)\n"
        
        if thereIsFailure: 
            if self.__useConfig.get(PromptFailure) != False:
                raise DriverException("Verification failed", "Could not evaluate all TM conditions\n" + description)
            else:
                # Do not raise the exception
                pass
        elif wasStopped:
            if not ("InWaitFor" in self.__useConfig): 
                raise DriverException("Verification stopped", "Aborted by user\n" + description)
            else:
                return False
        # No failure but still the verification did not succeed,
        # then check Prompt User                     
        else:
            if self.__useConfig.get(PromptUser)!=False:
                raise DriverException("Verification failed", "Some of the conditions evaluated to False\n" + description)
            else:
                # Do not raise the exception
                pass

        whichError = ""
        for ed in errors:
            if ed[1] is not None:
                if len(whichError)>0: whichError += ","
                whichError += ed[0].split()[-1]
    
    #===========================================================================
    def updateVerificationStatus(self, verifier):
        LOG("Verification status: " + verifier.name + "=" + verifier.value + "," + verifier.status) 
        if self.__useConfig.has_key(Notify):
            if not self.__useConfig.get(Notify): return
        self.__verifMutex.acquire()
        entry = self.__verifTable[verifier.step]
        entry[1] = verifier.value
        entry[2] = verifier.status
        entry[3] = verifier.reason
        entry[4] = verifier.updtime
        names  = ""
        values = ""
        status = ""
        reason = ""
        times  = ""
        for entry in self.__verifTable:
            if len(names)>0: 
                names  = names + ITEM_SEP
                values = values + ITEM_SEP
                status = status + ITEM_SEP
                reason = reason + ITEM_SEP
                times  = times + ITEM_SEP
            names = names + entry[0]
            values = values + entry[1]
            status = status + entry[2]
            times = times + entry[4]
            if len(entry[3])==0:
                reason = reason + " "
            else:
                reason = reason + entry[3]
            
        REGISTRY['CIF'].notify( NOTIF_TYPE_VERIF, names, values, status, reason, times)
        self.__verifMutex.release()

    #===========================================================================
    def setTMparam(self, *args, **kargs):
        config = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        properties = args[1]
        return self._setTMparam(param, properties, config)

    #===========================================================================
    def getTMparam(self, *args, **kargs):
        config = self.buildConfig(args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        param = args[0]
        prop = args[1]
        return self._getTMparam(param, prop, config)

    #===========================================================================
    def _setTMparam(self, *args, **kargs):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})

    #===========================================================================
    def _getTMparam(self, param, prop, config):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
    
    #===========================================================================
    def setLimit(self, *args, **kargs ):
        
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        #TODO implement more flexible input
        if len(args)!=3: 
            raise SyntaxException("Expected parameter, limit name and value")

        param = args[0]
        limit = args[1]
        value = args[2]

        if type(param)==str:
            param = self[param]

        if type(limit)!=str:
            raise SyntaxException("Expected a limit name")
        
        return self._setLimit( param, limit, value, useConfig)

    #===========================================================================
    def getLimit(self, *args, **kargs ):

        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        #TODO implement more flexible input
        if len(args)!=2: 
            raise SyntaxException("Expected parameter, and limit name")

        param = args[0]
        limit = args[1]

        if type(param)==str:
            param = self[param]

        if type(limit)!=str:
            raise SyntaxException("Expected a limit name")
        
        return self._getLimit( param, limit, useConfig)

    #===========================================================================
    def getLimits(self, *args, **kargs ):

        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        #TODO implement more flexible input
        if len(args)!=1: 
            raise SyntaxException("Expected parameter")

        param = args[0]

        if type(param)==str:
            param = self[param]

        return self._getLimits( param, useConfig)

    #===========================================================================
    def setLimits(self, *args, **kargs ):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        
        if len(args)!=2: 
            raise SyntaxException("Expected parameter and limits values")

        param = args[0]
        limits = args[1]

        if type(param)==str:
            param = self[param]

        return self._setLimits( param, limits, useConfig )

    #===========================================================================
    def loadLimits(self, *args, **kargs ):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        limitsList = args[0]
        return self._loadLimits( limitsList, useConfig )

    #===========================================================================
    def restoreNormalLimits(self, *args, **kargs ):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        return self._restoreNormalLimits( useConfig )

    #===========================================================================
    def databaseLookup(self, *args, **kargs):
        useConfig = self.buildConfig( args, kargs, self.getConfig(), INTERFACE_DEFAULTS)
        name   = args[0]
        rtype  = args[1]
        source = args[2]
        return self._databaseLookup( name, rtype, source, useConfig )

    #===========================================================================
    def _restoreNormalLimits(self, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return True
               
    #===========================================================================
    def _refreshItem(self, param, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _injectItem(self, param, value, config = {} ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _setLimit(self, param, limit, value, config ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _getLimit(self, param, limit, config ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _getLimits(self, param, config ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _setLimits(self, param, limits, config ):
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 

    #===========================================================================
    def _loadLimits(self, param, limits, config ):
        """
        param: TM item class or string with the param name
        limits: dictionary containing the limit definitions with LoYel, Midpoint, Expected, etc
        """
        REGISTRY['CIF'].write("Service not implemented on this driver", {Severity:WARNING})
        return 
               
    #===========================================================================
    def _operationStart(self):
        LOG("TM interface started operation")

    #===========================================================================
    def _operationEnd(self):
        LOG("TM interface finished  operation")
               
    #===========================================================================
    def __refreshItemNotify(self, param, useConfig):
        try:
            doNotify = useConfig.get(Notify) == True
            doWait   = useConfig.get(Wait) == True
            doExtended = useConfig.get(Extended) == True
            if doNotify and doWait:
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), "???", NOTIF_STATUS_PR, "" )
            self.__refreshItemValidity(param, useConfig)
            if doNotify and not doExtended:
                if useConfig.get(ValueFormat) == ENG:
                    value = repr(param._getEng())
                else:
                    value = repr(param._getRaw())
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), str(value), NOTIF_STATUS_OK, "" )
        except DriverException,ex:
            if doNotify:
                reason = ex.message
                REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, param.fullName(), "???", NOTIF_STATUS_FL, reason )
            raise ex

    #===========================================================================
    def __refreshItemValidity(self, param, config):
        value,validity = self._refreshItem(param, config)
        if not validity:
            raise DriverException("Parameter invalid")
        return [value,validity]
    
    #===========================================================================
    def __calccounter(self, retries):
        counter = retries
        if counter < 1: 
            counter = 1
        return counter
        
    #===========================================================================
    def __timeiter(self, value, config):
        if type(value)==list:
            comp = []
            for item in value:
                if isinstance(item, TmItemClass):
                    iv,status = self.__refreshItemValidity( item, config )
                    comp.append(iv)
                else:
                    comp.append(item)
        else:
            if isinstance(value, TmItemClass):
                comp,status = self.__refreshItemValidity( value, config )
            else:
                comp = value
        return comp

    #===========================================================================
    def __comparator(self, param, value_item, cFunc, symbol, config = {} ):
        
        # If no configuration is given, use the interface defaults
        if len(config)==0:
            config = self.getConfig()

        # Retrieve configuration parameters
        retries = config.get(Retries)
        tolerance = config.get(Tolerance)
        ignoreCase = config.get(IgnoreCase)

        # Retrieve initial values
        if type(param)==str:
            param = self[param]
        
        # Prepare comparison tools
        counter = self.__calccounter(retries)
        comp = self.__timeiter(value_item, config)

        # In the first iteration the user config will be used. In the rest
        # of retries, wait will be true in any case.
        firstCheck = True

        LOG("### Starting TM comparison")
        comparisonResult = False

        # Perform comparisons        
        while counter > 0:
            
            # Abort verifications on pause or abort commands
            for verifier in self.__verifiers:
                if verifier.stopped:
                    return False
            
            if firstCheck: 
                firstCheck = False
            else:
                config[Wait] = True
 
            LOG("### (" + str(counter) + ") Retrieving " + param.name() + " comparison value")
            cvalue,status = self.__refreshItemValidity(param, config)

            LOG("### (" + str(counter) + ") Comparing " + repr(param.name()) + "=" + repr(cvalue) + " against " + str(comp) +\
                ", iteration " + str(counter) + ", tolerance " + str(tolerance))
            LOG("### (" + str(counter) + ") Comparison config: " + repr(config))
            
            #-----------------------------------------------------------------------
            # Type checking
            self.__checkComparisonTypes(cvalue,comp)

            if cFunc(cvalue,comp,tolerance,ignoreCase):
                LOG("### (" + str(counter) + ") Comparison success") 
                comparisonResult = True
                break
            else:
                LOG("### (" + str(counter) + ") Comparison failed")
                comparisonResult = False
                
            if config.has_key(Notify) and config[Notify] == True:
                LOG("### (" + str(counter) + ") Notify comparison retry")
                val = symbol + str(comp)
                reason = "Value is " + str(cvalue) + ", retrying comparison (" + str(counter) + " left)"
                if config.has_key("STEP_ID"):
                    step = config.get("STEP_ID")
                    for verifier in self.__verifiers:
                        if verifier.step == step:
                            verifier.value = val
                            verifier.reason = reason
                            #REGISTRY['CIF'].write( "Retrying comparison for " + param.name() + " (" + str(counter) + " retries left)", {Severity:WARNING})
                            self.updateVerificationStatus(verifier)
                            break
                else:
                    REGISTRY['CIF'].notify( NOTIF_TYPE_VERIF, param.fullName(), str(val), NOTIF_STATUS_PR, reason )

            comp = self.__timeiter(value_item, config)
            counter = counter - 1

        LOG("### Finished TM comparison: " + repr(comparisonResult))

        return comparisonResult

    #===========================================================================
    def __bcomparator(self, param, lvalue_item, gvalue_item, cFunc, symbol, config = {} ):
        # If no configuration is given, use the interface defaults
        if len(config)==0:
            config = self.getConfig()

        # Retrieve configuration parameters
        retries = config.get(Retries)
        strict = config.get(Strict)
        tolerance = config.get(Tolerance)

        # Retrieve initial values
        if type(param)==str:
            param = self[param]
        cvalue,status = self.__refreshItemValidity(param, config)

        counter = self.__calccounter(retries)
        comp_lt = self.__timeiter(lvalue_item, config)
        comp_gt = self.__timeiter(gvalue_item, config)
        
        firstCheck  = True
        
        while counter > 0: 

            # Abort verifications on pause or abort commands
            for verifier in self.__verifiers:
                if verifier.stopped:
                    return False

            #-----------------------------------------------------------------------
            # Type checking
            self.__checkComparisonTypes(cvalue,comp_lt)
            self.__checkComparisonTypes(cvalue,comp_gt)

            if cFunc(cvalue,comp_lt, comp_gt, strict, tolerance): return True
            
            if firstCheck: 
                firstCheck = False
            else:
                config[Wait] = True
            
            cvalue,status = self.__refreshItemValidity(param, config)
            comp_lt = self.__timeiter(lvalue_item, config)
            comp_gt = self.__timeiter(gvalue_item, config)
            counter = counter - 1
            if config.has_key(Notify) and config[Notify] == True:
                val = symbol + " " + str(comp_lt) + "," + str(comp_gt)
                reason = "Value is " + str(cvalue) + ", retrying comparison (" + str(counter) + ")"
                if config.has_key("STEP_ID"):
                    step = config.get("STEP_ID")
                    for verifier in self.__verifiers:
                        if verifier.step == step:
                            verifier.value = val
                            verifier.reason = reason
                            self.updateVerificationStatus(verifier)
                            break
                else:
                    REGISTRY['CIF'].notify( NOTIF_TYPE_VERIF, param.fullName(), str(val), NOTIF_STATUS_PR, reason )
        return False

    #===========================================================================
    def __c_eq(self, cvalue, comp, tolerance = 0, ignoreCase = False):
        
        if type(comp)==list:
            for item in comp:
                if self.__c_eq(cvalue, item, tolerance, ignoreCase): return True
            return False

        #-----------------------------------------------------------------------
        # String comparisons (tolerance does not apply)
        if type(cvalue) == str:
            
            if ignoreCase:
                return (cvalue.upper() == comp.upper())
            else:
                return cvalue == comp
        #-----------------------------------------------------------------------
        # Numeric comparisons (ignoreCase does not apply)
        else:
            
            if tolerance<0:
                raise DriverException("Error: cannot accept negative tolerance")
            elif tolerance>0:
                return self.__c_btw(cvalue, comp-tolerance, comp+tolerance, False)
            else: 
                if type(cvalue)==float or type(comp)==float:
                    dnm = max(abs(cvalue),abs(comp))
                    if not dnm > 0: dnm = 1 
                    return ( abs(cvalue-comp) / dnm ) < EPSILON
                else:
                    return (cvalue == comp)
        
    #===========================================================================
    def __c_neq(self, cvalue, comp, tolerance = 0, ignoreCase = False): 

        if type(comp)==list:
            for item in comp:
                if self.__c_neq(cvalue, item, tolerance, ignoreCase): return True
            return False

        #-----------------------------------------------------------------------
        # String comparisons (tolerance does not apply)
        if type(cvalue) == str:
            if ignoreCase:
                return (cvalue.upper() != comp.upper())
            else:
                return cvalue != comp
        #-----------------------------------------------------------------------
        # Numeric comparisons (ignoreCase does not apply)
        else:
            if tolerance<0:
                raise DriverException("Error: cannot accept negative tolerance")
            elif tolerance>0:
                return self.__c_nbtw(cvalue, comp-tolerance, comp+tolerance, False)
            else: 
                if type(cvalue)==float or type(comp)==float:
                    dnm = max(abs(cvalue),abs(comp))
                    if not dnm > 0: dnm = 1 
                    return ( abs(cvalue-comp) / dnm ) > EPSILON
                else:
                    return (cvalue != comp)
    
    #===========================================================================
    def __c_lt(self, cvalue, comp, tolerance = 0, ignoreCase = False):
         
        if type(comp)==list:
            for item in comp:
                if self.__c_lt(cvalue, item, tolerance, ignoreCase): return True
            return False
         
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if type(cvalue)==float or type(comp)==float:
            notEqual = self.__c_neq(cvalue, comp, 0, ignoreCase) 
            return notEqual and (cvalue < comp)
        else:
            return (cvalue < comp)
        
    #===========================================================================
    def __c_le(self, cvalue, comp, tolerance = 0, ignoreCase = False):

        if type(comp)==list:
            for item in comp:
                if self.__c_le(cvalue, item, tolerance, ignoreCase): return True
            return False
        
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if type(cvalue)==float or type(comp)==float:
            equal = self.__c_eq(cvalue, comp, 0, ignoreCase) 
            return equal or (cvalue < comp)
        else:
            return (cvalue <= comp)
        
    #===========================================================================
    def __c_gt(self, cvalue, comp, tolerance = 0, ignoreCase = False):
        
        if type(comp)==list:
            for item in comp:
                if self.__c_gt(cvalue, item, tolerance, ignoreCase): return True
            return False

        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if type(cvalue)==float or type(comp)==float:
            notEqual = self.__c_neq(cvalue, comp, 0, ignoreCase) 
            return notEqual and (cvalue > comp)
        else:
            return (cvalue > comp)
        
    #===========================================================================
    def __c_ge(self, cvalue, comp, tolerance = 0, ignoreCase = False):
        
        if type(comp)==list:
            for item in comp:
                if self.__c_ge(cvalue, item, tolerance, ignoreCase): return True
            return False
         
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if type(cvalue)==float or type(comp)==float:
            equal = self.__c_eq(cvalue, comp, 0, ignoreCase) 
            return equal or (cvalue > comp)
        else:
            return (cvalue >= comp)
                
    #===========================================================================
    def __c_btw(self, cvalue, lcomp, gcomp, strict, tolerance = 0):
        
        if type(lcomp)==list or type(gcomp)==list:
            raise DriverException("Error: cannot use this operator with lists")
        
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        
        if tolerance<0:
            raise DriverException("Error: cannot accept negative tolerance")
        else:
            if strict:
                return (lcomp - tolerance < cvalue < gcomp + tolerance)
            else:
                return (lcomp - tolerance <= cvalue <= gcomp + tolerance)
            
    #===========================================================================
    def __c_nbtw(self, cvalue, lcomp, gcomp, strict, tolerance = 0): 

        if type(lcomp)==list or type(gcomp)==list:
            raise DriverException("Error: cannot use this operator with lists")
        
        if type(cvalue) == str:
            raise DriverException("Error: cannot use this operator with strings")
        if tolerance<0:
            raise DriverException("Error: cannot accept negative tolerance")
        else:
            if strict:
                return (lcomp + tolerance > cvalue) or (gcomp - tolerance < cvalue)
            else:
                return (lcomp + tolerance >= cvalue) or (gcomp - tolerance <= cvalue)

    #===========================================================================
    def __checkComparisonTypes(self, cvalue, comp):
        if type(cvalue) == str and type(comp) != str:
            raise DriverException("Comparing a string against a value of type " + str(type(comp)))
        elif type(cvalue) == bool and type(comp) != bool:
            raise DriverException("Comparing a boolean value against a value of type " + str(type(comp)))
        else:
            if type(cvalue) in [int,long,float] and type(comp) not in [int,long,float]:
                raise DriverException("Comparing a value of type " + str(type(cvalue)) +" against a value of type " + str(type(comp)))

    #===========================================================================
    def __checkComparsionArgs(self, args, kargs):
        if len(args)==0 and len(kargs)==0:
            raise SyntaxException("Received no parameters")
        
    #===========================================================================
    def __resetVerification(self):
        self.__verifTable = []
        for v in self.__verifiers[:]:
            self.__verifiers.remove(v)
                    
    #===========================================================================
    def __prepareVerification(self, verificationSteps):
        self.__resetVerification()
        stepCount = 0
        REGISTRY['CIF'].write( "Verifying telemetry conditions" )
        for step in verificationSteps:
            verifier = TmVerifierClass( stepCount, step, self.__useConfig )
            self.__verifiers.append( verifier )
            self.__verifTable.append([verifier.name,verifier.value,
                                      verifier.status,verifier.reason,verifier.updtime])
            stepCount = stepCount + 1

    #===========================================================================
    def __startVerifiers(self):
        for v in self.__verifiers: v.start()

    #===========================================================================
    def __waitVerifiers(self):
        while True:
            someAlive = False
            for v in self.__verifiers:
                v.join(0.2)
                if v.isAlive():
                    someAlive = True
                    break
            if not someAlive: return

    #===========================================================================
    def __checkVerifiers(self):
        
        # Show verification information
        verifMessage = ""
        for v in self.__verifiers:
            defn = v.getDefinition()
            paramName = v.getParamName()
            message = "    %-2s" % str(defn[0]) + ": Parameter " + repr(paramName) + \
                      " " + COMP_SYMBOLS[defn[1][1]] + " " + str(defn[1][2])

            # Take into account ternary operators
            if len(defn[1]) >= 4 and type(defn[1][3])!=dict: # it is value of ternary op
                message = message + " and " + str(defn[1][3])
            
            # Configuration
            configText = ""
            if len(defn[1]) >=4 and type(defn[1][-1])==dict:
                configText = ", ";
                configDict = defn[1][-1]
                for key in configDict:
                    if len(configText)>0: 
                        configText += " "
                    configText += str(key) + " = " + str(configDict.get(key))

            if len(verifMessage)>0: verifMessage += "\n"
            verifMessage += message + configText
        REGISTRY['CIF'].write(verifMessage)
        
        # Overall result of verification
        someWrong = False
        # Will be true if user interaction has been disabled
        superseded = False
        # Will be true if verification was interrupted
        stopped = False
        overallResult = TmResult() 
        errors = []
        for v in self.__verifiers:
            defn = v.getDefinition()
            keyName = str(defn[0]) + ": " + str(defn[1][0])
            overallResult[keyName] = (not v.failed)
            # This will be used later to decide if an exception shall be raised
            errors.append( [ keyName, v.reason, (v.error != None), v.failed, v.stopped ] )

            # Verifier step number
            stepNum = str(defn[0])
            
            # Decide how to report the failed verification step
            if v.stopped:
                stopped = True
                overallResult = False
                break

            elif v.failed:
                # Mark the error
                someWrong = True
                # If PromptUser is True, we do not need to check anything else
                # directly report the error
                if defn[2][PromptUser] == True:
                    severity = ERROR
                # If PromptUser is false, we will ignore the failed check provided
                # that it is not caused by a system failure (timeout, invalid param) 
                elif (v.error is None):
                    superseded = True
                    severity = INFORMATION
                # If there is a system failure, we will report it
                # unless explicitly said not to (PromptFailure=False)
                elif (defn[2][PromptFailure] == False):
                    superseded = True
                    severity = INFORMATION
                else:
                    superseded = False
                    severity = ERROR
                reason = v.reason
                message = "Verification " + stepNum + " failed. "
                message += reason + "."
                REGISTRY['CIF'].write( message , {Severity:severity} )
        
        if stopped:
            REGISTRY['CIF'].write( "Verification interrupted" )
        elif someWrong and superseded:
            REGISTRY['CIF'].write( "Verification conditions superseded" )
        elif not someWrong:
            REGISTRY['CIF'].write( "Verifications succeeded" )
        return overallResult,errors

    #==========================================================================
    def _onInterfaceCommand(self, commandId):
        if (commandId in ["CMD_INTERRUPT","CMD_ABORT"]):
            for verifier in self.__verifiers:
                verifier.stopVerification()

    #===========================================================================
    def _databaseLookup(self, name, resource_type, source, config ):
        REGISTRY['CIF'].write("TMTC database service not implemented on this driver", {Severity:WARNING})
        return None
