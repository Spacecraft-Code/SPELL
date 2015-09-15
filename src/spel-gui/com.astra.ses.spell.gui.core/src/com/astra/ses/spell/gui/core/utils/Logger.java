///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.utils
// 
// FILE      : Logger.java
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
package com.astra.ses.spell.gui.core.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import com.astra.ses.spell.gui.core.model.types.Level;

import org.slf4j.LoggerFactory;


/*******************************************************************************
 * @brief This class provides tracing/logging support. If needed, transmits
 *        logged data to the MasterView.
 *        
 *        2014-04-03 (DVillegas) Added the SLF4J Facade to allow changing the logging framework without changing the code.
 *        
 * @date 09/10/07
 ******************************************************************************/
public class Logger
{
	/** Length for class names */
	private static final int CLASS_NAME_LEN = 22;
	/** Default Config directory for SPELL */
	private static String DEFAULT_CONFIG_DIR = "config";
	
	
	/* STATIC CONSTRUCTOR */
	static {
		//Find default directory separator
		String sep = System.getProperty("file.separator");
		
		String sConfig = System.getenv("SPELL_CONFIG");
		if(sConfig==null || sConfig.isEmpty()) sConfig = System.getenv("SPELL_HOME") + sep + DEFAULT_CONFIG_DIR;
		if(sConfig==null || sConfig.isEmpty()) sConfig = ".";
		
		addClassPath( sConfig );
	}


	
	/* LOG METHODS */
	
	/***************************************************************************
	 * Log a debugging message specifying the level
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	public static void debug(String message, Level level, Object origin)
	{
		org.slf4j.Logger logger = LoggerFactory.getLogger( level.name() + "." + getFormatedClassName(origin) );

		logger.debug("{}: {}", level.log, message);
	} //debug

	/***************************************************************************
	 * Log an information message specifying the level
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	public static void info(String message, Level level, Object origin)
	{
		org.slf4j.Logger logger = LoggerFactory.getLogger( level.name() + "." + getFormatedClassName(origin) );

		logger.info("{}: {}", level.log, message);
	} //info

	/***************************************************************************
	 * Log a warning message specifying the level
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	public static void warning(String message, Level level, Object origin)
	{
		org.slf4j.Logger logger = LoggerFactory.getLogger( level.name() + "." + getFormatedClassName(origin) );

		logger.warn("{}: {}", level.log, message);
	} //warning

	/***************************************************************************
	 * Log an error message specifying the level
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	public static void error(String message, Level level, Object origin)
	{
		org.slf4j.Logger logger = LoggerFactory.getLogger( level.name() + "." + getFormatedClassName(origin) );

		logger.error("{}: {}", level.log, (message!=null?message:"Log message is null") );
	} //error


	/* HELPER METHODS */
	
	/*****
	 * Get the simple name of a class and cut it in case its lengh is greater than CLASS_NAME_LEN constant.
	 * 
	 * @param origin
	 * @return class name of maximum length CLASS_NAME_LEN
	 */
	public static String getFormatedClassName(Object origin) 
	{
		String sClassName = origin.getClass().getSimpleName();
		return (sClassName.length()>CLASS_NAME_LEN?sClassName.substring(0,CLASS_NAME_LEN-1)+"_":sClassName);
	}	
	
	/***
	 * Add a directory to the current java classpath.
	 * 
	 * @param path
	 */
	public static void addClassPath(String path) {
	    try {
			File f = new File(path);
			if( f.exists() ) {
			    URI u = f.toURI();
			    URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			    Class<URLClassLoader> urlClass = URLClassLoader.class;
			    Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
			    method.setAccessible(true);
			    method.invoke(urlClassLoader, new Object[]{u.toURL()});
			} else System.err.println("ERROR: SPELL Config directory does not exist. (Side effect: Logger configuration not available.)");
	    } catch(Exception e){
	    	System.err.println("ERROR: Loading SPELL Config directory into classpath (Side effect: Logger configuration not available.)\n" + e.getMessage());
	    }
	} //addDirectory2ClassPath
} //Logger Class
