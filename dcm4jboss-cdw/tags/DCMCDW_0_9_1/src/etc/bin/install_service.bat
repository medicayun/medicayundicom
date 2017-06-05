@echo off
setlocal
rem Set maximum size of the memory allocation pool
rem default value of 64MB may not be sufficient for dcmcdw
set JAVA_OPTS=-Xmx100m
set DIRNAME=%~dp0%
set RUNJAR=%DIRNAME%run.jar
if exist "%RUNJAR%" goto found_runjar
echo Could not locate %RUNJAR%. Please check that you are in the
echo bin directory when running this script.
goto eof

:found_runjar
if "%1" == "uninstall" goto uninstall
if "%1" == "server" goto install
if "%1" == "client" goto install
echo "Usage: %0 server|client|uninstall"
echo Options:
echo   client    install "DICOM CD Writer" service, using client hotspot vm
echo   server    install "DICOM CD Writer" service, using server hotspot vm
echo   uninstall uninstall "DICOM CD Writer" service
goto eof

:install
if not "%JAVA_HOME%" == "" goto found_javahome
echo set JAVA_HOME to your JDK 1.4 installation directory
goto eof

:found_javahome
set VM=%JAVA_HOME%\bin\%1\jvm.dll
if exist "%VM%" goto found_vm
set VM=%JAVA_HOME%\jre\bin\%1\jvm.dll
if exist "%VM%" goto found_vm
echo Could not locate %VM%. Please check that JAVA_HOME is set to your
echo JDK 1.4 installation directory
goto eof

:found_vm
set TOOLS_JAR=%JAVA_HOME%\lib\tools.jar
if exist "%TOOLS_JAR%" goto install
echo Could not locate %TOOLS_JAR%. Unexpected results may occur.
echo Make sure that JAVA_HOME points to a JDK and not a JRE.

:install
JavaService.exe -install "DICOM CD Writer" "%VM%" %JAVA_OPTS% -Djava.class.path=%TOOLS_JAR%;%RUNJAR%  -start org.jboss.Main -params -c dcmcdw -stop org.jboss.Main -method systemExit -current %DIRNAME%
goto eof

:uninstall
JavaService.exe -uninstall "DICOM CD Writer"
goto eof

:eof
endlocal
