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

import java.util.ArrayList;

import com.jme3.app.SimpleApplication;

import gui.console.ConsoleStateAbs.ConditionalNested;
import gui.console.ReflexFill.IReflexFillCfg;
import gui.console.ReflexFill.IReflexFillCfgVariant;
import gui.console.ReflexFill.ReflexFillCfg;

/**
 * All methods starting with "cmd" are directly accessible by user console commands.
 * Here are all base command related methods.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleCommands implements IReflexFillCfg{
	/**
	 * TODO temporary variable used only during methods migration
	 */
	public ConsoleStateAbs csaTmp = null;
	
	protected SimpleApplication	sapp;
	
	/**
	 * togglers
	 */
	public final String strTogglerCodePrefix="btg";
	protected BoolToggler	btgDbAutoBkp = new BoolToggler(this,false,strTogglerCodePrefix, "whenever a save happens, if the DB was modified, a backup will be created of the old file");
	protected BoolToggler	btgShowWarn = new BoolToggler(this,true,strTogglerCodePrefix);
	protected BoolToggler	btgShowInfo = new BoolToggler(this,true,strTogglerCodePrefix);
	protected BoolToggler	btgShowException = new BoolToggler(this,true,strTogglerCodePrefix);
	protected BoolToggler	btgEngineStatsView = new BoolToggler(this,false,strTogglerCodePrefix);
	protected BoolToggler	btgEngineStatsFps = new BoolToggler(this,false,strTogglerCodePrefix);
	protected BoolToggler	btgPreQueue = new BoolToggler(this,false,strTogglerCodePrefix);
	// developer vars, keep together!
	protected BoolToggler	btgShowDeveloperInfo=new BoolToggler(this,true,strTogglerCodePrefix);
	protected BoolToggler	btgShowDeveloperWarn=new BoolToggler(this,true,strTogglerCodePrefix);
	protected BoolToggler	btgShowExecQueuedInfo=new BoolToggler(this,false,strTogglerCodePrefix);
	protected BoolToggler	btgShowMiliseconds=new BoolToggler(this,false,strTogglerCodePrefix);
	protected BoolToggler	btgFpsLimit=new BoolToggler(this,false,strTogglerCodePrefix);
	protected BoolToggler	btgConsoleCpuRest=new BoolToggler(this,false,strTogglerCodePrefix,"Console update steps will be skipped if this is enabled.");
	
	/**
	 * used to hold a reference to the identified/typed user command
	 */
	protected BoolToggler	btgReferenceMatched;
	
	/**
	 * user can type these below at console (the actual commands are prepared by reflex)
	 */
	public final String strFinalCmdCodePrefix="CMD_";
	public final StringField CMD_CLOSE_CONSOLE = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_CONSOLE_HEIGHT = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_CONSOLE_SCROLL_BOTTOM = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_CONSOLE_STYLE = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_DB = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_ECHO = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FIX_CURSOR = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FIX_LINE_WRAP = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FIX_VISIBLE_ROWS_AMOUNT = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FUNCTION = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FUNCTION_CALL = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_FUNCTION_END = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_HELP = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_HISTORY = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_HK_TOGGLE = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_LINE_WRAP_AT = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_VAR_SET = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_SLEEP = new StringField(this,strFinalCmdCodePrefix);
	
	/**
	 * conditional user coding
	 */
	public final StringField CMD_IF = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_ELSE_IF = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_ELSE = new StringField(this,strFinalCmdCodePrefix);
	public final StringField CMD_IF_END = new StringField(this,strFinalCmdCodePrefix);
	
	/**
	 * this char indicates something that users (non developers) 
	 * should not have direct access.
	 */
	public final Character	RESTRICTED_TOKEN	= '&';
	public final String strFinalFieldRestrictedCmdCodePrefix="RESTRICTED_CMD_";
	public final StringField	RESTRICTED_CMD_SKIP_CURRENT_COMMAND	= new StringField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringField	RESTRICTED_CMD_END_OF_STARTUP_CMDQUEUE	= new StringField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringField	RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS	= new StringField(this,strFinalFieldRestrictedCmdCodePrefix);
	public final StringField	RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS	= new StringField(this,strFinalFieldRestrictedCmdCodePrefix);
	
	/**
	 * more tokens
	 */
	protected Character	chCommandDelimiter = ';';
	protected Character	chAliasPrefix = '$';
	protected Character	chVariableExpandPrefix = chAliasPrefix;
	protected Character	chFilterToken = '~';
	protected	Character chAliasBlockedToken = '-';
	protected Character	chAliasAllowedToken = '+';
	protected	Character chVarDeleteToken = '-';
	protected	Character	chCommentPrefix='#';
	protected	Character	chCommandPrefix='/';
	
	/**
	 * etc
	 */
	protected Boolean	bIfConditionExecCommands;
	protected ArrayList<ConditionalNested> aIfConditionNestedList = new ArrayList<ConditionalNested>();
	protected Boolean	bIfConditionIsValid;
	
	/**
	 * instance
	 */
	protected static ConsoleCommands instance;
	public static ConsoleCommands i(){return instance;}
	public ConsoleCommands(){
		instance=this;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = null;
		
		if(rfcv.getClass().isAssignableFrom(BoolToggler.class)){
			if(strTogglerCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCommandSuffix="Toggle";
			}
		}else
		if(rfcv.getClass().isAssignableFrom(StringField.class)){
			if(strFinalCmdCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
			}else
			if(strFinalFieldRestrictedCmdCodePrefix.equals(rfcv.getCodePrefixVariant())){
				rfcfg = new ReflexFillCfg();
				rfcfg.strCommandPrefix=""+RESTRICTED_TOKEN;
			}
		}
		
		return rfcfg;
	}
	
	public Character getCommandDelimiter() {
		return chCommandDelimiter;
	}
	public ConsoleCommands setCommandDelimiter(Character chCommandDelimiter) {
		this.chCommandDelimiter = chCommandDelimiter;
		return this;
	}
	public Character getAliasPrefix() {
		return chAliasPrefix;
	}
	public ConsoleCommands setAliasPrefix(Character chAliasPrefix) {
		this.chAliasPrefix = chAliasPrefix;
		return this;
	}
	public Character getVariableExpandPrefix() {
		return chVariableExpandPrefix;
	}
	public ConsoleCommands setVariableExpandPrefix(Character chVariableExpandPrefix) {
		this.chVariableExpandPrefix = chVariableExpandPrefix;
		return this;
	}
	public Character getFilterToken() {
		return chFilterToken;
	}
	public ConsoleCommands setFilterToken(Character chFilterToken) {
		this.chFilterToken = chFilterToken;
		return this;
	}
	public Character getAliasBlockedToken() {
		return chAliasBlockedToken;
	}
	public ConsoleCommands setAliasBlockedToken(Character chAliasBlockedToken) {
		this.chAliasBlockedToken = chAliasBlockedToken;
		return this;
	}
	public Character getAliasAllowedToken() {
		return chAliasAllowedToken;
	}
	public ConsoleCommands setAliasAllowedToken(Character chAliasAllowedToken) {
		this.chAliasAllowedToken = chAliasAllowedToken;
		return this;
	}
	public Character getVarDeleteToken() {
		return chVarDeleteToken;
	}
	public ConsoleCommands setVarDeleteToken(Character chVarDeleteToken) {
		this.chVarDeleteToken = chVarDeleteToken;
		return this;
	}
	public Character getCommentPrefix() {
		return chCommentPrefix;
	}
	public ConsoleCommands setCommentPrefix(Character chCommentPrefix) {
		this.chCommentPrefix = chCommentPrefix;
		return this;
	}
	public Character getCommandPrefix() {
		return chCommandPrefix;
	}
	public ConsoleCommands setCommandPrefix(Character chCommandPrefix) {
		this.chCommandPrefix = chCommandPrefix;
		return this;
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
	
	protected void cmdExit(){
		sapp.stop();
		System.exit(0);
	}
	
	protected boolean checkCmdValidityBoolTogglers(){
		btgReferenceMatched=null;
		for(BoolToggler btg : BoolToggler.getBoolTogglerListCopy()){
			if(checkCmdValidity(btg.getCmdId(), "[bEnable] "+btg.getHelp(), true)){
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
		return checkCmdValidity(strValidCmd, strComment, false);
	}
	protected boolean checkCmdValidity(String strValidCmd, String strComment, boolean bSkipSortCheck){
		if(csaTmp.strCmdLinePrepared==null){
			if(strComment!=null){
				strValidCmd+=commentToAppend(strComment);
			}
			
			csaTmp.addCmdToValidList(strValidCmd,bSkipSortCheck);
			
			return false;
		}
		
		if(RESTRICTED_CMD_SKIP_CURRENT_COMMAND.equals(csaTmp.strCmdLinePrepared))return false;
		if(csaTmp.isCommentedLine())return false;
		if(csaTmp.strCmdLinePrepared.trim().isEmpty())return false;
		
//		String strCheck = strPreparedCmdLine;
//		strCheck = strCheck.trim().split(" ")[0];
		strValidCmd = strValidCmd.trim().split(" ")[0];
		
//		return strCheck.equalsIgnoreCase(strValidCmd);
		return csaTmp.paramString(0).equalsIgnoreCase(strValidCmd);
	}
	
	protected boolean cmdEcho() {
		String strToEcho="";
		String strPart="";
		int iParam=1;
		while(strPart!=null){
			strToEcho+=strPart;
			strToEcho+=" ";
			strPart = csaTmp.paramString(iParam++);
		}
		strToEcho=strToEcho.trim();
		
		csaTmp.dumpEntry(strToEcho);
		
		return true;
	}

	protected boolean cmdIf() {
		return cmdIf(false);
	}
	protected boolean cmdIf(boolean bSkipNesting) {
		bIfConditionIsValid=false;
		
		String strCondition = csaTmp.paramString(1);
		
		boolean bNegate = false;
		if(strCondition.startsWith("!")){
			strCondition=strCondition.substring(1);
			bNegate=true;
		}
		
		Boolean bCondition = null;
		try{bCondition = Misc.i().parseBoolean(strCondition);}catch(NumberFormatException e){};//accepted exception
		
		if(bNegate)bCondition=!bCondition;
		
		if(bCondition==null){
			csaTmp.dumpWarnEntry("Invalid condition: "+strCondition);
			return false;
		}
		
		String strCmds = csaTmp.paramStringConcatenateAllFrom(2);
		if(strCmds==null)strCmds="";
		strCmds.trim();
		if(strCmds.isEmpty() || strCmds.startsWith(getCommentPrefixStr())){
			if(bSkipNesting){
				ConditionalNested cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
				cn.bCondition = bCondition;
			}else{
				aIfConditionNestedList.add(new ConditionalNested(bCondition));
			}
			
			bIfConditionExecCommands=bCondition;
		}else{
			if(!bSkipNesting){
				if(bCondition){
					csaTmp.addExecConsoleCommandToQueue(strCmds,true);
				}
			}
		}
		
		return true;
	}
	
	protected boolean cmdElse(){
//		bIfConditionExecCommands=!aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		ConditionalNested cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		bIfConditionExecCommands = !cn.bCondition;
		cn.bIfEndIsRequired = true;
		
		return true;
	}
	
	protected boolean cmdElseIf(){
		ConditionalNested cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		if(cn.bIfEndIsRequired){
			csaTmp.dumpExceptionEntry(new NullPointerException("command "+CMD_ELSE_IF.toString()
				+" is missplaced, ignoring"));
			bIfConditionExecCommands=false; //will also skip this block commands
			return false;
		}
		
		boolean bConditionSuccessAlready = cn.bCondition;
		
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
//				bIfEndIsRequired = false;
			}else{
				ConditionalNested cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
				bIfConditionExecCommands = cn.bCondition;
			}
		}else{
			csaTmp.dumpExceptionEntry(new NullPointerException("pointless condition ending..."));
			return false;
		}
		
		return true;
	}
}
