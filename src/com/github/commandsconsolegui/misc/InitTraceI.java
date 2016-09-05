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

package com.github.commandsconsolegui.misc;

import java.util.HashMap;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
@Deprecated
public class InitTraceI {
	private static InitTraceI instance = new InitTraceI();
	public static InitTraceI i(){return instance;}
	
	HashMap<Object,InitInfo> hmDebugTrace = new HashMap<Object,InitInfo>();
	private class InitInfo{
		private StackTraceElement[] asteInitDebug;
		public InitInfo(StackTraceElement[] asteInitDebug) {
			super();
			this.asteInitDebug = asteInitDebug;
		}
	}

	public <T> void assertIsNull(T objCheck){
		getAssertIsNull(objCheck,null,true);
	}
	private <T> T getAssertIsNull(T objCheck, T objNew, boolean bJustCheck){
		if(!bJustCheck){
			if(objNew==null)throw new NullPointerException("new object is null...");
		}
		
		if(objCheck==null){
			InitInfo icc = new InitInfo(Thread.currentThread().getStackTrace());
			hmDebugTrace.put(objNew, icc);
			return objNew;
		}else{
			InitInfo icc = hmDebugTrace.get(objCheck);
			String strObjValiInfo = objCheck.getClass().getName()+"; "+objCheck;

			if(icc==null){
				if(bJustCheck)return null;
				throw new NullPointerException("validating is not null, and there is no debug stack: "+
					strObjValiInfo);
			}else{
				NullPointerException npe = new NullPointerException(
					"already initialized, cannot be double initialized: "+strObjValiInfo);
				Throwable trw = new Throwable("already initialized at");
				trw.setStackTrace(icc.asteInitDebug);
				npe.initCause(trw);
				throw npe;
			}
		}
		
//		if(objValidating==null){
//			asteInitDebug=Thread.currentThread().getStackTrace();
//			return objNew;
//		}else{
//			NullPointerException npe = new NullPointerException(
//				"already initialized, cannot be double initialized: "+objValidating.getClass().getName());
//			Throwable trw = new Throwable("already initialized at");
//			trw.setStackTrace(asteInitDebug);
//			npe.initCause(trw);
//			throw npe;
//		}
	}
}
