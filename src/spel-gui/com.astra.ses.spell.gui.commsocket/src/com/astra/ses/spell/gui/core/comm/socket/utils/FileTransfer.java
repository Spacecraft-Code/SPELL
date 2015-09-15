///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.socket.utils
// 
// FILE      : FileTransfer.java
//
// DATE      : 2008-11-21 09:02
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
package com.astra.ses.spell.gui.core.comm.socket.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;

/*******************************************************************************
 * @brief Utility class for transferring files via CP or SCP
 * @date 28/04/08
 ******************************************************************************/
public class FileTransfer
{
	/** Holds the SCP client */
	private SCPClient   m_scp;
	private Connection  m_connection;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public FileTransfer()
	{
		m_scp = null;
		m_connection = null;
	}
  
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param data
	 *            Server data to be used to establish the tunnel
	 **************************************************************************/
	public FileTransfer( Connection conn, ServerInfo server ) throws Exception
	{
		if (conn == null)
		{
			Logger.debug("Creating SCP connection", Level.COMM, this);
			conn = new Connection(server.getHost());
			conn.connect();
			Logger.debug("Authenticating SCP", Level.COMM, this);
			
			Authenticator.authenticate(m_connection, server.getAuthentication());
		}
		m_scp = new SCPClient(conn);
		m_connection = conn;
	}

	/***************************************************************************
	 * Obtain a file from the server
	 **************************************************************************/
	public void getFile( String remoteFile, String targetDir ) throws IOException
	{
		if (m_scp == null)
		{
			copyFile (remoteFile, targetDir);
		}
		else
		{
			File target = new File(targetDir);
			if (!target.exists() || !target.isDirectory() || !target.canWrite())
			{
				throw new IOException("Target directory '" + targetDir + "' does not exist or is not writable");
			}
			Logger.debug("SCP retrieve '" + remoteFile + "' to '" + targetDir + "'", Level.COMM, this);
			m_scp.get(remoteFile, targetDir);
		}
	}
	
	/***************************************************************************
	 * Close connection
	 **************************************************************************/
	public void close()
	{
		if (m_connection != null)
		{
			m_connection.close();
		}
	}

	/***************************************************************************
     * Copy locally file
	 **************************************************************************/
	private void copyFile ( String source, String targetDir) throws IOException
	{
		File srcFile = new File (source);
		File tgtFile = new File (targetDir + File.separator + srcFile.getName() );

		if(!tgtFile.exists()) 
		{
			tgtFile.createNewFile();
		}

		FileChannel srcFC = null;
		FileChannel tgtFC = null;
		try 
		{
			srcFC = new FileInputStream(srcFile).getChannel();
			tgtFC = new FileOutputStream(tgtFile).getChannel();

			long count = 0;
			long size = srcFC.size();
			while((count += tgtFC.transferFrom(srcFC, count, size-count))<size);
		}
		finally 
		{
			if(srcFC != null) 
			{
				srcFC.close();
			}
			if(tgtFC != null) 
			{
				tgtFC.close();
			}
		}
	}
}
