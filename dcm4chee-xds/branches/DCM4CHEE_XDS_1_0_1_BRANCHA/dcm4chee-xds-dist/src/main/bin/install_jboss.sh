#!/bin/sh
# -------------------------------------------------------------------------
# copy needed JBOSS components into DCM4CHEE-XDS installation
# -------------------------------------------------------------------------

DIRNAME=`dirname $0`
DCM4CHEE_HOME="$DIRNAME"/..
DCM4CHEE_SERV="$DCM4CHEE_HOME"/server/default
DCM4CHEE_EP_CONFIG="$DCM4CHEE_SERV"/deploy/jbossws.sar/META-INF/standard-jaxws-endpoint-config.xml

if [ x$1 = x ]; then
  echo "Usage: $0 <path-to-jboss-4.2.3.GA-installation-directory>"
  exit 1
fi

JBOSS_HOME="$1"
JBOSS_SERV="$JBOSS_HOME"/server/default

if [ ! -f "$JBOSS_HOME"/bin/run.jar ]; then
  echo Could not locate jboss-4.2.3.GA in "$JBOSS_HOME"
  exit 1
fi

cp -v "$JBOSS_HOME"/bin/run.jar \
  "$JBOSS_HOME"/bin/shutdown.bat \
  "$JBOSS_HOME"/bin/shutdown.jar \
  "$JBOSS_HOME"/bin/shutdown.sh \
  "$JBOSS_HOME"/bin/twiddle.bat \
  "$JBOSS_HOME"/bin/twiddle.jar \
  "$JBOSS_HOME"/bin/twiddle.sh \
  "$DCM4CHEE_HOME"/bin

mkdir "$DCM4CHEE_HOME"/client
cp -v "$JBOSS_HOME"/client/jbossall-client.jar \
  "$JBOSS_HOME"/client/getopt.jar \
  "$DCM4CHEE_HOME"/client

cp -v -R "$JBOSS_HOME"/lib "$DCM4CHEE_HOME"

cp -v "$JBOSS_SERV"/conf/jbossjta-properties.xml \
  "$JBOSS_SERV"/conf/jboss-service.xml \
  "$JBOSS_SERV"/conf/jndi.properties \
  "$JBOSS_SERV"/conf/standardjboss.xml \
  "$JBOSS_SERV"/conf/standardjbosscmp-jdbc.xml \
  "$DCM4CHEE_SERV"/conf
cp -v -R "$JBOSS_SERV"/conf/props \
  "$JBOSS_SERV"/conf/xmdesc \
  "$DCM4CHEE_SERV"/conf

mkdir "$DCM4CHEE_SERV"/lib
cp -v "$JBOSS_SERV"/lib/* "$DCM4CHEE_SERV"/lib

cp -v "$JBOSS_SERV"/deploy/bsh-deployer.xml \
  "$JBOSS_SERV"/deploy/cache-invalidation-service.xml \
  "$JBOSS_SERV"/deploy/client-deployer-service.xml \
  "$JBOSS_SERV"/deploy/ear-deployer.xml \
  "$JBOSS_SERV"/deploy/ejb-deployer.xml \
  "$JBOSS_SERV"/deploy/ejb3-interceptors-aop.xml \
  "$JBOSS_SERV"/deploy/jboss-ha-local-jdbc.rar \
  "$JBOSS_SERV"/deploy/jboss-ha-xa-jdbc.rar \
  "$JBOSS_SERV"/deploy/jbossjca-service.xml \
  "$JBOSS_SERV"/deploy/jboss-local-jdbc.rar \
  "$JBOSS_SERV"/deploy/jboss-xa-jdbc.rar \
  "$JBOSS_SERV"/deploy/jmx-invoker-service.xml \
  "$JBOSS_SERV"/deploy/jsr88-service.xml \
  "$JBOSS_SERV"/deploy/mail-service.xml \
  "$JBOSS_SERV"/deploy/monitoring-service.xml \
  "$JBOSS_SERV"/deploy/properties-service.xml \
  "$JBOSS_SERV"/deploy/quartz-ra.rar \
  "$JBOSS_SERV"/deploy/sqlexception-service.xml \
  "$JBOSS_SERV"/deploy/hsqldb-ds.xml \
  "$DCM4CHEE_SERV"/deploy

mv "$DCM4CHEE_EP_CONFIG" "$DCM4CHEE_EP_CONFIG".dcm4che
cp -v -R "$JBOSS_SERV"/deploy/ejb3.deployer \
  "$JBOSS_SERV"/deploy/http-invoker.sar \
  "$JBOSS_SERV"/deploy/jboss-aop-jdk50.deployer \
  "$JBOSS_SERV"/deploy/jboss-bean.deployer \
  "$JBOSS_SERV"/deploy/jbossws.sar \
  "$DCM4CHEE_SERV"/deploy
mv "$DCM4CHEE_EP_CONFIG" "$DCM4CHEE_EP_CONFIG".orig
mv "$DCM4CHEE_EP_CONFIG".dcm4che "$DCM4CHEE_EP_CONFIG"
mv "$DCM4CHEE_SERV"/deploy/jbossws.sar/jaxb-api.jar "$DCM4CHEE_SERV"/lib
mv "$DCM4CHEE_SERV"/deploy/jbossws.sar/jaxb-impl.jar "$DCM4CHEE_SERV"/lib

cp -v "$JBOSS_SERV"/deploy/jboss-web.deployer/context.xml \
  "$JBOSS_SERV"/deploy/jboss-web.deployer/jasper-jdt.jar \
  "$JBOSS_SERV"/deploy/jboss-web.deployer/jbossweb-extras.jar \
  "$JBOSS_SERV"/deploy/jboss-web.deployer/jbossweb.jar \
  "$JBOSS_SERV"/deploy/jboss-web.deployer/jbossweb-service.jar \
  "$JBOSS_SERV"/deploy/jboss-web.deployer/jstl.jar \
  "$DCM4CHEE_SERV"/deploy/jboss-web.deployer
  
cp -v -R "$JBOSS_SERV"/deploy/jboss-web.deployer/conf \
  "$JBOSS_SERV"/deploy/jboss-web.deployer/jsf-libs \
  "$JBOSS_SERV"/deploy/jboss-web.deployer/META-INF \
  "$JBOSS_SERV"/deploy/jboss-web.deployer/ROOT.war \
  "$DCM4CHEE_SERV"/deploy/jboss-web.deployer

cp -v "$JBOSS_SERV"/deploy/jms/jms-ds.xml \
  "$JBOSS_SERV"/deploy/jms/jms-ra.rar \
  "$JBOSS_SERV"/deploy/jms/jvm-il-service.xml \
  "$JBOSS_SERV"/deploy/jms/uil2-service.xml \
  "$JBOSS_SERV"/deploy/jms/hsqldb-jdbc2-service.xml \
  "$DCM4CHEE_SERV"/deploy/jms

cp -v "$JBOSS_SERV"/deploy/jmx-console.war/checkJNDI.jsp \
  "$JBOSS_SERV"/deploy/jmx-console.war/displayMBeans.jsp \
  "$JBOSS_SERV"/deploy/jmx-console.war/displayOpResult.jsp \
  "$JBOSS_SERV"/deploy/jmx-console.war/index.jsp \
  "$JBOSS_SERV"/deploy/jmx-console.war/jboss.css \
  "$JBOSS_SERV"/deploy/jmx-console.war/style_master.css \
  "$DCM4CHEE_SERV"/deploy/jmx-console.war
  
cp -v -R "$JBOSS_SERV"/deploy/jmx-console.war/cluster \
  "$JBOSS_SERV"/deploy/jmx-console.war/images \
  "$JBOSS_SERV"/deploy/jmx-console.war/META-INF \
  "$DCM4CHEE_SERV"/deploy/jmx-console.war
  
cp -v -R "$JBOSS_SERV"/deploy/jmx-console.war/WEB-INF/classes \
  "$DCM4CHEE_SERV"/deploy/jmx-console.war/WEB-INF
  
echo Install additional dcm4chee-docstore for standalone XDS distribution:
cp -v -R "$DCM4CHEE_HOME"/standalone/lib "$DCM4CHEE_SERV"
  
echo Move XDS.b Source and XDS Query services from deploy directory to 'extras'
mkdir "$DCM4CHEE_HOME"/extras
mv "$DCM4CHEE_SERV"/deploy/dcm4chee-xdsb-src-mbean-*.sar "$DCM4CHEE_HOME"/extras
mv "$DCM4CHEE_SERV"/deploy/dcm4chee-xds-consumer-query-*.sar "$DCM4CHEE_HOME"/extras
  