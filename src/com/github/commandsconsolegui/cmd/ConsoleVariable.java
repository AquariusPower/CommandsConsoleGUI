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

package com.github.commandsconsolegui.cmd;

import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * can have no owner (field), it can be a simple console user variable.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ConsoleVariable {
//	public static interface IVarIdValueOwner{
//		public abstract void setObjectValue(Object objValue);
//		public abstract String getReport();
//		public abstract String getVarId();
//		public abstract Object getValueRaw();
//		public abstract void setConsoleVarLink(VarIdValueOwnerData vivo);
//		public abstract String getHelp();
//		public abstract String getSimpleCmdId();
//	}
	
	private String strUniqueId;
	private Object objRawValue = null;
//	private IVarIdValueOwner owner;
	private VarCmdFieldAbs vcfOwnerForRestrictedVars;
	private IReflexFillCfg rfcfgClassHoldingTheOwner;
	private String	strHelp;
	private StackTraceElement[] asteDbgLastSetOriginAt;
	private Class clValueInitialType;
	
	public ConsoleVariable(String strUniqueVarId, Object objValue,	VarCmdFieldAbs vcfOwner, IReflexFillCfg rfcfgClassHoldingTheOwner, String strHelp) {
		super();
		this.strUniqueId = strUniqueVarId;
		setRawValue(objValue);
//		this.objValue = objValue;
		this.vcfOwnerForRestrictedVars = vcfOwner;
		this.rfcfgClassHoldingTheOwner = rfcfgClassHoldingTheOwner;
		if(strHelp!=null)this.strHelp = strHelp; //to avoid removing it
	}
	
	public ConsoleVariable setOwnerForRestrictedVar(VarCmdFieldAbs vcfOwner){
		if(vcfOwner.isConsoleVarLinkSet())throw new PrerequisitesNotMetException("owner already has a console var link", this, vcfOwner);
		PrerequisitesNotMetException.assertNotAlreadySet("owner", this.vcfOwnerForRestrictedVars, vcfOwner, this);
		this.vcfOwnerForRestrictedVars=vcfOwner;
		return this;
	}
	
	public ConsoleVariable setValFixType(CommandsDelegator.CompositeControl cc, Object objValue) {
		cc.assertSelfNotNull();
		clValueInitialType=null;
		setRawValue(objValue);
		return this;
	}
	
	public ConsoleVariable setRawValue(Object objValue) {
		if(objValue!=null && clValueInitialType!=null){
//			if(this.objValue!=null && this.objValue.getClass()!=objValue.getClass()){
			if(objValue.getClass()!=clValueInitialType){
				throw new PrerequisitesNotMetException("type change is forbidden", this, clValueInitialType, objValue.getClass(), this.objRawValue, objValue);
			}
		}
		
		this.objRawValue=objValue;
		
		if(this.objRawValue!=null){
			if(clValueInitialType==null){
				clValueInitialType=this.objRawValue.getClass();
			}
		}
		
		this.asteDbgLastSetOriginAt=Thread.currentThread().getStackTrace();
		
		return this;
	}
	
	public Object getRawValue(){
		return this.objRawValue;
	}
	
	public String getHelp(){
		return strHelp==null?"":strHelp;
	}

	public String getUniqueVarId() {
		return strUniqueId;
	}

	public VarCmdFieldAbs getRestrictedOwner() {
		return vcfOwnerForRestrictedVars;
	}
	
	/**
	 * TODO rename to getInstanceHoldingRestrictedOwner()
	 * @return
	 */
	public IReflexFillCfg getRfcfgClassHoldingTheOwner() {
		return rfcfgClassHoldingTheOwner;
	}

}
