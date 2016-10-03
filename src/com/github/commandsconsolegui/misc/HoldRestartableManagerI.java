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

package com.github.commandsconsolegui.misc;

import java.util.ArrayList;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class HoldRestartableManagerI<T extends IRestartable> implements IManager<HoldRestartable<T>>{
	private static HoldRestartableManagerI instance = new HoldRestartableManagerI();
	public static HoldRestartableManagerI i(){return instance;}
	
	public static final class CompositeControl extends CompositeControlAbs<HoldRestartableManagerI>{
		private CompositeControl(HoldRestartableManagerI casm){super(casm);};
	};private CompositeControl ccSelf = new CompositeControl(this);
	
	private ArrayList<HoldRestartable<T>> ahrList=new ArrayList<HoldRestartable<T>>();
	
	public void revalidateAndUpdateAllRestartableHoldersFor(IRestartable irDiscarding, IRestartable irNew){
		for(HoldRestartable<T> hr:new ArrayList<HoldRestartable<T>>(ahrList)){
			// discard
			if(hr.isDiscardSelf() || DiscardableInstanceI.i().isBeingDiscardedRecursiveOwner(hr)){
				ahrList.remove(hr);
				continue;
			}
			
			// update ref
			if(hr.getRef()==irDiscarding){
				hr.setRef(irNew);
			}
		}
	}
	
	@Override
	public boolean add(HoldRestartable<T> hr){
		PrerequisitesNotMetException.assertNotAlreadyAdded(ahrList, hr, this);
		return ahrList.add(hr);
	}
	
	@Deprecated
	@Override
	public ArrayList<HoldRestartable<T>> getListCopy() {
		throw new UnsupportedOperationException("method not implemented yet");
	}
}
