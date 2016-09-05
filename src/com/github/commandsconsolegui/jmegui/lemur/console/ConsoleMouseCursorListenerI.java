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

package com.github.commandsconsolegui.jmegui.lemur.console;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.jmegui.console.GlobalConsoleGUII;
import com.github.commandsconsolegui.jmegui.MouseCursorButtonData;
import com.github.commandsconsolegui.jmegui.MouseCursorCentralI;
import com.github.commandsconsolegui.jmegui.console.ConsoleStateAbs;
import com.github.commandsconsolegui.jmegui.lemur.MouseCursorListenerAbs;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorMotionEvent;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ConsoleMouseCursorListenerI extends MouseCursorListenerAbs {
	private static ConsoleMouseCursorListenerI instance = new ConsoleMouseCursorListenerI();
	public static ConsoleMouseCursorListenerI i(){return instance;}
	
//	private ConsoleStateAbs<?> csa;
	private CommandsDelegator	cd;
//	private ConsoleStateAbs<?>	cgui;
	private boolean	bConfigured;
	
	public void configure(){
		this.cd=GlobalCommandsDelegatorI.i();
//		this.cgui=GlobalConsoleGuiI.i();
		
		if(!MouseCursorCentralI.i().isConfigured()){
			MouseCursorCentralI.i().configure(null);
		}
		
		bConfigured=true;
	}

	private String debugPart(Panel pnl){
		return pnl.getName()+","
			+pnl.getElementId()+","
			+pnl.getClass().getSimpleName()
			+";";
	}
	
	public void debugReport(CursorMotionEvent eventM, CursorButtonEvent eventB, Spatial target,	Spatial capture){
		if(!cd.btgShowDebugEntries.b())return;
		
		Panel pnlTgt = (Panel)target;
		Panel pnlCap = (Panel)capture;
		
		String strTgt	=	"";if(pnlTgt!=null)strTgt	="Tgt:"+debugPart(pnlTgt);
		String strCap	=	"";if(pnlCap!=null)strCap	="Cap:"+debugPart(pnlCap);
		String strB		=	"";if(eventB!=null)strB		="B:"+eventB.getButtonIndex()+";";
		String strM		=	"";if(eventM!=null)strM		="M:"+eventM.getX()+","+eventM.getY()+";";
		cd.dumpDebugEntry("FOCUS@"+strTgt+strCap+strB+strM);
	}
	
	private void cursorMoveEvent(CursorMotionEvent event, Spatial target,	Spatial capture) {
		if(!GlobalConsoleGUII.i().isScrollRequestTarget(target)){ //detect changed
			debugReport(event, null, target, capture);
		}
		
		GlobalConsoleGUII.i().setScrollRequestTarget(target);
	}
	
	@Override
	public void cursorEntered(CursorMotionEvent event, Spatial target,	Spatial capture) {
		super.cursorEntered(event, target, capture);
		cursorMoveEvent(event, target, capture);
	}
	
	@Override
	public void cursorMoved(CursorMotionEvent event, Spatial target, Spatial capture) {
		super.cursorMoved(event, target, capture);
		cursorMoveEvent(event, target, capture);
	}
	
//	@Override
//	private boolean click(EMouseCursorButton button, Spatial target,	Spatial capture) {
//		debugReport(null, button.getLastButtonEvent(), target, capture);
//		
//		if(!button.isPressed()){ //on release
//			switch(button){
//				case ActionClick:
//					if(cgui.isHintBox(target)){
//						cgui.checkAndApplyHintAtInputField();
//						return true;
//					}
//					break;
//				case ContextPropertiesClick:
//					break;
//				case ScrollClick:
//					break;
//			}
//		}
//		
//		return super.click(button, target, capture);
//	}
	
	@Override
	public boolean click(MouseCursorButtonData button, CursorButtonEvent eventButton, Spatial target,Spatial capture) {
		debugReport(null, eventButton, target, capture);
		
//		if(!button.isPressed()){ //on release
		switch(button.getActivatorType()){
			case Action1Click:
				if(GlobalConsoleGUII.i().isHintBox(target)){
					GlobalConsoleGUII.i().checkAndApplyHintAtInputField();
					return true;
				}
				break;
			case ContextPropertiesClick:
				break;
			case ScrollClick:
				break;
		}
//		}
		
		return super.click(button, eventButton, target, capture);
	}

	public boolean isConfigured() {
		return bConfigured;
	}
}
