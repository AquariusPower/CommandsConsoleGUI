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

package jmestates;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;

import console.ConsoleCommands;
import console.test.ConsoleGuiTest;

/**
 * Spare GPU fan!
 *
 * @author AquariusPower <https://github.com/AquariusPower>
 * 
 */
public class FpsLimiterState implements AppState{
	private static FpsLimiterState instance = new FpsLimiterState();
	public static FpsLimiterState i(){return instance;}
	
	public static final long lNanoOneSecond = 1000000000L; // 1s in nano time
	public static final float fNanoToSeconds = 1f/lNanoOneSecond; //multiply nano by it to get in seconds
	
	private long	lNanoTimePrevious;
	
	private long	lNanoFrameDelayByCpuUsage;
	
	private long	lNanoThreadSleep;
	private long	lNanoDelayLimit;
	private int	iMaxFPS;
	private boolean	bEnabled;
	private boolean	bInitialized;
	private SimpleApplication	sapp;
	private ConsoleCommands	cc;
	private boolean	bConfigured;
	
	public FpsLimiterState(){
		setMaxFps(60);
	}
//	public FpsLimiterState(int iMaxFPS){
//		this();
//		setMaxFps(iMaxFPS);
//	}
	
	public FpsLimiterState setMaxFps(int iMaxFPS){
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

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		bInitialized=true;
	}

	@Override
	public boolean isInitialized() {
		return bInitialized;
	}

	@Override
	public void setEnabled(boolean active) {
		this.bEnabled=active;
	}

	@Override
	public boolean isEnabled() {
		return bEnabled;
	}

	@Override
	public void stateAttached(AppStateManager stateManager) {
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {
	}

	@Override
	public void render(RenderManager rm) {
	}

	@Override
	public void postRender() {
	}

	@Override
	public void cleanup() {
	}
	
	public int getFpsLimit() {
		return iMaxFPS;
	}

	public void configureBeforeInitializing(SimpleApplication sapp, ConsoleCommands cc){
		if(bConfigured)throw new NullPointerException("already configured."); // KEEP ON TOP
		this.sapp=sapp;
		this.cc=cc;
		if(!sapp.getStateManager().attach(this))throw new NullPointerException("already attached state "+this.getClass().getName());
		bConfigured=true;
	}
}
