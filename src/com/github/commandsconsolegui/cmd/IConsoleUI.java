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

package com.github.commandsconsolegui.cmd;

import java.util.AbstractList;
import java.util.ArrayList;

import com.github.commandsconsolegui.misc.CheckInitAndCleanupI.ICheckInitAndCleanupI;

/**
 * 
 * This is a "functionality requester" general class for UI.
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public interface IConsoleUI extends ICheckInitAndCleanupI{
	public abstract void dumpAllStats();

//	public abstract void setConsoleMaxWidthInCharsForLineWrap(Integer paramInt);

//	public abstract Integer getConsoleMaxWidthInCharsForLineWrap();

	public abstract AbstractList<String> getDumpEntriesSlowedQueue();

	public abstract AbstractList<String> getDumpEntries();

	public abstract AbstractList<String> getAutoCompleteHint();

	public abstract String getInputText();

	public abstract void setInputField(String str);

	public abstract void scrollToBottomRequest();

	public abstract String getDumpAreaSliderStatInfo();

//	public abstract int getCmdHistoryCurrentIndex();

	public abstract int getLineWrapAt();

	public abstract ArrayList<String> wrapLineDynamically(DumpEntryData de);

	public abstract void clearDumpAreaSelection();

	public abstract void clearInputTextField();
	
	public abstract void updateEngineStats();
	
	public abstract void cmdLineWrapDisableDumpArea();

	public abstract boolean cmdEditCopyOrCut(boolean b);

	public abstract void setVisibleRowsAdjustRequest(Integer paramInt);

	public abstract boolean isVisibleRowsAdjustRequested();

	public abstract boolean statsFieldToggle();

//	public abstract void recreateConsoleGui();

	public abstract boolean isEnabled();

	public abstract void requestRestart();
}
