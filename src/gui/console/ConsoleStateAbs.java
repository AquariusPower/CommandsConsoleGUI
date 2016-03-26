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

package gui.console;

import gui.console.ReflexFill.IReflexFillCfgVariant;
import gui.console.ReflexFill.ReflexFillCfg;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
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
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GridPanel;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.Styles;

/**
 * A graphical console where developers and users can issue application commands. 
 * Project at: https://github.com/AquariusPower/CommandsConsoleGUI
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class ConsoleStateAbs implements AppState, ReflexFill.IReflexFillCfg{
	protected FpsLimiter fpslState = new FpsLimiter();
	
//	public final String strInputIMCPREFIX = "CONSOLEGUISTATE_";
	public final String strFinalFieldInputCodePrefix="INPUT_MAPPING_CONSOLE_";
	public final StringField INPUT_MAPPING_CONSOLE_TOGGLE = new StringField(this,strFinalFieldInputCodePrefix);
	public final StringField INPUT_MAPPING_CONSOLE_SCROLL_UP = new StringField(this,strFinalFieldInputCodePrefix);
	public final StringField INPUT_MAPPING_CONSOLE_SCROLL_DOWN = new StringField(this,strFinalFieldInputCodePrefix);
	public final StringField INPUT_MAPPING_CONSOLE_SHIFT_PRESSED	= new StringField(this,strFinalFieldInputCodePrefix);
	public final StringField INPUT_MAPPING_CONSOLE_CONTROL_PRESSED	= new StringField(this,strFinalFieldInputCodePrefix);
	
	public final String STYLE_CONSOLE="console";
	
	protected boolean bStartupCmdQueueDone = false; 
	
	// not public... development token... 
	protected final String	TOKEN_CMD_NOT_WORKING_YET = "[NOTWORKINGYET]";
	
	/**
	 * keep "initialized" vars together!
	 */
	protected boolean	bInitialized;
	
	/**
	 * keep delayers together!
	 */
	protected TimedDelay tdScrollToBottomRequestAndSuspend = new TimedDelay(0.5f);
	protected TimedDelay tdScrollToBottomRetry = new TimedDelay(0.1f);
	protected TimedDelay tdLetCpuRest = new TimedDelay(0.1f);
	protected TimedDelay tdStatsRefresh = new TimedDelay(0.5f);
	protected TimedDelay tdDumpQueuedEntry = new TimedDelay(1f/5f); // per second
	protected TimedDelay tdSpareGpuFan = new TimedDelay(1.0f/60f); // like 60 FPS
	
	/**
	 * keep guesses together!
	 * guesses should not exist... 
	 * they are imprecise, they just "work" therefore they may "just break"... 
	 * TODO better algorithms/calculations are required...
	 */
	protected int	iJumpBackGUESSED = 1;
	protected int	iDotsMarginSafetyGUESSED = 0;
	protected int	iSkipCharsSafetyGUESSED = 1;
	protected float	fSafetyMarginGUESSED = 20f;
	
	/**
	 * some day this one may not be required
	 */
	ExtraFunctionalitiesHK efHK = null;
	
	/**
	 * other vars!
	 */
	protected Container	ctnrConsole;
	protected ListBox<String>	lstbx;
	protected TextField	tfInput;
//	protected TextField tfAutoCompleteHint;
	protected SimpleApplication	sapp;
	protected boolean	bEnabled;
	protected VersionedList<String>	vlstrDumpEntriesSlowedQueue = new VersionedList<String>();;
	protected VersionedList<String>	vlstrDumpEntries = new VersionedList<String>();;
	protected VersionedList<String>	vlstrAutoCompleteHint = new VersionedList<String>();;
	protected int	iShowRows = 1;
	protected Integer	iToggleConsoleKey;
	protected Integer	iVisibleRowsAdjustRequest = 0; //0 means dynamic
	protected String	strInfoEntryPrefix			=". ";
	protected String	strWarnEntryPrefix			="?Warn: ";
	protected String	strErrorEntryPrefix			="!ERROR: ";
	protected String	strExceptionEntryPrefix	="!EXCEPTION: ";
	protected String	strDevWarnEntryPrefix="?DevWarn: ";
	protected String	strDevInfoEntryPrefix=". DevInfo: ";
	protected String	strSubEntryPrefix="\t";
	protected boolean	bInitiallyClosed = true;
	protected ArrayList<String> astrCmdHistory = new ArrayList<String>();
	protected ArrayList<String> astrCmdWithCmtValidList = new ArrayList<String>();
	protected ArrayList<String> astrBaseCmdValidList = new ArrayList<String>();
	protected ArrayList<String> astrStyleList = new ArrayList<String>();
	protected int	iCmdHistoryCurrentIndex = 0;
	protected	String	strNotSubmitedCmd=null; //null is a marker here
	protected Panel	pnlTest;
	protected String	strTypeCmd="Cmd";
	protected Label	lblStats;
	protected Float	fLstbxEntryHeight;
	protected Float	fStatsHeight;
	protected Float	fInputHeight;
	protected int	iScrollRetryAttemptsDBG;
	protected int	iScrollRetryAttemptsMaxDBG;
	protected int iMaxCmdHistSize = 1000;
	protected int iMaxDumpEntriesAmount = 100000;
	protected String	strFilePrefix = "Console"; //ConsoleStateAbs.class.getSimpleName();
	protected String	strFileTypeLog = "log";
	protected String	strFileTypeConfig = "cfg";
	protected String	strFileCmdHistory = strFilePrefix+"-CmdHist";
	protected String	strFileLastDump = strFilePrefix+"-LastDump";
	protected String	strFileInitConsCmds = strFilePrefix+"-Init";
	protected String	strFileSetup = strFilePrefix+"-Setup";
	protected String	strFileDatabase = strFilePrefix+"-DB";
	protected File	flCmdHist;
	protected File	flLastDump;
	protected float	fConsoleHeightPercDefault = 0.5f;
	protected float	fConsoleHeightPerc = fConsoleHeightPercDefault;
	protected ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
	protected ArrayList<String>	astrExecConsoleCmdsQueue = new ArrayList<String>();
	protected File	flInit;
	protected File	flDB;
	protected float	fLstbxHeight;
	protected int	iSelectionIndex = -1;
	protected int	iSelectionIndexPreviousForFill = -1;
	protected Double	dMouseMaxScrollBy = null; //max scroll if set
//	protected boolean bShowLineIndex = true;
	protected String strStyle = BaseStyles.GLASS;
//	protected String strStyle = Styles.ROOT_STYLE;
	protected String	strInputTextPrevious = "";
	protected AnalogListener	alConsoleScroll;
	protected ActionListener	alConsoleToggle;
//	protected String	strValidCmdCharsRegex = "A-Za-z0-9_-"+"\\"+strCommandPrefixChar;
	protected String	strValidCmdCharsRegex = "a-zA-Z0-9_"; // better not allow "-" as has other uses like negate number and commands functionalities
	protected String	strStatsLast = "";
	protected Container	ctnrStatsAndControls;
	protected Vector3f	v3fStatsAndControlsSize;
	protected Button	btnClipboardShow;
	protected boolean	bConsoleStyleCreated;
//	protected boolean	bUseDumbWrap = true;
	protected Integer	iConsoleMaxWidthInCharsForLineWrap = 0;
	protected BitmapFont	fntMakeFixedWidth;
	protected StatsAppState	stateStats;
//	protected boolean	bEngineStatsFps;
//	protected float	fMonofontCharWidth;
//	protected GridPanel	gpListboxDumpArea;
	protected int	iCopyFrom = -1;
	protected int	iCopyTo = -1;
	protected Button	btnCopy;
	protected Button	btnPaste;
	protected boolean	bAddEmptyLineAfterCommand = true;
//	protected String	strLineEncloseChar = "'";
	protected String	strCmdLinePrepared = "";
	protected CharSequence	strReplaceTAB = "  ";
	protected float	fWidestCharForCurrentStyleFont;
	protected boolean	bKeyShiftIsPressed;
	protected boolean	bKeyControlIsPressed;
	protected ListBox<String>	lstbxAutoCompleteHint;
	protected Vector3f	v3fConsoleSize;
	protected Vector3f	v3fApplicationWindowSize;
//	protected String	strPreviousCmdHistoryKey;
	protected String	strPreviousInputValue;
	protected int	iCmdHistoryPreviousIndex;
//	protected boolean	bShowExecQueuedInfo = false;
	protected ConsoleCommands	cc;
//	protected Hashtable<String,Object> htUserVariables = new Hashtable<String,Object>();
	protected TreeMap<String,Object> tmUserVariables = 
		new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
//	protected Hashtable<String,Object> htRestrictedVariables = new Hashtable<String,Object>();
	protected TreeMap<String,Object> tmRestrictedVariables =
		new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	protected ArrayList<Alias> aAliasList = new ArrayList<Alias>();
	protected String	strCmdLineOriginal;
	protected boolean	bLastAliasCreatedSuccessfuly;
	
	protected float	fTPF;
//	protected int	iFpsCount;
//	protected long	lNanoPreviousSecond;
//	protected int	lPreviousSecondFPS;
//	protected int	iUpdateCount;
	protected long	lNanoFrameTime;
	protected long	lNanoFpsLimiterTime;

	protected Boolean	bIfConditionIsValid;

	protected ArrayList<DumpEntry> adeDumpEntryFastQueue = new ArrayList<DumpEntry>();

	protected Button	btnCut;

	protected File	flSetup;

//	private boolean	bMultiLineIfCondition;

//	private int	iIfConditionNesting;
	protected ArrayList<Boolean> aIfConditionNestedList = new ArrayList<Boolean>();

	private Boolean	bIfConditionExecCommands;

	private boolean	bIfEndIsRequired;
	
	protected static ConsoleStateAbs instance;
	public static ConsoleStateAbs i(){
		return instance;
	}
	public ConsoleStateAbs() {
		if(instance==null)instance=this;
		cc = new ConsoleCommands();
	}
	public ConsoleStateAbs(int iToggleConsoleKey) {
		this();
		this.bEnabled=true; //just to let it be initialized at startup by state manager
		this.iToggleConsoleKey=iToggleConsoleKey;
	}
	
	protected String fileNamePrepare(String strFileBaseName, String strFileType, boolean bAddDateTime){
		return strFileBaseName
				+(bAddDateTime?"-"+getDateTimeForFilename():"")
				+"."+strFileType;
	}
	protected String fileNamePrepareCfg(String strFileBaseName, boolean bAddDateTime){
		return fileNamePrepare(strFileBaseName, strFileTypeConfig, bAddDateTime);
	}
	protected String fileNamePrepareLog(String strFileBaseName, boolean bAddDateTime){
		return fileNamePrepare(strFileBaseName, strFileTypeLog, bAddDateTime);
	}
	
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		if(isInitialized())throw new NullPointerException("already initialized...");
		
		sapp = (SimpleApplication)app;
		
		sapp.getStateManager().attach(fpslState);
		
		GuiGlobals.initialize(sapp);
		BaseStyles.loadGlassStyle(); //do not mess with default user styles: GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
		
		// init dump file, MUST BE THE FIRST!
		flLastDump = new File(fileNamePrepareLog(strFileLastDump,false));
		flLastDump.delete(); //each run will have a new file
		
		// init cmd history
		flCmdHist = new File(fileNamePrepareLog(strFileCmdHistory,false));
		cmdHistLoad();
		
		// init valid cmd list
		executeCommand(null); //to populate the array with available commands
		
		// restricted vars setup
		setupVars(false);
		flSetup = new File(fileNamePrepareCfg(strFileSetup,false));
		if(flSetup.exists()){
			addToExecConsoleCommandQueue(fileLoad(flSetup));
		}
		
		// init user cfg
		flInit = new File(fileNamePrepareCfg(strFileInitConsCmds,false));
		if(flInit.exists()){
			addToExecConsoleCommandQueue(fileLoad(flInit));
		}else{
			fileAppendLine(flInit, cc.getCommentPrefix()+" User console commands here will be executed at startup.");
		}
		
		// init DB
		flDB = new File(fileNamePrepareCfg(strFileDatabase,false));
		
		// other inits
		addToExecConsoleCommandQueue(cc.CMD_FIX_LINE_WRAP);
		addToExecConsoleCommandQueue(cc.CMD_CONSOLE_SCROLL_BOTTOM);
		addToExecConsoleCommandQueue(cc.CMD_DB+" "+EDataBaseOperations.load);
		addToExecConsoleCommandQueue(
			cc.CMD_DB+" "+EDataBaseOperations.save+" "+cc.getCommentPrefix()+"to shrink it");
		/**
		 * KEEP AS LAST queued cmds below!!!
		 */
		// must be the last queued command after all init ones!
		addToExecConsoleCommandQueue(cc.btgShowExecQueuedInfo.getCmdIdAsCommand(true));
		// end of initialization
		addToExecConsoleCommandQueue(cc.RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE);
		if(bInitiallyClosed){
			// after all, close the console
			addToExecConsoleCommandQueue(cc.CMD_CLOSE_CONSOLE);
		}
		
		astrStyleList.add(BaseStyles.GLASS);
		astrStyleList.add(Styles.ROOT_STYLE);
		astrStyleList.add(STYLE_CONSOLE);
		
		stateStats = sapp.getStateManager().getState(StatsAppState.class);
		updateEngineStats();
		
		// instantiations initializer
		initialize();
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
		
//		if(isInitialized()){
			this.bEnabled=bEnabled;
			
			if(this.bEnabled){
				sapp.getGuiNode().attachChild(ctnrConsole);
				GuiGlobals.getInstance().requestFocus(tfInput);
			}else{
				ctnrConsole.removeFromParent();
				closeHint();
				GuiGlobals.getInstance().requestFocus(null);
			}
			
			GuiGlobals.getInstance().setCursorEventsEnabled(this.bEnabled);
//		}
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
	
	/**
	 * this can be used after a cleanup() too
	 */
	protected void initialize(){
		if(sapp==null)throw new NullPointerException("base initialization required");
		
//		tdLetCpuRest.updateTime();
		tdStatsRefresh.updateTime();
		tdDumpQueuedEntry.updateTime();
		
		createMonoSpaceFixedFontStyle();
		
		v3fApplicationWindowSize = new Vector3f(
			sapp.getContext().getSettings().getWidth(),
			sapp.getContext().getSettings().getHeight(),
			0);
		
		// main container
		ctnrConsole = new Container(new BorderLayout(), strStyle);
		int iMargin=10;
		v3fConsoleSize = new Vector3f(
			v3fApplicationWindowSize.x -(iMargin*2),
			(v3fApplicationWindowSize.y * fConsoleHeightPerc) -iMargin,
			0); //TODO why Z shouldnt be 0? changed to 0.1 and 1, but made no difference.
		ctnrConsole.setPreferredSize(v3fConsoleSize); //setSize() does not work well..
		sapp.getGuiNode().attachChild(ctnrConsole);
		ctnrConsole.setLocalTranslation(
			iMargin, 
			sapp.getContext().getSettings().getHeight()-iMargin, 
			0);
		
		/**
		 * TOP ELEMENT =================================================================
		 */
		ctnrStatsAndControls = new Container(strStyle);
		ctnrConsole.addChild(ctnrStatsAndControls, BorderLayout.Position.North);
		
		// console stats
		lblStats = new Label("Console stats.",strStyle);
		lblStats.setColor(new ColorRGBA(1,1,0.5f,1));
		fStatsHeight = retrieveBitmapTextFor(lblStats).getLineHeight();
		ctnrStatsAndControls.addChild(lblStats,0,0);
		
		// buttons
		ArrayList<Button> abu = new ArrayList<Button>();
		int iButtonIndex=0;
		btnClipboardShow = new Button("ShwClpbrd",strStyle);
		abu.add(btnClipboardShow);
//		btnClipboardShow.setTextHAlignment(HAlignment.Center);
////		btnClipboardShow.setPreferredSize(new Vector3f(100,20,0));
//		btnClipboardShow.addClickCommands(new ButtonClick());
//		ctnrStatsAndControls.addChild(btnClipboardShow,0,++iButtonIndex);
		
		btnCopy = new Button("Copy",strStyle);
		abu.add(btnCopy);
//		btnCopy.addClickCommands(new ButtonClick());
//		ctnrStatsAndControls.addChild(btnCopy,0,++iButtonIndex);
		
		btnPaste = new Button("Paste",strStyle);
		abu.add(btnPaste);
//		btnPaste.addClickCommands(new ButtonClick());
//		ctnrStatsAndControls.addChild(btnPaste,0,++iButtonIndex);
		
		btnCut = new Button("Cut",strStyle);
		abu.add(btnCut);
//		btnCut.addClickCommands(new ButtonClick());
//		ctnrStatsAndControls.addChild(btnCut,0,++iButtonIndex);
		
		for(Button btn:abu){
			btn.setTextHAlignment(HAlignment.Center);
			//btnClipboardShow.setPreferredSize(new Vector3f(100,20,0));
			btn.addClickCommands(new ButtonClick());
			ctnrStatsAndControls.addChild(btn,0,++iButtonIndex);
		}
		
		/**
		 * CENTER ELEMENT (dump entries area) ===========================================
		 */
		lstbx = new ListBox<String>(new VersionedList<String>(),strStyle);
		Vector3f v3fLstbxSize = v3fConsoleSize.clone();
		lstbx.setSize(v3fLstbxSize); // no need to update fLstbxHeight, will be automatic
		//TODO not working? lstbx.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		
		/**
		 * The existance of at least one entry is very important to help on initialization.
		 * Actually to determine the listbox entry height.
		 */
		if(vlstrDumpEntries.isEmpty())vlstrDumpEntries.add(""+cc.getCommentPrefix()+" Initializing console.");
		
		lstbx.setModel(vlstrDumpEntries);
		lstbx.setVisibleItems(iShowRows);
//		lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		ctnrConsole.addChild(lstbx, BorderLayout.Position.Center);
		
//		gpListboxDumpArea = lstbx.getGridPanel();
		
		/**
		 * BOTTOM ELEMENT =================================================================
		 */
		// input
		tfInput = new TextField(""+cc.getCommandPrefix(),strStyle);
		fInputHeight = retrieveBitmapTextFor(tfInput).getLineHeight();
		ctnrConsole.addChild( tfInput, BorderLayout.Position.South );
		
		// auto complete hint
//		tfAutoCompleteHint = new TextField("No hints yet...",strStyle);
		lstbxAutoCompleteHint = new ListBox<String>(new VersionedList<String>(),strStyle);
		lstbxAutoCompleteHint.setModel(vlstrAutoCompleteHint);
		
		mapKeys();
		
		// focus
		GuiGlobals.getInstance().requestFocus(tfInput);
		
		// help (last thing)
//		dumpInfoEntry("ListBox height = "+fLstbxHeight);
//		dumpAllStats();
//		dumpInfoEntry("Hit F10 to toggle console.");
		
		/**
		 * =======================================================================
		 * =========================== LAST THING ================================
		 * =======================================================================
		 */
		bInitialized=true;
	}
	
	protected class ButtonClick implements Command<Button>{
		@Override
		public void execute(Button source) {
			if(source.equals(btnClipboardShow)){
				String strClipboard=retrieveClipboardString();
//				dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+" ( each line enclosed with \\"+strLineEncloseChar+" ):");
				dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+":");
				String[] astr = strClipboard.split("\n");
//				for(String str:astr)dumpEntry(strLineEncloseChar+str+strLineEncloseChar);
//				dumpEntry(""); // this empty line for clipboard content display is i
				dumpEntry(">>> Clipboard BEGIN");
				for(int i=0;i<astr.length;i++){
					String str=astr[i];
					if(i<(astr.length-1))str+="\\n";
					dumpEntry(false,true,false,str);
				}
				dumpEntry("<<< Clipboard END");
				if(bAddEmptyLineAfterCommand)dumpEntry("");
//				dumpEntry("");
				scrollToBottomRequest();
			}else
			if(source.equals(btnCopy)){
				editCopyOrCut(false,false);
			}else
			if(source.equals(btnCut)){
				editCopyOrCut(false,true);
			}else
			if(source.equals(btnPaste)){
				editPaste();
			}
		}
	}
	
	protected String editCopyOrCut(boolean bJustCollectText, boolean bCut) {
//		Integer iCopyTo = getDumpAreaSelectedIndex();
		String strTextToCopy = null;
		
		int iCopyToWork = iCopyTo;
		int iCopyFromWork = iCopyFrom;
		
		if(iCopyToWork>=0){
			
			if(iCopyFromWork==-1)iCopyFromWork=iCopyToWork;
			
			// wrap mode overrides this behavior
			boolean bMultiLineMode = iCopyFromWork!=iCopyToWork;
			
			if(iCopyFromWork>iCopyToWork){
				int iCopyFromBkp=iCopyFromWork;
				iCopyFromWork=iCopyToWork;
				iCopyToWork=iCopyFromBkp;
			}
			
			strTextToCopy="";
			while(true){ //multi-line copy
				if(iCopyFromWork>=vlstrDumpEntries.size())break;
				
				String strEntry =	bCut ? vlstrDumpEntries.remove(iCopyFromWork) :
					vlstrDumpEntries.get(iCopyFromWork);
//				strEntry=strEntry.replace("\\n","\n"); //translate in-between newline requests into newline
				
				boolean bJoinWithNext = false;
				if(strEntry.endsWith("\\")){
					bJoinWithNext=true;
					
					/**
					 * removes trailing linewrap indicator
					 */
					strEntry = strEntry.substring(0, strEntry.length()-1); 
				}else
				if(strEntry.endsWith("\\n")){
					bJoinWithNext=true;
					strEntry = strEntry.substring(0, strEntry.length()-2);
					strEntry+="\n";
				}
				
				strTextToCopy+=strEntry;
				
				if(!bJoinWithNext)strTextToCopy+="\n";
				
				if(bMultiLineMode){
					/**
					 * this overrides wrap mode, as user may not want other lines
					 * as he/she used a multi-line mode.
					 */
					if((iCopyFromWork-iCopyToWork)==0)break;
					
					if(bCut){iCopyToWork--;}else{iCopyFromWork++;};
				}else{ // single line mode
					if(bJoinWithNext){
						if(bCut){iCopyToWork--;}else{iCopyFromWork++;};
					}else{
						break;
					}
				}
			}
			
			if(!bJustCollectText){
				putStringToClipboard(strTextToCopy);
				
				lstbx.getSelectionModel().setSelection(-1); //clear selection
			}
		}
		
		if(!bJustCollectText){
			iCopyFrom=-1;
			iCopyTo=-1;
		}
		
		return strTextToCopy;
	}

	protected void editPaste() {
		String strPasted = retrieveClipboardString(true);
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
	
	protected void cmdHistSave(String strCmd) {
		fileAppendLine(flCmdHist,strCmd);
	}
	
	protected ArrayList<String> fileLoad(String strFile) {
		return fileLoad(new File(strFile));
	}
	protected ArrayList<String> fileLoad(File fl) {
		ArrayList<String> astr = new ArrayList<String>();
		if(fl.exists()){
			try{
				BufferedReader br=null;
		    try {
		    	br = new BufferedReader(new FileReader(fl));
		    	while(true){
						String strLine = br.readLine();
						if(strLine==null)break;
						astr.add(strLine);
		    	}
				} catch (IOException e) {
					dumpExceptionEntry(e);
				}finally{
					if(br!=null)br.close();
				}
			} catch (IOException e) {
				dumpExceptionEntry(e);
			}
		}else{
			dumpWarnEntry("File not found: "+fl.getAbsolutePath());
		}
		
		return astr;
	}
	
	protected void cmdHistLoad() {
		astrCmdHistory.addAll(fileLoad(flCmdHist));
	}
	
	protected void dumpSave(DumpEntry de) {
//		if(de.isSavedToLogFile())return;
		fileAppendLine(flLastDump,de.getLineOriginal());
	}
	
	protected void fileAppendList(File fl, ArrayList<String> astr) {
		BufferedWriter bw = null;
		try{
			try {
				bw = new BufferedWriter(new FileWriter(fl, true));
				for(String str:astr){
					bw.write(str);
					bw.newLine();
				}
			} catch (IOException e) {
				dumpExceptionEntry(e);
			}finally{
				if(bw!=null)bw.close();
			}
		} catch (IOException e) {
			dumpExceptionEntry(e);
		}
	}
	
	protected void fileAppendLine(File fl, String str) {
		ArrayList<String> astr = new ArrayList<String>();
		astr.add(str);
		fileAppendList(fl, astr);
	}
	
	protected void cmdTest(){
		dumpInfoEntry("testing...");
		String strOption = paramString(1);
		
		if(strOption.equalsIgnoreCase("fps")){
//			sapp.setSettings(settings);
		}else
		if(strOption.equalsIgnoreCase("allchars")){
			for(char ch=0;ch<256;ch++){
				dumpSubEntry(""+(int)ch+"='"+Character.toString(ch)+"'");
			}
		}else{
//		dumpSubEntry("["+(char)Integer.parseInt(strParam1, 16)+"]");
//		if(getDumpAreaSelectedIndex()>=0){
//			dumpSubEntry("Selection:"+getDumpAreaSelectedIndex()+": '"+vlstrDumpEntries.get(getDumpAreaSelectedIndex())+"'");
//		}
		}
		
	}	
	
	/**
	 * DO NOT USE!
	 * overlapping problem, doesnt work well...
	 * keep this method as reference! 
	 */
	protected void tweakDefaultFontToBecomeFixedSize(){
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
	
	protected void createMonoSpaceFixedFontStyle(){
		if(bConsoleStyleCreated)return;
		
		BitmapFont font = sapp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
//		BitmapFont font = sapp.getAssetManager().loadFont("Interface/Fonts/Console512x.fnt");
		//TODO improve the font quality to be more readable, how???
		
		Styles styles = GuiGlobals.getInstance().getStyles();
		
		ColorRGBA clBg;
		
		Attributes attrs;
		attrs = styles.getSelector(STYLE_CONSOLE); // this also creates the style
		attrs.set("fontSize", 16);
		attrs.set("color", ColorRGBA.White.clone());
		clBg = ColorRGBA.Blue.clone();clBg.b=0.25f;clBg.a=0.75f;
		attrs.set("background", new QuadBackgroundComponent(clBg));
		attrs.set("font", font);
		
//			attrs = styles.getSelector("grid", STYLE_CONSOLE);
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,1,0,1)));
		
		attrs = styles.getSelector(Button.ELEMENT_ID, STYLE_CONSOLE);
		attrs.set("color", new ColorRGBA(0,1,0.5f,1));
		clBg = new ColorRGBA(0,0,0.125f,1);
		attrs.set(Button.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
		attrs = styles.getSelector(TextField.ELEMENT_ID, STYLE_CONSOLE);
		attrs.set("color", new ColorRGBA(0.75f,1,1,1));
		clBg = new ColorRGBA(0.15f, 0.25f, 0, 1);
		attrs.set(TextField.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
//		lstbx.getElementId().child(ListBox.SELECTOR_ID);
		attrs = styles.getSelector(ListBox.ELEMENT_ID, ListBox.SELECTOR_ID, STYLE_CONSOLE);
//			attrs = styles.getSelector("list", "selector", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Red.clone());
		clBg = ColorRGBA.Yellow.clone();clBg.a=0.25f;
		attrs.set(ListBox.LAYER_BACKGROUND, new QuadBackgroundComponent(clBg));
		
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
//
//			attrs = styles.getSelector("slider", "button", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Yellow.clone());
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
//			
//			attrs = styles.getSelector("grid", "button", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Yellow.clone());
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
		
//		String strAllChars="W";
//		fMonofontCharWidth = fontWidth(strAllChars,STYLE_CONSOLE);
		
		bConsoleStyleCreated=true;
	}
	
	protected float fontWidth(String strChars){
		return fontWidth(strChars, strStyle, true);
	}
	protected float fontWidth(String strChars, String strStyle, boolean bAveraged){
		float f = retrieveBitmapTextFor(new Label(strChars,strStyle)).getLineWidth();
		if(bAveraged)f/=strChars.length();
		return f;
	}
	
	protected void mapKeysForInputField(){
		// simple actions
		KeyActionListener actSimpleActions = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
//				boolean bShift = key.hasModifier(0x01);
//				boolean bAlt = key.hasModifier(0x001);
//				case KeyInput.KEY_INSERT: //shift+ins paste
					//TODO case KeyInput.KEY_INSERT: //ctrl+ins copy
					//TODO case KeyInput.KEY_DELETE: //shift+del cut
				
				switch(key.getKeyCode()){
					case KeyInput.KEY_B: 
						if(bControl)iCopyFrom = getDumpAreaSelectedIndex();
						break;
					case KeyInput.KEY_C: 
						if(bControl)editCopyOrCut(false,false);
						break;
					case KeyInput.KEY_V: 
						if(bControl)editPaste();
						break;
					case KeyInput.KEY_X: 
						if(bControl)editCopyOrCut(false,true);
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
						if(bControl)toggleLineCommentOrCommand();
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
//				if(iCmdHistoryCurrentIndex==null){
//					iCmdHistoryCurrentIndex=astrCmdHistory.size();
//				}
				
				if(iCmdHistoryCurrentIndex<0)iCmdHistoryCurrentIndex=0; //cant underflow
				if(iCmdHistoryCurrentIndex>astrCmdHistory.size())resetCmdHistoryCursor(); //iCmdHistoryCurrentIndex=astrCmdHistory.size(); //can overflow by 1
				
				switch(key.getKeyCode()){
					case KeyInput.KEY_UP	:
						if(!navigateHint(-1)){
							iCmdHistoryCurrentIndex--;
							/**
							 * to not lose last possibly typed (but not issued) cmd
							 */
							if(iCmdHistoryCurrentIndex==(astrCmdHistory.size()-1)){ //requested last entry
//								strNotSubmitedCmd = getInputText();
								strNotSubmitedCmd = dumpAndClearInputField();
//								checkInputEmptyDumpIfNot(true);
							}
							fillInputFieldWithHistoryDataAtIndex(iCmdHistoryCurrentIndex);
						}
						
						break;
					case KeyInput.KEY_DOWN:
						if(!navigateHint(+1)){
							iCmdHistoryCurrentIndex++;
							if(iCmdHistoryCurrentIndex>=astrCmdHistory.size()){
								if(strNotSubmitedCmd!=null){
									setInputField(strNotSubmitedCmd);
								}
							}
							fillInputFieldWithHistoryDataAtIndex(iCmdHistoryCurrentIndex);
						}
						
						break;
				}
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
	
	protected void toggleLineCommentOrCommand() {
		String str = getInputText();
		if(str.startsWith(""+cc.getCommentPrefix())){
			str=str.substring(1);
		}else{
			str=cc.getCommentPrefix()+str;
		}
		setInputField(str);
	}
	protected boolean isHintActive(){
		return lstbxAutoCompleteHint.getParent()!=null;
	}
	
	protected boolean navigateHint(int iAdd){
		if(!isHintActive())return false;
		
		if(
				getSelectedHint()!=null
				||
				(iCmdHistoryCurrentIndex+1)>=astrCmdHistory.size() // end of cmd history
		){
			int iMaxIndex = vlstrAutoCompleteHint.size()-1;
			if(iMaxIndex<0)return false;
			
			Integer iCurrentIndex = lstbxAutoCompleteHint.getSelectionModel().getSelection();
			if(iCurrentIndex==null)iCurrentIndex=0;
			
			iCurrentIndex+=iAdd;
			
			if(iCurrentIndex<-1)iCurrentIndex=-1; //will clear the listbox selection
			if(iCurrentIndex>iMaxIndex)iCurrentIndex=iMaxIndex;
			
			lstbxAutoCompleteHint.getSelectionModel().setSelection(iCurrentIndex);
			
			scrollHintToIndex(iCurrentIndex);
			
			return iCurrentIndex>-1;
		}
		
		return false;
	}
	
	protected String getSelectedHint(){
		if(!isHintActive())return null;
		Integer i = lstbxAutoCompleteHint.getSelectionModel().getSelection();
		if(i==null)return null;
		return vlstrAutoCompleteHint.get(i);
	}
	
	protected void mapKeys(){
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
	      	double dScrollCurrentFlindex = getScrollDumpAreaFlindex();
	      	double dScrollBy = iShowRows/5; //20% of the visible rows
	      	if(dMouseMaxScrollBy!=null){
		      	if(dScrollBy > dMouseMaxScrollBy)dScrollBy = dMouseMaxScrollBy;
	      	}
	      	if(dScrollBy < 1)dScrollBy = 1;
	      	
					if(INPUT_MAPPING_CONSOLE_SCROLL_UP.equals(name)){
						scrollDumpArea(dScrollCurrentFlindex - dScrollBy);
					}else
					if(INPUT_MAPPING_CONSOLE_SCROLL_DOWN.equals(name)){
						scrollDumpArea(dScrollCurrentFlindex + dScrollBy);
					}
	      }
			}
		};
    
    sapp.getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SCROLL_UP+"", tggScrollUp);
    sapp.getInputManager().addListener(alConsoleScroll, INPUT_MAPPING_CONSOLE_SCROLL_UP+"");
    
    sapp.getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"", tggScrollDown);
    sapp.getInputManager().addListener(alConsoleScroll, INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"");
	}
	
	protected void fillInputFieldWithHistoryDataAtIndex(int iIndex){
		if(astrCmdHistory.size()==0)return;
		if(iIndex<0)return;
		if(iIndex>=astrCmdHistory.size())return;
		
		setInputField(astrCmdHistory.get(iIndex));
	}
	
	@Override
	public void update(float tpf) {
		if(!isEnabled())return;
		fTPF = tpf;
		if(tdLetCpuRest.isActive() && !tdLetCpuRest.isReady(true))return;
		
		updateToggles();
		updateDumpAreaSelectedIndex();
		updateVisibleRowsAmount();
		updateStats();
		updateScrollToBottom();
		if(efHK!=null)efHK.updateHK();
		updateExecConsoleCmdQueue();
		updateInputFieldFillWithSelectedEntry();
		updateAutoCompleteHint();
		updateDumpQueueEntry();
		
		updateCurrentCmdHistoryEntryReset();
		
		GuiGlobals.getInstance().requestFocus(tfInput);
	}
	
	protected void updateToggles() {
		if(cc.btgEngineStatsView.checkChangedAndUpdate())updateEngineStats();
		if(cc.btgEngineStatsFps.checkChangedAndUpdate())updateEngineStats();
		if(cc.btgFpsLimit.checkChangedAndUpdate())fpslState.setEnabled(cc.btgFpsLimit.b());
		if(cc.btgConsoleCpuRest.checkChangedAndUpdate())tdLetCpuRest.setActive(cc.btgConsoleCpuRest.b());
	}
	protected void resetCmdHistoryCursor(){
		iCmdHistoryCurrentIndex = astrCmdHistory.size();
	}
	
	protected void updateCurrentCmdHistoryEntryReset() {
		String strNewInputValue = getInputText();
		if((iCmdHistoryCurrentIndex-iCmdHistoryPreviousIndex)==0){
			if(!strNewInputValue.equals(strPreviousInputValue)){
				/**
				 * user has deleted or typed some character
				 */
				resetCmdHistoryCursor();
			}
		}
		
		strPreviousInputValue=strNewInputValue;
		iCmdHistoryPreviousIndex=iCmdHistoryCurrentIndex;
	}
	protected void updateAutoCompleteHint() {
		String strInputText = getInputText();
		if(strInputText.isEmpty())return;
		strInputText=extractCommandPart(strInputText,0);
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
		
		ArrayList<String> astr = autoComplete(
			strInputText, astrCmdWithCmtValidList, bKeyControlIsPressed);
		
		boolean bShowHint = false;
		
		if(astr.size()==0){
			bShowHint=false; // empty string, or simply no matches
		}else
		if(astr.size()==1 && strInputText.equals(extractCommandPart(astr.get(0),0))){
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
			
			vlstrAutoCompleteHint.clear();
			vlstrAutoCompleteHint.addAll(astr);
			lstbxAutoCompleteHint.getSelectionModel().setSelection(-1);
			
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
			if(iVisibleItems>vlstrAutoCompleteHint.size())iVisibleItems=vlstrAutoCompleteHint.size();
			float fHintHeight = fEntryHeightGUESSED*iVisibleItems;
			if(fHintHeight>fAvailableHeight){
				dumpDevWarnEntry("fHintHeight="+fHintHeight+",fAvailableHeight="+fAvailableHeight);
				fHintHeight=fAvailableHeight;
			}
			int iMinLinesGUESSED = 3; //seems to be required because the slider counts as 3 (up arrow, thumb, down arrow)
			float fMinimumHeightGUESSED = fEntryHeightGUESSED*iMinLinesGUESSED;
			if(fHintHeight<fMinimumHeightGUESSED)fHintHeight=fMinimumHeightGUESSED;
			lstbxAutoCompleteHint.setPreferredSize(new Vector3f(
				widthForListbox(),
				fHintHeight, //fInputHeight*iCount,
				0));
			lstbxAutoCompleteHint.setVisibleItems(iVisibleItems);//astr.size());
			
			lineWrapDisableFor(lstbxAutoCompleteHint.getGridPanel());
			
			scrollHintToIndex(0);
		}else{
			closeHint();
		}
		
		strInputTextPrevious = strInputText;
	}
	
	protected void scrollHintToIndex(int i){
		int iVisibleCount = lstbxAutoCompleteHint.getVisibleItems();
		
		int iVisibleMinIndex = (int)(
			lstbxAutoCompleteHint.getSlider().getModel().getMaximum()
			-lstbxAutoCompleteHint.getSlider().getModel().getValue()
		);
		
		int iVisibleMaxIndex = iVisibleMinIndex + iVisibleCount;
		Integer iScrollMinIndexTo = null;
		if(i < iVisibleMinIndex){
			iScrollMinIndexTo = i;
		}else
		if(i >= iVisibleMaxIndex){
			iScrollMinIndexTo = i -iVisibleCount +1;
		}
		
		if(iScrollMinIndexTo!=null){
			double d = lstbxAutoCompleteHint.getSlider().getModel().getMaximum();
			d -= iScrollMinIndexTo;
			if(d<0)d=0;
			lstbxAutoCompleteHint.getSlider().getModel().setValue(d);
		}
	}
	
	protected void closeHint(){
		/*
		tfAutoCompleteHint.setText("");
		tfAutoCompleteHint.removeFromParent();
		*/
		vlstrAutoCompleteHint.clear();
		lstbxAutoCompleteHint.removeFromParent();
	}
	
//	protected void fillAutoCompleteHint(ArrayList<String> astr){
//		lstbxAutoCompleteHint;
//	}
	
	/**
	 * Validates if the first extracted word is a valid command.
	 * 
	 * @param strCmdFullChk can be the full command line here
	 * @return
	 */
	protected boolean validateBaseCommand(String strCmdFullChk){
		strCmdFullChk = extractCommandPart(strCmdFullChk,0);
//		if(strCmdFullChk.startsWith(strCommandPrefixChar)){
//			strCmdFullChk=strCmdFullChk.substring(strCommandPrefixChar.length());
//		}
		return astrBaseCmdValidList.contains(strCmdFullChk);
	}
	
	/**
	 * 
	 * @param strCmdFull
	 * @param iPart 0 is base command, 1.. are params
	 * @return
	 */
	protected String extractCommandPart(String strCmdFull, int iPart){
		if(strCmdFull.startsWith(""+cc.getCommandPrefix())){
			strCmdFull=strCmdFull.substring(1); //1 cc.getCommandPrefix()Char
		}
		
		String[] astr = strCmdFull.split("[^$"+strValidCmdCharsRegex+"]");
		if(astr.length>iPart){
			return astr[iPart];
		}
		
		return null;
	}
	
//	protected String extractFirstCommandPart(String strCmdFull){
//		if(strCmdFull.startsWith(strCommandPrefixChar)){
//			strCmdFull=strCmdFull.substring(strCommandPrefixChar.length());
//		}
//		return strCmdFull.split("[^"+strValidCmdCharsRegex+"]")[0];
//	}
	
//	protected boolean checkInputEmpty(){
//		return checkInputEmptyDumpIfNot(false);
//	}
	/**
	 * after trim(), if empty or have only the command prefix char, 
	 * will return true.
	 * @return
	 */
	protected boolean isInputTextFieldEmpty(){
//	protected boolean checkInputEmptyDumpIfNot(boolean bDumpContentsIfNotEmpty){
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
	
	protected String dumpAndClearInputField(){
		if(!isInputTextFieldEmpty()){
			dumpInfoEntry("Not issued command below:");
			String str = getInputText();
			dumpEntry(str); //so user will not lose what was typing...
			clearInputTextField();
			return str;
		}
		return null;
	}
	
	protected void updateInputFieldFillWithSelectedEntry() {
		// auto-fill with selected command
		if(getDumpAreaSelectedIndex()>=0){
			if(iSelectionIndexPreviousForFill!=getDumpAreaSelectedIndex()){ //to let user type things...
				updateCopyFrom();
				
				String strCmdChk = editCopyOrCut(true,false); //vlstrDumpEntries.get(getDumpAreaSelectedIndex()).trim();
				strCmdChk=strCmdChk.trim();
				if(validateBaseCommand(strCmdChk)){
					if(!strCmdChk.startsWith(""+cc.getCommandPrefix()))strCmdChk=cc.getCommandPrefix()+strCmdChk;
					dumpAndClearInputField();
					int iCommentBegin = strCmdChk.indexOf(cc.getCommentPrefix());
					if(iCommentBegin>=0)strCmdChk=strCmdChk.substring(0,iCommentBegin);
					if(strCmdChk.endsWith("\\n"))strCmdChk=strCmdChk.substring(0,strCmdChk.length()-2);
					if(strCmdChk.endsWith("\n" ))strCmdChk=strCmdChk.substring(0,strCmdChk.length()-1);
					setInputField(strCmdChk);
				}
				
				iSelectionIndexPreviousForFill = getDumpAreaSelectedIndex();
			}
		}
	}
	
	protected void setInputField(String str){
		/**
		 * do NOT trim() the string, it may be being auto completed and 
		 * an space being appended to help on typing new parameters.
		 */
		tfInput.setText(str.replace("\n", "\\n").replace("\r", ""));
	}
	
	protected void updateDumpAreaSelectedIndex(){
		Integer i = lstbx.getSelectionModel().getSelection();
		iSelectionIndex = i==null ? -1 : i;
	}
	
	protected int getDumpAreaSelectedIndex(){
		return iSelectionIndex;
	}
	
	protected void updateCopyFrom(){
		int iSelected = getDumpAreaSelectedIndex();
		boolean bMultiLineMode = bKeyShiftIsPressed;
		if(iSelected>=0){
			if(bMultiLineMode){
				if(iCopyTo==-1){
					iCopyFrom = iSelected;
					iCopyTo = iSelected;
				}else{
					iCopyFrom = iCopyTo;
					iCopyTo = iSelected;
				}
			}else{
				iCopyFrom = iSelected;
				iCopyTo = iSelected;
			}
			
//			int iCopyToBkp = iCopyTo;
//			iCopyTo = iSelected;
//			
//			if(bKeyShiftIsPressed){
//				
//			}else{
//				iCopyFrom = iSelected;
//			}
//			
//			if(iCopyTo==-1){
//				iCopyFrom = iSelected;
//				iCopyTo = iSelected;
//			}else{
//				if(bKeyShiftIsPressed){
//					if(iCopyTo != iSelected){ 	// another entry selected
//						iCopyFrom = iCopyTo;
//					}
//				}
//			}
//			
//			iCopyTo = iSelected;
		}
	}
	
	protected void cmdLineWrapDisableDumpArea(){
		lineWrapDisableFor(lstbx.getGridPanel());
	}
	protected void lineWrapDisableFor(GridPanel gp){
		for(Spatial spt:gp.getChildren()){
			if(spt instanceof Button){
				retrieveBitmapTextFor((Button)spt).setLineWrapMode(LineWrapMode.NoWrap);
			}
		}
	}
	
	protected void updateVisibleRowsAmount(){
		if(fLstbxHeight != lstbx.getSize().y){
			iVisibleRowsAdjustRequest = 0; //dynamic
		}
		
		if(iVisibleRowsAdjustRequest==null)return;
		
		Integer iForceAmount = iVisibleRowsAdjustRequest;
		if(iForceAmount>0){
			iShowRows=iForceAmount;
			lstbx.setVisibleItems(iShowRows);
//			lstbx.getGridPanel().setVisibleSize(iShowRows,1);
			return;
		}
		
		if(lstbx.getGridPanel().getChildren().isEmpty())return;
		
		Button	btnFixVisibleRowsHelper = null;
		for(Spatial spt:lstbx.getGridPanel().getChildren()){
			if(spt instanceof Button){
				btnFixVisibleRowsHelper = (Button)spt;
				break;
			}
		}
		if(btnFixVisibleRowsHelper==null)return;
		
		fLstbxEntryHeight = retrieveBitmapTextFor(btnFixVisibleRowsHelper).getLineHeight();
		if(fLstbxEntryHeight==null)return;
		
		fLstbxHeight = lstbx.getSize().y;
		
		float fHeightAvailable = fLstbxHeight;
//			float fHeightAvailable = fLstbxHeight -fInputHeight;
//			if(ctnrConsole.hasChild(lblStats)){
//				fHeightAvailable-=fStatsHeight;
//			}
		iShowRows = (int) (fHeightAvailable / fLstbxEntryHeight);
		lstbx.setVisibleItems(iShowRows);
//		lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		dumpInfoEntry("fLstbxEntryHeight="+fmtFloat(fLstbxEntryHeight)+", "+"iShowRows="+iShowRows);
		
		iVisibleRowsAdjustRequest=null;
		
		cmdLineWrapDisableDumpArea();
	}
	
	protected BitmapText retrieveBitmapTextFor(Panel pnl){
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
		/**
		 * if hint area is active and has a selected entry, 
		 * it will override default command submit.
		 */
		if(isHintActive()){
			String strHintCmd = getSelectedHint();
			if(strHintCmd!=null){
				strHintCmd=cc.getCommandPrefix()+extractCommandPart(strHintCmd,0)+" ";
				if(!getInputText().equals(strHintCmd)){
					setInputField(strHintCmd);
					return true;
				}
			}
		}
		
		return actionSubmitCommand(strCmd);
	}
	
	protected void clearInputTextField() {
		setInputField(""+cc.getCommandPrefix());
	}
	
	protected String getInputText() {
		return tfInput.getText();
	}
	
	public boolean actionSubmitCommand(final String strCmd){
		if(strCmd.isEmpty() || strCmd.trim().equals(""+cc.getCommandPrefix())){
			clearInputTextField(); 
			return false;
		}
		
		String strType=strTypeCmd;
		boolean bIsCmd=true;
		boolean bShowInfo=true;
		if(strCmd.trim().startsWith(""+cc.getCommentPrefix())){
			strType="Cmt";
			bIsCmd=false;
		}else
		if(!strCmd.trim().startsWith(""+cc.getCommandPrefix())){
			strType="Inv";
			bIsCmd=false;
		}
		
		if(bIsCmd){
			if(strCmd.trim().endsWith(""+cc.getCommentPrefix())){
				bShowInfo=false;
			}
		}
		
//		String strTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+": ";
		if(bShowInfo)dumpInfoEntry(strType+": "+strCmd);
		
		clearInputTextField(); 
		
		// history
		boolean bAdd=true;
		if(!astrCmdHistory.isEmpty()){
			if(astrCmdHistory.get(astrCmdHistory.size()-1).equals(strCmd)){
				bAdd=false; //prevent sequential dups
			}
		}
		
		if(bAdd){
			astrCmdHistory.add(strCmd);
			
			cmdHistSave(strCmd);
			while(astrCmdHistory.size()>iMaxCmdHistSize){
				astrCmdHistory.remove(0);
			}
		}
		
		resetCmdHistoryCursor();
		
		if(strType.equals(strTypeCmd)){
			if(!executeCommand(strCmd)){
				dumpWarnEntry(strType+": FAIL: "+strCmd);
				showHelpForFailedCommand(strCmd);
			}
			
			if(bAddEmptyLineAfterCommand ){
				dumpEntry("");
			}
		}
		
		scrollToBottomRequest();
		
		return bIsCmd;
	}
	
	protected void addToExecConsoleCommandQueue(ArrayList<String> astrCmdList){
		addToExecConsoleCommandQueue(astrCmdList,false,true);
	}
	/**
	 * 
	 * @param astrCmdList
	 * @param bPrepend will append if false
	 */
	protected void addToExecConsoleCommandQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex){
		astrCmdList = new ArrayList<String>(astrCmdList);
		
		if(bShowExecIndex){
			for(int i=0;i<astrCmdList.size();i++){
				astrCmdList.set(i, 
				astrCmdList.get(i)+cc.commentToAppend("ExecIndex="+i));
			}
		}
		
		if(bPrepend){
			Collections.reverse(astrCmdList);
		}
		
		for(String str:astrCmdList){
			addToExecConsoleCommandQueue(str, bPrepend);
		}
	}
	protected void addToExecConsoleCommandQueue(StringField strfFullCmdLine){
		addToExecConsoleCommandQueue(strfFullCmdLine.toString());
	}
	protected void addToExecConsoleCommandQueue(String strFullCmdLine){
		addToExecConsoleCommandQueue(strFullCmdLine,false);
	}
	protected void addToExecConsoleCommandQueue(String strFullCmdLine, boolean bPrepend){
		strFullCmdLine=strFullCmdLine.trim();
		
		if(strFullCmdLine.startsWith(""+cc.getCommentPrefix()))return;
		if(strFullCmdLine.isEmpty())return;
		if(strFullCmdLine.equals(""+cc.getCommandPrefix()))return;
		
		if(!strFullCmdLine.startsWith(""+cc.RESTRICTED_TOKEN)){
			if(!strFullCmdLine.startsWith(""+cc.getCommandPrefix())){
				strFullCmdLine=cc.getCommandPrefix()+strFullCmdLine;
			}
		}
		
		dumpDevInfoEntry("CmdQueued: "+strFullCmdLine+(bPrepend?" #Prepended":""));
		
		if(bPrepend){
			astrExecConsoleCmdsQueue.add(0,strFullCmdLine);
		}else{
			astrExecConsoleCmdsQueue.add(strFullCmdLine);
		}
	}
	
	protected void updateExecConsoleCmdQueue() {
		if(astrExecConsoleCmdsQueue.size()>0){ // one per time! NO while here!!!!
			String str=astrExecConsoleCmdsQueue.remove(0);
			if(!str.trim().endsWith(""+cc.getCommentPrefix())){
				if(cc.btgShowExecQueuedInfo.get()){ // prevent messing user init cfg console log
					dumpInfoEntry("ExecQueued: "+str);
				}
			}
			if(!executeCommand(str)){
				dumpWarnEntry("ExecQueuedFail: "+str);
			}
		}
	}
	
	protected void showHelpForFailedCommand(String strFullCmdLine){
		if(validateBaseCommand(strFullCmdLine)){
//			addToExecConsoleCommandQueue(cc.CMD_HELP+" "+extractCommandPart(strFullCmdLine,0));
			cmdShowHelp(extractCommandPart(strFullCmdLine,0));
		}else{
			dumpWarnEntry("Invalid command: "+strFullCmdLine);
		}
	}
	
	public void scrollToBottomRequest(){
		tdScrollToBottomRequestAndSuspend.updateTime();
		tdScrollToBottomRetry.updateTime();
	}
	protected void scrollToBottomRequestSuspend(){
		tdScrollToBottomRequestAndSuspend.reset();
		tdScrollToBottomRetry.reset();
		
		// update retry attempts for debug info
		if(iScrollRetryAttemptsDBG > iScrollRetryAttemptsMaxDBG){
			iScrollRetryAttemptsMaxDBG = iScrollRetryAttemptsDBG;
		}
		iScrollRetryAttemptsDBG=0;
	}
	protected void updateScrollToBottom(){
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
			if(Double.compare(lstbx.getSlider().getModel().getPercent(), 0.0)==0){
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
	protected void scrollDumpArea(double dIndex){
		/**
		 * the index is actually inverted
		 */
		double dMax = lstbx.getSlider().getModel().getMaximum();
		if(dIndex==-1)dIndex=dMax;
		dIndex = dMax-dIndex;
		double dPerc = dIndex/dMax;
		
		lstbx.getSlider().getModel().setPercent(dPerc);
		lstbx.getSlider().getModel().setValue(dIndex);
	}
	
	/**
	 * 
	 * @return a "floating point index" (Dlindex)
	 */
	public double getScrollDumpAreaFlindex(){
		return lstbx.getSlider().getModel().getMaximum()
				-lstbx.getSlider().getModel().getValue();
	}
	
	protected String getDateTimeForFilename(){
		return new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
	}
	
	protected String getSimpleTime(){
		return "["+new SimpleDateFormat("HH:mm:ss"+(cc.btgShowMiliseconds.get()?".SSS":"")).format(Calendar.getInstance().getTime())+"]";
	}
	
	protected void dumpEntry(String strLineOriginal){
		dumpEntry(true, true, false, strLineOriginal);
	}
	
	/**
	 * dump strings will always be logged to file even if disabled.
	 */
	public static class DumpEntry{
		/**
		 * Beware, better do NOT change these defaults,
		 * as many usages of DumpEntry may depend on it.
		 * Maybe extend this class to have other defaults.
		 */
		boolean bApplyNewLineRequests = false; //this is a special behavior, disabled by default
		boolean bDumpToConsole = true;
		boolean bUseSlowQueue = false;
		String strLineOriginal = null;
		
		public boolean isApplyNewLineRequests() {
			return bApplyNewLineRequests;
		}
		public DumpEntry setApplyNewLineRequests(boolean bApplyNewLineRequests) {
			this.bApplyNewLineRequests = bApplyNewLineRequests;
			return this;
		}
		public boolean isDump() {
			return bDumpToConsole;
		}
		public DumpEntry setDumpToConsole(boolean bDump) {
			this.bDumpToConsole = bDump;
			return this;
		}
		public boolean isUseQueue() {
			return bUseSlowQueue;
		}
		public DumpEntry setUseSlowQueue(boolean bUseQueue) {
			this.bUseSlowQueue = bUseQueue;
			return this;
		}
		public String getLineOriginal() {
			return strLineOriginal;
		}
		public DumpEntry setLineOriginal(String strLineOriginal) {
			this.strLineOriginal = strLineOriginal;
			return this;
		}
		
	}
	
	protected void dumpEntry(boolean bApplyNewLineRequests, boolean bDump, boolean bUseSlowQueue, String strLineOriginal){
		DumpEntry de = new DumpEntry()
			.setApplyNewLineRequests(bApplyNewLineRequests)
			.setDumpToConsole(bDump)
			.setUseSlowQueue(bUseSlowQueue)
			.setLineOriginal(strLineOriginal);
		
		dumpEntry(de);
	}
	
	/**
	 * Simplest dump entry method, but still provides line-wrap.
	 * 
	 * @param bDumpToConsole if false, will only log to file
	 * @param strLineOriginal
	 */
	protected void dumpEntry(DumpEntry de){
		dumpSave(de);
		
		if(!isInitialized()){
			adeDumpEntryFastQueue.add(de);
			return;
		}
		
		if(!de.bDumpToConsole)return;
		
		if(iConsoleMaxWidthInCharsForLineWrap==null){
			applyDumpEntryOrPutToSlowQueue(de.bUseSlowQueue, de.strLineOriginal);
			return;
		}
		
		ArrayList<String> astr = new ArrayList<String>();
		if(de.strLineOriginal.isEmpty()){
			astr.add(de.strLineOriginal);
		}else{
			String strLine = de.strLineOriginal.replace("\t", strReplaceTAB);
			strLine=strLine.replace("\r", ""); //removes carriage return
			
			if(de.bApplyNewLineRequests){
				strLine=strLine.replace("\\n","\n"); //converts newline request into newline char
			}else{
				strLine=strLine.replace("\n","\\n"); //disables any newline char without losing it
			}
			
			int iWrapAt = iConsoleMaxWidthInCharsForLineWrap;
			if(STYLE_CONSOLE.equals(strStyle)){ //TODO is faster?
				iWrapAt = (int) (widthForDumpEntryField() / fWidestCharForCurrentStyleFont ); //'W' but any char will do for monospaced font
			}
			
			//TODO use \n to create a new line properly
			if(iWrapAt>0){ //fixed chars wrap
				String strLineToDump="";
				boolean bDumpAdd=false;
				for (int i=0;i<strLine.length();i++){
					char ch = strLine.charAt(i);
					strLineToDump+=ch;
					if(ch=='\n'){
						bDumpAdd=true;
					}else
					if(strLineToDump.length()==iWrapAt){
						bDumpAdd=true;
					}else
					if(i==(strLine.length()-1)){
						bDumpAdd=true;
					}
					
					if(bDumpAdd){
						astr.add(strLineToDump);
						strLineToDump="";
						bDumpAdd=false;
					}
				}
				
//						for (int i=0;i<strLine.length();i+=iWrapAt){
//							String strLineToDump = strLine.substring(
//									i, 
//									Math.min(strLine.length(),i+iWrapAt)
//								);
//							astr.add(strLineToDump);
//						}
			}else{ // auto wrap, TODO is this slow?
				String strAfter = "";
				float fMaxWidth = widthForDumpEntryField() - iDotsMarginSafetyGUESSED;
				while(strLine.length()>0){
					while(fontWidth(strLine, strStyle, false) > fMaxWidth){
						int iLimit = strLine.length()-iJumpBackGUESSED;
						strAfter = strLine.substring(iLimit) + strAfter;
						strLine = strLine.substring(0, iLimit);
					}
					astr.add(strLine);
					strLine = strAfter;
					strAfter="";
				}
			}
		}
		
		/**
		 * ADD LINE WRAP INDICATOR
		 */
		for(int i=0;i<astr.size();i++){
			String strPart = astr.get(i);
			if(i<(astr.size()-1)){
				if(strPart.endsWith("\n")){
					strPart=strPart.substring(0, strPart.length()-1)+"\\n"; // new line indicator
				}else{
					strPart+="\\"; // line wrap indicator
				}
			}
			
			applyDumpEntryOrPutToSlowQueue(de.bUseSlowQueue, strPart);
		}
		
	}
	
	protected void applyDumpEntryOrPutToSlowQueue(boolean bUseSlowQueue, String str) {
		if(bUseSlowQueue){
			vlstrDumpEntriesSlowedQueue.add(str);
		}else{
			vlstrDumpEntries.add(str);
		}
	}
	
	protected void updateDumpQueueEntry(){
		while(adeDumpEntryFastQueue.size()>0){
			dumpEntry(adeDumpEntryFastQueue.remove(0));
		}
			
		if(!tdDumpQueuedEntry.isReady(true))return;
		
		if(vlstrDumpEntriesSlowedQueue.size()>0){
			vlstrDumpEntries.add(vlstrDumpEntriesSlowedQueue.remove(0));
			
			while(vlstrDumpEntries.size() > iMaxDumpEntriesAmount){
				vlstrDumpEntries.remove(0);
			}
		}
	}
	
	protected void dumpInfoEntry(String str){
		dumpEntry(false, cc.btgShowInfo.get(), false, getSimpleTime()+strInfoEntryPrefix+str);
	}
	
	protected void dumpWarnEntry(String str){
		dumpEntry(false, cc.btgShowWarn.get(), false, getSimpleTime()+strWarnEntryPrefix+str);
	}
	
	protected void dumpErrorEntry(String str){
		dumpEntry(new DumpEntry()
			.setDumpToConsole(cc.btgShowWarn.get())
			.setLineOriginal(getSimpleTime()+strErrorEntryPrefix+str)
		);
//		dumpEntry(false, cc.btgShowWarn.get(), false, getSimpleTime()+strErrorEntryPrefix+str);
	}
	
	/**
	 * warnings that should not bother end users...
	 * @param str
	 */
	protected void dumpDevWarnEntry(String str){
		dumpEntry(false, cc.btgShowDeveloperWarn.get(), false, 
			getSimpleTime()+strDevWarnEntryPrefix+str);
	}
	
	protected void dumpDevInfoEntry(String str){
		dumpEntry(new DumpEntry()
			.setDumpToConsole(cc.btgShowDeveloperInfo.get())
			.setLineOriginal(getSimpleTime()+strDevInfoEntryPrefix+str)
		);
//		dumpEntry(false, cc.btgShowDeveloperInfo.get(), false, 
//			getSimpleTime()+strDevInfoEntryPrefix+str);
	}
	
	protected void dumpExceptionEntry(Exception e){
		dumpEntry(false, cc.btgShowException.get(), false, 
			getSimpleTime()+strExceptionEntryPrefix+e.toString());
		e.printStackTrace();
	}
	
	/**
	 * a simple, usually indented, output
	 * @param str
	 */
	protected void dumpSubEntry(String str){
		dumpEntry(strSubEntryPrefix+str);
	}
	
	public void addCmdToValidList(String strNew, boolean bSkipSortCheck){
		if(!astrCmdWithCmtValidList.contains(strNew)){
			if(!strNew.startsWith(TOKEN_CMD_NOT_WORKING_YET)){
				astrCmdWithCmtValidList.add(strNew);
//				astrBaseCmdValidList.add(str.split("[^"+strValidCmdCharsRegex +"]")[0]);
				String strBaseCmdNew = extractCommandPart(strNew,0);
				if(!bSkipSortCheck && astrBaseCmdValidList.size()>0){
					String strLast = astrBaseCmdValidList.get(astrBaseCmdValidList.size()-1);
					if(strLast.compareToIgnoreCase(strBaseCmdNew)>0){
//						if(
//								strLast.startsWith(BoolToggler.getPrefix())
//								&&
//								strLast.endsWith(BoolToggler.getSuffix())
//						){
//						}else{
							dumpDevWarnEntry("sorting required, last '"+strLast+"' new '"+strBaseCmdNew+"'");
//						}
					}
				}
				astrBaseCmdValidList.add(strBaseCmdNew);
			}
		}
	}
	
	protected boolean isCommentedLine(){
		if(strCmdLinePrepared==null)return false;
		return strCmdLinePrepared.trim().startsWith(""+cc.getCommentPrefix());
	}
	
	protected boolean checkCmdValidityBoolTogglers(){
		cc.btgReferenceMatched=null;
		for(BoolToggler btg : BoolToggler.getBoolTogglerListCopy()){
			if(checkCmdValidity(btg.getCmdId(), "[bEnable] "+btg.getHelp(), true)){
				cc.btgReferenceMatched = btg;
				break;
			}
		}
		return cc.btgReferenceMatched!=null;
	}
	protected boolean checkCmdValidity(String strValidCmd){
		return checkCmdValidity(strValidCmd, null);
	}
	protected boolean checkCmdValidity(StringField strfValidCmd, String strComment){
		return checkCmdValidity(strfValidCmd.toString(), strComment);
	}
	protected boolean checkCmdValidity(String strValidCmd, String strComment){
		return checkCmdValidity(strValidCmd, strComment, false);
	}
	protected boolean checkCmdValidity(String strValidCmd, String strComment, boolean bSkipSortCheck){
		if(strCmdLinePrepared==null){
			if(strComment!=null){
				strValidCmd+=cc.commentToAppend(strComment);
			}
			
			addCmdToValidList(strValidCmd,bSkipSortCheck);
			
			return false;
		}
		
		if(cc.RESTRICTED_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared))return false;
		if(isCommentedLine())return false;
		if(strCmdLinePrepared.trim().isEmpty())return false;
		
//		String strCheck = strPreparedCmdLine;
//		strCheck = strCheck.trim().split(" ")[0];
		strValidCmd = strValidCmd.trim().split(" ")[0];
		
//		return strCheck.equalsIgnoreCase(strValidCmd);
		return paramString(0).equalsIgnoreCase(strValidCmd);
	}
	
	protected void autoCompleteInputField(){
		autoCompleteInputField(false);
	}
	protected void autoCompleteInputField(boolean bMatchContains){
		final String strCmdPart = getInputText();
		
		String strCompletedCmd = autoCompleteWork(strCmdPart,bMatchContains);
		
		/**
		 * parameters completion!
		 */
		if(strCompletedCmd.equals(strCmdPart)){
			String strBaseCmd = extractCommandPart(strCmdPart,0);
			String strParam1 = extractCommandPart(strCmdPart,1);
			
			String strCmd=null;
			ArrayList<String> astrOptList = null;
			if(cc.CMD_CONSOLE_STYLE.equals(strBaseCmd)){
				strCmd=cc.CMD_CONSOLE_STYLE+" ";
				astrOptList=astrStyleList;
			}else
			if(cc.CMD_DB.equals(strBaseCmd)){
				strCmd=cc.CMD_DB+" ";
				astrOptList=EDataBaseOperations.getValuesAsArrayList();
			}
			
//			if(strParam1!=null){
			if(astrOptList!=null){
				strCompletedCmd=""+cc.getCommandPrefix()+strCmd;
				
				ArrayList<String> astr = strParam1==null ? astrOptList :
					autoComplete(strParam1, astrOptList, bMatchContains);
				if(astr.size()==1){
					strCompletedCmd+=astr.get(0);
				}else{
					dumpInfoEntry("Param autocomplete:");
					for(String str:astr){
						if(strParam1!=null && str.equals(astr.get(0)))continue;
						dumpSubEntry(str);
					}
					
					if(strParam1!=null){
						strCompletedCmd+=astr.get(0); //best partial param match
					}
				}
			}
//			}
		}
		
		if(strCompletedCmd.trim().isEmpty())strCompletedCmd=""+cc.getCommandPrefix();
		setInputField(strCompletedCmd);
		
		scrollToBottomRequest();
	}
	
	protected String autoCompleteWork(String strCmdPart, boolean bMatchContains){
		String strCmdPartOriginal = strCmdPart;
		strCmdPart=strCmdPart.trim();
		
		// no command typed
		if(strCmdPart.equalsIgnoreCase(""+cc.getCommandPrefix()) || strCmdPart.isEmpty())
			return strCmdPartOriginal;
		
		strCmdPart=strCmdPart.replaceFirst("^"+cc.getCommandPrefix(), "");
		
		// do not allow invalid chars
		if(!strCmdPart.matches("["+strValidCmdCharsRegex+"]*"))
			return strCmdPartOriginal;
		
		ArrayList<String> astr = autoComplete(strCmdPart, astrCmdWithCmtValidList, bMatchContains);
		String strFirst=astr.get(0); //the actual stored command may come with comments appended
		String strAppendSpace = "";
		if(astr.size()==1 && validateBaseCommand(strFirst)){
			strAppendSpace=" "; //found an exact command valid match, so add space
		}
////		if(astr.size()==1 && extractCommandPart(strFirst,0).length() > strCmdPart.length()){
//		if(astr.size()==1 && strFirst.length() > strCmdPart.length()){
//			strAppendSpace=" "; //found an exact command valid match, so add space
//		}
		
		// many possible matches
		if(astr.size()>1){
			dumpInfoEntry("AutoComplete: ");
			for(String str:astr){
				if(str.equals(strFirst))continue; //skip the partial improved match, 1st entry
				dumpSubEntry(cc.getCommandPrefix()+str);
			}
		}
		
		return cc.getCommandPrefix()+strFirst.split(" ")[0]+strAppendSpace;
	}
	
	/**
	 * Matching is case insensitive.
	 * 
	 * @param strPart partial match
	 * @param astrAllPossibilities all possible values to check for a match
	 * @return 
	 * 	If it has more than one entry, the first one will be an improved partial match.
	 * 	If it has only one entry, or it will be the unmodified part, 
	 *		or (if its length is bigger) it will be an exact match!
	 */
	public static ArrayList<String> autoComplete(String strPart, ArrayList<String> astrAllPossibilities, boolean bMatchContains){
		ArrayList<String> astrPossibleMatches = new ArrayList<String>();
		
		strPart=strPart.trim();
		if(strPart.isEmpty())return astrPossibleMatches;
//		if(strPart.matches("[^"+strValidCmdCharsRegex+"]"))return astrPossibleMatches;
		for(String str:astrAllPossibilities){
			if(bMatchContains){
				if(str.toLowerCase().contains(strPart.toLowerCase())){
					astrPossibleMatches.add(str);
				}
			}else{
				if(str.toLowerCase().startsWith(strPart.toLowerCase())){
					astrPossibleMatches.add(str);
				}
			}
		}
		
		// found single possibility
		if(astrPossibleMatches.size()==1)
			return astrPossibleMatches;
		
		if(!bMatchContains){
			lbMatch:while(true){
				Character ch = null;
				for(String str:astrPossibleMatches){
					if(str.length()<=strPart.length())break lbMatch;
					
					Character chOther = str.charAt(strPart.length());
					if(ch==null){
						ch = chOther;
					}
					
					if(Character.toLowerCase(ch)!=Character.toLowerCase(chOther)){
						break lbMatch;
					}
				}
				
				if(ch==null)break;
				strPart+=ch;
			}
		}
		
		// prepend partial better match, or simply unmodified part
		astrPossibleMatches.add(0,strPart);
		
		return astrPossibleMatches;
	}
	
	/**
	 * Each param can be enclosed within double quotes (")
	 * @param strFullCmdLine
	 * @return
	 */
	protected String convertToCmdParams(String strFullCmdLine){
		astrCmdAndParams.clear();
		
		/**
		 * remove comment
		 */
		int iCommentAt = strFullCmdLine.indexOf(cc.getCommentPrefix());
		String strComment = "";
		if(iCommentAt>=0){
			strComment=strFullCmdLine.substring(iCommentAt);
			strFullCmdLine=strFullCmdLine.substring(0,iCommentAt);
		}
		
		/**
		 * queue multicommands line
		 */
		if(strFullCmdLine.contains(""+cc.getCommandDelimiter())){
			ArrayList<String> astrMulti = new ArrayList<String>();
			astrMulti.addAll(Arrays.asList(strFullCmdLine.split(""+cc.getCommandDelimiter())));
			for(int i=0;i<astrMulti.size();i++){
				/**
				 * replace by propagating the existing comment to each part that will be executed
				 */
				astrMulti.set(i, astrMulti.get(i).trim()+" "+cc.getCommentPrefix()+"SplitCmdLine "+strComment);
			}
			addToExecConsoleCommandQueue(astrMulti,true,true);
			return cc.RESTRICTED_CMD_SKIP_CURRENT_COMMAND.toString();
		}
		
		/**
		 * Prepare parameters, that can be enclosed in double quotes.
		 * Param 0 is the actual command
		 */
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(strFullCmdLine);
		while (m.find()){
			String str=m.group(1);
			if(str!=null){
				if(str.trim().startsWith(""+cc.getCommentPrefix()))break; //ignore comments
				str=str.trim();
				str=str.replace("\"", ""); //remove all quotes on the string TODO could be only 1st and last? mmm... too much trouble...
				astrCmdAndParams.add(str);
			}
		}
		
		return strFullCmdLine;
	}
	
	/**
	 * 
	 * @return the first "word" in the command line, is the command
	 */
	protected String paramCommand(){
		return paramString(0);
	}
	
	/**
	 * 
	 * @param iIndex 0 is the command, >=1 are parameters
	 * @return
	 */
	protected String paramString(int iIndex){
		if(iIndex<astrCmdAndParams.size()){
			String str=astrCmdAndParams.get(iIndex);
			str = applyVariablesValues(str);
			return str;
		}
		return null;
	}
	protected String paramStringConcatenateAllFrom(int iStartIndex){
		String str=null;
		while(iStartIndex<astrCmdAndParams.size()){
			if(str!=null){
				str+=" ";
			}else{
				str="";
			}
			
			str+=astrCmdAndParams.get(iStartIndex++);
		}
		
		if(str!=null){
			str = applyVariablesValues(str);
		}
		
		return str;
	}
	
	/**
	 * 
	 * @param bAll if false, will bring only user variables
	 * @return
	 */
	protected ArrayList<String> getVariablesIdentifiers(boolean bAll){
		ArrayList<String> astr = Lists.newArrayList(tmUserVariables.keySet().iterator());
		if(bAll)astr.addAll(Lists.newArrayList(tmRestrictedVariables.keySet().iterator()));
		Collections.sort(astr);
		return astr;
	}
	
	protected String applyVariablesValues(String strParam){
		for(String strVarId : getVariablesIdentifiers(true)){
			String strToReplace=cc.getVariableExpandPrefix()+"{"+strVarId+"}";
			if(strParam.toLowerCase().contains(strToReplace.toLowerCase())){
//				strParam=strParam.replace(strToReplace, ""+getVarHT(strVarId).get(strVarId));
				strParam=strParam.replaceAll(
					"(?i)"+Pattern.quote(strToReplace), 
					""+getVarHT(strVarId).get(strVarId));
			}
		}
		return strParam;
	}
	
	/**
	 * 
	 * @return false if toggle failed
	 */
	protected boolean toggle(BoolToggler btg){
		if(paramBooleanCheckForToggle(1)){
			Boolean bEnable = paramBoolean(1);
			btg.set(bEnable==null ? !btg.get() : bEnable); //override
			dumpInfoEntry("Toggle, setting "+paramString(0)+" to "+btg.get());
			return true;
		}
		return false;
	}
	
	protected Boolean paramBooleanCheckForToggle(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return true; //if there was no param, will work like toggle
		
		Boolean b = paramBoolean(iIndex);
		if(b==null)return false; //if there was a param but it is invalid, will prevent toggle
		
		return true; // if reach here, will not be toggle, will be a set override
	}
	protected Boolean paramBoolean(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return null;
		/**
		 *	Not using java default method because it is permissive towards "false", ex.:
		 *	if user type "tre" instead of "true", it will result in `false`.
		 *	But false may be an undesired option.
		 *	Instead, user will be warned of the wrong typed value "tre".
		return Boolean.parseBoolean(str);
		 */
		if(str.equals("0"))return false;
		if(str.equals("1"))return true;
		if(str.equalsIgnoreCase("false"))return false;
		if(str.equalsIgnoreCase("true"))return true;
		
		dumpWarnEntry("invalid boolean value: "+str);
		
		return null;
	}
	protected Integer paramInt(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return null;
		return Integer.parseInt(str);
	}
	protected Float paramFloat(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return null;
		return Float.parseFloat(str);
	}
	
	protected String prepareCmdAndParams(String strFullCmdLine){
		if(strFullCmdLine!=null){
			strFullCmdLine = strFullCmdLine.trim();
			
			if(strFullCmdLine.isEmpty())return null; //dummy line
			
			// a comment shall not create any warning based on false return value...
			if(strFullCmdLine.startsWith(""+cc.getCommentPrefix()))return null; //comment is a "dummy command"
			
			// now it is possibly a command
			
			strFullCmdLine = strFullCmdLine.substring(1); //1 cc.getCommandPrefix()Char
			strFullCmdLine = strFullCmdLine.trim();
			
			if(strFullCmdLine.endsWith(""+cc.getCommentPrefix())){
				strFullCmdLine=strFullCmdLine.substring(0,strFullCmdLine.length()-1); //-1 cc.getCommentPrefix()Char
			}
			
			return convertToCmdParams(strFullCmdLine);
		}
		
		return null;
	}
	
	public String getPreparedCmdLine(){
		return strCmdLinePrepared;
	}
	
//	public class Alias{
//		String strAliasId;
//		String strCmdLine; // may contain ending comment too
//		boolean	bBlocked;
//		
//		@Override
//		public String toString() {
//			return cc.getCommandPrefix()+"alias "
//				+(bBlocked?chAliasBlockedToken:"")
//				+strAliasId+" "+strCmdLine;
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			Alias other = (Alias) obj;
//			if (!getOuterType().equals(other.getOuterType()))
//				return false;
//			if (bBlocked != other.bBlocked)
//				return false;
//			if (strAliasId == null) {
//				if (other.strAliasId != null)
//					return false;
//			} else if (!strAliasId.equals(other.strAliasId))
//				return false;
//			if (strCmdLine == null) {
//				if (other.strCmdLine != null)
//					return false;
//			} else if (!strCmdLine.equals(other.strCmdLine))
//				return false;
//			return true;
//		}
//		
//		/**
//		 * IMPORTANT:
//		 * on regenerate, comment out the getOuterType() line!
//		 * does NOT matter its parent...
//		 */
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + getOuterType().hashCode();
//			result = prime * result + (bBlocked ? 1231 : 1237);
//			result = prime * result
//					+ ((strAliasId == null) ? 0 : strAliasId.hashCode());
//			result = prime * result
//					+ ((strCmdLine == null) ? 0 : strCmdLine.hashCode());
//			return result;
//		}
//
//		protected ConsoleGuiState getOuterType() {
//			return ConsoleGuiState.this;
//		}
//	}
	
	protected Alias getAlias(String strAliasId){
		Alias aliasFound=null;
		for(Alias aliasCheck : aAliasList){
			if(aliasCheck.strAliasId.toLowerCase().equals(strAliasId.toLowerCase())){
				aliasFound = aliasCheck;
				break;
			}
		}
		return aliasFound;
	}
	
	protected boolean hasVar(String strVarId){
		return getVarHT(strVarId).get(strVarId)!=null;
	}
	
	protected boolean cmdRawLineCheckAlias(){
		bLastAliasCreatedSuccessfuly=false;
		
		if(strCmdLineOriginal==null)return false;
		
		String strCmdLine = strCmdLineOriginal.trim();
		String strExecAliasPrefix = ""+cc.getCommandPrefix()+cc.getAliasPrefix();
		if(strCmdLine.startsWith(cc.getCommandPrefix()+"alias ")){
			/**
			 * create
			 */
			Alias alias = new Alias();
			
			String[] astr = strCmdLine.split(" ");
			if(astr.length>=3){
				alias.strAliasId=astr[1];
				if(hasVar(alias.strAliasId)){
					dumpErrorEntry("Alias identifier '"+alias.strAliasId+"' conflicts with existing variable!");
					return false;
				}
				
				alias.strCmdLine=String.join(" ", Arrays.copyOfRange(astr, 2, astr.length));
				
				Alias aliasFound=getAlias(alias.strAliasId);
				if(aliasFound!=null)aAliasList.remove(aliasFound);
				
				aAliasList.add(alias);
				fileAppendLine(flDB, alias.toString());
				dumpSubEntry(alias.toString());
				
				bLastAliasCreatedSuccessfuly = true;
			}
		}else
		if(strCmdLine.startsWith(strExecAliasPrefix)){
			/**
			 * execute
			 */
			String strAliasId=strCmdLine
				.split(" ")[0]
				.substring(strExecAliasPrefix.length())
				.toLowerCase();
			for(Alias alias:aAliasList){
				if(!alias.strAliasId.toLowerCase().equals(strAliasId))continue;
				
				if(!alias.bBlocked){
					addToExecConsoleCommandQueue(alias.strCmdLine
						+cc.commentToAppend("alias="+alias.strAliasId), true);
					return true;
				}else{
					dumpWarnEntry(alias.toString());
				}
			}
		}
			
		return bLastAliasCreatedSuccessfuly;
	}
	
	protected boolean executePreparedCommand(){
		if(cc.RESTRICTED_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared))return true;
		
		/**
		 * means the command didnt have any problem, didnt fail, requiring a warning message
		 */
		boolean bCommandWorkedProperly = false;
		
		if(checkCmdValidityBoolTogglers()){
			bCommandWorkedProperly=toggle(cc.btgReferenceMatched);
		}else
		if(checkCmdValidity("alias",getAliasHelp(),true)){
			bCommandWorkedProperly=cmdAlias();
		}else
		if(checkCmdValidity("clearCommandsHistory")){
			astrCmdHistory.clear();
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity("clearDumpArea")){
			vlstrDumpEntries.clear();
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity(cc.CMD_CLOSE_CONSOLE,"like the bound key to do it")){
			setEnabled(false);
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity(cc.CMD_CONSOLE_HEIGHT,"[fPercent] of the application window")){
			Float f = paramFloat(1);
			modifyConsoleHeight(f);
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity(cc.CMD_CONSOLE_SCROLL_BOTTOM,"")){
			scrollToBottomRequest();
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity(cc.CMD_CONSOLE_STYLE,"[strStyleName] changes the style of the console on the fly, empty for a list")){
			String strStyle = paramString(1);
			if(strStyle==null)strStyle="";
			bCommandWorkedProperly=cmdStyleApply(strStyle);
		}else
		if(checkCmdValidity(cc.CMD_DB,EDataBaseOperations.help())){
			bCommandWorkedProperly=cmdDb();
		}else
//		if(checkCmdValidity("dumpFind","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
//		if(checkCmdValidity("dumpFindNext","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
//		if(checkCmdValidity("dumpFindPrevious","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
		if(checkCmdValidity(cc.CMD_ECHO," simply echo something")){
			bCommandWorkedProperly=cmdEcho();
		}else
		if(checkCmdValidity(cc.CMD_ELSE,"conditinal block in case 'if' fails")){
			bCommandWorkedProperly=cmdElse();
		}else
		if(checkCmdValidity(cc.CMD_ELSE_IF,"conditional block in case 'if' fails")){
			bCommandWorkedProperly=cmdElseIf();
		}else
		if(checkCmdValidity("execBatchCmdsFromFile ","<strFileName>")){
			String strFile = paramString(1);
			if(strFile!=null){
				astrExecConsoleCmdsQueue.addAll(fileLoad(strFile));
				bCommandWorkedProperly=true;
			}
		}else
		if(checkCmdValidity("exit","the application")){
			cmdExit();
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity(cc.CMD_FIX_CURSOR ,"in case cursor is invisible")){
			if(efHK==null){
				dumpWarnEntry("requires command: "+cc.CMD_HK_TOGGLE);
			}else{
				dumpInfoEntry("requesting: "+cc.CMD_FIX_CURSOR);
				efHK.bFixInvisibleTextInputCursorHK=true;
			}
			bCommandWorkedProperly = true;
		}else
		if(checkCmdValidity(cc.CMD_FIX_LINE_WRAP ,"in case words are overlapping")){
			cmdLineWrapDisableDumpArea();
			bCommandWorkedProperly = true;
		}else
		if(checkCmdValidity("fixVisibleRowsAmount ","[iAmount] in case it is not showing as many rows as it should")){
			iVisibleRowsAdjustRequest = paramInt(1);
			if(iVisibleRowsAdjustRequest==null)iVisibleRowsAdjustRequest=0;
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity("fpsLimit","[iMaxFps]")){
			Integer iMaxFps = paramInt(1);
			if(iMaxFps!=null){
				fpslState.setMaxFps(iMaxFps);
				bCommandWorkedProperly=true;
			}
			dumpSubEntry("FpsLimit = "+fpslState.getFpsLimit());
		}else
		if(checkCmdValidity(cc.CMD_HELP,"[strFilter] show (filtered) available commands")){
			cmdShowHelp(paramString(1));
			/**
			 * ALWAYS return TRUE here, to avoid infinite loop when improving some failed command help info!
			 */
			bCommandWorkedProperly=true; 
		}else
		if(checkCmdValidity(cc.CMD_HISTORY,"[strFilter] of issued commands (the filter results in sorted uniques)")){
			bCommandWorkedProperly=cmdShowHistory();
		}else
		if(checkCmdValidity(cc.CMD_HK_TOGGLE ,"[bEnable] allow hacks to provide workarounds")){
			if(paramBooleanCheckForToggle(1)){
				Boolean bEnable = paramBoolean(1);
				if(efHK==null && (bEnable==null || bEnable)){
					efHK=new ExtraFunctionalitiesHK(this);
				}
				
				if(efHK!=null){
					efHK.bAllowHK = bEnable==null ? !efHK.bAllowHK : bEnable; //override
					if(efHK.bAllowHK){
						dumpWarnEntry("Hacks enabled!");
					}else{
						dumpWarnEntry("Hacks may not be completely disabled/cleaned!");
					}
				}
				
				bCommandWorkedProperly=true;
			}
		}else
		if(checkCmdValidity(cc.CMD_IF,"<[!]<true|false>> [cmd|alias] if cmd|alias is not present, this will be a multiline block")){
			bCommandWorkedProperly=cmdIf();
		}else
		if(checkCmdValidity(cc.CMD_IF_END,"ends conditional exec block")){
			bCommandWorkedProperly=cmdIfEnd();
		}else
		if(checkCmdValidity("initFileShow ","show contents of init file at dump area")){
			dumpInfoEntry("Init file data: ");
			for(String str : fileLoad(flInit)){
				dumpSubEntry(str);
			}
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity(cc.CMD_LINE_WRAP_AT,"[iMaxChars] -1 = will trunc big lines, 0 = wrap will be automatic")){
			iConsoleMaxWidthInCharsForLineWrap = paramInt(1);
			if(iConsoleMaxWidthInCharsForLineWrap!=null){
				if(iConsoleMaxWidthInCharsForLineWrap==-1){
					iConsoleMaxWidthInCharsForLineWrap=null;
				}
			}
			updateWrapAt();
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity("quit","the application")){
			cmdExit();
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity("showBinds","")){
			dumpInfoEntry("Key bindings: ");
			dumpSubEntry("Ctrl+B - marks dump area begin selection marker for copy");
			dumpSubEntry("Ctrl+Del - clear input field");
			dumpSubEntry("TAB - autocomplete (starting with)");
			dumpSubEntry("Ctrl+TAB - autocomplete (contains)");
			dumpSubEntry("Ctrl+/ - toggle input field comment");
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity("showSetup","show restricted variables")){
			for(String str:fileLoad(flSetup)){
				dumpSubEntry(str);
			}
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity("statsFieldToggle","[bEnable] toggle simple stats field visibility")){
			bCommandWorkedProperly=statsFieldToggle();
		}else
		if(checkCmdValidity("statsShowAll","show all console stats")){
			dumpAllStats();
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity("test","[...] temporary developer tests")){
			cmdTest();
			if(efHK!=null)efHK.test();
			bCommandWorkedProperly=true;
		}else
		if(checkCmdValidity("varAdd","<varId> <[-]value>")){
			bCommandWorkedProperly=cmdVarAdd(paramString(1),paramString(2),false);
		}else
		if(checkCmdValidity(cc.CMD_VAR_SET,"[<varId> <value>] | [-varId] | ["+cc.getFilterToken()+"filter] - can be a number or a string, retrieve it's value with: ${varId}")){
			bCommandWorkedProperly=cmdVarSet();
		}else
		if(checkCmdValidity("varSetCmp","<varIdBool> <value> <cmp> <value>")){
			bCommandWorkedProperly=cmdVarSetCmp();
		}else
		if(checkCmdValidity(TOKEN_CMD_NOT_WORKING_YET+"zDisabledCommand"," just to show how to use it")){
			// keep this as reference
		}else{
//			if(strCmdLinePrepared!=null){
//				if(SPECIAL_CMD_MULTI_COMMAND_LINE_OK.equals(strCmdLinePrepared)){
//					bOk=true;
//				}
//			}
		}
		
		return bCommandWorkedProperly;
	}
	
	protected boolean isStartupCommandsQueueDone(){
		return bStartupCmdQueueDone;
	}
	
	public static enum EDataBaseOperations{
		/** saving also will shrink the DB */
		save,
		
		load,
		
		show,
		
		/** A backup is made of the existing file. */
		backup,
		
		;
		public static String help(){
			String str = null;
			for(EDataBaseOperations e:values()){
				if(str!=null){
					str+="|";
				}else{
					str="";
				}
				str+=e.toString();
			}
			return "["+str+"] aliases and variables, plain text file";
		}

		public static ArrayList<String> getValuesAsArrayList() {
			ArrayList<String> astr = new ArrayList<String>();
			for(EDataBaseOperations e:values()){
				astr.add(e.toString());
			}
			return astr;
		}
	}
	
	protected boolean cmdDb() {
		String strOpt = paramString(1);
		if(strOpt!=null){
			EDataBaseOperations edb = null;
			try {edb = EDataBaseOperations.valueOf(strOpt);}catch(IllegalArgumentException e){}
			return cmdDatabase(edb);
		}
		return false;
	}
	
//	protected boolean fileCopy(File flFrom, File flTo) {
//    FileChannel source = null;
//    FileChannel destination = null;
//    
//		try{
//	    if(!flTo.exists())flTo.createNewFile();
//	
//			source = new FileInputStream(flFrom).getChannel();
//			destination = new FileOutputStream(flTo).getChannel();
//			destination.transferFrom(source, 0, source.size());
//		} catch (IOException e) {
//			dumpExceptionEntry(e);
//			e.printStackTrace();
//			return false;
//		}finally{
//			try{if(source != null)
//				source.close();}catch(IOException e){e.printStackTrace();}
//			try{if(destination != null)
//				destination.close();}catch(IOException e){e.printStackTrace();}
//		}
//		
//		return true;
//	}
	
	protected boolean databaseSave(){
		ArrayList<String> astr = new ArrayList<>();
		
		ArrayList<String> astrVarList = getVariablesIdentifiers(false);
		for(String strVarId:astrVarList){
			astr.add(varReportPrepare(strVarId));
		}
		
		for(Alias alias:aAliasList){
			astr.add(alias.toString());
		}
		
		flDB.delete();
		fileAppendLine(flDB, cc.getCommentPrefix()+" DO NOT MODIFY! auto generated. Set overrides at user init file!");
		fileAppendList(flDB, astr);
		
		dumpInfoEntry("Database saved: "
			+astrVarList.size()+" vars, "
			+aAliasList.size()+" aliases, "
			+flDB.length()+" bytes,");
		
		setupRecreateFile();
		
		return true;
	}
	
	protected boolean cmdDatabase(EDataBaseOperations edbo){
		if(edbo==null)return false;
		
		switch(edbo){
			case load:
				/**
				 * prepend on the queue is important mainly at the initialization
				 */
				dumpInfoEntry("Loading Console Database:");
				addToExecConsoleCommandQueue(fileLoad(flDB),true,false);
				return true;
			case backup:
				return databaseBackup();
			case save:
				if(cc.btgDbAutoBkp.b()){
					if(isDatabaseChanged()){
						databaseBackup();
					}
				}
				
				return databaseSave();
			case show:
				for(String str:fileLoad(flDB)){
					dumpSubEntry(str);
				}
				return true;
		}
		
		return false;
	}
	
	protected boolean hasChanged(ERestrictedVars rv){
		String strValue = varGetValueString(""+cc.RESTRICTED_TOKEN+rv);
		switch(rv){
			case UserAliasListHashcode:
				return !(""+aAliasList.hashCode()).equals(strValue);
			case UserVariableListHashcode:
				return !(""+tmUserVariables.hashCode()).equals(strValue);
		}
		
		return false;
	}
	
	protected boolean isDatabaseChanged(){
		if(hasChanged(ERestrictedVars.UserAliasListHashcode))return true;
		if(hasChanged(ERestrictedVars.UserVariableListHashcode))return true;
		
		return false;
	}
	
	private boolean databaseBackup() {
		try {
			File fl = new File(fileNamePrepareCfg(strFileDatabase,true));
			Files.copy(flDB, fl);
			dumpSubEntry("Backup made: "+fl.getAbsolutePath()+"; "+fl.length()+" bytes");
		} catch (IOException ex) {
			dumpExceptionEntry(ex);
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	protected String getAliasHelp() {
		return "[<identifier> <commands>] | [<+|->identifier] | ["+cc.getFilterToken()+"filter]\n"
			+"\t\tWithout params, will list all aliases\n"
			+"\t\t"+cc.getFilterToken()+"filter - will filter (contains) the alias list\n"
			+"\t\t-identifier - will block that alias execution\n"
			+"\t\t+identifier - will un-block that alias execution\n"
			+"\t\tObs.: to execute an alias, "
				+"prefix the identifier with '"+cc.getAliasPrefix()+"', "
				+"ex.: "+cc.getCommandPrefix()+cc.getAliasPrefix() +"tst123";
	}
	
	protected boolean cmdAlias() {
		boolean bOk=false;
		String strAliasId = paramString(1);
		if(strAliasId!=null && strAliasId.startsWith(""+cc.getAliasAllowedToken())){
			bOk=aliasBlock(strAliasId.substring(1),false);
		}else
		if(strAliasId!=null && strAliasId.startsWith(""+cc.getAliasBlockedToken())){
			bOk=aliasBlock(strAliasId.substring(1),true);
		}else{
			String strFilter=null;
			if(strAliasId!=null && strAliasId.startsWith(""+cc.getFilterToken())){
				if(strAliasId.length()>1)strFilter = strAliasId.substring(1);
				strAliasId=null;
			}
			
			if(strAliasId==null){
				/**
				 * will list all aliases (or filtered)
				 */
				for(Alias alias:aAliasList){
					if(strFilter!=null && !alias.toString().toLowerCase().contains(strFilter.toLowerCase()))continue;
					dumpSubEntry(alias.toString());
				}
				dumpSubEntry(cc.commentToAppend("AliasListHashCode="+aAliasList.hashCode()));
				bOk=true;
			}else{
				bOk=bLastAliasCreatedSuccessfuly;
			}
		}
		
		return bOk;
	}
	protected boolean cmdEcho() {
		String strToEcho="";
		String strPart="";
		int iParam=1;
		while(strPart!=null){
			strToEcho+=strPart;
			strToEcho+=" ";
			strPart = paramString(iParam++);
		}
		strToEcho=strToEcho.trim();
		
		dumpEntry(strToEcho);
		
		return true;
	}
	
	protected boolean cmdShowHistory() {
		String strFilter = paramString(1);
		ArrayList<String> astrToDump = new ArrayList<String>();
		if(strFilter!=null){
			for(String str:astrCmdHistory){
				if(!str.toLowerCase().contains(strFilter.toLowerCase()))continue;
				str=str.trim(); // to prevent fail of unique check by spaces presence
				if(!astrToDump.contains(str))astrToDump.add(str);
				Collections.sort(astrToDump);
			}
		}else{
			astrToDump.addAll(astrCmdHistory);
		}
		
		for(String str:astrToDump){
//			dumpSubEntry(str);
			dumpEntry(false, true, false, str);
		}
		
		return true;
	}
	
	protected boolean statsFieldToggle() {
		if(paramBooleanCheckForToggle(1)){
			Boolean bEnable = paramBoolean(1);
			
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
	 * When creating variables, this method can only create custom user ones.
	 * @return
	 */
	protected boolean cmdVarSet() {
		boolean bOk=false;
		String strVarIdOrFilter = paramString(1);
		String strValue = paramString(2);
		if(strVarIdOrFilter==null || strVarIdOrFilter.startsWith(""+cc.getFilterToken())){
			/**
			 * LIST all, user and restricted
			 */
			dumpInfoEntry("Variables list:");
			if(strVarIdOrFilter!=null)strVarIdOrFilter=strVarIdOrFilter.substring(1);
			for(String strVarId : getVariablesIdentifiers(true)){
				if(strVarIdOrFilter!=null && !strVarId.toLowerCase().equals(strVarIdOrFilter.toLowerCase())){
					continue;
				}
				
				varReport(strVarId);
			}
			dumpSubEntry(cc.getCommentPrefix()+"UserVarListHashCode="+tmUserVariables.hashCode());
			bOk=true;
		}else
		if(strVarIdOrFilter!=null && strVarIdOrFilter.trim().startsWith(""+cc.getVarDeleteToken())){
			/**
			 * DELETE/UNSET only user variables
			 */
			bOk=tmUserVariables.remove(strVarIdOrFilter.trim().substring(1))!=null;
			if(bOk){
				dumpInfoEntry("Var '"+strVarIdOrFilter+"' deleted.");
			}else{
				dumpWarnEntry("Var '"+strVarIdOrFilter+"' not found.");
			}
		}else{
			/**
			 * SET user or restricted variable
			 */
			if(isRestrictedAndDoesNotExist(strVarIdOrFilter))return false;
			bOk=varSet(strVarIdOrFilter,strValue,true);
		}
		
		return bOk;
	}
	
	protected boolean isRestrictedAndDoesNotExist(String strVar){
		if(isRestricted(strVar)){
			// user can only set existing restricted vars
			if(!getVarHT(strVar).containsKey(strVar)){
				dumpWarnEntry("Restricted var does not exist: "+strVar);
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean cmdVarSetCmp() {
		String strVarId = paramString(1);
		if(isRestrictedAndDoesNotExist(strVarId))return false;
		
		String strValueLeft = paramString(2);
		String strCmp = paramString(3);
		String strValueRight = paramString(4);
		
		if(strCmp.equals("==")){
			return varSet(strVarId, ""+strValueLeft.equals(strValueRight), true);
		}else
		if(strCmp.equals("!=")){
			return varSet(strVarId, ""+(!strValueLeft.equals(strValueRight)), true);
		}else
		if(strCmp.equals("||")){
			return varSet(strVarId, ""+
				(parseBoolean(strValueLeft) || parseBoolean(strValueRight)), true);
		}else
		if(strCmp.equals("&&")){
			return varSet(strVarId, ""+
				(parseBoolean(strValueLeft) && parseBoolean(strValueRight)), true);
		}else
		if(strCmp.equals(">")){
			return varSet(strVarId, ""+
				(Double.parseDouble(strValueLeft) > Double.parseDouble(strValueRight)), true);
		}else
		if(strCmp.equals(">=")){
			return varSet(strVarId, ""+
				(Double.parseDouble(strValueLeft) >= Double.parseDouble(strValueRight)), true);
		}else
		if(strCmp.equals("<")){
			return varSet(strVarId, ""+
				(Double.parseDouble(strValueLeft) < Double.parseDouble(strValueRight)), true);
		}else
		if(strCmp.equals("<=")){
			return varSet(strVarId, ""+
					(Double.parseDouble(strValueLeft) <= Double.parseDouble(strValueRight)), true);
		}else{
			dumpWarnEntry("Invalid comparator: "+strCmp);
		}
		
		return false;
	}
	
	protected boolean cmdIf() {
		return cmdIf(false);
	}
	protected boolean cmdIf(boolean bSkipNesting) {
		bIfConditionIsValid=false;
		
		String strCondition = paramString(1);
		
		boolean bNegate = false;
		if(strCondition.startsWith("!")){
			strCondition=strCondition.substring(1);
			bNegate=true;
		}
		
		Boolean bCondition = null;
		try{bCondition = parseBoolean(strCondition);}catch(NumberFormatException e){};//accepted exception
		
		if(bNegate)bCondition=!bCondition;
		
		if(bCondition==null){
			dumpWarnEntry("Invalid condition: "+strCondition);
			return false;
		}
		
		String strCmds = paramStringConcatenateAllFrom(2);
		if(strCmds==null)strCmds="";
		strCmds.trim();
		if(strCmds.isEmpty() || strCmds.startsWith(cc.getCommentPrefixStr())){
			if(bSkipNesting){
				aIfConditionNestedList.set(aIfConditionNestedList.size()-1, bCondition);
			}else{
				aIfConditionNestedList.add(bCondition);
			}
			
			bIfConditionExecCommands=bCondition;
		}else{
			if(!bSkipNesting){
				if(bCondition){
					addToExecConsoleCommandQueue(strCmds,true);
				}
			}
		}
		
		return true;
	}
	
	protected boolean cmdElse(){
		bIfConditionExecCommands=!aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		bIfEndIsRequired = true;
		
		return true;
	}
	
	protected boolean cmdElseIf(){
		if(bIfEndIsRequired){
			dumpExceptionEntry(new NullPointerException("command "+cc.CMD_ELSE_IF.toString()
				+" is missplaced, ignoring"));
			bIfConditionExecCommands=false; //will also skip this block commands
			return false;
		}
		
		boolean bConditionSuccessAlready = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		
		if(bConditionSuccessAlready){
			/**
			 * if one of the conditions was successful, will skip all the remaining ones
			 */
			bIfConditionExecCommands=false;
		}else{
			return cmdIf(true);
		}
		
		return true;
	}
	
	protected boolean cmdIfEnd(){
		if(aIfConditionNestedList.size()>0){
			aIfConditionNestedList.remove(aIfConditionNestedList.size()-1);
			
			if(aIfConditionNestedList.size()==0){
				bIfConditionExecCommands=null;
				bIfEndIsRequired = false;
			}else{
				bIfConditionExecCommands = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
			}
		}else{
			dumpExceptionEntry(new NullPointerException("pointless condition ending..."));
			return false;
		}
		
		return true;
	}
	
	protected Boolean parseBoolean(String strValue){
		if(strValue.equalsIgnoreCase("true"))return new Boolean(true);
		if(strValue.equalsIgnoreCase("1"))return new Boolean(true);
		if(strValue.equalsIgnoreCase("false"))return new Boolean(false);
		if(strValue.equalsIgnoreCase("0"))return new Boolean(false);
		throw new NumberFormatException("invalid boolean value: "+strValue);
	}
	
	/**
	 * In case variable exists will be this method.
	 * @param strVarId
	 * @param strValueAdd
	 * @param bOverwrite
	 * @return
	 */
	protected boolean cmdVarAdd(String strVarId, String strValueAdd, boolean bOverwrite){
		if(isRestrictedAndDoesNotExist(strVarId))return false;
		
		Object objValueNew = null;
		Object objValueCurrent = getVarHT(strVarId).get(strVarId);
		
		if(objValueCurrent==null){
			dumpExceptionEntry(new NullPointerException("value is null for var "+strVarId));
			return false;
		}
			
//		if(objValueCurrent!=null){
			if(Boolean.class.isAssignableFrom(objValueCurrent.getClass())){
				// boolean is always overwrite
				objValueNew = parseBoolean(strValueAdd);
			}else
			if(Long.class.isAssignableFrom(objValueCurrent.getClass())){
				Long lValueCurrent = (Long)objValueCurrent;
				Long lValueAdd=null;
				try{lValueAdd = Long.parseLong(strValueAdd);}catch(NumberFormatException e){}// accepted exception!
				if(lValueAdd!=null){
					if(bOverwrite)lValueCurrent=0L;
					lValueCurrent+=lValueAdd;
					objValueNew = lValueCurrent;
				}else{
					dumpWarnEntry("Add value should be: "+Long.class.getSimpleName());
				}
			}else
			if(Double.class.isAssignableFrom(objValueCurrent.getClass())){
				Double dValueCurrent = (Double)objValueCurrent;
				Double dValueAdd=null;
				try{dValueAdd = Double.parseDouble(strValueAdd);}catch(NumberFormatException e){}// accepted exception!
				if(dValueAdd!=null){
					if(bOverwrite)dValueCurrent=0.0;
					dValueCurrent+=dValueAdd;
					objValueNew = dValueCurrent;
				}else{
					dumpWarnEntry("Add value should be: "+Double.class.getSimpleName());
				}
			}else{
				if(bOverwrite)objValueCurrent="";
				objValueNew = ""+objValueCurrent+strValueAdd;
			}
//		}else{
//			return varSet(strVarId, strValueAdd, true);
//		}
		
//		if(objValueNew==null)return false;
		
		varApply(strVarId,objValueNew,true);
		return true;
	}
	
	protected boolean isRestricted(String strId){
		return strId.startsWith(""+cc.RESTRICTED_TOKEN);
	}
	
	protected TreeMap<String, Object> getVarHT(String strVarId){
		if(isRestricted(strVarId)){
			return tmRestrictedVariables;
		}else{
			return tmUserVariables;
		}
	}
	
	protected File getVarFile(String strVarId){
		if(isRestricted(strVarId)){
			return flSetup;
		}else{
			return flDB;
		}
	}
	
	protected boolean varApply(String strVarId, Object objValue, boolean bSave){
		getVarHT(strVarId).put(strVarId,objValue);
		if(bSave)fileAppendLine(getVarFile(strVarId),varReportPrepare(strVarId));
		
		if(isRestricted(strVarId) && cc.btgShowDeveloperInfo.b()){
			varReport(strVarId);
		}
		
		return true;
	}
	
	protected String varReportPrepare(String strVarId) {
		Object objValue = getVarHT(strVarId).get(strVarId);
		return cc.getCommandPrefix()
			+cc.CMD_VAR_SET.toString()
			+" "
			+strVarId
			+" "
			+"\""+objValue+"\""
			+" "
			+"#"+objValue.getClass().getSimpleName();
	}
	
	protected void varReport(String strVarId) {
		Object objValue=getVarHT(strVarId).get(strVarId);
		if(objValue!=null){
			dumpSubEntry(varReportPrepare(strVarId));
		}else{
			dumpSubEntry(strVarId+" is not set...");
		}
	}
	
	/**
	 * This is able to create restricted variables.
	 * 
	 * @param strVarId
	 * @param strValue
	 * @return
	 */
	protected boolean varSet(String strVarId, String strValue, boolean bSave) {
		if(getAlias(strVarId)!=null){
			dumpErrorEntry("Variable identifier '"+strVarId+"' conflicts with existing alias!");
			return false;
		}
		
		if(strValue==null)return false; //strValue=""; //just creates the var
		
		if(hasVar(strVarId)){
			return cmdVarAdd(strVarId, strValue, true);
		}
		
		boolean bOk=false;
		
		/**
		 * Priority:
		 * Double would parse a Long.
		 * Boolean would be accepted by String that accepts everything. 
		 */
		if(!bOk)try{bOk=varApply(strVarId, Long  .parseLong  (strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)try{bOk=varApply(strVarId, Double.parseDouble(strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)try{bOk=varApply(strVarId, parseBoolean      (strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)bOk=varApply(strVarId,strValue,bSave);
		
		return bOk;
	}
	
	/**
	 * 
	 * @param strVarId
	 * @return "null" if not set
	 */
	protected String varGetValueString(String strVarId){
		Object obj = getVarHT(strVarId).get(strVarId);
		if(obj==null)return "null";
		return ""+obj;
	}
//	protected Double varGetValueDouble(String strVarId){
//		Object obj = ahkVariables.get(strVarId);
//		if(obj==null)return null;
//		if(obj instanceof Double)return (Double)obj;
//		dumpExceptionEntry(new typeex);
//		return null;
//	}
	
	protected boolean aliasBlock(String strAliasId, boolean bBlock) {
		for(Alias alias : aAliasList){
			if(alias.strAliasId.toLowerCase().equals(strAliasId.toLowerCase())){
				dumpInfoEntry((bBlock?"Blocking":"Unblocking")+" alias "+alias.strAliasId);
				alias.bBlocked=bBlock;
				return true;
			}
		}
		return false;
	}
	protected void cmdShowHelp(String strFilter) {
		if(strFilter==null){
			dumpInfoEntry("Available Commands:");
		}else{
			dumpInfoEntry("Help for '"+strFilter+"':");
		}
		
		Collections.sort(astrCmdWithCmtValidList,String.CASE_INSENSITIVE_ORDER);
		for(String str:astrCmdWithCmtValidList){
			if(strFilter!=null && !str.toLowerCase().contains(strFilter.toLowerCase()))continue;
			dumpSubEntry(cc.getCommandPrefix()+str);
		}
	}
	
	/**
	 * Command format: "commandIdentifier any comments as you wish"
	 * @param strFullCmdLineOriginal if null will populate the array of valid commands
	 * @return false if command execution failed
	 */
	protected boolean executeCommand(final String strFullCmdLineOriginal){
		strCmdLineOriginal = strFullCmdLineOriginal;
		
		boolean bOk = false;
		try{
			if(!bOk)bOk=cmdRawLineCheckEndOfStartupCmdQueue();
			
			if(!bOk)bOk=cmdRawLineCheckAlias();
			
//			if(!bOk)bOk=cmdRawLineCheckIfElse();
			
//			if(!bOk){
//				if(SPECIAL_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared)){
//					bOk=true;
//				}
//			}
			
			/**
			 * we will have a prepared line after below
			 */
			if(!bOk){
				strCmdLinePrepared = prepareCmdAndParams(strCmdLineOriginal);
				
				if(bIfConditionExecCommands!=null && !bIfConditionExecCommands){
					/**
					 * the if condition resulted in false, therefore commands must be skipped.
					 */
					bOk = true;
					
					/**
					 * The commands are being skipped.
					 * These are capable of ending the skipping.
					 */
					if(cc.CMD_ELSE_IF.equals(paramString(0))){
						bOk = cmdElseIf();
					}else
					if(cc.CMD_ELSE.equals(paramString(0))){
						bOk = cmdElse();
					}else
					if(cc.CMD_IF_END.equals(paramString(0))){
						bOk = cmdIfEnd();
					}else{
						dumpInfoEntry("ConditionalSkip: "+strCmdLinePrepared);
					}
				}else{
					bOk = executePreparedCommand();
				}
				
//				boolean bExec = false;
//				if(bIfConditionExecCommands!=null){
//					if(bIfConditionExecCommands){
//						bExec = true;
//					}else{
//						/**
//						 * the if condition resulted in false, therefore commands must be skipped.
//						 */
//						bOk = true;
//						
//						if(cc.CMD_IF_END.equals(paramString(0))){
//							/**
//							 * the commands may be being skipped, so this is required here.
//							 */
//							bOk = cmdIfEnd();
//						}else{
//							dumpInfoEntry("IfCondition.Skip: "+strCmdLinePrepared);
//						}
//					}
//				}else{
//					bExec = true;
//				}
//				
//				if(bExec) bOk = executePreparedCommand();
			}
		}catch(NumberFormatException e){
			// keep this one as "warning", as user may simply fix the typed value
			dumpWarnEntry("NumberFormatException: "+e.getMessage());
			e.printStackTrace();
			bOk=false;
		}
		
		return bOk;
	}
	
	protected boolean cmdRawLineCheckEndOfStartupCmdQueue() {
		if(cc.RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE.equals(strCmdLineOriginal)){
			bStartupCmdQueueDone=true;
			return true;
		}
		return false;
	}
//	protected boolean cmdRawLineCheckIfElse() {
//		return false;
//	}
	/**
	 * 
	 * @param fNewHeightPercent null to use the default
	 */
	protected void modifyConsoleHeight(Float fNewHeightPercent) {
		Vector3f v3fNew = ctnrConsole.getPreferredSize(); //getSize() does not work well..
		if(!v3fNew.equals(v3fConsoleSize)){
			dumpDevWarnEntry("sizes should be equal: "+v3fNew+v3fConsoleSize);
		}
		
		if(fNewHeightPercent==null)fNewHeightPercent=fConsoleHeightPercDefault;
		
		if(fNewHeightPercent>0.95f)fNewHeightPercent=0.95f;
		
		v3fNew.y = fNewHeightPercent * sapp.getContext().getSettings().getHeight();
		
		float fMin = fInputHeight +fStatsHeight +fLstbxEntryHeight*3; //will show only 2 rows, the 3 value is a safety margin
		
		if(v3fNew.y<fMin)v3fNew.y=fMin;
		
		ctnrConsole.setPreferredSize(v3fNew); //setSize() does not work well..
		v3fConsoleSize.set(v3fNew);
		
		fConsoleHeightPerc = fNewHeightPercent;
		
		iVisibleRowsAdjustRequest = 0; //dynamic
	}
	protected void updateEngineStats() {
		stateStats.setDisplayStatView(cc.btgEngineStatsView.get());
		stateStats.setDisplayFps(cc.btgEngineStatsFps.get());
	}
	protected void styleHelp(){
		dumpInfoEntry("Available styles:");
		for(String str:astrStyleList){
			dumpSubEntry(str);
		}
	}
	protected boolean styleCheck(String strStyle) {
		return astrStyleList.contains(strStyle);
	}
	
	protected void updateWrapAt(){
		if(iConsoleMaxWidthInCharsForLineWrap!=null){
			fWidestCharForCurrentStyleFont = fontWidth("W"); //W seems to be the widest in most/all chars sets
			
			if(iConsoleMaxWidthInCharsForLineWrap>0){
				iConsoleMaxWidthInCharsForLineWrap = (int) //like trunc
					((widthForDumpEntryField()/fWidestCharForCurrentStyleFont)-iSkipCharsSafetyGUESSED);
			}
		}
	}
	
	protected boolean cmdStyleApply(String strStyleNew) {
		boolean bOk = styleCheck(strStyleNew);
		if(bOk){
			strStyle=strStyleNew;
			
			updateWrapAt();
			
			sapp.enqueue(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					boolean bWasEnabled=isEnabled();
					setEnabled(false);
					cleanup();
					
					if(bWasEnabled){
						setEnabled(true);
					}
					modifyConsoleHeight(fConsoleHeightPerc);
					scrollToBottomRequest();
//					addToExecConsoleCommandQueue(cc.CMD_MODIFY_CONSOLE_HEIGHT+" "+fConsoleHeightPerc);
//					addToExecConsoleCommandQueue(cc.CMD_SCROLL_BOTTOM);
					return null;
				}
			});
		}else{
			dumpWarnEntry("invalid style: "+strStyleNew);
			styleHelp();
		}
		
		return bOk;
	}
	
	protected float widthForListbox(){
		return lstbx.getSize().x;
	}
	
	protected float widthForDumpEntryField(){
		return widthForListbox() -lstbx.getSlider().getSize().x -fSafetyMarginGUESSED;
	}
	
	protected void cmdExit(){
		sapp.stop();
		System.exit(0);
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
	
//	protected String fmtFloat(Float f,int iDecimalPlaces){
//		return fmtFloat(f==null?null:f.doubleValue(),iDecimalPlaces);
//	}
	protected String fmtFloat(double d){
		return fmtFloat(d,-1);
	}
	protected String fmtFloat(Float f, int iDecimalPlaces){
		return fmtFloat(f==null?null:f.doubleValue(),iDecimalPlaces);
	}
	protected String fmtFloat(Double d,int iDecimalPlaces){
		if(iDecimalPlaces==-1)iDecimalPlaces=2;
		return d==null?"null":String.format("%."+iDecimalPlaces+"f", d);
	}
	
	protected void dumpAllStats(){
		dumpEntry(true, cc.btgShowDeveloperInfo.get(), false,	
			getSimpleTime()+strDevInfoEntryPrefix+"Console stats (Dev):"+"\n"
				+"Console Height = "+fmtFloat(ctnrConsole.getSize().y)+"\n"
				+"Visible Rows = "+lstbx.getGridPanel().getVisibleRows()+"\n"
				+"Line Wrap At = "+iConsoleMaxWidthInCharsForLineWrap+"\n"
				+"ListBox Height = "+fmtFloat(lstbx.getSize().y)+"\n"
				+"ListBox Entry Height = "+fmtFloat(fLstbxEntryHeight)+"\n"
				
				+"Stats Text Field Height = "+fmtFloat(fStatsHeight)+"\n"
				+"Stats Container Height = "+fmtFloat(ctnrStatsAndControls.getSize().y)+"\n"
				
				+"Input Field Height = "+fmtFloat(fInputHeight)+"\n"
				+"Input Field Final Height = "+fmtFloat(tfInput.getSize().y)+"\n"
				
				+"Slider Value = "+fmtFloat(getScrollDumpAreaFlindex())+"\n"
				
				+"Slider Scroll request max retry attempts = "+iScrollRetryAttemptsMaxDBG);
		
		dumpSubEntry("Database User Variables Count = "+getVariablesIdentifiers(false).size());
		dumpSubEntry("Database User Aliases Count = "+aAliasList.size());
		
//		dumpSubEntry("Previous Second FPS  = "+lPreviousSecondFPS);
		
		for(BoolToggler bh : BoolToggler.getBoolTogglerListCopy()){
			dumpSubEntry(bh.getCmdId()+" = "+bh.get());
		}
		
		dumpSubEntry("User Dump File = "+flLastDump.getAbsolutePath());
		dumpSubEntry("User Commands History File = "+flCmdHist.getAbsolutePath());
		dumpSubEntry("User Database File = "+flDB.getAbsolutePath());
		dumpSubEntry("User Config File = "+flInit.getAbsolutePath());
	}
	
	/**
	 * this is heavy...
	 * @param bEscapeNL good to have single line result
	 * @return
	 */
	protected String retrieveClipboardString(boolean bEscapeNL){
		try{
			Transferable tfbl = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			String str = (String) tfbl.getTransferData(DataFlavor.stringFlavor);
			if(bEscapeNL){
				str=str.replace("\n", "\\n");
			}
			return str;
		} catch (UnsupportedFlavorException | IOException e) {
			dumpExceptionEntry(e);
		}
		
		return "";
	}
	protected String retrieveClipboardString(){
		return retrieveClipboardString(false);
	}
	protected void putStringToClipboard(String str){
		StringSelection ss = new StringSelection(str);
		Toolkit.getDefaultToolkit().getSystemClipboard()
			.setContents(ss, ss);
	}
	
	protected String prepareStatsFieldText(){
		if(!tdStatsRefresh.isReady(true))return strStatsLast;
		
		// this value is the top entry index
		int iMaxSliderIndex=vlstrDumpEntries.size()-lstbx.getGridPanel().getVisibleRows();
		
		strStatsLast = ""
				// user important
				+"CpFrom"+iCopyFrom
					+"to"+iCopyTo //getDumpAreaSelectedIndex()
					+","
				
				+"Hst"+iCmdHistoryCurrentIndex+"/"+(astrCmdHistory.size()-1)
					+","
				
				+"If"+aIfConditionNestedList.size()
					+","
					
				/**
				 * KEEP HERE AS REFERENCE!
				 * IMPORTANT, DO NOT USE
				 * clipboard reading is too heavy...
				+"Cpbd='"+retrieveClipboardString()+"', "
				 */
					
				// less important (mainly for debug)
				+"Slider"
					+fmtFloat(getScrollDumpAreaFlindex(),0)+"/"+iMaxSliderIndex+"+"+lstbx.getGridPanel().getVisibleRows()
					+"("+fmtFloat(100.0f -lstbx.getSlider().getModel().getPercent()*100f,0)+"%)"
					+","
				
				+"Tpf"+(fpslState.isEnabled() ? (int)(fTPF*1000.0f) : fmtFloat(fTPF,6)+"s")
					+(fpslState.isEnabled()?
						"="+fpslState.getFrameDelayByCpuUsageMilis()+"+"+fpslState.getThreadSleepTimeMilis()+"ms"
						:"")
					+","
					
		;
		
		return strStatsLast;
	}
	
	protected void updateStats(){
		lblStats.setText(prepareStatsFieldText());
	}
	
	public boolean isInitiallyClosed() {
		return bInitiallyClosed;
	}
	public void setCfgInitiallyClosed(boolean bInitiallyClosed) {
		this.bInitiallyClosed = bInitiallyClosed;
	}
	
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
	
	enum ERestrictedVars{
		UserVariableListHashcode,
		UserAliasListHashcode,
	}
	
	protected void setupRecreateFile(){
		flSetup.delete();
		
		fileAppendLine(flSetup, cc.getCommentPrefix()+" DO NOT EDIT!");
		fileAppendLine(flSetup, cc.getCommentPrefix()
			+" This file will be overwritten by the application!");
		fileAppendLine(flSetup, cc.getCommentPrefix()
			+" To set overrides use the user init config file.");
		
		setupVars(true);
	}
	protected void setupVars(boolean bSave){
		varSet(""+cc.RESTRICTED_TOKEN+ERestrictedVars.UserVariableListHashcode,
			""+tmUserVariables.hashCode(),
			bSave);
		
		varSet(""+cc.RESTRICTED_TOKEN+ERestrictedVars.UserAliasListHashcode,
			""+aAliasList.hashCode(),
			bSave);
	}
}

