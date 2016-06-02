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

package com.github.commandsconsolegui.console.jmegui;

import com.github.commandsconsolegui.cmd.CommandsDelegatorI;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;

/**
 * TODO can this make classes that shouldnt know others, be forced to know about? sure right?
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
@Deprecated
public class GlobalsJmeI {
	private static GlobalsJmeI instance = new GlobalsJmeI();
	public static GlobalsJmeI i(){return instance;}

	private Application	app;
	private SimpleApplication	sapp; //TODO will simple application vanish one day?
	private CommandsDelegatorI cd;
	
	public Application getApp(){
		return app;
	}
	
	public Application getSapp(){
		return sapp;
	}
	
	public CommandsDelegatorI getCmdDelegator(){
		return cd;
	}
	
	public void configure(Object... aobj){
		for(Object obj:aobj){
			if(obj instanceof Application){
				app=(Application) obj;
				if(obj instanceof SimpleApplication){
					sapp=(SimpleApplication) obj;
				}
			}else
			if(obj instanceof CommandsDelegatorI){
				cd = (CommandsDelegatorI)obj;
			}else
			{
				throw new UnsupportedOperationException("for: "+obj.getClass().getName());
			}
		}
	}
	
}
