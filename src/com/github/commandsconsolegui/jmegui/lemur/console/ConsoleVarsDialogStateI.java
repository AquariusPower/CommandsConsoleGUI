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
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.dialog.ChoiceDialogState;
import com.github.commandsconsolegui.jmegui.lemur.dialog.MaintenanceListDialogState;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * @author AquariusPower <https://github.com/AquariusPower>
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
				addEntry(new DialogListEntryData<T>().setText(vcf.getHelp(), vcf));
				addEntry(new DialogListEntryData<T>().setText(vcf.getUniqueVarId(), vcf));
				addEntry(new DialogListEntryData<T>().setText(""+vcf.getValueRaw(), vcf));
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
	
	@Override
	protected void updateList() {
		ArrayList<VarCmdFieldAbs> avcf = VarCmdFieldAbs.getListFullCopy();
		
		ArrayList<DialogListEntryData<T>> adled = getCompleteEntriesListCopy();
		
		for(VarCmdFieldAbs vcf:avcf){
			String strId = vcf.getUniqueVarId(true);
			if(strId==null)continue;
			
			DialogListEntryData<T> dledWork = null;
			for(DialogListEntryData<T> dled:adled){
				VarCmdFieldAbs vcfEntry = ((VarCmdFieldAbs)dled.getUserObj());
				if(vcf.getUniqueVarId().equals(vcfEntry.getUniqueVarId())){
					dledWork = dled;
				}
			}
			
			boolean bCreatedNow = false;
			if(dledWork==null){
//				if(strId!=null){
					dledWork = new DialogListEntryData<T>();
					dledWork.setText(strId, vcf);
					addEntry(dledWork);
//				}
			}
			
//			if(vcf instanceof StringCmdField)
			if(dledWork!=null){
				String strVal=null;
				if(vcf instanceof FloatDoubleVarField){
					strVal = MiscI.i().fmtFloat(((FloatDoubleVarField)vcf).getDouble(),3);
				}else
				if(vcf instanceof IntLongVarField){
					strVal = ""+((IntLongVarField)vcf).getLong();
				}else
				if(vcf instanceof BoolTogglerCmdField){
					strVal = ""+((BoolTogglerCmdField)vcf).getBool();
				}else
				if(vcf instanceof StringVarField){
					strVal = ((StringVarField)vcf).getStringValue();
					if(strVal==null)strVal="";
					if(strVal.length()>10)strVal=strVal.substring(0, 10)+"..."; //TODO use 3 dots single character if it exists..
				}else
				if(vcf instanceof TimedDelayVarField){
					strVal = MiscI.i().fmtFloat(((TimedDelayVarField)vcf).getDelayLimitSeconds(),3);
				}else
				if(vcf instanceof StringCmdField){
					// do nothing for now...
				}else{
					throw new PrerequisitesNotMetException("still unsupported type", vcf.getClass());
				}
				
				if(!bCreatedNow){
					dledWork.clearCustomButtonActions();
				}
				dledWork.addCustomButtonAction(strVal, (T)cv);
			}
			
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
