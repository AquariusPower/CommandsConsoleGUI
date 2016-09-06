/* 
	Copyright (c) 2016, Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/><https://sourceforge.net/u/teike/profile/>
	
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
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.cmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.AudioUII;
import com.github.commandsconsolegui.jmegui.AudioUII.EAudio;
import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.dialog.ChoiceDialogState;
import com.github.commandsconsolegui.jmegui.lemur.dialog.MaintenanceListDialogState;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.VarId;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/><https://sourceforge.net/u/teike/profile/>
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
		
		@SuppressWarnings("rawtypes")
		@Override
		protected boolean enableAttempt() {
			if(!super.enableAttempt())return false;
			
			dledAtParent = getParentReferencedDledListCopy().get(0);
			Object objUser = dledAtParent.getUserObj();
			if(objUser instanceof VarCmdFieldAbs){
				vcf = (VarCmdFieldAbs)objUser;
			}else{
				throw new PrerequisitesNotMetException("user object is not "+VarCmdFieldAbs.class, objUser, dledAtParent);
			}
			
			btgSortListEntries.setObjectRawValue(false);
			
			return true;
		}
		
//		@Override
//		protected void enableSuccess() {
//			super.enableSuccess();
////			setInputText(getUserEnterCustomValueToken()+vcf.getValueAsString(10));
//			setInputText(getUserEnterCustomValueToken()+vcf.getValueRaw());
//		}
		
		@Override
		protected String getTextInfo() {
			String str="";
			
			str+="Help("+vcf.getClass().getSimpleName()+"):\n";
			str+="\t"+(vcf.getHelp()==null ? "(no help)" : vcf.getHelp())+"\n";
			
			str+="Help("+ConsoleVarsDialogStateI.class.getSimpleName()+"):\n";
			str+="\tList and manage all console variables for all class listeners.\n";
			
			str+=super.getTextInfo();
			
			return str;
		}
		
		@Override
		protected void actionSubmit() { //just to help on debug...
			super.actionSubmit();
		}
		
		private class CmdApplyValueAtInput implements Command<Button>{
			@Override
			public void execute(Button source) {
				ChoiceVarDialogState.this.setInputTextAsUserTypedValue(
					ChoiceVarDialogState.this.getDledFrom(source).getVisibleText());
			}
		}
		CmdApplyValueAtInput cavai = new CmdApplyValueAtInput();
		private DialogListEntryData<T>	dledRawValue;
		
		@Override
		protected void updateList() {
			clearList();
			
			addEntry(new DialogListEntryData<T>(this).setText(vcf.getUniqueVarId(), vcf))
				.addCustomButtonAction("UniqueId",getCmdDummy());
			
			addEntry(new DialogListEntryData<T>(this).setText(vcf.getSimpleCmdId(), vcf))
				.addCustomButtonAction("SimpleId",getCmdDummy());
				
			addEntry(new DialogListEntryData<T>(this).setText(vcf.getHelp(), vcf))
				.addCustomButtonAction("Help",getCmdDummy());
			
			addEntry(
				new DialogListEntryData<T>(this).setText(vcf.getRawValueDefault(), vcf)
					.setAddVisibleQuotes(vcf instanceof StringVarField)
			).addCustomButtonAction("DefaultValueRaw->",(T)cavai);
			
			dledRawValue = 
				new DialogListEntryData<T>(this).setText(vcf.getRawValue(), vcf)
					.setAddVisibleQuotes(vcf instanceof StringVarField);
			addEntry(dledRawValue).addCustomButtonAction("ValueRaw->",(T)cavai);
			
			addEntry(
				new DialogListEntryData<T>(this).setText(vcf.getValueAsString(3), vcf)
					.setAddVisibleQuotes(vcf instanceof StringVarField)
			).addCustomButtonAction("Value->",(T)cavai);
			
			if(vcf instanceof FloatDoubleVarField){
				FloatDoubleVarField v = (FloatDoubleVarField)vcf;
				
				if(v.getMin()!=null){
					addEntry(
						new DialogListEntryData<T>(this).setText(v.getMin(), vcf)
					).addCustomButtonAction("MinValue->",(T)cavai);
				}
				
				if(v.getMax()!=null){
					addEntry(
						new DialogListEntryData<T>(this).setText(v.getMax(), vcf)
					).addCustomButtonAction("MaxValue->",(T)cavai);
				}
			}else
			if(vcf instanceof IntLongVarField){
				IntLongVarField v = (IntLongVarField)vcf;
				
				if(v.getMin()!=null){
					addEntry(
						new DialogListEntryData<T>(this).setText(v.getMin(), vcf)
					).addCustomButtonAction("MinValue->",(T)cavai);
				}
				
				if(v.getMax()!=null){
					addEntry(
						new DialogListEntryData<T>(this).setText(v.getMax(), vcf)
					).addCustomButtonAction("MaxValue->",(T)cavai);
				}
			}
			
//			addEntry(
//					new DialogListEntryData<T>(this).setText(vcf.getValueAsString(3), vcf)
//						.setAddVisibleQuotes(vcf instanceof StringVarField)
//				).addCustomButtonAction("CustomTypedReturnValue",(T)cavai);
			
			super.updateList();
		}
		
		@Override
		protected boolean disableAttempt() {
			if(!super.disableAttempt())return false;
			vcf=null; 
			clearList(); //from a previous enable
			setInputText("");
			return true;
		}
		
//		@Override
//		protected void actionSubmit() {
//			super.actionSubmit();
//		}
		@Override
		protected String getDefaultValueToUserModify() {
//			return ""+vcf.getRawValue();
			return dledRawValue.getVisibleText();
		}
		
	}
	
	@Override
	protected boolean modifyEntry(BaseDialogStateAbs<T, ?> diagModal,	DialogListEntryData<T> dledAtModal,	ArrayList<DialogListEntryData<T>> adledAtThisToApplyResultsList) {
		String strUserTypedValue = diagModal.getInputTextAsUserTypedValue();
		if(strUserTypedValue==null)return false; //was a filter
		
		if(strUserTypedValue.equals(""+null))strUserTypedValue=null; //user requested value to become null
		
		// must be after "null" string check above
		strUserTypedValue=MiscI.i().removeQuotes(strUserTypedValue);
		
		boolean bChangesMade=false;
		for(DialogListEntryData<T> dledToCfg:adledAtThisToApplyResultsList){
			VarCmdFieldAbs vcf = (VarCmdFieldAbs)dledToCfg.getUserObj();
			vcf.setObjectRawValue(strUserTypedValue);
			bChangesMade=true;
		}
		
		return bChangesMade;
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
		chd.setInputToUserEnterCustomValueMode(true);
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
		Object objUser = dledSelected.getUserObj();
		if(objUser instanceof BoolTogglerCmdField){
			changeValue(dledSelected);
		}else
		{
			if(objUser instanceof VarCmdFieldAbs){
			super.actionCustomAtEntry(dledSelected);
			}else{
				throw new PrerequisitesNotMetException("user object not a var", objUser, dledSelected, this);
			}
		}
	}
	
	private String strParentDeclaringClassKey="ParentDeclaringClass";
	private String strParentConcreteClassKey ="ParentConcreteClass";
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void updateList() {
		//clearList()
		ArrayList<VarCmdFieldAbs> avcf = VarCmdFieldAbs.getListFullCopy();
		
		for(VarCmdFieldAbs vcf:avcf){
			if(vcf.getUniqueVarId()==null)continue;
			
			// check if already at the list
			DialogListEntryData<T> dledVar = null;
			for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
				if(dled.getUserObj()==strParentDeclaringClassKey)continue;
				if(dled.getUserObj()==strParentConcreteClassKey)continue;
				VarCmdFieldAbs vcfAtListEntry = ((VarCmdFieldAbs)dled.getUserObj());
				if(vcf.getUniqueVarId().equals(vcfAtListEntry.getUniqueVarId())){
					dledVar = dled;
				}
			}
			
			// create new if not at list
			if(dledVar==null)dledVar=createNewVarEntry(vcf);
			
			// truncate value string
			String strVal=null;
			if(vcf.getRawValue()==null){
				strVal=""+null;
			}else{
				strVal=vcf.getValueAsString(3);
	//			if(strVal==null)strVal="";
				if(vcf instanceof StringVarField){
					strVal='"'+strVal+'"';
					if(strVal.length()>10){
						String strEtc="+"; //TODO use 3 dots single character if it exists or some other symbol?
						strVal=strVal.substring(0, 10-strEtc.length())+strEtc;
					}
				}
			}
			
			// update custom buttons as values may have changed
			//TODO compare if values changed?
			dledVar.clearCustomButtonActions();
			dledVar.addCustomButtonAction(strVal, (T)cv);
		}
		
		super.updateList();
	}
	
	/**
	 * For each var (field) there is only one declaring class (the super or concrete one).
	 * But, each concrete class can have many super classes, the declaring ones.
	 * So the top parent (root list entries) must be the concrete class!
	 * @param vcf
	 * @return
	 */
	private DialogListEntryData<T> createNewVarEntry(VarCmdFieldAbs vcf){
		VarId vidCopy = vcf.getIdTmpCopy();
		
		DialogListEntryData<T> dledVarEntryParent = null;
		
		if(vidCopy.getConcreteClassSName().equals(vidCopy.getDeclaringClassSName())){
			dledVarEntryParent = createParentEntry(
					vidCopy.getDeclaringClassSName(),
					strParentDeclaringClassKey);
		}else{
			DialogListEntryData<T> dledDeclaringClassParent = createParentEntry(
				vidCopy.getConcreteClassSName()+vidCopy.getDeclaringClassSName(),
				strParentDeclaringClassKey);
			
			DialogListEntryData<T> dledConcreteClassParent = createParentEntry(
				vidCopy.getConcreteClassSName(),
				strParentConcreteClassKey);
			
			if(dledDeclaringClassParent.getParent()==null){
				dledDeclaringClassParent.setParent(dledConcreteClassParent);
			}else{
				if(dledDeclaringClassParent.getParent()!=dledConcreteClassParent){
					throw new PrerequisitesNotMetException("Cannot re-parent a declaring class list entry", dledDeclaringClassParent, dledDeclaringClassParent.getParent(), dledConcreteClassParent);
				}
			}
			
			dledVarEntryParent = dledDeclaringClassParent;
		}
		
		// prepare var linked one
		String strVarId = vcf.getUniqueVarId(true);
		strVarId=strVarId.replaceFirst(vidCopy.getDeclaringClassSName(), "");
		strVarId=strVarId.replaceFirst(vidCopy.getConcreteClassSName(), "");
		
		DialogListEntryData<T> dledVar = new DialogListEntryData<T>(this);
		dledVar.setText(strVarId, vcf);
		dledVar.setParent(dledVarEntryParent);
		addEntry(dledVar);
		
		return dledVar;
	}
	
	private DialogListEntryData<T> createParentEntry(String strParentTextKey, String strParentKindKey){
		// prepare declaring class as tree parent  
		DialogListEntryData<T> dledParent=null;
		for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
			if(dled.getTextValue().equals(strParentTextKey)){
				dledParent=dled;
				break;
			}
		}
		if(dledParent==null){
			dledParent=new DialogListEntryData<T>(this);
			dledParent.setText(strParentTextKey,strParentKindKey);
			addEntry(dledParent);
		}
		
		return dledParent;
	}
	
	@Override
	protected boolean initAttempt() {
		if(!super.initAttempt())return false;
		
//		prepareListData();
		
		return true;
	}
}
