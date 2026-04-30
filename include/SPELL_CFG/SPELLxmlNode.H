// ################################################################################
// FILE       : SPELLxmlNode.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Wrapper for XML configuration node
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

#ifndef __SPELL_XMLNODE_H__
#define __SPELL_XMLNODE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_CFG */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
/** Represents and XML attribute. */
typedef struct SPELLxmlAttribute_
{
    std::string name;
    std::string value;

}
SPELLxmlAttribute;
/** Represents a list of XML attributes. */
typedef std::list<SPELLxmlAttribute*> SPELLxmlAttrList;

// DEFINES /////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
/**
 * \brief Implementation of an XML node.
 *
 * \par Description and usage:
 *
 * 		Used by the XML readers to build a generic, implementation-independent
 * 		tree of XML information.
 */
//////////////////////////////////////////////////////////////////////////////
class SPELLxmlNode
{
public: //--------------------------------------------------------------------
    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param name IN: node name.
		**////////////////////////////////////////////////////////////////////
		SPELLxmlNode( std::string name )
		{
			m_name = name;
			m_value = "";
		};

		//////////////////////////////////////////////////////////////////////
		/** Destructor
		**////////////////////////////////////////////////////////////////////
		~SPELLxmlNode()
		{
			m_attributes.clear();
			m_children.clear();
		}

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Provide the node name.
		 *
		 * \returns The XML node name.
		**////////////////////////////////////////////////////////////////////
		std::string    getName() const {
			return m_name;
		};

		//////////////////////////////////////////////////////////////////////
		/** Set the node value.
		 *
		 * \param value IN: value of the node.
		**////////////////////////////////////////////////////////////////////
		void setValue( std::string value ) {
			m_value = value;
		};

		//////////////////////////////////////////////////////////////////////
		/** Obtain the node value.
		 *
		 * \returns The value of the node.
		**////////////////////////////////////////////////////////////////////
		std::string getValue() const {
			return m_value;
		};

		//////////////////////////////////////////////////////////////////////
		/** Get the number of child nodes.
		 *
		 * \returns The number of child nodes, 0 if this is a leaf node.
		**////////////////////////////////////////////////////////////////////
		int numChildren() const {
			return m_children.size();
		};

		//////////////////////////////////////////////////////////////////////
		/** Check if the node has children.
		 *
		 * \returns True if the node has at least one child.
		**////////////////////////////////////////////////////////////////////
		bool hasChildren() const {
			return (m_children.size()>0);
		};

		//////////////////////////////////////////////////////////////////////
		/** Get all child nodes.
		 *
		 * \returns A list of all the child nodes.
		**////////////////////////////////////////////////////////////////////
		std::list<SPELLxmlNode*>& getChildren() {
			return m_children;
		};

		//////////////////////////////////////////////////////////////////////
		/** Add a child node to this one.
		 *
		 * \param node IN: child node to be added.
		**////////////////////////////////////////////////////////////////////
		void addChild( SPELLxmlNode* node ) {
			m_children.push_back(node);
		};

		//////////////////////////////////////////////////////////////////////
		/** Get the number of defined attributes for this node.
		 *
		 * \returns The number of defined attributes.
		**////////////////////////////////////////////////////////////////////
		int numAttributes() const {
			return m_attributes.size();
		};

		//////////////////////////////////////////////////////////////////////
		/** Check if this node has attributes.
		 *
		 * \returns True if the node has defined attributes.
		**////////////////////////////////////////////////////////////////////
		bool hasAttributes() const {
			return (m_attributes.size()>0);
		};

		//////////////////////////////////////////////////////////////////////
		/** Get the node attributes.
		 *
		 * \returns A list of all the defined attributes.
		**////////////////////////////////////////////////////////////////////
		std::list<SPELLxmlAttribute*>&    getAttributes()    {
			return m_attributes;
		};

		//////////////////////////////////////////////////////////////////////
		/** Add an attribute to this node.
		 *
		 * \param name IN: attribute name.
		 * \param value IN: attribute value.
		**////////////////////////////////////////////////////////////////////
		void addAttribute( std::string name, std::string value )
		{
			SPELLxmlAttribute* attr = new SPELLxmlAttribute();
			attr->name = name;
			attr->value = value;
			m_attributes.push_back(attr);
		}

		//////////////////////////////////////////////////////////////////////
		/** Obtain the value of a given attribute.
		 *
		 * \param name IN: attribute name.
		 *
		 * \returns The attribute value, or the empty string if there is no
		 * such attribute defined.
		**////////////////////////////////////////////////////////////////////
		std::string getAttributeValue( std::string name )
		{
			std::list<SPELLxmlAttribute*>::iterator it;
			for(it = m_attributes.begin(); it != m_attributes.end(); it++)
			{
				if ((*it)->name == name ) return (*it)->value;
			}
			return "";
		}

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
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the list of defined attributes */
		std::list<SPELLxmlAttribute*>    m_attributes;
		/** Holds the list of child nodes. */
		std::list<SPELLxmlNode*>         m_children;
		/** Holds the node name */
		std::string                      m_name;
		/** Holds the node value */
		std::string                      m_value;
};
/** Represents a list of XML nodes */
typedef std::list<SPELLxmlNode*> SPELLxmlNodeList;

/** Used to cleanup a node list from memory */
inline void dealloc_list( SPELLxmlNodeList& list)
{
    // Release memory allocated for the node list
    SPELLxmlNodeList::iterator it;
    SPELLxmlNodeList::iterator end = list.end();
    for( it = list.begin(); it != end; ++it )
    {
        delete (*it);
    }
    list.clear();
}

/*@}*/
#endif
