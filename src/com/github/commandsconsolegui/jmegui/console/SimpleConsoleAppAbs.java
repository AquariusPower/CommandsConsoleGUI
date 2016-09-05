/* 
Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>

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

import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.extras.SingleAppInstanceI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.GlobalGUINodeI;
import com.github.commandsconsolegui.globals.jmegui.GlobalRootNodeI;
import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.cmd.CommandsBackgroundStateI;
import com.github.commandsconsolegui.jmegui.extras.FpsLimiterStateI;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jmegui.lemur.MouseCursorListenerAbs;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.misc.IConfigure;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.input.KeyInput;
import com.simsilica.lemur.event.KeyInterceptState;
import com.simsilica.lemur.event.MouseEventControl;


/**
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
public abstract class SimpleConsoleAppAbs extends SimpleApplication implements IConsoleCommandListener, IReflexFillCfg, IConfigure<SimpleConsoleAppAbs> {
//	private boolean bHideSettings=true;
//	private boolean	bStopping;
	private boolean	bConfigured; 
	
//	private CommandsDelegator	cd;
	
	public SimpleConsoleAppAbs() {
		super();
	}
	
  public SimpleConsoleAppAbs(AppState... initialStates) {
    super(initialStates);
  }
	
  @Override
  public boolean isConfigured() {
  	return bConfigured;
  }
  
  public static class CfgParm implements ICfgParm {
  }
  private CfgParm cfg = null;
  @Override
  public SimpleConsoleAppAbs configure(ICfgParm icfg) {
  	cfg = (CfgParm)icfg;
  	
  	// globals must be set as soon as possible
		GlobalGUINodeI.iGlobal().set(getGuiNode());
		GlobalRootNodeI.iGlobal().set(getRootNode());
		
		/**
		 * Configs:
		 * 
		 * Shall be simple enough to not require any exact order.
		 * 
		 * Anything more complex can be postponed (from withing the config itself)
		 * with {@link CallQueueI}, or just put these things at initialization method.
		 */
		GlobalCommandsDelegatorI.i().configure();//ConsoleLemurStateI.i());
		ConsoleLemurStateI.i().configure(new ConsoleLemurStateI.CfgParm(
			null, KeyInput.KEY_F10));
  	
		MiscJmeI.i().configure(GlobalCommandsDelegatorI.i());
		
	//	CommandsBackgroundStateI.i().configure(new CommandsBackgroundStateI.CfgParm(GlobalConsoleGuiI.i()));
		CommandsBackgroundStateI.i().configure(new CommandsBackgroundStateI.CfgParm());
		
		FpsLimiterStateI.i().configure(new FpsLimiterStateI.CfgParm());
		
		UngrabMouseStateI.i().configure(new UngrabMouseStateI.CfgParm(null,null));
		
		AudioUII.i().configure(new AudioUII.CfgParm(
			DialogMouseCursorListenerI.class,
			MouseCursorListenerAbs.class, 
			MouseEventControl.class,
			KeyInterceptState.class));
		
		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
	
		bConfigured = true;
		
  	return this;
  }
  
	@Override
	public void simpleInitApp() {
		assertConfigured();
//	SingleInstanceState.i().configureBeforeInitializing(this,true);
		SingleAppInstanceI.i().configureRequiredAtApplicationInitialization();//cc);
	}
	
	/**
	 * PUT NOTHING HERE!
	 * Override just to tell you this: put stuff at {@link #destroy()}
	 */
	@Override
	public void stop(boolean waitFor) {
		super.stop(waitFor);
	}
	
	/**
	 * this is called when window is closed using close button too
	 */
	@Override
	public void destroy() {
		GlobalAppRefI.iGlobal().setAppExiting();
//		if(!bStopping)stop();//		GlobalAppRefI.iGlobal().setAppExiting();
		super.destroy();
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}

	@Override
	public void assertConfigured() {
	}
}	
