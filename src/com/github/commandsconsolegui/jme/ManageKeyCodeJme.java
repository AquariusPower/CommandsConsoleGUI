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
package com.github.commandsconsolegui.jme;

import com.github.commandsconsolegui.ManageKeyCode;
import com.github.commandsconsolegui.misc.KeyBind;
import com.jme3.input.KeyInput;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageKeyCodeJme extends ManageKeyCode{
	@Override
	public void configure() {
  	fillKeyIdCodeFrom(KeyInput.KEY_ESCAPE, KeyInput.class, "KEY_");
		super.configure();
	}
	
	enum EKeyMod{
		Ctrl,
		Alt,
		Shift,
		;
		
		private Key	key;
		
		private void setKey(Key key){
			this.key=key;
		}
		
		public String s(){return toString();}
		
		public static boolean isModKey(Key key){
			for(EKeyMod e:values()){
				if(e.key==key)return true;
				if(e.key.isMonitoredKey(key))return true;
			}
			return false;
		}
	}
	
	@Override
	public void addSpecialKeys() {
		super.addSpecialKeys();
		
		EKeyMod.Ctrl.key	=addKey(EKeyMod.Ctrl.s(),	KeyInput.KEY_LCONTROL,KeyInput.KEY_RCONTROL);
		EKeyMod.Alt.key		=addKey(EKeyMod.Alt.s(),	KeyInput.KEY_LMENU,		KeyInput.KEY_RMENU);
		EKeyMod.Shift.key	=addKey(EKeyMod.Shift.s(),KeyInput.KEY_LSHIFT,	KeyInput.KEY_RSHIFT);
	}

	@Override
	public KeyBind getPressedKeysAsKeyBind() {
		String strCfg = "";
		for(EKeyMod e:EKeyMod.values()){
			if(getKeyForId(e.s()).isPressed())strCfg+=e.s()+"+";
		}
		
		for(Key key:getKeyList()){
			if(EKeyMod.isModKey(key))continue;
			
			if(key.isPressed()){
				strCfg+=key.getSimpleId();
				return new KeyBind().setFromKeyCfg(strCfg);
			}
		}
		
		return null;
	}
	
}
