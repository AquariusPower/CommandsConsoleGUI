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

package com.github.commandsconsolegui.spCmd.varfield;

import java.io.File;
import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.KeyBind;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI.CallableX;

/**
* This class is intended to be used only as class field variables.
* It automatically creates console variables.
* 
* TODO set limit min and max, optinally throw exception or just fix the value to not over/underflow
* 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
*
*/
public class FileVarField extends VarCmdFieldAbs<File,FileVarField>{
	private String	strFullUserCommand;
	
	public FileVarField(IReflexFillCfg rfcfgOwnerUseThis, File fl) {
		super(rfcfgOwnerUseThis, EVarCmdMode.Var, fl, File.class, true);
	//	this.strFullCommand=strFullCommand;
		constructed();
	}
	public FileVarField(IReflexFillCfg rfcfgOwnerUseThis) {
		this(rfcfgOwnerUseThis, (File)null);
	}
	public FileVarField(IReflexFillCfg rfcfgOwnerUseThis, String str) {
		this(rfcfgOwnerUseThis, new File(str));
	}
	
	/**
	 * @param bPreventCallerRunOnce (dummified, will be overriden by true)
	 */
	@Override
	public FileVarField setObjectRawValue(Object objValue,boolean bPreventCallerRunOnce) {
		if(objValue == null){
			//keep this empty skipper nullifier
		}else
		if(objValue instanceof File){
			//expected type
		}else
		if(objValue instanceof String){
			objValue = new File((String)objValue);
		}else
		{
			throw new PrerequisitesNotMetException("unsupported class type", objValue.getClass());
		}
		
		super.setObjectRawValue(objValue,true); //must NEVER execute just on bind change...
		
		return getThis();
	}
	
	@Override
	public String getVariablePrefix() {
		return "File";
	}
	
	@Override
	protected FileVarField getThis() {
		return this;
	}
	
	private final String strCodePrefixDefault="flf";
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}
	
//	@Override
//	protected String getFailSafeDebugValueReport(Object val) {
//		if(val==null)return ""+null;
//		
//		return ((File)val).getPath();
//	}
	
	@Override
	public String getValueAsString(int iFloatingPrecision) {
		return getValue().getPath();
	}
	
	@Override
	public ArrayList<IManager> getManagerList() {
		ArrayList<IManager> a = super.getManagerList();
		a.addAll(ManageVarCmdFieldI.i().getManagerListFor(KeyBoundVarField.class));
		return a;
	}
	
	@Override
	public void addManager(IManager imgr) {
		super.addManager(imgr);
		if(!ManageVarCmdFieldI.i().isHasVarManager(imgr)){
			ManageVarCmdFieldI.i().putVarManager(imgr, KeyBoundVarField.class);
		}
	}
	
	public File getFile(){
		return getValue();
	}
}
