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

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.misc.HoldRestartable;
import com.github.commandsconsolegui.spAppOs.misc.ISingleInstance;
import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI;
import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI.IConfigure;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.spCmd.CommandsHelperI;
import com.github.commandsconsolegui.spCmd.IConsoleCommandListener;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.ReflexHacksPluginI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spCmd.varfield.KeyBoundVarField;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spExtras.SingleMandatoryAppInstanceI;
import com.github.commandsconsolegui.spExtras.globals.GlobalSingleMandatoryAppInstanceI;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData;
import com.github.commandsconsolegui.spJme.extras.SimpleApplicationHelperI;
import com.github.commandsconsolegui.spJme.globals.GlobalAppSettingsI;
import com.github.commandsconsolegui.spJme.globals.GlobalManageDialogJmeI;
import com.github.commandsconsolegui.spJme.misc.DebugTrackProblemsHKJme;
import com.github.commandsconsolegui.spJme.misc.EffectElectricity;
import com.github.commandsconsolegui.spLemur.console.SimpleConsolePlugin;
import com.github.commandsconsolegui.spLemur.dialog.MaintenanceListLemurDialogStateAbs;
import com.github.commandsconsolegui.spLemur.dialog.SimpleDiagChoice;
import com.github.commandsconsolegui.spLemur.dialog.SimpleDiagMaintList;
import com.github.commandsconsolegui.spLemur.dialog.SimpleDiagQuestion;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * This is a more detailed test.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ConsoleTestI<T extends Command<Button>> extends SimpleApplication implements IReflexFillCfg,IConsoleCommandListener,IConfigure,ISingleInstance{
	private static ConsoleTestI instance = new ConsoleTestI();
	public static ConsoleTestI i(){return instance;}
	
	//private final String strFinalFieldCodePrefix="CMD_";
	private final String strFieldCodePrefix="sf";
	private final String strFieldCodePrefixLess = "";
	
//	public final StringCmdField CMD_END_DEVELOPER_COMMAND_TEST = new StringCmdField(
//		this,CustomCommands.strFinalCmdCodePrefix);
	private final StringCmdField scfEndDeveloperCommandTest = new StringCmdField(this);
	private final StringCmdField scfHelp = new StringCmdField(this);
	
	/**
	 * these below were not implemented as commands here, 
	 * are just to exemplify how the auto-fill works.
	 */
	private final StringCmdField sfTestCommandAutoFillVariant1 = new StringCmdField(this,strFieldCodePrefix);
	private final StringCmdField testCommandAutoFillPrefixLessVariant2 = new StringCmdField(this,strFieldCodePrefixLess);
	private final StringCmdField scfCommandAutoFillPrefixLessVariantDefaulted3 = new StringCmdField(this,null);
	private final StringCmdField CMD_TRADITIONAL_PRETTYFIED_0 = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	
//	private final StringVarField svfOptionSelectedDialog2 = new StringVarField(this,"");
	
	// generic dialog
//	private ChoiceLemurDialogState<T>	diagChoice;

//	private MaintenanceListLemurDialogState<T>	diagList;
	private HoldRestartable<MaintenanceListLemurDialogStateAbs<T,?>>	hchdiagList = new HoldRestartable<MaintenanceListLemurDialogStateAbs<T,?>>(this);

//	private QuestionLemurDialogState<T>	diagQuestion;

//	private String	strOptionSelected;
	
	public ConsoleTestI() {
		super();
		bindListToggleEnable = new KeyBoundVarField(this);
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
		DelegateManagerI.i().addHandled(this);
//		ManageSingleInstanceI.i().add(this);
	}
	
	
	public boolean customCommand(Integer i){
		CommandsDelegator cd = GlobalCommandsDelegatorI.i();
		cd.dumpSubEntry("Shhh.. "+i+" end user(s) working!");
		
		cd.dumpSubEntry("CommandTest0: "+CMD_TRADITIONAL_PRETTYFIED_0);
		cd.dumpSubEntry("CommandTest1: "+sfTestCommandAutoFillVariant1);
		cd.dumpSubEntry("CommandTest2: "+testCommandAutoFillPrefixLessVariant2);
//		if(ReflexFillI.i().isUseDefaultCfgIfMissing()){
			cd.dumpSubEntry("CommandTest3: "+scfCommandAutoFillPrefixLessVariantDefaulted3);
//		}
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
		
		consolePlugin = new SimpleConsolePlugin(this);
		consolePlugin.configure(new SimpleConsolePlugin.CfgParm(
			ConsoleTestI.class.getName().replace(".",File.separator)));
		
		/**
		 * these adds otherwise impossible fixes and workarounds,
		 * can be safely disabled.
		 */
		ReflexHacksPluginI.i().configure(GlobalCommandsDelegatorI.i());
		PrerequisitesNotMetException.setProblemsChecker(new DebugTrackProblemsHKJme());
		
		GlobalManageDialogJmeI.i().setLinkToParentEffect(new EffectElectricity());
		GlobalManageDialogJmeI.i().setDragBorderEffect(
			new EffectElectricity()
				.setSkipDiscardingByOwner()
				.setAmplitudePerc(0.05f)
		);
		
		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
		
		bConfigured=true;
		return this;
  }
	
	@Override
	public void simpleInitApp() {
		ManageConfigI.i().assertConfigured(this);
		
		MsgI.i().setEnableDebugMessages(true);
		
		SimpleApplicationHelperI.i().simpleInitApp();
		
		GlobalSingleMandatoryAppInstanceI.i().configureRequiredAtApplicationInitialization();
		
		consolePlugin.initialize();
		
		//////////////////////// config this test
		SimpleDiagChoice<T> diagChoice = new SimpleDiagChoice<T>().configure(new SimpleDiagChoice.CfgParm(
			0.6f, 0.5f, null, null));
		
		SimpleDiagQuestion<T> diagQuestion = new SimpleDiagQuestion<T>().configure(new SimpleDiagQuestion.CfgParm(
			500f, 300f, null, null));
		
//		diagList = 
		hchdiagList.setRef(new SimpleDiagMaintList<T>().configure(new SimpleDiagMaintList.CfgParm<T>(
			null, null, null, null, diagChoice, diagQuestion)));
		
		prepareTestData(diagChoice);
		
		/**
		 * this must be initialized here because of its dependencies like {@link ManageKeyCode}
		 * that are available only after the console plugin initializer... TODO for now?
		 */
		bindListToggleEnable.setObjectRawValue(KeyInput.KEY_F5)
			.setCallerAssigned(new CallableX(this) {
				@Override
				public Boolean call() {
					hchdiagList.getRef().requestToggleEnabled();
					return true;
				}
			});
	}
	
	public final KeyBoundVarField bindListToggleEnable;

	private void prepareTestData(SimpleDiagChoice<T> diagChoice){
		for(int i=0;i<10;i++){
			diagChoice.addEntryQuick(null); 
		}
		
		MaintenanceListLemurDialogStateAbs<T,?> diagList = hchdiagList.getRef();
		diagList.addEntryQuick(null);
		diagList.addEntryQuick(null);
		
		DialogListEntryData dleS1 = diagList.addEntryQuick("section 1");
		diagList.addEntryQuick(null).setParent(dleS1);
		diagList.addEntryQuick(null).setParent(dleS1);
		diagList.addEntryQuick(null).setParent(dleS1);
		
		DialogListEntryData dleS2 = diagList.addEntryQuick("section 2");
		diagList.addEntryQuick(null).setParent(dleS2);
		diagList.addEntryQuick(null).setParent(dleS2);
		DialogListEntryData dleS21 = diagList.addEntryQuick("section 2.1").setParent(dleS2);
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
		SimpleApplicationHelperI.i().simpleUpdate(tpf);
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
		
		GlobalSingleMandatoryAppInstanceI.iGlobal().set(new SingleMandatoryAppInstanceI());
		GlobalSingleMandatoryAppInstanceI.i().configureOptionalAtMainMethod();
		
		ConsoleTestI.i().configure(new ConsoleTestI.CfgParm());
		
		ConsoleTestI.i().start();
	}

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator	cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(scfEndDeveloperCommandTest,"[iHowMany] users working?")){
			bCommandWorked = customCommand(cd.getCurrentCommandLine().paramInt(1));
		}else
		if(cd.checkCmdValidity(scfHelp,"specific custom help")){
			cd.dumpSubEntry("custom help");
			bCommandWorked = true;
		}else
		if(cd.checkCmdValidity(this,"testDialog")){
//			diagList.requestEnable();
			hchdiagList.getRef().requestEnable();
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
	 * This is called when pressing ESC key
	 */
	@Override
	public void stop(boolean waitFor) {
		GlobalCommandsDelegatorI.i().cmdRequestCleanSafeNormalExit();
		super.stop(waitFor);
	}
	
	@Override
	public void destroy() {
		SimpleApplicationHelperI.i().destroy();
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
	
	@Override
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}
	
	@Override
	public void handleError(String errMsg, Throwable t) {
		SimpleApplicationHelperI.i().handleError(errMsg, t);
		super.handleError(errMsg, t); //this will end up calling stop() here
	}
}	
