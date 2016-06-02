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

package com.github.commandsconsolegui.cmd.jmegui;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.IConsoleUI;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.BasePlusAppState;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.SimpleApplication;

/**
 * This is not a thread.
 * This is a state, at the main thread, that will keep the console running
 * while it is closed.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CommandsBackgroundState extends BasePlusAppState implements IReflexFillCfg{
	private static CommandsBackgroundState instance = new CommandsBackgroundState();
	public static CommandsBackgroundState i(){return instance;}
	
	protected BoolTogglerCmdField	btgExecCommandsInBackground=new BoolTogglerCmdField(this, true, BoolTogglerCmdField.strTogglerCodePrefix,
		"Will continue running console commands even if console is closed.");
	
	private IConsoleUI	cgsaGraphicalConsoleUI;
	private CommandsDelegatorI	ccCommandsPipe;

	private SimpleApplication	sapp;

	private boolean	bConfigured;
	
	/**
	 * expected params:
	 * {@link IConsoleUI}
	 */
	@Override
	public void configure(Object... aobj) {
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		
		for(Object obj:aobj){
			if(obj instanceof IConsoleUI){
				this.cgsaGraphicalConsoleUI = (IConsoleUI)obj;
			}else{
				throw new NullPointerException("invalid/unexpected object class, not supported: "+obj.getClass().getName());
			}
		}
		
		this.sapp=GlobalSappRefI.i().get();
		this.ccCommandsPipe=GlobalCommandsDelegatorI.i().get();
		
//		this.bEnabled=true; //just to let it be initialized at startup by state manager
		
		if(!sapp.getStateManager().attach(this))throw new NullPointerException("already attached state "+this.getClass().getName());
		
//		btgExecCommandsInBackground=new BoolToggler(this, false, ConsoleCommands.strTogglerCodePrefix,
//			"Will continue running console commands even if console is closed.");
		
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
		
		bConfigured=true;
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
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return ccCommandsPipe.getReflexFillCfg(rfcv);
	}

	@Override
	protected void onEnable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDisable() {
		// TODO Auto-generated method stub
		
	}
	
}
