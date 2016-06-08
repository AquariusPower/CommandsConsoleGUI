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

package com.github.commandsconsolegui.jmegui.lemur;

import com.github.commandsconsolegui.jmegui.BaseDialogStateAbs;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.AbstractCursorEvent;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.event.DefaultCursorListener;

/**
 * This will track the parentest spatial and let mouse cursor move it!
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class DialogDragMouseCursorListenerI extends DefaultCursorListener {
	private static DialogDragMouseCursorListenerI instance = new DialogDragMouseCursorListenerI();
	public static DialogDragMouseCursorListenerI i(){return instance;}
	
	protected Vector3f v3fPrevious=null;

	@Override
	public void cursorButtonEvent(CursorButtonEvent event, Spatial target,Spatial capture) {
		if(event.getButtonIndex()==MouseInput.BUTTON_LEFT){
			if( event.isPressed() ) {
				v3fPrevious=eventToV3f(event);
				LemurFocusHelperStateI.i().requestDialogFocus(capture);
			}else{
				v3fPrevious=null;
			}
		}
    
		super.cursorButtonEvent(event, target, capture);
	}
	
	protected Vector3f eventToV3f(AbstractCursorEvent event){
		return new Vector3f(event.getX(),event.getY(),0);
	}
	
	@Override
	protected void click(CursorButtonEvent event, Spatial target, Spatial capture) {
		v3fPrevious=null; //click mode disables drag
		super.click(event, target, capture);
	}
	
	@Override
	public void cursorMoved(CursorMotionEvent event, Spatial target, Spatial capture) {
		if(v3fPrevious!=null){
			Vector3f v3fDisplacement = v3fPrevious.subtract(eventToV3f(event)).negate();
			
			MiscJmeI.i().getParentestFrom(capture).move(v3fDisplacement);
			
			v3fPrevious=eventToV3f(event);
		}
		
		super.cursorMoved(event, target, capture);
	}

	public void configure() {
	}
}
