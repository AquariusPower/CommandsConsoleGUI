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

import java.util.HashMap;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * TODO validate enable/disable too? can only enable if disabled; can only disable if enabled.
 * TODO cant this class be removed? is it that useful? is it duplicating some other algorithm?
 */
public class CheckInitAndCleanupI {
	public static interface ICheckInitAndCleanupI{
//		public abstract void initializationCompleted();
		public abstract boolean isInitializedProperly();
	}; 
	
	static CheckInitAndCleanupI instance = new CheckInitAndCleanupI();
	public static CheckInitAndCleanupI i(){return instance;}
	
	HashMap<String,StackTraceElement[]> hmDebugTrace = new HashMap<String,StackTraceElement[]>();
	
	enum EInitMode{
		Global,
		State,
	}
	
	private String asKey(EInitMode eim, Object obj){
		return ""+eim+":"+obj.getClass().getName()+":"+obj.hashCode();
	}
	
	/**
	 * Useful to globals.
	 * 
	 * @param objGlobal
	 * @param objNew
	 * @return
	 */
	public <T> T assertGlobalIsNull(T objGlobal, T objNew){
		if(objNew==null)throw new NullPointerException("new object is null...");
		
		if(objGlobal==null){
			put(EInitMode.Global,objNew);
			return objNew;
		}else{
			StackTraceElement[] ste = get(EInitMode.Global,objGlobal);
			if(ste==null){
				throw new NullPointerException(
					"checking is not null, and there is no debug stack: "+objInfo(objGlobal));
			}else{
				throw prepareNPEWithCause(ste,objGlobal);
			}
		}
	}
	
	private void put(EInitMode eim, Object obj){
		hmDebugTrace.put(asKey(eim,obj), Thread.currentThread().getStackTrace());
	}
	
	private StackTraceElement[] get(EInitMode eim, Object obj){
		return hmDebugTrace.get(asKey(eim,obj));
	}
	
	/**
	 * to be used on initializers
	 * @param objKey use 'this'
	 */
	public void assertStateNotAlreadyInitializedAtInitializer(Object objKey){
		if(objKey==null)throw new NullPointerException("key is null...");
		
		StackTraceElement[] ste = get(EInitMode.State,objKey);
		if(ste==null){
			put(EInitMode.State,objKey);
		}else{
			throw prepareNPEWithCause(ste,objKey);
		}
	}
	
	/**
	 * to be used on cleanupers
	 * @param objKey use 'this'
	 */
	public void assertStateIsInitializedAtCleanup(Object objKey){
		StackTraceElement[] ste = get(EInitMode.State,objKey);
		if(ste==null){
			throw new NullPointerException("cannot be cleaned as was not initialized: "+objKey);
		}
		
		hmDebugTrace.remove(objKey);
	}

	private String objInfo(Object obj){
		return obj.getClass().getName()+"; "+obj;
	}
	
	private NullPointerException prepareNPEWithCause(StackTraceElement[] ste, Object obj){
		throw new PrerequisitesNotMetException("already initialized, cannot be double initialized: ", objInfo(obj))
			.initCauseAndReturnSelf("already initialized at",ste);
//		NullPointerException npe = new NullPointerException(
//				"already initialized, cannot be double initialized: "+objInfo(obj));
//		Throwable trw = new Throwable("already initialized at");
//		trw.setStackTrace(ste);
//		npe.initCause(trw);
//		return npe;
	}
}
