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

package com.github.commandsconsolegui.jmegui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.github.commandsconsolegui.jmegui.MouseCursorCentralI.EMouseCursorButton;
import com.github.commandsconsolegui.misc.PrerequisitesNotMetException;
import com.jme3.scene.Spatial;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class MouseCursorButtonClicks {
	EMouseCursorButton emcb = null;
	public MouseCursorButtonClicks(EMouseCursorButton emcb) {
		this.emcb = emcb;
	}
	
	public static class MouseButtonClick{
		long lMilis=-1;
		EMouseCursorButton emcb;
//		CursorButtonEvent eventButton;
		Spatial target;
		Spatial capture;
		
		public boolean isRepeating(MouseButtonClick other) {
			if (emcb!=other.emcb)return false;
			if (capture!=other.capture)return false;
//			if (!eventButton.equals(other.eventButton))return false;
			if (target!=other.target)return false;
			
			return true;
		}

		public MouseButtonClick(EMouseCursorButton emcb, Spatial target, Spatial capture) {
			super();
			this.lMilis=System.currentTimeMillis();
			this.emcb = emcb;
//			this.eventButton = eventButton;
			this.target = target;
			this.capture = capture;
		}
	}
	
	ArrayList<MouseButtonClick> aFlurryOfClicks = new ArrayList<MouseButtonClick>();
	
	@Deprecated
	public ArrayList<MouseButtonClick> getClicksListFor(EMouseCursorButton emcb){
//		if(clicksMaintenanceUpdate()){
			ArrayList<MouseButtonClick> a = new ArrayList<MouseButtonClick>();
			for(MouseButtonClick c:aFlurryOfClicks){
				if(c.emcb.compareTo(emcb)==0)a.add(c);
			}
			
			return a;
//		}
		
//		return null;
	}
	
	public int getMultiClickCountFor(EMouseCursorButton emcb){
//		ArrayList<MouseButtonClick> clkForButtonList = getClicksListFor(emcb);
		ArrayList<MouseButtonClick> clkForButtonList = new ArrayList<MouseButtonClick>(aFlurryOfClicks);
		Collections.reverse(clkForButtonList); //from newest to oldest
		
		int iClickCount=1;
		if(clkForButtonList!=null){
			MouseButtonClick clkNewer = null;;
			for(MouseButtonClick clkCurrent:clkForButtonList){
				if(clkNewer!=null){
					if(clkCurrent.isRepeating(clkNewer)){
						long lDelay=clkNewer.lMilis-clkCurrent.lMilis;
						if(lDelay<0)throw new PrerequisitesNotMetException("invalid negative multi-click delay");
						if(lDelay <= MouseCursorCentralI.i().ilvMultiClickMaxDelayMilis.getLong()){
							iClickCount++;
						}else{
							/**
							 * this will remove all too old ones
							 */
							aFlurryOfClicks.remove(clkCurrent);
						}
					}
				}
				
				clkNewer=clkCurrent;
			}
		}
		
		return iClickCount;
	}
	
	
	
//public void addClick(FlurryOfClicks focEMouseCursorButton emcb, CursorButtonEvent eventButton, Spatial target, Spatial capture) {
//aClicks.add(new FlurryOfClicks(emcb, eventButton, target, capture));
//}
	public void addClick(MouseButtonClick foc) {
		aFlurryOfClicks.add(foc);
	}

	public void clearClicks() {
		aFlurryOfClicks.clear();
	}
	
}
