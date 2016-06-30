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

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.GuiGlobals;

/**
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class CustomDialogGUIState<T extends Command<Button>> extends LemurDialogGUIStateAbs<T>{
	StringCmdField scfAddEntry = new StringCmdField(this,null,"[strEntryText]");
	
	public static enum EDiag{
		Cfg,
		List,
		Confirm,
		;
	}
	EDiag ediag = null;
	
	public CustomDialogGUIState(EDiag ediag) {
		this.ediag=ediag;
		super.bPrefixCmdWithIdToo = true;
	}
	
	public static class CfgParm<T> extends LemurDialogGUIStateAbs.CfgParm<T>{
		public CfgParm(boolean	bOptionSelectionMode,
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow, Float fInfoHeightPercentOfDialog,
				Integer iEntryHeightPixels){//, BaseDialogStateAbs<T> modalParent) {
			super(bOptionSelectionMode, null, null,
					fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, iEntryHeightPixels);//, modalParent);
		}
	}
	@Override
	public CustomDialogGUIState<T> configure(ICfgParm icfg) {
		@SuppressWarnings("unchecked")
		CfgParm<T> cfg = (CfgParm<T>)icfg;
		cfg.setUIId(ediag.toString());
		
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
	
//	@Override
//	protected void updateList() {
//		DialogListEntryData<T> dataValue = getModalChosenDataAndClearIt();
//		if(dataValue!=null){
//			DialogListEntryData<T> dataRef = getDataToApplyModalChoiceAndClearIt();
//			if(dataRef!=null){
//				switch(ediag){
//					case Cfg:
//						break;
//					case Confirm:
//						break;
//					case List:
//						dataRef.updateTextTo(dataValue.getText());
//						break;
//				}
//			}else{
//				cd().dumpWarnEntry("no entry selected at "+this.getId()+" to apply modal dialog option");
//			}
//		}
//		
////		super.updateList(adleFullList);
//		
//		super.updateList();
//	}
	
	@Override
	protected boolean updateOrUndo(float tpf) {
		if(getDiagModalCurrent()!=null){
			if(getDiagModalCurrent().getDiagModal().isChoiceMade()){
				applyResultsFromModalDialog();
			}
		}
		
		return super.updateOrUndo(tpf);
	}
	
//	@Override
//	public CustomDialogGUIState<T> getDiagModalCurrent() {
//		return (CustomDialogGUIState<T>) super.getDiagModalCurrent();
//	}
	
	@Override
	public void applyResultsFromModalDialog() {
		CustomDialogGUIState<T> diagModal = getDiagModalCurrent().getDiagModal();
		T cmdAtParent = getDiagModalCurrent().getCmdAtParent();
		ArrayList<DialogListEntryData<T>> adataToApplyResultsList = getDiagModalCurrent().getDataReferenceAtParentListCopy();
		
		boolean bChangesMade = false;
		for(DialogListEntryData<T> dataAtModal:diagModal.getDataSelectionListCopy()){
			switch(ediag){
				case List:
					if(cmdAtParent.equals(cmdDel)){
						if(dataAtModal.equals(diagModal.dataYes)){
							for(DialogListEntryData<T> dataToApplyResults:adataToApplyResultsList){
								removeEntry(dataToApplyResults);
								bChangesMade=true;
							}
						}
					}else
					if(cmdAtParent.equals(cmdCfg)){
						for(DialogListEntryData<T> dataToCfg:adataToApplyResultsList){
							dataToCfg.updateTextTo(dataAtModal.getText());
							bChangesMade=true;
						}
					}
					break;
			}
		}
	
		if(bChangesMade){
			requestRefreshList();
		}
		
		super.applyResultsFromModalDialog();
	}
	
	@SuppressWarnings("unchecked")
	public DialogListEntryData<T> getDataFrom(Spatial spt){
		String strKey = DialogListEntryData.class.getName();
		Object data = spt.getUserData(strKey);
		if(data==null)throw new PrerequisitesNotMetException("missing user object "+strKey);
		return (DialogListEntryData<T>) data;
	}
	
	public class CommandCfg implements Command<Button>{
		@SuppressWarnings("unchecked")
		@Override
		public void execute(Button btn) {
			CustomDialogGUIState.this.openModalDialog(EDiag.Cfg.toString(),getDataFrom(btn),(T)this);
		}
	}
	CommandCfg cmdCfg = new CommandCfg();
	
	public class CommandDel implements Command<Button>{
		@SuppressWarnings("unchecked")
		@Override
		public void execute(Button btn) {
			DialogListEntryData<T> data = getDataFrom(btn);
			
			if(data.isParent()){
//				CustomDialogGUIState.this.setDataToApplyModalChoice(data);
				CustomDialogGUIState.this.openModalDialog(EDiag.Confirm.toString(), data, (T)this);
			}else{
				CustomDialogGUIState.this.removeEntry(data);
			}
		}
	}
	CommandDel cmdDel = new CommandDel();
	
	public class CommandSel implements Command<Button>{
		@Override
		public void execute(Button btn) {
			CustomDialogGUIState.this.selectAndChoseOption(getDataFrom(btn));
		}
	}
	CommandSel cmdSel = new CommandSel();
	private DialogListEntryData<T>	dataYes;
	private DialogListEntryData<T>	dataNo;
	
	@SuppressWarnings("unchecked")
	public DialogListEntryData<T> addEntryQuick(String strText){
		DialogListEntryData<T> dle = new DialogListEntryData<T>();
		if(strText==null){
			strText=this.getId()+": New test entry: "
//				+MiscI.i().getDateTimeForFilename(true)
//				+", "
				+System.nanoTime();
		}
		dle.setText(strText,(T)cmdCfg);
		
		if(bOptionChoiceSelectionMode){
			dle.addCustomButtonAction("<-",(T)cmdSel);
		}else{
			/**
			 * this order matters
			 */
			dle.addCustomButtonAction("Cfg",(T)cmdCfg);
			dle.addCustomButtonAction("X",(T)cmdDel);
		}
		
		super.addEntry(dle);
		
//		if(adleFullList.size()>100)adleFullList.remove(0);
		
		requestRefreshList();
		
		return dle;
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
		switch(ediag){
			case Cfg:
				for(int i=0;i<10;i++)addEntryQuick(null); //some test data
				break;
			case Confirm:
				dataYes = addEntryQuick("[ yes    ]");
				dataNo  = addEntryQuick("[     no ]");
				break;
			case List:
				addEntryQuick(null);
				addEntryQuick(null);
				
				DialogListEntryData<T> dleS1 = addEntryQuick("section 1");
				addEntryQuick(null).setParent(dleS1);
				addEntryQuick(null).setParent(dleS1);
				addEntryQuick(null).setParent(dleS1);
				
				DialogListEntryData<T> dleS2 = addEntryQuick("section 2");
				addEntryQuick(null).setParent(dleS2);
				addEntryQuick(null).setParent(dleS2);
				DialogListEntryData<T> dleS21 = addEntryQuick("section 2.1").setParent(dleS2);
				addEntryQuick(null).setParent(dleS21);
				addEntryQuick(null).setParent(dleS21);
				addEntryQuick(null).setParent(dleS21);
				
				addEntryQuick("S2 child").setParent(dleS2); //ok, will be placed properly
				
				addEntryQuick("S1 child").setParent(dleS1); //out of order for test
				addEntryQuick("S21 child").setParent(dleS21); //out of order for test
				break;
		}
		
		return super.initOrUndo();
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cd) {
		boolean bCommandWorked = false;
		
		if(cd.checkCmdValidity(this,scfAddEntry,"[strText] [strParenUId]")){
			String strText = cd.getCurrentCommandLine().paramString(1);
			String strParentUId = cd.getCurrentCommandLine().paramString(2);
			
			DialogListEntryData<T> dleParent = null;
			for(DialogListEntryData<T> dle:adleCompleteEntriesList){
				if(dle.getUId().equalsIgnoreCase(strParentUId)){
					dleParent=dle;
					break;
				}
			}
			
			DialogListEntryData<T> dleNew = addEntryQuick(strText);
			if(dleParent!=null){
				dleNew.setParent(dleParent);
			}
			
			bCommandWorked = true;
		}else
		{
			return super.execConsoleCommand(cd);
		}
		
		return cd.cmdFoundReturnStatus(bCommandWorked);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execTextDoubleClickActionFor(DialogListEntryData<T> data) {
		if(isOptionSelectionMode())throw new PrerequisitesNotMetException("Option mode should not reach this method.");
		
		openModalDialog(EDiag.Cfg.toString(), data, (T)cmdCfg);
//		data.getActionTextDoubleClick().execute(null);
		
		return true;
	}

	@Override
	public boolean execActionFor(EMouseCursorButton e, Spatial sptSource) {
		switch(e){
			default:
				cd().dumpDevInfoEntry("no action for "+e+" "+sptSource.getName());
				break;
		}
		
		return true;
	}

	@Override
	protected void actionCustomAtEntry(DialogListEntryData<T> dataSelected) {
		openModalDialog(EDiag.Cfg.toString(), dataSelected, (T)cmdCfg);
	}
}
