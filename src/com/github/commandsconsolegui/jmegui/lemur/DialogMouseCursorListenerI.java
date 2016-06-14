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
import com.github.commandsconsolegui.jmegui.MouseCursorButtonData;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jmegui.lemur.extras.CellRendererDialogEntry.Cell;
import com.github.commandsconsolegui.jmegui.lemur.extras.CellRendererDialogEntry.ECell;
import com.github.commandsconsolegui.jmegui.lemur.extras.LemurDialogGUIStateAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorMotionEvent;


/**
 * This will track the parentest spatial and let mouse cursor move it!
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class DialogMouseCursorListenerI extends MouseCursorListenerAbs {
	private static DialogMouseCursorListenerI instance = new DialogMouseCursorListenerI();
	public static DialogMouseCursorListenerI i(){return instance;}
	
	@Override
	public boolean click(MouseCursorButtonData buttonData, CursorButtonEvent eventButton, Spatial target,Spatial capture, int iClickCount) {
		LemurFocusHelperStateI.i().requestDialogFocus(capture);
		
		// missing ones are ignored so each element can consume it properly
		boolean bConsumed = false;
		switch(buttonData.getActivatorType()){
			case Action1Click:
				if(iClickCount==1){
					Cell cell = (Cell)capture.getUserData(ECell.CellClassRef.toString());
					if(cell!=null){
						LemurDialogGUIStateAbs diag = LemurFocusHelperStateI.i().retrieveDialogFromSpatial(capture);
						if(cell.isCfgButton(capture)){
							diag.openCfgDataDialog(cell.getData());
						}else
						if(cell.isTextButton(capture)){
							diag.selectEntry(cell.getData());
						}else
						if(cell.isSelectButton(capture)){
							diag.selectEntry(cell.getData());
							diag.requestActionSubmit();
						}else{
							throw new PrerequisitesNotMetException("missing support for element "+capture.getName(), diag, capture, cell);
						}
					}
				}else
				if(iClickCount>=2){
					LemurDialogGUIStateAbs diag = LemurFocusHelperStateI.i().retrieveDialogFromSpatial(capture);
					bConsumed = diag.actionMultiClick(buttonData,capture,iClickCount);
				}
				break;
			case ScrollClick:
				bConsumed = LemurFocusHelperStateI.i().lowerDialogFocusPriority(capture);
				break;
			case ContextPropertiesClick:
				LemurDialogGUIStateAbs diag = LemurFocusHelperStateI.i().retrieveDialogFromSpatial(capture);
				bConsumed = diag.openPropertiesDialogFor(capture);
				break;
		}
		
		if(bConsumed)return true;
		
		return super.click(buttonData, eventButton, target, capture, iClickCount);
	}
	
	@Override
	public boolean drag(ArrayList<MouseCursorButtonData> aButtonList,CursorMotionEvent eventMotion, Spatial target, Spatial capture) {
		for(MouseCursorButtonData buttonData:aButtonList){
			// missing ones are ignored so each element can consume it properly
			switch(buttonData.getActivatorType()){
				case Action1Click:
					Spatial sptDialogMain = MiscJmeI.i().getParentestFrom(capture);
					Vector3f v3fNewPos = MiscJmeI.i().eventToV3f(eventMotion);
					Vector3f v3fDisplacement = buttonData.updateDragPosAndGetDisplacement(eventMotion, v3fNewPos);
					sptDialogMain.move(v3fDisplacement);
					return true;
			}
		}
			
		return super.drag(aButtonList, eventMotion, target, capture);
	}
}
