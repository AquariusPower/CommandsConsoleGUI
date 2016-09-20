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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Things that should be been coded in a certain way but were overlooked.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class PrerequisitesNotMetException extends NullPointerException {
	private static final long	serialVersionUID	= 1342052861109804737L;
	private static boolean	bRequestExit;

	public PrerequisitesNotMetException(boolean bExitApplication, String str, Object... aobj) {
		super(join(str,aobj));
		PrerequisitesNotMetException.bRequestExit=bExitApplication;
	}
	public PrerequisitesNotMetException(String str, Object... aobj) {
		this(true,str,aobj);
	}
	
	private static String join(String str, Object... aobj){
		String strRet=str;
		if(aobj!=null){
			for(int i=0;i<aobj.length;i++){
				Object obj=aobj[i];
				if(strRet.equals(str)){
					strRet+=":\n";
				}
				
				strRet+="  ["+i+"]";
				
				if(obj==null){
					strRet+=""+null;
				}else{
					strRet+=obj.getClass().getName()+", "+"value:"+obj;
				}
				
				strRet+="\n";
			}
		}
		return strRet;
	}
	
	public static boolean isExitRequested(){
		return bRequestExit;
	}
	
		public static void assertNotAlreadySet(String strDescWhat, Object objCurrent, Object objNew, Object... aobjMoreObjectsForDebugInfo){
		ArrayList<Object> aobjAll = new ArrayList<Object>();
		aobjAll.add(objCurrent);
		aobjAll.add(objNew);
		aobjAll.addAll(Arrays.asList(aobjMoreObjectsForDebugInfo));
		
		if(objCurrent!=null)throw new PrerequisitesNotMetException(strDescWhat+": already set", aobjAll.toArray());
	}
	
	public PrerequisitesNotMetException setCauseAndReturnSelf(String strCauseMessage, StackTraceElement[] asteCauseStack) {
		return initCauseAndReturnSelf(strCauseMessage, asteCauseStack);
	}
	/**
	 * this one helps as reads like {@link #initCause(Throwable)}
	 * @param strCauseMessage
	 * @param asteCauseStack
	 * @return
	 */
	public PrerequisitesNotMetException initCauseAndReturnSelf(String strCauseMessage, StackTraceElement[] asteCauseStack) {
		Throwable tw = new Throwable(strCauseMessage);
		tw.setStackTrace(asteCauseStack);
		return initCauseAndReturnSelf(tw);
	}
	
	public PrerequisitesNotMetException setCauseAndReturnSelf(Throwable cause) {
		return initCauseAndReturnSelf(cause);
	}
	/**
	 * this one helps as reads like {@link #initCause(Throwable)}
	 * @param cause
	 * @return
	 */
	public PrerequisitesNotMetException initCauseAndReturnSelf(Throwable cause) {
		super.initCause(cause);
		return this;
	}
	
//	@Override
//	public synchronized Throwable initCause(Throwable cause) {
//		// TODO Auto-generated method stub
//		return super.initCause(cause);
//	}
}
