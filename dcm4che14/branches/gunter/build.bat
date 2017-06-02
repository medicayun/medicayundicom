@echo off
REM ### ================================================================== ###
REM ##                                                                      ##
REM ## Copyright (c) 1998-2000 by Jason Dillon <jason@planet57.com> and     ##
REM ##               by Sacha Labourey <sacha.labourey@cogito-info.ch>      ##
REM ## Copyright (c) 2001 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com> ##
REM ##                                                                      ##
REM ## This file is part of dcm4che.                                        ##
REM ##                                                                      ##
REM ## This library is free software; you can redistribute it and/or modify ##
REM ## it under the terms of the GNU Lesser General Public License as       ##
REM ## published by the Free Software Foundation; either version 2 of the   ##
REM ## License, or (at your option) any later version.                      ##
REM ##                                                                      ##
REM ## This library is distributed in the hope that it will be useful, but  ##
REM ## WITHOUT ANY WARRANTY; without even the implied warranty of           ##
REM ## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    ##
REM ## Lesser General Public License for more details.                      ##
REM ##                                                                      ##
REM ### ================================================================== ###
REM ##                                                                      ##
REM ## This is the main entry point for the build system.                   ##
REM ## Users should be sure to execute this file rather than 'ant' to       ##
REM ## ensure the correct version is being used with the correct            ##
REM ## configuration.                                                       ##
REM ##                                                                      ##
REM ### ================================================================== ###

REM $Id: build.bat 3451 2002-04-07 20:39:37Z gunterze $

REM ******************************************************
REM Ignore the ANT_HOME variable: we want to use *our*
REM ANT version and associated JARs.
REM ******************************************************
REM Ignore the users classpath, cause it might mess
REM things up
REM ******************************************************

SETLOCAL

set CLASSPATH=
set ANT_HOME=
set JAXP_DOM_FACTORY=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
set JAXP_SAX_FACTORY=org.apache.crimson.jaxp.SAXParserFactoryImpl
REM set JAXP_DOM_FACTORY=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl
REM set JAXP_SAX_FACTORY=org.apache.xerces.jaxp.SAXParserFactoryImpl

set ANT_OPTS=-Djava.protocol.handler.pkgs=planet57.net.protocol -Djavax.xml.parsers.DocumentBuilderFactory=%JAXP_DOM_FACTORY% -Djavax.xml.parsers.SAXParserFactory=%JAXP_SAX_FACTORY% -Dbuild.script=build.bat

REM ******************************************************
REM - "for" loops have been unrolled for compatibility
REM   with some WIN32 systems.
REM ******************************************************

set NAMES=tools;tools\ant;tools\apache\ant
set SUBFOLDERS=..;..\..;..\..\..;..\..\..\..

REM ******************************************************
REM ******************************************************

SET EXECUTED=FALSE
for %%i in (%NAMES%) do call :subLoop %%i %1 %2 %3 %4 %5 %6

goto :EOF


REM ******************************************************
REM ********* Search for names in the subfolders *********
REM ******************************************************

:subLoop
for %%j in (%SUBFOLDERS%) do call :testIfExists %%j\%1\bin\ant.bat %2 %3 %4 %5 %6 %7

goto :EOF


REM ******************************************************
REM ************ Test if ANT Batch file exists ***********
REM ******************************************************

:testIfExists
if exist %1 call :BatchFound %1 %2 %3 %4 %5 %6 %7 %8

goto :EOF


REM ******************************************************
REM ************** Batch file has been found *************
REM ******************************************************

:BatchFound
if (%EXECUTED%)==(FALSE) call :ExecuteBatch %1 %2 %3 %4 %5 %6 %7 %8
set EXECUTED=TRUE

goto :EOF

REM ******************************************************
REM ************* Execute Batch file only once ***********
REM ******************************************************

:ExecuteBatch
echo Calling %1 %2 %3 %4 %5 %6 %7 %8
call %1 %2 %3 %4 %5 %6 %7 %8

:end

pause
