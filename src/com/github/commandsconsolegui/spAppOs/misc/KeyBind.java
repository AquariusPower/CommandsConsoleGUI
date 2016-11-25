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
package com.github.commandsconsolegui.spAppOs.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.github.commandsconsolegui.spAppOs.ManageKeyCode.Key;
import com.github.commandsconsolegui.spAppOs.globals.GlobalManageKeyCodeI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalSimulationTimeI;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class KeyBind {
	
	/** the last/main key to be pressed */
	private Key keyAction = null;
	
	private ArrayList<Key> akeyModifierList;// = new ArrayList<Key>();

	private Object	objOwner;
	
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
	
	public void setOwner(Object obj){
		PrerequisitesNotMetException.assertNotAlreadySet(this.objOwner, obj, "owner", this);
		this.objOwner=obj;
	}
	
	public boolean isActivated(){
		if(!keyAction.isPressed())return false;
		
		if(akeyModifierList!=null){
			for(Key key:akeyModifierList){
				if(!key.isPressed())return false;
			}			
		}
		
		return true;
	}
	
	public Key getActionKey(){
		return keyAction;
	}
	
	public void addModifier(String... astrKeyId){
		for(String strId:astrKeyId){
			addModifier(GlobalManageKeyCodeI.i().getKeyForId(strId));
		}
	}
	public void addModifier(int... aiKeyCode){
		for(Integer iKeyCode:aiKeyCode){
			addModifier(GlobalManageKeyCodeI.i().getFirstKeyForCode(iKeyCode));
		}
	}
	public void addModifier(Key... akey){
//		akeyModifierList.addAll(Arrays.asList(akey));
		if(akeyModifierList==null)akeyModifierList = new ArrayList<Key>();
		
		for(Key key:akey){
			PrerequisitesNotMetException.assertNotNull(key, "mod key", this);
			akeyModifierList.add(key);
		}
	}
	
	public ArrayList<Key> getKeyModifiersCopy(){
		if(akeyModifierList==null)return null;
		return new ArrayList<Key>(akeyModifierList);
	}
	
	public void setActionKey(Key key) {
		PrerequisitesNotMetException.assertNotAlreadySet(this.keyAction, key, "action key", this);
		PrerequisitesNotMetException.assertNotNull(key, "action key", this);
		
		this.keyAction=key;
	}
	
	public void setActionKey(String strId) {
		setActionKey(GlobalManageKeyCodeI.i().getKeyForId(strId));
	}
	public void setActionKey(int iKeyCode) {
		setActionKey(GlobalManageKeyCodeI.i().getFirstKeyForCode(iKeyCode));
	}
	
	/**
	 * last one is action key
	 * @return
	 */
	public Integer[] getAllKeyCodes(){
		if(akeyModifierList==null){
			return new Integer[]{keyAction.getKeyCode()};
		}
		
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
		setActionKey(GlobalManageKeyCodeI.i().getFirstKeyForCode(ai[ai.length-1])); //last
		
		for(int i=0;i<ai.length-1;i++){ //least last
			addModifier(ai[i]);
		}
		
		return this;
	}

	public KeyBind setFromKeyIds(String[] astr) {
		setActionKey(GlobalManageKeyCodeI.i().getKeyForId(astr[astr.length-1])); //last
		
		for(int i=0;i<astr.length-1;i++){ //least last
			addModifier(astr[i]);
		}
		
		return this;
	}

	public String getBindCfg(){
		String str="";
		
		if(akeyModifierList!=null){
			for(Key key:akeyModifierList){
				String strId=key.getSimpleId();
				str+=strId+"+";
	//			str+=GlobalManageKeyCodeI.i().getKeyIdFromCode(key.getKeyCode())+"+";
			}
		}
		
		str+=getActionKey().getSimpleId(); //GlobalManageKeyCodeI.i().getKeyIdFromCode(getActionKey().getKeyCode());
		
		return str;
	}

	public boolean isHasKeyModifiers() {
		return akeyModifierList!=null;
	}
	
	public int getKeyModListSize(){
		return akeyModifierList!=null ? akeyModifierList.size() : 0;
	}
	
	/**
	 * expected syntax ex:
	 * LCONTROL+RSHIFT+LMENU+F1 (specific mod keys)
	 * Ctrl+Alt+k (grouped mod keys)
	 * 
	 * @param strCfg
	 * @return
	 */
	public KeyBind setFromKeyCfg(String strCfg) {
		return setFromKeyIds(strCfg.split("[+]"));
	}

	/** consecutive activation limit*/
	private long lActLim = 1; //run once
	/** Consecutive Activation Interval Milis */
	private long lActDelayMilis = 0; //every frame
	
	public void setRepeatedActivation(){
		this.lActLim=0; //no limit
		this.lActDelayMilis=0; //every frame
	}
	public void setRepeatedActivation(long lDelayMilis){
		this.lActLim=0; //no limit
		this.lActDelayMilis=lDelayMilis;
	}
	public void setRepeatedActivation(long lLimit, long lDelayMilis){
		this.lActLim=lLimit;
		this.lActDelayMilis=lDelayMilis;
	}
	
	private long lConsecutiveActivationCount = 0;
	private long lLastActivationMilis=-1;

	public boolean isCanBeRunNowOrReset() {
		if(isActivated()){
			boolean bRun=false;
			
			if(lActLim>0){
				if(lConsecutiveActivationCount<lActLim){
					if(lConsecutiveActivationCount==0){ //no delay for 1st time
						bRun=true;
					}else{
						if(lActDelayMilis==0){ //every frame
							bRun=true;
						}else{
							if(GlobalSimulationTimeI.i().getMilis() > (lLastActivationMilis+lActDelayMilis)){
								bRun=true;
							}
						}
					}
				}
			}
			
			lLastActivationMilis = GlobalSimulationTimeI.i().getMilis();
			lConsecutiveActivationCount++;
			
			return bRun;
		}else{
			//reset
			lConsecutiveActivationCount=0;
			lLastActivationMilis=-1;
			
			return false;
		}
	}
	
//	public boolean isActivated(){
//		return getValue().isActivated();
//	}
	
	/**
	 * was already activated at least once
	 * @return
	 */
//	public boolean isWaitingDeactivation(){
	public boolean isWasAlreadyActivatedAtLeastOnce(){
		return lLastActivationMilis!=-1;
	}
	
//	public Comparator<KeyBind> getComparatorById(){
//		return new Comparator<KeyBind>() {
//			@Override
//			public int compare(KeyBind o1, KeyBind o2) {
//				return o1.get;
//			}
//		};
//	}
	
	public boolean isEquivalentTo(KeyBind kbOther) {
		if(getActionKey().getId().equals(kbOther.getActionKey().getId())){
			if(getKeyModListSize()==kbOther.getKeyModListSize()){
				if(getKeyModListSize()==0)return true;
				
				ArrayList<Key> akeyModOther = kbOther.getKeyModifiersCopy();
				for(Key keyMod:getKeyModifiersCopy()){
					if(akeyModOther.contains(keyMod))akeyModOther.remove(keyMod);
					if(akeyModOther.size()==0)return true;
				}
			}
		}
		
		return false;
	}
	
}
