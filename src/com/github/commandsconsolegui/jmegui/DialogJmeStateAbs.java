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

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalAppStateAbs;
import com.github.commandsconsolegui.jmegui.extras.UngrabMouseStateI;
import com.github.commandsconsolegui.misc.IWorkAroundBugFix;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.SimpleApplication;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class DialogJmeStateAbs extends CmdConditionalAppStateAbs implements IWorkAroundBugFix, IReflexFillCfg{
//	protected SimpleApplication	sapp;
//	protected CommandsDelegatorI	cd;
	
	protected Node	ctnrMainTopSubWindow;
	protected Node	intputField;
	
	protected String strUIId = null;
	protected String	strCmd;
//	protected StackTraceElement[] asteInitDebug = null;
	protected String	strTitle;
//	BoolTogglerCmdField btgShowDialog = new BoolTogglerCmdField(this,false);
	protected String	strCmdPrefix = "toggleUI";
	protected String	strCmdSuffix = "";
	private CommandsDelegatorI	cd;
	private SimpleApplication	sapp;
	
	public DialogJmeStateAbs(String strUIId){
		this.strUIId=strUIId;
		this.strCmd=strCmdPrefix+strUIId+strCmdSuffix;
		this.strTitle = "Dialog: "+strUIId;
//		btgShowDialog.setCustomCmdId(this.strCmd);
	}
	
	/**
	 * Activate, Start, Begin, Initiate.
	 * This will setup and instantiate everything to make it actually be able to work.
	 */
	@Override
	protected boolean initializeValidating() {
		initGUI(); //isEnabled()
		initKeyMappings();
		return true;
	}
	
	public String getCommand(){
		return strCmd;
	}
	
	public abstract void requestFocus(Spatial spt);
	
	public void configure() {
		super.configure(GlobalSappRefI.i().get());
		sapp=GlobalSappRefI.i().get();
		this.cd = GlobalCommandsDelegatorI.i().get();
		
		/**
		 * Sub windows dialogs must be initially disabled because they are 
		 * enabled on user demand.
		 */
		bEnabled=false; 
	}
	
	@Override
	protected boolean enableValidating() {
		sapp.getGuiNode().attachChild(ctnrMainTopSubWindow);
		
		requestFocus(intputField);
		
		setMouseCursorKeepUngrabbed(true);
		
		return true;
	}
	
	@Override
	protected boolean disableValidating() {
		ctnrMainTopSubWindow.removeFromParent();
		
		setMouseCursorKeepUngrabbed(false);
		
		return true;
	}
	
	/**
	 * This is important to prevent other parts of the application from 
	 * vanishing with (grabbing) the mouse cursor.
	 * @param b
	 */
	public void setMouseCursorKeepUngrabbed(boolean b) {
		UngrabMouseStateI.i().setKeepUngrabbedRequester(this,b);
	}

	protected abstract void initGUI();
	
	public void setTitle(String str){
		this.strTitle = str;
	}

	protected abstract void initKeyMappings();
	
	/**
	 * what happens when pressing ENTER keys
	 * default is to update the list filter
	 */
	protected abstract void actionSubmit();
	
	protected abstract String getInputText();
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return cd.getReflexFillCfg(rfcv);
	}
}
