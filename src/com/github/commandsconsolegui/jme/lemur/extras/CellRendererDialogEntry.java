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
import java.util.Map.Entry;

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jme.extras.DialogListEntryData;
import com.github.commandsconsolegui.jme.extras.DialogListEntryData.SliderValueData;
import com.github.commandsconsolegui.jme.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogStateAbs;
import com.github.commandsconsolegui.jme.lemur.dialog.LemurDialogStateAbs.SaveLmrDiag;
import com.github.commandsconsolegui.misc.CallQueueI.CallableX;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.github.commandsconsolegui.misc.WorkAroundI;
import com.github.commandsconsolegui.misc.WorkAroundI.BugFixBoolTogglerCmdField;
import com.github.commandsconsolegui.misc.jme.MiscJmeI;
import com.github.commandsconsolegui.misc.jme.lemur.MiscLemurStateI;
import com.jme3.font.LineWrapMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.list.CellRenderer;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class CellRendererDialogEntry<T> implements CellRenderer<DialogListEntryData<T>>, IReflexFillCfg {
//	private static StringVarField svfTreeDepthToken;
//	private static BoolTogglerCmdField	btgShowTreeUId;
	
	private float fCellHeightMult = 1f;
	private String	strStyle;
	private LemurDialogStateAbs<T,?>	diagParent;
//	private boolean	bOptionChoiceMode;
	public String	strLastCellUId="0";
	
	public CellRendererDialogEntry(String strStyle, LemurDialogStateAbs<T,?> diag){//, boolean bOptionChoiceMode) {
		this.strStyle=strStyle;
		this.diagParent=diag;
//		this.bOptionChoiceMode=bOptionChoiceMode;
		
//		/**
//		 * all other can come here too, even ones related to each instance,
//		 * just put them outside the static ones block.
//		 */
//		if(CellRendererDialogEntry.svfTreeDepthToken==null){
//			/**
//			 * Will be linked to the 1st instance of this class, 
//			 * no problem as it will be a global.
//			 */
//			CellRendererDialogEntry.svfTreeDepthToken = new StringVarField(this, " ", null);
//			CellRendererDialogEntry.btgShowTreeUId = new BoolTogglerCmdField(this,false).setCallNothingOnChange();
////			CellRendererDialogEntry.btgHoverHighlight = new BoolTogglerCmdField(this,true).setCallNothingOnChange();
//		}
	}
	
	public static class CellDialogEntry<T> extends Container implements IReflexFillCfg {
		public static enum EUserDataCellEntry{
			colorFgBkp,
//			classCellRef,
			bHoverOverIsWorking,
			;
			public String s(){return this.toString();}
		}
		
		public String	strUniqueId;
		private Button	btnText;
		private Button	btnTree;
		private Slider sliderForValue;
		
//		private Button	btnCfg;
//		private Button btnSelect;
		private DialogListEntryData<T>	dled;
		private CellRendererDialogEntry<T>	assignedCellRenderer;
		private String	strPrefix = "Cell";
//		private String	strColorFgBkpKey = "ColorFgBkp";
		private Container	cntrCustomButtons;
		private Container	cntrBase;

		private SliderValueData	svd;
		
		public LemurDialogStateAbs<T,?> getDialogOwner(){
			return assignedCellRenderer.diagParent;
		}
		
		public DialogListEntryData<T> getDialogListEntryData(){
			return dled;
		}
		
		/**
		 * KEEP here to help on DEBUG!
		 */
		@Override
		public void updateLogicalState(float tpf) {
			/**
			 * ATTENTION!!! 
			 * beware what you put here, can mess things up,
			 * like changing Spatials when they shouldnt?
			 */
			
//			if(sliderForValue!=null){
//				double d = sliderForValue.getModel().getValue();
//				if(!svd.isCurrentValueEqualTo(d))svd.setCurrentValue(d);
//			}
			
			try{
				super.updateLogicalState(tpf);
			}catch(IllegalArgumentException ex){
				//TODO remove this one day, this exception is happening randomly why?
				GlobalCommandsDelegatorI.i().dumpExceptionEntry(ex, this, this.getName(), this.btnText);
			}
		}
		
		/**
		 * the lemur rendered cell will detect the change and update related visible text
		 */
		private void updateVisibleText(){
			if(dled.getTextValue()!=null){
				if(!dled.getTextValue().equals(btnText.getText())){
					updateWithEntry(dled);
				}
			}
		}
		
		public void simpleUpdateThisCell(float tpf){
			if(sliderForValue!=null){
				double d = sliderForValue.getModel().getValue();
				
				if(!svd.isCurrentValueEqualTo(d)){
					svd.setCurrentValue(d);
				}
			}
			
			updateVisibleText();
		}
		
		private void updateTreeButton(){
			String strDepth = "";
			
			String strTreeAction = dled.isParent() ? 
				(dled.isTreeExpanded()?"-":"+") :
				ManageCellRendererI.i().svfTreeDepthToken.getValueAsString();
			
			String strDepthSeparator = "/";
			
			// tree depth
			DialogListEntryData<T> dledParent = dled.getParent();
			while(dledParent!=null){
				if(ManageCellRendererI.i().btgShowTreeUId.b()){
					strDepth = dledParent.getUId()+strDepthSeparator
						+strDepth;
				}else{
					strDepth = ManageCellRendererI.i().svfTreeDepthToken.getValueAsString()
						+strDepth;
				}
				dledParent = dledParent.getParent();
			}
			
			if(ManageCellRendererI.i().btgShowTreeUId.b()){
				strDepth+=dled.getUId();//+":";
			}
			
			if(strDepth.length()!=strDepth.trim().length()){
				strDepth=strDepth.replace(" ", ".");
				strDepth=strDepth.replace("\t", ".");
				if(strDepth.length()!=strDepth.trim().length()){
					throw new PrerequisitesNotMetException("unable to fix indentation", strDepth, this);
				}
			}
			
			String str = strDepth+"["+strTreeAction+"]";
//			if(!strDepth.isEmpty() && strDepth.length()!=strDepth.trim().length()){
//				strDepth = "["+strDepth+"]";
//			}
			
			btnTree.setText(str); //it seems to auto trim the string...
			btnTree.setInsets(new Insets3f(0, 0, 0, 10));
			
			btnTree.setColor((ColorRGBA) btnTree.getUserData(EUserDataCellEntry.colorFgBkp.s()));
			if(!assignedCellRenderer.diagParent.isOptionSelectionMode()){
				if(!dled.isParent())btnTree.setColor(btnTree.getShadowColor());
			}
		}
		
		@SuppressWarnings("unchecked")
		public CellDialogEntry(CellRendererDialogEntry<T> parentCellRenderer, DialogListEntryData<T> dledToSet){
			super(new BorderLayout(), parentCellRenderer.strStyle);
			
			this.setName(strPrefix+"MainContainer");
			
			this.assignedCellRenderer=parentCellRenderer;
			MiscJmeI.i().setUserDataPSH(this, getDialogOwner());
			strUniqueId=assignedCellRenderer.strLastCellUId=MiscI.i().getNextUniqueId(assignedCellRenderer.strLastCellUId);
			
			this.dled=dledToSet;
			
//			cntrBase = (Container)bugFix(0);
//			cntrBase = bugFix(Container.class, this, btgNOTWORKINGBugFixGapForListBoxSelectorArea);
			if(!ManageCellRendererI.i().btgNOTWORKINGBugFixGapForListBoxSelectorArea.isCallerAssigned()){
				ManageCellRendererI.i().btgNOTWORKINGBugFixGapForListBoxSelectorArea.setCallerAssigned(new CallableX(this) {
					@Override
					public Boolean call() {
						/**
						 * param ex.: Geometry geomCursor = MiscI.i().getParamFromArray(Geometry.class, aobjCustomParams, 0);
						 */
						
						Container cntr=null;
			//			if(btgNOTWORKINGBugFixGapForListBoxSelectorArea.b()){ //TODO the fix is not working anymore
							/**
							 * this requires that all childs (in this case buttons) have their style background
							 * color transparent (like alpha 0.5f) or the listbox selector will not be visible below them...
							 */
							// same layout as the cell container
							cntr = new Container(new BorderLayout(), assignedCellRenderer.strStyle);
							cntr.setName(ManageCellRendererI.i().btgNOTWORKINGBugFixGapForListBoxSelectorArea.getSimpleId()); //when mouse is over a cell, if the ListBox->selectorArea has the same world Z value of the button, it may be ordered before the button on the raycast collision results at PickEventSession.setCurrentHitTarget(ViewPort, Spatial, Vector2f, CollisionResult) line: 262	-> PickEventSession.cursorMoved(int, int) line: 482 
							addChild(cntr, Position.Center);
						
						this.setCallerReturnValue(cntr);
						
						return true;
					}
				});
			}
			cntrBase = WorkAroundI.i().bugFix(ManageCellRendererI.i().btgNOTWORKINGBugFixGapForListBoxSelectorArea, Container.class, this);
			
			btnTree = createButton("Tree", "?", cntrBase, Position.West);
			btnTree.addCommands(ButtonAction.Click, ctt);
			btnTree.setUserData(CellDialogEntry.class.getName(), this);
			btnTree.setUserData(EUserDataCellEntry.colorFgBkp.s(), btnTree.getColor());
			
			btnText = createButton("Text", this.dled.getVisibleText(), cntrBase, Position.Center);
			
			cntrCustomButtons = new Container(new SpringGridLayout(), assignedCellRenderer.strStyle);
			cntrBase.addChild(cntrCustomButtons,Position.East);
			
			applyEntryHeight();
			
			updateWithEntry(this.dled);
		}
		
		private void createSlider(Container cntrParent, Object... aobjConstraints) {
			svd = dled.getSliderForValue();
			
			sliderForValue = new Slider(
				new DefaultRangedValueModel(svd.getMinValue(),svd.getMaxValue(),svd.getCurrentValue()),
				Axis.X,
				//TODO complete the style properly: new ElementId(DialogStyleElementId.SliderForValueChange.s()), 
				assignedCellRenderer.strStyle);
			
			cntrParent.addChild(sliderForValue,aobjConstraints);
		}

		private void applyEntryHeight(){
			MiscLemurStateI.i().setSizeSafely(this, 
					-1f, 
					getPreferredSize().getY() * assignedCellRenderer.getCellHeightMult(),
					true);
		}
		
		static class CommandTreeToggle implements Command<Button>{
			@Override
			public void execute(Button source) {
				@SuppressWarnings("rawtypes")
				CellDialogEntry cell = (CellDialogEntry)source.getUserData(CellDialogEntry.class.getName());
				
				if(cell.dled.isParent()){
					cell.dled.toggleExpanded();
//					cell.assignedCellRenderer.diagParent.requestRefreshList();
				}
				cell.updateTreeButton();
			}
		}
		CommandTreeToggle ctt = new CommandTreeToggle();
		
//		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void updateWithEntry(DialogListEntryData<T> dled){
			DialogListEntryData<T> dledOld = this.dled;
			this.dled=dled;
			
			// tree button
			updateTreeButton();
			
			// entry text
			btnText.setText(dled.getVisibleText());
			
//			// optional slider
//			if(sliderForValue!=null){
//				svd.setCurrentValue(sliderForValue.getModel().getValue());
//			}
			
			// custom buttons
			boolean bButtonsChanged = false;
			
			// check if the entry data changed
			if(this.dled.equals(dledOld)){
				bButtonsChanged=true;
			}
			
			// check if buttons key and value changed
			if(!bButtonsChanged){
				ArrayList<Entry<String, T>> aenActionsListOld = dledOld.getCustomButtonsActionsListCopy();
				ArrayList<Entry<String, T>> aenActionsList = this.dled.getCustomButtonsActionsListCopy();
				if(aenActionsListOld.size()==aenActionsList.size()){
					for(int i=0;i<aenActionsList.size();i++){
						Entry<String,T> enOld = aenActionsListOld.get(i);
						Entry<String,T> en = aenActionsList.get(i);
						if(
							!en.getKey().equals(enOld.getKey()) ||
							!en.getValue().equals(enOld.getValue())
						){
							bButtonsChanged = true;
							break;
						}
					}				
				}else{
					bButtonsChanged=true;
				}
			}
			
			// apply changed (recreate buttons list)
			if(bButtonsChanged){
				cntrCustomButtons.clearChildren();
				
				ArrayList<Entry<String,T>> aenList = this.dled.getCustomButtonsActionsListCopy();
				int iLastIndex = aenList.size()-1;
				
				int iExtra=0;
				if(dled.getSliderForValue()!=null){
					iExtra++;
					createSlider(cntrCustomButtons, iLastIndex+iExtra);
				}
				
				for(int i=iLastIndex; i>=0; i--){
					Entry<String, T> entry = aenList.get(i);
					Button btn = createButton(entry.getKey(), "["+entry.getKey()+"]", cntrCustomButtons, i);
					btn.addCommands(ButtonAction.Click, (Command<Button>)entry.getValue());
				}
			}
		}
		
		/**
		 * 
		 * @param strId if null, will be the label 
		 * @param strLabel
		 * @param p
		 * @return
		 */
		private Button createButton(String strId, String strLabel, Container cntrParent, Object... aobjConstraints){
			if(strId==null)strId=strLabel;
			Button btn = new Button(strLabel,assignedCellRenderer.strStyle);
			MiscJmeI.i().retrieveBitmapTextFor(btn).setLineWrapMode(LineWrapMode.NoWrap);
			btn.setName(strPrefix+"Button"+strId);
//			btn.setUserData(EUserDataCellEntry.classCellRef.s(),this);
			MiscJmeI.i().setUserDataPSH(btn,this);
//			btn.setUserData(dled.getClass().getName(), dled);
			MiscJmeI.i().setUserDataPSH(btn,dled);
//			btn.setUserData(assignedCellRenderer.diagParent.getClass().getName(),assignedCellRenderer.diagParent);
			MiscJmeI.i().setUserDataPSH(btn,assignedCellRenderer.diagParent);
			CursorEventControl.addListenersToSpatial(btn, DialogMouseCursorListenerI.i());
			
			cntrParent.addChild(btn,aobjConstraints);
			
			DialogMouseCursorListenerI.i().addMouseCursorHighlightEffects(btn);
			
			return btn;
		}

//		public boolean isCfgButton(Spatial spt) {
//			return spt==btnCfg;
//		}
		
		public boolean isTextButton(Spatial spt){
			return spt==btnText;
		}

//		@Override
//		public Object bugFix(Object... aobj) {
//			return null;
//		}
		
//		@Override
//		public <BFR> BFR bugFix(Class<BFR> clReturnType, BFR objRetIfBugFixBoolDisabled, BoolTogglerCmdField btgBugFixId, Object... aobjCustomParams) {
//			if(!btgBugFixId.b())return objRetIfBugFixBoolDisabled;
//			
//			Object objRet = null;
//			boolean bFixed = false;
//			
//			if(btgNOTWORKINGBugFixGapForListBoxSelectorArea.isEqualToAndEnabled(btgBugFixId)){
//				/**
//				 * param ex.: Geometry geomCursor = MiscI.i().getParamFromArray(Geometry.class, aobjCustomParams, 0);
//				 */
//				
////				MiscI.i().assertSameClass(Container.class,clReturnType);
//				Container cntr=null;
////				if(btgNOTWORKINGBugFixGapForListBoxSelectorArea.b()){ //TODO the fix is not working anymore
//					/**
//					 * this requires that all childs (in this case buttons) have their style background
//					 * color transparent (like alpha 0.5f) or the listbox selector will not be visible below them...
//					 */
//					// same layout as the cell container
//					cntr = new Container(new BorderLayout(), assignedCellRenderer.strStyle);
////					Vector3f v3fSize = new Vector3f(this.getPreferredSize());
////					v3fSize.z=LemurMiscHelpersStateI.fPreferredThickness*2f;
////					LemurMiscHelpersStateI.i().setGrantedSize(cntr, v3fSize, true);
//					cntr.setName(btgNOTWORKINGBugFixGapForListBoxSelectorArea.getSimpleId()); //when mouse is over a cell, if the ListBox->selectorArea has the same world Z value of the button, it may be ordered before the button on the raycast collision results at PickEventSession.setCurrentHitTarget(ViewPort, Spatial, Vector2f, CollisionResult) line: 262	-> PickEventSession.cursorMoved(int, int) line: 482 
//					addChild(cntr, Position.Center);
////				}else{
////					cntr = this;
////				}
//				
//				bFixed=true;
//				
//				objRet=cntr;
//			}
//			
//			return MiscI.i().bugFixRet(clReturnType,bFixed, objRet, aobjCustomParams);
//		}

		@Override
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
			return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
		}
		
//		public void setOverrideBackgroundColorNegatingCurrent() {
//			overrideBackgroundColor(null,false,true);
//		}
//		public void resetOverrideBackgroundColor(){
//			overrideBackgroundColor(null,true,false);
//		}
//		public void setOverrideBackgroundColor(ColorRGBA colorApply) {
//			overrideBackgroundColor(colorApply,false,false);
//		}
//		/**
//		 * 
//		 * @param colorOverride if null, will reset (restore current normal bkg color)
//		 * @param bNegateCurrentColor overrides color param (least if it is null)
//		 */
//		private void overrideBackgroundColor(ColorRGBA colorOverride, boolean bResetToBackup, boolean bNegateCurrentColor) {
//			if(!btgHoverHighlight.b())return;
//			
//			GuiComponent gcBkg = getBackground();
//			if(gcBkg==null){
//				GlobalCommandsDelegatorI.i().dumpDevWarnEntry("background is null", this);
//				return;
//			}
//			
//			QuadBackgroundComponent qbc = (QuadBackgroundComponent)
//					gcBkg.getGuiControl().getComponent("background");
//			
//			ColorRGBA colorBkp=getUserData("BkgColorBkp");
//			if(colorBkp==null){
//				colorBkp = qbc.getColor();
//				setUserData("BkgColorBkp", colorBkp);
//			}
//			
//			if(bResetToBackup){
//				qbc.setColor(colorBkp);
//				setUserData("BkgColorBkp", null); //clear to not leave useless value there
//			}else{
//				if(bNegateCurrentColor){
//					colorOverride = MiscJmeI.i().negateColor(colorBkp);
//				}else{
//					if(colorOverride==null)throw new PrerequisitesNotMetException("invalid null color override", this);
//				}
//				
//				qbc.setColor(colorOverride);
//			}
//			
////			if(colorApply!=null){
////				if(!qbc.getColor().equals(colorApply)){
////					
////				}
////				
////				qbc.setColor(colorApply);
////			}else{
////				if(colorBkp!=null){
////					qbc.setColor(colorBkp);
////					setUserData("BkgColorBkp", null); //clear to not leave useless value there
////				}
////			}
//			
//		}
		@Override
		public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
			return fld.get(this);
		}
		@Override
		public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
			fld.set(this,value);
		}

		@Override
		public String getUniqueId() {
			return strUniqueId;
		}

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Panel getView(DialogListEntryData<T> dled, boolean selected, Panel existing) {
    if( existing == null ) {
      existing = new CellDialogEntry<T>(this,dled);
	  } else {
      ((CellDialogEntry<T>)existing).updateWithEntry(dled);
	  }
	  return existing;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}

	public float getCellHeightMult() {
		return fCellHeightMult;
	}

	public void setCellHeightMult(float fCellHeightMult) {
		this.fCellHeightMult = fCellHeightMult;
	}

	@Override
	public Object getFieldValue(Field fld) throws IllegalArgumentException, IllegalAccessException {
		return fld.get(this);
	}
	@Override
	public void setFieldValue(Field fld, Object value) throws IllegalArgumentException, IllegalAccessException {
		fld.set(this,value);
	}
	
	@Deprecated
	@Override
	public String getUniqueId() {
		throw new UnsupportedOperationException("is there a point on implementing this?");
	}
	
}
