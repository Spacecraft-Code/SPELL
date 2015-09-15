// ################################################################################
// FILE       : SPELLwsDynamicData.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the dynamic runtime data manager
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
#include "SPELL_WS/SPELLwsDynamicData.H"
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
#include "SPELL_WS/SPELLwsDictDataHandler.H"
#include "SPELL_WS/SPELLwsListDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_EXC/SPELLlnotab.H"
#include "SPELL_DTA/SPELLdtaContainerObject.H"
#include "SPELL_DTA/SPELLdtaVariableObject.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_WRP/SPELLpyHandle.H"
// System includes ---------------------------------------------------------

//=============================================================================
/** Fake object required for valuestack management */
typedef struct
{
	PyObject_HEAD;
	long 			it_index;
	PyListObject* 	it_seq;
}
ListIteratorMirror;
//=============================================================================


//=============================================================================
// CONSTRUCTOR: SPELLwsDynamicData::SPELLwsDynamicData
//=============================================================================
SPELLwsDynamicData::SPELLwsDynamicData( const SPELLwsStartupInfo& info, unsigned int depth, PyFrameObject* frame )
: m_startup(info),
  m_frame(frame)
{
	DEBUG("[DYN] Created dynamic data manager for frame " + PYCREPR(m_frame));
	assert(m_frame != NULL);

	if (info.persistentFile != "")
	{
		m_persistentFile = info.persistentFile + "_" + ISTR(depth) + ".wsd";
		DEBUG("DYN Using persistent file: '" + m_persistentFile + "'");
	}
	else
	{
		m_persistentFile = "";
	}

	if (info.recoveryFile != "")
	{
		m_recoveryFile = info.recoveryFile + "_" + ISTR(depth) + ".wsd";
		DEBUG("DYN Using recovery file: '" + m_recoveryFile + "'");
	}
	else
	{
		m_recoveryFile = "";
	}

	DEBUG("DYN working mode: " + WorkingModeToString(m_startup.workingMode));
	DEBUG("DYN perform recovery: " + BSTR(m_startup.performRecovery));

	// If we are in recovery mode
	if (m_startup.performRecovery && m_recoveryFile != "")
	{
		m_storage = new SPELLwsStorage( m_recoveryFile, SPELLwsStorage::MODE_READ);
	}
	else if (m_persistentFile != "")
	{
		m_storage = new SPELLwsStorage( m_persistentFile, SPELLwsStorage::MODE_WRITE);
	}
	m_depth = depth;
}

//=============================================================================
// DESTRUCTOR: SPELLwsDynamicData::~SPELLwsDynamicData
//=============================================================================
SPELLwsDynamicData::~SPELLwsDynamicData()
{
	DEBUG("[DYN] Destroyed dynamic data manager for frame " + PYCREPR(m_frame));
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::reset()
//=============================================================================
void SPELLwsDynamicData::reset()
{
	DEBUG("[DYN] Reset dynamic data for frame " + PYCREPR(m_frame));
	m_storage->reset();
	m_iBlocks.clear();
	// IMPORTANT do not DECREF objects in ValueStack or FastLocals, since they
	// are borrowed references managed by Python layer.
	m_valueStack.clear();
	m_fastLocals.clear();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::cleanup()
//=============================================================================
void SPELLwsDynamicData::cleanup()
{
	DEBUG("[DYN] Remove dynamic data file for frame " + PYCREPR(m_frame));
	if (m_persistentFile != "")
	{
		SPELLutils::deleteFile( m_persistentFile );
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::update()
//=============================================================================
void SPELLwsDynamicData::update()
{
	DEBUG("[DYN] Update dynamic data for frame " + PYCREPR(m_frame));
	// We need to keep a copy of these these data structures, since the Python interpreter code
	// unwinds the stack and the try-blocks in case of exceptions.
	updateTryBlocks();
	updateValueStack();
	updateFastLocals();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::recover()
//=============================================================================
void SPELLwsDynamicData::recover()
{
	DEBUG("[DYN] Recover dynamic data for frame " + PYCREPR(m_frame));
	// We need to re-create these data structures, since the Python interpreter code
	// unwinds the stack and the try-blocks in case of exceptions.
	recoverTryBlocks();
	recoverValueStack();
	recoverFastLocals();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::save()
//=============================================================================
void SPELLwsDynamicData::save()
{
	DEBUG("[DYN] Save dynamic data for frame " + PYCREPR(m_frame));
	DEBUG("[DYN] Pre-check errors...");
	SPELLpythonHelper::instance().checkError();
	// Empty the file. We only want the latest change
	// NOTE: this can be removed in case we want the full history of
	// changes, although some mods would need to be done to the load code
	DEBUG("[DYN] Resetting storage first");
	m_storage->reset();
	// Store current data
	storeParameters();
	SPELLpythonHelper::instance().checkError();
	storeTryBlocks();
	SPELLpythonHelper::instance().checkError();
	storeValueStack();
	SPELLpythonHelper::instance().checkError();
	storeGlobals();
	SPELLpythonHelper::instance().checkError();
	storeLocals();
	SPELLpythonHelper::instance().checkError();
	storeFastLocals();
	SPELLpythonHelper::instance().checkError();
	storeDTA("ARGS");
	SPELLpythonHelper::instance().checkError();
	storeDTA("IVARS");
	DEBUG("[DYN] Save dynamic data for frame " + PYCREPR(m_frame) + " done");
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::restore()
//=============================================================================
void SPELLwsDynamicData::restore()
{
	if (!m_storage->isReady())
	{
		THROW_EXCEPTION("Failed to restore dynamic data", "Storage not ready", SPELL_ERROR_WSTART);
	}

	DEBUG("[DYN] Restore dynamic data for frame " + PYCREPR(m_frame));
	loadParameters();
	loadTryBlocks();
	loadValueStack();
	loadGlobals();
	loadLocals();
	loadFastLocals();
	loadDTA("ARGS");
	loadDTA("IVARS");

	recover();

	DEBUG("[DYN] Restore dynamic data for frame " + PYCREPR(m_frame) + " done");

	if (m_persistentFile != "")
	{
		// Reset the storage to mode write, using now the assigned persistent file,
		// not the recovery file
		DEBUG("[DYN] Switching to saving state mode");
		delete m_storage;
		m_storage = new SPELLwsStorage( m_persistentFile, SPELLwsStorage::MODE_WRITE );
		// Re-save the state, so that the persistent file contains the current state
		save();
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::updateTryBlocks()
//=============================================================================
void SPELLwsDynamicData::updateTryBlocks()
{
	assert(m_frame != NULL);
	// If there are tryblocks defined in the frame
	unsigned int numTryBlocks = m_frame->f_iblock;
	if (numTryBlocks>0)
	{
		DEBUG("[DYN] Updating try blocks on frame " + PYCREPR(m_frame));

		// If there is a different number of blocks than before, update. We assume they dont change once created...
		if ( numTryBlocks > m_iBlocks.size())
		{
			PyTryBlock block = m_frame->f_blockstack[m_frame->f_iblock-1];
			m_iBlocks.push_back(block);
			DEBUG("[DYN] 	add block: [" + ISTR(block.b_type) + "," + ISTR(block.b_handler) + "," + ISTR(block.b_level) + "]");
		}
		// Less blocks...
		else if ((unsigned int) m_frame->f_iblock < m_iBlocks.size())
		{
			// Just throw it away
			TryBlocks::iterator it = m_iBlocks.end(); it--;
			m_iBlocks.erase(it);
			DEBUG("[DYN] 	remove block");
		}
		DEBUG("[DYN] Updating try blocks done");
	}
	// Take into account the case of going out loops and try blocks ( the frame
	// does not have blocks defined, so we must clear ours )
	else if (m_iBlocks.size()>0)
	{
		// No need to delete or decref
		m_iBlocks.clear();
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::recoverTryBlocks()
//=============================================================================
void SPELLwsDynamicData::recoverTryBlocks()
{
	assert(m_frame != NULL);
	// If there were blocks defined...
	if (m_iBlocks.size()>0)
	{
		DEBUG("[DYN] 	Restoring try blocks on frame " + PYCREPR(m_frame));
		TryBlocks::iterator it;
		unsigned int count = 0;
		for( it = m_iBlocks.begin(); it != m_iBlocks.end(); it++)
		{
			PyTryBlock& block = (*it);
			DEBUG("[DYN] 		block " + ISTR(count) + ": [" + ISTR(block.b_type) + "," + ISTR(block.b_handler) + "," + ISTR(block.b_level) + "]");
			PyFrame_BlockSetup( m_frame, block.b_type, block.b_handler, block.b_level );
			count++;
		}
		m_frame->f_iblock = m_iBlocks.size();
		DEBUG("[DYN] 	Restoring try blocks done");
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeTryBlocks()
//=============================================================================
void SPELLwsDynamicData::storeTryBlocks()
{
	DEBUG("[DYN] Storing try blocks (" + ISTR(m_iBlocks.size()) + ")" );
	// Store tryblocks first. Size of the list:
	m_storage->storeLong( m_iBlocks.size() );
	// Store the blocks now, if any
	TryBlocks::iterator bit;
	for( bit = m_iBlocks.begin(); bit != m_iBlocks.end(); bit++ )
	{
		m_storage->storeLong( (*bit).b_type );
		m_storage->storeLong( (*bit).b_handler );
		m_storage->storeLong( (*bit).b_level );
		DEBUG("    - [" + ISTR((*bit).b_type) + "," + ISTR((*bit).b_handler) + "," + ISTR((*bit).b_level) + "]");
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadTryBlocks()
//=============================================================================
void SPELLwsDynamicData::loadTryBlocks()
{
	int numBlocks = m_storage->loadLong();
	DEBUG("[DYN] Loading try blocks (" + ISTR(numBlocks) + ")" );
	for( int count=0; count<numBlocks; count++)
	{
		PyTryBlock* block = new PyTryBlock();
		block->b_type = m_storage->loadLong();
		block->b_handler = m_storage->loadLong();
		block->b_level = m_storage->loadLong();
		DEBUG("    - [" + ISTR(block->b_type) + "," + ISTR(block->b_handler) + "," + ISTR(block->b_level) + "]");
		m_iBlocks.push_back(*block);
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::updateValueStack()
//=============================================================================
void SPELLwsDynamicData::updateValueStack()
{
	assert(m_frame != NULL);
	// If there are stack values defined
	if ( (m_frame->f_stacktop != NULL) && (m_frame->f_valuestack != m_frame->f_stacktop) )
	{
		int stackCount = m_frame->f_stacktop - m_frame->f_valuestack;

		// Add a new item to the stack information
		if ((unsigned int) stackCount > m_valueStack.size())
		{
			DEBUG("[DYN] Updating value stack on frame " + PYCREPR(m_frame));

			PyObject** p;
			p = m_frame->f_valuestack;

			PyTypeObject* type = (PyTypeObject*) PyObject_Type(*p);
			if (STR(type->tp_name)=="listiterator" )
			{
				// Get the iterator characteristics
				ListIteratorMirror* iterator = (ListIteratorMirror*) (*p);
				DEBUG("[DYN] 	add list iterator: [" + ISTR(iterator->it_index) + "," + PYCREPR(iterator->it_seq) + "]");

				// Create a copy
				PyObject* newIterator = PyObject_GetIter( (PyObject*)iterator->it_seq );
				newIterator->ob_refcnt = (*p)->ob_refcnt;
				ListIteratorMirror* itm = (ListIteratorMirror*)newIterator;
				itm->it_index = iterator->it_index;
				// IMPORTANT this is a borrowed reference
				DEBUG("[DYN] 	stored iterator: [" + ISTR(itm->it_index) + "," + PYCREPR(itm->it_seq) + "]");
				m_valueStack.push_back(newIterator);
			}
			else
			{
				DEBUG("[DYN] Adding to valuestack, unprocessed: " + PYREPR(*p));
				// IMPORTANT this is a borrowed reference
				m_valueStack.push_back(*p);
			}

			DEBUG("[DYN] Updating value stack done");
		}
		// Less items
		else if ((unsigned int) stackCount < m_valueStack.size())
		{
			// Throw it away...
			ObjectList::iterator it = m_valueStack.end(); it--;
			DEBUG("[DYN] 	remove value stack object");
			// IMPORTANT do not DECREF objects in ValueStack or FastLocals, since they
			// are borrowed references managed by Python layer.
			m_valueStack.erase(it);
		}
		// Check item changes
		else if (stackCount>0)
		{
			PyObject** p;
			p = m_frame->f_valuestack;
			for( unsigned int index = 0; index < m_valueStack.size(); index++)
			{
				if (*p == NULL) break;
				DEBUG("[DYN] 	checking value stack object: " + PYREPR(*p));
				PyTypeObject* type = (PyTypeObject*) PyObject_Type(*p);
				if (STR(type->tp_name)=="listiterator" )
				{
				    ListIteratorMirror* stackIterator = (ListIteratorMirror*) (*p);
				    ListIteratorMirror* storedIterator = (ListIteratorMirror*) m_valueStack[index];
				    storedIterator->it_index = stackIterator->it_index;
					DEBUG("[DYN] 	update iterator: [" + ISTR(stackIterator->it_index) + "," + PYCREPR(stackIterator->it_seq) + "]");
				}
			}
		}
	}
	// No values defined in the frame, ensure we dont keep them either
	else if (m_valueStack.size() > 0)
	{
		// IMPORTANT do not DECREF objects in ValueStack or FastLocals, since they
		// are borrowed references managed by Python layer.
		m_valueStack.clear();
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::recoverValueStack()
//=============================================================================
void SPELLwsDynamicData::recoverValueStack()
{
	assert(m_frame != NULL);
	// If there are values to recover
	if (m_valueStack.size()>0)
	{
		DEBUG("[DYN] 	Restoring value stack on frame " + PYCREPR(m_frame));
		PyObject** stack_pointer = m_frame->f_valuestack;

		for( unsigned int count = 0; count<m_valueStack.size(); count++)
		{
			PyObject* stackObject = m_valueStack[count];
			DEBUG("[DYN] 		add value stack object " + PYCREPR(stackObject));
			PyTypeObject* type = (PyTypeObject*) PyObject_Type(stackObject);
			DEBUG("[DYN] Type is " + STR(type->tp_name));
			if (STR(type->tp_name)=="listiterator" )
			{
				ListIteratorMirror* stackIterator = (ListIteratorMirror*) (stackObject);
				DEBUG("[DYN] 	      restoring iterator: [" + ISTR(stackIterator->it_index) + "," + PYCREPR(stackIterator->it_seq) + "]");
				PyObject* newIterator = PyObject_GetIter( (PyObject*)stackIterator->it_seq );
				newIterator->ob_refcnt = stackObject->ob_refcnt;
				*stack_pointer++ = newIterator;
			}
			else
			{
				*stack_pointer++ = stackObject;
			}
		}
		m_frame->f_stacktop = stack_pointer--;
		m_frame->f_code->co_stacksize = m_valueStack.size();
		DEBUG("[DYN] 	Restoring value stack done");
	}
	// Otherwise define the top as the same value as the bottom of the stack
	else
	{
		m_frame->f_stacktop = m_frame->f_valuestack;
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeValueStack()
//=============================================================================
void SPELLwsDynamicData::storeValueStack()
{
	// Store the value stack. Size of the list first:
	m_storage->storeLong( m_valueStack.size() );
	DEBUG("[DYN] Storing value stack for frame " + PYCREPR(m_frame));
	ObjectList::iterator it;
	for( it = m_valueStack.begin(); it != m_valueStack.end(); it++)
	{
	    PyTypeObject* type = (PyTypeObject*) PyObject_Type(*it);
	    bool isListIterator = STR(type->tp_name)=="listiterator";
		if (isListIterator)
		{
			// Store the identifier tag
			PyObject* mod_str  = STRPY("<LIST-ITERATOR>");
			SPELLwsObjectDataHandler marker(mod_str);
			marker.setStorage(m_storage);
			// Store the marker
			marker.write();
		}
		else
		{
			// Store the identifier tag
			PyObject* mod_str  = STRPY("<OBJECT>");
			SPELLwsObjectDataHandler marker(mod_str);
			marker.setStorage(m_storage);
			// Store the marker
			marker.write();
		}

		if (isListIterator)
		{
		    // The iterator characteristics
		    ListIteratorMirror* iterator = (ListIteratorMirror*) (*it);
		    // The list
		    SPELLwsListDataHandler list( (PyObject*) iterator->it_seq );
		    list.setStorage(m_storage);

		    // The current index
		    m_storage->storeLong(iterator->it_index);
		    // Store the list
		    list.write();
		}
		else
		{
		    SPELLwsObjectDataHandler handler( *it );
		    handler.setStorage(m_storage);
		    handler.write();
		}
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadValueStack()
//=============================================================================
void SPELLwsDynamicData::loadValueStack()
{
	long numValues = m_storage->loadLong();
	DEBUG("[DYN] Loading value stack for frame " + PYCREPR(m_frame));
	for( int count = 0; count < numValues; count++)
	{
		// Retrieve the marker
		SPELLwsObjectDataHandler marker(NULL);
		marker.setStorage(m_storage);
		// Load the marker
		marker.read();
		PyObject* markerObj = marker.getObject();
		std::string markerStr = PYSTR(markerObj);
		if (markerStr == "<OBJECT>")
		{
			// Retrieve the marker
			SPELLwsObjectDataHandler object(NULL);
			object.setStorage(m_storage);
			// Store the marker
			object.read();
			PyObject* value = object.getObject();
			m_valueStack.push_back(value);
		}
		else
		{
			long index = m_storage->loadLong();
		    SPELLwsListDataHandler list(NULL);
		    list.setStorage(m_storage);

			ListIteratorMirror* iterator = new ListIteratorMirror();
			iterator->it_index = index;
			list.read();
			iterator->it_seq = (PyListObject*) list.getObject();
			m_valueStack.push_back( (PyObject*) iterator );
		}
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::updateFastLocals()
//=============================================================================
void SPELLwsDynamicData::updateFastLocals()
{
	assert(m_frame != NULL);
	int numLocals = m_frame->f_code->co_nlocals-1;
	DEBUG("[DYN] Update fast locals: " + ISTR(numLocals));
	if (numLocals<0) numLocals = 0;
	m_fastLocals.clear();
    if ((numLocals>0) && ((unsigned int)numLocals != m_fastLocals.size()))
	{
		PyObject** lplus = m_frame->f_localsplus;
		for ( int count = 0; count<numLocals-1; count++)
		{
			if (*lplus)
			{
				DEBUG("      add fast local: " + PYREPR(*lplus));
				// IMPORTANT  this is a borrowed reference
				m_fastLocals.push_back(*lplus);
				lplus++;
			}
		}
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::recoverFastLocals()
//=============================================================================
void SPELLwsDynamicData::recoverFastLocals()
{
	assert(m_frame != NULL);
	DEBUG("[DYN] Recover fast locals: " + ISTR(m_fastLocals.size()));
	if (m_fastLocals.size()>0)
	{
		 for( unsigned int count = 0; count<m_fastLocals.size(); count++)
		 {
			 PyObject* obj = m_fastLocals[count];
			 DEBUG("      recover fast local " + PYREPR(obj));
			 m_frame->f_localsplus[count] = obj;
		 }
		 m_frame->f_code->co_nlocals = m_fastLocals.size()+1;
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeFastLocals()
//=============================================================================
void SPELLwsDynamicData::storeFastLocals()
{
	DEBUG("[DYN] Store fast locals: " + ISTR(m_fastLocals.size()));
	// Store the fast locals. Size of list first
	m_storage->storeLong( m_fastLocals.size() );
	ObjectList::iterator it;
	for( it = m_fastLocals.begin(); it != m_fastLocals.end(); it++)
	{
		DEBUG("      storing fast local " + PYREPR(*it));
		m_storage->storeObjectOrNone(*it);
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadFastLocals()
//=============================================================================
void SPELLwsDynamicData::loadFastLocals()
{
	long numLocals = m_storage->loadLong();
	DEBUG("[DYN] Load fast locals: " + ISTR(numLocals));
	for(int count=0; count<numLocals; count++)
	{
		PyObject* obj = m_storage->loadObject();
		DEBUG("      loaded fast local " + PYREPR(obj));
		m_fastLocals.push_back( obj );
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeGlobals()
//=============================================================================
void SPELLwsDynamicData::storeGlobals()
{
	DEBUG("[DYN] Storing globals");
	assert( m_frame->f_globals != NULL );
	storeDictionary( m_frame->f_globals );
	storeConstReferences( m_frame->f_globals );
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadGlobals()
//=============================================================================
void SPELLwsDynamicData::loadGlobals()
{
	// Do not overwrite the globals dictionary, just overwrite restored values
	// The functions and builtins are not considered in persistency


	if (m_frame->f_globals == NULL)
	{
		THROW_EXCEPTION("Cannot restore frame " + PYCREPR(m_frame), "Frame globals dictionary is missing", SPELL_ERROR_WSTART);
	}

	PyObject* restored = loadDictionary();

	// Sanity check
	if (restored == NULL)
	{
		THROW_EXCEPTION("Cannot restore frame " + PYCREPR(m_frame), "Could not recover globals dictionary", SPELL_ERROR_WSTART);
	}

	SPELLpyHandle keys = PyDict_Keys( restored );
	int numKeys = PyList_Size( keys.get() );
	for( int index=0; index<numKeys; index++ )
	{
		PyObject* key = PyList_GetItem(keys.get(),index);
		PyObject* value = PyDict_GetItem( restored, key );
		PyDict_SetItem( m_frame->f_globals, key, value );
	}

	// Sanity check
	if (m_frame->f_globals == NULL)
	{
		THROW_EXCEPTION("Cannot restore frame " + PYCREPR(m_frame), "Failed to restore frame globals", SPELL_ERROR_WSTART);
	}

	loadConstReferences( m_frame->f_globals );
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeLocals()
//=============================================================================
void SPELLwsDynamicData::storeLocals()
{
	DEBUG("[DYN] Storing locals");
	// IMPORTANT: do not remove this PyFrame calls. Inside functions, local variables MAY be defined in the
	// fast locals, depending on their contents. These calls are needed so that the storeDictionary() call
	// is able to store those local variables which were not moved to fast locals. Fast locals and locals
	// are somehow complementary mechanisms and these calls are needed for their consistency when storing
	// and loading variables.
	PyFrame_FastToLocals(m_frame);
	storeDictionary( m_frame->f_locals );
	PyFrame_LocalsToFast(m_frame,0);
	storeConstReferences( m_frame->f_locals );
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadLocals()
//=============================================================================
void SPELLwsDynamicData::loadLocals()
{
	PyObject* restored = loadDictionary();

	if (restored != NULL)
	{
		DEBUG("[DYN] Restored locals dictionary");
		// IMPORTANT: do not remove this PyFrame calls. Inside functions, local variables MAY be defined in the
		// fast locals, depending on their contents. These calls are needed so that the storeDictionary() call
		// is able to store those local variables which were not moved to fast locals. Fast locals and locals
		// are somehow complementary mechanisms and these calls are needed for their consistency when storing
		// and loading variables.
		PyFrame_FastToLocals(m_frame);
		SPELLpyHandle keys = PyDict_Keys( restored );
		int numKeys = PyList_Size( keys.get() );
		for( int index=0; index<numKeys; index++ )
		{
			PyObject* key = PyList_GetItem(keys.get(),index);
			PyObject* value = PyDict_GetItem( restored, key );
			PyDict_SetItem( m_frame->f_locals, key, value );
		}
		PyFrame_LocalsToFast(m_frame,1);
	}

	loadConstReferences( m_frame->f_locals );
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeConstReferences()
//=============================================================================
void SPELLwsDynamicData::storeConstReferences( PyObject* dictionary )
{
	DEBUG("[DYN] Store const references");
	if (dictionary == NULL)
	{
		m_storage->storeLong( 0 );
		return;
	}
	// Store possibly loaded functions which are ignored by the dictionary
	// handler, if they are part of the code definition
	DEBUG("[DYN] Counting const references");
	// Store first the amount of code references
	unsigned int numReferences = 0;
	int numConsts = 0;
	PyObject* co_consts = m_frame->f_code->co_consts;
	if (co_consts != NULL)
	{
		numConsts = PyTuple_Size(co_consts);
		DEBUG("[DYN] Defined " + ISTR(numConsts) + " constants in code");
		for( int index = 0; index < numConsts; index++ )
		{
			PyObject* item = PyTuple_GetItem(co_consts,index);
			// If the constant is a code object
			if ((item != NULL)&&(PyCode_Check(item)))
			{
				PyCodeObject* code = (PyCodeObject*) item;
				// And the code object has been loaded in the globals dictionary
				if (PyDict_Contains( dictionary, code->co_name ))
				{
					numReferences++;
				}
			}
		}
	}

	DEBUG("[DYN] Number of code references: " + ISTR(numReferences));
	m_storage->storeLong( numReferences );

	if (numReferences>0)
	{
		for( int index = 0; index < numConsts; index++ )
		{
			PyObject* item = PyTuple_GetItem(co_consts,index);
			// If the constant is a code object
			if ((item != NULL)&&(PyCode_Check(item)))
			{
				PyCodeObject* code = (PyCodeObject*) item;
				// And the code object has been loaded in the globals dictionary
				if (PyDict_Contains( dictionary, code->co_name ))
				{
					DEBUG("[DYN] Storing code reference: " + PYSTR(code->co_name));
					m_storage->storeObject( code->co_name );
				}
			}
		}
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeConstReferences()
//=============================================================================
void SPELLwsDynamicData::loadConstReferences( PyObject* dictionary )
{
	// Load the number of possible const references
	unsigned int numReferences = m_storage->loadLong();

	if (numReferences > 0)
	{
		DEBUG("[DYN] " + ISTR(numReferences) + "  global references to load");
		for(unsigned int count = 0; count < numReferences; count++ )
		{
			PyObject* codeName = m_storage->loadObject();
			PyObject* co_consts = m_frame->f_code->co_consts;
			int numConsts = PyTuple_Size(co_consts);
			for( int index = 0; index < numConsts; index++ )
			{
				PyObject* item = PyTuple_GetItem(co_consts,index);
				// If the constant is a code object
				if ((item != NULL)&&(PyCode_Check(item)))
				{
					PyCodeObject* code = (PyCodeObject*) item;
					if (PYSTR(code->co_name) == PYSTR(codeName))
					{
						DEBUG("[DYN] Loading reference to " + PYREPR(codeName));
						PyDict_SetItem( dictionary, code->co_name, PyFunction_New( (PyObject*) code, m_frame->f_globals) );
					}
				}
			}
		}
	}
	else
	{
		DEBUG("[DYN] No global references to load");
	}
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeParameters()
//=============================================================================
void SPELLwsDynamicData::storeParameters()
{
	DEBUG("[DYN] Storing frame parameters");
	m_storage->storeLong(m_frame->f_lineno);
	DEBUG("   - Lineno     : " + ISTR(m_frame->f_lineno));
	m_storage->storeLong(m_frame->f_lasti);
	DEBUG("   - Instruction: " + ISTR(m_frame->f_lasti));
	m_storage->storeLong(m_frame->f_iblock);
	DEBUG("   - IBlocks    : " + ISTR(m_frame->f_iblock));
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadParameters()
//=============================================================================
void SPELLwsDynamicData::loadParameters()
{
	DEBUG("[DYN] Loading frame parameters");
	m_frame->f_lineno = m_storage->loadLong();
	DEBUG("   - Lineno     : " + ISTR(m_frame->f_lineno));
	m_frame->f_lasti  = m_storage->loadLong();
	// The last instruction pointer needs to be adjusted. Same as for fixState() algorithm

	SPELLlnotab lnotab(m_frame->f_code);

	if ((m_frame->f_lineno<0)||(m_frame->f_lasti<0))
	{
		THROW_EXCEPTION("Unable to restore frame dynamic data", "Cannot restore frame parameters", SPELL_ERROR_WSTART);
	}

	DEBUG("[DYN] Adjusting offset");
	int offsetForLine = lnotab.offset(m_frame->f_lineno);
	// If the current offset matches the first offset corresponding to the restored
	// line in the lnotab, we are in the main proc and NO function call. We need
	// to decrease the last instruction offset by 1 (see PyEval_EvalFrameEx)
	if (offsetForLine == m_frame->f_lasti)
	{
		DEBUG("[DYN] Corresponds to normal statement");
		m_frame->f_lasti = offsetForLine-1;
	}
	// Otherwise, we need to increase the offset by 2 (+3,-1) in order to position
	// the interpreter right after the CALL_FUNCTION opcode, in the POP_TOP opcode
	else
	{
		DEBUG("[DYN] Corresponds to function call");
		int nextLine = lnotab.lineAfter(m_frame->f_lineno);
		DEBUG("[DYN] Next line is " + ISTR(nextLine));
		int nextInstruction = lnotab.offset(nextLine);
		m_frame->f_lasti = nextInstruction -1; // Will position it in the lastLine but POP_TOP instr.
	}
	DEBUG("   - Instruction: " + ISTR(m_frame->f_lasti));
	m_frame->f_iblock = m_storage->loadLong();
	DEBUG("   - IBlocks    : " + ISTR(m_frame->f_iblock));
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeDictionary()
//=============================================================================
void SPELLwsDynamicData::storeDictionary( PyObject* dictionary )
{
	SPELLwsDictDataHandler handler(dictionary);
	handler.setStorage(m_storage);
	handler.write();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadDictionary()
//=============================================================================
PyObject* SPELLwsDynamicData::loadDictionary()
{
	SPELLwsDictDataHandler handler(NULL);
	handler.setStorage(m_storage);
	handler.read();
	return handler.getObject();
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::storeDTA()
//=============================================================================
void SPELLwsDynamicData::storeDTA( const std::string& container )
{
	DEBUG("[DYN] Store DTA container: " + container);

	PyObject* pyObj = PyDict_GetItemString(SPELLpythonHelper::instance().getMainDict(), container.c_str() );

	if (pyObj == NULL)
	{
		LOG_WARN("No DTA container to store: " + container);
		return;
	}

	SPELLdtaContainerObject* argsObj = reinterpret_cast<SPELLdtaContainerObject*>(pyObj);
	SPELLdtaContainer* args = argsObj->container;
	PyObject* keys = args->getKeys();
	unsigned int numKeys = PyList_Size(keys);

	m_storage->storeLong(numKeys);

	DEBUG("[DYN] " + ISTR(numKeys) + " items");

	for(unsigned int index=0; index < numKeys; index++)
	{
		PyObject* item = args->getValueEx(PyList_GetItem(keys,index));
		SPELLdtaVariableObject* varObj = reinterpret_cast<SPELLdtaVariableObject*>(item);
		SPELLdtaVariable* var = varObj->var;
		PyObject* value = var->getValueEx();
		bool confirmGet = var->getConfirmGet();
		std::vector<SPELLpyValue> expected = var->getExpected();
		std::vector<SPELLpyValue> range = var->getRange();
		std::string format = var->getFormat();
		std::string type = var->getType();

		DEBUG("     - key: " + var->getName());

		// 0. Key
		m_storage->storeObject( SSTRPY(var->getName()) );

		// 1. Value
		DEBUG("     - value: " + PYREPR(value));
		SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(value);
		handler->setStorage(m_storage);
		m_storage->storeLong(handler->getCode());
		handler->write();

		// 2. Format, Type and Confirm
		if (format.empty())
		{
			DEBUG("     - no format given");
			m_storage->storeObject( Py_None );
		}
		else
		{
			DEBUG("     - format: " + format);
			m_storage->storeObject( SSTRPY(format) );
		}
		if (type.empty())
		{
			DEBUG("     - no type given");
			m_storage->storeObject( Py_None );
		}
		else
		{
			DEBUG("     - type: " + type);
			m_storage->storeObject( SSTRPY(type) );
		}
		m_storage->storeObject( confirmGet ? Py_True : Py_False );

		// 3. Expected
		m_storage->storeLong(expected.size());
		for(std::vector<SPELLpyValue>::iterator it = expected.begin(); it != expected.end(); it++)
		{
			SPELLpyValue& val = *it;
			m_storage->storeObject(val.get());
		}

		// 4. Range
		m_storage->storeLong(range.size());
		for(std::vector<SPELLpyValue>::iterator it = range.begin(); it != range.end(); it++)
		{
			SPELLpyValue& val = *it;
			m_storage->storeObject(val.get());
		}
	}

	DEBUG("[DYN] Data container stored");
}

//=============================================================================
// METHOD    : SPELLwsDynamicData::loadDTA()
//=============================================================================
void SPELLwsDynamicData::loadDTA( const std::string& container )
{
	DEBUG("[DYN] Loading data container: " + container);

	PyObject* pyObj = PyDict_GetItemString(SPELLpythonHelper::instance().getMainDict(), container.c_str() );

	if (pyObj == NULL)
	{
		LOG_WARN("No such container: " + container + ", will not reload it");
		return;
	}

	SPELLdtaContainerObject* argsObj = reinterpret_cast<SPELLdtaContainerObject*>(pyObj);
	SPELLdtaContainer* args = argsObj->container;

	unsigned int numKeys = m_storage->loadLong();

	DEBUG("[DYN] Items to load: " + ISTR(numKeys));

	for(unsigned int index=0; index < numKeys; index++)
	{
		// 0. Key
		PyObject* pyName = m_storage->loadObject();

		DEBUG("     - key: " + PYREPR(pyName));

		// 1. Value
		SPELLwsData::Code code = (SPELLwsData::Code) m_storage->loadLong();
		SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(code);
		handler->setStorage(m_storage);
		handler->read();

		PyObject* value = handler->getObject();

		DEBUG("     - value: " + PYREPR(value));

		// 2. Format, Type and Confirm
		PyObject* pyFormat = m_storage->loadObject();
		DEBUG("     - format: " + PYREPR(pyFormat));
		PyObject* pyType = m_storage->loadObject();
		DEBUG("     - type: " + PYREPR(pyType));
		PyObject* pyConfirm = m_storage->loadObject();

		// 3. Expected
		PyObject* expected = NULL;
		unsigned int numExpected = m_storage->loadLong();
		if (numExpected>0) expected = PyList_New(numExpected);
		for(unsigned int count = 0; count < numExpected; count++)
		{
			PyObject* value = m_storage->loadObject();
			Py_INCREF(value);
			PyList_SetItem(expected, count, value);
		}

		// 4. Range
		PyObject* range= NULL;
		unsigned int numRange = m_storage->loadLong();
		if (numRange>0) range= PyList_New(numRange);
		for(unsigned int count = 0; count < numRange; count++)
		{
			PyObject* value = m_storage->loadObject();
			Py_INCREF(value);
			PyList_SetItem(range, count, value);
		}

		// Create the variable
		PyObject* varClass = SPELLpythonHelper::instance().getObject("libSPELL_DTA", "Var");
		Py_INCREF(varClass);
		assert(varClass != NULL);
		PyObject* dict = PyDict_New();
		Py_XINCREF(dict);
		// Reference count of key and value need to be increased
		// they are borrowed from the arguments tuple
		// see SPELLdtaContainerObjectMethods.c
		// BUT, PyDict_SetItem does this by itself.
		PyDict_SetItemString( dict, LanguageModifiers::Default.c_str(), value );
		if (pyType && pyType != Py_None)
		{
			PyDict_SetItemString( dict, LanguageModifiers::Type.c_str(), pyType );
		}
		if (pyFormat && pyFormat != Py_None)
		{
			PyDict_SetItemString( dict, LanguageModifiers::ValueFormat.c_str(), pyFormat );
		}
		if (pyConfirm && pyConfirm != Py_None)
		{
			PyDict_SetItemString( dict, LanguageModifiers::Confirm.c_str(), pyConfirm );
		}
		if (expected)
		{
			PyDict_SetItemString( dict, LanguageModifiers::Expected.c_str(), expected );
		}
		if (range)
		{
			PyDict_SetItemString( dict, LanguageModifiers::Range.c_str(), range );
		}
		PyObject* instance = SPELLpythonHelper::instance().newInstance(varClass, NULL, dict);
		Py_XINCREF(instance);
		args->setValue(pyName,instance);
	}

	DEBUG("[DYN] Data container loaded");

}
