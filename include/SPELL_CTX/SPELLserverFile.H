// ################################################################################
// FILE       : SPELLserverFile.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Type of server file definition
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

#ifndef __SPELL_SERVER_FILE_H__
#define __SPELL_SERVER_FILE_H__

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
typedef enum SPELLserverFile_
{
	FILE_ASRUN,
	FILE_LOG,
	FILE_UNKNOWN
}
SPELLserverFile;

/*@}*/
#endif
