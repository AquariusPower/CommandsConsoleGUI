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

package com.github.commandsconsolegui.spAppOs.misc;

import com.github.commandsconsolegui.spAppOs.SimulationTime.ISimulationTime;
import com.github.commandsconsolegui.spAppOs.globals.GlobalSimulationTimeI;


/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class Request {
	long lRequestNano = 0;
	private boolean	bUseGlobalSimulationTime = false;
	private ISimulationTime	owner = null;
	private long	lDelayNano = 0;
	
	/**
	 * this one will use real time
	 */
	public Request(){}
	
	public Request(boolean bUseGlobalSimulationTime){
		this.bUseGlobalSimulationTime=bUseGlobalSimulationTime;
	}
	
	/**
	 * this one will use owner simulation time
	 * @param owner
	 */
	public Request(ISimulationTime owner){
		this.owner=owner;
	}
	
//	private long currentTime(boolean bUseGlobal){
	private long currentTime(){
		if(owner!=null){
			return (owner.getCurrentNano());
		}else{
			if(bUseGlobalSimulationTime){
//				if(bUseGlobal){
					return GlobalSimulationTimeI.i().getNano();
//				}else{
//					throw new PrerequisitesNotMetException("to use this method, either an owner or flag to use simulation time is required", owner, bUseGlobalSimulationTime, this);
//				}
			}else{
				return (System.nanoTime());
			}
		}
	}
	
	/**
	 * requests will always be processed at least on the next frame!
	 * @return 
	 */
	public Request requestNow() {
		requestNow(0f);
		return this;
	}
	public Request requestNow(float fDelaySeconds) {
		if(fDelaySeconds<0f)throw new PrerequisitesNotMetException("invalid negative delay", fDelaySeconds, this);
		this.lDelayNano  = TimeHelperI.i().secondsToNano(fDelaySeconds);
		this.lRequestNano = currentTime();
		return this;
	}
	
	public boolean isReady(){
		return isReady(false);
	}
	
	public Request reset(){
		lRequestNano=0;
		return this;
	}
	
	public boolean isReady(boolean bReset){
		/**
		 * keep as LESS THAN, so the request can only be ready on the NEXT frame
		 * this prevents many problems... TODO exemplify.
		 */
		if(isRequestActive() && (lRequestNano+lDelayNano)<currentTime()){
			if(bReset)reset();
			return true;
		}
		
		return false;
	}
	
	public boolean isReadyAndReset(){
		return isReady(true);
	}

	public boolean isRequestActive() {
		return lRequestNano!=0;
	}
	
//	public boolean isReadyAndReset(){
//		if(owner!=null){
//			return isReadyAndReset(owner.getCurrentNano());
//		}else{
//			if(!bUseGlobalSimulationTime){
//				return isReadyAndReset(System.nanoTime());
//			}
//		}
//	}
	
//	/**
//	 * @param lOwnerCurrentNano must be newer than request time
//	 * @return
//	 */
//	private boolean isReadyAndReset(long lOwnerCurrentNano){
//		if(lRequestNano!=0 && lRequestNano<lOwnerCurrentNano){
//			lRequestNano=0;
//			return true;
//		}
//		
//		return false;
//	}
}
