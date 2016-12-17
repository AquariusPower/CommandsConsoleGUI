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
package com.github.commandsconsolegui.spCmd.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import com.github.commandsconsolegui.spAppOs.misc.MiscI;
import com.github.commandsconsolegui.spAppOs.misc.PrerequisitesNotMetException;
import com.github.commandsconsolegui.spAppOs.misc.RefHolder;
import com.github.commandsconsolegui.spAppOs.misc.Buffeds.BfdArrayList;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 * @param <E>
 */
public class RegisteredClasses<E>{
	RefHolder<TreeMap<String,Class<E>>> rhtmSubClass = new RefHolder<TreeMap<String,Class<E>>>(new TreeMap<String,Class<E>>());
	BfdArrayList<E> aTargetList = new BfdArrayList<E>(){};
	
	/**
	 * will add super classes basically
	 * @param objTarget
	 * @param bItsInnerClassesToo
	 * @param bItsEnclosingClassesToo
	 */
	public void addClassesOf(E objTarget, boolean bItsInnerClassesToo, boolean bItsEnclosingClassesToo){
		PrerequisitesNotMetException.assertNotNull(objTarget, "objTarget", this);
		PrerequisitesNotMetException.assertNotAlreadyAdded(aTargetList, objTarget, this);
		
		for(Class cl:MiscI.i().getSuperClassesOf(objTarget,true)){
			registerClass(cl);
		}
		
		if(bItsInnerClassesToo){
			for(Class cl:objTarget.getClass().getDeclaredClasses()){ //register inner classes
				registerClass(cl);
			}
		}
		
		if(bItsEnclosingClassesToo){ //enclosing tree
			for(Class cl:MiscI.i().getEnclosingClassesOf(objTarget)){
				registerClass(cl);
			}
		}
		
		aTargetList.add(objTarget);
	}
	public void registerClass(Class<E> cl){
		rhtmSubClass.getRef().put(cl.getName(),cl);
	}
	public boolean isContainClassTypeName(String strClassTypeKey){
		Class<E> cl = (rhtmSubClass.getRef().get(MiscI.i().getEnclosingIfAnonymous(strClassTypeKey)));
		return ( cl != null );
	}
	public ArrayList<E> getTargetListCopy(){
		return new ArrayList<E>(Arrays.asList(aTargetList.toArray()));
	}
}
