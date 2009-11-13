@echo off
set MAXMEM=512m
set APP_HOME=%CD:\bin=%

set START=%CD%
set APP_HOME=%0%
set APP_HOME=%APP_HOME:~0,-4%
cd "%APP_HOME%"
cd ..
set APP_HOME=%CD%
cd "%START%

REM DO NOT EDIT BELOW THIS LINE
shift
java -Xmx%MAXMEM% -classpath "%APP_HOME%\lib\classworlds-1.1.jar" -Dclassworlds.conf="%APP_HOME%\bin\bag.classworlds.conf" -Dapp.home="%APP_HOME%" -Dlog_file="%APP_HOME%\logs\bag.log" org.codehaus.classworlds.Launcher %*