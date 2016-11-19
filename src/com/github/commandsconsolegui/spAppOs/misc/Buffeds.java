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

package com.github.commandsconsolegui.spAppOs.misc;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 *
 */
public class Buffeds {
	/**
	 * Does not allow duplicates.
	 * Does not allow nulls.
	 * 
	 * must be abstract to ensure an anonimous (inner) instance is created,
	 * otherwise {@link ReflexFillI#getGenericParamAsClassTypeFrom(Object, int)}
	 * will not work
	 */
	public static abstract class BfdArrayList<E> extends ArrayList<E>{
		@Override
		public E[] toArray() {
			/**
			 * new instance array of size 0 here is important to avoid exception with class types that 
			 * have no default empty constructor! 
			 */
			Class cl=null;
			
//			if(size()>0){
//				cl=get(0).getClass();
//			}else{
				cl = ReflexFillI.i().getGenericParamAsClassTypeFrom(this,0);
//			}
			
			E[] a = (E[])Array.newInstance(cl,0);
		  return super.toArray(a);
		}
		
		public BfdArrayList() {
			super();
		}
		
		private boolean isCanAdd(E o){
			if(o==null)return false;
			if(contains(o))return false;
			return true;
		}
		
		@Override
		public boolean add(E e) {
			if(!isCanAdd(e))return false;
			return super.add(e);
		}
		@Override
		public void add(int index, E element) {
			if(!isCanAdd(element))return;
			super.add(index, element);
		}
		
		@Override
		public boolean addAll(Collection<? extends E> c) {
			for(E o:(E[])c.toArray()){if(!isCanAdd(o))c.remove(o);}
			return super.addAll(c);
		}
		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			for(E o:(E[])c.toArray()){if(!isCanAdd(o))c.remove(o);}
			return super.addAll(index, c);
		}
		
    public BfdArrayList(Collection<? extends E> c) {
    	super(c);
    }
    
    public BfdArrayList(int i) {
			super(i);
		}
    
		public BfdArrayList<E> getCopy(){
			return new BfdArrayList<E>(this){};
		}
		
		/**
		 * TODO this works???
		 * @return
		 */
		public BfdArrayList getGenericCopy(){
			return new BfdArrayList(this){};
		}
	}
}
