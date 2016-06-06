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

package com.github.commandsconsolegui.jmegui.lemur.console;

import java.util.ArrayList;
import java.util.Collections;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.jmegui.ConditionalStateAbs;
import com.github.commandsconsolegui.jmegui.ConditionalStateAbs.ICfgParm;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.cmd.CmdConditionalStateAbs;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.focus.FocusChangeEvent;
import com.simsilica.lemur.focus.FocusChangeListener;
import com.simsilica.lemur.focus.FocusManagerState;
import com.simsilica.lemur.focus.FocusTarget;

/**
 *
 * @author AquariusPower <https://github.com/AquariusPower>
 * 
 */
public class LemurFocusHelperStateI extends CmdConditionalStateAbs implements FocusChangeListener {
	private static LemurFocusHelperStateI	instance=new LemurFocusHelperStateI();
	public static LemurFocusHelperStateI i(){return instance;}
	
	ArrayList<FocusTarget> aftZOrderList = new ArrayList<FocusTarget>();
	ArrayList<Spatial> asptFocusRequestList = new ArrayList<Spatial>();
	private FocusManagerState focusState;
//	private SimpleApplication	sapp;
//	private CommandsDelegatorI	cc;
	private Float	fBaseZ = 0f;
	
//	public void configure(){
//		this.sapp=GlobalSappRefI.i().get();
//		this.cc=GlobalCommandsDelegatorI.i().get();
//		
//		cc.addConsoleCommandListener(this);
//	}
	
	public static class CfgParm implements ICfgParm{
		Float fBaseZ;
		public CfgParm(Float fBaseZ) {
			super();
			this.fBaseZ = fBaseZ;
		}
	}
	/**
	 * The initial Z value from where the dialogs will be sorted/ordered.
	 * So if you have other gui elements, this can be changed to show dialogs above or under them.
	 * @return 
	 * @return 
	 */
	@Override
	public LemurFocusHelperStateI configure(ICfgParm icfg) {
//	public void configure(Float fBaseZ){
		CfgParm cfg = (CfgParm)icfg;
		if(cfg.fBaseZ!=null)this.fBaseZ = cfg.fBaseZ;
		super.configure(new CmdConditionalStateAbs.CfgParm(
			LemurFocusHelperStateI.class.getSimpleName(), false));
//		configure();
		return storeCfgAndReturnSelf(icfg);
	}
	
	public ArrayList<String> debugReport(){
		ArrayList<String> astr=new ArrayList<String>();
		
		for(int i=asptFocusRequestList.size()-1;i>=0;i--){
			Spatial spt = asptFocusRequestList.get(i);
			int iPriority = asptFocusRequestList.size()-i-1;
			astr.add("FocusPriority("+iPriority+"):"+spt.getName());
		}
		
		ArrayList<FocusTarget> aftRev = new ArrayList<FocusTarget>(aftZOrderList);
		Collections.reverse(aftRev);
		for(FocusTarget ft:aftRev){
			GuiControl gct = (GuiControl)ft;
			Spatial spt = MiscJmeI.i().getParentestFrom(gct.getSpatial());
			astr.add("ZOrder:"+spt.getName()+",Z="+spt.getLocalTranslation().z);
		}
		
		return astr;
	}
	
	public Spatial getFocused(){
		return getFocusManagerState().getFocus();
	}
	
	/**
	 * keep private for this focus helper to work properly 
	 * @return
	 */
	private FocusManagerState getFocusManagerState(){
		if(focusState==null)focusState = app().getStateManager().getState(FocusManagerState.class);
		return focusState;
	}
	
	/**
	 * will also re-apply focus to the newest one
	 * @param spt
	 */
	public void removeFocusableFromList(Spatial spt) {
		asptFocusRequestList.remove(spt);
		if(asptFocusRequestList.size()>0){
			requestFocus(asptFocusRequestList.get(asptFocusRequestList.size()-1));
		}else{
			removeAllFocus();
		}
	}
	
	public void removeAllFocus(){
		asptFocusRequestList.clear();
		GuiGlobals.getInstance().requestFocus(null);
	}
	
	public void requestFocus(Spatial spt) {
		if(spt==null)throw new NullPointerException("invalid null focusable");
		
		asptFocusRequestList.remove(spt); //to update the priority
		asptFocusRequestList.add(spt);
		
		if(spt instanceof TextField){
			LemurMiscHelpersStateI.i().setTextFieldInputToBlinkCursor((TextField) spt);
		}
		
		GuiGlobals.getInstance().requestFocus(spt);
	}
	
	public boolean isFocusRequesterListEmpty(){
		return asptFocusRequestList.size()==0;
	}
	
	public Spatial getCurrentFocusRequester(){
		if(asptFocusRequestList.size()==0)return null;
		return asptFocusRequestList.get(asptFocusRequestList.size()-1);
	}
	
	private void zSortLatest(FocusTarget ftLatest, boolean bJustRemove){
		aftZOrderList.remove(ftLatest); //remove to update priority
		if(!bJustRemove)aftZOrderList.add(ftLatest);
		
		/**
		 * fix list from any inconsistency
		 */
		for(FocusTarget ft:new ArrayList<FocusTarget>(aftZOrderList)){
			GuiControl gct = (GuiControl)ft;
			if(MiscJmeI.i().getParentestFrom(gct.getSpatial()).getParent()==null){
				aftZOrderList.remove(ft);
			}
		}
		
		/**
		 * organize in Z
		 */
		float fDisplacement=0.1f;
		for(int i=aftZOrderList.size()-1;i>=0;i--){
			FocusTarget ft = aftZOrderList.get(i);
//			if(ftSorting instanceof GuiControl){
				float fZ = fBaseZ + (i*fDisplacement)+fDisplacement; //so will always be above all other GUI elements that are expectedly at 0
				
				GuiControl gct = (GuiControl)ft;
				Spatial spt = MiscJmeI.i().getParentestFrom(gct.getSpatial());
				
				/**
				 * must be re-added to work...
				 */
				Node nodeParent = spt.getParent();
				spt.removeFromParent();
				spt.getLocalTranslation().z=fZ;
				nodeParent.attachChild(spt);
//				parentestApplyZ(gct.getSpatial(), fZ);
//				gct.getSpatial().getLocalTranslation().z = (i*0.1f)+0.1f; //so will always be above all other GUI elements that are expectedly at 0
//			}
		}
	}
	
//	public void parentestApplyZ(Spatial spt, float fZ){
//		Spatial sptParentest = spt;
//		
//		while(true){
//			if(sptParentest.getParent()==null)return;
//			if(sapp.getGuiNode().equals(sptParentest.getParent()))break;
//			sptParentest=sptParentest.getParent();
//		}
//		
//		sptParentest.getLocalTranslation().z=fZ;
//	}
	
	@Override
	public void focusGained(FocusChangeEvent event) {
		zSortLatest(event.getSource(),false);
	}
	
	@Override
	public void focusLost(FocusChangeEvent event) {
//		zSortLatest(event.getSource(),true);
	}
	
	public void addFocusChangeListener(Spatial sptTarget) {
		sptTarget.getControl(GuiControl.class).addFocusChangeListener(this);
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI cc) {
		boolean bCmdEndedGracefully = false;
		
		if(cc.checkCmdValidity(this,"debugFocusReport","")){
			for(String str:debugReport())cc.dumpSubEntry(str);
			bCmdEndedGracefully = true;
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCmdEndedGracefully);
	}
	
	@Override
	protected boolean initCheckPrerequisites() {
		if(GuiGlobals.getInstance()==null)return false;
		return super.initCheckPrerequisites();
	}
	
	@Override
	protected boolean updateOrUndo(float tpf) {
		Spatial spt = getCurrentFocusRequester();
		GuiGlobals.getInstance().requestFocus(spt); //TODO timed delay?
		
		GuiGlobals.getInstance().setCursorEventsEnabled(
			!isFocusRequesterListEmpty());
		
		return super.updateOrUndo(tpf);
	}

}
