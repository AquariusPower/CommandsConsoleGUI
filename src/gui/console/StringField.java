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

package gui.console;

import gui.console.ReflexFill.IReflexFillCfg;
import gui.console.ReflexFill.IReflexFillCfgVariant;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class StringField implements IReflexFillCfgVariant{
	String str;
	IReflexFillCfg rfcfgOwner;
	private int	iReflexFillCfgVariant;
	
	public StringField(IReflexFillCfg rfcfgOwner){
		this(rfcfgOwner,0);
	}
	public StringField(IReflexFillCfg rfcfgOwner, int iReflexFillCfgVariant){
		this.iReflexFillCfgVariant=iReflexFillCfgVariant;
		if(rfcfgOwner==null)throw new NullPointerException("cant be null for: "+IReflexFillCfg.class.getName());
		this.rfcfgOwner=rfcfgOwner;
	}
	public StringField(String str){
		this.str=str;
	}
	
	@Override
	public String toString() {
		if(this.str==null)this.str=ReflexFill.i().createIdentifierWithFieldName(rfcfgOwner, this);
		return this.str;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.str.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return str.hashCode();
	}
	
	@Override
	public int getReflexFillCfgVariant() {
		return iReflexFillCfgVariant;
	}
}
