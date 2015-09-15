///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : FileManager.java
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
package com.astra.ses.spell.gui.core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageAsRunList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDeleteFile;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDeleteRecovery;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageFilePath;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetDataDirs;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetFilesInDir;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRecoveryList;
import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.files.BasicServerFile;
import com.astra.ses.spell.gui.core.model.files.IServerFile;
import com.astra.ses.spell.gui.core.model.files.LogFile;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * @brief Provides access to SPELL server file management
 * @date 20/05/08
 ******************************************************************************/
public class FileManager extends BaseService implements IFileManager
{
	/** Service identifier */
	public static final String	ID	         = "com.astra.ses.spell.gui.FileManager";
	
	/** Holds reference to the context proxy */
	private IContextProxy m_proxy;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public FileManager()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
		m_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getServerFilePath(String procId, ServerFileType type, IProgressMonitor monitor )
	{
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		String path = null;
		try
		{
			Logger.debug("Retrieving server file path: " + procId, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageFilePath(procId,type);
			SPELLmessage response = proxy.sendRequest(msg);
			if (response != null)
			{
				path = SPELLmessageFilePath.getPath(response);
			}
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return path;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getServerFilePath( ServerFileType type, IProgressMonitor monitor )
	{
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		String path = null;
		try
		{
			Logger.debug("Retrieving server file path for type: " + type, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageFilePath(type);
			SPELLmessage response = proxy.sendRequest(msg);
			if (response != null)
			{
				path = SPELLmessageFilePath.getPath(response);
			}
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return path;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IServerFile getServerFile(String path, ServerFileType typeId, ProcessingLimits limits, IProgressMonitor monitor) throws Exception
	{
		Logger.debug("Downloading file '" + path + "'", Level.COMM, this);
		String targetDir = System.getenv("SPELL_DATA") + File.separator + "Runtime";
		Logger.debug("Target: '" + targetDir+ "'", Level.COMM, this);

		
		IServerFile file = null;
		try
		{
			File td = new File(targetDir);
			if (!td.exists() || !td.isDirectory() || !td.canWrite())
			{
				throw new RuntimeException("Runtime directory does not exist or is not writable");
			}
			
			List<String> lines = new ArrayList<String>();
			
			m_proxy.getFile(path, targetDir);
			String filename = path;
			int idx = path.lastIndexOf("/");
			if (idx == -1)
			{
				idx = path.lastIndexOf("\\");
			}
			if (idx != -1)
			{
				filename = filename.substring(idx);
			}

			String localFile = targetDir + File.separator + filename;
			
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(localFile) ) );
			String line = null;
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					lines.add(line);
				}
				if ( limits != null && lines.size()>limits.fileLineCount)
				{
					Logger.warning("Reached limit for server line count (" + lines.size() + ")", Level.PROC, this);
					lines.clear();
				}
			}
			while (line != null);

			// We got the lines, create the file
			switch (typeId)
			{
			case ASRUN:
				if ( limits != null && lines.size()>limits.processLineCount) 
				{
					Logger.warning("Reached limit for processing line count (" + lines.size() + ")", Level.PROC, this);
					List<String> toProcess = lines.subList(0, 5);
					toProcess.addAll(lines.subList(lines.size()-limits.processLineCount, lines.size()));
					lines = toProcess;
				}
				file = new AsRunFile("", path, lines);
				break;
			case EXECUTOR_LOG:
				if ( limits != null && lines.size()>limits.processLineCount) 
				{
					Logger.warning("Reached limit for processing line count (" + lines.size() + ")", Level.PROC, this);
					lines = lines.subList(lines.size()-limits.processLineCount, lines.size());
				}
				file = new LogFile("", path, lines);
				break;
			case OTHER:
				file = new BasicServerFile(path,lines);
			}
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			throw new RuntimeException("Unable to retrieve server file.\n\n" + ex.getLocalizedMessage() + "\n\nTarget directory: " + targetDir);
		}
		return file;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String downloadServerFile(String path, IProgressMonitor monitor) throws Exception
	{
		Logger.debug("Downloading file '" + path + "'", Level.COMM, this);
		String targetDir = System.getenv("SPELL_DATA") + File.separator + "Runtime";
		Logger.debug("Target: '" + targetDir+ "'", Level.COMM, this);

		
		String localFile = null;
		try
		{
			File td = new File(targetDir);
			if (!td.exists() || !td.isDirectory() || !td.canWrite())
			{
				throw new RuntimeException("Runtime directory does not exist or is not writable");
			}
			m_proxy.getFile(path, targetDir);
			String filename = path;
			int idx = path.lastIndexOf("/");
			if (idx == -1)
			{
				idx = path.lastIndexOf("\\");
			}
			if (idx != -1)
			{
				filename = filename.substring(idx);
			}

			localFile = targetDir + File.separator + filename;
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			throw new RuntimeException("Unable to download server file.\n\n" + ex.getLocalizedMessage() + "\n\nTarget directory: " + targetDir);
		}
		return localFile;
	}

	@Override
	public void deleteServerFile(String path, IProgressMonitor monitor)
	{
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);

		try
		{
			Logger.debug("Deleting server file: " + path, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageDeleteFile(path);
			proxy.sendRequest(msg);
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.interfaces.IContextProxy#deleteRecoveryFiles
	 * ()
	 */
	@Override
	public void deleteRecoveryFiles(String mainFile, IProgressMonitor monitor)
	{
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);

		try
		{
			Logger.debug("Deleting recovery files for: " + mainFile, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageDeleteRecovery(mainFile);
			proxy.sendRequest(msg);
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
	}

	@Override
	public List<String> getRecoveryFileList()
	{
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);

		List<String> list = new ArrayList<String>();
		try
		{
			Logger.debug("Retrieving list of recovery files", Level.COMM, this);
			SPELLmessage msg = new SPELLmessageRecoveryList();
			SPELLmessage response = proxy.sendRequest(msg);
			if (response != null)
			{
				list = SPELLmessageRecoveryList.getList(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return list;
	}

	@Override
	public List<String> getAsRunFileList()
	{
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);

		List<String> list = new ArrayList<String>();
		try
		{
			Logger.debug("Retrieving list of ASRUN files", Level.COMM, this);
			SPELLmessage msg = new SPELLmessageAsRunList();
			SPELLmessage response = proxy.sendRequest(msg);
			if (response != null)
			{
				list = SPELLmessageAsRunList.getList(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return list;
	}

	@Override
	public List<String> getDataDirectories()
	{
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		List<String> list = new ArrayList<String>();
		try
		{
			Logger.debug("Retrieving list of data directories ", Level.COMM, this);
			SPELLmessage msg = new SPELLmessageGetDataDirs();
			SPELLmessage response = proxy.sendRequest(msg);
			if (response != null)
			{
				list = SPELLmessageGetDataDirs.getList(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return list;
	}

	@Override
	public List<String> getFilesInDirectory( String directory )
	{
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		List<String> list = new ArrayList<String>();
		try
		{
			Logger.debug("Retrieving list of recovery files", Level.COMM, this);
			SPELLmessage msg = new SPELLmessageGetFilesInDir( directory );
			SPELLmessage response = proxy.sendRequest(msg);
			if (response != null)
			{
				list = SPELLmessageGetFilesInDir.getList(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return list;
	}
}
