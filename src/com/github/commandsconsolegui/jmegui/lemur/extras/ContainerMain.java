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
import com.simsilica.lemur.Container;
import com.simsilica.lemur.core.GuiLayout;
import com.simsilica.lemur.style.ElementId;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class ContainerMain extends Container implements ISpatialValidator{
//	ISpatialValidator diag;
//	
//	public ContainerMain setDiagOwner(ISpatialValidator diag){
//		this.diag=diag;
//		return this;
//	}
	
	@Override
	public void updateLogicalState(float tpf) {
		try{
			super.updateLogicalState(tpf);
		}catch(Exception e){
			GlobalCommandsDelegatorI.i().dumpExceptionEntry(e, this, tpf);
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
	
	public ContainerMain() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ContainerMain(ElementId elementId, String style) {
		super(elementId, style);
		// TODO Auto-generated constructor stub
	}
	
	public ContainerMain(ElementId elementId) {
		super(elementId);
		// TODO Auto-generated constructor stub
	}
	
	public ContainerMain(GuiLayout layout, boolean applyStyles,
			ElementId elementId, String style) {
		super(layout, applyStyles, elementId, style);
		// TODO Auto-generated constructor stub
	}
	
	public ContainerMain(GuiLayout layout, ElementId elementId, String style) {
		super(layout, elementId, style);
		// TODO Auto-generated constructor stub
	}
	
	public ContainerMain(GuiLayout layout, ElementId elementId) {
		super(layout, elementId);
		// TODO Auto-generated constructor stub
	}
	
	public ContainerMain(GuiLayout layout, String style) {
		super(layout, style);
		// TODO Auto-generated constructor stub
	}
	
	public ContainerMain(GuiLayout layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}
	
	public ContainerMain(String style) {
		super(style);
		// TODO Auto-generated constructor stub
	}
	
//	@Override
//	public boolean isAllowLogicalStateUpdate() {
//		return diag.isAllowLogicalStateUpdate();
//	}
}
