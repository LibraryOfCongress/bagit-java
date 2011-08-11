@echo off
set MAXMEM=1024m
set APP_HOME=%CD:\bin=%

set START=%CD%
set APP_HOME=%0%
set APP_HOME=%APP_HOME:~0,-4%
cd "%APP_HOME%"
cd ..
set APP_HOME=%CD%
cd "%START%

for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set d=%%c%%a%%b)
for /f "tokens=1,2 delims=. " %%a in ("%time%") do set t=%%a
set t=%t::=_%

shift
java -Xmx%MAXMEM% -classpath "%APP_HOME%\lib\classworlds-1.1.jar" -Dclassworlds.conf="%APP_HOME%\bin\bag.classworlds.conf" -Dapp.home="%APP_HOME%" -Dlog.timestamp="%d%-%t%" -Dversion="${pom.version}" org.codehaus.classworlds.Launcher %*