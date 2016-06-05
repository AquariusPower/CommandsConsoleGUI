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

/**
 * Reattaches other states safely, one step per call to this state update. 
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
@Deprecated
public class ReattachSafelyState extends ConditionalStateAbs{
//	private static ReattachOtherStateSafelyStateI instance = new ReattachOtherStateSafelyStateI();
//	public static ReattachOtherStateSafelyStateI i(){return instance;}
	
	enum ERecreateConsoleSteps{
		Step0DiscardTarget,
		Step1AttachNew,
		Step2PostInitializationOfNew,
	}
	
	ERecreateConsoleSteps ercCurrentStep = null;
	
	private Callable<Void>	postInitialization;

	private ConditionalStateAbs casTarget;
	
	private ConditionalStateAbs casNew;

	private boolean	bWasEnabled;
	
//	@Deprecated
//	@Override
//	protected void configure(Application app) {
//		throw new NullPointerException("deprecated!!!");
//	}
	public static class CfgParm implements ICfgParm{
		ConditionalStateAbs casTarget;
		public CfgParm(ConditionalStateAbs casTarget) {
			super();
			this.casTarget = casTarget;
		}
	}
	@Override
	public void configure(ICfgParm icfg) {
//	public void configure(ConditionalAppStateAbs casTarget) {
		CfgParm cfg = (CfgParm)icfg;
		
		if(cfg.casTarget==null)throw new NullPointerException("target state is null");
		this.casTarget=cfg.casTarget;
		
		super.configure(new ConditionalStateAbs.CfgParm(
			GlobalAppRefI.i().get()));
	}
	
	@Override
	public boolean updateOrUndo(float tpf) {
		if(ercCurrentStep==null)return true;
		
		/**
		 * We can have several state's instances of the same class,
		 * like several dialogs currently opened.
		 */
		boolean bStepDone = false;
		switch(ercCurrentStep){
			case Step0DiscardTarget:
				if(casTarget.isAttachedToManager()){
					if(!casTarget.reattachPrepareAndValidateStep(ercCurrentStep))return false;
					casTarget.requestDiscard();
					ercCurrentStep=ERecreateConsoleSteps.Step1AttachNew;
					bStepDone=true;
				}
				break;
			case Step1AttachNew:
				if(casTarget.isDiscarded()){
					if(!casTarget.reattachPrepareAndValidateStep(ercCurrentStep))return false;
					
					casNew = casTarget.createAndConfigureSelfCopy();
					casNew.setEnabledRequest(bWasEnabled);
					
//					Callable<Void> attach = new Callable<Void>() {
//						@Override
//						public Void call() throws Exception {
//							ConditionalAppStateManagerI.i().attach(casNew);
//							if(bWasEnabled){
//								casNew.setEnabledRequest(true);
//							}
//							return null;
//						}
//					};
//					
//					getApp().enqueue(attach);
					ercCurrentStep=ERecreateConsoleSteps.Step2PostInitializationOfNew;
					bStepDone=true;
				}
				break;
			case Step2PostInitializationOfNew:
				if(casNew.isInitializedProperly()){
					getApp().enqueue(postInitialization);
					ercCurrentStep=null;
					bStepDone=true;
				}
				break;
			default:
				return false;
		}
		
		if(!bStepDone)return false;
		
		return super.updateOrUndo(tpf);
	}
	
	public boolean isProcessingRequest(){
		return ercCurrentStep!=null;
	}
	
//	public void request(Callable<Void> detach, Callable<Void> attach,	Callable<Void> postInitialization) {
	public void request(Callable<Void> postInitialization) {
		this.bWasEnabled=casTarget.isEnabled();
		this.postInitialization = postInitialization;
		this.ercCurrentStep=ERecreateConsoleSteps.Step0DiscardTarget;
	}

}
