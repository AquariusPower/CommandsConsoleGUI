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

package console.gui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import misc.AutoComplete;
import misc.Debug;
import misc.Misc;
import misc.ReflexFill;
import misc.ReflexFill.IReflexFillCfgVariant;
import misc.ReflexFill.ReflexFillCfg;
import misc.StringField;
import misc.TimedDelay;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.Styles;

import console.ConsoleCommands;
import console.ConsoleScriptCommands;
import console.DumpEntry;
import console.EDataBaseOperations;
import console.IConsoleCommand;
import console.IConsoleUI;

/**
 * A graphical console where developers and users can issue application commands.
 * This class connects the console commands class with JMonkeyEngine.
 * It must contain the base for the GUI to work.
 * 
 * Project at: https://github.com/AquariusPower/CommandsConsoleGUI
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class ConsoleGuiStateAbs implements AppState, ReflexFill.IReflexFillCfg, IConsoleCommand, IConsoleUI{
//	public FpsLimiterState fpslState = new FpsLimiterState();
	
//	public final String strInputIMCPREFIX = "CONSOLEGUISTATE_";
	public final String strFinalFieldInputCodePrefix="INPUT_MAPPING_CONSOLE_";
	public final StringField INPUT_MAPPING_CONSOLE_TOGGLE = new StringField(this,strFinalFieldInputCodePrefix);
	public final StringField INPUT_MAPPING_CONSOLE_SCROLL_UP = new StringField(this,strFinalFieldInputCodePrefix);
	public final StringField INPUT_MAPPING_CONSOLE_SCROLL_DOWN = new StringField(this,strFinalFieldInputCodePrefix);
	public final StringField INPUT_MAPPING_CONSOLE_SHIFT_PRESSED	= new StringField(this,strFinalFieldInputCodePrefix);
	public final StringField INPUT_MAPPING_CONSOLE_CONTROL_PRESSED	= new StringField(this,strFinalFieldInputCodePrefix);
	
	public final String STYLE_CONSOLE="console";
	
//	public boolean bStartupCmdQueueDone = false; 
	
	/**
	 * commands user can type
	 */
	public final StringField CMD_CLOSE_CONSOLE = new StringField(this,ConsoleCommands.strFinalCmdCodePrefix);
	public final StringField CMD_CONSOLE_HEIGHT = new StringField(this,ConsoleCommands.strFinalCmdCodePrefix);
	public final StringField CMD_CONSOLE_SCROLL_BOTTOM = new StringField(this,ConsoleCommands.strFinalCmdCodePrefix);
	public final StringField CMD_CONSOLE_STYLE = new StringField(this,ConsoleCommands.strFinalCmdCodePrefix);
	
	/**
	 * keep "initialized" vars together!
	 */
	public boolean	bInitialized;
	
	/**
	 * keep delayers together!
	 */
	public TimedDelay tdScrollToBottomRequestAndSuspend = new TimedDelay(0.5f);
	public TimedDelay tdScrollToBottomRetry = new TimedDelay(0.1f);
//	public TimedDelay tdLetCpuRest = new TimedDelay(0.1f);
//	public TimedDelay tdStatsRefresh = new TimedDelay(0.5f);
//	public TimedDelay tdDumpQueuedEntry = new TimedDelay(1f/5f); // per second
//	public TimedDelay tdSpareGpuFan = new TimedDelay(1.0f/60f); // like 60 FPS
	
	/**
	 * keep guesses together!
	 * guesses should not exist... 
	 * they are imprecise, they just "work" therefore they may "just break"... 
	 * TODO better algorithms/calculations are required...
	 */
	public int	iJumpBackGUESSED = 1;
	public int	iDotsMarginSafetyGUESSED = 0;
	public int	iSkipCharsSafetyGUESSED = 1;
	public float	fSafetyMarginGUESSED = 20f;
	
	/**
	 * some day this one may not be required
	 */
	public LemurGuiExtraFunctionalitiesHK efHK = null;
	
	/**
	 * other vars!
	 */
	public Container	ctnrConsole;
	public ListBox<String>	lstbxDumpArea;
	public TextField	tfInput;
//	public TextField tfAutoCompleteHint;
	public SimpleApplication	sapp;
	public boolean	bEnabled;
	public VersionedList<String>	vlstrDumpEntriesSlowedQueue = new VersionedList<String>();;
	public VersionedList<String>	vlstrDumpEntries = new VersionedList<String>();;
	public AbstractList<String> vlstrAutoCompleteHint;
	public Node lstbxAutoCompleteHint;
	public int	iShowRows = 1;
	public Integer	iToggleConsoleKey;
	public Integer	iVisibleRowsAdjustRequest = 0; //0 means dynamic
//	public int	iCmdHistoryCurrentIndex = 0;
//	public String	strInfoEntryPrefix			=". ";
//	public String	strWarnEntryPrefix			="?Warn: ";
//	public String	strErrorEntryPrefix			="!ERROR: ";
//	public String	strExceptionEntryPrefix	="!EXCEPTION: ";
//	public String	strDevWarnEntryPrefix="?DevWarn: ";
//	public String	strDevInfoEntryPrefix=". DevInfo: ";
//	public String	strSubEntryPrefix="\t";
	public boolean	bInitiallyClosedOnce = true;
//	public ArrayList<String> astrCmdHistory = new ArrayList<String>();
//	public ArrayList<String> astrCmdWithCmtValidList = new ArrayList<String>();
//	public ArrayList<String> astrBaseCmdValidList = new ArrayList<String>();
	public ArrayList<String> astrStyleList = new ArrayList<String>();
//	public int	iCmdHistoryCurrentIndex = 0;
	public	String	strNotSubmitedCmd=null; //null is a marker here
	public Panel	pnlTest;
//	public String	strTypeCmd="Cmd";
	public Label	lblStats;
	public Float	fLstbxEntryHeight;
	public Float	fStatsHeight;
	public Float	fInputHeight;
	public int	iScrollRetryAttemptsDBG;
	public int	iScrollRetryAttemptsMaxDBG;
//	public int iMaxCmdHistSize = 1000;
//	public int iMaxDumpEntriesAmount = 100000;
//	public String	strFilePrefix = "Console"; //ConsoleStateAbs.class.getSimpleName();
//	public String	strFileTypeLog = "log";
//	public String	strFileTypeConfig = "cfg";
//	public String	strFileCmdHistory = strFilePrefix+"-CmdHist";
//	public String	strFileLastDump = strFilePrefix+"-LastDump";
//	public String	strFileInitConsCmds = strFilePrefix+"-Init";
//	public String	strFileSetup = strFilePrefix+"-Setup";
//	public String	strFileDatabase = strFilePrefix+"-DB";
	public float	fConsoleHeightPercDefault = 0.5f;
	public float	fConsoleHeightPerc = fConsoleHeightPercDefault;
//	public ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
//	public ArrayList<String>	astrExecConsoleCmdsQueue = new ArrayList<String>();
//	public ArrayList<PreQueueCmdsBlockSubList>	astrExecConsoleCmdsPreQueue = new ArrayList<PreQueueCmdsBlockSubList>();
//	public File	flCmdHist;
//	public File	flLastDump;
//	public File	flInit;
//	public File	flDB;
//	public File	flSetup;
	public float	fLstbxHeight;
	public int	iSelectionIndex = -1;
	public int	iSelectionIndexPreviousForFill = -1;
	public Double	dMouseMaxScrollBy = null; //max scroll if set
//	public boolean bShowLineIndex = true;
	public String strStyle = BaseStyles.GLASS;
//	public String strStyle = Styles.ROOT_STYLE;
	public String	strInputTextPrevious = "";
	public AnalogListener	alConsoleScroll;
	public ActionListener	alConsoleToggle;
//	public String	strValidCmdCharsRegex = "A-Za-z0-9_-"+"\\"+strCommandPrefixChar;
//	public String	strValidCmdCharsRegex = "a-zA-Z0-9_"; // better not allow "-" as has other uses like negate number and commands functionalities
//	public String	strStatsLast = "";
	public Container	ctnrStatsAndControls;
	public Vector3f	v3fStatsAndControlsSize;
	public Button	btnClipboardShow;
	public boolean	bConsoleStyleCreated;
//	public boolean	bUseDumbWrap = true;
//	public Integer	iConsoleMaxWidthInCharsForLineWrap = 0;
	public BitmapFont	fntMakeFixedWidth;
	public StatsAppState	stateStats;
//	public boolean	bEngineStatsFps;
//	public float	fMonofontCharWidth;
//	public GridPanel	gpListboxDumpArea;
//	public int	iCopyFrom = -1;
//	public int	iCopyTo = -1;
	public Button	btnCopy;
	public Button	btnPaste;
//	public boolean	bAddEmptyLineAfterCommand = true;
//	public String	strLineEncloseChar = "'";
//	public String	strCmdLinePrepared = "";
//	public CharSequence	strReplaceTAB = "  ";
	public float	fWidestCharForCurrentStyleFont;
	public boolean	bKeyShiftIsPressed;
	public boolean	bKeyControlIsPressed;
	public Vector3f	v3fConsoleSize;
	public Vector3f	v3fApplicationWindowSize;
//	public String	strPreviousCmdHistoryKey;
	public String	strPreviousInputValue;
	public int	iCmdHistoryPreviousIndex;
//	public boolean	bShowExecQueuedInfo = false;
	public ConsoleCommands	cc;
//	public Hashtable<String,Object> htUserVariables = new Hashtable<String,Object>();
//public Hashtable<String,Object> htRestrictedVariables = new Hashtable<String,Object>();
//	public TreeMap<String,Object> tmUserVariables = 
//		new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
//	public TreeMap<String,Object> tmRestrictedVariables =
//		new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
//	public TreeMap<String,ArrayList<String>> tmFunctions = 
//			new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
//	public ArrayList<Alias> aAliasList = new ArrayList<Alias>();
//	public String	strCmdLineOriginal;
//	public boolean	bLastAliasCreatedSuccessfuly;
//	public float	fTPF;
//	public long	lNanoFrameTime;
//	public long	lNanoFpsLimiterTime;
//	public Boolean	bIfConditionIsValid;
//	public ArrayList<DumpEntry> adeDumpEntryFastQueue = new ArrayList<DumpEntry>();
	public Button	btnCut;
//	public ArrayList<ConditionalNested> aIfConditionNestedList = new ArrayList<ConditionalNested>();
//	public Boolean	bIfConditionExecCommands;
//	public String	strPrepareFunctionBlockForId;
//	public boolean	bFuncCmdLineRunning;
//	public boolean	bFuncCmdLineSkipTilEnd;
//	public long lLastUniqueId = 0;

	private ConsoleCursorListener consoleCursorListener = new ConsoleCursorListener(this);

	public Spatial	sptScrollTarget;
	private Integer	iStatsTextSafeLength = null;
	private boolean	bExceptionOnce = true;

//	private boolean	bUsePreQueue = false; 
	
	/**
	 * USER MUST IMPLEMENT THESE METHODS,
	 * keep them together for easy review
	 */
	public abstract void clearHintSelection();
	public abstract Integer getHintIndex();
	public abstract ConsoleGuiStateAbs setHintIndex(Integer i);
	public abstract ConsoleGuiStateAbs setHintBoxSize(Vector3f v3fBoxSizeXY, Integer iVisibleLines);
	public abstract void scrollHintToIndex(int i);
	public abstract void lineWrapDisableForChildrenOf(Node gp);
	
	public static class PreQueueCmdsBlockSubList{
		public TimedDelay tdSleep = null;
		public String strUId = Misc.i().getNextUniqueId();
		public ArrayList<String> astrCmdList = new ArrayList<String>();
		public boolean	bPrepend = false;
		public boolean bForceFailBlockExecution = false;
		public boolean	bInfoSleepBegin = false;
		public String	strBlockInfo;
		
		public String getUniqueInfo(){
			return ConsoleGuiStateAbs.i().cc.commentToAppend("UId=\""+strUId+"\","+strBlockInfo);
		}
	}
	
	public static ConsoleGuiStateAbs instance;
	public static ConsoleGuiStateAbs i(){
		return instance;
	}
	public ConsoleGuiStateAbs(ConsoleCommands cc) {
		if(instance==null)instance=this;
		this.cc = cc==null ? new ConsoleScriptCommands(this) : cc;
		this.cc.csaTmp=this;
	}
	public ConsoleGuiStateAbs(int iToggleConsoleKey, ConsoleCommands cc) {
		this(cc);
		this.bEnabled=true; //just to let it be initialized at startup by state manager
		this.iToggleConsoleKey=iToggleConsoleKey;
	}
	public ConsoleGuiStateAbs(int iToggleConsoleKey) {
		this(iToggleConsoleKey,null);
	}
	
//	public String fileNamePrepare(String strFileBaseName, String strFileType, boolean bAddDateTime){
//		return strFileBaseName
//				+(bAddDateTime?"-"+Misc.i().getDateTimeForFilename():"")
//				+"."+strFileType;
//	}
//	public String fileNamePrepareCfg(String strFileBaseName, boolean bAddDateTime){
//		return fileNamePrepare(strFileBaseName, strFileTypeConfig, bAddDateTime);
//	}
//	public String fileNamePrepareLog(String strFileBaseName, boolean bAddDateTime){
//		return fileNamePrepare(strFileBaseName, strFileTypeLog, bAddDateTime);
//	}
	
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		if(isInitialized())throw new NullPointerException("already initialized...");
		
		sapp = (SimpleApplication)app;
//		cc.sapp = sapp;
		
//		sapp.getStateManager().attach(fpslState);
		
		GuiGlobals.initialize(sapp);
		BaseStyles.loadGlassStyle(); //do not mess with default user styles: GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
		
		cc.initialize(sapp);
		
//		// init dump file, MUST BE THE FIRST!
//		flLastDump = new File(fileNamePrepareLog(strFileLastDump,false));
//		flLastDump.delete(); //each run will have a new file
//		
//		// init cmd history
//		flCmdHist = new File(fileNamePrepareLog(strFileCmdHistory,false));
//		cmdHistLoad();
//		
//		// init valid cmd list
//		executeCommand(null); //to populate the array with available commands
//		
//		// restricted vars setup
//		setupVars(false);
//		flSetup = new File(fileNamePrepareCfg(strFileSetup,false));
//		if(flSetup.exists()){
//			addCmdListOneByOneToQueue(fileLoad(flSetup), false, false);
//		}
//		
//		// before user init file
//		addExecConsoleCommandToQueue(cc.btgShowExecQueuedInfo.getCmdIdAsCommand(false));
//		addExecConsoleCommandToQueue(cc.btgShowDebugEntries.getCmdIdAsCommand(false));
//		addExecConsoleCommandToQueue(cc.btgShowDeveloperWarn.getCmdIdAsCommand(false));
//		addExecConsoleCommandToQueue(cc.btgShowDeveloperInfo.getCmdIdAsCommand(false));
//		
//		// init user cfg
//		flInit = new File(fileNamePrepareCfg(strFileInitConsCmds,false));
//		if(flInit.exists()){
//			addCmdListOneByOneToQueue(fileLoad(flInit), false, false);
//		}else{
//			fileAppendLine(flInit, cc.getCommentPrefix()+" User console commands here will be executed at startup.");
//		}
//		
//		// init DB
//		flDB = new File(fileNamePrepareCfg(strFileDatabase,false));
		
		// other inits
		cc.addExecConsoleCommandToQueue(cc.CMD_FIX_LINE_WRAP);
		cc.addExecConsoleCommandToQueue(CMD_CONSOLE_SCROLL_BOTTOM);
		cc.addExecConsoleCommandToQueue(cc.CMD_DB+" "+EDataBaseOperations.load);
		cc.addExecConsoleCommandToQueue(
			cc.CMD_DB+" "+EDataBaseOperations.save+" "+cc.getCommentPrefix()+"to shrink it");
		/**
		 * KEEP AS LAST queued cmds below!!!
		 */
		// must be the last queued command after all init ones!
		// end of initialization
		cc.addExecConsoleCommandToQueue(cc.RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE);
//		addExecConsoleCommandToQueue(cc.btgPreQueue.getCmdIdAsCommand(true)); //		 TODO temporary workaround, pre-queue cannot be enable from start yet...
		if(bInitiallyClosedOnce){
			// after all, close the console
			cc.addExecConsoleCommandToQueue(CMD_CLOSE_CONSOLE);
		}
		
		astrStyleList.add(BaseStyles.GLASS);
		astrStyleList.add(Styles.ROOT_STYLE);
		astrStyleList.add(STYLE_CONSOLE);
		
		stateStats = sapp.getStateManager().getState(StatsAppState.class);
		updateEngineStats();
		
		// instantiations initializer
		initialize();
		
		bInitiallyClosedOnce=false; // to not interfere on reinitializing after a cleanup
		
		sapp.getStateManager().attach(new ConsoleCommandsBackgroundState(this, cc));
	}
	
	@Override
	public boolean isInitialized() {
		return bInitialized;
	}

	@Override
	public void setEnabled(boolean bEnabled) {
		if(!isInitialized()){
			initialize();
		}
		
		this.bEnabled=bEnabled;
	}

	@Override
	public boolean isEnabled() {
		return bEnabled;
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
	}
	
	public void setVisible(boolean b){
		if(b){
			ctnrConsole.setCullHint(CullHint.Inherit); //TODO use bkp with: Never ?
			lstbxAutoCompleteHint.setCullHint(CullHint.Inherit); //TODO use bkp with: Dynamic ?
		}else{
			ctnrConsole.setCullHint(CullHint.Always); 
			lstbxAutoCompleteHint.setCullHint(CullHint.Always);
		}
	}
	
	/**
	 * this can be used after a cleanup() too
	 */
	public void initialize(){
		if(sapp==null)throw new NullPointerException("base initialization required");
		
		CursorEventControl.addListenersToSpatial(lstbxAutoCompleteHint, consoleCursorListener);
		lstbxAutoCompleteHint.setName("Hints");
		
//		tdLetCpuRest.updateTime();
//		tdStatsRefresh.updateTime();
//		tdDumpQueuedEntry.updateTime();
		
//		createMonoSpaceFixedFontStyle();
		
		v3fApplicationWindowSize = new Vector3f(
			sapp.getContext().getSettings().getWidth(),
			sapp.getContext().getSettings().getHeight(),
			0);
		
		// main container
		ctnrConsole = new Container(new BorderLayout(), strStyle);
		ctnrConsole.setName("ConsoleContainer");
		int iMargin=10;
		v3fConsoleSize = new Vector3f(
			v3fApplicationWindowSize.x -(iMargin*2),
			(v3fApplicationWindowSize.y * fConsoleHeightPerc) -iMargin,
			0); //TODO why Z shouldnt be 0? changed to 0.1 and 1, but made no difference.
		ctnrConsole.setPreferredSize(v3fConsoleSize); //setSize() does not work well..
//		ctnrConsole.setSize(v3fConsoleSize);
		sapp.getGuiNode().attachChild(ctnrConsole);
		ctnrConsole.setLocalTranslation(
			iMargin, 
			sapp.getContext().getSettings().getHeight()-iMargin, 
			0);
		
		/**
		 * TOP ELEMENT =================================================================
		 */
		ctnrStatsAndControls = new Container(strStyle);
		ctnrStatsAndControls.setName("ConsoleStats");
		ctnrConsole.addChild(ctnrStatsAndControls, BorderLayout.Position.North);
		
		// console stats
		lblStats = new Label("Console stats.",strStyle);
		lblStats.setColor(new ColorRGBA(1,1,0.5f,1));
		lblStats.setPreferredSize(new Vector3f(v3fConsoleSize.x*0.75f,1,0));
		fStatsHeight = retrieveBitmapTextFor(lblStats).getLineHeight();
		ctnrStatsAndControls.addChild(lblStats,0,0);
		
		// buttons
		ArrayList<Button> abu = new ArrayList<Button>();
		int iButtonIndex=0;
		btnClipboardShow = new Button("ShwClpbrd",strStyle);
		abu.add(btnClipboardShow);
		
		btnCopy = new Button("Copy",strStyle);
		abu.add(btnCopy);
		
		btnPaste = new Button("Paste",strStyle);
		abu.add(btnPaste);
		
		btnCut = new Button("Cut",strStyle);
		abu.add(btnCut);
		
		for(Button btn:abu){
			btn.setTextHAlignment(HAlignment.Center);
			//BUG buttons do not obbey this: btn.setPreferredSize(new Vector3f(50,1,0));
			btn.addClickCommands(new ButtonClick());
			ctnrStatsAndControls.addChild(btn,0,++iButtonIndex);
		}
		
		/**
		 * CENTER ELEMENT (dump entries area) ===========================================
		 */
		lstbxDumpArea = new ListBox<String>(new VersionedList<String>(),strStyle);
		lstbxDumpArea.setName("DumpArea");
    CursorEventControl.addListenersToSpatial(lstbxDumpArea, consoleCursorListener);
		Vector3f v3fLstbxSize = v3fConsoleSize.clone();
//		v3fLstbxSize.x/=2;
//		v3fLstbxSize.y/=2;
		lstbxDumpArea.setSize(v3fLstbxSize); // no need to update fLstbxHeight, will be automatic
		//TODO not working? lstbx.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		
		/**
		 * The existance of at least one entry is very important to help on initialization.
		 * Actually to determine the listbox entry height.
		 */
		if(vlstrDumpEntries.isEmpty())vlstrDumpEntries.add(""+cc.getCommentPrefix()+" Initializing console.");
		
		lstbxDumpArea.setModel(vlstrDumpEntries);
		lstbxDumpArea.setVisibleItems(iShowRows);
//		lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		ctnrConsole.addChild(lstbxDumpArea, BorderLayout.Position.Center);
		
//		gpListboxDumpArea = lstbx.getGridPanel();
		
		/**
		 * BOTTOM ELEMENT =================================================================
		 */
		// input
		tfInput = new TextField(""+cc.getCommandPrefix(),strStyle);
		tfInput.setName("UserInput");
    CursorEventControl.addListenersToSpatial(tfInput, consoleCursorListener);
		fInputHeight = retrieveBitmapTextFor(tfInput).getLineHeight();
		ctnrConsole.addChild( tfInput, BorderLayout.Position.South );
		
		mapKeys();
		
		// focus
		GuiGlobals.getInstance().requestFocus(tfInput);
		
		// help (last thing)
//		dumpInfoEntry("ListBox height = "+fLstbxHeight);
//		dumpAllStats();
//		dumpInfoEntry("Hit F10 to toggle console.");
		
		if(bInitiallyClosedOnce){
			setVisible(false);
		}
		
		/**
		 * =======================================================================
		 * =========================== LAST THING ================================
		 * =======================================================================
		 */
		bInitialized=true;
	}
	
//	public void showClipboard(){
//		showClipboard(true);
//	}
//	public void showClipboard(boolean bShowNL){
//		String strClipboard=retrieveClipboardString();
//	//	dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+" ( each line enclosed with \\"+strLineEncloseChar+" ):");
//		cc.dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+":");
//		String[] astr = strClipboard.split("\n");
//	//	for(String str:astr)dumpEntry(strLineEncloseChar+str+strLineEncloseChar);
//	//	dumpEntry(""); // this empty line for clipboard content display is i
//		cc.dumpEntry(">>> Clipboard BEGIN");
//		for(int i=0;i<astr.length;i++){
//			String str=astr[i];
//			if(bShowNL && i<(astr.length-1))str+="\\n";
//			cc.dumpEntry(false,true,false,str);
//		}
//		cc.dumpEntry("<<< Clipboard END");
//		if(bAddEmptyLineAfterCommand)cc.dumpEntry("");
//	//	dumpEntry("");
//		scrollToBottomRequest();
//	}
	
	public class ButtonClick implements Command<Button>{
		@Override
		public void execute(Button source) {
			if(source.equals(btnClipboardShow)){
				cc.showClipboard();
			}else
			if(source.equals(btnCopy)){
				cc.editCopyOrCut(false,false,false);
			}else
			if(source.equals(btnCut)){
				cc.editCopyOrCut(false,true,false);
			}else
			if(source.equals(btnPaste)){
				editPaste();
			}
		}
	}
	
	public boolean cmdEditCopyOrCut(boolean bCut) {
		String strParam1 = cc.paramString(1);
		boolean bUseCommandDelimiterInsteadOfNewLine=false;
		if(strParam1!=null){
			switch(strParam1){
				case "-d":bUseCommandDelimiterInsteadOfNewLine=true;break;
			}
		}
		String str = cc.editCopyOrCut(false, bCut, bUseCommandDelimiterInsteadOfNewLine);
		return true;
	}
	
	public void editPaste() {
		String strPasted = Misc.i().retrieveClipboardString(true);
		if(strPasted.endsWith("\\n"))strPasted=strPasted.substring(0, strPasted.length()-2);
		
		String strCurrent = getInputText();
//		if(checkInputEmpty() && validateBaseCommand(strPasted)){
		if(isInputTextFieldEmpty() && strPasted.trim().startsWith(""+cc.getCommandPrefix())){
			strCurrent = strPasted.trim(); //replace "empty" line with command (can be invalid in this case, user may complete it properly)
		}else{
			if(efHK!=null){
				strCurrent = efHK.pasteAtCaratPositionHK(strCurrent,strPasted);
			}else{
				strCurrent+=strPasted;
			}
		}
		
		setInputField(strCurrent); 
		
		if(efHK!=null)efHK.positionCaratProperlyHK();
	}
	
//	public void cmdHistSave(String strCmd) {
//		fileAppendLine(flCmdHist,strCmd);
//	}
	
//	public ArrayList<String> fileLoad(String strFile) {
//		return fileLoad(new File(strFile));
//	}
//	public ArrayList<String> fileLoad(File fl) {
//		ArrayList<String> astr = new ArrayList<String>();
//		if(fl.exists()){
//			try{
//				BufferedReader br=null;
//		    try {
//		    	br = new BufferedReader(new FileReader(fl));
//		    	while(true){
//						String strLine = br.readLine();
//						if(strLine==null)break;
//						astr.add(strLine);
//		    	}
//				} catch (IOException e) {
//					dumpExceptionEntry(e);
//				}finally{
//					if(br!=null)br.close();
//				}
//			} catch (IOException e) {
//				dumpExceptionEntry(e);
//			}
//		}else{
//			dumpWarnEntry("File not found: "+fl.getAbsolutePath());
//		}
//		
//		return astr;
//	}
	
//	public void cmdHistLoad() {
//		astrCmdHistory.addAll(fileLoad(flCmdHist));
//	}
//	
//	public void dumpSave(DumpEntry de) {
////		if(de.isSavedToLogFile())return;
//		fileAppendLine(flLastDump,de.getLineOriginal());
//	}
	
//	public void fileAppendList(File fl, ArrayList<String> astr) {
//		BufferedWriter bw = null;
//		try{
//			try {
//				bw = new BufferedWriter(new FileWriter(fl, true));
//				for(String str:astr){
//					bw.write(str);
//					bw.newLine();
//				}
//			} catch (IOException e) {
//				dumpExceptionEntry(e);
//			}finally{
//				if(bw!=null)bw.close();
//			}
//		} catch (IOException e) {
//			dumpExceptionEntry(e);
//		}
//	}
//	
//	public void fileAppendLine(File fl, String str) {
//		ArrayList<String> astr = new ArrayList<String>();
//		astr.add(str);
//		fileAppendList(fl, astr);
//	}
	
//	public void cmdTest(){
//		dumpInfoEntry("testing...");
//		String strOption = paramString(1);
//		
//		if(strOption.equalsIgnoreCase("fps")){
////			sapp.setSettings(settings);
//		}else
//		if(strOption.equalsIgnoreCase("allchars")){
//			for(char ch=0;ch<256;ch++){
//				dumpSubEntry(""+(int)ch+"='"+Character.toString(ch)+"'");
//			}
//		}else{
////		dumpSubEntry("["+(char)Integer.parseInt(strParam1, 16)+"]");
////		if(getDumpAreaSelectedIndex()>=0){
////			dumpSubEntry("Selection:"+getDumpAreaSelectedIndex()+": '"+vlstrDumpEntries.get(getDumpAreaSelectedIndex())+"'");
////		}
//		}
//		
//	}	
	
	/**
	 * DO NOT USE!
	 * overlapping problem, doesnt work well...
	 * keep this method as reference! 
	 */
	public void tweakDefaultFontToBecomeFixedSize(){
		fntMakeFixedWidth = sapp.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
		BitmapCharacterSet cs = fntMakeFixedWidth.getCharSet();
		for(int i=0;i<256;i++){ //is there more than 256?
			BitmapCharacter bc = cs.getCharacter(i);
			if(bc!=null){
				bc.setXAdvance(15); 
			}
		}
		GuiGlobals.getInstance().getStyles().setDefault(fntMakeFixedWidth);
	}
	
	public float fontWidth(String strChars){
		return fontWidth(strChars, strStyle, true);
	}
	public float fontWidth(String strChars, String strStyle, boolean bAveraged){
		float f = retrieveBitmapTextFor(new Label(strChars,strStyle)).getLineWidth();
		if(bAveraged)f/=strChars.length();
		return f;
	}
	
	public void mapKeysForInputField(){
		// simple actions
		KeyActionListener actSimpleActions = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
//				boolean bShift = key.hasModifier(0x01);
//				boolean bAlt = key.hasModifier(0x001);
//				case KeyInput.KEY_INSERT: //shift+ins paste
					//TODO ? case KeyInput.KEY_INSERT: //ctrl+ins copy
					//TODO ? case KeyInput.KEY_DELETE: //shift+del cut
				
				switch(key.getKeyCode()){
					case KeyInput.KEY_B: 
						if(bControl)cc.iCopyFrom = getDumpAreaSelectedIndex();
						break;
					case KeyInput.KEY_C: 
						if(bControl)cc.editCopyOrCut(false,false,false);
						break;
					case KeyInput.KEY_V: 
						if(bKeyShiftIsPressed){
							if(bControl)cc.showClipboard();
						}else{
							if(bControl)editPaste();
						}
						break;
					case KeyInput.KEY_X: 
						if(bControl)cc.editCopyOrCut(false,true,false);
						break;
					case KeyInput.KEY_NUMPADENTER:
					case KeyInput.KEY_RETURN:
						actionSubmit(getInputText());
						break;
					case KeyInput.KEY_TAB:
						autoCompleteInputField(bControl);
						break;
					case KeyInput.KEY_DELETE:
						if(bControl)clearInputTextField();
						break;
					case KeyInput.KEY_SLASH:
						if(bControl)cc.toggleLineCommentOrCommand();
						break;
				}
			}

		};
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_TAB), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_TAB,KeyAction.CONTROL_DOWN), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_RETURN), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_NUMPADENTER), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_B,KeyAction.CONTROL_DOWN), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_C,KeyAction.CONTROL_DOWN), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_V,KeyAction.CONTROL_DOWN), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_X,KeyAction.CONTROL_DOWN), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_DELETE,KeyAction.CONTROL_DOWN), actSimpleActions);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_SLASH,KeyAction.CONTROL_DOWN), actSimpleActions);
		
		// cmd history select action
		KeyActionListener actCmdHistoryEntrySelectAction = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				navigateCmdHistOrHintBox(source,key);
			}
		};
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_UP), actCmdHistoryEntrySelectAction);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_DOWN), actCmdHistoryEntrySelectAction);
		
		// scroll actions
		KeyActionListener actDumpNavigate = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
				double dCurrent = getScrollDumpAreaFlindex();
				double dAdd = 0;
				switch(key.getKeyCode()){
					case KeyInput.KEY_PGUP:
						dAdd = -iShowRows;
						break;
					case KeyInput.KEY_PGDN:
						dAdd = +iShowRows;
						break;
					case KeyInput.KEY_HOME:
						if(bControl)dAdd = -dCurrent;
						break;
					case KeyInput.KEY_END:
						if(bControl)dAdd = vlstrDumpEntries.size();
						break;
				}
				scrollDumpArea(dCurrent + dAdd);
				scrollToBottomRequestSuspend();
			}
		};
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_PGUP), actDumpNavigate);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_PGDN), actDumpNavigate);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_HOME, KeyAction.CONTROL_DOWN), actDumpNavigate);
		tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_END, KeyAction.CONTROL_DOWN), actDumpNavigate);
	}
	
	public boolean isHintActive(){
		return lstbxAutoCompleteHint.getParent()!=null;
	}
	
	public String getSelectedHint(){
		if(!isHintActive())return null;
		Integer i = getHintIndex();
		if(i==null)return null;
		return vlstrAutoCompleteHint.get(i);
	}
	
	public void mapKeys(){
		mapKeysForInputField();
		
		// console toggle
		if(iToggleConsoleKey!=null){
			if(!sapp.getInputManager().hasMapping(INPUT_MAPPING_CONSOLE_TOGGLE.toString())){
				sapp.getInputManager().addMapping(INPUT_MAPPING_CONSOLE_TOGGLE.toString(), 
					new KeyTrigger(iToggleConsoleKey));
					
				alConsoleToggle = new ActionListener() {
					@Override
					public void onAction(String name, boolean isPressed, float tpf) {
						if(isPressed && INPUT_MAPPING_CONSOLE_TOGGLE.equals(name)){
//							if(!isInitialized()){
//								initialize();
//							}
							setEnabled(!isEnabled());
							
							/**
							 * as it is initially invisible, from the 1st time user opens the console on, 
							 * it must be visible.
							 */
							setVisible(true);
						}
					}
				};
				sapp.getInputManager().addListener(alConsoleToggle, INPUT_MAPPING_CONSOLE_TOGGLE.toString());            
			}
		}
		
		if(!sapp.getInputManager().hasMapping(INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.toString())){
			sapp.getInputManager().addMapping(INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.toString(), 
					new KeyTrigger(KeyInput.KEY_LCONTROL),
					new KeyTrigger(KeyInput.KEY_RCONTROL));
			
			ActionListener al = new ActionListener() {
				@Override
				public void onAction(String name, boolean isPressed, float tpf) {
					bKeyControlIsPressed  = isPressed;
				}
			};
			sapp.getInputManager().addListener(al, INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.toString());            
		}
		
		if(!sapp.getInputManager().hasMapping(INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.toString())){
			sapp.getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.toString(), 
				new KeyTrigger(KeyInput.KEY_LSHIFT),
				new KeyTrigger(KeyInput.KEY_RSHIFT));
				
			ActionListener al = new ActionListener() {
				@Override
				public void onAction(String name, boolean isPressed, float tpf) {
					bKeyShiftIsPressed  = isPressed;
				}
			};
			sapp.getInputManager().addListener(al, INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.toString());            
		}
		
		// mouse scroll
    Trigger[] tggScrollUp = {new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false)};
    Trigger[] tggScrollDown = {new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true)};
    
    alConsoleScroll = new AnalogListener() {
			@Override
			public void onAnalog(String name, float value, float tpf) {
	      if (isEnabled()) {
	      	boolean bUp = INPUT_MAPPING_CONSOLE_SCROLL_UP.equals(name);
	      	
	      	if(sptScrollTarget==null)return;
	      	
	      	if(sptScrollTarget.equals(lstbxDumpArea)){
		      	double dScrollCurrentFlindex = getScrollDumpAreaFlindex();
		      	double dScrollBy = iShowRows/5; //20% of the visible rows
		      	if(dMouseMaxScrollBy!=null){
			      	if(dScrollBy > dMouseMaxScrollBy)dScrollBy = dMouseMaxScrollBy;
		      	}
		      	if(dScrollBy < 1)dScrollBy = 1;
		      	
						if(bUp){
							scrollDumpArea(dScrollCurrentFlindex - dScrollBy);
						}else{
//						if(INPUT_MAPPING_CONSOLE_SCROLL_DOWN.equals(name)){
							scrollDumpArea(dScrollCurrentFlindex + dScrollBy);
						}
	      	}else
	      	if(sptScrollTarget.equals(tfInput)){
	      		navigateCmdHistOrHintBox(tfInput, bUp?ENav.Up:ENav.Down);
	      	}else
	      	if(sptScrollTarget.equals(lstbxAutoCompleteHint)){
	      		navigateCmdHistOrHintBox(lstbxAutoCompleteHint, bUp?ENav.Up:ENav.Down);
	      	}
	      }
			}
		};
    
    sapp.getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SCROLL_UP+"", tggScrollUp);
    sapp.getInputManager().addListener(alConsoleScroll, INPUT_MAPPING_CONSOLE_SCROLL_UP+"");
    
    sapp.getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"", tggScrollDown);
    sapp.getInputManager().addListener(alConsoleScroll, INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"");
	}
	
	public void fillInputFieldWithHistoryDataAtIndex(int iIndex){
		if(cc.astrCmdHistory.size()==0)return;
		if(iIndex<0)return;
		if(iIndex>=cc.astrCmdHistory.size())return;
		
		setInputField(cc.astrCmdHistory.get(iIndex));
	}
	
//	class ThreadBackgrounCommands implements Runnable{
//		@Override
//		public void run() {
//			TimedDelay td = new TimedDelay(1000.0f/60f)
//			while(true){
//				if(!cc.btgExecCommandsInBackground.b())return;
//				
//			}
//		}
//	}
	
	@Override
	synchronized public void update(float tpf) {
		/**
		 *	Will update only if enabled... //if(!isEnabled())return;
		 */
		
		cc.update(tpf);
		
		updateInputFieldFillWithSelectedEntry();
		updateCurrentCmdHistoryEntryReset();
		updateStats();
		updateAutoCompleteHint();
		updateDumpAreaSelectedIndex();
		updateVisibleRowsAmount();
		updateScrollToBottom();
		if(efHK!=null)efHK.updateHK();
		
		GuiGlobals.getInstance().requestFocus(tfInput);
	}
	
//	/**
//	 * TODO use base 36 (max alphanum chars amount)
//	 * @return
//	 */
//	public String getNextUniqueId(){
//		return ""+(++lLastUniqueId);
//	}
	
//	public void updateToggles() {
//		if(cc.btgEngineStatsView.checkChangedAndUpdate())updateEngineStats();
//		if(cc.btgEngineStatsFps.checkChangedAndUpdate())updateEngineStats();
//		if(cc.btgFpsLimit.checkChangedAndUpdate())fpslState.setEnabled(cc.btgFpsLimit.b());
//		if(cc.btgConsoleCpuRest.checkChangedAndUpdate())tdLetCpuRest.setActive(cc.btgConsoleCpuRest.b());
////		if(cc.btgPreQueue.checkChangedAndUpdate())bUsePreQueue=cc.btgPreQueue.b();
//	}
//	public void resetCmdHistoryCursor(){
//		cc.iCmdHistoryCurrentIndex = cc.astrCmdHistory.size();
//	}
	
	public void updateCurrentCmdHistoryEntryReset() {
		String strNewInputValue = getInputText();
		if((cc.iCmdHistoryCurrentIndex-iCmdHistoryPreviousIndex)==0){
			if(!strNewInputValue.equals(strPreviousInputValue)){
				/**
				 * user has deleted or typed some character
				 */
				cc.resetCmdHistoryCursor();
			}
		}
		
		strPreviousInputValue=strNewInputValue;
		iCmdHistoryPreviousIndex=cc.iCmdHistoryCurrentIndex;
	}
	
	public void closeHint(){
		vlstrAutoCompleteHint.clear();
		lstbxAutoCompleteHint.removeFromParent();
	}
	
//	/**
//	 * Validates if the first extracted word is a valid command.
//	 * 
//	 * @param strCmdFullChk can be the full command line here
//	 * @return
//	 */
//	public boolean validateBaseCommand(String strCmdFullChk){
//		strCmdFullChk = extractCommandPart(strCmdFullChk,0);
////		if(strCmdFullChk.startsWith(strCommandPrefixChar)){
////			strCmdFullChk=strCmdFullChk.substring(strCommandPrefixChar.length());
////		}
//		return astrBaseCmdValidList.contains(strCmdFullChk);
//	}
	
//	/**
//	 * 
//	 * @param strCmdFull
//	 * @param iPart 0 is base command, 1.. are params
//	 * @return
//	 */
//	public String extractCommandPart(String strCmdFull, int iPart){
//		if(strCmdFull.startsWith(""+cc.getCommandPrefix())){
//			strCmdFull=strCmdFull.substring(1); //1 cc.getCommandPrefix()Char
//		}
//		
////		String[] astr = strCmdFull.split("[^$"+strValidCmdCharsRegex+"]");
////		if(astr.length>iPart){
////			return astr[iPart];
////		}
//		ArrayList<String> astr = convertToCmdParamsList(strCmdFull);
//		if(iPart>=0 && astr.size()>iPart){
//			return astr.get(iPart);
//		}
//		
//		return null;
//	}
	
//	public String extractFirstCommandPart(String strCmdFull){
//		if(strCmdFull.startsWith(strCommandPrefixChar)){
//			strCmdFull=strCmdFull.substring(strCommandPrefixChar.length());
//		}
//		return strCmdFull.split("[^"+strValidCmdCharsRegex+"]")[0];
//	}
	
//	public boolean checkInputEmpty(){
//		return checkInputEmptyDumpIfNot(false);
//	}
	/**
	 * after trim(), if empty or have only the command prefix char, 
	 * will return true.
	 * @return
	 */
	public boolean isInputTextFieldEmpty(){
//	public boolean checkInputEmptyDumpIfNot(boolean bDumpContentsIfNotEmpty){
		String strCurrentInputText = getInputText().trim();
		
		if(strCurrentInputText.isEmpty())return true;
		
		if(strCurrentInputText.equals(""+cc.getCommandPrefix()))return true;
		
//		if(bDumpContentsIfNotEmpty){
//			dumpInfoEntry("Not issued command below:");
//			dumpEntry(strCurrentInputText); //so user will not lose what was typing...
//			/**
//			 * Do not scroll in this case. No command was issued...
//			 */
//		}
		
		return false;
	}
	
	public String dumpAndClearInputField(){
		if(!isInputTextFieldEmpty()){
			cc.dumpInfoEntry("Not issued command below:");
			String str = getInputText();
			cc.dumpEntry(str); //so user will not lose what was typing...
			clearInputTextField();
			return str;
		}
		return null;
	}
	
	public void updateInputFieldFillWithSelectedEntry() {
		// auto-fill with selected command
		if(getDumpAreaSelectedIndex()>=0){
			if(iSelectionIndexPreviousForFill!=getDumpAreaSelectedIndex()){ //to let user type things...
				updateCopyFrom();
				
				String strCmdChk = cc.editCopyOrCut(true,false,true); //vlstrDumpEntries.get(getDumpAreaSelectedIndex()).trim();
				strCmdChk=strCmdChk.trim();
				if(cc.validateBaseCommand(strCmdChk)){
					if(!strCmdChk.startsWith(""+cc.getCommandPrefix()))strCmdChk=cc.getCommandPrefix()+strCmdChk;
					dumpAndClearInputField();
//					int iCommentBegin = strCmdChk.indexOf(cc.getCommentPrefix());
//					if(iCommentBegin>=0)strCmdChk=strCmdChk.substring(0,iCommentBegin);
//					strCmdChk=clearCommentsFromMultiline(strCmdChk);
//					if(strCmdChk.endsWith("\\n"))strCmdChk=strCmdChk.substring(0,strCmdChk.length()-2);
//					if(strCmdChk.endsWith("\n" ))strCmdChk=strCmdChk.substring(0,strCmdChk.length()-1);
					setInputField(strCmdChk);
				}
				
				iSelectionIndexPreviousForFill = getDumpAreaSelectedIndex();
			}
		}
	}
	
	@Override
	public void setInputField(String str){
		/**
		 * do NOT trim() the string, it may be being auto completed and 
		 * an space being appended to help on typing new parameters.
		 */
		tfInput.setText(fixStringToInputField(str));
	}
	
	public String clearCommentsFromMultiline(String str){
		/**
		 * this will remove any in-between comments
		 */
//		str=str.replace("\\n", "\n");
		String strCommentRegion=Pattern.quote(cc.getCommentPrefixStr())+"[^\n]*\n";
		String strJoinToken="\n";
		if(str.matches(".*"+strCommentRegion+".*")){
			str=String.join(strJoinToken, str.split(strCommentRegion));
		}
		
		strCommentRegion=Pattern.quote(cc.getCommentPrefixStr())+"[^"+cc.getCommandDelimiterStr()+"]*"+cc.getCommandDelimiterStr();
		strJoinToken=cc.getCommandDelimiterStr();
		if(str.matches(".*"+strCommentRegion+".*")){
			str=String.join(strJoinToken, str.split(strCommentRegion));
		}
		
		return str;
	}
	
	public String fixStringToInputField(String str){
		str=clearCommentsFromMultiline(str);
		
		/**
		 * removes ending NL
		 */
		if(str.endsWith("\\n"))str=str.substring(0,str.length()-2);
		if(str.endsWith("\n" ))str=str.substring(0,str.length()-1);
		
		/**
		 * Lines ending with command delimiter, do not require newline.
		 */
		return cc.convertNewLineToCmdDelimiter(str)
			/**
			 * convert special chars to escaped ones
			 */
			.replace("\n", "\\n")
			.replace("\t", "\\t") //			.replace("\t", strReplaceTAB)
			.replace("\r", "");
	}
	
	public void updateDumpAreaSelectedIndex(){
		Integer i = lstbxDumpArea.getSelectionModel().getSelection();
		iSelectionIndex = i==null ? -1 : i;
	}
	
	public int getDumpAreaSelectedIndex(){
		return iSelectionIndex;
	}
	
	public void updateCopyFrom(){
		int iSelected = getDumpAreaSelectedIndex();
		boolean bMultiLineMode = bKeyShiftIsPressed;
		if(iSelected>=0){
			if(bMultiLineMode){
				if(cc.iCopyTo==-1){
					cc.iCopyFrom = iSelected;
					cc.iCopyTo = iSelected;
				}else{
					cc.iCopyFrom = cc.iCopyTo;
					cc.iCopyTo = iSelected;
				}
			}else{
				cc.iCopyFrom = iSelected;
				cc.iCopyTo = iSelected;
			}
			
		}
	}
	
	/**
	 * this is to fix the dump entry by disallowing automatic line wrapping
	 */
	public void cmdLineWrapDisableDumpArea(){
		lineWrapDisableForChildrenOf(lstbxDumpArea);
	}
	
	public void updateVisibleRowsAmount(){
		if(fLstbxHeight != lstbxDumpArea.getSize().y){
			iVisibleRowsAdjustRequest = 0; //dynamic
		}
		
		if(iVisibleRowsAdjustRequest==null)return;
		
		Integer iForceAmount = iVisibleRowsAdjustRequest;
		if(iForceAmount>0){
			iShowRows=iForceAmount;
//			lstbx.setVisibleItems(iShowRows);
//			lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		}else{
			if(lstbxDumpArea.getGridPanel().getChildren().isEmpty())return;
			
			Button	btnFixVisibleRowsHelper = null;
			for(Spatial spt:lstbxDumpArea.getGridPanel().getChildren()){
				if(spt instanceof Button){
					btnFixVisibleRowsHelper = (Button)spt;
					break;
				}
			}
			if(btnFixVisibleRowsHelper==null)return;
			
			fLstbxEntryHeight = retrieveBitmapTextFor(btnFixVisibleRowsHelper).getLineHeight();
			if(fLstbxEntryHeight==null)return;
			
			fLstbxHeight = lstbxDumpArea.getSize().y;
			
			float fHeightAvailable = fLstbxHeight;
//				float fHeightAvailable = fLstbxHeight -fInputHeight;
//				if(ctnrConsole.hasChild(lblStats)){
//					fHeightAvailable-=fStatsHeight;
//				}
			iShowRows = (int) (fHeightAvailable / fLstbxEntryHeight);
		}
		
		lstbxDumpArea.setVisibleItems(iShowRows);
		
		cc.varSet(cc.CMD_FIX_VISIBLE_ROWS_AMOUNT, ""+iShowRows, true);
		
	//	lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		cc.dumpInfoEntry("fLstbxEntryHeight="+Misc.i().fmtFloat(fLstbxEntryHeight)+", "+"iShowRows="+iShowRows);
		
		iVisibleRowsAdjustRequest=null;
		
		cmdLineWrapDisableDumpArea();
	}
	
	public BitmapText retrieveBitmapTextFor(Node pnl){
		for(Spatial c : pnl.getChildren()){
			if(c instanceof BitmapText){
				return (BitmapText)c;
			}
		}
		return null;
	}

	/**
	 * This is what happens when Enter key is pressed.
	 * @param strCmd
	 * @return false if was a comment, empty or invalid
	 */
	public boolean actionSubmit(final String strCmd){
		if(checkAndApplyHintAtInputField())return true;
		
		return cc.actionSubmitCommand(strCmd);
	}
	
	public boolean checkAndApplyHintAtInputField(){
		/**
		 * if hint area is active and has a selected entry, 
		 * it will override default command submit.
		 */
		if(isHintActive()){
			String strHintCmd = getSelectedHint();
			if(strHintCmd!=null){
				strHintCmd=cc.getCommandPrefix()+cc.extractCommandPart(strHintCmd,0)+" ";
				if(!getInputText().equals(strHintCmd)){
					setInputField(strHintCmd);
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	@Override
	public void clearInputTextField() {
		setInputField(""+cc.getCommandPrefix());
	}
	
	@Override
	public String getInputText() {
		return tfInput.getText();
	}
	
//	public boolean actionSubmitCommand(final String strCmd){
//		if(strCmd.isEmpty() || strCmd.trim().equals(""+cc.getCommandPrefix())){
//			clearInputTextField(); 
//			return false;
//		}
//		
//		String strType=strTypeCmd;
//		boolean bIsCmd=true;
//		boolean bShowInfo=true;
//		if(strCmd.trim().startsWith(""+cc.getCommentPrefix())){
//			strType="Cmt";
//			bIsCmd=false;
//		}else
//		if(!strCmd.trim().startsWith(""+cc.getCommandPrefix())){
//			strType="Inv";
//			bIsCmd=false;
//		}
//		
//		if(bIsCmd){
//			if(strCmd.trim().endsWith(""+cc.getCommentPrefix())){
//				bShowInfo=false;
//			}
//		}
//		
////		String strTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+": ";
//		if(bShowInfo)dumpInfoEntry(strType+": "+strCmd);
//		
//		clearInputTextField(); 
//		
//		// history
//		boolean bAdd=true;
//		if(!astrCmdHistory.isEmpty()){
//			if(astrCmdHistory.get(astrCmdHistory.size()-1).equals(strCmd)){
//				bAdd=false; //prevent sequential dups
//			}
//		}
//		
//		if(bAdd){
//			cc.astrCmdHistory.add(strCmd);
//			
//			cc.cmdHistSave(strCmd);
//			while(cc.astrCmdHistory.size()>cc.iMaxCmdHistSize){
//				cc.astrCmdHistory.remove(0);
//			}
//		}
//		
//		resetCmdHistoryCursor();
//		
//		if(strType.equals(strTypeCmd)){
//			if(!cc.executeCommand(strCmd)){
//				cc.dumpWarnEntry(strType+": FAIL: "+strCmd);
//				cc.showHelpForFailedCommand(strCmd);
//			}
//			
//			if(cc.bAddEmptyLineAfterCommand ){
//				cc.dumpEntry("");
//			}
//		}
//		
//		scrollToBottomRequest();
//		
//		return bIsCmd;
//	}
	
	@Override
	public void scrollToBottomRequest(){
		if(!cc.btgAutoScroll.b())return;
		
		tdScrollToBottomRequestAndSuspend.updateTime();
		tdScrollToBottomRetry.updateTime();
	}
	
	public void scrollToBottomRequestSuspend(){
		tdScrollToBottomRequestAndSuspend.reset();
		tdScrollToBottomRetry.reset();
		
		// update retry attempts for debug info
		if(iScrollRetryAttemptsDBG > iScrollRetryAttemptsMaxDBG){
			iScrollRetryAttemptsMaxDBG = iScrollRetryAttemptsDBG;
		}
		iScrollRetryAttemptsDBG=0;
	}
	
	public void updateScrollToBottom(){
		if(!tdScrollToBottomRequestAndSuspend.isActive())return;
		
//		if(tdScrollToBottomRetry.getCurrentDelay() < tdScrollToBottomRetry.lDelayLimit){
		if(!tdScrollToBottomRetry.isReady()){
			return;
		}
		
		/**
		 * being after the retry delay check (lets say 0.1s),
		 * this will actually work by checking like at: 1s, 1.1s, 1.2s, ...
		 * like a cooldown time to make sure the slider accepted the command.
		 */
		if(tdScrollToBottomRequestAndSuspend.isReady()){
			if(Double.compare(lstbxDumpArea.getSlider().getModel().getPercent(), 0.0)==0){
				scrollToBottomRequestSuspend();
				return;
			}
		}
		
		scrollDumpArea(-1);
		tdScrollToBottomRetry.updateTime();
		iScrollRetryAttemptsDBG++;
	}	
	
	/**
	 * 
	 * @param dIndex if -1, means max index (bottom)
	 */
	public void scrollDumpArea(double dIndex){
		/**
		 * the index is actually inverted
		 */
		double dMax = lstbxDumpArea.getSlider().getModel().getMaximum();
		if(dIndex==-1)dIndex=dMax;
		dIndex = dMax-dIndex;
		double dPerc = dIndex/dMax;
		
		lstbxDumpArea.getSlider().getModel().setPercent(dPerc);
		lstbxDumpArea.getSlider().getModel().setValue(dIndex);
	}
	
	/**
	 * 
	 * @return a "floating point index" (Dlindex)
	 */
	public double getScrollDumpAreaFlindex(){
		return lstbxDumpArea.getSlider().getModel().getMaximum()
				-lstbxDumpArea.getSlider().getModel().getValue();
	}
	
	public void autoCompleteInputField(){
		autoCompleteInputField(false);
	}
	public void autoCompleteInputField(boolean bMatchContains){
		String strCmdPart = getInputText();
		String strCmdAfterCarat="";
		
		Integer iCaratPositionHK = efHK==null?null:efHK.getInputFieldCaratPosition();
		if(iCaratPositionHK!=null){
			strCmdAfterCarat = strCmdPart.substring(iCaratPositionHK);
			strCmdPart = strCmdPart.substring(0, iCaratPositionHK);
		}
		
		String strCompletedCmd = autoCompleteWork(strCmdPart,bMatchContains);
		
		/**
		 * parameters completion!
		 */
		if(strCompletedCmd.equals(strCmdPart)){
			String strBaseCmd = cc.extractCommandPart(strCmdPart,0);
			String strParam1 = cc.extractCommandPart(strCmdPart,1);
			
			String strCmd=null;
			String strPartToComplete=null;
			ArrayList<String> astrOptList = null;
			boolean bIsVarCompletion=false;
			if(CMD_CONSOLE_STYLE.equals(strBaseCmd)){
				strCmd=CMD_CONSOLE_STYLE+" ";
				astrOptList=astrStyleList;
				strPartToComplete=strParam1;
			}else
			if(cc.CMD_DB.equals(strBaseCmd)){
				strCmd=cc.CMD_DB+" ";
				astrOptList=EDataBaseOperations.getValuesAsArrayList();
				strPartToComplete=strParam1;
			}else
			if(cc.CMD_VAR_SET.equals(strBaseCmd)){
				strCmd=cc.CMD_VAR_SET+" ";
				astrOptList=cc.getVariablesIdentifiers(false);
				strPartToComplete=strParam1;
				bIsVarCompletion=true;
			}else{
				/**
				 * complete for variables ids when retrieving variable value 
				 */
				String strRegexVarOpen=Pattern.quote(""+cc.getVariableExpandPrefix()+"{");
				String strRegex=".*"+strRegexVarOpen+"["+cc.strValidCmdCharsRegex+cc.RESTRICTED_TOKEN+"]*$";
				if(strCompletedCmd.matches(strRegex)){
					strCmd=strCompletedCmd.trim().substring(1); //removes command prefix
					astrOptList=cc.getVariablesIdentifiers(true);
					
					/**
					 * here the variable is being filtered
					 */
					String[] astrToVar = strCompletedCmd.split(".*"+strRegexVarOpen);
					if(astrToVar.length>1){
						strPartToComplete=astrToVar[astrToVar.length-1];
						strCmd=strCmd.substring(0, strCmd.length()-strPartToComplete.length());
						bIsVarCompletion=true;
					}
				}
			}
			
//			if(strParam1!=null){
			if(astrOptList!=null){
				strCompletedCmd=""+cc.getCommandPrefix()+strCmd;
				
				ArrayList<String> astr = strPartToComplete==null ? astrOptList :
					AutoComplete.i().autoComplete(strPartToComplete, astrOptList, bMatchContains);
				if(astr.size()==1){
					strCompletedCmd+=astr.get(0);
				}else{
					cc.dumpInfoEntry("Param autocomplete:");
					String strFirst = astr.remove(0);
					for(String str:astr){
//						if(strPartToComplete!=null && str.equals(astr.get(0)))continue;
						if(bIsVarCompletion)str=cc.varReportPrepare(str);
						cc.dumpSubEntry(str);
					}
					
					if(strPartToComplete!=null){
						strCompletedCmd+=strFirst; //best partial param match
					}
				}
			}
//			}
		}
		
		if(strCompletedCmd.trim().isEmpty())strCompletedCmd=""+cc.getCommandPrefix();
		setInputField(strCompletedCmd+strCmdAfterCarat);
		if(efHK!=null)efHK.setCaratPosition(strCompletedCmd.length());
		
		scrollToBottomRequest();
	}
	
	public String autoCompleteWork(String strCmdPart, boolean bMatchContains){
		String strCmdPartOriginal = strCmdPart;
		strCmdPart=strCmdPart.trim();
		
		// no command typed
		if(strCmdPart.equalsIgnoreCase(""+cc.getCommandPrefix()) || strCmdPart.isEmpty())
			return strCmdPartOriginal;
		
		strCmdPart=strCmdPart.replaceFirst("^"+cc.getCommandPrefix(), "");
		
		// do not allow invalid chars
		if(!cc.isValidIdentifierCmdVarAliasFuncString(strCmdPart))
//		if(!strCmdPart.matches("["+strValidCmdCharsRegex+"]*"))
			return strCmdPartOriginal;
		
		ArrayList<String> astr = AutoComplete.i().autoComplete(strCmdPart, cc.astrCmdWithCmtValidList, bMatchContains);
		String strFirst=astr.get(0); //the actual stored command may come with comments appended
		String strAppendSpace = "";
		if(astr.size()==1 && cc.validateBaseCommand(strFirst)){
			strAppendSpace=" "; //found an exact command valid match, so add space
		}
////		if(astr.size()==1 && extractCommandPart(strFirst,0).length() > strCmdPart.length()){
//		if(astr.size()==1 && strFirst.length() > strCmdPart.length()){
//			strAppendSpace=" "; //found an exact command valid match, so add space
//		}
		
		// many possible matches
		if(astr.size()>1){
			cc.dumpInfoEntry("AutoComplete: ");
			for(String str:astr){
				if(str.equals(strFirst))continue; //skip the partial improved match, 1st entry
				cc.dumpSubEntry(cc.getCommandPrefix()+str);
			}
		}
		
		return cc.getCommandPrefix()+strFirst.split(" ")[0]+strAppendSpace;
	}
	
	public boolean statsFieldToggle() {
		if(cc.paramBooleanCheckForToggle(1)){
			Boolean bEnable = cc.paramBoolean(1);
			
			boolean bIsVisible = ctnrStatsAndControls.getParent()!=null;
			boolean bSetVisible = !bIsVisible; //toggle
			
			if(bEnable!=null)bSetVisible = bEnable; //override
			
			if(bSetVisible){
				if(!bIsVisible){
					ctnrConsole.addChild(ctnrStatsAndControls,BorderLayout.Position.North);
				}
			}else{
				if(bIsVisible){
					ctnrConsole.removeChild(ctnrStatsAndControls);
				}
			}
			
			updateVisibleRowsAmount();
			
			return true;
		}
		
		return false;
	}

	/**
	 * 
	 * @param fNewHeightPercent null to use the default
	 */
	public void modifyConsoleHeight(Float fNewHeightPercent) {
		Vector3f v3fNew = ctnrConsole.getPreferredSize(); //getSize() does not work well..
		if(!v3fNew.equals(v3fConsoleSize)){
			cc.dumpDevWarnEntry("sizes should be equal: "+v3fNew+v3fConsoleSize);
		}
		
		if(fNewHeightPercent==null)fNewHeightPercent=fConsoleHeightPercDefault;
		
		if(fNewHeightPercent>0.95f)fNewHeightPercent=0.95f;
		
		v3fNew.y = fNewHeightPercent * sapp.getContext().getSettings().getHeight();
		
		float fMin = fInputHeight +fStatsHeight +fLstbxEntryHeight*3; //will show only 2 rows, the 3 value is a safety margin
		
		if(v3fNew.y<fMin)v3fNew.y=fMin;
		
		ctnrConsole.setPreferredSize(v3fNew); //setSize() does not work well..
//		ctnrConsole.setSize(v3fNew); //setSize() does not work well..
		v3fConsoleSize.set(v3fNew);
		
		fConsoleHeightPerc = fNewHeightPercent;
		
		cc.varSet(CMD_CONSOLE_HEIGHT, ""+fConsoleHeightPerc, true);
		
		iVisibleRowsAdjustRequest = 0; //dynamic
	}
	public void updateEngineStats() {
		stateStats.setDisplayStatView(cc.btgEngineStatsView.get());
		stateStats.setDisplayFps(cc.btgEngineStatsFps.get());
	}
	public void styleHelp(){
		cc.dumpInfoEntry("Available styles:");
		for(String str:astrStyleList){
			cc.dumpSubEntry(str);
		}
	}
	public boolean styleCheck(String strStyle) {
		return astrStyleList.contains(strStyle);
	}
	
	public boolean cmdStyleApply(String strStyleNew) {
		boolean bOk = styleCheck(strStyleNew);
		if(bOk){
			strStyle=strStyleNew;
			
			cc.varSet(CMD_CONSOLE_STYLE, strStyle, true);
			
			updateFontStuff();
			
			sapp.enqueue(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					final boolean bWasEnabled=isEnabled();
					setEnabled(false);
					cleanup();
					
//					sapp.enqueue(new Callable<Void>() {
//						@Override
//						public Void call() throws Exception {
							if(bWasEnabled){
								setEnabled(true);
							}
							modifyConsoleHeight(fConsoleHeightPerc);
							scrollToBottomRequest();
//							return null;
//						}
//					});
//					addToExecConsoleCommandQueue(cc.CMD_MODIFY_CONSOLE_HEIGHT+" "+fConsoleHeightPerc);
//					addToExecConsoleCommandQueue(cc.CMD_SCROLL_BOTTOM);
					return null;
				}
			});
		}else{
			cc.dumpWarnEntry("invalid style: "+strStyleNew);
			styleHelp();
		}
		
		return bOk;
	}
	
	public float widthForListbox(){
		return lstbxDumpArea.getSize().x;
	}
	
	public float widthForDumpEntryField(){
		//TODO does slider and the safety margin are necessary? slider is inside list box right?
		return widthForListbox() -lstbxDumpArea.getSlider().getSize().x -fSafetyMarginGUESSED;
//		return widthForListbox();
	}
	
	@Override
	public void render(RenderManager rm) {
	}

	@Override
	public void postRender() {
	}

	@Override
	public void cleanup() {
//		tdLetCpuRest.reset();
//		tdScrollToBottomRequestAndSuspend.reset();
//		tdScrollToBottomRetry.reset();
//		tdTextCursorBlinkHK.reset();
		
		ctnrConsole.clearChildren();
//		tfAutoCompleteHint.removeFromParent();
		lstbxAutoCompleteHint.removeFromParent();
		ctnrConsole.removeFromParent();
		
		//TODO should keymappings be at setEnabled() ?
    sapp.getInputManager().deleteMapping(INPUT_MAPPING_CONSOLE_SCROLL_UP+"");
    sapp.getInputManager().deleteMapping(INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"");
    sapp.getInputManager().removeListener(alConsoleScroll);
    
    /**
     * IMPORTANT!!!
     * Toggle console must be kept! Re-initialization depends on it!
		sapp.getInputManager().deleteMapping(INPUT_MAPPING_CONSOLE_TOGGLE);
    sapp.getInputManager().removeListener(alConsoleToggle);
     */
    
    if(efHK!=null)efHK.cleanupHK();
    bInitialized=false;
	}
	
//	public boolean isInitiallyClosed() {
//		return bInitiallyClosedOnce;
//	}
//	public void setCfgInitiallyClosed(boolean bInitiallyClosed) {
//		this.bInitiallyClosedOnce = bInitiallyClosed;
//	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(StringField.class)){
			if(strFinalFieldInputCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCommandPrefix = "CONSOLEGUISTATE_";
				rfcfg.bFirstLetterUpperCase = true;
			}else
			if(cc.strFinalFieldRestrictedCmdCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCommandPrefix = ""+cc.RESTRICTED_TOKEN;
				rfcfg.bFirstLetterUpperCase = true;
			}
			
			
//			switch(rfcv.getReflexFillCfgVariant()){
//				case 0:
//					rfcfg = new ReflexFillCfg();
//					rfcfg.strCodingStyleFieldNamePrefix = "INPUT_MAPPING_CONSOLE_";
//					rfcfg.strCommandPrefix = IMCPREFIX;
//					rfcfg.bFirstLetterUpperCase = true;
//					break;
//			}
		}
		
		if(rfcfg==null)rfcfg = cc.getReflexFillCfg(rfcv);
		
		return rfcfg;
	}

	@Override
	public boolean executePreparedCommand(){
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(CMD_CLOSE_CONSOLE,"like the bound key to do it")){
			setEnabled(false);
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(CMD_CONSOLE_HEIGHT,"[fPercent] of the application window")){
			Float f = cc.paramFloat(1);
			modifyConsoleHeight(f);
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(CMD_CONSOLE_SCROLL_BOTTOM,"")){
			scrollToBottomRequest();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(CMD_CONSOLE_STYLE,"[strStyleName] changes the style of the console on the fly, empty for a list")){
			String strStyle = cc.paramString(1);
			if(strStyle==null)strStyle="";
			bCommandWorked=cmdStyleApply(strStyle);
		}else
		{}
		
		return bCommandWorked;
	}
	
	@Override
	public void dumpAllStats() {
		cc.dumpEntry(true, cc.btgShowDeveloperInfo.get(), false,	
			Misc.i().getSimpleTime(cc.btgShowMiliseconds.get())
				+cc.strDevInfoEntryPrefix+"Console stats (Dev):"+"\n"
			
			+"Console Height = "+Misc.i().fmtFloat(ctnrConsole.getSize().y)+"\n"
			+"Visible Rows = "+lstbxDumpArea.getGridPanel().getVisibleRows()+"\n"
			+"Line Wrap At = "+cc.iConsoleMaxWidthInCharsForLineWrap+"\n"
			+"ListBox Height = "+Misc.i().fmtFloat(lstbxDumpArea.getSize().y)+"\n"
			+"ListBox Entry Height = "+Misc.i().fmtFloat(fLstbxEntryHeight)+"\n"
			
			+"Stats Text Field Height = "+Misc.i().fmtFloat(fStatsHeight)+"\n"
			+"Stats Container Height = "+Misc.i().fmtFloat(ctnrStatsAndControls.getSize().y)+"\n"
			
			+"Input Field Height = "+Misc.i().fmtFloat(fInputHeight)+"\n"
			+"Input Field Final Height = "+Misc.i().fmtFloat(tfInput.getSize().y)+"\n"
			
			+"Slider Value = "+Misc.i().fmtFloat(getScrollDumpAreaFlindex())+"\n"
			
			+"Slider Scroll request max retry attempts = "+iScrollRetryAttemptsMaxDBG);
	}
	
//	@Override
//	public void setConsoleMaxWidthInCharsForLineWrap(Integer paramInt) {
//		iConsoleMaxWidthInCharsForLineWrap=paramInt;
//	}
//	
//	@Override
//	public Integer getConsoleMaxWidthInCharsForLineWrap() {
//		return iConsoleMaxWidthInCharsForLineWrap;
//	}
	
	@Override
	public AbstractList<String> getDumpEntries() {
		return vlstrDumpEntries;
	}
	
	@Override
	public AbstractList<String> getDumpEntriesSlowedQueue() {
		return vlstrDumpEntriesSlowedQueue;
	}
	
	@Override
	public AbstractList<String> getAutoCompleteHint() {
		return vlstrAutoCompleteHint;
	}

	public boolean navigateHint(int iAdd){
		if(!isHintActive())return false;
		
		if(
				getSelectedHint()!=null
				||
				(cc.iCmdHistoryCurrentIndex+1) >= cc.astrCmdHistory.size() // end of cmd history
		){
			int iMaxIndex = getAutoCompleteHint().size()-1;
			if(iMaxIndex<0)return false;
			
			Integer iCurrentIndex = getHintIndex();
			if(iCurrentIndex==null)iCurrentIndex=0;
			
			iCurrentIndex+=iAdd;
			
			if(iCurrentIndex<-1)iCurrentIndex=-1; //will clear the listbox selection
			if(iCurrentIndex>iMaxIndex)iCurrentIndex=iMaxIndex;
			
			setHintIndex(iCurrentIndex);
			
			scrollHintToIndex(iCurrentIndex);
			
			return iCurrentIndex>-1;
		}
		
		return false;
	}
	
	enum ENav{
		Up,
		Down,
	}
	
	public void navigateCmdHistOrHintBox(TextEntryComponent source, KeyAction key) {
		ENav enav = null;
		switch(key.getKeyCode()){
			case KeyInput.KEY_UP	:
				enav=ENav.Up;
				break;
			case KeyInput.KEY_DOWN:
				enav=ENav.Down;
				break;
		}		
		
		navigateCmdHistOrHintBox(source, enav);
	}
	public void navigateCmdHistOrHintBox(Object objSource, ENav enav) {
		if(cc.iCmdHistoryCurrentIndex<0)cc.iCmdHistoryCurrentIndex=0; //cant underflow
		if(cc.iCmdHistoryCurrentIndex>cc.astrCmdHistory.size())cc.resetCmdHistoryCursor(); //iCmdHistoryCurrentIndex=astrCmdHistory.size(); //can overflow by 1
		
		if(objSource!=tfInput && objSource!=lstbxAutoCompleteHint)objSource=null;
		
		boolean bOk=false;
		switch(enav){
			case Up:
				if(objSource==null || objSource==lstbxAutoCompleteHint)bOk=navigateHint(-1);
				
				if((objSource==null && !bOk) || objSource==tfInput){
					cc.iCmdHistoryCurrentIndex--;
					/**
					 * to not lose last possibly typed (but not issued) cmd
					 */
					if(cc.iCmdHistoryCurrentIndex==(cc.astrCmdHistory.size()-1)){ //requested last entry
						strNotSubmitedCmd = dumpAndClearInputField();
					}
					fillInputFieldWithHistoryDataAtIndex(cc.iCmdHistoryCurrentIndex);
				}
				
				break;
			case Down:
				if(objSource==null || objSource==lstbxAutoCompleteHint)bOk=navigateHint(+1);
				
				if((objSource==null && !bOk) || objSource==tfInput){
					cc.iCmdHistoryCurrentIndex++;
					if(cc.iCmdHistoryCurrentIndex>=cc.astrCmdHistory.size()){
						if(strNotSubmitedCmd!=null){
							setInputField(strNotSubmitedCmd);
						}
					}
					fillInputFieldWithHistoryDataAtIndex(cc.iCmdHistoryCurrentIndex);
				}
				
				break;
		}
	}
	
//	/**
//	 * TODO: find a way to properly limit the chars length, or show it in a safer way?
//	 * If the string is too long, it will shrink the right side buttons until
//	 * their width becomes negative and application will crash...
//	 * 
//	 */
//	public String fixStatsWidth(String str){
//		int iMax=50;
//		if(str.length()<iMax)return str;
//		return str.substring(0, iMax);
//	}
//	
//	public void updateStats(){
//		if(!cc.tdStatsRefresh.isReady(true))return;
//		String str = cc.prepareStatsFieldText();
////		if(str.length()>wrap)
////		str+="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
//		str+=cc.strTest;
//		
//		lblStats.setText(str);
//		
//		try{
//			ctnrConsole.updateLogicalState(0.05f);
//		}catch(Exception ex){
//			cc.dumpExceptionEntry(ex);
//			lblStats.setText(fixStatsWidth(str));
//		}
//	}
	
	/**
	 * BugFix: because the related crash should/could be prevented/preventable.
	 * 
	 * Buttons get smashed, shrinking to less than 0, they seem to not accept preferred size constraint.
	 * 
	 * By fixating the size of the label, this crash preventer is not that necesary anymore.
	 */
	public void bugfixStatsLabelTextSize(){
		boolean bEnableThisCrashPreventer=false;if(!bEnableThisCrashPreventer)return;
		
		boolean bFailed=false;
		while(true){
			String str=lblStats.getText();
			if(str.isEmpty())break;
			
			boolean bCheckAlways=true; //safer
			if(!bCheckAlways){
				if(iStatsTextSafeLength!=null){
					if(str.length()>iStatsTextSafeLength){
						str=str.substring(0,iStatsTextSafeLength);
						lblStats.setText(str);
					}
					break;
				}
			}
			
			boolean bRetry=false;
			try{
				/**
				 * this is when crashes outside of here
				 */
				ctnrConsole.updateLogicalState(0.05f);
				
				if(bFailed){ //had failed, so look for a safe length
					if(iStatsTextSafeLength==null || !iStatsTextSafeLength.equals(str.length())){
						iStatsTextSafeLength=str.length();
						cc.dumpDebugEntry("StatsTextSafeLength="+str.length());
					}
				}
			}catch(Exception ex){
				if(bExceptionOnce){
					cc.dumpExceptionEntry(ex);
					bExceptionOnce=false;
				}
				
				str=str.substring(0,str.length()-1);
				lblStats.setText(str);
				
				bRetry = true;
				bFailed = true;
			}
			
			if(!bRetry)break;
		}
	}
	
	public void updateStats(){
		if(!cc.tdStatsRefresh.isReady(true))return;
		
		String str = cc.prepareStatsFieldText();
		
		if(Debug.i().isKeyEnabled(Debug.EKey.StatsText))str+=cc.strTest;
		
		lblStats.setText(str);
		
		bugfixStatsLabelTextSize();
	}	
	
	public void updateAutoCompleteHint() {
		String strInputText = getInputText();
		if(strInputText.isEmpty())return;
		strInputText=cc.extractCommandPart(strInputText,0);
		if(strInputText==null)return; //something invalid was typed...
		if(lstbxAutoCompleteHint.getParent()==null && bKeyControlIsPressed){
			/**
			 * in this case, there is no hint,
			 * and match mode: "contains" was requested by user,
			 * so fill it with something!
			 */
		}else{
			if(strInputTextPrevious.equals(strInputText))return;
		}
		
		ArrayList<String> astr = AutoComplete.i().autoComplete(
			strInputText, cc.astrCmdWithCmtValidList, bKeyControlIsPressed);
		
		boolean bShowHint = false;
		
		if(astr.size()==0){
			bShowHint=false; // empty string, or simply no matches
		}else
		if(astr.size()==1 && strInputText.equals(cc.extractCommandPart(astr.get(0),0))){
			// no extra matches found, only what was already typed was returned
			bShowHint=false;
		}else{
			bShowHint=true; // show all extra matches
		}
		
		if(bShowHint){
			for(int i=0;i<astr.size();i++){
				String str=astr.get(i);
				int iNL = str.indexOf("\n");
				if(iNL>=0){
					astr.set(i,str.substring(0,iNL));
				}
			}
			
			getAutoCompleteHint().clear();
			getAutoCompleteHint().addAll(astr);
			clearHintSelection();
			
			Node nodeParent = sapp.getGuiNode();
			if(!nodeParent.hasChild(lstbxAutoCompleteHint)){
				nodeParent.attachChild(lstbxAutoCompleteHint);
			}
			
			//lstbxAutoCompleteHint.setLocalTranslation(new Vector3f(0, -fInputHeight, 0));
			Vector3f v3f = tfInput.getWorldTranslation().clone();
			v3f.y -= tfInput.getSize().y;
			lstbxAutoCompleteHint.setLocalTranslation(v3f);
			
			float fEntryHeightGUESSED = fInputHeight; //TODO should be the listbox entry height
			float fAvailableHeight = v3fApplicationWindowSize.y -v3fConsoleSize.y -fEntryHeightGUESSED;
			int iVisibleItems = (int) (fAvailableHeight/fEntryHeightGUESSED);
			if(iVisibleItems==0)iVisibleItems=1;
			if(iVisibleItems>getAutoCompleteHint().size())iVisibleItems=getAutoCompleteHint().size();
			float fHintHeight = fEntryHeightGUESSED*iVisibleItems;
			if(fHintHeight>fAvailableHeight){
				cc.dumpDevWarnEntry("fHintHeight="+fHintHeight+",fAvailableHeight="+fAvailableHeight);
				fHintHeight=fAvailableHeight;
			}
			int iMinLinesGUESSED = 3; //seems to be required because the slider counts as 3 (up arrow, thumb, down arrow)
			float fMinimumHeightGUESSED = fEntryHeightGUESSED*iMinLinesGUESSED;
			if(fHintHeight<fMinimumHeightGUESSED)fHintHeight=fMinimumHeightGUESSED;
			setHintBoxSize(new Vector3f(widthForListbox(),fHintHeight,0), iVisibleItems);
//			lstbxAutoCompleteHint.setPreferredSize(new Vector3f(
//				);
//			lstbxAutoCompleteHint.setVisibleItems(iVisibleItems);//astr.size());
			
			lineWrapDisableForChildrenOf(lstbxAutoCompleteHint);
			
			scrollHintToIndex(0);
		}else{
			closeHint();
		}
		
		strInputTextPrevious = strInputText;
	}
	
	@Override
	public String getDumpAreaSliderStatInfo() {
		// this value is the top entry index
		int iMaxSliderIndex=getDumpEntries().size()-lstbxDumpArea.getGridPanel().getVisibleRows();
		
		return "Sl"
				+Misc.i().fmtFloat(getScrollDumpAreaFlindex(),0)+"/"+iMaxSliderIndex+"+"+lstbxDumpArea.getGridPanel().getVisibleRows()
				+"("+Misc.i().fmtFloat(100.0f -lstbxDumpArea.getSlider().getModel().getPercent()*100f,0)+"%)"
				+";";

	}
	
//	@Override
//	public int getCmdHistoryCurrentIndex() {
//		return iCmdHistoryCurrentIndex;
//	}
	
	@Override
	public int getLineWrapAt() {
		boolean bUseFixedWrapColumn = cc.btgUseFixedLineWrapModeForAllFonts.b();
		
		/**
		 * Mono spaced fonts can always have a fixed linewrap column!
		 */
		if(!bUseFixedWrapColumn)bUseFixedWrapColumn=STYLE_CONSOLE.equals(strStyle);
		
		if(bUseFixedWrapColumn){
			return (int)
				(widthForDumpEntryField() / fWidestCharForCurrentStyleFont)
				-iSkipCharsSafetyGUESSED;
		}
		
		return 0; //wrap will be dynamic
	}
	
	public void updateFontStuff(){
//		if(true)return; //dummified
//	if(cc.iConsoleMaxWidthInCharsForLineWrap!=null){
		
		/**
		 * W seems to be the widest in most/all chars sets
		 * so, when using a fixed wrap column, this is the safest char width reference!
		 */
		fWidestCharForCurrentStyleFont = fontWidth("W");
		
//		if(cc.iConsoleMaxWidthInCharsForLineWrap>0){
//			cc.iConsoleMaxWidthInCharsForLineWrap = (int) //like trunc
//				((widthForDumpEntryField()/fWidestCharForCurrentStyleFont)
//				-iSkipCharsSafetyGUESSED);
//		}
//	}
	}
	
	/**
	 * Auto wrap.
	 *  
	 * This is MUCH slower than using a mono spaced font and having a fixed linewrap column...
	 */
	@Override
	public ArrayList<String> wrapLineDynamically(DumpEntry de) {
		ArrayList<String> astrToDump = new ArrayList<String>();
		
		String[] astr = de.getLineBaking().split("\n");
		
		for(String strLine:astr){
			/**
			 * Dynamically adjust each line length based on the pixels size it will have
			 * after rendered.
			 * It removes characters from the end until the line fits on the limits.
			 */
			String strAfter = "";
			float fMaxWidth = widthForDumpEntryField() - iDotsMarginSafetyGUESSED;
			while(strLine.length()>0){
				while(fontWidth(strLine, strStyle, false) > fMaxWidth){
					int iLimit = strLine.length()-iJumpBackGUESSED;
					strAfter = strLine.substring(iLimit) + strAfter;
					strLine = strLine.substring(0, iLimit);
				}
				astrToDump.add(strLine);
				strLine = strAfter;
				strAfter="";
			}
		}
		
		return astrToDump;
	}
	
	@Override
	public void clearDumpAreaSelection() {
		lstbxDumpArea.getSelectionModel().setSelection(-1); //clear selection
	}
}

