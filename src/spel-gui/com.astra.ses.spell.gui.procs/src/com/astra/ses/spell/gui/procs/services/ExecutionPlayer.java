///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.services
// 
// FILE      : ExecutionPlayer.java
//
// DATE      : Jun 19, 2013
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
package com.astra.ses.spell.gui.procs.services;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.AsRunFileLine;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification.StackType;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.PromptDisplayType;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunProcessing;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunReplayResult;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class ExecutionPlayer
{
	private static DateFormat s_df = null;
	private IProcedure m_model;
	private String m_localAsrunPath;
	private double m_totalMemory;
	private boolean m_retrievedLine;
	private StatusNotification m_retrievedStatus;
	private ErrorData m_retrievedError;
	// Data to build a prompt
	private String m_promptMessage = null;
	private Vector<String> m_promptOptions = null;
	private Vector<String> m_promptExpected = null;
	private boolean m_numericPrompt = false;
	private Scope m_promptScope = Scope.OTHER;
	/** Holds reference to the context proxy to retrieve summary info */
	private IContextProxy m_proxy;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ExecutionPlayer( IProcedure model, String localAsrunPath, IContextProxy proxy )
	{
		m_model = model;
		m_localAsrunPath = localAsrunPath;
		m_totalMemory = Runtime.getRuntime().totalMemory()*1.0;
		m_proxy = proxy;
		reset();
		
		if (s_df == null)
		{
			// Time configuration
			IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
			s_df = cfg.getTimeFormat();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void reset()
	{
		m_retrievedLine = false;
		m_retrievedStatus = null;
		m_retrievedError = null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void replay( IProgressMonitor monitor, long delayMsec, AsRunReplayResult result )
	{
		initializeResult(result);

		try
		{
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(m_localAsrunPath) ), 1000 );
			String line = null;
			String procId = m_model.getProcId();
			boolean replay = m_model.isInReplayMode();
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					if (monitor.isCanceled()) break;
					if (line.startsWith("#") || line.trim().isEmpty()) continue;
					
					AsRunFileLine arLine = new AsRunFileLine(procId, line);
					processAsRunLine(arLine, result, replay);
					if (!replay)
					{
						recreatePrompt();
					}
					reportProgress(monitor,result);
					if (delayMsec != 0)
					{
						try{ Thread.sleep(delayMsec); }catch(Exception ignore){};
					}
				}
			}
			while (line != null);
		}
		catch(Exception ex)
		{
			result.status = AsRunProcessing.FAILED;
			result.message = "Failed to process ASRUN: " + ex.getLocalizedMessage();
			return;
		}

		if (m_model.isInReplayMode())
		{
			if (m_retrievedStatus == null || !m_retrievedLine ) 
			{
				try 
				{
					// IMPORTANT
					// It is set to null for REPLAY models where there is no sense on requesting execinfo
					if (m_proxy == null) 
					{
						IExecutorInfo info = m_proxy.getExecutorInfo(m_model.getProcId());
						Logger.error("Incomplete ASRUN processing: no status information found", Level.PROC, this);
						m_retrievedStatus = new StatusNotification( m_model.getProcId(), info.getStatus() );
						result.status = AsRunProcessing.FAILED;
					}
					else
					{
						Logger.debug("Retrieving final information from server", Level.PROC, this);
						IExecutorInfo info = m_proxy.getExecutorInfo(m_model.getProcId());
						m_retrievedStatus = new StatusNotification(m_model.getProcId(),info.getStatus());
						m_retrievedError  = info.getError();
	
						Logger.debug("Current status: " + info.getStatus(), Level.PROC, this);
						Logger.debug("Current stack : " + info.getStack(), Level.PROC, this);
	
						// Ensure the correct final position of line
						StackNotification ndata = new StackNotification( StackType.LINE, m_model.getProcId(), info.getStack(), info.getCodeName());
						Date currentTime = Calendar.getInstance().getTime();
						String currentTimeStr = s_df.format(currentTime);
						ndata.setTime(currentTimeStr);
						try
						{
							m_model.getRuntimeProcessor().notifyProcedureStack(ndata);
							m_retrievedLine = true;
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						};
					}
				} 
				catch (Exception e) 
				{
					Logger.error("Incomplete ASRUN processing: no status information found", Level.PROC, this);
					m_retrievedStatus = new StatusNotification( m_model.getProcId(), ExecutorStatus.UNKNOWN );
					result.status = AsRunProcessing.FAILED;
					e.printStackTrace();
				}
			}
			
			if (m_retrievedStatus.getStatus().equals(ExecutorStatus.PROMPT))
			{
				recreatePrompt();
			}
			
			notifyFinalStatus(result);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void processAsRunLine( AsRunFileLine line, AsRunReplayResult result, boolean replayMode )
	{
		Date currentTime = Calendar.getInstance().getTime();
		String currentTimeStr = s_df.format(currentTime);
		try
		{
			switch (line.getAsRunType())
			{
			case LINE: // Fall through
			case STAGE:
				m_retrievedLine = true;
			case CALL: // Fall through
			case RETURN:
			{
				StackNotification ndata = (StackNotification) line.getNotificationData();
				ndata.setTime(currentTimeStr);
				try
				{
					m_model.getRuntimeProcessor().notifyProcedureStack(ndata);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				};
				break;
			}
			case ITEM:
			{
				ItemNotification idata = (ItemNotification) line.getNotificationData();
				idata.setTime(line.getTimestamp());
				try
				{
					m_model.getRuntimeProcessor().notifyProcedureItem(idata);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				};
				break;
			}
			case DISPLAY: // Fall through
			case ANSWER:
			{
				DisplayData ddata = (DisplayData) line.getNotificationData();

				String timestamp = line.getTimestamp();
				Date ntimestamp = s_df.parse(timestamp);
				ddata.setTime( Long.toString(ntimestamp.getTime()*1000) );
				try
				{
					m_model.getRuntimeProcessor().notifyProcedureDisplay(ddata);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				};
				break;
			}
			case STATUS:
			{
				// Update only if the procedure is not in error
				m_retrievedStatus = (StatusNotification) line.getNotificationData();
				if (!replayMode)
				{
					try
					{
						m_model.getRuntimeProcessor().notifyProcedureStatus(m_retrievedStatus);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					};
				}
				break;
			}
			case ERROR:
				m_retrievedError = (ErrorData) line.getNotificationData();
				if (!replayMode)
				{
					try
					{
						m_model.getController().setError(m_retrievedError);
						m_model.getRuntimeProcessor().notifyProcedureError(m_retrievedError);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					};
				}
				break;
			case INIT:
				break;
			case PROMPT:
			{
				m_promptMessage = line.getDataA();
				// Clear other prompt data, this may be not the first prompt
				// in the asrun
				m_numericPrompt = false;
				m_promptOptions = null;
				m_promptExpected = null;
				
				// Process and display again the prompt message
				DisplayData pdata = (DisplayData) line.getNotificationData();

				String timestamp = line.getTimestamp();
				Date ntimestamp = s_df.parse(timestamp);
				pdata.setTime( Long.toString(ntimestamp.getTime()*1000) );
				
				try
				{
					m_model.getRuntimeProcessor().notifyProcedureDisplay(pdata);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				};
				break;
			}
			case PROMPT_TYPE:
			{
				if (line.getSubType().equals("16"))
				{
					m_numericPrompt = true;
				}
				break;
			}
			case PROMPT_OPTIONS:
			{
				String opts = line.getSubType();
				String[] options = opts.split(",,");
				m_promptOptions = new Vector<String>();
				m_promptOptions.addAll(Arrays.asList(options));
				break;
			}
			case PROMPT_EXPECTED:
			{
				String expt = line.getSubType();
				String[] expected = expt.split(",,");
				m_promptExpected = new Vector<String>();
				m_promptExpected.addAll(Arrays.asList(expected));
				break;
			}
			case UACTION:
			{
				try
				{
					UserActionNotification uadata = (UserActionNotification) line.getNotificationData();
					m_model.getRuntimeProcessor().notifyProcedureUserAction(uadata);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				};
				break;
			}	
			default:
				Logger.error("Unknown AsRun data in line " + result.processedLines, Level.PROC, this);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error("Failed to process ASRUN line " + result.processedLines + ":" + line.toString().replace("\t", "<T>"), Level.PROC, this);
			Logger.error("   " + ex.getLocalizedMessage(), Level.PROC, this);
			result.message = "failed to process some lines";
			result.status = AsRunProcessing.PARTIAL;
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void reportProgress( IProgressMonitor monitor, AsRunReplayResult result )
	{
		// Display progress
		result.processedLines++;

		Double pcm = (Runtime.getRuntime().freeMemory() / m_totalMemory) * 100.0;
		String mem = pcm.toString();
		int idx = mem.indexOf(".");
		int tlm = idx + 3;
		if (tlm<mem.length())
		{
			mem = mem.substring(0, tlm );
		}
		
		if (pcm<10.0)
		{
			result.status = AsRunProcessing.PARTIAL;
			result.message = "oldest ASRUN data discarded due to memory usage";
			m_model.getRuntimeProcessor().clearNotifications();
			Logger.warning("NOTIFICATIONS DISCARDED!! (" + result.processedLines + ")", Level.PROC, this);
		}
		
		monitor.subTask( "" + result.processedLines + " lines processed, available memory " + mem + "%");

	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void initializeResult( AsRunReplayResult result )
	{
		result.processedLines = 0;
		result.status = AsRunProcessing.COMPLETE;
		result.message = "";
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void recreatePrompt()
	{
		Logger.debug("Recreating prompt", Level.PROC, this);
		InputData inputData = null;
		if (m_promptOptions != null)
		{
			m_promptMessage = m_promptMessage.replace("%C%", "\n");
			inputData = new InputData(null, m_model.getProcId(), m_promptMessage, m_promptScope, m_promptOptions, m_promptExpected, "", false,
			        PromptDisplayType.RADIO);
		}
		else
		{
			inputData = new InputData(null, m_model.getProcId(), m_promptMessage, m_promptScope, "", m_numericPrompt, false);
		}
		m_model.getController().notifyProcedurePrompt(inputData);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void notifyFinalStatus( AsRunReplayResult result )
	{
		// Send the status notification to the model
		m_model.getRuntimeProcessor().notifyProcedureStatus(m_retrievedStatus);
		
		if (m_retrievedError != null)
		{
			m_model.getController().setError(m_retrievedError);
		}

		if (m_retrievedLine == false)
		{
			Logger.error("Unable to retrieve current line information", Level.PROC, this);
			result.message = "could not gather line information from ASRUN";
			result.status = AsRunProcessing.FAILED;
		}
	}
}
