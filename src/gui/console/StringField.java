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
//	String str = "ERROR: NOT SET"; // hashcode depends on it not being null
	protected String strValue = null;
	protected IReflexFillCfg rfcfgOwner;
//	protected int	iReflexFillCfgVariant;
	protected String	strReflexFillCfgCodePrefixVariant;
	
//	public StringField(IReflexFillCfg rfcfgOwner){
//		this(rfcfgOwner,0);
//	}
	
	/**
	 * The value cannot be prepared at the constructor, 
	 * as it has not returned yet, so it's object owner will not have 
	 * a valid field (will still be null).
	 */
	public StringField(IReflexFillCfg rfcfgOwner, String strReflexFillCfgCodePrefixVariant){ // int iReflexFillCfgVariant){
//		this.iReflexFillCfgVariant=iReflexFillCfgVariant;
		this.strReflexFillCfgCodePrefixVariant = strReflexFillCfgCodePrefixVariant;
		this.rfcfgOwner=rfcfgOwner;
		
		if(this.rfcfgOwner==null){
			throw new NullPointerException("cant be null for: "+IReflexFillCfg.class.getName());
		}
	}
	public StringField(String strValue){
		this.strValue=strValue;
	}
	
	@Override
	public String toString() {
		if(strValue==null)initialize();
		return this.strValue;
	}
	
	protected void initialize(){
		/**
		 * This basically prevents recursive infinite loop,
		 * if this is called at reflex fill method.
		 */
		strValue=errorMessage();
		this.strValue = ReflexFill.i().createIdentifierWithFieldName(this.rfcfgOwner, this);
//		throw new NullPointerException("not initialized properly: "+this);
	}
	
	private String errorMessage(){
		return "ERROR: "+StringField.class.getName()+" not yet properly initialized!";
	}
	
	@Override
	public boolean equals(Object obj) {
//		if(strValue==null)throw new NullPointerException(errorMessage());
		if(strValue==null)initialize();
		return this.strValue.equals(obj);
	}
	
	@Override
	public int hashCode() {
//		if(strValue==null)throw new NullPointerException(errorMessage());
		if(strValue==null)initialize();
		return strValue.hashCode();
	}
	
//	@Override
//	public int getReflexFillCfgVariant() {
//		return iReflexFillCfgVariant;
//	}

	@Override
	public String getCodePrefixVariant() {
		return strReflexFillCfgCodePrefixVariant;
	}
}
