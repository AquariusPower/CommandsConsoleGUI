/* 
	Copyright (c) 2016, AquariusPower <https://github.com/AquariusPower>
	
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification, are permitted 
	provided that the following conditions are met:

	1.	Redistributions of source code must retain the above copyright notice, this list of conditions 
		and the following disclaimer.

	2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
		and the following disclaimer in the documentation and/or other materials provided with the distribution.
	
	3.	Neither the name of the copyright holder nor the names of its contributors may be used to endorse 
		or promote products derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
	PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
	LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN 
	IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.github.commandsconsolegui.console;

import com.github.commandsconsolegui.misc.BoolTogglerCmd;
import com.github.commandsconsolegui.misc.ReflexFill;
import com.github.commandsconsolegui.misc.ReflexFill.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFill.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFill.ReflexFillCfg;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;

/**
 * This is not a thread.
 * This is a state, at the main thread, that will keep the console running
 * while it is closed.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleCommandsBackgroundState implements AppState, IReflexFillCfg{
	private static ConsoleCommandsBackgroundState instance = new ConsoleCommandsBackgroundState();
	public static ConsoleCommandsBackgroundState i(){return instance;}
	
	protected BoolTogglerCmd	btgExecCommandsInBackground=new BoolTogglerCmd(this, false, BoolTogglerCmd.strTogglerCodePrefix,
		"Will continue running console commands even if console is closed.");
	
	private IConsoleUI	cgsaGraphicalConsoleUI;
	private ConsoleCommands	ccCommandsPipe;
	private boolean	bEnabled;
	private boolean	bInitialized;

	private SimpleApplication	sapp;

	private boolean	bConfigured;

	public void configure(SimpleApplication sapp, IConsoleUI cgsa, ConsoleCommands cc){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		
		this.sapp=sapp;
		this.bEnabled=true; //just to let it be initialized at startup by state manager
		this.cgsaGraphicalConsoleUI = cgsa;
		this.ccCommandsPipe=cc;
		
		if(!sapp.getStateManager().attach(this))throw new NullPointerException("already attached state "+this.getClass().getName());
		
//		btgExecCommandsInBackground=new BoolToggler(this, false, ConsoleCommands.strTogglerCodePrefix,
//			"Will continue running console commands even if console is closed.");
		
		ReflexFill.i().assertReflexFillFieldsForOwner(this);
		
		bConfigured=true;
	}
	
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		bInitialized=true;
	}

	@Override
	public boolean isInitialized() {
		return bInitialized;
	}

	@Override
	public void setEnabled(boolean b) {
		this.bEnabled=b;
	}

	@Override
	public boolean isEnabled() {
		return bEnabled;
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
	}

	@Override
	public void update(float tpf) {
		if(cgsaGraphicalConsoleUI.isEnabled())return; //foreground execution
		
		if(!btgExecCommandsInBackground.b())return;
		
		/**
		 * This way, being controlled by JME, the commands will happen in the same 
		 * rate they would with the other state.
		 */
		ccCommandsPipe.update(tpf);
	}

	@Override
	public void render(RenderManager rm) {
	}

	@Override
	public void postRender() {
	}

	@Override
	public void cleanup() {
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return ccCommandsPipe.getReflexFillCfg(rfcv);
	}
	
}
