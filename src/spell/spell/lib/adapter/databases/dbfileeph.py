###################################################################################
## MODULE     : spell.lib.adapter.databases.dbfileeph_a2100
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Database based on ephemeris files for A2100
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

from dbfile import *
import re

################################################################################
class DatabaseFileEphA2100(DatabaseFile):
    
    #===========================================================================
    def __init__(self, name, path, defaultExt = None):
        super(DatabaseFileEphA2100, self).__init__(name,path,defaultExt)
    
    #===========================================================================
    def _readData(self):
        # Load the file contents
        lines = file(self._filename).readlines()
        self._vkeys = []
        self._types = {}
        self._properties = {}
        # Will hold the line to be imported
        for line in lines:

            # Ignore blank lines
            if (line is None) or (len(line)==0): continue
            
            key = None
            value = None
            
            # Process comments
            if line.startswith("*"):
                # Ignore comment lines
                if line.strip()=="*": continue
                
                # Header lines
                elements = line.split(":")
                if len(elements) == 2:
                    key = elements[0].strip()[1:]
                    value = elements[1].strip()
                else:
                    key = elements[0].strip()
                    value = None
            elif line.startswith("$ID"):
                key = 'ID'
                value = ""
                for elem in elements[1:]:
                    if len(value)!=0: value = value + " "
                    value = value + elem 
            elif line.startswith("$END"):
                continue
            else:
                elements = line.split()
                if len(elements)==3:
                    key = elements[0].strip()
                    value = elements[2].strip()
                else:
                    LOG("ERROR: Unparseable EPH line: " + repr(line))
                    continue
                    
            # Amendments
            if key == 'S/C ID': key = 'SCID'
                    
            if value:
                value,vtype = ImportValue(value)
                    
            # Warn about duplicated data
            if self._properties.has_key(key):
                LOG("WARNING: duplicated database key: " + repr(key))
            else:
                self[key] = value
                if vtype:
                    self._types[key] = vtype
        # End for

    #===========================================================================
    def _writeData(self, theFile):
        pass


################################################################################
class DatabaseFileEphSB4000(DatabaseFile):
    
    #===========================================================================
    def __init__(self, name, path, defaultExt = None):
        super(DatabaseFileEphSB4000, self).__init__(name,path,defaultExt)
    
    #===========================================================================
    def _readData(self):
        # Load the file contents
        lines = file(self._filename).readlines()
        self._vkeys = []
        self._types = {}
        self._properties = {}
        
        keyCount = 1
        # Will hold the line to be imported
        for line in lines:

            # Ignore blank lines
            if (line is None) or (len(line)==0): continue
            
            elements = line.split()

            
            if line.startswith("!") and len(elements)==3:
                key = elements[1]
                value = elements[2]
            elif len(elements)==14:
                key = keyCount
                keyCount = keyCount + 1
                value = {}
                value['Epoch'] = elements[0] + " " + elements[1]
                value['Drift_Rate'] = float(elements[2])
                value['Eccentricity'] = float(elements[3])
                value['Inclination'] = float(elements[4])
                value['RA_Node'] = float(elements[5])
                value['Argument_of_Perigee'] = float(elements[6])
                value['Mean_Anomaly'] = float(elements[7])
                value['Mean_Longitude'] = float(elements[8])
                value['Spacecraft_Coefficient'] = float(elements[9])
                value['Longitude_Acceleration'] = float(elements[10])
                value['Maneuver_In_Progress'] = elements[11] == 'x'
                value['Continuous_Thrust_In_Progress'] = elements[12] == 'x'
                value['Seconds_Since_1_JAN_2000_00_00_00'] = int(elements[13])

                self[key] = value
                
            if keyCount > 100: return
        # End for

    #===========================================================================
    def _writeData(self, theFile):
        pass


################################################################################
class DatabaseFileEphOscS2(DatabaseFile):
    
    #===========================================================================
    def __init__(self, name, path, defaultExt = None):
        super(DatabaseFileEphOscS2, self).__init__(name,path,defaultExt)
    
    #===========================================================================
    def _readData(self):
        # Load the file contents
        lines = file(self._filename).readlines()
        self._vkeys = []
        self._types = {}
        self._properties = {}
        
        lineCount = 0
        # Will hold the line to be imported
        for line in lines:

            # Ignore blank lines
            if (line is None) or (len(line)==0): continue
            
            key = None
            
            elements = line.split()
            
            if len(elements)==8:
                lineCount = lineCount +1
                if lineCount>100: return
                key = elements[0].strip() + " " + elements[1].strip()
                dataDict = {}
                dataDict['Ephemeris_Date'] = elements[0].strip()
                dataDict['Ephemeris_Time'] = elements[1].strip()
                dataDict['ECI_Position_X'] = float(elements[2].strip())
                dataDict['ECI_Position_Y'] = float(elements[3].strip())
                dataDict['ECI_Position_Z'] = float(elements[4].strip())
                dataDict['ECI_Velocity_X'] = float(elements[5].strip())
                dataDict['ECI_Velocity_Y'] = float(elements[6].strip())
                dataDict['ECI_Velocity_Z'] = float(elements[7].strip())
                
                # Warn about duplicated data
                if self._properties.has_key(key):
                    LOG("WARNING: duplicated database key: " + repr(key))
                else:
                    self[key] = dataDict
        # End for

    #===========================================================================
    def _writeData(self, theFile):
        pass
