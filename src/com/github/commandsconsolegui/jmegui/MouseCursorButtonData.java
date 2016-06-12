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

import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.MouseCursorButtonClicks.MouseButtonClick;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class MouseCursorButtonData{
	EMouseCursorButton eButton = null;
	
	MouseCursorButtonClicks clicks=null;
	
	Long lPressedMilis = null;
//	Long lReleasedMilis = null;
	
	Vector3f v3fPressedPos = null;
	
	Vector3f v3fDragLastUpdatePos = null;
//	Vector3f v3fReleasedPos = null;
	
//	CursorButtonEvent eventButton;
//	Vector3f v3fPressedDraggingPosToConsume = null; 
//	CursorMotionEvent	eventMotion;
//	public boolean	bIsPressed;
//	public Vector3f	v3fDragDisplacement;
	
	public MouseCursorButtonData(EMouseCursorButton e) {
		this.eButton=e;
		this.clicks = new MouseCursorButtonClicks(e);
	}
	
	public MouseCursorButtonClicks getClicks(){
		return clicks;
	}
	
	void reset(){
		lPressedMilis = null;
		v3fPressedPos = null;
		v3fDragLastUpdatePos = null;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MouseCursorButtonData [lPressedMilis=");
		builder.append(lPressedMilis);
		builder.append(", v3fPressedPos=");
		builder.append(v3fPressedPos);
		builder.append(", v3fDragLastUpdatePos=");
		builder.append(v3fDragLastUpdatePos);
		builder.append("]");
		return builder.toString();
	}

	public boolean isPressed() {
		return lPressedMilis!=null;
	}
	
	public void setPressed(Vector3f v3fPressedPos){
		if(isPressed())throw new PrerequisitesNotMetException("already pressed! ",this,v3fPressedPos);
		
		this.lPressedMilis = System.currentTimeMillis();
		this.v3fPressedPos = v3fPressedPos.clone();
		this.v3fDragLastUpdatePos = v3fPressedPos.clone();
	}
	
	public Vector3f updateDragPosAndGetDisplacement(Vector3f v3fNewDragPos){
		if(!isPressed())return null;
		
		Vector3f v3fDiff = this.v3fDragLastUpdatePos.subtract(v3fNewDragPos);
		this.v3fDragLastUpdatePos.set(v3fNewDragPos);
		
		return v3fDiff;
	}
	
	/**
	 * Last drag displacement must be get before this call at
	 * {@link #updateDragPosAndGetDisplacement(Vector3f)}
	 * 
	 * @return
	 */
	public Long setReleasedAndGetDelay(){
		if(!isPressed())return null;
		
		long lDelay = System.currentTimeMillis() - this.lPressedMilis;
		this.lPressedMilis=null;
		return lDelay;
	}

	public EMouseCursorButton getActivatorType() {
		return eButton;
	}
	
	public int checkAndRetrieveClickCount(Spatial target, Spatial capture) {
		Long lDelay = setReleasedAndGetDelay();
		if(lDelay!=null){
			if(MouseCursorCentralI.i().isClickDelay(lDelay)){
				clicks.addClick(new MouseButtonClick(eButton, target, capture));
				return clicks.getMultiClickCountFor(eButton);
			}
		}else{
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("null delay, already released button");
		}
		
		return 0;
	}

//	public void applyDrag(Vector3f v3fMouseCursorPosPrevious, Vector3f v3fNewMouseCursorPos) {
//		Vector3f v3fDisplacement = v3fMouseCursorPosPrevious
//			.subtract(v3fNewMouseCursorPos).negate();
//		
//		mcbi.v3fDragDisplacement = v3fDisplacement;
//	}
	
//	public Vector3f getDragDisplacement() {
//		return this.v3fDragDisplacement;
//	}
//	
//	public CursorButtonEvent getLastButtonEvent() {
//		return this.eventButton;
//	}
//	
}
