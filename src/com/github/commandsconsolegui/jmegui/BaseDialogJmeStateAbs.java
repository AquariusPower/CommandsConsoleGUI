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

import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalAppStateAbs;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.misc.IWorkAroundBugFix;
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
public abstract class BaseDialogJmeStateAbs extends CmdConditionalAppStateAbs implements IReflexFillCfg{
//	protected SimpleApplication	sapp;
//	protected CommandsDelegatorI	cd;
	
	private Spatial	sptContainerMain;
	private Spatial	sptIntputField;
	
	protected String strUIId = null;
//	protected String	strCmd;
//	protected StackTraceElement[] asteInitDebug = null;
	protected String	strTitle;
//	BoolTogglerCmdField btgShowDialog = new BoolTogglerCmdField(this,false);
	
	public Spatial getContainerMain(){
		return sptContainerMain;
	}
	
	protected BaseDialogJmeStateAbs setContainerMain(Spatial spt){
		this.sptContainerMain=spt;
		return this;
	}
	
	public static class CfgParm implements ICfgParm{
		String strUIId;
		boolean bIgnorePrefixAndSuffix;
		Node nodeGUI;
		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI) {
			super();
			this.strUIId = strUIId;
			this.bIgnorePrefixAndSuffix = bIgnorePrefixAndSuffix;
			this.nodeGUI = nodeGUI;
		}
	}
	@Override
	public void configure(ICfgParm icfg) {
		CfgParm cfg = (CfgParm)icfg;
//	protected void configure(String strUIId,boolean bIgnorePrefixAndSuffix,Node nodeGUI) {
		/**
		 * Dialogs must be initially disabled because they are enabled on user demand.
		 */
		bEnabled=false;
		
		super.setNodeGUI(cfg.nodeGUI);//getNodeGUI()

		strCmdPrefix = "toggleUI";
		strCmdSuffix = "";
		
		if(cfg.strUIId==null || cfg.strUIId.isEmpty())throw new NullPointerException("invalid UI identifier");
		this.strUIId=cfg.strUIId;
		
//		this.strCmd=strCmdPrefix+strUIId+strCmdSuffix;
		this.strTitle = "Dialog: "+cfg.strUIId;
//		btgShowDialog.setCustomCmdId(this.strCmd);
		
		super.configure(new CmdConditionalAppStateAbs.CfgParm(cfg.strUIId, cfg.bIgnorePrefixAndSuffix));
	}
	
	/**
	 * Activate, Start, Begin, Initiate.
	 * This will setup and instantiate everything to make it actually be able to work.
	 */
	@Override
	protected boolean initializeValidating() {
		if(!initGUI())return false;
		if(!initKeyMappings())return false;
		return super.initializeValidating();
	}
	
	protected abstract boolean initGUI();
	protected abstract boolean initKeyMappings();
	public abstract String getInputText();
	public abstract void requestFocus(Spatial spt);
	
	/**
	 * what happens when pressing ENTER keys
	 * default is to update the list filter
	 */
	protected abstract void actionSubmit();
	
	@Override
	protected boolean enableValidating() {
		getNodeGUI().attachChild(sptContainerMain);
		
		requestFocus(sptIntputField);
		
		setMouseCursorKeepUngrabbed(true);
		
		return super.enableValidating();
	}
	
	@Override
	protected boolean disableValidating() {
		sptContainerMain.removeFromParent();
		
		setMouseCursorKeepUngrabbed(false);
		
		return super.disableValidating();
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

	protected BaseDialogJmeStateAbs setIntputField(Spatial sptIntputField) {
		this.sptIntputField = sptIntputField;
		return this;
	}
}
