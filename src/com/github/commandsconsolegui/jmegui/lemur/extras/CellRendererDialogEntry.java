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

import com.github.commandsconsolegui.cmd.varfield.BoolTogglerCmdField;
import com.github.commandsconsolegui.cmd.varfield.StringVarField;
import com.github.commandsconsolegui.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfg;
import com.github.commandsconsolegui.misc.ReflexFillI.IReflexFillCfgVariant;
import com.github.commandsconsolegui.misc.ReflexFillI.ReflexFillCfg;
import com.jme3.font.LineWrapMode;
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
	protected StringVarField svfTreeDepthToken = new StringVarField(this, " ", null);
	protected BoolTogglerCmdField	btgShowTreeUId=new BoolTogglerCmdField(this,true).setCallNothingOnChange();
	
	public enum ECell{
		CellClassRef
	}

	private String	strStyle;
	private LemurDialogGUIStateAbs<T>	diagParent;
//	private boolean	bOptionChoiceMode;
	
	public CellRendererDialogEntry(String strStyle, LemurDialogGUIStateAbs<T> diag){//, boolean bOptionChoiceMode) {
		this.strStyle=strStyle;
		this.diagParent=diag;
//		this.bOptionChoiceMode=bOptionChoiceMode;
	}
	
	public static class Cell<T> extends Container{
		private Button	btnText;
		private Button	btnTree;
		
//		private Button	btnCfg;
//		private Button btnSelect;
		private DialogListEntryData<T>	data;
		private CellRendererDialogEntry<T>	assignedCellRenderer;
		private String	strPrefix = "Cell";
		private Container	cntrCustomButtons;
		
		public DialogListEntryData<T> getData(){
			return data;
		}
		
		protected void updateTreeButton(){
			String strDepth = "";
			
			String strTreeAction = data.isParent() ? 
				(data.isTreeExpanded()?"-":"+") :
				assignedCellRenderer.svfTreeDepthToken.getStringValue();
			
			String strDepthSeparator = "/";
			
			// tree depth
			DialogListEntryData<T> dataParent = data.getParent();
			while(dataParent!=null){
				if(assignedCellRenderer.btgShowTreeUId.b()){
					strDepth = dataParent.getUId()+strDepthSeparator
						+strDepth;
				}else{
					strDepth = assignedCellRenderer.svfTreeDepthToken.getStringValue()
						+strDepth;
				}
				dataParent = dataParent.getParent();
			}
			
			if(assignedCellRenderer.btgShowTreeUId.b()){
				strDepth+=data.getUId()+":";
			}
			
			String str = "["+strTreeAction+strDepth+"]";
//			if(!strDepth.isEmpty() && strDepth.length()!=strDepth.trim().length()){
//				strDepth = "["+strDepth+"]";
//			}
			
			btnTree.setText(str); //it seems to auto trim the string...
			btnTree.setInsets(new Insets3f(0, 0, 0, 10));
			
			if(!data.isParent())btnTree.setColor(btnTree.getShadowColor());
		}
		
		@SuppressWarnings("unchecked")
		public Cell(CellRendererDialogEntry<T> parentCellRenderer, DialogListEntryData<T> dataToSet){
			super(new BorderLayout(), parentCellRenderer.strStyle);
			
			this.setName(strPrefix+"MainContainer");
			
			this.assignedCellRenderer=parentCellRenderer;
			this.data=dataToSet;
			
			btnTree = createButton("Tree", "?", this, Position.West);
			btnTree.addCommands(ButtonAction.Click, ctt);
			btnTree.setUserData(Cell.class.getName(), this);
			
			btnText = createButton("Text", this.data.getText(), this, Position.Center);
			
			cntrCustomButtons = new Container(new SpringGridLayout(),assignedCellRenderer.strStyle);
			addChild(cntrCustomButtons,Position.East);
			
			update(this.data);
		}
		
		static class CommandTreeToggle implements Command<Button>{
			@Override
			public void execute(Button source) {
				@SuppressWarnings("rawtypes")
				Cell cell = (Cell)source.getUserData(Cell.class.getName());
				
				if(cell.data.isParent()){
					cell.data.toggleExpanded();
					cell.assignedCellRenderer.diagParent.requestRefreshList();
				}
				cell.updateTreeButton();
			}
		}
		CommandTreeToggle ctt = new CommandTreeToggle();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void update(DialogListEntryData<T> data){
			DialogListEntryData<T> dataOld = this.data;
			this.data=data;
			
			// tree button
			updateTreeButton();
			
			// entry text
			btnText.setText(data.getText());
			
			// custom buttons
			boolean bButtonsChanged = false;
			
			if(this.data.equals(dataOld))bButtonsChanged=true;//1st update is self
			
			if(!bButtonsChanged){
				Entry[] entryOldList = dataOld.getCustomButtonsActionsListCopy();
				Entry[] entryList = this.data.getCustomButtonsActionsListCopy();
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
				Entry[] eList = this.data.getCustomButtonsActionsListCopy();
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
			btn.setUserData(ECell.CellClassRef.toString(),this);
			btn.setUserData(data.getClass().getName(), data);
			CursorEventControl.addListenersToSpatial(btn, DialogMouseCursorListenerI.i());
			cntr.addChild(btn,aobjConstraints);
			return btn;
		}

//		public boolean isCfgButton(Spatial spt) {
//			return spt==btnCfg;
//		}
		
		public boolean isTextButton(Spatial spt){
			return spt==btnText;
		}
		
//		public boolean isSelectButton(Spatial spt){
//			return spt==btnSelect;
//		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Panel getView(DialogListEntryData<T> data, boolean selected, Panel existing) {
    if( existing == null ) {
      existing = new Cell<T>(this,data);
	  } else {
      ((Cell<T>)existing).update(data);
	  }
	  return existing;
	}

	@Override
	public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv) {
		return GlobalCommandsDelegatorI.i().getReflexFillCfg(rfcv);
	}
}
