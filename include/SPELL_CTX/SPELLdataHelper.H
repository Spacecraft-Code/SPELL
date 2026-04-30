// ################################################################################
// FILE       : SPELLdataHelper.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Utilities for data handling
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

#ifndef __SPELL_DATA_HELPER_H__
#define __SPELL_DATA_HELPER_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
#include "SPELL_CTX/SPELLopenMode.H"
#include "SPELL_CTX/SPELLclientMode.H"
#include "SPELL_CTX/SPELLclientOperation.H"
#include "SPELL_CTX/SPELLexecutorOperation.H"
#include "SPELL_CTX/SPELLsharedDataOperation.H"
#include "SPELL_CTX/SPELLserverFile.H"
// Project includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_UTIL/SPELLutils.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
namespace SPELLdataHelper
{
	//============================================================================
	// FUNCTION        :
	//============================================================================
	SPELLserverFile serverFileFromString( const std::string& file );

	//============================================================================
	// FUNCTION        :
	//============================================================================
	std::string openModeToString( const SPELLopenMode& mode );

	//============================================================================
	// FUNCTION        :
	//============================================================================
	SPELLopenMode openModeFromString( const std::string& mode );

	//============================================================================
	// FUNCTION        :
	//============================================================================
	std::string clientModeToString( const SPELLclientMode& mode );

	//============================================================================
	// FUNCTION        :
	//============================================================================
	SPELLclientMode clientModeFromString( const std::string& mode  );

	//============================================================================
	// FUNCTION        :
	//============================================================================
	std::string sharedDataOperationToString( const SPELLsharedDataOperation& operation );

	//============================================================================
	// FUNCTION        :
	//============================================================================
	std::string clientOperationToString( const SPELLclientOperation& operation );

	//============================================================================
	// FUNCTION        :
	//============================================================================
	std::string executorStatusToString( SPELLexecutorStatus st );

	//============================================================================
	// FUNCTION        :
	//============================================================================
	SPELLexecutorStatus stringToExecutorStatus( std::string st );

	//=============================================================================
	// FUNCTION		   : sourceToString
	//=============================================================================
	std::string sourceToString( std::vector<std::string> lines );

	//=============================================================================
	// FUNCTION        : linesToString
	//=============================================================================
	std::string linesToString( std::vector<std::string> lines );
};

/*@}*/
#endif
