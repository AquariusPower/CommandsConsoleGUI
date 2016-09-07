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

package com.github.commandsconsolegui.jmegui.extras;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.print.attribute.standard.Chromaticity;

import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.scene.Node;

/**
 * Each list entry can show differently, they could be:
 * - blank spacer
 * - simple section title
 * - an option that can be cofigured
 * - a sound that can play/pause/stop (buttons)
 * - a check box to simply enable/disable
 * - etc...
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <T> is the action class for buttons
 */
public class DialogListEntryData<T> implements Savable{
	private static String strLastUniqueId = "0";
	
	private String	strUniqueId;
	private BaseDialogStateAbs diagOwner;
	
	private HashMap<String,T> hmCustomButtonsActions = new HashMap<String,T>();
	private Object	objUser;
	
	private DialogListEntryData<T> parent; //it must be set as the parent type too
	private boolean bTreeExpanded = false; 
	private ArrayList<DialogListEntryData<T>> aChildList = new ArrayList<DialogListEntryData<T>>();
	
//	int iKey;
	private String strText;
//	T objRef;
	
	public DialogListEntryData(BaseDialogStateAbs diagOwner) {
		this.diagOwner=diagOwner;
		this.strUniqueId = DialogListEntryData.strLastUniqueId = (MiscI.i().getNextUniqueId(strLastUniqueId));
	}
	
//	public DialogListEntryData<T> renameCustomButtonAction(T action, String strLabelTextIdNew){
//		for(Entry<String, T> en:hmCustomButtonsActions.entrySet()){
//			if(en.getValue()==)
//		}
//		T action = hmCustomButtonsActions.remove(strLabelTextIdOld);
//		hmCustomButtonsActions.put(strLabelTextIdNew,action);
//		return this;
//	}
	public DialogListEntryData<T> addCustomButtonAction(String strLabelTextId, T action){
		hmCustomButtonsActions.put(strLabelTextId,action);
		return this;
	}
	public DialogListEntryData<T> clearCustomButtonActions(){
		hmCustomButtonsActions.clear();
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
	
//	private char chQuotesL='"';
//	private char chQuotesR='"';
	private boolean bAddVisibleQuotes=false;
	
	public boolean isAddVisibleQuotes(){
		return bAddVisibleQuotes;
	}
	
	public DialogListEntryData<T> setAddVisibleQuotes(boolean b){
		this.bAddVisibleQuotes=b;
		return this;
	}
	
	public String getTextValue() {
		return strText;
	}
	
	/**
	 * if text is null will be set to "null"
	 * if empty or blank, will be surrounded by quotes,
	 * TODO lemur seems to only accept with some actual text?
	 * 
	 * @return
	 */
	public String getVisibleText() {
		if(strText==null){
			return ""+null;
		}
		
		if(strText.trim().isEmpty() || isAddVisibleQuotes()){
			return '"'+strText+'"';
		}
		
		return strText;
	}
	
	public Object getUserObj(){
		return this.objUser;
	}
	
	/**
	 * @param strText 
	 * @return
	 */
	public DialogListEntryData<T> updateTextTo(Object objText) {
		if(objText == null){
			this.strText=null;
		}else
		if(objText instanceof String){
			this.strText=(String)objText;
		}else{
			this.strText=objText.toString();
		}
		
		return this;
	}
	
	
	/**
	 * 
	 * @param strText 
	 * @param objUser the linked object represented by the text
	 * @return
	 */
	public DialogListEntryData<T> setText(Object objText, Object objUser) {
		updateTextTo(objText);
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
		if(this==parent)throw new PrerequisitesNotMetException("cant parent self: parent==this", this);
		
		// remove self from previous parent
		DialogListEntryData<T> dledParentOld = this.getParent();
		if(dledParentOld!=null){
			dledParentOld.aChildList.remove(this);
		}
		
		// consistency check
		if(this.hasChild(parent)){
			throw new PrerequisitesNotMetException("cant be child of a child", this, parent, this.aChildList);
		}
		
		// parent can be null
		this.parent = parent;
		
		// adds self to new parent child list
		if(this.getParent()!=null){
			if(!this.getParent().aChildList.contains(this)){
				this.getParent().aChildList.add(this);
			}
		}
		
		return this;
	}
	
	public boolean hasChild(DialogListEntryData<T> dledChildToCheck){
		if(dledChildToCheck==null){
			throw new PrerequisitesNotMetException("null child to check", this);
		}
		
		for(DialogListEntryData<T> dledChild:this.aChildList){
			if(dledChild==dledChildToCheck)return true;
			
			if(dledChild.aChildList.size()>0){
				if(dledChild.hasChild(dledChildToCheck))return true;
			}
		}
		
		return false;
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
		diagOwner.requestRefreshList();
//		cell.assignedCellRenderer.diagParent.requestRefreshList();
		
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

	public DialogListEntryData<T> clearChildren() {
		aChildList.clear();
		return this;
	}
	
	/**
	 * Returns root one (parent less). 
	 * 
	 * @return null if it has no parent
	 */
	public DialogListEntryData<T> getParentest() {
		DialogListEntryData<T> dledParent = getParent();
		if(dledParent==null)return null;
		
//		ArrayList<DialogListEntryData<T>> adledConsistencyChk = new ArrayList<DialogListEntryData<T>>();
		while(true){
			DialogListEntryData<T> dledParentTmp = dledParent.getParent();
			if(dledParentTmp==null)return dledParent;
			
//			if(adledConsistencyChk.contains(dledParentTmp)){
//				throw new PrerequisitesNotMetException("tree is inconsistent (child/parent loop)", adledConsistencyChk, dledParentTmp);
//			}else{
//				adledConsistencyChk.add(dledParentTmp);
//			}
			
			dledParent = dledParentTmp;
		}
	}
	
//	public void setUId(String strUId) {
//		this.strUId = strUId;
//	}
}
