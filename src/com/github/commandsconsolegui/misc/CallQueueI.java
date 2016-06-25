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

package com.github.commandsconsolegui.misc;

import java.util.ArrayList;
import java.util.concurrent.Callable;


/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CallQueueI {
	private static CallQueueI instance = new CallQueueI();
	public static CallQueueI i(){return instance;}
	
	ArrayList<Callable<Boolean>> aCallList = new ArrayList<Callable<Boolean>>();
	
	public int update(float fTPF){
		int i=0;
		
		for(Callable<Boolean> caller:new ArrayList<Callable<Boolean>>(aCallList)){
			try {
				if(caller.call().booleanValue()){
					aCallList.remove(caller);
					i++;
				}else{
					appendCall(caller); //will retry
				}
			} catch (Exception e) {
				NullPointerException npe = new NullPointerException("callable exception");
				npe.initCause(e);
				throw npe;
			}
		}
		
		return i;
	}

	public synchronized void appendCall(Callable<Boolean> caller) {
		addCall(caller, false);
	}
	
	public synchronized void addCall(Callable<Boolean> caller, boolean bPrepend) {
		if(caller==null)throw new PrerequisitesNotMetException("null caller");
		
//	if(aCallList.contains(caller))
		aCallList.remove(caller);
		
		if(bPrepend){
			aCallList.add(0,caller);
		}else{
			aCallList.add(caller);
		}
	}
}
