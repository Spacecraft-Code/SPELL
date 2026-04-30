// ################################################################################
// FILE       : SPELLstatusInfo.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor status information structure
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

#ifndef __SPELL_STATUS_INFO_H__
#define __SPELL_STATUS_INFO_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_CIF */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Holds information used for status notifications
 **
**////////////////////////////////////////////////////////////////////////////
typedef struct SPELLstatusInfo_
{
	SPELLstatusInfo_( const SPELLexecutorStatus& st )
	{
		status = st;
		condition = "";
		actionLabel = "";
		actionEnabled = false;
	};

	/** Current status */
	SPELLexecutorStatus	status;
	/** Execution condition, if any */
	std::string		condition;
	/** Currently programmed user action, if any */
	std::string		actionLabel;
	/** Enabled flag for the user action */
	bool			actionEnabled;
}
SPELLstatusInfo;

/*@}*/
#endif
