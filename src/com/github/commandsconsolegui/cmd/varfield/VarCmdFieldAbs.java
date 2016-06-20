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

package com.github.commandsconsolegui.cmd.varfield;

import com.github.commandsconsolegui.cmd.CommandData;
import com.github.commandsconsolegui.cmd.VarIdValueOwnerData;
import com.github.commandsconsolegui.cmd.VarIdValueOwnerData.IVarIdValueOwner;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.IdTmp;

/**
 * TODO migrate most things possible to here
 *
 * @author AquariusPower <https://github.com/AquariusPower>
 * 
 */
public abstract class VarCmdFieldAbs implements IReflexFillCfgVariant, IVarIdValueOwner{
	protected boolean bReflexingIdentifier = true;
	protected String strVarId = null;
	protected String strCmdId = null;
	protected String strCoreId = null;
	protected String strDebugErrorHelper = "ERROR: "+this.getClass().getName()+" not yet properly initialized!!!";
	protected IReflexFillCfg	rfcfgOwner;
	protected VarIdValueOwnerData	vivo;
	protected String strHelp="";
	protected CommandData	cmdd;
	
	public CommandData getCmdData(){
		return this.cmdd;
	}
	
	public void setCmdData(CommandData cmdd){
		this.cmdd=cmdd;
	}
	
	public void setVarData(VarIdValueOwnerData	vivo){
		this.vivo=vivo;
	}
	public VarIdValueOwnerData getVarData(){
		return this.vivo;
	}
	
	@Override
	public boolean isReflexing() {
		return bReflexingIdentifier;
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends VarCmdFieldAbs> T setId(IdTmp id){
		String strExceptionId = null;
		
		/**
		 * must be an exception as it can have already been read/collected with automatic value.
		 */
		if(id.bIsVariable){
			if(strVarId!=null){
				strExceptionId=strVarId;
			}else{
				strVarId=id.strFull;
			}
		}else{
			if(strCmdId!=null){
				strExceptionId=strCmdId;
			}else{
				strCmdId=id.strFull;
			}
		}
		
		if(strExceptionId!=null){
			throw new NullPointerException("asked for '"+id.strFull+"' but was already set to: "+strExceptionId);
		}
		
		strCoreId = id.strCore;
		
		strDebugErrorHelper=null; //clear error helper
		
		return (T)this;
	}
	
	/**
	 * sets the command identifier that user will type in the console
	 * 
	 * @param strId
	 * @param bIsVariable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends VarCmdFieldAbs> T setCustomId(String strId, boolean bIsVariable){
		setId(new IdTmp(bIsVariable,strId,strId));
		return (T)this;
	}

	@Override
	public String getCoreId() {
		if(strCmdId==null)getCmdId();//just to init
		return strCoreId;
	}

	public String getCmdId(){
		if(strCmdId==null){
			setId(ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwner,this,false));
		}
		
		return strCmdId;
	}
	
	@Override
	public String getHelp(){
		return strHelp;
	}
}
