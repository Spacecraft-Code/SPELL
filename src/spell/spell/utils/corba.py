###################################################################################
## MODULE     : spell.utils.corba
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: CORBA helper class
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

#*******************************************************************************
# SPELL Imports
#*******************************************************************************

#*******************************************************************************
# Local Imports
#*******************************************************************************
from log import *

#*******************************************************************************
# System Imports
#*******************************************************************************
from omniORB import CORBA,PortableServer
import CosNaming,sys,threading

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

###############################################################################
class CorbaContainer( threading.Thread ):

    """
    Thread class used for executing CORBA loop in background.
    A CORBA helper providing a start() method is expected in constructor. 
    """
    
    #==========================================================================
    def __init__(self, helper ):
        # Initialize thread
        threading.Thread.__init__(self)
        self.__helper = helper
        
    #==========================================================================
    def run(self):
        # Start the CORBA loop
        self.__helper.start()

###############################################################################
class CorbaException(BaseException):
    """
    Raised by the CORBA helper when any CORBA-related operation fails.
    """
    pass

###############################################################################
class CorbaHelperClass(object):
    
    """
    Generic CORBA wrapper. 
    Inherits from LoggerClass in order to use basic logging features.
    """
    _ORB = None
    _POA = None
    _ready = False
    _testing = False
    _started = False
    _activated = False
    _nsID = []
    _nameServers = {}
    _namePorts = {}
    _nameComponents = {}
    _nameContexts = {}
         
    #==========================================================================
    def __init__(self):
        LOG("Created")
         
    #==========================================================================
    def __checkORB(self): 
        if not self._ready:
            raise CorbaException("ORB not initialised")
        
    #==========================================================================
    def __checkNS(self, nsID):
        if self._nameContexts.get(nsID) is None:
            raise CorbaException("Name context " + nsID + " not available")

    #==========================================================================
    def isReady(self):
        return self._ready

    #==========================================================================
    def isRunning(self):
        return self._started

    #==========================================================================
    def isActivated(self):
        return self._activated

    #==========================================================================
    def initialise(self):
        """
        DESCRIPTION:
            Initialise the ORB
            
        ARGUMENTS:
            Nothing
            
        RETURNS:
            Nothing
            
        RAISES:
            CorbaException on failure
        """
        if self._started: self.stop()
        if self._ready: return
        
        LOG("Initialising ORB")
        
        # ORB initialization arguments
        arguments = sys.argv
        
        # Initialize the ORB
        try:
            self._ORB = CORBA.ORB_init(arguments, CORBA.ORB_ID)
        except Exception,e:
            raise CorbaException("Could not initialize ORB: ", e)
            
        if self._ORB is None:
            raise CorbaException("Could not get ORB instance: ", e)
        
        # Obtain the root POA
        try:
            LOG("Obtaining POA")
            self._POA = self._ORB.resolve_initial_references("RootPOA")
        except Exception,e:
            raise CorbaException("Could not get POA reference: ", e)
            
        if self._POA is None:
            raise CorbaException("Could not get POA reference")
        
        self._ready = True
        LOG("ORB initialised")
    
    #==========================================================================
    def addNameService(self, nsID, nameserver, port):
        """
        DESCRIPTION:
            Add a name service to the CORBA layer.
            
        ARGUMENTS:
            nsID        The name service identifier
            nameserver  The name service host
            port        The name service port  
            
        RETURNS:
            Nothing
            
        RAISES:
            CorbaException on failure
        """
        
        if not self._ready:
            raise CorbaException("ORB not initialized")
        
        self._nsID.append(nsID)
        self._nameServers[nsID] = nameserver
        self._namePorts[nsID] = port
        
        # Obtain the name service
        try:
            LOG("Resolving name service " + nsID + " at " + nameserver + ":" + port)
            cloc = "corbaloc:iiop:" + nameserver + ":" + port + "/NameService"
            obj = self._ORB.string_to_object(cloc)
            self._nameContexts[nsID] = obj._narrow( CosNaming.NamingContext )
        except Exception, e:
            raise CorbaException("Could not resolve name service " + nsID + ": ", e)

    #==========================================================================
    def delNameService(self, nsID):
        """
        DESCRIPTION:
            Remove a name service from the CORBA layer.
            
        ARGUMENTS:
            nsID        The name service identifier
            
        RETURNS:
            Nothing
            
        RAISES:
            CorbaException on failure
        """
        
        if not self._ready:
            raise CorbaException("ORB not initialized")
        
        if not nsID in self._nsID:
            raise CorbaException("No such name service: " + nsID)
        
        self._nsID.remove(nsID)
        if self._nameComponents.has_key(nsID):
            self._nameComponents.pop(nsID)
            
        self._nameServers.pop(nsID)
        self._namePorts.pop(nsID)
        self._nameContexts.pop(nsID)

    #==========================================================================
    def hasNameService(self, nsID):
        """
        DESCRIPTION:
            Check if the given name service is available.
            
        ARGUMENTS:
            nsID        The name service identifier
            
        RETURNS:
            True if the name service is available
            
        RAISES:
            CorbaException on failure
        """
        
        if not self._ready:
            raise CorbaException("ORB not initialized")
        
        if not nsID in self._nsID:
            return False
        
        return True
            
    #==========================================================================
    def setContext(self, nsID, domain, subdomain ):
        
        """
        DESCRIPTION:
            Set the context to be used in the given name service. The context 
            is composed of two levels: domain and subdomain. An object may be 
            designated using a name component like DOMAIN/SUBDOMAIN/OBJECTNAME.
        
        ARGUMENTS:
            nsID        The name service identifier
            domain      The context domain name
            subdomain   The context subdomain name
            
        RETURNS:
            Nothing
            
        RAISES:
            CorbaException on failure
        """
        if type(domain)!=str or type(subdomain)!=str:
            raise CorbaException("Incorrect arguments for name context")

        self._nameComponents[nsID] = [ CosNaming.NameComponent(domain,"") ]
        self._nameComponents.get(nsID).append( 
                    CosNaming.NameComponent(subdomain,"") ) 

        LOG("Set " + nsID + " name context: " + domain + "/" + subdomain)
    
    #==========================================================================
    def bindObject(self, nsID, obj, name):
        
        """
        DESCRIPTION:
            Bind the given CORBA object with the given name into the given
            name service. 
        
        ARGUMENTS:
            nsID        The name service identifier
            obj         A valid CORBA object
            name        Name for the object
            
        RETURNS:
            Nothing
            
        RAISES:
            CorbaException on failure
        """
        self.__checkORB()
        self.__checkNS(nsID)
        
        # Append the object name to the root name component if any. 
        # A context like DOMAIN/SUBDOMAIN/NAME is built this way.
        # If there is no context defined for this name service,
        # a simple name component is built.
        if self._nameComponents.has_key(nsID):
            namec = self._nameComponents.get(nsID)[:]
            namec.append( CosNaming.NameComponent(name,"") )
        else:
            namec = [ CosNaming.NameComponent(name,"") ]
            
        LOG("Binding: " + repr(namec))
        try:
            inst = obj._this()
            LOG("Object IOR: '" + self._ORB.object_to_string(inst) + "'")
            self._nameContexts.get(nsID).rebind(namec,inst)
        except Exception,e:
            raise CorbaException("Could not bind object: " +  repr(e))

        LOG("Object " + name + " bound on NS " + nsID)

    #==========================================================================
    def unbindObject(self, nsID, name):
        """
        DESCRIPTION:
            Unbind an object from the given name service
        
        ARGUMENTS:
            nsID        The name service identifier
            name        The object name
        
        RETURNS:
            Nothing
        
        RAISES:
            CorbaException on failure
        """

        self.__checkORB()
        self.__checkNS(nsID)

        # Build the name context: copy the configured context if any,
        # and append the name component at the end.
        if self._nameComponents.has_key(nsID):
            namec = self._nameComponents.get(nsID)[:]
            namec.append( CosNaming.NameComponent(name,"") )
        else:
            namec = [ CosNaming.NameComponent(name,"") ]

        # Unbind the object
        try:
            self._nameContexts.get(nsID).unbind(namec)
        except Exception,e:
            raise CorbaException("Could not unbind object: ", e)

        LOG("Object " + name + " unbound from NS " + nsID)
        
    #==========================================================================
    def objectFromIOR(self, ior):
        """
        DESCRIPTION:
            Obtain an object instance from a given IOR string
        
        ARGUMENTS:
            ior        The interoperable object reference
        
        RETURNS:
            The object instance
        
        RAISES:
            Nothing
        """
        return self._ORB.string_to_object(ior)

    #==========================================================================
    def objectToIOR(self, obj):
        """
        DESCRIPTION:
            Obtain an IOR string from a given object instance
        
        ARGUMENTS:
            obj        The object instance
        
        RETURNS:
            The IOR string
        
        RAISES:
            Nothing
        """
        return self._ORB.object_to_string(obj)

    #==========================================================================
    def getObject(self, nsID, name, narrowClass ):
        """
        DESCRIPTION:
            Obtain an object reference from the given name service using the
            given object name. Once obtained, cast the CORBA object to the proper
            type by using the helper class 'narrowClass'.
        
        ARGUMENTS:
            nsID            The name service identifier
            name            The object name
            narrowClass     The class for narrowing the object instance
            
        RETURNS:
            The object instance
            
        RAISES:
            CorbaException on failure
        """
        
        # Build the name component if needed
        if self._nameComponents.has_key(nsID):
            namec = self._nameComponents.get(nsID)[:]
            namec.append( CosNaming.NameComponent( name, "") )
        else:
            namec = [ CosNaming.NameComponent(name,"") ]
        
        try:
            obj = self._nameContexts.get(nsID).resolve(namec)
            nobj = obj._narrow( narrowClass )
        except CosNaming.NamingContext.NotFound, e:
            raise CorbaException("Object not found: " + name)
        return nobj

    #==========================================================================
    def getObjectType(self, nsID, name, type, narrowClass ):
        """
        DESCRIPTION:
            See method 'getObject'. This method adds an additional argument
            specifying the object "kind" or type for the name component.
        
        ARGUMENTS:
            nsID            The name service identifier
            name            The object name
            type            The object typestring
            narrowClass     The class for narrowing the object instance
            
        RETURNS:
            The object instance
            
        RAISES:
            CorbaException on failure
        """
        
        # Build the name component list if needed
        if self._nameComponents.has_key(nsID):
            namec = self._nameComponents.get(nsID)[:]
            namec.append( CosNaming.NameComponent( name, type) )
        else:
            namec = [ CosNaming.NameComponent(name,type) ]

        # Resolve the object
        try:
            obj = self._nameContexts.get(nsID).resolve(namec)
            nobj = obj._narrow( narrowClass )
        except CosNaming.NamingContext.NotFound, e:
            raise CorbaException("Object not found: " + name + "." + type )
        return nobj

    #==========================================================================
    def getObjectByContextType(self, nsID, domain, subdomain, name, type, narrowClass ):
        
        """
        DESCRIPTION:
            See 'getObject' method. This method allows overriding a previously
            configured name context for a particular case. It may be required to
            obtain an object which is bound withing a domain and/or subdomain 
            different from the ones configured with 'setContext' method.

        ARGUMENTS:
            nsID            The name service identifier
            domain          The object domain name
            subdomain       The object subdomain name
            name            The object name
            type            The object typestring
            narrowClass     The class for narrowing the object instance
        
        RETURNS:
            The object instance
        
        RAISES:
            CorbaException on failure
        """
        
        # Build the namecomponent list always
        namec = [ CosNaming.NameComponent( domain, "" ) ]
        namec.append( CosNaming.NameComponent( subdomain, "" ) )
        namec.append( CosNaming.NameComponent( name, type ) )
        try:
            obj = self._nameContexts.get(nsID).resolve(namec)
            nobj = obj._narrow( narrowClass )
        except CosNaming.NamingContext.NotFound, e:
            raise CorbaException("Object not found: " + name + "." + type )
        return nobj

    #==========================================================================
    def releaseObject(self, object ):
        """
        DESCRIPTION:
            Release the given CORBA object.
            
        ARGUMENTS:
            object        The object to be released.
            
        RETURNS:
            Nothing
            
        RAISES:
            Nothing
            
        TODO: handle failures when releasing an object in the incorrect order
        (BadOrder exception)
        """
        if self._started:
            try:
                object._release()
            except:
                #TODO
                pass

    #==========================================================================
    def activate(self):
        """
        DESCRIPTION:
            Activate the POA manager in order to enable invocation marshallings
            
        ARGUMENTS:
            Nothing
            
        RETURNS:
            Nothing
            
        RAISES:
            Nothing
        """
        self.__checkORB()
        poaManager = self._POA._get_the_POAManager()
        poaManager.activate()
        self._activated = True
        LOG("POA activated")

    #==========================================================================
    def start(self):
        """
        DESCRIPTION:
            Start the ORB (blocking call)
            
        ARGUMENTS:
            Nothing
            
        RETURNS:
            Nothing
            
        RAISES:
            Nothing
        """
        LOG("ORB started")
        self._started = True
        self._ORB.run()
        
    #==========================================================================
    def startBackground(self):
        """
        DESCRIPTION:
            Start the ORB on background
            
        ARGUMENTS:
            Nothing
            
        RETURNS:
            Nothing
            
        RAISES:
            Nothing
        """
        # Create the container thread
        self._ORBThread = CorbaContainer(self)
        # Start the thread
        self._ORBThread.start()
    
    #==========================================================================
    def stop(self):
        """
        DESCRIPTION:
            Stop the ORB (tipically used when ORB is running on background)
            
        ARGUMENTS:
            Nothing
            
        RETURNS:
            Nothing
            
        RAISES:
            Nothing
        """
        LOG("ORB stopped")
        self._started = False
        self._ORB.shutdown(1)
        
    #==========================================================================
    def getPOA(self):
        """
        DESCRIPTION:
            Obtain the object adapter
            
        ARGUMENTS:
            Nothing
            
        RETURNS:
            The portable object adapter
            
        RAISES:
            Nothing
        """
        return self._POA
