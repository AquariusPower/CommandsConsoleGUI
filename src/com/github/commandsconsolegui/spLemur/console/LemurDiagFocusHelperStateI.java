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

package com.github.commandsconsolegui.spLemur.console;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI;
import com.github.commandsconsolegui.spAppOs.misc.CompositeControlAbs;
import com.github.commandsconsolegui.spAppOs.misc.MsgI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.ManageCallQueueI.CallableX;
import com.github.commandsconsolegui.spCmd.CommandsDelegator;
import com.github.commandsconsolegui.spCmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.spCmd.varfield.FloatDoubleVarField;
import com.github.commandsconsolegui.spJme.DialogStateAbs;
import com.github.commandsconsolegui.spJme.cmd.CmdConditionalStateAbs;
import com.github.commandsconsolegui.spJme.globals.GlobalOSAppJmeI;
import com.github.commandsconsolegui.spJme.misc.MiscJmeI;
import com.github.commandsconsolegui.spLemur.MouseCursorListenerAbs;
import com.github.commandsconsolegui.spLemur.dialog.LemurDialogStateAbs;
import com.github.commandsconsolegui.spLemur.misc.MiscLemurStateI;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.focus.FocusChangeEvent;
import com.simsilica.lemur.focus.FocusChangeListener;
import com.simsilica.lemur.focus.FocusManagerState;
import com.simsilica.lemur.focus.FocusTarget;

/**
 *
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 */
public class LemurDiagFocusHelperStateI extends CmdConditionalStateAbs<LemurDiagFocusHelperStateI> implements FocusChangeListener {
	public static final class CompositeControl extends CompositeControlAbs<LemurDiagFocusHelperStateI>{
		private CompositeControl(LemurDiagFocusHelperStateI casm){super(casm);}; }
	private CompositeControl ccSelf = new CompositeControl(this);
	
	private static LemurDiagFocusHelperStateI	instance=new LemurDiagFocusHelperStateI();
	public static LemurDiagFocusHelperStateI i(){return instance;}
	
	ArrayList<FocusTarget> aftZOrderList = new ArrayList<FocusTarget>();
	ArrayList<Spatial> asptFocusRequestList = new ArrayList<Spatial>();
//	TreeMap<LemurDialogGUIStateAbs,Panel> tmFocusRequestList = new TreeMap<LemurDialogGUIStateAbs, Panel>(new Comparator<T>() {
//	});
	private FocusManagerState focusState;
//	private SimpleApplication	sapp;
//	private CommandsDelegatorI	cc;
	private final FloatDoubleVarField fdvDialogBazeZ = new FloatDoubleVarField(this, 20f, "the starting point at Z axis to place dialogs");
	private final FloatDoubleVarField fdvDialogZDisplacement = new FloatDoubleVarField(this, 10f, "the displacement between dialogs in the Z axis, to let one be shown properly above another.");
	
	public float getDialogZDisplacement(){
		return fdvDialogZDisplacement.getFloat();
	}
	
//	public void configure(){
//		this.sapp=GlobalSappRefI.i().get();
//		this.cc=GlobalCommandsDelegatorI.i().get();
//		
//		cc.addConsoleCommandListener(this);
//	}
	
	public LemurDiagFocusHelperStateI() {
		setPrefixCmdWithIdToo(true);
	}
	
	public static class CfgParm extends CmdConditionalStateAbs.CfgParm{
		Float fBaseZ;
		public CfgParm(Float fBaseZ) {
			super(null);
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
	public LemurDiagFocusHelperStateI configure(ICfgParm icfg) {
//	public void configure(Float fBaseZ){
		CfgParm cfg = (CfgParm)icfg;
		if(cfg.fBaseZ!=null)this.fdvDialogBazeZ.setObjectRawValue(cfg.fBaseZ);
//		super.configure(new CmdConditionalStateAbs.CfgParm(LemurFocusHelperStateI.class.getSimpleName()));
		super.configure(icfg);
//		configure();
		
//		MouseCursor.i().configure(null);
		
//		return storeCfgAndReturnSelf(cfg);
		return getThis();
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
	
	public boolean isDialogFocusedFor(Spatial spt){
		if(spt.equals(getFocused()))return true; //quick test 1st
		if(getFocused()==null)return false;
		
		Spatial sptParentest = MiscJmeI.i().getParentestFrom(spt);
		Spatial sptFocusedParentest = MiscJmeI.i().getParentestFrom(getFocused());
		return sptParentest.equals(sptFocusedParentest);
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
		assertIsNotAtRenderingNode(spt);
		asptFocusRequestList.remove(spt);
		if(asptFocusRequestList.size()>0){
//			requestFocus(asptFocusRequestList.get(asptFocusRequestList.size()-1));
			requestFocus(getCurrentFocusRequester());
		}else{
			removeAllFocus(); //this will also clear the list, np tho, already empty..
		}
	}
	
	public void assertIsAtRenderingNode(Spatial spt) {
		assertAtRenderingNode(true,spt);
	}
	public void assertIsNotAtRenderingNode(Spatial spt) {
		assertAtRenderingNode(false,spt);
	}
	private void assertAtRenderingNode(boolean bIs, Spatial spt){
//		Node parent = MiscJmeI.i().getParentestFrom(spt).getParent();
//		boolean bIsAtGuiNode = GlobalGUINodeI.i().equals(parent);
		boolean b = MiscJmeI.i().isGoingToBeRenderedNow(spt);
		if(bIs){
			if(!b){
				throw new PrerequisitesNotMetException("it is NOT at GUI node "+spt.getName());
			}
		}else{
			if(b){
				throw new PrerequisitesNotMetException("it is STILL at GUI node "+spt.getName());
			}
		}
	}
	
	public void removeAllFocus(){
		for(Spatial spt:asptFocusRequestList)assertIsNotAtRenderingNode(spt);
		asptFocusRequestList.clear();
		GuiGlobals.getInstance().requestFocus(null);
	}

	static class ClickFocusMouseCursorListener extends MouseCursorListenerAbs{
		@Override
		public void cursorButtonEvent(CursorButtonEvent event, Spatial target, Spatial capture) {
			if(event.isPressed()){ //do not consume the event
				LemurDiagFocusHelperStateI.i().requestDialogFocus(capture);
			}
			
			super.cursorButtonEvent(event, target, capture);
		}
	}
	ClickFocusMouseCursorListener focusMouseCursorListener = new ClickFocusMouseCursorListener();
	
	public void prepareDialogToBeFocused(LemurDialogStateAbs diag){
		MiscJmeI.i().setUserDataPSH(diag.getInputFieldForManagement(ccSelf), diag);
//		diag.getContainerMain().setUserData(LemurDialogGUIStateAbs.class.getName(), diag);
		
		CursorEventControl.addListenersToSpatial(diag.getDialogMainContainer(), 
			focusMouseCursorListener);
	}
	
	/**
	 * The parentest spatial (or the current) must contain a user data reference to it's dialog owner.
	 * @param sptAny
	 * @return
	 */
	public LemurDialogStateAbs retrieveDialogFromSpatial(Spatial sptAny){
		Spatial sptParentest = MiscJmeI.i().getParentestFrom(sptAny);
		LemurDialogStateAbs diag = MiscJmeI.i().getUserDataPSH(sptParentest, LemurDialogStateAbs.class);
//		LemurDialogGUIStateAbs diag = (LemurDialogGUIStateAbs)MiscJmeI.i().getParentestFrom(sptAny)
//				.getUserData(LemurDialogGUIStateAbs.class.getName());
		
//		if(diag==null){
//			if (sptParentest instanceof CellDialogEntry) {
//				CellDialogEntry cde = (CellDialogEntry) sptParentest;
//				diag = cde.getDialogOwner();
//			}
//		}
		
		if(diag==null)throw new PrerequisitesNotMetException("no dialog at: "+sptAny.getName());
		
		return diag;
	}
	
	public boolean lowerDialogFocusPriority(Spatial sptAny){
		LemurDialogStateAbs diag = retrieveDialogFromSpatial(sptAny);
		Spatial sptFocusable = diag.getInputFieldForManagement(ccSelf);
		
		if(asptFocusRequestList.contains(sptFocusable)){
			asptFocusRequestList.remove(sptFocusable);
			asptFocusRequestList.add(0,sptFocusable);
			requestDialogFocus(getCurrentFocusRequester());
			return true;
		}else{
			cd().dumpDevWarnEntry("spatial not at focus stack "+sptAny.getName());
		}
		
		return false;
	}
	
	public void requestDialogFocus(Spatial sptChild) {
		LemurDialogStateAbs diag = retrieveDialogFromSpatial(sptChild);
		if(!diag.isLayoutValid()){
			MsgI.i().devWarn("diag layout is invalid", diag, this);
			return; //TODO return false? or queue this request?
		}
		
		LemurDialogStateAbs diagFocus = diag;
		for(Object objDiagModal:diag.getModalChildListCopy()){
			LemurDialogStateAbs diagModal = (LemurDialogStateAbs) objDiagModal;
			if(diagModal.isEnabled()){ //there can only have on modal enabled at a time TODO right?
				diagFocus = diagModal;
				break; 
			}
		}
		
		requestFocus(diagFocus.getInputFieldForManagement(ccSelf),true);
	}
	
	/**
	 * @param spt
	 */
	public void requestFocus(Spatial spt) {
		requestFocus(spt, false);
	}
	
	/**
	 * We can have more than one input field in the same dialog, therefore
	 * the focus request must be to the field.
	 * 
	 * @param spt
	 * @param bOnlyIfAlreadyOnTheList if false, will be added to the list
	 */
	public void requestFocus(final Spatial spt, final boolean bOnlyIfAlreadyOnTheList) {
		if(spt==null)throw new NullPointerException("invalid null focusable");
		
		if(bOnlyIfAlreadyOnTheList){
			if(!asptFocusRequestList.contains(spt)){
//				GlobalCommandsDelegatorI.i().dumpDevWarnEntry("not on the focus list", spt);
				return;
			}
		}
		
		LemurDialogStateAbs diag = retrieveDialogFromSpatial(spt);
		if(!diag.isLayoutValid()){
			ManageCallQueueI.i().addCall(new CallableX(this,1000) {
				@Override
				public Boolean call() {
					requestFocus(spt,bOnlyIfAlreadyOnTheList);
					return true;
				}
			});
			
			return;
		}
		
		ArrayList<LemurDialogStateAbs> adiag = new ArrayList<LemurDialogStateAbs>();
//		if (diag instanceof LemurDialogStateAbs) {
//			LemurDialogStateAbs diagLemur = (LemurDialogStateAbs) diag;
//		adiag.addAll(diagLemur.getParentsDialogList());
			adiag.addAll(diag.getParentsDialogList());
//		}
		adiag.add(diag);
		
		/**
		 * bring all parents up when focusing a modal child
		 */
		for(LemurDialogStateAbs diagWork:adiag){
			if(!diagWork.isRestartRequested()){
				Spatial sptDeNest = diagWork.getInputFieldForManagement(ccSelf);
				
				if(diagWork.isLayoutValid())assertIsAtRenderingNode(sptDeNest);
				/**
				 * re-add to update the priority to top priority
				 */
				asptFocusRequestList.remove(sptDeNest);
				asptFocusRequestList.add(sptDeNest); 
				
				if(sptDeNest instanceof TextField){
					zSortLatest(((TextField)sptDeNest).getControl(GuiControl.class), false);
				}
			}
		}
		
		if(spt instanceof TextField){
			MiscLemurStateI.i().setTextFieldInputToBlinkCursor((TextField) spt);
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
		aftZOrderList.remove(ftLatest); //remove just to update priority
		if(!bJustRemove)aftZOrderList.add(ftLatest);
		
//		if(ftLatest instanceof TextEntryComponent){
//			TextEntryComponent tec = (TextEntryComponent)ftLatest;
//		if(ftLatest instanceof GuiControl){
//			GuiControl gc = (GuiControl)ftLatest;
//			gc.getNode();
//		}
		
		/**
		 * fix list from any inconsistency
		 */
		for(FocusTarget ft:new ArrayList<FocusTarget>(aftZOrderList)){
			GuiControl gc = (GuiControl)ft;
			// remove ones that are attached to nothing
			//TODO check concerning the workaround to invalid layouts? where it may have no parent but the dialog is still there (just invalid but active) 
			if(MiscJmeI.i().getParentestFrom(gc.getSpatial()).getParent()==null){
				aftZOrderList.remove(ft);
			}
		}
		
		/**
		 * organize in Z
		 */
		for(int i=aftZOrderList.size()-1;i>=0;i--){
			FocusTarget ft = aftZOrderList.get(i);
//			if(ftSorting instanceof GuiControl){
			
				/**
				 * so will always be above all other GUI elements that are expectedly at 0
				 */
				float fZ = fdvDialogBazeZ.getFloat() 
					+ (i*fdvDialogZDisplacement.floatValue())
					+ fdvDialogZDisplacement.floatValue(); 
				
				GuiControl gcTarget = (GuiControl)ft;
				Spatial spt = MiscJmeI.i().getParentestFrom(gcTarget.getSpatial());
				
				DialogStateAbs diag = MiscJmeI.i().getUserDataPSH(spt, DialogStateAbs.class.getName());
//				BaseDialogStateAbs diag = (BaseDialogStateAbs)spt.getUserData(BaseDialogStateAbs.class.getName());
				if(diag!=null){
					if(ft==ftLatest){
						diag.focusGained();
					}else{
						diag.focusLost();
					}
				}
				
//				WorkAroundI.i().bugFix(btgBugFixZSortApply, spt, fZ);
				applyDialogZOrder(spt, fZ);
				
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
	public ECmdReturnStatus execConsoleCommand(CommandsDelegator cc) {
		boolean bCmdEndedGracefully = false;
		
		if(cc.checkCmdValidity(this,"debugFocusSortZOrderReport",null,"")){
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
	protected boolean updateAttempt(float tpf) {
		if(GlobalOSAppJmeI.i().isShowingAlert(false)){
			//TODO this does anything? has no text input element on it ...
			GuiGlobals.getInstance().requestFocus(GlobalOSAppJmeI.i().getAlertSpatial());
		}else{
			Spatial spt = getCurrentFocusRequester();
			GuiGlobals.getInstance().requestFocus(spt); //TODO timed delay?
		}
		
		GuiGlobals.getInstance().setCursorEventsEnabled(!isFocusRequesterListEmpty());
		
		return super.updateAttempt(tpf);
	}
	
	/**
	 * Lemur only controls the Z order for child elements.
	 * Each dialog is a Top (parentest) element. 
	 * There is no Lemur auto Z positioning for top elements.
	 * The top elements are all at 0.0f (TODO confirm this).
	 * These dialogs managing exists only on this project. 
	 * @param spt
	 * @param fZ
	 * @return
	 */
	private boolean applyDialogZOrder(Spatial spt, Float fZ){
		if(!isEnabled())return true; //skipper
		
		/**
		 * must be re-added to work...
		 */
		Node nodeParent = spt.getParent();
		if(nodeParent==null){
			GlobalCommandsDelegatorI.i().dumpDevWarnEntry("parent shouldnt be null...", spt, fZ, this);
			return false;
		}
		
		spt.removeFromParent();
		spt.getLocalTranslation().z=fZ;
		nodeParent.attachChild(spt);
		
		return true;
	}
	
//	private final BugFixBoolTogglerCmdField btgBugFixZSortApply = new BugFixBoolTogglerCmdField(this,false)
//		.setCallerAssigned(new CallableX(this) {
//			@Override
//			public Boolean call() {
//				if(!isEnabled())return true; //skipper
//				
//				Spatial spt = this.getParamsForMaintenance().getParam(Spatial.class, 0);
//				Float fZ = this.getParamsForMaintenance().getParam(Float.class, 1);
//				
//				/**
//				 * must be re-added to work...
//				 */
//				Node nodeParent = spt.getParent();
//				if(nodeParent==null){
//					GlobalCommandsDelegatorI.i().dumpDevWarnEntry("parent shouldnt be null...", spt, this, btgBugFixZSortApply);
//					return false;
//				}
//				
//				spt.removeFromParent();
//				spt.getLocalTranslation().z=fZ;
//				nodeParent.attachChild(spt);
//				
//				return true;
//			}
//		});
	
//	@Override
//	public <BFR> BFR bugFix(Class<BFR> clReturnType, BFR objRetIfBugFixBoolDisabled, BoolTogglerCmdField btgBugFixId, Object... aobjCustomParams) {
//		if(!btgBugFixId.b())return objRetIfBugFixBoolDisabled;
//		
//		boolean bFixed = false;
//		Object objRet = null;
//		
//		if(btgBugFixZSortApply.isEqualToAndEnabled(btgBugFixId)){
//			Spatial spt = MiscI.i().getParamFromArray(Spatial.class, aobjCustomParams, 0);
//			Float fZ = MiscI.i().getParamFromArray(Float.class, aobjCustomParams, 1);
//			
//			/**
//			 * must be re-added to work...
//			 */
//			Node nodeParent = spt.getParent();
//			spt.removeFromParent();
//			spt.getLocalTranslation().z=fZ;
//			nodeParent.attachChild(spt);
//			
//			bFixed=true;
//		}
//		
//		return MiscI.i().bugFixRet(clReturnType,bFixed, objRet, aobjCustomParams);
//	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurDiagFocusHelperStateI.class)return super.getFieldValue(fld);
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		if(fld.getDeclaringClass()!=LemurDiagFocusHelperStateI.class){super.setFieldValue(fld,value);return;}
		fld.set(this,value);
	}

	@Override
	protected LemurDiagFocusHelperStateI getThis() {
		return this;
	}
}
