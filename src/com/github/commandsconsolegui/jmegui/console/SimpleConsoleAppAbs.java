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

package com.github.commandsconsolegui.jmegui.console;

import java.util.concurrent.Callable;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.extras.SingleAppInstanceI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.globals.jmegui.GlobalRootNodeI;
import com.github.commandsconsolegui.globals.jmegui.console.GlobalConsoleGuiI;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.cmd.CommandsBackgroundStateI;
import com.github.commandsconsolegui.jmegui.extras.FpsLimiterStateI;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;


/**
* 
* @author AquariusPower <https://github.com/AquariusPower>
*
*/
public abstract class SimpleConsoleAppAbs extends SimpleApplication implements IConsoleCommandListener, IReflexFillCfg {
	public final BoolTogglerCmdField	btgFpsLimit=new BoolTogglerCmdField(this,false);
	protected boolean bHideSettings=true; 
	
//	protected CommandsDelegator	cd;
	
	public SimpleConsoleAppAbs() {
		super();
	}
	
  public SimpleConsoleAppAbs(AppState... initialStates) {
    super(initialStates);
  }
	
	@Override
	public void simpleInitApp() {
		MiscJmeI.i().configure(GlobalCommandsDelegatorI.i());
		
		GlobalGUINodeI.iGlobal().set(getGuiNode());
		GlobalRootNodeI.iGlobal().set(getRootNode());
		
		CommandsBackgroundStateI.i().configure(new CommandsBackgroundStateI.CfgParm(
			GlobalConsoleGuiI.i()));
		
		FpsLimiterStateI.i().configure(new FpsLimiterStateI.CfgParm());
		btgFpsLimit.setCallOnChange(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				FpsLimiterStateI.i().setEnabledRequest(btgFpsLimit.b());
				return true;
			}
		});
		
		UngrabMouseStateI.i().configure(new UngrabMouseStateI.CfgParm(null,null));
		
		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
		
//	SingleInstanceState.i().configureBeforeInitializing(this,true);
		SingleAppInstanceI.i().configureRequiredAtApplicationInitialization();//cc);
	}
	
	@Override
	public void stop(boolean waitFor) {
		GlobalAppRefI.iGlobal().setAppExiting(true);
//		ConditionalStateManagerI.i().applicationIsExiting();
//		UngrabMouseStateI.i().applicationIsExiting();
		super.stop(waitFor);
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}
}	
