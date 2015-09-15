///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessage.java
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
package com.astra.ses.spell.gui.core.comm.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import com.astra.ses.spell.gui.core.interfaces.IMessageField;

/*******************************************************************************
 * @brief Helper class for processing XML messages exchanged with SPELL server
 * @date 18/09/07
 ******************************************************************************/
public class SPELLmessage
{
	protected static final String LIST_SEPARATOR = "\3";
	
	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the XML data parsing class */
	private TreeMap<String, String>	m_data;
	/** Holds the compressed flag */
	private boolean m_compressed;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Message constructor
	 **************************************************************************/
	public SPELLmessage(TreeMap<String, String> data)
	{
		m_data = data;
		m_compressed = false;
	}

	/***************************************************************************
	 * Empty message constructor
	 **************************************************************************/
	public SPELLmessage()
	{
		m_data = new TreeMap<String, String>();
		m_compressed = false;
	}

	/***************************************************************************
	 * Copy message constructor
	 **************************************************************************/
	public SPELLmessage( SPELLmessage other )
	{
		m_data = other.m_data;
		m_compressed = other.m_compressed;
	}

	/***************************************************************************
	 * Get the list of defined keys
	 **************************************************************************/
	public Set<String> getKeys()
	{
		return m_data.keySet();
	}
	
	/***************************************************************************
	 * Obtain the XML message data string for debugging, not IPC
	 **************************************************************************/
	public String dataStr()
	{
		String result = "";
		for (String key : m_data.keySet())
		{
			result += key + "," + m_data.get(key) + ",";
		}
		return result;
	}

	/***************************************************************************
	 * Obtain the type of the message. The type field specifies if the message
	 * is a SPELL command, a data request, a failure information, or other
	 * types. The type field is the root node of the DOM document as well.
	 * 
	 * @return The data type.
	 **************************************************************************/
	public String getType()
	{
		return m_data.get("root");
	}

	/***************************************************************************
	 * Set the message type.
	 * 
	 * @param type
	 *            The message type string.
	 **************************************************************************/
	public void setType(String type)
	{
		m_data.put("root", type);
	}

	/***************************************************************************
	 * Obtain the message identifier. The message identifier can be seen as the
	 * message subtype. For example, allows distinguishing between different
	 * subtypes of commands or requests.
	 * 
	 * @return The message identifier.
	 **************************************************************************/
	public String getId()
	{
		return m_data.get(IMessageField.FIELD_ID);
	}

	/***************************************************************************
	 * Get the message compressed flag
	 **************************************************************************/
	public boolean isCompressed()
	{
		return m_compressed;
	}

	/***************************************************************************
	 * Set the message compressed flag
	 **************************************************************************/
	public void setCompressed( boolean compressed )
	{
		m_compressed = compressed;
	}

	/***************************************************************************
	 * Assign the message identifier of this message.
	 * 
	 * @param id
	 *            The message identifier.
	 **************************************************************************/
	public void setId(String id)
	{
		m_data.put(IMessageField.FIELD_ID, id);
	}

	/***************************************************************************
	 * Assign the sequence number of this message.
	 **************************************************************************/
	public void setSequence(long seq)
	{
		m_data.put(IMessageField.FIELD_SEQUENCE, Long.toString(seq));
	}

	/***************************************************************************
	 * Get the sequence number of this message.
	 **************************************************************************/
	public long getSequence()
	{
		String seq = m_data.get(IMessageField.FIELD_SEQUENCE);
		if (seq == null) return -1;
		return Long.parseLong(seq);
	}

	/***************************************************************************
	 * Obtain the key of the source peer of this message. Each peer should have
	 * an unique key which identifies it.
	 * 
	 * @return The source peer key.
	 **************************************************************************/
	public String getKey()
	{
		return m_data.get(IMessageField.FIELD_IPC_KEY);
	}

	/***************************************************************************
	 * Obtain the identifier of the sender of this message.
	 * 
	 * @return The sender id
	 **************************************************************************/
	public String getSender()
	{
		return m_data.get(IMessageField.FIELD_SENDER_ID);
	}

	/***************************************************************************
	 * Obtain the identifier of the receiver of this message.
	 * 
	 * @return The receiver id
	 **************************************************************************/
	public String getReceiver()
	{
		return m_data.get(IMessageField.FIELD_RECEIVER_ID);
	}

	/***************************************************************************
	 * Assign the source peer key of this message.
	 * 
	 * @param src
	 *            The source peer key.
	 **************************************************************************/
	public void setKey(String src)
	{
		m_data.put(IMessageField.FIELD_IPC_KEY, src);
	}

	/***************************************************************************
	 * Assign the receiver id
	 * 
	 * @param id
	 *            The receiver id
	 **************************************************************************/
	public void setReceiver(String id)
	{
		m_data.put(IMessageField.FIELD_RECEIVER_ID, id);
	}

	/***************************************************************************
	 * Assign the sender id
	 * 
	 * @param id
	 *            The sender id
	 **************************************************************************/
	public void setSender(String id)
	{
		m_data.put(IMessageField.FIELD_SENDER_ID, id);
	}

	/***************************************************************************
	 * Set a message property name and value.
	 * 
	 * @param name
	 *            Property name.
	 * @param value
	 *            Property value.
	 **************************************************************************/
	public void set(String name, String value)
	{
		m_data.put(name, value);
	}

	/***************************************************************************
	 * Obtain the value of a given property.
	 * 
	 * @param name
	 *            Property name.
	 * @return Property value.
	 * @throws MessageException
	 *             if there is no such property.
	 **************************************************************************/
	public String get(String name) throws MessageException
	{
		if (!m_data.containsKey(name))
		{
			System.err.println("Missing property in message " + getId() + ": " + name);
			System.err.println(dataStr());
			throw new MessageException("Message '" + getId() + "' has not such property: " + name);
		}
		return m_data.get(name);
	}

	/***************************************************************************
	 * Check if the message contains the given key
	 **************************************************************************/
	public boolean hasKey(String key)
	{
		return m_data.containsKey(key);
	}
	
	/***************************************************************************
	 * Uncompression algorithm
	 **************************************************************************/
	public static byte[] uncompress( byte[] data ) throws Exception
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPInputStream gis = new GZIPInputStream(bis);
		int numRead = 0;
		byte[] output = new byte[512];
		while(numRead != -1)
		{
			numRead = gis.read(output, 0, output.length);
			if (numRead >0)
			{
				bos.write(output,0,numRead);
			}
		}
		bis.close();
		bos.close();
		return bos.toByteArray();
	}
	
	/***************************************************************************
	 * Extract tags
	 **************************************************************************/
	public static TreeMap<String, String> fromData( byte[] data ) throws Exception
	{
		TreeMap<String, String> tags = new TreeMap<String, String>();

	    String key = "";
	    String value = "";
	    int length = 0;
	    short b1, b2, b3, b4;

	    try
	    {
		    for( int pos = 0 ; pos < data.length; )
		    {
		    	// Bytes are signed, and need to be converted to unsigned for the length calculation
		    	b1 = (data[pos] >= 0) ? data[pos] : (short) (data[pos] + 256);
		    	b2 = (data[pos+1] >= 0) ? data[pos+1] : (short) (data[pos+1] + 256);
		        length = b1 * 256 + b2;
		        pos += 2;
	
		        // Get the key string
		        try
		        {
		        	key = new String( data,  pos, length );
		        }
		        catch(Exception ex)
		        {
			    	System.err.println("Error processing key");
		        	System.err.println("Data size   : " + data.length);
		        	System.err.println("Position    : " + pos);
		        	System.err.println("Value length: " + length);
		        	throw ex;
		        }
		        
		        pos += length;
	
		    	// Bytes are signed, and need to be converted to unsigned for the length calculation
		    	b1 = (data[pos]   >= 0) ? data[pos]   : (short) (data[pos]   + 256);
		    	b2 = (data[pos+1] >= 0) ? data[pos+1] : (short) (data[pos+1] + 256);
		    	b3 = (data[pos+2] >= 0) ? data[pos+2] : (short) (data[pos+2] + 256);
		    	b4 = (data[pos+3] >= 0) ? data[pos+3] : (short) (data[pos+3] + 256);
		    	// Compose the length now
		        length = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;  
		        pos += 4;
	
		        // Get the value string
		        value = "";
		        try
		        {
		        	value = new String( data, pos, length );
		        }
		        catch(Exception ex)
		        {
			    	System.err.println("Error processing value (key " + key + ")");
		        	System.err.println("Data size   : " + data.length);
		        	System.err.println("Position    : " + pos);
		        	System.err.println("Value length: " + length);
		        	throw ex;
		        }
		        pos += length;
	
		        tags.put(key,value);
		    }
	    }
	    catch(Exception ex)
	    {
	    	System.err.println("Error creating message from data: " + ex.getLocalizedMessage());
	    	System.err.println("Created tags:");
	    	for(String mkey : tags.keySet())
	    	{
	    		System.err.println( "     " + mkey + "==>" + tags.get(mkey));
	    	}
	    }

	    return tags;
	}
	
	/***************************************************************************
	 * Dump message data
	 **************************************************************************/
	public static void dumpData( byte[] data )
	{
	    boolean first = true;
	    String res = "";

	    for(int i = 0 ; i < data.length ; i++)
	    {
	        if (i % 16 == 0)
	        {
	            String ihex = Integer.toHexString(i).toUpperCase();
	            if (ihex.length()==1) ihex = "000" + ihex;
	            if (ihex.length()==2) ihex = "00" + ihex;
	            if (ihex.length()==3) ihex = "0" + ihex;
	            res += "\n" + ihex + " ";
	            first = true;
	        }
	        if(!first) res += " ";
	        first = false;
	        String hex = Integer.toHexString(data[i]).toUpperCase();
	        if (hex.length()==1) hex = "0" + hex;
	        res += hex;
	    }

	    res += "\n";
		System.out.print(res);
	}
}
