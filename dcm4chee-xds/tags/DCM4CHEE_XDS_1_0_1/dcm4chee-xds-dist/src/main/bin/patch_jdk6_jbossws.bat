@echo off
rem -------------------------------------------------------------------------
rem Patch JBOSS WS for JDK6 
rem -------------------------------------------------------------------------

if "%OS%" == "Windows_NT"  setlocal
set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

set DCM4CHEE_HOME=%DIRNAME%..
set DCM4CHEE_LIB=%DCM4CHEE_HOME%\server\default\lib

if exist "%DCM4CHEE_LIB%" goto found_dcm4chee
echo Could not locate %DCM4CHEE_LIB%. 
echo Please check that you are in the
echo bin directory when running this script.
goto end

:found_dcm4chee

set DCM4CHEE_ENDORSED=%DCM4CHEE_HOME%\lib\endorsed

copy "%DCM4CHEE_LIB%\jaxb-api.jar" "%DCM4CHEE_ENDORSED%"
copy "%DCM4CHEE_LIB%\jboss-jaxrpc.jar" "%DCM4CHEE_ENDORSED%"
copy "%DCM4CHEE_LIB%\jboss-jaxws.jar" "%DCM4CHEE_ENDORSED%"
copy "%DCM4CHEE_LIB%\jboss-jaxws-ext.jar" "%DCM4CHEE_ENDORSED%"
copy "%DCM4CHEE_LIB%\jboss-saaj.jar" "%DCM4CHEE_ENDORSED%"

echo JBoss WS patched for JDK6!

:end
if "%OS%" == "Windows_NT" endlocal
