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

package com.github.commandsconsolegui.spJme.extras;

import com.github.commandsconsolegui.spAppOs.DelegateManagerI;
import com.github.commandsconsolegui.spAppOs.ManageExitI;
import com.github.commandsconsolegui.spAppOs.globals.GlobalOSAppI;
import com.github.commandsconsolegui.spAppOs.globals.cmd.GlobalCommandsDelegatorI;
import com.github.commandsconsolegui.spAppOs.misc.IManager;
import com.github.commandsconsolegui.spAppOs.misc.ISingleInstance;
import com.github.commandsconsolegui.spAppOs.misc.ManageSingleInstanceI;
import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spJme.globals.GlobalAppRefI;
import com.jme3.app.SimpleApplication;
import com.jme3.system.lwjgl.LwjglAbstractDisplay;


/**
 * If you are using {@link SimpleApplication}, these are important methods to call on it.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class SimpleApplicationHelperI extends SimpleApplication implements ISingleInstance {
	private static SimpleApplicationHelperI instance = new SimpleApplicationHelperI();
	public static SimpleApplicationHelperI i(){return instance;}
	
	public SimpleApplicationHelperI() {
		DelegateManagerI.i().addManaged(this);
//		ManageSingleInstanceI.i().add(this);
	}
	
	@Override
	public void simpleInitApp() {
		// disable some mappings to let the console manage it.
		GlobalAppRefI.i().getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_CAMERA_POS); //TODO there is no super code for it?
		GlobalAppRefI.i().getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_MEMORY); //TODO there is no super code for it?
		GlobalAppRefI.i().getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_HIDE_STATS);
		GlobalAppRefI.i().getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT); //this is important to let ESC be used to more things
		
	}
	
	/**
	 * this is called for uncaugth exceptions! from {@link LwjglAbstractDisplay}
	 */
	@Override
	public void handleError(String errMsg, Throwable t) {
		PrerequisitesNotMetException.setExitRequestCause(errMsg,t);
	}
	
	/**
	 * this is directly called when window is closed using it's close button
	 */
	@Override
	public void destroy() {
		GlobalCommandsDelegatorI.i().cmdRequestCleanSafeNormalExit();
	}
	
	/**
	 * this is a clean/safe exit by request
	 */
	@Override
	public void simpleUpdate(float tpf) {
		if(GlobalOSAppI.i().isApplicationExiting()){
			if(ManageExitI.i().isCanCleanExit()){
				GlobalAppRefI.i().stop();
				return; //useless?
			}
		}
	}

	@Override
	public String getUniqueId() {
		return MiscI.i().prepareUniqueId(this);
	}

}