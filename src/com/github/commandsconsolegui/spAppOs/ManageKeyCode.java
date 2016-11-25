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
package com.github.commandsconsolegui.spAppOs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.github.commandsconsolegui.spAppOs.globals.GlobalManageKeyCodeI;
import com.github.commandsconsolegui.spAppOs.misc.KeyBind;
import com.github.commandsconsolegui.spAppOs.misc.ManageConfigI.IConfigure;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;

/**
 * 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class ManageKeyCode implements IConfigure<ManageKeyCode>{
//	private static ManageKeyCodeI instance = new ManageKeyCodeI();
//	public static ManageKeyCodeI i(){return instance;}
	
	public static class Key{
		private String strId;
		private Integer iKeyCode = null;
		private boolean bPressed = false;
		ArrayList<Key> akeyMonitoredList = null;
		
//		private Key(Integer iKeyCode) {
//			this(ManageKeyCodeI.i().getKeyId(iKeyCode), iKeyCode);
//		}
		
		/**
		 * Must remain private, the scope of available keys is limited and must be managed here!
		 * @param strId
		 * @param iKeyCode
		 */
		private Key(String strId, Integer iKeyCode) {
			this(strId);
			
			PrerequisitesNotMetException.assertNotNull(iKeyCode, "code", this);
			PrerequisitesNotMetException.assertIsTrue("invalid negative keycode", iKeyCode>=0, this);
			
			this.iKeyCode=iKeyCode;
		}
		
		/**
		 * Even end user could set this.
		 * @param strId
		 * @param akeyToMonitor if any of these keys is pressed, this "key group reference" will return as pressed
		 */
		public Key(String strId, Key... akeyToMonitor) {
			this(strId);
			
			PrerequisitesNotMetException.assertNotNull(akeyToMonitor, "keys to monitor", this);
			PrerequisitesNotMetException.assertIsTrue("keys to monitor list has items", akeyToMonitor.length>0, this);
			
			addKeysToMonitor(akeyToMonitor);
		}
		
		private Key(String strId){
			PrerequisitesNotMetException.assertNotEmpty("id", strId, this);
			this.strId=strId;
		}
		
		public ArrayList<Key> getKeysToMonitorCopy(){
			return new ArrayList<Key>(akeyMonitoredList);
		}
		
		public boolean isModeKeyGroupMonitor(){
			return akeyMonitoredList!=null;
		}
		public boolean isModeKeyWithCode(){
			return iKeyCode!=null;
		}
		
		public boolean addKeysToMonitor(Key... akeyToMonitor){
			return workKeysMonitor(true,akeyToMonitor);
		}
		public boolean removeKeysFromMonitor(Key... akeyToMonitor){
			return workKeysMonitor(false,akeyToMonitor);
		}
		private boolean workKeysMonitor(boolean bAdd, Key... akeyToMonitor){
			if(iKeyCode!=null){
				throw new PrerequisitesNotMetException("this key is NOT a group reference!", this, iKeyCode, akeyToMonitor);
			}
			
			if(this.akeyMonitoredList==null)this.akeyMonitoredList = new ArrayList<Key>();
			
			int iCount=0;
			for(Key key:akeyToMonitor.clone()){
				if(key==null)continue;
				
				if(bAdd){
					if(this.akeyMonitoredList.contains(key)){
						MsgI.i().devWarn("already contains", this, key);
						continue;
					}
					
					this.akeyMonitoredList.add(key);
					iCount++;
				}else{
					this.akeyMonitoredList.remove(key);
					iCount++;
				}
			}
			
			return iCount>0;
		}
		
		public Integer getKeyCode() {
			return iKeyCode;
		}

		public boolean isPressed() {
			if(akeyMonitoredList!=null){
				for(Key key:akeyMonitoredList){
					if(key.isPressed())return true;
				}
				return false;
			}
			
			return bPressed;
		}
		private void setPressed(boolean bPressed) {
			if(akeyMonitoredList!=null)throw new PrerequisitesNotMetException("this key is a group reference, cannot be directly pressed...", this, akeyMonitoredList, bPressed);
			this.bPressed = bPressed;
		}
		
		public String getSimpleId(){
			String strIdSimple = strId;
			
			String strPrefix=GlobalManageKeyCodeI.i().getKeyIdPrefixFilter();
			if(strPrefix!=null){
				if(strIdSimple.startsWith(strPrefix)){
					strIdSimple=strIdSimple.substring(strPrefix.length());
				}
			}
			
			return strIdSimple;
		}
		
		public String getId() {
			return strId;
		}

		public boolean isMonitoredKey(Key key) {
			return akeyMonitoredList.contains(key);
		}
	}
	
	private ArrayList<Key> akeyList = new ArrayList<Key>();
	private TreeMap<String,Key> tmKey = new TreeMap<String,Key>(String.CASE_INSENSITIVE_ORDER);
	private TreeMap<Integer,HashMap<String,Key>> tmCodeKeys = new TreeMap<Integer,HashMap<String,Key>>();
	private String	strKeyIdPrefixFilter;
	private int	iKeyCodeForEscape=-1;
	private int	iKeyCodeForEnter=-1;
	private boolean	bConfigured;
	
	public Key addKey(String strId, Integer... aiCodeToMonitor){
		ArrayList<Key> akey = new ArrayList<Key>();
		for(Integer iKeyCode:aiCodeToMonitor){
			akey.add(getFirstKeyForCode(iKeyCode));
		}
		
		return addKey(strId, akey.toArray(new Key[0]));
	}
	public Key addKey(String strId, Key... akeyToMonitor){
		return addKeyWorkFull(strId,null,akeyToMonitor);
	}
	
	/**
	 * 
	 * @param strId
	 * @param iCode
	 * @return true if added or already set with same code, false if already set with different code
	 */
	private Key addKeyWorkFull(String strId, Integer iCode, Key... akeyToMonitor){
		if(iCode!=null && akeyToMonitor.length>0){
			throw new PrerequisitesNotMetException("both params were set...", iCode, akeyToMonitor);
		}
		
		Key keyExisting = tmKey.get(strId);
		if(keyExisting!=null){
			if(keyExisting.isModeKeyWithCode()){
				Integer iCodeExisting = keyExisting.getKeyCode();
				if(iCodeExisting==iCode){
					MsgI.i().devWarn("already set", strId, iCode);
					return keyExisting;
				}else{
					MsgI.i().devWarn("cannot modify the code for", strId, iCodeExisting, iCode);
					return null;
				}
			}
		}
		
		if(iCode!=null){
			for(Entry<String,Key> entry:tmKey.entrySet()){
				if(entry.getValue().getKeyCode()==iCode){
					String strExistingId=entry.getKey();
					/**
					 * The source of ids and codes may contain more than one id with the same code.
					 * A new source (class) may also contain new ids for already setup codes.
					 */
					MsgI.i().devWarn("(multiplicity) already contains code", strExistingId, iCode, strId);
				}
			}
			
			Key keyNew = new Key(strId,iCode);
			PrerequisitesNotMetException.assertNotAlreadySet(tmKey.get(strId), keyNew, "key id", strId, iCode, this);
			tmKey.put(strId, keyNew);
			akeyList.add(keyNew);
			
			/**
			 * populate list of keys with the same code
			 */
			HashMap<String,Key> hm = tmCodeKeys.get(iCode);
			if(hm==null){
				hm=new HashMap<String,Key>();
				tmCodeKeys.put(iCode,hm);
			}
			hm.put(keyNew.getId(), keyNew);
			
			return keyNew;
		}else
		if(akeyToMonitor.length>0){
			if(keyExisting!=null && !keyExisting.isModeKeyGroupMonitor()){
				MsgI.i().devWarn("existing is not a key group monitor", keyExisting);
				return null;
			}
			
			Key keyGroupMonitor = keyExisting;
			if(keyGroupMonitor==null){
				keyGroupMonitor = new Key(strId);
			}
			
			if(keyGroupMonitor.addKeysToMonitor(akeyToMonitor)){
				tmKey.put(strId, keyGroupMonitor);
			}else{
				MsgI.i().devWarn("no key added to monitoring", strId, akeyToMonitor);
				return null;
			}
			
			return keyGroupMonitor;
		}else{
			throw new PrerequisitesNotMetException("both are not set", iCode, akeyToMonitor);
		}
		
	}
	
	protected ArrayList<Key> getKeyList(){
		return akeyList;
	}
	public ArrayList<Key> getKeyListCopy(){
//		if(tmKey.size()!=akeyList.size())
		return new ArrayList<Key>(akeyList);
	}
	
//	public ArrayList<Key> getKeyListCopy() {
//		ArrayList<Key> akeyList = new ArrayList<Key>();
//		for(Entry<String,Key> entry:tmKey.entrySet()){
//			akeyList.add(entry.getValue());
//		}
//		return akeyList;
//	}
	
	public ArrayList<String> getKeyCodeListReport(){
		ArrayList<String> astr = new ArrayList<String>();
		for(Key key:getKeyListCopy()){
			astr.add(key.getId()+"="+key.getKeyCode());
		}
		Collections.sort(astr);
		return astr;
	}
	
	public int getKeyCodeForReturn() {
		return iKeyCodeForEnter;
	}
	public int getKeyCodeForEscape(){
		return iKeyCodeForEscape;
	}
	
	public String getKeyIdPrefixFilter() {
		return strKeyIdPrefixFilter;
	}
	
	/** 
	 * TODO should this be allowed to be called only once? other classes without conflicts would be no problem tho...
	 * @param iKeyCodeForEscape to setup a generic cancel fail-safe key
	 * @param iKeyCodeForReturn to setup a generic accept fail-safe key
	 * @param cl
	 * @param strKeyIdPrefixFilter can be null
	 * @return
	 */
	public boolean fillKeyIdCodeFrom(int iKeyCodeForEscape, int iKeyCodeForReturn, Class<?> cl, String strKeyIdPrefixFilter){
		this.strKeyIdPrefixFilter=strKeyIdPrefixFilter;
		
		this.iKeyCodeForEscape=iKeyCodeForEscape;
		this.iKeyCodeForEnter=iKeyCodeForReturn;
		
//		if(tmIdCode.size()>0){			return;		}
		try {
			int iMaxCode=-1;
			for(Field fld:cl.getFields()){
				if(strKeyIdPrefixFilter!=null && !strKeyIdPrefixFilter.isEmpty()){
					if(!fld.getName().startsWith(strKeyIdPrefixFilter))continue;
				}
				
				int iCode=(Integer)fld.get(null);
				if(iCode>iMaxCode)iMaxCode=iCode;
				
				String strId=fld.getName();//.substring(4); //removes the KEY_ prefix
				
				if(addKeyWorkFull(strId,iCode)==null){
					throw new PrerequisitesNotMetException("keycode filling failed",strId,iCode,cl,strKeyIdPrefixFilter);
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new PrerequisitesNotMetException("unexpected").setCauseAndReturnSelf(e);
		}
		
		return true;
	}
	
	public Integer getKeyCodeFromId(String strId){
		Key key = tmKey.get(strId);
		if(key.isModeKeyWithCode())return key.getKeyCode();
		return null;
	}
	
	public String getKeyIdFromCode(int iCode){
		for(Entry<String,Key> entry:tmKey.entrySet()){
			Key key = entry.getValue();
			if(!key.isModeKeyWithCode())continue;
			if(key.getKeyCode()==iCode)return key.getId();
		}
		return null;
	}
	
	public void refreshPressedState(String strKeyId, boolean bPressed){
		tmKey.get(strKeyId).setPressed(bPressed);
	}
	
	public void refreshPressedState(int iKeyCode, boolean bPressed){
		for(Key key:tmCodeKeys.get(iKeyCode).values()){
			key.setPressed(bPressed);
		}
	}
	
	/**
	 * @param iKeyCode
	 * @return the first key (id) assigned to the specified keyCode
	 */
	public Key getFirstKeyForCode(Integer iKeyCode) {
		HashMap<String, Key> hm = tmCodeKeys.get(iKeyCode);
		if(hm==null)return null;
		
		return hm.entrySet().iterator().next().getValue();
	}

	public Key getKeyForId(String strId) {
		Key key = tmKey.get(strId);
		if(key==null)key = tmKey.get(strKeyIdPrefixFilter+strId);
		return key;
	}
	
	public void addSpecialKeys() {
		int iSpecialCodeStart=Integer.MAX_VALUE;
		
		// a mouse listener can be used to set these
		addKeyWorkFull("mouseWheelUp",		iSpecialCodeStart--);
		addKeyWorkFull("mouseWheelDown",	iSpecialCodeStart--);
		addKeyWorkFull("mouseButton0",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton1",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton2",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton3",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton4",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton5",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton6",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton7",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton8",		iSpecialCodeStart--);
		addKeyWorkFull("mouseButton9",		iSpecialCodeStart--);
		
		//TODO joystick
	}
	
	public void configure() {
  	addSpecialKeys();
	}
	
	public abstract KeyBind getPressedKeysAsKeyBind();
	
	public ArrayList<Key> getAllPressedKeys(){
		ArrayList<Key> akey = new ArrayList<Key>();
		for(Key key:getKeyList()){
			if(key.isPressed()){
				akey.add(key);
			}
		}
		return akey;
	}
	public String getAllPressedKeysSimpleReport() {
		String str="";
		for(Key key:getAllPressedKeys()){
			if(!str.isEmpty())str+="+";
			str+=key.getSimpleId();
		}
		return str;
	}
	
	@Override
	public boolean isConfigured() {
		return bConfigured;
	}
	
	@Override
	public ManageKeyCode configure(ICfgParm icfg) {
		bConfigured=true;
		return this;
	}
}
