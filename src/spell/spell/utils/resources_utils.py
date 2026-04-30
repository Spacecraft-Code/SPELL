###################################################################################
## MODULE     : spell.utils.resources_utils
## DATE       : April 16, 2021
## PROJECT    : SPELL
## DESCRIPTION: Utilities for TC resources (Options)
## --------------------------------------------------------------------------------
##
##  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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
__all__ = ['getResources', 'setResource', 'getResourceAcceptedValues', "getResourceNameMapUtil"]

# FIXME: implement getresources ... called in SPELL Monitor directly from here, also call getResourceAcceptedValues

cal_to_raw_result = None
raw_to_cal_result = None
maps_initialized = False

def getResourcesUtil(IsLocal, debug=False):
    # Initialize maps if not yet done
    if not maps_initialized:
        _computeResourceCalibratedRawMap(debug=debug)

    # get raw values resources
    #Display( "Starting retrieving resources", WARNING)
    raw_resources_map = GetResources(IsLocal=IsLocal)
    #Display( "Finished retrieving resources", WARNING)

    # Convert to Engineering values
    if raw_to_cal_result is None:
        # no Eng values
        cal_resources_map = raw_resources_map
    else:
        cal_resources_map = {}
        for raw_name, raw_value in raw_resources_map.items():
            cal_name = raw_name
            cal_value = raw_value
            try:
                cal_name, val_map = raw_to_cal_result[raw_name]
                try:
                    cal_value = val_map[raw_value]
                except KeyError:
                    pass
            except KeyError:
                pass
            cal_resources_map[cal_name] = cal_value

    return cal_resources_map

def setResourceUtil(resource, value, IsLocal, debug=False):
    # Initialize maps if not yet done
    if not maps_initialized:
        _computeResourceCalibratedRawMap(debug=debug)

    # convert Engineering value to raw value
    raw_name = resource
    raw_value = value
    if cal_to_raw_result is not None:
        # There are values
        try:
            raw_name, val_map = cal_to_raw_result[resource]
            try:
                raw_value = val_map[value]
            except KeyError:
                pass
        except KeyError:
            pass

    # Set the resource
    Display(
        "Starting updating {} resource '{}' to '{}' from {}".format(
            ("(local)" if IsLocal else "(global)"), 
            resource, 
            value, 
            "SPELL Resource GUI"
        ), 
        WARNING
    )
    ret_val = SetResource(raw_name, raw_value, IsLocal=IsLocal)
    Display(
        "Finished updating {} resource '{}' to '{}' from {}".format(
            ("(local)" if IsLocal else "(global)"), 
            resource, 
            value, 
            "SPELL Resource GUI"
        ), 
        WARNING
    )

    return ret_val

def getResourceAcceptedValuesUtil(debug=False):
    # Initialize maps if not yet done
    if not maps_initialized:
        _computeResourceCalibratedRawMap(debug=debug)

    cal_accepted_values_map = {}
    if cal_to_raw_result is not None:
        for cal_name, raw_name_val_map in cal_to_raw_result.items():
            cal_accepted_values_map[cal_name] = sorted(list(raw_name_val_map[1].items()))

    return cal_accepted_values_map

def getResourceNameMapUtil(debug=False):
    # Initialize maps if not yet done
    if not maps_initialized:
        _computeResourceCalibratedRawMap(debug=debug)

    cal_raw_name_map = {}
    if cal_to_raw_result is not None:
        for cal_name, raw_name_val_map in cal_to_raw_result.items():
            cal_raw_name_map[cal_name] = raw_name_val_map[0]

    return cal_raw_name_map


def _computeResourceCalibratedRawMap(debug=False):
    global cal_to_raw_result 
    global raw_to_cal_result 
    global maps_initialized

    maps_initialized = True

    default_keys = []
    calibration_keys = []
    name_key_calibration_key_map = {}
    try:
        gdb_keys = list(GDB.keys())
    except Exception as e:
        if debug:
            Display("# [error] failed to get GDB keys. raised exception: "+str(e))
        return {}
    for key in gdb_keys:
        if key.startswith("Default_"):
            default_keys.append(key)
        elif key.endswith("_CALIBRATION"):
            calibration_keys.append(key)
        else:
            name_key_calibration_key_map[key] = None

    if len(default_keys) != len(calibration_keys):
        if debug:
            Display("## [Warning] not all resources have default value.")
        if len(default_keys) > len(calibration_keys):
            if debug:
                Display("# [error] Problem Getting Calibration (More default values than calibrations)")
            return

    for calibration_key in calibration_keys:
        name = calibration_key[:-len("_CALIBRATION")]
        if name not in name_key_calibration_key_map:
            if debug:
                Display("# [error] Calibration has no name key in GDB ("+name+")")
            return
        name_key_calibration_key_map[name] = calibration_key
    
    # Remove name keys with None as calibration Key (They are not returned by GetResources)
    to_del = [key for key, val in name_key_calibration_key_map.items() if val is None]
    for key in to_del:
        del name_key_calibration_key_map[key]

    local_cal_to_raw_result = {}
    local_raw_to_cal_result = {}

    try:
        for cal_name, caldat_key in name_key_calibration_key_map.items():
            raw_name = GDB.get(cal_name)
            raw_cal_val = GDB.get(caldat_key)
            cal_raw_val = {}
            for raw, cal in raw_cal_val.items():
                if type(cal) != str:
                    if type(cal) not in (int, float):
                        # TODO: FIXME: handle as in userlib GetGNDOptions
                        if debug:
                            Display("# [error] Dict as Engineering value of resource (TODO: handle this) -- " + str(raw_cal_val))
                        return
                    else:
                        # TODO: Further calibration over here, if needed
                        pass
                cal_raw_val[cal] = raw
            # Make sure that cal is of string type
            cal_raw_val = {str(cal): raw for cal, raw in cal_raw_val.items()}
            raw_cal_val = {raw: str(cal) for raw, cal in raw_cal_val.items()}

            # Some checks
            if raw_name in local_raw_to_cal_result:
                if debug:
                    Display ("## [warning] Multiple Engineering names for raw resource: " + raw_name)

            # Put into maps
            local_cal_to_raw_result[cal_name] = (raw_name, cal_raw_val)
            local_raw_to_cal_result[raw_name] = (cal_name, raw_cal_val)
    except Exception as e:
        if debug:
            Display("# [error] Exception raised during map creation: " + str(e))
        return

    cal_to_raw_result = local_cal_to_raw_result
    raw_to_cal_result = local_raw_to_cal_result
