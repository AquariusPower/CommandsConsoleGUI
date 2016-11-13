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

package com.github.commandsconsolegui.jme.lemur;

import java.util.ArrayList;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jme.DialogStateAbs;
import com.github.commandsconsolegui.jme.MouseCursorButtonData;
import com.github.commandsconsolegui.jme.ManageMouseCursorI.EMouseCursorButton;
import com.github.commandsconsolegui.jme.MultiClickCondStateI;
import com.github.commandsconsolegui.jme.MultiClickCondStateI.ECallMode;
import com.github.commandsconsolegui.jme.lemur.console.LemurDiagFocusHelperStateI;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogStateAbs;
import com.github.commandsconsolegui.jme.lemur.extras.CellRendererDialogEntry.CellDialogEntry;
import com.github.commandsconsolegui.jme.lemur.extras.CellRendererDialogEntry.CellDialogEntry.EUserDataCellEntry;
import com.github.commandsconsolegui.misc.CallQueueI.CallableWeak;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.github.commandsconsolegui.misc.jme.lemur.MiscLemurStateI;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorMotionEvent;


/**
 * This will track the parentest spatial and let mouse cursor move it!
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class DialogMouseCursorListenerI extends MouseCursorListenerAbs {
	private static DialogMouseCursorListenerI instance = new DialogMouseCursorListenerI();
	public static DialogMouseCursorListenerI i(){return instance;}
	
	@Override
	public boolean clickEnd(MouseCursorButtonData buttonData, CursorButtonEvent eventButton, Spatial target,Spatial capture) {
		if(capture==null)return false; //TODO ??? if(capture==null)capture=target; ??? 
		
		LemurDiagFocusHelperStateI.i().requestDialogFocus(capture);
		
		//TODO try to disable elements at parent dialog (as modal child has focus), this didnt work: if(LemurFocusHelperStateI.i().isDialogFocusedFor(capture))return false;
		
		// missing ones are ignored so each element can consume it properly
		boolean bConsumed = false;
		final LemurDialogStateAbs<?,?> diag = 
				(LemurDialogStateAbs)LemurDiagFocusHelperStateI.i().retrieveDialogFromSpatial(capture);
		EMouseCursorButton e = buttonData.getActivatorType();
		switch(e){
			case Action1Click:
//				if(iClickCount==1){
//					final CellDialogEntry cell = (CellDialogEntry<?>)capture.getUserData(EUserDataCellEntry.classCellRef.s());
					final CellDialogEntry cell = MiscJmeI.i().getUserDataPSH(capture, CellDialogEntry.class);
					if(cell!=null){
//						if(cell.isCfgButton(capture)){
//							diag.openCfgDataDialog(cell.getData());
//							bConsumed=true;
//						}else
						if(cell.isTextButton(capture)){
							switch(MultiClickCondStateI.i().getActivatorNextUpdateIndex(capture)){
								case 1:
									MultiClickCondStateI.i().updateActivator(ECallMode.OncePromptly, capture, new CallableWeak<Boolean>() {
										@Override public Boolean call() {
											diag.selectEntry(cell.getDialogListEntryData());
											return true;
										}
									});
									bConsumed=true;
									break;
								case 2:
									MultiClickCondStateI.i().updateActivator(ECallMode.OnceAfterDelay, capture, new CallableWeak<Boolean>() {
										@Override public Boolean call() {
											if(cell.getDialogListEntryData().isParent()){
												cell.getDialogListEntryData().toggleExpanded();
											}else{
												if(diag.isOptionSelectionMode()){
													diag.selectAndChoseOption(cell.getDialogListEntryData());
												}else{
													diag.execTextDoubleClickActionFor(cell.getDialogListEntryData());
	//												diag.openCfgDataDialog(cell.getData());
												}
											}
											return true;
										}
									});
									bConsumed=true;
									break;
								case 3: //skipper
									MultiClickCondStateI.i().updateActivator(
										ECallMode.JustSkip, capture, null);
									bConsumed=true;
									break;
							}
						}else
//						if(cell.isSelectButton(capture)){
//							diag.selectAndChoseOption(cell.getData());
////							diag.selectEntry(cell.getData());
////							diag.requestActionSubmit();
//							bConsumed=true;
//						}else
						{
							GlobalCommandsDelegatorI.i().dumpDevWarnEntry("no support for element"+capture.getName()
									+", but... there are two listeners, this is the mouse one, "
									+"the cursor one is not consuming the event... "
									+"see  PickEventSession.buttonEvent()",
								diag, capture, cell); //TODO implement multi-click at the other listener flow?
//							throw new PrerequisitesNotMetException("missing support for element "+capture.getName(), diag, capture, cell);
						}
					}
//				}else
//				if(iClickCount>=2){
//					bConsumed = diag.actionMultiClick(buttonData,capture,iClickCount);
//				}
				break;
			case ScrollClick:
				bConsumed = LemurDiagFocusHelperStateI.i().lowerDialogFocusPriority(capture);
				break;
//			case ContextPropertiesClick:
////			LemurDialogGUIStateAbs diag = LemurFocusHelperStateI.i().retrieveDialogFromSpatial(capture);
//				bConsumed = diag.openPropertiesDialogFor(capture);
//				break;
			default:
				bConsumed = diag.execActionFor(e,capture);
				break;
		}
		
		if(bConsumed)return true;
		
		return super.clickEnd(buttonData, eventButton, target, capture);
	}
	
	@Override
	public boolean dragging(ArrayList<MouseCursorButtonData> aButtonList,CursorMotionEvent eventMotion, Spatial target, Spatial capture) {
		for(MouseCursorButtonData buttonData:aButtonList){
			// missing ones are ignored so each element can consume it properly
			switch(buttonData.getActivatorType()){
				case Action1Click:
					Spatial sptDialogMain = MiscJmeI.i().getParentestFrom(capture);
					Vector3f v3fNewPos = MiscLemurStateI.i().eventToV3f(eventMotion);
					Vector3f v3fDisplacement = buttonData.updateDragPosAndGetDisplacement(eventMotion, v3fNewPos);
					
					DialogStateAbs diag = MiscJmeI.i().getUserDataPSH(sptDialogMain,DialogStateAbs.class);
					diag.setBeingDragged(this,true);
					diag.move(capture, v3fDisplacement);
//					sptDialogMain.move(v3fDisplacement);
					return true;
			}
		}
			
		return super.dragging(aButtonList, eventMotion, target, capture);
	}
	
	@Override
	public boolean dragEnd(MouseCursorButtonData buttonData, CursorButtonEvent eventButton, Spatial target, Spatial capture) {
		switch(buttonData.getActivatorType()){
			case Action1Click:
				/**
				 * https://hub.jmonkeyengine.org/t/lemur-cursorlistener-it-is-ok-to-use-target-if-capture-is-null/36936/2
				 * 
				 * If you don't have a capture then that just means the button down event never happened over 
				 * another spatial... or if it did, that spatial never consumed the event. So there is no capture.
				 * 
				 * Target is the spatial that is currently receiving the event. The one to which this listener is 
				 * attached in some way.
				 */
				if(capture!=null){
					Spatial sptDialogMain = MiscJmeI.i().getParentestFrom(capture);
					
					DialogStateAbs diag = MiscJmeI.i().getUserDataPSH(sptDialogMain,DialogStateAbs.class);
					
//					if(diag==null){
////						GlobalCommandsDelegatorI.i().dumpWarnEntry("no direct dialog for dragged: "+capture.getName(),
////							capture, sptDialogMain);
//						if(sptDialogMain instanceof CellDialogEntry){
//							CellDialogEntry cde = (CellDialogEntry)sptDialogMain;
//							diag = cde.getDialogOwner();
//						}
//					}
					
//					if(diag!=null){
						diag.setBeingDragged(this,false);
						diag.save();
		//				diag.requestSaveDialog();
//					}
					
					return true;
				}
		}
		
		return super.dragEnd(buttonData, eventButton, target, capture);
	}
}
