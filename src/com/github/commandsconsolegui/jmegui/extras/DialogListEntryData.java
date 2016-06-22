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

package com.github.commandsconsolegui.jmegui.extras;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;

/**
 * Each list entry can show differently, they could be:
 * - blank spacer
 * - simple section title
 * - an option that can be cofigured
 * - a sound that can play/pause/stop (buttons)
 * - a check box to simply enable/disable
 * - etc...
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class DialogListEntryData<T> implements Savable{
	int iKey;
	String strText;
	Object objRef;
	HashMap<String,T> hmLabelAction = new HashMap<String,T>();
	T	actionTextDoubleClick;
	
	public DialogListEntryData<T> addLabelAction(String strId, T action){
		hmLabelAction.put(strId,action);
		return this;
	}
	
//	@SuppressWarnings("unchecked")
//	public Entry<String,T>[] getLabelActionListCopy(){
//		return (Entry<String,T>[])hmLabelAction.entrySet().toArray();
//	}
	public Entry[] getLabelActionListCopy(){
		return hmLabelAction.entrySet().toArray(new Entry[0]);
	}
	
	public int getKey() {
		return iKey;
	}
	public void setKey(int iKey) {
		this.iKey = iKey;
	}
	public String getText() {
		return strText;
	}
	public T getActionTextDoubleClick(){
		return this.actionTextDoubleClick;
	}
	public void setText(String strText, T actionDoubleClick) {
		this.strText = strText;
		this.actionTextDoubleClick=actionDoubleClick;
	}
	public Object getRef() {
		return objRef;
	}
	public void setRef(Object objRef) {
		this.objRef = objRef;
	}
	
	@Override
	public String toString() {
		return iKey+","+strText+","+objRef;
	}
	
	public void setCfg(DialogListEntryData data) {
		this.iKey = data.iKey;
		this.strText = data.strText;
		this.objRef = data.objRef;
	}
	
	/**
	 * TODO should be a more refined string
	 * @return
	 */
	public String report() {
		return this.toString();
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
