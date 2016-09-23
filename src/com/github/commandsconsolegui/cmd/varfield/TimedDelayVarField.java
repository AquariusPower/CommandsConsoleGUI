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

package com.github.commandsconsolegui.cmd.varfield;

import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs.EVarCmdMode;
import com.github.commandsconsolegui.misc.SimpleHandleExceptionsI;
import com.github.commandsconsolegui.misc.IHandleExceptions;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;

/**
 * Use this class to avoid running code on every loop.
 * Or to have any kind of delayed execution.
 * Even just retrieve the current delay percentual for gradual variations.
 * 
 * For this class are also automatically generated console variables to directly tweak them.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */

public class TimedDelayVarField extends VarCmdFieldAbs<Float,TimedDelayVarField>{
	private static IHandleExceptions ihe = SimpleHandleExceptionsI.i();
//	private static ArrayList<TimedDelayVarField> atdList = new ArrayList<TimedDelayVarField>();
	
	private static Long lCurrentTimeNano;
	private static boolean	bConfigured;
	
	public static final long lNanoOneSecond = 1000000000L; // 1s in nano time
	public static final float fNanoToSeconds = 1f/lNanoOneSecond; //multiply nano by it to get in seconds
	public static final float fSecondsToNano = lNanoOneSecond; //multiply seconds to get in nano
	
	private Long	lLastUpdateReferenceTimeNano;
//	private float	fDelayLimitSeconds;
//	private long	lDelayLimitNano;

	private boolean	bOscilate;

//	public static final String	strCodePrefix = "td";
	
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
//		this.bReflexingIdentifier=false;
	}
	/**
	 * This constructor is for field variables.
	 * Such variables will be stored easily accessible and configurable at console!
	 * 
	 * @param rfcfgOwnerUseThis use null if this is not a class field, but a local variable
	 * @param fDelayDefault
	 */
	public TimedDelayVarField(IReflexFillCfg rfcfgOwnerUseThis, float fDelayDefault, String strHelp) {
//		if(rfcfgOwnerUseThis!=null)atdList.add(this); //only fields allowed
//		super(rfcfgOwnerUseThis!=null); //only fields allowed
		super(rfcfgOwnerUseThis, EVarCmdMode.Var, fDelayDefault); //only fields allowed
//		this.setOwner(rfcfgOwnerUseThis);
//		setObjectRawValue(fDelay);
		setHelp(strHelp);
		constructed();
	}
	
	private TimedDelayVarField setDelayLimitSeconds(float fDelaySeconds){
//		this.fDelayLimitSeconds = fDelaySeconds;
//		this.lDelayLimitNano = (long) (this.fDelayLimitSeconds * lNanoOneSecond);;
//		setObjectRawValue( (long) (fDelaySeconds * lNanoOneSecond) );
		setObjectRawValue(fDelaySeconds);
//		if(isActive())updateTime(); //this is required for consistency at get percentual
		return this;
	}
	
	public long getCurrentDelayNano() {
		return getCurrentDelayNano(false,false);
	}
	
	/**
	 * 
	 * @param bOverlapLimit if false, update is required to get a value withing the limit. If true,
	 * will use {@link #lLastUpdateReferenceTimeNano} to precisely determine the delay based on 
	 * the remainder of a the division by {@link #getDelayLimitNano()} 
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
			
			lCurrentDelay = lTotalDelayNano%getDelayLimitNano();
			
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
		boolean bReady = getCurrentDelayNano() >= getDelayLimitNano();
		if(bIfReadyWillAlsoUpdate){
			if(bReady)updateTime();
		}
		return bReady;
	}
	public long getDelayLimitMilis(){
		return getDelayLimitNano()/1000000;
	}
	public long getDelayLimitNano(){
		return (long) (getDelayLimitSeconds()*fSecondsToNano);
//		if(bOscilate){
//			return lDelayLimitNano*2;
//		}else{
//			return lDelayLimitNano;
//		}
	}
	public float getDelayLimitSeconds(){
		if(bOscilate){
//			return fDelayLimitSeconds*2f;
//			return lDelayLimitNano*fNanoToSeconds*2f;
			return getValue()*2f;
		}else{
//			return lDelayLimitNano*fNanoToSeconds;
			return getValue();
//			return fDelayLimitSeconds;
		}
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
	 * @return 
	 */
	public TimedDelayVarField setActive(boolean b){
		if(b){
			if(!isActive())updateTime();
		}else{
			reset();
		}
		
		return this;
	}
	
	public TimedDelayVarField setOscilateMode(boolean b){
		this.bOscilate=b;
		return this;
	}
	
	public float getCurrentDelayCalc(float fMaxValue,boolean bIfReadyWillAlsoUpdate) {
		return getCurrentDelayCalc(fMaxValue,bOscilate,false,bIfReadyWillAlsoUpdate);
	}
	public float getCurrentDelayCalcDynamic(float fMaxValue) {
		return getCurrentDelayCalc(fMaxValue,bOscilate,true,null);
	}
	/**
	 * 
	 * @param fMaxValue
	 * @param bOscilate
	 * @param bDynamic
	 * @param bIfReadyWillAlsoUpdate
	 * @return
	 */
	private float getCurrentDelayCalc(float fMaxValue, boolean bOscilate, boolean bDynamic, Boolean bIfReadyWillAlsoUpdate) {
		float fHalf=(fMaxValue/2f);
		
		Float fPerc = null;
		if(bDynamic){
			fPerc = getCurrentDelayPercentualDynamic();
		}else{
			fPerc = getCurrentDelayPercentual(bIfReadyWillAlsoUpdate);
		}
		float fCurrent = (fPerc * fMaxValue);
		
		if(!bOscilate)return fCurrent;
		
		float fOscilatedCurrent=0f;
		
		//ex.: max is 10
		if(fCurrent<fHalf){
			fOscilatedCurrent=fCurrent*2f; //ex.: from 0 to 10: 1 -> 2; 4 -> 8;
		}else{
			fOscilatedCurrent=fMaxValue-((fCurrent-fHalf)*2f); //ex.: from 10 to 0: 6 -> 8; 9 -> 2;
		}
		
		return fOscilatedCurrent;
	}	
	/**
	 * Will overlap {@link #getDelayLimitNano()} not requiring {@link #updateTime()} 
	 * to return precise values.
	 * 
	 * @return
	 */
	public float getCurrentDelayPercentualDynamic() {
		long lCurrentDelay = getCurrentDelayNano(true,false);
		
		double dPerc = 1.0 - ((double)lCurrentDelay)/((double)getDelayLimitNano());
		
		return (float)dPerc;
	}
	
	/**
	 * Will not overlap {@link #getDelayLimitNano()}, so {@link #updateTime()} is required.
	 */
	public float getCurrentDelayPercentual(boolean bIfReadyWillAlsoUpdate) {
		long lCurrentDelayNano = getCurrentDelayNano();
		
		long lDiff = getDelayLimitNano()-lCurrentDelayNano;
		if(lDiff<0)lDiff=0; //if it took too much time, this constraint will fix the negative value
		
		double dPerc = 1.0 - ((double)lDiff)/((double)getDelayLimitNano());
		
		if(isReady(bIfReadyWillAlsoUpdate)){
			return 1f; //if from getting the current to now it gets ready, will return 100%
		}else{
			return (float)dPerc;
		}
	}

	@Override
	public String getCodePrefixVariant() {
		return getCodePrefixDefault();
	}

//	public TimedDelayVarField setObjectRawValue(CommandsDelegator.CompositeControl cc, Object objValue) {
//		cc.assertSelfNotNull();
//		setObjectRawValue(objValue);
//		return this;
//	}
	@Override
	public TimedDelayVarField setObjectRawValue(Object objValue) {
		if(objValue==null)throw new PrerequisitesNotMetException(TimedDelayVarField.class.getSimpleName()+" can't be set to null!");
		
		if(objValue instanceof Double){
			objValue=( ((Double)objValue).floatValue() );
		}else
		if(objValue instanceof FloatDoubleVarField){
			objValue=( ((FloatDoubleVarField)objValue).getFloat() );
		}else
		if(objValue instanceof IntLongVarField){
			objValue=( ((IntLongVarField)objValue).getInt().floatValue() );
		}else
		if(objValue instanceof String){
			objValue=( Float.parseFloat((String)objValue) );
		}else
		{
			objValue=((Float)objValue); //default is expected type
		}
		
//		if(super.getConsoleVarLink()!=null)
			super.setObjectRawValue(objValue);
//		super.setObjectValue(ccCD, objValue);
		
		return this;
	}
	
//	public TimedDelayVarField setValue(float fDelay){
//		setDelayLimitSeconds(fDelay);
//		if(super.getConsoleVarLink()!=null)setObjectRawValue(this.fDelayLimitSeconds);
//		return this;
//	}
//
	
//	@Override
//	public String getReport() {
//		return getUniqueVarId()+" = "+getDelayLimitSeconds();
//	}

//	@Override
//	public Object getRawValue() {
//		return getDelayLimitSeconds();
//	}

//	@Override
//	public void setConsoleVarLink(VarIdValueOwnerData vivo) {
//		this.vivo=vivo;
//	}
	
//	@Override
//	public String toString() {
////		if(getValueRaw()==null)return null;
//		return MiscI.i().fmtFloat(getDelayLimitSeconds(),3);
//	}
	@Override
	public String getValueAsString() {
		return getValueAsString(3);
	}
//	@Override
//	public String getValueAsString(int iIfFloatPrecision) {
//		return MiscI.i().fmtFloat(getDelayLimitSeconds(), iIfFloatPrecision);
//	}

	@Override
	public String getHelp() {
		return null; //TODO check its uses and let it use super.getHelp()
	}

	@Override
	public String getVariablePrefix() {
		return "TimedDelay";
	}
	
	@Override
	protected TimedDelayVarField getThis() {
		return this;
	}

	private static String strCodePrefixDefault="td";
	@Override
	public String getCodePrefixDefault() {
		return strCodePrefixDefault;
	}
}
