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

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.extras.FpsLimiterStateI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector3f;

/**
 * Allows multiple mouse buttons to be clicked or dragged at same time.
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class MouseCursor implements IReflexFillCfg, IConsoleCommandListener{
	public final StringCmdField CMD_FIX_RESETING_MOUSE_CURSOR = new StringCmdField(this,CommandsDelegator.strFinalCmdCodePrefix);
	
	private static MouseCursor instance = new MouseCursor();
	public static MouseCursor i(){return instance;}
	
	IntLongVarField ilvClickMaxDelayMilis = new IntLongVarField(this,300);
	
//	protected long lClickDelayMilis;
	
	/**
	 * Calling this method will also promptly instantiate this class and
	 * make its console var(s) initially available!
	 * 
	 * @param lClickDelayMilis will use default if null
	 */
	public void configure(Long lClickDelayMilis) {
		if(lClickDelayMilis!=null)ilvClickMaxDelayMilis.setObjectValue(lClickDelayMilis);
		GlobalCommandsDelegatorI.i().get().addConsoleCommandListener(this);
	}
	
	static class MouseCursorButtonData{
		Long lPressedMilis = null;
//		Long lReleasedMilis = null;
		
		Vector3f v3fPressedPos = null;
		
		Vector3f v3fDragLastUpdatePos = null;
//		Vector3f v3fReleasedPos = null;
		
//		CursorButtonEvent eventButton;
//		Vector3f v3fPressedDraggingPosToConsume = null; 
//		CursorMotionEvent	eventMotion;
//		public boolean	bIsPressed;
//		public Vector3f	v3fDragDisplacement;
		
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
	}
	
	public static class MouseCursorUpdatedValues{
		public EMouseCursorButton emcb = null;
		public Long lButtonReleasedDelayMilis = null;
		public Vector3f v3fDragDisplacementStep = null;
	}
	
	public static void invertButtons(){
		EMouseCursorButton.ActionClick.setIndex(
			EMouseCursorButton.ContextPropertiesClick.getIndex());
		
		EMouseCursorButton.ContextPropertiesClick.setIndex(
			EMouseCursorButton.ActionClick.getIndex());
	}
	
	public static enum EMouseCursorButton{
		ActionClick(MouseInput.BUTTON_LEFT),
		ContextPropertiesClick(MouseInput.BUTTON_RIGHT),
		ScrollClick(MouseInput.BUTTON_MIDDLE),
		
		//TODO is below not actually supportable?
		Action4Click(MouseInput.BUTTON_MIDDLE+1),
		Action5Click(MouseInput.BUTTON_MIDDLE+2),
		Action6Click(MouseInput.BUTTON_MIDDLE+3),
		Action7Click(MouseInput.BUTTON_MIDDLE+4),
		Action8Click(MouseInput.BUTTON_MIDDLE+5),
		Action9Click(MouseInput.BUTTON_MIDDLE+6),
		;
		
		int iIndex;
		
		MouseCursorButtonData mcbd = new MouseCursorButtonData();
		
		EMouseCursorButton(int iIndex){
			setIndex(iIndex);
		}
		public void setIndex(int iIndex){
			this.iIndex=iIndex;
		}
		public int getIndex(){
			return iIndex;
		}
		
		public boolean isIndex(int iIndex){
			return this.iIndex==iIndex;
		}
		
		public static EMouseCursorButton get(int iIndex){
			for(EMouseCursorButton e:EMouseCursorButton.values()){
				if(e.isIndex(iIndex))return e;
			}
			return null;
		}
		
//		public EMouseCursorButton prepareData(CursorButtonEvent event) {
////	  	mcbd.eventButton = event;
////	  	mcbd.bIsPressed = event.isPressed();
//	  	
//	    long lMilis = System.currentTimeMillis();
//	    Vector3f v3fPos = MiscJmeI.i().eventToV3f(event);
//	    
//	    if(event.isPressed()){
////	    	mcbd.v3fReleasedPos = null;
////	    	mcbd.lReleasedMilis = null;
//	    	
//	    	mcbd.v3fPressedPos = v3fPos;
//	    	mcbd.lPressedMilis = lMilis;
////	    	binfo.v3fPressedDraggingPosToConsume = v3fPos;
//	    }else{
//	    	mcbd.v3fReleasedPos = v3fPos;
//	    	mcbd.lReleasedMilis = lMilis;
//	    }
//	    
//	    return this;
//		}
//		
//		public boolean isClicked(){
//	    if(!mcbd.eventButton.isPressed() && mcbd.lReleasedMilis != null){
//	  		if(mcbd.lReleasedMilis <= (mcbd.lPressedMilis + MouseCursor.i().ilvClickDelayMilis.getLong())){
//	  			return true;
//	  		}
//	    }
//	    
//	    return false;
//		}
//		
		public boolean isPressed() {
			return mcbd.lPressedMilis!=null;
		}
		
		public void setPressed(Vector3f v3fPressedPos){
			if(isPressed())throw new PrerequisitesNotMetException("already pressed! ",this,v3fPressedPos);
			
			mcbd.lPressedMilis = System.currentTimeMillis();
			mcbd.v3fPressedPos = v3fPressedPos.clone();
			mcbd.v3fDragLastUpdatePos = v3fPressedPos.clone();
		}
		
		public Vector3f updateDragPosAndGetDisplacement(Vector3f v3fNewDragPos){
			if(!isPressed())return null;
			
			Vector3f v3fDiff = mcbd.v3fDragLastUpdatePos.subtract(v3fNewDragPos);
			mcbd.v3fDragLastUpdatePos.set(v3fNewDragPos);
			
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
			
			long lDelay = System.currentTimeMillis() - mcbd.lPressedMilis;
			mcbd.lPressedMilis=null;
			return lDelay;
		}
		
//		public void applyDrag(Vector3f v3fMouseCursorPosPrevious, Vector3f v3fNewMouseCursorPos) {
//			Vector3f v3fDisplacement = v3fMouseCursorPosPrevious
//				.subtract(v3fNewMouseCursorPos).negate();
//			
//			mcbi.v3fDragDisplacement = v3fDisplacement;
//		}
		
//		public Vector3f getDragDisplacement() {
//			return mcbd.v3fDragDisplacement;
//		}
//		
//		public CursorButtonEvent getLastButtonEvent() {
//			return mcbd.eventButton;
//		}
//		
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().get().getReflexFillCfg(rfcv);
	}

	public boolean isClickDelay(Long lDelayMilis) {
		return lDelayMilis < ilvClickMaxDelayMilis.getLong();
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
			e.mcbd.reset();
		}
	}
	
	public String report(){
		String str="";
		for(EMouseCursorButton e:EMouseCursorButton.values()){
			str+=""+e+": "+e.mcbd.toString()+"\n";
		}
		return str;
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,CMD_FIX_RESETING_MOUSE_CURSOR,null)){
			String strBefore = report();
			cc.dumpSubEntry(cc.getCommentPrefixStr()+"Before:\n"+report());
			
			resetFixingAllButtonsState();
			
			String strAfter = report();
			String[] aBefore = strBefore.split("\n");
			String[] aAfter = strAfter.split("\n");
			boolean bChanged=false;
			for(int i=0;i<aBefore.length;i++){
				if(!aBefore[i].equals(aAfter[i])){
					cc.dumpSubEntry(cc.getCommentPrefixStr()+"Changed: "+aAfter[i]);
					bChanged = true;
				}
			}
			if(!bChanged)cc.dumpSubEntry(cc.getCommentPrefixStr()+"Nothing changed...");
			
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(this,"mouseCursorReport","")){
			cc.dumpSubEntry(report());
			bCommandWorked=true;
		}else
		{
//			return cc.executePreparedCommandRoot();
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
}
