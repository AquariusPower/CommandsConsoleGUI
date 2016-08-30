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

package com.github.commandsconsolegui.jmegui.console;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.regex.Pattern;

import truetypefont.TrueTypeBitmapGlyph;
import truetypefont.TrueTypeFont;
import truetypefont.TrueTypeKey;
import truetypefont.TrueTypeLoader;

import com.github.commandsconsolegui.cmd.CommandData;
import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.DumpEntryData;
import com.github.commandsconsolegui.cmd.EDataBaseOperations;
import com.github.commandsconsolegui.cmd.IConsoleUI;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.ConditionalStateManagerI;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurMiscHelpersStateI;
//import com.github.commandsconsolegui.console.gui.lemur.LemurMiscHelpersState;
import com.github.commandsconsolegui.misc.AutoCompleteI;
import com.github.commandsconsolegui.misc.AutoCompleteI.AutoCompleteResult;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.DebugI;
import com.github.commandsconsolegui.misc.DebugI.EDbgKey;
import com.github.commandsconsolegui.misc.IWorkAroundBugFix;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.StatsAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Texture2D;

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
public abstract class ConsoleStateAbs<T,R extends ConsoleStateAbs<T,R>> extends BaseDialogStateAbs<T,R> implements IConsoleUI, IWorkAroundBugFix {
//	private FpsLimiterState fpslState = new FpsLimiterState();
	
//	ReattachSafelyState rss;
	
	//	private final String strInputIMCPREFIX = "CONSOLEGUISTATE_";
	public final String strFinalFieldInputCodePrefix="INPUT_MAPPING_CONSOLE_";
	public final StringCmdField INPUT_MAPPING_CONSOLE_TOGGLE = new StringCmdField(this,strFinalFieldInputCodePrefix);
	public final StringCmdField INPUT_MAPPING_CONSOLE_SCROLL_UP = new StringCmdField(this,strFinalFieldInputCodePrefix);
	public final StringCmdField INPUT_MAPPING_CONSOLE_SCROLL_DOWN = new StringCmdField(this,strFinalFieldInputCodePrefix);
	public final StringCmdField INPUT_MAPPING_CONSOLE_SHIFT_PRESSED	= new StringCmdField(this,strFinalFieldInputCodePrefix);
	public final StringCmdField INPUT_MAPPING_CONSOLE_CONTROL_PRESSED	= new StringCmdField(this,strFinalFieldInputCodePrefix);
	
	public final String STYLE_CONSOLE="console";
	
//	private boolean bStartupCmdQueueDone = false; 
	
	/**
	 * commands user can type
	 */
	public final StringCmdField CMD_CLOSE_CONSOLE = new StringCmdField(this,CommandsDelegator.strFinalCmdCodePrefix);
	public final StringCmdField CMD_CONSOLE_HEIGHT = new StringCmdField(this,CommandsDelegator.strFinalCmdCodePrefix);
	public final StringCmdField CMD_CONSOLE_STYLE = new StringCmdField(this,CommandsDelegator.strFinalCmdCodePrefix);
	public final StringCmdField CMD_DEFAULT = new StringCmdField(this,CommandsDelegator.strFinalCmdCodePrefix);
	public final StringCmdField CMD_FONT_LIST = new StringCmdField(this,CommandsDelegator.strFinalCmdCodePrefix);
	
	private String strDefaultFont = "DroidSansMono";
	private StringVarField	svfUserFontOption = new StringVarField(this, strDefaultFont, null);
	private int iDefaultFontSize = 12;
	private IntLongVarField ilvFontSize = new IntLongVarField(this, iDefaultFontSize,null);
	
//	/**
//	 * keep "initialized" vars together!
//	 */
//	private boolean	bInitialized;
	
	/**
	 * keep delayers together!
	 */
	private TimedDelayVarField tdStatsRefresh = new TimedDelayVarField(this,0.5f,"delay between simple stats field refresh");
	private TimedDelayVarField tdScrollToBottomRequestAndSuspend = new TimedDelayVarField(this,0.5f,null);
	private TimedDelayVarField tdScrollToBottomRetry = new TimedDelayVarField(this,0.1f,null);

//	private TimedDelay tdLetCpuRest = new TimedDelay(0.1f);
//	private TimedDelay tdStatsRefresh = new TimedDelay(0.5f);
//	private TimedDelay tdDumpQueuedEntry = new TimedDelay(1f/5f); // per second
//	private TimedDelay tdSpareGpuFan = new TimedDelay(1.0f/60f); // like 60 FPS
	
	/**
	 * keep guesses together!
	 * guesses should not exist... 
	 * they are imprecise, they just "work" therefore they may "just break"... 
	 * TODO better algorithms/calculations are required...
	 */
	private int	iJumpBackGUESSED = 1;
	private int	iDotsMarginSafetyGUESSED = 0;
	private int	iSkipCharsSafetyGUESSED = 1;
	private float	fSafetyMarginGUESSED = 20f;
	
//	/**
//	 * some day this one may not be required
//	 */
//	private LemurGuiExtraFunctionalitiesHK efHK = null;
//	public LemurGuiExtraFunctionalitiesHK getLemurHK(){return efHK;}
//	public void initLemurHK(){efHK=new LemurGuiExtraFunctionalitiesHK(this);};
	
	/**
	 * other vars!
	 */
//	private Node	ctnrMainTopSubWindow;
	private Node	lstbxDumpArea;
//	private Node	intputField;
//	private TextField tfAutoCompleteHint;
//	private SimpleApplication	sapp;
//	private boolean	bEnabled;
	private AbstractList<String> vlstrDumpEntriesSlowedQueue;
	private AbstractList<String> vlstrDumpEntries;
	private AbstractList<String> vlstrAutoCompleteHint;
	private Node lstbxAutoCompleteHint;
	private int	iShowRows = 1;
//	private Integer	iToggleConsoleKey = null;
	private Integer	iVisibleRowsAdjustRequest = 0; //0 means dynamic
//	private int	iCmdHistoryCurrentIndex = 0;
//	private String	strInfoEntryPrefix			=". ";
//	private String	strWarnEntryPrefix			="?Warn: ";
//	private String	strErrorEntryPrefix			="!ERROR: ";
//	private String	strExceptionEntryPrefix	="!EXCEPTION: ";
//	private String	strDevWarnEntryPrefix="?DevWarn: ";
//	private String	strDevInfoEntryPrefix=". DevInfo: ";
//	private String	strSubEntryPrefix="\t";
	private boolean	bInitiallyClosedOnce = true;
//	private ArrayList<String> astrCmdHistory = new ArrayList<String>();
//	private ArrayList<String> astrCmdWithCmtValidList = new ArrayList<String>();
//	private ArrayList<String> astrBaseCmdValidList = new ArrayList<String>();
	private ArrayList<String> astrStyleList = new ArrayList<String>();
//	private int	iCmdHistoryCurrentIndex = 0;
	private	String	strNotSubmitedCmd=null; //null is a marker here
//	private Panel	pnlTest;
//	private String	strTypeCmd="Cmd";
//	private Label	lblStats;
	private Float	fLstbxEntryHeight;
	private Float	fStatsHeight;
	private Float	fInputHeight;
	private int	iScrollRetryAttemptsDBG;
	private int	iScrollRetryAttemptsMaxDBG;
//	private int iMaxCmdHistSize = 1000;
//	private int iMaxDumpEntriesAmount = 100000;
//	private String	strFilePrefix = "Console"; //ConsoleStateAbs.class.getSimpleName();
//	private String	strFileTypeLog = "log";
//	private String	strFileTypeConfig = "cfg";
//	private String	strFileCmdHistory = strFilePrefix+"-CmdHist";
//	private String	strFileLastDump = strFilePrefix+"-LastDump";
//	private String	strFileInitConsCmds = strFilePrefix+"-Init";
//	private String	strFileSetup = strFilePrefix+"-Setup";
//	private String	strFileDatabase = strFilePrefix+"-DB";
	private FloatDoubleVarField	fdvConsoleHeightPercDefault = new FloatDoubleVarField(this,0.5,"percentual of the application window");
	
	/**
	 * use the console height command to set this var, do not use a console variable for it,
	 * mainly because it is already implemented that way and working well...
	 */
	private float	fConsoleHeightPerc = fdvConsoleHeightPercDefault.getFloat();
//	private ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
//	private ArrayList<String>	astrExecConsoleCmdsQueue = new ArrayList<String>();
//	private ArrayList<PreQueueCmdsBlockSubList>	astrExecConsoleCmdsPreQueue = new ArrayList<PreQueueCmdsBlockSubList>();
//	private File	flCmdHist;
//	private File	flLastDump;
//	private File	flInit;
//	private File	flDB;
//	private File	flSetup;
	private float	fLstbxHeight;
	private int	iSelectionIndex = -1;
	private int	iSelectionIndexPreviousForFill = -1;
	private Double	dMouseMaxScrollBy = null; //max scroll if set
//	private boolean bShowLineIndex = true;
//	private String strStyle = BaseStyles.GLASS;
	private String strStyle = STYLE_CONSOLE;
//	private String strStyle = Styles.ROOT_STYLE;
	private String	strInputTextPrevious = "";
	private AnalogListener	alConsoleScroll;
	private ActionListener	alConsoleToggle;
//	private String	strValidCmdCharsRegex = "A-Za-z0-9_-"+"\\"+strCommandPrefixChar;
//	private String	strValidCmdCharsRegex = "a-zA-Z0-9_"; // better not allow "-" as has other uses like negate number and commands functionalities
//	private String	strStatsLast = "";
	private Node	ctnrStatsAndControls;
	private Vector3f	v3fStatsAndControlsSize;
//	private Button	btnClipboardShow;
	private boolean	bConsoleStyleCreated;
//	private boolean	bUseDumbWrap = true;
//	private Integer	iConsoleMaxWidthInCharsForLineWrap = 0;
	private BitmapFont	fntMakeFixedWidth;
	private StatsAppState	stateStats;
//	private boolean	bEngineStatsFps;
//	private float	fMonofontCharWidth;
//	private GridPanel	gpListboxDumpArea;
//	private int	iCopyFrom = -1;
//	private int	iCopyTo = -1;
//	private Button	btnCopy;
//	private Button	btnPaste;
//	private boolean	bAddEmptyLineAfterCommand = true;
//	private String	strLineEncloseChar = "'";
//	private String	strCmdLinePrepared = "";
//	private CharSequence	strReplaceTAB = "  ";
	private Float	fWidestCharForCurrentStyleFont = null;
	private boolean	bKeyShiftIsPressed;
	private boolean	bKeyControlIsPressed;
	private Vector3f	v3fConsoleSize;
	private Vector3f	v3fApplicationWindowSize;
//	private String	strPreviousCmdHistoryKey;
	private String	strPreviousInputValue;
	private int	iCmdHistoryPreviousIndex;
//	private boolean	bShowExecQueuedInfo = false;
//	private CommandsDelegatorI	cd;
//	private Hashtable<String,Object> htUserVariables = new Hashtable<String,Object>();
//protected Hashtable<String,Object> htRestrictedVariables = new Hashtable<String,Object>();
//	private TreeMap<String,Object> tmUserVariables = 
//		new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
//	private TreeMap<String,Object> tmRestrictedVariables =
//		new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
//	private TreeMap<String,ArrayList<String>> tmFunctions = 
//			new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
//	private ArrayList<Alias> aAliasList = new ArrayList<Alias>();
//	private String	strCmdLineOriginal;
//	private boolean	bLastAliasCreatedSuccessfuly;
//	private float	fTPF;
//	private long	lNanoFrameTime;
//	private long	lNanoFpsLimiterTime;
//	private Boolean	bIfConditionIsValid;
//	private ArrayList<DumpEntry> adeDumpEntryFastQueue = new ArrayList<DumpEntry>();
//	private Button	btnCut;
//	private ArrayList<ConditionalNested> aIfConditionNestedList = new ArrayList<ConditionalNested>();
//	private Boolean	bIfConditionExecCommands;
//	private String	strPrepareFunctionBlockForId;
//	private boolean	bFuncCmdLineRunning;
//	private boolean	bFuncCmdLineSkipTilEnd;
//	private long lLastUniqueId = 0;

//	private ConsoleCursorListener consoleCursorListener;

	private Spatial	sptScrollTarget;
	private Integer	iStatsTextSafeLength = null;
	private boolean	bExceptionOnce = true;
	private boolean	bKeepInitiallyInvisibleUntilFirstClosed = false;
	private boolean	bFullyInitialized = false;
	
//	private FocusManagerState	focusState;
//	private Spatial	sptPreviousFocus;
//	private boolean	bRestorePreviousFocus;
	private boolean	bInitializeOnlyTheUI;
//	private boolean	bConfigured;
	private BitmapFont	font;
	private BitmapFont	fontConsoleDefault;
	private String	strConsoleDefaultFontName = "Console";
	private int	iMargin;
	private Node	sliderDumpArea;
	private BitmapFont	fontConsoleExtraDefault;
//	private boolean	bConfigureSimpleCompleted;

	BoolTogglerCmdField btgBugFixStatsLabelTextSize = 
		new BoolTogglerCmdField(this,false,null,"FIXED: By fixating the size of the label, this crash preventer is not that necesary anymore.");
	private Object	ccTrustedManipulator;
//	private boolean	bUsePreQueue = false; 
	
	/**********************************************
	 * USER MUST IMPLEMENT THESE METHODS,
	 * keep them together for easy review
	 */
	
//	/** This method is GUI independent */
//	protected abstract Object getFocus();
//	/**
//	 * This method is GUI independent
//	 * @param obj if null, is to remove focus from everything
//	 */
//	protected abstract boolean setFocus(Object obj);
//	/** This method is GUI independent */
//	protected abstract void removeFocus(Object obj);
	protected abstract float fontWidth(String strChars, String strStyle, boolean bAveraged);
	protected abstract void setStatsText(String str);
	protected abstract String getStatsText();
	protected abstract void updateVisibleRowsAmount(); 
	protected abstract void clearHintSelection();
	protected abstract Integer getHintIndex();
	protected abstract ConsoleStateAbs setHintIndex(Integer i);
	protected abstract ConsoleStateAbs setHintBoxSize(Vector3f v3fBoxSizeXY, Integer iVisibleLines);
	protected abstract void scrollHintToIndex(int i);
	protected abstract void lineWrapDisableForChildrenOf(Node gp);
	protected abstract boolean mapKeysForInputField();
	protected abstract int getVisibleRows();
	protected abstract Vector3f getSizeOf(Spatial spt);
	//public abstract Vector3f getDumpAreaSliderSize();
	protected abstract Vector3f getContainerConsolePreferredSize();
	protected abstract void setContainerConsolePreferredSize(Vector3f v3f);
	protected abstract void updateDumpAreaSelectedIndex();
	protected abstract Double getDumpAreaSliderPercent();
	protected abstract Integer getInputFieldCaratPosition();
//	protected abstract Vector3f getDumpAreaSize();
//	protected abstract Vector3f getStatsAndControlsSize();
//	protected abstract Vector3f getInputFieldSize();
	/**
	 * @param dIndex if -1, means max index (bottom)
	 */
	protected abstract void scrollDumpArea(double dIndex);
	/**
	 * @return a "floating point index" (Dlindex)
	 */
	protected abstract double getScrollDumpAreaFlindex();
	/**
	 * 
	 * @param bAdd if false will remove
	 * @param pnlChild
	 * @param p
	 */
	public abstract void addRemoveContainerConsoleChild(boolean bAdd, Node pnlChild);
	
//	private static ConsoleGuiStateAbs instance;
//	private static ConsoleGuiStateAbs i(){
//		return instance;
//	}
//	
	
//	public void configureSimple(int iToggleConsoleKey){
//		this.iToggleConsoleKey=iToggleConsoleKey;
//		
//		bConfigureSimpleCompleted = true;
//	}
	
//	@Deprecated
//	@Override
//	private void configure(String strCmdIdentifier,	boolean bIgnorePrefixAndSuffix) {
//		throw new NullPointerException("deprecated!!!");
//	}
	
//	public static class CfgParm implements ICfgParm{
//		String strUIId;
//		boolean bIgnorePrefixAndSuffix;
//		int iToggleConsoleKey;
//		Node nodeGUI;
//		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix,
//				int iToggleConsoleKey, Node nodeGUI) {
//			super();
//			this.strUIId = strUIId;
//			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
//			this.iToggleConsoleKey = iToggleConsoleKey;
//			this.nodeGUI = nodeGUI;
//		}
//	}
	public static class CfgParm extends BaseDialogStateAbs.CfgParm{
		private int iToggleConsoleKey;
		public CfgParm(String strUIId, Node nodeGUI, int iToggleConsoleKey) {
			super(strUIId, nodeGUI);//, null);
			this.iToggleConsoleKey = iToggleConsoleKey;
		}
	}	
	private CfgParm cfg;
	@Override
	public R configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		
		/**
		 * The console is a special dialog.
		 * Many things depend on it.
		 * Even if initially enabled, for the looks it will be made invisible.
		 */
		
		super.configure(cfg);
		
//		this.iToggleConsoleKey=cfg.iToggleConsoleKey;
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	protected void addStyle(String strStyleId){
		if(!astrStyleList.contains(strStyleId)){
			astrStyleList.add(strStyleId);
		}
	}
	
	@Override
	protected boolean initGUI() {
//		initializePre();
		addStyle(STYLE_CONSOLE);
		
//		sapp = (SimpleApplication)app;
//		cc.sapp = sapp;
		
//		app().getStateManager().attach(fpslState);
		tdStatsRefresh.updateTime();
		
//		GuiGlobals.initialize(sapp);
//		BaseStyles.loadGlassStyle(); //do not mess with default user styles: GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
		
		fontConsoleDefault = app().getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		fontConsoleExtraDefault = app().getAssetManager().loadFont("Interface/Fonts/DroidSansMono.fnt");
		app().getAssetManager().registerLoader(TrueTypeLoader.class, "ttf");
		
//		cc.configure(this,sapp);
//		cd().initialize();
		
		// other inits
		cd().addCmdToQueue(cd().CMD_FIX_LINE_WRAP);
		cd().addCmdToQueue(cd().CMD_CONSOLE_SCROLL_BOTTOM);
		
		/**
		 * KEEP AS LAST queued cmds below!!!
		 */
		// must be the last queued command after all init ones!
		// end of initialization
		cd().addCmdToQueue(cd().RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE);
//		addExecConsoleCommandToQueue(cc.btgPreQueue.getCmdIdAsCommand(true)); //		 TODO temporary workaround, pre-queue cannot be enable from start yet...
		if(bInitiallyClosedOnce){
			// after all, close the console
			bKeepInitiallyInvisibleUntilFirstClosed=true;
			cd().addCmdToQueue(CMD_CLOSE_CONSOLE);
		}
		
//		astrStyleList.add(BaseStyles.GLASS);
//		astrStyleList.add(Styles.ROOT_STYLE);
//		astrStyleList.add(STYLE_CONSOLE);
		
		stateStats = app().getStateManager().getState(StatsAppState.class);
		updateEngineStats();
		
		// instantiations initializer
		initializeOnlyTheUIpreInit();
		initializeOnlyTheUIallSteps();
		
		bInitiallyClosedOnce=false; // to not interfere on reinitializing after a cleanup
		
//		ConsoleCommandsBackgroundState ccbs = new ConsoleCommandsBackgroundState(this, cc);
//		ConsoleCommandsBackgroundState.i().configure(sapp,this,cc);
//		if(!app().getStateManager().attach(ccbs))throw new NullPointerException("already attached state "+ccbs.getClass().getName());
		
//		bInitialized=true;
		return true;
	}
	
//	@Override
//	public boolean isInitialized() {
//		return bInitialized;
//	}
	
	@Override
	protected boolean enableOrUndo() {
		if(!bInitializeOnlyTheUI){
			initializeOnlyTheUIallSteps();
		}
		
		return super.enableOrUndo();
	}
	
//	@Override
//	public void onEnable() {
//		super.onEnable();
//		
//		if(!bInitializeOnlyTheUI){
//			initializeOnlyTheUI();
//		}
//	}
//	
//	@Override
//	public void onDisable() {
//		super.onDisable();
//	}
//	
//	@Override
//	public void setEnabled(boolean bEnabled) {
//		if(!bInitializeOnlyTheUI){
//			initializeOnlyTheUI();
//		}
//		
//		this.bEnabled=bEnabled;
//	}
//
//	@Override
//	public boolean isEnabled() {
//		return bEnabled;
//	}
//
//	@Override
//	public void stateAttached(AppStateManager stateManager) {
//	}
//
//	@Override
//	public void stateDetached(AppStateManager stateManager) {
//	}
	
	/**
	 * to be used only during initialization to "look good"
	 * @param b
	 */
	private void setInitializationVisibility(boolean b){
		if(b){
			getContainerMain().setCullHint(CullHint.Inherit); //TODO use bkp with: Never ?
			lstbxAutoCompleteHint.setCullHint(CullHint.Inherit); //TODO use bkp with: Dynamic ?
		}else{
			getContainerMain().setCullHint(CullHint.Always); 
			lstbxAutoCompleteHint.setCullHint(CullHint.Always);
		}
	}
	
	private void initializeOnlyTheUIpreInit(){
		if(bInitializeOnlyTheUI)throw new NullPointerException("already configured!");
		
		v3fApplicationWindowSize = new Vector3f(
				app().getContext().getSettings().getWidth(),
				app().getContext().getSettings().getHeight(),
				LemurMiscHelpersStateI.fPreferredThickness);
		
		iMargin=2;
		v3fConsoleSize = new Vector3f(
			v3fApplicationWindowSize.x -(iMargin*2),
			(v3fApplicationWindowSize.y * fConsoleHeightPerc) -iMargin,
			LemurMiscHelpersStateI.fPreferredThickness);
	}
	
//	private float fPreferredThickness=10.0f;
	
	private void initializeOnlyTheUIallSteps(){
		prepareStyle();
		initializeOnlyTheUI();
		updateFontStuff();
	}
	
	/**
	 * this can be used after a cleanup() too
	 */
	protected void initializeOnlyTheUI(){
//		consoleCursorListener = new ConsoleCursorListener();
//		consoleCursorListener.configure(sapp, cc, this);
//		CursorEventControl.addListenersToSpatial(lstbxAutoCompleteHint, consoleCursorListener);
		lstbxAutoCompleteHint.setName("ConsoleHints");
		lstbxDumpArea.setName("ConsoleDumpArea");
		getIntputField().setName("ConsoleInput");
		getContainerMain().setName("ConsoleContainer");
		ctnrStatsAndControls.setName("ConsoleStats");
		
//		tdLetCpuRest.updateTime();
//		tdStatsRefresh.updateTime();
//		tdDumpQueuedEntry.updateTime();
		
//		createMonoSpaceFixedFontStyle();
		
		// main container
		getNodeGUI().attachChild(getContainerMain());
		getContainerMain().setLocalTranslation(
			iMargin, 
			app().getContext().getSettings().getHeight()-iMargin, 
			0); // Z translation controls what will be shown above+/below- others, will be automatically set by FocusChangeListenerI
		
//		initKeyMappings(); //		mapKeys();
		
//		// focus
//		GuiGlobals.getInstance().requestFocus(intputField);
		
		// help (last thing)
//		dumpInfoEntry("ListBox height = "+fLstbxHeight);
//		dumpAllStats();
//		dumpInfoEntry("Hit F10 to toggle console.");
		
		if(bInitiallyClosedOnce){
			setInitializationVisibility(false);
		}
		
		/**
		 * =======================================================================
		 * =========================== LAST THING ================================
		 * =======================================================================
		 */
		bInitializeOnlyTheUI=true;
	}
	
	@Override
	public boolean cmdEditCopyOrCut(boolean bCut) {
		String strParam1 = cd().getCurrentCommandLine().paramString(1);
		boolean bUseCommandDelimiterInsteadOfNewLine=false;
		if(strParam1!=null){
			switch(strParam1){
				case "-d":bUseCommandDelimiterInsteadOfNewLine=true;break;
			}
		}
		
		/**
		 * the string is sent to clipboard, no need to collect here...
		 * TODO log it may be?
		 */
		String str = cd().editCopyOrCut(false, bCut, bUseCommandDelimiterInsteadOfNewLine);
		
		return true;
	}
	
//	private String prepareToPaste(String strPasted, String strCurrent){
//		if(isInputTextFieldEmpty() && strPasted.trim().startsWith(""+cd().getCommandPrefix())){
//			/**
//			 * replaces currently input field "empty" line 
//			 * with the command (can be invalid in this case, user may complete it properly)
//			 */
//			strCurrent = strPasted.trim(); 
//		}else{
//			strCurrent+=strPasted; //simple append if there is no carat position reference
//		}
//		
//		return strCurrent;
//	}
	
//	private void editPasteAppending(String strPasted) {
////		String strPasted = MiscI.i().retrieveClipboardString(true);
////		if(strPasted.endsWith("\\n"))strPasted=strPasted.substring(0, strPasted.length()-2);
//		
//		String strCurrent = getInputText();
////		strCurrent = prepareToPaste(strPasted, strCurrent);
//		
//////		if(isInputTextFieldEmpty() && strPasted.trim().startsWith(""+cd().getCommandPrefix())){
////		if(isInputTextFieldEmpty() && cd().isCommandString(strPasted)){
////			/**
////			 * replaces currently input field "empty" line 
////			 * with the command (can be invalid in this case, user may complete it properly)
////			 */
////			strCurrent = strPasted.trim(); 
////		}else{
//			strCurrent+=strPasted; //simple append if there is no carat position reference
////		}
//		
//		setInputFieldText(strCurrent); 
//	}
	
	protected void editPasteFromClipBoard() {
		String str = MiscI.i().retrieveClipboardString(true);
		if(str.endsWith("\\n"))str=str.substring(0, str.length()-2);
		
		if(isInputTextFieldEmpty() && cd().isCommandString(str)){
			/**
			 * replaces currently input field "empty" line 
			 * with the command (can be invalid in this case, user may complete it properly)
			 */
			setInputFieldText(str.trim());
		}else{
			if(!editInsertAtCaratPosition(str)){
				/**
				 * just append the text
				 */
				setInputFieldText(getInputText()+str); 
//				editPasteAppending(str);
			}
		}
	}	
	
	protected abstract boolean editInsertAtCaratPosition(String str);
	
//	private void cmdHistSave(String strCmd) {
//		fileAppendLine(flCmdHist,strCmd);
//	}
	
//	private ArrayList<String> fileLoad(String strFile) {
//		return fileLoad(new File(strFile));
//	}
//	private ArrayList<String> fileLoad(File fl) {
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
	
//	private void cmdHistLoad() {
//		astrCmdHistory.addAll(fileLoad(flCmdHist));
//	}
//	
//	private void dumpSave(DumpEntry de) {
////		if(de.isSavedToLogFile())return;
//		fileAppendLine(flLastDump,de.getLineOriginal());
//	}
	
//	private void fileAppendList(File fl, ArrayList<String> astr) {
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
//	private void fileAppendLine(File fl, String str) {
//		ArrayList<String> astr = new ArrayList<String>();
//		astr.add(str);
//		fileAppendList(fl, astr);
//	}
	
//	private void cmdTest(){
//		dumpInfoEntry("testing...");
//		String strOption = paramString(1);
//		
//		if(strOption.equalsIgnoreCase("fps")){
////			app().setSettings(settings);
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
	
	private float fontWidth(String strChars){
		return fontWidth(strChars, strStyle, true);
	}
	
	private boolean isHintActive(){
		return lstbxAutoCompleteHint.getParent()!=null;
	}
	
	private String getSelectedHint(){
		if(!isHintActive())return null;
		Integer i = getHintIndex();
		if(i==null)return null;
		return vlstrAutoCompleteHint.get(i);
	}
	
	@Override
	protected boolean initKeyMappings() {
		if(!mapKeysForInputField())return false;
		
		// console toggle
//		if(iToggleConsoleKey!=null){
			if(!app().getInputManager().hasMapping(INPUT_MAPPING_CONSOLE_TOGGLE.toString())){
				app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_TOGGLE.toString(), 
					new KeyTrigger(cfg.iToggleConsoleKey));
					
				alConsoleToggle = new ActionListener() {
					@Override
					public void onAction(String name, boolean isPressed, float tpf) {
						if(isPressed && INPUT_MAPPING_CONSOLE_TOGGLE.equals(name)){
//							if(!isInitialized()){
//								initialize();
//							}
							toggleRequest();
//							setEnabledRequest(!isEnabled());
							
							/**
							 * as it is initially invisible, from the 1st time user opens the console on, 
							 * it must be visible.
							 */
							setInitializationVisibility(true);
						}
					}
				};
				app().getInputManager().addListener(alConsoleToggle, INPUT_MAPPING_CONSOLE_TOGGLE.toString());            
			}
//		}
		
		if(!app().getInputManager().hasMapping(INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.toString())){
			app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.toString(), 
					new KeyTrigger(KeyInput.KEY_LCONTROL),
					new KeyTrigger(KeyInput.KEY_RCONTROL));
			
			ActionListener al = new ActionListener() {
				@Override
				public void onAction(String name, boolean isPressed, float tpf) {
					bKeyControlIsPressed  = isPressed;
				}
			};
			app().getInputManager().addListener(al, INPUT_MAPPING_CONSOLE_CONTROL_PRESSED.toString());            
		}
		
		if(!app().getInputManager().hasMapping(INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.toString())){
			app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.toString(), 
				new KeyTrigger(KeyInput.KEY_LSHIFT),
				new KeyTrigger(KeyInput.KEY_RSHIFT));
				
			ActionListener al = new ActionListener() {
				@Override
				public void onAction(String name, boolean isPressed, float tpf) {
					bKeyShiftIsPressed  = isPressed;
				}
			};
			app().getInputManager().addListener(al, INPUT_MAPPING_CONSOLE_SHIFT_PRESSED.toString());            
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
	      	if(sptScrollTarget.equals(getIntputField())){
	      		navigateCmdHistOrHintBox(getIntputField(), bUp?ENav.Up:ENav.Down);
	      	}else
	      	if(sptScrollTarget.equals(lstbxAutoCompleteHint)){
	      		navigateCmdHistOrHintBox(lstbxAutoCompleteHint, bUp?ENav.Up:ENav.Down);
	      	}
	      }
			}
		};
    
		app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SCROLL_UP+"", tggScrollUp);
		app().getInputManager().addListener(alConsoleScroll, INPUT_MAPPING_CONSOLE_SCROLL_UP+"");
    
		app().getInputManager().addMapping(INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"", tggScrollDown);
		app().getInputManager().addListener(alConsoleScroll, INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"");
		
		return true;
	}
	
	private void fillInputFieldWithHistoryDataAtIndex(int iIndex){
		String str = cd().getCmdHistoryAtIndex(iIndex);
		if(str==null)return;
		
		setInputFieldText(str);
	}
	
//	class ThreadBackgrounCommands implements Runnable{
//		@Override
//		private void run() {
//			TimedDelay td = new TimedDelay(1000.0f/60f)
//			while(true){
//				if(!cc.btgExecCommandsInBackground.b())return;
//				
//			}
//		}
//	}
	
	@Override
	protected boolean updateOrUndo(float tpf) {
		if(!super.updateOrUndo(tpf))return false;
		
		if(bKeepInitiallyInvisibleUntilFirstClosed){
			setInitializationVisibility(false);
		}else{
			setInitializationVisibility(isEnabled());
		}
		
		if(!isEnabled())return true; //one update may actually happen after being disabled...
		
		/**
		 *	Will update only if enabled... //if(!isEnabled())return;
		 */
		
		cd().update(tpf);
		
		updateInputFieldFillWithSelectedEntry();
		updateCurrentCmdHistoryEntryReset();
		updateStats();
		updateAutoCompleteHint();
		updateDumpAreaSelectedIndex();
		updateVisibleRowsAmount();
		updateScrollToBottom();
//		updateBlinkInputFieldTextCursor(intputField);
//		if(efHK!=null)efHK.updateHK();
		
		updateOverrideInputFocus();
		
		return true; 
	}
	
	protected abstract void updateOverrideInputFocus();
//	{
//		if(isEnabled()){
//			LemurFocusHelperStateI.i().requestFocus(getIntputField());
//			setFocus(getIntputField());
//		}else{
//			removeFocus(getIntputField());
//		}
//	}
	
//	private void updateOverrideInputFocus(){
//		Spatial sptWithFocus = (Spatial) getFocus();
//		
//		if(isEnabled()){
//			if(intputField!=sptWithFocus){
//				if(sptPreviousFocus==null){
//					sptPreviousFocus = sptWithFocus;
//					bRestorePreviousFocus=true;
//				}
//				
//				setFocus(intputField);
//			}
//		}else{
//			/**
//			 * this shall happen only once on console closing...
//			 */
//			if(bRestorePreviousFocus){
//				if(sptPreviousFocus!=null){
//					if(
//							!sptPreviousFocus.hasAncestor(app().getGuiNode())
//							&&
//							!sptPreviousFocus.hasAncestor(app().getRootNode()) //TODO can it be at root node?
//					){
//						/**
//						 * if it is not in one of the rendering nodes,
//						 * simply removes the focus, to give it back to 
//						 * other parts of the application.
//						 */
//						sptPreviousFocus = null;
//					}
//				}
//				
//				setFocus(sptPreviousFocus);
////				GuiGlobals.getInstance().requestFocus(sptPreviousFocus);
//				sptPreviousFocus = null;
//				bRestorePreviousFocus=false;
//			}
//		}
//	}
	
//	/**
//	 * TODO use base 36 (max alphanum chars amount)
//	 * @return
//	 */
//	private String getNextUniqueId(){
//		return ""+(++lLastUniqueId);
//	}
	
//	private void updateToggles() {
//		if(cc.btgEngineStatsView.checkChangedAndUpdate())updateEngineStats();
//		if(cc.btgEngineStatsFps.checkChangedAndUpdate())updateEngineStats();
//		if(cc.btgFpsLimit.checkChangedAndUpdate())fpslState.setEnabled(cc.btgFpsLimit.b());
//		if(cc.btgConsoleCpuRest.checkChangedAndUpdate())tdLetCpuRest.setActive(cc.btgConsoleCpuRest.b());
////		if(cc.btgPreQueue.checkChangedAndUpdate())bUsePreQueue=cc.btgPreQueue.b();
//	}
//	private void resetCmdHistoryCursor(){
//		cc.iCmdHistoryCurrentIndex = cc.getCmdHistorySize();
//	}
	
	private void updateCurrentCmdHistoryEntryReset() {
		String strNewInputValue = getInputText();
		if((cd().getCmdHistoryCurrentIndex()-iCmdHistoryPreviousIndex)==0){
			if(!strNewInputValue.equals(strPreviousInputValue)){
				/**
				 * user has deleted or typed some character
				 */
				cd().resetCmdHistoryCursor();
			}
		}
		
		strPreviousInputValue=strNewInputValue;
		iCmdHistoryPreviousIndex=cd().getCmdHistoryCurrentIndex();
	}
	
	protected void closeHint(){
		vlstrAutoCompleteHint.clear();
		lstbxAutoCompleteHint.removeFromParent();
	}
	
//	/**
//	 * Validates if the first extracted word is a valid command.
//	 * 
//	 * @param strCmdFullChk can be the full command line here
//	 * @return
//	 */
//	private boolean validateBaseCommand(String strCmdFullChk){
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
//	private String extractCommandPart(String strCmdFull, int iPart){
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
	
//	private String extractFirstCommandPart(String strCmdFull){
//		if(strCmdFull.startsWith(strCommandPrefixChar)){
//			strCmdFull=strCmdFull.substring(strCommandPrefixChar.length());
//		}
//		return strCmdFull.split("[^"+strValidCmdCharsRegex+"]")[0];
//	}
	
//	private boolean checkInputEmpty(){
//		return checkInputEmptyDumpIfNot(false);
//	}
	/**
	 * after trim(), if empty or have only the command prefix char (pseudo empty), 
	 * will return true.
	 * @return
	 */
	private boolean isInputTextFieldEmpty(){
//	private boolean checkInputEmptyDumpIfNot(boolean bDumpContentsIfNotEmpty){
		String strCurrentInputText = getInputText().trim();
		
		if(strCurrentInputText.isEmpty())return true;
		
		if(strCurrentInputText.equals(""+cd().getCommandPrefix()))return true;
		
//		if(bDumpContentsIfNotEmpty){
//			dumpInfoEntry("Not issued command below:");
//			dumpEntry(strCurrentInputText); //so user will not lose what was typing...
//			/**
//			 * Do not scroll in this case. No command was issued...
//			 */
//		}
		
		return false;
	}
	
	private String dumpAndClearInputField(){
		if(!isInputTextFieldEmpty()){
			cd().dumpInfoEntry("Not issued command below:");
			String str = getInputText();
			cd().dumpEntry(str); //so user will not lose what was typing...
			clearInputTextField();
			return str;
		}
		return null;
	}
	
	private void updateInputFieldFillWithSelectedEntry() {
		// auto-fill with selected command
		if(getDumpAreaSelectedIndex()>=0){// && isInputTextFieldEmpty()){
			if(iSelectionIndexPreviousForFill!=getDumpAreaSelectedIndex()){ //to let user type things...
				updateCopyFrom();
				
				if(bKeyControlIsPressed){
					String strCmdChk = cd().editCopyOrCut(true,false,true); //vlstrDumpEntries.get(getDumpAreaSelectedIndex()).trim();
					strCmdChk=strCmdChk.trim();
					if(cd().validateCommand(strCmdChk,false)){
						if(!strCmdChk.startsWith(""+cd().getCommandPrefix()))strCmdChk=cd().getCommandPrefix()+strCmdChk;
						dumpAndClearInputField();
//						int iCommentBegin = strCmdChk.indexOf(cc.getCommentPrefix());
//						if(iCommentBegin>=0)strCmdChk=strCmdChk.substring(0,iCommentBegin);
//						strCmdChk=clearCommentsFromMultiline(strCmdChk);
//						if(strCmdChk.endsWith("\\n"))strCmdChk=strCmdChk.substring(0,strCmdChk.length()-2);
//						if(strCmdChk.endsWith("\n" ))strCmdChk=strCmdChk.substring(0,strCmdChk.length()-1);
						setInputFieldText(strCmdChk);
					}
				}
				
				updateSelectionIndexForAutoFillInputFieldText();
//				iSelectionIndexPreviousForFill = getDumpAreaSelectedIndex();
			}
		}
	}
	private void updateSelectionIndexForAutoFillInputFieldText() {
		iSelectionIndexPreviousForFill = getDumpAreaSelectedIndex();
	}
	
	private String clearCommentsFromMultiline(String str){
		/**
		 * this will remove any in-between comments
		 */
//		str=str.replace("\\n", "\n");
		String strCommentRegion=Pattern.quote(cd().getCommentPrefixStr())+"[^\n]*\n";
		String strJoinToken="\n";
		if(str.matches(".*"+strCommentRegion+".*")){
			str=String.join(strJoinToken, str.split(strCommentRegion));
		}
		
		strCommentRegion=Pattern.quote(cd().getCommentPrefixStr())+"[^"+cd().getCommandDelimiterStr()+"]*"+cd().getCommandDelimiterStr();
		strJoinToken=cd().getCommandDelimiterStr();
		if(str.matches(".*"+strCommentRegion+".*")){
			str=String.join(strJoinToken, str.split(strCommentRegion));
		}
		
		return str;
	}
	
	protected String fixStringToInputField(String str){
		str=clearCommentsFromMultiline(str);
		
		/**
		 * removes ending NL
		 */
		if(str.endsWith("\\n"))str=str.substring(0,str.length()-2);
		if(str.endsWith("\n" ))str=str.substring(0,str.length()-1);
		
		/**
		 * Lines ending with command delimiter, do not require newline.
		 */
		return cd().convertNewLineToCmdDelimiter(str)
			/**
			 * convert special chars to escaped ones
			 */
			.replace("\n", "\\n")
			.replace("\t", "\\t") //			.replace("\t", strReplaceTAB)
			.replace("\r", "");
	}
	
//	public CommandsDelegatorI getConsoleCommands(){
//		return this.cd;
//	}
	
	protected int getDumpAreaSelectedIndex(){
		return iSelectionIndex;
	}
	protected R setDumpAreaSelectedIndex(int i){
		this.iSelectionIndex = i;
		return getThis();
	}
	
	private void updateCopyFrom(){
		cd().updateCopyFrom(getDumpAreaSelectedIndex(), bKeyShiftIsPressed);
	}
	
	/**
	 * this is to fix the dump entry by disallowing automatic line wrapping
	 */
	@Override
	public void cmdLineWrapDisableDumpArea(){
		lineWrapDisableForChildrenOf(lstbxDumpArea);
	}
	
//	private void updateVisibleRowsAmount(){
//		if(fLstbxHeight != getDumpAreaSize().y){
//			iVisibleRowsAdjustRequest = 0; //dynamic
//		}
//		
//		if(iVisibleRowsAdjustRequest==null)return;
//		
//		Integer iForceAmount = iVisibleRowsAdjustRequest;
//		if(iForceAmount>0){
//			iShowRows=iForceAmount;
////			lstbx.setVisibleItems(iShowRows);
////			lstbx.getGridPanel().setVisibleSize(iShowRows,1);
//		}else{
//			if(lstbxDumpArea.getGridPanel().getChildren().isEmpty())return;
//			
//			Button	btnFixVisibleRowsHelper = null;
//			for(Spatial spt:lstbxDumpArea.getGridPanel().getChildren()){
//				if(spt instanceof Button){
//					btnFixVisibleRowsHelper = (Button)spt;
//					break;
//				}
//			}
//			if(btnFixVisibleRowsHelper==null)return;
//			
//			fLstbxEntryHeight = retrieveBitmapTextFor(btnFixVisibleRowsHelper).getLineHeight();
//			if(fLstbxEntryHeight==null)return;
//			
//			fLstbxHeight = getDumpAreaSize().y;
//			
//			float fHeightAvailable = fLstbxHeight;
////				float fHeightAvailable = fLstbxHeight -fInputHeight;
////				if(ctnrMainTopSubWindow.hasChild(lblStats)){
////					fHeightAvailable-=fStatsHeight;
////				}
//			iShowRows = (int) (fHeightAvailable / fLstbxEntryHeight);
//		}
//		
//		lstbxDumpArea.setVisibleItems(iShowRows);
//		
//		cc.varSet(cc.CMD_FIX_VISIBLE_ROWS_AMOUNT, ""+iShowRows, true);
//		
//	//	lstbx.getGridPanel().setVisibleSize(iShowRows,1);
//		cc.dumpInfoEntry("fLstbxEntryHeight="+MiscI.i().fmtFloat(fLstbxEntryHeight)+", "+"iShowRows="+iShowRows);
//		
//		iVisibleRowsAdjustRequest=null;
//		
//		cmdLineWrapDisableDumpArea();
//	}
	
//	private BitmapText retrieveBitmapTextFor(Node pnl){
//		for(Spatial c : pnl.getChildren()){
//			if(c instanceof BitmapText){
//				return (BitmapText)c;
//			}
//		}
//		return null;
//	}

	/**
	 * This is what happens when Enter key is pressed.
	 * @param strCmd
	 * @return false if was a comment, empty or invalid
	 */
	protected boolean actionSubmit(final String strCmd){
		if(checkAndApplyHintAtInputField())return true;
		
		return cd().actionSubmitCommand(strCmd);
	}
	
	public boolean checkAndApplyHintAtInputField(){
		/**
		 * if hint area is active and has a selected entry, 
		 * it will override default command submit.
		 */
		if(isHintActive()){
			String strHintCmd = getSelectedHint();
			if(strHintCmd!=null){
				strHintCmd=cd().getCommandPrefix()
					+cd().extractCommandPart(strHintCmd,0)+" "
					+String.join(" ", cd().convertToCmdParamsList(getInputText(),1));
//					+cd().extractCommandPart(getInputText(),1)
				setInputFieldText(strHintCmd);
				return true;
//				if(!getInputText().equals(strHintCmd)){
//					editInsertAtCaratPosition(strHintCmd);
////					setInputFieldText(strHintCmd);
//					return true;
//				}
			}
		}
		
		return false;
	}
	
	
	@Override
	public void clearInputTextField() {
		setInputFieldText(""+cd().getCommandPrefix());
	}
	
//	private boolean actionSubmitCommand(final String strCmd){
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
//			if(astrCmdHistory.get(getCmdHistorySize()-1).equals(strCmd)){
//				bAdd=false; //prevent sequential dups
//			}
//		}
//		
//		if(bAdd){
//			cc.astrCmdHistory.add(strCmd);
//			
//			cc.cmdHistSave(strCmd);
//			while(cc.getCmdHistorySize()>cc.iMaxCmdHistSize){
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
		if(!cd().btgAutoScroll.b())return;
		
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
	
	private void updateScrollToBottom(){
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
			if(Double.compare(getDumpAreaSliderPercent(), 0.0)==0){
				scrollToBottomRequestSuspend();
				return;
			}
		}
		
		scrollDumpArea(-1);
		tdScrollToBottomRetry.updateTime();
		iScrollRetryAttemptsDBG++;
	}	
	
//	/**
//	 * 
//	 * @param dIndex if -1, means max index (bottom)
//	 */
//	private void scrollDumpArea(double dIndex){
//		/**
//		 * the index is actually inverted
//		 */
//		double dMax = lstbxDumpArea.getSlider().getModel().getMaximum();
//		if(dIndex==-1)dIndex=dMax;
//		dIndex = dMax-dIndex;
//		double dPerc = dIndex/dMax;
//		
//		lstbxDumpArea.getSlider().getModel().setPercent(dPerc);
//		lstbxDumpArea.getSlider().getModel().setValue(dIndex);
//	}
	
//	/**
//	 * 
//	 * @return a "floating point index" (Dlindex)
//	 */
//	private double getScrollDumpAreaFlindex(){
//		return lstbxDumpArea.getSlider().getModel().getMaximum()
//				-lstbxDumpArea.getSlider().getModel().getValue();
//	}
	
//	private String autoCompleteInputField(){
//		return autoCompleteInputField(false);
//	}
	protected String autoCompleteInputFieldWithCmd(boolean bMatchContains){
		String strCmdPart = getInputText();
		String strCmdAfterCarat="";
		
//		Integer iCaratPositionHK = efHK==null?null:efHK.getInputFieldCaratPosition();
		Integer iCaratPosition = getInputFieldCaratPosition();
		if(iCaratPosition!=null){
			strCmdAfterCarat = strCmdPart.substring(iCaratPosition);
			strCmdPart = strCmdPart.substring(0, iCaratPosition);
		}
		
		String strCompletedCmd = autoCompleteCmdMoreWork(strCmdPart,bMatchContains);
		
		/**
		 * parameters completion!
		 */
		if(strCompletedCmd.equals(strCmdPart)){
			String strBaseCmd = cd().extractCommandPart(strCmdPart,0);
			String strParam1 = cd().extractCommandPart(strCmdPart,1);
			
			String strCmd=null;
			String strPartToComplete=null;
			ArrayList<String> astrOptList = null;
			boolean bIsVarCompletion=false;
			if(CMD_CONSOLE_STYLE.equals(strBaseCmd)){
				strCmd=CMD_CONSOLE_STYLE+" ";
				astrOptList=getStyleListClone();
				strPartToComplete=strParam1;
			}else
			if(cd().CMD_DB.equals(strBaseCmd)){
				strCmd=cd().CMD_DB+" ";
				astrOptList=EDataBaseOperations.getValuesAsArrayList();
				strPartToComplete=strParam1;
			}else
			if(cd().CMD_VAR_SET.equals(strBaseCmd)){
				strCmd=cd().CMD_VAR_SET+" ";
				astrOptList=cd().getVariablesIdentifiers(false);
				strPartToComplete=strParam1;
				bIsVarCompletion=true;
			}else{
				/**
				 * complete for variables ids when retrieving variable value 
				 */
				String strRegexVarOpen=Pattern.quote(""+cd().getVariableExpandPrefix()+"{");
				String strRegex=".*"+strRegexVarOpen+"["+MiscI.strValidCmdCharsRegex+cd().RESTRICTED_TOKEN+"]*$";
				if(strCompletedCmd.matches(strRegex)){
					strCmd=strCompletedCmd.trim().substring(1); //removes command prefix
					astrOptList=cd().getVariablesIdentifiers(true);
					
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
				strCompletedCmd=""+cd().getCommandPrefix()+strCmd;
				
				ArrayList<String> astr = strPartToComplete==null ? astrOptList :
					AutoCompleteI.i().autoComplete(strPartToComplete, astrOptList, bMatchContains);
				if(astr.size()==1){
					strCompletedCmd+=astr.get(0);
				}else{
					cd().dumpInfoEntry("Param autocomplete:");
					String strFirst = null;
					if(strPartToComplete!=null)strFirst = astr.remove(0);
					for(String str:astr){
//						if(strPartToComplete!=null && str.equals(astr.get(0)))continue;
						if(bIsVarCompletion)str=cd().varReportPrepare(str);
						cd().dumpSubEntry(str);
					}
					
					if(strPartToComplete!=null){
						strCompletedCmd+=strFirst; //best partial param match
					}
				}
			}
//			}
		}
		
		if(strCompletedCmd.trim().isEmpty())strCompletedCmd=""+cd().getCommandPrefix();
		setInputFieldText(strCompletedCmd+strCmdAfterCarat);
//		LemurMiscHelpersState.i().setCaratPosition(intputField, strCompletedCmd.length());
//		if(efHK!=null)efHK.setCaratPosition(strCompletedCmd.length());
		
		scrollToBottomRequest();
		
		return strCompletedCmd;
	}
	
	private ArrayList<String> getStyleListClone() {
		return new ArrayList<String>(astrStyleList);
	}
	private String autoCompleteCmdMoreWork(String strCmdPart, boolean bMatchContains){
		String strCmdPartOriginal = strCmdPart;
		strCmdPart=strCmdPart.trim();
		
		// no command typed
		if(strCmdPart.equalsIgnoreCase(""+cd().getCommandPrefix()) || strCmdPart.isEmpty()){
			return strCmdPartOriginal;
		}
		
		strCmdPart=strCmdPart.replaceFirst("^"+cd().getCommandPrefix(), "");
		
		// do not allow invalid chars
		if(!MiscI.i().isValidIdentifierCmdVarAliasFuncString(strCmdPart)){
			return strCmdPartOriginal;
		}
		
		ArrayList<String> astr = AutoCompleteI.i().autoComplete(
			strCmdPart, cd().getAllPossibleCommands(), bMatchContains);
		/**
		 * Adjust the autocomplete result based on core and unique cmds:
		 * 0 - is partially typed
		 * 1 - is core or unique cmd
		 * 2 - is core or unique cmd
		 * In this case, there is no core cmds conflicts.
		 */
		if(astr.size()==3){
			CommandData cmdd = cd().getCmdDataIfSame(astr.get(1), astr.get(2));
			if(cmdd!=null){
//			CommandData cmdd = cd().getCmdDataFor(astr.get(1));
////			if(cmdd==null)cmdd=cd().getCmdDataFor(astr.get(1));
//			if(cmdd.equals(cd().getCmdDataFor(astr.get(2)))){
				astr.clear();
				astr.add(cmdd.getSimpleCmdId());
			}
		}
		
		String strFirst=astr.get(0); //the actual stored command may come with comments appended
		
		String strAppendSpace = "";
		if(astr.size()==1 && cd().validateCommand(strFirst,true)){
			strAppendSpace=" "; //found an exact command valid match, so add space
		}
		
		// many possible matches
		if(astr.size()>1){
			cd().dumpInfoEntry("AutoComplete: ");
			for(String str:astr){
				if(str.equals(strFirst))continue; //skip the partial improved match, 1st entry
				cd().dumpSubEntry(cd().getCommandPrefix()+str);
			}
		}
		
		return cd().getCommandPrefix()+strFirst.split(" ")[0]+strAppendSpace;
	}
	
	@Override
	public boolean statsFieldToggle() {
		if(cd().getCurrentCommandLine().paramBooleanCheckForToggle(1)){
			Boolean bEnable = cd().getCurrentCommandLine().paramBoolean(1);
			
			boolean bIsVisible = ctnrStatsAndControls.getParent()!=null;
			boolean bSetVisible = !bIsVisible; //toggle
			
			if(bEnable!=null)bSetVisible = bEnable; //override
			
			if(bSetVisible){
				if(!bIsVisible){
					addRemoveContainerConsoleChild(true,ctnrStatsAndControls);
//					ctnrMainTopSubWindow.addChild(ctnrStatsAndControls,BorderLayout.Position.North);
				}
			}else{
				if(bIsVisible){
					addRemoveContainerConsoleChild(false,ctnrStatsAndControls);
//					ctnrMainTopSubWindow.removeChild(ctnrStatsAndControls);
				}
			}
			
			updateVisibleRowsAmount();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param fNewHeightPercent null to use the default
	 */
	private void modifyConsoleHeight(Float fNewHeightPercent) {
		Vector3f v3fNew = getContainerConsolePreferredSize(); //getSize() does not work well..
		if(!v3fNew.equals(v3fConsoleSize)){
			cd().dumpDevWarnEntry("sizes should be equal: "+v3fNew+v3fConsoleSize);
		}
		
		if(fNewHeightPercent==null)fNewHeightPercent=fdvConsoleHeightPercDefault.getFloat();
		
		if(fNewHeightPercent>0.95f)fNewHeightPercent=0.95f;
		
		v3fNew.y = fNewHeightPercent * app().getContext().getSettings().getHeight();
		
		float fMin = getInputFieldHeight() +getStatsHeight() +fLstbxEntryHeight*3; //will show only 2 rows, the 3 value is a safety margin
		
		if(v3fNew.y<fMin)v3fNew.y=fMin;
		
		setContainerConsolePreferredSize(v3fNew); //setSize() does not work well..
//		ctnrMainTopSubWindow.setSize(v3fNew); //setSize() does not work well..
		v3fConsoleSize.set(v3fNew);
		
		fConsoleHeightPerc = (fNewHeightPercent);
		
		cd().varSet(CMD_CONSOLE_HEIGHT, ""+fConsoleHeightPerc, true);
		
		iVisibleRowsAdjustRequest = 0; //dynamic
	}
	
	@Override
	public boolean isVisibleRowsAdjustRequested(){
		return iVisibleRowsAdjustRequest!=null;
	}
	
	@Override
	public void updateEngineStats() {
		stateStats.setDisplayStatView(cd().btgEngineStatsView.get());
		stateStats.setDisplayFps(cd().btgEngineStatsFps.get());
	}
	private void styleHelp(){
		cd().dumpInfoEntry("Available styles:");
		//There is still no way to collect styles from: GuiGlobals.getInstance().getStyles()
		for(String str:astrStyleList){
			cd().dumpSubEntry(str);
		}
	}
	private boolean styleCheck(String strStyle) {
		return astrStyleList.contains(strStyle);
	}
	
	private boolean cmdStyleApply(String strStyleNew) {
		boolean bOk = styleCheck(strStyleNew);
		if(bOk){
			strStyle=strStyleNew;
			
			cd().varSet(CMD_CONSOLE_STYLE, strStyle, true);
			
//			updateFontStuff();
			
			cd().cmdResetConsole();
		}else{
			cd().dumpWarnEntry("invalid style: "+strStyleNew);
			styleHelp();
		}
		
		return bOk;
	}
	
	private float widthForListbox(){
		return getSizeOf(lstbxDumpArea).x;
	}
	
	/**
	 * TODO review this method, may be sliderDumpArea variable is unnecessary...
	 * @return
	 */
	private float widthForDumpEntryField(){
		//TODO does slider and the safety margin are necessary?
		return widthForListbox() -getSizeOf(sliderDumpArea).x -fSafetyMarginGUESSED;
//		return widthForListbox();
	}
	
	@Override
	public Node getContainerMain(){
		return (Node)super.getContainerMain();
	}
	
	/**
	 * This method can be overriden but this is not necessary,
	 * because this is not a cleanup procedure...
	 * 
	 * This class object will be discarded/trashed/gc.
	 * 
	 * see how flow starting with {@link #requestRestart()} works
	 */
	@Override
	public boolean prepareAndCheckIfReadyToDiscard(ConditionalStateManagerI.CompositeControl cc) {
//		tdLetCpuRest.reset();
//		tdScrollToBottomRequestAndSuspend.reset();
//		tdScrollToBottomRetry.reset();
//		tdTextCursorBlinkHK.reset();
		
		if(getContainerMain().getChildren().size()>0){
			System.err.println("WARN: console container should have been properly/safely cleaned using specific gui methods by overriding this method.");
			getContainerMain().detachAllChildren();
		}
//		ctnrMainTopSubWindow.clearChildren();
//		tfAutoCompleteHint.removeFromParent();
		lstbxAutoCompleteHint.removeFromParent();
		getContainerMain().removeFromParent();
		
		//TODO should keymappings be at setEnabled() ?
//    /**
//     * IMPORTANT!!!
//     * Toggle console must be kept! Re-initialization depends on it!
//     * 
		app().getInputManager().deleteMapping(INPUT_MAPPING_CONSOLE_TOGGLE.toString());
    app().getInputManager().removeListener(alConsoleToggle);
//     */
    app().getInputManager().deleteMapping(INPUT_MAPPING_CONSOLE_SCROLL_UP+"");
    app().getInputManager().deleteMapping(INPUT_MAPPING_CONSOLE_SCROLL_DOWN+"");
    app().getInputManager().removeListener(alConsoleScroll);
    
//    if(efHK!=null)efHK.cleanupHK();
    bInitializeOnlyTheUI=false;
//    bInitialized=false;
    
		return super.prepareAndCheckIfReadyToDiscard(cc);
	}
	
//	private boolean isInitiallyClosed() {
//		return bInitiallyClosedOnce;
//	}
//	private void setCfgInitiallyClosed(boolean bInitiallyClosed) {
//		this.bInitiallyClosedOnce = bInitiallyClosed;
//	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(StringCmdField.class)){
			if(strFinalFieldInputCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg(rfcv);
				rfcfg.setPrefixCmd("CONSOLEGUISTATE_");
				rfcfg.setFirstLetterUpperCase(true);
			}else
			if(CommandsDelegator.strFinalFieldRestrictedCmdCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg(rfcv);
				rfcfg.setPrefixCmd(""+CommandsDelegator.RESTRICTED_TOKEN);
				rfcfg.setFirstLetterUpperCase(true);
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
		
//		if(rfcfg==null)rfcfg = cd().getReflexFillCfg(rfcv);
		if(rfcfg==null)rfcfg = super.getReflexFillCfg(rfcv);
		
		return rfcfg;
	}

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator	cd){
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,CMD_CLOSE_CONSOLE,"like the bound key to do it")){
			setEnabledRequest(false);
//			bKeepInitiallyInvisibleUntilFirstClosed=false; //"first close" is the hint
//			bFullyInitialized=true; //it will only be completely initialized after the 1st close...
			bCommandWorked=true;
		}else
		if(cd.checkCmdValidity(this,CMD_CONSOLE_HEIGHT,"[fPercent] of the application window")){
			Float f = cd.getCurrentCommandLine().paramFloat(1);
			modifyConsoleHeight(f);
			bCommandWorked=true;
		}else
//		if(cc.checkCmdValidity(this,cc.CMD_CONSOLE_SCROLL_BOTTOM,"")){
//			scrollToBottomRequest();
//			bCommandWorked=true;
//		}else
		if(cd.checkCmdValidity(this,CMD_CONSOLE_STYLE,"[strStyleName] changes the style of the console on the fly, empty for a list")){
			String strStyle = cd.getCurrentCommandLine().paramString(1);
			if(strStyle==null)strStyle="";
			bCommandWorked=cmdStyleApply(strStyle);
		}else
		if(cd.checkCmdValidity(this,CMD_DEFAULT,"will revert to default values/config/setup (use if something goes wrong)")){
			//TODO apply all basic settings here, like font size etc
			svfUserFontOption.setObjectValue(strDefaultFont);
			ilvFontSize.setObjectValue(iDefaultFontSize);
			bCommandWorked=cmdStyleApply(STYLE_CONSOLE);
		}else
		if(cd.checkCmdValidity(this,CMD_FONT_LIST,"[strFilter] use 'all' as filter to show all, otherwise only monospaced will be the default filter")){
			String strFilter = cd.getCurrentCommandLine().paramString(1);
			if(strFilter==null){
				strFilter="mono";
			}else{
				if(strFilter.equalsIgnoreCase("all")){
					strFilter=null;
				}
			}
			for(String str:getSystemFontList(strFilter))cd.dumpSubEntry(str);
			bCommandWorked=true;
		}else
		{
			return super.execConsoleCommand(cd);
//			return ECmdReturnStatus.NotFound;
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public void dumpAllStats() {
		cd().dumpEntry(true, cd().btgShowDeveloperInfo.get(), false, false,	
			MiscI.i().getSimpleTime(cd().btgShowMiliseconds.get())
				+cd().getDevInfoEntryPrefix()+"Console stats (Dev): "+"\n"
			
			+"Console Height = "+MiscI.i().fmtFloat(getSizeOf(getContainerMain()).y)+"\n"
			+"Visible Rows = "+getVisibleRows()+"\n"
			+"Line Wrap At = "+cd().getCurrentFixedLineWrapAtColumn()+"\n"
			+"ListBox Height = "+MiscI.i().fmtFloat(getSizeOf(lstbxDumpArea).y)+"\n"
			+"ListBox Entry Height = "+MiscI.i().fmtFloat(fLstbxEntryHeight)+"\n"
			
			+"Stats Text Field Height = "+MiscI.i().fmtFloat(getStatsHeight())+"\n"
			+"Stats Container Height = "+MiscI.i().fmtFloat(getSizeOf(ctnrStatsAndControls).y)+"\n"
			
			+"Input Field Height = "+MiscI.i().fmtFloat(getInputFieldHeight())+"\n"
			+"Input Field Final Height = "+MiscI.i().fmtFloat(getSizeOf(getIntputField()).y)+"\n"
			
			+"Slider Value = "+MiscI.i().fmtFloat(getScrollDumpAreaFlindex())+"\n"
			
			+"Slider Scroll request max retry attempts = "+iScrollRetryAttemptsMaxDBG);
	}
	
//	@Override
//	private void setConsoleMaxWidthInCharsForLineWrap(Integer paramInt) {
//		iConsoleMaxWidthInCharsForLineWrap=paramInt;
//	}
//	
//	@Override
//	private Integer getConsoleMaxWidthInCharsForLineWrap() {
//		return iConsoleMaxWidthInCharsForLineWrap;
//	}
	
	
	private boolean navigateHint(int iAdd){
		if(!isHintActive())return false;
		
		if(
				getSelectedHint()!=null
				||
				(cd().getCmdHistoryCurrentIndex()+1) >= cd().getCmdHistorySize() // end of cmd history
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
	
	enum ENav{
		Up,
		Down,
	}
	
	protected void navigateCmdHistOrHintBox(Object source, int iKeyCode) {
		ENav enav = null;
		switch(iKeyCode){
			case KeyInput.KEY_UP	:
				enav=ENav.Up;
				break;
			case KeyInput.KEY_DOWN:
				enav=ENav.Down;
				break;
		}		
		
		navigateCmdHistOrHintBox(source, enav);
	}
	
	private void navigateCmdHistOrHintBox(Object objSource, ENav enav) {
		if(cd().getCmdHistoryCurrentIndex()<0)cd().setCmdHistoryCurrentIndex(0); //cant underflow
		if(cd().getCmdHistoryCurrentIndex()>cd().getCmdHistorySize())cd().resetCmdHistoryCursor(); //iCmdHistoryCurrentIndex=getCmdHistorySize(); //can overflow by 1
		
		if(!objSource.equals(getIntputField()) && !objSource.equals(lstbxAutoCompleteHint)){
			objSource=null;
		}
		
		boolean bOk=false;
		switch(enav){
			case Up:
				if(objSource==null || objSource==lstbxAutoCompleteHint)bOk=navigateHint(-1);
				
				if((objSource==null && !bOk) || (objSource!=null && objSource.equals(getIntputField()))){
					cd().addCmdHistoryCurrentIndex(-1);
					/**
					 * to not lose last possibly typed (but not issued) cmd
					 */
					if(cd().getCmdHistoryCurrentIndex()==(cd().getCmdHistorySize()-1)){ //requested last entry
						strNotSubmitedCmd = dumpAndClearInputField();
					}
					fillInputFieldWithHistoryDataAtIndex(cd().getCmdHistoryCurrentIndex());
				}
				
				break;
			case Down:
				if(objSource==null || objSource==lstbxAutoCompleteHint)bOk=navigateHint(+1);
				
				if((objSource==null && !bOk) || objSource==getIntputField()){
					cd().addCmdHistoryCurrentIndex(1);
					if(cd().getCmdHistoryCurrentIndex()>=cd().getCmdHistorySize()){
						if(strNotSubmitedCmd!=null){
							setInputFieldText(strNotSubmitedCmd);
						}
					}
					fillInputFieldWithHistoryDataAtIndex(cd().getCmdHistoryCurrentIndex());
				}
				
				break;
		}
	}
	
	@Override
	public <BFR> BFR bugFix(Class<BFR> clReturnType, BoolTogglerCmdField btgBugFixId, Object... aobjCustomParams) {
		if(btgBugFixStatsLabelTextSize.isEqualToAndEnabled(btgBugFixId)){
			/**
			 * BugFix: because the related crash should/could be prevented/preventable.
			 * TODO this bugfix is slow right?
			 * 
			 * Buttons get smashed, shrinking to less than 0, they seem to not accept preferred size constraint.
			 */
			
			boolean bFailed=false;
			while(true){
				String str=getStatsText();
				if(str.isEmpty())break;
				
				boolean bCheckAlways=true; //safer
				if(!bCheckAlways){
					if(iStatsTextSafeLength!=null){
						if(str.length()>iStatsTextSafeLength){
							str=str.substring(0,iStatsTextSafeLength);
							setStatsText(str);
						}
						break;
					}
				}
				
				boolean bRetry=false;
				try{
					/**
					 * this is when crashes outside of here
					 */
					getContainerMain().updateLogicalState(0.05f);
					
					if(bFailed){ //had failed, so look for a safe length
						if(iStatsTextSafeLength==null || !iStatsTextSafeLength.equals(str.length())){
							iStatsTextSafeLength=str.length();
							cd().dumpDebugEntry("StatsTextSafeLength="+str.length());
						}
					}
				}catch(Exception ex){
					if(bExceptionOnce){
						cd().dumpExceptionEntry(ex);
						bExceptionOnce=false;
					}
					
					str=str.substring(0,str.length()-1);
					setStatsText(str);
					
					bRetry = true;
					bFailed = true;
				}
				
				if(!bRetry)break;
			}
		}
		
		return null;
	}
	
//	enum EBugFix{
//		StatsLabelTextSize,
//	}
//	@Override
//	public Object bugFix(Object... aobj) {
//		switch((EBugFix)aobj[0]){
//			case StatsLabelTextSize:{
//				/**
//				 * BugFix: because the related crash should/could be prevented/preventable.
//				 * 
//				 * Buttons get smashed, shrinking to less than 0, they seem to not accept preferred size constraint.
//				 * 
//				 * By fixating the size of the label, this crash preventer is not that necesary anymore.
//				 */
//				boolean bEnableThisCrashPreventer=false;if(!bEnableThisCrashPreventer)return null;
//				
//				boolean bFailed=false;
//				while(true){
//					String str=getStatsText();
//					if(str.isEmpty())break;
//					
//					boolean bCheckAlways=true; //safer
//					if(!bCheckAlways){
//						if(iStatsTextSafeLength!=null){
//							if(str.length()>iStatsTextSafeLength){
//								str=str.substring(0,iStatsTextSafeLength);
//								setStatsText(str);
//							}
//							break;
//						}
//					}
//					
//					boolean bRetry=false;
//					try{
//						/**
//						 * this is when crashes outside of here
//						 */
//						getContainerMain().updateLogicalState(0.05f);
//						
//						if(bFailed){ //had failed, so look for a safe length
//							if(iStatsTextSafeLength==null || !iStatsTextSafeLength.equals(str.length())){
//								iStatsTextSafeLength=str.length();
//								cd().dumpDebugEntry("StatsTextSafeLength="+str.length());
//							}
//						}
//					}catch(Exception ex){
//						if(bExceptionOnce){
//							cd().dumpExceptionEntry(ex);
//							bExceptionOnce=false;
//						}
//						
//						str=str.substring(0,str.length()-1);
//						setStatsText(str);
//						
//						bRetry = true;
//						bFailed = true;
//					}
//					
//					if(!bRetry)break;
//				}
//			}break;
//		}
//		
//		return null;
//	}
	
	private void updateStats(){
		if(!tdStatsRefresh.isReady(true))return;
		
		String str = cd().prepareStatsFieldText();
		
		if(DebugI.i().isKeyEnabled(DebugI.EDbgKey.StatsText))str+=cd().strDebugTest;
		
		setStatsText(str);
		
		bugFix(null,btgBugFixStatsLabelTextSize);
//		bugFix(EBugFix.StatsLabelTextSize);
	}	
	
	private void updateAutoCompleteHint() {
		String strInputText = getInputText();
		if(strInputText.isEmpty())return;
		strInputText=cd().extractCommandPart(strInputText,0);
		if(strInputText==null)return; //something invalid was typed...
		if(bKeyControlIsPressed && bKeyShiftIsPressed){
			/**
			 * user asked for 'contains' override even if there are hints already.
			 */
		}else
		if(lstbxAutoCompleteHint.getParent()==null && bKeyControlIsPressed){
			/**
			 * in this case, the hint list is empty,
			 * and match mode: "contains" was requested by user.
			 */
		}else{
			if(strInputTextPrevious.equals(strInputText))return;
		}
		
		AutoCompleteResult acr = AutoCompleteI.i().autoComplete(
			strInputText, cd().getAllPossibleCommands(), bKeyControlIsPressed, true);
		
//		if(acr.bUsingFuzzy){
//			cd().dumpWarnEntry("using FUZZY match for: "+strInputText);
//		}
		
		boolean bShowHint = false;
		
		if(acr.astr.size()==0){
			bShowHint=false; // empty string, or simply no matches
		}else
		if(acr.astr.size()==1 && strInputText.equals(cd().extractCommandPart(acr.astr.get(0),0))){
			// no extra matches found, only what was already typed was returned
			bShowHint=false;
		}else{
			bShowHint=true; // show all extra matches
		}
		
		if(bShowHint){
			for(int i=0;i<acr.astr.size();i++){
				String str=acr.astr.get(i);
				int iNL = str.indexOf("\n");
				if(iNL>=0){
					acr.astr.set(i,str.substring(0,iNL));
				}
			}
			
			if(acr.bUsingFuzzy){
				for(int i=0;i<acr.astr.size();i++){
					acr.astr.set(i, 
						acr.astr.get(i)+" "+cd().getCommentPrefixStr()+cd().getFuzzyFilterModeToken());
				}
			}
			
			vlstrAutoCompleteHint.clear();
			vlstrAutoCompleteHint.addAll(acr.astr);
			clearHintSelection();
			
			Node nodeParent = getNodeGUI();
			if(!nodeParent.hasChild(lstbxAutoCompleteHint)){
				nodeParent.attachChild(lstbxAutoCompleteHint);
			}
			
			//lstbxAutoCompleteHint.setLocalTranslation(new Vector3f(0, -fInputHeight, 0));
			Vector3f v3f = getIntputField().getWorldTranslation().clone();
			v3f.y -= getSizeOf(getIntputField()).y;
			lstbxAutoCompleteHint.setLocalTranslation(v3f);
			
			float fEntryHeightGUESSED = getInputFieldHeight(); //TODO should be the listbox entry height
			float fAvailableHeight = v3fApplicationWindowSize.y -v3fConsoleSize.y -fEntryHeightGUESSED;
			int iVisibleItems = (int) (fAvailableHeight/fEntryHeightGUESSED);
			if(iVisibleItems==0)iVisibleItems=1;
			if(iVisibleItems>vlstrAutoCompleteHint.size())iVisibleItems=vlstrAutoCompleteHint.size();
			float fHintHeight = fEntryHeightGUESSED*iVisibleItems;
			if(fHintHeight>fAvailableHeight){
				cd().dumpDevWarnEntry("fHintHeight="+fHintHeight+",fAvailableHeight="+fAvailableHeight);
				fHintHeight=fAvailableHeight;
			}
			int iMinLinesGUESSED = 3; //seems to be required because the slider counts as 3 (up arrow, thumb, down arrow)
			float fMinimumHeightGUESSED = fEntryHeightGUESSED*iMinLinesGUESSED;
			if(fHintHeight<fMinimumHeightGUESSED)fHintHeight=fMinimumHeightGUESSED;
			setHintBoxSize(new Vector3f(widthForListbox(),fHintHeight,0), iVisibleItems);
			
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
		int iMaxSliderIndex=vlstrDumpEntries.size()-getVisibleRows();
		
		return "Sl"
				+MiscI.i().fmtFloat(getScrollDumpAreaFlindex(),0)+"/"+iMaxSliderIndex+"+"+getVisibleRows()
				+"("+MiscI.i().fmtFloat(100.0f -getDumpAreaSliderPercent()*100f,0)+"%)"
				+";";

	}
	
//	@Override
//	private int getCmdHistoryCurrentIndex() {
//		return iCmdHistoryCurrentIndex;
//	}
	
	@Override
	public int getLineWrapAt() {
//		updateFontStuff();

		boolean bUseFixedWrapColumn = cd().btgUseFixedLineWrapModeForAllFonts.b();
		
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
	
	private void updateFontStuff(){
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
	public ArrayList<String> wrapLineDynamically(DumpEntryData de) {
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
	
	public boolean isHintBox(Spatial target) {
		return target==lstbxAutoCompleteHint;
	}
	public boolean isScrollRequestTarget(Spatial target) {
		return target == sptScrollTarget;
	}
	public void setScrollRequestTarget(Spatial target) {
		this.sptScrollTarget = target;
	}
	
//	@Override
//	public void recreateConsoleGui() {
//		if(rss.isProcessingRequest()){
//			cd().dumpWarnEntry("Console recreation request is already being processed...");
//			return;
//		}
//		
////		Callable<Void> detach = new Callable<Void>() {
////			@Override
////			public Void call() throws Exception {
////				app().getStateManager().detach(ConsoleJmeStateAbs.this);
////				return null;
////			}
////		};
////		
////		final boolean bWasEnabled=isEnabled();
////		Callable<Void> attach = new Callable<Void>() {
////			@Override
////			public Void call() throws Exception {
////				app().getStateManager().attach(ConsoleJmeStateAbs.this);
////				if(bWasEnabled){
////					setEnabledRequest(true);
////				}
////				return null;
////			}
////		};
//		
//		Callable<Void> postInitialization = new Callable<Void>() {
//			@Override
//			public Void call() throws Exception {
//				modifyConsoleHeight(fConsoleHeightPerc);
//				scrollToBottomRequest();
//				return null;
//			}
//		};
//		
////		rss.request(detach,attach,postInitialization);
//		rss.request(postInitialization);
//	}
	
	/**
	 * TODO WIP, not working yet... may be it is not possible to convert at all yet?
	 * @param ttf
	 * @return
	 */
	private BitmapFont convertTTFtoBitmapFont(TrueTypeFont ttf){
		String strGlyphs="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789./*-+?\\;\"!@#$*()&^%";
		TrueTypeBitmapGlyph attbg[] = ttf.getBitmapGlyphs(strGlyphs);
		BitmapFont font = new BitmapFont();
		BitmapCharacterSet bcs = new BitmapCharacterSet();
		int iMaxHeight = -1;
		int iMaxWidth = -1;
		for(TrueTypeBitmapGlyph ttbg : attbg){
			BitmapCharacter bc = new BitmapCharacter();
			char ch = ttbg.getCharacter().charAt(0);
			bc.setChar(ttbg.getCharacter().charAt(0));
			
			bc.setWidth(ttbg.w);
			bc.setHeight(ttbg.h);
			
			bc.setX(ttbg.x);
			bc.setY(ttbg.y);
			
			bc.setXAdvance(ttbg.xAdvance);
			bc.setXOffset(ttbg.getHeightOffset());
			bc.setYOffset(ttbg.y);
			
			bcs.addCharacter(ch, bc);
			
			if(bc.getHeight()>iMaxHeight)iMaxHeight=bc.getHeight();
			if(bc.getWidth()>iMaxWidth)iMaxWidth=bc.getWidth();
		}
		font.setCharSet(bcs);
		
		Texture2D t2d = ttf.getAtlas();
//		Image imgAtlas = t2d.getImage();
//		Image imgTmp = imgAtlas.clone();
//		imgTmp.getData(0).rewind();
//		imgTmp.setData(0, imgTmp.getData(0).asReadOnlyBuffer());
//		MiscI.i().saveImageToFile(imgTmp,"temp"+ttf.getFont().getName().replace(" ",""));
		if(DebugI.i().isKeyEnabled(EDbgKey.DumpFontImg)){ //EDbgKey.values()
			//TODO why image file ends empty??
			MiscJmeI.i().saveImageToFile(t2d.getImage(),
				EDbgKey.DumpFontImg.toString()+ttf.getFont().getName().replace(" ",""));
		}
		
		bcs.setBase(iMaxHeight); //TODO what is this!?
//		bcs.setBase(ttf.getFont().getSize()); 
		bcs.setHeight(t2d.getImage().getHeight());
		bcs.setLineHeight(iMaxHeight);
		bcs.setWidth(t2d.getImage().getWidth());
		bcs.setRenderedSize(iMaxHeight);
//		bcs.setStyle(style);
		
		/**
		 * TODO why this fails? missing material's "colorMap" ...
		font.setPages(new Material[]{ttf.getBitmapGeom("A", ColorRGBA.White).getMaterial()});
		 */
//		font.setPages(new Material[]{fontConsoleDefault.getPage(0)});
//		Material mat = ttf.getBitmapGeom(strGlyphs, ColorRGBA.White).getMaterial();
//		Material mat = fontConsoleDefault.getPage(0).clone();
		Material mat = fontConsoleExtraDefault.getPage(0).clone();
		mat.setTexture("ColorMap", t2d); //TODO wow, weird results from this...
//		mat.setTexture("ColorMap", ttf.getAtlas());
//		mat.setParam("ColorMap", VarType.Texture2D, ttf.getAtlas());
		font.setPages(new Material[]{mat});
		
//		Material m = new Material();
//		m.setp
		
//		font.getCharSet().getCharacter(33);
//		fontConsoleDefault.getCharSet().getCharacter(35).getChar();
		
//		Material[] amat = new Material[fontConsoleDefault.getPageSize()];
		
//	ttf.getAtlas();
		
		/**
		 * 
		 * check for missing glyphs?
		private boolean hasContours(String character) {
	    GlyphVector gv = font.createGlyphVector(frc, character);
	    GeneralPath path = (GeneralPath)gv.getOutline();
	    PathIterator pi = path.getPathIterator(null);
	    if (pi.isDone())
	        return false;
	    
	    return true;
		}
		 */
		
		//app().getAssetManager().unregisterLocator(fontFile.getParent(), FileLocator.class);
		return font;
	}
	
	/**
	 * TODO this is probably not to be used...
	 */
	public static class TrueTypeFontFromSystem extends TrueTypeFont{
		public TrueTypeFontFromSystem(AssetManager assetManager, Font font, int pointSize, int outline) {
    	super(assetManager, font, pointSize, outline);
    }
	}
	
	/**
	 * TODO this is not working
	 * @param strFontID
	 * @param iFontSize
	 * @return
	 */
	private BitmapFont fontFromTTF(String strFontID, int iFontSize){
		if(true)return null; //TODO dummified
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font fntFound=null;
		for(Font fnt:ge.getAllFonts()){
			if(fnt.getFontName().toLowerCase().equalsIgnoreCase(strFontID)){
				fntFound=fnt;
				break;
			}
		}
		
		if(fntFound==null)return null;
		
		cd().dumpInfoEntry("System font: "+strFontID);
		
		//TODO this is probably wrong...
		TrueTypeKey ttk = new TrueTypeKey(strFontID,0,iFontSize,1);
		fntFound = fntFound.deriveFont(ttk.getStyle(), ttk.getPointSize());
		
		/**
		 * TODO how to directly get a system Font and create a TrueTypeFont without loading it with the file? 
		 */
		return convertTTFtoBitmapFont(
			new TrueTypeFontFromSystem(
				app().getAssetManager(), 
				fntFound,
				ttk.getPointSize(),
				ttk.getOutline()
			));
	}
	
	private BitmapFont fontFromTTFFile(String strFilePath, int iFontSize){
		File fontFile = new File(strFilePath);
		
		if(fontFile.getParent()==null)return null; //not a file with path
		
		app().getAssetManager().registerLocator(fontFile.getParent(), FileLocator.class);
		
		TrueTypeKey ttk = new TrueTypeKey(strFilePath, java.awt.Font.PLAIN, iFontSize);
		
		TrueTypeFont ttf=null;
		try{
			ttf = (TrueTypeFont)app().getAssetManager().loadAsset(ttk);
		}catch(AssetNotFoundException|IllegalArgumentException ex){
			// missing file
			cd().dumpExceptionEntry(ex);
		}
		
		app().getAssetManager().unregisterLocator(fontFile.getParent(), FileLocator.class);
		
		if(ttf==null)return null;
		
		cd().dumpInfoEntry("Font from file: "+strFilePath);
		
		return convertTTFtoBitmapFont(ttf);
	}
	
	/**
	 * 
	 * @param strFilter can be null
	 * @return
	 */
	public ArrayList<String> getSystemFontList(String strFilter){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ArrayList<String> astr = new ArrayList<String>();
		for(Font fnt:ge.getAllFonts()){
			if(strFilter==null || fnt.getFontName().toLowerCase().contains(strFilter)){
				astr.add(fnt.getFontName());
			}
		}
		return astr;
	}
	
	public void prepareStyle() {
		String strFontName=svfUserFontOption.getStringValue();
	//	if(bConsoleStyleCreated)return;
		
		font=null;
		try{
			if(font==null){ //system font object
				font = fontFromTTF(strFontName,ilvFontSize.intValue());
			}
			
			if(font==null){ //custom font file
				font = fontFromTTFFile(strFontName,ilvFontSize.intValue());
			}
			
			if(font==null){ //bundled fonts
				strFontName=strFontName.replace(" ","");
				String strFile = "Interface/Fonts/"+strFontName+".fnt";
				font = app().getAssetManager().loadFont(strFile);
				if(font!=null)cd().dumpInfoEntry("Bundled font: "+strFile);
			}
		}catch(AssetNotFoundException ex){
			cd().dumpExceptionEntry(ex);
			font = fontConsoleDefault; //fontConsoleExtraDefault
//			strFontName="Console";
//			font = app().getAssetManager().loadFont("Interface/Fonts/"+strFontName+".fnt");
//		svUserFontOption.setObjectValue(strFontName);
			svfUserFontOption.setObjectValue(strConsoleDefaultFontName );
		}
	//	BitmapFont font = app().getAssetManager().loadFont("Interface/Fonts/Console512x.fnt");
		
//		updateFontStuff();
	}

	protected void setDumpEntriesSlowedQueue(AbstractList<String> vlstrDumpEntriesSlowedQueue) {
		this.vlstrDumpEntriesSlowedQueue = vlstrDumpEntriesSlowedQueue;
	}
	
	public boolean isFullyInitialized(){
		return bFullyInitialized;
	}
	
	public void setHintBox(Node listBox) {
		this.lstbxAutoCompleteHint=listBox;
	}
	
	public Node getHintBox() {
		return lstbxAutoCompleteHint;
	}
	
	@Override
	protected boolean disableOrUndo() {
		bKeepInitiallyInvisibleUntilFirstClosed=false; //"first close" is the hint
		bFullyInitialized=true; //it will only be completely initialized after the 1st close...
		
		return super.disableOrUndo();
	}
	
	public void setStatsAndControls(Node container) {
		this.ctnrStatsAndControls = container;
	}
	
	public Node getStatsAndControls() {
		return ctnrStatsAndControls;
	}

	@Override
	public void clearSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateInputField() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DialogListEntryData<T> getSelectedEntryData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateList() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateTextInfo() {
		// TODO Auto-generated method stub
	}
	
	public String getStyle() {
		return strStyle;
	}
	
	public int getShowRowsAmount() {
		return iShowRows;
	}
	
	protected R setShowRowsAmount(Integer i) {
		this.iShowRows=i;
		return getThis();
	}
	public Integer getVisibleRowsAdjustRequest() {
		return iVisibleRowsAdjustRequest;
	}
	@Override
	public void setVisibleRowsAdjustRequest(Integer i){
		iVisibleRowsAdjustRequest=i;
	}
	public Float getLstbxEntryHeight() {
		return fLstbxEntryHeight;
	}
	protected R setLstbxEntryHeight(Float fLstbxEntryHeight) {
		this.fLstbxEntryHeight = fLstbxEntryHeight;
		return getThis();
	}
	@Override
	public AbstractList<String> getDumpEntries(CompositeControlAbs<?> ccTrustedManipulator) {
		chkCfgTrustedManipulator(ccTrustedManipulator);
		return vlstrDumpEntries;
	}
	protected AbstractList<String> getDumpEntries() {
		return vlstrDumpEntries;
	}
	protected R setDumpEntries(AbstractList<String> vlstrDumpEntries) {
		this.vlstrDumpEntries = vlstrDumpEntries;
		return getThis();
	}

	@Override
	public AbstractList<String> getDumpEntriesSlowedQueue(CompositeControlAbs<?> ccTrustedManipulator) {
		chkCfgTrustedManipulator(ccTrustedManipulator);
		return vlstrDumpEntriesSlowedQueue;
	}
	
//	@Override
//	public AbstractList<String> getAutoCompleteHintList() {
//		return vlstrAutoCompleteHint;
//	}

	private void chkCfgTrustedManipulator(CompositeControlAbs<?> ccTrustedManipulator) {
		ccTrustedManipulator.assertSelfNotNull();
		
		if(this.ccTrustedManipulator==null){
			this.ccTrustedManipulator=ccTrustedManipulator; //configure
		}else{
			if(this.ccTrustedManipulator!=ccTrustedManipulator){
				throw new PrerequisitesNotMetException("Only the first manipulator is allowed to have further access", this.ccTrustedManipulator, ccTrustedManipulator);
			}
		}
	}
	public float getLstbxHeight() {
		return fLstbxHeight;
	}
	protected void setLstbxHeight(float fLstbxHeight) {
		this.fLstbxHeight = fLstbxHeight;
	}
	protected Node getLstbxDumpArea() {
		return lstbxDumpArea;
	}
	protected void setLstbxDumpArea(Node lstbxDumpArea) {
		this.lstbxDumpArea = lstbxDumpArea;
	}
	
	public boolean isKeyShiftIsPressed() {
		return bKeyShiftIsPressed;
	}
	
	protected void setKeyShiftIsPressed(boolean bKeyShiftIsPressed) {
		this.bKeyShiftIsPressed = bKeyShiftIsPressed;
	}
	protected BitmapFont getFont() {
		return font;
	}
	protected R setFont(BitmapFont font) {
		this.font = font;
		return getThis();
	}
	protected BitmapFont getFntMakeFixedWidth() {
		return fntMakeFixedWidth;
	}
	protected void setFntMakeFixedWidth(BitmapFont fntMakeFixedWidth) {
		this.fntMakeFixedWidth = fntMakeFixedWidth;
	}
	protected AbstractList<String> getHintList() {
		return vlstrAutoCompleteHint;
	}
	protected void setHintList(AbstractList<String> vlstrAutoCompleteHint) {
		this.vlstrAutoCompleteHint = vlstrAutoCompleteHint;
	}
	public Vector3f getConsoleSizeCopy() {
		return v3fConsoleSize.clone();
	}
	protected void setConsoleSize(Vector3f v3fConsoleSize) {
		this.v3fConsoleSize = v3fConsoleSize;
	}
	protected Node getSliderDumpArea() {
		return sliderDumpArea;
	}
	protected void setSliderDumpArea(Node sliderDumpArea) {
		this.sliderDumpArea = sliderDumpArea;
	}
	protected abstract Float getStatsHeight();
	
}

