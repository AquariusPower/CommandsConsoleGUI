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

import java.util.ArrayList;

import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.MouseButtonFlurryOfClicks;
import com.github.commandsconsolegui.jmegui.MouseCursor;
import com.github.commandsconsolegui.jmegui.MouseCursor.EMouseCursorButton;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorListener;
import com.simsilica.lemur.event.CursorMotionEvent;

/**
 * Click detection is based in time delay on this class.
 * Consumes the click only on button release.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public abstract class MouseCursorListenerAbs implements CursorListener {
	MouseButtonFlurryOfClicks clicks = new MouseButtonFlurryOfClicks();
	
	@Override
	public void cursorButtonEvent(CursorButtonEvent eventButton, Spatial target, Spatial capture) {
		EMouseCursorButton emcb = EMouseCursorButton.get(eventButton.getButtonIndex());
		
		if(eventButton.isPressed()){
			emcb.setPressed(MiscJmeI.i().eventToV3f(eventButton));
			
    	if(clickBegin(emcb, eventButton, target, capture)){
    		eventButton.setConsumed();
    	}
		}else{
			if(emcb.isPressed()){ // This check is NOT redundant. May happen just by calling: {@link MouseCursor#resetFixingAllButtons()}
				int iClickCount=clicks.checkAndRetrieveClickCount(emcb, target, capture);
				
//				if(MouseCursor.i().isClickDelay(emcb.setReleasedAndGetDelay())){
//					MouseCursor.i().addClick(
//	      		new MouseButtonClick(emcb, eventButton, target, capture));
//					
//					int iClickCount=MouseCursor.i().getMultiClickCountFor(emcb);
				
				if(iClickCount>0){
					/**
					 * In this case, any displacement will be ignored.
					 * TODO could the minimal displacement it be used in some way?
					 */
	      	if(click(emcb, eventButton, target, capture, iClickCount)){
	      		eventButton.setConsumed();
	      	}
				}
			}
		}
	}
		
	/**
	 * 
	 * @param event
	 * @param target
	 * @param capture
	 * @return if it is to be consumed
	 */
	public boolean click(EMouseCursorButton button, CursorButtonEvent eventButton, Spatial target,	Spatial capture, int iClickCount){
		return false;
	}
	
	/**
	 * this method is if you want to consume the event when the mouse cursor button 
	 * is pressed, this will also give access to {@link #clickEnd(EMouseCursorButton, CursorButtonEvent, Spatial, Spatial)}
	 * @param button
	 * @param eventButton
	 * @param target
	 * @param capture
	 * @return
	 */
	public boolean clickBegin(EMouseCursorButton button, CursorButtonEvent eventButton, Spatial target,	Spatial capture){
		return false;
	}
	
	public boolean clickEnd(EMouseCursorButton button, CursorButtonEvent eventButton, Spatial target,	Spatial capture){
		return false;
	}
	
	@Override
	public void cursorEntered(CursorMotionEvent event, Spatial target,Spatial capture) {
		// TODO Auto-generated method stub
	}

	@Override
	public void cursorExited(CursorMotionEvent event, Spatial target,Spatial capture) {
		// TODO Auto-generated method stub
	}
	
	public boolean drag(ArrayList<EMouseCursorButton> aButtonList, CursorMotionEvent eventMotion, Spatial target,	Spatial capture){
		return false;
	}

	@Override
	public void cursorMoved(CursorMotionEvent eventMotion, Spatial target, Spatial capture) {
		ArrayList<EMouseCursorButton> aButtonList = new ArrayList<EMouseCursorButton>();
		for(EMouseCursorButton e:EMouseCursorButton.values()){
			// Buttons pressed during drag
			if(e.isPressed())aButtonList.add(e);
		}
		
		if(drag(aButtonList, eventMotion, target, capture)){
			eventMotion.setConsumed();
		}
		
	}

	
}
