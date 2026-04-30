// ################################################################################
// FILE       : SPELLcifStartupInfo.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Information for CIF startup
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

#ifndef __SPELL_CIF_STARTUP_INFO_H__
#define __SPELL_CIF_STARTUP_INFO_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_CIF */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **      No concurrency issues foreseen.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcifStartupInfo
{
public:

	SPELLcifStartupInfo( const SPELLcifStartupInfo& info )
	{
		procId = info.procId;
		procName = info.procName;
		contextName = info.contextName;
		contextPort = info.contextPort;
		timeId = info.timeId;
	}

	SPELLcifStartupInfo( SPELLcifStartupInfo* info )
	{
		procId = info->procId;
		procName = info->procName;
		contextName = info->contextName;
		contextPort = info->contextPort;
		timeId = info->timeId;
	}

	SPELLcifStartupInfo()
	{
		timeId = "";
		procId = "";
		procName = "";
		contextName = "";
		contextPort = -1;
	}

	std::string procId;
	std::string procName;
	std::string contextName;
	int contextPort;
	std::string timeId;
};

/*@}*/
#endif
