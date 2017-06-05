@ECHO OFF
rem Batch file to install dcm4che/cdw Media Creation Server

rem %~dp0 is the expanded pathname of the current script under NT
set BINDIR=.
if "%OS%"=="Windows_NT" set BINDIR=%~dp0

set DCMCDW=%BINDIR%\..
set DEFAULT=%DCMCDW%\..\default

copy %DEFAULT%\deploy\hsqldb-ds.xml %DCMCDW%\deploy
copy %DEFAULT%\deploy\jboss-jca.sar %DCMCDW%\deploy
copy %DEFAULT%\deploy\jboss-local-jdbc.rar %DCMCDW%\deploy
copy %DEFAULT%\deploy\transaction-service.xml %DCMCDW%\deploy
xcopy /E %DEFAULT%\deploy\jbossweb-tomcat50.sar %DCMCDW%\deploy\jbossweb-tomcat50.sar\
xcopy /E %DEFAULT%\deploy\jmx-invoker-adaptor-server.sar %DCMCDW%\deploy\jmx-invoker-adaptor-server.sar\
xcopy /E %DEFAULT%\deploy\jmx-console.war %DCMCDW%\deploy\jmx-console.war\

copy %DEFAULT%\deploy\jms\hsqldb-jdbc2-service.xml %DCMCDW%\deploy\jms
copy %DEFAULT%\deploy\jms\jvm-il-service.xml %DCMCDW%\deploy\jms
  
copy %DEFAULT%\conf\jbossmq-state.xml %DCMCDW%\conf
copy %DEFAULT%\conf\jndi.properties %DCMCDW%\conf
copy %DEFAULT%\conf\login-config.xml %DCMCDW%\conf
copy %DEFAULT%\conf\server.policy %DCMCDW%\conf
xcopy /E %DEFAULT%\conf\xmdesc %DCMCDW%\conf\xmdesc\
xcopy /E %DEFAULT%\lib %DCMCDW%\lib\
