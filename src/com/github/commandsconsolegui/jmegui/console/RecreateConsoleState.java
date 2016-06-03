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

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.ConditionalAppStateAbs;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class RecreateConsoleState extends ConditionalAppStateAbs {
	private static RecreateConsoleState instance = new RecreateConsoleState();
	public static RecreateConsoleState i(){return instance;}
	
	enum ERecreateConsoleSteps{
		Detach_0,
		Attach_1,
		PostInitialization_2,
	}
	
	ERecreateConsoleSteps ercCurrentStep = null;
	
	private AppStateManager	sm;
	private SimpleApplication	sapp;

	private Callable<Void>	detach;

	private Callable<Void>	attach;

	private Callable<Void>	postInitialization;
	
	@Override
	protected void initialize(Application app) {
		super.initialize(app);
		sapp = GlobalSappRefI.i().get();
		sm = GlobalSappRefI.i().get().getStateManager();
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf);
		
		if(ercCurrentStep==null)return;
		
		switch(ercCurrentStep){
			case Detach_0:
				if(sm.getState(this.getClass())!=null){
					sapp.enqueue(detach);
					ercCurrentStep=ERecreateConsoleSteps.Attach_1;
				}
				break;
			case Attach_1:
				if(sm.getState(this.getClass())==null){
					sapp.enqueue(attach);
					ercCurrentStep=ERecreateConsoleSteps.PostInitialization_2;
				}
				break;
			case PostInitialization_2:
				if(isInitialized()){
					sapp.enqueue(postInitialization);
					ercCurrentStep=null;
				}
				break;
		}
		
	}
	
	public boolean isProcessingRequest(){
		return ercCurrentStep!=null;
	}
	
	public void request(Callable<Void> detach, Callable<Void> attach,	Callable<Void> postInitialization) {
		this.detach = detach;
		this.attach = attach;
		this.postInitialization = postInitialization;
		ercCurrentStep=ERecreateConsoleSteps.Detach_0;
	}
	
	@Override
	protected void onEnable() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI ccRequester) {
		// TODO Auto-generated method stub
		return null;
	}

}
