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

package com.github.commandsconsolegui.spLemur.console;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.SimulationTime;
import com.github.commandsconsolegui.spAppOs.globals.GlobalMainThreadI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalManageKeyBindI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalManageKeyCodeI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalSimulationTimeI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalSingleMandatoryAppInstanceI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalUpdaterI;
import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.ISimpleGetThisTrickIndicator;
import com.github.commandsconsolegui.spAppOs.misc.ISingleInstance;
import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI;
import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI.IConfigure;
import com.github.commandsconsolegui.spAppOs.misc.ManageSingleInstanceI;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spCmd.ScriptingCommandsDelegator;
import com.github.commandsconsolegui.spCmd.Updater;
import com.github.commandsconsolegui.spCmd.UserCmdStackTrace;
import com.github.commandsconsolegui.spJme.AudioUII;
import com.github.commandsconsolegui.spJme.ManageKeyBindJme;
import com.github.commandsconsolegui.spJme.ManageKeyCodeJme;
import com.github.commandsconsolegui.spJme.cmd.CommandsBackgroundStateI;
import com.github.commandsconsolegui.spJme.extras.FpsLimiterStateI;
import com.github.commandsconsolegui.spJme.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.spJme.globals.GlobalAppRefI;
import com.github.commandsconsolegui.spJme.globals.GlobalGUINodeI;
import com.github.commandsconsolegui.spJme.globals.GlobalJmeAppOSI;
import com.github.commandsconsolegui.spJme.globals.GlobalRootNodeI;
import com.github.commandsconsolegui.spJme.globals.GlobalSimpleAppRefI;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.github.commandsconsolegui.spLemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.spLemur.MouseCursorListenerAbs;
import com.github.commandsconsolegui.spLemur.OSAppLemur;
import com.github.commandsconsolegui.spLemur.dialog.ManageLemurDialogI;
import com.github.commandsconsolegui.spLemur.globals.GlobalLemurConsoleStateI;
import com.github.commandsconsolegui.spLemur.globals.GlobalLemurDialogHelperI;
import com.github.commandsconsolegui.spLemur.misc.LemurEffectsI;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.system.JmeSystem.StorageFolderType;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.event.KeyInterceptState;
import com.simsilica.lemur.event.MouseEventControl;


/**
* 
* TODO remove SimpleApplication dependency... make this a plugin like...
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
public class SimpleConsolePlugin implements IReflexFillCfg, ISingleInstance, IConfigure<SimpleConsolePlugin> {
	private boolean	bConfigured; 
	
	public SimpleConsolePlugin(Application app){
		if(Thread.getDefaultUncaughtExceptionHandler()==null){ //TODO is this too much???
//		if(Thread.currentThread().getUncaughtExceptionHandler()==Thread.currentThread().getThreadGroup()){
//			System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>"+Thread.currentThread().getName());
//			Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			/**
			 * this may never be reached as it is already set at {@link LwjglAbstractDisplay}
			 */
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
//					System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
					throw new PrerequisitesNotMetException("uncaught exception at thread "+t.getName()).initCauseAndReturnSelf(e);
				}
			});
		}
		
		DelegateManagerI.i().add(this);
//		ManageSingleInstanceI.i().add(this);
		
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
  	
  	setGlobals();
  	
  	if(!GlobalManageKeyCodeI.i().isConfigured()){
  		GlobalManageKeyCodeI.i().configure();
  	}
  	
  	if(!GlobalManageKeyBindI.i().isConfigured()){
  		GlobalManageKeyBindI.i().configure();
  	}
  	
		/**
		 * Configs:
		 * 
		 * Shall be simple enough to not require any exact order.
		 * 
		 * Anything more complex can be postponed (from withing the config itself)
		 * with {@link CallQueueI}, or just put these things at initialization method.
		 */
  	if(!GlobalLemurConsoleStateI.iGlobal().isSet()){
  		GlobalLemurConsoleStateI.iGlobal().set(new SimpleLemurConsoleState());
  	}
  	if(!GlobalLemurConsoleStateI.i().isConfigured()){
  		GlobalLemurConsoleStateI.i().configure(new SimpleLemurConsoleState.CfgParm(
				null, KeyInput.KEY_F10));
  	}
  	if(!GlobalCommandsDelegatorI.i().isConfigured()){
  		GlobalCommandsDelegatorI.i().configure();//ConsoleLemurStateI.i());
  	}
  	
  	if(!MiscJmeI.i().isConfigured()){
//  		MiscJmeI.i().configure(GlobalCommandsDelegatorI.i());
  		MiscJmeI.i().configure(new MiscJmeI.CfgParm());
  	}
		
	//	CommandsBackgroundStateI.i().configure(new CommandsBackgroundStateI.CfgParm(GlobalConsoleGuiI.i()));
  	if(!CommandsBackgroundStateI.i().isConfigured()){
  		CommandsBackgroundStateI.i().configure(new CommandsBackgroundStateI.CfgParm());
  	}
		
  	if(!FpsLimiterStateI.i().isConfigured()){
  		FpsLimiterStateI.i().configure(new FpsLimiterStateI.CfgParm());
  	}
		
  	if(!UngrabMouseStateI.i().isConfigured()){
  		UngrabMouseStateI.i().configure(new UngrabMouseStateI.CfgParm(null,null));
  	}
		
  	if(!UserCmdStackTrace.i().isConfigured()){
  		UserCmdStackTrace.i().configure(new UserCmdStackTrace.CfgParm(
				DialogMouseCursorListenerI.class,
				MouseCursorListenerAbs.class, 
				MouseEventControl.class,
				KeyInterceptState.class));
  	}
  	
  	if(!AudioUII.i().isConfigured()){
			AudioUII.i().configure(new AudioUII.CfgParm());
  	}
  	
  	if(!LemurEffectsI.i().isConfigured()){
  		LemurEffectsI.i().configure(null);
  	}
		
//		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
	
		bConfigured = true;
		
  	return this;
  }
  
	/**
	 * cannot be further extended for getThis() trick to work
	 */
  public static final class SimpleLemurConsoleState extends LemurConsoleStateAbs<Command<Button>, SimpleLemurConsoleState> implements ISimpleGetThisTrickIndicator{
		@Override public SimpleLemurConsoleState getThis() {return this;}
  }
  
	protected void setGlobals() {
  	/**
  	 * every global can have its defaults here, or can be customized by you before here.
  	 */
  	if(!GlobalUpdaterI.iGlobal().isSet()){
  		GlobalUpdaterI.iGlobal().set(new Updater());
  	}
		
  	if(!GlobalSimulationTimeI.iGlobal().isSet()){
  		GlobalSimulationTimeI.iGlobal().set(new SimulationTime(System.nanoTime()));
  	}
  	
  	if(!GlobalCommandsDelegatorI.iGlobal().isSet()){
  		// defaults to the more complex (scripting) one
  		GlobalCommandsDelegatorI.iGlobal().set(new ScriptingCommandsDelegator());
  	}
  	
  	if(!GlobalManageKeyCodeI.iGlobal().isSet()){
  		GlobalManageKeyCodeI.iGlobal().set(new ManageKeyCodeJme());
  	}
  	
  	if(!GlobalManageKeyBindI.iGlobal().isSet()){
  		GlobalManageKeyBindI.iGlobal().set(new ManageKeyBindJme());
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
  		GlobalLemurDialogHelperI.iGlobal().set(ManageLemurDialogI.i());
  	}
  	
  	if(!GlobalJmeAppOSI.iGlobal().isSet()){
			GlobalJmeAppOSI.iGlobal().set(new OSAppLemur(
				cfg.strApplicationBaseSaveDataPath, StorageFolderType.Internal));
  	}
	}

	public SimpleConsolePlugin initialize() {
		ManageConfigI.i().assertConfigured(this);
		
		if(GlobalSingleMandatoryAppInstanceI.iGlobal().isSet()){
			GlobalSingleMandatoryAppInstanceI.i().configureRequiredAtApplicationInitialization();//cc);
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

	@Override
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}

}	
