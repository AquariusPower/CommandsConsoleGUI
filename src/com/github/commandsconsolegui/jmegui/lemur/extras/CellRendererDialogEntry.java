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

package com.github.commandsconsolegui.jmegui.lemur.extras;

import java.util.Map.Entry;

import com.github.commandsconsolegui.cmd.CommandsDelegator;
import com.github.commandsconsolegui.cmd.CommandsDelegator.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.misc.IWorkAroundBugFix;
import com.github.commandsconsolegui.misc.MiscI;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.font.LineWrapMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.list.CellRenderer;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CellRendererDialogEntry<T> implements CellRenderer<DialogListEntryData<T>>, IReflexFillCfg {
	protected static StringVarField svfTreeDepthToken;
	protected static BoolTogglerCmdField	btgShowTreeUId;
	
	protected String	strStyle;
	protected LemurDialogGUIStateAbs<T>	diagParent;
//	private boolean	bOptionChoiceMode;
	
	public CellRendererDialogEntry(String strStyle, LemurDialogGUIStateAbs<T> diag){//, boolean bOptionChoiceMode) {
		this.strStyle=strStyle;
		this.diagParent=diag;
//		this.bOptionChoiceMode=bOptionChoiceMode;
		
		/**
		 * all other can come here too, even ones related to each instance,
		 * just put them outside the static ones block.
		 */
		if(CellRendererDialogEntry.svfTreeDepthToken==null){
			/**
			 * Will be linked to the 1st instance of this class, 
			 * no problem as it will be a global.
			 */
			CellRendererDialogEntry.svfTreeDepthToken = new StringVarField(this, " ", null);
			CellRendererDialogEntry.btgShowTreeUId = new BoolTogglerCmdField(this,false).setCallNothingOnChange();
		}
	}
	
	public static class Cell<T> extends Container implements IWorkAroundBugFix, IReflexFillCfg {
		public static enum EUserData{
			colorFgBkp,
			cellClassRef,
			;
		}
		
		protected static BoolTogglerCmdField btgBugFixGapForListBoxSelectorArea;
		
		protected Button	btnText;
		protected Button	btnTree;
		
//		private Button	btnCfg;
//		private Button btnSelect;
		protected DialogListEntryData<T>	dled;
		protected CellRendererDialogEntry<T>	assignedCellRenderer;
		protected String	strPrefix = "Cell";
//		private String	strColorFgBkpKey = "ColorFgBkp";
		protected Container	cntrCustomButtons;
		protected Container	cntrBase;
		
		public DialogListEntryData<T> getDialogListEntryData(){
			return dled;
		}
		
		protected void updateTreeButton(){
			String strDepth = "";
			
			String strTreeAction = dled.isParent() ? 
				(dled.isTreeExpanded()?"-":"+") :
				assignedCellRenderer.svfTreeDepthToken.getStringValue();
			
			String strDepthSeparator = "/";
			
			// tree depth
			DialogListEntryData<T> dledParent = dled.getParent();
			while(dledParent!=null){
				if(assignedCellRenderer.btgShowTreeUId.b()){
					strDepth = dledParent.getUId()+strDepthSeparator
						+strDepth;
				}else{
					strDepth = assignedCellRenderer.svfTreeDepthToken.getStringValue()
						+strDepth;
				}
				dledParent = dledParent.getParent();
			}
			
			if(assignedCellRenderer.btgShowTreeUId.b()){
				strDepth+=dled.getUId();//+":";
			}
			
			String str = "["+strTreeAction+strDepth+"]";
//			if(!strDepth.isEmpty() && strDepth.length()!=strDepth.trim().length()){
//				strDepth = "["+strDepth+"]";
//			}
			
			btnTree.setText(str); //it seems to auto trim the string...
			btnTree.setInsets(new Insets3f(0, 0, 0, 10));
			
			btnTree.setColor((ColorRGBA) btnTree.getUserData(EUserData.colorFgBkp.toString()));
			if(!assignedCellRenderer.diagParent.isOptionSelectionMode()){
				if(!dled.isParent())btnTree.setColor(btnTree.getShadowColor());
			}
		}
		
		@SuppressWarnings("unchecked")
		public Cell(CellRendererDialogEntry<T> parentCellRenderer, DialogListEntryData<T> dledToSet){
			super(new BorderLayout(), parentCellRenderer.strStyle);
			
			/**
			 * all other can come here too, even ones related to each instance,
			 * just put them outside the static ones block.
			 */
			if(Cell.btgBugFixGapForListBoxSelectorArea==null){
				/**
				 * Will be linked to the 1st instance of this class, 
				 * no problem as it will be a global.
				 */
				Cell.btgBugFixGapForListBoxSelectorArea = 
					new BoolTogglerCmdField(this,true).setCallNothingOnChange();
			}
			
			this.setName(strPrefix+"MainContainer");
			
			this.assignedCellRenderer=parentCellRenderer;
			this.dled=dledToSet;
			
//			cntrBase = (Container)bugFix(0);
			cntrBase = bugFix(Container.class, btgBugFixGapForListBoxSelectorArea);
			
			btnTree = createButton("Tree", "?", cntrBase, Position.West);
			btnTree.addCommands(ButtonAction.Click, ctt);
			btnTree.setUserData(Cell.class.getName(), this);
			btnTree.setUserData(EUserData.colorFgBkp.toString(), btnTree.getColor());
			
			btnText = createButton("Text", this.dled.getText(), cntrBase, Position.Center);
			
			cntrCustomButtons = new Container(new SpringGridLayout(), assignedCellRenderer.strStyle);
			cntrBase.addChild(cntrCustomButtons,Position.East);
			
			update(this.dled);
		}
		
		static class CommandTreeToggle implements Command<Button>{
			@Override
			public void execute(Button source) {
				@SuppressWarnings("rawtypes")
				Cell cell = (Cell)source.getUserData(Cell.class.getName());
				
				if(cell.dled.isParent()){
					cell.dled.toggleExpanded();
					cell.assignedCellRenderer.diagParent.requestRefreshList();
				}
				cell.updateTreeButton();
			}
		}
		CommandTreeToggle ctt = new CommandTreeToggle();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void update(DialogListEntryData<T> dled){
			DialogListEntryData<T> dataOld = this.dled;
			this.dled=dled;
			
			// tree button
			updateTreeButton();
			
			// entry text
			btnText.setText(dled.getText());
			
			// custom buttons
			boolean bButtonsChanged = false;
			
			if(this.dled.equals(dataOld))bButtonsChanged=true;//1st update is self
			
			if(!bButtonsChanged){
				Entry[] entryOldList = dataOld.getCustomButtonsActionsListCopy();
				Entry[] entryList = this.dled.getCustomButtonsActionsListCopy();
				if(entryOldList.length==entryList.length){
					for(int i=0;i<entryList.length;i++){
						Entry entryOld = entryOldList[i];
						Entry entry = entryList[i];
						if(
							!entry.getKey().equals(entryOld.getKey()) ||
							!entry.getValue().equals(entryOld.getValue())
						){
							bButtonsChanged = true;
							break;
						}
					}				
				}else{
					bButtonsChanged=true;
				}
			}
			
			if(bButtonsChanged){
				Entry[] eList = this.dled.getCustomButtonsActionsListCopy();
				cntrCustomButtons.clearChildren();
				for(int i=eList.length-1;i>=0;i--){
					Entry<String, T> entry = eList[i];
					createButton(entry.getKey(), "["+entry.getKey()+"]", cntrCustomButtons, i)
						.addCommands(ButtonAction.Click, (Command<Button>)entry.getValue());
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
		protected Button createButton(String strId, String strLabel, Container cntr, Object... aobjConstraints){
			if(strId==null)strId=strLabel;
			Button btn = new Button(strLabel,assignedCellRenderer.strStyle);
			MiscJmeI.i().retrieveBitmapTextFor(btn).setLineWrapMode(LineWrapMode.NoWrap);
			btn.setName(strPrefix+"Button"+strId);
			btn.setUserData(EUserData.cellClassRef.toString(),this);
			btn.setUserData(dled.getClass().getName(), dled);
			CursorEventControl.addListenersToSpatial(btn, DialogMouseCursorListenerI.i());
			cntr.addChild(btn,aobjConstraints);
			
			DialogMouseCursorListenerI.i().addDefaultCommands(btn);
			
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
		
		@SuppressWarnings("unchecked")
		@Override
		public <BFR> BFR bugFix(Class<BFR> clReturnType, BoolTogglerCmdField btgBugFixId,	Object... aobjCustomParams) {
			if(btgBugFixId==btgBugFixGapForListBoxSelectorArea){
//				MiscI.i().assertSameClass(Container.class,clReturnType);
				Container cntr=null;
				if(btgBugFixGapForListBoxSelectorArea.b()){
					/**
					 * this requires that all childs (in this case buttons) have their style background
					 * color transparent (like alpha 0.5f) or the listbox selector will not be visible below them...
					 */
					// same layout as the cell container
					cntr = new Container(new BorderLayout(), assignedCellRenderer.strStyle);
					cntr.setName("bugfixGap"); //when mouse is over a cell, if the ListBox->selectorArea has the same world Z value of the button, it may be ordered before the button on the raycast collision results at PickEventSession.setCurrentHitTarget(ViewPort, Spatial, Vector2f, CollisionResult) line: 262	-> PickEventSession.cursorMoved(int, int) line: 482 
					addChild(cntr, Position.Center);
				}else{
					cntr = this;
				}
				return (BFR)cntr;
			}
			
			return null;
		}

		@Override
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
			return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
		}

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Panel getView(DialogListEntryData<T> dled, boolean selected, Panel existing) {
    if( existing == null ) {
      existing = new Cell<T>(this,dled);
	  } else {
      ((Cell<T>)existing).update(dled);
	  }
	  return existing;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}

}
