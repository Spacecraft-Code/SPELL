// ################################################################################
// FILE       : SPELLdataHelper.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Data handling utilities
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLdataHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// FUNCTION: Convert server file
//=============================================================================
SPELLserverFile SPELLdataHelper::serverFileFromString( const std::string& str )
{
	if (str == MessageValue::DATA_FILE_ASRUN)
	{
		return FILE_ASRUN;
	}
	else if (str == MessageValue::DATA_FILE_EXEC_LOG )
	{
		return FILE_LOG;
	}
	return FILE_UNKNOWN;
}

//=============================================================================
// FUNCTION:	sourceToString
//=============================================================================
std::string SPELLdataHelper::sourceToString( std::vector<std::string> lines )
{
	std::string add = "";
	std::string dataStr = "";
	std::vector<std::string>::iterator it;
	for( it = lines.begin(); it != lines.end(); it++)
	{
		if ((*it).size()==0) { add = " "; } else { add = ""; };
		if (dataStr != "") dataStr += IPCinternals::CODE_SEPARATOR;
		dataStr += (*it) + add;
	}
	return dataStr;
}

//=============================================================================
// FUNCTION: linesToString
//=============================================================================
std::string SPELLdataHelper::linesToString( std::vector<std::string> lines )
{
	std::string add = "";
	std::string dataStr = "";
	std::vector<std::string>::iterator it;
	for( it = lines.begin(); it != lines.end(); it++)
	{
		if ((*it).size()==0) { add = " "; } else { add = ""; };
		if (dataStr != "") dataStr += "\n";
		dataStr += (*it) + add;
	}
	return dataStr;
}

//============================================================================
// FUNCTION        :
//============================================================================
std::string SPELLdataHelper::executorStatusToString( SPELLexecutorStatus st )
{
    return MessageValue::StatusStr[st];
}

//============================================================================
// FUNCTION        :
//============================================================================
SPELLexecutorStatus SPELLdataHelper::stringToExecutorStatus( std::string st )
{
    int idx = 0;
    for( idx = 0; idx<MessageValue::NumStatus; idx++)
    {
        if (st == MessageValue::StatusStr[idx]) return (SPELLexecutorStatus) idx;
    }
    return STATUS_UNKNOWN;
}

//============================================================================
// FUNCTION        :
//============================================================================
std::string SPELLdataHelper::sharedDataOperationToString( const SPELLsharedDataOperation& operation )
{
	switch(operation)
	{
	case SET_SHARED_DATA: return MessageValue::DATA_SET_SHARED_DATA;
	case DEL_SHARED_DATA: return MessageValue::DATA_DEL_SHARED_DATA;
	case CLEAR_SHARED_SCOPE: return MessageValue::DATA_CLEAR_SHARED_SCOPE;
	case ADD_SHARED_SCOPE: return MessageValue::DATA_ADD_SHARED_SCOPE;
	case DEL_SHARED_SCOPE: return MessageValue::DATA_DEL_SHARED_SCOPE;
	default:
		LOG_ERROR("Unknown context operation");
		return "";
	}
}

//============================================================================
// FUNCTION        :
//============================================================================
std::string SPELLdataHelper::clientOperationToString( const SPELLclientOperation& operation )
{
	switch(operation)
	{
	case CLIENT_OP_LOGIN:	return MessageValue::DATA_CLOP_LOGIN;
	case CLIENT_OP_LOGOUT:  return MessageValue::DATA_CLOP_LOGOUT;
	case CLIENT_OP_CRASH:  return MessageValue::DATA_CLOP_LOGOUT; // To be reviewed
	default:
		LOG_ERROR("Unknown client operation");
		return "";
	}
}

//============================================================================
// FUNCTION        :
//============================================================================
std::string SPELLdataHelper::clientModeToString( const SPELLclientMode& mode )
{
	switch(mode)
	{
	case CLIENT_MODE_CONTROL: return MessageValue::DATA_GUI_MODE_C;
	case CLIENT_MODE_MONITOR: return MessageValue::DATA_GUI_MODE_M;
	case CLIENT_MODE_BACKGROUND: return MessageValue::DATA_GUI_MODE_B;
	case CLIENT_MODE_UNKNOWN: return "UNKNOWN";
	default:
		LOG_ERROR("Unknown client mode");
		return "";
	}
}

//============================================================================
// FUNCTION        :
//============================================================================
SPELLclientMode SPELLdataHelper::clientModeFromString( const std::string& mode  )
{
	if (mode == MessageValue::DATA_GUI_MODE_C) return CLIENT_MODE_CONTROL;
	if (mode == MessageValue::DATA_GUI_MODE_M) return CLIENT_MODE_MONITOR;
	if (mode == MessageValue::DATA_GUI_MODE_B) return CLIENT_MODE_BACKGROUND;
	return CLIENT_MODE_UNKNOWN;
}

//============================================================================
// FUNCTION        :
//============================================================================
std::string SPELLdataHelper::openModeToString( const SPELLopenMode& mode )
{
	std::string openMode = "{";
	if ((mode & OPEN_MODE_AUTOMATIC)>0)
	{
		openMode += LanguageModifiers::Automatic + ":" + PythonConstants::True;
	}
	else
	{
		openMode += LanguageModifiers::Automatic + ":" + PythonConstants::False;
	}
	if ((mode & OPEN_MODE_VISIBLE)>0)
	{
		openMode += "," + LanguageModifiers::Visible + ":" + PythonConstants::True;
	}
	else
	{
		openMode += "," + LanguageModifiers::Visible + ":" + PythonConstants::False;
	}
	if ((mode & OPEN_MODE_BLOCKING)>0)
	{
		openMode += "," + LanguageModifiers::Blocking + ":" + PythonConstants::True;
	}
	else
	{
		openMode += "," + LanguageModifiers::Blocking + ":" + PythonConstants::False;
	}
	openMode += "}";
	return openMode;
}

//============================================================================
// FUNCTION        :
//============================================================================
SPELLopenMode SPELLdataHelper::openModeFromString( const std::string& mode )
{
	SPELLopenMode openMode = OPEN_MODE_UNKNOWN;
	if (mode != "")
	{
		std::string cmode = mode;
		cmode = cmode.substr(1, cmode.size()-2);
		std::vector<std::string> pairs = SPELLutils::tokenize( cmode, ",");
		std::vector<std::string>::iterator it;
		for( it = pairs.begin(); it != pairs.end(); it++)
		{
			std::vector<std::string> kv = SPELLutils::tokenize( *it, ":" );
			if (kv.size()!=2) continue;
			std::string modifier = kv[0];
			std::string value = kv[1];
			SPELLutils::trim(modifier);
			SPELLutils::trim(value);
			if (modifier.find(LanguageModifiers::Automatic) != std::string::npos)
			{
				if (value == PythonConstants::True) openMode = (SPELLopenMode) (openMode | OPEN_MODE_AUTOMATIC);
			}
			else if (modifier.find(LanguageModifiers::Visible) != std::string::npos)
			{
				if (value == PythonConstants::True) openMode = (SPELLopenMode) (openMode | OPEN_MODE_VISIBLE);
			}
			else if (modifier.find(LanguageModifiers::Blocking) != std::string::npos)
			{
				if (value == PythonConstants::True) openMode = (SPELLopenMode) (openMode | OPEN_MODE_BLOCKING);
			}
		}
	}
	else
	{
		LOG_WARN("Using default open mode");
		openMode = (SPELLopenMode) (OPEN_MODE_VISIBLE | OPEN_MODE_BLOCKING);
	}
	return openMode;
}
