#!/bin/sh
# -------------------------------------------------------------------------
# copy XDS components into DCM4CHEE archive installation
# -------------------------------------------------------------------------

DIRNAME=`dirname $0`
DCM4CHEE_HOME="$DIRNAME"/..
DCM4CHEE_SERV="$DCM4CHEE_HOME"/server/default

if [ x$1 = x ]; then
  echo "Usage: $0 <path-to-dcm4chee-xds-installation-directory>"
  exit 1
fi

XDS_HOME="$1"
XDS_SERV="$XDS_HOME"/server/default

if [ ! -f "$XDS_HOME"/bin/upgrade_jbossws.sh ]; then
  echo Could not locate dcm4chee-xds in "$XDS_HOME"
  exit 1
fi

cp -v -R "$XDS_SERV/deploy" "$DCM4CHEE_SERV"
cp -v -R "$XDS_SERV/conf" "$DCM4CHEE_SERV"
cp -v "$XDS_SERV/lib/dcm4chee-xdsa-repository-mbean-0.0.1.jar" \
  "$XDS_SERV/lib/dcm4chee-xds-docstore-spi-0.0.1.jar" \
  "$XDS_SERV/lib/dcm4chee-xds-common-0.0.1.jar" \
  "$XDS_SERV/lib/dcm4che-core-2.0.14.jar" \
  "$XDS_SERV/lib/dcm4che-net-2.0.14.jar" \
  "$DCM4CHEE_SERV/lib"
cp -v "$XDS_HOME/bin/upgrade_jbossws.sh" \
  "$XDS_HOME/bin/upgrade_jbossws.bat" \
  "$XDS_HOME/bin/patch_jbossws.sh" \
  "$XDS_HOME/bin/patch_jbossws.bat" \
  "$DCM4CHEE_HOME/bin"

echo XDS components installed!
