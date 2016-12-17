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

package com.github.commandsconsolegui.spJme;

import java.lang.reflect.Field;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.ISingleInstance;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.spAppOs.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.spCmd.CommandsHelperI;
import com.github.commandsconsolegui.spCmd.IConsoleCommandListener;
import com.github.commandsconsolegui.spCmd.globals.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spCmd.varfield.IntLongVarField;
import com.github.commandsconsolegui.spCmd.varfield.StringCmdField;
import com.github.commandsconsolegui.spJme.globals.GlobalAppRefI;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.jme3.input.MouseInput;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

/**
 * Allows multiple mouse buttons to be clicked or dragged at same time.
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class ManageMouseCursorI implements IReflexFillCfg, IConsoleCommandListener, ISingleInstance{
	private static ManageMouseCursorI instance = new ManageMouseCursorI();
	public static ManageMouseCursorI i(){return instance;}
	
	public static final class CompositeControl extends CompositeControlAbs<ManageMouseCursorI>{
		private CompositeControl(ManageMouseCursorI casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	public ManageMouseCursorI() {
		DelegateManagerI.i().addHandled(this);
//		ManageSingleInstanceI.i().add(this);
	}
	
	public final StringCmdField CMD_FIX_RESETING_MOUSE_CURSOR = new StringCmdField(this,CommandsHelperI.i().getCmdCodePrefix());
	public final StringCmdField scfMouseCursorReport = new StringCmdField(this);
	
	IntLongVarField ilvClickMaxDelayMilis = new IntLongVarField(this,300,"the delay between button pressed and button released");
	IntLongVarField ilvMultiClickMaxDelayMilis = new IntLongVarField(this,500,"the delay between each subsequent click (button released moment)");
	
//	ArrayList<MouseCursorButtonsControl> amcabList = new ArrayList<MouseCursorButtonsControl>();
	
	MouseCursorButtonsControl mcbcSingleInstance = new MouseCursorButtonsControl(ccSelf,this);

	private boolean	bConfigured;
	
	public MouseCursorButtonsControl getButtonsInstance(){
		return mcbcSingleInstance;
	}
	
//	@Deprecated
//	public MouseCursorButtonsControl createButtonsInstance(Object objParent){
//		MouseCursorButtonsControl mcab = new MouseCursorButtonsControl(ccSelf,objParent);
//		amcabList.add(mcab);
//		return mcab;
//	}
	
//	private long lClickDelayMilis;
	
	/**
	 * Calling this method will also promptly instantiate this class and
	 * make its console var(s) initially available!
	 * 
	 * @param lClickDelayMilis will use default if null
	 */
	public void configure(Long lClickDelayMilis) {
		if(lClickDelayMilis!=null)ilvClickMaxDelayMilis.setObjectRawValue(lClickDelayMilis);
		GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
		
		if(!MultiClickCondStateI.i().isConfigured()){
			MultiClickCondStateI.i().configure(new MultiClickCondStateI.CfgParm());
		}
		
		bConfigured=true;
	}
	
	public static class MouseCursorUpdatedValues{
		public EMouseCursorButton emcb = null;
		public Long lButtonReleasedDelayMilis = null;
		public Vector3f v3fDragDisplacementStep = null;
	}
	
	public void invertButtons(){
		int i = EMouseCursorButton.Action1Click.getIndex();
		
		EMouseCursorButton.Action1Click.setIndex(
			EMouseCursorButton.ContextPropertiesClick.getIndex());
		
		EMouseCursorButton.ContextPropertiesClick.setIndex(i);
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
		
		public static EMouseCursorButton get(int iIndex){ //@STATIC_OK
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
	
	public boolean isMultiClickDelayWithinLimitFrom(Long lFromMilis) {
		return isMultiClickDelayWithinLimit(System.currentTimeMillis()-lFromMilis); //must be realtime as is about detecting user actions/clicks on real world
	}
	public boolean isMultiClickDelayWithinLimit(Long lDelayMilis) {
		return lDelayMilis <= ilvMultiClickMaxDelayMilis.getLong();
	}
	
	public boolean isClickDelayFrom(Long lFromMilis) {
		return isClickDelay(System.currentTimeMillis()-lFromMilis); //must be realtime as is about detecting user actions/clicks on real world
	}
	public boolean isClickDelay(Long lDelayMilis) {
		return lDelayMilis <= ilvClickMaxDelayMilis.getLong();
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(CMD_FIX_RESETING_MOUSE_CURSOR)){
			String strBefore = getButtonsInstance().report();
//			cc.dumpSubEntry(cc.getCommentPrefixStr()+"Before:\n"+strBefore);
			
			getButtonsInstance().resetFixingAllButtonsState();
			
			String strAfter = getButtonsInstance().report();
			String[] aBefore = strBefore.split("\n");
			String[] aAfter = strAfter.split("\n");
			boolean bChanged=false;
			for(int i=0;i<aBefore.length;i++){
				if(!aBefore[i].equals(aAfter[i])){
					cc.dumpSubEntry(cc.getCommentPrefixStr()+"Before: "+aBefore[i]);
					cc.dumpSubEntry(cc.getCommentPrefixStr()+"After:  "+aAfter[i]);
					bChanged = true;
				}
			}
			if(!bChanged)cc.dumpSubEntry(cc.getCommentPrefixStr()+"Nothing changed...");
			
			bCommandWorked=true;
		}else
		if(cc.checkCmdValidity(scfMouseCursorReport)){
			cc.dumpSubEntry(getButtonsInstance().report());
			bCommandWorked=true;
		}else
		{
//			return cc.executePreparedCommandRoot();
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
//	@Override
//	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
//		boolean bCommandWorked = false;
//		
//		if(cc.checkCmdValidity(this,CMD_FIX_RESETING_MOUSE_CURSOR,null)){
//			String strBefore = report();
//			cc.dumpSubEntry(cc.getCommentPrefixStr()+"Before:\n"+report());
//			
//			for(MouseCursorButtonsControl mcab:amcabList){
//				mcab.resetFixingAllButtonsState();
//			}
//			
//			String strAfter = report();
//			String[] aBefore = strBefore.split("\n");
//			String[] aAfter = strAfter.split("\n");
//			boolean bChanged=false;
//			for(int i=0;i<aBefore.length;i++){
//				if(!aBefore[i].equals(aAfter[i])){
//					cc.dumpSubEntry(cc.getCommentPrefixStr()+"Changed: "+aAfter[i]);
//					bChanged = true;
//				}
//			}
//			if(!bChanged)cc.dumpSubEntry(cc.getCommentPrefixStr()+"Nothing changed...");
//			
//			bCommandWorked=true;
//		}else
//			if(cc.checkCmdValidity(this,"mouseCursorReport","")){
//				for(MouseCursorButtonsControl mcab:amcabList){
//					cc.dumpSubEntry(mcab.report());
//				}
//				bCommandWorked=true;
//			}else
//			{
////			return cc.executePreparedCommandRoot();
//				return ECmdReturnStatus.NotFound;
//			}
//		
//		return cc.cmdFoundReturnStatus(bCommandWorked);
//	}

	public boolean isConfigured() {
		return bConfigured;
	}

//	private String report() {
//		String str="";
//		for(MouseCursorButtonsControl m:amcabList){
//			str+=m.report();
//		}
//		return str;
//	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}

	public Vector3f getMouseCursorPositionCopyAsV3f() {
		Vector2f v2f = getMouseCursorPositionCopy();
		return new Vector3f(v2f.x,v2f.y,0);
	}
	public Vector2f getMouseCursorPositionCopy() {
		return GlobalAppRefI.i().getInputManager().getCursorPosition().clone();
	}
	
	public Vector3f getPosWithMouseOnCenter(Vector3f v3fSize){
		Vector3f v3fPos = getMouseCursorPositionCopyAsV3f();
		v3fPos.subtractLocal(MiscJmeI.i().getCenterXYposOf(v3fSize));
		return v3fPos;
	}
	
	@Override
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}

}