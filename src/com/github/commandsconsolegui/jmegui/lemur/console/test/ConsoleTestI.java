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

import org.lwjgl.opengl.XRandR;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.extras.SingleAppInstanceI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jmegui.console.GlobalConsoleGUII;
import com.github.commandsconsolegui.jmegui.console.SimpleConsoleAppAbs;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.dialog.ChoiceDialogState;
import com.github.commandsconsolegui.jmegui.lemur.dialog.MaintenanceListDialogState;
import com.github.commandsconsolegui.jmegui.lemur.dialog.QuestionDialogState;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexHacks;
import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleTestI<T extends Command<Button>> extends SimpleConsoleAppAbs implements IReflexFillCfg{
	private static ConsoleTestI instance = new ConsoleTestI();
	public static ConsoleTestI i(){return instance;}
	
	private boolean bHideSettings=true; 
	
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
	private StringCmdField CMD_TRADITIONAL_PRETTYFIED_0 = new StringCmdField(this,CommandsTest.strFinalCmdCodePrefix);
	
//	private StringVarField svfOptionSelectedDialog2 = new StringVarField(this,"");
	
	// generic dialog
	private ChoiceDialogState<T>	diagChoice;

	private MaintenanceListDialogState<T>	diagList;

	private QuestionDialogState<T>	diagQuestion;

//	private String	strOptionSelected;
	
	public ConsoleTestI() {
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
	
	/**
	 * 
	 */
	@Override
	public void simpleInitApp() {
		MsgI.bDebug=true;
		
		/**
		 * Globals setup are actually very simple, and must come 1st!
		 * Such classes shall have parameter-less default constructors.
		 */
		GlobalCommandsDelegatorI.iGlobal().set(new CommandsTest());
//		GlobalConsoleGuiI.iGlobal().set(ConsoleLemurStateI.i());
		
		/**
		 * Configs:
		 * 
		 * Shall be simple enough to not require any exact order.
		 * 
		 * Anything more complex can be postponed (from withing the config itself)
		 * with {@link CallQueueI#appendCall(java.util.concurrent.Callable)}.
		 */
		GlobalCommandsDelegatorI.i().configure();//ConsoleLemurStateI.i());
		ConsoleLemurStateI.i().configure(new ConsoleLemurStateI.CfgParm(
			null, KeyInput.KEY_F10, getGuiNode()));
		
		//////////////////////// super init depends on globals
		super.simpleInitApp(); // basic initializations
		
		//////////////////////// config this test
		// test dialogs
//		diagChoice = new BasicDialogStateAbs<T>(BasicDialogStateAbs.EDiag.Choice).configure(
//			new BasicDialogStateAbs.CfgParm(true, 0.6f, 0.5f, null, null));
		diagChoice = new ChoiceDialogState<T>().configure(new ChoiceDialogState.CfgParm(
			0.6f, 0.5f, null, null).doPrepareTestData());
		
//		diagQuestion = new BasicDialogStateAbs<T>(BasicDialogStateAbs.EDiag.Question).configure(
//			new BasicDialogStateAbs.CfgParm(true, 500f, 300f, null, null));
		diagQuestion = new QuestionDialogState<T>().configure(new QuestionDialogState.CfgParm(
			500f, 300f, null, null).doPrepareTestData());
		
//		diagList = new BasicDialogStateAbs<T>(BasicDialogStateAbs.EDiag.BrowseManagementList).configure(
//			new BasicDialogStateAbs.CfgParm(false, null, null, null, null))
//			.addModalDialog(diagChoice)
//			.addModalDialog(diagQuestion);
		diagList = new MaintenanceListDialogState<T>().configure(new MaintenanceListDialogState.CfgParm<T>(
			null, null, null, null, diagChoice, diagQuestion).doPrepareTestData());
	}
	
	/**
	 * this is here just to help
	 */
	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}
	
	public static void main( String... args ) {
		GlobalAppRefI.iGlobal().set(ConsoleTestI.i());
		
		if(ConsoleTestI.i().bHideSettings){
			AppSettings as = new AppSettings(true);
			as.setResizable(true);
			as.setWidth(1024);
			as.setHeight(700); //768
//			as.setFullscreen(false);
			//as.setFrameRate(30); //TODO are we unable to change this on-the-fly after starting the application?
			ConsoleTestI.i().setSettings(as);
			ConsoleTestI.i().setShowSettings(false);
		}
		
		SingleAppInstanceI.i().configureOptionalAtMainMethod();
		
		ConsoleTestI.i().start();
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
		if(cd.checkCmdValidity(this,"testDialog")){
			diagList.requestEnable();
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
	
//	@Override
//	public void stop() {
//		getCamera().setViewPort(0f, 1f, 0f, 1f);
//		super.stop();
//	}
}	
