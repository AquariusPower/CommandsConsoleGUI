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

import java.util.HashMap;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 * TODO validate enable/disable too? can only enable if disabled; can only disable if enabled.
 */
public class CheckInitAndCleanupI {
	public static interface ICheckInitAndCleanupI{
		public abstract void initializationCompleted();
		public abstract boolean isInitializationCompleted();
	}; 
	
	static CheckInitAndCleanupI instance = new CheckInitAndCleanupI();
	public static CheckInitAndCleanupI i(){return instance;}
	
	HashMap<Object,InitializedInfo> hmDebugTrace = new HashMap<Object,InitializedInfo>();
	private class InitializedInfo{
		private StackTraceElement[] asteInitDebug;
		public InitializedInfo(StackTraceElement[] asteInitDebug) {
			super();
			this.asteInitDebug = asteInitDebug;
		}
	}
	
	/**
	 * Useful to globals.
	 * 
	 * @param objCheck
	 * @param objNew
	 * @return
	 */
	public <T> T getNewAssertingCheckIsNull(T objCheck, T objNew){
		if(objNew==null)throw new NullPointerException("new object is null...");
		
		if(objCheck==null){
			InitializedInfo icc = new InitializedInfo(Thread.currentThread().getStackTrace());
			hmDebugTrace.put(objNew, icc);
			return objNew;
		}else{
			InitializedInfo icc = hmDebugTrace.get(objCheck);
			if(icc==null){
				throw new NullPointerException(
					"checking is not null, and there is no debug stack: "+objInfo(objCheck));
			}else{
				throw prepareNPEWithCause(icc,objCheck);
			}
		}
	}
	
	private String objInfo(Object obj){
		return obj.getClass().getName()+"; "+obj;
	}
	
	private NullPointerException prepareNPEWithCause(InitializedInfo icc, Object obj){
		NullPointerException npe = new NullPointerException(
				"already initialized, cannot be double initialized: "+objInfo(obj));
		Throwable trw = new Throwable("already initialized at");
		trw.setStackTrace(icc.asteInitDebug);
		npe.initCause(trw);
		return npe;
	}
	
	/**
	 * to be used on initializers
	 * @param objKey use 'this'
	 */
	public void assertNotAlreadyInitializedAtInitializer(Object objKey){
		if(objKey==null)throw new NullPointerException("key is null...");
		
		InitializedInfo icc = hmDebugTrace.get(objKey);
		if(icc==null){
			icc = new InitializedInfo(Thread.currentThread().getStackTrace());
			hmDebugTrace.put(objKey, icc);
		}else{
			throw prepareNPEWithCause(icc,objKey);
//			NullPointerException npe = new NullPointerException(
//				"already initialized, cannot be double initialized: "+objKey);
//			Throwable trw = new Throwable("already initialized at");
//			trw.setStackTrace(icc.asteInitDebug);
//			npe.initCause(trw);
//			throw npe;
		}
	}
	
	/**
	 * to be used on cleanupers
	 * @param objKey use 'this'
	 */
	public void assertInitializedAtCleanup(Object objKey){
		InitializedInfo icc = hmDebugTrace.get(objKey);
		if(icc==null){
			throw new NullPointerException("cannot be cleaned as was not initialized: "+objKey);
		}
		
		hmDebugTrace.remove(objKey);
	}
}
