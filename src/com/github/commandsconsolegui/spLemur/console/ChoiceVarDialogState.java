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

package com.github.commandsconsolegui.spLemur.console;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RefHolder;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spCmd.varfield.ManageVarCmdFieldI;
import com.github.commandsconsolegui.spCmd.varfield.NumberVarFieldAbs;
import com.github.commandsconsolegui.spCmd.varfield.StringVarField;
import com.github.commandsconsolegui.spCmd.varfield.VarCmdFieldAbs;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData.SliderValueData.ESliderKey;
import com.github.commandsconsolegui.spLemur.dialog.ChoiceLemurDialogStateAbs;
import com.github.commandsconsolegui.spLemur.extras.CellRendererDialogEntry;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

/**
 * cannot be extended for getThis() trick to work
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <ACT>
 */
public final class ChoiceVarDialogState<ACT extends Command<Button>> extends ChoiceLemurDialogStateAbs<ACT,ChoiceVarDialogState<ACT>> {
	public static class CfgParm extends ChoiceLemurDialogStateAbs.CfgParm{
		public CfgParm(Float fDialogWidthPercentOfAppWindow,
				Float fDialogHeightPercentOfAppWindow,
				Float fInfoHeightPercentOfDialog, Float fEntryHeightMultiplier) {
			super(fDialogWidthPercentOfAppWindow, fDialogHeightPercentOfAppWindow,
					fInfoHeightPercentOfDialog, fEntryHeightMultiplier);
		}
	}
	CfgParm cfg;
	
	public ChoiceVarDialogState() {
//		DelegateManagerI.i().addManager(this, VarCmdFieldAbs.class);
	}
	
	@Override
	public ChoiceVarDialogState<ACT> configure(ICfgParm icfg) {
		this.cfg=(CfgParm)icfg;
		
		ManageVarCmdFieldI.i().addVarAllowedSetter(this);
//		ManageVarCmdFieldI.i().putVarManager(this,VarCmdFieldAbs.class);
		
		super.configure(icfg);
		
		return getThis();
	}
	
	private VarCmdFieldAbs	vcf;
	private DialogListEntryData<ACT,VarCmdFieldAbs>	dledAtParent;
//	private FileChoiceDialogState	diagFile;
	
//	@Override
//	public boolean doItAllProperly(CompositeControl cc, float tpf) {
////		if(isTryingToEnable()){
////			int i=2;int i2=i;
////		}
//		return super.doItAllProperly(cc, tpf);
//	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected boolean enableAttempt() {
		ArrayList<DialogListEntryData> adledAtParentList = getParentReferencedDledListCopy();
		if(adledAtParentList.size()==0){
			GlobalCommandsDelegatorI.i().dumpWarnEntry("no entry selected at parente dialog", this, getParentDialog());
			cancelEnableRequest();
			return false;
		}
		if(!super.enableAttempt())return false;
		
		dledAtParent = adledAtParentList.get(0);
		Object objUser = dledAtParent.getLinkedObj();
		vcf=null; 
		if(objUser instanceof VarCmdFieldAbs){
			vcf = (VarCmdFieldAbs)objUser;
			hchVar = new RefHolder(vcf.getRawValue());
		}else{
			throw new PrerequisitesNotMetException("user object is not "+VarCmdFieldAbs.class, objUser, dledAtParent);
		}
		
//		if(vcf instanceof FileVarField){
//			FileVarField flVar = ((FileVarField)vcf);
//			addModalDialog(FileChoiceDialogStateI.i());
//			FileChoiceDialogStateI.i().setInitiallyChosenFile(flVar.getFile());
//			FileChoiceDialogStateI.i().requestEnable();
//		}
		
		btgSortListEntries.setBoolean(false);
		
		return true;
	}
	
	@Override
	protected String getTextInfo() {
		String str="";
		
		str+="Help("+vcf.getClass().getSimpleName()+"):\n";
		str+="\t"+(vcf.getHelp()==null ? "(no help)" : vcf.getHelp())+"\n";
		
		str+="Help("+VarsDialogStateI.class.getSimpleName()+"):\n";
		str+="\tList and manage all console variables for all class listeners.\n";
		
		str+=super.getTextInfo();
		
		return str;
	}
	
	@Override
	public ChoiceVarDialogState<ACT> copyToSelfValuesFrom(ChoiceVarDialogState<ACT> discarding) {
		super.copyToSelfValuesFrom(discarding);
		
		PrerequisitesNotMetException.assertNotNull(discarding.vcf,"var");
		this.vcf=discarding.vcf;
		
		return getThis();
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
	private DialogListEntryData<ACT,VarCmdFieldAbs>	dledRawValue;
	private boolean	bListIsFilled;
	private DialogListEntryData<ACT,VarCmdFieldAbs>	dledVals;
	private DialogListEntryData<ACT,VarCmdFieldAbs>	dledInfo;
	private RefHolder	hchVar;
	private DialogListEntryData<ACT,VarCmdFieldAbs>	dledValue;
	protected boolean	bUpdateFromSliderChange;
	
	private void createInfoEntries(){
		DialogListEntryData<ACT,VarCmdFieldAbs> dledNew = null;
		
		dledNew = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
		dledNew.setText(vcf.getUniqueVarId(), vcf);
		dledNew.setParent(dledInfo);
		addEntry(dledNew).addCustomButtonAction("UniqueId",getCmdDummy());
		
		dledNew = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
		dledNew.setText(vcf.getSimpleId(), vcf);
		dledNew.setParent(dledInfo);
		addEntry(dledNew).addCustomButtonAction("SimpleId",getCmdDummy());
			
		dledNew = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
		dledNew.setText(vcf.getHelp(), vcf);
		dledNew.setParent(dledInfo);
		addEntry(dledNew).addCustomButtonAction("Help",getCmdDummy());
	}
	
	private void createValueEntries(){
		DialogListEntryData<ACT,VarCmdFieldAbs> dledNew = null;
		
		dledNew = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
		dledNew.setText(vcf.getRawValueDefault(), vcf);
		dledNew.setAddVisibleQuotes(vcf instanceof StringVarField);
		dledNew.setParent(dledVals);
		addEntry(dledNew).addCustomButtonAction("DefaultValueRaw->",(ACT)cavai);
		
		dledRawValue = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
		dledRawValue.setText(vcf.getRawValue(), vcf);
		dledRawValue.setAddVisibleQuotes(vcf instanceof StringVarField);
		dledRawValue.setParent(dledVals);
		addEntry(dledRawValue).addCustomButtonAction("ValueRaw->",(ACT)cavai);
		
		dledValue = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
		dledValue.setText(vcf.getValueAsString(3), vcf);
		dledValue.setAddVisibleQuotes(vcf instanceof StringVarField);
		dledValue.setParent(dledVals);
		addEntry(dledValue).addCustomButtonAction("Value->",(ACT)cavai);
		
		if(vcf instanceof NumberVarFieldAbs){
			createValueNumberEntries();
		}
	}
	

	/**
	 * see {@link CellRendererDialogEntry#updateVisibleText()}
	 */
	private void updateValueEntries(){
		dledRawValue.updateTextTo(vcf.getRawValue());
		dledValue.updateTextTo(vcf.getValueAsString(3));
	}
	
	private void createValueNumberEntries(){
		DialogListEntryData<ACT,VarCmdFieldAbs> dledNew = null;
		
		final NumberVarFieldAbs<Number,?> varn = (NumberVarFieldAbs<Number,?>)vcf;
		
		if(varn.getMin()!=null && varn.getMax()!=null){
			dledNew = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
			dledNew.setParent(dledVals);
			addEntry(dledNew.setText(varn.getMin(), vcf)).addCustomButtonAction("MinValue->",(ACT)cavai);
			
			dledNew = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
			dledNew.setParent(dledVals);
			addEntry(dledNew.setText(varn.getMax(), vcf)).addCustomButtonAction("MaxValue->",(ACT)cavai);
			
			///////////// the slider
			final DialogListEntryData<ACT,VarCmdFieldAbs> dledSlider = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
			
			CallableX caller = new CallableX(this){
				@Override
				public Boolean call() {
//					varn.setObjectRawValue(dledSlider.getSliderForValue().getCurrentValue());
//					varn.setObjectRawValue(getCustomValue(ESliderKey.CurrentValue.s()));
					varn.setNumber((Number)getCustomValue(ESliderKey.CurrentValue.s()));
					bUpdateFromSliderChange=true;
					return true;
				}
			};
			
			dledSlider.setParent(dledVals);
			dledSlider.setText("Change value: ", vcf);
			dledSlider.setSlider(
				varn.getMin().doubleValue(), varn.getMax().doubleValue(), varn.getNumber().doubleValue(),
				caller, true, false);
			addEntry(dledSlider);
		}
	}
	
	@Override
	protected boolean updateAttempt(float tpf) {
		// keep here to help on debug...
		if(!super.updateAttempt(tpf))return false;
		
		return true;
	}
	
	@Override
	protected boolean simpleUpdateVisibleCells(float tpf) {
		if(!super.simpleUpdateVisibleCells(tpf))return false;
		
		if(hchVar.isChangedAndUpdateHash(vcf.getRawValue())){
			updateValueEntries();
			
			if(bUpdateFromSliderChange){
				setInputTextAsUserTypedValue(""+vcf.getRawValue());
				bUpdateFromSliderChange=false;
			}
//			requestRefreshUpdateList();
		}
		
		return true;
	}
	
	@Override
	protected void updateList() {
		if(!bListIsFilled){
//			clearList();
			
			DialogListEntryData<ACT,VarCmdFieldAbs> dledNew = null;
			
			if(dledInfo==null){
				dledInfo = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
				dledInfo.setText("Info:", vcf);
				dledInfo.setTreeExpanded(true);
			}
			addEntry(dledInfo);
			
			createInfoEntries();
			
			if(dledVals==null){
				dledVals = new DialogListEntryData<ACT,VarCmdFieldAbs>(this);
				dledVals.setText("Value:", vcf);
				dledVals.setTreeExpanded(true);
			}
			addEntry(dledVals);
			
			createValueEntries();
			
//			addEntry(
//					new DialogListEntryData<T>(this).setText(vcf.getValueAsString(3), vcf)
//						.setAddVisibleQuotes(vcf instanceof StringVarField)
//				).addCustomButtonAction("CustomTypedReturnValue",(T)cavai);
			
			bListIsFilled=true;
		}
		
		super.updateList();
	}
	
	@Override
	protected void clearList() {
		super.clearList();
		dledInfo.clearChildren();
		dledVals.clearChildren();
		bListIsFilled=false;
	}
	
	@Override
	protected boolean disableAttempt() {
		if(!super.disableAttempt())return false;
		clearList(); //from a previous enable
		setInputText("");
		return true;
	}
	
	@Override
	protected String getDefaultValueToUserModify() {
//		return ""+vcf.getRawValue();
		return dledRawValue.getVisibleText();
	}
	
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=ChoiceVarDialogState.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=ChoiceVarDialogState.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}

	@Override
	protected ChoiceVarDialogState<ACT> getThis() {
		return this;
	}

}
