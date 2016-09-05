/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower>
	
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

package com.github.commandsconsolegui.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.misc.MiscI;
import com.google.common.collect.Lists;

/**
 *	This class holds all commands that allows users to create
 *	scripts that will run in the console.
 *	
 *	To prevent users using these scripting capabilities,
 *	extend from {@link #ConsoleCommands()} instead.
 *
 * @author Henrique Abdalla <https://github.com/AquariusPower>
 *
 */
public class ScriptingCommandsDelegator extends CommandsDelegator {
	public final StringCmdField CMD_FUNCTION = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_FUNCTION_CALL = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_FUNCTION_END = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField scfFunctionList = new StringCmdField(this);
	public final StringCmdField scfFunctionShow = new StringCmdField(this);
	
	/**
	 * conditional user coding
	 */
	public final StringCmdField CMD_IF = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_ELSE_IF = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_ELSE = new StringCmdField(this,strFinalCmdCodePrefix);
	public final StringCmdField CMD_IF_END = new StringCmdField(this,strFinalCmdCodePrefix);

	public String	strPrepareFunctionBlockForId;
	public boolean	bFuncCmdLineRunning;
	public boolean	bFuncCmdLineSkipTilEnd;
	
	private Boolean	bIfConditionIsValid;
	private Boolean	bIfConditionExecCommands;
	private ArrayList<ConditionalNestedData> aIfConditionNestedList = new ArrayList<ConditionalNestedData>();
	
	public TreeMap<String,ArrayList<String>> tmFunctions = 
		new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
	
//	public ConsoleScriptCommands(IConsoleUI icg) {
//		super(icg);
//	}
	public boolean checkFuncExecEnd() {
		if(ccl.getOriginalLine()==null)return false;
		return ccl.getOriginalLine().startsWith(RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS.toString());
	}
	public boolean checkFuncExecStart() {
		if(ccl.getOriginalLine()==null)return false;
		return ccl.getOriginalLine().startsWith(RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS.toString());
	}
	public boolean cmdFunctionCall() {
		String strFunctionId = ccl.paramString(1);
		
		/**
		 * put cmds block at queue
		 */
		ArrayList<String> astrFuncBlock = tmFunctions.get(strFunctionId);
		if(astrFuncBlock==null)return false;
		
		astrFuncBlock.removeAll(Collections.singleton(null));
		if(astrFuncBlock!=null && astrFuncBlock.size()>0){
			dumpInfoEntry("Running function: "+strFunctionId+" "+getCommentPrefix()+"totLines="+astrFuncBlock.size());
			
			/**
			 * params var ids
			 */
			ArrayList<String> astrFuncParams = new ArrayList<String>();
			int i=2;
			while(true){
				String strParamValue = ccl.paramString(i);
				if(strParamValue==null)break;
				String strParamId=strFunctionId+"_"+(i-1);
				astrFuncParams.add(strParamId);
				if(hasVar(strParamId)){
					dumpWarnEntry("RmConflictingVar: "+varReportPrepare(strParamId));
					varDelete(strParamId);
				}
				varSet(strParamId, strParamValue, false);
				i++;
			}
			
			ArrayList<String> astrFuncBlockToExec = new ArrayList<String>(astrFuncBlock);
			/**
			 * prepend section (is inverted order)
			 */
//			for(String strUnsetVar:astrFuncParams){
//				if(hasVar(strUnsetVar)){
//					/**
//					 * Tries to inform user about the inconsistency.
//					 */
//					dumpWarnEntry("Conflicting func param var will be removed: "+strUnsetVar);
//				}
//				astrFuncBlockToExec.add(0,getCommandPrefix()+
//						CMD_VAR_SET.toString()+" "+getVarDeleteToken()+strUnsetVar);
//			}
			astrFuncBlockToExec.add(0,RESTRICTED_CMD_FUNCTION_EXECUTION_STARTS+" "+strFunctionId);
			
			/**
			 * append section
			 */
			for(String strUnsetVar:astrFuncParams){
				astrFuncBlockToExec.add(getCommandPrefix()+
					CMD_VAR_SET.toString()+" "+getVarDeleteToken()+strUnsetVar);
			}
			astrFuncBlockToExec.add(RESTRICTED_CMD_FUNCTION_EXECUTION_ENDS+" "+strFunctionId);
			addCmdsBlockToPreQueue(astrFuncBlockToExec, true, true, "Func:"+strFunctionId);
		}
		
		return true;
	}
	public boolean cmdFunctionBegin() {
		String strFunctionId = ccl.paramString(1);
		
		if(!MiscI.i().isValidIdentifierCmdVarAliasFuncString(strFunctionId))return false;
		
		tmFunctions.put(strFunctionId, new ArrayList<String>());
		dumpInfoEntry("Function creation begins for: "+strFunctionId);
		
		strPrepareFunctionBlockForId=strFunctionId;
		
		return true;
	}
	public boolean functionFeed(String strCmdLine){
		ArrayList<String> astr = tmFunctions.get(strPrepareFunctionBlockForId);
		astr.add(strCmdLine);
		dumpDevInfoEntry("Function line added: "+strCmdLine+" "+getCommentPrefix()+"tot="+astr.size());
		return true;
	}
	public boolean functionEndCheck(String strCmdLine) {
		if(CMD_FUNCTION_END.equals(extractCommandPart(strCmdLine,0))){
			return cmdFunctionEnd();
		}
//		String strCmdCheck=""+getCommandPrefix()+CMD_FUNCTION_END.toString();
//		strCmdCheck=strCmdCheck.toLowerCase();
//		if(strCmdCheck.equals(strCmdLine.trim().toLowerCase())){
//			return cmdFunctionEnd();
//		}
		return false;
	}
	public boolean cmdFunctionEnd() {
		if(strPrepareFunctionBlockForId==null){
			dumpExceptionEntry(new NullPointerException("no function being prepared..."));
			return false;
		}
		
		dumpInfoEntry("Function creation ends for: "+strPrepareFunctionBlockForId);
		strPrepareFunctionBlockForId=null;
		
		return true;
	}
	
	@Override
	public ECmdReturnStatus execCmdFromConsoleRequestRoot(){
		boolean bCommandWorked = false;
		
		ECmdReturnStatus ecrs = super.execCmdFromConsoleRequestRoot();
		if(ecrs.compareTo(ECmdReturnStatus.NotFound)!=0)return ecrs;
		
		if(!bCommandWorked){
			if(checkCmdValidity(getPseudoListener(),CMD_FUNCTION,"<id> begins a function block")){
				bCommandWorked=cmdFunctionBegin();
			}else
			if(checkCmdValidity(getPseudoListener(),CMD_FUNCTION_CALL,"<id> [parameters...] retrieve parameters values with ex.: ${id_1} ${id_2} ...")){
				bCommandWorked=cmdFunctionCall();
			}else
			if(checkCmdValidity(getPseudoListener(),CMD_FUNCTION_END,"ends a function block")){
				bCommandWorked=cmdFunctionEnd();
			}else
			if(checkCmdValidity(getPseudoListener(),scfFunctionList,"[filter]")){
				String strFilter = ccl.paramString(1);
				ArrayList<String> astr = Lists.newArrayList(tmFunctions.keySet().iterator());
				for(String str:astr){
					if(strFilter!=null && !str.toLowerCase().contains(strFilter.toLowerCase()))continue;
					dumpSubEntry(str);
				}
				bCommandWorked=true;
			}else
			if(checkCmdValidity(getPseudoListener(),scfFunctionShow,"<functionId>")){
				String strFuncId = ccl.paramString(1);
				if(strFuncId!=null){
					ArrayList<String> astr = tmFunctions.get(strFuncId);
					if(astr!=null){
						dumpSubEntry(getCommandPrefixStr()+CMD_FUNCTION+" "+strFuncId+getCommandDelimiter());
						for(String str:astr){
							str=getSubEntryPrefix()+getSubEntryPrefix()+str+getCommandDelimiter();
//								dumpSubEntry("\t"+str+getCommandDelimiter());
							dumpEntry(false, true, false, false, str);
						}
						dumpSubEntry(getCommandPrefixStr()+CMD_FUNCTION_END+getCommandDelimiter());
						bCommandWorked=true;
					}
				}
			}else
			if(checkCmdValidity(getPseudoListener(),CMD_ELSE,"conditinal block")){
				bCommandWorked=cmdElse();
			}else
			if(checkCmdValidity(getPseudoListener(),CMD_ELSE_IF,"<[!]<true|false>> conditional block")){
				bCommandWorked=cmdElseIf();
			}else
			if(checkCmdValidity(getPseudoListener(),CMD_IF,"<[!]<true|false>> [cmd|alias] if cmd|alias is not present, this will be a multiline block start!")){
				bCommandWorked=cmdIf();
			}else
			if(checkCmdValidity(getPseudoListener(),CMD_IF_END,"ends conditional block")){
				bCommandWorked=cmdIfEnd();
			}else
			{
				return ECmdReturnStatus.NotFound;
			}
		}
		
		return cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public ECmdReturnStatus stillExecutingCommand() {
		Boolean bCmdFoundAndWorked=null; //null is not found, true or false is found but worked or failed
		
		if(bCmdFoundAndWorked==null){
			if(bFuncCmdLineRunning){
				if(checkFuncExecEnd()){
					bFuncCmdLineRunning=false;
					bFuncCmdLineSkipTilEnd=false;
					bCmdFoundAndWorked=true;
				}
			}
		}
		
		if(bCmdFoundAndWorked==null){
			if(checkFuncExecStart()){
				bFuncCmdLineRunning=true;
				bCmdFoundAndWorked=true;
			}else
			if(strPrepareFunctionBlockForId!=null){
				if(bCmdFoundAndWorked==null)bCmdFoundAndWorked = functionEndCheck(ccl.getOriginalLine()); //before feed
				if(bCmdFoundAndWorked==null)bCmdFoundAndWorked = functionFeed(ccl.getOriginalLine());
			}else
			if(bIfConditionExecCommands!=null && !bIfConditionExecCommands){
				/**
				 * These are capable of stopping the skipping.
				 */
				if(CMD_ELSE_IF.equals(ccl.paramString(0))){
					if(bCmdFoundAndWorked==null)bCmdFoundAndWorked = cmdElseIf();
				}else
				if(CMD_ELSE.equals(ccl.paramString(0))){
					if(bCmdFoundAndWorked==null)bCmdFoundAndWorked = cmdElse();
				}else
				if(CMD_IF_END.equals(ccl.paramString(0))){
					if(bCmdFoundAndWorked==null)bCmdFoundAndWorked = cmdIfEnd();
				}else{
					/**
					 * The if condition resulted in false, therefore commands must be skipped.
					 */
					dumpInfoEntry("ConditionalSkip: "+ccl.getCmdLinePrepared());
					if(bCmdFoundAndWorked==null)bCmdFoundAndWorked = true;
				}
			}
		}
		
		if(bCmdFoundAndWorked==null){
			if(bFuncCmdLineRunning && bFuncCmdLineSkipTilEnd){
				dumpWarnEntry("SkippingRemainingFunctionCmds: "+ccl.getCmdLinePrepared());
				bCmdFoundAndWorked = true; //this just means that the skip worked
			}
		}
		
		if(bCmdFoundAndWorked==null){
			/**
			 * normal commands execution
			 */
			ECmdReturnStatus ecrs = super.stillExecutingCommand();
			if(ecrs.compareTo(ECmdReturnStatus.NotFound)!=0)return ecrs;
		}
		
		if(bCmdFoundAndWorked==null){
			if(bFuncCmdLineRunning){
				// a command may fail inside a function, only that first one will generate error message 
				bFuncCmdLineSkipTilEnd=true;
			}
		}
		
		if(bCmdFoundAndWorked==null)return ECmdReturnStatus.NotFound;
		return cmdFoundReturnStatus(bCmdFoundAndWorked);
	}
	
	@Override
	public String prepareStatsFieldText() {
		String strStatsLast = super.prepareStatsFieldText();
		
		if(EStats.FunctionCreation.isShow() && strPrepareFunctionBlockForId!=null){
			strStatsLast+=
					"F="+strPrepareFunctionBlockForId
						+";";
		}
		
		if(EStats.IfConditionalBlock.isShow() && aIfConditionNestedList.size()>0){
			strStatsLast+=
					"If"+aIfConditionNestedList.size()
						+";";
		}
		
		return strStatsLast;
	}

	public boolean cmdIf() {
		return cmdIf(false);
	}
	public boolean cmdIf(boolean bSkipNesting) {
		bIfConditionIsValid=false;
		
		String strCondition = ccl.paramString(1);
		
		boolean bNegate = false;
		if(strCondition.startsWith("!")){
			strCondition=strCondition.substring(1);
			bNegate=true;
		}
		
		Boolean bCondition = null;
		try{bCondition = MiscI.i().parseBoolean(strCondition);}catch(NumberFormatException e){};//accepted exception
		
		if(bNegate)bCondition=!bCondition;
		
		if(bCondition==null){
			dumpWarnEntry("Invalid condition: "+strCondition);
			return false;
		}
		
		String strCmds = ccl.paramStringConcatenateAllFrom(2);
		if(strCmds==null)strCmds="";
		strCmds.trim();
		if(strCmds.isEmpty() || strCmds.startsWith(getCommentPrefixStr())){
			if(bSkipNesting){
				ConditionalNestedData cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
				cn.bCondition = bCondition;
			}else{
				aIfConditionNestedList.add(new ConditionalNestedData(bCondition));
			}
			
			bIfConditionExecCommands=bCondition;
		}else{
			if(!bSkipNesting){
				if(bCondition){
					addCmdToQueue(strCmds,true);
				}
			}
		}
		
		return true;
	}
	
	public boolean cmdElse(){
//		bIfConditionExecCommands=!aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		ConditionalNestedData cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		bIfConditionExecCommands = !cn.bCondition;
		cn.bIfEndIsRequired = true;
		
		return true;
	}
	
	public boolean cmdElseIf(){
		ConditionalNestedData cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
		if(cn.bIfEndIsRequired){
			dumpExceptionEntry(new NullPointerException("command "+CMD_ELSE_IF.toString()
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
	
	public boolean cmdIfEnd(){
		if(aIfConditionNestedList.size()>0){
			aIfConditionNestedList.remove(aIfConditionNestedList.size()-1);
			
			if(aIfConditionNestedList.size()==0){
				bIfConditionExecCommands=null;
//				bIfEndIsRequired = false;
			}else{
				ConditionalNestedData cn = aIfConditionNestedList.get(aIfConditionNestedList.size()-1);
				bIfConditionExecCommands = cn.bCondition;
			}
		}else{
			dumpExceptionEntry(new NullPointerException("pointless condition ending..."));
			return false;
		}
		
		return true;
	}
	
}
