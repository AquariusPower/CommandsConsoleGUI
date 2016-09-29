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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class AliasData{
	String strAliasId;
	String strCmdLine; // may contain ending comment too
	boolean	bBlocked;
	CommandsDelegator cc;
	
	public AliasData(CommandsDelegator cc){
		this.cc=cc;
	}
	
	@Override
	public String toString() {
		return cc.getCommandPrefix()+"alias "
			+(bBlocked?cc.getAliasBlockedToken():"")
			+strAliasId+" "+strCmdLine;
	}
	
	@Override
	public boolean equals(Object obj) {
		/**
		 * TODO if you need to comprare something, do an specific method to compare, isIdEqual() etc... do not mess with default object equals() !!
		 */
		GlobalCommandsDelegatorI.i().dumpDevWarnEntry("this method is really necessary? or can be removed?",this);
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AliasData other = (AliasData) obj;
		if (bBlocked != other.bBlocked)
			return false;
//		if (cc == null) {
//			if (other.cc != null)
//				return false;
//		} else if (!cc.equals(other.cc))
//			return false;
		if (strAliasId == null) {
			if (other.strAliasId != null)
				return false;
		} else if (!strAliasId.equals(other.strAliasId))
			return false;
		if (strCmdLine == null) {
			if (other.strCmdLine != null)
				return false;
		} else if (!strCmdLine.equals(other.strCmdLine))
			return false;
		return true;
	}
	
	/**
	 * This hashcode is useful to detect if any alias on the list has changed its internal fields,
	 * and therefore, made the list different itself.
	 * So, the hash of the list should be based on the hash of the aliase's field's values, and not 
	 * on the Object hash that varies.
	 * TODO tho, the list and this alias could be checked by ex.: a report md5sum or something like that...
	 * TODO verify why the list really needs this?
	 * TODO make an alias list change checker (that does exactly what this code does) and do not touch default hash() and equals()?
	 */
	@Override
	public int hashCode() {
		GlobalCommandsDelegatorI.i().dumpDevWarnEntry("can this method not be overriden? or is it that useful?",this);
//		try {
//			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("usages ST"+
//				MessageDigest.getInstance("MD5").digest(
//					Thread.currentThread().getStackTrace().toString().getBytes()),this);
//		} catch (NoSuchAlgorithmException e) {}
		
		final int prime = 31;
		int result = 1;
		result = prime * result + (bBlocked ? 1231 : 1237);
//		result = prime * result + ((cc == null) ? 0 : cc.hashCode());
		result = prime * result
				+ ((strAliasId == null) ? 0 : strAliasId.hashCode());
		result = prime * result
				+ ((strCmdLine == null) ? 0 : strCmdLine.hashCode());
		return result;
	}

}
