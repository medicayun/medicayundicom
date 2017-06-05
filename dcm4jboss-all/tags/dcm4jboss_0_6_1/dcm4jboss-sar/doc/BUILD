BUILD INSTRUCTIONS FOR DCM4JBOSS-SAR:
====================================

In order to build dcm4jboss-web you have to download and install:

- Ant 1.6.x (from http://ant.apache.org)

- JUnit 3.8.x (from http://junit.org)

- Jakarta Cactus 1.5 for J2EE 1.3 [http://jakarta.apache.org/cactus/]
(needed to build dcm4jboss-ejb, which itself required to build dcm4jboss-sar)

- XDoclet 1.2 [http://xdoclet.sourceforge.net]

- JBoss 3.2.3 [http://jboss.org]

- Maverick 2.2.0 + Domify 2.0.2 [http://mav.sourceforge.net]

Create an environment variable pointing to the Ant home directory (ANT_HOME)
and add the $ANT_HOME/bin directory to your PATH.

Copy junit.jar from the JUnit package into $ANT_HOME/lib/.

You need to build the dcm4che core library and the dcm4jboss-ejb 
components from the source, before you can build dcm4jboss-web.

At first time, connecting to the project CVS repository, you have to login by

>cvs -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che login
CVS password:<enter>

== Building the dcm4che core library ==

Download the source of the dcm4che core library by

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4che14 

and build the library jar, by running

>ant jar

with the dcm4che14 distribution directory as working directory.

== Building the EJB and the JDBC Data Access components ==

Download the source of the dcm4jboss-ejb package by

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4jboss-ejb

The default build script (build.xml) assumes following locations of
components required for building dcm4jboss-ejb:

<!-- Override with your dcm4che dist location -->
<property name="dcm4che.home" value="../dcm4che14/build"/>		

<!-- Override with your XDoclet bundle dist location -->
<property name="xdoclet.home" value="${user.home}/xdoclet-1.2"/>

<!-- Override with your Cactus bundle dist location -->
<property name="cactus.home" value="${user.home}/jakarta-cactus-13-1.5"/>

<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.home" value="${user.home}/jboss-3.2.3"/>		

Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.

After that, invoking

>ant jar

with the dcm4jboss-ejb distribution directory as working directory
builds database specific ejb-jars and jars needed for EJB clients and
for direct DB access via JDBC.

== Building the Web Application Archive ==

The source of the dcm4jboss-web package is available in module dcm4jboss-web
in the dcm4che project CVS repository:

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4jboss-web

The default build script (build.xml) assumes following locations of
components required for building dcm4jboss-web:

<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.dir" value="${user.home}/jboss-3.2.3" />

<!-- Override with your Maverick dist location -->
<property name="maverick.dir" value="../maverick" />

<!-- Override with your Domify dist location -->
<property name="opt-domify.dir" value="../opt-domify" />

<!-- Override with your dcm4jboss-ejb working directory -->
<property name="dcm4jboss-ejb.dir" value="../dcm4jboss-ejb/target" />

Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.

After that, invoking

>ant war

with the dcm4jboss-war dist directory as working directory
generates the Web Application Archive

target/dcm4jboss-web.war
.

== Building DICOM services components and prepare distribution packages ==

The source of the dcm4jboss-sar package is available in module dcm4jboss-sar
in the dcm4che project CVS repository:

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4jboss-sar

The default build script (build.xml) assumes following locations of
components required for building dcm4jboss-sar:

<!-- Override with your dcm4che dist location -->
<property name="dcm4che.home" value="../dcm4che14/build"/>		

<!-- Override with your dcm4jboss-ejb dist location -->
<property name="dcm4jboss-ejb.home" value="../dcm4jboss-ejb"/>		

<!-- Override with your XDoclet bundle dist location -->
<property name="xdoclet.home" value="${user.home}/xdoclet-1.2"/>

<!-- Override with your JBoss/Web server bundle dist location -->
<property name="jboss.home" value="${user.home}/jboss-3.2.3"/>		

Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file build.properties.default to
build.properties and modify the values in it.

After that, invoking

>ant dist

with the dcm4jboss-sar distribution directory as working directory
generates the distribution packages

target/dcm4jboss-<version>.zip
target/dcm4jboss-standalone-hsql-<version>.zip
target/dcm4jboss-standalone-psql-<version>.zip
target/dcm4jboss-standalone-db2-<version>.zip
target/dcm4jboss-standalone-oracle-<version>.zip
.
