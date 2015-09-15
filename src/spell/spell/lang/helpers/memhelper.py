###################################################################################
## MODULE     : spell.lang.helpers.memhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for memory management
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
class GenerateMemoryReport_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    __type = None
    __image = None
    __begin = None
    __end = None
    __source = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "MEM")
        self._opName = ""
        self.__type = None
        self.__image = None
        self.__begin = None
        self.__end = None
        self.__source = None

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        
        if len(args) != 0 and len(args)!=1:
            raise SyntaxException("This function does not support so many positional arguments")
        
        # Syntax: GenerateMemoryReport( Type=, Image=, Begin=, End=, Source=, others
        # Syntax: GenerateMemoryReport( 'IMAGE', Type=, Begin=, End=, Source=, others
        if len(args)==1:
            self.__image = args[0]
        else:
            if not self.hasConfig(Image):
                raise SyntaxException("Expected an 'Image' modifier or a positional argument indicating the memory image name")
            self.__image = self.getConfig(Image)
            
        if type(self.__image) != str:
            raise SyntaxException("Expected a string for the image name")
        
        if not self.hasConfig(Type):
            raise SyntaxException("Expected a 'Type' modifier indicating the type of report")
        self.__type = self.getConfig(Type)
        
        if not self.hasConfig(Source):
            raise SyntaxException("Expected a 'Source' modifier indicating the data source")
        self.__source = self.getConfig(Source)
        
        if not self.hasConfig(Begin):
            raise SyntaxException("Expected a 'Begin' modifier indicating the memory range start")
        self.__begin = self.getConfig(Begin)

        if not self.hasConfig(End):
            raise SyntaxException("Expected an 'End' modifier indicating the memory range end")
        self.__end = self.getConfig(End)

        if self.hasConfig(ValueFormat):
            format = self.getConfig(ValueFormat)
            if not format in [RAW,ENG]:
                raise SyntaxException("Invalid format specified: '" + str(format) + "'")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip generation of memory report and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip generation of memory report and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to generate the memory report again")

        # Store information for possible failures
        self.setFailureInfo("MEM", "Generate")

        result = REGISTRY['MEM'].generateReport( self.__image, self.__type, self.__begin, self.__end, self.__source, config = self.getConfig() )

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry generation of memory report", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip generation of memory report", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel generation of memory report", {Severity:WARNING} )
        return [False, False]

################################################################################
class CompareMemoryImages_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    __image1 = None
    __image2 = None
    __type = None
    __begin = None
    __end = None
    __source = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "MEM")
        self._opName = ""
        self.__image1 = None
        self.__image2 = None
        self.__type = None
        self.__begin = None
        self.__end = None
        self.__source = None

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        
        if len(args) != 0 and len(args)!=1:
            raise SyntaxException("This function does not support so many positional arguments")

        # Syntax: CompareMemoryImages( Type=, Image=, Begin=, End=, Source=, others
        # Syntax: CompareMemoryImages( ['Image1', 'Image2'], Type=, Begin=, End=, Source=... )
        
        if len(args)==1:
            if type(args[0]) != list or len(args[0])!=2:
                raise SyntaxException("Expected a list with two image names as unique positional argument")
            self.__image1 = args[0][0]
            self.__image2 = args[0][1]
        else:
            if not self.hasConfig(Image):
                raise SyntaxException("Expected an 'Image' modifier or a positional argument indicating the memory image names")
            image = self.getConfig(Image)
            if type(image) != list or len(image)!=2:
                raise SyntaxException("Expected a list with two image names as the value for the Image modifier")
            self.__image1 = image[0]
            self.__image2 = image[1]
            
        if type(self.__image1) != str or type(self.__image2)!= str:
            raise SyntaxException("Expected strings for the image names")
        
        if not self.hasConfig(Type):
            raise SyntaxException("Expected a 'Type' modifier indicating the type of reports")
        self.__type = self.getConfig(Type)
        
        if not self.hasConfig(Source):
            raise SyntaxException("Expected a 'Source' modifier indicating the data source")
        self.__source = self.getConfig(Source)
        
        if not self.hasConfig(Begin):
            raise SyntaxException("Expected a 'Begin' modifier indicating the memory range start")
        self.__begin = self.getConfig(Begin)

        if not self.hasConfig(End):
            raise SyntaxException("Expected an 'End' modifier indicating the memory range end")
        self.__end = self.getConfig(End)

        if self.hasConfig(ValueFormat):
            format = self.getConfig(ValueFormat)
            if not format in [RAW,ENG]:
                raise SyntaxException("Invalid format specified: '" + str(format) + "'")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip memory image comparison and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip memory image comparison and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to compare memory images again")

        # Store information for possible failures
        self.setFailureInfo("MEM", "Compare")

        result = REGISTRY['MEM'].compareImages( self.__image1, self.__image2, self.__type, self.__begin, self.__end, self.__source, config = self.getConfig() )

        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry memory image comparison", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip memory image comparison", {Severity:WARNING} )
        return [False, True]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel memory image comparison", {Severity:WARNING} )
        return [False, False]

################################################################################
class MemoryLookup_Helper(WrapperHelper):

    """
    DESCRIPTION:
    """    
    __name= None
    __image = None
    __address = None
    __type = None
    __source = None

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "MEM")
        self._opName = ""

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args) != 0 and len(args)!=1:
            raise SyntaxException("This function does not support so many positional arguments")

        # Syntax: MemoryLookup( Name=, Source=, Type= others )
        # Syntax: MemoryLookup( 'NAME', Image=, Source=, Type= others )

        if len(args)==1:
            self.__name = args[0]
        else:
            if not self.hasConfig(Name):
		if not self.hasConfig(Address):
                  raise SyntaxException("Expected a 'Name/Address' modifier indicating the memory resource name/address")
		else:
                   self.__address = self.getConfig(Address)
                   if type(self.__address) != int:
                      raise SyntaxException("Expected an integer for the address")
	    else:
              self.__name = self.getConfig(Name)
              if type(self.__name) != str:
                 raise SyntaxException("Expected a string for the resource name")
        
        if not self.hasConfig(Type):
            raise SyntaxException("Expected a 'Type' modifier indicating the type of reports")
        self.__type = self.getConfig(Type)

        if self.__type == 'ADA' and self.__name == None:
           raise SyntaxException("Please provide ADA variable name")
	elif (self.__type == 'Logical_Address' or self.__type == 'Physical_Address') and self.__address == None:
           raise SyntaxException("Please provide hex address")

        if not self.hasConfig(Image):
            raise SyntaxException("Expected a 'Image' modifier indicating the image reference")
        self.__image = self.getConfig(Image)
        
        if not self.hasConfig(Source):
            raise SyntaxException("Expected a 'Source' modifier indicating the data source")
        self.__source = self.getConfig(Source)
        
        if self.hasConfig(ValueFormat):
            format = self.getConfig(ValueFormat)
            if not format in [RAW,ENG]:
                raise SyntaxException("Invalid format specified: '" + str(format) + "'")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._setActionString( ACTION_SKIP   ,  "Skip memory lookup and return None")
        self._setActionString( ACTION_REPEAT ,  "Perform memory lookup again")

        # Store information for possible failures
        self.setFailureInfo("MEM", "Lookup")

        result = REGISTRY['MEM'].memoryLookup( self.__name, self.__address, self.__image, self.__type, self.__source, config = self.getConfig() )
                
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry memory lookup", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip memory lookup", {Severity:WARNING} )
        return [False, True]
