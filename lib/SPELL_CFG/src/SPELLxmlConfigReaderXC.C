// ################################################################################
// FILE       : SPELLxmlConfigReaderXC.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the configuration reader with Xerces-C
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_CFG/SPELLxmlConfigReader.H"
#include "SPELL_CFG/SPELLxmlConfigReaderXC.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
// System includes ---------------------------------------------------------
#include <xercesc/dom/DOM.hpp>
#include <xercesc/dom/DOMDocumentType.hpp>
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMImplementation.hpp>
#include <xercesc/dom/DOMImplementationLS.hpp>
#include <xercesc/dom/DOMNodeIterator.hpp>
#include <xercesc/dom/DOMNodeList.hpp>
#include <xercesc/dom/DOMText.hpp>

#include <xercesc/util/XMLUni.hpp>

using namespace xercesc;

//=============================================================================
// CONSTRUCTOR    : SPELLxmlConfigReaderXC::SPELLxmlConfigReaderXC
//=============================================================================
SPELLxmlConfigReaderXC::SPELLxmlConfigReaderXC()
{
    m_parser = NULL;
    m_document = NULL;
    try
    {
        XMLPlatformUtils::Initialize();

        m_parser = new XercesDOMParser();
        m_parser->setDoNamespaces(false);
        m_parser->setValidationScheme( XercesDOMParser::Val_Never );
        m_parser->setDoSchema( false );
        m_parser->setLoadExternalDTD( false );
    }
    catch(const XMLException& ex)
    {
        delete m_parser;
        m_parser = NULL;
        char* message = XMLString::transcode(ex.getMessage());
        THROW_EXCEPTION("Error during initialization", message, SPELL_ERROR_CONFIG);
    }
}

//=============================================================================
// DESTRUCTOR : SPELLxmlConfigReaderXC::~SPELLxmlConfigReaderXC
//=============================================================================
SPELLxmlConfigReaderXC::~SPELLxmlConfigReaderXC()
{
    delete m_parser;
    m_parser = NULL;
    m_document->release();
    m_document = NULL;
    try
    {
        // Terminate Xerces
        XMLPlatformUtils::Terminate();
    }
    catch( xercesc::XMLException& e )
    {
        std::string message = xercesc::XMLString::transcode( e.getMessage() );
        THROW_EXCEPTION("Error during cleanup", message, SPELL_ERROR_CONFIG);
    }
    // There is no need to free m_document, will be freed by the parser
}

//=============================================================================
// METHOD: SPELLxmlConfigReaderXC::parseFile
//=============================================================================
bool SPELLxmlConfigReaderXC::parseFile( std::string xmlFile )
{
    struct stat fileStatus;

    if (m_parser == NULL ) THROW_EXCEPTION("Unable to parse file", "No parser available", SPELL_ERROR_CONFIG);

    if (stat(xmlFile.c_str(), &fileStatus)==-1)
    {
        if( errno == ENOENT )
        {
            THROW_EXCEPTION("Cannot parse file " + xmlFile, "File name does not exist, or path is an empty string.", SPELL_ERROR_FILESYSTEM );
        }
        else if( errno == ENOTDIR )
        {
            THROW_EXCEPTION("Cannot parse file " + xmlFile, "A component of the path is not a directory.", SPELL_ERROR_FILESYSTEM );
        }
        else if( errno == ELOOP )
        {
            THROW_EXCEPTION("Cannot parse file " + xmlFile, "Too many symbolic links", SPELL_ERROR_FILESYSTEM );
        }
        else if( errno == EACCES )
        {
            THROW_EXCEPTION("Cannot parse file " + xmlFile, "Permission denied", SPELL_ERROR_FILESYSTEM );
        }
        else if( errno == ENAMETOOLONG )
        {
            THROW_EXCEPTION("Cannot parse file " + xmlFile, "File cannot be read", SPELL_ERROR_FILESYSTEM );
        }
    }

    bool result = true;
    try
    {
        const char* theFile = xmlFile.c_str();
        m_parser->parse(theFile);
        m_document = m_parser->getDocument();
    }
    catch (const XMLException& ex)
    {
        std::string message = XMLString::transcode(ex.getMessage());
        THROW_EXCEPTION("XML error while parsing " + xmlFile, message, SPELL_ERROR_CONFIG );
    }
    catch (const DOMException& ex)
    {
        std::string message = XMLString::transcode(ex.getMessage());
        THROW_EXCEPTION("DOM error while parsing " + xmlFile, message, SPELL_ERROR_CONFIG );
    }
    catch (...)
    {
        THROW_EXCEPTION("Unexpected exception while parsing " + xmlFile, "<?>", SPELL_ERROR_CONFIG );
    }
    return result;
}

//=============================================================================
// METHOD: SPELLxmlConfigReaderXC::getRoot
//=============================================================================
SPELLxmlNode* SPELLxmlConfigReaderXC::getRoot()
{
    if (m_document == NULL)
    {
        THROW_EXCEPTION("Cannot find root", "No document available", SPELL_ERROR_CONFIG);
    }

    // Get the top-level element
    DOMElement* elementRoot = m_document->getDocumentElement();
    SPELLxmlNode* root = convertToNode( elementRoot );
    return root;
}

//=============================================================================
// METHOD: SPELLxmlConfigReaderXC::findElementsByName
//=============================================================================
SPELLxmlNodeList SPELLxmlConfigReaderXC::findElementsByName( std::string tagName )
{
    SPELLxmlNodeList result;
    if (m_document == NULL)
    {
        THROW_EXCEPTION("Cannot find elements", "No document available", SPELL_ERROR_CONFIG);
    }

    DEBUG("[CFGR] Get elements by name: " + tagName);

    // Get the tag translation
    XMLCh* tag = XMLString::transcode(tagName.c_str());

    // Get the top-level element
    DOMElement* elementRoot = m_document->getDocumentElement();
    if( !elementRoot )
	{
    	THROW_EXCEPTION("Cannot get root element", "Empty document", SPELL_ERROR_CONFIG);
	}

    DOMNodeList* children = elementRoot->getChildNodes();

    const XMLSize_t nodeCount = children->getLength();

    // For all nodes, children of "root" in the XML tree.
    for (XMLSize_t xx = 0; xx < nodeCount; ++xx)
    {
        DOMNode* currentNode = children->item(xx);
        if (currentNode->getNodeType() && // true is not NULL
                currentNode->getNodeType() == DOMNode::ELEMENT_NODE) // is element
        {
            // Found node which is an Element. Re-cast node as element
            DOMElement* currentElement = dynamic_cast<xercesc::DOMElement*> (currentNode);
            if (XMLString::equals(currentElement->getTagName(), tag))
            {
                result.push_back( convertToNode( currentElement ) );
            }
        }
    }

    XMLString::release(&tag);

    DEBUG("[CFGR] Found " + ISTR(result.size()) + " elements");

    return result;
}

//=============================================================================
// METHOD: SPELLxmlConfigReaderXC::convertToNode
//=============================================================================
SPELLxmlNode* SPELLxmlConfigReaderXC::convertToNode( DOMElement* element )
{
    // Create an abstract node with this name
    SPELLxmlNode* node = new SPELLxmlNode( XMLString::transcode(element->getNodeName()) );

    // Get any possible attributes
    DOMNamedNodeMap* attrs = element->getAttributes();
    XMLSize_t numAttrs = attrs->getLength();
    for( XMLSize_t idx = 0; idx < numAttrs; idx++)
    {
        // Get the attribute node
        DOMNode* attrNode = attrs->item(idx);
        // Get name and value
        const XMLCh* aname  = attrNode->getNodeName();
        const XMLCh* avalue = attrNode->getNodeValue();
        // Convert name and value to strings
        std::string name = "<?>";
        if (aname != NULL)
        {
            name = XMLString::transcode(aname);
        }
        std::string value = "<?>";
        if (avalue != NULL)
        {
            value = XMLString::transcode(avalue);
        }
        node->addAttribute( name, value );
    }

    // Get any possible children
    DOMNodeList* children = element->getChildNodes();
    XMLSize_t numChildren = children->getLength();
    for( XMLSize_t idx = 0; idx < numChildren; idx++)
    {
        // Get the children node
        DOMNode* childNode = children->item(idx);
        // Process only ELEMENTs and TEXTs
        if (childNode->getNodeType() && // true is not NULL
                childNode->getNodeType() == DOMNode::ELEMENT_NODE) // is element
        {
            // For elements, recursively add children
            SPELLxmlNode* child = convertToNode( dynamic_cast<xercesc::DOMElement*>(childNode) );
            node->addChild(child);
        }
        else if (childNode->getNodeType() == DOMNode::TEXT_NODE)
        {
            // For text values, add the value. This code will just ignore
            // carriage-return values
            const XMLCh* nvalue = childNode->getNodeValue();
            if (nvalue != NULL)
            {
                std::string thevalue = XMLString::transcode(nvalue);
                SPELLutils::trim(thevalue, " \n\r\t");
                node->setValue( thevalue );
            }
        }
    }

    return node;
}
