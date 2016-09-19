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

package com.github.commandsconsolegui.jmegui.savablevalues;

import java.io.IOException;

import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class SaveFloat extends SaveValueAbs<Float,SaveFloat> {
	public SaveFloat() { //required by Savable loading procedure
		super();
	}
	
	public SaveFloat(IReflexFillCfg rfcfgOwner, Float v) {
		super(rfcfgOwner);
		super.setValue(v);
	}
	
	@Override
	public SaveFloat getThis() {
		return this;
	}
	
	@Override
	public String getCodePrefixDefault() {
		return "svf";
	}

	@Override
	public String getCodePrefixVariant() {
		return getCodePrefixDefault();
	}

	@Override
	protected void saveAt(OutputCapsule oc) throws IOException {
//		oc.write(getValue(), getUniqueId(), getDefaultValue());
		oc.write(getValue(), E.value.s(), getDefaultValue());
	}

	@Override
	protected Float loadFrom(InputCapsule ic) throws IOException {
//		return ic.readFloat(getUniqueId(), getDefaultValue());
		return ic.readFloat(E.value.s(), Float.NaN); //NAN is a way to indicate that it failed to load/find the name on the file 
	}
	
}
