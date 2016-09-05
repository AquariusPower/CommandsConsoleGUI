/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower>
	
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

package com.github.commandsconsolegui.jmegui.lemur.console;

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.cmd.varfield.TimedDelayVarField;
import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.dialog.ChoiceDialogState;
import com.github.commandsconsolegui.jmegui.lemur.dialog.MaintenanceListDialogState;
import com.github.commandsconsolegui.misc.IdTmp;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower>
 */
public class ConsoleVarsDialogStateI<T extends Command<Button>> extends MaintenanceListDialogState<T> {
	private static ConsoleVarsDialogStateI<Command<Button>>	instance=new ConsoleVarsDialogStateI<Command<Button>>();
	public static ConsoleVarsDialogStateI<Command<Button>> i(){return instance;}
	
	private static class ChoiceVarDialogState<T extends Command<Button>> extends ChoiceDialogState<T>{
		private static class CfgParm extends ChoiceDialogState.CfgParm{
			public CfgParm(Float fDialogWidthPercentOfAppWindow,
					Float fDialogHeightPercentOfAppWindow,
					Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier) {
				super(fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
						fInfoHeightPercentOfDialog, fEntryHeightMultiplier);
			}
		}

		private VarCmdFieldAbs	vcf;
		private DialogListEntryData<T>	dledAtParent;
		
		@Override
		protected boolean enableAttempt() {
			if(!super.enableAttempt())return false;
			
			dledAtParent = getParentReferencedDledListCopy().get(0);
			vcf = (VarCmdFieldAbs)dledAtParent.getUserObj();
			
			btgSortListEntries.setObjectRawValue(false);
			
			return true;
		}
		
		@Override
		protected String getTextInfo() {
			String str="";
			
			str+="Help("+ConsoleVarsDialogStateI.class.getSimpleName()+"):\n";
			if(vcf!=null)str+="\t"+vcf.getHelp()+"\n";
			
			str+=super.getTextInfo();
			
			return str;
		}
		
		@Override
		protected void updateList() {
			clearList();
			
			if(vcf!=null){
				addEntry(new DialogListEntryData<T>().setText("(Help)"+vcf.getHelp(), vcf));
				addEntry(new DialogListEntryData<T>().setText("(UniqueId)"+vcf.getUniqueVarId(), vcf));
				addEntry(new DialogListEntryData<T>().setText("(SimpleId)"+vcf.getSimpleCmdId(), vcf));
				addEntry(new DialogListEntryData<T>().setText("(Value)"+vcf.getValueRaw(), vcf));
			}
			
			super.updateList();
		}
	}
	
	private ChoiceVarDialogState chd = new ChoiceVarDialogState();
	
	public static class CfgParm extends MaintenanceListDialogState.CfgParm{
		public CfgParm(Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier) {
			super(fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, fEntryHeightMultiplier, null, null);
		}
	}
	private CfgParm	cfg;
	@Override
	public ConsoleVarsDialogStateI<T> configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		
		chd.configure(new ChoiceVarDialogState.CfgParm(0.9f, 0.5f, 0.5f, null));//.setId(strId));
		chd.setInputToTypeValueMode(true);
		cfg.setDiagChoice(chd);
		
		cfg.setDiagQuestion(null);
		
		super.configure(cfg);
		
		return storeCfgAndReturnSelf(cfg);
	}
	
	protected class ChangeValue implements Command<Button>{
		@Override
		public void execute(Button source) {
			ConsoleVarsDialogStateI.this.actionCustomAtEntry(
				ConsoleVarsDialogStateI.this.getDledFrom(source));
//			ConsoleVarsDialogStateI.this.changeValue(
//				ConsoleVarsDialogStateI.this.getDledFrom(source));
		}
	}
	private ChangeValue cv = new ChangeValue();
	
	private void changeValue(DialogListEntryData<T> dled){
		AudioUII.i().playOnUserAction(EAudio.SelectEntry);
		
		VarCmdFieldAbs vcf = (VarCmdFieldAbs)dled.getUserObj();
		boolean bChanged=false;
		if(vcf instanceof BoolTogglerCmdField){
			((BoolTogglerCmdField)vcf).toggle();
			bChanged=true;
		}else{
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("not yet supported "+vcf.getClass());
			AudioUII.i().playOnUserAction(EAudio.Failure);				
		}
		
		if(bChanged)requestRefreshList();
	}
	
	@Override
	protected void actionCustomAtEntry(DialogListEntryData<T> dledSelected) {
		if(dledSelected.getUserObj() instanceof BoolTogglerCmdField){
			changeValue(dledSelected);
		}else
		{
			super.actionCustomAtEntry(dledSelected);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void updateList() {
//		clearList(); //TODO @@@Remove this line
		ArrayList<VarCmdFieldAbs> avcf = VarCmdFieldAbs.getListFullCopy();
		
		String strParentDeclaringClass="ParentDeclaringClass";
		for(VarCmdFieldAbs vcf:avcf){
			String strId = vcf.getUniqueVarId(true);
			if(strId==null)continue;
			
			// check if already at the list
			DialogListEntryData<T> dledWork = null;
			for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
				if(dled.getUserObj()==strParentDeclaringClass)continue;
				VarCmdFieldAbs vcfAtListEntry = ((VarCmdFieldAbs)dled.getUserObj());
				if(vcf.getUniqueVarId().equals(vcfAtListEntry.getUniqueVarId())){
					dledWork = dled;
				}
			}
			
			// create new if not at list
			if(dledWork==null){
				IdTmp idCopy = vcf.getIdTmpCopy();
				
				// prepare declaring class as tree parent  
				DialogListEntryData<T> dledDeclaringClassParent=null;
				String strDecl = idCopy.getDeclaringClassSName();
				for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
					if(dled.getText().equals(strDecl)){
						dledDeclaringClassParent=dled;
						break;
					}
				}
				if(dledDeclaringClassParent==null){
					dledDeclaringClassParent=new DialogListEntryData<T>();
					dledDeclaringClassParent.setText(strDecl,strParentDeclaringClass);
					addEntry(dledDeclaringClassParent);
				}
				
				// prepare var linked one
				dledWork = new DialogListEntryData<T>();
				dledWork.setText(strId, vcf);
				dledWork.setParent(dledDeclaringClassParent);
				addEntry(dledWork);
			}
			
			// truncate value string
			String strVal=vcf.getValueAsString(3);
			if(strVal==null)strVal="";
			if(strVal.length()>10)strVal=strVal.substring(0, 10)+"..."; //TODO use 3 dots single character if it exists or some other symbol?
			
			// update custom buttons as values may have changed
			//TODO compare if values changed?
			dledWork.clearCustomButtonActions();
			dledWork.addCustomButtonAction(strVal, (T)cv);
		}
		
		super.updateList();
	}
	
	@Override
	protected boolean initAttempt() {
		if(!super.initAttempt())return false;
		
//		prepareListData();
		
		return true;
	}
}
