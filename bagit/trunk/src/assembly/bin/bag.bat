@echo off
set MAXMEM=512m

REM DO NOT EDIT BELOW THIS LINE
shift
java -Xmx%MAXMEM% -classpath ..\lib\classworlds-1.1.jar -Dclassworlds.conf=bag.classworlds.conf -Dapp.home=%CD%\.. -Dlog_file=..\logs\bag.log org.codehaus.classworlds.Launcher %*
