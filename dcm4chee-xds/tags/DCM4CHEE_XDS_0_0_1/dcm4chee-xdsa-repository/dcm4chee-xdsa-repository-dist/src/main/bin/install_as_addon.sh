#!/bin/sh
# -------------------------------------------------------------------------
# copy XDS.a Repository components into DCM4CHEE installation
# -------------------------------------------------------------------------

DIRNAME=`dirname $0`
XDS_HOME="$DIRNAME"/..
XDS_SERV="$XDS_HOME"/server/default

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

cp -v -R "$XDS_SERV" "$DCM4CHEE_SERV"

if [ ! -f "$DCM4CHEE_SERV"/deploy/dcm4chee-xds-store2dcm*.sar ]; then
  echo Store2Dcm Service is not installed in this DCM4CHEE instance!
  echo Please build/install current dcm4chee-xds-store2dcm service.
  exit 0
fi

echo XDS.a Repository installed!
