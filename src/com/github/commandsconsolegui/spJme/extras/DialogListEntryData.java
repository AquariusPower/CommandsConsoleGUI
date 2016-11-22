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

package com.github.commandsconsolegui.spJme.extras;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import com.github.commandsconsolegui.spAppOs.misc.HoldRestartable;
import com.github.commandsconsolegui.spAppOs.misc.IHasOwnerInstance;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spJme.AudioUII;
import com.github.commandsconsolegui.spJme.DialogStateAbs;
import com.github.commandsconsolegui.spJme.extras.DialogListEntryData.SliderValueData.ESliderKey;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;

/**
 * Each list entry can show differently, they could be:
 * - blank spacer
 * - simple section title
 * - an option that can be cofigured
 * - a sound that can play/pause/stop (buttons)
 * - a check box to simply enable/disable
 * - etc...
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <ACT> is the action class for buttons
 */
public class DialogListEntryData<ACT,LINK> implements Savable,IHasOwnerInstance<DialogStateAbs>{ 
//	private static String strLastUniqueId = "0";
	
	private String	strUniqueId;
	private HoldRestartable<DialogStateAbs> hrdiagOwner = null; //new HoldRestartable<DialogStateAbs>();
	
	public static class CustomAction<ACT>{
		private String strLabelTextId;
		private ACT action;
		private String strPopupHint;
		
		public ACT getAction() {
			return action;
		}
		private void setAction(ACT action) {
			this.action = action;
		}
		public String getPopupHint() {
			return strPopupHint;
		}
		private void setPopupHint(String strPopupHint) {
			this.strPopupHint = strPopupHint;
		}
		public String getLabelTextId() {
			return strLabelTextId;
		}
		private void setLabelTextId(String strLabelTextId) {
			this.strLabelTextId = strLabelTextId;
		}
	}
	
	private HashMap<String,CustomAction<ACT>> hmCustomActions = new HashMap<String,CustomAction<ACT>>();
	private LINK	objLinked;
	
	private DialogListEntryData parent; //it must be set as the parent type too
	private boolean bTreeExpanded = false; 
	private ArrayList<DialogListEntryData<ACT,LINK>> aChildList = new ArrayList<DialogListEntryData<ACT,LINK>>();
	
	public static class SliderValueData{
		private Double dSliderMin;
		private Double dSliderMax;
		private boolean bSliderCallOnChange=false;
		private boolean bSliderIntegerMode=false;
		private CallableX callerSlider;
		private Double dSliderCurrentValue;

		public Double getMinValue() {
			return dSliderMin;
		}

		public Double getMaxValue() {
			return dSliderMax;
		}
		
		public enum ESliderKey{
			CurrentValue,
			;
			public String s(){return this.toString();}
		}
		
		public boolean isCurrentValueEqualTo(double dNewValue) {
			return Double.compare(this.dSliderCurrentValue, dNewValue)==0;
		}
		
		public void setCurrentValue(double dNewValue) {
			if(!isCurrentValueEqualTo(dNewValue)){
				this.dSliderCurrentValue=dNewValue;
				
				if(bSliderCallOnChange){
					doCallOnChange();
				}
			}
		}
		
		public Double getCurrentValue() {
			return dSliderCurrentValue;
		}

//		public void sliderCheckAndCallOnChange() {
//			if(bSliderCallOnChange){
//				sliderCall();
//			}
//		}
		private void doCallOnChange() {
			if(callerSlider!=null){
				callerSlider.putCustomValue(ESliderKey.CurrentValue.s(), getCurrentValue());
				ManageCallQueueI.i().addCall(callerSlider);
			}
		}

		public boolean isIntegerMode() {
			return bSliderIntegerMode;
		}

//		public boolean isShowSlider() {
//			return bShowSlider;
//		}
	}
	private SliderValueData svd = null;
	
//	int iKey;
	private String strText;
//	T objRef;
	
//	public DialogListEntryData() {
//		GlobalDialogHelperI.i().
//		this.strUniqueId = DialogListEntryData.strLastUniqueId = (MiscI.i().getNextUniqueId(strLastUniqueId));
//	}
	public DialogListEntryData(DialogStateAbs diagOwner) {
//		this();
//		this.strUniqueId = DialogListEntryData.strLastUniqueId = (MiscI.i().getNextUniqueId(strLastUniqueId));
		this.strUniqueId = diagOwner.getCreateNextEntryUniqueId();
		setOwner(diagOwner);
//		hrdiagOwner.setRef(diagOwner);
//		this.diagOwner=diagOwner;
	}
	
//	public DialogListEntryData<T> renameCustomButtonAction(T action, String strLabelTextIdNew){
//		for(Entry<String, T> en:hmCustomButtonsActions.entrySet()){
//			if(en.getValue()==)
//		}
//		T action = hmCustomButtonsActions.remove(strLabelTextIdOld);
//		hmCustomButtonsActions.put(strLabelTextIdNew,action);
//		return this;
//	}
	
	/**
	 * 
	 * @param dMin
	 * @param dMax
	 * @param dCurrent
	 * @param callerSlider will store {@link ESliderKey#CurrentValue} with {@link CallableX#putCustomValue(String, Object)}
	 * @param bCallWhileChanging
	 * @param bIntMode
	 * @return
	 */
	public DialogListEntryData<ACT,LINK> setSlider(Double dMin, Double dMax, Double dCurrent, CallableX callerSlider, boolean bCallWhileChanging, boolean bIntMode){
		this.svd = new SliderValueData();
		
		this.svd.dSliderMin=dMin;
		this.svd.dSliderMax=dMax;
		this.svd.dSliderCurrentValue=dCurrent;
		this.svd.callerSlider=callerSlider;
		this.svd.bSliderCallOnChange=bCallWhileChanging;
		this.svd.bSliderIntegerMode=bIntMode;
//		this.svd.bShowSlider=true;
		
		return this;
	}
	
	public SliderValueData getSliderForValue(){
		return svd;
	}
	
	public DialogListEntryData<ACT,LINK> addCustomButtonAction(String strLabelTextId, ACT action){
		return addCustomButtonAction(strLabelTextId,action,null);
	}
	public DialogListEntryData<ACT,LINK> addCustomButtonAction(String strLabelTextId, ACT action,String strPopupHint){
		CustomAction<ACT> ca = new CustomAction<ACT>();
		ca.setLabelTextId(strLabelTextId);
		ca.setAction(action);
		ca.setPopupHint(strPopupHint);
		hmCustomActions.put(strLabelTextId,ca);
		return this;
	}
	public DialogListEntryData<ACT,LINK> clearCustomButtonActions(){
		hmCustomActions.clear();
		return this;
	}
	
//	@SuppressWarnings("unchecked")
//	public Entry<String,T>[] getLabelActionListCopy(){
//		return (Entry<String,T>[])hmLabelAction.entrySet().toArray();
//	}
//	@SuppressWarnings("rawtypes")
	@SuppressWarnings("unchecked")
	public <E extends Entry<String,CustomAction<ACT>>> ArrayList<E> getCustomButtonsActionsListCopy(){
		E[] aen = (E[])hmCustomActions.entrySet().toArray(new Entry[0]);
		return new ArrayList<E>(Arrays.asList(aen));
	}
	
//	public int getKey() {
//		return iKey;
//	}
//	public DialogListEntryData<T> setKey(int iKey) {
//		this.iKey = iKey;
//		return this;
//	}
	
//	private char chQuotesL='"';
//	private char chQuotesR='"';
	private boolean bAddVisibleQuotes=false;

//	private RangedValueModel	modelSliderValue;
	
	public boolean isAddVisibleQuotes(){
		return bAddVisibleQuotes;
	}
	
	public DialogListEntryData<ACT,LINK> setAddVisibleQuotes(boolean b){
		this.bAddVisibleQuotes=b;
		return this;
	}
	
	public String getTextValue() {
		return strText;
	}
	
	/**
	 * if text is null will be set to "null"
	 * if empty or blank, will be surrounded by quotes,
	 * TODO lemur seems to only accept with some actual text?
	 * 
	 * @return
	 */
	public String getVisibleText() {
		if(strText==null){
			return ""+null;
		}
		
		if(strText.trim().isEmpty() || isAddVisibleQuotes()){
			return '"'+strText+'"';
		}
		
		return strText;
	}
	
	public LINK getLinkedObj(){
		return this.objLinked;
	}
	
	/**
	 * @param strText 
	 * @return
	 */
	public DialogListEntryData<ACT,LINK> updateTextTo(Object objText) {
		if(objText == null){
			this.strText=null;
		}else
		if(objText instanceof String){
			this.strText=(String)objText;
		}else{
			this.strText=objText.toString();
		}
		
		return this;
	}
	
	
	/**
	 * 
	 * @param strText an easily/clearly readable info about the linked object
	 * @param objLinked the linked object represented by the text
	 * @return
	 */
	public DialogListEntryData<ACT,LINK> setText(Object objText, LINK objLinked) {
		updateTextTo(objText);
		this.objLinked=objLinked;
		return this;
	}
	
	@Override
	public String toString() {
//		return iKey+","+strText+","+objRef;
		return "UId="+getUId()+","+strText+",childCount="+aChildList.size();
	}
	
//	public void setCfg(DialogListEntryData data) {
////		this.iKey = data.iKey;
//		this.strText = data.strText;
////		this.objRef = data.objRef;
//	}
	
	/**
	 * TODO should be a more refined string
	 * @return
	 */
	public String report() {
		return this.toString();
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public DialogListEntryData<ACT,LINK> getParent() {
		return parent;
	}
	
	/**
	 * 
	 * @param parent if null, will just remove from existing parent
	 * @return
	 */
	public DialogListEntryData<ACT,LINK> setParent(DialogListEntryData<ACT,LINK> parent) {
		if(this==parent)throw new PrerequisitesNotMetException("cant parent self: parent==this", this);
		
		// remove self from previous parent
		DialogListEntryData<ACT,LINK> dledParentOld = this.getParent();
		if(dledParentOld!=null){
			dledParentOld.aChildList.remove(this);
		}
		
		// consistency check
		if(parent!=null && this.hasChild(parent)){
			throw new PrerequisitesNotMetException("cant be child of a child", this, parent, this.aChildList);
		}
		
		// parent can be null
		this.parent = parent;
		
		// adds self to new parent child list
		if(this.getParent()!=null){
			if(!this.getParent().aChildList.contains(this)){
				this.getParent().aChildList.add(this);
			}
		}
		
		return this;
	}
	
	public boolean hasChild(DialogListEntryData<ACT,LINK> dledChildToCheck){
		if(dledChildToCheck==null){
			throw new PrerequisitesNotMetException("null child to check", this);
		}
		
		for(DialogListEntryData<ACT,LINK> dledChild:this.aChildList){
			if(dledChild==dledChildToCheck)return true;
			
			if(dledChild.aChildList.size()>0){
				if(dledChild.hasChild(dledChildToCheck))return true;
			}
		}
		
		return false;
	}

//	public EEntryType geteType() {
//		return eType;
//	}
//
//	public DialogListEntryData<T> setType(EEntryType eType) {
//		this.eType = eType;
//		return this;
//	}

	public boolean isTreeExpanded() {
		return bTreeExpanded;
	}

	public DialogListEntryData<ACT,LINK> setTreeExpanded(boolean bTreeExpanded) {
		this.bTreeExpanded = bTreeExpanded;
		return this;
	}
	
	public ArrayList<DialogListEntryData<ACT,LINK>> getAllParents(){
		ArrayList<DialogListEntryData<ACT,LINK>> adledParentList = new ArrayList<DialogListEntryData<ACT,LINK>>();
		
		DialogListEntryData<ACT,LINK> dledParent = getParent();
		while(dledParent!=null){
			adledParentList.add(dledParent);
			dledParent = dledParent.getParent();
		}
		
		return adledParentList;
	}
	
	public boolean toggleExpanded() {
//		if(!isParent())return false;
		
		this.bTreeExpanded = !this.bTreeExpanded;
		
		if(this.bTreeExpanded){
			for(DialogListEntryData<ACT,LINK> dledParent:getAllParents()){
				dledParent.setTreeExpanded(true);
			}
		}
		
		getOwner().requestRefreshUpdateList();
//		diagOwner.requestRefreshUpdateList();
//		cell.assignedCellRenderer.diagParent.requestRefreshList();
		
		AudioUII.i().playOnUserAction(isTreeExpanded() ?
				AudioUII.EAudio.ExpandSubTree : AudioUII.EAudio.ShrinkSubTree);
		
		return this.bTreeExpanded; 
	}
	
	public void setOwner(DialogStateAbs diag){
		if(getParent()==null){ // this is root
			if(this.hrdiagOwner==null)this.hrdiagOwner=new HoldRestartable<DialogStateAbs>(this);
			
			DialogStateAbs diagOwner = this.hrdiagOwner.getRef();
			if(diagOwner==null){
				this.hrdiagOwner.setRef(diag);
			}else
			if(diagOwner==diag){
				// just ignore
			}else
			{
				throw new PrerequisitesNotMetException("cannot change the diag owner this way, see "+HoldRestartable.class);
			}
		}else{
			/**
			 * this way will lower the holders amount!
			 */
			getParentest().setOwner(diag); //mainly to let it be validated, but can happen to be actually setting also
			if(this.hrdiagOwner!=null){
				this.hrdiagOwner.discardSelf(getParentest().hrdiagOwner);
				this.hrdiagOwner=null;
//				this.hrdiagOwner.requestDiscardSelf(this,diag);
			}
		}
	}
	@Override
	public DialogStateAbs getOwner(){
		if(getParent()==null){ //root one
			return hrdiagOwner.getRef();
		}
		
		if(hrdiagOwner!=null){
//			MsgI.i().warn("inconsistent, only root one should have owner", this, hrdiagOwner);
			MsgI.i().devInfo("cleaning child ("+getUId()+":"+getVisibleText()+") diagOwner ref ("+hrdiagOwner.getRef().getUniqueId()+"), only root one needs it", this, hrdiagOwner);
			hrdiagOwner=null;
		}
		
		return getParent().getOwner();
//		return getParentest().hrdiagOwner.get();
//		return hrdiagOwner.get();
	}
	
	public boolean isParent() {
		return aChildList.size()>0;
	}

	public ArrayList<DialogListEntryData> getChildrenCopy() {
		return new ArrayList<DialogListEntryData>(aChildList);
	}
	
	public String getUId() {
		return strUniqueId;
	}

	public DialogListEntryData<ACT,LINK> clearChildren() {
		aChildList.clear();
		return this;
	}
	
	/**
	 * Returns root one (parent less). 
	 * 
	 * @return null if it has no parent
	 */
	public DialogListEntryData<ACT,LINK> getParentest() {
		DialogListEntryData<ACT,LINK> dledParent = getParent();
		if(dledParent==null)return null;
		
//		ArrayList<DialogListEntryData<T>> adledConsistencyChk = new ArrayList<DialogListEntryData<T>>();
		while(true){
			DialogListEntryData<ACT,LINK> dledParentTmp = dledParent.getParent();
			if(dledParentTmp==null)return dledParent;
			
//			if(adledConsistencyChk.contains(dledParentTmp)){
//				throw new PrerequisitesNotMetException("tree is inconsistent (child/parent loop)", adledConsistencyChk, dledParentTmp);
//			}else{
//				adledConsistencyChk.add(dledParentTmp);
//			}
			
			dledParent = dledParentTmp;
		}
	}

	public void sortChildren(Comparator<DialogListEntryData> cmpDled) {
		Collections.sort(aChildList, cmpDled);
	}

//	public RangedValueModel getSliderValueModel() {
//		return modelSliderValue;
//	}
	
//	@Override
//	public boolean isBeingDiscarded() {
//		return DiscardableInstanceI.i().isSelfOrRecursiveOwnerBeingDiscarded(getOwner());
//	}

}
