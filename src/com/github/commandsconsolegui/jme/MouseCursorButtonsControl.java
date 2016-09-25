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

import java.util.HashMap;

import com.github.commandsconsolegui.jme.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class MouseCursorButtonsControl {
	HashMap<EMouseCursorButton, MouseCursorButtonData> hmButtonData = new HashMap<EMouseCursorButton, MouseCursorButtonData>();
	private Object	objParent;
	
	public MouseCursorButtonsControl(MouseCursorCentralI.CompositeControl cc, Object objParent) {
		cc.assertSelfNotNull();
		
		if(objParent==null)throw new PrerequisitesNotMetException("missing parent");
		this.objParent=objParent;
		
		for(EMouseCursorButton e:EMouseCursorButton.values()){
			hmButtonData.put(e,new MouseCursorButtonData(e));
		}
	}
	
	/**
	 * In case user press a button, but when the button is released, 
	 * that event is not captured like when freeze lag happens.
	 * 
	 * TODO find a way to detect such condition, may be when a dialog is created or closed, no button should have its pressed state recognized?
	 * TODO call this after every dialog/console mouse cursor action completes?
	 * 
	 * To fix that, use this command: {@link #CMD_FIX_RESETING_MOUSE_BUTTONS}
	 * 
	 * To test it, at console, open a dialog and call this command ex.:
	 *	/sleep 3 fixResetingMouseCursor #(if I havent refactored yet...)
	 * Now, while dragging the dialog around, you will lose that grab.
	 */
	public void resetFixingAllButtonsState(){
		for(EMouseCursorButton e:EMouseCursorButton.values()){
			getMouseCursorDataFor(e).reset();
		}
	}
	
	public MouseCursorButtonData getMouseCursorDataFor(EMouseCursorButton e){
		return hmButtonData.get(e);
	}
	
	public String report(){
		String str="";
		for(EMouseCursorButton e:EMouseCursorButton.values()){
			str+=""//+objParent.getClass().getSimpleName()
				+e+": "
				+getMouseCursorDataFor(e).toString()
				+"\n";
		}
		return str;
	}
	
}
