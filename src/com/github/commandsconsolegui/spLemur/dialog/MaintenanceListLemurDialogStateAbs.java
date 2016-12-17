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

package com.github.commandsconsolegui.spLemur.dialog;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.misc.HoldRestartable;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spJme.AudioUII;
import com.github.commandsconsolegui.spJme.DialogStateAbs;
import com.github.commandsconsolegui.spJme.AudioUII.EAudio;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData;
import com.github.commandsconsolegui.spLemur.console.ChoiceVarDialogState;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * This is like the inventory list.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public abstract class MaintenanceListLemurDialogStateAbs<T extends Command<Button>, THIS extends MaintenanceListLemurDialogStateAbs<T,THIS>> extends BasicLemurDialogStateAbs<T,THIS> {
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
				LemurDialogStateAbs<T,?> diagQuestion) 
		{
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
	public THIS configure(ICfgParm icfg) {
		cfg = (CfgParm<T>)icfg;
		
		if(cfg.getDiagChoice()!=null)addModalDialog(cfg.getDiagChoice());
		if(cfg.getDiagQuestion()!=null)addModalDialog(cfg.getDiagQuestion());
		
		super.configure(cfg);
		
//		storeCfgAndReturnSelf(cfg);
		return getThis();
	}
	
	@Override
	public void applyResultsFromModalDialog() {
		DialogStateAbs<T,?> diagModal = getChildDiagModalInfoCurrent().getDiagModal();
		T cmdRequestedAtThisDiag = getChildDiagModalInfoCurrent().getCmdAtParent();
		ArrayList<DialogListEntryData> adledToApplyResultsList = getChildDiagModalInfoCurrent().getParentReferencedDledListCopy();
		
		boolean bChangesMade = false;
		for(DialogListEntryData<T,?> dledAtModal:diagModal.getDataSelectionListCopy()){
				if(cmdRequestedAtThisDiag.equals(cmdDel)){
					if(diagModal instanceof QuestionLemurDialogStateAbs){
						QuestionLemurDialogStateAbs qds = (QuestionLemurDialogStateAbs)diagModal;
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
						throw new PrerequisitesNotMetException("unexpected diag", diagModal, QuestionLemurDialogStateAbs.class);
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
	protected boolean modifyEntry(DialogStateAbs diagModal, DialogListEntryData dledAtModal, ArrayList<DialogListEntryData> adledToApplyResultsList) {
		boolean bChangesMade=false;
		for(DialogListEntryData<T,?> dledToCfg:adledToApplyResultsList){
			dledToCfg.updateTextTo(dledAtModal.getTextValue());
			bChangesMade=true;
		}
		return bChangesMade;
	}

	protected boolean deleteEntry(ArrayList<DialogListEntryData> adledToApplyResultsList) {
		boolean bChangesMade = false;
		for(DialogListEntryData<T,?> dledToApplyResults:adledToApplyResultsList){
			removeEntry(dledToApplyResults);
			bChangesMade=true;
		}
		
		return bChangesMade;
	}
	
	@Override
	protected DialogStateAbs getDiagChoice(DialogListEntryData dledSelected){
		return cfg.getDiagChoice();
	}
	
	@Override
	protected void actionMainAtEntry(DialogListEntryData<T,?> dledSelected, Spatial sptActionSourceElement) {
		if(cfg.getDiagChoice()!=null){
			super.actionMainAtEntry(dledSelected, sptActionSourceElement);
			openModalDialog(getDiagChoice(dledSelected).getUniqueId(), dledSelected, (T)cmdCfg);
		}else{
			AudioUII.i().playOnUserAction(AudioUII.EAudio.Failure);
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("no choice dialog configured for "+this, dledSelected);
		}
	}

	public class CommandDel implements Command<Button>{
		@SuppressWarnings("unchecked")
		@Override
		public void execute(Button btn) {
			DialogListEntryData<T,?> dled = getDledFrom(btn);
			
			if(dled.isParent()){
//				CustomDialogGUIState.this.setDataToApplyModalChoice(data);
				if(cfg.getDiagQuestion()!=null){
					MaintenanceListLemurDialogStateAbs.this.openModalDialog(cfg.getDiagQuestion().getUniqueId(), dled, (T)this);
					AudioUII.i().play(EAudio.Question);
				}else{
					AudioUII.i().playOnUserAction(AudioUII.EAudio.Failure);
					GlobalCommandsDelegatorI.i().dumpDevWarnEntry("no question dialog configured for "+this, btn, dled);
				}
			}else{
				MaintenanceListLemurDialogStateAbs.this.removeEntry(dled);
			}
		}
	}
	CommandDel cmdDel = new CommandDel();
	
	@Override
	public DialogListEntryData<T,?> addEntryQuick(String strText) {
		DialogListEntryData<T,?> dled = super.addEntryQuick(strText);
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
			actionMainAtEntry(getDledFrom(btn),btn);
//			DialogTestState.this.actionSubmit();
		}
	}
	CommandCfg cmdCfg = new CommandCfg();
	
//	@Override
//	protected MaintenanceListLemurDialogState<T> getThis() {
//		return this;
//	}
//	@Override
//	protected THIS getThis() {
//		return (THIS)this; //this class can be further extended, therefore must be this way... TODO could be better implemented?
//	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=MaintenanceListLemurDialogStateAbs.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=MaintenanceListLemurDialogStateAbs.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}

}
