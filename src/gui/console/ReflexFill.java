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

package gui.console;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 
 * @author AquariusPower <https://github.com/AquariusPower>
 *
 */
public class ReflexFill {
	public static interface IReflexFillCfg{
		public ReflexFillCfg getReflexFillCfg(IReflexFillCfgVariant rfcv);
	}
	
	/**
	 * for the same class, there may have user preferred prefix/suffix
	 */
	public static interface IReflexFillCfgVariant{
		public int getReflexFillCfgVariant();
	}
	
	public static class ReflexFillCfg{
		/**
		 * to be removed from the final identifier string
		 */
		String	strCodingStyleFieldNamePrefix="";
		
		String	strCommandPrefix="";
		
		String	strCommandSuffix="";
		
		boolean bFirstLetterUpperCase = false;
	}
	
	private static ReflexFill i = new ReflexFill();
	
	/**
	 * 
	 * @return instance
	 */
	public static ReflexFill i(){
		return i;
	}
	
	/**
	 * based on reflected variable name
	 * @param rfcfgOwner 
	 * @param strCodingStyleFieldNamePrefix 
	 * @param strCommandPrefix 
	 * @param strCommandSuffix 
	 * @return
	 */
	protected String createIdentifierWithFieldName(IReflexFillCfg rfcfgOwner, IReflexFillCfgVariant rfcv){
		ReflexFillCfg rfcfg = rfcfgOwner.getReflexFillCfg(rfcv);
		Class<?> cl = rfcfgOwner.getClass();
		while(true){
			for(Field fld:cl.getDeclaredFields()){
				try {
					boolean bWasAccessible = fld.isAccessible();
					if(!bWasAccessible)fld.setAccessible(true);
					if(fld.get(rfcfgOwner)==rfcv){
						String strCommand=fld.getName();
						if(strCommand.startsWith(rfcfg.strCodingStyleFieldNamePrefix)){
							//remove prefix
							strCommand=strCommand.substring(rfcfg.strCodingStyleFieldNamePrefix.length());
							
							if(Modifier.isFinal(fld.getModifiers())){
								/**
								 * upper case with underscores
								 */
								String strCmdNew = null;
								for(String strWord : strCommand.split("_")){
									if(strCmdNew==null){
										if(rfcfg.bFirstLetterUpperCase){
											strCmdNew=firstLetter(strWord.toLowerCase(),true);
										}else{
											strCmdNew=strWord.toLowerCase();
										}
									}else{
										strCmdNew+=firstLetter(strWord.toLowerCase(),true);
									}
								}
								strCommand=strCmdNew;
							}else{
								/**
								 * already nice to read field name
								 * lower case 1st char
								 */
								strCommand=firstLetter(strCommand,false);
							}
						}
						strCommand=rfcfg.strCommandPrefix+strCommand+rfcfg.strCommandSuffix;
						return strCommand;
					}
					if(!bWasAccessible)fld.setAccessible(false);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			cl=cl.getSuperclass();
			if(cl.getName().equals(Object.class.getName()))break;
		}
		
		throw new NullPointerException("failed to automatically set command id for: "+this);
	}
	
	/**
	 * 
	 * @param str
	 * @param bCapitalize if false will lower case
	 * @return
	 */
	public String firstLetter(String str, boolean bCapitalize){
		if(bCapitalize){
			return Character.toUpperCase(str.charAt(0))
				+str.substring(1);
		}else{
			return Character.toLowerCase(str.charAt(0))
				+str.substring(1);
		}
	}
	
}
