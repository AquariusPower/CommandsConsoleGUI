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

package com.github.commandsconsolegui.jme.lemur.dialog;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jme.AudioUII;
import com.github.commandsconsolegui.jme.AudioUII.EAudio;
import com.github.commandsconsolegui.jme.DialogStateAbs;
import com.github.commandsconsolegui.jme.extras.DialogListEntryData;
import com.github.commandsconsolegui.jme.lemur.console.ChoiceVarDialogState;
import com.github.commandsconsolegui.misc.HoldRestartable;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * This is like the inventory list.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class MaintenanceListLemurDialogState<T extends Command<Button>> extends BasicLemurDialogStateAbs<T,MaintenanceListLemurDialogState<T>> {
	public static class CfgParm<T> extends BasicLemurDialogStateAbs.CfgParm{
		private HoldRestartable<LemurDialogStateAbs<T,?>> hrdiagChoice = new HoldRestartable<LemurDialogStateAbs<T,?>>(this);
		private HoldRestartable<LemurDialogStateAbs<T,?>> hrdiagQuestion = new HoldRestartable<LemurDialogStateAbs<T,?>>(this);
//		private LemurDialogStateAbs<T,?>	diagChoice;
//		private LemurDialogStateAbs<T,?>	diagQuestion;

		public CfgParm(
				Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier,
				LemurDialogStateAbs<T,?> diagChoice,
				LemurDialogStateAbs<T,?> diagQuestion) {
			super(fDialogWidthPercentOfAppWindow,
					fDialogHeightPercentOfAppWindow, fInfoHeightPercentOfDialog,
					fEntryHeightMultiplier);
			this.setDiagChoice(diagChoice);
			this.setDiagQuestion(diagQuestion);
//			super.setUIId(MaintenanceListDialogStateAbs.class.getSimpleName());
		}

		public LemurDialogStateAbs<T,?> getDiagChoice() {
			return hrdiagChoice.getRef();
		}

		public void setDiagChoice(LemurDialogStateAbs<T,?> diagChoice) {
			this.hrdiagChoice.setRef(diagChoice);
		}

		public LemurDialogStateAbs<T,?> getDiagQuestion() {
			return hrdiagQuestion.getRef();
		}

		public void setDiagQuestion(LemurDialogStateAbs<T,?> diagQuestion) {
			this.hrdiagQuestion.setRef(diagQuestion);
		}
	}
	private CfgParm<T>	cfg;
	@Override
	public MaintenanceListLemurDialogState<T> configure(ICfgParm icfg) {
		cfg = (CfgParm<T>)icfg;
		
		if(cfg.getDiagChoice()!=null)addModalDialog(cfg.getDiagChoice());
		if(cfg.getDiagQuestion()!=null)addModalDialog(cfg.getDiagQuestion());
		
		super.configure(cfg);
		
		storeCfgAndReturnSelf(icfg);
		return getThis();
	}
	
//	@Override
//	protected boolean prepareTestData(){
//		addEntryQuick(null);
//		addEntryQuick(null);
//		
//		DialogListEntryData<T> dleS1 = addEntryQuick("section 1");
//		addEntryQuick(null).setParent(dleS1);
//		addEntryQuick(null).setParent(dleS1);
//		addEntryQuick(null).setParent(dleS1);
//		
//		DialogListEntryData<T> dleS2 = addEntryQuick("section 2");
//		addEntryQuick(null).setParent(dleS2);
//		addEntryQuick(null).setParent(dleS2);
//		DialogListEntryData<T> dleS21 = addEntryQuick("section 2.1").setParent(dleS2);
//		addEntryQuick(null).setParent(dleS21);
//		addEntryQuick(null).setParent(dleS21);
//		addEntryQuick(null).setParent(dleS21);
//		
//		addEntryQuick("S2 child").setParent(dleS2); //ok, will be placed properly
//		
//		addEntryQuick("S1 child").setParent(dleS1); //out of order for test
//		addEntryQuick("S21 child").setParent(dleS21); //out of order for test
//		
//		return true;
//	}

	@Override
	public void applyResultsFromModalDialog() {
		DialogStateAbs<T,?> diagModal = getChildDiagModalInfoCurrent().getDiagModal();
		T cmdRequestedAtThisDiag = getChildDiagModalInfoCurrent().getCmdAtParent();
		ArrayList<DialogListEntryData<T>> adledToApplyResultsList = getChildDiagModalInfoCurrent().getParentReferencedDledListCopy();
		
		boolean bChangesMade = false;
		for(DialogListEntryData<T> dledAtModal:diagModal.getDataSelectionListCopy()){
				if(cmdRequestedAtThisDiag.equals(cmdDel)){
					if(diagModal instanceof QuestionLemurDialogState){
						QuestionLemurDialogState<T> qds = (QuestionLemurDialogState<T>)diagModal;
						if(qds.isYes(dledAtModal)){
							bChangesMade = deleteEntry(adledToApplyResultsList);
							
							/**
							 * !!! ATTENTION !!!
							 * There is already a sound for entries removal
							if(bChangesMade)AudioUII.i().play(EAudio.ReturnChosen);
							 */
						}else
						if(qds.isNo(dledAtModal)){
//						if(dataAtModal.equals(qds.dataNo)){
							AudioUII.i().play(EAudio.ReturnNothing);
						}
					}else{
						throw new PrerequisitesNotMetException("unexpected diag", diagModal, QuestionLemurDialogState.class);
					}
				}else
				if(cmdRequestedAtThisDiag.equals(cmdCfg)){
					bChangesMade = modifyEntry(diagModal, dledAtModal, adledToApplyResultsList);
					if(bChangesMade)AudioUII.i().play(EAudio.ReturnChosen);
				}
		}
		
		if(bChangesMade){
			requestRefreshUpdateList();
		}
		
		super.applyResultsFromModalDialog();
	}
	
	/**
	 * 
	 * @param diagModal mainly to give more options when overriding this method
	 * @param dledAtModal
	 * @param adledToApplyResultsList
	 * @return
	 */
	protected boolean modifyEntry(DialogStateAbs<T,?> diagModal, DialogListEntryData<T> dledAtModal, ArrayList<DialogListEntryData<T>> adledToApplyResultsList) {
		boolean bChangesMade=false;
		for(DialogListEntryData<T> dledToCfg:adledToApplyResultsList){
			dledToCfg.updateTextTo(dledAtModal.getTextValue());
			bChangesMade=true;
		}
		return bChangesMade;
	}

	protected boolean deleteEntry(ArrayList<DialogListEntryData<T>> adledToApplyResultsList) {
		boolean bChangesMade = false;
		for(DialogListEntryData<T> dledToApplyResults:adledToApplyResultsList){
			removeEntry(dledToApplyResults);
			bChangesMade=true;
		}
		
		return bChangesMade;
	}

	@Override
	protected void actionCustomAtEntry(DialogListEntryData<T> dledSelected) {
		if(cfg.getDiagChoice()!=null){
			super.actionCustomAtEntry(dledSelected);
			openModalDialog(cfg.getDiagChoice().getId(), dledSelected, (T)cmdCfg);
		}else{
			AudioUII.i().playOnUserAction(AudioUII.EAudio.Failure);
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("no choice dialog configured for "+this, dledSelected);
		}
	}

	public class CommandDel implements Command<Button>{
		@SuppressWarnings("unchecked")
		@Override
		public void execute(Button btn) {
			DialogListEntryData<T> dled = getDledFrom(btn);
			
			if(dled.isParent()){
//				CustomDialogGUIState.this.setDataToApplyModalChoice(data);
				if(cfg.getDiagQuestion()!=null){
					MaintenanceListLemurDialogState.this.openModalDialog(cfg.getDiagQuestion().getId(), dled, (T)this);
					AudioUII.i().play(EAudio.Question);
				}else{
					AudioUII.i().playOnUserAction(AudioUII.EAudio.Failure);
					GlobalCommandsDelegatorI.i().dumpDevWarnEntry("no question dialog configured for "+this, btn, dled);
				}
			}else{
				MaintenanceListLemurDialogState.this.removeEntry(dled);
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
			actionCustomAtEntry(getDledFrom(btn));
//			DialogTestState.this.actionSubmit();
		}
	}
	CommandCfg cmdCfg = new CommandCfg();
	
	@Override
	protected MaintenanceListLemurDialogState<T> getThis() {
		return this;
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=MaintenanceListLemurDialogState.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=MaintenanceListLemurDialogState.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
}
