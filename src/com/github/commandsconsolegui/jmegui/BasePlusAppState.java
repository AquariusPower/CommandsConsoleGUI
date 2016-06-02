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

import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.CheckInitAndCleanupI;
import com.github.commandsconsolegui.misc.CheckInitAndCleanupI.ICheckInitAndCleanupI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class BasePlusAppState extends BaseAppState implements ICheckInitAndCleanupI, IReflexFillCfg{
	Long	lInitializationCompletedMilis;
//	long	lInitializationStartedMilis;
//	long	lInitializationMaxDelayMilis=1000;
//	long	lMessageCoolDownDelayMilis=3000;
//	long	lLastMessageMilis;
	
	TimedDelayVarField tdDebugMessageDelay = new TimedDelayVarField(this,3.0f);
	TimedDelayVarField tdInitDelayLimit = new TimedDelayVarField(this,5.0f);
	
	public abstract void configure(Object... aobj);
	
	@Override
	protected void initialize(Application app) {
		tdInitDelayLimit.setActive(true);
		CheckInitAndCleanupI.i().assertNotAlreadyInitializedAtInitializer(this);
	}
	
	/**
	 * after the cleanup, the state will be removed from the state manager!!!
	 */
	@Override
	protected void cleanup(Application app) {
		CheckInitAndCleanupI.i().assertInitializedAtCleanup(this);
	}
	
	/**
	 * put at the end of the initialization of the instantiated class
	 */
	@Override
	public void initializationCompleted(){
		this.lInitializationCompletedMilis=System.currentTimeMillis();
	}
	
	@Override
	public boolean isInitializationCompleted(){
		if(lInitializationCompletedMilis==null){
			if(tdInitDelayLimit.isReady()){
				tdDebugMessageDelay.setActive(true);
				if(tdDebugMessageDelay.isReady(true)){
					System.err.println("Warn: ["+tdInitDelayLimit.getCurrentDelayNano()+"] initialization is taking too long for "+this);
//					long lTimeMilis = System.currentTimeMillis();
//					if(lTimeMilis > (lLastMessageMilis+lMessageCoolDownDelayMilis)){
//						if(lTimeMilis > (lInitializationStartedMilis+lInitializationMaxDelayMilis)){
//							System.err.println("Warn: ["+lTimeMilis+"] initialization is taking too long for "+this);
//							lLastMessageMilis = lTimeMilis;
//						}
//					}
				}
			}
		}
		
		return lInitializationCompletedMilis!=null;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().get().getReflexFillCfg(rfcv);
	}
}
