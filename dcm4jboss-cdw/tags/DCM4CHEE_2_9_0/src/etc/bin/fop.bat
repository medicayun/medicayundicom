@ECHO OFF

rem %~dp0 is the expanded pathname of the current script under NT
set BINDIR=.
if "%OS%"=="Windows_NT" set BINDIR=%~dp0
set LIBDIR=%BINDIR%\..\lib

set LOCALCLASSPATH=%LIBDIR%\fop.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\avalon-framework-cvs-20020806.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\batik.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\jimi-1.0.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\jai_core.jar
set LOCALCLASSPATH=%LOCALCLASSPATH%;%LIBDIR%\jai_codec.jar
java -cp "%LOCALCLASSPATH%" org.apache.fop.apps.Fop -c %BINDIR%\fopcfg.xml %1 %2 %3 %4 %5 %6 %7 %8
