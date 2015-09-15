///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language
// 
// FILE      : SpellProgrammingLanguage.java
//
// DATE      : 2009-11-23
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

/*****************************************************************************
 * Class for providing spell language special constants, functions and symbols
 * It tries to add new symbols to Python programming language
 *****************************************************************************/
public class SpellProgrammingLanguage
{
	private static SpellProgrammingLanguage s_instance;
	/** Holds the list of available SPELL constants */
	private ArrayList<String> m_constants;
	/** Holds the list of available SPELL functions */
	private ArrayList<String> m_functions;
	/** Holds the list of SPELL critical functions */
	private ArrayList<String> m_criticalFunctions;
	/** Holds the list of available SPELL modifiers */
	private ArrayList<String> m_modifiers;

	/**
	 * Holds the default list of SPELL functions, applied when the plugin cannot
	 * obtain them
	 */
	private static String[] s_defaultFunctions = { "GetTM", "Display", "Verify", "SendAndVerify", "SendAndVerifyAdjLim", "Send",
	        "SetGroundParameter", "Prompt", "Abort", "Event", "Step", "GetResource", "SetResource", "UserLogin", "UserLogout",
	        "DisplayStep", "CheckUser", "StartTask", "StopTask", "CheckTask", "SetExecDelay", "StartProc", "LoadDictionary", "WaitFor",
	        "AdjustLimits", "EnableAlarms", "DisableAlarms", "SetTMparam", "GetTMparam", "Script", "OpenDisplay", "CloseDisplay",
	        "PrintDisplay", "Goto", "Notification", "OpenFile", "CloseFile", "DeleteFile", "ReadFile", "WriteFile", "ReadDirectory",
	        "GetLimits", "SetLimits", "IsAlarmed", "GenerateMemoryReport", "CompareMemoryImages", "TMTCLookup", "MemoryLookup",
	        "EnableRanging", "DisableRanging", "StartRanging", "StartRangingCalibration", "GetBasebandNames",
	        "GetBasebandConfig", "SetBasebandConfig", "GetAntennaNames", "AbortRanging",
	        "EnableLimits", "DisableLimits", "OpenWorkspace", "CloseWorkspace" };

	/**
	 * Holds the default list of SPELL CRITICAL functions, applied when the
	 * plugin cannot obtain them
	 */
	private static String[] s_criticalFunctions = { "SendAndVerify", "SendAndVerifyAdjLim", "Send", "Abort" };

	/**
	 * Holds the default list of SPELL modifiers, applied when the plugin cannot
	 * obtain them
	 */
	private static String[] s_defaultModifiers = { "ValueFormat", "OnFailure", "Wait", "Timeout", "Delay", "TryAll", "Time", "Retries",
	        "Host", "Tolerance", "Delay", "Type", "Range", "Severity", "Scope", "OnTrue", "OnFalse", "PromptUser", "PromptFailure",
	        "Retry", "GiveChoice", "HandleError", "ValueType", "Radix", "Units", "Strict", "Interval", "Until", "HiYel", "HiRed", "LoYel",
	        "LoRed", "HiBoth", "LoBoth", "Midpoint", "Limits", "IgnoreCase", "Block", "Sequence", "Default", "Mode", "Confirm", "OnSkip",
	        "SendDelay", "eq", "gt", "lt", "neq", "ge", "le", "btw", "nbw", "Printer", "Format", "Extended", "Reuse", "Monitor" };
	/**
	 * Holds the default list of SPELL constants, applied when the plugin cannot
	 * obtain them
	 */
	private static String[] s_defaultConstants = { "YES_NO", "STEP", "PREV_STEP", "ALPHA", "NUM", "LIST", "YES", "OK_CANCEL", "OK",
	        "CANCEL", "NO", "YES_NO", "COMBO", "ENG", "RAW", "DEC", "BIN", "OCT", "HEX", "INFO", "WARNING", "ERROR", "DISPLAY", "LOGVIEW",
	        "DIALOG", "LONG", "DATETIME", "RELTIME", "STRING", "FLOAT", "BOOLEAN", "ABORT", "SKIP", "REPEAT", "RECHECK", "RESEND",
	        "NOACTION", "PROMPT", "NOPROMPT", "ACTION_ABORT", "ACTION_SKIP", "ACTION_REPEAT", "ACTION_RESEND", "ACTION_CANCEL", "MINUTE",
	        "HOUR", "TODAY", "YESTERDAY", "DAY", "SECOND", "ITEM_SUCCESS", "ITEM_FAILED", "ITEM_PROGRESS" };
	/**
	 * Holds the default list of SPELL entities, applied when the plugin cannot
	 * obtain them
	 */
	private static String[] s_defaultEntities = { "DAY", "GDB", "SCDB", "ARGS", "PROC", "IVARS", "TIME", "HOUR", "MINUTE", "NOW", "SECOND",
	        "TODAY", "TOMORROW", "YESTERDAY", "File", "Var", "DataContainer" };

	/** Holds the list of python and other keywords */
	private static String[] s_defaultKeywords = { "for", "if", "elif", "else", "try", "except", "while", "in", "print", "del", "def",
	        "command", "sequence", "group", "args", "verify", "config", "True", "False", "import", "type", "level", "and", "or", "not",
	        "global", "str", "abs", "float", "int", "pass", "assert" };

	/**************************************************************************
	 * Spell languange singleton get() method
	 * 
	 * @return
	 *************************************************************************/
	public static SpellProgrammingLanguage getInstance()
	{
		if (s_instance == null)
		{
			s_instance = new SpellProgrammingLanguage();
		}
		return s_instance;
	}

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	private SpellProgrammingLanguage()
	{
		m_constants = new ArrayList<String>();
		m_functions = new ArrayList<String>();
		m_modifiers = new ArrayList<String>();
		m_criticalFunctions = new ArrayList<String>();
		m_criticalFunctions.addAll(Arrays.asList(s_criticalFunctions));
	}

	/**************************************************************************
	 * Get spell constants
	 *************************************************************************/
	public String[] getSpellConstants()
	{
		if (m_constants.size() == 0)
			parseConstants();
		// Provide the hardcoded defaults if it was unable to read the constants
		// from SPELL library
		if (m_constants.size() == 0)
		{
			return s_defaultConstants;
		}
		return m_constants.toArray(new String[0]);
	}

	/**************************************************************************
	 * Parse the SPELL language constants file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseConstants()
	{
		m_constants.clear();
		String path = System.getenv("SPELL_HOME");
		if (path == null)
		{
			System.err.println("[LANGUAGE] Unable to read constants file. No SPELL_HOME defined!!!");
			return;
		}
		String sep = System.getProperty("file.separator");
		path += sep + "spell" + sep + "spell" + sep + "lang" + sep + "constants.py";
		File constantsFile = new File(path);
		if (!constantsFile.exists())
		{
			System.err.println("[LANGUAGE] " + constantsFile.getAbsolutePath() + " does not exist. Using default");
			Bundle bundle = Activator.getDefault().getBundle();
			Path filePath = new Path("default_constants");
			URL url = FileLocator.find(bundle, filePath, Collections.EMPTY_MAP);
			URL fileURL = null;
			try
			{
				fileURL = FileLocator.toFileURL(url);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			constantsFile = new File(fileURL.getPath());
		}
		try
		{
			FileReader in = new FileReader(constantsFile);
			BufferedReader reader = new BufferedReader(in);
			String line = null;
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					line = line.trim();
					if (line.isEmpty())
						continue;
					if (line.startsWith("#"))
						continue;
					if (!line.contains("="))
						continue;
					if (line.startsWith("="))
						continue;
					String[] items = line.split(" ");
					m_constants.add(items[0]);
				}
			}
			while (line != null);
			reader.close();
			in.close();
		}
		catch (FileNotFoundException e)
		{
			String err = "Unable to find constants file: " + e;
			System.err.println("[LANGUAGE] " + err);
		}
		catch (IOException e)
		{
			System.err.println("[LANGUAGE] Unable to read constants file: " + e);
		}
	}

	/**************************************************************************
	 * Get spell functions
	 *************************************************************************/
	public String[] getSpellFunctions()
	{
		if (m_functions.size() == 0)
		{
			String standardLanguageFile = System.getenv("SPELL_HOME");
			if (standardLanguageFile == null)
			{
				System.err.println("[LANGUAGE] Unable to read functions file. No SPELL_HOME defined!!!");
				return m_functions.toArray(new String[0]);
			}
			String sep = System.getProperty("file.separator");
			standardLanguageFile += sep + "spell" + sep + "spell" + sep + "lang" + sep + "functions.py";
			parseFunctions( standardLanguageFile );
		}
		
		// Provide the hardcoded defaults if there are no items read
		if (m_functions.size() == 0)
		{
			return s_defaultFunctions;
		}
		/*
		 * FIXME some functions don't appear in the functions file to avoid
		 * conflicts, but anyhow they are functions, so they are added here
		 */
		m_functions.add("Goto");
		m_functions.add("Step");
		return m_functions.toArray(new String[0]);
	}

	/**************************************************************************
	 * Get spell functions
	 *************************************************************************/
	public boolean isCriticalFunction(String functionName)
	{
		return m_criticalFunctions.contains(functionName);
	}

	/**************************************************************************
	 * Parse the SPELL language functions file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseFunctions( String path )
	{
		m_functions.clear();
		File functionsFile = new File(path);
		if (!functionsFile.exists())
		{
			System.err.println("[LANGUAGE] " + functionsFile.getAbsolutePath() + " does not exist. Using default");
			Bundle bundle = Activator.getDefault().getBundle();
			Path filePath = new Path("default_functions");
			URL url = FileLocator.find(bundle, filePath, Collections.EMPTY_MAP);
			URL fileURL = null;
			try
			{
				fileURL = FileLocator.toFileURL(url);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			functionsFile = new File(fileURL.getPath());
		}
		try
		{
			FileReader in = new FileReader(functionsFile);
			BufferedReader reader = new BufferedReader(in);
			String line = null;
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					line = line.trim();
					if (line.isEmpty())
						continue;
					if (line.startsWith("def"))
					{
						String[] items = line.split(" ");
						String funcName = items[1];
						if (funcName.endsWith("("))
							funcName = funcName.substring(0, funcName.length() - 1);
						m_functions.add(funcName);
					}
				}
			}
			while (line != null);
			reader.close();
			in.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("[LANGUAGE] Unable to find functions file: " + e);
		}
		catch (IOException e)
		{
			System.err.println("[LANGUAGE] Unable to read functions file: " + e);
		}
	}

	/**************************************************************************
	 * Get spell modifiers
	 *************************************************************************/
	public String[] getSpellModifiers()
	{
		if (m_modifiers.isEmpty())
			parseModifiers();
		// Provide the hardcoded defaults if there are no items read
		if (m_modifiers.size() == 0)
		{
			return s_defaultModifiers;
		}
		return m_modifiers.toArray(new String[0]);
	}

	/**************************************************************************
	 * Parse the SPELL language modifiers file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseModifiers()
	{
		String path = System.getenv("SPELL_HOME");
		if (path == null)
		{
			System.err.println("[LANGUAGE] Unable to read modifiers file. No SPELL_HOME defined!!!");
			return;
		}
		String sep = System.getProperty("file.separator");
		path += sep + "spell" + sep + "spell" + sep + "lang" + sep + "modifiers.py";
		File modifiersFile = new File(path);
		if (!modifiersFile.exists())
		{
			System.err.println("[LANGUAGE] " + modifiersFile.getAbsolutePath() + " does not exist. Using default");
			Bundle bundle = Activator.getDefault().getBundle();
			Path filePath = new Path("default_modifiers");
			URL url = FileLocator.find(bundle, filePath, Collections.EMPTY_MAP);
			URL fileURL = null;
			try
			{
				fileURL = FileLocator.toFileURL(url);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			modifiersFile = new File(fileURL.getPath());
		}
		try
		{
			FileReader in = new FileReader(modifiersFile);
			BufferedReader reader = new BufferedReader(in);
			String line = null;
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					line = line.trim();
					if (line.isEmpty())
						continue;
					if (line.startsWith("#"))
						continue;
					if (!line.contains("="))
						continue;
					if (line.startsWith("="))
						continue;
					String[] items = line.split(" ");
					m_modifiers.add(items[0]);
				}
			}
			while (line != null);
			reader.close();
			in.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("[LANGUAGE] Unable to find modifiers file: " + e);
		}
		catch (IOException e)
		{
			System.err.println("[LANGUAGE] Unable to read modifiers file: " + e);
		}
	}

	/***************************************************************************
	 * Get spell entities
	 * 
	 * @return a set of tokens which are considered "entities" in SPELL language
	 **************************************************************************/
	public String[] getSpellEntities()
	{
		/*
		 * FIXME at this moment these entities are hardcoded
		 */
		return s_defaultEntities;
	}

	/***************************************************************************
	 * Get spell entities
	 * 
	 * @return a set of tokens which are considered "entities" in SPELL language
	 **************************************************************************/
	public String[] getSpellKeywords()
	{
		/*
		 * FIXME at this moment these entities are hardcoded
		 */
		return s_defaultKeywords;
	}
}
