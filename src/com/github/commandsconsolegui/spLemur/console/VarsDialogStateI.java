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

package com.github.commandsconsolegui.spLemur.console;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.PkgTopRef;
import com.github.commandsconsolegui.spAppOs.globals.GlobalManageKeyBindI;
import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spCmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.spCmd.varfield.FileVarField;
import com.github.commandsconsolegui.spCmd.varfield.KeyBoundVarField;
import com.github.commandsconsolegui.spCmd.varfield.ManageVarCmdFieldI;
import com.github.commandsconsolegui.spCmd.varfield.StringVarField;
import com.github.commandsconsolegui.spCmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.spCmd.varfield.VarCmdUId;
import com.github.commandsconsolegui.spJme.AudioUII;
import com.github.commandsconsolegui.spJme.AudioUII.EAudio;
import com.github.commandsconsolegui.spJme.DialogStateAbs;
import com.github.commandsconsolegui.spJme.ManageConditionalStateI;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData;
import com.github.commandsconsolegui.spLemur.dialog.MaintenanceListLemurDialogStateAbs;
import com.jme3.input.KeyInput;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/><https://sourceforge.net/u/teike/profile/>
 */
public class VarsDialogStateI<T extends Command<Button>> extends MaintenanceListLemurDialogStateAbs<T,VarsDialogStateI<T>> {
	private static VarsDialogStateI<Command<Button>>	instance=new VarsDialogStateI<Command<Button>>();
	public static VarsDialogStateI<Command<Button>> i(){return instance;}
	
	private final KeyBoundVarField bindToggleEnable = new KeyBoundVarField(this);
	
	@Override
	protected boolean modifyEntry(DialogStateAbs<T, ?> diagModal,	DialogListEntryData<T> dledAtModal,	ArrayList<DialogListEntryData<T>> adledAtThisToApplyResultsList) {
		String strUserTypedValue = diagModal.getInputTextAsUserTypedValue();
		if(strUserTypedValue==null)return false; //was a filter
		
		if(strUserTypedValue.equals(""+null))strUserTypedValue=null; //user requested value to become null
		
		// must be after "null" string check above
		if(strUserTypedValue!=null){
			strUserTypedValue=MiscI.i().removeQuotes(strUserTypedValue);
		}
		
		// can be null
		boolean bChangesMade=false;
		for(DialogListEntryData<T> dledToCfg:adledAtThisToApplyResultsList){
			VarCmdFieldAbs vcf = (VarCmdFieldAbs)dledToCfg.getUserObj();
			vcf.setObjectRawValue(strUserTypedValue);
			bChangesMade=true;
		}
		
		return bChangesMade;
	}
	
//	private HoldRestartable<ChoiceVarDialogState> hrdiagChoice = new HoldRestartable<ChoiceVarDialogState>(this);
	
	public static class CfgParm extends MaintenanceListLemurDialogStateAbs.CfgParm{
		public CfgParm(Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier) {
			super(fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, fEntryHeightMultiplier, null, null);
		}
	}
	private CfgParm	cfg;
	@Override
	public VarsDialogStateI<T> configure(ICfgParm icfg) {
		cfg = (CfgParm)icfg;
		
		ManageVarCmdFieldI.i().addVarAllowedSetter(this);
		
//		if(hrdiagChoice.getRef()==null){ 
			//when restarting this, it's child may be already/still there TODO restart child too?
			ChoiceVarDialogState diagChoice = ManageConditionalStateI.i().getConditionalState(
				ChoiceVarDialogState.class, null);
			if(diagChoice==null){
				diagChoice=new ChoiceVarDialogState();
				diagChoice.configure(new ChoiceVarDialogState.CfgParm(0.9f, 0.5f, 0.5f, null));//.setId(strId));
				diagChoice.setInputToUserEnterCustomValueMode(true);
			}
//			hrdiagChoice.setRef(diagChoice);
//		}
		
		cfg.setDiagChoice(diagChoice);
//		cfg.setDiagChoice(hrdiagChoice.getRef());
		
		cfg.setDiagQuestion(null);
		
		super.configure(cfg);
		
		bindToggleEnable.setObjectRawValue(KeyInput.KEY_F9)
			.setCallerAssigned(new CallableX(this) {
				@Override
				public Boolean call() {
//					if(!ConsoleVarsDialogStateI.this.isConfigured())return false;
//					if(!ConsoleVarsDialogStateI.this.isInitializedProperly())return false;
					
					requestToggleEnabled();
					
					return true;
				}
			});
		
		storeCfgAndReturnSelf(cfg);
		return getThis();
	}
	
	@Override
	protected VarsDialogStateI<T> getThis() {
		return this;
	}
	
	protected class CmdBtnChangeValue implements Command<Button>{
		@Override
		public void execute(Button source) {
			VarsDialogStateI.this.actionCustomAtEntry(
				VarsDialogStateI.this.getDledFrom(source));
//			ConsoleVarsDialogStateI.this.changeValue(
//				ConsoleVarsDialogStateI.this.getDledFrom(source));
		}
	}
	private CmdBtnChangeValue cbcv = new CmdBtnChangeValue();
	
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
		
		if(bChanged)requestRefreshUpdateList();
	}
	
	@Override
	protected void actionCustomAtEntry(DialogListEntryData<T> dledSelected) {
		Object objUser = dledSelected.getUserObj();
		if(objUser instanceof BoolTogglerCmdField){
			changeValue(dledSelected);
		}else
		if(objUser instanceof KeyBoundVarField){
			GlobalManageKeyBindI.i().captureAndSetKeyBindAt((KeyBoundVarField)objUser, this);
//			requestRefreshUpdateList();
		}else
		{
			if(objUser instanceof VarCmdFieldAbs){
				super.actionCustomAtEntry(dledSelected);
			}else
			if(objUser instanceof EGroupKeys){
				// skipper, this is actually an empty group (no tree expanded/shrinked)
			}else{
				throw new PrerequisitesNotMetException("user object not a var", objUser, dledSelected, this);
			}
		}
	}
	
	enum EGroupKeys{
		ParentDeclaringClass,
		ParentConcreteClass,
		DialogsCons,
		Dialogs,
		PkgTopRef,
		;
	}
	private DialogListEntryData<T>	dledConsProjPackage;
	private DialogListEntryData<T>	dledDiagsCons;
	private DialogListEntryData<T>	dledDiags;
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void updateList() {
		//clearList()
		ArrayList<VarCmdFieldAbs> avcf = ManageVarCmdFieldI.i().getHandledListCopy();
		
		for(VarCmdFieldAbs vcfVarEntry:avcf){
			if(vcfVarEntry.getUniqueVarId()==null)continue;
			
			// check if already at the list
			DialogListEntryData<T> dledVarEntry = null;
			for(DialogListEntryData<T> dled:getCompleteEntriesListCopy()){
				Object obj = dled.getUserObj();
				if (obj instanceof EGroupKeys)continue;
				VarCmdFieldAbs vcfAtListEntry = ((VarCmdFieldAbs)obj);
				if(vcfVarEntry.getUniqueVarId().equals(vcfAtListEntry.getUniqueVarId())){
					dledVarEntry = dled;
				}
			}
			
			// create new if not at list
			if(dledVarEntry==null){
				dledVarEntry=createNewVarEntry(vcfVarEntry); //will return with parent already set
				
				// will set the package entry as the rootEntry's parent
				String strVarConcrPkg = vcfVarEntry.getIdTmpCopy().getConcreteClass().getPackage().getName();
				boolean bConsProj = strVarConcrPkg.startsWith(PkgTopRef.class.getPackage().getName());
				if (vcfVarEntry.getOwner() instanceof DialogStateAbs) {
					setParentestParent(dledVarEntry, bConsProj ? dledDiagsCons : dledDiags);
				}else{
					if(bConsProj){
						setParentestParent(dledVarEntry, dledConsProjPackage);
					}
				}
			}
			
			// truncate value string
			String strVal=null;
			int iShortPreviewValueStringSizeLimit=10;
			if(vcfVarEntry.isValueNull()){
				strVal=""+null;
			}else{
				strVal=vcfVarEntry.getValueAsString(3);
	//			if(strVal==null)strVal="";
				String strEtc="+"; //TODO use 3 dots single character if it exists or some other symbol?
				if(vcfVarEntry instanceof StringVarField){
					strVal='"'+strVal+'"';
				}else
				if(vcfVarEntry instanceof FileVarField){
					if(strVal.length()>iShortPreviewValueStringSizeLimit){
						String[] a = strVal.split(File.separator);
						strVal=a[a.length-1];
					}
				}
				
				if(strVal.length()>iShortPreviewValueStringSizeLimit){
					strVal=strVal.substring(0, iShortPreviewValueStringSizeLimit-strEtc.length())+strEtc;
				}
			}
			
			// update custom buttons as values may have changed
			//TODO compare if values changed?
			dledVarEntry.clearCustomButtonActions();
			dledVarEntry.addCustomButtonAction(strVal, (T)cbcv);
		}
		
		
		super.updateList(); //MiscI.i().getClassTreeFor(this)
	}
	
	private void setParentestParent(DialogListEntryData<T> dledEntry, DialogListEntryData<T> dledParent){
		DialogListEntryData<T> dledParentest = dledEntry.getParentest();
		
		if (dledParentest.getUserObj() instanceof EGroupKeys) {
			EGroupKeys egk = (EGroupKeys) dledParentest.getUserObj();
			switch(egk){
				case PkgTopRef:
				case DialogsCons:
				case Dialogs:
					// constant top groups are already setup at initialization
					return; 
			}
		}
		
		if(dledParentest!=dledParent){
			dledParentest.setParent(dledParent);
		}
	}
	
	/**
	 * For each var (field) there is only one declaring class (the super or concrete one).
	 * But, each concrete class can have many super classes, the declaring ones.
	 * So the top parent (root list entries) must be the concrete class!
	 * @param vcf
	 * @return
	 */
	private DialogListEntryData<T> createNewVarEntry(VarCmdFieldAbs vcf){
		VarCmdUId vcuidCopy = vcf.getIdTmpCopy();
		
		DialogListEntryData<T> dledVarEntryParent = null;
		
		if(vcuidCopy.getConcreteClassSimpleName().equals(vcuidCopy.getDeclaringClassSimpleName())){
			dledVarEntryParent = createParentEntry(
					vcuidCopy.getDeclaringClassSimpleName(),
					EGroupKeys.ParentDeclaringClass);
		}else{
			DialogListEntryData<T> dledDeclaringClassParent = createParentEntry(
				vcuidCopy.getConcreteClassSimpleName()+vcuidCopy.getDeclaringClassSimpleName(),
				EGroupKeys.ParentDeclaringClass);
			
			DialogListEntryData<T> dledConcreteClassParent = createParentEntry(
				vcuidCopy.getConcreteClassSimpleName(),
				EGroupKeys.ParentConcreteClass);
			
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
		strVarId=strVarId.replaceFirst(vcuidCopy.getDeclaringClassSimpleName(), "");
		strVarId=strVarId.replaceFirst(vcuidCopy.getConcreteClassSimpleName(), "");
		
		DialogListEntryData<T> dledVar = new DialogListEntryData<T>(this);
		String strExtraInfo="";
		if(vcf instanceof FileVarField){
			strExtraInfo="(file)";
		}
		dledVar.setText(strExtraInfo+strVarId, vcf);
		dledVar.setParent(dledVarEntryParent);
		addEntry(dledVar);
		
		return dledVar;
	}
	
	private DialogListEntryData<T> createParentEntry(String strParentTextKey, EGroupKeys egk){
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
			dledParent.setText(strParentTextKey,egk);
			addEntry(dledParent);
		}
		
		return dledParent;
	}
	
	@Override
	protected boolean initAttempt() {
		if(!super.initAttempt())return false;
		
		// the top package as parent:
		dledConsProjPackage = new DialogListEntryData<T>(this);
		dledConsProjPackage.setText("(Package: "+PkgTopRef.class.getPackage().getName()+")", EGroupKeys.PkgTopRef);//PkgTopRef.class);
		
		dledDiagsCons = new DialogListEntryData<T>(this);
		dledDiagsCons.setText("(DialogsCons)",EGroupKeys.DialogsCons);
		dledDiagsCons.setParent(dledConsProjPackage);
		
		dledDiags = new DialogListEntryData<T>(this);
		dledDiags.setText("(Dialogs)",EGroupKeys.Dialogs);
		
		return true;
	}
	
	@Override
	public VarsDialogStateI<T> copyToSelfValuesFrom(VarsDialogStateI<T> casDiscarding) {
		cfg.setRestartCopyToSelfAllEntries(false);
		
		super.copyToSelfValuesFrom(casDiscarding);
		
		return getThis();
	}
	
	@Override
	protected String getTextInfo() {
		String str="";
		
		str+="Help("+VarsDialogStateI.class.getSimpleName()+"):\n";
		str+="\tAll console variables are accessible thru this dialog.\n";
		
		return str+super.getTextInfo();
	}
	
	@Override
	protected void initSuccess() {
		super.initSuccess();
		addEntry(dledConsProjPackage);
		addEntry(dledDiagsCons);
		addEntry(dledDiags);
	}
	
//	@Override
//	public void focusGained() {
//		super.focusGained();
//		requestRefreshList();
//	}
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=VarsDialogStateI.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=VarsDialogStateI.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}
	
	@Override
	public void requestRefresh() {
		requestRefreshUpdateList();
	}

}
