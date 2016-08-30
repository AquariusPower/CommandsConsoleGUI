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

package com.github.commandsconsolegui.jmegui.lemur.dialog;

import java.util.ArrayList;

import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ListDialogState<T extends Command<Button>> extends BasicDialogStateAbs<T,ListDialogState<T>> {
	public static class CfgParm<T extends Command<Button>> extends BasicDialogStateAbs.CfgParm{
		private LemurDialogGUIStateAbs<T,?>	diagChoice;
		private LemurDialogGUIStateAbs<T,?>	diagQuestion;

		public CfgParm(
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Integer iEntryHeightPixels,
				LemurDialogGUIStateAbs<T,?> diagChoice,
				LemurDialogGUIStateAbs<T,?> diagQuestion) {
			super(fDialogWidthPercentOfAppWindow,
					fDialogHeightPercentOfAppWindow, fInfoHeightPercentOfDialog,
					iEntryHeightPixels);
			this.diagChoice=diagChoice;
			this.diagQuestion=diagQuestion;
			super.setUIId(ListDialogState.class.getSimpleName());
		}
	}
	private CfgParm<T>	cfg;
	@Override
	public ListDialogState<T> configure(ICfgParm icfg) {
		cfg = (CfgParm<T>)icfg;
		
		addModalDialog(cfg.diagChoice);
		addModalDialog(cfg.diagQuestion);
		
		super.configure(cfg);
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	@Override
	public boolean prepareTestData(){
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
		
		return true;
	}

	@Override
	public void applyResultsFromModalDialog() {
		BaseDialogStateAbs<T,?> diagModal = getDiagModalCurrent().getDiagModal();
		T cmdAtParent = getDiagModalCurrent().getCmdAtParent();
		ArrayList<DialogListEntryData<T>> adataToApplyResultsList = getDiagModalCurrent().getDataReferenceAtParentListCopy();
		
		boolean bChangesMade = false;
		for(DialogListEntryData<T> dataAtModal:diagModal.getDataSelectionListCopy()){
				if(cmdAtParent.equals(cmdDel)){
					if(diagModal instanceof QuestionDialogState){
						QuestionDialogState<T> qds = (QuestionDialogState<T>)diagModal;
						
						if(qds.isYes(dataAtModal)){
//						if(dataAtModal.equals(qds.dataYes)){
							for(DialogListEntryData<T> dataToApplyResults:adataToApplyResultsList){
								removeEntry(dataToApplyResults);
								bChangesMade=true;
							}
							
							/**
							 * There is already a sound for entries removal
							if(bChangesMade)AudioUII.i().play(EAudio.ReturnChosen);
							 */
						}else
						if(qds.isNo(dataAtModal)){
//						if(dataAtModal.equals(qds.dataNo)){
							AudioUII.i().play(EAudio.ReturnNothing);
						}
					}
				}else
				if(cmdAtParent.equals(cmdCfg)){
					for(DialogListEntryData<T> dataToCfg:adataToApplyResultsList){
						dataToCfg.updateTextTo(dataAtModal.getText());
						bChangesMade=true;
					}
					
					if(bChangesMade)AudioUII.i().play(EAudio.ReturnChosen);
				}
		}
		
		if(bChangesMade){
			requestRefreshList();
		}
		
		super.applyResultsFromModalDialog();
	}

	@Override
	protected void actionCustomAtEntry(DialogListEntryData<T> dataSelected) {
		super.actionCustomAtEntry(dataSelected);
		openModalDialog(cfg.diagChoice.getId(), dataSelected, (T)cmdCfg);
	}

	public class CommandDel implements Command<Button>{
		@SuppressWarnings("unchecked")
		@Override
		public void execute(Button btn) {
			DialogListEntryData<T> dled = getDataFrom(btn);
			
			if(dled.isParent()){
//				CustomDialogGUIState.this.setDataToApplyModalChoice(data);
				ListDialogState.this.openModalDialog(cfg.diagQuestion.getId(), dled, (T)this);
				AudioUII.i().play(EAudio.Question);
			}else{
				ListDialogState.this.removeEntry(dled);
			}
		}
	}
	CommandDel cmdDel = new CommandDel();
	
	@Override
	public DialogListEntryData<T> addEntryQuick(String strText) {
		DialogListEntryData<T> dled = super.addEntryQuick(strText);
		/**
		 * this order matters
		 */
		dled.addCustomButtonAction("Cfg",(T)cmdCfg);
		dled.addCustomButtonAction("X",(T)cmdDel);
		
		return dled;
	}

	public class CommandCfg implements Command<Button>{
		@Override
		public void execute(Button btn) {
//			DialogTestState.this.openModalDialog(EDiag.Cfg.toString(), getDataFrom(btn), (T)this);
			actionCustomAtEntry(getDataFrom(btn));
//			DialogTestState.this.actionSubmit();
		}
	}
	CommandCfg cmdCfg = new CommandCfg();
	
	@Override
	protected ListDialogState<T> getThis() {
		return this;
	}
}
