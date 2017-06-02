#!/bin/sh
# -------------------------------------------------------------------------
# copy Store2Dcm Service components into DCM4CHEE installation
# -------------------------------------------------------------------------

DIRNAME=`dirname $0`
STORE_HOME="$DIRNAME"/..
STORE_SERV="$STORE_HOME"/server/default

if [ x$1 = x ]; then
  echo "Usage: $0 <path-to-dcm4chee-installation-directory>"
  exit 1
fi

DCM4CHEE_HOME="$1"
DCM4CHEE_SERV="$DCM4CHEE_HOME"/server/default

if [ ! -f "$DCM4CHEE_HOME"/bin/run.jar ]; then
  echo Could not locate DCM4CHEE in "$DCM4CHEE_HOME"
  exit 1
fi

cp -v -R "$STORE_SERV" "$DCM4CHEE_SERV"

echo Store2Dcm Service installed!
