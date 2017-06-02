@echo off
setlocal

set DCM4CHEE_HOME=%1
set DCM4CHEE_SERV=%DCM4CHEE_HOME%\server\default

if not [%1] == [] goto found_arg1
echo "Usage: install_xero <path-to-dcm4chee-installation-dir>"
goto end
:found_arg1

if exist "%DCM4CHEE_SERV%" goto found_dcm4chee
echo Could not locate %DCM4CHEE_SERV%.  Please check that you provided
echo the path to your DCM4CHEE server.
goto end

:found_dcm4chee

erase %DCM4CHEE_SERV%\lib\jaxb*
copy xero.war %DCM4CHEE_SERV%\deploy /Y
copy wado2.war %DCM4CHEE_SERV%\deploy /Y

echo About to update your server.xml, default web service and log configuration.
echo Hit break if you do NOT want to do this.
pause
echo Updating server.xml in jboss-web.deployer.
copy server.xml %DCM4CHEE_SERV%\deploy\jboss-web.deployer\server.xml /Y
copy index.html default.htm /Y
copy jboss-log4j.xml %DCM4CHEE_SERV%\conf /Y
%JAVA_HOME%\bin\jar uvf %DCM4CHEE_SERV%\deploy\dcm4chee-wado.war index.html default.htm


