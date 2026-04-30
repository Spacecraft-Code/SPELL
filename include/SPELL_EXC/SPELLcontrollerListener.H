// ################################################################################
// FILE       : SPELLcontrollerListener.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Controller listener interface
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

#ifndef __SPELL_CONTROLLER_LISTENER_H__
#define __SPELL_CONTROLLER_LISTENER_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
// System includes ---------------------------------------------------------


/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Interface for controller listeners
 **
 ** \par Description and usage:
 **
 **		Used by objects that require receiving controller events
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcontrollerListener
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLcontrollerListener() {;};

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		virtual std::string getId() { return "<noid>"; };
		virtual void callbackBeforeWait() {;};
		virtual void callbackAfterWait() {;};
		virtual void callbackBeforeDriverOperation() {;};
		virtual void callbackAfterDriverOperation() {;};
		virtual void callbackBeforeStatusChange( const SPELLexecutorStatus& status ) {;};
		virtual void callbackAfterStatusChange( const SPELLexecutorStatus& status ) {;};
		virtual void callbackOnStart() {;};
		virtual void callbackOnFinish() {;};
		virtual void callbackOnLineStep() {;};
};

typedef std::vector<SPELLcontrollerListener*> SPELLcontrollerListeners;

/*@}*/
#endif
