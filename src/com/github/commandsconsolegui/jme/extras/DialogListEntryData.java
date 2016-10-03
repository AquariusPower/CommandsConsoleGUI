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

package com.github.commandsconsolegui.jme.extras;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import com.github.commandsconsolegui.globals.jme.GlobalDialogHelperI;
import com.github.commandsconsolegui.jme.AudioUII;
import com.github.commandsconsolegui.jme.DialogStateAbs;
import com.github.commandsconsolegui.jme.extras.DialogListEntryData.SliderValueData.ESliderKey;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.DiscardableInstanceI;
import com.github.commandsconsolegui.misc.HoldRestartable;
import com.github.commandsconsolegui.misc.IDiscardableInstance;
import com.github.commandsconsolegui.misc.IHasOwnerInstance;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.MsgI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.simsilica.lemur.RangedValueModel;

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
 * @param <T> is the action class for buttons
 */
public class DialogListEntryData<T> implements Savable,IHasOwnerInstance<DialogStateAbs>{ //IDiscardableInstance
//	private static String strLastUniqueId = "0";
	
	private String	strUniqueId;
	private HoldRestartable<DialogStateAbs> hrdiagOwner = null; //new HoldRestartable<DialogStateAbs>();
	
	private HashMap<String,T> hmCustomButtonsActions = new HashMap<String,T>();
	private Object	objUser;
	
	private DialogListEntryData<T> parent; //it must be set as the parent type too
	private boolean bTreeExpanded = false; 
	private ArrayList<DialogListEntryData<T>> aChildList = new ArrayList<DialogListEntryData<T>>();
	
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
				CallQueueI.i().addCall(callerSlider);
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
		this.strUniqueId = diagOwner.getNextUniqueId();
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
	public DialogListEntryData<T> setSlider(Double dMin, Double dMax, Double dCurrent, CallableX callerSlider, boolean bCallWhileChanging, boolean bIntMode){
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
	
	public DialogListEntryData<T> addCustomButtonAction(String strLabelTextId, T action){
		hmCustomButtonsActions.put(strLabelTextId,action);
		return this;
	}
	public DialogListEntryData<T> clearCustomButtonActions(){
		hmCustomButtonsActions.clear();
		return this;
	}
	
//	@SuppressWarnings("unchecked")
//	public Entry<String,T>[] getLabelActionListCopy(){
//		return (Entry<String,T>[])hmLabelAction.entrySet().toArray();
//	}
//	@SuppressWarnings("rawtypes")
	@SuppressWarnings("unchecked")
	public <E extends Entry<String,T>> ArrayList<E> getCustomButtonsActionsListCopy(){
		E[] aen = (E[])hmCustomButtonsActions.entrySet().toArray(new Entry[0]);
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

private RangedValueModel	modelSliderValue;
	
	public boolean isAddVisibleQuotes(){
		return bAddVisibleQuotes;
	}
	
	public DialogListEntryData<T> setAddVisibleQuotes(boolean b){
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
	
	public Object getUserObj(){
		return this.objUser;
	}
	
	/**
	 * @param strText 
	 * @return
	 */
	public DialogListEntryData<T> updateTextTo(Object objText) {
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
	 * @param strText 
	 * @param objUser the linked object represented by the text
	 * @return
	 */
	public DialogListEntryData<T> setText(Object objText, Object objUser) {
		updateTextTo(objText);
//		this.strText = strText;
		this.objUser=objUser;
//		this.objRef=objReference;
		return this;
	}
//	public Object getRef() {
//		return objRef;
//	}
//	public DialogListEntryData<T> setRef(Object objRef) {
//		this.objRef = objRef;
//		return this;
//	}
	
//	public T getAction(){
//		return objRef;
//	}
	
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

	public DialogListEntryData<T> getParent() {
		return parent;
	}
	
	/**
	 * 
	 * @param parent if null, will just remove from existing parent
	 * @return
	 */
	public DialogListEntryData<T> setParent(DialogListEntryData<T> parent) {
		if(this==parent)throw new PrerequisitesNotMetException("cant parent self: parent==this", this);
		
		// remove self from previous parent
		DialogListEntryData<T> dledParentOld = this.getParent();
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
	
	public boolean hasChild(DialogListEntryData<T> dledChildToCheck){
		if(dledChildToCheck==null){
			throw new PrerequisitesNotMetException("null child to check", this);
		}
		
		for(DialogListEntryData<T> dledChild:this.aChildList){
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

	public DialogListEntryData<T> setTreeExpanded(boolean bTreeExpanded) {
		this.bTreeExpanded = bTreeExpanded;
		return this;
	}
	
	public ArrayList<DialogListEntryData<T>> getAllParents(){
		ArrayList<DialogListEntryData<T>> adledParentList = new ArrayList<DialogListEntryData<T>>();
		
		DialogListEntryData<T> dledParent = getParent();
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
			for(DialogListEntryData<T> dledParent:getAllParents()){
				dledParent.setTreeExpanded(true);
			}
		}
		
		hrdiagOwner.getRef().requestRefreshUpdateList();
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
			MsgI.i().devInfo("cleaning child ("+getUId()+":"+getVisibleText()+") diagOwner ref ("+hrdiagOwner.getRef().getId()+"), only root one needs it", this, hrdiagOwner);
			hrdiagOwner=null;
		}
		
		return getParent().getOwner();
//		return getParentest().hrdiagOwner.get();
//		return hrdiagOwner.get();
	}
	
	public boolean isParent() {
		return aChildList.size()>0;
	}

	public ArrayList<DialogListEntryData<T>> getChildrenCopy() {
		return new ArrayList<DialogListEntryData<T>>(aChildList);
	}
	
	public String getUId() {
		return strUniqueId;
	}

	public DialogListEntryData<T> clearChildren() {
		aChildList.clear();
		return this;
	}
	
	/**
	 * Returns root one (parent less). 
	 * 
	 * @return null if it has no parent
	 */
	public DialogListEntryData<T> getParentest() {
		DialogListEntryData<T> dledParent = getParent();
		if(dledParent==null)return null;
		
//		ArrayList<DialogListEntryData<T>> adledConsistencyChk = new ArrayList<DialogListEntryData<T>>();
		while(true){
			DialogListEntryData<T> dledParentTmp = dledParent.getParent();
			if(dledParentTmp==null)return dledParent;
			
//			if(adledConsistencyChk.contains(dledParentTmp)){
//				throw new PrerequisitesNotMetException("tree is inconsistent (child/parent loop)", adledConsistencyChk, dledParentTmp);
//			}else{
//				adledConsistencyChk.add(dledParentTmp);
//			}
			
			dledParent = dledParentTmp;
		}
	}

	public void sortChildren(Comparator<DialogListEntryData<T>> cmpDled) {
		Collections.sort(aChildList, cmpDled);
	}

	public RangedValueModel getSliderValueModel() {
		return modelSliderValue;
	}
	
//	@Override
//	public boolean isBeingDiscarded() {
//		return DiscardableInstanceI.i().isSelfOrRecursiveOwnerBeingDiscarded(getOwner());
//	}

}
