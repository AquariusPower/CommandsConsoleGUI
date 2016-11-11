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

import com.github.commandsconsolegui.cmd.ManageKeyCodeI;
import com.github.commandsconsolegui.cmd.ManageKeyCodeI.Key;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class KeyBind {
	
	/** the last/main key to be pressed */
	private Key keyAction = null;
	
	private ArrayList<Key> akeyModifierList = new ArrayList<Key>();
	
//	private void applyPressedState(Key key, int iKeyCodeCheck, boolean bPressed){
//		if(key.getKeyCode()==iKeyCodeCheck)key.bPressed=(bPressed);
//	}
//	public void applyPressedState(int iKeyCode, boolean bPressed){
//		applyPressedState(keyAction, iKeyCode, bPressed);
//		
//		for(Key key:akeyModifierList){
//			applyPressedState(key, iKeyCode, bPressed);
//		}
//	}
	
	public boolean isActivated(){
		if(!keyAction.isPressed())return false;
		
		for(Key key:akeyModifierList){
			if(!key.isPressed())return false;
		}			
		
		return true;
	}
	
	public Key getActionKey(){
		return keyAction;
	}
	
	public void addModifier(String... astrKeyId){
		for(String strId:astrKeyId){
			addModifier(ManageKeyCodeI.i().getKeyForId(strId));
		}
	}
	public void addModifier(int... aiKeyCode){
		for(Integer iKeyCode:aiKeyCode){
			addModifier(ManageKeyCodeI.i().getFirstKeyForCode(iKeyCode));
		}
	}
	public void addModifier(Key... akey){
//		akeyModifierList.addAll(Arrays.asList(akey));
		for(Key key:akey){
			PrerequisitesNotMetException.assertNotNull("mod key", key, this);
			akeyModifierList.add(key);
		}
	}
	
	public ArrayList<Key> getModifiers(){
		return new ArrayList<Key>(akeyModifierList);
	}
	
	public void setActionKey(Key key) {
		PrerequisitesNotMetException.assertNotAlreadySet("action key", this.keyAction, key, this);
		PrerequisitesNotMetException.assertNotNull("action key", key, this);
		
		this.keyAction=key;
	}
	
	public void setActionKey(String strId) {
		setActionKey(ManageKeyCodeI.i().getKeyForId(strId));
	}
	public void setActionKey(int iKeyCode) {
		setActionKey(ManageKeyCodeI.i().getFirstKeyForCode(iKeyCode));
	}
	
	/**
	 * last one is action key
	 * @return
	 */
	public Integer[] getAllKeyCodes(){
		Integer[] ai = new Integer[akeyModifierList.size()+1];
		
		int i=0;
		for(Key key:akeyModifierList){
			ai[i++]=key.getKeyCode();
		}
		ai[i]=keyAction.getKeyCode(); //last
		
		return ai;
	}
	
	/**
	 * last one is action key
	 * @return 
	 * @return
	 */
	public KeyBind setFromKeyCodes(Integer... ai){
		setActionKey(ManageKeyCodeI.i().getFirstKeyForCode(ai[ai.length-1])); //last
		
		for(int i=0;i<ai.length-1;i++){ //least last
			addModifier(ai[i]);
		}
		
		return this;
	}

	public KeyBind setFromKeyIds(String[] astr) {
		setActionKey(ManageKeyCodeI.i().getKeyForId(astr[astr.length-1])); //last
		
		for(int i=0;i<astr.length-1;i++){ //least last
			addModifier(astr[i]);
		}
		
		return this;
	}

	public String getBindCfg(){
		String str="";
		
		for(Key key:getModifiers()){
			str+=ManageKeyCodeI.i().getKeyId(key.getKeyCode())+"+";
		}
		
		str+=ManageKeyCodeI.i().getKeyId(getActionKey().getKeyCode());
		
		return str;
	}
}
