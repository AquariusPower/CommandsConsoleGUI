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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class MsgI {
	private static MsgI instance = new MsgI();
	public static MsgI i(){return instance;}
	
	public static boolean bDebug=false;
	
	/**
	 * 
	 * @param str
	 * @param bSuccess
	 * @param objOwner use "this"
	 */
	public void dbg(String str, boolean bSuccess, Object objOwner){
		if(!bDebug)return;
		
		if(!bSuccess){
			int i,i2=0;
			i=i2;i2=i;
		}
		
		System.err.println(MsgI.class.getName()+":DBG: "
			+new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()))+": "
			+(objOwner!=null ? objOwner.getClass().getName()+": " : "")
			+(bSuccess?"ok":"FAIL")+": "
			+str);
	}
	
	public void warn(String str, Object... aobj){
		System.err.println(MsgI.class.getName()+":WARN: "
			+new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()))+": "
			+str+": "
			+aobj);
	}
	
}
