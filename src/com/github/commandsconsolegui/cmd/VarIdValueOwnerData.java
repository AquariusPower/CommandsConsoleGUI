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

package com.github.commandsconsolegui.cmd;

import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class VarIdValueOwnerData {
//	public static interface IVarIdValueOwner{
//		public abstract void setObjectValue(Object objValue);
//		public abstract String getReport();
//		public abstract String getVarId();
//		public abstract Object getValueRaw();
//		public abstract void setConsoleVarLink(VarIdValueOwnerData vivo);
//		public abstract String getHelp();
//		public abstract String getSimpleCmdId();
//	}
	
	private String strId;
	private Object objValue;
//	private IVarIdValueOwner owner;
	private VarCmdFieldAbs<?> owner;
	private IReflexFillCfg rfcfgClassHoldingTheOwner;
	private String	strHelp;
	private StackTraceElement[] asteLastSetOriginDebug;
	
	public VarIdValueOwnerData(String strId, Object objValue,	VarCmdFieldAbs<?> vivoOwner, IReflexFillCfg rfcfgClassHoldingTheOwner, String strHelp) {
		super();
		this.strId = strId;
		this.objValue = objValue;
		this.owner = vivoOwner;
		this.rfcfgClassHoldingTheOwner = rfcfgClassHoldingTheOwner;
		if(strHelp!=null)this.strHelp = strHelp; //to avoid removing it
	}

	public void setObjectValue(Object objValue) {
		this.objValue=objValue;
		this.asteLastSetOriginDebug=Thread.currentThread().getStackTrace();
	}
	
	public Object getObjectValue(){
		return this.objValue;
	}
	
	public String getHelp(){
		return strHelp==null?"":strHelp;
	}

	public String getId() {
		return strId;
	}

	public VarCmdFieldAbs<?> getOwner() {
		return owner;
	}

	public IReflexFillCfg getRfcfgClassHoldingTheOwner() {
		return rfcfgClassHoldingTheOwner;
	}
	
}
