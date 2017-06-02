#!/bin/sh
JBOSS_HOME=${JBOSS_HOME:-$HOME/jboss-4.0.4.GA}
JBOSS_LIB=$JBOSS_HOME/server/default/lib
JBOSS_EJB3=$JBOSS_HOME/server/default/deploy/ejb3.deployer
MVN="mvn install:install-file -DgeneratePom=true -Dpackaging=jar "
$MVN -Dfile=$JBOSS_LIB/jboss-j2ee.jar \
     -DgroupId=jboss \
     -DartifactId=jboss-j2ee \
     -Dversion=4.0.4.GA
$MVN -Dfile=$JBOSS_LIB/ejb3-persistence.jar \
     -DgroupId=jboss \
     -DartifactId=ejb3-persistence \
     -Dversion=4.0.4.GA  
$MVN -Dfile=$JBOSS_EJB3/jboss-ejb3.jar \
     -DgroupId=jboss \
     -DartifactId=jboss-ejb3 \
     -Dversion=4.0.4.GA
$MVN -Dfile=$JBOSS_EJB3/jboss-ejb3x.jar \
     -DgroupId=jboss \
     -DartifactId=jboss-ejb3x \
     -Dversion=4.0.4.GA
$MVN -Dfile=$JBOSS_EJB3/jboss-annotations-ejb3.jar \
     -DgroupId=jboss \
     -DartifactId=jboss-annotations-ejb3 \
     -Dversion=4.0.4.GA
$MVN -Dfile=$JBOSS_LIB/hibernate-annotations.jar \
     -DgroupId=hibernate \
     -DartifactId=hibernate-annotations \
     -Dversion=3.2.0.CR1
$MVN -Dfile=$JBOSS_LIB/hibernate-entitymanager.jar \
     -DgroupId=hibernate \
     -DartifactId=hibernate-entitymanager \
     -Dversion=3.2.0.CR1
$MVN -Dfile=$JBOSS_LIB/hibernate.jar \
     -DgroupId=hibernate \
     -DartifactId=hibernate \
     -Dversion=3.2.0.cr2
	       
