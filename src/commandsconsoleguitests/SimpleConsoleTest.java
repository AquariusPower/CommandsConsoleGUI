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

package commandsconsoleguitests;

import com.github.commandsconsolegui.spExtras.SingleMandatoryAppInstanceI;
import com.github.commandsconsolegui.spExtras.globals.GlobalSingleMandatoryAppInstanceI;
import com.github.commandsconsolegui.spJme.extras.SimpleApplicationHelperI;
import com.github.commandsconsolegui.spJme.globals.GlobalAppSettingsI;
import com.github.commandsconsolegui.spLemur.console.SimpleConsolePlugin;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

/**
 * This is the simplest possible usage.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class SimpleConsoleTest extends SimpleApplication {
	@Override
	public void simpleInitApp() {
		// if you have your own single instance code, you can safely remove this line below
		GlobalSingleMandatoryAppInstanceI.i().configureRequiredAtApplicationInitialization();
		
		// here could be only it's initialize method, but will work this way also.
		new SimpleConsolePlugin(this)
			.configure(new SimpleConsolePlugin.CfgParm(this.getClass()))
			.initialize();
		
		/**
		 * use this if going to exec cmd from this class: 
				GlobalCommandsDelegatorI.i().addConsoleCommandListener(this);
		 */
	}
	
	public static void main( String... args ) {
		// if you have your own single instance code, you can safely remove these 2 lines below
		GlobalSingleMandatoryAppInstanceI.iGlobal().set(new SingleMandatoryAppInstanceI());
		GlobalSingleMandatoryAppInstanceI.i().configureOptionalAtMainMethod();
		
		SimpleConsoleTest test = new SimpleConsoleTest();
		
		/**
		 * will will want to set at least the app title, and this global is also important to let 
		 * some functionalities work
		 */
		GlobalAppSettingsI.iGlobal().set(new AppSettings(true));
		GlobalAppSettingsI.i().setTitle(test.getClass().getSimpleName());
		test.setSettings(GlobalAppSettingsI.i());
		
		test.start();
	}

	@Override
	public void stop(boolean waitFor) {
		SimpleApplicationHelperI.i().stop();
		super.stop(waitFor);
	}
	@Override
	public void destroy() {
		SimpleApplicationHelperI.i().destroy();
		super.destroy();
	}
}	
