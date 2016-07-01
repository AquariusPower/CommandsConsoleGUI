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

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.extras.SingleAppInstanceI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.console.GlobalConsoleGuiI;
import com.github.commandsconsolegui.jmegui.console.SimpleConsoleAppAbs;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CustomTestI<T extends Command<Button>> extends SimpleConsoleAppAbs implements IReflexFillCfg{
	private static CustomTestI instance = new CustomTestI();
	public static CustomTestI i(){return instance;}
	
	protected boolean bHideSettings=true; 
	
	//private final String strFinalFieldCodePrefix="CMD_";
	private final String strFieldCodePrefix="sf";
	private final String strFieldCodePrefixLess = "VariantAsPrefixLess";
	
//	public final StringCmdField CMD_END_DEVELOPER_COMMAND_TEST = new StringCmdField(
//		this,CustomCommands.strFinalCmdCodePrefix);
	public final StringCmdField scfEndDeveloperCommandTest = new StringCmdField(this);
	public final StringCmdField scfHelp = new StringCmdField(this);
	
	/**
	 * these below were not implemented as commands here, 
	 * are just to exemplify how the auto-fill works.
	 */
	private StringCmdField sfTestCommandAutoFillVariant1 = new StringCmdField(this,strFieldCodePrefix);
	private StringCmdField testCommandAutoFillPrefixLessVariant2 = new StringCmdField(this,strFieldCodePrefixLess);
	private StringCmdField testCommandAutoFillPrefixLessVariantDefaulted3 = new StringCmdField(this,null);
	private StringCmdField CMD_TRADITIONAL_PRETTYFIED_0 = new StringCmdField(this,CustomCommands.strFinalCmdCodePrefix);
	
//	private StringVarField svfOptionSelectedDialog2 = new StringVarField(this,"");
	
	// generic dialog
	private CustomDialogGUIState<T>	diagCfg;

	private CustomDialogGUIState<T>	diag;

	private CustomDialogGUIState<T>	diagConfirm;

//	private String	strOptionSelected;
	
	public CustomTestI() {
		super();
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
	}
	
	public boolean endDevCustomMethod(Integer i){
		CommandsDelegator cd = GlobalCommandsDelegatorI.i();
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
		//////////////////////// GLOBALS
		// the commands pipe
		GlobalCommandsDelegatorI.iGlobal().set(new CustomCommands());
		
		// the conole UI
		GlobalConsoleGuiI.iGlobal().set(ConsoleLemurStateI.i());
		ConsoleLemurStateI.i().configure(new ConsoleLemurStateI.CfgParm(
				null, KeyInput.KEY_F10, getGuiNode()));
		
		//////////////////////// super init depends on globals
		super.simpleInitApp(); // basic initializations
		
		//////////////////////// config this test
		
		// test dialogs
		diagCfg = new CustomDialogGUIState<T>(CustomDialogGUIState.EDiag.Cfg).configure(
			new CustomDialogGUIState.CfgParm(true, 0.6f, 0.5f, null, null));//, null));
		
		diagConfirm = new CustomDialogGUIState<T>(CustomDialogGUIState.EDiag.Confirm).configure(
			new CustomDialogGUIState.CfgParm(true, 500f, 300f, null, null));//, diag));
		
		diag = new CustomDialogGUIState<T>(CustomDialogGUIState.EDiag.List).configure(
			new CustomDialogGUIState.CfgParm(false, null, null, null, null))//, null))
			.addModalDialog(diagCfg)
			.addModalDialog(diagConfirm);
		
	}
	
	/**
	 * this is here just to help
	 */
	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}
	
	public static void main( String... args ) {
		GlobalAppRefI.iGlobal().set(CustomTestI.i());
		
		if(CustomTestI.i().bHideSettings){
			AppSettings as = new AppSettings(true);
			as.setResizable(true);
			as.setWidth(1024);
			as.setHeight(768);
			//as.setFrameRate(30); //TODO are we unable to change this on-the-fly after starting the application?
			CustomTestI.i().setSettings(as);
			CustomTestI.i().setShowSettings(false);
		}
		
		SingleAppInstanceI.i().configureOptionalAtMainMethod();
		
		CustomTestI.i().start();
	}

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator	cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,scfEndDeveloperCommandTest,"[iHowMany] users working?")){
			bCommandWorked = endDevCustomMethod(cd.getCurrentCommandLine().paramInt(1));
		}else
		if(cd.checkCmdValidity(this,scfHelp,"specific custom help")){
			cd.dumpSubEntry("custom help");
			bCommandWorked = true;
		}else
//		if(cc.checkCmdValidity(this,"conflictTest123","")){
//		}else
		{
			return ECmdReturnStatus.NotFound;
		}
			
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(StringCmdField.class)){
			if(strFieldCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg(rfcv);
				rfcfg.setPrefixCmd("Niceprefix");
				rfcfg.setSuffix("Nicesuffix");
				rfcfg.setFirstLetterUpperCase(true);
			}else
			if(strFieldCodePrefixLess.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg(rfcv);
				rfcfg.setCodingStyleFieldNamePrefix(null);
				rfcfg.setFirstLetterUpperCase(true);
			}
		}
		
		/**
		 * If you are coding in the same style of another class,
		 * just call it!
		 * Remember to set the same variant!
		 */
		if(rfcfg==null)rfcfg = super.getReflexFillCfg(rfcv);//cd.getReflexFillCfg(rfcv);
		
		return rfcfg;
	}
	
}	