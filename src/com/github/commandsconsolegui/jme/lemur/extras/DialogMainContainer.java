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
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jme.DialogStateAbs;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogStateAbs;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.core.GuiLayout;
import com.simsilica.lemur.style.ElementId;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class DialogMainContainer extends Container implements ISpatialValidator, IReflexFillCfg{
	private DialogStateAbs	diagOwner;
	private boolean	bLayoutValid;
//	ISpatialValidator diag;
//	
	
	private Vector3f	v3fLastValidPreferredSize;
	private Vector3f	v3fLastFailedSize;
	
	private BoolTogglerCmdField btgAllowInvalidSize = new BoolTogglerCmdField(this,false);
	private String	strMessageKey;
	
	@Override
	public boolean isLayoutValid() {
		return bLayoutValid;
	}
	
	@Override
	public void updateLogicalState(float tpf) {
		if(!bUseCrashPrevention){
			super.updateLogicalState(tpf);
			
			//after update
			bLayoutValid=true;
			return;
		}
		
		ArrayList<Object> aobjDbg=new ArrayList<Object>();
		aobjDbg.add(this);
		aobjDbg.add(cntrCenterMain);
		aobjDbg.add(pnlFallbackForImpossibleLayout);
		aobjDbg.add(diagOwner);
		aobjDbg.add(getSize());
		aobjDbg.add(getPreferredSize());
		aobjDbg.add(getLocalTranslation());
		aobjDbg.add(tpf);
		
		if(v3fLastFailedSize==null || !v3fLastFailedSize.equals(getSize())){
			Vector3f v3fMinSize = new Vector3f(20,20,0);
			Vector3f v3fPreferredSize = getPreferredSize().clone();
			if(v3fPreferredSize.x<v3fMinSize.x){
				diagOwner.setRequestHitBorderToContinueDragging(true);
				v3fPreferredSize.x=v3fMinSize.x;
			}
			if(v3fPreferredSize.y<v3fMinSize.y){
				diagOwner.setRequestHitBorderToContinueDragging(true);
				v3fPreferredSize.y=v3fMinSize.y;
			}
			if(!v3fPreferredSize.equals(getPreferredSize())){
				setPreferredSize(v3fPreferredSize);
			}
			
			try{
				addChild(cntrCenterMain, BorderLayout.Position.Center); //actually replaces
				super.updateLogicalState(tpf);
				
				//after update
				v3fLastValidPreferredSize = getPreferredSize().clone();
				v3fLastFailedSize = null;
				bLayoutValid=true;
			}catch(IllegalArgumentException e){
				bLayoutValid=false;
				
				v3fLastFailedSize = getSize().clone();
				
				messageIgnoringSafeOnes(e,aobjDbg);
				
//				if(btgAllowInvalidSize.b()){
//					GlobalCommandsDelegatorI.i().dumpDevInfoEntry(strMessageKey, aobjDbg);
//				}else{
//					GlobalCommandsDelegatorI.i().dumpDevWarnEntry(strMessageKey, aobjDbg);
//				}
				
				addChild(pnlFallbackForImpossibleLayout, BorderLayout.Position.Center); //actually replaces
			}
		}
		
		if(!bLayoutValid){
			try{
				if(btgAllowInvalidSize.b()){
					super.updateLogicalState(tpf); //this lets the user resize the dialog freely
				}else{
					// restore last valid
					if(v3fLastValidPreferredSize!=null){
						addChild(cntrCenterMain, BorderLayout.Position.Center); //actually replaces
						setPreferredSize(v3fLastValidPreferredSize);
						super.updateLogicalState(tpf);
						
						diagOwner.setRequestHitBorderToContinueDragging(true);
						
						//after update
						bLayoutValid=true; 
					}
				}
			}catch(IllegalArgumentException e){
				aobjDbg.add(e);
				messageIgnoringSafeOnes(e,aobjDbg);
			}
		}
		
	}
	
//	private LemurDialogStateAbs getLemurDiagOwner() {
//		return (LemurDialogStateAbs)diagOwner;
//	}

	private void messageIgnoringSafeOnes(Exception e, ArrayList<Object> aobjDbg){
		if(e.getMessage().startsWith("Size cannot be negative:")){
		}else
		{
			Exception ex = new Exception(strMessageKey);
			ex.setStackTrace(e.getStackTrace());
			ex.initCause(e.getCause());
//			aobjDbg.add(ex);
//			GlobalCommandsDelegatorI.i().dumpDevWarnEntry(strMessageKey, aobjDbg);
			GlobalCommandsDelegatorI.i().dumpExceptionEntry(ex, aobjDbg);
		}
	}
	
//	/**
//	 * TODO is this useful too?
//	 */
//	@Override
//	public void updateGeometricState() {
//		try{
//			super.updateGeometricState();
//		}catch(Exception e){
//			GlobalCommandsDelegatorI.i().dumpExceptionEntry(e, this);
//		}
//	}
	
	public DialogMainContainer() {
		super();
		throw new UnsupportedOperationException("use the right constructor");
	}
	
	public DialogMainContainer(ElementId elementId, String style) {
		super(elementId, style);
		throw new UnsupportedOperationException("use the right constructor");
	}
	
	public DialogMainContainer(ElementId elementId) {
		super(elementId);
		throw new UnsupportedOperationException("use the right constructor");
	}
	
	public DialogMainContainer(GuiLayout layout, boolean applyStyles,
			ElementId elementId, String style) {
		super(layout, applyStyles, elementId, style);
		throw new UnsupportedOperationException("use the right constructor");
	}
	
	public DialogMainContainer(GuiLayout layout, ElementId elementId, String style) {
		super(layout, elementId, style);
		throw new UnsupportedOperationException("use the right constructor");
	}
	
	public DialogMainContainer(GuiLayout layout, ElementId elementId) {
		super(layout, elementId);
		throw new UnsupportedOperationException("use the right constructor");
	}
	
	public DialogMainContainer(DialogStateAbs diagOwner, GuiLayout layout, String style) {
		super(layout, style);
		setDiagOwner(diagOwner);
		this.strMessageKey = diagOwner.getId()+" update logical state failed";
	}
	
	private void setDiagOwner(DialogStateAbs diagOwner) {
		this.diagOwner=diagOwner;
	}

	public DialogMainContainer(GuiLayout layout) {
		super(layout);
		throw new UnsupportedOperationException("use the right constructor");
	}
	
	public DialogMainContainer(String style) {
		super(style);
		throw new UnsupportedOperationException("use the right constructor");
	}

	private Panel	pnlFallbackForImpossibleLayout;
	private Container cntrCenterMain;
	private boolean	bUseCrashPrevention = false;

	public void setImpossibleLayoutIndicatorAndCenterMain(Panel pnlImpossibleLayout, Container cntrCenterMain) {
		bUseCrashPrevention=true;
		this.pnlFallbackForImpossibleLayout = pnlImpossibleLayout==null ? new Panel() : pnlImpossibleLayout;
		this.cntrCenterMain=cntrCenterMain;
		
		MiscJmeI.i().setUserDataPSH(this.pnlFallbackForImpossibleLayout, diagOwner);
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcvField) {
		ReflexFillCfg rfcfg = GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcvField);
		rfcfg.setPrefixCustomId(diagOwner.getId());
		return rfcfg;
	}
	
//	@Override
//	public boolean isAllowLogicalStateUpdate() {
//		return diag.isAllowLogicalStateUpdate();
//	}
	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}
}
