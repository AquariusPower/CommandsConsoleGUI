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

import java.util.Arrays;
import java.util.HashMap;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageDebugDataI {
	private static ManageDebugDataI instance = new ManageDebugDataI();
	public static ManageDebugDataI i(){return instance;}
	
	public static enum EDbgStkOrigin{
		Constructed,
		Discarded,
		
		LastSetValue,
		
		LastCall,
		
		;

		public String s() {return toString();}
	}
	
	public static class DebugData{
		HashMap<String,DebugInfo> hm = new HashMap<String,DebugInfo>();
		
		public DebugInfo get(String eso){
			DebugInfo di = hm.get(eso); //searches
			
			if(di==null){ //creates
				di=new DebugInfo();
				hm.put(eso,di);
			}
			
			return di;
		}
		
		public void setStackForCurrentMethod(DebugInfo di, int iIncIndex){
			StackTraceElement[] aste = Thread.currentThread().getStackTrace();
			/**
			 * skips:
			 * 0 - getStackTrace()
			 * 1 - this method
			 */
			aste = Arrays.copyOfRange(aste, 2+iIncIndex, aste.length);
			di.rh.setHolded(aste);
		}
	}
	
	private static class DebugInfo{
		EDbgStkOrigin eso;
		
		/**
		 * {@link RefHolder} to improve stack readability on IDE
		 */
		RefHolder<StackTraceElement[]> rh=new RefHolder<StackTraceElement[]>(null);
	}
	
	
	/**
	 * use like if(RunMode.bValidateDevCode)this.di=DebugData.i().(this.di,...)
	 * @param diExisting
	 * @param eso
	 * @return
	 */
	public DebugData setStack(DebugData dbgExisting, EDbgStkOrigin eso){
		return setStack(dbgExisting,eso.s(),1);
	}
	/**
	 * will use as key the fully qualified caller method name
	 * @param dbgExisting
	 * @return
	 */
	public DebugData setStack(DebugData dbgExisting){
		return setStack(dbgExisting, Thread.currentThread().getStackTrace()[2].toString(), 1);
	}
	/**
	 * 
	 * @param dbgExisting
	 * @param strKey
	 * @return
	 */
	public DebugData setStack(DebugData dbgExisting, String strKey){
		return setStack(dbgExisting,strKey,1);
	}
	/**
	 * @DevSelfNote IMPORTANT!!! all other related methods here must call this one DIRECTLY!!! because of the stack index!
	 * 
	 * @param dbgExisting
	 * @param str
	 * @param iIncStackIndex
	 * @return
	 */
	private DebugData setStack(DebugData dbgExisting, String str, int iIncStackIndex){
		DebugData dbg = dbgExisting;
		if(dbg==null)dbg=new DebugData();
		dbg.setStackForCurrentMethod(dbg.get(str), iIncStackIndex+1);
		
		return dbg;
	}
	
	/**
	 * 
	 * @param dbg
	 * @param eso
	 * @return {@link RefHolder} is more IDE readable than stack
	 */
	public RefHolder<StackTraceElement[]> getStack(DebugData dbg, EDbgStkOrigin eso){
		return getStack(dbg,eso.s());
	}
	public RefHolder<StackTraceElement[]> getStack(DebugData dbg, String eso){
		if(dbg==null)return null;
		DebugInfo di = dbg.get(eso);
		if(di==null)return null;
		return di.rh;
	}
}
