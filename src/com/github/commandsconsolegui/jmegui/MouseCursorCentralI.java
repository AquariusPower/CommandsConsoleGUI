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

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.cmd.varfield.StringCmdField;
import com.github.commandsconsolegui.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.ConditionalStateManagerI.CompositeControl;
import com.github.commandsconsolegui.misc.CompositeControlAbs;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * Allows multiple mouse buttons to be clicked or dragged at same time.
 * @author AquariusPower <https://github.com/AquariusPower>
 */
public class MouseCursorCentralI implements IReflexFillCfg, IConsoleCommandListener{
	private static MouseCursorCentralI instance = new MouseCursorCentralI();
	public static MouseCursorCentralI i(){return instance;}
	
	public static class CompositeControl extends CompositeControlAbs<MouseCursorCentralI>{
		private CompositeControl(MouseCursorCentralI casm){super(casm);};
	}
	private CompositeControl ccSelf = new CompositeControl(this);
	
	public final StringCmdField CMD_FIX_RESETING_MOUSE_CURSOR = new StringCmdField(this,CommandsDelegator.strFinalCmdCodePrefix);
	
	IntLongVarField ilvClickMaxDelayMilis = new IntLongVarField(this,300,"the delay between button pressed and button released");
	IntLongVarField ilvMultiClickMaxDelayMilis = new IntLongVarField(this,500,"the delay between each subsequent click (button released moment)");
	
	ArrayList<MouseCursorButtonsControl> amcabList = new ArrayList<MouseCursorButtonsControl>();
	
	public MouseCursorButtonsControl  createButtonsInstance(Object objParent){
		MouseCursorButtonsControl mcab = new MouseCursorButtonsControl(ccSelf,objParent);
		amcabList.add(mcab);
		return mcab;
	}
	
//	protected long lClickDelayMilis;
	
	/**
	 * Calling this method will also promptly instantiate this class and
	 * make its console var(s) initially available!
	 * 
	 * @param lClickDelayMilis will use default if null
	 */
	public void configure(Long lClickDelayMilis) {
		if(lClickDelayMilis!=null)ilvClickMaxDelayMilis.setObjectValue(lClickDelayMilis);
		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
	}
	
	public static class MouseCursorUpdatedValues{
		public EMouseCursorButton emcb = null;
		public Long lButtonReleasedDelayMilis = null;
		public Vector3f v3fDragDisplacementStep = null;
	}
	
	public static void invertButtons(){
		EMouseCursorButton.Action1Click.setIndex(
			EMouseCursorButton.ContextPropertiesClick.getIndex());
		
		EMouseCursorButton.ContextPropertiesClick.setIndex(
			EMouseCursorButton.Action1Click.getIndex());
	}
	
	public static enum EMouseCursorButton{
		Action1Click(MouseInput.BUTTON_LEFT),
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
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}
	
	public boolean isMultiClickDelayFrom(Long lFromMilis) {
		return isMultiClickDelay(System.currentTimeMillis()-lFromMilis);
	}
	public boolean isMultiClickDelay(Long lDelayMilis) {
		return lDelayMilis <= ilvMultiClickMaxDelayMilis.getLong();
	}
	
	public boolean isClickDelayFrom(Long lFromMilis) {
		return isClickDelay(System.currentTimeMillis()-lFromMilis);
	}
	public boolean isClickDelay(Long lDelayMilis) {
		return lDelayMilis <= ilvClickMaxDelayMilis.getLong();
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,CMD_FIX_RESETING_MOUSE_CURSOR,null)){
			String strBefore = report();
			cc.dumpSubEntry(cc.getCommentPrefixStr()+"Before:\n"+report());
			
			for(MouseCursorButtonsControl mcab:amcabList){
				mcab.resetFixingAllButtonsState();
			}
			
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
			for(MouseCursorButtonsControl mcab:amcabList){
				cc.dumpSubEntry(mcab.report());
			}
			bCommandWorked=true;
		}else
		{
//			return cc.executePreparedCommandRoot();
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}

	private String report() {
		String str="";
		for(MouseCursorButtonsControl m:amcabList){
			str+=m.report();
		}
		return str;
	}

}
