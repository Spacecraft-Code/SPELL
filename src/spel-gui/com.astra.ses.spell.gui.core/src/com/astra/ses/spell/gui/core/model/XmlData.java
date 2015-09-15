///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model
// 
// FILE      : XmlData.java
//
// DATE      : 2008-11-21 08:58
//
// Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.model;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.astra.ses.spell.gui.core.comm.messages.XmlException;

/*******************************************************************************
 * @brief Helper class for parsing XML data.
 * @date 18/09/07
 ******************************************************************************/
public class XmlData extends PropertySet
{
	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the name of the DOM root node */
	private String	m_root;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param data
	 *            The XML source data string
	 * @throws XmlException
	 *             if there is an error on the XML data
	 **************************************************************************/
	public XmlData(String data) throws XmlException
	{
		super();
		m_root = null;
		if (data != null)
		{
			setXML(data);
		}
	}

	/***************************************************************************
	 * Assign the XML data of this entity
	 * 
	 * @param data
	 *            String containing XML code.
	 * @throws XmlException
	 *             if there is an error on the XML data
	 **************************************************************************/
	public void setXML(String data) throws XmlException
	{
		// Create the document builder factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document doc = null;
		try
		{
			// Create a new builder and use it to parse the input string
			db = dbf.newDocumentBuilder();
			doc = db.parse(new InputSource(new StringReader(data)));
		}
		catch (Exception e)
		{
			System.err.println(data);
			throw new XmlException(e.getLocalizedMessage());
		}
		// Get the root element
		Element docElement = doc.getDocumentElement();
		// Set the root element name
		m_root = docElement.getTagName();
		// Obtain all property nodes
		NodeList nl = docElement.getElementsByTagName("property");
		// If we found at least one property:
		if (nl != null && nl.getLength() > 0)
		{
			m_properties.clear();
			// Read the property name and value
			for (int count = 0; count < nl.getLength(); count++)
			{
				Element element = (Element) nl.item(count);
				String name = element.getAttribute("name");
				String value = element.getTextContent();
				m_properties.put(name, value);
			}
		}
	}

	/***************************************************************************
	 * Obtain the XML data string of this entity.
	 * 
	 * @return the XML data string
	 * @throws XmlException
	 *             if there are no properties defined or there is no root name
	 **************************************************************************/
	public String getXML() throws XmlException
	{
		// Build the XML code
		String xml = "<?xml version=\"1.0\"?>\n";
		// Root is the DOM root node
		xml += "<" + m_root + ">\n";
		// We need at least one property
		if (m_properties.size() == 0 || m_root == null) { throw new XmlException(
		        "Malformed message: no root or properties"); }
		// Fill the property nodes
		for (String name : m_properties.keySet())
		{
			String value = m_properties.get(name);
			if (value == null) value = " ";
			xml += "    <property name=\"" + name + "\">" + adapt(value)
			        + "</property>\n";
		}
		xml += "</" + m_root + ">\n";
		return xml;
	}

	/***************************************************************************
	 * Assign an XML property field
	 * 
	 * @param name
	 *            Property name
	 * @param value
	 *            Property value
	 **************************************************************************/
	public void set(String name, String value)
	{
		if (value != null)
		{
			value = adapt(value);
		}
		super.setProperty(name, value);
	}

	/***************************************************************************
	 * Obtain the value of an XML property field
	 * 
	 * @param name
	 *            Name of the property
	 * @return The value of the property, null if it does not exist
	 **************************************************************************/
	public String get(String name)
	{
		String data = super.getProperty(name);
		if (data != null)
		{
			data = radapt(data);
		}
		return data;
	}

	/***************************************************************************
	 * Check wether the entity contains the given property or not
	 * 
	 * @param property
	 *            Property name
	 * @return True if the property is present
	 **************************************************************************/
	public boolean contains(String property)
	{
		return m_properties.containsKey(property);
	}

	/***************************************************************************
	 * Obtain the name of the DOM root node
	 * 
	 * @return The name of the DOM root node
	 **************************************************************************/
	public String getRoot()
	{
		return m_root;
	}

	/***************************************************************************
	 * Assign the name of the DOM root node
	 * 
	 * @param root
	 *            The name of the node
	 **************************************************************************/
	public void setRoot(String root)
	{
		m_root = root;
	}

	protected String adapt(String value)
	{
		value = value.replaceAll("'", "\"");
		value = value.replaceAll("&", "&amp");
		value = value.replaceAll("<", "&lt;");
		value = value.replaceAll(">", "&gt;");
		return value;
	}

	protected String radapt(String value)
	{
		value = value.replaceAll("&amp;", "&");
		value = value.replaceAll("&lt;", "<");
		value = value.replaceAll("&gt;", ">");
		value = value.replaceAll("&apos;", "'");
		return value;
	}
}
