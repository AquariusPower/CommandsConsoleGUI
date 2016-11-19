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

import java.util.Arrays;

import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spCmd.varfield.VarCmdFieldAbs;

/**
 * can have no owner (field), it can be a simple console user variable.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 * @param <VAL> this constrainer is only useful in compilation time...
 */
public class ConsoleVariable<VAL> {
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
	private VAL objRawValue = null;
//	private IVarIdValueOwner owner;
	private VarCmdFieldAbs vcfRestrictedVarOwner;
	private IReflexFillCfg rfcfgClassHoldingTheOwner;
	private String	strHelp;
	private StackTraceElement[] asteDbgLastSetOriginAt;
	private Class clValueInitialType;
	private boolean	bRestrictedOwnerRequested;
	
//	public ConsoleVariable(String strUniqueVarId, VAL objValue,	VarCmdFieldAbs vcfOwner, IReflexFillCfg rfcfgClassHoldingTheOwner, String strHelp) {
	public ConsoleVariable(String strUniqueVarId, VAL objValue,	IReflexFillCfg rfcfgClassHoldingTheOwner, String strHelp) {
		super();
		this.bRestrictedOwnerRequested=CommandsHelperI.i().isRestricted(strUniqueVarId);
		this.strUniqueId = CommandsHelperI.i().removeRestrictedToken(strUniqueVarId);
		setRawValue(objValue);
//		this.objValue = objValue;
//		this.vcfOwnerForRestrictedVars = vcfOwner;
		this.rfcfgClassHoldingTheOwner = rfcfgClassHoldingTheOwner;
		if(strHelp!=null)this.strHelp = strHelp; //to avoid removing it
	}
	
	public ConsoleVariable setRestrictedVarOwner(VarCmdFieldAbs vcfOwner){
		if(vcfOwner.isConsoleVarLinkSet()){
			if(vcfOwner.isConsoleVarLink(this)){
				MsgI.i().devWarn("redundant varlink setup to self", this, vcfOwner);
			}else{
				throw new PrerequisitesNotMetException("owner already has another console var link set", this, vcfOwner);
			}
		}
		
		PrerequisitesNotMetException.assertNotAlreadySet("owner", this.vcfRestrictedVarOwner, vcfOwner, this);
		
		if(!bRestrictedOwnerRequested){
			MsgI.i().devWarn("restricted var owner not requested but is being set for "+getUniqueVarId(true), this, vcfOwner);
		}
		
		this.vcfRestrictedVarOwner=vcfOwner;
		
		return this;
	}
	
	public ConsoleVariable setValFixType(CommandsDelegator.CompositeControl cc, VAL objValue) {
		cc.assertSelfNotNull();
		
		if(getRestrictedVarOwner()!=null){
			throw new PrerequisitesNotMetException("can only fix type if owner is not set", this, getRestrictedVarOwner(), objValue);
		}
		
		clValueInitialType=null;
		
		setRawValue(objValue);
		
		return this;
	}
	
	private ConsoleVariable setRawValue(VAL objValue) {
		if(getRestrictedVarOwner()!=null){
			getRestrictedVarOwner().setObjectRawValue(objValue);
		}else{
			setRawValueDirectly(objValue);
		}
		
		return this;
	}
	
	/**
	 * If restricted var owner is set, this is also it's value.
	 * @param objValue
	 * @return
	 */
	public ConsoleVariable setRawValue(CommandsDelegator.CompositeControl cc, VAL objValue) {
		cc.assertSelfNotNull();
		setRawValue(objValue);
		return this;
	}
	public ConsoleVariable setRawValue(VarCmdFieldAbs.CompositeControl cc,VAL objValue) {
		cc.assertSelfNotNull();
		setRawValueDirectly(objValue);
		return this;
	}
	private ConsoleVariable setRawValueDirectly(VAL objValue) {
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

	public String getUniqueVarId(boolean bWithRestrictedTokenIfAppliable) {
		if(bWithRestrictedTokenIfAppliable && bRestrictedOwnerRequested){
			return CommandsHelperI.i().getRestrictedToken()+strUniqueId;
		}else{
			return strUniqueId;
		}
	}

	public VarCmdFieldAbs getRestrictedVarOwner() {
		return vcfRestrictedVarOwner;
	}
	
	/**
	 * TODO rename to getInstanceHoldingRestrictedOwner()
	 * @return
	 */
	public IReflexFillCfg getRfcfgClassHoldingTheOwner() {
		return rfcfgClassHoldingTheOwner;
	}

	public boolean isRestricted() {
		if(bRestrictedOwnerRequested && vcfRestrictedVarOwner==null){
			MsgI.i().devInfo("restricted var owner still not set for "+getUniqueVarId(true), this);
		}
		
		return bRestrictedOwnerRequested;
	}

	public boolean isRestrictedVarLinkConsistent() {
		return (getRestrictedVarOwner()!=null && getRestrictedVarOwner().isConsoleVarLink(this));
	}

	@Override
	public String toString() {
		return strUniqueId+"="+objRawValue;
	}
	
	
}
