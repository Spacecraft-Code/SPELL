// ################################################################################
// FILE       : SPELLdtaContainerObjectMethods.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of data container
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Local includes ----------------------------------------------------------
#include "SPELL_DTA/SPELLdtaContainerObject.H"
#include "SPELL_DTA/SPELLdtaVariableObject.H"
#include "SPELL_DTA/SPELLdtaContainer.H"
// System includes ---------------------------------------------------------

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////


//============================================================================
// FUNCTION        : SPELLdtaContainerObject_Init
//============================================================================
int SPELLdtaContainerObject_Init( SPELLdtaContainerObject* self, PyObject* args, PyObject* kwds )
{
	// All the information is held by the dta container. These
	// Python bindings are just the facade.

	if (args != NULL && PyTuple_Size(args)==1)
	{
		std::string name = PYSSTR( PyTuple_GetItem(args,0) );
		self->container = new SPELLdtaContainer( name );
	}
	else
	{
		self->container = new SPELLdtaContainer();
	}
    return 0;
}

//============================================================================
// FUNCTION        : SPELLdtaContainerObject_Dealloc
//============================================================================
void SPELLdtaContainerObject_Dealloc( SPELLdtaContainerObject* self )
{
	delete self->container;
    self->ob_type->tp_free( (PyObject*) self );
}

//============================================================================
// FUNCTION        : SPELLdtaContainerObject_New
//============================================================================
PyObject* SPELLdtaContainerObject_New( PyTypeObject* type, PyObject* args, PyObject* kwds )
{
    //NOTE:  Py_INCREF is already called!
    SPELLdtaContainerObject* self;
    self = (SPELLdtaContainerObject*) type->tp_alloc(type,0);
    return (PyObject*)self;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Set
//============================================================================
PyObject* SPELLdtaContainerObject_Set( PyObject* self, PyObject* args )
{
	// Set a value into the container
	DEBUG("[DTAPY] Set " + PYREPR(args));

	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	if (PyTuple_Size(args)!=2)
	{
		THROW_SYNTAX_EXCEPTION("Cannot set data item", "Expected a key name and a value");
	}
	PyObject* key = PyTuple_GetItem(args,0);
	PyObject* value = PyTuple_GetItem(args,1);

	try
	{
		dself->container->setValue(key,value);
	}
	catch(SPELLcoreException& ex)
	{
		THROW_DRIVER_EXCEPTION(ex.getError(), ex.getReason());
	}
	DEBUG("[DTAPY] Set OK");
	Py_RETURN_NONE;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Get
//============================================================================
PyObject* SPELLdtaContainerObject_Get( PyObject* self, PyObject* args )
{
	DEBUG("[DTAPY] Get " + PYREPR(args));
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);

	PyObject* key = PyTuple_GetItem(args,0);
	PyObject* result = NULL;

	try
	{
		result = dself->container->getValue(key);
	}
	catch(SPELLcoreException& ex)
	{
		THROW_DRIVER_EXCEPTION(ex.getError(), ex.getReason());
	}
	DEBUG("[DTAPY] Got " + PYREPR(result));
	return result;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Keys
//============================================================================
PyObject* SPELLdtaContainerObject_Keys( PyObject* self, PyObject* args )
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	return dself->container->getKeys();
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Values
//============================================================================
PyObject* SPELLdtaContainerObject_Values( PyObject* self, PyObject* args )
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	return PyDict_Values(dself->container->getDict());
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Repr
//============================================================================
PyObject* SPELLdtaContainerObject_Repr( PyObject* self )
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	return PyObject_Str(dself->container->getDict());
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Str
//============================================================================
PyObject* SPELLdtaContainerObject_Str( PyObject* self )
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	return PyObject_Repr(dself->container->getDict());
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DictLength
//============================================================================
Py_ssize_t SPELLdtaContainerObject_DictLength( PyObject* self )
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	return PyDict_Size(dself->container->getDict());
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DictSubscript
//============================================================================
PyObject* SPELLdtaContainerObject_DictSubscript( PyObject* self, PyObject* key )
{
	DEBUG("[DTAPY] DictSubscript " + PYREPR(key));
	PyObject* args = NULL;
	try
	{
		SPELLpythonHelper::instance().checkError();
		if ((!PyString_Check(key))&&(!PyLong_Check(key))&&(!PyInt_Check(key))&&(!PyFloat_Check(key)))
		{
			THROW_RUNTIME_EXCEPTION( "Cannot get item", "Key must be a string or number");
		}
		DEBUG("[DTAPY] Create args " + PYREPR(key));
		args = PyTuple_New(1);
		PyTuple_SetItem(args,0,key);
		Py_INCREF(key);
		Py_INCREF(args);
		SPELLpythonHelper::instance().checkError();
		DEBUG("[DTAPY] Calling get " + PYREPR(args));
	}
	catch(SPELLcoreException& ex)
	{
		THROW_RUNTIME_EXCEPTION( "Cannot get item", ex.what());
	}
	return SPELLdtaContainerObject_Get(self,args);
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DictAssSub
//============================================================================
int SPELLdtaContainerObject_DictAssSub( PyObject* self, PyObject* key, PyObject* value )
{
	DEBUG("[DTAPY] DictAsssub " + PYREPR(key) + "," + PYREPR(value));
	PyObject* args = NULL;
	try
	{
		if ((!PyString_Check(key))&&(!PyLong_Check(key))&&(!PyInt_Check(key))&&(!PyFloat_Check(key)))
		{
			THROW_RUNTIME_EXCEPTION_NR( "Cannot set item", "Key must be a string or number");
			return -1;
		}
		args = PyTuple_New(2);
		PyTuple_SetItem(args,0, key);
		PyTuple_SetItem(args,1,value);
		Py_INCREF(key);
		Py_INCREF(value);
		Py_INCREF(args);
		DEBUG("[DTAPY] Calling set " + PYREPR(args));
		SPELLpythonHelper::instance().checkError();
	}
	catch(SPELLcoreException& ex)
	{
		THROW_RUNTIME_EXCEPTION_NR( "Cannot set item", ex.what());
	}
	if (SPELLdtaContainerObject_Set(self,args) == NULL) return -1;
	return 0;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_HasKey
//============================================================================
PyObject* SPELLdtaContainerObject_HasKey( PyObject* self, PyObject* args )
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	if (PyTuple_Size(args)!=1)
	{
		THROW_SYNTAX_EXCEPTION("Cannot check key existence", "Expected a dictionary key");
	}
	PyObject* key = PyTuple_GetItem(args,0);
	if ((!PyString_Check(key))&&(!PyLong_Check(key))&&(!PyInt_Check(key))&&(!PyFloat_Check(key)))
	{
		THROW_SYNTAX_EXCEPTION( "Cannot check key", "Key must be a string or number" );
	}
	if (dself->container->hasKey(key))
	{
		Py_RETURN_TRUE;
	}
	Py_RETURN_FALSE;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_GetAttrRO
//============================================================================
PyObject * SPELLdtaContainerObject_GetAttrRO(PyObject* self, PyObject* key)
{
	return PyObject_GenericGetAttr(self,key);
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_SetAttrRO
//============================================================================
int SPELLdtaContainerObject_SetAttrRO(PyObject* self, PyObject* key, PyObject* value )
{
	return PyObject_GenericSetAttr(self,key,value);
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_GetAttr
//============================================================================
PyObject * SPELLdtaContainerObject_GetAttr(PyObject* self, char* key )
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	return PyDict_GetItemString(dself->container->getDict(), key);
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_SetAttr
//============================================================================
int SPELLdtaContainerObject_SetAttr(PyObject* self, char* key, PyObject* value )
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(self);
	return PyDict_SetItemString(dself->container->getDict(), key, value);
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Traverse
//============================================================================
int SPELLdtaContainerObject_Traverse(PyObject* container, visitproc visit, void* arg)
{
	Py_ssize_t i = 0;
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(container);
	PyObject *pk;
	PyObject *pv;

	while (PyDict_Next(dself->container->getDict(), &i, &pk, &pv))
	{
		Py_VISIT(pk);
		Py_VISIT(pv);
	}
	return 0;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Clear
//============================================================================
int SPELLdtaContainerObject_Clear(PyObject* container)
{
	SPELLdtaContainerObject* dself = reinterpret_cast<SPELLdtaContainerObject*>(container);
	PyDict_Clear(dself->container->getDict());
	return 0;
}

static PyMethodDef dictiter_methods[] = {
        {"__length_hint__", (PyCFunction)SPELLdtaContainerObject_IterLen, METH_NOARGS, "container size"},
        {NULL,          NULL}           /* sentinel */
};

PyTypeObject SPELLdtaContainerIterator_Type =
{
        PyObject_HEAD_INIT(&PyType_Type)
        0,                                      /* ob_size */
        "container-keyiterator",                /* tp_name */
        sizeof(SPELLdtaContainerIteratorObject),/* tp_basicsize */
        0,                                      /* tp_itemsize */
        /* methods */
        (destructor)SPELLdtaContainerObject_DeallocIterator, /* tp_dealloc */
        0,                                      /* tp_print */
        0,                                      /* tp_getattr */
        0,                                      /* tp_setattr */
        0,                                      /* tp_compare */
        0,                                      /* tp_repr */
        0,                                      /* tp_as_number */
        0,                                      /* tp_as_sequence */
        0,                                      /* tp_as_mapping */
        0,                                      /* tp_hash */
        0,                                      /* tp_call */
        0,                                      /* tp_str */
        PyObject_GenericGetAttr,                /* tp_getattro */
        0,                                      /* tp_setattro */
        0,                                      /* tp_as_buffer */
        Py_TPFLAGS_DEFAULT,                     /* tp_flags */
        0,                                      /* tp_doc */
        0,                                      /* tp_traverse */
        0,                                      /* tp_clear */
        0,                                      /* tp_richcompare */
        0,                                      /* tp_weaklistoffset */
        PyObject_SelfIter,                      /* tp_iter */
        (iternextfunc)SPELLdtaContainerObject_NextKey, /* tp_iternext */
        dictiter_methods,                       /* tp_methods */
        0,
};

//============================================================================
//============================================================================
PyObject* SPELLdtaContainerObject_NewIterator( SPELLdtaContainerObject* container )
{
    SPELLdtaContainerIteratorObject* di = PyObject_New( SPELLdtaContainerIteratorObject, &SPELLdtaContainerIterator_Type);
    if (di == NULL) return NULL;
    Py_XINCREF(di);
    PyDictObject* dict = reinterpret_cast<PyDictObject*>(container->container->getDict());
    di->di_dict   = dict;
    di->di_used   = dict->ma_used;
    di->di_pos    = 0;
    di->len       = dict->ma_used;
	di->di_result = NULL;
    return (PyObject *)di;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DeallocIterator
//============================================================================
void SPELLdtaContainerObject_DeallocIterator( SPELLdtaContainerIteratorObject* di )
{
	di->ob_type->tp_free( (PyObject*)di );
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_NextKey
//============================================================================
PyObject* SPELLdtaContainerObject_NextKey( SPELLdtaContainerIteratorObject* di )
{
    PyObject *key;
    register Py_ssize_t i, mask;
    register PyDictEntry *ep;

    DEBUG("[DTAPY] Next iterator key on " + PYCREPR(di));

    PyDictObject* d = di->di_dict;

    DEBUG("[DTAPY] Dictionary: " + PYCREPR(d));

    if (d == NULL) return NULL;

    if (di->di_used != d->ma_used)
    {
		PyErr_SetString(PyExc_RuntimeError, "dictionary changed size during iteration");
		di->di_used = -1; /* Make this state sticky */
		LOG_ERROR("Dictionary changed size during iteration!!!");
		return NULL;
    }

    i = di->di_pos;
    if (i < 0) goto fail;
    ep = d->ma_table;
    mask = d->ma_mask;
    while (i <= mask && ep[i].me_value == NULL) i++;
    di->di_pos = i+1;
    if (i > mask) goto fail;
    di->len--;
    key = ep[i].me_key;
    if (key == NULL) goto fail;
    Py_INCREF(key);
    return key;
fail:
    DEBUG("[DTAPY] No key found to iterate on");
    di->di_dict = NULL;
    PyErr_Clear();
    return NULL;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_Iter
//============================================================================
PyObject* SPELLdtaContainerObject_Iter( SPELLdtaContainerObject* container )
{
	return SPELLdtaContainerObject_NewIterator(container);
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_IterLen
//============================================================================
PyObject* SPELLdtaContainerObject_IterLen( SPELLdtaContainerIteratorObject* di )
{
	Py_ssize_t len = 0;
	if (di->di_dict != NULL && di->di_used == di->di_dict->ma_used)
	{
		len = di->len;
	}
	return PyInt_FromSize_t(len);
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_EnableNotifications
//============================================================================
PyObject* SPELLdtaContainerObject_EnableNotifications( PyObject* self, PyObject* args )
{
	SPELLdtaContainerObject* container = reinterpret_cast<SPELLdtaContainerObject*>(self);
	container->container->setNotificationsEnabled(true);
	Py_RETURN_NONE;
}

//============================================================================
// FUNCTION       : SPELLdtaContainerObject_DisableNotifications
//============================================================================
PyObject* SPELLdtaContainerObject_DisableNotifications( PyObject* self, PyObject* args )
{
	SPELLdtaContainerObject* container = reinterpret_cast<SPELLdtaContainerObject*>(self);
	container->container->setNotificationsEnabled(false);
	Py_RETURN_NONE;
}
