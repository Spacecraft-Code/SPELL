// ################################################################################
// FILE       : SPELLdispatchListener.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Dispatch listener interface
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

#ifndef __SPELL_DISPATCH_LISTENER_H__
#define __SPELL_DISPATCH_LISTENER_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// System includes ---------------------------------------------------------


/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Interface for dispatch listeners
 **
 ** \par Description and usage:
 **
 **		Used by objects that require receiving dispatch events
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLdispatchListener
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLdispatchListener() {;};

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		virtual std::string getId() { return "<noid>"; };
		virtual void callbackEventLine( PyFrameObject* frame, const std::string& file, const int line, const std::string& name ) {;};
		virtual void callbackEventCall( PyFrameObject* frame, const std::string& file, const int line, const std::string& name ) {;};
		virtual void callbackEventReturn( PyFrameObject* frame, const std::string& file, const int line, const std::string& name ) {;};
		virtual void callbackEventError( PyFrameObject* frame, const std::string& file, const int line, const std::string& name ) {;};
};

typedef std::vector<SPELLdispatchListener*> SPELLdispatchListeners;

/*@}*/
#endif
