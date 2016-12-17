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

package com.github.commandsconsolegui.spCmd.misc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalSimulationTimeI;
import com.github.commandsconsolegui.spAppOs.misc.Buffeds.BfdArrayList;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.Parameters;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RefHolder;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.RunMode;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.RetryOnFailure.IRetryListOwner;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;


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
public class ManageCallQueueI implements IReflexFillCfg,IManager<ManageCallQueueI.CallableX> {
	private static ManageCallQueueI instance = new ManageCallQueueI();
	public static ManageCallQueueI i(){return instance;}
	
	public ManageCallQueueI() {
		DelegateManagerI.i().addManager(this, CallableX.class);
//		ManageSingleInstanceI.i().add(this);
	}
	
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
	
	/**
	 * safe/readonly fields
	 */
	public static class CallerInfo{
		private boolean	bRetryOnFail = true;
		private boolean bQuietOnFail = false;
		private boolean bAllowQueue = true;
		
		public boolean isAllowQueue() {
			return bAllowQueue;
		}
		public boolean isQuietOnFail() {
			return bQuietOnFail;
		}
		public boolean isRetryOnFail() {
			return bRetryOnFail ;
		}
	}
	
	public static abstract class CallableX implements CallableWeak<Boolean>,IRetryListOwner{
		private CallerInfo ci = new CallerInfo();
		private boolean bPrepend;
		private HashMap<String,Object> hmCustom;
//		private int	iDelayBetweenRetriesMilis = 0;
		private RetryOnFailure rReQueue;
		private ArrayList<RetryOnFailure>	arList;
		private String	strId;
		private long	lLastUpdateMillis;
		private String strDbgGenericSuperClass;
		private RefHolder<StackTraceElement[]>  rhasteDbgInstancedAt = new RefHolder<StackTraceElement[]>(null);
		private RefHolder<StackTraceElement[]>  rhasteDbgLastQueuedAt = new RefHolder<StackTraceElement[]>(null);;
		private long iFailCount=0;
		private Object	objEnclosing;
//		private Object[]	aobjParams;
		private int	iFailTimesWarn;
		private boolean	bIgnoreRecursiveCallWarning;
		private ArrayList<CallableX> acallerListRecursion = new ArrayList<CallableX>();
		
		public CallableX(Object objEnclosing) {
			this(objEnclosing,0);
		}
		public CallableX(Object objEnclosing, int iRetryDelayMilis) {
			super();
			
			// this is a guess check...
			if(objEnclosing instanceof Integer)throw new PrerequisitesNotMetException("use 'this' on enclosing", objEnclosing, iRetryDelayMilis, this);
				
			this.objEnclosing=objEnclosing;
			
			strDbgGenericSuperClass = this.getClass().getGenericSuperclass().getTypeName();
			rhasteDbgInstancedAt.setHolded(Thread.currentThread().getStackTrace());
			
			hmCustom = new HashMap<String, Object>();
			
			// keep together in this order:
			arList = new ArrayList<RetryOnFailure>(); //MUST BE BEFORE {@link RetryOnFailure} instance below!!! 
			rReQueue = new RetryOnFailure(this, "ReQueue");
			
			rReQueue.setRetryDelay(iRetryDelayMilis);
//			this.iDelayBetweenRetriesMilis=i;
		}
		
		public CallerInfo getInfo(){
			return ci;
		}
		
		/**
		 * can only be run now
		 * @return
		 */
		public CallableX setQueueDenied() {
			ci.bAllowQueue = false;
			return this;
		}
		
		public CallableX setQuietOnFail(boolean bQuietOnFail) {
			ci.bQuietOnFail = bQuietOnFail;
			return this;
		}
		
		public CallableX setRetryOnFail(boolean bRetryOnFail) {
			ci.bRetryOnFail = bRetryOnFail;
			return this;
		}
		
		public CallableX setIgnoreRecursiveCallWarning(){
			this.bIgnoreRecursiveCallWarning=true;
			return this;
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
			if(this.strId!=null)PrerequisitesNotMetException.assertNotAlreadySet(this.strId, strId, "id", this);
			if(strId==null)throw new PrerequisitesNotMetException("null id", this);
			this.strId = CallableX.class.getSimpleName()+ReflexFillI.i().getCommandPartSeparator()+strId;
		}
		
		@Override
		public String getUniqueId() {
			if(strId!=null)return "CallerId="+strId;
			return "Owner:"+getOwner();
		}
		
		public CallableX setFailWarnEveryTimes(int iTimes){
			this.iFailTimesWarn=iTimes;
			return this;
		}
		
		@Override
		public long getCurrentTimeMilis() {
			return lLastUpdateMillis;
		}
		
		/**
		 * use this also to ensure the delay between retries will also be an initial delay for the 1st attempt
		 * @return
		 */
		public CallableX updateTimeMilisNow() {
			this.lLastUpdateMillis = GlobalSimulationTimeI.i().getMillis();
			return this;
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
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CallableX [ci=");
			builder.append(ci);
			builder.append(", bPrepend=");
			builder.append(bPrepend);
			builder.append(", hmCustom=");
			builder.append(hmCustom);
			builder.append(", rReQueue=");
			builder.append(rReQueue);
			builder.append(", arList=");
			builder.append(arList);
			builder.append(", strId=");
			builder.append(strId);
			builder.append(", lLastUpdateMilis=");
			builder.append(lLastUpdateMillis);
			builder.append(", strDbgGenericSuperClass=");
			builder.append(strDbgGenericSuperClass);
			builder.append(", rhasteDbgInstancedAt=");
			builder.append(rhasteDbgInstancedAt);
			builder.append(", rhasteDbgLastQueuedAt=");
			builder.append(rhasteDbgLastQueuedAt);
			builder.append(", iFailCount=");
			builder.append(iFailCount);
			builder.append(", objEnclosing=");
			builder.append(objEnclosing);
			builder.append(", iFailTimesWarn=");
			builder.append(iFailTimesWarn);
			builder.append(", bIgnoreRecursiveCallWarning=");
			builder.append(bIgnoreRecursiveCallWarning);
			builder.append(", acallerListRecursion=");
			builder.append(acallerListRecursion);
			builder.append(", param=");
			builder.append(param);
			builder.append(", lDbgLastRunTimeMilis=");
			builder.append(lDbgLastRunTimeMilis);
			builder.append(", lDbgLastQueueTimeMilis=");
			builder.append(lDbgLastQueueTimeMilis);
			builder.append(", objReturnValue=");
			builder.append(objReturnValue);
			builder.append("]");
			return builder.toString();
		}
		
		
	}
	
	BfdArrayList<CallableX> aCallList = new BfdArrayList<CallableX>(){};
	private boolean	bIgnoreRecursiveCallWarning;
	private CallableX	callerQueuedCurrentlyRunning;
	private CallableX	callerCurrentlyRunning;
	
	public int update(float fTPF){
		int i=0;
		
//		for(CallableX caller:aCallList.toArray(new CallableX[aCallList.size()])){
		for(CallableX caller:aCallList.toArray()){
			assertQueueAllowed(caller);
			
			caller.updateTimeMilisNow();
			if(!caller.rReQueue.isReadyAndUpdate()){
				continue;
			}
			
			callerQueuedCurrentlyRunning=caller;
			if(runCallerCode(caller))i++;
		}
		
		return i;
	}
	
	private void assertQueueAllowed(CallableX caller) {
		if(!caller.ci.isAllowQueue()){
			throw new PrerequisitesNotMetException("cannot be run on queue mode", caller);
		}
	}

	private boolean runCallerCode(CallableX caller) {
//		try {
			caller.lDbgLastRunTimeMilis=GlobalSimulationTimeI.i().getMillis();
			callerCurrentlyRunning=caller;
			if(caller.call().booleanValue()){
				caller.iFailCount=0; //reset counter
				aCallList.remove(caller);
				return true;
			}else{
				caller.iFailCount++;
				
				if(!caller.ci.isQuietOnFail()){
					boolean bShowMsg=false;
					if(caller.iFailTimesWarn>0){
						bShowMsg = (caller.iFailCount%caller.iFailTimesWarn)==0;
					}else{
						bShowMsg = caller.iFailCount==1; //only 1st time
					}
					
					if(bShowMsg){
						GlobalCommandsDelegatorI.i().dumpDevWarnEntry(
							"caller failed: "+caller.getUniqueId(), 
							caller, 
							caller.getOwner(), 
//							caller.getClass().getDeclaringClass(),
							caller.rhasteDbgLastQueuedAt.getHolded()
						);
					}
				}
				
				if(caller.ci.isRetryOnFail()){
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
	
	@Override
	public boolean addHandled(CallableX caller) {
		boolean b = aCallList.contains(caller);
		addCall(caller);
		return !b;
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
		
		warnIfPossibleCallRecursiveness(caller);
		
//	if(aCallList.contains(caller))
		while(aCallList.remove(caller)); //prevent multiplicity
		
		if(bTryToRunNow){
//			caller.asteDbgQueuedAt=null;
			return runCallerCode(caller);
		}else{
			assertQueueAllowed(caller);
			caller.lDbgLastQueueTimeMilis=GlobalSimulationTimeI.i().getMillis();
			if(bApplyOriginalDebugStack)caller.rhasteDbgLastQueuedAt.setHolded(Thread.currentThread().getStackTrace());
			if(caller.bPrepend){
				aCallList.add(0,caller);
			}else{
				aCallList.add(caller);
			}
			
			return true;
		}
	}

	private void warnIfPossibleCallRecursiveness(CallableX callerNewAdded) {
		if(callerCurrentlyRunning!=null && callerCurrentlyRunning.bIgnoreRecursiveCallWarning)return;
//		if(bIgnoreRecursiveCallWarning)return;
		if(!RunMode.bDebugIDE)return;
		
		ArrayList<StackTraceElement> asteList = new ArrayList<StackTraceElement>();
		StackTraceElement[] aste = Thread.currentThread().getStackTrace();
		for(StackTraceElement ste:aste){
			if(ste.getMethodName().equals("call")){
				asteList.add(ste);
			}
		}
		
		if(asteList.size()>0){
			if(callerNewAdded.acallerListRecursion.contains(callerCurrentlyRunning)){
				MsgI.i().warn("possible call recursiveness", asteList, callerNewAdded.acallerListRecursion, callerNewAdded);
			}else{
				callerNewAdded.acallerListRecursion.add(callerCurrentlyRunning);
			}
		}
	}

	public int getWaitingAmount() {
		return aCallList.size();
	}
	
	StringCmdField scfReport = new StringCmdField(this)
		.setCallerAssigned(new CallableX(this) {
			@Override
			public Boolean call() {
				GlobalCommandsDelegatorI.i().dumpSubEntry(aCallList);
				return true;
			}
		});
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcvField);
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
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}
	
	@Override
	public ArrayList<CallableX> getHandledListCopy() {
		return new BfdArrayList<ManageCallQueueI.CallableX>(aCallList){}; 
	}
}