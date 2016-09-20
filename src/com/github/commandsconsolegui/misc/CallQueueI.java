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
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.RetryOnFailure.IRetryListOwner;


/**
 * 
 * Useful for:
 * - lazy/conditoinal initializations
 * - conditional tasks
 * - to lower the complexity avoiding creating unnecesary methods
 * - what more?
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class CallQueueI {
	private static CallQueueI instance = new CallQueueI();
	public static CallQueueI i(){return instance;}
	
	public static interface CallableWeak<V> extends Callable<V>{
		/**
		 * without exception thrown, the debug will stop in the exact place where the exception
		 * happens inside a call (finally!!)
		 * 
		 * @return true if succeeded and the queue can be discarded, false if failed and it must be retried.
		 */
    @Override
		V call();
	}
	
	public static abstract class CallableX implements CallableWeak<Boolean>,IRetryListOwner{
		private boolean bPrepend;
		private HashMap<String,Object> hmCustom;
//		private int	iDelayBetweenRetriesMilis = 0;
		private RetryOnFailure rReQueue;
		private ArrayList<RetryOnFailure>	arList;
		private String	strId;
		private long	lLastUpdateMilis;
		private String strDbgGenericSuperClass;
		private StackTraceElement[] asteDbgInstancedAt;
		private StackTraceElement[] asteDbgLastQueuedAt;
		private long iFailCount=0;
		private Object	objEnclosing;
//		private Object[]	aobjParams;
		private boolean	bRetryOnFail = true;
		private boolean bQuietOnFail = false;
		private boolean bAllowQueue = true;
		private int	iFailTimesWarn;
		
		public CallableX(Object objEnclosing) {
			this(objEnclosing,0);
		}
		public CallableX(Object objEnclosing, int iRetryDelayMilis) {
			super();
			
			// this is a guess check...
			if(objEnclosing instanceof Integer)throw new PrerequisitesNotMetException("use 'this' on enclosing", objEnclosing, iRetryDelayMilis, this);
				
			this.objEnclosing=objEnclosing;
			
			strDbgGenericSuperClass = this.getClass().getGenericSuperclass().getTypeName();
			asteDbgInstancedAt = Thread.currentThread().getStackTrace();
			
			hmCustom = new HashMap<String, Object>();
			
			// keep together in this order:
			arList = new ArrayList<RetryOnFailure>(); //MUST BE BEFORE {@link RetryOnFailure} instance below!!! 
			rReQueue = new RetryOnFailure(this, "ReQueue");
			
			rReQueue.setRetryDelay(iRetryDelayMilis);
//			this.iDelayBetweenRetriesMilis=i;
		}
		
		/**
		 * can only be run now
		 * @return
		 */
		public CallableX setQueueDenied() {
			this.bAllowQueue = false;
			return this;
		}
		
		public boolean isAllowQueue() {
			return bAllowQueue;
		}
		
		public CallableX setQuietOnFail(boolean bQuietOnFail) {
			this.bQuietOnFail = bQuietOnFail;
			return this;
		}
		
		public boolean isQuietOnFail() {
			return bQuietOnFail;
		}
		
		public CallableX setRetryOnFail(boolean bRetryOnFail) {
			this.bRetryOnFail = bRetryOnFail;
			return this;
		}
		
		public boolean isRetryOnFail() {
			return bRetryOnFail ;
		}
		
		public CallableX setAsPrepend(){
			bPrepend=true;
			return this;
		}
		public CallableX putCustomValue(String strKey, Object objValue){
			hmCustom.put(strKey,objValue);
			return this;
		}
		public Object getCustomValue(String strKey){
			return hmCustom.get(strKey);
		}
//		public int getDelayBetweenRetries() {
//			return iDelayBetweenRetriesMilis;
//		}
		
		@Override
		public ArrayList<RetryOnFailure> getRetryListForManagement(RetryOnFailure.CompositeControl ccSelf) {
			return arList ;
		}
		
		public void setId(String strId) {
			if(this.strId!=null)PrerequisitesNotMetException.assertNotAlreadySet("id", this.strId, strId, this);
			if(strId==null)throw new PrerequisitesNotMetException("null id", this);
			this.strId = CallableX.class.getSimpleName()+ReflexFillI.i().getCommandPartSeparator()+strId;
		}
		
		@Override
		public String getId() {
			if(strId!=null)return "CallerId="+strId;
			return "Owner:"+getOwner();
		}
		
		public CallableX setFailWarnEveryTimes(int iTimes){
			this.iFailTimesWarn=iTimes;
			return this;
		}
		
		@Override
		public long getCurrentTimeMilis() {
			return lLastUpdateMilis;
		}
		public void updateTimeMilisTo(long lLastUpdateMilis) {
			this.lLastUpdateMilis = lLastUpdateMilis;
		}
		
		public Object getOwner() {
			return objEnclosing;
		}
		
		Parameters param = new Parameters();
		public long	lDbgLastRunTimeMilis;
		public long	lDbgLastQueueTimeMilis;
		private Object	objReturnValue;
		
		/**
		 * the access to the caller already is restricted 
		 * @return
		 */
		public Parameters getParamsForMaintenance(){
			return param;
		}
//		
//		public void setParams(Object... aobjParams) {
//			this.aobjParams=aobjParams;
//		}
//		public Object[] getAllParams(){
//			return this.aobjParams;
//		}
		
		public Object getReturnValue() {
			return objReturnValue;
		}
		
		public void setCallerReturnValue(Object obj){
			this.objReturnValue=obj;
		}
	}
	
	ArrayList<CallableX> aCallList = new ArrayList<CallableX>();
	
	public int update(float fTPF){
		int i=0;
		
		for(CallableX caller:new ArrayList<CallableX>(aCallList)){
			assertQueueAllowed(caller);
			
			caller.updateTimeMilisTo(System.currentTimeMillis());
			if(!caller.rReQueue.isReadyAndUpdate()){
				continue;
			}
			
			if(runCallerCode(caller))i++;
		}
		
		return i;
	}
	
	private void assertQueueAllowed(CallableX caller) {
		if(!caller.isAllowQueue()){
			throw new PrerequisitesNotMetException("cannot be run on queue mode", caller);
		}
	}

	private boolean runCallerCode(CallableX caller) {
//		try {
			caller.lDbgLastRunTimeMilis=System.currentTimeMillis();
			if(caller.call().booleanValue()){
				caller.iFailCount=0; //reset counter
				aCallList.remove(caller);
				return true;
			}else{
				caller.iFailCount++;
				
				if(!caller.isQuietOnFail()){
					boolean bShowMsg=false;
					if(caller.iFailTimesWarn>0){
						bShowMsg = (caller.iFailCount%caller.iFailTimesWarn)==0;
					}else{
						bShowMsg = caller.iFailCount==1; //only 1st time
					}
					
					if(bShowMsg){
						GlobalCommandsDelegatorI.i().dumpDevWarnEntry(
							"caller failed: "+caller.getId(), 
							caller, 
							caller.getOwner(), 
//							caller.getClass().getDeclaringClass(),
							caller.asteDbgLastQueuedAt
						);
					}
				}
				
				if(caller.isRetryOnFail()){
					addCall(caller, false, false); //will retry
				}
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
		addCall(caller,false,true);
	}
	
	/**
	 * if the caller returns false, it will be retried on the queue.
	 * 
	 * @param caller
	 * @param bPrepend
	 */
	public synchronized Boolean addCall(CallableX caller, boolean bTryToRunNow) {
		return addCall(caller,bTryToRunNow,true);
	}
	private synchronized Boolean addCall(CallableX caller, boolean bTryToRunNow, boolean bApplyOriginalDebugStack) {
		if(caller==null)throw new PrerequisitesNotMetException("null caller");
		
//	if(aCallList.contains(caller))
		while(aCallList.remove(caller)); //prevent multiplicity
		
		if(bTryToRunNow){
//			caller.asteDbgQueuedAt=null;
			return runCallerCode(caller);
		}else{
			assertQueueAllowed(caller);
			caller.lDbgLastQueueTimeMilis=System.currentTimeMillis();
			if(bApplyOriginalDebugStack)caller.asteDbgLastQueuedAt=Thread.currentThread().getStackTrace();
			if(caller.bPrepend){
				aCallList.add(0,caller);
			}else{
				aCallList.add(caller);
			}
			
			return true;
		}
	}

	public int getWaitingAmount() {
		return aCallList.size();
	}
}
