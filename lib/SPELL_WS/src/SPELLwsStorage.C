// ################################################################################
// FILE       : SPELLwsStorage.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the storage model
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_WS/SPELLwsStorage.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------
#include "marshal.h"



#define LATEST_MARSHAL_VERSION 2

//=============================================================================
// CONSTRUCTOR: SPELLwsStorage::SPELLwsStorage
//=============================================================================
SPELLwsStorage::SPELLwsStorage( std::string filename, Mode mode )
{
	m_opCounter = 0;
    m_filename = filename;
    m_mode = mode;
    // Prefer to use stdio/FILE for this due to the Python marshal
    // API functions
    switch(mode)
    {
    case MODE_READ:
        DEBUG("[*] Open persistent file in read mode: " + filename);
        m_file.open( filename.c_str(), std::ios_base::in | std::ios_base::binary );
    	m_traceFilename = filename + "_READ";
        break;
    case MODE_WRITE:
        DEBUG("[*] Open persistent file in write mode: " + filename);
        m_file.open( filename.c_str(),std::ios_base::out | std::ios_base::binary );
    	m_traceFilename = filename + "_WRITE";
        break;
    case MODE_UNINIT:
        THROW_EXCEPTION("Unable to setup persistent storage", "Mode is not set", SPELL_ERROR_WSTART);
        break;
    }

	m_trace.open(m_traceFilename.c_str(), std::ios_base::out );

    if (!m_file.is_open())
    {
        LOG_ERROR("Unable to setup persistent storage, cannot open file: '" + filename + "'");
    }
}

//=============================================================================
// DESTRUCTOR: SPELLwsStorage::~SPELLwsStorage
//=============================================================================
SPELLwsStorage::~SPELLwsStorage()
{
	DEBUG("[*] Close persistent file: " + m_filename);
	m_trace.close();
	m_file.close();
}

//=============================================================================
// METHOD    : SPELLwsStorage::getMode
//=============================================================================
SPELLwsStorage::Mode SPELLwsStorage::getMode()
{
    return m_mode;
}

//=============================================================================
// METHOD    : SPELLwsStorage::reset()
//=============================================================================
void SPELLwsStorage::reset()
{
    DEBUG("[*] Reset persistent file");
    m_file.close();
    m_trace.close();
    if (m_mode == MODE_READ)
    {
        m_file.open( m_filename.c_str(), std::ios_base::in | std::ios_base::binary );
    	m_traceFilename = m_filename + "_READ";
    }
    else
    {
        m_file.open( m_filename.c_str(), std::ios_base::out | std::ios_base::binary );
    	m_traceFilename = m_filename + "_WRITE";
    }
    if (!m_file.is_open())
    {
        THROW_EXCEPTION("Unable to reset persistent storage", "Cannot open file: '" + m_filename + "'", SPELL_ERROR_FILESYSTEM);
    }
	m_opCounter = 0;
	m_trace.open(m_traceFilename.c_str(), std::ios_base::out );
}

//=============================================================================
// METHOD    : SPELLwsStorage::reset()
//=============================================================================
void SPELLwsStorage::reset( const SPELLwsStorage::Mode& mode )
{
    m_mode = mode;
    reset();
}

//=============================================================================
// METHOD    : SPELLwsStorage::storeObject
//=============================================================================
void SPELLwsStorage::storeObject( PyObject* object )
{
	if (!m_file.is_open()) return;
    if (object == NULL)
    {
        THROW_EXCEPTION("Unable to store object", "Null reference given", SPELL_ERROR_WSTART);
    }
    if (m_mode == MODE_READ)
    {
        THROW_EXCEPTION("Unable to store object", "Initialized in read mode", SPELL_ERROR_WSTART);
    }

    std::string ptype = PYREPR(PyObject_Type(object));
	DEBUG("Encoding object " + PYREPR(object) + " of type " + ptype);
	// Marshal the object to the persistent storage file
	PyObject* marshal = PyMarshal_WriteObjectToString( object, LATEST_MARSHAL_VERSION );
	// Ensure there is no internal Python error
	SPELLpythonHelper::instance().checkError();
	int marshal_len = PyObject_Length(marshal);
	DEBUG("Marshal length: " + ISTR(marshal_len));
	char* encoded = PyString_AsString(marshal);

    // FORMAT IN FILE:
    // COUNT \1 PTYPE \1 MARSHAL LENGTH
    // MARSHAL

    // If the marshal was OK, continue and store in file: OP,TYPE,MARSHALLED
    m_opCounter++;
    m_file << m_opCounter << "\1" << ptype << "\1" << marshal_len << std::endl;
    m_file.write(encoded,marshal_len); m_file << std::endl;

    std::flush(m_file);

    m_trace << "[" << m_opCounter << "] STORE (" << m_filename << ") " + PYREPR(object) << " [ Type=" + ptype << ", Marhsal length=" + ISTR(marshal_len) + " ]" << std::endl;
    std::flush(m_trace);

}

//=============================================================================
// METHOD    : SPELLwsStorage::storeLong
//=============================================================================
void SPELLwsStorage::storeLong( long value )
{
	if (!m_file.is_open()) return;
    if (m_mode == MODE_READ)
    {
        THROW_EXCEPTION("Unable to store long value", "Initialized in read mode", SPELL_ERROR_WSTART);
    }
    PyObject* longObject = PyLong_FromLong(value);
    storeObject(longObject);
    Py_XDECREF(longObject);
}

//=============================================================================
// METHOD    : SPELLwsStorage::storeObjectOrNone
//=============================================================================
void SPELLwsStorage::storeObjectOrNone( PyObject* object )
{
	if (!m_file.is_open()) return;
    if (m_mode == MODE_READ)
    {
        THROW_EXCEPTION("Unable to store object", "Initialized in read mode", SPELL_ERROR_WSTART);
    }
    if (object == NULL)
    {
        // Marshal None to the file
    	storeObject(Py_None);
    }
    else
    {
        storeObject(object);
    }
}

//=============================================================================
// METHOD    : SPELLwsStorage::loadObject
//=============================================================================
PyObject* SPELLwsStorage::loadObject()
{
	if (!m_file.is_open()) return NULL;
    if (m_mode == MODE_WRITE)
    {
        THROW_EXCEPTION("Unable to load object", "Initialized in write mode", SPELL_ERROR_WSTART);
    }

    // FORMAT IN FILE:
    // COUNT \1 PTYPE \1 MARSHAL LENGTH
    // MARSHAL

    std::string line = "";
    // Get first the line with the info
    while(!m_file.eof() && (line == ""))
    {
    	std::getline(m_file,line);
        DEBUG("Obtained line [" + line + "]");
    }

    DEBUG("Load object from line [" + line + "]");

    std::vector<std::string> elements = SPELLutils::tokenize(line,"\1");

    PyObject* obj = NULL;
    std::string ptype = elements[1];
	int marshal_len = STRI(elements[2]);
	DEBUG("Decoding object of type " + elements[1] + ", marshal length " + elements[2]);
	// Get the line with the marshal
	char buffer[4512];
	m_file.read(buffer,marshal_len);
	obj = (PyObject*) PyMarshal_ReadObjectFromString( buffer, marshal_len );
	DEBUG("Decoded: " + PYCREPR(obj));
	// Check that the unmarshal was ok
	SPELLpythonHelper::instance().checkError();

    m_opCounter++;
    m_trace << "[" << m_opCounter << "] LOAD (" << m_filename << ") " << PYREPR(obj) << " [ Type=" << PYREPR(PyObject_Type(obj)) << ", Marshal length=" + ISTR(marshal_len) + " ]" << std::endl;
    std::flush(m_trace);


    if (obj != NULL) Py_INCREF(obj);

    return obj;
}

//=============================================================================
// METHOD    : SPELLwsStorage::loadLong
//=============================================================================
long SPELLwsStorage::loadLong()
{
	if (!m_file.is_open()) return -1;
    if (m_mode == MODE_WRITE)
    {
        THROW_EXCEPTION("Unable to load long value", "Initialized in write mode", SPELL_ERROR_WSTART);
    }
    PyObject* longObject = loadObject();
    long value = PyLong_AsLong(longObject);
    Py_XDECREF(longObject);
    return value;
}
