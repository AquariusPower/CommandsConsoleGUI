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

package com.github.commandsconsolegui.jmegui;

import java.util.concurrent.Callable;

import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.jme3.app.state.AppState;

/**
 * Reattaches other states safely, one step per call to this state update. 
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ReattachSafelyState extends ConditionalAppStateAbs{
//	private static ReattachOtherStateSafelyStateI instance = new ReattachOtherStateSafelyStateI();
//	public static ReattachOtherStateSafelyStateI i(){return instance;}
	
	public static interface ReattachSafelyValidateSteps extends AppState{
		public abstract boolean reattachValidateStep(ERecreateConsoleSteps ercs);
	}
	
	enum ERecreateConsoleSteps{
		Step0Detach,
		Step1Attach,
		Step2PostInitialization,
	}
	
	ERecreateConsoleSteps ercCurrentStep = null;
	
//	private Callable<Void>	detach;
//	private Callable<Void>	attach;
	private Callable<Void>	postInitialization;

	private ConditionalAppStateAbs stateTarget;

	private boolean	bWasEnabled;
	
//	@Deprecated
//	@Override
//	protected void configure(Application app) {
//		throw new NullPointerException("deprecated!!!");
//	}
	public static class CfgParm implements ICfgParm{
		ConditionalAppStateAbs stateTarget;
		public CfgParm(ConditionalAppStateAbs stateTarget) {
			super();
			this.stateTarget = stateTarget;
		}
	}
	@Override
	public void configure(ICfgParm icfg) {
//	public void configure(ConditionalAppStateAbs stateTarget) {
		CfgParm cfg = (CfgParm)icfg;
		
		if(cfg.stateTarget==null)throw new NullPointerException("target state is null");
		this.stateTarget=cfg.stateTarget;
		
		super.configure(new ConditionalAppStateAbs.CfgParm(
			GlobalSappRefI.i().get()));
	}
	
	@Override
	public boolean updateValidating(float tpf) {
		if(ercCurrentStep==null)return true;
		
		if(!stateTarget.reattachValidateStep(ercCurrentStep))return false;
		
		/**
		 * Not using: app().getStateManager().getState()
		 * because I can have several state's instances of the same class,
		 * like several dialogs currently opened.
		 */
		boolean bIsAttached = stateTarget.isAttachedToStateManager();
		
		boolean bStepDone = false;
		switch(ercCurrentStep){
			case Step0Detach:
				if(bIsAttached){
					Callable<Void> detach = new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							app().getStateManager().detach(stateTarget);
							return null;
						}
					};
					
					getApp().enqueue(detach);
					ercCurrentStep=ERecreateConsoleSteps.Step1Attach;
					bStepDone=true;
				}
				break;
			case Step1Attach:
				if(!bIsAttached){
					Callable<Void> attach = new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							app().getStateManager().attach(stateTarget);
							if(bWasEnabled){
								setEnabledRequest(true);
							}
							return null;
						}
					};
					
					getApp().enqueue(attach);
					ercCurrentStep=ERecreateConsoleSteps.Step2PostInitialization;
					bStepDone=true;
				}
				break;
			case Step2PostInitialization:
				if(stateTarget.isInitializedProperly()){
					getApp().enqueue(postInitialization);
					ercCurrentStep=null;
					bStepDone=true;
				}
				break;
			default:
				return false;
		}
		
		if(!bStepDone)return false;
		
		return super.updateValidating(tpf);
	}
	
	public boolean isProcessingRequest(){
		return ercCurrentStep!=null;
	}
	
//	public void request(Callable<Void> detach, Callable<Void> attach,	Callable<Void> postInitialization) {
	public void request(Callable<Void> postInitialization) {
//		this.detach = detach;
//		this.attach = attach;
		bWasEnabled=stateTarget.isEnabled();
		this.postInitialization = postInitialization;
		ercCurrentStep=ERecreateConsoleSteps.Step0Detach;
	}

}
