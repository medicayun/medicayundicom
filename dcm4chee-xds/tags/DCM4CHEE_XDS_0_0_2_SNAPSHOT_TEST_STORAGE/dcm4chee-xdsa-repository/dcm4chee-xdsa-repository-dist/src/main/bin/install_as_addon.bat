@echo off
rem -------------------------------------------------------------------------
rem copy XDS.a Repository components into DCM4CHEE installation
rem -------------------------------------------------------------------------

if "%OS%" == "Windows_NT"  setlocal
set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

set XDS_HOME=%DIRNAME%..
set XDS_SERV=%XDS_HOME%\server\default

if exist "%XDS_SERV%" goto found_xds
echo Could not locate %XDS_SERV%. 
echo Please check that you are in the
echo bin directory when running this script.
goto end

:found_xds
if not [%1] == [] goto found_arg1
echo "Usage: install_as_addon <path-to-dcm4chee-directory>"
goto end

:found_arg1
set DCM4CHEE_HOME=%1
set DCM4CHEE_SERV=%DCM4CHEE_HOME%\server\default

if exist "%DCM4CHEE_SERV%" goto found_dcm4chee
echo Could not locate DCM4CHEE in %DCM4CHEE_HOME%.
goto end

:found_dcm4chee
xcopy /S "%XDS_SERV%" "%DCM4CHEE_SERV%" 

if exist "%DCM4CHEE_SERV%/deploy/dcm4chee-xds-store2dcm*" goto found_store2dcm
echo Store2Dcm Service is not installed in this DCM4CHEE instance!
echo Please build/install current dcm4chee-xds-store2dcm service.
goto end

:found_store2dcm
echo XDS.a Repository installed!

:end
if "%OS%" == "Windows_NT" endlocal
