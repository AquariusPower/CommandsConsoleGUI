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

import com.github.commandsconsolegui.jmegui.extras.DialogListEntryData;
import com.github.commandsconsolegui.jmegui.lemur.DialogMouseCursorListenerI;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.BorderLayout;
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
	
	public CellRendererDialogEntry() {
		
	}
	
	public static class Cell extends Container{
		private Button	btnText;
		private Button	btnCfg;

		public Cell(DialogListEntryData data){
			super(new BorderLayout());
			
			btnText = new Button(data.getText());
			btnText.setName("CellText");
			btnText.setUserData(ECell.CellClassRef.toString(),this);
			addChild(btnText,BorderLayout.Position.Center);
			
			btnCfg = new Button("Cfg");
			btnCfg.setName("CellCfg");
			btnCfg.setUserData(ECell.CellClassRef.toString(),this);
//			btnCfg.setSize(new Vector3f(30,20,0)); //TODO use font metrics...
			CursorEventControl.addListenersToSpatial(btnCfg, DialogMouseCursorListenerI.i());
			addChild(btnCfg,BorderLayout.Position.East);
		}
		
		public void update(DialogListEntryData data){
			btnText.setText(data.getText());
		}

		public boolean isCfgButton(Spatial spt) {
			return spt==btnCfg;
		}
	}
	
	@Override
	public Panel getView(DialogListEntryData value, boolean selected, Panel existing) {
    if( existing == null ) {
      existing = new Cell(value);
	  } else {
      ((Cell)existing).update(value);
	  }
	  return existing;
	}
}
