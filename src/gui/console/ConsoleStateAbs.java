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
import java.util.AbstractList;
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
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.Styles;

/**
 * This class connects the console commands class with JMonkeyEngine.
 * 
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
	protected AbstractList<String> vlstrAutoCompleteHint;
	protected Node lstbxAutoCompleteHint;
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
	protected ArrayList<PreQueueSubList>	astrExecConsoleCmdsPreQueue = new ArrayList<PreQueueSubList>();
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
	protected TreeMap<String,ArrayList<String>> tmFunctions = 
			new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
	protected ArrayList<Alias> aAliasList = new ArrayList<Alias>();
	protected String	strCmdLineOriginal;
	protected boolean	bLastAliasCreatedSuccessfuly;
	protected float	fTPF;
	protected long	lNanoFrameTime;
	protected long	lNanoFpsLimiterTime;
//	protected Boolean	bIfConditionIsValid;
	protected ArrayList<DumpEntry> adeDumpEntryFastQueue = new ArrayList<DumpEntry>();
	protected Button	btnCut;
	protected File	flSetup;
//	protected ArrayList<ConditionalNested> aIfConditionNestedList = new ArrayList<ConditionalNested>();
//	protected Boolean	bIfConditionExecCommands;
	protected String	strPrepareFunctionBlockForId;
	protected boolean	bFuncCmdLineRunning;
	protected boolean	bFuncCmdLineSkipTilEnd;
	protected long lLastUniqueId = 0;

//	private boolean	bUsePreQueue = false; 
	
	protected static class PreQueueSubList{
		TimedDelay tdSleep = null;
		String strUId = ConsoleStateAbs.i().getNextUniqueId();
		ArrayList<String> astrCmdList = new ArrayList<String>();
		boolean	bPrepend = false;
		public String getPreparedCommentUId(){
			return ConsoleStateAbs.i().cc.commentToAppend("UId=\""+strUId+"\"");
		}
	}
	
	protected static class ConditionalNested{
		public ConditionalNested(boolean bCondition){
			this.bCondition=bCondition;
		}
		Boolean bCondition = null;
		Boolean	bIfEndIsRequired = false;
	}
	
	protected static ConsoleStateAbs instance;
	public static ConsoleStateAbs i(){
		return instance;
	}
	public ConsoleStateAbs(ConsoleCommands cc) {
		if(instance==null)instance=this;
		this.cc = cc==null?new ConsoleCommands():cc;
		this.cc.csaTmp=this;
	}
	public ConsoleStateAbs(int iToggleConsoleKey, ConsoleCommands cc) {
		this(cc);
		this.bEnabled=true; //just to let it be initialized at startup by state manager
		this.iToggleConsoleKey=iToggleConsoleKey;
	}
	public ConsoleStateAbs(int iToggleConsoleKey) {
		this(iToggleConsoleKey,null);
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
		cc.sapp = sapp;
		
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
			addExecConsoleCommandBlockToQueue(fileLoad(flSetup));
		}
		
		// init user cfg
		flInit = new File(fileNamePrepareCfg(strFileInitConsCmds,false));
		if(flInit.exists()){
			addExecConsoleCommandBlockToQueue(fileLoad(flInit));
		}else{
			fileAppendLine(flInit, cc.getCommentPrefix()+" User console commands here will be executed at startup.");
		}
		
		// init DB
		flDB = new File(fileNamePrepareCfg(strFileDatabase,false));
		
		// other inits
		addExecConsoleCommandToQueue(cc.CMD_FIX_LINE_WRAP);
		addExecConsoleCommandToQueue(cc.CMD_CONSOLE_SCROLL_BOTTOM);
		addExecConsoleCommandToQueue(cc.CMD_DB+" "+EDataBaseOperations.load);
		addExecConsoleCommandToQueue(
			cc.CMD_DB+" "+EDataBaseOperations.save+" "+cc.getCommentPrefix()+"to shrink it");
		/**
		 * KEEP AS LAST queued cmds below!!!
		 */
		// must be the last queued command after all init ones!
		addExecConsoleCommandToQueue(cc.btgShowExecQueuedInfo.getCmdIdAsCommand(true));
		// end of initialization
		addExecConsoleCommandToQueue(cc.RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE);
		// TODO temporary workaround, pre-queue cannot be enable from start yet...
		addExecConsoleCommandToQueue(cc.btgPreQueue.getCmdIdAsCommand(true));
		if(bInitiallyClosed){
			// after all, close the console
			addExecConsoleCommandToQueue(cc.CMD_CLOSE_CONSOLE);
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
	
	/**
	 * this can be used after a cleanup() too
	 */
	protected void initialize(){
		if(sapp==null)throw new NullPointerException("base initialization required");
		
//		tdLetCpuRest.updateTime();
		tdStatsRefresh.updateTime();
		tdDumpQueuedEntry.updateTime();
		
//		createMonoSpaceFixedFontStyle();
		
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
	
	protected abstract void clearHintSelection();
	protected abstract Integer getHintIndex();
	protected abstract ConsoleStateAbs setHintIndex(Integer i);
	protected abstract ConsoleStateAbs setHintBoxSize(Vector3f v3fBoxSizeXY, Integer iVisibleLines);
	protected abstract void scrollHintToIndex(int i);
	protected abstract void lineWrapDisableForChildrenOf(Node gp);
	
	protected boolean navigateHint(int iAdd){
		if(!isHintActive())return false;
		
		if(
				getSelectedHint()!=null
				||
				(iCmdHistoryCurrentIndex+1)>=astrCmdHistory.size() // end of cmd history
		){
			int iMaxIndex = vlstrAutoCompleteHint.size()-1;
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
	
	protected String getSelectedHint(){
		if(!isHintActive())return null;
		Integer i = getHintIndex();
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
		
		updateExecCmdPreQueueDispatcher(); //before exec queue 
		updateExecConsoleCmdQueue(); // after pre queue
		
		updateInputFieldFillWithSelectedEntry();
		updateAutoCompleteHint();
		updateDumpQueueEntry();
		
		updateCurrentCmdHistoryEntryReset();
		
		GuiGlobals.getInstance().requestFocus(tfInput);
	}
	
	/**
	 * TODO use base 36 (max alphanum chars amount)
	 * @return
	 */
	protected String getNextUniqueId(){
		return ""+(++lLastUniqueId);
	}
	
	protected void updateToggles() {
		if(cc.btgEngineStatsView.checkChangedAndUpdate())updateEngineStats();
		if(cc.btgEngineStatsFps.checkChangedAndUpdate())updateEngineStats();
		if(cc.btgFpsLimit.checkChangedAndUpdate())fpslState.setEnabled(cc.btgFpsLimit.b());
		if(cc.btgConsoleCpuRest.checkChangedAndUpdate())tdLetCpuRest.setActive(cc.btgConsoleCpuRest.b());
//		if(cc.btgPreQueue.checkChangedAndUpdate())bUsePreQueue=cc.btgPreQueue.b();
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
			if(iVisibleItems>vlstrAutoCompleteHint.size())iVisibleItems=vlstrAutoCompleteHint.size();
			float fHintHeight = fEntryHeightGUESSED*iVisibleItems;
			if(fHintHeight>fAvailableHeight){
				dumpDevWarnEntry("fHintHeight="+fHintHeight+",fAvailableHeight="+fAvailableHeight);
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
	
	protected void closeHint(){
		vlstrAutoCompleteHint.clear();
		lstbxAutoCompleteHint.removeFromParent();
	}
	
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
		
//		String[] astr = strCmdFull.split("[^$"+strValidCmdCharsRegex+"]");
//		if(astr.length>iPart){
//			return astr[iPart];
//		}
		ArrayList<String> astr = convertToCmdParamsList(strCmdFull);
		if(iPart>=0 && astr.size()>iPart){
			return astr.get(iPart);
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
		tfInput.setText(fixStringToInputField(str));
	}
	
	protected String fixStringToInputField(String str){
		return str.replace("\n", "\\n").replace("\r", "");
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
	
	/**
	 * this is to fix the dump entry by disallowing automatic line wrapping
	 */
	protected void cmdLineWrapDisableDumpArea(){
		lineWrapDisableForChildrenOf(lstbx);
	}
	
	protected void updateVisibleRowsAmount(){
		if(fLstbxHeight != lstbx.getSize().y){
			iVisibleRowsAdjustRequest = 0; //dynamic
		}
		
		if(iVisibleRowsAdjustRequest==null)return;
		
		Integer iForceAmount = iVisibleRowsAdjustRequest;
		if(iForceAmount>0){
			iShowRows=iForceAmount;
//			lstbx.setVisibleItems(iShowRows);
//			lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		}else{
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
//				float fHeightAvailable = fLstbxHeight -fInputHeight;
//				if(ctnrConsole.hasChild(lblStats)){
//					fHeightAvailable-=fStatsHeight;
//				}
			iShowRows = (int) (fHeightAvailable / fLstbxEntryHeight);
		}
		
		lstbx.setVisibleItems(iShowRows);
		
		varSet(cc.CMD_FIX_VISIBLE_ROWS_AMOUNT, ""+iShowRows, true);
		
	//	lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		dumpInfoEntry("fLstbxEntryHeight="+fmtFloat(fLstbxEntryHeight)+", "+"iShowRows="+iShowRows);
		
		iVisibleRowsAdjustRequest=null;
		
		cmdLineWrapDisableDumpArea();
	}
	
	protected BitmapText retrieveBitmapTextFor(Node pnl){
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
	
	protected void addExecConsoleCommandBlockToQueue(ArrayList<String> astrCmdList){
		addExecConsoleCommandBlockToQueue(astrCmdList,false,true);
	}
	/**
	 * 
	 * @param astrCmdList
	 * @param bPrepend will append if false
	 */
	protected void addExecConsoleCommandBlockToQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex){
		if(cc.btgPreQueue.b()){
			/**
			 * TODO still unable to startup with pre-queue enabled...
			 */
			addExecConsoleCommandBlockToPreQueue(astrCmdList, bPrepend, bShowExecIndex);
		}else{
			ArrayList<String> astrCmdListCopy = new ArrayList<String>(astrCmdList);
			
			if(bShowExecIndex){
				for(int i=0;i<astrCmdListCopy.size();i++){
					astrCmdListCopy.set(i, astrCmdListCopy.get(i)+cc.commentToAppend("ExecIndex="+i));
				}
			}
			
			if(bPrepend){
				Collections.reverse(astrCmdListCopy);
			}
			for(String strCmd:astrCmdListCopy){
				addExecConsoleCommandToQueue(strCmd, bPrepend);
			}
		}
	}
	protected void addExecConsoleCommandBlockToPreQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex){
		PreQueueSubList pqe = new PreQueueSubList();
		pqe.bPrepend=bPrepend;
		pqe.astrCmdList = new ArrayList<String>(astrCmdList);
		for(int i=0;i<pqe.astrCmdList.size();i++){
			pqe.astrCmdList.set(i, pqe.astrCmdList.get(i)+pqe.getPreparedCommentUId());
		}
		if(bShowExecIndex){
			for(int i=0;i<pqe.astrCmdList.size();i++){
				pqe.astrCmdList.set(i, pqe.astrCmdList.get(i)+cc.commentToAppend("ExecIndex="+i)
				);
			}
		}
		
		astrExecConsoleCmdsPreQueue.add(pqe);
		dumpDevInfoEntry("AddedCommandBlock"+pqe.getPreparedCommentUId());
	}
	
	protected boolean doesCmdQueueStillHasUId(String strUId){
		for(String strCmd:astrExecConsoleCmdsQueue){
			if(strCmd.contains(strUId))return true;
		}
		return false;
	}
	
	protected void updateExecCmdPreQueueDispatcher(){
		for(PreQueueSubList pqe:astrExecConsoleCmdsPreQueue.toArray(new PreQueueSubList[0])){
			if(pqe.tdSleep!=null){
				if(pqe.tdSleep.isReady()){
					if(doesCmdQueueStillHasUId(pqe.getPreparedCommentUId())){
						/**
						 * will wait all commands of this same pre queue list
						 * to complete, before continuing, in case the delay was too short.
						 */
						continue;
					}else{
						pqe.tdSleep=null;
					}
				}else{
					continue;
				}
			}
			
			if(pqe.astrCmdList.size()==0){
				astrExecConsoleCmdsPreQueue.remove(pqe);
			}else{
				ArrayList<String> astrCmdListFast = new ArrayList<String>();
				
				while(pqe.astrCmdList.size()>0){
					String strCmd = pqe.astrCmdList.remove(0);
					String strCmdBase = extractCommandPart(strCmd, 0);
					if(strCmdBase==null)continue;
					
					if(cc.CMD_SLEEP.equals(strCmdBase)){
						String strParam1 = extractCommandPart(strCmd, 1);
						strParam1=applyVariablesValues(strParam1);
						Float fDelay = Float.parseFloat(strParam1);
						pqe.tdSleep = new TimedDelay(fDelay);
						pqe.tdSleep.updateTime();
						dumpDevInfoEntry(strCmd);
						break;
					}else{
						astrCmdListFast.add(strCmd);
					}
				}
				
				if(astrCmdListFast.size()>0){
					if(pqe.bPrepend){
						Collections.reverse(astrCmdListFast);
					}
					for(String str:astrCmdListFast){
						addExecConsoleCommandToQueue(str, pqe.bPrepend);
					}
				}
				
			}
		}
	}
	protected void addExecConsoleCommandToQueue(StringField sfFullCmdLine){
		addExecConsoleCommandToQueue(sfFullCmdLine.toString());
	}
	protected void addExecConsoleCommandToQueue(String strFullCmdLine){
		addExecConsoleCommandToQueue(strFullCmdLine,false);
	}
	protected void addExecConsoleCommandToQueue(String strFullCmdLine, boolean bPrepend){
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
	
//	protected boolean cc.checkCmdValidityBoolTogglers(){
//		cc.btgReferenceMatched=null;
//		for(BoolToggler btg : BoolToggler.getBoolTogglerListCopy()){
//			if(cc.checkCmdValidity(btg.getCmdId(), "[bEnable] "+btg.getHelp(), true)){
//				cc.btgReferenceMatched = btg;
//				break;
//			}
//		}
//		return cc.btgReferenceMatched!=null;
//	}
//	protected boolean cc.checkCmdValidity(String strValidCmd){
//		return cc.checkCmdValidity(strValidCmd, null);
//	}
//	protected boolean cc.checkCmdValidity(StringField strfValidCmd, String strComment){
//		return cc.checkCmdValidity(strfValidCmd.toString(), strComment);
//	}
//	protected boolean cc.checkCmdValidity(String strValidCmd, String strComment){
//		return cc.checkCmdValidity(strValidCmd, strComment, false);
//	}
//	protected boolean cc.checkCmdValidity(String strValidCmd, String strComment, boolean bSkipSortCheck){
//		if(strCmdLinePrepared==null){
//			if(strComment!=null){
//				strValidCmd+=cc.commentToAppend(strComment);
//			}
//			
//			addCmdToValidList(strValidCmd,bSkipSortCheck);
//			
//			return false;
//		}
//		
//		if(cc.RESTRICTED_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared))return false;
//		if(isCommentedLine())return false;
//		if(strCmdLinePrepared.trim().isEmpty())return false;
//		
////		String strCheck = strPreparedCmdLine;
////		strCheck = strCheck.trim().split(" ")[0];
//		strValidCmd = strValidCmd.trim().split(" ")[0];
//		
////		return strCheck.equalsIgnoreCase(strValidCmd);
//		return paramString(0).equalsIgnoreCase(strValidCmd);
//	}
	
	protected void autoCompleteInputField(){
		autoCompleteInputField(false);
	}
	protected void autoCompleteInputField(boolean bMatchContains){
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
			String strBaseCmd = extractCommandPart(strCmdPart,0);
			String strParam1 = extractCommandPart(strCmdPart,1);
			
			String strCmd=null;
			String strPartToComplete=null;
			ArrayList<String> astrOptList = null;
			boolean bIsVarCompletion=false;
			if(cc.CMD_CONSOLE_STYLE.equals(strBaseCmd)){
				strCmd=cc.CMD_CONSOLE_STYLE+" ";
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
				astrOptList=getVariablesIdentifiers(false);
				strPartToComplete=strParam1;
				bIsVarCompletion=true;
			}else{
				/**
				 * complete for variables ids when retrieving variable value 
				 */
				String strRegexVarOpen=Pattern.quote(""+cc.getVariableExpandPrefix()+"{");
				String strRegex=".*"+strRegexVarOpen+"["+strValidCmdCharsRegex+cc.RESTRICTED_TOKEN+"]*$";
				if(strCompletedCmd.matches(strRegex)){
					strCmd=strCompletedCmd.trim().substring(1); //removes command prefix
					astrOptList=getVariablesIdentifiers(true);
					
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
					autoComplete(strPartToComplete, astrOptList, bMatchContains);
				if(astr.size()==1){
					strCompletedCmd+=astr.get(0);
				}else{
					dumpInfoEntry("Param autocomplete:");
					String strFirst = astr.remove(0);
					for(String str:astr){
//						if(strPartToComplete!=null && str.equals(astr.get(0)))continue;
						if(bIsVarCompletion)str=varReportPrepare(str);
						dumpSubEntry(str);
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
	
	protected String autoCompleteWork(String strCmdPart, boolean bMatchContains){
		String strCmdPartOriginal = strCmdPart;
		strCmdPart=strCmdPart.trim();
		
		// no command typed
		if(strCmdPart.equalsIgnoreCase(""+cc.getCommandPrefix()) || strCmdPart.isEmpty())
			return strCmdPartOriginal;
		
		strCmdPart=strCmdPart.replaceFirst("^"+cc.getCommandPrefix(), "");
		
		// do not allow invalid chars
		if(!isValidIdentifierCmdVarAliasFuncString(strCmdPart))
//		if(!strCmdPart.matches("["+strValidCmdCharsRegex+"]*"))
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
	
	protected boolean isValidIdentifierCmdVarAliasFuncString(String strCmdPart) {
		if(strCmdPart==null)return false;
		return strCmdPart.matches("["+strValidCmdCharsRegex+"]*");
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
	
	protected String convertToCmdParams(String strFullCmdLine){
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
				astrMulti.set(i, astrMulti.get(i).trim()
					+(strComment.isEmpty()?"":cc.commentToAppend(strComment))
					+cc.commentToAppend("SplitCmdLine")
				);
//				astrMulti.set(i, astrMulti.get(i).trim()+" "+cc.getCommentPrefix()+"SplitCmdLine "+strComment);
			}
			addExecConsoleCommandBlockToQueue(astrMulti,true,true);
			return cc.RESTRICTED_CMD_SKIP_CURRENT_COMMAND.toString();
		}
		
		astrCmdAndParams.clear();
		astrCmdAndParams.addAll(convertToCmdParamsList(strFullCmdLine));
		return String.join(" ",astrCmdAndParams);
	}
	
	/**
	 * Each param can be enclosed within double quotes (")
	 * @param strFullCmdLine
	 * @return
	 */
	protected ArrayList<String> convertToCmdParamsList(String strFullCmdLine){
		ArrayList<String> astrCmdParams = new ArrayList<String>();
//		astrCmdAndParams.clear();
		
		/**
		 * Prepare parameters, separated by blanks, that can be enclosed in double quotes.
		 * Param 0 is the actual command
		 */
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(strFullCmdLine);
		while (m.find()){
			String str=m.group(1);
			if(str!=null){
				if(str.trim().startsWith(""+cc.getCommentPrefix()))break; //ignore comments
				str=str.trim();
				str=str.replace("\"", ""); //remove all quotes on the string TODO could be only 1st and last? mmm... too much trouble...
//				astrCmdAndParams.add(str);
				astrCmdParams.add(str);
			}
		}
		
//		return strFullCmdLine;
		return astrCmdParams;
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
	
	/**
	 * this method must be used just before a command is going to be executed,
	 * so variables have time to be updated by other commands etc.
	 * @param strParam
	 * @return
	 */
	protected String applyVariablesValues(String strParam){
		// fast skip
		if(!strParam.contains(cc.getVariableExpandPrefix()+"{"))return strParam;
		
		for(String strVarId : getVariablesIdentifiers(true)){
			String strToReplace=cc.getVariableExpandPrefix()+"{"+strVarId+"}";
			if(strParam.toLowerCase().contains(strToReplace.toLowerCase())){
//				strParam=strParam.replace(strToReplace, ""+getVarHT(strVarId).get(strVarId));
				strParam=strParam.replaceAll(
					"(?i)"+Pattern.quote(strToReplace), 
					""+getVarValue(strVarId));
				
				// nothing remaining to be done
				if(!strParam.contains(cc.getVariableExpandPrefix()+"{"))break;
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
			varSet(btg, ""+btg.getBoolean(), true);
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
			
			strFullCmdLine = strFullCmdLine.trim();
			if(strFullCmdLine.startsWith(cc.getCommandPrefixStr())){
				strFullCmdLine = strFullCmdLine.substring(1); //cmd prefix 1 char
			}
			
			if(strFullCmdLine.endsWith(cc.getCommentPrefixStr())){
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
		return selectVarSource(strVarId).get(strVarId)!=null;
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
					addExecConsoleCommandToQueue(alias.strCmdLine
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
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidityBoolTogglers()){
			bCommandWorked=toggle(cc.btgReferenceMatched);
		}else
		if(cc.checkCmdValidity("alias",getAliasHelp(),true)){
			bCommandWorked=cmdAlias();
		}else
		if(cc.checkCmdValidity("clearCommandsHistory")){
			astrCmdHistory.clear();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity("clearDumpArea")){
			vlstrDumpEntries.clear();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(cc.CMD_CLOSE_CONSOLE,"like the bound key to do it")){
			setEnabled(false);
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(cc.CMD_CONSOLE_HEIGHT,"[fPercent] of the application window")){
			Float f = paramFloat(1);
			modifyConsoleHeight(f);
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(cc.CMD_CONSOLE_SCROLL_BOTTOM,"")){
			scrollToBottomRequest();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(cc.CMD_CONSOLE_STYLE,"[strStyleName] changes the style of the console on the fly, empty for a list")){
			String strStyle = paramString(1);
			if(strStyle==null)strStyle="";
			bCommandWorked=cmdStyleApply(strStyle);
		}else
		if(cc.checkCmdValidity(cc.CMD_DB,EDataBaseOperations.help())){
			bCommandWorked=cmdDb();
		}else
//		if(cc.checkCmdValidity("dumpFind","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
//		if(cc.checkCmdValidity("dumpFindNext","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
//		if(cc.checkCmdValidity("dumpFindPrevious","<text> finds, select and scroll to it at dump area")){
//			bCommandWorkedProperly=cmdFind();
//		}else
		if(cc.checkCmdValidity(cc.CMD_ECHO," simply echo something")){
			bCommandWorked=cc.cmdEcho();
		}else
		if(cc.checkCmdValidity(cc.CMD_ELSE,"conditinal block")){
			bCommandWorked=cc.cmdElse();
		}else
		if(cc.checkCmdValidity(cc.CMD_ELSE_IF,"<[!]<true|false>> conditional block")){
			bCommandWorked=cc.cmdElseIf();
		}else
		if(cc.checkCmdValidity("execBatchCmdsFromFile ","<strFileName>")){
			String strFile = paramString(1);
			if(strFile!=null){
				addExecConsoleCommandBlockToQueue(fileLoad(strFile));
//				astrExecConsoleCmdsQueue.addAll(fileLoad(strFile));
				bCommandWorked=true;
			}
		}else
		if(cc.checkCmdValidity("exit","the application")){
			cc.cmdExit();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(cc.CMD_FIX_CURSOR ,"in case cursor is invisible")){
			if(efHK==null){
				dumpWarnEntry("requires command: "+cc.CMD_HK_TOGGLE);
			}else{
				dumpInfoEntry("requesting: "+cc.CMD_FIX_CURSOR);
				efHK.bFixInvisibleTextInputCursorHK=true;
			}
			bCommandWorked = true;
		}else
		if(cc.checkCmdValidity(cc.CMD_FIX_LINE_WRAP ,"in case words are overlapping")){
			cmdLineWrapDisableDumpArea();
			bCommandWorked = true;
		}else
		if(cc.checkCmdValidity(cc.CMD_FIX_VISIBLE_ROWS_AMOUNT,"[iAmount] in case it is not showing as many rows as it should")){
			iVisibleRowsAdjustRequest = paramInt(1);
			if(iVisibleRowsAdjustRequest==null)iVisibleRowsAdjustRequest=0;
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(cc.CMD_FUNCTION,"<id> begins a function block")){
			bCommandWorked=cmdFunctionBegin();
		}else
		if(cc.checkCmdValidity(cc.CMD_FUNCTION_CALL,"<id> [parameters...] retrieve parameters values with ex.: ${id_1} ${id_2} ...")){
			bCommandWorked=cmdFunctionCall();
		}else
		if(cc.checkCmdValidity(cc.CMD_FUNCTION_END,"ends a function block")){
			bCommandWorked=cmdFunctionEnd();
		}else
		if(cc.checkCmdValidity("fpsLimit","[iMaxFps]")){
			Integer iMaxFps = paramInt(1);
			if(iMaxFps!=null){
				fpslState.setMaxFps(iMaxFps);
				bCommandWorked=true;
			}
			dumpSubEntry("FpsLimit = "+fpslState.getFpsLimit());
		}else
		if(cc.checkCmdValidity(cc.CMD_HELP,"[strFilter] show (filtered) available commands")){
			cmdShowHelp(paramString(1));
			/**
			 * ALWAYS return TRUE here, to avoid infinite loop when improving some failed command help info!
			 */
			bCommandWorked=true; 
		}else
		if(cc.checkCmdValidity(cc.CMD_HISTORY,"[strFilter] of issued commands (the filter results in sorted uniques)")){
			bCommandWorked=cmdShowHistory();
		}else
		if(cc.checkCmdValidity(cc.CMD_HK_TOGGLE ,"[bEnable] allow hacks to provide workarounds")){
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
				
				bCommandWorked=true;
			}
		}else
		if(cc.checkCmdValidity(cc.CMD_IF,"<[!]<true|false>> [cmd|alias] if cmd|alias is not present, this will be a multiline block start!")){
			bCommandWorked=cc.cmdIf();
		}else
		if(cc.checkCmdValidity(cc.CMD_IF_END,"ends conditional block")){
			bCommandWorked=cc.cmdIfEnd();
		}else
		if(cc.checkCmdValidity("initFileShow ","show contents of init file at dump area")){
			dumpInfoEntry("Init file data: ");
			for(String str : fileLoad(flInit)){
				dumpSubEntry(str);
			}
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(cc.CMD_LINE_WRAP_AT,"[iMaxChars] -1 = will trunc big lines, 0 = wrap will be automatic")){
			iConsoleMaxWidthInCharsForLineWrap = paramInt(1);
			if(iConsoleMaxWidthInCharsForLineWrap!=null){
				if(iConsoleMaxWidthInCharsForLineWrap==-1){
					iConsoleMaxWidthInCharsForLineWrap=null;
				}
			}
			updateWrapAt();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity("quit","the application")){
			cc.cmdExit();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity("showBinds","")){
			dumpInfoEntry("Key bindings: ");
			dumpSubEntry("Ctrl+B - marks dump area begin selection marker for copy");
			dumpSubEntry("Ctrl+Del - clear input field");
			dumpSubEntry("TAB - autocomplete (starting with)");
			dumpSubEntry("Ctrl+TAB - autocomplete (contains)");
			dumpSubEntry("Ctrl+/ - toggle input field comment");
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity("showSetup","show restricted variables")){
			for(String str:fileLoad(flSetup)){
				dumpSubEntry(str);
			}
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(cc.CMD_SLEEP,"<fDelay> will wait before executing next command in the command block")){
			/**
			 * This is only used on the pre-queue, 
			 * here it is ignored.
			 */
			if(!cc.btgPreQueue.b())dumpWarnEntry(cc.CMD_SLEEP+" only works with pre-queue enabled");
			dumpWarnEntry(cc.CMD_SLEEP+" only works on command blocks like alias, functions, running a file");
			bCommandWorked=true;
		}else
//		if(cc.checkCmdValidity("showDump","<filter> show matching entries from dump log file")){
//			String strFilter = paramString(1);
//			if(strFilter!=null){
//				for(String str:fileLoad(flLastDump)){
//					if(str.toLowerCase().contains(strFilter)){
//						dumpEntry(false, true, false, str);
//					}
//				}
//				bCommandWorked=true;
//			}
//		}else
		if(cc.checkCmdValidity("statsFieldToggle","[bEnable] toggle simple stats field visibility")){
			bCommandWorked=statsFieldToggle();
		}else
		if(cc.checkCmdValidity("statsShowAll","show all console stats")){
			dumpAllStats();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity("test","[...] temporary developer tests")){
			cmdTest();
			if(efHK!=null)efHK.test();
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity("varAdd","<varId> <[-]value>")){
			bCommandWorked=cmdVarAdd(paramString(1),paramString(2),true,false);
		}else
//		if(cc.checkCmdValidity(cc.CMD_VAR_SET,"[<varId> <value>] | [-varId] | ["+cc.getFilterToken()+"filter] - can be a number or a string, retrieve it's value with: ${varId}")){
		if(
			cc.checkCmdValidity(cc.CMD_VAR_SET,
				"<[<varId> <value>] | [-varId]> "
					+"Can be boolean(true/false, and after set accepts 1/0), number(integer/floating) or string; "
					+"-varId will delete it; "
					+"Retrieve it's value with "+cc.getVariableExpandPrefix()+"{varId}; "
					+"Restricted variables will have no effect; "
			)
		){
			bCommandWorked=cmdVarSet();
		}else
		if(cc.checkCmdValidity("varSetCmp","<varIdBool> <value> <cmp> <value>")){
			bCommandWorked=cmdVarSetCmp();
		}else
		if(cc.checkCmdValidity("varShow","[["+cc.RESTRICTED_TOKEN+"]filter] list user or restricted variables.")){
			bCommandWorked=cmdVarShow();
		}else
		if(cc.checkCmdValidity(TOKEN_CMD_NOT_WORKING_YET+"zDisabledCommand"," just to show how to use it")){
			// keep this as reference
		}else{
//			if(strCmdLinePrepared!=null){
//				if(SPECIAL_CMD_MULTI_COMMAND_LINE_OK.equals(strCmdLinePrepared)){
//					bOk=true;
//				}
//			}
		}
		
		return bCommandWorked;
	}
	
	protected boolean isStartupCommandsQueueDone(){
		return bStartupCmdQueueDone;
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
				addExecConsoleCommandBlockToQueue(fileLoad(flDB),true,false);
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
	
	protected boolean hasChanged(ERestrictedSetupLoadableVars rv){
		String strValue = varGetValueString(""+cc.RESTRICTED_TOKEN+rv);
		switch(rv){
			case userAliasListHashcode:
				return !(""+aAliasList.hashCode()).equals(strValue);
			case userVariableListHashcode:
				return !(""+tmUserVariables.hashCode()).equals(strValue);
		}
		
		return false;
	}
	
	protected boolean isDatabaseChanged(){
		if(hasChanged(ERestrictedSetupLoadableVars.userAliasListHashcode))return true;
		if(hasChanged(ERestrictedSetupLoadableVars.userVariableListHashcode))return true;
		
		return false;
	}
	
	protected boolean databaseBackup() {
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

	protected boolean cmdVarShow() {
		String strFilter = paramString(1);
		if(strFilter==null)strFilter="";
		strFilter=strFilter.trim();
		
		/**
		 * LIST all, user and restricted
		 */
		dumpInfoEntry("Variables list:");
		boolean bRestrictedOnly=false;
		if(strFilter.startsWith(""+cc.RESTRICTED_TOKEN)){
			bRestrictedOnly=true;
			strFilter=strFilter.substring(1);
		}
//		if(strFilter!=null)strFilter=strFilter.substring(1);
		for(String strVarId : getVariablesIdentifiers(true)){
			if(isRestricted(strVarId) && !bRestrictedOnly)continue;
			if(!isRestricted(strVarId) && bRestrictedOnly)continue;
			
			if(strVarId.toLowerCase().contains(strFilter.toLowerCase()))varReport(strVarId);
		}
		dumpSubEntry(cc.getCommentPrefix()+"UserVarListHashCode="+tmUserVariables.hashCode());
		
		return true;
	}
	
	/**
	 * When creating variables, this method can only create custom user ones.
	 * @return
	 */
	protected boolean cmdVarSet() {
		String strVarId = paramString(1);
		String strValue = paramString(2);
		
		if(strVarId==null)return false;
		
		boolean bOk = false;
		if(strVarId.trim().startsWith(""+cc.getVarDeleteToken())){
			/**
			 * DELETE/UNSET only user variables
			 */
			bOk=tmUserVariables.remove(strVarId.trim().substring(1))!=null;
			if(bOk){
				dumpInfoEntry("Var '"+strVarId+"' deleted.");
			}else{
				dumpWarnEntry("Var '"+strVarId+"' not found.");
			}
		}else{
			/**
			 * SET user or restricted variable
			 */
			if(isRestrictedAndDoesNotExist(strVarId))return false;
			bOk=varSet(strVarId,strValue,true);
		}
		
		return bOk;
	}
	
	protected boolean isRestrictedAndDoesNotExist(String strVar){
		if(isRestricted(strVar)){
			// user can only set existing restricted vars
			if(!selectVarSource(strVar).containsKey(strVar)){
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
				(Misc.i().parseBoolean(strValueLeft) || Misc.i().parseBoolean(strValueRight)), true);
		}else
		if(strCmp.equals("&&")){
			return varSet(strVarId, ""+
				(Misc.i().parseBoolean(strValueLeft) && Misc.i().parseBoolean(strValueRight)), true);
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
	
	protected boolean checkFuncExecEnd() {
		if(strCmdLineOriginal==null)return false;
		return strCmdLineOriginal.startsWith(cc.RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS.toString());
	}
	protected boolean checkFuncExecStart() {
		if(strCmdLineOriginal==null)return false;
		return strCmdLineOriginal.startsWith(cc.RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS.toString());
	}
	protected boolean cmdFunctionCall() {
		String strFunctionId = paramString(1);
		
		ArrayList<String> astrFuncParams = new ArrayList<String>();
		int i=2;
		while(true){
			String strFuncParam = paramString(i);
			if(strFuncParam==null)break;
			String strParamId=strFunctionId+"_"+(i-1);
			astrFuncParams.add(strParamId);
			varSet(strParamId, strFuncParam, false);
			i++;
		}
		
		ArrayList<String> astrFuncBlock = tmFunctions.get(strFunctionId);
		
		if(astrFuncBlock!=null && astrFuncBlock.size()>0){
			dumpInfoEntry("Running function: "+strFunctionId+" "+cc.getCommentPrefix()+"totLines="+astrFuncBlock.size());
			
			ArrayList<String> astrFuncBlockToExec = new ArrayList<String>(astrFuncBlock);
			astrFuncBlockToExec.add(0,cc.RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS+" "+strFunctionId);
			for(String strUnsetVar:astrFuncParams){
				astrFuncBlockToExec.add(cc.getCommandPrefix()+
					cc.CMD_VAR_SET.toString()+" "+cc.getVarDeleteToken()+strUnsetVar);
			}
			astrFuncBlockToExec.add(cc.RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS+" "+strFunctionId);
			addExecConsoleCommandBlockToQueue(astrFuncBlockToExec, true, true);
		}
		
		return true;
	}
	protected boolean cmdFunctionBegin() {
		String strFunctionId = paramString(1);
		
		if(!isValidIdentifierCmdVarAliasFuncString(strFunctionId))return false;
		
		tmFunctions.put(strFunctionId, new ArrayList<String>());
		dumpInfoEntry("Function block begin for: "+strFunctionId);
		
		strPrepareFunctionBlockForId=strFunctionId;
		
		return true;
	}
	protected boolean functionFeed(String strCmdLine){
		ArrayList<String> astr = tmFunctions.get(strPrepareFunctionBlockForId);
		astr.add(strCmdLine);
		dumpDevInfoEntry("Function line added: "+strCmdLine+" "+cc.getCommentPrefix()+"tot="+astr.size());
		return true;
	}
	protected boolean functionEndCheck(String strCmdLine) {
		String strCmdCheck=""+cc.getCommandPrefix()+cc.CMD_FUNCTION_END.toString();
		strCmdCheck=strCmdCheck.toLowerCase();
		if(strCmdCheck.equals(strCmdLine.trim().toLowerCase())){
			return cmdFunctionEnd();
		}
		return false;
	}
	protected boolean cmdFunctionEnd() {
		if(strPrepareFunctionBlockForId==null){
			dumpExceptionEntry(new NullPointerException("no function being prepared..."));
			return false;
		}
		
		dumpInfoEntry("Function block ends for: "+strPrepareFunctionBlockForId);
		strPrepareFunctionBlockForId=null;
		
		return true;
	}
	
//	protected Boolean parseBoolean(String strValue){
//		if(strValue.equalsIgnoreCase("true"))return new Boolean(true);
//		if(strValue.equalsIgnoreCase("1"))return new Boolean(true);
//		if(strValue.equalsIgnoreCase("false"))return new Boolean(false);
//		if(strValue.equalsIgnoreCase("0"))return new Boolean(false);
//		throw new NumberFormatException("invalid boolean value: "+strValue);
//	}
	
	/**
	 * In case variable exists will be this method.
	 * @param strVarId
	 * @param strValueAdd
	 * @param bOverwrite
	 * @return
	 */
	protected boolean cmdVarAdd(String strVarId, String strValueAdd, boolean bSave, boolean bOverwrite){
		if(isRestrictedAndDoesNotExist(strVarId))return false;
		
		Object objValueNew = null;
		Object objValueCurrent = selectVarSource(strVarId).get(strVarId);
		
		if(objValueCurrent==null){
			dumpExceptionEntry(new NullPointerException("value is null for var "+strVarId));
			return false;
		}
			
//		if(objValueCurrent!=null){
			if(Boolean.class.isAssignableFrom(objValueCurrent.getClass())){
				// boolean is always overwrite
				objValueNew = Misc.i().parseBoolean(strValueAdd);
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
		
		varApply(strVarId,objValueNew,bSave);
		return true;
	}
	
	protected boolean isRestricted(String strId){
		return strId.startsWith(""+cc.RESTRICTED_TOKEN);
	}
	
	protected Object getVarValue(String strVarId){
		return selectVarSource(strVarId).get(strVarId);
	}
	protected void setVarValue(String strVarId, Object objValue){
		selectVarSource(strVarId).put(strVarId,objValue);
	}
	protected TreeMap<String, Object> selectVarSource(String strVarId){
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
	
	protected void varSaveSetupFile(){
		for(String strVarId : getVariablesIdentifiers(true)){
			if(isRestricted(strVarId))fileAppendVar(strVarId);
		}
	}
	
	protected void fileAppendVar(String strVarId){
		String strCommentOut="";
		String strReadOnlyComment="";
		if(isRestricted(strVarId)){
			try{ERestrictedSetupLoadableVars.valueOf(strVarId.substring(1));}catch(IllegalArgumentException e){
				/**
				 * comment non loadable restricted variables, like the ones set by commands
				 */
				strCommentOut=cc.getCommentPrefixStr();
				strReadOnlyComment="(ReadOnly)";
			}
		}
		
		fileAppendLine(getVarFile(strVarId), strCommentOut+varReportPrepare(strVarId)+strReadOnlyComment);
	}
	
	protected boolean varApply(String strVarId, Object objValue, boolean bSave){
		selectVarSource(strVarId).put(strVarId,objValue);
		if(bSave)fileAppendVar(strVarId);
		
		if(isRestricted(strVarId) && cc.btgShowDeveloperInfo.b()){
			varReport(strVarId);
		}
		
		return true;
	}
	
	protected String varReportPrepare(String strVarId) {
		Object objValue = selectVarSource(strVarId).get(strVarId);
		String str="";
		
		str+=cc.getCommandPrefix();
		str+=cc.CMD_VAR_SET.toString();
		str+=" ";
		str+=strVarId;
		str+=" ";
		if(objValue!=null){
			str+="\""+objValue+"\"";
			str+=" ";
		}
		str+="#";
		if(objValue!=null){
			str+=objValue.getClass().getSimpleName();
		}else{
			str+="(ValueNotSet)";
		}
		str+=" ";
		str+=(isRestricted(strVarId)?"(Restricted)":"(User)");
		
		return str;
	}
	
	protected void varReport(String strVarId) {
		Object objValue=selectVarSource(strVarId).get(strVarId);
		if(objValue!=null){
			dumpSubEntry(varReportPrepare(strVarId));
		}else{
			dumpSubEntry(strVarId+" is not set...");
		}
	}
	
	protected boolean varSet(StringField sfId, String strValue, boolean bSave) {
		return varSet(cc.RESTRICTED_TOKEN+sfId.toString(), strValue, bSave);
	}
	protected boolean varSet(BoolToggler btg, String strValue, boolean bSave) {
		return varSet(cc.RESTRICTED_TOKEN+btg.getCmdId(), strValue, bSave);
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
			return cmdVarAdd(strVarId, strValue, bSave, true);
		}
		
		boolean bOk=false;
		
		/**
		 * Priority:
		 * Double would parse a Long.
		 * Boolean would be accepted by String that accepts everything. 
		 */
		if(!bOk)try{bOk=varApply(strVarId, Long  .parseLong     (strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)try{bOk=varApply(strVarId, Double.parseDouble   (strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)try{bOk=varApply(strVarId, Misc.i().parseBoolean(strValue),bSave);}catch(NumberFormatException e){}// accepted exception!
		if(!bOk)bOk=varApply(strVarId,strValue,bSave);
		
		return bOk;
	}
	
	/**
	 * 
	 * @param strVarId
	 * @return "null" if not set
	 */
	protected String varGetValueString(String strVarId){
		Object obj = selectVarSource(strVarId).get(strVarId);
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
		
		boolean bCmdWorkDone = false;
		try{
			if(!bCmdWorkDone)bCmdWorkDone=cmdRawLineCheckEndOfStartupCmdQueue();
			
			if(!bCmdWorkDone)bCmdWorkDone=cmdRawLineCheckAlias();
			
//			if(!bOk)bOk=cmdRawLineCheckIfElse();
			
//			if(!bOk){
//				if(SPECIAL_CMD_SKIP_CURRENT_COMMAND.equals(strCmdLinePrepared)){
//					bOk=true;
//				}
//			}
			
			
			
			/**
			 * we will have a prepared line after below
			 */
			strCmdLinePrepared = prepareCmdAndParams(strCmdLineOriginal);
			
			if(!bCmdWorkDone){
				if(bFuncCmdLineRunning){
					if(checkFuncExecEnd()){
						bFuncCmdLineRunning=false;
						bFuncCmdLineSkipTilEnd=false;
						bCmdWorkDone=true;
					}
				}
			}
			
			if(!bCmdWorkDone){
				if(checkFuncExecStart()){
					bFuncCmdLineRunning=true;
					bCmdWorkDone=true;
				}else
				if(strPrepareFunctionBlockForId!=null){
					if(!bCmdWorkDone)bCmdWorkDone = functionEndCheck(strCmdLineOriginal); //before feed
					if(!bCmdWorkDone)bCmdWorkDone = functionFeed(strCmdLineOriginal);
				}else
				if(cc.bIfConditionExecCommands!=null && !cc.bIfConditionExecCommands){
					/**
					 * These are capable of stopping the skipping.
					 */
					if(cc.CMD_ELSE_IF.equals(paramString(0))){
						if(!bCmdWorkDone)bCmdWorkDone = cc.cmdElseIf();
					}else
					if(cc.CMD_ELSE.equals(paramString(0))){
						if(!bCmdWorkDone)bCmdWorkDone = cc.cmdElse();
					}else
					if(cc.CMD_IF_END.equals(paramString(0))){
						if(!bCmdWorkDone)bCmdWorkDone = cc.cmdIfEnd();
					}else{
						/**
						 * The if condition resulted in false, therefore commands must be skipped.
						 */
						dumpInfoEntry("ConditionalSkip: "+strCmdLinePrepared);
						if(!bCmdWorkDone)bCmdWorkDone = true;
					}
				}else{
					if(bFuncCmdLineRunning && bFuncCmdLineSkipTilEnd){
						dumpWarnEntry("SkippingRemainingFunctionCmds: "+strCmdLinePrepared);
						bCmdWorkDone = true; //this just means that the skip worked
					}
					
					if(!bCmdWorkDone){
						/**
						 * normal commands execution
						 */
						bCmdWorkDone = executePreparedCommand();
						
						if(bFuncCmdLineRunning && !bCmdWorkDone){
							// a command may fail inside a function, only that first one will generate error message 
							bFuncCmdLineSkipTilEnd=true;
						}
					}
				}
				
			}
		}catch(NumberFormatException e){
			// keep this one as "warning", as user may simply fix the typed value
			dumpWarnEntry("NumberFormatException: "+e.getMessage());
			e.printStackTrace();
			bCmdWorkDone=false;
		}
		
		return bCmdWorkDone;
	}
	
//	protected boolean isFunctionExecLine() {
//		if(cc.RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS.equals(strCmdLineOriginal)){
//			bStartupCmdQueueDone=true;
//			return true;
//		}
//		return false;
//	}
	
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
		
		varSet(cc.CMD_CONSOLE_HEIGHT, ""+fConsoleHeightPerc, true);
		
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
			
			varSet(cc.CMD_CONSOLE_STYLE, strStyle, true);
			
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
				
				+"If"+cc.aIfConditionNestedList.size()
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
	
	/**
	 * These variables can be loaded from the setup file!
	 */
	enum ERestrictedSetupLoadableVars{
		userVariableListHashcode,
		userAliasListHashcode,
	}
	
	protected void setupRecreateFile(){
		flSetup.delete();
		
		fileAppendLine(flSetup, cc.getCommentPrefix()+" DO NOT EDIT!");
		fileAppendLine(flSetup, cc.getCommentPrefix()
			+" This file will be overwritten by the application!");
		fileAppendLine(flSetup, cc.getCommentPrefix()
			+" To set overrides use the user init config file.");
		fileAppendLine(flSetup, cc.getCommentPrefix()
			+" For command's values, the commands usage are required, the variable is just an info about their setup value.");
		fileAppendLine(flSetup, cc.getCommentPrefix()
			+" Some values will be read tho to provide restricted functionalities not accessible to users.");
		
		setupVars(true);
	}
	
	protected void setupVars(boolean bSave){
		varSet(""+cc.RESTRICTED_TOKEN+ERestrictedSetupLoadableVars.userVariableListHashcode,
			""+tmUserVariables.hashCode(),
			false);
		
		varSet(""+cc.RESTRICTED_TOKEN+ERestrictedSetupLoadableVars.userAliasListHashcode,
			""+aAliasList.hashCode(),
			false);
		
		if(bSave)varSaveSetupFile();
	}
	
}

