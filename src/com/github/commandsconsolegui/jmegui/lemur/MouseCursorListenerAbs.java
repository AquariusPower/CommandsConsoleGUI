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
import com.github.commandsconsolegui.jmegui.MouseCursor;
import com.github.commandsconsolegui.jmegui.MouseCursor.EMouseCursorButton;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.math.Vector3f;
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
//	class ButtonInfo{
//		Long lPressedMilis = null;
//		Long lReleasedMilis = null;
//		
//		Vector3f v3fPressedPos = null;
//		Vector3f v3fReleasedPos = null;
//		
//		CursorButtonEvent eventButton;
////		Vector3f v3fPressedDraggingPosToConsume = null; 
//		CursorMotionEvent	eventMotion;
//		public boolean	bIsPressed;
//		public Vector3f	v3fDragDisplacement;
//		
//		public boolean isPressed() {
//			return bIsPressed;
//		}
//	}
//	
//	protected ButtonInfo	beLeft = new ButtonInfo();;
//	protected ButtonInfo	beMiddle = new ButtonInfo();;
//	protected ButtonInfo	beRight = new ButtonInfo();
	
//	protected long	lClickDelayMilis;
//	private Vector3f	v3fDragMouseCursorPosPrevious;
	
//	protected ButtonInfo prepareButtonInfo(CursorButtonEvent event){
////		ButtonInfo be = null;
//		EMouseCursorButton emcb = EMouseCursorButton.get(event.getButtonIndex());
////    switch(event.getButtonIndex()){
////    	case MouseInput.BUTTON_LEFT:
////    		be = beLeft;
////    		break;
////    	case MouseInput.BUTTON_MIDDLE:
////    		be = beMiddle;
////    		break;
////    	case MouseInput.BUTTON_RIGHT:
////    		be = beRight;
////    		break;
////    }
//    
//		emcb.prepareInfo(event);
////  	emcb.setEventButton(event);
////  	emcb.setPressed(event.isPressed);
////  	
////    long lMilis = System.currentTimeMillis();
////    Vector3f v3fPos = eventToV3f(event);
////    
////    if(be.bIsPressed){
////    	be.v3fReleasedPos = null;
////    	be.lReleasedMilis = null;
////    	
////    	be.v3fPressedPos = v3fPos;
////    	be.lPressedMilis = lMilis;
//////    	be.v3fPressedDraggingPosToConsume = v3fPos;
////    }else{
////    	be.v3fReleasedPos = v3fPos;
////    	be.lReleasedMilis = lMilis;
////    }
////    
////    return be;
//	}
	
	@Override
	public void cursorButtonEvent(CursorButtonEvent eventButton, Spatial target, Spatial capture) {
		EMouseCursorButton emcb = EMouseCursorButton.get(eventButton.getButtonIndex());
		
		if(eventButton.isPressed()){
			emcb.setPressed(MiscJmeI.i().eventToV3f(eventButton));
		}else{
			if(MouseCursor.i().isClickDelay(emcb.setReleasedAndGetDelay())){
				/**
				 * In this case, any displacement will be ignored.
				 * TODO could the minimal displacement it be used in some way?
				 */
      	if(cursorClick(emcb, eventButton, target, capture)){
      		eventButton.setConsumed();
      	}
			}
		}
		
////    ButtonInfo be = prepareButtonInfo(event);
//    EMouseCursorButton emcb = EMouseCursorButton.get(event.getButtonIndex())
//    	.prepareData(event);
//    
//    if(emcb.isClicked()){ // on release
////    if( !event.isPressed() && emcb.lReleasedMilis != null){
////  		if(emcb.lReleasedMilis <= (emcb.lPressedMilis+lClickDelayMilis)){
//      	if(click(emcb, target, capture)){
//      		v3fDragMouseCursorPosPrevious = null;
//      		event.setConsumed();
//      	}else{ //drag mode
//      		v3fDragMouseCursorPosPrevious = MiscJmeI.i().eventToV3f(event);
//      	}
//  	}
//	}
	}
	
//	public Vector3f eventToV3f(AbstractCursorEvent event){
//		return new Vector3f(event.getX(),event.getY(),0);
//	}
	
	/**
	 * 
	 * @param event
	 * @param target
	 * @param capture
	 * @return if it is to be consumed
	 */
	public boolean cursorClick(EMouseCursorButton button, CursorButtonEvent eventButton, Spatial target,	Spatial capture){
		return false;
	}
	
//	protected void dragApply(EMouseCursorButton emcb, CursorMotionEvent eventMotion, Spatial target, Spatial capture){
//		if(v3fMouseCursorPosPrevious!=null){
//			emcb.applyDrag(v3fMouseCursorPosPrevious, MiscJmeI.i().eventToV3f(eventMotion));
//			
////			Vector3f v3fNewPos = MiscJmeI.i().eventToV3f(eventMotion);
////			
////			Vector3f v3fDisplacement = v3fMouseCursorPosPrevious
////				.subtract(v3fNewPos).negate();
////			
////			emcb.mcbi.v3fDragDisplacement = v3fDisplacement;
////			MiscJmeI.i().getParentestFrom(capture).move(v3fDisplacement);
//			
//			if(drag(be.eventButton, be.v3fDragDisplacement, target, capture)){
//				v3fMouseCursorPosPrevious=v3fNewPos;
//				eventMotion.setConsumed();
//			}
//		}
//	}
	
	@Override
	public void cursorEntered(CursorMotionEvent event, Spatial target,Spatial capture) {
		// TODO Auto-generated method stub
	}

	@Override
	public void cursorExited(CursorMotionEvent event, Spatial target,Spatial capture) {
		// TODO Auto-generated method stub
	}
	
//	protected void cursorDragged(CursorMotionEvent eventMotion, Spatial target, Spatial capture){
//		ArrayList<EMouseCursorButton> aButtonList = new ArrayList<EMouseCursorButton>();
//		
//		for(EMouseCursorButton emcb:EMouseCursorButton.values()){
//			if(emcb.isPressed())aButtonList.add(emcb);
//		}
//		
//		Vector3f v3fNewPos = MiscJmeI.i().eventToV3f(eventMotion);
//		
//		Vector3f v3fDisplacement = v3fDragMouseCursorPosPrevious
//			.subtract(v3fNewPos).negate();
//		
////			Vector3f v3fDragDisplacement = v3fDisplacement;
////			emcb.applyDrag(v3fMouseCursorPosPrevious, v3fNewPos);
//		
//		if(drag(aButtonList, v3fDisplacement, target, capture)){
//			v3fDragMouseCursorPosPrevious=v3fNewPos;
//			eventMotion.setConsumed();
//		}
//	}
	
	public boolean cursorDragged(ArrayList<EMouseCursorButton> aButtonList, CursorMotionEvent eventMotion, Spatial target,	Spatial capture){
		return false;
	}

	@Override
	public void cursorMoved(CursorMotionEvent eventMotion, Spatial target, Spatial capture) {
		ArrayList<EMouseCursorButton> aButtonList = new ArrayList<EMouseCursorButton>();
		for(EMouseCursorButton e:EMouseCursorButton.values()){
			// Buttons pressed during drag
			if(e.isPressed())aButtonList.add(e);
		}
		
		if(cursorDragged(aButtonList, eventMotion, target, capture)){
			eventMotion.setConsumed();
		}
		
//		if(v3fDragMouseCursorPosPrevious!=null){
//			cursorDragged(eventMotion, target, capture);
//		}
		
//			if(emcb.isPressed()){ //dragApply(e, eventMotion, target, capture);
//				if(v3fMouseCursorPosPrevious!=null){
//					Vector3f v3fNewPos = MiscJmeI.i().eventToV3f(eventMotion);
//					
//					emcb.applyDrag(v3fMouseCursorPosPrevious, v3fNewPos);
//					
//					if(drag(emcb.getLastButtonEvent(), emcb.getDragDisplacement(), target, capture)){
//						v3fMouseCursorPosPrevious=v3fNewPos.clone();
//						eventMotion.setConsumed();
//					}
//				}
//			}
//		}
		
//		if(EMouseCursorButton.Action.isPressed()){
//			dragApply(EMouseCursorButton.Action, eventMotion, target, capture);
//		}
//		if(beLeft.isPressed())dragApply(beLeft, eventMotion, target, capture);
//		if(beMiddle.isPressed())dragApply(beMiddle, eventMotion, target, capture);
//		if(beRight.isPressed())dragApply(beRight, eventMotion, target, capture);
	}

	
//	public long getClickDelayMilis() {
//		return lClickDelayMilis;
//	}
//
//	public void setClickDelayMilis(long lClickDelayMilis) {
//		if(lClickDelayMilis<=0)throw new PrerequisitesNotMetException("invalid click delay "+lClickDelayMilis);
//		this.lClickDelayMilis = lClickDelayMilis;
//	}
}
