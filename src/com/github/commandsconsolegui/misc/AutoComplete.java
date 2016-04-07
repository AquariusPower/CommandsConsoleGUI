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

package com.github.commandsconsolegui.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class AutoComplete {
	public static AutoComplete instance = new AutoComplete();
	public static AutoComplete i(){return instance;}
	
	/**
	 * Matching is case insensitive.
	 * 
	 * @param strPart partial match
	 * @param astrAllPossibilities all possible values to check for a match
	 * @return 
	 * 	If it has more than one entry, the first one will be an improved partial match.
	 * 	If it has only one entry, or it will be the unmodified part, 
	 *		or (if its length is bigger) it will be an exact match!
	 */
	public ArrayList<String> autoComplete(String strPart, ArrayList<String> astrAllPossibilities, boolean bMatchContains){
		ArrayList<String> astrPossibleMatches = new ArrayList<String>();
		
		strPart=strPart.trim();
		if(strPart.isEmpty())return astrPossibleMatches;
//		if(strPart.matches("[^"+strValidCmdCharsRegex+"]"))return astrPossibleMatches;
		for(String str:astrAllPossibilities){
			if(bMatchContains){
				if(str.toLowerCase().contains(strPart.toLowerCase())){
					astrPossibleMatches.add(str);
				}
			}else{
				if(str.toLowerCase().startsWith(strPart.toLowerCase())){
					astrPossibleMatches.add(str);
				}
			}
		}
		
		// found single possibility
		if(astrPossibleMatches.size()==1)
			return astrPossibleMatches;
		
		if(!bMatchContains){
			lbMatch:while(true){
				Character ch = null;
				for(String str:astrPossibleMatches){
					if(str.length()<=strPart.length())break lbMatch;
					
					Character chOther = str.charAt(strPart.length());
					if(ch==null){
						ch = chOther;
					}
					
					if(Character.toLowerCase(ch)!=Character.toLowerCase(chOther)){
						break lbMatch;
					}
				}
				
				if(ch==null)break;
				strPart+=ch;
			}
		}
		
		// sort before prepending the improved match
		Collections.sort(astrPossibleMatches);
		
		// prepend improved partial match (or it can be simply the unmodified part...)
		astrPossibleMatches.add(0,strPart);
		
		return astrPossibleMatches;
	}
}
