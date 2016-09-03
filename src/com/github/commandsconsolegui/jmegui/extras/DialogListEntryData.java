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

package com.github.commandsconsolegui.jmegui.extras;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;

/**
 * Each list entry can show differently, they could be:
 * - blank spacer
 * - simple section title
 * - an option that can be cofigured
 * - a sound that can play/pause/stop (buttons)
 * - a check box to simply enable/disable
 * - etc...
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 * @param <T> is the action class for buttons
 */
public class DialogListEntryData<T> implements Savable{
	private static String strLastUniqueId = "0";
	
	private String	strUniqueId;
	
	HashMap<String,T> hmCustomButtonsActions = new HashMap<String,T>();
	Object	objUser;
	
	DialogListEntryData<T> parent; //it must be set as the parent type too
	boolean bTreeExpanded = false; 
	ArrayList<DialogListEntryData<T>> aChildList = new ArrayList<DialogListEntryData<T>>();
	
//	int iKey;
	String strText;
//	T objRef;
	
	public DialogListEntryData() {
		this.strUniqueId = DialogListEntryData.strLastUniqueId = (MiscI.i().getNextUniqueId(strLastUniqueId));
	}
	
	public DialogListEntryData<T> addCustomButtonAction(String strLabelTextId, T action){
		hmCustomButtonsActions.put(strLabelTextId,action);
		return this;
	}
	
//	@SuppressWarnings("unchecked")
//	public Entry<String,T>[] getLabelActionListCopy(){
//		return (Entry<String,T>[])hmLabelAction.entrySet().toArray();
//	}
	@SuppressWarnings("rawtypes")
	public Entry[] getCustomButtonsActionsListCopy(){
		return hmCustomButtonsActions.entrySet().toArray(new Entry[0]);
	}
	
//	public int getKey() {
//		return iKey;
//	}
//	public DialogListEntryData<T> setKey(int iKey) {
//		this.iKey = iKey;
//		return this;
//	}
	public String getText() {
		return strText;
	}
	public Object getUserObj(){
		return this.objUser;
	}
	public DialogListEntryData<T> updateTextTo(String strText) {
		if(strText==null)throw new PrerequisitesNotMetException("null text", this, strText);
		this.strText=strText;
		return this;
	}
	
	/**
	 * 
	 * @param strText 
	 * @param objUser the linked object represented by the text
	 * @return
	 */
	public DialogListEntryData<T> setText(String strText, Object objUser) {
		updateTextTo(strText);
//		this.strText = strText;
		this.objUser=objUser;
//		this.objRef=objReference;
		return this;
	}
//	public Object getRef() {
//		return objRef;
//	}
//	public DialogListEntryData<T> setRef(Object objRef) {
//		this.objRef = objRef;
//		return this;
//	}
	
//	public T getAction(){
//		return objRef;
//	}
	
	@Override
	public String toString() {
//		return iKey+","+strText+","+objRef;
		return "UId="+getUId()+","+strText;
	}
	
//	public void setCfg(DialogListEntryData data) {
////		this.iKey = data.iKey;
//		this.strText = data.strText;
////		this.objRef = data.objRef;
//	}
	
	/**
	 * TODO should be a more refined string
	 * @return
	 */
	public String report() {
		return this.toString();
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public DialogListEntryData<T> getParent() {
		return parent;
	}
	
	/**
	 * 
	 * @param parent if null, will just remove from existing parent
	 * @return
	 */
	public DialogListEntryData<T> setParent(DialogListEntryData<T> parent) {
		if(this.parent!=null){
			this.parent.aChildList.remove(this);
		}
		
		this.parent = parent;
		
		if(this.parent!=null){
			this.parent.aChildList.add(this);
		}
		
		return this;
	}

//	public EEntryType geteType() {
//		return eType;
//	}
//
//	public DialogListEntryData<T> setType(EEntryType eType) {
//		this.eType = eType;
//		return this;
//	}

	public boolean isTreeExpanded() {
		return bTreeExpanded;
	}

	public DialogListEntryData<T> setTreeExpanded(boolean bTreeExpanded) {
		this.bTreeExpanded = bTreeExpanded;
		return this;
	}
	
	public boolean toggleExpanded() {
//		if(!isParent())return false;
		
		this.bTreeExpanded = !this.bTreeExpanded;
		
		AudioUII.i().playOnUserAction(isTreeExpanded() ?
				AudioUII.EAudio.ExpandSubTree : AudioUII.EAudio.ShrinkSubTree);
		
		return this.bTreeExpanded; 
	}

	public boolean isParent() {
		return aChildList.size()>0;
	}

	public ArrayList<DialogListEntryData<T>> getChildrenCopy() {
		return new ArrayList<DialogListEntryData<T>>(aChildList);
	}

	public String getUId() {
		return strUniqueId;
	}

//	public void setUId(String strUId) {
//		this.strUId = strUId;
//	}
}
