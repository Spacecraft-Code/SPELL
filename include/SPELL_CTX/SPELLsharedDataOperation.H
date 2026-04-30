// ################################################################################
// FILE       : SPELLsharedDataOperation.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Definition of the shared data operations
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

#ifndef __SPELL_SHARED_DATA_OPERATION_H__
#define __SPELL_SHARED_DATA_OPERATION_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
// Project includes ----------------------------------------------------------

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
typedef enum SPELLsharedDataOperation_
{
	SET_SHARED_DATA,
	DEL_SHARED_DATA,
	CLEAR_SHARED_SCOPE,
	ADD_SHARED_SCOPE,
	DEL_SHARED_SCOPE
}
SPELLsharedDataOperation;

/*@}*/
#endif
