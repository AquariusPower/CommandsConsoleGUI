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

package com.github.commandsconsolegui.jme;

import com.github.commandsconsolegui.ManageKeyCode.Key;
import com.github.commandsconsolegui.cmd.ManageKeyBind;
import com.github.commandsconsolegui.globals.GlobalManageKeyCodeI;
import com.github.commandsconsolegui.globals.jme.GlobalAppRefI;
import com.github.commandsconsolegui.misc.CallQueueI;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.MsgI;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;


/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ManageKeyBindJme extends ManageKeyBind {
	private ActionListener	alGeneralJmeKeyCodeListener;
	
	@Override
	public void configure() {
		super.configure();
		
		// lazy initialization
		CallQueueI.i().addCall(new CallableX(this) {
			@Override
			public Boolean call() {
				if(!GlobalAppRefI.iGlobal().isSet())return false;
				if(GlobalAppRefI.i().getInputManager()==null)return false;
				
				for(Key key:GlobalManageKeyCodeI.i().getKeyListCopy()){
					addKeyCodeMapping(key);
				}
				
				return true;
			}
		});
		
		alGeneralJmeKeyCodeListener = new ActionListener() {
			@Override
			public void onAction(String strKeyId, boolean bPressed, float tpf) {
				GlobalManageKeyCodeI.i().refreshPressedState(strKeyId, bPressed);
			}
		};
		
	}
	
	private void addKeyCodeMapping(Key key){
		if(!key.isKeyWithCode())return;
		
		removeKeyCodeMaping(key);
		
		String strMapping=key.getId();
		GlobalAppRefI.i().getInputManager().addMapping(strMapping, new KeyTrigger(key.getKeyCode()));
		GlobalAppRefI.i().getInputManager().addListener(alGeneralJmeKeyCodeListener, strMapping);
	}
	
	private void removeKeyCodeMaping(Key key){
		String strMapping=key.getId();
		if(GlobalAppRefI.i().getInputManager().hasMapping(strMapping)){
			MsgI.i().devWarn("keycode mapping already existed?", strMapping);
			GlobalAppRefI.i().getInputManager().deleteMapping(strMapping);
		}
	}
	
//	@Override
//	public void configure(){
//		alGeneralJmeKeyCodeListener = new ActionListener() {
//			@Override
//			public void onAction(String name, boolean isPressed, float tpf) {
//				if(!isPressed)return;
////				refreshPressedState();
//				
////				// all field JME binds go here
////				if(bindToggleConsole.checkRunCallerAssigned(isPressed,name))return;
//				
//				executeUserBinds(isPressed,name);
//			}
//		};
//		
//	}
//	
//	@Override
//	public void removeKeyBind(String strMapping){
//		if(GlobalAppRefI.i().getInputManager().hasMapping(strMapping)){
//			GlobalAppRefI.i().getInputManager().deleteMapping(strMapping);
//		}
//		
//		super.removeKeyBind(strMapping);
//	}
//	
//	@Override
//	public void addKeyBind(KeyBoundVarField bind){
//		super.addKeyBind(bind);
//			
//		String strMapping=getMappingFrom(bind);
//		
//		if(GlobalAppRefI.i().getInputManager().hasMapping(strMapping)){
//			GlobalAppRefI.i().getInputManager().deleteMapping(strMapping);
//		}
//		
//		GlobalAppRefI.i().getInputManager().addMapping(strMapping,
//			MiscJmeI.i().asTriggerArray(bind));
//		
//		GlobalAppRefI.i().getInputManager().addListener(alGeneralJmeKeyCodeListener, strMapping);
//	}
	
}
