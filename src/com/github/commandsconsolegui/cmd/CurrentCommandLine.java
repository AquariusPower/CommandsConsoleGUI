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

import com.github.commandsconsolegui.cmd.CommandsDelegator.CompositeControl;
import com.github.commandsconsolegui.misc.CompositeControlAbs;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower>
 *
 */
public class CurrentCommandLine {
	public static final class CompositeControl extends CompositeControlAbs<CurrentCommandLine>{
		private CompositeControl(CurrentCommandLine casm){super(casm);};
	};
	private CompositeControl ccSelf = new CompositeControl(this);
	
	private String	strCmdLineOriginal;
	
	private ArrayList<String>	astrCmdAndParams = new ArrayList<String>();
	private String strCmdLinePrepared = null;
	
	private String strMainCommand = null;

	private CommandsDelegator	cd;
	
	public CurrentCommandLine(CommandsDelegator cd) {
		this.cd=cd;
	}
	
	public String getOriginalLine(){
		return strCmdLineOriginal;
	}
	
	public ArrayList<String> getPreparedCmdAndParamsListCopy(){
		return new ArrayList<String>(astrCmdAndParams);
	}
	public ArrayList<String> getPreparedCmdAndParamsListCopy(int iFromIndexIncl){
		return getPreparedCmdAndParamsListCopy(iFromIndexIncl, astrCmdAndParams.size());
	}
	public ArrayList<String> getPreparedCmdAndParamsListCopy(int iFromIndexIncl, int iToIndexExcl){
		return new ArrayList<String>(astrCmdAndParams.subList(iFromIndexIncl, iToIndexExcl));
	}
	
	public void updateFrom(String strFullCmdLine) {
		clear();
		astrCmdAndParams.addAll(cd.convertToCmdAndParamsList(strFullCmdLine));
		setCmdLinePrepared(String.join(" ", astrCmdAndParams));
	}
	
	public void setOriginalLine(String strFullCmdLineOriginal) {
		this.strCmdLineOriginal=strFullCmdLineOriginal;
	}

	public void clear(){
		setCmdLinePrepared(null);
		astrCmdAndParams.clear();
	}

	public String paramCommand(){
		return paramCommand(false);
	}
	/**
	 * 
	 * @return the first "word" in the command line, is the command
	 */
	public String paramCommand(boolean bDumpMessages){
		String strMainCmd = paramString(0);
		
		/**
		 * convert to full command if possible
		 */
		if(strMainCmd!=null && !cd.isFullCmd(strMainCmd)){
			strMainCmd = cd.convertToFullCmdIfItIsCoreCmd(strMainCmd,bDumpMessages);
//			if(cd.isCoreCmd(strMainCmd)){
//				strMainCmd = cd.getFullCmdFromCore(strMainCmd);
//			}
		}
		
		return strMainCmd;
	}
	
	/**
	 * 
	 * @param iIndex 0 is the command, >=1 are parameters
	 * @return
	 */
	public String paramString(int iIndex){
		if(iIndex<astrCmdAndParams.size()){
			String str=astrCmdAndParams.get(iIndex);
			str = cd.applyVariablesValues(ccSelf,str);
			return str;
		}
		return null;
	}
	public String paramStringConcatenateAllFrom(int iStartIndex){
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
			str = cd.applyVariablesValues(ccSelf,str);
		}
		
		return str;
	}

	public Boolean paramBoolean(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return null;
		/**
		 *	Not using java default method because it is permissive towards "false", ex.:
		 *	if user type "tre" instead of "true", it will result in `false`.
		 *	But false may be an undesired option.
		 *	Instead, user will be warned of the wrong typed value "tre".
		 *	KEEP THIS COMMENTED CODE AS A WARNING!
		return Boolean.parseBoolean(str);
		 */
		if(str.equals("0"))return false;
		if(str.equals("1"))return true;
		if(str.equalsIgnoreCase("false"))return false;
		if(str.equalsIgnoreCase("true"))return true;
		
		cd.dumpExceptionEntry(new NumberFormatException("invalid string to boolean: "+str));
		
		return null;
	}
	public Integer paramInt(int iIndex){
		return paramInt(iIndex,false);
	}
	public Integer paramInt(int iIndex, boolean bNullOnParseFail){
		String str = paramString(iIndex);
		if(str==null)return null;
		try{return Integer.parseInt(str);}catch(NumberFormatException ex){
			if(!bNullOnParseFail)throw ex;
		};
		return null;
	}
	public Float paramFloat(int iIndex){
		return paramFloat(iIndex,false);
	}
	public Float paramFloat(int iIndex, boolean bNullOnParseFail){
		String str = paramString(iIndex);
		if(str==null)return null;
		try{return Float.parseFloat(str);}catch(NumberFormatException ex){
			if(!bNullOnParseFail)throw ex;
		};
		return null;
	}

	public Boolean paramBooleanCheckForToggle(int iIndex){
		String str = paramString(iIndex);
		if(str==null)return true; //if there was no param, will work like toggle
		
		Boolean b = paramBoolean(iIndex);
		if(b==null)return false; //if there was a param but it is invalid, will prevent toggle
		
		return true; // if reach here, will not be toggle, will be a set override
	}

	public boolean isCommentedLine(){
		if(getCmdLinePrepared()==null)return false;
		return getCmdLinePrepared().trim().startsWith(""+cd.getCommentPrefix());
	}

	public String getCmdLinePrepared() {
		return strCmdLinePrepared;
	}

	public void setCmdLinePrepared(String strCmdLinePrepared) {
		this.strCmdLinePrepared = strCmdLinePrepared;
	}
}
