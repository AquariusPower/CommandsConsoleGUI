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

package com.github.commandsconsolegui.jme.lemur.console;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.SimulationTime;
import com.github.commandsconsolegui.cmd.ScriptingCommandsDelegator;
import com.github.commandsconsolegui.globals.GlobalMainThreadI;
import com.github.commandsconsolegui.globals.GlobalOperationalSystemI;
import com.github.commandsconsolegui.globals.GlobalSimulationTimeI;
import com.github.commandsconsolegui.globals.GlobalSingleAppInstanceI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jme.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jme.GlobalGUINodeI;
import com.github.commandsconsolegui.globals.jme.GlobalRootNodeI;
import com.github.commandsconsolegui.globals.jme.GlobalSimpleAppRefI;
import com.github.commandsconsolegui.globals.jme.lemur.GlobalLemurDialogHelperI;
import com.github.commandsconsolegui.jme.AudioUII;
import com.github.commandsconsolegui.jme.JMEOperationalSystem;
import com.github.commandsconsolegui.jme.cmd.CommandsBackgroundStateI;
import com.github.commandsconsolegui.jme.extras.FpsLimiterStateI;
import com.github.commandsconsolegui.jme.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.jme.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jme.lemur.MouseCursorListenerAbs;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogManagerI;
import com.github.commandsconsolegui.misc.Configure;
import com.github.commandsconsolegui.misc.Configure.IConfigure;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.system.JmeSystem.StorageFolderType;
import com.simsilica.lemur.event.KeyInterceptState;
import com.simsilica.lemur.event.MouseEventControl;


/**
* 
* TODO remove SimpleApplication dependency... make this a plugin like...
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
public class SimpleConsolePlugin implements IReflexFillCfg, IConfigure<SimpleConsolePlugin> {
	private boolean	bConfigured; 
	
	public SimpleConsolePlugin(Application app){
  	if(app instanceof SimpleApplication){
  		GlobalSimpleAppRefI.iGlobal().set((SimpleApplication)app);
  	}else{
  		GlobalAppRefI.iGlobal().set(app);
  	}
	}
	
  @Override
  public boolean isConfigured() {
  	return bConfigured;
  }
  
  public static class CfgParm implements ICfgParm {
  	private String strApplicationBaseSaveDataPath;

		public CfgParm(String strApplicationBaseSaveDataPath) {
			super();
			this.strApplicationBaseSaveDataPath = strApplicationBaseSaveDataPath;
		}
  	
  }
  private CfgParm cfg = null;
  @Override
  public SimpleConsolePlugin configure(ICfgParm icfg) {
  	cfg = (CfgParm)icfg;
  	
  	/**
  	 * every global can have its defaults here, or can be customized by you before here.
  	 */
  	if(!GlobalSimulationTimeI.iGlobal().isSet()){
  		GlobalSimulationTimeI.iGlobal().set(new SimulationTime(System.nanoTime()));
  	}
  	
  	if(!GlobalCommandsDelegatorI.iGlobal().isSet()){
  		// defaults to the more complex one
  		GlobalCommandsDelegatorI.iGlobal().set(new ScriptingCommandsDelegator());
  	}
  	
  	// globals must be set as soon as possible
  	if(GlobalSimpleAppRefI.iGlobal().isSet()){
  		if(!GlobalGUINodeI.iGlobal().isSet()){
    		GlobalGUINodeI.iGlobal().set(GlobalSimpleAppRefI.i().getGuiNode());
  		}
  		
  		if(!GlobalRootNodeI.iGlobal().isSet()){
  			GlobalRootNodeI.iGlobal().set(GlobalSimpleAppRefI.i().getRootNode());
  		}
  	}
  	
  	if(!GlobalLemurDialogHelperI.iGlobal().isSet()){
  		GlobalLemurDialogHelperI.iGlobal().set(LemurDialogManagerI.i());
  	}
  	
  	if(!GlobalOperationalSystemI.iGlobal().isSet()){
			GlobalOperationalSystemI.iGlobal().set(new JMEOperationalSystem(
				cfg.strApplicationBaseSaveDataPath, StorageFolderType.Internal));
  	}
  	
		/**
		 * Configs:
		 * 
		 * Shall be simple enough to not require any exact order.
		 * 
		 * Anything more complex can be postponed (from withing the config itself)
		 * with {@link CallQueueI}, or just put these things at initialization method.
		 */
		GlobalCommandsDelegatorI.i().configure();//ConsoleLemurStateI.i());
		LemurConsoleStateI.i().configure(new LemurConsoleStateI.CfgParm(
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
		
//		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
	
		bConfigured = true;
		
  	return this;
  }
  
	public SimpleConsolePlugin initialize() {
		Configure.assertConfigured(this);
		if(GlobalSingleAppInstanceI.iGlobal().isSet()){
			GlobalSingleAppInstanceI.i().configureRequiredAtApplicationInitialization();//cc);
		}
		GlobalMainThreadI.iGlobal().set(Thread.currentThread());
		return this;
	}
	
//	/**
//	 * PUT NOTHING HERE!
//	 * Override just to tell you this: put stuff at {@link #destroy()}
//	 */
//	@Override
//	public void stop(boolean waitFor) {
//		super.stop(waitFor);
//	}
//	
//	/**
//	 * this is called when window is closed using close button too
//	 */
//	@Override
//	public void destroy() {
//		GlobalAppRefI.iGlobal().setAppExiting();
////		if(!bStopping)stop();//		GlobalAppRefI.iGlobal().setAppExiting();
//		super.destroy();
//	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}
}	
