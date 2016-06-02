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

package com.github.commandsconsolegui.console.jmegui.lemur;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.console.jmegui.ConsoleGuiStateAbs;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.globals.GlobalConsoleGuiI;
import com.github.commandsconsolegui.globals.GlobalSappRefI;
import com.jme3.app.SimpleApplication;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.event.DefaultCursorListener;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ConsoleCursorListener extends DefaultCursorListener {
	ConsoleGuiStateAbs csa;
	private SimpleApplication	sapp;
	private CommandsDelegatorI	cc;
	private ConsoleGuiStateAbs	cgui;
	
	public void configure(){
		this.sapp=GlobalSappRefI.i().get();
		this.cc=GlobalCommandsDelegatorI.i().get();
		this.cgui=GlobalConsoleGuiI.i().get();
	}

	protected String debugPart(Panel pnl){
		return pnl.getName()+","
			+pnl.getElementId()+","
			+pnl.getClass().getSimpleName()
			+";";
	}
	
	public void debugReport(CursorMotionEvent eventM, CursorButtonEvent eventB, Spatial target,	Spatial capture){
		if(!cc.btgShowDebugEntries.b())return;
		
		Panel pnlTgt = (Panel)target;
		Panel pnlCap = (Panel)capture;
		
		String strTgt	=	"";if(pnlTgt!=null)strTgt	="Tgt:"+debugPart(pnlTgt);
		String strCap	=	"";if(pnlCap!=null)strCap	="Cap:"+debugPart(pnlCap);
		String strB		=	"";if(eventB!=null)strB		="B:"+eventB.getButtonIndex()+";";
		String strM		=	"";if(eventM!=null)strM		="M:"+eventM.getX()+","+eventM.getY()+";";
		cc.dumpDebugEntry("FOCUS@"+strTgt+strCap+strB+strM);
	}
	
	protected void cursorMoveEvent(CursorMotionEvent event, Spatial target,	Spatial capture) {
		if(!cgui.isScrollRequestTarget(target)){ //detect changed
			debugReport(event, null, target, capture);
		}
		
		cgui.setScrollRequestTarget(target);
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
	
	@Override
	protected void click(CursorButtonEvent event, Spatial target,	Spatial capture) {
		super.click(event, target, capture);
		
		if(!event.isPressed()){ //on release
			if(event.getButtonIndex()==0){ //main button
				if(cgui.isHintBox(target)){
					cgui.checkAndApplyHintAtInputField();
				}
			}
		}
		
		debugReport(null, event, target, capture);
	}
}
