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

package com.github.commandsconsolegui.spJme.extras;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.TimeHelperI;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.spJme.cmd.CmdConditionalStateAbs;

/**
 * Spare GPU fan!
 *
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
public class FpsLimiterStateI extends CmdConditionalStateAbs<FpsLimiterStateI>{
	private static FpsLimiterStateI instance = new FpsLimiterStateI();
	public static FpsLimiterStateI i(){return instance;}
	
//	public final long lNanoOneSecond = 1000000000L; // 1s in nano time
//	public final float fNanoToSeconds = 1f/lNanoOneSecond; //multiply nano by it to get in seconds
	
	private long	lNanoTimePrevious;
	
	private long	lNanoFrameDelayByCpuUsage;
	
	private long	lNanoThreadSleep;
	private long	lNanoDelayLimit;
	private int	iMaxFPS;
	
	public FpsLimiterStateI() {
		setPrefixCmdWithIdToo(true);
	}
	
	public static class CfgParm extends CmdConditionalStateAbs.CfgParm{
		public CfgParm() {super(null);}
	}
	private CfgParm cfg;
	@Override
	public FpsLimiterStateI configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		setMaxFps(60);
//		super.configure(new CmdConditionalStateAbs.CfgParm(FpsLimiterStateI.class.getSimpleName()));
		super.configure(icfg);
//		return storeCfgAndReturnSelf(cfg);
		return getThis();
	}
	
	public FpsLimiterStateI setMaxFps(int iMaxFPS){
		this.iMaxFPS=iMaxFPS;
		if(this.iMaxFPS<1)this.iMaxFPS=1;
//		lNanoDelayLimit = (long) ((1.0f/this.iMaxFPS)*lNanoOneSecond);
		lNanoDelayLimit = (long) TimeHelperI.i().secondsToNano(((1.0f/this.iMaxFPS)));
//		lMilisDelayLimit = lNanoDelayLimit/1000000L;
		return this;
	}
	
	public long getThreadSleepTimeNano(){
		return lNanoThreadSleep;
	}
	
	public long getThreadSleepTimeMilis(){
		return lNanoThreadSleep/1000000L;
	}
	
	public double getThreadSleepTime(){
		return TimeHelperI.i().nanoToSeconds(lNanoThreadSleep);
//		return lNanoThreadSleep*fNanoToSeconds;
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
	protected boolean updateAttempt(float tpf) {
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
		
		return true;
	}
	
	public long getFrameDelayByCpuUsageMilis(){
		return lNanoFrameDelayByCpuUsage/1000000L;
	}

	public int getFpsLimit() {
		return iMaxFPS;
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,"fpsLimit",null,"[iMaxFps]")){
			Integer iMaxFps = cd.getCurrentCommandLine().paramInt(1);
			if(iMaxFps!=null){
				FpsLimiterStateI.i().setMaxFps(iMaxFps);
				bCommandWorked=true;
			}
			cd.dumpSubEntry("FpsLimit = "+FpsLimiterStateI.i().getFpsLimit());
		}else
		{
//			return cc.executePreparedCommandRoot();
			return ECmdReturnStatus.NotFound;
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}
	
	public String getSimpleStatsReport(float fTPF){
		return "Tpf"
			+(FpsLimiterStateI.i().isEnabled() ? 
				(int)(fTPF*1000.0f) : 
				MiscI.i().fmtFloat(fTPF,6)+"s")
			+(FpsLimiterStateI.i().isEnabled() ? 
				"="+FpsLimiterStateI.i().getFrameDelayByCpuUsageMilis()+"+"+FpsLimiterStateI.i().getThreadSleepTimeMilis()+"ms" :
				"");
	}

	@Override
	protected FpsLimiterStateI getThis() {
		return this;
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=FpsLimiterStateI.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=FpsLimiterStateI.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
}
