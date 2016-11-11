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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;

/**
 * 
* @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageKeyCodeI {
	private static ManageKeyCodeI instance = new ManageKeyCodeI();
	public static ManageKeyCodeI i(){return instance;}
	
	public static class Key{
		private String strId;
		private int iKeyCode = -1;
		private boolean bPressed = false;
		
//		private Key(Integer iKeyCode) {
//			this(ManageKeyCodeI.i().getKeyId(iKeyCode), iKeyCode);
//		}
		
		/**
		 * Must remain private, the scope of available keys is limited and must be managed here!
		 * @param strId
		 * @param iKeyCode
		 */
		private Key(String strId, Integer iKeyCode) {
			PrerequisitesNotMetException.assertNotEmpty("id", strId, this);
			PrerequisitesNotMetException.assertIsTrue("invalid negative keycode", iKeyCode>=0, this);
			
			this.strId=strId;
			this.iKeyCode=iKeyCode;
		}
		
		public int getKeyCode() {
			return iKeyCode;
		}
		private void setKeyCode(int iKeyCode) {
			this.iKeyCode = iKeyCode;
		}

		public boolean isPressed() {
			return bPressed;
		}
		private void setPressed(boolean bPressed) {
			this.bPressed = bPressed;
		}

		public String getId() {
			return strId;
		}
	}
	
	private TreeMap<String,Key> tmKey = new TreeMap<String,Key>(String.CASE_INSENSITIVE_ORDER);
	private TreeMap<Integer,HashMap<String,Key>> tmCodeKeys = new TreeMap<Integer,HashMap<String,Key>>();
	private String	strKeyIdPrefixFilter;
	
	public ManageKeyCodeI() {
		int iSpecialCodeStart=Integer.MAX_VALUE;
		addKeyIdCode("ctrl"	, iSpecialCodeStart--);
		addKeyIdCode("shift", iSpecialCodeStart--);
		addKeyIdCode("alt"	, iSpecialCodeStart--);
		
		addKeyIdCode("mouseWheelUp"	, iSpecialCodeStart--);
		addKeyIdCode("mouseWheelDown"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton0"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton1"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton2"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton3"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton4"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton5"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton6"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton7"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton8"	, iSpecialCodeStart--);
		addKeyIdCode("mouseButton9"	, iSpecialCodeStart--);
	}

	/**
	 * 
	 * @param strId
	 * @param iCode
	 * @return true if added or already set with same code, false if already set with different code
	 */
	public boolean addKeyIdCode(String strId, Integer iCode){
		Key key = tmKey.get(strId);
		if(key!=null){
			Integer iCodeExisting = key.getKeyCode();
			if(iCodeExisting==iCode){
				MsgI.i().devWarn("already set", strId, iCode);
				return true;
			}else{
				MsgI.i().devWarn("cannot modify the code for", strId, iCodeExisting, iCode);
				return false;
			}
		}
		
//		if(tmIdCode.values().contains(iCode)){
//			String strExistingId=null;
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
			
//			throw new PrerequisitesNotMetException("already contains code", strExistingId, iCode, strKey);
//		}
		
		Key keyNew = new Key(strId,iCode);
		tmKey.put(strId, keyNew);
		
		/**
		 * populate list of keys with the same code
		 */
		HashMap<String,Key> hm = tmCodeKeys.get(iCode);
		if(hm==null){
			hm=new HashMap<String,Key>();
			tmCodeKeys.put(iCode,hm);
		}
		hm.put(keyNew.getId(), keyNew);
		
		return true;
	}
	
	public ArrayList<Key> getAllKeysListCopy() {
		ArrayList<Key> akeyList = new ArrayList<Key>();
		for(Entry<String,Key> entry:tmKey.entrySet()){
			akeyList.add(entry.getValue());
		}
		return akeyList;
	}
	
	public ArrayList<String> getAllKeyCodesReport(){
		ArrayList<String> astr = new ArrayList<String>();
		for(Key key:getAllKeysListCopy()){
			astr.add(key.getId()+"="+key.getKeyCode());
		}
		Collections.sort(astr);
		return astr;
	}
	
	/**
	 * 
	 * @param cl
	 * @param strKeyIdPrefixFilter can be null
	 * @return
	 */
	public boolean fillKeyIdCodeFrom(Class<?> cl, String strKeyIdPrefixFilter){
		this.strKeyIdPrefixFilter=strKeyIdPrefixFilter;
		
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
				
				if(!addKeyIdCode(strId,iCode)){
					throw new PrerequisitesNotMetException("keycode filling failed",strId,iCode,cl,strKeyIdPrefixFilter);
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new PrerequisitesNotMetException("unexpected").setCauseAndReturnSelf(e);
		}
		
		return true;
	}
	
	public int getKeyCode(String strId){
		return tmKey.get(strId).getKeyCode();
	}
	
	public String getKeyId(int iCode){
		for(Entry<String,Key> entry:tmKey.entrySet()){
			Key key = entry.getValue();
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
		return tmCodeKeys.get(iKeyCode).entrySet().iterator().next().getValue();
	}

	public Key getKeyForId(String strId) {
		Key key = tmKey.get(strId);
		if(key==null)key = tmKey.get(strKeyIdPrefixFilter+strId);
		return key;
	}

}
