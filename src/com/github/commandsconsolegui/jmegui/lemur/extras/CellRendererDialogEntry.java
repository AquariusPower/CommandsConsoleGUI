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

import com.github.commandsconsolegui.jmegui.MiscJmeI;
import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.jme3.font.LineWrapMode;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.list.CellRenderer;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class CellRendererDialogEntry implements CellRenderer<DialogListEntryData> {
	public enum ECell{
		CellClassRef
	}

	private String	strStyle;
	private boolean	bOptionChoiceMode;
	
	public CellRendererDialogEntry(String strStyle, boolean bOptionChoiceMode) {
		this.strStyle=strStyle;
		this.bOptionChoiceMode=bOptionChoiceMode;
	}
	
	public static class Cell extends Container{
		private Button	btnText;
		private Button	btnCfg;
		private Button btnSelect;
		private DialogListEntryData	data;
		private CellRendererDialogEntry	assignedCellRenderer;
		private String	strPrefix = "Cell";
		
		public DialogListEntryData getData(){
			return data;
		}
		
		public Cell(CellRendererDialogEntry parentCellRenderer, DialogListEntryData data){
			super(new BorderLayout());
			
			this.setName(strPrefix+"MainContainer");
			
			this.assignedCellRenderer=parentCellRenderer;
			this.data=data;
			
			btnText = createButton("Text", data.getText(), Position.Center);
//			btnText = new Button(data.getText(),parent.strStyle);
//			btnText.setName("CellText");
//			btnText.setUserData(ECell.CellClassRef.toString(),this);
//			CursorEventControl.addListenersToSpatial(btnText, DialogMouseCursorListenerI.i());
//			addChild(btnText,BorderLayout.Position.Center);
			
			Position p = Position.East;
			if(assignedCellRenderer.bOptionChoiceMode){
				btnSelect = createButton(null, "Select", p);
//				btnSelect = new Button("Select",assignedCellRenderer.strStyle);
//				btnSelect.setName("CellSelect");
//				btnSelect.setUserData(ECell.CellClassRef.toString(),this);
//	//			btnCfg.setSize(new Vector3f(30,20,0)); //TODO use font metrics...
//				CursorEventControl.addListenersToSpatial(btnSelect, DialogMouseCursorListenerI.i());
//				addChild(btnSelect,BorderLayout.Position.East);
			}else{
				btnCfg = createButton(null, "Cfg", p);
//				btnCfg = new Button("Cfg",parent.strStyle);
//				btnCfg.setName("CellCfg");
//				btnCfg.setUserData(ECell.CellClassRef.toString(),this);
//	//			btnCfg.setSize(new Vector3f(30,20,0)); //TODO use font metrics...
//				CursorEventControl.addListenersToSpatial(btnCfg, DialogMouseCursorListenerI.i());
//				addChild(btnCfg,BorderLayout.Position.East);
			}
		}
		
		/**
		 * 
		 * @param strId if null, will be the label 
		 * @param strLabel
		 * @param p
		 * @return
		 */
		protected Button createButton(String strId, String strLabel, Position p){
			if(strId==null)strId=strLabel;
			Button btn = new Button(strLabel,assignedCellRenderer.strStyle);
			MiscJmeI.i().retrieveBitmapTextFor(btn).setLineWrapMode(LineWrapMode.NoWrap);
			btn.setName(strPrefix +strId);
			btn.setUserData(ECell.CellClassRef.toString(),this);
			CursorEventControl.addListenersToSpatial(btn, DialogMouseCursorListenerI.i());
			addChild(btn,p);
			return btn;
		}
		
		public void update(DialogListEntryData data){
			this.data=data;
			btnText.setText(data.getText());
		}

		public boolean isCfgButton(Spatial spt) {
			return spt==btnCfg;
		}
		
		public boolean isTextButton(Spatial spt){
			return spt==btnText;
		}
		
		public boolean isSelectButton(Spatial spt){
			return spt==btnSelect;
		}
		
		
	}
	
	@Override
	public Panel getView(DialogListEntryData data, boolean selected, Panel existing) {
    if( existing == null ) {
      existing = new Cell(this,data);
	  } else {
      ((Cell)existing).update(data);
	  }
	  return existing;
	}
}
