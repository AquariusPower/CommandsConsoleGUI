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

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.ConditionalAppStateAbs;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class UngrabMouseStateI extends ConditionalAppStateAbs {
	private static UngrabMouseStateI instance = new UngrabMouseStateI();
	public static UngrabMouseStateI i(){return instance;}
	
	ArrayList<Object> aobjKeepUngrabbedRequesterList = new ArrayList<Object>();
	long lTimeLastUpdateMilis;
	long lDelayToUngrabMilis=500;
	boolean bWasUnGrabbedDuringSlowdown=true;
	Thread	t;
	Runnable	r;
//	boolean bCleaningUp=false;
	
	/**
	 * let end developer decide when it will re-grab
	 */
	private boolean	bKeepUngrabbedOnSlowDown = false;
	
	/**
	 * @param lSlowMachineDelayToUngrabMilis null to use default
	 * @param bKeepUngrabbedOnSlowdown null to use default
	 */
	public void configureValidating(Long lSlowMachineDelayToUngrabMilis, Boolean bKeepUngrabbedOnSlowdown) {
		super.configureValidating(GlobalSappRefI.i().get());
		
		if(lSlowMachineDelayToUngrabMilis!=null)this.lDelayToUngrabMilis=lSlowMachineDelayToUngrabMilis;
		if(bKeepUngrabbedOnSlowdown!=null)this.bKeepUngrabbedOnSlowDown=bKeepUngrabbedOnSlowdown;
		
		GlobalSappRefI.i().get().getStateManager().attach(this);
	}
	
	/**
	 * this will also instantly ungrab the mouse cursor
	 * @param objRequester
	 * @param bKeepUngrabbed
	 */
	public synchronized void setKeepUngrabbedRequester(Object objRequester, boolean bKeepUngrabbed){
		if(bKeepUngrabbed){
			if(!aobjKeepUngrabbedRequesterList.contains(objRequester)){
				aobjKeepUngrabbedRequesterList.add(objRequester);
			}
		}else{
			if(aobjKeepUngrabbedRequesterList.contains(objRequester)){
				aobjKeepUngrabbedRequesterList.remove(objRequester);
			}
		}
	}
	
	@Override
	protected boolean initializeValidating() {
		r = new Runnable() {
			@Override
			public void run() {
				while(!isCleaningUp()){
					updateAtNewThread();
					try {
						Thread.sleep(lDelayToUngrabMilis);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		t = new Thread(r);
		t.setName(UngrabMouseStateI.class.getSimpleName());
		
		updateTimeAtMainThread();
		t.start();
		
//		initializationCompleted();
		return true;
	}
	
//	@Override
//	public void update(float tpf) {
//		super.update(tpf);
//		updateTimeAtMainThread();
//	}
//	
	private void updateTimeAtMainThread(){
		lTimeLastUpdateMilis=System.currentTimeMillis();
	}
	
	public void updateAtNewThread() {
		long lCurrentTimeMilis = System.currentTimeMillis();
		boolean bIsSlow = lCurrentTimeMilis > (lTimeLastUpdateMilis+lDelayToUngrabMilis);
		lTimeLastUpdateMilis=lCurrentTimeMilis;
		
//		boolean bIsGrabbed = org.lwjgl.input.Mouse.isGrabbed();
		boolean bIsGrabbed = !GlobalSappRefI.i().get().getInputManager().isCursorVisible();
		boolean bKeepUngrabbedRequested = aobjKeepUngrabbedRequesterList.size()>0;
		
		if(bIsGrabbed){
			if(bIsSlow || bKeepUngrabbedRequested){
				bWasUnGrabbedDuringSlowdown=true;
//				org.lwjgl.input.Mouse.setGrabbed(false);
				GlobalSappRefI.i().get().getInputManager().setCursorVisible(true);
			}
		}else{
			if(bKeepUngrabbedOnSlowDown){}else
			if(bKeepUngrabbedRequested){}else
			{
				/**
				 * restore grab state if it was grabbed before slowdown
				 */
				if(!bIsSlow){
					if(bWasUnGrabbedDuringSlowdown || !bKeepUngrabbedRequested){
//						org.lwjgl.input.Mouse.setGrabbed(true);
						GlobalSappRefI.i().get().getInputManager().setCursorVisible(false);
					}
				}
			}
		}
		
	}
	
	@Override
	protected boolean enableValidating() {
		updateTimeAtMainThread();
		return true;
	}

	@Override
	protected boolean checkInitPrerequisites() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean updateValidating(float tpf) {
		updateTimeAtMainThread();
		return true;
	}

	@Override
	protected boolean disableValidating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean cleanupValidating() {
		// TODO Auto-generated method stub
		return false;
	}
	
//	@Override
//	protected void cleanup(Application app) {
//		super.cleanup(app);
//		bCleaningUp=true;
//	}

}
