@echo off
rem -------------------------------------------------------------------------
rem copy XDS components into DCM4CHEE Archive installation
rem -------------------------------------------------------------------------

if "%OS%" == "Windows_NT"  setlocal
set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

set DCM4CHEE_HOME=%DIRNAME%..
set DCM4CHEE_SERV=%DCM4CHEE_HOME%\server\default

if exist "%DCM4CHEE_SERV%" goto found_dcm4chee
echo Could not locate %DCM4CHEE_SERV%. 
echo Please check that you are in the
echo bin directory when running this script.
goto end

:found_dcm4chee
if not [%1] == [] goto found_arg1
echo "Usage: install_xds <path-to-dcm4chee-xds-installation-directory>"
goto end

:found_arg1
set XDS_HOME=%1
set XDS_SERV=%XDS_HOME%\server\default

if exist "%XDS_SERV%" goto found_xds
echo Could not locate dcm4chee-xds in %XDS_SERV%.
goto end

:found_xds
xcopy /S "%XDS_SERV%\deploy\dcm4che*" "%DCM4CHEE_SERV%\deploy" 
copy "%XDS_SERV%\conf\xmdesc\dcm4chee-xds-xmbean.xml" "%DCM4CHEE_SERV%\conf\xmdesc"
copy "%XDS_SERV%"\lib\dcm4chee-xdsa-repository-mbean-0.0.2-SNAPSHOT.jar "%DCM4CHEE_SERV%\lib"
copy "%XDS_SERV%"\lib\dcm4chee-xds-common-0.0.2-SNAPSHOT.jar "%DCM4CHEE_SERV%\lib"
copy "%XDS_SERV%\lib\dcm4che-core-2.0.14.jar" "%DCM4CHEE_SERV%\lib"
copy "%XDS_SERV%\lib\dcm4che-net-2.0.14.jar" "%DCM4CHEE_SERV%\lib"
copy "%XDS_HOME%\bin\upgrade_jbossws.sh" "%DCM4CHEE_HOME%\bin"
copy "%XDS_HOME%\bin\upgrade_jbossws.bat" "%DCM4CHEE_HOME%\bin"
copy "%XDS_HOME%\bin\patch_jbossws.sh" "%DCM4CHEE_HOME%\bin"
copy "%XDS_HOME%\bin\patch_jbossws.bat" "%DCM4CHEE_HOME%\bin"

echo XDS components installed!

:end
if "%OS%" == "Windows_NT" endlocal
