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

package com.github.commandsconsolegui.jmegui;

import java.util.ArrayList;

import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class BaseDialogStateAbs extends CmdConditionalStateAbs implements IReflexFillCfg{
	protected Spatial	sptContainerMain;
	protected Spatial	sptIntputField;
	protected String	strTitle;
	
	protected BaseDialogStateAbs modalParent;
	protected ArrayList<BaseDialogStateAbs> aModalChildList = new ArrayList<BaseDialogStateAbs>();
	protected DialogListEntryData	dataToCfgReference;
	protected DialogListEntryData dataCfgValue;
	private boolean	bRequestedActionSubmit;
//	private Object[]	aobjModalAnswer;
	
	public Spatial getContainerMain(){
		return sptContainerMain;
	}
	
	protected BaseDialogStateAbs setContainerMain(Spatial spt){
		this.sptContainerMain=spt;
		return this;
	}
	
	public static class CfgParm implements ICfgParm{
		String strUIId;
		boolean bIgnorePrefixAndSuffix;
		Node nodeGUI;
		boolean bInitiallyEnabled;
		BaseDialogStateAbs modalParent;
//		Long lMouseCursorClickDelayMilis;
		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI, boolean bInitiallyEnabled, BaseDialogStateAbs modalParent){//, Long lMouseCursorClickDelayMilis) {
			super();
			this.strUIId = strUIId;
			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
			this.nodeGUI = nodeGUI;
			this.bInitiallyEnabled=bInitiallyEnabled;
			this.modalParent=modalParent;
//			this.lMouseCursorClickDelayMilis=lMouseCursorClickDelayMilis;
		}
	}
	@Override
	public BaseDialogStateAbs configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
//	protected void configure(String strUIId,boolean bIgnorePrefixAndSuffix,Node nodeGUI) {
		
//		bEnabled=cfg.bInitiallyEnabled;
//		if(!cfg.bInitiallyEnabled)requestDisable();
		if(!cfg.bInitiallyEnabled)initiallyDisabled();
		
//		MouseCursor.i().configure(cfg.lMouseCursorClickDelayMilis);
		
		super.setNodeGUI(cfg.nodeGUI);//getNodeGUI()
		
		this.modalParent=cfg.modalParent;
//		updateModalParent();
		
		strCmdPrefix = "toggleUI";
		strCmdSuffix = "";
		
//		ConditionalStateManagerI.i().
		if(cfg.strUIId==null || cfg.strUIId.isEmpty())throw new NullPointerException("invalid UI identifier");
		this.strCaseInsensitiveId=cfg.strUIId;
		
//		this.strCmd=strCmdPrefix+strUIId+strCmdSuffix;
		this.strTitle = "Dialog: "+cfg.strUIId;
//		btgShowDialog.setCustomCmdId(this.strCmd);
		
		super.configure(new CmdConditionalStateAbs.CfgParm(cfg.strUIId, cfg.bIgnorePrefixAndSuffix));
		
		return storeCfgAndReturnSelf(icfg);
	}
	
	/**
	 * Activate, Start, Begin, Initiate.
	 * This will setup and instantiate everything to make it actually be able to work.
	 */
	@Override
	protected boolean initOrUndo() {
		if(!initGUI())return false;
		if(!initKeyMappings())return false;
		return super.initOrUndo();
	}
	
	protected abstract boolean initGUI();
	protected abstract boolean initKeyMappings();
	public abstract String getInputText();
	
	/**
	 * what happens when pressing ENTER keys
	 * default is to update the list filter
	 */
	protected abstract void actionSubmit();
	
	public void requestActionSubmit() {
		bRequestedActionSubmit=true;
	}
	
	@Override
	protected boolean updateOrUndo(float tpf) {
		if(bRequestedActionSubmit){
			actionSubmit();
			bRequestedActionSubmit=false;
		}
		
		return super.updateOrUndo(tpf);
	}
	
	@Override
	protected boolean enableOrUndo() {
		getNodeGUI().attachChild(sptContainerMain);
		
		setMouseCursorKeepUngrabbed(true);
		if(modalParent!=null)modalParent.updateModalChild(true,this);
//		updateModalParent(true);
		
		return super.enableOrUndo();
	}
	
	@Override
	protected boolean disableOrUndo() {
		sptContainerMain.removeFromParent();
		
		setMouseCursorKeepUngrabbed(false);
		if(modalParent!=null)modalParent.updateModalChild(false,this);
//		updateModalParent(false);
		
		return super.disableOrUndo();
	}
	
	/**
	 * This is important to prevent other parts of the application from 
	 * vanishing with (grabbing) the mouse cursor.
	 * @param b
	 */
	public void setMouseCursorKeepUngrabbed(boolean b) {
		UngrabMouseStateI.i().setKeepUngrabbedRequester(this,b);
	}
	
	public void setTitle(String str){
		this.strTitle = str;
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return cd().getReflexFillCfg(rfcv);
	}

	public Spatial getIntputField() {
		return sptIntputField;
	}

	protected BaseDialogStateAbs setIntputField(Spatial sptIntputField) {
		this.sptIntputField = sptIntputField;
		return this;
	}

	public BaseDialogStateAbs getParentDialog(){
		return this.modalParent;
	}
	
	public void setModalParent(LemurDialogGUIStateAbs modalParent) {
		if(this.modalParent!=null)throw new PrerequisitesNotMetException("modal parente already set",this.modalParent,modalParent);
		this.modalParent=modalParent;
	}
	
//	public abstract void setAnswerFromModalChild(Object... aobj);
	
	public ArrayList<BaseDialogStateAbs> getModalChildListCopy() {
		return new ArrayList<BaseDialogStateAbs>(aModalChildList);
	}
	protected void updateModalChild(boolean bAdd, BaseDialogStateAbs modal) {
//		if(this.modalParent==null)return;
			
		if(bAdd){
			if(!aModalChildList.contains(modal)){
				aModalChildList.add(modal);
			}
		}else{
			aModalChildList.remove(modal);
//			aobjModalAnswer=modal.aobjModalAnswer;
//			setModalAnswer(modal.aobjModalAnswer);
		}
	}
//	protected void updateModalParent(boolean bAdd) {
//		if(this.modalParent==null)return;
//		
//		if(bAdd){
//			if(!modalParent.aModalChildList.contains(this)){
//				modalParent.aModalChildList.add(this);
//			}
//		}else{
//			modalParent.aModalChildList.remove(this);
//			modalParent.setModalAnswer(this.aobjModalAnswer);
//		}
//	}
//	protected void setModalAnswer(Object... aobjModalAnswer) {
//		this.aobjModalAnswer=aobjModalAnswer;
//	}

	public void setCfgDataValue(DialogListEntryData data) {
		this.dataCfgValue = data;
	}
	
	/**
	 * the answer will be null after this
	 * @return
	 */
	public DialogListEntryData getCfgDataValueAndClearIt(){
		DialogListEntryData data = dataCfgValue;
		dataCfgValue = null;
		return data;
	}
	
	public boolean isCfgDataValueSet() {
		return dataCfgValue!=null;
	}
	
	/**
	 * this data will have it's value modified
	 * @param dataToCfg
	 */
	protected void setCfgDataReference(DialogListEntryData dataToCfg) {
		this.dataToCfgReference=dataToCfg;
	}
	protected DialogListEntryData getCfgDataRefAndClearIt() {
		DialogListEntryData data = this.dataToCfgReference;
		this.dataToCfgReference=null;
		return data;
	}
	public String getCfgDataRefReport() {
		return this.dataToCfgReference.report();
	}
	
}
