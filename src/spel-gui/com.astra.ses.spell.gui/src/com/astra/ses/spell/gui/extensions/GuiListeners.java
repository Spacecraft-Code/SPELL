///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : GuiListeners.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.extensions;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.astra.ses.spell.gui.core.interfaces.listeners.IBaseListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreApplicationStatusListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreClientOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreServerOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ApplicationStatus;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ExecutorOperationSummary;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureItemsListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureMessageListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStackListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStatusListener;
import com.astra.ses.spell.gui.model.properties.AsRunOpenTester;
import com.astra.ses.spell.gui.model.properties.ProcedureConfigurableTester;
import com.astra.ses.spell.gui.model.properties.ProcedureOpenTester;
import com.astra.ses.spell.gui.model.properties.ProcedurePausedTester;
import com.astra.ses.spell.gui.procs.extensionpoints.IProcedureListener;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureModelListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

class GuiListeners
{
	// Listeners for notifications from PROCS plugin
	private Collection<IProcedureListener> m_procedureListeners = new ArrayList<IProcedureListener>();
	private Collection<IProcedureModelListener> m_procedureModelListeners = new ArrayList<IProcedureModelListener>();

	// Listeners for application status
	private Collection<ICoreApplicationStatusListener> m_applicationStatusListeners = new ArrayList<ICoreApplicationStatusListener>();
	
	// Listeners for GUI categorized events
	private Collection<IGuiProcedureMessageListener> m_procedureMessageListeners = new ArrayList<IGuiProcedureMessageListener>();
	private Collection<IGuiProcedurePromptListener> m_procedurePromptListeners = new ArrayList<IGuiProcedurePromptListener>();
	private Collection<IGuiProcedureItemsListener> m_procedureItemListeners = new ArrayList<IGuiProcedureItemsListener>();
	private Collection<IGuiProcedureStackListener> m_procedureStackListeners = new ArrayList<IGuiProcedureStackListener>();
	private Collection<IGuiProcedureStatusListener> m_procedureStatusListeners = new ArrayList<IGuiProcedureStatusListener>();

	// Listeners for server operations
	private Collection<ICoreProcedureOperationListener> m_procedureOperationListeners = new ArrayList<ICoreProcedureOperationListener>();
	private Collection<ICoreClientOperationListener> m_clientOperationListeners = new ArrayList<ICoreClientOperationListener>();
	private Collection<ICoreContextOperationListener> m_contextOperationListeners = new ArrayList<ICoreContextOperationListener>();
	private Collection<ICoreServerOperationListener> m_serverOperationListeners = new ArrayList<ICoreServerOperationListener>();

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addListener(IBaseListener listener, Class<?> api)
	{
		Logger.debug("Add listener " + listener.getListenerId() + " for API " + api.getCanonicalName(), Level.PROC, this);
		if (api.equals(IProcedureModelListener.class))
		{
			if (!m_procedureModelListeners.contains(listener))
			{
				m_procedureModelListeners.add((IProcedureModelListener) listener);
			}
		}
		else if (api.equals(IProcedureListener.class))
		{
			if (!m_procedureListeners.contains(listener))
			{
				m_procedureListeners.add((IProcedureListener) listener);
			}
		}
		else if (api.equals(IGuiProcedureMessageListener.class))
		{
			if (!m_procedureMessageListeners.contains(listener))
			{
				m_procedureMessageListeners.add((IGuiProcedureMessageListener) listener);
			}
		}
		else if (api.equals(IGuiProcedurePromptListener.class))
		{
			if (!m_procedurePromptListeners.contains(listener))
			{
				m_procedurePromptListeners.add((IGuiProcedurePromptListener) listener);
			}
		}
		else if (api.equals(IGuiProcedureItemsListener.class))
		{
			if (!m_procedureItemListeners.contains(listener))
			{
				m_procedureItemListeners.add((IGuiProcedureItemsListener) listener);
			}
		}
		else if (api.equals(IGuiProcedureStackListener.class))
		{
			if (!m_procedureStackListeners.contains(listener))
			{
				m_procedureStackListeners.add((IGuiProcedureStackListener) listener);
			}
		}
		else if (api.equals(IGuiProcedureStatusListener.class))
		{
			if (!m_procedureStatusListeners.contains(listener))
			{
				m_procedureStatusListeners.add((IGuiProcedureStatusListener) listener);
			}
		}
		else if (api.equals(ICoreProcedureOperationListener.class))
		{
			if (!m_procedureOperationListeners.contains(listener))
			{
				m_procedureOperationListeners.add((ICoreProcedureOperationListener) listener);
			}
		}
		else if (api.equals(ICoreClientOperationListener.class))
		{
			if (!m_clientOperationListeners.contains(listener))
			{
				m_clientOperationListeners.add((ICoreClientOperationListener) listener);
			}
		}
		else if (api.equals(ICoreServerOperationListener.class))
		{
			if (!m_serverOperationListeners.contains(listener))
			{
				m_serverOperationListeners.add((ICoreServerOperationListener) listener);
			}
		}
		else if (api.equals(ICoreContextOperationListener.class))
		{
			if (!m_contextOperationListeners.contains(listener))
			{
				m_contextOperationListeners.add((ICoreContextOperationListener) listener);
			}
		}
		else if (api.equals(ICoreApplicationStatusListener.class))
		{
			if (!m_applicationStatusListeners.contains(listener))
			{
				m_applicationStatusListeners.add((ICoreApplicationStatusListener) listener);
			}
		}
		else
		{
			Logger.error("Unknown listener class: " + listener, Level.GUI, this);
		}
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeListener(IBaseListener listener)
	{
		Logger.debug("Remove listener " + listener.getListenerId(), Level.PROC, this);
		if (m_procedureModelListeners.contains(listener))
		{
			m_procedureModelListeners.remove(listener);
		}
		if (m_procedureListeners.contains(listener))
		{
			m_procedureListeners.remove( listener);
		}
		if (m_procedureMessageListeners.contains(listener))
		{
			m_procedureMessageListeners.remove( listener);
		}
		if (m_procedurePromptListeners.contains(listener))
		{
			m_procedurePromptListeners.remove( listener);
		}
		if (m_procedureItemListeners.contains(listener))
		{
			m_procedureItemListeners.remove(listener);
		}
		if (m_procedureStackListeners.contains(listener))
		{
			m_procedureStackListeners.remove( listener);
		}
		if (m_procedureStatusListeners.contains(listener))
		{
			m_procedureStatusListeners.remove(listener);
		}
		if (m_procedureOperationListeners.contains(listener))
		{
			m_procedureOperationListeners.remove( listener);
		}
		if (m_clientOperationListeners.contains(listener))
		{
			m_clientOperationListeners.remove(listener);
		}
		if (m_serverOperationListeners.contains(listener))
		{
			m_serverOperationListeners.remove( listener);
		}
		if (m_contextOperationListeners.contains(listener))
		{
			m_contextOperationListeners.remove(listener);
		}
		if (m_applicationStatusListeners.contains(listener))
		{
			m_applicationStatusListeners.remove(listener);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void refreshEvaluationService()
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
		{
			IEvaluationService svc = (IEvaluationService) window.getService(IEvaluationService.class);
			if (svc != null)
			{
				Logger.debug("Refresh evaluation service", Level.PROC, this);
				svc.requestEvaluation(ProcedurePausedTester.ID);
				svc.requestEvaluation(ProcedureConfigurableTester.ID);
				svc.requestEvaluation(AsRunOpenTester.ID);
				svc.requestEvaluation(ProcedureOpenTester.ID);
			}
		}
	}

	// ==========================================================================
	// EVENT FIRE METHODS
	// ==========================================================================

	protected void fireModelDisabled(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();
				for (IProcedureModelListener listener : m_procedureModelListeners)
				{
					try
					{
						Logger.debug("Notify [model disabled] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyProcedureModelDisabled(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireModelEnabled(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{

			@Override
			public void run()
			{
				refreshEvaluationService();
				for (IProcedureModelListener listener : m_procedureModelListeners)
				{
					try
					{
						Logger.debug("Notify [model enabled] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyProcedureModelEnabled(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireModelLoaded(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{

			@Override
			public void run()
			{
				for (IProcedureModelListener listener : m_procedureModelListeners)
				{
					try
					{
						Logger.debug("Notify [model loaded] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyProcedureModelLoaded(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireModelReset(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{

			@Override
			public void run()
			{
				for (IProcedureModelListener listener : m_procedureModelListeners)
				{
					try
					{
						Logger.debug("Notify [model reset] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyProcedureModelReset(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				}
			}
		});
	}

	// ==========================================================================
	protected void fireModelUnloaded(final IProcedure model, final UnloadType type)
	{
		Display.getDefault().syncExec(new Runnable()
		{

			@Override
			public void run()
			{
				for (IProcedureModelListener listener : m_procedureModelListeners)
				{
					try
					{
						Logger.debug("Notify [model unloaded] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyProcedureModelUnloaded(model, type);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireModelConfigured(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureModelListener listener : m_procedureModelListeners)
				{
					try
					{
						Logger.debug("Notify [model configured] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyProcedureModelConfigured(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureDisplay(final IProcedure model, final DisplayData data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureListener clt : m_procedureListeners)
				{
					try
					{
						//Logger.debug("Notify [procedure display] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
						clt.notifyProcedureDisplay(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IGuiProcedureMessageListener listener : m_procedureMessageListeners)
				{
					try
					{
						//Logger.debug("Notify [procedure display] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyDisplay(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureError(final IProcedure model, final ErrorData data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureListener clt : m_procedureListeners)
				{
					try
					{
						Logger.debug("Notify [procedure error] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
						clt.notifyProcedureError(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IGuiProcedureStatusListener listener : m_procedureStatusListeners)
				{
					try
					{
						Logger.debug("Notify [procedure error] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyError(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureItem(final IProcedure model, final ItemNotification data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureListener clt : m_procedureListeners)
				{
					try
					{
						//Logger.debug("Notify [procedure item] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
						clt.notifyProcedureItem(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IGuiProcedureItemsListener listener : m_procedureItemListeners)
				{
					try
					{
						//Logger.debug("Notify [procedure item] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyItem(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureStack(final IProcedure model, final StackNotification data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureListener clt : m_procedureListeners)
				{
					try
					{
						//Logger.debug("Notify [procedure stack] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
						clt.notifyProcedureStack(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IGuiProcedureStackListener listener : m_procedureStackListeners)
				{
					try
					{
						//Logger.debug("Notify [procedure stack] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyStack(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureStatus(final IProcedure model, final StatusNotification data)
	{
		try
		{
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					refreshEvaluationService();

					for (IProcedureListener clt : m_procedureListeners)
					{
						try
						{
							Logger.debug("Notify [procedure status] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
							clt.notifyProcedureStatus(model, data);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
					for (IGuiProcedureStatusListener listener : m_procedureStatusListeners)
					{
						try
						{
							Logger.debug("Notify [procedure status] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
							listener.notifyStatus(model, data);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			});
		}
		catch (Exception ex)
		{
		}
		;
	}

	// ==========================================================================
	protected void fireProcedurePrompt(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureListener clt : m_procedureListeners)
				{
					try
					{
						Logger.debug("Notify [prompt start] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
						clt.notifyProcedurePrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IGuiProcedurePromptListener listener : m_procedurePromptListeners)
				{
					try
					{
						Logger.debug("Notify [prompt start] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureFinishPrompt(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureListener clt : m_procedureListeners)
				{
					try
					{
						Logger.debug("Notify [prompt finish] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
						clt.notifyProcedureFinishPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IGuiProcedurePromptListener listener : m_procedurePromptListeners)
				{
					try
					{
						Logger.debug("Notify [prompt finish] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyFinishPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureCancelPrompt(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureListener clt : m_procedureListeners)
				{
					try
					{
						Logger.debug("Notify [prompt cancel] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
						clt.notifyProcedureCancelPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				}
				for (IGuiProcedurePromptListener listener : m_procedurePromptListeners)
				{
					try
					{
						Logger.debug("Notify [prompt cancel] to " + listener.getListenerId(), Level.PROC, GuiListeners.class);
						listener.notifyCancelPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureUserAction(final IProcedure model, final UserActionNotification data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureListener clt : m_procedureListeners)
				{
					try
					{
						Logger.debug("Notify [procedure uaction] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
						clt.notifyProcedureUserAction(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureClosed(final String procId, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure closed] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureClosed(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureControlled(final String procId, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure controlled] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureControlled(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureKilled(final String procId, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure killed] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureKilled(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureCrashed(final String procId, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure crashed] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureCrashed(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureMonitored(final String procId, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure monitored] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureMonitored(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureOpen(final String procId, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure open] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureOpen(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureReleased(final String procId, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure released] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureReleased(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureStatus(final String procId, final ExecutorStatus status, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure status] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureStatus(procId, status, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireProcedureSummary(final String procId, final ExecutorOperationSummary summary, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreProcedureOperationListener mon : m_procedureOperationListeners)
				{
					try
					{
						Logger.debug("Notify [procedure summary] to " + mon.getListenerId(), Level.PROC, GuiListeners.class);
						mon.notifyRemoteProcedureSummary(procId, summary, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	protected void fireClientConnected(final String clientKey, final String host)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreClientOperationListener clt : m_clientOperationListeners)
				{
					Logger.debug("Notify [client connected] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyClientConnected(clientKey, host);
				}
			}
		});
	}

	// ==========================================================================
	protected void fireClientDisconnected(final String clientKey, final String host)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreClientOperationListener clt : m_clientOperationListeners)
				{
					Logger.debug("Notify [client disconnected] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyClientDisconnected(clientKey, host);
				}
			}
		});
	}

	// ==========================================================================
	protected void fireContextAttached(final ContextInfo info)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreContextOperationListener clt : m_contextOperationListeners)
				{
					Logger.debug("Notify [context attached] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyContextAttached(info);
				}
			}
		});
	}

	// ==========================================================================
	protected void fireContextDetached()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreContextOperationListener clt : m_contextOperationListeners)
				{
					Logger.debug("Notify [context detached] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyContextDetached();
				}
			}
		});
	}

	// ==========================================================================
	protected void fireListenerConnected(final ServerInfo info)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreServerOperationListener clt : m_serverOperationListeners)
				{
					Logger.debug("Notify [listener connected] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyListenerConnected(info);
				}
			}
		});
	}

	// ==========================================================================
	protected void fireListenerDisconnected()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreServerOperationListener clt : m_serverOperationListeners)
				{
					Logger.debug("Notify [listener disconnected] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyListenerDisconnected();
				}
			}
		});
	}

	// ==========================================================================
	protected void fireListenerError(final ErrorData data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreServerOperationListener clt : m_serverOperationListeners)
				{
					Logger.debug("Notify [listener error] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyListenerError(data);
				}
			}
		});
	}

	// ==========================================================================
	protected void fireContextStarted(final ContextInfo info)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreServerOperationListener clt : m_serverOperationListeners)
				{
					Logger.debug("Notify [context started] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyContextStarted(info);
				}
			}
		});
	}

	// ==========================================================================
	protected void fireContextStopped(final ContextInfo info)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreServerOperationListener clt : m_serverOperationListeners)
				{
					Logger.debug("Notify [context stopped] to " + clt.getListenerId(), Level.PROC, GuiListeners.this);
					clt.notifyContextStopped(info);
				}
			}
		});
	}

	// ==========================================================================
	protected void fireContextError(final ErrorData data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ICoreContextOperationListener clt : m_contextOperationListeners)
				{
					Logger.debug("Notify [context error] to " + clt.getListenerId(), Level.PROC, GuiListeners.class);
					clt.notifyContextError(data);
				}
			}
		});
	}

	// ==========================================================================
    protected void fireApplicationStatus( final ApplicationStatus status )
    {
	    Display.getDefault().syncExec( new Runnable()
	    {
	    	public void run()
	    	{
	    		for(ICoreApplicationStatusListener listener : m_applicationStatusListeners)
	    		{
	    			listener.onApplicationStatus(status);
	    		}
	    	}
	    });
	    
    }
}
