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

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.github.commandsconsolegui.jmegui.ImprovedAppState;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class BaseGUIStateAbs extends ImprovedAppState implements IConsoleCommandListener, IReflexFillCfg{
	protected SimpleApplication	sapp;
	protected CommandsDelegatorI	cd;
	
	protected Node	ctnrMainTopSubWindow;
	protected Node	intputField;
	
	protected String strUIId = null;
	protected String	strCmd;
//	protected StackTraceElement[] asteInitDebug = null;
	protected String	strTitle;
//	BoolTogglerCmdField btgShowDialog = new BoolTogglerCmdField(this,false);
	protected String	strCmdPrefix = "toggleUI";
	protected String	strCmdSuffix = "";
	
	public BaseGUIStateAbs(String strUIId){
		this.strUIId=strUIId;
		this.strCmd=strCmdPrefix+strUIId+strCmdSuffix;
		this.strTitle = "Dialog: "+strUIId;
//		btgShowDialog.setCustomCmdId(this.strCmd);
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
	
	public String getCommand(){
		return strCmd;
	}
	
	public abstract void requestFocus(Spatial spt);
	
	@Override
	public void configure(Object... aobj) {
		this.sapp = GlobalSappRefI.i().get();
		this.sapp.getStateManager().attach(this);
		
		this.cd = GlobalCommandsDelegatorI.i().get();
		this.cd.addConsoleCommandListener(this);
	}
	
	@Override
	public void onEnable() {
		sapp.getGuiNode().attachChild(ctnrMainTopSubWindow);
		
		requestFocus(intputField);
		
		setMouseCursorKeepUngrabbed(true);
	}
	
	@Override
	public void onDisable() {
		ctnrMainTopSubWindow.removeFromParent();
		
		setMouseCursorKeepUngrabbed(false);
	}
	
	/**
	 * This is important to prevent other parts of the application from 
	 * vanishing with (grabbing) the mouse cursor.
	 * @param b
	 */
	public abstract void setMouseCursorKeepUngrabbed(boolean b);

	protected abstract void initGUI();
	
	public void toggle() {
		setEnabled(!isEnabled());
	}
	
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
