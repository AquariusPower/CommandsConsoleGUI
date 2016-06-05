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

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.jmegui.extras.InteractionDialogStateAbs;
import com.github.commandsconsolegui.jmegui.lemur.console.ConsoleLemurStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurFocusHelperStateI;
import com.github.commandsconsolegui.jmegui.lemur.console.LemurMiscHelpersStateI;
import com.github.commandsconsolegui.misc.MiscI;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;

/**
* 
* More info at {@link InteractionDialogStateAbs}
* 
* @author AquariusPower <https://github.com/AquariusPower>
*
*/
public abstract class LemurDialogGUIStateAbs <V> extends InteractionDialogStateAbs<V> {
	protected Label	lblTitle;
	protected Label	lblTextInfo;
	protected ListBox<String>	lstbxEntriesToSelect;
	protected VersionedList<String>	vlstrEntriesList = new VersionedList<String>();
	protected int	iVisibleRows;
	
	@Override
	public Container getContainerMain(){
		return (Container)super.getContainerMain();
	}
	
	@Override
	protected boolean initGUI(){
		return initGUI(0.75f, 0.25f);
	}
	
	protected boolean initGUI(float fDialogPerc, float fInfoPerc){
		return initGUI(fDialogPerc, fInfoPerc,null);
	}
	
	public static class CfgParm extends InteractionDialogStateAbs.CfgParm{
		public CfgParm(String strUIId, boolean bIgnorePrefixAndSuffix, Node nodeGUI) {
			super(strUIId, bIgnorePrefixAndSuffix, nodeGUI);
		}
	}
	
	/**
	 * The input field will not require height, will be small on south edge.
	 * @param fDialogPerc the percentual width/height to cover the application screen/window 
	 * @param fInfoPerc the percentual height to show informational text, the list and input field will properly use the remaining space
	 */
	protected boolean initGUI(float fDialogPerc, float fInfoPerc, Integer iEntryHeightPixels){
		String strStyle = ConsoleLemurStateI.i().STYLE_CONSOLE;//BaseStyles.GLASS;
		
		Vector3f v3fApplicationWindowSize = new Vector3f(
			app().getContext().getSettings().getWidth(),
			app().getContext().getSettings().getHeight(),
			0);
			
		setContainerMain(new Container(new BorderLayout(), strStyle));
		getContainerMain().setName(strUIId+"_Dialog");
		
		Vector3f v3fDiagWindowSize = v3fApplicationWindowSize.mult(fDialogPerc);
		getContainerMain().setPreferredSize(v3fDiagWindowSize);
		
		///////////////////////// NORTH
		cntrNorth = new Container(new BorderLayout(), strStyle);
		getNorthContainer().setName(strUIId+"_NorthContainer");
		Vector3f v3fNorthSize = v3fDiagWindowSize.clone();
		v3fNorthSize.y *= fInfoPerc;
		getNorthContainer().setPreferredSize(v3fNorthSize);
		
		//title 
		lblTitle = new Label(strTitle,strStyle);
		lblTitle.setName(strUIId+"_Title");
		lblTitle.setColor(ColorRGBA.Green); //TODO make it custom
		getNorthContainer().addChild(lblTitle, BorderLayout.Position.North);
		
		// simple info
		lblTextInfo = new Label("",strStyle);
		lblTextInfo.setName(strUIId+"_TxtInfo");
		getNorthContainer().addChild(lblTextInfo, BorderLayout.Position.Center);
		
		getContainerMain().addChild(getNorthContainer(), BorderLayout.Position.North);
		
		//////////////////////////// CENTER
		// list
		float fListPerc = 1.0f - fInfoPerc;
		Vector3f v3fEntryListSize = v3fDiagWindowSize.clone();
		v3fEntryListSize.y *= fListPerc;
		lstbxEntriesToSelect = new ListBox<String>(new VersionedList<String>(),strStyle);
		lstbxEntriesToSelect.setName(strUIId+"_EntriesList");
		lstbxEntriesToSelect.setSize(v3fEntryListSize); //not preferred, so the input field can fit properly
		//TODO multi was not implemented yet... lstbxVoucherListBox.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		getContainerMain().addChild(lstbxEntriesToSelect, BorderLayout.Position.Center);
		
		vlstrEntriesList.add("(Empty list)");
		lstbxEntriesToSelect.setModel((VersionedList<String>)vlstrEntriesList);
		if(iEntryHeightPixels==null){
			if(vlstrEntriesList.size()>0){
				if(vlstrEntriesList.get(0) instanceof String){
					Float fEntryHeight = LemurMiscHelpersStateI.i().guessEntryHeight(lstbxEntriesToSelect);
					if(fEntryHeight!=null){
						iEntryHeightPixels=fEntryHeight.intValue(); //calc based on entry (or font) height and listbox height
						cd().dumpInfoEntry("entry height "+iEntryHeightPixels);
					}else{
						iEntryHeightPixels=20; //blind placeholder
						cd().dumpWarnEntry("blind entry height "+iEntryHeightPixels);
					}
				}
			}
		}
		iVisibleRows = (int) (v3fEntryListSize.y/iEntryHeightPixels);
		lstbxEntriesToSelect.setVisibleItems(iVisibleRows);
		
		//////////////////////////////// SOUTH
		// filter
		setIntputField(new TextField("",strStyle));
		getInputField().setName(strUIId+"_InputField");
		LemurFocusHelperStateI.i().addFocusChangeListener(getInputField());
		getContainerMain().addChild(getInputField(), BorderLayout.Position.South);
		
		Vector3f v3fPos = new Vector3f(
			(v3fApplicationWindowSize.x-v3fDiagWindowSize.x)/2f,
			(v3fApplicationWindowSize.y-v3fDiagWindowSize.y)/2f+v3fDiagWindowSize.y,
			0
		);
		getContainerMain().setLocalTranslation(v3fPos);
		
		getNodeGUI().attachChild(getContainerMain());
		
		return true;
	}
	
	protected Container getNorthContainer() {
		return (Container)cntrNorth;
	}

	@Override
	public void clearSelection() {
		lstbxEntriesToSelect.getSelectionModel().setSelection(-1); //clear selection
	}
	
	@Override
	public void requestFocus(Spatial spt) {
		LemurFocusHelperStateI.i().requestFocus(spt);
	}
	
	@Override
	protected boolean disableOrUndo() {
		if(getInputField().equals(LemurFocusHelperStateI.i().getFocused())){
			LemurFocusHelperStateI.i().removeFocusableFromList(getInputField());
		}
		
		return super.disableOrUndo();
	}
	
	/**
	 * when dialog is enabled,
	 * default is to fill with the last filter
	 */
	@Override
	protected void updateInputField(){
		getInputField().setText(strLastFilter);
	}
	
	protected TextField getInputField(){
		return (TextField)getIntputField();
	}
	
	@Override
	protected Integer getSelectedIndex(){
		Integer iSel = lstbxEntriesToSelect.getSelectionModel().getSelection();
		if(iSel==null)return null;
		return iSel;
	}
	@Override
	protected String getSelectedKey(){
		Integer i = getSelectedIndex();
		if(i==null)return null;
		if(i>=vlstrEntriesList.size())return null;
		return vlstrEntriesList.get(i);
	}
	
	/**
	 * call {@link #updateList(ArrayList)} from this overriden method.
	 */
	@Override
	protected abstract void updateList();
	
	protected void updateList(ArrayList<V> aValueList){
		hmKeyValue.clear();
		vlstrEntriesList.clear();
		lstbxEntriesToSelect.getSelectionModel().setSelection(-1);
		
		for(V val:aValueList){
			String strEntry = formatEntryKey(val);
			if(strEntry==null)throw new NullPointerException("entry is null, not formatted?");
			if(strLastFilter.isEmpty() || strEntry.toLowerCase().contains(strLastFilter)){
				vlstrEntriesList.add(strEntry);
				hmKeyValue.put(strEntry, val);
			}
		}
		
		if(strLastSelectedKey!=null){
			int i = vlstrEntriesList.indexOf(strLastSelectedKey);
			if(i>-1)lstbxEntriesToSelect.getSelectionModel().setSelection(i);
		}
		
		updateTextInfo();
	}
	
	/**
	 * default is the class name, will look like the dialog title
	 */
	@Override
	protected void updateTextInfo(){
//		lblTextInfo.setText("DIALOG for "+this.getClass().getSimpleName());
		lblTextInfo.setText("Info:");
	}
	
	@Override
	public void setTitle(String str) {
		super.setTitle(str);
		lblTitle.setText(str);
	}
	
	protected int getMaxIndex(){
		return (int)lstbxEntriesToSelect.getSlider().getModel().getMaximum()-1;
	}
	
	protected int getTopEntryIndex(){
		int iTopEntryIndex = (int)(
			getMaxIndex()-lstbxEntriesToSelect.getSlider().getModel().getValue());
		return iTopEntryIndex;
	}
	
	protected int getBottomEntryIndex(){
		return getTopEntryIndex()+iVisibleRows;
	}
	
	protected void scrollTo(int iIndex){
		lstbxEntriesToSelect.getSlider().getModel().setValue(getMaxIndex()-iIndex);
	}
	
	@Override
	public boolean doItAllProperly(float tpf) {
		if(!super.doItAllProperly(tpf))return false;
		
		Integer iSelected = getSelectedIndex();
		if(iSelected!=null){
			int iTopEntryIndex = getTopEntryIndex();
			int iBottomItemIndex = getBottomEntryIndex();
			Integer iScrollTo=null;
			if(iSelected>=iBottomItemIndex){
				iScrollTo=iSelected-iBottomItemIndex+iTopEntryIndex;
			}else
			if(iSelected<=iTopEntryIndex){
				iScrollTo=iSelected-1;
			}
			if(iScrollTo!=null){
				scrollTo(iScrollTo);
			}
		}
		
		return true;
	}
	
	@Override
	protected boolean initKeyMappings(){
		KeyActionListener actSimpleActions = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
	//		boolean bShift = key.hasModifier(0x01);
				
				Integer iSel = lstbxEntriesToSelect.getSelectionModel().getSelection();
				int iMax = getMaxIndex();
				if(iSel==null){
					if(iMax>0){
						iSel=0;
					}else{
						iSel=-1;
					}
				}
					
				switch(key.getKeyCode()){
					case KeyInput.KEY_ESCAPE:
						setEnabledRequest(false);
						break;
					case KeyInput.KEY_UP:
						if(iSel>0)lstbxEntriesToSelect.getSelectionModel().setSelection(iSel-1);
						break;
					case KeyInput.KEY_DOWN:
						if(iSel<iMax)lstbxEntriesToSelect.getSelectionModel().setSelection(iSel+1);
						break;
					case KeyInput.KEY_NUMPADENTER:
					case KeyInput.KEY_RETURN:
						actionSubmit();
						break;
					case KeyInput.KEY_V: 
						if(bControl){
							getInputField().setText(getInputField().getText()
								+MiscI.i().retrieveClipboardString(true));
						}
						break;
				}
			}
	
		};
		
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_RETURN), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_NUMPADENTER), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_ESCAPE), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_UP), actSimpleActions);
		getInputField().getActionMap().put(new KeyAction(KeyInput.KEY_DOWN), actSimpleActions);
		
		return true;
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,"showDialogKeyBinds","")){
			cc.dumpSubEntry("ESC - close; Up/Down - nav. list entry; Enter - accept/submit choice;");
			bCommandWorked = true;
		}else
		{
			return super.execConsoleCommand(cc);
//			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	public String getInputText() {
		return getInputField().getText();
	}

}
