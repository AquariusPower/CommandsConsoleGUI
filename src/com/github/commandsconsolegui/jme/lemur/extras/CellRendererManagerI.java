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
package com.github.commandsconsolegui.jme.lemur.extras;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.misc.IManager;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.WorkAroundI.BugFixBoolTogglerCmdField;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class CellRendererManagerI implements IReflexFillCfg,IManager{
	private static CellRendererManagerI instance = new CellRendererManagerI();
	public static CellRendererManagerI i(){return instance;}
	
	public final StringVarField svfTreeDepthToken = new StringVarField(this, " ", null);
	public final BoolTogglerCmdField	btgShowTreeUId = new BoolTogglerCmdField(this,false).setCallNothingOnChange();
	public final BugFixBoolTogglerCmdField btgNOTWORKINGBugFixGapForListBoxSelectorArea = new BugFixBoolTogglerCmdField(this,false);
//		.setCallerAssigned(new CallableX(this) {
//			@Override
//			public Boolean call() {
//				/**
//				 * param ex.: Geometry geomCursor = MiscI.i().getParamFromArray(Geometry.class, aobjCustomParams, 0);
//				 */
//				
//				Container cntr=null;
//	//			if(btgNOTWORKINGBugFixGapForListBoxSelectorArea.b()){ //TODO the fix is not working anymore
//					/**
//					 * this requires that all childs (in this case buttons) have their style background
//					 * color transparent (like alpha 0.5f) or the listbox selector will not be visible below them...
//					 */
//					// same layout as the cell container
//					cntr = new Container(new BorderLayout(), assignedCellRenderer.strStyle);
//					cntr.setName(btgNOTWORKINGBugFixGapForListBoxSelectorArea.getSimpleId()); //when mouse is over a cell, if the ListBox->selectorArea has the same world Z value of the button, it may be ordered before the button on the raycast collision results at PickEventSession.setCurrentHitTarget(ViewPort, Spatial, Vector2f, CollisionResult) line: 262	-> PickEventSession.cursorMoved(int, int) line: 482 
//					addChild(cntr, Position.Center);
//				
//				this.setCallerReturnValue(cntr);
//				
//				return true;
//			}
//		});

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException,	IllegalAccessException {
		return fld.get(this);
	}

	@Override
	public void setFieldValue(Field fld, Object value)throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}
	
	@Deprecated
	@Override
	public boolean add(Object objNew) {
		throw new UnsupportedOperationException("method not implemented yet");
	}
	
	@Deprecated
	@Override
	public ArrayList getListCopy() {
		throw new UnsupportedOperationException("method not implemented yet");
	}
}
