@echo off
setlocal
set DIRNAME=%~dp0
set RUNJAR=%DIRNAME%\run.jar
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
echo   client    install dcm4chee service, using client hotspot vm
echo   server    install dcm4chee service, using server hotspot vm
echo   uninstall uninstall dcm4chee service
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
rem JVM memory allocation pool parameters. Modify as appropriate.
set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx512m

rem With Sun JVMs reduce the RMI GCs to once per hour
set JAVA_OPTS=%JAVA_OPTS% -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000

rem Set java.library.path to find native jai-imageio components 
set JAVA_OPTS=%JAVA_OPTS% -Djava.library.path=%DIRNAME%

rem Set app.name and app.pid used in emitted audit log messages
set JAVA_OPTS=%JAVA_OPTS% -Dapp.name=dcm4chee -Dapp.pid=%RANDOM%

JavaService.exe -install dcm4chee "%VM%" %JAVA_OPTS% "-Djava.class.path=%TOOLS_JAR%;%RUNJAR%"  -start org.jboss.Main -stop org.jboss.Main -method systemExit  -out "%DIRNAME%\out.txt" -err "%DIRNAME%\err.txt"
goto eof

:uninstall
JavaService.exe -uninstall dcm4chee
goto eof

:eof
endlocal
