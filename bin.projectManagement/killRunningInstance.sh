#!/bin/bash

eval `secinit`

astrRunClasses=(`egrep "public static void main[(]" ./src/* -rIc |grep -v :0 |sed -r 's".*/src/(.*)[.]java:1$"\1"' |tr "/" "."`)&&:
declare -p astrRunClasses
#echoc --info "astrRunClasses='${astrRunClasses[@]}'"

for strRunClass in "${astrRunClasses[@]}";do
	if [[ -z "$strRunClass" ]];then echoc -p "strRunClass='$strRunClass'";exit 1;fi
	
	declare -p strRunClass
	
	nPid="`pgrep -f "java .* $strRunClass"`"&&:
	if [[ -n "$nPid" ]];then
		SECFUNCexecA -ce ps -o pid,cmd -p $nPid

		if echoc -q "kill $nPid?";then
			SECFUNCexecA -ce kill -SIGKILL $nPid
		fi
	else
		echoc --info "no pid found"
	fi
done

