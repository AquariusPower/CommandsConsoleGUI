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

package com.github.commandsconsolegui.jmegui.extras;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.jmegui.ConditionalAppStateAbs.ICfgParm;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalAppStateAbs;
import com.jme3.app.Application;

/**
 * Spare GPU fan!
 *
 * @author AquariusPower <https://github.com/AquariusPower>
 * 
 */
public class FpsLimiterStateI extends CmdConditionalAppStateAbs{
	private static FpsLimiterStateI instance = new FpsLimiterStateI();
	public static FpsLimiterStateI i(){return instance;}
	
	public static final long lNanoOneSecond = 1000000000L; // 1s in nano time
	public static final float fNanoToSeconds = 1f/lNanoOneSecond; //multiply nano by it to get in seconds
	
	private long	lNanoTimePrevious;
	
	private long	lNanoFrameDelayByCpuUsage;
	
	private long	lNanoThreadSleep;
	private long	lNanoDelayLimit;
	private int	iMaxFPS;
	
	public static class CfgParm implements ICfgParm{} //look at super class
	@Override
	public void configure(ICfgParm icfg) {
		setMaxFps(60);
		super.configure(new CmdConditionalAppStateAbs.CfgParm(
			FpsLimiterStateI.class.getSimpleName(),false));
	}
	
	public FpsLimiterStateI setMaxFps(int iMaxFPS){
		this.iMaxFPS=iMaxFPS;
		if(this.iMaxFPS<1)this.iMaxFPS=1;
		lNanoDelayLimit = (long) ((1.0f/this.iMaxFPS)*lNanoOneSecond);
//		lMilisDelayLimit = lNanoDelayLimit/1000000L;
		return this;
	}
	
	public long getThreadSleepTimeNano(){
		return lNanoThreadSleep;
	}
	
	public long getThreadSleepTimeMilis(){
		return lNanoThreadSleep/1000000L;
	}
	
	public float getThreadSleepTime(){
		return lNanoThreadSleep*fNanoToSeconds;
	}
	
	/**
	 * FPS Limiter:
	 * 	The time per frame (tpf) is measured from after Thread.sleep() to just before Thread.sleep(),
	 * 	therefore, this is the tpf based on CPU usage/encumberance.
	 * 
	 * 	For the limiter to work properly, this method MUST be called at every frame update,
	 * 	and only from a single place!
	 * 
	 * @param bFpsLimiter
	 */
	@Override
	public void update(float tpf) {
		try {
			/**
			 * //MUST BE BEFORE THE SLEEP!!!!!!
			 */
			lNanoFrameDelayByCpuUsage = System.nanoTime() - lNanoTimePrevious;
			lNanoThreadSleep = lNanoDelayLimit -lNanoFrameDelayByCpuUsage;
			if(lNanoThreadSleep<0L)lNanoThreadSleep=0L; //only useful for reports
			
			if(lNanoThreadSleep>0L)Thread.sleep(getThreadSleepTimeMilis());
			
			/**
			 * MUST BE AFTER THE SLEEP!!!!!!!
			 */
			lNanoTimePrevious = System.nanoTime();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public long getFrameDelayByCpuUsageMilis(){
		return lNanoFrameDelayByCpuUsage/1000000L;
	}

	public int getFpsLimit() {
		return iMaxFPS;
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(null,"fpsLimit","[iMaxFps]")){
			Integer iMaxFps = cc.paramInt(1);
			if(iMaxFps!=null){
				FpsLimiterStateI.i().setMaxFps(iMaxFps);
				bCommandWorked=true;
			}
			cc.dumpSubEntry("FpsLimit = "+FpsLimiterStateI.i().getFpsLimit());
		}else
		{
//			return cc.executePreparedCommandRoot();
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}

}
