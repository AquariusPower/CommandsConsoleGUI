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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.jme3.scene.Node;

/**
 * This class would be useful to a non Lemur dialog too.
 * TODO mix this with LemurDialogGUIStateAbs or {@link BaseDialogStateAbs} or both (some things to one, some to the other)?
 * 
 * A console command will be automatically created based on the configured {@link #strUIId}.<br>
 * See it at {@link BaseDialogStateAbs}.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class InteractionDialogStateAbs extends BaseDialogStateAbs{
	protected Node cntrNorth;
	protected String	strLastFilter = "";
//	protected ArrayList<DialogListEntry> aEntryList = new ArrayList<DialogListEntry>();
//	protected String	strLastSelectedKey;
	protected boolean bOptionSelectionMode = false;
	protected DialogListEntry	dleLastSelected;
//	private V	valueOptionSelected;

	public abstract void clearSelection();
	
//	public V getOptionSelected(){
//		return valueOptionSelected;
//	}
	
//	@Override
//	protected boolean disableOrUndo() {
//		if(!super.disableOrUndo())return false;
//		getModalParent().setAnswerFromModal(getOptionSelected());
//		return true;
//	}
	
	public static class CfgParm extends BaseDialogStateAbs.CfgParm{
		public boolean	bOptionSelectionMode;
		public CfgParm(boolean bOptionSelectionMode, String strUIId, boolean bIgnorePrefixAndSuffix,Node nodeGUI,BaseDialogStateAbs modalParent) {
			super(
				strUIId, 
				bIgnorePrefixAndSuffix, 
				nodeGUI,
				/** 
				 * Dialogs must be initially disabled because they are enabled 
				 * on user demand. 
				 */
				false,
				modalParent);
			this.bOptionSelectionMode=bOptionSelectionMode;
		}
	}
	@Override
	public InteractionDialogStateAbs configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
		this.bOptionSelectionMode=cfg.bOptionSelectionMode;
		super.configure(icfg);
		return storeCfgAndReturnSelf(icfg);
	}
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	protected abstract void updateInputField();
	
	public abstract DialogListEntry getSelectedEntry();
//		Integer i = getSelectedIndex();
//		if(i==null)return null;
//		return aEntryList.get(i);
//	}
//	protected abstract Integer getSelectedIndex();
	
//	
//	protected abstract String getSelectedEntryKey();
//	protected String getSelectedKey(){
//		for(Entry<String, V> entry:hmKeyValue.entrySet()){
//			if(entry.getValue().equals(getSelectedValue())){
//				return entry.getKey();
//			}
//		}
//		return null;
//	}
//	
//	protected DialogListEntry getSelectedEntryValue() {
//		return aEntryList.get(getSelectedEntryKey());
//	}
	
	/**
	 * 
	 */
	protected abstract void updateList();
	
	protected abstract void updateTextInfo();
	
	protected String getTextInfo(){
		String str="Info: Type a list filter at input text area and hit Enter.\n";
		
		if(bOptionSelectionMode){
			str+="Option Mode: when hitting Enter, if an entry is selected, it's value will be chosen.\n";
		}
		
		return str;
	}
	
	/**
	 * this will react to changes on the list
	 */
	@Override
	protected boolean updateOrUndo(float tpf) {
//		String str = getSelectedEntryKey();
//		if(str!=null)strLastSelectedKey=str;
		DialogListEntry dle = getSelectedEntry();
		if(dle!=null)dleLastSelected = dle;
		
		if(isAnswerFromModalFilled()){
			updateAllParts();
		}
		
		return super.updateOrUndo(tpf);
	}
	
//	/**
//	 * What will be shown at each entry on the list.
//	 * Default is to return the default string about the object.
//	 * 
//	 * @param val
//	 * @return
//	 */
//	public String formatEntryKey(V val){
//		return ""+val.hashCode();
//	}
	

	/**
	 * override empty to disable filter
	 */
	protected void applyListKeyFilter(){
		this.strLastFilter=getInputText();
	}
	
	/**
	 * what happens when pressing ENTER keys
	 * default is to update the list filter
	 */
	@Override
	protected void actionSubmit(){
		applyListKeyFilter();
		updateAllParts();
		
		if(bOptionSelectionMode){
			
//			if(getInputText().isEmpty()){ // was cleared
				DialogListEntry dle = getSelectedEntry(); //this value is in this console variable now
				getModalParent().setAnswerFromModal(dle);
				if(dle!=null){
					cd().dumpInfoEntry(this.getId()+": Option Selected: "+dle.toString());
					requestDisable(); //close if there is one entry selected
				}
//			}else{
//				getInputField().setText(""); //clear input field
//			}
			
//			}
		}
		
	}
	
	protected void updateAllParts(){
		updateTextInfo();
		
		updateList();
		
		updateInputField();
	}
	
	@Override
	protected boolean enableOrUndo() {
		updateAllParts();
		return super.enableOrUndo();
	}
	
//	@Override
//	protected boolean disableOrUndo() {
//		if(getModalParent()!=null)getModalParent().setAnswerFromModalChild(valueOptionSelected);
//		return super.disableOrUndo();
//	}
}
