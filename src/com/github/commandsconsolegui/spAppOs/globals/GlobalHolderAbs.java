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

package com.github.commandsconsolegui.spAppOs.globals;

import com.github.commandsconsolegui.spAppOs.misc.CheckInitAndCleanupI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;

/**
 * Global Reference Holder
 * 
 * Useful to:
 * 	Lower as much as possible the dependencies, so some class field's values can be globalized.
 * 	Easily access what can/should be globally accessible.
 * 	Allow some classes to be optionally instanced/enabled.
 * 	The same object may be present in more than one global, just being a sub-class, scope dependency range.
 * 	Allow globally accessible overriden class/instance.
 * 
 * 	The main application class is important to be stored on a global to make it's uses depend
 * solely on it's superclass.
 * 	Some classes that require a non empty constructor will fit well with this Global implementation.
 * 	So, single instance classes that are globally accessible and will be instanced with an empty constructor,
 * have no real need to use this, but.. this helps on letting the class be extended easily, 
 * therefore should be used everywhere! 
 *
 *	TODO make all single instances use this methodology to let them be extended easily
 *	TODO classes ending with I should be intended to be accessible only thru the globals
 *	TODO such classes shall also have a static field to optionally prevent new instances by assigning it's 1st, at it's constructor
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 * 
 * @param <T>
 */
public abstract class GlobalHolderAbs<T> { //not abstract methods yet tho...
	/**
	 * optional to improve functionality
	 */
	public static interface IGlobalOpt{
		public boolean isDiscarded();
	}
	
	T obj;
	
	private void setAssertingNotAlreadySet(T objNew){
		validate();
		this.obj = CheckInitAndCleanupI.i().assertGlobalIsNull(this.obj, objNew);
	}
	
	/**
	 * validates if referenced object is set
	 * @return
	 */
	public T get(){
		if(!isSet()){
			throw new PrerequisitesNotMetException("global not set yet...", this);
		}
		return obj;
	}
	
	public void validate(){
		if(obj instanceof IGlobalOpt){
			if(((IGlobalOpt)obj).isDiscarded()){
				obj=null;
			}
		}
	}
	
	public boolean isSet(){
		validate();
		return obj!=null;
	}
	
	public T set(T obj){
		setAssertingNotAlreadySet(obj);
		return this.obj; //easy chain
	} 
}
