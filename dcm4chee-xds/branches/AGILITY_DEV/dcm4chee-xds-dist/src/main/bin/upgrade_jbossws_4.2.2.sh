#!/bin/sh
# ----------------------------------------------------------------------------------------
# Update JBOSS WS components of DCM4CHEE-XDS installation to JBossWS 3.0.1-native-2.0.4.GA
# ----------------------------------------------------------------------------------------

DIRNAME=`dirname $0`
DCM4CHEE_HOME="$DIRNAME"/..
DCM4CHEE_SERV="$DCM4CHEE_HOME"/server/default

if [ x$1 = x ]; then
  echo "Usage: $0 <path-to-jbossws-3.0.1-native-2.0.4.GA-installation-directory>"
  exit 1
fi

JBOSS_WS_HOME="$1"
JBOSS_WS_DEPLOY="$JBOSS_WS_HOME"/deploy

if [ ! -f "$JBOSS_WS_DEPLOY"/bin/wstools.sh ]; then
  echo Could not locate 3.0.1-native-2.0.4.GA in "$JBOSS_WS_HOME"
  exit 1
fi

JBOSS_WS_LIB="$JBOSS_WS_DEPLOY/lib"
DCM4CHEE_CLIENT="$DCM4CHEE_HOME/client"
DCM4CHEE_LIB="$DCM4CHEE_SERV/lib"
DCM4CHEE_JBOSSWS="$DCM4CHEE_SERV/deploy/jbossws.sar"

rm "$DCM4CHEE_JBOSSWS/jbossws-deploy.conf"

cp -v "$JBOSS_WS_DEPLOY"/bin/wsconsume.bat \
  "$JBOSS_WS_DEPLOY"/bin/wsconsume.sh \
  "$JBOSS_WS_DEPLOY"/bin/wsprovide.bat \
  "$JBOSS_WS_DEPLOY"/bin/wsprovide.sh \
  "$JBOSS_WS_DEPLOY"/bin/wsrunclient.bat \
  "$JBOSS_WS_DEPLOY"/bin/wsrunclient.sh \
  "$JBOSS_WS_DEPLOY"/bin/wstools.bat \
  "$JBOSS_WS_DEPLOY"/bin/wstools.sh \
  "$DCM4CHEE_HOME"/bin

cp -v "$JBOSS_WS_LIB"/FastInfoset.jar \
  "$JBOSS_WS_LIB"/jaxb-api.jar \
  "$JBOSS_WS_LIB"/jaxb-impl.jar \
  "$JBOSS_WS_LIB"/jaxb-xjc.jar \
  "$JBOSS_WS_LIB"/jaxws-rt.jar \
  "$JBOSS_WS_LIB"/jaxws-tools.jar \
  "$JBOSS_WS_LIB"/jbossws-common.jar \
  "$JBOSS_WS_LIB"/jbossws-framework.jar \
  "$JBOSS_WS_LIB"/jbossws-client.jar \
  "$JBOSS_WS_LIB"/jboss-jaxrpc.jar \
  "$JBOSS_WS_LIB"/jboss-jaxws.jar \
  "$JBOSS_WS_LIB"/jboss-jaxws-ext.jar \
  "$JBOSS_WS_LIB"/jboss-saaj.jar \
  "$JBOSS_WS_LIB"/jbossws-spi.jar \
  "$JBOSS_WS_LIB"/jettison.jar \
  "$JBOSS_WS_LIB"/policy.jar \
  "$JBOSS_WS_LIB"/stax-api.jar \
  "$JBOSS_WS_LIB"/stax-ex.jar \
  "$JBOSS_WS_LIB"/streambuffer.jar \
  "$JBOSS_WS_LIB"/wsdl4j.jar \
  "$JBOSS_WS_LIB"/wstx.jar \
  "$DCM4CHEE_CLIENT"

# We need jaxb in DCM4CHEE_LIB instead of DCM4CHEE_JBOSSWS for XDS.a implementation which is not using web service stack!

cp -v "$JBOSS_WS_LIB"/jaxb-api.jar \
  "$JBOSS_WS_LIB"/jaxb-impl.jar \
  "$JBOSS_WS_LIB"/jbossws-common.jar \
  "$JBOSS_WS_LIB"/jbossws-framework.jar \
  "$JBOSS_WS_LIB"/jboss-jaxrpc.jar \
  "$JBOSS_WS_LIB"/jboss-jaxws.jar \
  "$JBOSS_WS_LIB"/jboss-jaxws-ext.jar \
  "$JBOSS_WS_LIB"/jboss-saaj.jar \
  "$JBOSS_WS_LIB"/jbossws-spi.jar \
 "$DCM4CHEE_LIB"

cp -v "$JBOSS_WS_LIB"/jboss-jaxb-intros.jar \
  "$JBOSS_WS_LIB"/jboss-jaxrpc.jar \
  "$JBOSS_WS_LIB"/jboss-jaxws.jar \
  "$JBOSS_WS_LIB"/jboss-saaj.jar \
  "$JBOSS_WS_LIB"/jbossws-core.jar \
  "$JBOSS_WS_LIB"/policy.jar \
  "$JBOSS_WS_LIB"/stax-api.jar \
  "$JBOSS_WS_LIB"/wsdl4j.jar \
  "$JBOSS_WS_LIB"/wstx.jar \
  "$JBOSS_WS_LIB"/xmlsec.jar \
 "$DCM4CHEE_JBOSSWS"

cp -v "$JBOSS_WS_HOME/build/jbossws-default-deploy.conf" "$DCM4CHEE_JBOSSWS/jbossws-deploy.conf"

cp -v "$JBOSS_WS_DEPLOY/resources/jbossws-jboss42.sar/META-INF/jboss-service.xml" "$DCM4CHEE_JBOSSWS/META-INF/jboss-service.xml"

cp -v "$JBOSS_WS_DEPLOY/resources/standard-jaxws-client-config.xml" "$DCM4CHEE_JBOSSWS/META-INF"
