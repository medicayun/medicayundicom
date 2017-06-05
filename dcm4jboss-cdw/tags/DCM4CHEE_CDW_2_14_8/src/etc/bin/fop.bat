@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
set PROGNAME=run.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0%

pushd %DIRNAME%..
set JBOSS_HOME=%CD%
popd

rem Setup the java endorsed dirs
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

set LIBDIR=%JBOSS_HOME%\server\default\lib

set LOCALCLASSPATH=%LIBDIR%\fop.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\avalon-framework-cvs-20020806.jar

java "-Djava.endorsed.dirs=%JBOSS_ENDORSED_DIRS%" ^
     --classpath "%LOCALCLASSPATH%" org.apache.fop.apps.Fop ^
     -c %JBOSS_HOME%\bin\fopcfg.xml %*
