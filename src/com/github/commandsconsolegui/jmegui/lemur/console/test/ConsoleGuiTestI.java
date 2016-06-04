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

package com.github.commandsconsolegui.jmegui.lemur.console.test;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.extras.SingleAppInstanceI;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalConsoleGuiI;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.cmd.CommandsBackgroundState;
import com.github.commandsconsolegui.jmegui.extras.FpsLimiterStateI;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleGuiTestI extends SimpleApplication implements IConsoleCommandListener, IReflexFillCfg{
	private static ConsoleGuiTestI instance = new ConsoleGuiTestI();
	public static ConsoleGuiTestI i(){return instance;}
	
	protected boolean bHideSettings=true; 
	
	//private final String strFinalFieldCodePrefix="CMD_";
	private final String strFieldCodePrefix="sf";
	private final String strFieldCodePrefixLess = "VariantAsPrefixLess";
	
	public final StringCmdField CMD_END_USER_COMMAND_TEST = new StringCmdField(this,CustomCommandsI.strFinalCmdCodePrefix);
	
	/**
	 * these below were not implemented as commands here, 
	 * are just to exemplify how the auto-fill works.
	 */
	private StringCmdField sfTestCommandAutoFillVariant1 = new StringCmdField(this,strFieldCodePrefix);
	private StringCmdField testCommandAutoFillPrefixLessVariant2 = new StringCmdField(this,strFieldCodePrefixLess);
	private StringCmdField testCommandAutoFillPrefixLessVariantDefaulted3 = new StringCmdField(this,null);
	private StringCmdField CMD_TRADITIONAL_PRETTYFIED_0 = new StringCmdField(this,CustomCommandsI.strFinalCmdCodePrefix);
	private CustomDialogGUIState	diag;

	private CommandsDelegatorI	cd;
	
	public boolean endUserCustomMethod(Integer i){
		cd.dumpSubEntry("Shhh.. "+i+" end user(s) working!");
		
		cd.dumpSubEntry("CommandTest0: "+CMD_TRADITIONAL_PRETTYFIED_0);
		cd.dumpSubEntry("CommandTest1: "+sfTestCommandAutoFillVariant1);
		cd.dumpSubEntry("CommandTest2: "+testCommandAutoFillPrefixLessVariant2);
		if(ReflexFillI.i().isbUseDefaultCfgIfMissing()){
			cd.dumpSubEntry("CommandTest3: "+testCommandAutoFillPrefixLessVariantDefaulted3);
		}
		return true;
	}
	
	@Override
	public void simpleInitApp() {
		cd = GlobalCommandsDelegatorI.i().set(new CustomCommandsI());
		MiscJmeI.i().configure(cd);
		
		GlobalConsoleGuiI.i().set(ConsoleLemurStateI.i());
		
		ConsoleLemurStateI.i().configure(new ConsoleLemurStateI.CfgParm(
				ConsoleGuiTestI.class.getSimpleName(), false, KeyInput.KEY_F10, getGuiNode()));
		CommandsBackgroundState.i().configure(new CommandsBackgroundState.CfgParm(
				ConsoleLemurStateI.i()));
		FpsLimiterStateI.i().configure(null);
		UngrabMouseStateI.i().configure(new UngrabMouseStateI.CfgParm(
			null,null));

		cd.addConsoleCommandListener(this);
		
		diag = new CustomDialogGUIState();
		diag.configure("TestDialog",false);
		
//		SingleInstanceState.i().configureBeforeInitializing(this,true);
		SingleAppInstanceI.i().configureRequiredAtApplicationInitialization();//cc);
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}
	
	public static void main( String... args ) {
		ConsoleGuiTestI main = (ConsoleGuiTestI) GlobalSappRefI.i().set(ConsoleGuiTestI.i());
		main.configure();
		
		if(main.bHideSettings){
			AppSettings as = new AppSettings(true);
			as.setResizable(true);
			as.setWidth(1024);
			as.setHeight(768);
			//as.setFrameRate(30);
			main.setSettings(as);
			main.setShowSettings(false);
		}
		
		SingleAppInstanceI.i().configureOptionalAtMainMethod();
		
		main.start();
	}

	private void configure() {
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
	}

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI	cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,CMD_END_USER_COMMAND_TEST,"[iHowMany] users working?")){
			bCommandWorked = endUserCustomMethod(cc.paramInt(1));
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
			
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(StringCmdField.class)){
			if(strFieldCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCommandPrefix = "Niceprefix";
				rfcfg.strCommandSuffix = "Nicesuffix";
				rfcfg.bFirstLetterUpperCase = true;
			}else
			if(strFieldCodePrefixLess.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCodingStyleFieldNamePrefix=null;
				rfcfg.bFirstLetterUpperCase = true;
			}
		}
		
		/**
		 * If you are coding in the same style of another class,
		 * just call it!
		 * Remember to set the same variant!
		 */
//		if(rfcfg==null)rfcfg = getReflexFillCfg(rfcv);
		if(rfcfg==null)rfcfg = cd.getReflexFillCfg(rfcv);
		
		return rfcfg;
	}
}	
