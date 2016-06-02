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

package com.github.commandsconsolegui.extras.jmegui;

import java.util.HashMap;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.console.jmegui.lemur.LemurFocusHelperI;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.BasePlusAppState;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * A console command will be automatically created based on the configured {@link #strUIId}.<br>
 * See it at {@link #BaseUIStateAbs(String)}.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class BaseUIStateAbs <V> extends BasePlusAppState implements IConsoleCommandListener, IReflexFillCfg{
	protected SimpleApplication	sapp;
	protected Node	ctnrDialog;
	protected Node cntrNorth;
	protected Node	intputText;
	protected String	strLastFilter = "";
	protected HashMap<String,V> hmKeyValue = new HashMap<String,V>();
	protected String	strLastSelectedKey;
	protected String strUIId = null;
	protected String	strCmd;
//	protected StackTraceElement[] asteInitDebug = null;
	protected CommandsDelegatorI	cd;
	protected String	strTitle;
//	BoolTogglerCmdField btgShowDialog = new BoolTogglerCmdField(this,false);
	protected String	strCmdPrefix = "toggleUI";
	protected String	strCmdSuffix = "";
	
	public BaseUIStateAbs(String strUIId){
		this.strUIId=strUIId;
		this.strCmd=strCmdPrefix+strUIId+strCmdSuffix;
		this.strTitle = "Dialog: "+strUIId;
//		btgShowDialog.setCustomCmdId(this.strCmd);
	}
	
	public String getCommand(){
		return strCmd;
	}
	
	@Override
	public void configure(Object... aobj) {
		this.sapp = GlobalSappRefI.i().get();
		this.sapp.getStateManager().attach(this);
		
		this.cd = GlobalCommandsDelegatorI.i().get();
		this.cd.addConsoleCommandListener(this);
	}
	
	/**
	 * Activate, Start, Begin, Initiate.
	 * This will setup and activate everything to make it actually start working.
	 */
	@Override
	public void initialize(Application app) {
		super.initialize(app);
//		asteInitDebug = Thread.currentThread().getStackTrace();
		
		initGUI();
		initKeyMappings();
		
		setEnabled(false);
		
//		cc.addConsoleCommandListener(this);
	}
	
	protected abstract void initGUI();
	
	public abstract void clearSelection();
	
	public void toggle() {
		setEnabled(!isEnabled());
	}
	
	public abstract void requestFocus(Spatial spt);
	
	public void setTitle(String str){
		this.strTitle = str;
	}
	
	@Override
	public void onEnable() {
		sapp.getGuiNode().attachChild(ctnrDialog);
		
		updateTextInfo();
		updateList();
		updateInputField();
		
		requestFocus(intputText);
		
		setMouseCursorKeepUngrabbed(true);
	}
	
	@Override
	public void onDisable() {
		ctnrDialog.removeFromParent();
		
		setMouseCursorKeepUngrabbed(false);
	}
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	protected abstract void updateInputField();

	protected abstract Integer getSelectedIndex();
	
	protected abstract String getSelectedKey();
	
	protected V getSelectedValue() {
		return hmKeyValue.get(getSelectedKey());
	}
	
	/**
	 * 
	 */
	protected abstract void updateList();
	
	protected abstract void updateTextInfo();
	
	@Override
	public void update(float tpf) {
		String str = getSelectedKey();
		if(str!=null)strLastSelectedKey=str;
		
		updateTextInfo();
		
		//requestFocus(intputText); //keep focus at input as it shall have all listeners.
		
		setMouseCursorKeepUngrabbed(isEnabled());
	}
	
	/**
	 * This is important to prevent other parts of the application from 
	 * vanishing with (grabbing) the mouse cursor.
	 * @param b
	 */
	public abstract void setMouseCursorKeepUngrabbed(boolean b);
	
	/**
	 * What will be shown at each entry on the list.
	 * Default is to return the default string about the object.
	 * 
	 * @param val
	 * @return
	 */
	public String formatEntryKey(V val){
		return val.toString();
	}
	
	protected abstract void initKeyMappings();
	
	/**
	 * what happens when pressing ENTER keys
	 * default is to update the list filter
	 */
	protected void actionSubmit(){
		strLastFilter=getInputText();
		updateList();
	}

	protected abstract String getInputText();

	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,strCmd,"[bEnabledForce]")){
			Boolean bEnabledForce = cc.paramBoolean(1);
			if(bEnabledForce!=null){
				setEnabled(bEnabledForce);
			}else{
				toggle();
			}
			bCommandWorked = true;
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return cd.getReflexFillCfg(rfcv);
	}
}
