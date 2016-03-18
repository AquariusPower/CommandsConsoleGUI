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

package gui.console;

/**
 * Use this class to avoid running code on every loop.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class TimedDelay{
	protected static Long lCurrentTime;
	
	public static final long lNanoOneSecond = 1000000000L; // 1s in nano time
	public static final float fNanoToSeconds = 1f/lNanoOneSecond; //multiply nano by it to get in seconds
	
	protected Long	lNanoTime;
	protected float	fDelayLimit;
	protected Long	lNanoDelayLimit;
//	private long	lNanoTimePrevious;
//	private long	lNanoFrameDelay;
	private long	lNanoThreadSleep;
	
	/**
	 * use this to prevent current time to read from realtime
	 * @param lCurrentTime if null, will use realtime
	 */
	public static void setCurrentTime(Long lCurrentTime){
		TimedDelay.lCurrentTime = lCurrentTime;
	}
	
	public static long getCurrentTime(){
		if(lCurrentTime==null)return System.nanoTime();
		return lCurrentTime;
	}
	
	public TimedDelay(float fDelay) {
		super();
		this.fDelayLimit = fDelay;
		this.lNanoDelayLimit = (long) (this.fDelayLimit * lNanoOneSecond);;
	}
	public long getCurrentDelay() {
		if(!isActive())throw new NullPointerException("inactive"); //this, of course, affects all others using this method
//		System.err.println(getCurrentTime()-lNanoTime);
		return getCurrentTime()-lNanoTime;
	}
	public void updateTime() {
		lNanoTime = getCurrentTime();
	}
	public boolean isReady() {
		return isReady(false);
	}
	public boolean isReady(boolean bIfReadyWillAlsoUpdate) {
		boolean bReady = getCurrentDelay() >= lNanoDelayLimit;
		if(bIfReadyWillAlsoUpdate){
			if(bReady)updateTime();
		}
		return bReady;
	}
	public void reset() {
		lNanoTime=null;
	}
	public boolean isActive() {
		return lNanoTime!=null;
	}
	public void setActive(boolean b){
		if(b){
			if(!isActive())updateTime();
		}else{
			reset();
		}
	}
	public float getCurrentDelayPercentual() {
		float f = 1.0f - ((lNanoDelayLimit-getCurrentDelay())*fNanoToSeconds);
		if(f<0f)throw new NullPointerException("negative value: "+f);
		return f;
	}
	public float getCurrentDelayPercentualWithinBounds() {
		float f = getCurrentDelayPercentual();
		if(f>1f)return 1f;
		return f;
	}
	
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
	
}
