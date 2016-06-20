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
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.extras.SingleAppInstanceI;
import com.github.commandsconsolegui.globals.GlobalAppRefI;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalConsoleGuiI;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI;
import com.github.commandsconsolegui.jmegui.console.SimpleConsoleAppAbs;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs.EModalDiagType;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.input.KeyInput;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CustomTestI extends SimpleConsoleAppAbs implements IReflexFillCfg{
	private static CustomTestI instance = new CustomTestI();
	public static CustomTestI i(){return instance;}
	
	protected boolean bHideSettings=true; 
	
	//private final String strFinalFieldCodePrefix="CMD_";
	private final String strFieldCodePrefix="sf";
	private final String strFieldCodePrefixLess = "VariantAsPrefixLess";
	
//	public final StringCmdField CMD_END_DEVELOPER_COMMAND_TEST = new StringCmdField(
//		this,CustomCommands.strFinalCmdCodePrefix);
	public final StringCmdField scfEndDeveloperCommandTest = new StringCmdField(this,"scf");
	
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
	private CustomDialogGUIState	diagCfg;

	private CustomDialogGUIState	diag;

//	private String	strOptionSelected;
	
	public CustomTestI() {
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
	}
	
	public boolean endDevCustomMethod(Integer i){
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
		// the commands pipe
		cd = GlobalCommandsDelegatorI.iGlobal().set(new CustomCommands());
		
		// the conole UI
		GlobalConsoleGuiI.iGlobal().set(ConsoleLemurStateI.i());
		ConsoleLemurStateI.i().configure(new ConsoleLemurStateI.CfgParm(
			null, false, KeyInput.KEY_F10, getGuiNode()));
		
		// this dialog, after closed, may have diag2.getOptionSelected() available
		diagCfg = new CustomDialogGUIState().configure(new CustomDialogGUIState.CfgParm(
			true, "ConfigDialog", false, getGuiNode(), 0.5f, 0.6f, null, null, null
		));
		
		// test dialogs
		diag = new CustomDialogGUIState().configure(new CustomDialogGUIState.CfgParm(
			false, "TestDialog", false, getGuiNode(), null, null, null, null, null
		));
		diag.configModalDialog(EModalDiagType.ListEntryConfig, diagCfg);
		
		// other basic initializations
		super.simpleInitApp();
	}
	
	/**
	 * this is here just to help
	 */
	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}
	
	public static void main( String... args ) {
		CustomTestI main = (CustomTestI) GlobalAppRefI.iGlobal().set(
			CustomTestI.i());
		
		if(main.bHideSettings){
			AppSettings as = new AppSettings(true);
			as.setResizable(true);
			as.setWidth(1024);
			as.setHeight(768);
			//as.setFrameRate(30); //TODO are we unable to change this on-the-fly after starting the application?
			main.setSettings(as);
			main.setShowSettings(false);
		}
		
		SingleAppInstanceI.i().configureOptionalAtMainMethod();
		
		main.start();
	}

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator	cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,scfEndDeveloperCommandTest,"[iHowMany] users working?")){
			bCommandWorked = endDevCustomMethod(cc.paramInt(1));
		}else
//		if(cc.checkCmdValidity(this,"conflictTest123","")){
//		}else
//		if(cc.checkCmdValidity(this,"conflictTest123","")){
//		}else
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
