///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : DirectoryTree.java
//
// DATE      : Feb 8, 2012
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.model.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;

public class DirectoryTree
{
	private static IFileManager s_files = null;
	
	private String m_name;
	private String m_rootPath;
	private DirectoryTree m_parent;
	private List<DirectoryTree> m_subdirectories;
	private List<DirectoryFile> m_files;

	public DirectoryTree( String name, String path )
	{
		this(null,name,path);
	}

	public DirectoryTree( DirectoryTree parent, String name, String path )
	{
		if (s_files == null)
		{
			s_files = (IFileManager) ServiceManager.get(IFileManager.class);
		}
		m_name = name;
		m_parent = parent;
		m_rootPath = path;
		m_subdirectories = new ArrayList<DirectoryTree>();
		m_files = new ArrayList<DirectoryFile>();
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public DirectoryTree getParent()
	{
		return m_parent;
	}
	
	public boolean hasChildren()
	{
		return ((m_files.size()>0)||(m_subdirectories.size()>0));
	}
	
	public void getTreeFromServer( IProgressMonitor monitor )
	{
		m_subdirectories.clear();
		m_files.clear();
		List<String> items = s_files.getFilesInDirectory(m_rootPath);
		monitor.beginTask("Reading directory " + m_rootPath, items.size());
		for(String item : items)
		{
			if (item.startsWith("+"))
			{
				String itemName = item.substring(1,item.length());
				monitor.subTask("Subdirectory " + itemName);
				try
				{
					DirectoryTree subtree = new DirectoryTree( this, itemName, m_rootPath + "/" + itemName );
					subtree.getTreeFromServer(monitor);
					m_subdirectories.add(subtree);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				monitor.subTask("File " + item);
				DirectoryFile file = new DirectoryFile(this,item); 
				m_files.add( file );
			}
			monitor.worked(1);
		}
	}
	
	public void addDirectory( DirectoryTree subdir )
	{
		m_subdirectories.add(subdir);
	}
	
	public List<DirectoryTree> getSubdirs()
	{
		return m_subdirectories;
	}
	
	public List<DirectoryFile> getFiles()
	{
		return m_files;
	}
	
	public String getPath()
	{
		return m_rootPath;
	}
}
