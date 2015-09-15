// ################################################################################
// FILE       : SPELLwsStorage.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: WS storage model
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
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

#ifndef __SPELL_STORAGE_H__
#define __SPELL_STORAGE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Project includes --------------------------------------------------------

/** \defgroup SPELL_WS Warmstart & Recovery mechanism (SPELL_WS)
 *
 * This module provides the warmstart & Python interpreter recovery
 * mechanisms.
 *
 * */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Represents a persistent storage where Python API objects can be
 **        saved to or loade from.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLwsStorage
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
		/** Working mode for the storage file */
		enum Mode
		{
			/** Data can be read from the storage */
			MODE_READ,
			/** Data can be written to the storage */
			MODE_WRITE,
			/** Not set */
			MODE_UNINIT
		};
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**
		** \param filename IN: The path to the file were persistent data will
		**                     be stored.
		** \param mode IN: working mode.
		**////////////////////////////////////////////////////////////////////
		SPELLwsStorage( std::string filename, Mode mode );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLwsStorage();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Store the given Python object in the persistent storage.
		**
		** \param object IN: The object to be stored.
		**
		** \throws SPELLcoreException if the object cannot be stored.
		** \throws SPELLcoreException if there is an error in the marshal
		**////////////////////////////////////////////////////////////////////
		void storeObject( PyObject* object );

		//////////////////////////////////////////////////////////////////////
		/** Store the given long value in the persistent storage.
		**
		** \param value IN: The long value to be stored.
		**
		** \throws SPELLcoreException if the value cannot be stored.
		** \throws SPELLcoreException if there is an error in the marshal
		**////////////////////////////////////////////////////////////////////
		void storeLong( long value );

		//////////////////////////////////////////////////////////////////////
		/** Store the given Python object in the persistent storage. If the
		** object is NULL, stores the None Python object.
		**
		** \param object IN: The object to be stored.
		**
		** \throws SPELLcoreException if the object cannot be stored.
		** \throws SPELLcoreException if there is an error in the marshal
		**////////////////////////////////////////////////////////////////////
		void storeObjectOrNone( PyObject* object );

		//////////////////////////////////////////////////////////////////////
		/** Load the next object from persistent storage
		**
		** \returns The object loaded, or NULL
		**
		** \throws SPELLcoreException if the object cannot be loaded.
		**////////////////////////////////////////////////////////////////////
		PyObject* loadObject();

		//////////////////////////////////////////////////////////////////////
		/** Load the next long from persistent storage
		**
		** \returns The long value loaded
		**
		** \throws SPELLcoreException if the value cannot be loaded.
		**////////////////////////////////////////////////////////////////////
		long loadLong();

		//////////////////////////////////////////////////////////////////////
		/** Get the mode of the storage
		**
		** \returns The mode
		**////////////////////////////////////////////////////////////////////
		Mode getMode();

		//////////////////////////////////////////////////////////////////////
		/** Reset the persistent storage (empty the file)
		**////////////////////////////////////////////////////////////////////
		void reset();

		//////////////////////////////////////////////////////////////////////
		/** Check if the storage is ready
		**////////////////////////////////////////////////////////////////////
		bool isReady() { return m_file != NULL; };

		//////////////////////////////////////////////////////////////////////
		/** Reset the persistent storage and set the mode (empty the file)
		 *
		 * \param mode IN: the new mode
		**////////////////////////////////////////////////////////////////////
		void reset( const SPELLwsStorage::Mode& mode );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Get the Python type object corresponding to the string code.
		**
		** \returns The type object or NULL;
		**
		** \throws SPELLcoreException if the type object cannot be identified.
		**////////////////////////////////////////////////////////////////////
		PyTypeObject* getType( const std::string& code );

    // DATA MEMBERS //////////////////////////////////////////////////////////
    	/** Holds the file name for the storage data file */
    	std::string m_filename;
    	/** Holds the file name for the trace data file */
    	std::string m_traceFilename;
    	/** Holds the operation counter */
    	unsigned long m_opCounter;
    	/** Holds the working mode */
    	Mode          m_mode;
    	/** File handle for the storage data file */
    	std::fstream  m_file;
    	/** File handle for the trace data file */
    	std::ofstream m_trace;
};

/*@}*/
#endif
