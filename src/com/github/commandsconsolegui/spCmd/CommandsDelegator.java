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

package com.github.commandsconsolegui.spCmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.ManageKeyCode.Key;
import com.github.commandsconsolegui.spAppOs.globals.GlobalManageKeyCodeI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalOSAppI;
import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.DiscardableInstanceI;
import com.github.commandsconsolegui.spAppOs.misc.IHandleExceptions;
import com.github.commandsconsolegui.spAppOs.misc.IMessageListener;
import com.github.commandsconsolegui.spAppOs.misc.IMultiInstanceOverride;
import com.github.commandsconsolegui.spAppOs.misc.ISingleInstance;
import com.github.commandsconsolegui.spAppOs.misc.IUserInputDetector;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.DebugData;
import com.github.commandsconsolegui.spAppOs.misc.ManageDebugDataI.EDbgStkOrigin;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MiscI.EStringMatchMode;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI.PseudoException;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.RunMode;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.globals.GlobalConsoleUII;
import com.github.commandsconsolegui.spCmd.globals.GlobalManageKeyBindI;
import com.github.commandsconsolegui.spCmd.misc.DebugI;
import com.github.commandsconsolegui.spCmd.misc.DebugI.EDebugKey;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spCmd.misc.RegisteredClasses;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdFieldAbs;
import com.github.commandsconsolegui.spCmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.spCmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.spCmd.varfield.KeyBoundVarField;
import com.github.commandsconsolegui.spCmd.varfield.ManageVarCmdFieldI;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spCmd.varfield.StringVarField;
import com.github.commandsconsolegui.spCmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.spCmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.spCmd.varfield.VarCmdUId;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * All methods starting with "cmd" are directly accessible by user console commands.
 * Here are all base command related methods.
 * 
 * No instance for this, the user must create such instance to use anyplace needed,
 * so specialized methods will be recognized properly. Class name ends with I to 
 * indicate that!
 * Use {@link GlobalCommandsDelegatorI} to set and access such instance.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class CommandsDelegator implements IReflexFillCfg, ISingleInstance, IHandleExceptions, IMessageListener, IUserInputDetector{ //, IManager<VarCmdFieldAbs>{
	public static final class CompositeControl extends CompositeControlAbs<CommandsDelegator>{
		private CompositeControl(CommandsDelegator cc){super(cc);};
	};private CompositeControl ccSelf = new CompositeControl(this);
//	public static final T instance;
	
	
	/**
	 * TODO temporary variable used only during methods migration, commands class must not depend/know about state class.
	 */
//	public ConsoleGuiStateAbs csaTmp = null;
	
//	private SimpleApplication	sapp;
	
	// not protected... development token... 
	public final String	TOKEN_CMD_NOT_WORKING_YET = "[NOTWORKINGYET]";
	
	private boolean bAllowUserCmdOS = false;
	
	CurrentCommandLine ccl = new CurrentCommandLine(this);
	
	private final KeyBoundVarField bindFastExit = new KeyBoundVarField(this)
		.setHelp("the developer's debug mode most helpful key-binding!")
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				cmdRequestCleanSafeNormalExit();
				return true;
			}
		});
	
	/**
	 * Togglers:
	 * 
	 * Adding a toggler field on any class, 
	 * will automatically create the related console command!
	 */
//	public final BoolToggler	btgAcceptExternalExitRequests = new BoolToggler(this,false,strTogglerCodePrefix, 
//		"if a ");
	public final BoolTogglerCmdField	btgDbAutoBkp = new BoolTogglerCmdField(this,false, 
		"whenever a save happens, if the DB was modified, a backup will be created of the old file").setCallNothingOnChange();
	public final BoolTogglerCmdField	btgShowWarn = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgShowInfo = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgShowException = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgValidateDevCode = new BoolTogglerCmdField(this,RunMode.bValidateDevCode)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				RunMode.bValidateDevCode=btgValidateDevCode.b();
				return true;
			}
		});
	public final BoolTogglerCmdField	btgDumpToTerminal = new BoolTogglerCmdField(this,true,
		"The system terminal where the application is being run, will also receive "+CommandsDelegator.class.getSimpleName()+" output.").setCallNothingOnChange();
	public final BoolTogglerCmdField	btgEngineStatsView = new BoolTogglerCmdField(this,false);
	public final BoolTogglerCmdField	btgEngineStatsFps = new BoolTogglerCmdField(this,false);
	public final BoolTogglerCmdField	btgShowMiliseconds=new BoolTogglerCmdField(this,false).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgShowConvertedCommandInfo=new BoolTogglerCmdField(this,false).setCallNothingOnChange();
//	public final BoolTogglerCmdField	btgStringContainsFuzzyFilter=new BoolTogglerCmdField(this,true);
	public final BoolTogglerCmdField	btgConsoleCpuRest=new BoolTogglerCmdField(this,false,
		"Console update steps will be skipped if this is enabled.");
	public final BoolTogglerCmdField	btgAutoScroll=new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgUseFixedLineWrapModeForAllFonts=new BoolTogglerCmdField(this,false,
		"If enabled, this will use a fixed line wrap column even for non mono spaced fonts, "
		+"based on the width of the 'W' character. Otherwise it will dynamically guess the best "
		+"fitting string size.").setCallNothingOnChange();
	
	/**
	 * Developer vars, keep together!
	 * Initialy true, the default init will disable them.
	 */
	public final BoolTogglerCmdField	btgShowDebugEntries=new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgShowDeveloperInfo=new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgShowDeveloperWarn=new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	public final BoolTogglerCmdField	btgShowExecQueuedInfo=new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	
	/**
	 * keep delayers together!
	 */
	private final TimedDelayVarField tdLetCpuRest = new TimedDelayVarField(this,0.1f,"updates will be skipped and only one update will be processed per delay, if this is active");
	private final TimedDelayVarField tdDumpQueuedSlowEntry = new TimedDelayVarField(this,1f/5f,"how many dump entries will be shown per second (from the slow queue)");
//	private final TimedDelayVarField tdSpareGpuFan = new TimedDelayVarField(this,1.0f/60f); // like 60 FPS
	
	/**
	 * used to hold a reference to the identified/typed user command
	 */
	private BoolTogglerCmdFieldAbs	btgReferenceMatched;
	
	/**
	 * user can type these below at console (the actual commands are prepared by reflex)
	 */
//	public final StringField CMD_CLOSE_CONSOLE = new StringField(this,CommandsHelperI.i().getCmdCodePrefix());
//	public final StringField CMD_CONSOLE_HEIGHT = new StringField(this,CommandsHelperI.i().getCmdCodePrefix());
//	public final StringField CMD_CONSOLE_SCROLL_BOTTOM = new StringField(this,CommandsHelperI.i().getCmdCodePrefix());
//	public final StringField CMD_CONSOLE_STYLE = new StringField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField scfBindKey = new StringCmdField(this);
	public final StringCmdField scfUnBindKey = new StringCmdField(this);
	public final StringCmdField scfAddKeyCodeMonitor = new StringCmdField(this);
	public final StringCmdField scfBindList = new StringCmdField(this);
	public final StringCmdField CMD_CONSOLE_SCROLL_BOTTOM = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_CLEAR_COMMANDS_HISTORY = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_DB = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_ECHO = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField scfListFiles = new StringCmdField(this);
	public final StringCmdField scfListKeyCodes = new StringCmdField(this);
//	public final StringCmdField scfTestException = new StringCmdField(this);
	public final StringCmdField scfMessagesBufferClear = new StringCmdField(this);
	public final StringCmdField scfChangeCommandSimpleId = new StringCmdField(this);
	public final StringCmdField CMD_FIX_LINE_WRAP = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_FIX_VISIBLE_ROWS_AMOUNT = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField scfHelp = new StringCmdField(this);
	public final StringCmdField CMD_HISTORY = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_HK_TOGGLE = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_LINE_WRAP_AT = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField scfMessageReview = new StringCmdField(this);
	public final StringCmdField CMD_VAR_SET = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_SLEEP = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_STATS_ENABLE = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField CMD_REPEAT = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField	CMD_STATS_FIELD_TOGGLE  = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField	CMD_STATS_SHOW_ALL = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField	scfDevTest = new StringCmdField(this);
	public final StringCmdField	CMD_VAR_ADD = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField	CMD_VAR_SET_CMP = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField	scfVarShow = new StringCmdField(this);
	public final StringCmdField	CMD_RESET = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField	CMD_SHOW_SETUP = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField scfClearDumpArea = new StringCmdField(this);
	public final StringCmdField scfAlias = new StringCmdField(this);
	public final StringCmdField scfCmdOS = new StringCmdField(this);
	public final StringCmdField scfFileShowData = new StringCmdField(this);
	public final StringCmdField scfFixAllCmdsConflictsAutomatically = new StringCmdField(this);
	public final StringCmdField scfExit = new StringCmdField(this);
	public final StringCmdField scfExecBatchCmdsFromFile = new StringCmdField(this);
	public final StringCmdField scfEditCut = new StringCmdField(this);
	public final StringCmdField scfEditCopy = new StringCmdField(this);
	public final StringCmdField scfEditShowClipboad = new StringCmdField(this);
	public final StringCmdField scfShowCommandsSimpleIdConflicts = new StringCmdField(this);
	public final StringCmdField scfQuit = new StringCmdField(this);
	
	/**
	 * this char indicates something that users (non developers) 
	 * should not have direct access.
	 */
	public final StringCmdField	RESTRICTED_CMD_SKIP_CURRENT_COMMAND	= new StringCmdField(this,CommandsHelperI.i().getRestrictedCmdCodePrefix());
	public final StringCmdField	RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE	= new StringCmdField(this,CommandsHelperI.i().getRestrictedCmdCodePrefix());
	public final StringCmdField	RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS	= new StringCmdField(this,CommandsHelperI.i().getRestrictedCmdCodePrefix());
	public final StringCmdField	RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS	= new StringCmdField(this,CommandsHelperI.i().getRestrictedCmdCodePrefix());
	
	/**
	 * more tokens
	 */
	private char	chCommandDelimiter;
	private char	chAliasPrefix;
	private char	chVariableExpandPrefix;
	/** any place that accepts a filter, when starting with this token, will run as fuzzy filter */
	private char	chFuzzyFilterModeToken;
	private	char chAliasBlockedToken;
	private char	chAliasAllowedToken;
	private	char chVarDeleteToken;
	private	char	chCommentPrefix;
	private	char	chCommandPrefix;
	private char chCopyRangeIndicator;
	private String strCopyRangeIndicator;
	
	/**
	 * etc
	 */
	
	/** 0 is auto wrap, -1 will trunc big lines */
	private final IntLongVarField ilvConsoleMaxWidthInCharsForLineWrap = new IntLongVarField(this,0,null);
	
	private final IntLongVarField ilvCurrentFixedLineWrapAtColumn = new IntLongVarField(this,0,null);
	private HashMap<IConsoleCommandListener,StackTraceElement[]> hmDebugListenerAddedStack = new HashMap<IConsoleCommandListener,StackTraceElement[]>();
	
	private boolean	bAddEmptyLineAfterCommand = true;
//	private IConsoleUI	icui;
	private boolean bStartupCmdQueueDone = false; 
//	private CharSequence	strReplaceTAB = "  ";
	private int	iCopyFrom = -1;
	private int	iCopyTo = -1;
	private int	iCmdHistoryCurrentIndex = 0;
//	private String	strStatsLast = "";
	private boolean	bLastAliasCreatedSuccessfuly;
	private float	fTPF;
	private long	lNanoFrameTime;
	private long	lNanoFpsLimiterTime;
	private String	strFilePrefix = "Console"; //ConsoleStateAbs.class.getSimpleName();
	private String	strFileTypeLog = "log";
	private String	strFileTypeConfig = "cfg";
	private String	strFileCmdHistory = strFilePrefix+"-CmdHist";
	private String	strFileLastDump = strFilePrefix+"-LastDump";
	private String	strFileInitConsCmds = strFilePrefix+"-Init";
	private String	strFileSetup = strFilePrefix+"-Setup";
	private String	strFileDatabase = strFilePrefix+"-DB";
	private final IntLongVarField ilvMaxCmdHistSize = new IntLongVarField(this,1000,null);
	private final IntLongVarField ilvMaxDumpEntriesAmount = new IntLongVarField(this,100000,"max dump area list size before older ones get removed");
//	private ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
	
	/**
	 * Must be an array (not a hashmap), because the newest entries will remain longer on the buffer.
	 */
	private ArrayList<ImportantMsgData>	aimBufferList = new ArrayList<ImportantMsgData>();
	
	private ArrayList<String>	astrExecConsoleCmdsQueue = new ArrayList<String>();
	private ArrayList<PreQueueCmdsBlockSubListData>	astrExecConsoleCmdsPreQueue = new ArrayList<PreQueueCmdsBlockSubListData>();
//	private String	strCmdLinePrepared = "";
	private TreeMap<String,ConsoleVariable> tmUserVariables = 
		new TreeMap<String, ConsoleVariable>(String.CASE_INSENSITIVE_ORDER);
	private TreeMap<String,ConsoleVariable> tmRestrictedVariables =
		new TreeMap<String, ConsoleVariable>(String.CASE_INSENSITIVE_ORDER);
	private File	flCmdHist;
	private File	flLastDump;
	private File	flInit;
	private File	flDB;
	private File	flSetup;
	private ArrayList<DumpEntryData> adeDumpEntryFastQueue = new ArrayList<DumpEntryData>();
	private String	strInfoEntryPrefix			=". ";
	private String	strWarnEntryPrefix			="?Warn: ";
	private String	strErrorEntryPrefix			="!ERROR: ";
	private String	strExceptionEntryPrefix	="!EXCEPTION: ";
	private String	strDevWarnEntryPrefix="?DevWarn: ";
	private String	strDevInfoEntryPrefix=". DevInfo: ";
	private String	strSubEntryPrefix="\t";
//	private String	strCmdLineOriginal;
	private ArrayList<String> astrCmdHistory = new ArrayList<String>();
//	private ArrayList<String> astrCmdWithCmtValidList = new ArrayList<String>();
//	private ArrayList<String> astrBaseCmdValidList = new ArrayList<String>();
	private ArrayList<AliasData> aAliasList = new ArrayList<AliasData>();
//	private ArrayList<CommandData> acmdList = new ArrayList<CommandData>();
	private TreeMap<String,CommandData> trmCmddList = new TreeMap<String,CommandData>(String.CASE_INSENSITIVE_ORDER);
//	LinkedHashMap<String,CommandData> asdf = new LinkedHashMap<String,CommandData>(String.CASE_INSENSITIVE_ORDER);
	
	private class PreQueueCmdsBlockSubListData{
		private TimedDelayVarField tdSleep = null; //private class, no need to be final..
		private String strUId = MiscI.i().getNextUniqueId();
		private ArrayList<String> astrCmdList = new ArrayList<String>();
		private boolean	bPrepend = false;
		private boolean bForceFailBlockExecution = false;
		private boolean	bInfoSleepBegin = false;
		private String	strBlockInfo;
		
		private String getUniqueInfo(){
			return "UId=\""+strUId+"\","+strBlockInfo;
		}
	}
	
	public CommandsDelegator() {
		DelegateManagerI.i().addHandled(this);
//		ManageSingleInstanceI.i().add(this);
		
		rsc.addClassesOf(this, true, true);
		
		setCommandDelimiter(';');
		setAliasPrefix('$');
		setVariableExpandPrefix(chAliasPrefix);
		/** any place that accepts a filter, when starting with this token, will run as fuzzy filter */
		setFuzzyFilterModeToken('~');
		setAliasBlockedToken('-');
		setAliasAllowedToken('+');
		setVarDeleteToken('-');
		setCommentPrefix('#');
		setCommandPrefix('/');
		setCopyRangeIndicator((char)182); //TODO describe what char is this...
	}
	
	/**
	 * Here will be setup basic variants for some {@link VarCmdFieldAbs}
	 */
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {
		ReflexFillCfg rfcfg = null;
		
//		boolean bCodePrefixIsDefault = rfcv.isCodePrefixVariantEqualDefault();
		boolean bCodePrefixIsDefault = rfcvField.getCodePrefixDefault().equals(rfcvField.getCodePrefixVariant());
		if(rfcvField.getClass().isAssignableFrom(BoolTogglerCmdField.class)){
//			if(BoolTogglerCmdField.getCodePrefixDefault().equals(rfcv.getCodePrefixVariant())){
			if(bCodePrefixIsDefault){
				rfcfg = new ReflexFillCfg(rfcvField);
				rfcfg.setSuffix("Toggle");
			}
			
			if(rfcfg!=null)rfcfg.setAsCommandToo(true);
		}else
		if(rfcvField.getClass().isAssignableFrom(KeyBoundVarField.class)){
			if(bCodePrefixIsDefault){
				rfcfg = new ReflexFillCfg(rfcvField);
				rfcfg.setSuffix("Bind"); // this is important to help on using filters at vars dialog
			}
			
			if(rfcfg!=null)rfcfg.setAsCommandToo(true);
		}else
		if(rfcvField.getClass().isAssignableFrom(TimedDelayVarField.class)){
			if(bCodePrefixIsDefault){
				rfcfg = new ReflexFillCfg(rfcvField);
				rfcfg.setSuffix("TimedDelay");
			}
			
			if(rfcfg!=null)rfcfg.setAsCommandToo(true);
		}else
		if(rfcvField.getClass().isAssignableFrom(StringCmdField.class)){
			if(CommandsHelperI.i().getCmdCodePrefix().equals(rfcvField.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg(rfcvField);
			}else
			if(CommandsHelperI.i().getRestrictedCmdCodePrefix().equals(rfcvField.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg(rfcvField);
				rfcfg.setPrefixCmd(""+CommandsHelperI.i().getRestrictedToken());
			}else
			if(bCodePrefixIsDefault){
				rfcfg = new ReflexFillCfg(rfcvField);
			}
			
			if(rfcfg!=null)rfcfg.setAsCommandToo(true);
		}
//		StringVarField
//		FloatDoubleVarField
//		IntLongVarField
		
		if(rfcfg!=null){
			if(rfcfg.isCommandToo()){
				rfcfg.setPrefixCmd(ReflexFillI.i().getPrefixCmdDefault());
//				Field field = ReflexFillI.i().assertAndGetField(rfcv.getOwner(), rfcv);
////			rfcfg.setPrefix("cmd"+cl.getSimpleName());
//				rfcfg.setPrefixDeclaringClass(field.getDeclaringClass().getSimpleName());
//				rfcfg.setPrefixInstancedClass(rfcv.getOwner().getClass().getSimpleName());
				rfcfg.setFirstLetterUpperCase(true);
			}
		}else{
			/**
			 *  put nothing here, the null cfg will be taken care properly.
			 *  see {@link ReflexFillI#createIdentifierWithFieldName(IReflexFillCfg, IReflexFillCfgVariant, boolean)}
			dumpDevWarnEntry("unable to create a cfg for variant: "+rfcv);
			 */
		}
	
		return rfcfg;
	}
	
	public void assertValidToken(char ch){
		if(Character.isDigit(ch) || Character.isWhitespace(ch) || Character.isLowerCase(ch) || Character.isUpperCase(ch)){
			throw new PrerequisitesNotMetException("invalid token "+ch);
		}
	}
	
	public Character getCommandDelimiter() {
		return chCommandDelimiter;
	}
	public String getCommandDelimiterStr() {
		return ""+chCommandDelimiter;
	}
	private CommandsDelegator setCommandDelimiter(char chCommandDelimiter) {
		assertValidToken(chCommandDelimiter);
		this.chCommandDelimiter = chCommandDelimiter;
		return this;
	}
	public Character getAliasPrefix() {
		return chAliasPrefix;
	}
	private CommandsDelegator setAliasPrefix(char chAliasPrefix) {
		assertValidToken(chAliasPrefix);
		this.chAliasPrefix = chAliasPrefix;
		return this;
	}
	public Character getVariableExpandPrefix() {
		return chVariableExpandPrefix;
	}
	private CommandsDelegator setVariableExpandPrefix(char chVariableExpandPrefix) {
		assertValidToken(chVariableExpandPrefix);
		this.chVariableExpandPrefix = chVariableExpandPrefix;
		return this;
	}
	public Character getFuzzyFilterModeToken() {
		return chFuzzyFilterModeToken;
	}
	private CommandsDelegator setFuzzyFilterModeToken(char chFilterToken) {
		assertValidToken(chFilterToken);
		this.chFuzzyFilterModeToken = chFilterToken;
		return this;
	}
	public Character getAliasBlockedToken() {
		return chAliasBlockedToken;
	}
	private CommandsDelegator setAliasBlockedToken(char chAliasBlockedToken) {
		assertValidToken(chAliasBlockedToken);
		this.chAliasBlockedToken = chAliasBlockedToken;
		return this;
	}
	public Character getAliasAllowedToken() {
		return chAliasAllowedToken;
	}
	private CommandsDelegator setAliasAllowedToken(char chAliasAllowedToken) {
		assertValidToken(chAliasAllowedToken);
		this.chAliasAllowedToken = chAliasAllowedToken;
		return this;
	}
	public Character getVarDeleteToken() {
		return chVarDeleteToken;
	}
	public String getVarDeleteTokenStr() {
		return ""+chVarDeleteToken;
	}
	private CommandsDelegator setVarDeleteToken(char chVarDeleteToken) {
		assertValidToken(chVarDeleteToken);
		this.chVarDeleteToken = chVarDeleteToken;
		return this;
	}
	public Character getCommentPrefix() {
		return chCommentPrefix;
	}
	private CommandsDelegator setCommentPrefix(char chCommentPrefix) {
		assertValidToken(chCommentPrefix);
		this.chCommentPrefix = chCommentPrefix;
		return this;
	}
	public Character getCommandPrefix() {
		return chCommandPrefix;
	}
	private CommandsDelegator setCommandPrefix(char chCommandPrefix) {
		assertValidToken(chCommandPrefix);
		this.chCommandPrefix = chCommandPrefix;
		return this;
	}
	
	public char getChCopyRangeIndicator() {
		return chCopyRangeIndicator;
	}

	public void setCopyRangeIndicator(char chCopyRangeIndicator) {
		assertValidToken(chCopyRangeIndicator);
		this.chCopyRangeIndicator = chCopyRangeIndicator;
		this.strCopyRangeIndicator=""+chCopyRangeIndicator;
	}
	
	public String commentToAppend(String strText){
		strText=strText.trim();
		if(strText.startsWith(getCommentPrefixStr())){
			strText=strText.substring(1);
		}
		return " "+getCommentPrefix()+strText;
	}
	public String getCommentPrefixStr() {
		return ""+chCommentPrefix;
	}
	public String getCommandPrefixStr() {
		return ""+chCommandPrefix;
	}
	
	private boolean bCleanExitAlreadyRequested=false;
	public void cmdRequestCleanSafeNormalExit(){
		if(bCleanExitAlreadyRequested)return;
		
//		Dialog d = new Dialog(owner)
//		GlobalOSAppI.i().showSystemAlert("exiting!!!", this); //TODO requires one update at least...
//		GlobalOSAppI.i().update(0.01f);
		
		if(bInitialized){
			//TODO sleep or wait for application to stop outside here?
			cmdDatabase(EDataBaseOperations.save);
		}
		
		GlobalOSAppI.i().setAppExiting();
		
		bCleanExitAlreadyRequested=true;
		
//		System.exit(0); //TODO remove?
	}
	
	private boolean checkCmdValidityBoolTogglers(){
		btgReferenceMatched=null;
		for(BoolTogglerCmdFieldAbs btg : ManageVarCmdFieldI.i().getListCopy(BoolTogglerCmdFieldAbs.class)){
			String strSimpleCmdId = btg.getSimpleId();
			if(!strSimpleCmdId.endsWith("Toggle"))strSimpleCmdId+="Toggle";
			if(checkCmdValidity(btg.getOwner(), btg.getUniqueCmdId(), strSimpleCmdId, "[bEnable] "+btg.getHelp(), true)){
				btgReferenceMatched = btg;
				break;
			}
		}
		return btgReferenceMatched!=null;
	}
	
	/**
	 * placeholder pseudo dummy class to help pipe all commands in a single place
	 */
	private class PseudoSelfListener implements IReflexFillCfg,IConsoleCommandListener,ISingleInstance{
		public PseudoSelfListener(){
			DelegateManagerI.i().addHandled(this);
//			ManageSingleInstanceI.i().add(this);
		}
		@Override
		public ECmdReturnStatus execConsoleCommand(CommandsDelegator ccRequester) {
			throw new NullPointerException("This method shall never be called!");
		}
		@Override
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
			throw new NullPointerException("This method shall never be called!");
		}
		@Override
		public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
			throw new NullPointerException("This method shall never be called!");
		}
		@Override
		public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
			throw new NullPointerException("This method shall never be called!");
		}
		@Override
		public String getUniqueId() {
			return MiscI.i().prepareUniqueId(this);
		}
	}
	private PseudoSelfListener icclPseudo = new PseudoSelfListener();
	protected PseudoSelfListener getPseudoListener(){
		return icclPseudo;
	}
	
	public String getListenerId(IReflexFillCfg irfc){
		if(irfc.equals(icclPseudo)){
			return "ROOT";
		}
		
//		String strClassTree="";
//		Class<?> cl = irfc.getClass();
//		while(!cl.toString().equals(Object.class.toString())){
//			if(!strClassTree.isEmpty())strClassTree+=",";
//			strClassTree+=cl.getSimpleName();
//			cl=cl.getSuperclass();
//		}
//		
////		return iccl.getClass().getSimpleName();
////		ReflexFillI.i().assertAndGetField(iccl., objFieldValue)
//		return strClassTree;
		return MiscI.i().getClassTreeReportFor(irfc, true);
	}
	
	public boolean checkCmdValidity(IReflexFillCfg irfc, String strUniqueCmdId){
		return checkCmdValidity(irfc, strUniqueCmdId, null, null);
	}
//	public boolean checkCmdValidity(IReflexFillCfg irfc, StringCmdField scf, String strHelp){
	public boolean checkCmdValidity(StringCmdField scf, String strHelp){
		if(bFillCommandList){
			if(strHelp==null || strHelp.isEmpty())throw new PrerequisitesNotMetException("use the no help method...", scf);
			
			String strCurrentlySetHelp = scf.getHelp();
			if(strCurrentlySetHelp==null || strCurrentlySetHelp.isEmpty()){
				scf.setHelp(strHelp);
			}else{
				if(!strCurrentlySetHelp.equals(strHelp)){
					throw new PrerequisitesNotMetException("the help that is set differs from new one", strCurrentlySetHelp, strHelp);
				}
			}
		}
		
		return checkCmdValidity(scf);
	}
//	public boolean checkCmdValidity(IReflexFillCfg irfc, StringCmdField scf){
	public boolean checkCmdValidity(StringCmdField scf){
		return checkCmdValidity(scf.getOwner(), scf.getUniqueCmdId(), scf.getSimpleId(), scf.getHelp());
	}
//	public boolean checkCmdValidity(IReflexFillCfg irfc, StringCmdField scf, String strComment){
//		if(strComment==null){
//			strComment = scf.getHelp();
//		}else{
//			if(scf.getHelp()!=null){
//				strComment+="\n"+scf.getHelp();
//			}
//		}
//		
//		boolean bCmdMatches = checkCmdValidity(irfc, scf.getUniqueCmdId(), scf.getSimpleId(), strComment);
////		if(bCmdMatches){
////			scf.applyValueCallNow();
////		}
//		return bCmdMatches; 
//	}
	
	public boolean checkCmdValidity(IReflexFillCfg irfc, String strUniqueCmdId, String strSimpleCmdId, String strComment){
		return checkCmdValidity(irfc, strUniqueCmdId, strSimpleCmdId, strComment, false);
	}
	/**
	 * 
	 * @param irfcOwner
	 * @param strUniqueCmdId
	 * @param strSimpleCmdId (can be null)
	 * @param strComment (can be null)
	 * @param bSkipSortCheck
	 * @return
	 */
	public boolean checkCmdValidity(IReflexFillCfg irfcOwner, String strUniqueCmdId, String strSimpleCmdId, String strComment, boolean bSkipSortCheck){
//		if(strCmdLinePrepared==null){
		if(bFillCommandList){
			if(strComment!=null){
				strUniqueCmdId+=commentToAppend(strComment);
			}
			
			if(strSimpleCmdId==null)strSimpleCmdId=strUniqueCmdId;
			addCmdToValidList(irfcOwner,strUniqueCmdId,strSimpleCmdId,bSkipSortCheck);
			
			return false;
		}
		
		if(RESTRICTED_CMD_SKIP_CURRENT_COMMAND.isUniqueCmdIdEqualTo(ccl.getCmdLinePrepared()))return false;
		if(ccl.isCommentedLine())return false;
		if(ccl.getCmdLinePrepared().trim().isEmpty())return false;
		
//		String strCheck = strPreparedCmdLine;
//		strCheck = strCheck.trim().split(" ")[0];
		strUniqueCmdId = strUniqueCmdId.trim().split(" ")[0];
		
//		return strCheck.equalsIgnoreCase(strValidCmd);
		boolean bCmdMatches = ccl.paramCommand().equalsIgnoreCase(strUniqueCmdId);
		return bCmdMatches;
	}
	
//	private boolean matchCommand(String strValidCmd){
//		
//	}
	
	private boolean cmdEcho() {
		String strToEcho="";
		String strPart="";
		int iParam=1;
		while(strPart!=null){
			strToEcho+=strPart;
			strToEcho+=" ";
			strPart = ccl.paramString(iParam++);
		}
		strToEcho=strToEcho.trim();
		
		dumpEntry(strToEcho);
		
		return true;
	}

	ArrayList<IConsoleCommandListener> aConsoleCommandListenerList = new ArrayList<IConsoleCommandListener>();

	public String	strDebugTest = ""; //no problem be public

	private boolean	bConfigured;

//	private ArrayList<String>	astrBaseCmdCacheList = new ArrayList<String>();
	private ArrayList<String>	astrBaseCmdCmtCacheList = new ArrayList<String>();

	private String	strLastTypedUserCommand;

	private ArrayList<Exception>	aExceptionList = new ArrayList<Exception>();

	private String	strCurrentDay;

	private boolean	bFillCommandList;

	private boolean	bInitialized;

	private CommandData	cmddLastAdded;

//	private TreeMap<String,KeyBoundVarField> tmbindList = new TreeMap<String, KeyBoundVarField>(String.CASE_INSENSITIVE_ORDER);


//	private int	iBoolTogglersListHashCode;


//	private ECmdReturnStatus	ecrsCurrentCommandReturnStatus;
	
	private void assertConfigured(){
		if(bConfigured)return;
		throw new NullPointerException(CommandsDelegator.class.getName()+" was not configured!");
	}
	
	public void removeListenerAndCmds(IConsoleCommandListener iccl){
		if(!aConsoleCommandListenerList.contains(iccl)){
			throw new PrerequisitesNotMetException("listener already removed?", iccl);
		}
		
//		removeAllCmdsFor(iccl);
		removeAllCmdsOfOwnersBeingDiscarded();
		
//		//remove all of it's owned fields from the list
//		for(VarCmdFieldAbs vcf:VarCmdFieldManagerI.i().getListFullCopy()){
//			if(vcf.getOwner()==iccl){
//				vcf.discardSelf(ccSelf);
//			}
//		}
		
		hmDebugListenerAddedStack.remove(iccl);
		aConsoleCommandListenerList.remove(iccl);
	}
	
	/**
	 * Any class can attach it's command interpreter.
	 * 
	 * It will call the listener in a safe way, 
	 * to let it fill the recognized commands list with it's own commands. 
	 * 
	 * @param icc
	 */
	public void addConsoleCommandListener(final IConsoleCommandListener icc){
		if(icc==null)throw new NullPointerException("invalid null commands listener.");
		if(icc instanceof IMultiInstanceOverride)return; //only single instances allowed
		
		if(aConsoleCommandListenerList.contains(icc)){
			throw new PrerequisitesNotMetException("listener already added: "+icc.getClass().getName())
				.initCauseAndReturnSelf("Listener added at:",hmDebugListenerAddedStack.get(icc));
		}
		
		hmDebugListenerAddedStack.put(icc,Thread.currentThread().getStackTrace());
		aConsoleCommandListenerList.add(icc);
		
		/**
		 * postponed to let configurations run smoothly
		 */
		ManageCallQueueI.i().addCall(new CallableX(this) {
			@Override
			public Boolean call() {
				if(!CommandsDelegator.this.isInitialized()){
					this.setQuietOnFail(true); //no messages, this is expected
					return false; //wait its initialization
				}
				this.setQuietOnFail(false);
				
				//this will let it fill the commands list for this listener
				bFillCommandList=true;
				icc.execConsoleCommand(CommandsDelegator.this); 
				bFillCommandList=false;
				return true;
			}
		});
	}
	
	public boolean isFillingCommandList(){
		return bFillCommandList;
	}
	
	public static enum ECmdReturnStatus{
		// oks
		FoundAndWorked,
//		FoundCallerAndQueuedIt, //TODO add a failed message for the caller?
		
		// failers
		FoundAndFailedGracefully,
		FoundAndExceptionHappened,
		
		// skippers
		NotFound,
		Skip,
		;
		
		Object obj;
		private DebugData	dbg;
		public ECmdReturnStatus setCustomReturnValue(Object obj){
			PrerequisitesNotMetException.assertNotAlreadySet(this.obj, obj, "custom return value should be extracted before setting a new one!", dbg, this);
			if(RunMode.bValidateDevCode)dbg=ManageDebugDataI.i().setStack(dbg,EDbgStkOrigin.LastSetValue);
			this.obj=obj;
			return this;
		}
		public boolean isCustomReturnValueSet(){
			return this.obj!=null;
		}
		public Object getCustomReturnValue(){
			return this.obj;
		}
		public Object getExtractingCustomReturnValue(){
			boolean bEnforceSetBeforeGet=false;
			if(bEnforceSetBeforeGet){
				PrerequisitesNotMetException.assertIsTrue("set", this.obj!=null, this);
			}
//			Object obj = isCustomReturnValueSet() ? getExtractingCustomReturnValue() : null; //to at least reset
			Object objOut=this.obj;
			this.obj=null; //reset to complete the extraction
			return objOut;
		}
		public Exception getExtractingCustomReturnValueAsException(){
			Object obj = getExtractingCustomReturnValue(); //to at least reset
			if(this.compareTo(FoundAndExceptionHappened)!=0)return null;
			return (Exception)obj;
		}
	}
	
	/**
	 * extend this class to access it directly
	 * @param ccl
	 * @return
	 */
	protected boolean cmdOS(CurrentCommandLine ccl){
		boolean bOk=true;
		
		String strOSName=System.getProperty("os.name");
		if(!strOSName.toLowerCase().startsWith(ccl.paramString(1).toLowerCase())){
			/**
			 * skip message would be just annoying...
			 */
			return true; //just skip
		}
		
		ArrayList<String> astrOSCmd = new ArrayList<String>();
		if(strOSName.equalsIgnoreCase("linux")){
			astrOSCmd.add("bash");
			astrOSCmd.add("-c");
		}
		astrOSCmd.add(String.join(" ",ccl.getPreparedCmdAndParamsListCopyFrom(2)));
		
		try {
			dumpSubEntry("Running OS command: "+astrOSCmd);
			
//			Process p = Runtime.getRuntime().exec(astrOSCmd.toArray(new String[0]));
//			InputStream isErr = p.getErrorStream();
			
			ProcessBuilder pb = new ProcessBuilder(astrOSCmd);
			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			Process p = pb.start();
			int iExit = p.waitFor();
			
			dumpSubEntry("OS command exit value: "+iExit);
			bOk = iExit==0;
		} catch (IOException|InterruptedException e) {
			dumpExceptionEntry(e, astrOSCmd);
		}
		
		return bOk;
	}
	
	/**
	 * This is the actual commands delegator root/core/main method.
	 * 
	 * Scripting commands sub-class must override {@link #stillExecutingCommand()} that
	 * ends calling this method at a point.
	 * 
	 * @return
	 */
	protected ECmdReturnStatus execCmdFromConsoleRequestRoot(){
		if(!bFillCommandList && RESTRICTED_CMD_SKIP_CURRENT_COMMAND.isUniqueCmdIdEqualTo(ccl.getCmdLinePrepared())){
			return ECmdReturnStatus.Skip;
		}
		
		ccl.paramCommand(true);
//		if(!ccl.paramString(0).equalsIgnoreCase(ccl.paramCommand())){
////			dumpInfoEntry("Converted "+strMainCmd+" to "+strConvertedTo);
//			dumpInfoEntry("Command converted to "+ccl.paramCommand());
//		}
		
		Boolean bCmdWorked = null; //must be filled with true or false to have been found!
//		boolean bCmdCallerPrepared = false;
		
		for(StringCmdField scf:ascfCmdWithCallerList){
//			if(checkCmdValidity(icclPseudo,scf)){
			if(checkCmdValidity(scf)){
				bCmdWorked = scf.callerAssignedRunNow();
				if(!bCmdWorked && scf.getCallerAssignedInfo().isRetryOnFail()){
					dumpInfoEntry("First exec failed but will be retried: "+scf.getUniqueCmdId());
					bCmdWorked=true; //the command worked as retry is an option.
				}
//				bCmdCallerPrepared = true;
				break;
			}
		}
		
//		convertCommandIfPossible();
		
		if(bCmdWorked!=null && bCmdWorked){
			//keep empty
		}else
		if(checkCmdValidityBoolTogglers()){
			bCmdWorked=toggle(btgReferenceMatched);
		}else
		if(checkCmdValidity(scfAlias,getAliasHelp())){
			bCmdWorked=cmdAlias();
		}else
		if(checkCmdValidity(scfAddKeyCodeMonitor,"<id> <code...>")){
			String strNewKeyId = ccl.paramString(1);
			
			for(Key key:GlobalManageKeyCodeI.i().getKeyListCopy()){
				if(key.getId().equalsIgnoreCase(strNewKeyId)){
					dumpWarnEntry("conflicting with", key);
					bCmdWorked=false;
					break;
				}
			}
			
			if(bCmdWorked==null){
				ArrayList<Integer> ai = new ArrayList<Integer>();
				
				Integer iCode=null;
				int iIndex=2;
				while( (iCode=ccl.paramInt(iIndex++)) != null ){
					ai.add(iCode);
				}
				
				GlobalManageKeyCodeI.i().addKey(strNewKeyId, ai.toArray(new Integer[0]));
			}
		}else
		if(checkCmdValidity(scfUnBindKey,"<KeyMod...+KeyAction>")){
			String strBindCfg = ccl.paramString(1);
			GlobalManageKeyBindI.i().removeKeyBind(strBindCfg);
			setupRecreateFile();
		}else
		if(checkCmdValidity(scfBindKey,"<KeyMod...+KeyAction> <full console command>")){
			String strBindCfg = ccl.paramString(1);
			
			KeyBoundVarField bind = new KeyBoundVarField(null,strBindCfg);
			bind.setUserCommand(ccl.getPreparedCmdAndParamsListCopyFrom(2));
			
//			tmbindList.put(strBindCfg,bind);
			GlobalManageKeyBindI.i().addHandled(bind);
			
			setupRecreateFile();
//			cui().addKeyBind(bind);
			
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfBindList,"show user binds list")){
			dumpSubEntry(GlobalManageKeyBindI.i().getReportAsCommands(scfBindKey,false));
//			for(KeyBoundVarField bind:GlobalManageKeyBindI.i().getListCopy()){
//				String strMapping = GlobalManageKeyBindI.i().getMappingFrom(bind);
//				if(strMapping.equals(bind.getBindCfg())){
//					strMapping="";
//				}else{
//					strMapping="ActId("+strMapping+")";
//				}
//				
//				String strUserCmd = bind.getUserCommand();
//				if(strUserCmd==null){
//					strUserCmd="";
//				}else{
//					strUserCmd="Cmd("+strUserCmd+")";
//				}
//				dumpSubEntry(bind.getBindCfg()+":"+strUserCmd+strMapping);
//			}
			
			bCmdWorked=true;
		}else
		if(checkCmdValidity(this,"activateSelfWindow")){
			String strAppTitle=GlobalOSAppI.i().getApplicationTitle();
			if(strAppTitle==null){
				dumpWarnEntry("this functionality requires the application title to have been set");
			}else{
				addCmdToQueue(scfCmdOS.getUniqueCmdId()+" linux "
					+"xdotool windowactivate $(xdotool search --name \"^"+strAppTitle+"$\")");
			}
			bCmdWorked = true;
		}else
		if(checkCmdValidity(CMD_CLEAR_COMMANDS_HISTORY)){
			astrCmdHistory.clear();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfClearDumpArea)){
			cui().getDumpEntriesForManagement(ccSelf).clear();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(CMD_CONSOLE_SCROLL_BOTTOM)){
			cui().scrollToBottomRequest();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(CMD_DB,EDataBaseOperations.help())){
			bCmdWorked=cmdDb();
		}else
		if(checkCmdValidity(CMD_ECHO," simply echo something")){
			bCmdWorked=cmdEcho();
		}else
		if(checkCmdValidity(scfChangeCommandSimpleId,"<strUniqueCmdIdWithSimpleConflict> <strNewSimpleCmdId>")){
			String strUniqueCmdIdWithSimpleConflict = ccl.paramString(1);
			String strNewSimpleCmdId = ccl.paramString(2);
			
			boolean bUserFail=false;
			CommandData cmddWithSimpleConflict = getCmdDataFor(strUniqueCmdIdWithSimpleConflict,false);
			if(cmddWithSimpleConflict==null){
				dumpWarnEntry("command not found "+strUniqueCmdIdWithSimpleConflict);
				bUserFail=true;
			}else
			if(!cmddWithSimpleConflict.isSimpleCmdIdConflicting()){
				dumpWarnEntry("cmd has no conflicts: "+cmddWithSimpleConflict.getUniqueCmdId(), cmddWithSimpleConflict.asHelp());
				bUserFail=true;
			}
			
			if(!bUserFail){
				if(fixSimpleCmdConflict(cmddWithSimpleConflict,strNewSimpleCmdId,false,false)){
					dumpSubEntry(cmddWithSimpleConflict.asHelp());
					bCmdWorked=true;
				}
			}
		}else
		if(checkCmdValidity(scfCmdOS,"<strOSName> <astrOSCommandAndParams> runs an OS command. Will only be executed if in the correponding OS, otherwise will be skipped, so use with the other OS alternatives nearby.")){
			if(bAllowUserCmdOS){
				bCmdWorked=cmdOS(getCurrentCommandLine());
			}else{
				dumpUserErrorEntry("OS cmd not allowed to users.");
				bCmdWorked=false;
			}
		}else
		if(checkCmdValidity(scfEditShowClipboad,"--noNL")){
			String strParam1 = ccl.paramString(1);
			boolean bShowNL=true;
			if(strParam1!=null){
				if(strParam1.equals("--noNL")){
					bShowNL=false;
				}
			}
			showClipboard(bShowNL);
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfEditCopy,"-d end lines with command delimiter instead of NL;")){
			bCmdWorked=cui().cmdEditCopyOrCut(false);
		}else
		if(checkCmdValidity(scfEditCut,"like copy, but cut :)")){
			bCmdWorked=cui().cmdEditCopyOrCut(true);
		}else
		if(checkCmdValidity(scfExecBatchCmdsFromFile,"<strFileName>")){
			String strFile = ccl.paramString(1);
			if(strFile!=null){
				addCmdListOneByOneToQueue(MiscI.i().fileLoad(strFile),false,false);
//				astrExecConsoleCmdsQueue.addAll(Misc.i().fileLoad(strFile));
				bCmdWorked=true;
			}
		}else
		if(checkCmdValidity(scfExit,"the application")){
			cmdRequestCleanSafeNormalExit();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfFileShowData,"<ini|setup|CompleteFileName> show contents of file at dump area")){
			String strOpt = ccl.paramString(1);
			
			if(strOpt!=null){
				File fl = null;
				switch(strOpt){
					case "ini":
						dumpInfoEntry("Init file data: ");
						fl = flInit;
						break;
					case "setup":
						dumpInfoEntry("Setup file data: ");
						fl = flSetup;
						break;
					default:
						fl = new File(strOpt);
				}
				
				if(fl.exists()){
					for(String str : MiscI.i().fileLoad(fl)){
						dumpSubEntry(str);
					}
				}else{
					dumpWarnEntry("File does not exist: "+fl.getAbsolutePath());
				}
			}
			
			bCmdWorked=true;
		}else
		if(checkCmdValidity(CMD_FIX_LINE_WRAP ,"in case words are overlapping")){
			cui().cmdLineWrapDisableDumpArea();
			bCmdWorked = true;
		}else
		if(checkCmdValidity(CMD_FIX_VISIBLE_ROWS_AMOUNT,"[iAmount] in case it is not showing as many rows as it should")){
			cui().setVisibleRowsAdjustRequest(ccl.paramInt(1));
			if(!cui().isVisibleRowsAdjustRequested())cui().setVisibleRowsAdjustRequest(0);
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfFixAllCmdsConflictsAutomatically,"[strSimpleCmdFilter]")){
			String strSimpleCmdFilter = ccl.paramString(1);

			bCmdWorked=fixSimpleCmdConflictForAll(strSimpleCmdFilter);
		}else
		if(checkCmdValidity(scfHelp,"[strFilter...] show (filtered) available commands for each param")){
			String strFilter=null;
			int iParamIndex=1;
			boolean bFiltered=false;
			while((strFilter=ccl.paramString(iParamIndex++))!=null){
				cmdShowHelp(strFilter);
				bFiltered=true;
			}
			
			if(!bFiltered)cmdShowHelp(null);
			
			/**
			 * ALWAYS return TRUE here, to avoid infinite loop when improving some failed command help info!
			 */
			bCmdWorked=true; 
		}else
		if(checkCmdValidity(CMD_HISTORY,"[strFilter] of issued commands (the filter results in sorted uniques)")){
			bCmdWorked=cmdShowHistory();
		}else
//		if(checkCmdValidity(CMD_HK_TOGGLE ,"[bEnable] allow hacks to provide workarounds")){
//			if(paramBooleanCheckForToggle(1)){
//				Boolean bEnable = paramBoolean(1);
//				icui().setHKenabled(bEnable);
//				bCmdEndedGracefully=true;
//			}
//		}else
		if(checkCmdValidity(CMD_LINE_WRAP_AT,"[iMaxChars] 0 = wrap will be automatic")){
			Integer i = ccl.paramInt(1);
			if(i!=null && i>=0){ // a value was supplied
//				ilvConsoleMaxWidthInCharsForLineWrap.setObjectValue(ccSelf,i);
				ilvConsoleMaxWidthInCharsForLineWrap.setObjectRawValue(i);
//				if(i==-1){
//					/**
//					 * prefered using null instead of -1 that is for the user type a valid integer
//					 */
//					iConsoleMaxWidthInCharsForLineWrap=null;
//				}else{
//					iConsoleMaxWidthInCharsForLineWrap=i;
//				}
			}
//			csaTmp.updateFontStuff();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfListFiles,"<strPath> list files on directory")){
			String strPath = ccl.paramString(1);
			
			try {
				ArrayList<File> afl = MiscI.i().listFiles(strPath);
				
				for(File fl:afl){
					dumpSubEntry(fl.isDirectory() ? fl.getPath()+"/" : fl.getName());
				}
				
				bCmdWorked=true;
			} catch (FileNotFoundException ex) {
				dumpExceptionEntry(ex, strPath);
			}
			
		}else
		if(checkCmdValidity(scfListKeyCodes,"list all available key ids and codes")){
			for(String str:GlobalManageKeyCodeI.i().getKeyCodeListReport()){
				dumpSubEntry(str);
			}
		}else
		if(checkCmdValidity(scfMessagesBufferClear,"clear all warning and exceptions stored in memory")){
			aimBufferList.clear();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfMessageReview,"[[ [-|~][simpleFilter|regexFilter] | index] [stackLimit]] can be a (negated|fuzzy) filter, an index or an uid. If it has an exception, it will be dumped.")){
			cmdMessageReview();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfQuit,"the application")){
			cmdRequestCleanSafeNormalExit();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(CMD_REPEAT,"<iTimes> <strOtherCommand> will repeat other command iTimes")){
			Integer iTimes = ccl.paramInt(1);
			if(iTimes!=null && iTimes>=0){ // 0 or 1 is accepted in case iTimes is the result of some variable.
				String strOtherCmd = ccl.paramStringConcatenateAllFrom(2);
				if(strOtherCmd!=null){
					for(int i=0;i<iTimes;i++)addCmdToQueue(strOtherCmd);
					bCmdWorked=true;
				}
			}
		}else
		if(checkCmdValidity(CMD_RESET,"will reset the console (restart it)")){
			cmdResetConsole();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(CMD_SHOW_SETUP,"show restricted variables")){
			for(String str:MiscI.i().fileLoad(flSetup)){
				dumpSubEntry(str);
			}
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfShowCommandsSimpleIdConflicts)){
			ArrayList<CommandData> aC = new ArrayList<CommandData>();
			for(CommandData cmdd:trmCmddList.values()){
//				aC.addAll(cmdd.getCoreIdConflictListClone());
				for(CommandData cmddC:cmdd.getSimpleIdConflictListClone()){
					if(!aC.contains(cmddC))aC.add(cmddC);
				}
			}
			
			Collections.sort(aC);
//			Collections.sort(aC, new Comparator<CommandData>() {
//				@Override
//				public int compare(CommandData o1, CommandData o2) {
//					if(o1.getCoreCmdId().equalsIgnoreCase(o2.getCoreCmdId())){
//						return o1.getUniqueCmdId().compareTo(o2.getUniqueCmdId());
//					}
//					return o1.getCoreCmdId().compareTo(o2.getCoreCmdId());
//				}
//			});
			
//			/**
//			 * this removes dups
//			 */
//			LinkedHashSet<CommandData> lhs = new LinkedHashSet<CommandData>(aC);
			
			if(aC.size()>0){
				String strLastCoreId=null;
				for(CommandData cmddC:aC){//.toArray(new CommandData[0])){
					String strCurrentCoreId=cmddC.getSimpleCmdId();
					
					if(!strCurrentCoreId.equalsIgnoreCase(strLastCoreId)){
						dumpSubEntry("CoreId: "+strCurrentCoreId);
						strLastCoreId=strCurrentCoreId;
					}
					
					dumpSubEntry(strSubEntryPrefix+cmddC.getUniqueCmdId());
					
//					if(strLastCoreId==null){
//						strLastCoreId=strCurrentCoreId;
//					}
				}
			}
			
			bCmdWorked=true;
		}else
		if(checkCmdValidity(CMD_SLEEP,"<fDelay> [singleCmd] will wait before executing next command in the command block; alternatively will wait before executing command in-line, but then it will not sleep the block it is in!")){
			Float fSleep = ccl.paramFloat(1);
			String strCmds = ccl.paramStringConcatenateAllFrom(2);
			
			if(strCmds!=null){
				/**
				 * creates mini block
				 */
				ArrayList<String> astrCmdList = new ArrayList<String>();
				astrCmdList.add(CMD_SLEEP+" "+fSleep);
				astrCmdList.add(strCmds);
				addCmdsBlockToPreQueue(astrCmdList, false, false, "in-line sleep commands");
			}else{
				/**
				 * This mode is only used on the pre-queue, 
				 * here it is ignored.
				 */
				dumpWarnEntry(CMD_SLEEP+" without cmd, only works on command blocks like functions");
			}
			bCmdWorked=true;
		}else
		if(checkCmdValidity(CMD_STATS_ENABLE,"[idToEnable [bEnable]] empty for a list. bEnable empty to toggle.")){
			bCmdWorked=true;
			String strId=ccl.paramString(1);
			Boolean bValue=ccl.paramBoolean(2);
			if(strId!=null){
				EStats e=null;
				try{e=EStats.valueOf(strId);}catch(IllegalArgumentException ex){
					bCmdWorked=false;
					dumpWarnEntry("Invalid option: "+strId+" "+bValue);
				}
				
				if(e!=null){
					e.bShow = ( bValue!=null ? bValue : !e.isShow() );
				}
			}else{
				for(EStats e:EStats.values()){
					dumpSubEntry(e.toString()+" "+e.isShow());
				}
			}
		}else
		if(checkCmdValidity(CMD_STATS_FIELD_TOGGLE,"[bEnable] toggle simple stats field visibility")){
			bCmdWorked=cui().statsFieldToggle();
		}else
		if(checkCmdValidity(CMD_STATS_SHOW_ALL,"show all console stats")){
			dumpAllStats();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(scfDevTest,"[...] temporary developer tests")){
			cmdTest();
			bCmdWorked=true;
		}else
		if(checkCmdValidity(CMD_VAR_ADD,"<varId> <[+|-]value>")){
			bCmdWorked=cmdVarAdd(ccl.paramString(1), ccl.paramString(2),true);
		}else
		if(
				checkCmdValidity(CMD_VAR_SET,
				"<[<varId> <value>] | [-varId]> "
					+"Can be boolean(true/false, and after set accepts 1/0), number(integer/floating) or string; "
					+"-varId will delete it; "
					+"Retrieve it's value with "+getVariableExpandPrefix()+"{varId}; "
					+"Restricted variables will have no effect; "
			)
		){
			bCmdWorked=cmdVarSet();
		}else
		if(checkCmdValidity(CMD_VAR_SET_CMP,"<varIdBool> <value> <cmp> <value>")){
			bCmdWorked=cmdVarSetCmp();
		}else
		if(checkCmdValidity(scfVarShow,"[["+CommandsHelperI.i().getRestrictedToken()+"]regexFilter] list user or restricted variables.")){
			bCmdWorked=cmdVarShow();
		}else
		if(checkCmdValidity(icclPseudo,TOKEN_CMD_NOT_WORKING_YET+"zDisabledCommand",null," just to show how to use it")){
			// keep this as reference
		}else
		{
			if(!bFillCommandList){ //when each listener is added, they already fill the list!
				for(IConsoleCommandListener icc:aConsoleCommandListenerList){
					ECmdReturnStatus ecrs = icc.execConsoleCommand(this);
						
					if(ecrs==null){
						throw new PrerequisitesNotMetException("return status cannot be null", icc);
					}
						
					switch(ecrs){
						case NotFound:
						case Skip:
							continue; //try next cmd listener
						default:
							return ecrs;
					}
				}
			}
		}
		
		if(bCmdWorked==null){
//			if(bCmdCallerPrepared){
//				return ECmdReturnStatus.FoundCallerAndQueuedIt;
//			}else{
				return ECmdReturnStatus.NotFound;
//			}
		}
		
		/**
		 * exception will be captured out of here
		 */
		return cmdFoundReturnStatus(bCmdWorked);
	}
	
//	private void convertCommandIfPossible() {
//		paramCommand()
//	}
	
	private String strUIdToken="uid=";
	private void dumpMessageReview(ImportantMsgData imsg, Integer iStackLimit, Integer iCurrentIndex, boolean bListingMode){
		String strCurrentIndex = iCurrentIndex==null ? "" : ""+iCurrentIndex+": ";
		
		String strHeader = 
			getCommandPrefixStr()+scfMessageReview.getSimpleId()+" "+strUIdToken+imsg.getUId()+" "
			+getCommentPrefixStr()+strCurrentIndex+" ";
		
		if( !bListingMode && (imsg.getDumpEntryData().getException()!=null) ){
//			dumpSubEntry("MsgInfo: "+strHeader+imsg.getExceptionHappenedAtInfo());
			dumpSubEntry(strHeader+imsg.getExceptionHappenedAtId());
			dumpExceptionEntry(imsg, iStackLimit==null?0:iStackLimit);
		}else{
			dumpSubEntry(strHeader
				+imsg.getDumpEntryData().getLineFinal(false)+" "
				+imsg.getExceptionHappenedAtId());
		}
	}
	private void cmdMessageReview() {
		String strFilter = ccl.paramString(1);
		Integer iIndex = ccl.paramInt(1,true);
		Integer iStackLimit = ccl.paramInt(2,true);
		
		String strUId=null;
		if(strFilter!=null){
			strFilter=improveSimpleFilter(strFilter);
			if(strFilter.startsWith(strUIdToken)){
				strUId=strFilter.substring(strUIdToken.length());
			}
		}
		
		if(strUId==null)dumpSubEntry("FinalRegexFilter: "+strFilter);
		
		ArrayList<ImportantMsgData> aimsg = new ArrayList<ImportantMsgData>(aimBufferList);
		
		Collections.sort(aimsg, ImportantMsgData.cmpFirstOcurrenceCreationTime());
		
		if(iIndex!=null){
			dumpMessageReview(aimsg.get(iIndex), iStackLimit, iIndex, false);
		}else{
			boolean bListMode = strUId==null;
			int iCount=0;
			for(int i=0;i<aimsg.size();i++){
				ImportantMsgData imsg = aimsg.get(i);
				if(strUId!=null){
					if(!imsg.getUId().equals(strUId))continue;
				}else{
					if(!containsFilterString(imsg.getMsgKey(),strFilter))continue;
				}
				
				dumpMessageReview(imsg, iStackLimit, i, bListMode);
				iCount++;
				
				if(strUId!=null)break;
			}
			
			if(bListMode){ //total
				dumpSubEntry("Messages(shown="+iCount+";total="+aimsg.size()+")");
			}
		}
	}

	private boolean fixSimpleCmdConflictForAll(String strSimpleCmdFilter){
//		boolean bFixedAll=true;
		ArrayList<String> astrFailedList = new ArrayList<String>();
		ArrayList<CommandData> acmddWithConflict = new ArrayList<CommandData>();
		for(CommandData cmdd:trmCmddList.values()){
			if(!cmdd.isSimpleCmdIdConflicting())continue;
			if(
					strSimpleCmdFilter!=null && 
					!cmdd.getSimpleCmdId().toLowerCase().contains(strSimpleCmdFilter.toLowerCase())
			){
				continue;
			}
			
			acmddWithConflict.add(cmdd);
		}
		
		for(CommandData cmdd:acmddWithConflict){	
			boolean b=fixSimpleCmdConflict(cmdd);
			String str = cmdd.getSimpleCmdId()+", "+cmdd.getUniqueCmdId();
			if(b){
				dumpSubEntry("Fixed cmd conflict for: "+str);
			}else{
				astrFailedList.add(str);
//				bFixedAll=false;
			}
		}
		
		for(String str:astrFailedList){
			dumpWarnEntry("FAILED to fix cmd conflict for: "+str);
		}
		
//		return astrFailedList.size()==0;
		return true; //just see warning messages for the failures...
	}
	private boolean fixSimpleCmdConflict(CommandData cmddWithSimpleConflict) {
		VarCmdUId vcid = cmddWithSimpleConflict.getVar().getVarCmdUIdForManagement(ccSelf);
		
		boolean b = fixSimpleCmdConflict(
			cmddWithSimpleConflict,
			vcid.getPrefixCustom() + cmddWithSimpleConflict.getSimpleCmdId(),
			true,
			true);
		
		if(!b && vcid.getPrefixCustom().isEmpty()){
			b = fixSimpleCmdConflict(
				cmddWithSimpleConflict,
				vcid.getConcreteClass().getSimpleName() + cmddWithSimpleConflict.getSimpleCmdId(),
				true,
				false);
		}
		
		return b;
	}
	
	/**
	 * 
	 * @param cmddWithSimpleConflict
	 * @param strNewSimpleCmdId
	 * @param bForce
	 * @param bSupressMessages as it can be retried
	 * @return
	 */
	private boolean fixSimpleCmdConflict(CommandData cmddWithSimpleConflict, String strNewSimpleCmdId, boolean bForce, boolean bSupressMessages) {
		CommandData cmddFromNewSimple = getCmdDataFor(strNewSimpleCmdId,true);
		if(cmddFromNewSimple==null){
			cmddWithSimpleConflict.applySimpleCmdIdConflictFixed(ccSelf, strNewSimpleCmdId, bForce);//, trmCmds.values());
//			dumpSubEntry(cmddWithSimpleConflict.asHelp());
			return true;
		}else{
			if(cmddWithSimpleConflict==cmddFromNewSimple){ //not a conflict
				if(!bSupressMessages)dumpInfoEntry("Simple command '"+strNewSimpleCmdId+"' already assigned to: "+cmddWithSimpleConflict.getUniqueCmdId());
				return true;
			}else{
				if(!bSupressMessages)dumpWarnEntry("Simple command '"+strNewSimpleCmdId+"' already used by: "+cmddFromNewSimple.getUniqueCmdId(), cmddWithSimpleConflict.getUniqueCmdId());
			}
		}
		
		return false;
	}
	
//	private IConsoleUI cui;
//	private IConsoleUI icui(){
//		return GlobalConsoleUII.i();
//	}
	
	public void cmdResetConsole() {
		cui().requestRestart();
//		icui().recreateConsoleGui();
	}

	private ECmdReturnStatus precmdRawLineCheckAlias(){
		bLastAliasCreatedSuccessfuly=false;
		
		if(ccl.getOriginalLine()==null)return ECmdReturnStatus.NotFound;
		
		String strCmdLine = ccl.getOriginalLine().trim();
		String strExecAliasPrefix = ""+getCommandPrefix()+getAliasPrefix();
		if(strCmdLine.startsWith(getCommandPrefix()+"alias ")){
			/**
			 * create
			 */
			AliasData alias = new AliasData(this);
			
			String[] astr = strCmdLine.split(" ");
			if(astr.length>=3){
				alias.strAliasId=astr[1];
				if(hasVar(alias.strAliasId)){
					dumpUserErrorEntry("Alias identifier '"+alias.strAliasId+"' conflicts with existing variable!");
					return ECmdReturnStatus.FoundAndFailedGracefully;
				}
				
				alias.strCmdLine=String.join(" ", Arrays.copyOfRange(astr, 2, astr.length));
				
				AliasData aliasFound=getAlias(alias.strAliasId);
				if(aliasFound!=null)aAliasList.remove(aliasFound);
				
				aAliasList.add(alias);
				MiscI.i().fileAppendLineTS(flDB, alias.toString());
				dumpSubEntry(alias.toString());
				
				bLastAliasCreatedSuccessfuly = true;
				return ECmdReturnStatus.FoundAndWorked;
			}
			
			/**
			 * parameters were missing...
			 */
			return ECmdReturnStatus.FoundAndFailedGracefully;
		}else
		if(strCmdLine.startsWith(strExecAliasPrefix)){
			/**
			 * execute
			 */
			String strAliasId=strCmdLine
				.split(" ")[0]
				.substring(strExecAliasPrefix.length())
				.toLowerCase();
			AliasData alias = getAlias(strAliasId);
			if(alias!=null){
				if(alias.bBlocked){
					dumpWarnEntry(alias.toString()); //will show the blocked status
					
					return ECmdReturnStatus.FoundAndFailedGracefully;
				}else{
					addCmdToQueue(alias.strCmdLine
						+commentToAppend("alias="+alias.strAliasId), true);
					return ECmdReturnStatus.FoundAndWorked;
				}
			}else{
				dumpWarnEntry("Alias not found: "+strAliasId);
				
				/**
				 * alias execution request/prefix was found, so it is not some other command...
				 */
				return ECmdReturnStatus.FoundAndFailedGracefully;
			}
		}
		
		return ECmdReturnStatus.NotFound;
	}

	/**
	 * dumps can happen before this class initialization, 
	 * they will be stored to be shown later. 
	 * 
	 * provides line-wrap
	 * 
	 * @param bDumpToConsole if false, will only log to file
	 * @param strLineOriginal
	 */
	private void dumpEntry(DumpEntryData de){
		if(!cui().isInitializedProperly()){
			adeDumpEntryFastQueue.add(de);
			return;
		}
		
		dumpSave(de);
		
		if(de.isImportant()){
//			addImportantMsgToBuffer(de.getType(),de.getKey(),de.getException());
			/**
			 * if this stack is already from an important message, it will 
			 * just refresh that message on the list. 
			 */
			addImportantMsgToBuffer(de);
		}
		
//		PrintStream output = System.out;
//		if(de.isStderr())output = System.err;
//		output.println("CONS: "+de.getLineOriginal());
		if(btgDumpToTerminal.b()){
			de.sendToPrintStream(); 
		}
		
//		if(!icui().isInitializationCompleted()){
//			adeDumpEntryFastQueue.add(de);
//			return;
//		}
		
		dumpEntryToConsole(de);
	}
	
	private void dumpEntryToConsole(DumpEntryData de){
		if(!de.isDumpToConsole())return;
		
		ArrayList<String> astrDumpLineList = new ArrayList<String>();
		
//		if(de.isImportant()){
//			astrDumpLineList.add("\t"+getCommandPrefixStr()+scfMessageReview.getSimpleId()+" uid="+de.getImportantMessageLink().getUId()+" #");
//		}
		
//		if(de.getLineOriginal().isEmpty()){
		if(de.getLineFinal().isEmpty()){
			astrDumpLineList.add("");
		}else{
			de.setLineBaking(de.getLineFinal().replace("\t", MiscI.i().getTabAsSpaces()));
			de.setLineBaking(de.getLineBaking().replace("\r", "")); //removes carriage return
			
			if(de.isApplyNewLineRequests()){
				de.setLineBaking(de.getLineBaking().replace("\\n","\n")); //converts newline request into newline char
			}else{
				de.setLineBaking(de.getLineBaking().replace("\n","\\n")); //disables any newline char without losing it
			}
			
//			/**
//			 * will put the line as it is,
//			 * the UI will handle the line being truncated.
//			 */
//			if(!de.bApplyNewLineRequests){
//				if(iConsoleMaxWidthInCharsForLineWrap<0){
//					applyDumpEntryOrPutToSlowQueue(de.bUseSlowQueue, de.strLineOriginal);
//					return;
//				}
//			}
			
			int iWrapAt = ilvConsoleMaxWidthInCharsForLineWrap.intValue();
//			if(iConsoleMaxWidthInCharsForLineWrap==null){
			if(iWrapAt==0){ //updateFontStuff();
				iWrapAt = cui().getLineWrapAt();
//				if(STYLE_CONSOLE.equals(strStyle)){ //TODO is faster?
//					iWrapAt = (int) (widthForDumpEntryField() / fWidestCharForCurrentStyleFont ); //'W' but any char will do for monospaced font
//				}
			}
			
//			ilvCurrentFixedLineWrapAtColumn.setObjectValue(ccSelf,iWrapAt);
			ilvCurrentFixedLineWrapAtColumn.setObjectRawValue(iWrapAt);
			
			//TODO use \n to create a new line properly
			if(iWrapAt>0){ //fixed chars wrap
				/**
				 * fills each line till the wrap column or \n
				 */
				String strLineToDump="";
				boolean bDumpAdd=false;
				for (int i=0;i<de.getLineBaking().length();i++){
					char ch = de.getLineBaking().charAt(i);
					strLineToDump+=ch;
					if(ch=='\n'){
						bDumpAdd=true;
					}else
					if(strLineToDump.length()==iWrapAt){
						bDumpAdd=true;
					}else
					if(i==(de.getLineBaking().length()-1)){
						bDumpAdd=true;
					}
					
					if(bDumpAdd){
						astrDumpLineList.add(strLineToDump);
						strLineToDump="";
						bDumpAdd=false;
					}
				}
				
			}else{ 
				astrDumpLineList.addAll(cui().wrapLineDynamically(de));
			}
		}
		
		/**
		 * ADD LINE WRAP INDICATOR
		 */
		for(int i=0;i<astrDumpLineList.size();i++){
			String strPart = astrDumpLineList.get(i);
			if(i<(astrDumpLineList.size()-1)){
				if(strPart.endsWith("\n")){
					strPart=strPart.substring(0, strPart.length()-1)+"\\n"; // new line indicator
				}else{
					strPart+="\\"; // line wrap indicator
				}
			}
			
			applyDumpEntryOrPutToSlowQueue(de.isUseSlowQueue(), strPart);
		}
		
	}

	private void applyDumpEntryOrPutToSlowQueue(boolean bUseSlowQueue, String str) {
		if(bUseSlowQueue){
			cui().getDumpEntriesSlowedQueueForManagement(ccSelf).add(str);
		}else{
			cui().getDumpEntriesForManagement(ccSelf).add(str);
		}
	}
	
	private void updateDumpQueueEntry(){
		while(adeDumpEntryFastQueue.size()>0){
			dumpEntry(adeDumpEntryFastQueue.remove(0));
		}
			
		if(!tdDumpQueuedSlowEntry.isReady(true))return;
		
		if(cui().getDumpEntriesSlowedQueueForManagement(ccSelf).size()>0){
			cui().getDumpEntriesForManagement(ccSelf).add(cui().getDumpEntriesSlowedQueueForManagement(ccSelf).remove(0));
			
			while(cui().getDumpEntriesForManagement(ccSelf).size() > ilvMaxDumpEntriesAmount.getLong()){
				cui().getDumpEntriesForManagement(ccSelf).remove(0);
			}
		}
	}
	
	/**
	 * TODO concatenate custom objects in the message key?
	 * see {@link #dumpEntry(DumpEntryData)}
	 * @param strMessageKey
	 * @param aobj
	 */
	public void dumpInfoEntry(String strMessageKey, Object... aobj){
		dumpEntry(false, btgShowInfo.get(), false, true, strInfoEntryPrefix+strMessageKey, aobj);
	}
	
	/**
	 * see {@link #dumpEntry(DumpEntryData)}
	 * @param strMessageKey
	 * @param aobj
	 */
	public void dumpWarnEntry(String strMessageKey, Object... aobj){
		dumpWarnEntry(null,strMessageKey,aobj);
	}
	public void dumpWarnEntry(Exception exUserCause, String strMessageKey, Object... aobj){
		EMessageType e = EMessageType.Warn;
		Exception ex = new PseudoException(e.s()+":"+strMessageKey);
		ex.initCause(exUserCause);
		ex.setStackTrace(Thread.currentThread().getStackTrace());
		dumpEntry(new DumpEntryData()
			.setImportant(e.s(),strMessageKey,ex)
			.setApplyNewLineRequests(false)
			.setDumpToConsole(btgShowWarn.get())
			.setUseSlowQueue(false)
			.setKey(strWarnEntryPrefix+strMessageKey)
			.setDumpObjects(aobj)
		);
	}
	
	private void addImportantMsgToBuffer(DumpEntryData de){
		ImportantMsgData imsg = new ImportantMsgData(de);
		de.setImportantMessageLink(imsg);
		addImportantMsgToBuffer(de.getType(), imsg);
	}
	
//	private void addImportantMsgToBuffer(String strMsgType,String strMsgKey,Exception ex){
//		addImportantMsgToBuffer(strMsgType, new ImportantMsgData(strMsgKey,ex,ex.getStackTrace()));
//	}
	
//	private void addImportantMsgToBuffer(String strMsgType,String strMsgKey,StackTraceElement[] aste){
//		addImportantMsgToBuffer(strMsgType, new ImportantMsg(strMsgKey,null,aste));
//	}
	public final IntLongVarField ilvImportantMessagesBufferMaxSize = new IntLongVarField(this, 1000, "");
	private void addImportantMsgToBuffer(String strMsgType,ImportantMsgData imsgNew){
		String str="["+strMsgType+"] "+imsgNew.getMsgKey();
		for(ImportantMsgData imsg:aimBufferList.toArray(new ImportantMsgData[0])){
			if(imsgNew.isIdenticalTo(imsg)){
				imsgNew.applyFirstOcurrenceCreationTimeFrom(imsg);
				aimBufferList.remove(imsg);
				break;
			}
		}
		
		aimBufferList.add(imsgNew.updateBufferedTime());
		
		Collections.sort(aimBufferList, ImportantMsgData.cmpBufferedTime());
		while(aimBufferList.size()>ilvImportantMessagesBufferMaxSize.intValue()){
			aimBufferList.remove(0);
		}
	}
	
	enum EMessageType{
		PROBLEM,
		WARNING,
		ERROR, DevWarn, UserInputError, Warn, Exception,
		;
		public String s(){return this.toString();}
	}
	
	/**
	 * A problem is something that may make other parts of the application malfunction.
	 * It will not break the application, but will make it misbehave.
	 * It can but shouldnt be ignored.
	 * see {@link #dumpEntry(DumpEntryData)}
	 * 
	 * @param strMessageKey
	 * @param aobj
	 */
	public void dumpProblemEntry(String strMessageKey, Object... aobj){
		EMessageType e = EMessageType.PROBLEM;
		Exception ex = new PseudoException(e.s()+":"+strMessageKey);
		ex.setStackTrace(Thread.currentThread().getStackTrace());
		dumpEntry(new DumpEntryData()
			.setImportant(e.s(),strMessageKey,ex)
			.setPrintStream(System.err)
			.setDumpToConsole(btgShowWarn.get())
			.setDumpObjects(aobj)
			.setKey(strErrorEntryPrefix+strMessageKey)
		);
	}
	
	/**
	 * User error is something the user did and can be fixed by him/her without having
	 * to modify this code.
	 * see {@link #dumpEntry(DumpEntryData)}
	 * 
	 * @param strMessageKey
	 * @param aobj
	 */
	public void dumpUserErrorEntry(String strMessageKey, Object... aobj){
		EMessageType e = EMessageType.UserInputError;
		Exception ex = new PseudoException(e.s()+":"+strMessageKey);
		ex.setStackTrace(Thread.currentThread().getStackTrace());
//		addImportantMsgToBuffer(strType,str,ex);
		dumpEntry(new DumpEntryData()
			.setImportant(e.s(),strMessageKey,ex)
			.setPrintStream(System.err)
			.setDumpToConsole(btgShowWarn.get())
			.setDumpObjects(aobj)
			.setKey(strErrorEntryPrefix+strMessageKey)
		);
//		dumpEntry(false, btgShowWarn.get(), false, Misc.i().getSimpleTime(btgShowMiliseconds.get())+strErrorEntryPrefix+str);
	}
	
	/**
	 * warnings that should not bother end users...
	 * see {@link #dumpEntry(DumpEntryData)}
	 * @param strMessageKey
	 */
	public void dumpDevWarnEntry(String strMessageKey, Object... aobj){
		EMessageType e = EMessageType.DevWarn;
		Exception ex = new PseudoException(e.s()+":"+strMessageKey);
		ex.setStackTrace(Thread.currentThread().getStackTrace());
//		addImportantMsgToBuffer(strType,str,ex);
//		dumpEntry(false, btgShowDeveloperWarn.get(), false, 
		dumpEntry(new DumpEntryData()
			.setImportant(e.s(),strMessageKey,ex)
			.setApplyNewLineRequests(false)
			.setDumpToConsole(btgShowDeveloperWarn.get())
			.setUseSlowQueue(false)
			.setDumpObjects(aobj)
			.setKey(strDevWarnEntryPrefix+strMessageKey)
		);
	}
	
	/**
	 * see {@link #dumpEntry(DumpEntryData)}
	 * @param strMessageKey
	 * @param aobj
	 */
	public void dumpDebugEntry(String strMessageKey, Object... aobj){
		dumpEntry(new DumpEntryData()
			.setDumpToConsole(btgShowDebugEntries.get())
			.setDumpObjects(aobj)
			.setKey("[DBG]"+strMessageKey)
		);
	}
	
	/**
	 * see {@link #dumpEntry(DumpEntryData)}
	 * @param strMessageKey
	 * @param aobj
	 */
	public void dumpDevInfoEntry(String strMessageKey, Object... aobj){
		dumpEntry(new DumpEntryData()
			.setDumpToConsole(btgShowDeveloperInfo.get())
			.setDumpObjects(aobj)
			.setKey(strDevInfoEntryPrefix+strMessageKey)
		);
//		dumpEntry(false, btgShowDeveloperInfo.get(), false, 
//			Misc.i().getSimpleTime(btgShowMiliseconds.get())+strDevInfoEntryPrefix+str);
	}
	
	/**
	 * see {@link #dumpEntry(DumpEntryData)}
	 * @param imsg
	 * @param iShowStackElementsCount
	 */
	private void dumpExceptionEntry(ImportantMsgData imsg, Integer iShowStackElementsCount) {
		/**
		 * it is coming from the message buffer, so will not read it...
		 */
		dumpExceptionEntryWork(null, imsg.getDumpEntryData().getException(), iShowStackElementsCount, false, imsg.getDumpEntryData().getCustomObjects());
	}
//	public void dumpExceptionEntry(String strMsgOverride, Throwable ex, Object... aobj){
//		dumpExceptionEntryWork(strMsgOverride, ex, null, true, aobj);
//	}
	public void dumpExceptionEntry(String strMsgOverride, Throwable ex, Object... aobj){
//		Exception exOverride=ex;
//		if(strMsgOverride!=null){
//			exOverride = new Exception(strMsgOverride+"; "+ex.getMessage());
//			exOverride.setStackTrace(ex.getStackTrace());
//			exOverride.initCause(ex.getCause());
//		}
		dumpExceptionEntryWork(strMsgOverride, ex, null, true, aobj);
	}
	/**
	 * see {@link #dumpEntry(DumpEntryData)}
	 * @param ex
	 * @param aobj
	 */
	public void dumpExceptionEntry(Exception ex, Object... aobj){
		dumpExceptionEntryWork(null, ex, null, true, aobj);
	}
	/**
	 * see {@link #dumpEntry(DumpEntryData)}
	 * 
	 * @param ex
	 * @param iShowStackElementsCount if null, will show nothing. If 0, will show all.
	 * @param bAddToMsgBuffer
	 */
//	private void dumpExceptionEntryWork(String strMsgPrepend, Exception ex, StackTraceElement[] asteStackOverride, Integer iShowStackElementsCount, boolean bAddToMsgBuffer, Object... aobj){
	private void dumpExceptionEntryWork(String strMsgPrepend, Throwable ex, Integer iShowStackElementsCount, boolean bAddToMsgBuffer, Object... aobj){
		String strMsgFull=ex.getMessage();
		if(strMsgPrepend!=null){
			strMsgFull=strMsgPrepend+"; "+strMsgFull;
			Exception exOverride = new Exception(strMsgFull);
			exOverride.setStackTrace(ex.getStackTrace());
			exOverride.initCause(ex.getCause());
			ex=exOverride;
		}
		
//		if(asteStackOverride!=null){
//			strMsgFull="(StackOverriden)";
//			Exception exOverride = new Exception(strMsgFull);
//			exOverride.setStackTrace(asteStackOverride);
//			exOverride.initCause(ex);
//			ex=exOverride;
//		}
		
//		String strTime="";
		PrintStream psStack = System.err;
		PrintStream psInfo = System.err;
		if(bAddToMsgBuffer){ //the exception is happening right now
//			strTime=MiscI.i().getSimpleTime(btgShowMiliseconds.get());
//			addImportantMsgToBuffer("Exception",ex.toString(),ex);
			ex.printStackTrace();
			psStack = null; //avoiding dup: already dumped to terminal, above
		}else{
			/**
			 * if it is not being added to buffer, means it is being reviewed by developer/user
			 * so will use stdout, as the exception is not happening right now.
			 */
			psInfo = psStack = System.out;
		}
		
		String strMsgKey = ex.toString();
		if (ex instanceof PrerequisitesNotMetException) {
			PrerequisitesNotMetException pex = (PrerequisitesNotMetException) ex;
			strMsgKey=pex.getMessageKey();
		}
		dumpEntry(new DumpEntryData()
//		.setImportant(EMessageType.Exception.s(), strMsgPrepend!=null ? strMsgPrepend : ex.toString(), ex)
			.setImportant(EMessageType.Exception.s(), strMsgFull, ex)
			.setPrintStream(psInfo) //this is good to show the time at terminal
			.setApplyNewLineRequests(false)
			.setDumpToConsole(btgShowException.get())
			.setDumpObjects(aobj)
			.setShowDumpObjects(iShowStackElementsCount!=null)
			.setUseSlowQueue(false)
			.setApplyNewLineRequests(true)
			.setKey(strExceptionEntryPrefix+strMsgKey)
		);
		
		Throwable twbToDump = ex;
		if(iShowStackElementsCount!=null){
			while(twbToDump!=null){
				StackTraceElement[] aste = twbToDump.getStackTrace();
//				if(asteStackOverride==null)asteStackOverride=twbToDump.getStackTrace();
				
				for(int i=0;i<aste.length;i++){
					StackTraceElement ste = aste[i]; 
					if(iShowStackElementsCount>0 && i>=iShowStackElementsCount)break;
					dumpStackPart(psStack,ste.toString());
				}
				
//				Throwable twbCurrent = twbToDump;
				twbToDump=twbToDump.getCause();
//				if(twbCurrent==twbToDump)break; //TODO why the exception cause would be itself!?!?
				if(twbToDump!=null){
					dumpStackPart(psStack,"[Caused by] "+twbToDump.getMessage());
//					asteStackOverride=twbToDump.getStackTrace();
				}
			}
		}
		
	}
	
	private void dumpStackPart(PrintStream psStack, String strMsg){
		dumpEntry(new DumpEntryData()
			.setPrintStream(psStack) 
			.setApplyNewLineRequests(true)
			.setDumpToConsole(true)
			.setUseSlowQueue(false)
			.setShowTime(false)
			.setKey(strSubEntryPrefix+strMsg));
	}
	
	public void dumpSubEntry(List<?> aobj){
		for(Object obj:aobj)dumpSubEntry(obj.toString());
	}
	/**
	 * a simple, usually indented, output
	 * see {@link #dumpEntry(DumpEntryData)}
	 * @param strMessageKey
	 */
	public void dumpSubEntry(String strMessageKey){
		if(strMessageKey.contains("\n")){
			String[] astr = strMessageKey.split("\n");
			for(String strLine:astr){
				dumpEntry(strSubEntryPrefix+strLine);
			}
		}else{
			dumpEntry(strSubEntryPrefix+strMessageKey);
		}
	}
	
	/**
	 * use with caution!
	 * TODO remove all BoolTogglers, and other field CMDs
	 * @param iccl
	 * @return 
	 */
	@Deprecated
	private ArrayList<CommandData> removeAllCmdsFor(IConsoleCommandListener iccl){
		MsgI.i().debug("WARNING: Removing all registered commands for: "+iccl.getClass().getName(), true, this);
//		System.err.println("WARNING: Removing all registered commands for: "+iccl.getClass().getName());
		
//		for(CommandData cmdd:trmCmds.values().toArray(new CommandData[0])){
//			if(cmdd.getOwner()==iccl){
//				if(trmCmds.remove(cmdd.getUniqueCmdId()) != cmdd){
//					throw new PrerequisitesNotMetException("Inconsistent tree");
//				}
//				
//				if(cmddLastAdded==cmdd)cmddLastAdded=null;
//			}
//		}
		
		ArrayList<String> astr = getAllCommandFor(iccl);
		MsgI.i().debug("WARNING: removing: "+astr.toString(), true, this);
//		System.err.println("WARNING: removing: "+astr.toString());
		
		ArrayList<CommandData> acmdRm = new ArrayList<CommandData>();
		
		for(String strUCmd:astr){
			CommandData cmd = trmCmddList.remove(strUCmd);
			if(cmd == null){
				throw new PrerequisitesNotMetException("Inconsistent");
			}else{
				acmdRm.add(cmd);
			}
		}
		
//		System.err.println("DBG: "+getAllCommandFor(iccl));
		
		if(trmCmddList.get(cmddLastAdded.getUniqueCmdId())==null){
			cmddLastAdded=null;
		}
		
		Thread.dumpStack();
		
		return acmdRm;
	}
	
	public ArrayList<CommandData> removeAllCmdsOfOwnersBeingDiscarded(){
		ArrayList<CommandData> acmdRm = new ArrayList<CommandData>();
		
		for(Entry<String, CommandData> entry:trmCmddList.entrySet().toArray(new Entry[0])){
			CommandData cmd = entry.getValue();
			if(DiscardableInstanceI.i().isBeingDiscardedRecursiveOwner(cmd)){
				if(trmCmddList.remove(entry.getKey())==null){
					throw new PrerequisitesNotMetException("Inconsistent"); //will never happen? should also not contain null values, wel.. 
				}
				
				MsgI.i().debug("RemovingCmd: "+entry.getKey());
				acmdRm.add(cmd);
				
				/**
				 * cmd and var owners are granted to be the same the moment such link is created.
				 */
				VarCmdFieldAbs vcf = cmd.getVar();
				if(ascfCmdWithCallerList.contains(vcf)){
					ascfCmdWithCallerList.remove(vcf);
				}
				
			}
		}
		
//		for(StringCmdField scf:new ArrayList<StringCmdField>(ascfCmdWithCallerList)){
//			if(DiscardableInstanceI.i().isBeingDiscardedRecursiveOwner(scf)){
//				ascfCmdWithCallerList.remove(scf);
//			}
//		}
		
		if(trmCmddList.get(cmddLastAdded.getUniqueCmdId())==null){
			cmddLastAdded=null;
		}
		
		Thread.dumpStack();
		
		return acmdRm;
	}
	
	public ArrayList<String> getAllCommandFor(IConsoleCommandListener iccl){
		ArrayList<String> astr = new ArrayList<String>();
		for(CommandData cmdd:trmCmddList.values().toArray(new CommandData[0])){
			if(cmdd.getOwner()==iccl){
				astr.add(cmdd.getUniqueCmdId());
			}
		}
		return astr;
	}
	
	/**
	 * The var {@link VarCmdFieldAbs} will be searched and assigned, no need to bring it as parameter.
	 * 
	 * @param irfcOwner
	 * @param strUniqueCmdIdNew
	 * @param strSimpleCmdIdNew
	 * @param bSkipSortCheck
	 */
	private void addCmdToValidList(IReflexFillCfg irfcOwner, String strUniqueCmdIdNew, String strSimpleCmdIdNew, boolean bSkipSortCheck){
//	private void addCmdToValidList(IReflexFillCfg irfcOwner, VarCmdFieldAbs vcfInitLink, String strUniqueCmdIdNew, String strSimpleCmdIdNew, boolean bSkipSortCheck){
//		String strConflict=null;
//		if(vcfInitLink!=null && vcfInitLink.getOwner()!=irfcOwner){
//			throw new PrerequisitesNotMetException("inconsistent",vcfInitLink, vcfInitLink.getOwner(), irfcOwner);
//		}
		
		if(irfcOwner==null){
			throw new PrerequisitesNotMetException("listener reference cannot be null");
		}
			
//		if(!astrCmdWithCmtValidList.contains(strNew)){
		if(!strUniqueCmdIdNew.startsWith(TOKEN_CMD_NOT_WORKING_YET)){
//			String strBaseCmdNew = extractCommandPart(strNew,0);
			String strBaseCmdNew = strUniqueCmdIdNew.split(" ")[0];
			
			String strComment = "";
			if(strUniqueCmdIdNew.length()>strBaseCmdNew.length()){
				strComment=strUniqueCmdIdNew.substring(strBaseCmdNew.length()).trim();
			}
			
//			CommandData cmddNew = new CommandData(irfcOwner, vcfInitLink, strBaseCmdNew, strSimpleCmdIdNew, strComment);
			CommandData cmddNew = new CommandData(irfcOwner, strBaseCmdNew, strSimpleCmdIdNew, strComment);
			
			/**
			 * conflict check, will discard in case it is identical origin
			 */
			for(CommandData cmdd:trmCmddList.values()){
				if(cmdd.getUniqueCmdId().equalsIgnoreCase(cmddNew.getUniqueCmdId())){
					/**
					 * already set from same origin, just skip.
					 * TODO explain clearly (after debug) WHY this is not an exception?!
					 */
					if(cmdd.identicalTo(cmddNew))return;
					
//				if(CommandData.getCmdComparator().compare(cmdd, cmddNew)==0){
					throw new PrerequisitesNotMetException("conflicting commands id "
						+"'"+cmdd.getUniqueCmdId()+"'"
						+" for "
						+"'"+getListenerId(cmdd.getOwner())+"("+cmdd.getOwner()+")'"
						+" vs "
						+"'"+getListenerId(cmddNew.getOwner())+"("+cmddNew.getOwner()+")'"
					);
				}
			}
			
			/**
			 * Simple cmd conflict must be here, after the discarding happens to not store
			 * discarded objects
			 */
			for(CommandData cmdd:trmCmddList.values()){
				if(cmdd.getSimpleCmdId().equalsIgnoreCase(cmddNew.getSimpleCmdId())){
					cmdd.addSimpleIdConflict(cmddNew);
					cmddNew.addSimpleIdConflict(cmdd);
//					aAllCmdConflictList.add(cmddNew);
				}
			}
			
			/**
			 * link command data with var
			 */
//			ArrayList<VarCmdFieldAbs> avcf = new ArrayList<VarCmdFieldAbs>();
			ArrayList<VarCmdFieldAbs> avcfLinkList = new ArrayList<VarCmdFieldAbs>();
			for(VarCmdFieldAbs vcf:ManageVarCmdFieldI.i().getHandledListCopy()){
				if(!vcf.isCmd())continue;//avcf.add(vcf);
//			}
////			avcf.addAll(ManageVarCmdFieldI.i().getListCopy(BoolTogglerCmdField.class));
////			avcf.addAll(ManageVarCmdFieldI.i().getListCopy(StringCmdField.class));
////			avcf.addAll(ManageVarCmdFieldI.i().getListCopy(KeyBoundVarField.class));
////			int iCount=0;
//			for(VarCmdFieldAbs vcf:avcf){
				if(vcf.getUniqueCmdId().equalsIgnoreCase(cmddNew.getUniqueCmdId())){
					vcf.setCmdData(cmddNew);
					cmddNew.setVar(vcf);
//					iCount++;
					avcfLinkList.add(vcf);
				}
			}
			if(avcfLinkList.size()>1){
				throw new PrerequisitesNotMetException("there should have only one var link to one cmd data",
					cmddNew, avcfLinkList);
			}
			
//			acmdList.add(cmddNew);
			trmCmddList.put(cmddNew.getUniqueCmdId(), cmddNew);
			cmddLastAdded = cmddNew;
			MsgI.i().debug(cmddNew.getOwner().getClass().getSimpleName()+":"+cmddNew.getUniqueCmdId(), true, this);
			
			/**
			 * coded sorting check (unnecessary actually), just useful for developers
			 * be more organized. 
			 */
			if(!bSkipSortCheck && trmCmddList.size()>0){ // DebugI.i().dumpValue(trmCmddList)
//				String strLast = acmdList.get(acmdList.size()-1).getBaseCmd();
				String strLast = cmddLastAdded.getUniqueCmdId();
				if(strLast.compareToIgnoreCase(strBaseCmdNew)>0){
					dumpDevWarnEntry("sorting required, last '"+strLast+"' new '"+strBaseCmdNew+"'");
				}
			}
		}
	}
	
	public CurrentCommandLine getCurrentCommandLine(){
		return ccl;
	}
//	public CurrentCommandLine getCCL(){
//		return ccl;
//	}
	
	private boolean cmdVarShow() {
		String strFilter = ccl.paramString(1);
		boolean bRestrictedOnly=false;
		if(strFilter==null){
			strFilter="";
		}else{
			strFilter=strFilter.trim();
			
			if(strFilter.startsWith(""+CommandsHelperI.i().getRestrictedToken())){
				bRestrictedOnly=true;
			}
			
			strFilter=CommandsHelperI.i().removeRestrictedToken(strFilter);
		}
		
		if(strFilter.isEmpty()){
			strFilter=".*";
		}else{
			strFilter=improveSimpleFilter(strFilter);
		}
		
//		strFilter=strFilter.trim();
		
		/**
		 * LIST all, user and restricted
		 */
		dumpInfoEntry("Variables list:");
//		boolean bRestrictedOnly=false;
//		if(strFilter.startsWith(""+CommandsHelperI.i().getRestrictedToken())){
//			bRestrictedOnly=true;
//			strFilter=strFilter.substring(1);
//		}
		
//		setupVars(false); //this will refresh any pending variables
//		databaseSave(); //this will refresh any pending variables too
		
		ArrayList<String> astr = new ArrayList<String>();
//		if(strFilter!=null)strFilter=strFilter.substring(1);
		ArrayList<String> avar = getVariablesIdentifiers(true);
		for(String strVarId : avar){
			boolean bRestricted = isRestrictedVar(strVarId);//tmRestrictedVariables.get(strVarId)!=null;
//			if(CommandsHelperI.i().isRestricted(strVarId) && !bRestrictedOnly)continue;
//			if(!CommandsHelperI.i().isRestricted(strVarId) && bRestrictedOnly)continue;
			if(bRestricted && !bRestrictedOnly)continue;
			if(!bRestricted && bRestrictedOnly)continue;
			
			/**
			 * empty filter will work too.
			 */
//			if(strVarId.startsWith(""+CommandsHelperI.i().getRestrictedToken())){
//				strVarId=strVarId.substring(1);
//			}
//			if(containsFilterString(CommandsHelperI.i().removeRestrictedToken(strVarId),strFilter)){
			if(containsFilterString(strVarId,strFilter)){
				astr.add(strVarId);
			}
		}
		Collections.sort(astr);
		for(String str:astr){
			varReport(str);
		}
		
		dumpSubEntry(getCommentPrefix()
			+"UserVarListHashCode="+tmUserVariables.hashCode()+", "
			+"Shown="+astr.size()+", "
			+"Total="+avar.size());
		
		return true;
	}
	
	protected boolean varDelete(String strVarId){
		/**
		 * DELETE/UNSET only user variables
		 */
		boolean bCmdWorkDone=tmUserVariables.remove(strVarId)!=null;
		if(bCmdWorkDone){
			dumpInfoEntry("Var '"+strVarId+"' deleted.");
		}else{
			dumpWarnEntry("Var '"+strVarId+"' not found.");
		}
		return bCmdWorkDone;
	}
	
	/**
	 * When creating variables, this method can only create custom user ones.
	 * @return
	 */
	private boolean cmdVarSet() {
		String strVarId = ccl.paramString(1);
		String strValue = ccl.paramString(2);
		
		if(strVarId==null)return false;
		
		boolean bCmdWorkDone = false;
		if(strVarId.trim().startsWith(getVarDeleteTokenStr())){
			bCmdWorkDone=varDelete(strVarId.trim().substring(1));
		}else{
			/**
			 * SET user or restricted variable
			 */
			if(isRestrictedAndDoesNotExist(strVarId))return false; //invalid user typed restricted var id
			if(isVarSet(strVarId)){
				dumpSubEntry("Previous value: \""+varGetValueString(strVarId)+"\"");
			}else{
				dumpSubEntry("Creating console var: \""+strVarId+"\"");
			}
//			varReport(strVarId); //to show previous value
			bCmdWorkDone=varSet(strVarId,strValue,true);
		}
		
		return bCmdWorkDone;
	}
	
	private boolean isRestrictedAndDoesNotExist(String strVar){
		if(CommandsHelperI.i().isRestricted(strVar)){
			// user can only set existing restricted vars
			if(!selectVarSource(strVar).containsKey(CommandsHelperI.i().removeRestrictedToken(strVar))){
				dumpDevInfoEntry("Restricted var does not exist: "+strVar);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean cmdVarSetCmp() {
		String strVarId = ccl.paramString(1);
		if(isRestrictedAndDoesNotExist(strVarId))return false;
		
		String strValueLeft = ccl.paramString(2);
		String strCmp = ccl.paramString(3);
		String strValueRight = ccl.paramString(4);
		
		if(strCmp.equals("==")){
			return varSet(strVarId, ""+strValueLeft.equals(strValueRight), true);
		}else
		if(strCmp.equals("!=")){
			return varSet(strVarId, ""+(!strValueLeft.equals(strValueRight)), true);
		}else
		if(strCmp.equals("||")){
			return varSet(strVarId, ""+
				(MiscI.i().parseBoolean(strValueLeft) || MiscI.i().parseBoolean(strValueRight)), true);
		}else
		if(strCmp.equals("&&")){
			return varSet(strVarId, ""+
				(MiscI.i().parseBoolean(strValueLeft) && MiscI.i().parseBoolean(strValueRight)), true);
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

	private boolean databaseSave(){
		ArrayList<String> astr = new ArrayList<>();
		
		ArrayList<String> astrVarList = getVariablesIdentifiers(false);
		for(String strVarId:astrVarList){
			astr.add(varReportPrepare(strVarId));
		}
		
		for(AliasData alias:aAliasList){
			astr.add(alias.toString());
		}
		
		flDB.delete();
		MiscI.i().fileAppendLineTS(flDB, getCommentPrefix()+" DO NOT MODIFY! auto generated. Set overrides at user init file!");
		MiscI.i().fileAppendListTS(flDB, astr);
		
		dumpInfoEntry("Database saved: "
			+astrVarList.size()+" vars, "
			+aAliasList.size()+" aliases, "
			+flDB.length()+" bytes,");
		
		setupRecreateFile();
		
		return true;
	}
	
	private boolean cmdDatabase(EDataBaseOperations edbo){
		if(edbo==null)return false;
		
		switch(edbo){
			case load:
				/**
				 * prepend on the queue is important mainly at the initialization
				 */
				dumpInfoEntry("Loading Console Database:");
				addCmdListOneByOneToQueue(MiscI.i().fileLoad(flDB),true,false);
				return true;
			case backup:
				return databaseBackup();
			case save:
				if(btgDbAutoBkp.b()){
					if(isDatabaseChanged()){
						databaseBackup();
					}
				}
				
				return databaseSave();
			case show:
				for(String str:MiscI.i().fileLoad(flDB)){
					dumpSubEntry(str);
				}
				return true;
		}
		
		return false;
	}
	
	private boolean hasChanged(ERestrictedSetupLoadableVars rv){
		String strValue = varGetValueString(""+CommandsHelperI.i().getRestrictedToken()+rv);
		switch(rv){
			case userAliasListHashcode:
				return !(""+aAliasList.hashCode()).equals(strValue);
			case userVariableListHashcode:
				return !(""+tmUserVariables.hashCode()).equals(strValue);
		}
		
		return false;
	}
	
	private boolean isDatabaseChanged(){
		if(hasChanged(ERestrictedSetupLoadableVars.userAliasListHashcode))return true;
		if(hasChanged(ERestrictedSetupLoadableVars.userVariableListHashcode))return true;
		
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
	
	private String getAliasHelp() {
		return "[<identifier> <commands>] | [<+|->identifier] | ["+getFuzzyFilterModeToken()+"filter]\n"
			+"\t\tCreates an alias to run a in-line commands block (each separated by '"+getCommandDelimiter()+"')\n"
			+"\t\tWithout params, will list all aliases\n"
			+"\t\t"+getFuzzyFilterModeToken()+"filter - will filter (contains) the alias list\n"
			+"\t\t-identifier - will block that alias execution\n"
			+"\t\t+identifier - will un-block that alias execution\n"
			+"\t\tObs.: to execute an alias, "
				+"prefix the identifier with '"+getAliasPrefix()+"', "
				+"ex.: "+getCommandPrefix()+getAliasPrefix() +"tst123";
	}
	
	private boolean cmdAlias() {
		boolean bOk=false;
		String strAliasId = ccl.paramString(1);
		if(strAliasId!=null && strAliasId.startsWith(""+getAliasAllowedToken())){
			bOk=aliasBlock(strAliasId.substring(1),false);
		}else
		if(strAliasId!=null && strAliasId.startsWith(""+getAliasBlockedToken())){
			bOk=aliasBlock(strAliasId.substring(1),true);
		}else{
			String strFilter=null;
			if(strAliasId!=null && strAliasId.startsWith(""+getFuzzyFilterModeToken())){
				if(strAliasId.length()>1)strFilter = strAliasId.substring(1);
				strAliasId=null;
			}
			
			if(strAliasId==null){
				/**
				 * will list all aliases (or filtered)
				 */
				for(AliasData alias:aAliasList){
					if(!containsFilterString(alias.toString(),strFilter))continue;
					dumpSubEntry(alias.toString());
				}
				dumpSubEntry(commentToAppend("AliasListHashCode="+aAliasList.hashCode()));
				bOk=true;
			}else{
				bOk=bLastAliasCreatedSuccessfuly;
			}
		}
		
		return bOk;
	}
	
	/**
	 * 
	 * @param strText
	 * @param strFilter if null, means it is to skip the filter, means "match all", so returns true
	 * @return
	 */
	private boolean containsFilterString(String strText, String strFilter){
		if(strFilter==null)return true;
		
		EStringMatchMode eMode = EStringMatchMode.Regex;
		if(strFilter.startsWith(""+getFuzzyFilterModeToken())){
			eMode=EStringMatchMode.Fuzzy;
			if(strFilter.length()>1){
				strFilter=strFilter.substring(1);
			}else{
				strFilter=null;
			}
		}
		
//		return MiscI.i().containsFuzzyMatch(strText, strFilter, !btgStringContainsFuzzyFilter.b(), true);
		return MiscI.i().containsFuzzyMatch(strText, strFilter, eMode, true);
	}
	
	private boolean cmdShowHistory() {
		String strFilter = ccl.paramString(1);
		ArrayList<String> astrToDump = new ArrayList<String>();
		if(strFilter!=null){
			for(String str:astrCmdHistory){
				if(!containsFilterString(str,improveSimpleFilter(strFilter)))continue;
				str=str.trim(); // to prevent fail of unique check by spaces presence
				if(!astrToDump.contains(str))astrToDump.add(str);
				Collections.sort(astrToDump);
			}
		}else{
			astrToDump.addAll(astrCmdHistory);
		}
		
		for(String str:astrToDump){
//			dumpSubEntry(str);
			dumpEntry(false, true, false, false, str);
		}
		
		return true;
	}

	private boolean isStartupCommandsQueueDone(){
		return bStartupCmdQueueDone;
	}
	
	private boolean cmdDb() {
		String strOpt = ccl.paramString(1);
		if(strOpt!=null){
			EDataBaseOperations edb = null;
			try {edb = EDataBaseOperations.valueOf(strOpt);}catch(IllegalArgumentException e){}
			return cmdDatabase(edb);
		}
		return false;
	}

	private AliasData getAlias(String strAliasId){
		AliasData aliasFound=null;
		for(AliasData aliasCheck : aAliasList){
			if(aliasCheck.strAliasId.toLowerCase().equals(strAliasId.toLowerCase())){
				aliasFound = aliasCheck;
				break;
			}
		}
		return aliasFound;
	}
	
	protected boolean hasVar(String strVarId){
		return getVarFromRightSource(strVarId)!=null;
	}
	
//	protected VarIdValueOwnerData getVar(String strVarId){
//		return getVarFromRightSource(strVarId);
//	}

	/**
	 * 
	 * @return false if toggle failed
	 */
	private boolean toggle(BoolTogglerCmdFieldAbs btg){
		if(ccl.paramBooleanCheckForToggle(1)){
			Boolean bEnable = ccl.paramBoolean(1);
			btg.setObjectRawValue(bEnable==null ? !btg.get() : bEnable); //overrider
			checkAndCreateConsoleVarLink(btg,true);
			dumpInfoEntry("Toggle, setting "+ccl.paramString(0)+" to "+btg.get());
			return true;
		}
		return false;
	}
	
	private void prepareCmdAndParams(){
		String strCleaningCmdLine = ccl.getOriginalLine(); //dont touch the original...
		
		if(strCleaningCmdLine!=null){
			strCleaningCmdLine = strCleaningCmdLine.trim();
			
			if(strCleaningCmdLine.isEmpty())return; // null; //dummy line
			
			// a comment shall not create any warning based on false return value...
			if(strCleaningCmdLine.startsWith(""+getCommentPrefix()))return; // null; //comment is a "dummy command"
			
			// now it is possibly a command
			
			strCleaningCmdLine = strCleaningCmdLine.trim();
			if(strCleaningCmdLine.startsWith(getCommandPrefixStr())){
				strCleaningCmdLine = strCleaningCmdLine.substring(getCommandPrefixStr().length()); //cmd prefix 1 char
			}
			
			if(strCleaningCmdLine.endsWith(getCommentPrefixStr())){
				strCleaningCmdLine=strCleaningCmdLine.substring(0,strCleaningCmdLine.length()-1); //-1 getCommentPrefix()Char
			}
			
//			return 
			prepareAndCleanMultiCommandsLine(strCleaningCmdLine);
		}
		
//		return null;
	}
	
//	private String getPreparedCmdLine(){
//		return strCmdLinePrepared;
//	}
	
	/**
	 * Cleans from comments.
	 * Queues multi commands line, will return a command skipper in this case.
	 * 
	 * @param strCleaningCmdLine
	 * @return the prepared and cleaned single command line, or a skipper
	 */
	private void prepareAndCleanMultiCommandsLine(String strCleaningCmdLine){
		/**
		 * remove comment
		 */
		int iCommentAt = strCleaningCmdLine.indexOf(getCommentPrefix());
		String strComment = "";
		if(iCommentAt>=0){
			strComment=strCleaningCmdLine.substring(iCommentAt);
			strCleaningCmdLine=strCleaningCmdLine.substring(0,iCommentAt);
		}
		
		/**
		 * queue multicommands line
		 */
		if(strCleaningCmdLine.contains(""+getCommandDelimiter())){
			ArrayList<String> astrMulti = new ArrayList<String>();
			astrMulti.addAll(Arrays.asList(strCleaningCmdLine.split(""+getCommandDelimiter())));
			for(int i=0;i<astrMulti.size();i++){
				/**
				 * replace by propagating the existing comment to each part that will be executed
				 */
				astrMulti.set(i, astrMulti.get(i).trim()
					+(strComment.isEmpty()?"":commentToAppend(strComment))
					+commentToAppend("SplitCmdLine")
				);
//				astrMulti.set(i, astrMulti.get(i).trim()+" "+getCommentPrefix()+"SplitCmdLine "+strComment);
			}
			
			addCmdListOneByOneToQueue(astrMulti,true,true);
			
			ccl.setCmdLinePrepared(RESTRICTED_CMD_SKIP_CURRENT_COMMAND.toString());
//			return RESTRICTED_CMD_SKIP_CURRENT_COMMAND.toString();
		}
		
//		astrCmdAndParams.clear(); 
//		clearPreparedCommandLine(); //make sure it is emptied
//		astrCmdAndParams.addAll(convertToCmdParamsList(strCleaningCmdLine));
		ccl.updateFrom(strCleaningCmdLine);
//		return String.join(" ",astrCmdAndParams);
	}
	
//	public ArrayList<String> getPreparedCmdParamsListCopy(){
//		return new ArrayList<String>(astrCmdAndParams);
//	}
	
	/**
	 * Each param can be enclosed within double quotes (")
	 * @param strFullCmdLine
	 * @return
	 */
	public ArrayList<String> convertToCmdAndParamsList(String strFullCmdLine){
		return convertToCmdParamsList(strFullCmdLine, null, null);
	}
	public ArrayList<String> convertToCmdParamsList(String strFullCmdLine, Integer iBeginIndexInclusive){
		return convertToCmdParamsList(strFullCmdLine, iBeginIndexInclusive, null);
	}
	public ArrayList<String> convertToCmdParamsList(String strFullCmdLine, Integer iBeginIndexInclusive, Integer iEndIndexExclusive){
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
				if(str.trim().startsWith(""+getCommentPrefix()))break; //ignore comments
				str=str.trim();
				str=str.replace("\"", ""); //remove all quotes on the string TODO could be only 1st and last? mmm... too much trouble...
//				astrCmdAndParams.add(str);
				astrCmdParams.add(str);
			}
		}
		
		if(iBeginIndexInclusive==null)iBeginIndexInclusive=0;
		if(iEndIndexExclusive==null)iEndIndexExclusive=astrCmdParams.size();
		
//		return strFullCmdLine;
		return new ArrayList<String>(
			astrCmdParams.subList(iBeginIndexInclusive, iEndIndexExclusive));
	}
	
	/**
	 * 
	 * @param bAll if false, will bring only user variables
	 * @return
	 */
	public ArrayList<String> getVariablesIdentifiers(boolean bAll){
		ArrayList<String> astr = Lists.newArrayList(tmUserVariables.keySet().iterator());
		if(bAll)astr.addAll(Lists.newArrayList(tmRestrictedVariables.keySet().iterator()));
		Collections.sort(astr);
		return astr;
	}
	
	/**
	 * see {@link #applyVariablesValues(String)}
	 * @param cc
	 * @param strParam
	 * @return
	 */
	public String applyVariablesValues(CurrentCommandLine.CompositeControl cc, String strParam){
		cc.assertSelfNotNull();
		return applyVariablesValues(strParam);
	}
	/**
	 * this method must be used just before a command is going to be executed,
	 * so variables have time to be updated by other commands etc.
	 * @param strParam
	 * @return
	 */
	private String applyVariablesValues(String strParam){
		// fast skip
		if(!strParam.contains(getVariableExpandPrefix()+"{"))return strParam;
		
		for(String strVarId : getVariablesIdentifiers(true)){
			String strToReplace=getVariableExpandPrefix()+"{"+strVarId+"}";
			if(strParam.toLowerCase().contains(strToReplace.toLowerCase())){
//				strParam=strParam.replace(strToReplace, ""+getVarHT(strVarId).get(strVarId));
				strParam=strParam.replaceAll(
					"(?i)"+Pattern.quote(strToReplace), 
					""+getVarValue(strVarId));
				
				// nothing remaining to be done
				if(!strParam.contains(getVariableExpandPrefix()+"{"))break;
			}
		}
		return strParam;
	}
	
	private ConsoleVariable getVarFromRightSource(String strId){
		return selectVarSource(strId).get(CommandsHelperI.i().removeRestrictedToken(strId));
	}
	
	private boolean cmdVarAdd(String strVarId, String strValueAdd, boolean bSave){
		return varAdd(getVarFromRightSource(strVarId), new ConsoleVariable(strVarId, strValueAdd, null, null), bSave);
	}
	
	private boolean varAdd(ConsoleVariable cvarBase, ConsoleVariable cvarAdd, boolean bSave){
		return varAddOrOverwriteWork(false, cvarBase, cvarAdd, bSave);
	}
	private boolean varOverwrite(ConsoleVariable cvarBase, ConsoleVariable cvarAdd, boolean bSave){
		return varAddOrOverwriteWork(true, cvarBase, cvarAdd, bSave);
	}
	/**
	 * TODO clarify this code
	 * In case variable exists will be this method.
	 * @param strVarId
	 * @param strValueAdd
	 * @param bOverwrite
	 * @return
	 */
	private boolean varAddOrOverwriteWork(
		boolean bOverwrite, 
		ConsoleVariable cvarRequested, 
		ConsoleVariable cvarAddOrOverwrite, 
		boolean bSave
	){
		if(isRestrictedAndDoesNotExist(cvarRequested.getUniqueVarId(true)))return false;
		
		ConsoleVariable cvarFound = getVarFromRightSource(cvarRequested.getUniqueVarId(true));
		if(cvarFound!=cvarRequested){ //TODO pointless check?
			throw new PrerequisitesNotMetException("requested and found should not differ!", cvarRequested, cvarFound);
//			dumpDevWarnEntry("shouldnt be the same object?", cvarRequested, cvarFound);
		}
		
		Object objValueCurrent = cvarRequested.getRawValue();
		if(objValueCurrent==null){
			if(!bOverwrite)dumpWarnEntry("existing value is null, changing from ADD to SET (overwrite) mode", cvarRequested);
			bOverwrite=true;
		}
		
		Object objValueNew = null;
		if(bOverwrite){
			objValueNew = cvarAddOrOverwrite.getRawValue();
		}else{
	//		if(objValueCurrent==null && !bOverwrite){
	//			throw new PrerequisitesNotMetException("cannot add to an existing null value, as the existing value type cannot be verified", cvarRequested, cvarAddOrOverwrite);
	////			dumpExceptionEntry(new NullPointerException("value is null for var "+cvarRequested.getUniqueVarId(true)));
	////			return false;
	//		}
			objValueNew = prepareAddedValue(objValueCurrent, cvarAddOrOverwrite.getRawValue());
		}
			
		cvarRequested.setRawValue(ccSelf,MiscI.i().parseToPrimitivesWithPriority(objValueNew==null ? null : ""+objValueNew));
		return varApply(cvarRequested,null,bSave);
	}
	
	private Object prepareAddedValue(Object objValueCurrent, Object objValueAdd){
		assertValueIsPrimitive(objValueCurrent);
		assertValueIsPrimitive(objValueAdd);
		
		// after this moment both should have the same class type
		objValueCurrent	=MiscI.i().parseToPrimitivesWithPriority(""+objValueCurrent);
		objValueAdd			=MiscI.i().parseToPrimitivesWithPriority(""+objValueAdd);
		
		Class clType = objValueCurrent.getClass();
		if(objValueCurrent.getClass()==objValueAdd.getClass()){
			if(Boolean.class.isAssignableFrom(clType)){
				// boolean is always overwrite
				return MiscI.i().parseBoolean(""+objValueAdd);
			}else
			if(Long.class.isAssignableFrom(clType)){
				return ((Long)objValueCurrent)+((Long)objValueAdd);
			}else
			if(Double.class.isAssignableFrom(clType)){
				return ((Double)objValueCurrent)+((Double)objValueAdd);
			}else{ //string
				return ""+objValueCurrent+objValueAdd;
			}
		}else{
			throw new PrerequisitesNotMetException("Value type to be added should be "
				+clType.getSimpleName()+"("+objValueCurrent+") but is "
				+objValueAdd.getClass()+"("+objValueAdd+")");
		}
		
//		Object objValueNew = objValueCurrent;
//		
//		Class clTypeRequired = objValueCurrent.getClass();
//		boolean bAddTypeMismatchError = false;
//		
//		String strValueAdd = ""+cvarAddOrOverwrite.getRawValue();
//		if(Boolean.class.isAssignableFrom(clTypeRequired)){
//			// boolean is always overwrite
//			objValueNew = MiscI.i().parseBoolean(strValueAdd);
//		}else
//		if(Long.class.isAssignableFrom(clTypeRequired)){
//			Long lValueCurrent = (Long)objValueCurrent;
//			Long lValueAdd=null;
//			try{lValueAdd = Long.parseLong(strValueAdd);}catch(NumberFormatException e){}// accepted exception!
//			if(lValueAdd!=null){
//				if(bOverwrite)lValueCurrent=0L;
//				lValueCurrent+=lValueAdd;
//				objValueNew = lValueCurrent;
//			}else{
//				bAddTypeMismatchError=true;
//			}
//		}else
//		if(Double.class.isAssignableFrom(clTypeRequired)){
//			Double dValueCurrent = (Double)objValueCurrent;
//			Double dValueAdd=null;
//			try{dValueAdd = Double.parseDouble(strValueAdd);}catch(NumberFormatException e){}// accepted exception!
//			if(dValueAdd!=null){
//				if(bOverwrite)dValueCurrent=0.0;
//				dValueCurrent+=dValueAdd;
//				objValueNew = dValueCurrent;
//			}else{
//				bAddTypeMismatchError=true;
//			}
//		}else{
//			/**
//			 * simple String
//			 */
//			if(bOverwrite)objValueCurrent="";
//			objValueNew = ""+objValueCurrent+strValueAdd;
//		}
//		
//		if(bAddTypeMismatchError){
//			dumpWarnEntry("Value type to be added should be: "+clTypeRequired.getSimpleName(), strValueAdd);
//			return false;
//		}
	}
	
//	private boolean isRestricted(String strId){
//		return strId.startsWith(""+CommandsHelperI.i().getRestrictedToken());
//	}
	
	/**
	 * 
	 * @param strVarId
	 * @return "null" if not set
	 */
	private String varGetValueString(String strVarId){
		return ""+getVarValue(strVarId);
	}
	
	private Object getVarValue(String strVarId){
		ConsoleVariable var = getVarFromRightSource(strVarId);
		if(var==null)throw new PrerequisitesNotMetException("var is not set", strVarId, this);
		return var.getRawValue();
	}
//	private void setVarValue(String strVarId, VarIdValueOwner vivo){
//		selectVarSource(strVarId).put(strVarId,vivo);
//	}
	private TreeMap<String, ConsoleVariable> selectVarSource(ConsoleVariable cvar){
		return cvar.isRestricted() ? tmRestrictedVariables : tmUserVariables;
	}
	private TreeMap<String, ConsoleVariable> selectVarSource(String strVarId){
		boolean bRestricted = CommandsHelperI.i().isRestricted(strVarId);
		if(!bRestricted){
			if(isRestrictedVar(strVarId)){
				bRestricted=true;
			}
		}
		
		if(bRestricted){
			return tmRestrictedVariables;
		}else{
			return tmUserVariables;
		}
	}
	
	private File getVarFile(String strVarId){
		if(isRestrictedVar(strVarId)){
			return flSetup;
		}else{
			return flDB;
		}
	}
	
	private void varSaveAppendingAtSetupFile(){
		ArrayList<String> astr = getVariablesIdentifiers(true);
		Collections.sort(astr);
		for(String strVarId : astr){
//			if(CommandsHelperI.i().isRestricted(strVarId)){
			if(isRestrictedVar(strVarId)){
				fileAppendVar(strVarId);
			}
		}
		
		// store keybinds
		MiscI.i().fileAppendListTS(flSetup, 
			GlobalManageKeyBindI.i().getReportAsCommands(scfBindKey, true));
	}
	
	private void fileAppendVar(String strVarId){
		ConsoleVariable cvar = getVarFromRightSource(strVarId);
		if(cvar.getRawValue()==null){
			MsgI.i().devWarn("will not save a var with null value", strVarId);
			return;
		}
		
		String strCommentOut="";
		String strReadOnlyComment="";
		if(isRestrictedVar(strVarId)){
//			try{ERestrictedSetupLoadableVars.valueOf(strVarId.substring(1));}catch(IllegalArgumentException e){
//			if(isCommandPrefixVar(strVarId)){
			if(strVarId.startsWith(""+CommandsHelperI.i().getRestrictedToken()+ReflexFillI.i().getPrefixCmdDefault())){
				/**
				 * TODO commands param values saved as vars? not following the right implementation? this shouldnt even exist right?
				 * comment non loadable restricted variables, like the ones set by commands
				 */
				strCommentOut=getCommentPrefixStr();
				strReadOnlyComment="(ReadOnly)";
			}
		}
		
		MiscI.i().fileAppendLineTS(getVarFile(strVarId), strCommentOut+varReportPrepare(strVarId)+strReadOnlyComment);
	}
	
//	private boolean isCommandPrefixVar(String strVarId) {
//		return strVarId.startsWith(""+CommandsHelperI.i().getRestrictedToken()+ReflexFillI.i().getPrefixCmdDefault());
//	}

//	@Deprecated //this was preventing user changes to be loaded on next startup
//	private void _fileAppendVar(String strVarId){
//		String strCommentOut="";
//		String strReadOnlyComment="";
//		if(CommandsHelperI.i().isRestricted(strVarId)){
//			try{ERestrictedSetupLoadableVars.valueOf(strVarId.substring(1));}catch(IllegalArgumentException e){
//				/**
//				 * comment non loadable restricted variables, like the ones set by commands
//				 */
//				strCommentOut=getCommentPrefixStr();
//				strReadOnlyComment="(ReadOnly)";
//			}
//		}
//		
//		MiscI.i().fileAppendLine(getVarFile(strVarId), strCommentOut+varReportPrepare(strVarId)+strReadOnlyComment);
//	}
	
	public boolean isRestrictedVar(String strId){
		if(CommandsHelperI.i().isRestricted(strId))return true;
		return tmRestrictedVariables.containsKey(strId);
	}
	
	private void createConsoleVarLinkWithField(ConsoleVariable cvar, VarCmdFieldAbs vcfOwner){
		if(cvar.getRestrictedVarOwner()==null){
			if(vcfOwner==null){
				for(VarCmdFieldAbs vcfTmp:ManageVarCmdFieldI.i().getHandledListCopy()){
					if(!vcfTmp.isVar())continue;
//					DebugI.i().conditionalBreakpoint(KeyBoundVarField.class.isInstance(vcfTmp));
//					if(vcfTmp.getUniqueVarId().equals(CommandsHelperI.i().removeRestrictedToken(cvar.getUniqueVarId()))){
					if(vcfTmp.getUniqueVarId().equals(cvar.getUniqueVarId(false))){
						vcfOwner=vcfTmp;
						break;
					}
				}
			}
			
			if(vcfOwner!=null){
				vcfOwner.setConsoleVarLink(ccSelf,cvar);
			}else{
				MsgI.i().devWarn("has no owner", cvar); //TODO has no owner yet? will lazy set later?
			}
		}
	}
	
	private boolean varApply(ConsoleVariable cvar, VarCmdFieldAbs vcfOwner, boolean bSave){
		/**
		 * stores the console variable
		 */
		TreeMap<String, ConsoleVariable> tmSrc = selectVarSource(cvar);//.getUniqueVarId(true));
		ConsoleVariable cvarAtSrc = tmSrc.get(cvar.getUniqueVarId(false));
		if(cvarAtSrc!=null && cvar!=cvarAtSrc){
			dumpDevWarnEntry("console variable is being modified", cvar.getUniqueVarId(true), cvarAtSrc, cvar);
		}
		tmSrc.put(cvar.getUniqueVarId(false), cvar);
		
//	if(vivo.getOwner()!=null){ //TODO!!! PROLEM: can only set owner if it is not set!
//	// it can have no owner (field), it can be a simple variable.
//	vivo.getOwner().setConsoleVarLink(ccSelf,vivo);
//}
		if(tmSrc==tmRestrictedVariables){ //cvar.isRestricted()
			createConsoleVarLinkWithField(cvar, vcfOwner);
		}
		
		/**
		 * save the console variable
		 */
		if(bSave)fileAppendVar(cvar.getUniqueVarId(true));
		
//		if(CommandsHelperI.i().isRestricted(cvar.getUniqueVarId()) && btgShowDeveloperInfo.b()){
		if(cvar.isRestricted() && btgShowDeveloperInfo.b()){
			varReport(cvar.getUniqueVarId(true));
		}
		
		return true;
	}
	
	public String varReportPrepare(String strVarId) {
		ConsoleVariable cvar = getVarFromRightSource(strVarId);
		if(cvar!=null){
			return varReportPrepare(cvar);
		}
		return "ERROR: Var Not found: "+strVarId;
	}
	public String varReportPrepare(ConsoleVariable cvar) {
		String str="";
		
		// as reusable command
		str+=getCommandPrefix();
		
		if(CMD_VAR_SET.getCmdData()!=null){
			if(CMD_VAR_SET.getCmdData().isSimpleCmdIdConflicting()){
				str+=CMD_VAR_SET.getCmdData().getUniqueCmdId();
			}else{
				str+=CMD_VAR_SET.getCmdData().getSimpleCmdId();
			}
		}else{
			str+=CMD_VAR_SET.getUniqueCmdId();
		}
		
		str+=" ";
		str+=cvar.getUniqueVarId(true);
		str+=" ";
		
		Object objVal = cvar.getRawValue();
////		if(cvar!=null){
//			if(objVal==null){
//				str+=null;
//			}else{
//				str+="\""+cvar.getRawValue()+"\"";
//			}
		if(objVal==null){
			str+=null;
		}else{
			str+="\"";
			if(cvar.getRestrictedVarOwner()!=null){
				str+=cvar.getRestrictedVarOwner().getValueAsString();
			}else{
				assertValueIsPrimitive(objVal);
				str+=objVal;
			}
			str+="\"";
		}
			str+=" ";
//		}
		
		// comments
		str+="#";
		// var type
		if(cvar!=null && objVal!=null){
			str+=objVal.getClass().getSimpleName();
		}else{
			str+="(ValueNotSet)";
		}
		str+=" ";
		// scope
		str+=(cvar.isRestricted()?"(Restricted)":"(User)");
		// dev info
		if(btgShowDeveloperInfo.b()){
			if(cvar.getRestrictedVarOwner()!=null && cvar.getRfcfgClassHoldingTheOwner()!=null){
				str+=" ";
//				str+="["+vivo.getRfcfgClassHoldingTheOwner().getClass().getName()+"]";
				str+="["+ReflexFillI.i().getDeclaringClass(cvar.getRfcfgClassHoldingTheOwner(), cvar.getRestrictedVarOwner()).getName()+"]";
			}
		}
		str+=" ";
		str+=cvar.getHelp();
		
		return str;
	}
	
	public void varReport(String strVarId) {
		ConsoleVariable cvar=getVarFromRightSource(strVarId);
		if(cvar!=null){
			varReport(cvar);
		}else{
			dumpSubEntry(strVarId+" is not set...");
			dumpSubEntry("");//for readability
		}
	}
	public void varReport(ConsoleVariable cvar) {
		dumpSubEntry(varReportPrepare(cvar.getUniqueVarId(false)));
		dumpSubEntry("");//for readability
	}
	
	public boolean varSet(StringCmdField scfId, String strValue, boolean bSave) {
		return varSet(
			CommandsHelperI.i().getRestrictedToken()+scfId.getUniqueCmdId(),
			strValue,
			bSave);
	}
	
//	private boolean varRestoreTo(BoolTogglerCmd btgOwner){
//		VarIdValueOwner vivo = getVar(CommandsHelperI.i().getRestrictedToken()+btgOwner.getCmdId());
//		if(vivo==null)return false;
//		btgOwner.setObjectValue(vivo.getObjectValue());
//		dumpSubEntry(btgOwner.getReport());
//		return true;
//	}
//	
//	private boolean varRestoreTo(TimedDelayVar tdOwner){
//		VarIdValueOwner vivo = getVar(CommandsHelperI.i().getRestrictedToken()+tdOwner.getVarId());
//		if(vivo==null)return false;
//		tdOwner.setObjectValue(vivo.getObjectValue());
//		dumpSubEntry(tdOwner.getReport());
//		return true;
//	}
	
	/**
	 * @param owner
	 * @param strValue
	 * @return
	 */
	private boolean varApplyValueAtRestrictedOwner(VarCmdFieldAbs owner, String strValue){
		ConsoleVariable cvar = getVarFromRightSource(CommandsHelperI.i().getRestrictedToken()+owner.getUniqueVarId());
		if(cvar==null)return false;
		owner.setObjectRawValue(strValue);
		dumpSubEntry(owner.getFailSafeDebugReport());
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public void checkAndCreateConsoleVarLink(VarCmdFieldAbs vcfOwner, boolean bSave) {
		IReflexFillCfg rfcfg=null;
		if(vcfOwner instanceof IReflexFillCfgVariant){
			/**
			 * check if it is configured as a class field should be
			 */
			rfcfg = ((IReflexFillCfgVariant)vcfOwner).getOwner();
			if(rfcfg==null){
				MsgI.i().devWarn("is not a field", vcfOwner);
				return;
//				return false;
			}
		}
		
		ConsoleVariable cvar=vcfOwner.getConsoleVarLink(ccSelf);
		if(cvar==null){ //the console var link was not set yet
//		if(cvar!=null){ //the console var link was already set
//			if(!vcfOwner.isAllowNullValue() && !vcfOwner.isValueNull()){
//				cvar.setRawValue(vcfOwner.getRawValue());
//			}
//		}else{ //the console var link was not set yet
			
//			Object objVal=null;
//			if(!vcfOwner.isAllowNullValue() && !vcfOwner.isValueNull()){
//				objVal=vcfOwner.getRawValue();
//			}
			
			cvar = new ConsoleVariable(
				CommandsHelperI.i().getRestrictedToken()+vcfOwner.getUniqueVarId(),
				null, //objVal,
				rfcfg,
				vcfOwner.getHelp());
			
			varApply(cvar, vcfOwner, bSave);
		}
	}
	
	/**
	 * this will apply the value at the owner also (if it exists)
	 * @param strVarId
	 * @param strValue
	 * @param bSave
	 * @return
	 */
	public boolean varSet(String strVarId, String strValue, boolean bSave) {
//		VarIdValueOwnerData vivo = getVar(strVarId);
//		if(vivo==null){
//			VarCmdFieldManagerI.i().getListFullCopy()
//			
//			vivo = new VarIdValueOwnerData(strVarId, strValue, null, null, null);
//		}
//		
		/**
		 * "null" is a reserved global keyword
		 */
		if(strValue.equals("null")){
			MsgI.i().devWarn("null value not allowed", strVarId); //TODO why it should be prevented? lets see how it works out...
			return false;
		}
		
//		if(strValue.equals("null")){
//			strValue=null;
//		}
		
		boolean bOk=varSetFixingType(new ConsoleVariable(strVarId, strValue, null, null), bSave);
		
		if(bOk){
			ConsoleVariable cvar = getVarFromRightSource(strVarId);
			if(cvar.getRestrictedVarOwner()!=null){
				varApplyValueAtRestrictedOwner(cvar.getRestrictedVarOwner(),strValue);
			}
		}
		
		return bOk;
	}
//	public boolean varSet(String strVarId, String strValue, boolean bSave) {
//		boolean bOk=varSet(new VarIdValueOwnerData(strVarId, strValue, null, null, null), bSave);
//		
//		if(bOk){
//			VarIdValueOwnerData vivo = getVar(strVarId);
//			if(vivo.getOwner()!=null){
//				varRestoreTo(vivo.getOwner(),strValue);
//			}
//		}
//		
//		return bOk;
//	}
	
	/**
	 * This is able to create restricted variables too.
	 */
	public boolean varSetFixingType(ConsoleVariable cvar, boolean bSave) {
		if(getAlias(cvar.getUniqueVarId(false))!=null){
			dumpUserErrorEntry("Variable identifier '"+cvar.getUniqueVarId(true)+"' conflicts with existing alias!");
			return false;
		}
		
		if(cvar.getRawValue()==null)return false; // null has no type... 
		
		ConsoleVariable cvarExisting = getVarFromRightSource(cvar.getUniqueVarId(true));
		if(cvarExisting!=null){
//			if(vivoExisting.getOwner() instanceof BoolToggler){
//				int i=0;
//			}
			return varOverwrite(cvarExisting, cvar, bSave);
		}else{
			/**
			 * Priority:
			 * Double would parse a Long.
			 * Boolean would be accepted by String that accepts everything. 
			 */
			boolean bOk=false;
			
			String strValue = null;
			if(cvar.getRestrictedVarOwner()!=null){
				strValue=cvar.getRestrictedVarOwner().getValueAsString();
			}else{
				Object objVal = cvar.getRawValue();
				assertValueIsPrimitive(objVal);
				
				/**
				 * this will only work well for primitives, 
				 * otherwise the parsing would break at VarCmdFieldAbs' subclass
				 */
				strValue = ""+cvar.getRawValue(); 
			}
//			String strValue = ""+cvar.getRawValue();
			if(!cvar.isRestrictedVarLinkConsistent()){
				cvar.setValFixType(ccSelf, MiscI.i().parseToPrimitivesWithPriority(strValue));
				bOk=varApply(cvar,null,bSave);
//				if(!bOk)try{cvar.setValFixType(ccSelf,Long  .parseLong      (strValue));bOk=varApply(cvar,null,bSave);}catch(NumberFormatException e){}// accepted exception!
//				if(!bOk)try{cvar.setValFixType(ccSelf,Double.parseDouble    (strValue));bOk=varApply(cvar,null,bSave);}catch(NumberFormatException e){}// accepted exception!
//				if(!bOk)try{cvar.setValFixType(ccSelf,MiscI.i().parseBoolean(strValue));bOk=varApply(cvar,null,bSave);}catch(NumberFormatException e){}// accepted exception!
//				if(!bOk){		cvar.setValFixType(ccSelf,strValue);bOk=varApply(cvar,null,bSave);}
			}else{ // already set
//			if(cvar.getRestrictedVarOwner()!=null && cvar.getRestrictedVarOwner().isConsoleVarLink(cvar)){
//			if(!bOk){		cvar.setValFixType(ccSelf,strValue);bOk=varApply(cvar,bSave);}
//				DebugI.i().conditionalBreakpoint(true); //TODO remove
			}
			
			return bOk;
		}
	}
	
	private void assertValueIsPrimitive(Object objRawValue) {
		if(!MiscI.i().isPrimitive(objRawValue)){
			throw new PrerequisitesNotMetException("not supported type "+objRawValue.getClass().getName(), objRawValue);
		}
	}

	public boolean isVarSet(String strVarId){
		return getVarFromRightSource(strVarId)!=null;
	}
	
//	private Double varGetValueDouble(String strVarId){
//		Object obj = ahkVariables.get(strVarId);
//		if(obj==null)return null;
//		if(obj instanceof Double)return (Double)obj;
//		dumpExceptionEntry(new typeex);
//		return null;
//	}
	
	private boolean aliasBlock(String strAliasId, boolean bBlock) {
		for(AliasData alias : aAliasList){
			if(alias.strAliasId.toLowerCase().equals(strAliasId.toLowerCase())){
				dumpInfoEntry((bBlock?"Blocking":"Unblocking")+" alias "+alias.strAliasId);
				alias.bBlocked=bBlock;
				return true;
			}
		}
		return false;
	}
	
	private String improveSimpleFilter(String strFilter){
		strFilter=strFilter.trim();
		
		boolean bInverseMode=false;
		if(strFilter.startsWith("-")){
			strFilter=strFilter.substring(1);
			bInverseMode=true;
		}
		
		if(MiscI.i().isValidIdentifierCmdVarAliasFuncString(strFilter)){
			/**
			 * if user typed a simple string (not a regex), 
			 * it will match like contains mode
			 */
			if(strFilter.isEmpty()){
				strFilter=".*";
			}else{
				strFilter=".*"+strFilter+".*";
			}
		}
		
		if(bInverseMode){
			strFilter="^((?!"+strFilter+").)*$";
		}
		
		return strFilter;
	}
	
	public void cmdShowHelp(String strFilter) {
		if(strFilter==null){
			dumpInfoEntry("Available Commands ("+trmCmddList.size()+"):");
		}else{
			strFilter = improveSimpleFilter(strFilter);
			dumpInfoEntry("Help for '"+strFilter+"':");
		}
		
//		Collections.sort(acmdList, CommandData.getCmdComparator());
		
		ArrayList<CommandData> acmddList = new ArrayList<CommandData>(trmCmddList.values());
		Collections.sort(acmddList); //cmdList.contains(scfAlias) trmCmddList.get("cmd_CommandsTest_CommandsDelegator_Alias")
		
		for(CommandData cmdd:acmddList){//trmCmds.values()){ cmdList.indexOf(scfAlias)
			String str=cmdd.asHelp(); 
			str=str.trim();
			if(str.startsWith(getCommandPrefixStr()))str=str.substring(1);
			if(!containsFilterString(str,strFilter))continue;
//			dumpSubEntry(getCommandPrefix()+cmd.asHelp());
			dumpSubEntry(cmdd.asHelp());
			dumpSubEntry(""); //for readability
		}
	}
	
//	public ArrayList<String> getBaseCommandsWithComment() {
//		if(astrBaseCmdCmtCacheList.size()!=acmdList.size()){
//			astrBaseCmdCmtCacheList.clear();
//			for(CommandData cmd:acmdList){
//				astrBaseCmdCmtCacheList.add(cmd.getBaseCmd()+" "+cmd.getComment());
//				Collections.sort(astrBaseCmdCmtCacheList);
//			}
//		}
//		return astrBaseCmdCmtCacheList;
//	}
	
	public ArrayList<String> getAllPossibleCommands(){
		ArrayList<String> a = new ArrayList<String>();
		
		for(CommandData cmdd:trmCmddList.values()){
			if(!a.contains(cmdd.getSimpleCmdId()))a.add(cmdd.getSimpleCmdId());
			a.add(cmdd.getUniqueCmdId());
		}
		
		Collections.sort(a);
		
		return a;
	}
	
	public ArrayList<String> getUniqueCommands(){
		return new ArrayList<String>(Arrays.asList(trmCmddList.keySet().toArray(new String[0])));
//		if(astrBaseCmdCacheList.size()!=hmCmds.size()){
//			astrBaseCmdCacheList.clear();
//			for(CommandData cmd:hmCmds.values()){ //keys would suffice tho
//				astrBaseCmdCacheList.add(cmd.getBaseCmd());
//				Collections.sort(astrBaseCmdCacheList);
//			}
//		}
//		return astrBaseCmdCacheList;
	}
	
	protected ECmdReturnStatus stillExecutingCommand(){
//		ECmdReturnStatus e = 
		return execCmdFromConsoleRequestRoot();
//		if(e.compareTo(ECmdReturnStatus.NotFound)==0)return false;
//		return true;
	}
	
	public boolean isFound(ECmdReturnStatus ecrs){
		switch(ecrs){
			case FoundAndWorked:
//			case FoundCallerAndQueuedIt:
				
			case FoundAndExceptionHappened:
			case FoundAndFailedGracefully:
				
				return true;
		}
		return false;
	}
	
	/**
	 * Command format: "commandIdentifier any comments as you wish"
	 * @param strFullCmdLineOriginal if null will populate the array of valid commands
	 * @return false if command execution failed
	 */
	private ECmdReturnStatus executeCommand(final String strFullCmdLineOriginal){
		assertConfigured();
		
		ECmdReturnStatus ecrs = ECmdReturnStatus.NotFound;
		
		ccl.setOriginalLine(strFullCmdLineOriginal);
//		strCmdLineOriginal = strFullCmdLineOriginal;
		
//		boolean bCommandFound = false;
		
		try{
			if(!isFound(ecrs))ecrs=precmdRawLineCheckEndOfStartupCmdQueue();
			
			if(!isFound(ecrs))ecrs=precmdRawLineCheckAlias();
			
			if(!isFound(ecrs))ecrs=precmdExpandRegexAsCommands();
			
			if(!isFound(ecrs)){
				/**
				 * we will have a prepared line after below
				 */
//				strCmdLinePrepared = prepareCmdAndParams(strCmdLineOriginal);
				prepareCmdAndParams();
			}
			
			if(!isFound(ecrs))ecrs=stillExecutingCommand();
			
		}catch(Exception ex){
			dumpExceptionEntry(ex);
			
			ecrs=ECmdReturnStatus.FoundAndExceptionHappened.setCustomReturnValue(ex);
		}
		
//		catch(NumberFormatException e){
//			// keep this one as "warning", as user may simply fix the typed value
//			dumpWarnEntry("NumberFormatException: "+e.getMessage());
//			e.printStackTrace();
//			bCmdFoundAndApplied=false;
//		}
		
//		/**
//		 * clear prepared line to mark end of command execution attempt
//		 */
//		ccl.clear();//clearPreparedCommandLine();
		ccl = new CurrentCommandLine(this); //prepare for a fresh new command
		
		return ecrs;
	}
	
//	private void clearPreparedCommandLine() {
//		strCmdLinePrepared = null;
//		astrCmdAndParams.clear();
//	}

	public void dumpEntry(String strLineOriginal, Object... aobj){
		dumpEntry(true, true, false, false, strLineOriginal, aobj);
	}
	
	public void dumpEntry(boolean bApplyNewLineRequests, boolean bDump, boolean bUseSlowQueue, boolean bShowTime, String strLineOriginal, Object... aobj){
		DumpEntryData de = new DumpEntryData()
			.setApplyNewLineRequests(bApplyNewLineRequests)
			.setDumpToConsole(bDump)
			.setDumpObjects(aobj)
			.setUseSlowQueue(bUseSlowQueue)
			.setShowTime(bShowTime)
			.setKey(strLineOriginal);
		
		dumpEntry(de);
	}
	
	public void update(float tpf) {
		if(GlobalOSAppI.i().isApplicationExiting()){
//			GlobalAppRefI.i().stop();
			return;
		}
		
		if(!bConfigured)throw new NullPointerException("not configured yet");
		if(!bInitialized)throw new NullPointerException("not initialized yet");
		if(PrerequisitesNotMetException.isExitRequested())cmdRequestCleanSafeNormalExit();
		
		this.fTPF = tpf;
		if(tdLetCpuRest.isActive() && !tdLetCpuRest.isReady(true))return;
		
		if(!cui().isInitializedProperly())return;
		
		updateNewDay();
		updateCheckNewVarCmdAndToggleFields();
		updateExecPreQueuedCmdsBlockDispatcher(); //before exec queue 
		updateExecConsoleCmdQueue(); // after pre queue
		updateDumpQueueEntry();
		updateHandleException();
	}
	
	public float getTPF(){
		return fTPF;
	}
	
	private void updateNewDay() {
		String str = MiscI.i().getSimpleDate();
		if(!str.equalsIgnoreCase(strCurrentDay) || DebugI.i().isKeyEnabled(EDebugKey.ShowConsNewDayInfoOnce)){
			strCurrentDay=str;
			dumpInfoEntry("Welcome to a new day "+strCurrentDay+"!");
			DebugI.i().disableKey(EDebugKey.ShowConsNewDayInfoOnce);
		}
	}
	
	/**
	 * if there are new commands, booltogglers, variables available, prepare them.
	 */
	private void updateCheckNewVarCmdAndToggleFields() {
//		int iBoolTogglersListHashCodeTmp = VarCmdFieldManagerI.i().getListHash();
//		if(iBoolTogglersListHashCodeTmp!=iBoolTogglersListHashCode){
		if(ManageVarCmdFieldI.i().isListChanged()){
			bFillCommandList=true;
			updateCmdWithCallerList();
			checkCmdValidityBoolTogglers(); //update cmd list with new booltoggler commands
			
			// LAST THING is Setup Vars!!!
			setupVars(false); //update var list with all kinds of vars (booltogglers too)
			bFillCommandList=false;
			
//			iBoolTogglersListHashCode=iBoolTogglersListHashCodeTmp;
		}
	}
	
	ArrayList<StringCmdField> ascfCmdWithCallerList = new ArrayList<StringCmdField>();
	private File	flConsDataPath;
	private boolean	bConsoleCommandRunningFromDirectUserInput;
	private void updateCmdWithCallerList() {
		for(StringCmdField scf:ManageVarCmdFieldI.i().getListCopy(StringCmdField.class)){
			if(scf.isCallerAssigned()){
				if(!ascfCmdWithCallerList.contains(scf)){
					checkCmdValidity(scf);
					
					ascfCmdWithCallerList.add(scf);
				}
			}
		}
	}

	private void addCmdListOneByOneToQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex){
		ArrayList<String> astrCmdListCopy = new ArrayList<String>(astrCmdList);
		
		if(bShowExecIndex){
			for(int i=0;i<astrCmdListCopy.size();i++){
				astrCmdListCopy.set(i, astrCmdListCopy.get(i)+commentToAppend("ExecIndex="+i));
			}
		}
		
		if(bPrepend){
			Collections.reverse(astrCmdListCopy); //so the first to be execute will continue being so
		}
		for(String strCmd:astrCmdListCopy){
			addCmdToQueue(strCmd, bPrepend);
		}
	}
	
	protected void addCmdsBlockToPreQueue(ArrayList<String> astrCmdList, boolean bPrepend, boolean bShowExecIndex, String strBlockInfo){
		PreQueueCmdsBlockSubListData pqe = new PreQueueCmdsBlockSubListData();
		pqe.strBlockInfo=strBlockInfo;
		pqe.bPrepend=bPrepend;
		pqe.astrCmdList = new ArrayList<String>(astrCmdList);
		for(int i=0;i<pqe.astrCmdList.size();i++){
			pqe.astrCmdList.set(
				i, 
				pqe.astrCmdList.get(i)
					+commentToAppend(pqe.getUniqueInfo()));
		}
		if(bShowExecIndex){
			for(int i=0;i<pqe.astrCmdList.size();i++){
				pqe.astrCmdList.set(i, pqe.astrCmdList.get(i)+commentToAppend("ExecIndex="+i)
				);
			}
		}
		
		astrExecConsoleCmdsPreQueue.add(pqe);
		dumpDevInfoEntry("AddedCommandBlock"+commentToAppend(pqe.getUniqueInfo()));
	}
	
	private boolean doesCmdQueueStillHasUId(String strUId){
		for(String strCmd:astrExecConsoleCmdsQueue){
			if(strCmd.contains(strUId))return true;
		}
		return false;
	}
	
	private void updateExecPreQueuedCmdsBlockDispatcher(){
		for(PreQueueCmdsBlockSubListData pqe:astrExecConsoleCmdsPreQueue.toArray(new PreQueueCmdsBlockSubListData[0])){
			if(pqe.tdSleep.isActive()){
				if(pqe.tdSleep.isReady()){
					if(doesCmdQueueStillHasUId(commentToAppend(pqe.getUniqueInfo()))){
						/**
						 * will wait all commands of this same pre queue list
						 * to complete, before continuing, in case the delay was too short.
						 */
						continue;
					}else{
						pqe.tdSleep.setActive(false);
					}
				}else{
					if(!pqe.bInfoSleepBegin){
						dumpDevInfoEntry("Sleeping for "
							+MiscI.i().fmtFloat(pqe.tdSleep.getDelayLimitSeconds())+"s: "
							+commentToAppend(pqe.getUniqueInfo()));
						pqe.bInfoSleepBegin =true;
					}
					continue;
				}
			}
			
			if(pqe.astrCmdList.size()==0 || pqe.bForceFailBlockExecution){
				astrExecConsoleCmdsPreQueue.remove(pqe);
			}else{
				ArrayList<String> astrCmdListFast = new ArrayList<String>();
				
				while(pqe.astrCmdList.size()>0){
					String strCmd = pqe.astrCmdList.remove(0);
					String strCmdBase = extractCommandPart(strCmd, 0);
					if(strCmdBase==null)continue;
					
					if(CMD_SLEEP.equals(strCmdBase)){
						String strParam1 = extractCommandPart(strCmd, 1);
						strParam1=applyVariablesValues(strParam1);
						Float fDelay=null;
						try{fDelay = Float.parseFloat(strParam1);}catch(NumberFormatException ex){
							dumpExceptionEntry(ex);
							pqe.bForceFailBlockExecution=true;
							break;
						}
						pqe.tdSleep.resetAndChangeDelayTo(fDelay);
						pqe.tdSleep.updateTime();
//						dumpDevInfoEntry(strCmd);
						break;
					}else{
						astrCmdListFast.add(strCmd);
					}
				}
				
				if(pqe.bForceFailBlockExecution)continue;
				
				if(astrCmdListFast.size()>0){
//					if(pqe.bPrepend){
//						Collections.reverse(astrCmdListFast);
//					}
//					for(String str:astrCmdListFast){
//						addCmdToQueue(str, pqe.bPrepend);
//					}
					astrCmdListFast.add(CMD_CONSOLE_SCROLL_BOTTOM.toString());
					addCmdListOneByOneToQueue(astrCmdListFast, pqe.bPrepend, false);
				}
				
			}
		}
	}
	public void addCmdToQueue(StringCmdField scf){
		addCmdToQueue(scf.getUniqueCmdId());
	}
	public void addCmdToQueue(String strFullCmdLine){
		addCmdToQueue(strFullCmdLine,false);
	}
	public void addCmdToQueue(String strFullCmdLine, boolean bPrepend){
		strFullCmdLine=strFullCmdLine.trim();
		
		if(strFullCmdLine.startsWith(""+getCommentPrefix()))return;
		if(strFullCmdLine.isEmpty())return;
		if(strFullCmdLine.equals(""+getCommandPrefix()))return; //the token alone, is like empty
		
		if(!strFullCmdLine.startsWith(""+CommandsHelperI.i().getRestrictedToken())){
			if(!strFullCmdLine.startsWith(""+getCommandPrefix())){
				strFullCmdLine=getCommandPrefix()+strFullCmdLine;
			}
		}
		
		dumpDevInfoEntry("CmdQueued: "+strFullCmdLine+(bPrepend?" #Prepended":""));
		
//		if(strFullCmdLine==null || strFullCmdLine.equalsIgnoreCase(getCommandPrefix()+"null")){
//			throw new PrerequisitesNotMetException("TODO what???", strFullCmdLine, bPrepend);
//		}
		
		if(bPrepend){
			astrExecConsoleCmdsQueue.add(0,strFullCmdLine);
		}else{
			astrExecConsoleCmdsQueue.add(strFullCmdLine);
		}
	}
	
	private void updateExecConsoleCmdQueue() {
		if(astrExecConsoleCmdsQueue.size()>0){ // one per time! NO while here!!!!
			String strCmdOnQueue=astrExecConsoleCmdsQueue.remove(0);
			if(!strCmdOnQueue.trim().endsWith(""+getCommentPrefix())){
				if(btgShowExecQueuedInfo.get()){ // prevent messing user init cfg console log
					dumpInfoEntry("QueueExec: "+strCmdOnQueue);
				}
			}
			
			ECmdReturnStatus ecrs = executeCommand(strCmdOnQueue);
			switch(ecrs){
				case FoundAndWorked:
					ecrs.getExtractingCustomReturnValue(); //just to reset
//				case FoundCallerAndQueuedIt:
					break;
				default:
					dumpWarnEntry(ecrs.getExtractingCustomReturnValueAsException(),"QueueExecFail("+ecrs+"): "+strCmdOnQueue);
					break;
			}
//			if(ecrs.compareTo(ECmdReturnStatus.FoundAndWorked)!=0){
//					dumpWarnEntry("QueueExecFail("+ecrs+"): "+str);
//			}
		}
	}
	
	private void showHelpForFailedCommand(String strFullCmdLine){
//		if(validateCommand(strFullCmdLine,true)){
		if(validateCommand(strFullCmdLine,false)){
//			addToExecConsoleCommandQueue(CMD_HELP+" "+extractCommandPart(strFullCmdLine,0));
			cmdShowHelp(extractCommandPart(strFullCmdLine,0));
		}else{
			dumpWarnEntry("Invalid command: "+strFullCmdLine);
		}
	}
	private void cmdHistLoad() {
		astrCmdHistory.addAll(MiscI.i().fileLoad(flCmdHist));
	}
	
	private void dumpSave(DumpEntryData de) {
//		if(de.isSavedToLogFile())return;
		MiscI.i().fileAppendLineTS(flLastDump, de.getLineFinal(true));
	}
	/**
	 * These variables can be loaded from the setup file!
	 */
	enum ERestrictedSetupLoadableVars{
		userVariableListHashcode,
		userAliasListHashcode,
	}
	
	public void setupRecreateFile(){
		dumpDevInfoEntry("Recreating restricted vars setup file:");
		
//		flSetup.delete();
		if(flSetup.exists()){
			flSetup.renameTo(new File(flSetup.getAbsoluteFile()+MiscI.i().getDateTimeForFilename()+".bkp"));
		}
		
		/**
		 * comments for user
		 */
		MiscI.i().fileAppendLineTS(flSetup, getCommentPrefix()+" DO NOT EDIT!");
		MiscI.i().fileAppendLineTS(flSetup, getCommentPrefix()
			+" This file will be overwritten by the application!");
		MiscI.i().fileAppendLineTS(flSetup, getCommentPrefix()
			+" To set overrides use the user init config file.");
		MiscI.i().fileAppendLineTS(flSetup, getCommentPrefix()
			+" For command's values, the commands usage are required, the variable is just an info about their setup value.");
//		MiscI.i().fileAppendLine(flSetup, getCommentPrefix()
//			+" Some values will be read tho to provide restricted functionalities not accessible to users.");
		MiscI.i().fileAppendLineTS(flSetup, getCommentPrefix()
				+"____________________________________");
		
		setupVars(true);
	}
	
	private void setupVarsAllFrom(ArrayList<VarCmdFieldAbs> avcf){
//		ArrayList<IVarIdValueOwner> a2 = new ArrayList<IVarIdValueOwner>(a);
		
		/**
		 * check for conflicting var ids
		 */
		for(VarCmdFieldAbs vcf:avcf.toArray(new VarCmdFieldAbs[0])){
			for(VarCmdFieldAbs vcf2:avcf.toArray(new VarCmdFieldAbs[0])){
				if(vcf==vcf2)continue; //skip self
				
				if(vcf.getUniqueVarId().equalsIgnoreCase(vcf2.getUniqueVarId())){
					throw new PrerequisitesNotMetException("conflicting var id: "+vcf.getUniqueVarId(),
						(ReflexFillI.i().getDeclaringClass(vcf.getOwner(),vcf)),
						vcf.getOwner().getClass(),
						(ReflexFillI.i().getDeclaringClass(vcf2.getOwner(),vcf2)),
						vcf2.getOwner().getClass());
				}
			}
		}
		
		for(VarCmdFieldAbs vcf:avcf){
			checkAndCreateConsoleVarLink(vcf,false);
		}
	}
	
	/**
	 * override to allow custom external vars to be updated thru this method
	 * will be called from {@link #setupVars(boolean)}
	 */
	protected void setupVars(){}
	
	private void setupVars(boolean bSave){
		setupVars();
		
		varSet(""+CommandsHelperI.i().getRestrictedToken()+ERestrictedSetupLoadableVars.userVariableListHashcode,
			""+tmUserVariables.hashCode(),
			false);
		
		varSet(""+CommandsHelperI.i().getRestrictedToken()+ERestrictedSetupLoadableVars.userAliasListHashcode,
			""+aAliasList.hashCode(),
			false);
		
		ArrayList<VarCmdFieldAbs> acvarAllVarsList = new ArrayList<VarCmdFieldAbs>();
		acvarAllVarsList.addAll(ManageVarCmdFieldI.i().getListCopy(KeyBoundVarField.class));
		acvarAllVarsList.addAll(ManageVarCmdFieldI.i().getListCopy(BoolTogglerCmdFieldAbs.class)); //special case!
		acvarAllVarsList.addAll(ManageVarCmdFieldI.i().getListCopy(TimedDelayVarField.class));
		acvarAllVarsList.addAll(ManageVarCmdFieldI.i().getListCopy(FloatDoubleVarField.class));
		acvarAllVarsList.addAll(ManageVarCmdFieldI.i().getListCopy(IntLongVarField.class));
		acvarAllVarsList.addAll(ManageVarCmdFieldI.i().getListCopy(StringVarField.class));
		setupVarsAllFrom(acvarAllVarsList);
		
		if(bSave)varSaveAppendingAtSetupFile();
	}
	/**
	 * 
	 * @param strCmdFull
	 * @param iPart 0 is base command, 1.. are params
	 * @return
	 */
	public String extractCommandPart(String strCmdFull, int iPart){
		strCmdFull=strCmdFull.trim();
		if(strCmdFull.startsWith(""+getCommandPrefix())){
			strCmdFull=strCmdFull.substring(1); //1 getCommandPrefix()Char
		}
		
//		String[] astr = strCmdFull.split("[^$"+strValidCmdCharsRegex+"]");
//		if(astr.length>iPart){
//			return astr[iPart];
//		}
		ArrayList<String> astr = convertToCmdAndParamsList(strCmdFull);
		if(iPart>=0 && astr.size()>iPart){
			String strCmdPart = astr.get(iPart);
			if(strCmdPart.endsWith(getCommandDelimiterStr())){
				strCmdPart = strCmdPart.substring(0, strCmdPart.length()-getCommandDelimiterStr().length());
			}
			return strCmdPart;
		}
		
		return null;
	}
	
	public boolean isCommandString(String str) {
		return str.trim().startsWith(getCommandPrefixStr());
	}
	
	private void dumpAllStats(){
		cui().dumpAllStats();
		
		dumpSubEntry("Database User Variables Count = "+getVariablesIdentifiers(false).size());
		dumpSubEntry("Database User Aliases Count = "+aAliasList.size());
		
//		dumpSubEntry("Previous Second FPS  = "+lPreviousSecondFPS);
		
		for(BoolTogglerCmdFieldAbs btg : ManageVarCmdFieldI.i().getListCopy(BoolTogglerCmdFieldAbs.class)){
			dumpSubEntry(btg.getFailSafeDebugReport());
		}
		
//		for(TimedDelayVar td : TimedDelayVar.getListCopy()){
//			dumpSubEntry(td.getReport());
//		}
		
		dumpSubEntry("User Dump File = "+flLastDump.getAbsolutePath());
		dumpSubEntry("User Commands History File = "+flCmdHist.getAbsolutePath());
		dumpSubEntry("User Database File = "+flDB.getAbsolutePath());
		dumpSubEntry("User Config File = "+flInit.getAbsolutePath());
	}
	
	/**
	 * Validates if the first extracted word is a valid command.
	 * 
	 * @param strCmdChk can be the full command line here
	 * @return
	 */
	public boolean validateCommand(String strCmdChk, boolean bOnlyUniques){
		strCmdChk = extractCommandPart(strCmdChk,0);
		
		return (bOnlyUniques?getUniqueCommands():getAllPossibleCommands())
			.contains(strCmdChk);
	}
	
	public static enum EStats{
		CommandsHistory,
		ConsoleSliderControl,
		CopyFromTo,
		FunctionCreation(true),
		IfConditionalBlock(true),
		MouseCursorPosition,
		TimePerFrame,
		CallQueueSize,
		;
		
		private boolean bShow;
		public boolean isShow(){return bShow;};
		
		EStats(){}
		EStats(boolean b){this.bShow=b;}
	}
	
	/**
	 * this must be here because a console not using JME can show this info too.
	 * @return
	 */
	public String prepareStatsFieldText(){
		String strStatsLast = "";
		
		if(EStats.CopyFromTo.isShow()){
			strStatsLast+=
					// user important
					"Cp"+iCopyFrom
						+">"+iCopyTo //getDumpAreaSelectedIndex()
						+";";
		}
						
		if(EStats.CommandsHistory.isShow()){
			strStatsLast+=
					"Hs"+iCmdHistoryCurrentIndex+"/"+(astrCmdHistory.size()-1)
						+";";
		}
					
//		if(EStats.IfConditionalBlock.b && aIfConditionNestedList.size()>0){
//			strStatsLast+=
//					"If"+aIfConditionNestedList.size()
//						+";";
//		}
					
//		if(EStats.FunctionCreation.b && strPrepareFunctionBlockForId!=null){
//			strStatsLast+=
//					"F="+strPrepareFunctionBlockForId
//						+";";
//		}
			
		if(EStats.CallQueueSize.isShow()){
			strStatsLast+=
				"Qu"+ManageCallQueueI.i().getWaitingAmount()
					+";";
		}
		
		/**
		 * KEEP HERE AS REFERENCE!
		 * IMPORTANT, DO NOT USE
		 * clipboard reading is too heavy...
		+"Cpbd='"+retrieveClipboardString()+"', "
		 */
						
					// less important (mainly for debug)
		if(EStats.ConsoleSliderControl.isShow()){
			strStatsLast+=cui().getDumpAreaSliderStatInfo();
		}
					
//		if(EStats.TimePerFrame.b){
//			strStatsLast+=
//					"Tpf"+(fpslState.isEnabled() ? (int)(fTPF*1000.0f) : Misc.i().fmtFloat(fTPF,6)+"s")
//						+(fpslState.isEnabled()?
//							"="+fpslState.getFrameDelayByCpuUsageMilis()+"+"+fpslState.getThreadSleepTimeMilis()+"ms"
//							:"")
//						+";";
//		}
		
		return strStatsLast;
	}
	
	private void cmdHistSave(String strCmd) {
		MiscI.i().fileAppendLineTS(flCmdHist,strCmd);
	}
	
	private IConsoleUI cui(){
		return GlobalConsoleUII.i();
	}
	
	/**
	 * configure must happen before initialization
	 * @param cui
	 * @param sapp
	 */
	public void configure(){ //IConsoleUI icui){//, SimpleApplication sapp){
		if(bConfigured)throw new NullPointerException("already configured.");		// KEEP ON TOP
		
//		this.cui = GlobalConsoleUII.i();
		
//		Init.i().initialize(sapp, this);
//		TimedDelayVarField.configure(this);
//		BoolTogglerCmdField.configure(this);
		ReflexFillI.i().assertReflexFillFieldsForOwner(this);
		
		DebugI.i().configure(this);
		MiscI.i().configure(this);
		MsgI.i().addListener(this);
//		ReflexHacks.i().configure(this, this);
//		ReflexHacks.i().configure(this);
//		SingleInstanceState.i().initialize(sapp, this);
		
//		CommandsBackgroundState.i().configure(sapp, icui, this);
		
//		if(icui==null)throw new NullPointerException("invalid "+IConsoleUI.class.getName()+" instance");
//		this.icui=icui;
		
//		this.sapp=sapp;
		
		/**
		 * postponed to let configuration run smoothly
		 */
		ManageCallQueueI.i().addCall(new CallableX(this) {
			@Override
			public Boolean call() {
				return initialize();
			}
		}.setIgnoreRecursiveCallWarning());
//		});
		
		bConfigured=true;
	}
	
	private boolean initialize(){
		if(!bConfigured)throw new NullPointerException("not configured yet");
		if(bInitialized)throw new NullPointerException("already initialized, remove dup");
		
		tdDumpQueuedSlowEntry.updateTime();
		
		CallableX call = new CallableX(this) {
			@Override
			public Boolean call() {
				cui().updateEngineStats();
				return true;
			}
		};
		btgEngineStatsView.setCallerAssigned(call);
		btgEngineStatsFps.setCallerAssigned(call);
		
		btgConsoleCpuRest.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				tdLetCpuRest.setActive(btgConsoleCpuRest.b());
				return true;
			}
		});
	
		// init dump file, MUST BE THE FIRST!
		flLastDump = new File(fileNamePrepareLog(strFileLastDump,false));
		flLastDump.delete(); //each run will have a new file
		
		// init cmd history
		flCmdHist = new File(fileNamePrepareLog(strFileCmdHistory,false));
		cmdHistLoad();
		
		// restricted vars setup
		setupVars(false);
		flSetup = new File(fileNamePrepareCfg(strFileSetup,false));
		if(flSetup.exists()){
			addCmdListOneByOneToQueue(MiscI.i().fileLoad(flSetup), false, false);
		}
		
		// before user init file
		addCmdToQueue(btgShowExecQueuedInfo.getCmdIdAsCommand(false));
		addCmdToQueue(btgShowDebugEntries.getCmdIdAsCommand(false));
		addCmdToQueue(btgShowDeveloperWarn.getCmdIdAsCommand(false));
		addCmdToQueue(btgShowDeveloperInfo.getCmdIdAsCommand(false));
		
		ManageCallQueueI.i().addCall(new CallableX(this) {
			@Override
			public Boolean call() {
				fixSimpleCmdConflict(scfHelp.getCmdData(), scfHelp.getSimpleId(), true, true);
				return true;
			}
		});
//		addCmdToQueue(scfChangeCommandSimpleId.getUniqueCmdId()+" "+CMD_HELP.getUniqueCmdId()+" help");
		
		// init DB
		flDB = new File(fileNamePrepareCfg(strFileDatabase,false));
		addCmdToQueue(CMD_DB.getUniqueCmdId()+" "+EDataBaseOperations.load);
		addCmdToQueue(CMD_DB.getUniqueCmdId()+" "+EDataBaseOperations.save
			+" "+commentToAppend("to shrink it"));
		
		// init user cfg
		flInit = new File(fileNamePrepareCfg(strFileInitConsCmds,false));
		if(flInit.exists()){
			addCmdListOneByOneToQueue(MiscI.i().fileLoad(flInit), false, false);
		}else{
			MiscI.i().fileAppendLineTS(flInit, getCommentPrefix()+" User console commands here will be executed at startup.");
		}
		
		// for debug mode, auto show messages
		if(RunMode.bDebugIDE)addCmdToQueue(scfMessageReview);
		
		// init valid cmd list
		bFillCommandList=true;
		execCmdFromConsoleRequestRoot();
		bFillCommandList=false;
//		executeCommand(null); //to populate the array with available commands
		
		bInitialized=true;
		
		return bInitialized;
	}
	
	enum ETest{
		fps,
		allChars,
		stats,
		exception,
		throwExceptionNPE,
		throwExceptionPNME,
		;
		
		public static ETest valueOfCaseInsensitive(String str){
			for(ETest e:values()){
				if(e.toString().toLowerCase().equals(str.toLowerCase()))return e;
			}
			return null;
		}
	}
	private void cmdTest(){
		dumpInfoEntry("testing...");
		String strOption = ccl.paramString(1);
		
		if(strOption==null){
//			dumpSubEntry(Arrays.toString(ETest.values()));
			dumpSubEntry(Arrays.asList(ETest.values()));
			return;
		}
		
		ETest et = ETest.valueOfCaseInsensitive(strOption);
		switch(et){
			case fps:
//			sapp.setSettings(settings);
				break;
			case allChars:
				for(char ch=0;ch<256;ch++){
					dumpSubEntry(""+(int)ch+"='"+Character.toString(ch)+"'");
				}
				break;
			case stats:
				strDebugTest=ccl.paramString(2);
//				dumpDevInfoEntry("lblTxtSize="+csaTmp.lblStats.getText().length());
				break;
			case exception:
				test2();
				test3();
				break;
			case throwExceptionNPE:
				throw new NullPointerException("test exception");
			case throwExceptionPNME:
				throw new PrerequisitesNotMetException("test exception");
		}
		
	}
	private void test3(){
		test2();
	}
	private void test2(){
		dumpExceptionEntry(new NullPointerException("testEx"));
		dumpWarnEntry("testWarn");
	}

	public void showClipboard(){
		showClipboard(true);
	}
	public void showClipboard(boolean bShowNL){
		String strClipboard=MiscI.i().retrieveClipboardString();
	//	dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+" ( each line enclosed with \\"+strLineEncloseChar+" ):");
		dumpInfoEntry("Clipboard contents, size="+strClipboard.length()+":");
		String[] astrLines = strClipboard.split("\n");
	//	for(String str:astr)dumpEntry(strLineEncloseChar+str+strLineEncloseChar);
	//	dumpEntry(""); // this empty line for clipboard content display is i
		String strFill="";for(int i=0;i<20;i++)strFill+=strCopyRangeIndicator;
		dumpEntry(strFill+" Clipboard BEGIN>>>");
		for(int i=0;i<astrLines.length;i++){
			String strLine=astrLines[i];
			if(bShowNL && i<(astrLines.length-1))strLine+="\\n";
			dumpEntry(false,true,false,false,strLine);
		}
		dumpEntry("<<<Clipboard END "+strFill);
		if(bAddEmptyLineAfterCommand)dumpEntry("");
	//	dumpEntry("");
		cui().scrollToBottomRequest();
	}
	
	public String getConsoleSaveDataPath(){
		File flPath = GlobalOSAppI.i().getBaseSaveDataPath();
		flConsDataPath = new File(flPath.getAbsolutePath()+File.separator+"CommandsConsoleSaveData"+File.separator);
		if(!flConsDataPath.exists())flConsDataPath.mkdirs();
		return flConsDataPath+File.separator;
	}
	
	private String fileNamePrepare(String strFileBaseName, String strFileType, boolean bAddDateTime){
		return getConsoleSaveDataPath()
			+strFileBaseName
				+(bAddDateTime?"-"+MiscI.i().getDateTimeForFilename():"")
				+"."+strFileType;
	}
	private String fileNamePrepareCfg(String strFileBaseName, boolean bAddDateTime){
		return fileNamePrepare(strFileBaseName, strFileTypeConfig, bAddDateTime);
	}
	private String fileNamePrepareLog(String strFileBaseName, boolean bAddDateTime){
		return fileNamePrepare(strFileBaseName, strFileTypeLog, bAddDateTime);
	}
	
	private ECmdReturnStatus precmdExpandRegexAsCommands(){
		String strCmdLine = ccl.getOriginalLine();
		
		if(isCommandString(strCmdLine)){
			String strCmd = extractCommandPart(strCmdLine,0);
			
			if(!MiscI.i().isValidIdentifierCmdVarAliasFuncString(strCmd)){
				String strCmdRegex=strCmd;
				boolean bCmdRegexMatched=false;
//				String strParams = ccl.getOriginalLine().substring(strCmd.length());
				ArrayList<String> astrCmdParams = convertToCmdAndParamsList(strCmdLine);
				astrCmdParams.remove(0); //keep only params
				String strParams=String.join(" ",astrCmdParams);
				ArrayList<String> astrAllCmds = getAllPossibleCommands();
				ArrayList<String> astrRequestedCmds = new ArrayList<String>();
				int iCount=0;
				for(String strCmdChk:astrAllCmds){
					if(strCmdChk.matches(strCmdRegex)){
						astrRequestedCmds.add(getCommandPrefixStr()+strCmdChk+" "+strParams);
//						addCmdToQueue(getCommandPrefixStr()+strCmdChk+" "+strParams);
						iCount++;
					}
				}
					
				if(iCount==astrAllCmds.size()){
					dumpWarnEntry("forbidden omni command regex "+strCmdRegex);
				}else{
					for(String str:astrRequestedCmds){
						addCmdToQueue(str);
						bCmdRegexMatched=true;
					}
				}
				
				if(bCmdRegexMatched)return ECmdReturnStatus.FoundAndWorked;
			}
		}
		
		return ECmdReturnStatus.NotFound;
	}
	
	private ECmdReturnStatus precmdRawLineCheckEndOfStartupCmdQueue() {
		if(!bStartupCmdQueueDone){
			String strCmd = getCommandPrefixStr()+RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE.getUniqueCmdId();
			if(strCmd.equalsIgnoreCase(ccl.getOriginalLine())){
//			if(RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE.equals(ccl.strCmdLineOriginal)){
				bStartupCmdQueueDone=true;
//				bFullyInitialized=true;
				return ECmdReturnStatus.FoundAndWorked;
			}
		}
		
		return ECmdReturnStatus.NotFound;
	}

	public void toggleLineCommentOrCommand() {
		String str = cui().getInputText();
		if(str.startsWith(""+getCommentPrefix())){
			str=str.substring(1);
		}else{
			str=getCommentPrefix()+str;
		}
		cui().setInputFieldText(str);
	}

	@Override
	synchronized public void handleExceptionThreaded(Exception e) {
		aExceptionList.add(e);
	}
	
	private void updateHandleException(){
		while(aExceptionList.size()>0){
			dumpExceptionEntry(aExceptionList.remove(0));
		}
	}
	
	public String convertNewLineToCmdDelimiter(String str){
		return str
			.replace(getCommandDelimiterStr()+"\n", getCommandDelimiterStr())
			.replace("\n", getCommandDelimiterStr());
	}
	
	public void updateCopyFrom(int iSelected, boolean bMultiLineMode){
		if(iSelected>=0){
			if(bMultiLineMode){
				if(iCopyTo==-1){
					iCopyFrom = iSelected;
					iCopyTo = iSelected;
				}else{
					updateCopyRangeCharIndicator(iCopyFrom,iCopyTo,false);
					iCopyFrom = iCopyTo;
					iCopyTo = iSelected;
					updateCopyRangeCharIndicator(iCopyFrom,iCopyTo,true);
				}
			}else{
				updateCopyRangeCharIndicator(iCopyFrom,iCopyTo,false);
				iCopyFrom = iSelected;
				iCopyTo = iSelected;
			}
		}
	}
	
	/**
	 * 
	 * @param iCpFrom
	 * @param iCpTo
	 * @param bApply if false will clear
	 */
	private void updateCopyRangeCharIndicator(int iCpFrom, int iCpTo, boolean bApply) {
	  if(iCpFrom>=0 && iCpTo>=0 && iCpFrom!=iCpTo){
	  	int iMin=Math.min(iCpFrom,iCpTo);
	  	int iMax=Math.max(iCpFrom,iCpTo);
	  	for(int i=iMin;i<=iMax;i++){
	  		if(bApply){
	  			cui().getDumpEntriesForManagement(ccSelf).set(i,strCopyRangeIndicator+cui().getDumpEntriesForManagement(ccSelf).get(i));
	  		}else{
	  			cui().getDumpEntriesForManagement(ccSelf).set(i,cui().getDumpEntriesForManagement(ccSelf).get(i)
		  			.replaceFirst("^["+strCopyRangeIndicator+"]",""));
	  		}
	  	}
	  }
	}

	public String editCopyOrCut(boolean bJustCollectText, boolean bCut, boolean bUseCommandDelimiterInsteadOfNewLine) {
	//	Integer iCopyTo = getDumpAreaSelectedIndex();
		String strTextToCopy = null;
		
		int iCopyToWork = iCopyTo;
		int iCopyFromWork = iCopyFrom;
		if(!bJustCollectText){
			updateCopyRangeCharIndicator(iCopyFrom, iCopyTo, false);
		}
		
	//	String strNL="\n";
	//	if(bUseCommandDelimiterInsteadOfNewLine){
	////		str=str.replace("\n", getCommandDelimiterStr());
	//		strNL=getCommandDelimiterStr();
	//	}
		
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
				if(iCopyFromWork>=cui().getDumpEntriesForManagement(ccSelf).size())break;
				
				String strEntry =	bCut ? cui().getDumpEntriesForManagement(ccSelf).remove(iCopyFromWork) :
					cui().getDumpEntriesForManagement(ccSelf).get(iCopyFromWork);
	//			strEntry=strEntry.replace("\\n","\n"); //translate in-between newline requests into newline
				
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
			
			if(bUseCommandDelimiterInsteadOfNewLine){
				strTextToCopy=convertNewLineToCmdDelimiter(strTextToCopy);
			}
			
			if(!bJustCollectText){
				MiscI.i().putStringToClipboard(strTextToCopy);
				
				cui().clearDumpAreaSelection();
//				lstbxDumpArea.getSelectionModel().setSelection(-1); //clear selection
			}
		}else{
			/**
			 * nothing selected at dump area,
			 * use text input field as source
			 */
			String str = cui().getInputText();
			if(!str.trim().equals(""+chCommandPrefix))MiscI.i().putStringToClipboard(str);
		}
		
		if(!bJustCollectText){
			iCopyFrom=-1;
			iCopyTo=-1;
		}
		
		return strTextToCopy;
	}
	
	public void setCmdHistoryCurrentIndex(int i){
		this.iCmdHistoryCurrentIndex=i;
	}
	public void addCmdHistoryCurrentIndex(int i){
		this.iCmdHistoryCurrentIndex+=i;
	}
	public int getCmdHistoryCurrentIndex(){
		return iCmdHistoryCurrentIndex;
	}
	public void resetCmdHistoryCursor(){
		iCmdHistoryCurrentIndex = astrCmdHistory.size();
	}
	
	/**
	 * This method is only to be called when user types command in the console.
	 * 
	 * @param strCmd
	 * @return
	 */
	public boolean actionSubmitDirectlyFromUserInput(final String strCmd){
		if(strCmd.isEmpty() || strCmd.trim().equals(""+getCommandPrefix())){
			cui().clearInputTextField(); 
			return false;
		}
		
		String strTypeCmd="Cmd";
		String strType=strTypeCmd;
		boolean bIsCmd=true;
		boolean bShowInfo=true;
		if(strCmd.trim().startsWith(""+getCommentPrefix())){
			strType="Cmt";
			bIsCmd=false;
		}else
		if(!strCmd.trim().startsWith(""+getCommandPrefix())){
			strType="Inv";
			bIsCmd=false;
		}
		
		if(bIsCmd){
			if(strCmd.trim().endsWith(""+getCommentPrefix())){
				bShowInfo=false;
			}
		}
		
//		String strTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+": ";
		if(bShowInfo){
			if(bIsCmd){
				dumpSubEntry("");
				String str="[USER COMMAND]";
				dumpEntry(Strings.padEnd(str, getCurrentFixedLineWrapAtColumn(), '_'));
			}
			dumpInfoEntry(strType+": "+strCmd);
		}
		
		cui().clearInputTextField(); 
		
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
			while(astrCmdHistory.size()>ilvMaxCmdHistSize.getLong()){
				astrCmdHistory.remove(0);
			}
		}
		
		resetCmdHistoryCursor();
		
		if(strType.equals(strTypeCmd)){
			strLastTypedUserCommand = strCmd;
			
			bConsoleCommandRunningFromDirectUserInput=true;
			ECmdReturnStatus ecrs = executeCommand(strCmd);
			bConsoleCommandRunningFromDirectUserInput=false;
			
			switch(ecrs){
				case FoundAndExceptionHappened:
				case FoundAndFailedGracefully:
					dumpWarnEntry(strType+": FAIL("+ecrs+"): "+strCmd, ecrs.getExtractingCustomReturnValue());
					showHelpForFailedCommand(strCmd);
					break;
				case NotFound:
				case Skip:
					dumpWarnEntry(strType+": ("+ecrs+"): "+strCmd, ecrs.getExtractingCustomReturnValue());
					break;
			}
			
			if(bAddEmptyLineAfterCommand ){
				dumpEntry("");
			}
		}
		
		cui().scrollToBottomRequest();
		
		return bIsCmd;
	}
	
	/**
	 * User commands, like thru console, may contain invalid values.
	 * Exceptions from it must never make the application exit...
	 */
	@Override
	public boolean isConsoleCommandRunningFromDirectUserInput(){
		return bConsoleCommandRunningFromDirectUserInput;
	}
	
	public int getCmdHistorySize() {
		return astrCmdHistory.size();
	}
	
	public String getCmdHistoryAtIndex(int i){
		if(getCmdHistorySize()==0)return null;
		if(i<0)return null;
		if(i>=getCmdHistorySize())return null;
		
		return astrCmdHistory.get(i);
	}

	public String getDevInfoEntryPrefix() {
		return strDevInfoEntryPrefix;
	}
	
	public int getCurrentFixedLineWrapAtColumn(){
		return ilvCurrentFixedLineWrapAtColumn.intValue();
	}
//	private void setConsoleUI(IConsoleUI icui) {
//		this.icui=icui;
//	}

	public void repeatLastUserTypedCommand() {
		dumpInfoEntry("Repeating: "+strLastTypedUserCommand);
		if(strLastTypedUserCommand==null)return;
		addCmdToQueue(strLastTypedUserCommand, true);
	}

	public ECmdReturnStatus cmdFoundReturnStatus(boolean bCommandWorked) {
		return bCommandWorked?ECmdReturnStatus.FoundAndWorked:ECmdReturnStatus.FoundAndFailedGracefully;
	}

	public boolean isConfigured() {
		if(!bConfigured)MsgI.i().debug("is cfg", false, this);
		return bConfigured;
	}

	public boolean isInitialized() {
		if(!bInitialized){
			MsgI.i().debug("is ini", false, this);
//			if(isConfigured()){
//				initialize();
//			}
		}
		return bInitialized;
	}

	public boolean isFullCmd(String strMainCmd) {
		return trmCmddList.containsKey(strMainCmd);
	}

//	public boolean isCoreCmd(String strMainCmd) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public String getFullCmdFromCore(String strMainCmd) {
//		return null;
//	}
	
	public String convertToFullCmdIfItIsCoreCmd(String strCmd, boolean bDumpMessages) {
		for(Entry<String, CommandData> entry:trmCmddList.entrySet()){
			CommandData cmdd = entry.getValue();
			if(cmdd.getSimpleCmdId().equalsIgnoreCase(strCmd)){
				ArrayList<CommandData> acmddConflictList = cmdd.getSimpleIdConflictListClone();
				if(acmddConflictList.size()>0){
					if(bDumpMessages){
						dumpWarnEntry("Unable to converto core command '"+cmdd.getSimpleCmdId()+"', it has conflicts:");
						dumpSubEntry(cmdd.asHelp());
						for(CommandData cmddC:acmddConflictList){
							dumpSubEntry(cmddC.asHelp());
						}
					}
					return strCmd;
				}
				
				String strConvertedTo=cmdd.getUniqueCmdId(); //entry.getKey()
				if(bDumpMessages && btgShowConvertedCommandInfo.b()){
					dumpInfoEntry("Converted '"+strCmd+"' to '"+strConvertedTo+"'");
				}
				return strConvertedTo;
			}
		}
		
		return strCmd;
	}
	
	/**
	 * If both cmds refere to the same command, and such has no conflicts, return it's data.
	 * 
	 * @param strCmdId1 core or unique cmd id
	 * @param strCmdId2 core or unique cmd id
	 * @return 
	 */
	public CommandData getCmdDataIfSame(String strCmdId1,String strCmdId2){
		CommandData cmdd1 = trmCmddList.get(strCmdId1);
		CommandData cmdd2 = trmCmddList.get(strCmdId2);
		
		if(cmdd1!=null){
			if(cmdd1.getSimpleCmdId().equalsIgnoreCase(strCmdId2)){
				if(!cmdd1.isSimpleCmdIdConflicting())return cmdd1;
			}
		}
		
		if(cmdd2!=null){
			if(cmdd2.getSimpleCmdId().equalsIgnoreCase(strCmdId1)){
				if(!cmdd2.isSimpleCmdIdConflicting())return cmdd2;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param strCmdId can be the unique or a simple without conflicts
	 * @param bAllowSimpleWithConflicts the 1st match will be returned (beware messing things up...)
	 * @return
	 */
	public CommandData getCmdDataFor(String strCmdId, boolean bAllowSimpleWithConflicts) {
		CommandData cmdd = trmCmddList.get(strCmdId);
		if(cmdd==null){
			/**
			 * look for simple id
			 */
			for(CommandData cmddTmp:trmCmddList.values()){
				if(!bAllowSimpleWithConflicts){
					if(cmddTmp.isSimpleCmdIdConflicting())continue; //only allow precise match
				}
				
				if(cmddTmp.getSimpleCmdId().equalsIgnoreCase(strCmdId)){
					cmdd=cmddTmp;
					break;
				}
			}
		}
		
		return cmdd;
	}


	public String getSubEntryPrefix() {
		return strSubEntryPrefix;
	}


	protected void setSubEntryPrefix(String strSubEntryPrefix) {
		this.strSubEntryPrefix = strSubEntryPrefix;
	}


	public boolean isAllowUserCmdOS() {
		return bAllowUserCmdOS;
	}


	protected void setAllowUserCmdOS(boolean bAllowUserCmdOS) {
		this.bAllowUserCmdOS = bAllowUserCmdOS;
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this, value);
	}

	@Override
	public boolean info(String str, Object... aobj) {
		dumpInfoEntry(str, aobj);
		return true;
	}

	@Override
	public boolean warn(String str, Object... aobj) {
		dumpWarnEntry(str, aobj);
		return true;
	}

	@Override
	public boolean debug(String str, Object... aobj) {
		dumpDebugEntry(str, aobj);
		return true;
	}

	@Override
	public boolean exception(String strMsgOverride, Throwable ex, Object... aobj) {
		dumpExceptionEntry(strMsgOverride, ex, aobj);
		return true;
	}

	@Override
	public boolean devInfo(String str, Object... aobj) {
		dumpDevInfoEntry(str, aobj);
		return true;
	}

	@Override
	public boolean devWarn(String str, Object... aobj) {
		dumpDevWarnEntry(str, aobj);
		return true;
	}

	@Override
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}
	
	RegisteredClasses<CommandsDelegator> rsc = new RegisteredClasses<CommandsDelegator>();
	public RegisteredClasses<CommandsDelegator> getRegisteredClasses() {
		return rsc;
	}

//	@Override
//	public boolean add(VarCmdFieldAbs objNew) {
//		throw new UnsupportedOperationException("method not implemented yet");
//	}
//
//	@Override
//	public ArrayList<VarCmdFieldAbs> getListCopy() {
//		throw new UnsupportedOperationException("method not implemented yet");
//	}

//	@Override
//	public boolean infoSystemTopOverride(String str) {
//		dumpEntry(false, true, false, true, "SystemInfo:"+str);
//		cui().infoSystemTopOverride(str);
//		return true;
//	}
	
//	public void executeUserBinds(boolean bRun, String strId){
//		for(KeyBoundVarField bind:tmbindList.values()){
//			if(bind.checkRunCallerAssigned(bRun, strId))break;
//		}
//	}
}
