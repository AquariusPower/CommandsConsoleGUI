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

package com.github.commandsconsolegui.jmegui.extras;

import java.util.ArrayList;

import com.github.commandsconsolegui.globals.jmegui.GlobalAppRefI;
import com.github.commandsconsolegui.jmegui.ConditionalStateAbs;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class UngrabMouseStateI extends ConditionalStateAbs {
	private static UngrabMouseStateI instance = new UngrabMouseStateI();
	public static UngrabMouseStateI i(){return instance;}
	
	ArrayList<Object> aobjKeepUngrabbedRequesterList = new ArrayList<Object>();
	long lLastUpdateMilisAtMainThread;
	long lDelayToUngrabMilis=500;
	boolean bWasUnGrabbedDuringSlowdown=true;
	Thread	t;
	Runnable	r;
//	boolean bCleaningUp=false;
	
	/**
	 * let end developer decide when it will re-grab
	 */
	private boolean	bKeepUngrabbedOnSlowDown = false;
	
	public static class CfgParm extends ConditionalStateAbs.CfgParm{
		Long lSlowMachineDelayToUngrabMilis;
		Boolean bKeepUngrabbedOnSlowdown;
		public CfgParm(Long lSlowMachineDelayToUngrabMilis,
				Boolean bKeepUngrabbedOnSlowdown) {
			super(null);
			this.lSlowMachineDelayToUngrabMilis = lSlowMachineDelayToUngrabMilis;
			this.bKeepUngrabbedOnSlowdown = bKeepUngrabbedOnSlowdown;
		}
	}
	/**
	 * @param lSlowMachineDelayToUngrabMilis null to use default
	 * @param bKeepUngrabbedOnSlowdown null to use default
	 * @return 
	 */
	@Override
	public UngrabMouseStateI configure(ICfgParm icfg) {
//	public void configure(Long lSlowMachineDelayToUngrabMilis, Boolean bKeepUngrabbedOnSlowdown) {
		CfgParm cfg = (CfgParm)icfg;
		super.configure(icfg);
		
		if(cfg.lSlowMachineDelayToUngrabMilis!=null)this.lDelayToUngrabMilis=cfg.lSlowMachineDelayToUngrabMilis;
		if(cfg.bKeepUngrabbedOnSlowdown!=null)this.bKeepUngrabbedOnSlowDown=cfg.bKeepUngrabbedOnSlowdown;
		
//		GlobalSappRefI.i().get().getStateManager().attach(this);
		return storeCfgAndReturnSelf(icfg);
	}
//	@Deprecated
//	@Override
//	private void configure(Application app) {
//		throw new NullPointerException("deprecated!!!");
//	}
	
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
	protected boolean initAttempt() {
		r = new Runnable() {
			@Override
			public void run() {
//				while(!isCleaningUp()){ //mainly during application close
				while(!isDiscarding() && !GlobalAppRefI.iGlobal().isApplicationExiting()){ //mainly during application close
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
		return super.initAttempt();
	}
	
//	@Override
//	public void update(float tpf) {
//		super.update(tpf);
//		updateTimeAtMainThread();
//	}
//	
	private void updateTimeAtMainThread(){
		lLastUpdateMilisAtMainThread=System.currentTimeMillis();
	}
	
	public void updateAtNewThread() {
		long lCurrentTimeMilis = System.currentTimeMillis();
		boolean bIsSlow = lCurrentTimeMilis > (lLastUpdateMilisAtMainThread+lDelayToUngrabMilis);
//		lTimeLastUpdateMilis=lCurrentTimeMilis;
		
//		boolean bIsGrabbed = org.lwjgl.input.Mouse.isGrabbed();
		boolean bIsGrabbed = !GlobalAppRefI.i().getInputManager().isCursorVisible();
		boolean bKeepUngrabbedRequested = aobjKeepUngrabbedRequesterList.size()>0;
		
		if(bIsGrabbed){
			if(bIsSlow || bKeepUngrabbedRequested){
				bWasUnGrabbedDuringSlowdown=true;
//				org.lwjgl.input.Mouse.setGrabbed(false);
				GlobalAppRefI.i().getInputManager().setCursorVisible(true);
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
						GlobalAppRefI.i().getInputManager().setCursorVisible(false);
					}
				}
			}
		}
		
	}
	
	@Override
	protected boolean enableAttempt() {
		updateTimeAtMainThread();
		return super.enableAttempt();
	}

	@Override
	protected boolean updateAttempt(float tpf) {
		updateTimeAtMainThread();
		return super.updateAttempt(tpf);
	}

	@Override
	protected void enableFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void disableFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void enableSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void disableSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initFailed() {
		// TODO Auto-generated method stub
		
	}

}
