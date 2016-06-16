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

package com.github.commandsconsolegui.jmegui.lemur.console.test;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.MiscI;
import com.jme3.scene.Node;
import com.simsilica.lemur.GuiGlobals;

/**
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class CustomDialogGUIState extends LemurDialogGUIStateAbs{
	StringCmdField cmdAddEntries = null;
//	ArrayList<String> astr;
//	HashMap<String,String> hmKeyValueTmp = new HashMap<String,String>();
//	private Object	answerFromModal;
	
//public static class CfgParm implements ICfgParm{
//String strUIId;
//boolean bIgnorePrefixAndSuffix;
//Node nodeGUI;
//public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix,Node nodeGUI) {
//	super();
//	this.strUIId = strUIId;
//	this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
//	this.nodeGUI =nodeGUI;
//}
//}
	public static class CfgParm extends LemurDialogGUIStateAbs.CfgParm{
		public CfgParm(boolean	bOptionSelectionMode,String strUIId, boolean bIgnorePrefixAndSuffix,
				Node nodeGUI, Float fDialogHeightPercentOfAppWindow,
				Float fDialogWidthPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Integer iEntryHeightPixels, BaseDialogStateAbs modalParent) {
			super(bOptionSelectionMode,strUIId, bIgnorePrefixAndSuffix, nodeGUI,
					fDialogHeightPercentOfAppWindow, fDialogWidthPercentOfAppWindow,
					fInfoHeightPercentOfDialog, iEntryHeightPixels, modalParent);
			// TODO Auto-generated constructor stub
		}
	}

	protected ArrayList<DialogListEntryData>	adleFullList = new ArrayList<DialogListEntryData>();
	
	@Override
	public CustomDialogGUIState configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
		
		super.configure(cfg); //params are identical
		
		/**
		 * this is just an example as state changes can be delayed
		 */
		super.setRetryDelay(500);
		super.rInit.setRetryDelay(1000); //after the generic one
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	@Override
	protected String getTextInfo() {
		return super.getTextInfo()+"Custom Dialog: Test.";
	};
	
	@Override
	protected void updateList() {
		DialogListEntryData dataValue = getCfgDataValueAndClearIt();
		if(dataValue!=null){
//			int i = adleFullList.indexOf(getCfgDataRefAndClearIt());
//			if(getSelectedIndex()>=0){
			DialogListEntryData dataRef = getCfgDataRefAndClearIt();
//			if(i>=0){
			if(dataRef!=null){
//				adleFullList.set(getSelectedIndex(), dleAnswer);
//				adleFullList.set(i, dleAnswer);
				dataRef.setCfg(dataValue);
			}else{
				cd().dumpWarnEntry("no entry selected at "+this.getId()+" to apply modal dialog option");
			}
		}
//		else{
//			addEntry(null);
//		}
		
		updateList(adleFullList);
		
		super.updateList();
	}
	
	public void addEntry(String strText){
		DialogListEntryData dle = new DialogListEntryData();
		if(strText==null){
			strText=this.getId()+": New test entry: "
//				+MiscI.i().getDateTimeForFilename(true)
//				+", "
				+System.nanoTime();
		}
		dle.setText(strText);
		adleFullList.add(dle);
		if(adleFullList.size()>100)adleFullList.remove(0);
		
		requestRefreshList();
	}
	
//	@Override
//	protected String getSelectedEntryKey() {
//		return hmKeyValueTmp.get(formatEntryKey(getSelectedEntryValue()));
//	}
	
	@Override
	protected boolean initCheckPrerequisites() {
		if(GuiGlobals.getInstance()==null)return false;
		
		return super.initCheckPrerequisites();
	}
	
	@Override
	protected boolean initOrUndo() {
		for(int i=0;i<10;i++)addEntry(null); //some test data
		return super.initOrUndo();
	}
	
//	@Override
//	protected boolean disableOrUndo() {
//		if(!super.disableOrUndo())return false;
//		
//		getModalParent().setAnswerFromModal(getOptionSelected());
//		
//		return true;
//	}
	
//	@Override
//	public String formatEntryKey(String val) {
//		return "Entry: "+val;
//	}

//	@Override
//	protected String getSelectedKey() {
//		return null;
//	}

//	@Override
//	public void setAnswerFromModalChild(Object... aobj) {
//		if(aobj.length==0)return;
//		
//		answerFromModal = aobj[1];
//	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		if(cmdAddEntries==null){
			cmdAddEntries = new StringCmdField(getId()+"AddEntry","[strText]");
		}
//		if(!isConfigured())return ECmdReturnStatus.Skip;
		
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,cmdAddEntries,null)){
			String strText = cc.paramString(1);
			addEntry(strText);
			bCommandWorked = true;
		}else
		{
			return super.execConsoleCommand(cc);
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
}
