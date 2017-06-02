#!/bin/sh
# -------------------------------------------------------------------------
# Patch JBOSS WS components for Axis2 client support and 'no internet access'
# -------------------------------------------------------------------------

DIRNAME=`dirname $0`
DCM4CHEE_HOME="$DIRNAME"/..
DCM4CHEE_SERV="$DCM4CHEE_HOME"/server/default
if [ x$1 = x ]; then
  XDS_HOME="$DCM4CHEE_HOME"
else
  XDS_HOME="$1"
fi

XDS_PATCHES_JBOSSWS="$XDS_HOME"/patches/jbossws-3.0.1-native-2.0.4.GA

if [ ! -d "$XDS_PATCHES_JBOSSWS" ]; then
  echo "Usage: $0 <path-to-dcm4chee-xds-installation-directory>"
  exit 1
fi

cp -v -R "$XDS_PATCHES_JBOSSWS/deploy" "$DCM4CHEE_SERV"

echo JBoss WS components patched!
