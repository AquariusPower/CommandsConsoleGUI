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

package com.github.commandsconsolegui.jme;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableWeak;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;

/**
 * Dispatches multi-click cmds when they are ready.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class MultiClickCondStateI extends ConditionalStateAbs {
	private static MultiClickCondStateI instance = new MultiClickCondStateI();
	public static MultiClickCondStateI i(){return instance;}
	
	public static class MultiClick{
		/**
		 * a null call will still increate the counter (think as a skipper to provide correct next index).
		 */
		int iRunCount = 0;
		
		Long lLastRequestMilis=null;
		
		/**
		 * can be nullified to "run nothing"
		 * ex.: 
		 *   1st click will execute callCmd1,
		 *   2nd click will be null (will call nothing),
		 *   3rd click will execute callCmd3
		 * So only 1st click and 3rd multi-click will be recognized.
		 */
		CallableWeak<Boolean> callCmd=null;
		
		int	iClickCount=0;

		ECallMode	eCallMode;
	}
	
	HashMap<Object,MultiClick> hmActivatorCmd = new HashMap<Object,MultiClick>();
	
	public static class CfgParm extends ConditionalStateAbs.CfgParm{
		public CfgParm() {
			super(null);
		}
	}
	
	@Override
	public ConditionalStateAbs configure(ICfgParm icfg) {
		return super.configure(icfg);
	}
	
	@Override
	protected boolean updateAttempt(float tpf) {
		if(!super.updateAttempt(tpf))return false;
		
		for(Object objActivator:hmActivatorCmd.keySet().toArray()){
			MultiClick mc = hmActivatorCmd.get(objActivator);
			
			/**
			 * If was not refreshed, it has timed out waiting for a new cmd change.
			 */
			boolean bTimedOut = !MouseCursorCentralI.i().isMultiClickDelayWithinLimitFrom(
				mc.lLastRequestMilis);
			
			boolean bRunNow = false;
			boolean bRemoveNow = false;
			CallableWeak<Boolean> callCmd = mc.callCmd;
			switch(mc.eCallMode){
				case OnceAfterDelay:
					bRemoveNow = bRunNow = bTimedOut;
					break;
				case EveryFrame:
					bRunNow = true;
					bRemoveNow = bTimedOut;
					break;
				case OncePromptly: // but only once
					if(mc.iRunCount==0)bRunNow = true;
					bRemoveNow = bTimedOut;
					break;
				case JustSkip: 
					bRunNow = true;
					callCmd = null;
					bRemoveNow = bTimedOut;
					break;
			}
			
			if(bRunNow){
//				try {
					if(callCmd==null || callCmd.call().booleanValue()){
						mc.iRunCount++;
					}
//				} catch (Exception e) {
//					PrerequisitesNotMetException npe = new PrerequisitesNotMetException("something went wrong...", this, objActivator, mc, callCmd);
//					npe.initCause(e);
//					throw npe;
//				}
			}
			
			if(bRemoveNow){
				hmActivatorCmd.remove(objActivator);
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param objActivator
	 * @return good for switch
	 */
	public int getActivatorNextUpdateIndex(Object objActivator){
		MultiClick mc = hmActivatorCmd.get(objActivator);
		if(mc==null)return 1;
		
		return mc.iClickCount+1; //useful to make it clear, to distinguish 1st, 2nd, 3rd clicks etc.
	}
	
	public static enum ECallMode{
		OnceAfterDelay,
		
		/**
		 * This is actually not that useful, but good in case to make everything execute from here instead of the code point using it.
		 * Useful also in case of changing the mode dynamically.
		 */
		OncePromptly,
		
		EveryFrame, 
		
		/**
		 * this will override a set cmd caller
		 */
		JustSkip, 
	}
	
	/**
	 * this can be used to repeat the previous command
	 * 
	 * @param objActivator
	 * @return
	 */
	public CallableWeak<Boolean> getActivatorCurrentCallCmd(Object objActivator){
		MultiClick mc = hmActivatorCmd.get(objActivator);
		if(mc==null)return null;
		return mc.callCmd;
	}
	
	/**
	 * 
	 * @param objActivator is the "think" that can be activated by the user direct action using mouse cursor, keyboard etc. Can be a button, a 3D entity etc...
	 * @param callCmd can be null to disable, return true means success, and will be removed from the list
	 * @param bCallPromptly
	 */
	public void updateActivator(ECallMode e, Object objActivator, CallableWeak<Boolean> callCmd){
		MultiClick mc = hmActivatorCmd.get(objActivator);
		if(mc==null){
			mc = new MultiClick();
			hmActivatorCmd.put(objActivator,mc);
		}
		mc.eCallMode = e;
		mc.lLastRequestMilis=System.currentTimeMillis();
		mc.callCmd = callCmd; //can be null here, acts like a skipper
		mc.iClickCount++;
	}

	@Override
	protected void enableFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void disableFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void enableSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void disableSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initFailed() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcvField);
	}

	@Override
	protected ConditionalStateAbs getThis() {
		return this;
	}
}
