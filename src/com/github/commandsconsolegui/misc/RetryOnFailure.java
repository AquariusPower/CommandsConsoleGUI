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

package com.github.commandsconsolegui.misc;

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class RetryOnFailure implements IReflexFillCfg{
	public static final class CompositeControl extends CompositeControlAbs<RetryOnFailure>{
		private CompositeControl(RetryOnFailure casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	public static interface IRetryListOwner{
		ArrayList<RetryOnFailure> getRetryListForManagement(RetryOnFailure.CompositeControl ccSelf);
		//quite unsafe:	ArrayList<RetryOnFailure> arList = new ArrayList<RetryOnFailure>();
		
		/**
		 * this is the last time the owner was updated, to know if it's retry is allowed,
		 * at {@link RetryOnFailure}
		 * @return
		 */
		long getCurrentTimeMilis();
		
		/**
		 * to compose field's Id's
		 * at {@link RetryOnFailure}
		 * @return
		 */
		String getId();
	}
	
	private long lStartMilis=0; // as this is only in case of failure, the 1st attempt will always be ready!
	
	/** 0 means retry at every update*/
	private IntLongVarField ilvDelayMilis = null;
//	long lDelayMilis=0;

	private String	strId;

	private IRetryListOwner	irlo;
	
	public RetryOnFailure(IRetryListOwner irlo, String strId){
		this.irlo=irlo;
		this.strId=strId;
		
		ilvDelayMilis = new IntLongVarField(
			/**
			 * If the owner is an anonymous class,
			 * null here means to ignore this field.
			 * So it basically prevents the creation of console variables.
			 */
			MiscI.i().isAnonymousClass(irlo) ? null : this, 
			0L, "retry delay between failed state mode attempts");
		
		if(findRetryModeById(irlo.getRetryListForManagement(ccSelf), strId)!=null)throw new PrerequisitesNotMetException("conflicting retry id", strId); 
		irlo.getRetryListForManagement(ccSelf).add(this);
	}
	
	public static RetryOnFailure findRetryModeById(ArrayList<RetryOnFailure> arList, String strId){
	//	for(RetryOnFailure r:irlo.getRetryList(ccSelf)){
		for(RetryOnFailure r:arList){
			if(r.isId(strId))return r;
		}
		return null;
	}
	
	public boolean isId(String strId){
		return (this.strId.equals(strId));
	}
	
	public void setRetryDelay(long lMilis){
		this.ilvDelayMilis.setObjectRawValue(lMilis);
	}
	
	/**
	 * Will be ready if the delay time from last failure has been reached.
	 * Will also be ready if there was no previous failure.
	 * 
	 * @return
	 */
	public boolean isReady(){
		return irlo.getCurrentTimeMilis() >= (lStartMilis+ilvDelayMilis.getLong());
	}
	public boolean isReadyAndUpdate(){
		if(isReady()){
			updateStartTime();
			return true;
		}
		return false;
	}

	public void updateStartTime() {
		lStartMilis=irlo.getCurrentTimeMilis();
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		ReflexFillCfg rfcfg = GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
		if(rfcfg==null)rfcfg=new ReflexFillCfg(rfcv);
//		rfcfg.setPrefixCustomId(cond.getId());
		rfcfg.setPrefixCustomId(irlo.getId()+ReflexFillI.i().getCommandPartSeparator()+this.strId);
		return rfcfg;
	}

	public String getId() {
		return strId;
	}
	
}
