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

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.VarIdValueOwnerData;
import com.github.commandsconsolegui.cmd.VarIdValueOwnerData.IVarIdValueOwner;
import com.github.commandsconsolegui.misc.HandleExceptionsRaw;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.ReflexFillI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;

/**
 * Use this class to avoid running code on every loop.
 * Or to have any kind of delayed execution.
 * Even just retrieve the current delay percentual for gradual variations.
 * 
 * For this class are also automatically generated console variables to directly tweak them.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */

public class TimedDelayVarField extends VarCmdFieldAbs{
	protected static IHandleExceptions ihe = HandleExceptionsRaw.i();
	protected static ArrayList<TimedDelayVarField> atdList = new ArrayList<TimedDelayVarField>();
	
	protected static Long lCurrentTimeNano;
	protected static boolean	bConfigured;
	
	public static final long lNanoOneSecond = 1000000000L; // 1s in nano time
	public static final float fNanoToSeconds = 1f/lNanoOneSecond; //multiply nano by it to get in seconds
	
	protected Long	lLastUpdateReferenceTimeNano;
	protected float	fDelayLimitSeconds;
	protected long	lDelayLimitNano;

	protected IReflexFillCfg	rfcfgOwner;
	protected VarIdValueOwnerData	vivo;
	protected String	strHelp;

	public static final String	strCodePrefixVariant = "td";
	
	public static void configure(IHandleExceptions ihe){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		if(ihe==null)throw new NullPointerException("invalid instance for "+IHandleExceptions.class.getName()); // KEEP ON TOP
		TimedDelayVarField.ihe=ihe;
		bConfigured=true;
	}
	
	/**
	 * use this to prevent current time to read from realtime
	 * @param lCurrentTimeNano if null, will use realtime
	 */
	public static void setCurrentTimeNano(Long lCurrentTimeNano){
		TimedDelayVarField.lCurrentTimeNano = lCurrentTimeNano;
	}
	
	public static long getCurrentTimeNano(){
		if(lCurrentTimeNano==null)return System.nanoTime();
		return lCurrentTimeNano;
	}
	
	/**
	 * This constructor is exclusively for methods local variables.
	 * Such variables will not be stored neither easily accessible at console.
	 * 
	 * @param rfcfgOwnerUseThis
	 * @param fDelay
	 */
	public TimedDelayVarField(float fDelay, String strHelp) {
		this(null,fDelay,strHelp);
		this.bReflexingIdentifier=false;
	}
	/**
	 * This constructor is for field variables.
	 * Such variables will be stored easily accessible and configurable at console!
	 * 
	 * @param rfcfgOwnerUseThis use null if this is not a class field, but a local variable
	 * @param fDelay
	 */
	public TimedDelayVarField(IReflexFillCfg rfcfgOwnerUseThis, float fDelay, String strHelp) {
		if(rfcfgOwnerUseThis!=null)atdList.add(this); //only fields allowed
		this.rfcfgOwner=rfcfgOwnerUseThis;
		setDelayLimitSeconds(fDelay);
		this.strHelp=strHelp;
	}
	
	private void setDelayLimitSeconds(float fDelaySeconds){
		this.fDelayLimitSeconds = fDelaySeconds;
		this.lDelayLimitNano = (long) (this.fDelayLimitSeconds * lNanoOneSecond);;
//		if(isActive())updateTime(); //this is required for consistency at get percentual 
	}
	
	public long getCurrentDelayNano() {
		return getCurrentDelayNano(false,false);
	}
	
	/**
	 * 
	 * @param bOverlapLimit if false, update is required to get a value withing the limit. If true,
	 * will use {@link #lLastUpdateReferenceTimeNano} to precisely determine the delay based on 
	 * the remainder of a the division by {@link #lDelayLimitNano} 
	 * @param bOverlapModeAlsoUpdateReferenceTime
	 * @return
	 */
	public long getCurrentDelayNano(boolean bOverlapLimit, boolean bOverlapModeAlsoUpdateReferenceTime) {
		if(!isActive())throw new NullPointerException("inactive"); //this, of course, affects all others using this method
//		System.err.println(getCurrentTime()-lNanoTime);
		
		long lCurrentDelay = 0;
		
		if(bOverlapLimit){
			long lCurrentTimeNano = getCurrentTimeNano();
			
			long lTotalDelayNano = lCurrentTimeNano - lLastUpdateReferenceTimeNano;
			
			lCurrentDelay = lTotalDelayNano%lDelayLimitNano;
			
//			while(lTimeNanoCopy<lCurrentTimeNano){
//				lTimeNanoCopy+=lDelayLimitNano;
//			}
//			
//			long lOverlappedDiff = lTimeNanoCopy - lCurrentTimeNano;
//			
//			lCurrentDelay = lDelayLimitNano - lOverlappedDiff;
			
			if(bOverlapModeAlsoUpdateReferenceTime){
				lLastUpdateReferenceTimeNano = lCurrentTimeNano;
			}
		}else{
			lCurrentDelay = getCurrentTimeNano()-lLastUpdateReferenceTimeNano;
		}
		
		return lCurrentDelay;
	}
	
	public void updateTime() {
		lLastUpdateReferenceTimeNano = getCurrentTimeNano();
	}
	public boolean isReady() {
		return isReady(false);
	}
	public boolean isReady(boolean bIfReadyWillAlsoUpdate) {
		boolean bReady = getCurrentDelayNano() >= lDelayLimitNano;
		if(bIfReadyWillAlsoUpdate){
			if(bReady)updateTime();
		}
		return bReady;
	}
	public long getDelayLimitNano(){
		return lDelayLimitNano;
	}
	public float getDelayLimitSeconds(){
		return fDelayLimitSeconds;
	}
	public void reset() {
		lLastUpdateReferenceTimeNano=null;
	}
	public boolean isActive() {
		return lLastUpdateReferenceTimeNano!=null;
	}
	
	/**
	 * can be called many subsequent times without updating the reference time
	 * @param b
	 */
	public void setActive(boolean b){
		if(b){
			if(!isActive())updateTime();
		}else{
			reset();
		}
	}
	
	/**
	 * Will overlap {@link #lDelayLimitNano} not requiring {@link #updateTime()} 
	 * to return precise values.
	 * 
	 * @return
	 */
	public float getCurrentDelayPercentualDynamic() {
		long lCurrentDelay = getCurrentDelayNano(true,false);
		
//		long lTimeNanoCopy = lLastUpdateReferenceTimeNano;
//		long lCurrentTimeNano = getCurrentTimeNano();
//		
//		long lDiff = lCurrentTimeNano-lTimeNanoCopy;
//		
//		long lRemainder = lDiff%lDelayLimitNano;
//		
//		
//		while(lTimeNanoCopy<lCurrentTimeNano){
//			lTimeNanoCopy+=lDelayLimitNano;
//		}
//		
//		long lCurrentDelay = lDelayLimitNano - (lTimeNanoCopy - lCurrentTimeNano);
//		
//		
//		
//		
//		long lCurrentDelay = getCurrentDelayNano();
//		long lDiff = lDelayLimitNano-lCurrentDelay;
//		
//		while(lDiff<0){
//			updateTime();
//		}
		
		double dPerc = 1.0 - ((double)lCurrentDelay)/((double)lDelayLimitNano);
		
		return (float)dPerc;
	}
	
	/**
	 * Will not overlap {@link #lDelayLimitNano}, so {@link #updateTime()} is required.
	 * 
	 * @return if null, indicates that an update is required.
	 */
	public Float getCurrentDelayPercentual(boolean bIfReadyWillAlsoUpdate) {
		if(bIfReadyWillAlsoUpdate)isReady(true); //just to auto update.
		
		long lCurrentDelay = getCurrentDelayNano();
		long lDiff = lDelayLimitNano-lCurrentDelay;
		if(lDiff<0){
			if(bIfReadyWillAlsoUpdate){
				return 0f;
			}else{
				return null;
			}
		}
		double dPerc = 1.0 - ((double)lDiff)/((double)lDelayLimitNano);
		
		return (float)dPerc;
//		
//		float f = 1.0f - ((lNanoDelayLimit-getCurrentDelayNano())*fNanoToSeconds);
//		if(f<0f){
//			ihe.handleExceptionThreaded(new NullPointerException("negative value: "+f));
//			f=0f;
//		}
//		return f;
	}
	
//	public float getCurrentDelayPercentualWithinBounds() {
//		float f = getCurrentDelayPercentual();
//		if(f>1f)return 1f;
//		return f;
//	}
	
//	/**
//	 * Will use the delay value to sleep the thread.
//	 * 
//	 * FPS Limiter:
//	 * 	The TimePerFrame (tpf) is measured from after the sleepCode to just before the sleepCode,
//	 * 	therefore, this is the tpf based on CPU usage/encumberance.
//	 * 
//	 * 	For the limiter to work properly, this method MUST be called at every frame update,
//	 * 	and only from a single place!
//	 * 
//	 * @param bFpsLimiter
//	 */
//	public void updateSleepThread(boolean bFpsLimiter){
//		try {
//			if(bFpsLimiter){
//				lNanoFrameDelay = System.nanoTime() - lNanoTimePrevious; //MUST BE BEFORE THE SLEEP!!!!!!
//				lNanoThreadSleep = lNanoDelayLimit -lNanoFrameDelay;
//				if(lNanoThreadSleep<0L)lNanoThreadSleep=0L;
//			}else{
//				lNanoThreadSleep = lNanoDelayLimit;
//			}
//			
//			if(lNanoThreadSleep>0L)Thread.sleep(lNanoDelayLimit/1000000L); //milis
//			
//			if(bFpsLimiter){
//				lNanoTimePrevious = System.nanoTime(); //MUST BE AFTER THE SLEEP!!!!!!!
//			}
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
	
	public static ArrayList<TimedDelayVarField> getListCopy(){
		return new ArrayList<TimedDelayVarField>(atdList);
	}

	@Override
	public String getVarId() {
		if(strVarId==null){
			super.setId(ReflexFillI.i().createIdentifierWithFieldName(rfcfgOwner, this, true));
		}
		
		return strVarId;
	}

	@Override
	public String getCodePrefixVariant() {
		return strCodePrefixVariant;
	}

	@Override
	public IReflexFillCfg getOwner() {
		return rfcfgOwner;
	}

	@Override
	public void setObjectValue(Object objValue) {
		if(objValue instanceof Double){
			setDelayLimitSeconds( ((Double)objValue).floatValue() );
		}else{
			setDelayLimitSeconds((Float)objValue);
		}
		
		if(vivo!=null)vivo.setObjectValue(objValue);
	}

	@Override
	public String getReport() {
		return getVarId()+" = "+getDelayLimitSeconds();
	}

	@Override
	public Object getValueRaw() {
		return getDelayLimitSeconds();
	}

	@Override
	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
		this.vivo=vivo;
	}
	
	@Override
	public String toString() {
//		if(getValueRaw()==null)return null;
		return MiscI.i().fmtFloat(getDelayLimitSeconds(),3);
	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVariablePrefix() {
		return "TimedDelay";
	}

}
