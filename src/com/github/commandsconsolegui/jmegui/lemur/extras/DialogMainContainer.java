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

package com.github.commandsconsolegui.jmegui.lemur.extras;

import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
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
public class DialogMainContainer extends Container implements ISpatialValidator{
	private boolean	bLayoutValid;
//	ISpatialValidator diag;
//	
//	public ContainerMain setDiagOwner(ISpatialValidator diag){
//		this.diag=diag;
//		return this;
//	}
	
//	private Panel pnlDummy = new Panel();

	private Vector3f	v3fLastValidSize;
	
	@Override
	public boolean isLayoutValid() {
		return bLayoutValid;
	}
	
	@Override
	public void updateLogicalState(float tpf) {
		if(bUseCrashPrevention){
//			if(!isLayoutValid() && v3fLastValidSize!=null){
//				setPreferredSize(v3fLastValidSize);
//			}
			
			Vector3f v3fMinSize = new Vector3f(20,20,0);
//			Vector3f v3fMinSize = pnlImpossibleLayout.getPreferredSize(); //TODO should consider the border size too...
//			v3fMinSize.y*=2f; //TODO do not use this trick...
//			Vector3f v3fMinSize = getPreferredSize();
//			float fMin=20;
			Vector3f v3fSize = getPreferredSize().clone();
//			if(v3fSize.x<fMin)v3fSize.x=fMin;
//			if(v3fSize.y<fMin)v3fSize.y=fMin;
			if(v3fSize.x<v3fMinSize.x)v3fSize.x=v3fMinSize.x;
			if(v3fSize.y<v3fMinSize.y)v3fSize.y=v3fMinSize.y;
			if(!v3fSize.equals(getPreferredSize())){
				setPreferredSize(v3fSize);
			}
			
//			v3fLastValidSize = null;
			try{
//				if(!isLayoutValid()){
					addChild(cntrCenterMain, BorderLayout.Position.Center); //actually replaces
//				}
				super.updateLogicalState(tpf);
				v3fLastValidSize = getPreferredSize().clone();
				bLayoutValid=true;
			}catch(Exception e){
				bLayoutValid=false;
				
				GlobalCommandsDelegatorI.i().dumpExceptionEntry(e, this, tpf);
				
//				if(pnlImpossibleLayout.getSize().length() >= pnlImpossibleLayout.getPreferredSize().length()){
//					addChild(pnlImpossibleLayout, BorderLayout.Position.Center); //actually replaces
//				}else{
					addChild(pnlImpossibleLayout, BorderLayout.Position.Center); //actually replaces
//				}
				try{super.updateLogicalState(tpf);}catch(Exception e2){}
			}
		}else{
			super.updateLogicalState(tpf);
			bLayoutValid=true;
		}
		
//		if(isAllowLogicalStateUpdate()){
//			super.updateLogicalState(tpf);
//		}
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
		// TODO Auto-generated constructor stub
	}
	
	public DialogMainContainer(ElementId elementId, String style) {
		super(elementId, style);
		// TODO Auto-generated constructor stub
	}
	
	public DialogMainContainer(ElementId elementId) {
		super(elementId);
		// TODO Auto-generated constructor stub
	}
	
	public DialogMainContainer(GuiLayout layout, boolean applyStyles,
			ElementId elementId, String style) {
		super(layout, applyStyles, elementId, style);
		// TODO Auto-generated constructor stub
	}
	
	public DialogMainContainer(GuiLayout layout, ElementId elementId, String style) {
		super(layout, elementId, style);
		// TODO Auto-generated constructor stub
	}
	
	public DialogMainContainer(GuiLayout layout, ElementId elementId) {
		super(layout, elementId);
		// TODO Auto-generated constructor stub
	}
	
	public DialogMainContainer(GuiLayout layout, String style) {
		super(layout, style);
		// TODO Auto-generated constructor stub
	}
	
	public DialogMainContainer(GuiLayout layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}
	
	public DialogMainContainer(String style) {
		super(style);
		// TODO Auto-generated constructor stub
	}

	private Panel	pnlImpossibleLayout;
	private Container cntrCenterMain;
	private boolean	bUseCrashPrevention = false;
	public void setImpossibleLayoutIndicatorAndCenterMain(Panel pnlImpossibleLayout, Container cntrCenterMain) {
		bUseCrashPrevention=true;
		this.pnlImpossibleLayout = pnlImpossibleLayout==null ? new Panel() : pnlImpossibleLayout;
		this.cntrCenterMain=cntrCenterMain;
	}
	
//	@Override
//	public boolean isAllowLogicalStateUpdate() {
//		return diag.isAllowLogicalStateUpdate();
//	}
}
