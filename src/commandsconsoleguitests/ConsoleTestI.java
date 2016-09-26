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

package commandsconsoleguitests;

import java.io.File;
import java.lang.reflect.Field;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.extras.SingleAppInstanceManager;
import com.github.commandsconsolegui.globals.GlobalSingleAppInstanceI;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jme.GlobalAppRefI;
import com.github.commandsconsolegui.globals.jme.GlobalAppSettingsI;
import com.github.commandsconsolegui.jme.extras.DialogListEntryData;
import com.github.commandsconsolegui.jme.lemur.console.SimpleConsolePlugin;
import com.github.commandsconsolegui.jme.lemur.dialog.ChoiceLemurDialogState;
import com.github.commandsconsolegui.jme.lemur.dialog.MaintenanceListLemurDialogState;
import com.github.commandsconsolegui.jme.lemur.dialog.QuestionLemurDialogState;
import com.github.commandsconsolegui.misc.Configure;
import com.github.commandsconsolegui.misc.Configure.IConfigure;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexHacksPluginI;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * This is a more detailed test.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ConsoleTestI<T extends Command<Button>> extends SimpleApplication implements IReflexFillCfg,IConsoleCommandListener,IConfigure{
	private static ConsoleTestI instance = new ConsoleTestI();

//	private static AppSettings	as;

	public static ConsoleTestI i(){return instance;}
	
	//private final String strFinalFieldCodePrefix="CMD_";
	private final String strFieldCodePrefix="sf";
	private final String strFieldCodePrefixLess = "";
	
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
	private StringCmdField scfCommandAutoFillPrefixLessVariantDefaulted3 = new StringCmdField(this,null);
	private StringCmdField CMD_TRADITIONAL_PRETTYFIED_0 = new StringCmdField(this,CommandsTest.strFinalCmdCodePrefix);
	
//	private StringVarField svfOptionSelectedDialog2 = new StringVarField(this,"");
	
	// generic dialog
	private ChoiceLemurDialogState<T>	diagChoice;

	private MaintenanceListLemurDialogState<T>	diagList;

	private QuestionLemurDialogState<T>	diagQuestion;

//	private String	strOptionSelected;
	
	public ConsoleTestI() {
		super();
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
	}
	
	
	public boolean customCommand(Integer i){
		CommandsDelegator cd = GlobalCommandsDelegatorI.i();
		cd.dumpSubEntry("Shhh.. "+i+" end user(s) working!");
		
		cd.dumpSubEntry("CommandTest0: "+CMD_TRADITIONAL_PRETTYFIED_0);
		cd.dumpSubEntry("CommandTest1: "+sfTestCommandAutoFillVariant1);
		cd.dumpSubEntry("CommandTest2: "+testCommandAutoFillPrefixLessVariant2);
		if(ReflexFillI.i().isUseDefaultCfgIfMissing()){
			cd.dumpSubEntry("CommandTest3: "+scfCommandAutoFillPrefixLessVariantDefaulted3);
		}
		return true;
	}
	
	private SimpleConsolePlugin	consolePlugin;
	
  public static class CfgParm implements ICfgParm {}
  private CfgParm cfg = null;
	private boolean bConfigured;
  @Override
	public ConsoleTestI configure(ICfgParm icfg) {
  	cfg = (CfgParm)icfg;
  	
		/**
		 * Globals setup are actually very simple, and must come 1st!
		 * Such classes shall have parameter-less default constructors.
		 */
		GlobalCommandsDelegatorI.iGlobal().set(new CommandsTest());
//		GlobalConsoleGuiI.iGlobal().set(ConsoleLemurStateI.i());
		
		/**
		 * this adds otherwise impossible fixes and workarounds,
		 * can be safely disabled.
		 */
		ReflexHacksPluginI.i().configure(GlobalCommandsDelegatorI.i());
		
		consolePlugin = new SimpleConsolePlugin(this);
		consolePlugin.configure(new SimpleConsolePlugin.CfgParm(
			ConsoleTestI.class.getName().replace(".",File.separator)));
		
		bConfigured=true;
		return this;
  }
	
	/**
	 * 
	 */
	@Override
	public void simpleInitApp() {
		Configure.assertConfigured(this);
		
		MsgI.bDebug=true;
		
		consolePlugin.initialize();
		
		//////////////////////// config this test
		diagChoice = new ChoiceLemurDialogState<T>().configure(new ChoiceLemurDialogState.CfgParm(
			0.6f, 0.5f, null, null));
		
		diagQuestion = new QuestionLemurDialogState<T>().configure(new QuestionLemurDialogState.CfgParm(
			500f, 300f, null, null));
		
		diagList = new MaintenanceListLemurDialogState<T>().configure(new MaintenanceListLemurDialogState.CfgParm<T>(
			null, null, null, null, diagChoice, diagQuestion));
		
		prepareTestData();
	}
	
	private void prepareTestData(){
		for(int i=0;i<10;i++){
			diagChoice.addEntryQuick(null); 
		}
		
		diagList.addEntryQuick(null);
		diagList.addEntryQuick(null);
		
		DialogListEntryData<T> dleS1 = diagList.addEntryQuick("section 1");
		diagList.addEntryQuick(null).setParent(dleS1);
		diagList.addEntryQuick(null).setParent(dleS1);
		diagList.addEntryQuick(null).setParent(dleS1);
		
		DialogListEntryData<T> dleS2 = diagList.addEntryQuick("section 2");
		diagList.addEntryQuick(null).setParent(dleS2);
		diagList.addEntryQuick(null).setParent(dleS2);
		DialogListEntryData<T> dleS21 = diagList.addEntryQuick("section 2.1").setParent(dleS2);
		diagList.addEntryQuick(null).setParent(dleS21);
		diagList.addEntryQuick(null).setParent(dleS21);
		diagList.addEntryQuick(null).setParent(dleS21);
		
		diagList.addEntryQuick("S2 child").setParent(dleS2); //ok, will be placed properly
		
		diagList.addEntryQuick("S1 child").setParent(dleS1); //out of order for test
		diagList.addEntryQuick("S21 child").setParent(dleS21); //out of order for test
	}
	
	/**
	 * this is here just to help
	 */
	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}
	
	public static void main( String... args ) {
		System.getProperties().list(System.out); //some cool info
		
//		GlobalAppRefI.iGlobal().set(ConsoleTestI.i());
		
		GlobalAppSettingsI.iGlobal().set(new AppSettings(true));
		GlobalAppSettingsI.i().setTitle(ConsoleTestI.class.getSimpleName());
		boolean bHideJMESettingsDialog=true; 
		if(bHideJMESettingsDialog){
			GlobalAppSettingsI.i().setResizable(true);
			GlobalAppSettingsI.i().setWidth(1024);
			GlobalAppSettingsI.i().setHeight(650); //768
//			as.setFullscreen(false);
			//as.setFrameRate(30); //TODO are we unable to change this on-the-fly after starting the application?
			ConsoleTestI.i().setShowSettings(false);
		}
		ConsoleTestI.i().setSettings(GlobalAppSettingsI.i());
		
		GlobalSingleAppInstanceI.iGlobal().set(new SingleAppInstanceManager());
		GlobalSingleAppInstanceI.i().configureOptionalAtMainMethod();
		
		ConsoleTestI.i().configure(new ConsoleTestI.CfgParm());
		
		ConsoleTestI.i().start();
	}

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator	cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,scfEndDeveloperCommandTest,"[iHowMany] users working?")){
			bCommandWorked = customCommand(cd.getCurrentCommandLine().paramInt(1));
		}else
		if(cd.checkCmdValidity(this,scfHelp,"specific custom help")){
			cd.dumpSubEntry("custom help");
			bCommandWorked = true;
		}else
		if(cd.checkCmdValidity(this,"testDialog")){
			diagList.requestEnable();
			bCommandWorked = true;
		}else
//		if(cd.checkCmdValidity(this,"activateSelfWindow")){
//			cd.addCmdToQueue(cd.scfCmdOS.getUniqueCmdId()+" linux "
//				+"xdotool windowactivate $(xdotool search --name \"^"+as.getTitle()+"$\")");
//			bCommandWorked = true;
//		}else
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
//			if(rfcv.getCodePrefixVariant().isEmpty()){
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
		if(rfcfg==null){
			rfcfg = GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
		}
		
		return rfcfg;
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
	public boolean isConfigured() {
		return this.bConfigured;
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
