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

package com.github.commandsconsolegui.cmd;

import java.util.Arrays;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ImportantMsgData{
	String strMsg;
	Exception ex;
	StackTraceElement[] aste;
	
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
	public ImportantMsgData(String str, Exception ex, StackTraceElement[] aste) {
		this.strMsg=str;
//		if(ex==null){
//			ex=new Exception("(no real exception, just the stack trace)");
//			ex.setStackTrace(aste);
//		}
		this.ex=ex;
		this.aste=aste;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(aste);
		result = prime * result + ((strMsg == null) ? 0 : strMsg.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImportantMsgData other = (ImportantMsgData) obj;
		if (!Arrays.equals(aste, other.aste))
			return false;
		if (strMsg == null) {
			if (other.strMsg != null)
				return false;
		} else if (!strMsg.equals(other.strMsg))
			return false;
		return true;
	}
	
}
