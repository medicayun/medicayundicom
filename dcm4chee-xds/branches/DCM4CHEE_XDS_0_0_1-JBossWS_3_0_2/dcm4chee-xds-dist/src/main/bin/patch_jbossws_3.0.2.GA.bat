@echo off
rem -------------------------------------------------------------------------
rem Patch JBOSS WS components for Axis2 client support and 'no internet access'
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
set XDS_HOME=%DCM4CHEE_HOME%
goto :test_patches

:found_arg1
set XDS_HOME=%1

:test_patches
set XDS_PATCHES_JBOSSWS=%XDS_HOME%\patches\jbossws-native-3.0.2.GA

if exist "%XDS_PATCHES_JBOSSWS%" goto found_patches
echo "%XDS_PATCHES_JBOSSWS% doesn't exist!"
echo "Usage: patch_jbossws <path-to-dcm4chee-xds-installation-directory>"
goto end

:found_patches
set DCM4CHEE_JBOSSWS=%DCM4CHEE_SERV%\deploy\jbossws.sar

xcopy /S "%XDS_PATCHES_JBOSSWS%" "%DCM4CHEE_SERV%"

echo JBoss WS components patched!

:end
if "%OS%" == "Windows_NT" endlocal
