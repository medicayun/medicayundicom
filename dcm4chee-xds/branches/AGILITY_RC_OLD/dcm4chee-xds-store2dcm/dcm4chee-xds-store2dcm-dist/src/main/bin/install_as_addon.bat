@echo off
rem -------------------------------------------------------------------------
rem copy Store2Dcm Service components into DCM4CHEE installation
rem -------------------------------------------------------------------------

if "%OS%" == "Windows_NT"  setlocal
set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%

set STORE_HOME=%DIRNAME%..
set STORE_SERV=%STORE_HOME%\server\default

if exist "%STORE_SERV%" goto found_store
echo Could not locate %STORE_SERV%. 
echo Please check that you are in the
echo bin directory when running this script.
goto end

:found_store
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
xcopy /S "%STORE_SERV%" "%DCM4CHEE_SERV%" 
echo Store2Dcm Service installed!

:end
if "%OS%" == "Windows_NT" endlocal
