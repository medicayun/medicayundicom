#!/bin/sh
# --------------------------------------------------------
# copy WADO2 components into DCM4CHEE Archive installation
# --------------------------------------------------------

DIRNAME=`dirname $0`
DCM4CHEE_HOME="$DIRNAME"/..
DCM4CHEE_SERV="$DCM4CHEE_HOME"/server/default

if [ x$1 = x ]; then
  echo "Usage: $0 <path-to-dcm4chee-xero-installation-directory>"
  exit 1
fi

XER_HOME="$1"
XER_SERV="$XER_HOME"/server/default

if [ ! -f "$XER_SERV"/deploy/wado2.war ]; then
  echo Could not locate dcm4chee-xero in "$XER_HOME"
  exit 1
fi

cp -v "$XER_SERV"/conf/ae-local.properties "$DCM4CHEE_SERV"/conf/
cp -v "$XER_SERV"/deploy/wado2.war "$DCM4CHEE_SERV"/deploy/
