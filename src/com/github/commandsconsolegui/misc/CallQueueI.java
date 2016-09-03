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

package com.github.commandsconsolegui.misc;

import java.util.ArrayList;
import java.util.concurrent.Callable;


/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CallQueueI {
	private static CallQueueI instance = new CallQueueI();
	public static CallQueueI i(){return instance;}
	
	public static interface CallableWeak<V> extends Callable<V>{
		/**
		 * without exception thrown, the debug will stop in the exact place where the exception
		 * happens inside a call (finally!!)
		 */
    @Override
		V call();
	}
	
	public static abstract class CallableX implements CallableWeak<Boolean>{
		boolean bPrepend;
		public CallableX setAsPrepend(){
			bPrepend=true;
			return this;
		}
	}
	
	ArrayList<CallableX> aCallList = new ArrayList<CallableX>();
	
	public int update(float fTPF){
		int i=0;
		
		for(CallableX caller:new ArrayList<CallableX>(aCallList)){
			if(runCallerCode(caller))i++;
		}
		
		return i;
	}
	
	private boolean runCallerCode(CallableX caller) {
//		try {
			if(caller.call().booleanValue()){
				aCallList.remove(caller);
				return true;
			}else{
				addCall(caller, false); //will retry
			}
//		} catch (Exception e) {
//			NullPointerException npe = new NullPointerException("callable exception");
//			npe.initCause(e);
//			throw npe;
//		}
		
		return false;
	}

//	/**
//	 * see {@link #addCall(Callable, boolean)}
//	 * 
//	 * @param caller
//	 */
//	public synchronized void appendCall(CallableX caller) {
//		addCall(caller, false);
//	}
	
	/**
	 * see {@link #addCall(Callable, boolean)}
	 * 
	 * @param caller
	 */
	public synchronized void addCall(CallableX caller) {
		addCall(caller,false);
	}
	
	/**
	 * if the caller returns false, it will be retried on the queue.
	 * 
	 * @param caller
	 * @param bPrepend
	 */
	public synchronized void addCall(CallableX caller, boolean bTryToRunNow) {
		if(caller==null)throw new PrerequisitesNotMetException("null caller");
		
//	if(aCallList.contains(caller))
		aCallList.remove(caller); //prevent duplicity
		
		if(bTryToRunNow){
			runCallerCode(caller);
		}else{
			if(caller.bPrepend){
				aCallList.add(0,caller);
			}else{
				aCallList.add(caller);
			}
		}
	}
}
