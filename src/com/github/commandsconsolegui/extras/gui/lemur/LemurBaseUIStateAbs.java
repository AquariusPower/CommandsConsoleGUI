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

package com.github.commandsconsolegui.extras.gui.lemur;

import java.util.ArrayList;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.github.commandsconsolegui.cmd.CommandsDelegatorI.ECmdReturnStatus;
import com.github.commandsconsolegui.cmd.IConsoleCommandListener;
import com.github.commandsconsolegui.console.gui.lemur.ConsoleGUILemurState;
import com.github.commandsconsolegui.console.gui.lemur.LemurFocusHelperI;
import com.github.commandsconsolegui.console.gui.lemur.LemurMiscHelpersStateI;
import com.github.commandsconsolegui.extras.gui.BaseUIStateAbs;
import com.github.commandsconsolegui.misc.MiscI;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.math.Vector3f;
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
* @author AquariusPower <https://github.com/AquariusPower>
*
*/
public abstract class LemurBaseUIStateAbs <V> extends BaseUIStateAbs<V> implements IConsoleCommandListener{
	protected Label	lblTextInfo;
	protected ListBox<String>	lstbxEntriesToSelect;
	protected VersionedList<String>	vlstrEntriesList = new VersionedList<String>();
	
	public LemurBaseUIStateAbs(String strUIId){
		super(strUIId);
		this.strCmd="toggleUI"+strUIId;
	}
	
	public String getCommand(){
		return strCmd;
	}
	
	public void configure(CommandsDelegatorI cc){
		this.cc=cc;
	}
	
	/**
	 * Activate, Start, Begin, Initiate.
	 * This will setup and activate everything to make it actually start working.
	 */
	@Override
	public void initialize(Application app) {
		asteInitDebug = Thread.currentThread().getStackTrace();
		
		this.sapp = (SimpleApplication)app;
		
		initGUI();
		initKeyMappings();
		
		setEnabled(false);
		
		cc.addConsoleCommandListener(this);
	}
	
	/**
	 * mainly to configure/call {@link #initGUI(float, float, float)}
	 */
	@Override
	protected void initGUI(){
		initGUI(0.75f, 0.25f);
	}
	
	@Override
	protected void initGUI(float fDialogPerc, float fInfoPerc){
		initGUI(fDialogPerc, fInfoPerc,null);
	}
	
	protected Container getTopContainer(){
		return (Container)ctnrTop;
	}
	
	/**
	 * The input field will not require height, will be small on south edge.
	 * @param fDialogPerc the percentual width/height to cover the application screen/window 
	 * @param fInfoPerc the percentual height to show informational text, the list and input field will properly use the remaining space
	 */
	@Override
	protected void initGUI(float fDialogPerc, float fInfoPerc, Integer iEntryHeightPixels){
		String strStyle = ConsoleGUILemurState.i().STYLE_CONSOLE;//BaseStyles.GLASS;
		
		Vector3f v3fApplicationWindowSize = new Vector3f(
			sapp.getContext().getSettings().getWidth(),
			sapp.getContext().getSettings().getHeight(),
			0);
			
		ctnrTop = new Container(new BorderLayout(), strStyle);
		getTopContainer().setName(strUIId+"_TopDiag");
		
		Vector3f v3fDiagWindowSize = v3fApplicationWindowSize.mult(fDialogPerc);
		getTopContainer().setPreferredSize(v3fDiagWindowSize);
		
		// simple info
		lblTextInfo = new Label("",strStyle);
		lblTextInfo.setName(strUIId+"_TxtInfo");
		Vector3f v3fInfoSize = v3fDiagWindowSize.clone();
		v3fInfoSize.y *= fInfoPerc;
		lblTextInfo.setPreferredSize(v3fInfoSize);
		getTopContainer().addChild(lblTextInfo, BorderLayout.Position.North);
		
		// list area
		float fListPerc = 1.0f - fInfoPerc;
		Vector3f v3fEntryListSize = v3fDiagWindowSize.clone();
		v3fEntryListSize.y *= fListPerc;
		lstbxEntriesToSelect = new ListBox<String>(new VersionedList<String>(),strStyle);
		lstbxEntriesToSelect.setName(strUIId+"_EntriesList");
		lstbxEntriesToSelect.setSize(v3fEntryListSize); //not preferred, so the input field can fit properly
		//TODO multi was not implemented yet... lstbxVoucherListBox.getSelectionModel().setSelectionMode(SelectionMode.Multi);
		getTopContainer().addChild(lstbxEntriesToSelect, BorderLayout.Position.Center);
		
		vlstrEntriesList.add("(Empty list)");
		lstbxEntriesToSelect.setModel((VersionedList<String>)vlstrEntriesList);
		if(iEntryHeightPixels==null){
			if(vlstrEntriesList.size()>0){
				if(vlstrEntriesList.get(0) instanceof String){
					Float fEntryHeight = LemurMiscHelpersStateI.i().guessEntryHeight(lstbxEntriesToSelect);
					if(fEntryHeight!=null){
						iEntryHeightPixels=fEntryHeight.intValue(); //calc based on entry (or font) height and listbox height
						cc.dumpInfoEntry("entry height "+iEntryHeightPixels);
					}else{
						iEntryHeightPixels=20; //blind placeholder
						cc.dumpWarnEntry("blind entry height "+iEntryHeightPixels);
					}
				}
			}
		}
		lstbxEntriesToSelect.setVisibleItems((int) (v3fEntryListSize.y/iEntryHeightPixels));
		
		// filter
		tfInputText = new TextField("",strStyle);
		getInputField().setName(strUIId+"_InputField");
		LemurFocusHelperI.i().addFocusChangeListener(getInputField());
		getTopContainer().addChild(getInputField(), BorderLayout.Position.South);
		
		Vector3f v3fPos = new Vector3f(
			(v3fApplicationWindowSize.x-v3fDiagWindowSize.x)/2f,
			(v3fApplicationWindowSize.y-v3fDiagWindowSize.y)/2f+v3fDiagWindowSize.y,
			0
		);
		getTopContainer().setLocalTranslation(v3fPos);
		
		sapp.getGuiNode().attachChild(getTopContainer());
	}
	
	public void clearSelection() {
		lstbxEntriesToSelect.getSelectionModel().setSelection(-1); //clear selection
	}
	
	public void toggle() {
		setEnabled(!isEnabled());
	}
	
	@Override
	public void requestFocus(Spatial spt) {
		LemurFocusHelperI.i().requestFocus(spt);
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		
		ctnrTop.removeFromParent();
		
		if(getInputField().equals(LemurFocusHelperI.i().getFocused())){
			LemurFocusHelperI.i().removeFocusableFromList(getInputField());
		}
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
		return (TextField)tfInputText;
	}
	
	protected Integer getSelectedIndex(){
		Integer iSel = lstbxEntriesToSelect.getSelectionModel().getSelection();
		if(iSel==null)return null;
		return iSel;
	}
	protected String getSelectedKey(){
		Integer i = getSelectedIndex();
		if(i==null)return null;
		if(i>=vlstrEntriesList.size())return null;
		return vlstrEntriesList.get(i);
	}
	protected V getSelectedValue() {
		return hmKeyValue.get(getSelectedKey());
	}
	
	/**
	 * mainly to call {@link #updateList(ArrayList)}
	 */
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
	protected void updateTextInfo(){
		lblTextInfo.setText("DIALOG for "+this.getClass().getSimpleName());
	}
	
	@Override
	public void update(float tpf) {
		String str = getSelectedKey();
		if(str!=null)strLastSelectedKey=str;
		
		updateTextInfo();
		
		setMouseKeepUngrabbed(isEnabled());
	}
	
	public abstract void setMouseKeepUngrabbed(boolean b);
	
	/**
	 * What will be shown at each entry on the list.
	 * Default is to return the default string about the object.
	 * 
	 * @param val
	 * @return
	 */
	public String formatEntryKey(V val){
		return val.toString();
	}
	
	protected void initKeyMappings(){
		KeyActionListener actSimpleActions = new KeyActionListener() {
			@Override
			public void keyAction(TextEntryComponent source, KeyAction key) {
				boolean bControl = key.hasModifier(KeyAction.CONTROL_DOWN); //0x1
	//		boolean bShift = key.hasModifier(0x01);
				
				Integer iSel = lstbxEntriesToSelect.getSelectionModel().getSelection();
				if(iSel==null)iSel=-1;
					
				switch(key.getKeyCode()){
					case KeyInput.KEY_ESCAPE:
						setEnabled(false);
						break;
					case KeyInput.KEY_UP:
						lstbxEntriesToSelect.getSelectionModel().setSelection(iSel-1);
						break;
					case KeyInput.KEY_DOWN:
						lstbxEntriesToSelect.getSelectionModel().setSelection(iSel+1);
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
	}
	
	/**
	 * what happens when pressing ENTER keys
	 * default is to update the list filter
	 */
	protected void actionSubmit(){
		strLastFilter=getInputField().getText();
		updateList();
	}
	
	@Override
	public ECmdReturnStatus execConsoleCommand(CommandsDelegatorI cc) {
		boolean bCommandWorked = false;
		
		if(cc.checkCmdValidity(this,strCmd,"[bEnabledForce]")){
			Boolean bEnabledForce = cc.paramBoolean(1);
			if(bEnabledForce!=null){
				setEnabled(bEnabledForce);
			}else{
				toggle();
			}
			bCommandWorked = true;
		}else
		{
			return ECmdReturnStatus.NotFound;
		}
		
		return cc.cmdFoundReturnStatus(bCommandWorked);
	}
	
	@Override
	protected void cleanup(Application app) {
	}
}
