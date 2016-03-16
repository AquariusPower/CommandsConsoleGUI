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
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.simsilica.lemur.style.StyleLoader;
import com.simsilica.lemur.style.Styles;

/**
 * A simple graphical console where developers and users can issue commands. 
 * https://gist.github.com/AquariusPower/73eba57eb999a760a641
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleGuiState implements AppState, ReflexFill.IReflexFillCfg{
	
	public final String IMCPREFIX = "CONSOLEGUISTATE_";
	public final StringField INPUT_MAPPING_CONSOLE_TOGGLE = new StringField(this);
	public final StringField INPUT_MAPPING_CONSOLE_SCROLL_UP = new StringField(this);
	public final StringField INPUT_MAPPING_CONSOLE_SCROLL_DOWN = new StringField(this);
	public final StringField INPUT_MAPPING_CONSOLE_SHIFT_PRESSED	= new StringField(this);
	public final StringField INPUT_MAPPING_CONSOLE_CONTROL_PRESSED	= new StringField(this);
	
	public final String STYLE_CONSOLE="console";
	
	public final String	TOKEN_MULTI_COMMAND_LINE	= "_MultiCommandLineToken";
	
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
	protected VersionedList<String>	vlstrDumpEntries = new VersionedList<String>();;
	protected VersionedList<String>	vlstrAutoCompleteHint = new VersionedList<String>();;
	protected int	iShowRows = 1;
	protected Integer	iToggleConsoleKey;
	protected Integer	iVisibleRowsAdjustRequest = 0; //0 means dynamic
	protected String	strInfoEntryPrefix			=". ";
	protected String	strWarnEntryPrefix			="?Warn: ";
	protected String	strExceptionEntryPrefix	="!EXCEPTION: ";
	protected String	strDevWarnEntryPrefix="?DevWarn: ";
	protected String	strDevInfoEntryPrefix=". DevInfo: ";
	protected String	strSubEntryPrefix="\t";
	protected boolean	bInitiallyClosed = true;
	protected ArrayList<String> astrCmdHistory = new ArrayList<String>();
	protected ArrayList<String> astrCmdCmtValidList = new ArrayList<String>();
	protected ArrayList<String> astrBaseCmdValidList = new ArrayList<String>();
	protected ArrayList<String> astrStyleList = new ArrayList<String>();
	protected int	iCmdHistoryCurrentIndex = 0;
	protected	String	strNotSubmitedCmd=null; //null is a marker here
	protected	String	strCommentPrefixChar="#";
	protected	String	strCommandPrefixChar="/";
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
	protected String	strFilePrefix = ConsoleGuiState.class.getSimpleName();
	protected String	strFileCmdHistory = strFilePrefix+"-CmdHist.txt";
	protected String	strFileLastDump = strFilePrefix+"-LastDump.txt";
	protected String	strFileInitConsCmds = strFilePrefix+"-Init.cfg";
	protected File	flCmdHist;
	protected File	flLastDump;
	protected float	fConsoleHeightPercDefault = 0.5f;
	protected float	fConsoleHeightPerc = fConsoleHeightPercDefault;
	protected ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
	protected ArrayList<String>	astrExecConsoleCmdsQueue = new ArrayList<String>();
	protected File	flInit;
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
	protected String	strValidCmdCharsRegex = "a-zA-Z0-9_-";
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
	protected String	strCommandDelimiter = ";";
//	protected boolean	bShowExecQueuedInfo = false;
	protected ConsoleCommands	cc;
	Hashtable<String,Object> ahkVariables = new Hashtable<String,Object>();
	protected ArrayList<Alias> aAliasList = new ArrayList<Alias>();
	protected String	strCmdLineOriginal;
	protected boolean	bLastAliasCreatedSuccessfuly;
	protected String	strAliasPrefix = "$";
	private BoolToggler	btgReferenceMatched;
	
	protected static ConsoleGuiState i;
	public static ConsoleGuiState i(){
		return i;
	}
	public ConsoleGuiState() {
		if(i==null)i=this;
		cc = new ConsoleCommands();
	}
	public ConsoleGuiState(int iToggleConsoleKey) {
		this();
		this.bEnabled=true; //just to let it be initialized at startup by state manager
		this.iToggleConsoleKey=iToggleConsoleKey;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		if(isInitialized())throw new NullPointerException("already initialized...");
		
		sapp = (SimpleApplication)app;
		
		GuiGlobals.initialize(sapp);
		BaseStyles.loadGlassStyle(); //do not mess with default user styles: GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
		
		// init cmd history
		flCmdHist = new File(strFileCmdHistory);
		cmdHistLoad();
		
		// init dump file
		flLastDump = new File(strFileLastDump);
		flLastDump.delete(); //each run will have a new file
		
		// init valid cmd list
		executeCommand(null); //to populate the array with available commands
		
		// init user cfg
		flInit = new File(strFileInitConsCmds);
		if(flInit.exists()){
			addToExecConsoleCommandQueue(fileLoad(flInit));
		}else{
			fileAppendLine(flInit, strCommentPrefixChar+" console commands here will be executed at startup");
		}
		
		// other inits
		addToExecConsoleCommandQueue(cc.CMD_FIX_LINE_WRAP);
		addToExecConsoleCommandQueue(cc.CMD_CONSOLE_SCROLL_BOTTOM);
		if(bInitiallyClosed){
			addToExecConsoleCommandQueue(cc.CMD_CLOSE_CONSOLE);
		}
		/**
		 * must be the last queued command after all init ones!
		 */
		addToExecConsoleCommandQueue(cc.btgShowExecQueuedInfo.getCmdId()+" "+true);
		
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
		
		tdLetCpuRest.updateTime();
		tdStatsRefresh.updateTime();
		
		createMonoSpaceFontStyle();
		
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
		 * TOP ELEMENT
		 */
		ctnrStatsAndControls = new Container(strStyle);
		ctnrConsole.addChild(ctnrStatsAndControls, BorderLayout.Position.North);
		
		// console stats
		lblStats = new Label("Console stats.",Styles.ROOT_STYLE); //TODO make use chosen style... root one is more readable...
		lblStats.setColor(ColorRGBA.Yellow);
		fStatsHeight = retrieveBitmapTextFor(lblStats).getLineHeight();
		ctnrStatsAndControls.addChild(lblStats,0,0);
		
		// buttons 
		int iButtonIndex=0;
		btnClipboardShow = new Button("ShwClpbrd",Styles.ROOT_STYLE); //TODO make use chosen style... root one is more readable...
//		btnClipboardShow.setPreferredSize(new Vector3f(100,20,0));
		btnClipboardShow.addClickCommands(new ButtonClick());
		ctnrStatsAndControls.addChild(btnClipboardShow,0,++iButtonIndex);
		
		btnCopy = new Button("Copy",Styles.ROOT_STYLE); //TODO make use chosen style... root one is more readable...
//		btnClipboardShow.setPreferredSize(new Vector3f(100,20,0));
		btnCopy.addClickCommands(new ButtonClick());
		ctnrStatsAndControls.addChild(btnCopy,0,++iButtonIndex);
		
		btnPaste = new Button("Paste",Styles.ROOT_STYLE); //TODO make use chosen style... root one is more readable...
//		btnClipboardShow.setPreferredSize(new Vector3f(100,20,0));
		btnPaste.addClickCommands(new ButtonClick());
		ctnrStatsAndControls.addChild(btnPaste,0,++iButtonIndex);
		
		/**
		 * CENTER ELEMENT (dump entries area)
		 */
		lstbx = new ListBox<String>(new VersionedList<String>(),strStyle);
		Vector3f v3fLstbxSize = v3fConsoleSize.clone();
		lstbx.setSize(v3fLstbxSize); // no need to update fLstbxHeight, will be automatic
		//TODO not working? lstbx.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		
		/**
		 * The existance of at least one entry is very important to help on initialization.
		 * Actually to determine the listbox entry height.
		 */
		if(vlstrDumpEntries.isEmpty())vlstrDumpEntries.add(strCommentPrefixChar+" Initializing console.");
		
		lstbx.setModel(vlstrDumpEntries);
		lstbx.setVisibleItems(iShowRows);
//		lstbx.getGridPanel().setVisibleSize(iShowRows,1);
		ctnrConsole.addChild(lstbx, BorderLayout.Position.Center);
		
//		gpListboxDumpArea = lstbx.getGridPanel();
		
		/**
		 * BOTTOM ELEMENT
		 */
		// input
		tfInput = new TextField(strCommandPrefixChar,strStyle);
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
				for(String str:astr)dumpEntry(str);
				dumpEntry("<<< Clipboard END");
				if(bAddEmptyLineAfterCommand)dumpEntry("");
//				dumpEntry("");
				scrollToBottomRequest();
			}else
			if(source.equals(btnCopy)){
				editCopy();
			}else
			if(source.equals(btnPaste)){
				editPaste();
			}
		}
	}
	
	protected void editCopy() {
//		Integer iCopyTo = getDumpAreaSelectedIndex();
		if(iCopyTo>=0){
			
			if(iCopyFrom==-1)iCopyFrom=iCopyTo;
			
			// wrap mode overrides this behavior
			boolean bMultiLineMode = iCopyFrom!=iCopyTo;
			
			if(iCopyFrom>iCopyTo){
				int iCopyFromBkp=iCopyFrom;
				iCopyFrom=iCopyTo;
				iCopyTo=iCopyFromBkp;
			}
			
			String str = "";
			while(true){ //multi-line copy
				if(iCopyFrom>=vlstrDumpEntries.size())break;
				
				String strEntry = vlstrDumpEntries.get(iCopyFrom);
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
				
				str+=strEntry;
				
				if(!bJoinWithNext)str+="\n";
				
				if(bMultiLineMode){
					/**
					 * this overrides wrap mode, as user may not want other lines
					 * as he/she used a multi-line mode.
					 */
					if((iCopyFrom-iCopyTo)==0)break;
					
					iCopyFrom++;
				}else{ // single line mode
					if(bJoinWithNext){
						iCopyFrom++;
					}else{
						break;
					}
				}
			}
			putStringToClipboard(str);
			
			lstbx.getSelectionModel().setSelection(-1); //clear selection
		}
		
		iCopyFrom=-1;
		iCopyTo=-1;
	}

	protected void editPaste() {
		String strPasted = retrieveClipboardString(true);
		if(strPasted.endsWith("\\n"))strPasted=strPasted.substring(0, strPasted.length()-2);
		
		String strCurrent = getInputText();
//		if(checkInputEmpty() && validateBaseCommand(strPasted)){
		if(isInputTextFieldEmpty() && strPasted.trim().startsWith(strCommandPrefixChar)){
			strCurrent = strPasted.trim(); //replace "empty" line with command (can be invalid in this case, user may complete it properly)
		}else{
			if(efHK!=null){
				strCurrent = efHK.pasteAtCaratPositionHK(strCurrent,strPasted);
			}else{
				strCurrent+=strPasted;
			}
		}
		
		tfInput.setText(strCurrent); 
		
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
	
	protected void dumpSave(String str) {
		fileAppendLine(flLastDump,str);
	}
	
	protected void fileAppendLine(File fl, String str) {
		BufferedWriter bw = null;
		try{
			try {
				bw = new BufferedWriter(new FileWriter(fl, true));
				bw.write(str);
				bw.newLine();
			} catch (IOException e) {
				dumpExceptionEntry(e);
			}finally{
				if(bw!=null)bw.close();
			}
		} catch (IOException e) {
			dumpExceptionEntry(e);
		}
	}
	
	protected void test(){
		dumpInfoEntry("testing...");
		
		String strParam1 = paramString(1);
		
		dumpSubEntry("["+(char)Integer.parseInt(strParam1, 16)+"]");
		
//		if(getDumpAreaSelectedIndex()>=0){
//			dumpSubEntry("Selection:"+getDumpAreaSelectedIndex()+": '"+vlstrDumpEntries.get(getDumpAreaSelectedIndex())+"'");
//		}
	}	
	
	/**
	 * DO NOT USE!
	 * overlapping problem, doesnt work well... 
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
	
	protected void createMonoSpaceFontStyle(){
		if(bConsoleStyleCreated)return;
		
		boolean bUseGroovyScriptMode=false;
		if(bUseGroovyScriptMode){
			new StyleLoader().loadStyle(STYLE_CONSOLE, new StringReader(
					"import com.simsilica.lemur.component.*;\n" + 
					"\n" + 
					"selector('"+STYLE_CONSOLE+"') {\n" + 
					"    fontSize = 17\n" + 
//				"    font = font('Interface/Fonts/Default.fnt')\n" +
					"    font = font('Interface/Fonts/Console.fnt')\n" +
					"    color = color(1,1,1,1)\n" + 
					"    background = new QuadBackgroundComponent(color(0,0,0.25,1))\n" + 
					"}\n" + 
//						"selector('button', 'console') {\n" + 
//						"    background = new QuadBackgroundComponent(color(0, 1, 0, 1))\n" + 
//						"    color = color(0.5, 0.75, 0.75, 0.85)\n" + 
//						"}\n" + 
//						"selector('slider', 'button', 'console') {\n" + 
//						"    fontSize = 10\n" + 
//						"}"
					""
				));
		}else{
			BitmapFont font = sapp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
			//TODO improve the font quality to be more readable, how???
			
			Styles styles = GuiGlobals.getInstance().getStyles();
			
			ColorRGBA cl;
			
			Attributes attrs;
			attrs = styles.getSelector(STYLE_CONSOLE); // this also creates the style
			attrs.set("fontSize", 16);
			attrs.set("color", ColorRGBA.White.clone());
			cl = ColorRGBA.Blue.clone();cl.b=0.25f;cl.a=0.75f;
			attrs.set("background", new QuadBackgroundComponent(cl));
			attrs.set("font", font);
			
//			attrs = styles.getSelector("grid", STYLE_CONSOLE);
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,1,0,1)));
			
			attrs = styles.getSelector(Button.ELEMENT_ID, STYLE_CONSOLE);
			attrs.set("color", ColorRGBA.Green.clone());
			cl = ColorRGBA.Blue.clone();cl.a = 0.5f;
			attrs.set(Button.LAYER_BACKGROUND, new QuadBackgroundComponent(cl));
			
//		lstbx.getElementId().child(ListBox.SELECTOR_ID);
			attrs = styles.getSelector(ListBox.ELEMENT_ID, ListBox.SELECTOR_ID, STYLE_CONSOLE);
//			attrs = styles.getSelector("list", "selector", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Red.clone());
			cl = ColorRGBA.Yellow.clone();cl.a=0.25f;
			attrs.set(ListBox.LAYER_BACKGROUND, new QuadBackgroundComponent(cl));
			
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
//
//			attrs = styles.getSelector("slider", "button", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Yellow.clone());
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
//			
//			attrs = styles.getSelector("grid", "button", STYLE_CONSOLE);
//			attrs.set("color", ColorRGBA.Yellow.clone());
//			attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(0,0,0.25f,1)));
		}
		
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
						if(bControl)editCopy();
						break;
					case KeyInput.KEY_V: 
						if(bControl)editPaste();
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
									tfInput.setText(strNotSubmitedCmd);
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
		if(str.startsWith(strCommentPrefixChar)){
			str=str.substring(1);
		}else{
			str=strCommentPrefixChar+str;
		}
		tfInput.setText(str);
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
		
		tfInput.setText(astrCmdHistory.get(iIndex));
	}
	
	@Override
	public void update(float tpf) {
		if(!isEnabled())return;
		
		if(!tdLetCpuRest.isReady(true))return;
		
		updateToggles();
		updateDumpAreaSelectedIndex();
		updateVisibleRowsAmount();
		updateStats();
		updateScrollToBottom();
		if(efHK!=null)efHK.updateHK();
		updateExecConsoleCmdQueue();
		updateInputFieldFillWithSelectedEntry();
		updateAutoCompleteHint();
		
		updateCurrentCmdHistoryEntryReset();
		
		GuiGlobals.getInstance().requestFocus(tfInput);
	}
	
	protected void updateToggles() {
		if(cc.btgEngineStatsView.checkChangedAndUpdate())updateEngineStats();
		if(cc.btgEngineStatsFps.checkChangedAndUpdate())updateEngineStats();
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
//		strInputText=strInputText.substring(strCommandPrefixChar.length());
//	if(tfAutoCompleteHint.getParent()==null && bKeyControlIsPressed){
		if(lstbxAutoCompleteHint.getParent()==null && bKeyControlIsPressed){
			/**
			 * in this case, there is no hint,
			 * and match mode: "contains" was requested by user,
			 * so fill it with something!
			 */
		}else{
			if(strInputTextPrevious.equals(strInputText))return;
		}
		
		ArrayList<String> astr = autoComplete(strInputText, astrCmdCmtValidList, bKeyControlIsPressed);
		
		boolean bShowHint = false;
		
		if(astr.size()==0){
			bShowHint=false; //empty string, or simply no matches
		}else
		if(astr.size()==1 && strInputText.equals(extractCommandPart(astr.get(0),0))){
			// no extra matches found, only what was already typed was returned
			bShowHint=false;
		}else{
//		if(astr.size()>1){
			bShowHint=true; //show all extra matches
		}
		
//		if( astr.size()==1 && (astr.get(0).length() > strInputText.length()) ){ 
//			//was a single match
//			bShowHint=true;
//		}else
//		if(astr.size()<=1){
//			// no match, only what was already typed was returned
//			bShowHint=false;
//		}else{
//			// >1 matches
//			bShowHint=true;
//		}
		
		if(bShowHint){
			/*
			String strHint = "";
			for(String str:astr){
				//if(str.equals(strInputText))continue;
				if(astr.size()>1 && str.equals(astr.get(0)))continue; //skip the 1st that is still a partial match
				strHint+=str+"\n";
			}
			
			tfAutoCompleteHint.setText(strHint);
			if(!tfInput.hasChild(tfAutoCompleteHint)){
				tfInput.attachChild(tfAutoCompleteHint);
				tfAutoCompleteHint.setLocalTranslation(new Vector3f(0, -this.fInputHeight, 0));
			}
			*/
			
			vlstrAutoCompleteHint.clear();
			vlstrAutoCompleteHint.addAll(astr);
//			lstbxAutoCompleteHint.updateLogicalState(0f);
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
//				int iCount = astr.size();
//				if(iCount==1)iCount++; // must not be 1 or will crash
			lstbxAutoCompleteHint.setPreferredSize(new Vector3f(
				widthForListbox(),
				fHintHeight, //fInputHeight*iCount,
				0));
//				lstbxAutoCompleteHint.getGridPanel().setVisibleSize(iShowRows,1);
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
		if(strCmdFull.startsWith(strCommandPrefixChar)){
			strCmdFull=strCmdFull.substring(strCommandPrefixChar.length());
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
		
		if(strCurrentInputText.equals(strCommandPrefixChar))return true;
		
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
				
				String strCmdChk = vlstrDumpEntries.get(getDumpAreaSelectedIndex()).trim();
				if(validateBaseCommand(strCmdChk)){
					dumpAndClearInputField();
//					checkInputEmptyDumpIfNot(true);
					tfInput.setText(strCmdChk);
				}
				
				iSelectionIndexPreviousForFill = getDumpAreaSelectedIndex();
			}
		}
	}
	
	protected void updateDumpAreaSelectedIndex(){
		Integer i = lstbx.getSelectionModel().getSelection();
		iSelectionIndex = i==null ? -1 : i;
	}
	
	protected int getDumpAreaSelectedIndex(){
		return iSelectionIndex;
	}
	
	protected void updateCopyFrom(){
		int i = getDumpAreaSelectedIndex();
		if(i>=0){
			if(iCopyTo>=0){
				if(bKeyShiftIsPressed){
					if(iCopyTo != i){ 	// another entry selected
						iCopyFrom = iCopyTo;
					}
				}
			}
			
			iCopyTo = i;
		}
	}
	
	protected void lineWrapDisableDumpArea(){
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
		
		lineWrapDisableDumpArea();
	}
	
//	protected BitmapFont retrieveBitmapFontFor(Panel pnl){
//		return retrieveBitmapTextFor(pnl).getFont();
//	}
//	
//protected Float retrieveBitmapTextHeightFor(Panel pnl){
//for(Spatial c : pnl.getChildren()){
//	if(c instanceof BitmapText){
//		BitmapText bt = (BitmapText)c;
//		return bt.getLineHeight();
////		return bt.getHeight();
//	}
//}
//return null;
//}
//	
	
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
				strHintCmd=strCommandPrefixChar+extractCommandPart(strHintCmd,0)+" ";
				if(!getInputText().equals(strHintCmd)){
					tfInput.setText(strHintCmd);
					return true;
				}
			}
		}
		
		return actionSubmitCommand(strCmd);
	}
	
	protected void clearInputTextField() {
		tfInput.setText(strCommandPrefixChar);
	}
	
	protected String getInputText() {
		return tfInput.getText();
	}
	
	public boolean actionSubmitCommand(final String strCmd){
		if(strCmd.isEmpty() || strCmd.trim().equals(strCommandPrefixChar)){
			clearInputTextField(); 
			return false;
		}
		
		String strType=strTypeCmd;
		boolean bIsCmd=true;
		boolean bShowInfo=true;
		if(strCmd.trim().startsWith(strCommentPrefixChar)){
			strType="Cmt";
			bIsCmd=false;
		}else
		if(!strCommandPrefixChar.isEmpty() && !strCmd.trim().startsWith(strCommandPrefixChar)){
			strType="Inv";
			bIsCmd=false;
		}
		
		if(bIsCmd){
			if(strCmd.trim().endsWith(strCommentPrefixChar)){
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
		addToExecConsoleCommandQueue(astrCmdList,false);
	}
	/**
	 * 
	 * @param astrCmdList
	 * @param bPrepend will append if false
	 */
	protected void addToExecConsoleCommandQueue(ArrayList<String> astrCmdList, boolean bPrepend){
		if(bPrepend){
			astrCmdList = new ArrayList<String>(astrCmdList);
			Collections.reverse(astrCmdList);;
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
		
		if(strFullCmdLine.startsWith(strCommentPrefixChar))return;
		if(strFullCmdLine.isEmpty())return;
		if(strFullCmdLine.equals(strCommandPrefixChar))return;
		
		if(!strFullCmdLine.startsWith(strCommandPrefixChar)){
			strFullCmdLine=strCommandPrefixChar+strFullCmdLine;
		}
		
		dumpDevInfoEntry("CmdQueued:["+strFullCmdLine+"]");
		
		if(bPrepend){
			astrExecConsoleCmdsQueue.add(0,strFullCmdLine);
		}else{
			astrExecConsoleCmdsQueue.add(strFullCmdLine);
		}
	}
	
	protected void updateExecConsoleCmdQueue() {
		if(astrExecConsoleCmdsQueue.size()>0){ // one per time! NO while here!!!!
			String str=astrExecConsoleCmdsQueue.remove(0);
			if(!str.trim().endsWith(strCommentPrefixChar)){
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
			showHelp(extractCommandPart(strFullCmdLine,0));
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
	
	protected String getSimpleTime(){
		return "["+new SimpleDateFormat("HH:mm:ss"+(cc.btgShowMiliseconds.get()?".SSS":"")).format(Calendar.getInstance().getTime())+"]";
	}
	
	protected void dumpEntry(String strLineOriginal){
		dumpEntry(true, true, strLineOriginal);
	}
	/**
	 * Simplest dump entry method, but still provides line-wrap.
	 * 
	 * @param bDump if false, will only log to file
	 * @param strLineOriginal
	 */
	protected void dumpEntry(boolean bApplyNewLineRequests, boolean bDump, String strLineOriginal){
		if(bDump){
			if(iConsoleMaxWidthInCharsForLineWrap!=null){
				ArrayList<String> astr = new ArrayList<String>();
				if(strLineOriginal.isEmpty()){
					astr.add(strLineOriginal);
				}else{
					String strLine = strLineOriginal.replace("\t", strReplaceTAB);
					strLine=strLine.replace("\r", ""); //removes carriage return
					
					if(bApplyNewLineRequests){
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
					vlstrDumpEntries.add(strPart);
				}
			}else{
				vlstrDumpEntries.add(strLineOriginal);
			}
			
			while(vlstrDumpEntries.size() > iMaxDumpEntriesAmount){
				vlstrDumpEntries.remove(0);
			}
		}
		
		dumpSave(strLineOriginal);
	}
	
	protected void dumpInfoEntry(String str){
		dumpEntry(false, cc.btgShowInfo.get(), getSimpleTime()+strInfoEntryPrefix+str);
	}
	
	protected void dumpWarnEntry(String str){
		dumpEntry(false, cc.btgShowWarn.get(), getSimpleTime()+strWarnEntryPrefix+str);
	}
	
	/**
	 * warnings that should not bother end users...
	 * @param str
	 */
	protected void dumpDevWarnEntry(String str){
		dumpEntry(false, cc.btgShowDeveloperWarn.get(), getSimpleTime()+strDevWarnEntryPrefix+str);
	}
	
	protected void dumpDevInfoEntry(String str){
		dumpEntry(false, cc.btgShowDeveloperInfo.get(), getSimpleTime()+strDevInfoEntryPrefix+str);
	}
	
	protected void dumpExceptionEntry(Exception e){
		dumpEntry(false, cc.btgShowException.get(), getSimpleTime()+strExceptionEntryPrefix+e.toString());
		e.printStackTrace();
	}
	
	/**
	 * a simple, usually indented, output
	 * @param str
	 */
	protected void dumpSubEntry(String str){
		dumpEntry(strSubEntryPrefix+str);
	}
	
	public void addCmdToValidList(String str){
		if(!astrCmdCmtValidList.contains(str)){
			if(!str.startsWith(TOKEN_CMD_NOT_WORKING_YET)){
				astrCmdCmtValidList.add(str);
//				astrBaseCmdValidList.add(str.split("[^"+strValidCmdCharsRegex +"]")[0]);
				astrBaseCmdValidList.add(extractCommandPart(str,0));
			}
		}
	}
	
	protected boolean isCommentedLine(){
		if(strCmdLinePrepared==null)return false;
		return strCmdLinePrepared.trim().startsWith(strCommentPrefixChar);
	}
	
	protected boolean checkCmdValidityBoolTogglers(){
		btgReferenceMatched=null;
		for(BoolToggler btg : BoolToggler.getBoolTogglerListCopy()){
			if(checkCmdValidity(btg.getCmdId(), "[bEnable]")){
				btgReferenceMatched = btg;
				break;
			}
		}
		return btgReferenceMatched!=null;
	}
	protected boolean checkCmdValidity(String strValidCmd){
		return checkCmdValidity(strValidCmd, null);
	}
	protected boolean checkCmdValidity(StringField strfValidCmd, String strComment){
		return checkCmdValidity(strfValidCmd.toString(), strComment);
	}
	protected boolean checkCmdValidity(String strValidCmd, String strComment){
		if(strCmdLinePrepared==null){
			if(strComment!=null){
				strValidCmd+=" "+strCommentPrefixChar+strComment;
			}
			
			addCmdToValidList(strValidCmd);
			
			return false;
		}
		
		if(TOKEN_MULTI_COMMAND_LINE.equals(strCmdLinePrepared))return false;
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
			if(strParam1!=null){
				if(cc.CMD_CONSOLE_STYLE.equals(strBaseCmd)){
					strCompletedCmd=strCommandPrefixChar+
						cc.CMD_CONSOLE_STYLE+" "
						+autoComplete(strParam1, astrStyleList, bMatchContains).get(0);
				}
			}
		}
		
		if(strCompletedCmd.trim().isEmpty())strCompletedCmd=strCommandPrefixChar;
		tfInput.setText(strCompletedCmd);
		
		scrollToBottomRequest();
	}
	
	protected String autoCompleteWork(String strCmdPart, boolean bMatchContains){
		String strCmdPartOriginal = strCmdPart;
		strCmdPart=strCmdPart.trim();
		
		// no command typed
		if(strCmdPart.equalsIgnoreCase(strCommandPrefixChar) || strCmdPart.isEmpty())
			return strCmdPartOriginal;
		
		strCmdPart=strCmdPart.replaceFirst("^"+strCommandPrefixChar, "");
		
		// do not allow invalid chars
		if(!strCmdPart.matches("["+strValidCmdCharsRegex+"]*"))
			return strCmdPartOriginal;
		
		ArrayList<String> astr = autoComplete(strCmdPart, astrCmdCmtValidList, bMatchContains);
		String strFirst=astr.get(0);
		String strAppendSpace = "";
		if(astr.size()==1 && strFirst.length() > strCmdPart.length()){
			strAppendSpace=" "; //found an exact command valid match, so add space
		}
		
		// many possible matches
		if(astr.size()>1){
			dumpInfoEntry("AutoComplete: ");
			for(String str:astr){
				if(str.equals(strFirst))continue; //skip the partial improved match, 1st entry
				dumpSubEntry(strCommandPrefixChar+str);
			}
		}
		
		return strCommandPrefixChar+strFirst.split(" ")[0]+strAppendSpace;
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
		int iCommentAt = strFullCmdLine.indexOf(strCommentPrefixChar);
		if(iCommentAt>=0){
			strFullCmdLine=strFullCmdLine.substring(0,iCommentAt);
		}
		
		/**
		 * queue multicommands line
		 */
		if(strFullCmdLine.contains(strCommandDelimiter)){
			ArrayList<String> astrMulti = new ArrayList<String>();
			astrMulti.addAll(Arrays.asList(strFullCmdLine.split(strCommandDelimiter)));
			for(int i=0;i<astrMulti.size();i++){
				astrMulti.set(i,astrMulti.get(i)+strCommentPrefixChar+i);
			}
			
//			strFullCmdLine=null;
			addToExecConsoleCommandQueue(astrMulti,true);
//			Collections.reverse(astrMulti);
//			for(String strCmd : astrMulti){
//				addToExecConsoleCommandQueue(strCmd);
////				if(strFullCmdLine==null){
////					strFullCmdLine = strCmd;
////				}else{
////					addToExecConsoleCommandQueue(strCmd);
////				}
//			}
			return TOKEN_MULTI_COMMAND_LINE;
		}
		
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(strFullCmdLine);
		while (m.find()){
			String str=m.group(1);
			if(str!=null){
				if(str.trim().startsWith(strCommentPrefixChar))break; //ignore comments
				str=str.trim();
				str=str.replace("\"", ""); //remove all quotes on the string TODO could be only 1st and last? mmm... too much trouble...
//				if(str.startsWith("\"") && str.endsWith("\"")){
//					str=str.replace("\"", ""); //remove all quotes on the string TODO could be only 1st and last? mmm... too much trouble...
//				}else{
////					str=str.replace(strCommandDelimiter, " ;"); //good to help on splitting multi-commands line
//					if(str.contains(strCommandDelimiter)){
//						for(String strPart:str.split(strCommandDelimiter)){
//							astr.add(strPart+strCommandDelimiter);
//						}
////						astr.addAll(Arrays.asList(str.split(strCommandDelimiter)));
//						str = null;
//					}
//				}
//				if(str!=null)astr.add(str);
				astrCmdAndParams.add(str);
			}
		}
		
		return strFullCmdLine;
	}
	
//	protected ArrayList<String> splitMultiCommandsLine(ArrayList<String> astr){
//		dumpDevInfoEntry("splitting:"+astr);
//		
//		ArrayList<String> astrNew = new ArrayList<String>();
//		ArrayList<String> astrMulti = new ArrayList<String>();
//		boolean bMultiCommand=false;
//		for(String strParam : astr){
//			if(!bMultiCommand){
//				/**
//				 * commands MUST NEVER have spaces
//				 * so trim() is only applied if it is a command
//				 */
//				if(
//						strParam.trim().startsWith(strCommandDelimiter)
//						||
//						strParam.trim().endsWith(strCommandDelimiter)
//				){
//					// remove first delimiter only one only
//					strParam=strParam.trim().substring(strCommandDelimiter.length());
//					bMultiCommand=true;
//				}
//			}
//			
//			if(bMultiCommand){
//				astrMulti.add(strParam);
//			}else{
//				astrNew.add(strParam);
//			}
//		}
//		
//		if(astrMulti.size()>0){
//			addToExecConsoleCommandQueue("\""+String.join("\" \"", astrMulti)+"\"");
//		}
//		
//		return astrNew;
//	}
	
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
		if(astrCmdAndParams.size()>iIndex){
			return astrCmdAndParams.get(iIndex);
		}
		return null;
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
	
//	/**
//	 * 
//	 * @param bToggling
//	 * @return null means toggle force param evaluation failed
//	 */
//	protected Boolean executeCommandToggleHelper(boolean bToggling){
//		if(paramBooleanCheckForToggle(1)){
//			Boolean bEnable = paramBoolean(1);
//			bToggling = bEnable==null ? !bToggling : bEnable; //override
//			return bToggling;
//		}
//		return null;
//	}
	
	protected String prepareCmdAndParams(String strFullCmdLine){
		if(strFullCmdLine!=null){
			strFullCmdLine = strFullCmdLine.trim();
			
			if(strFullCmdLine.isEmpty())return null; //dummy line
			
			// a comment shall not create any warning based on false return value...
			if(strFullCmdLine.startsWith(strCommentPrefixChar))return null; //comment is a "dummy command"
			
			// now it is possibly a command
			
			strFullCmdLine = strFullCmdLine.substring(strCommandPrefixChar.length());
			strFullCmdLine = strFullCmdLine.trim();
			
			if(strFullCmdLine.endsWith(strCommentPrefixChar)){
				strFullCmdLine=strFullCmdLine.substring(0,strFullCmdLine.length()-strCommentPrefixChar.length());
			}
			
			return convertToCmdParams(strFullCmdLine);
		}
		
		return null;
	}
	
	public String getPreparedCmdLine(){
		return strCmdLinePrepared;
	}
	
	public class Alias{
		String strAliasId;
		String strCmdLine; // may contain ending comment too
		boolean	bBlocked;
		@Override
		public String toString() {
			return strCommandPrefixChar+"alias "
					+(bBlocked?"[BLOCKED]":"")
					+strAliasId+" "+strCmdLine;
		}
	}
	
	protected boolean checkAlias(){
		bLastAliasCreatedSuccessfuly=false;
		if(strCmdLineOriginal==null)return false;
		String str = strCmdLineOriginal.trim();
		String strExecAliasPrefix = strCommandPrefixChar+strAliasPrefix;
		if(str.startsWith(strCommandPrefixChar+"alias ")){
			/**
			 * create
			 */
			Alias alias = new Alias();
			
			String[] astr = str.split(" ");
			if(astr.length>=3){
				alias.strAliasId=astr[1];
				alias.strCmdLine=String.join(" ", Arrays.copyOfRange(astr, 2, astr.length));
				
				Alias aliasFound=null;
				for(Alias aliasCheck : aAliasList){
					if(aliasCheck.strAliasId.toLowerCase().equals(alias.strAliasId.toLowerCase())){
						aliasFound = aliasCheck;
						break;
					}
				}
				
				if(aliasFound!=null)aAliasList.remove(aliasFound);
				
				aAliasList.add(alias);
				
				bLastAliasCreatedSuccessfuly = true;
			}
		}else
		if(str.startsWith(strExecAliasPrefix)){
			/**
			 * execute
			 */
			str=str.split(" ")[0].substring(strExecAliasPrefix.length()).toLowerCase();
			for(Alias alias:aAliasList){
				if(alias.strAliasId.toLowerCase().equals(str)){
					if(!alias.bBlocked){
						addToExecConsoleCommandQueue(alias.strCmdLine, true);
						return true;
					}else{
						dumpWarnEntry(alias.toString());
					}
				}
			}
		}
			
		return bLastAliasCreatedSuccessfuly;
	}
	
	protected boolean executePreparedCommand(){
		boolean bOk = false;
		
		if(checkCmdValidityBoolTogglers()){
			bOk=toggle(btgReferenceMatched);
		}else
		if(checkCmdValidity("alias","[<[+|-|~]identifier>] [commands]\n"
				+"\t\tWithout params, will list all aliases\n"
				+"\t\t~filter - will filter (contains) the alias list\n"
				+"\t\t-identifier - will block that alias execution\n"
				+"\t\t+identifier - will un-block that alias execution\n"
				+"\t\tObs.: to execute an alias prefix the identifier with '"+strAliasPrefix+"', ex.: "+strCommandPrefixChar+strAliasPrefix +"tst123")
		){
			String strAliasId = paramString(1);
			if(strAliasId!=null && strAliasId.startsWith("+")){
				bOk=aliasBlock(strAliasId.substring(1),false);
			}else
			if(strAliasId!=null && strAliasId.startsWith("-")){
				bOk=aliasBlock(strAliasId.substring(1),true);
			}else{
				String strFilter=null;
				if(strAliasId!=null && strAliasId.startsWith("~")){
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
					bOk=true;
				}else{
					bOk=bLastAliasCreatedSuccessfuly;
				}
			}
		}else
//		if(checkCmdValidity("aliasBlock","<identifier> all nexts attempts to run the alias will be blocked")){
//			bOk=aliasBlock(paramString(1),true);
//		}else
//		if(checkCmdValidity("aliasList","[strFilter] list available aliases")){
//			String strFilter = paramString(1);
//			for(Alias alias:aAliasList){
//				if(strFilter!=null && !alias.toString().toLowerCase().contains(strFilter))continue;
//				dumpSubEntry(alias.toString());
//			}
//			bOk=true;
//		}else
//		if(checkCmdValidity("aliasUnblock","<identifier>")){
//			bOk=aliasBlock(paramString(1),false);
//		}else
		if(checkCmdValidity("clearCommandsHistory")){
			astrCmdHistory.clear();
			bOk=true;
		}else
		if(checkCmdValidity("clearDumpArea")){
			vlstrDumpEntries.clear();
			bOk=true;
		}else
		if(checkCmdValidity(cc.CMD_CLOSE_CONSOLE,"like the bound key to do it")){
			setEnabled(false);
			bOk=true;
		}else
		if(checkCmdValidity(cc.CMD_CONSOLE_STYLE,"[strStyleName] changes the style of the console on the fly, empty for a list")){
			String strStyle = paramString(1);
			if(strStyle==null)strStyle="";
			bOk=styleApply(strStyle);
		}else
		if(checkCmdValidity(cc.CMD_ECHO," simply echo something")){
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
			bOk=true;
		}else
//		if(checkCmdValidity(cc.btgEngineStatsView.getCmdId(),"[bEnable] messages")){
//			bOk = toggle(cc.btgEngineStatsView);
//		}else
//		if(checkCmdValidity(cc.btgEngineStatsFps.getCmdId(),"[bEnable] messages")){
//			bOk = toggle(cc.btgEngineStatsFps);
//		}else
		if(checkCmdValidity("execBatchCmdsFromFile ","<strFileName>")){
			String strFile = paramString(1);
			if(strFile!=null){
				astrExecConsoleCmdsQueue.addAll(fileLoad(strFile));
				bOk=true;
			}
		}else
		if(checkCmdValidity("exit ","the application")){
			exit();
			bOk=true;
		}else
		if(checkCmdValidity(cc.CMD_FIX_CURSOR ,"in case cursor is invisible")){
			if(efHK==null){
				dumpWarnEntry("requires command: "+cc.CMD_HK_TOGGLE);
			}else{
				dumpInfoEntry("requesting: "+cc.CMD_FIX_CURSOR);
				efHK.bFixInvisibleTextInputCursorHK=true;
			}
			bOk = true;
		}else
		if(checkCmdValidity(cc.CMD_FIX_LINE_WRAP ,"in case words are overlapping")){
			lineWrapDisableDumpArea();
			bOk = true;
		}else
		if(checkCmdValidity("fixVisibleRowsAmount ","[iAmount] in case it is not showing as many rows as it should")){
			iVisibleRowsAdjustRequest = paramInt(1);
			if(iVisibleRowsAdjustRequest==null)iVisibleRowsAdjustRequest=0;
			bOk=true;
		}else
		if(checkCmdValidity(cc.CMD_HELP,"[strFilter] show (filtered) available commands")){
			showHelp(paramString(1));
			bOk=true; //ALWAYS TRUE, to avoid infinite loop when improving some failed command help info!
		}else
		if(checkCmdValidity(cc.CMD_HISTORY,"[strFilter] of issued commands (the filter results in sorted uniques)")){
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
				dumpSubEntry(str);
			}
			
			bOk=true;
		}else
		if(checkCmdValidity("showBinds","")){
			dumpInfoEntry("Key bindings: ");
			dumpSubEntry("Ctrl+B - marks dump area begin selection marker for copy");
			dumpSubEntry("Ctrl+Del - clear input field");
			dumpSubEntry("TAB - autocomplete (starting with)");
			dumpSubEntry("Ctrl+TAB - autocomplete (contains)");
			dumpSubEntry("Ctrl+/ - toggle input field comment");
			bOk=true;
		}else
		if(checkCmdValidity("initFileShow ","show contents of init file at dump area")){
			dumpInfoEntry("Init file data: ");
			for(String str : fileLoad(flInit)){
				dumpSubEntry(str);
			}
			bOk=true;
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
				
				bOk=true;
			}
		}else
		if(checkCmdValidity(cc.CMD_LINE_WRAP_AT,"[iMaxChars] -1 = will trunc big lines, 0 = wrap will be automatic")){
			iConsoleMaxWidthInCharsForLineWrap = paramInt(1);
			if(iConsoleMaxWidthInCharsForLineWrap!=null){
				if(iConsoleMaxWidthInCharsForLineWrap==-1){
					iConsoleMaxWidthInCharsForLineWrap=null;
				}
			}
			updateWrapAt();
			bOk=true;
		}else
		if(checkCmdValidity(cc.CMD_CONSOLE_HEIGHT,"[fPercent] of the application window")){
			Float f = paramFloat(1);
			modifyConsoleHeight(f);
			bOk=true;
		}else
		if(checkCmdValidity("quit ","the application")){
			exit();
			bOk=true;
		}else
		if(checkCmdValidity(cc.CMD_CONSOLE_SCROLL_BOTTOM,"")){
			scrollToBottomRequest();
			bOk=true;
		}else
		if(checkCmdValidity("set","<varId> <value> can be a number or a string, use as: $varId")){
			ahkVariables.put(paramString(1),paramString(2));
			bOk=true;
		}else
//		if(checkCmdValidity(cc.btgShowDeveloperInfo.getCmdId(),"[bEnable] messages")){
//			bOk = toggle(cc.btgShowDeveloperInfo);
//		}else
//		if(checkCmdValidity(cc.btgShowDeveloperWarn.getCmdId(),"[bEnable] messages")){
//			bOk = toggle(cc.btgShowDeveloperWarn);
//		}else
//		if(checkCmdValidity(cc.btgShowExecQueuedInfo.getCmdId(),"[bEnable] messages")){
//			bOk = toggle(cc.btgShowExecQueuedInfo);
//		}else
//		if(checkCmdValidity(cc.btgShowInfo.getCmdId(),"[bEnable] messages")){
//			bOk = toggle(cc.btgShowInfo);
//		}else
//		if(checkCmdValidity(cc.btgShowWarn.getCmdId(),"[bEnable] messages")){
//			bOk = toggle(cc.btgShowWarn);
//		}else
//		if(checkCmdValidity(cc.btgShowException.getCmdId(),"[bEnable] messages")){
//			bOk = toggle(cc.btgShowException);
//		}else
		if(checkCmdValidity("statsShowAll ","show all console stats")){
			dumpAllStats();
			bOk=true;
		}else
		if(checkCmdValidity("statsFieldToggle ","[bEnable] toggle simple stats field visibility")){
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
				
				bOk=true;
			}
		}else
		if(checkCmdValidity("test ","[...] temporary developer tests")){
			test();
			if(efHK!=null)efHK.test();
			bOk=true;
		}else
		if(checkCmdValidity(TOKEN_CMD_NOT_WORKING_YET+"zDisabledCommand"," just to show how to use it")){
			// keep this as reference
		}else{
			if(strCmdLinePrepared!=null){
				if(TOKEN_MULTI_COMMAND_LINE.equals(strCmdLinePrepared)){
					bOk=true;
				}
			}
		}
		
		return bOk;
	}
	
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
	protected void showHelp(String strFilter) {
		if(strFilter==null){
			dumpInfoEntry("Available Commands:");
		}else{
			dumpInfoEntry("Help for '"+strFilter+"':");
		}
		
		Collections.sort(astrCmdCmtValidList,String.CASE_INSENSITIVE_ORDER);
		for(String str:astrCmdCmtValidList){
			if(strFilter!=null && !str.toLowerCase().contains(strFilter.toLowerCase()))continue;
			dumpSubEntry(strCommandPrefixChar+str);
		}
	}
	
	/**
	 * Command format: "commandIdentifier any comments as you wish"
	 * @param strFullCmdLineOriginal if null will populate the array of valid commands
	 * @return false if command execution failed
	 */
	protected boolean executeCommand(final String strFullCmdLineOriginal){
		boolean bOk = false;
		this.strCmdLineOriginal = strFullCmdLineOriginal;
		try{
			bOk=checkAlias();
			if(!bOk){
				strCmdLinePrepared = prepareCmdAndParams(strFullCmdLineOriginal);
				bOk = executePreparedCommand();
			}
		}catch(NumberFormatException e){
			// keep this one as "warning", as user may simply fix the typed value
			dumpWarnEntry("NumberFormatException: "+e.getMessage());
			e.printStackTrace();
			bOk=false;
		}
		
		return bOk;
	}
	
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
	
	protected boolean styleApply(String strStyleNew) {
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
	
	protected void exit(){
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
	
	public String getCommentPrefixChar() {
		return strCommentPrefixChar;
	}
	public void setCfgCommentPrefixChar(String strCommentPrefixChar) {
		this.strCommentPrefixChar = strCommentPrefixChar;
	}
	public String getCommandPrefixChar() {
		return strCommandPrefixChar;
	}
	public void setCfgCommandPrefixChar(String strCommandPrefixChar) {
		this.strCommandPrefixChar = strCommandPrefixChar;
	}
	
//	protected String fmtFloat(Float f,int iDecimalPlaces){
//		return fmtFloat(f==null?null:f.doubleValue(),iDecimalPlaces);
//	}
	protected String fmtFloat(double d){
		return fmtFloat(d,-1);
	}
	protected String fmtFloat(Double d,int iDecimalPlaces){
		if(iDecimalPlaces==-1)iDecimalPlaces=2;
		return d==null?"null":String.format("%."+iDecimalPlaces+"f", d);
	}
	
	protected void dumpAllStats(){
		dumpInfoEntry("Console stats: ");
		
		dumpSubEntry("Console Height = "+fmtFloat(ctnrConsole.getSize().y));
		
		dumpSubEntry("Visible Rows = "+lstbx.getGridPanel().getVisibleRows());
		dumpSubEntry("Line Wrap At = "+iConsoleMaxWidthInCharsForLineWrap);
		dumpSubEntry("ListBox Height = "+fmtFloat(lstbx.getSize().y));
		dumpSubEntry("ListBox Entry Height = "+fmtFloat(fLstbxEntryHeight));
		
		for(BoolToggler bh : BoolToggler.getBoolTogglerListCopy()){
			dumpSubEntry(bh.getCmdId()+" = "+bh.get());
		}
		
		dumpSubEntry("Stats Text Field Height = "+fmtFloat(fStatsHeight));
		dumpSubEntry("Stats Container Height = "+fmtFloat(ctnrStatsAndControls.getSize().y));
		
		dumpSubEntry("Input Field Height = "+fmtFloat(fInputHeight));
		dumpSubEntry("Input Field Final Height = "+fmtFloat(tfInput.getSize().y));
		
		dumpSubEntry("Slider Value = "+fmtFloat(getScrollDumpAreaFlindex()));
		
		dumpSubEntry("Slider Scroll request max retry attempts = "+iScrollRetryAttemptsMaxDBG);
		
		dumpSubEntry("Commands History File = "+flCmdHist.getAbsolutePath());
		dumpSubEntry("Config File = "+flInit.getAbsolutePath());
		dumpSubEntry("Dump File = "+flLastDump.getAbsolutePath());
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
//		double dSliderHumanReadableValue = getScrollDumpAreaIndex();
//			vlstrDumpEntries.size() 
//			-getScrollDumpAreaIndex()
//			-lstbx.getGridPanel().getVisibleRows();
//		if(dSliderHumanReadableValue<0)dSliderHumanReadableValue=0;
		int iMaxSliderIndex=vlstrDumpEntries.size()-lstbx.getGridPanel().getVisibleRows();
		
		strStatsLast = ""
				// user important
				+"CpFrom"+iCopyFrom
					+"to"+getDumpAreaSelectedIndex()
					+","
				
				+"Hst"+iCmdHistoryCurrentIndex+"/"+(astrCmdHistory.size()-1)
					+","
				
//				+"Tot"+vlstrDumpEntries.size()
//					+","
					
				/**
				 * KEEP HERE AS REFERENCE!
				 * IMPORTANT, DO NOT USE
				 * clipboard reading is too heavy...
				+"Cpbd='"+retrieveClipboardString()+"', "
				 */
					
				// less important (mainly for debug)
				+"Slider"
//				+fmtFloat(dSliderHumanReadableValue,0)+"/"+vlstrDumpEntries.size()
					+fmtFloat(getScrollDumpAreaFlindex(),0)+"/"+iMaxSliderIndex+"+"+lstbx.getGridPanel().getVisibleRows()
//					+":"
					+"("+fmtFloat(100.0f -lstbx.getSlider().getModel().getPercent()*100f,0)+"%)"
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
		ReflexFillCfg rfcfg = new ReflexFillCfg();
		if(rfcv.getClass().isAssignableFrom(StringField.class)){
			rfcfg.strCodingStyleFieldNamePrefix = "INPUT_MAPPING_CONSOLE_";
			rfcfg.strCommandPrefix = IMCPREFIX;
			rfcfg.bFirstLetterUpperCase = true;
		}
		
		return rfcfg;
	}
}

