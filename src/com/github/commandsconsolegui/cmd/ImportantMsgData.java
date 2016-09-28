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

package com.github.commandsconsolegui.cmd;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 
 * TODO this is basically a DumpEntryData holder, make it be like that.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ImportantMsgData {
	String strMsgKey;
//	long lKeyOcurrenceTimes=0;
	Exception ex;
	StackTraceElement[] asteExceptionHappenedAt;
	DumpEntryData de;
	Long lBufferedTimeNano;
	Long lFirstOcurrenceCreationTimeNano;
	
//	/**
//	 * for warnings
//	 * @param str
//	 * @param aste
//	 */
//	public ImportantMsg(String str, StackTraceElement[] aste) {
//		this(str, null, aste);
//	}
//	/**
//	 * for exceptions
//	 * @param str
//	 * @param ex
//	 */
//	public ImportantMsg(String str, Exception ex) {
//		this(str, ex, ex.getStackTrace());
//	}
	
//	public ImportantMsgData(String str, Exception ex, StackTraceElement[] aste) {
//		this.strMsg=str;
////		if(ex==null){
////			ex=new Exception("(no real exception, just the stack trace)");
////			ex.setStackTrace(aste);
////		}
//		this.ex=ex;
//		this.aste=aste;
//	}
	
	public ImportantMsgData(DumpEntryData de) {
//		this(de.getKey(),de.getException(),de.getException().getStackTrace());
		this.de=de;
		
		this.strMsgKey=de.getKey();
//	if(ex==null){
//		ex=new Exception("(no real exception, just the stack trace)");
//		ex.setStackTrace(aste);
//	}
		this.ex=de.getException();
		this.asteExceptionHappenedAt=de.getException().getStackTrace();
		
		lFirstOcurrenceCreationTimeNano=System.nanoTime();
	}
	
	public boolean isIdenticalTo(ImportantMsgData imdOther){
		if (!strMsgKey.equals(imdOther.strMsgKey))return false; //equals() is faster, and such messages will not have string letter case difference...
		if (!Arrays.equals(asteExceptionHappenedAt, imdOther.asteExceptionHappenedAt))return false;
		
		return true;
	}
	
	public DumpEntryData getDumpEntryData(){
		return de;
	}
	
	public ImportantMsgData updateBufferedTime(){
		lBufferedTimeNano = System.nanoTime();
		return this;
	}
	
	public long getBufferedTime() {
		return lBufferedTimeNano;
	}
	
	public static Comparator<? super ImportantMsgData> cmpFirstOcurrenceCreationTime() {
		return new Comparator<ImportantMsgData>() {
			@Override
			public int compare(ImportantMsgData o1, ImportantMsgData o2) {
				return o1.lFirstOcurrenceCreationTimeNano.compareTo(o2.lFirstOcurrenceCreationTimeNano);
			}
		};
	}

	public static Comparator<? super ImportantMsgData> cmpBufferedTime() {
		return new Comparator<ImportantMsgData>() {
			@Override
			public int compare(ImportantMsgData o1, ImportantMsgData o2) {
				return o1.lBufferedTimeNano.compareTo(o2.lBufferedTimeNano);
			}
		};
	}

	public void applyFirstOcurrenceCreationTimeFrom(ImportantMsgData imsg) {
		this.lFirstOcurrenceCreationTimeNano=imsg.lFirstOcurrenceCreationTimeNano;
		de.incKeyOcurrenceTimes();
	}
}

