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
 * "Things that should be been coded in a certain way but were overlooked",
 * but... is basically a buffed generic NullPointerException anyway...
 * 
 * This exception can be captured tho may request the application to exit gracefully.
 * 
 * TODO substitute all NullPointerException by this one.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class PrerequisitesNotMetException extends NullPointerException { //@STATIC_OK
	private static final long	serialVersionUID	= 1342052861109804737L;
	private static PrerequisitesNotMetException	exRequestExit;
	private static IUserInputDetector	userInputDetector;

	public PrerequisitesNotMetException(boolean bExitApplication, String strMessage, Object... aobj) {
		super(DebugI.i().joinMessageWithObjects(strMessage,aobj));
		
		if(bExitApplication && (userInputDetector!=null && !userInputDetector.isConsoleCommandRunningFromDirectUserInput())){
			PrerequisitesNotMetException.exRequestExit = this;
		}
	}
	public PrerequisitesNotMetException(String str, Object... aobj) {
		this(true,str,aobj);
	}
	
	public static void setUserInputDetector(IUserInputDetector u){
		userInputDetector = u;
	}
	
	public static boolean isExitRequested(){
		return exRequestExit!=null;
	}
	
	public static PrerequisitesNotMetException getExitRequestCause(){
		return exRequestExit;
	}
	
	public static void assertNotEmpty(String strDescWhat, String str, Object... aobjMoreObjectsForDebugInfo){
		assertNotNull(strDescWhat, str, aobjMoreObjectsForDebugInfo);
		
		if(str.isEmpty()){
			ArrayList<Object> aobjAll = new ArrayList<Object>();
			aobjAll.add(str);
			aobjAll.addAll(Arrays.asList(aobjMoreObjectsForDebugInfo));
			
			throw new PrerequisitesNotMetException(strDescWhat+": is empty", aobjAll.toArray());
		}
	}
	public static void assertNotNull(String strDescWhat, Object obj, Object... aobjMoreObjectsForDebugInfo){
		if(obj==null){
			ArrayList<Object> aobjAll = new ArrayList<Object>();
			aobjAll.add(obj);
			aobjAll.addAll(Arrays.asList(aobjMoreObjectsForDebugInfo));
			
			throw new PrerequisitesNotMetException(strDescWhat+": is null", aobjAll.toArray());
		}
	}
	
	public static <T> void assertNotAlreadyAdded(ArrayList<T> aList, T objNew, Object... aobj){
		String strMsg=null;
		if(objNew==null){
			strMsg="cant be null";
		}
		
		if(aList.contains(objNew)){
			strMsg="already added";
		}
		
		if(strMsg!=null){
			ArrayList<Object> aobjAll = new ArrayList<Object>();
			aobjAll.add(aList);
			aobjAll.add(objNew);
			aobjAll.addAll(Arrays.asList(aobj));
			
			throw new PrerequisitesNotMetException("already added", aobjAll);
		}
	}
	
	public static void assertIsTrue(String strDescWhat, boolean b, Object... aobjMoreObjectsForDebugInfo){
		if(!b){
			throw new PrerequisitesNotMetException("NOT "+strDescWhat+"!", aobjMoreObjectsForDebugInfo);
		}
	}
	
	public static void assertNotAlreadySet(String strDescWhat, Object objCurrent, Object objNew, Object... aobjMoreObjectsForDebugInfo){
		if(objCurrent!=null){
			ArrayList<Object> aobjAll = new ArrayList<Object>();
			aobjAll.add(objCurrent);
			aobjAll.add(objNew);
			aobjAll.addAll(Arrays.asList(aobjMoreObjectsForDebugInfo));
			
			throw new PrerequisitesNotMetException(strDescWhat+": already set", aobjAll.toArray());
		}
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
	
	@Deprecated
	@Override
	public synchronized Throwable initCause(Throwable cause) {
		throw new NullPointerException("do not use, just to avoid ignoring the more useful ones");
	}
	
}
