#!/bin/bash

eval `secinit`

astrRunClasses=(`egrep "public static void main[(]" ./src/* -rIc |grep -v :0 |sed -r 's".*/src/(.*)[.]java:1$"\1"' |tr "/" "."`)&&:
declare -p astrRunClasses
#echoc --info "astrRunClasses='${astrRunClasses[@]}'"

for strRunClass in "${astrRunClasses[@]}";do
	if [[ -z "$strRunClass" ]];then echoc -p "strRunClass='$strRunClass'";exit 1;fi
	
	declare -p strRunClass
	
	anPid=(`pgrep -f "java .* $strRunClass"`)&&:
	if [[ -n "${anPid[@]-}" ]];then
		for nPid in "${anPid[@]}";do
			SECFUNCexecA -ce ps --no-headers -o pid,cmd -p $nPid
			
			if((${#anPid[@]}>1));then
				if ! echoc -q "kill $nPid?";then continue;fi
			fi
			
			SECFUNCexecA -ce kill -SIGKILL $nPid
		done
	else
		echoc --info "no pid found"
	fi
done

