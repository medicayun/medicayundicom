#!/bin/sh
#
# Shell script to install dcm4che/cdw Media Creation Server

DCMCDW=`dirname $0`/..
DEFAULT=$DCMCDW/../default

cp -R \
  $DEFAULT/deploy/hsqldb-ds.xml \
  $DEFAULT/deploy/jboss-jca.sar \
  $DEFAULT/deploy/jboss-local-jdbc.rar \
  $DEFAULT/deploy/jbossweb-tomcat50.sar \
  $DEFAULT/deploy/jmx-invoker-adaptor-server.sar \
  $DEFAULT/deploy/jmx-console.war \
  $DEFAULT/deploy/transaction-service.xml \
  $DCMCDW/deploy

cp \
  $DEFAULT/deploy/jms/hsqldb-jdbc2-service.xml \
  $DEFAULT/deploy/jms/jvm-il-service.xml \
  $DCMCDW/deploy/jms/
  
cp -R \
  $DEFAULT/conf/jbossmq-state.xml \
  $DEFAULT/conf/jndi.properties \
  $DEFAULT/conf/login-config.xml \
  $DEFAULT/conf/server.policy \
  $DEFAULT/conf/xmdesc \
  $DCMCDW/conf
  
cp -R $DEFAULT/lib $DCMCDW/
