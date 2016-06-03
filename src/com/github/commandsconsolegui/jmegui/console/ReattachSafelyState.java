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

package com.github.commandsconsolegui.jmegui.console;

import java.util.concurrent.Callable;

import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.ConditionalAppStateAbs;
import com.github.commandsconsolegui.jmegui.console.ReattachSafelyState.ReattachSafelyValidateSteps;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;

/**
 * Reattaches other states safely, one step per call to this state update. 
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ReattachSafelyState <S extends ReattachSafelyValidateSteps> extends ConditionalAppStateAbs<SimpleApplication> {
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
	
	private Callable<Void>	detach;
	private Callable<Void>	attach;
	private Callable<Void>	postInitialization;

	private S	stateTarget;
	
	public ReattachSafelyState(S stateTarget){
		this.stateTarget=stateTarget;
			
		configureValidating();
	}
	
	protected boolean configureValidating() {
		if(this.stateTarget==null)throw new NullPointerException("target state is null");
		
		return super.configureValidating(GlobalSappRefI.i().get());
	}
	
	@Override
	public boolean updateValidating(float tpf) {
		if(ercCurrentStep==null)return false;
		
		if(stateTarget.reattachValidateStep(ercCurrentStep))return false;
		
		switch(ercCurrentStep){
			case Step0Detach:
				if(getApp().getStateManager().getState(stateTarget.getClass())!=null){
					getApp().enqueue(detach);
					ercCurrentStep=ERecreateConsoleSteps.Step1Attach;
					return true;
				}
				break;
			case Step1Attach:
				if(getApp().getStateManager().getState(stateTarget.getClass())==null){
					getApp().enqueue(attach);
					ercCurrentStep=ERecreateConsoleSteps.Step2PostInitialization;
					return true;
				}
				break;
			case Step2PostInitialization:
				if(isInitializedProperly()){
					getApp().enqueue(postInitialization);
					ercCurrentStep=null;
					return true;
				}
				break;
		}
		
		return false;
	}
	
	public boolean isProcessingRequest(){
		return ercCurrentStep!=null;
	}
	
	public void request(Callable<Void> detach, Callable<Void> attach,	Callable<Void> postInitialization) {
		this.detach = detach;
		this.attach = attach;
		this.postInitialization = postInitialization;
		ercCurrentStep=ERecreateConsoleSteps.Step0Detach;
	}

	@Override
	protected boolean checkInitPrerequisites() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean initializeValidating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean enableValidating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean disableValidating() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
