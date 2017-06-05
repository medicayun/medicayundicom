BUILD INSTRUCTIONS FOR DCM4JBOSS-EJB:
====================================

In order to build dcm4jboss-ejb you have to download and install:
- Ant 1.6.x (from http://ant.apache.org)
- JUnit 3.8.x (from http://junit.org)
- Jakarta Cactus 1.5 for J2EE 1.3 [http://jakarta.apache.org/cactus/]
- XDoclet 1.2 [http://xdoclet.sourceforge.net]
- JBoss 3.2.3 [http://jboss.org]

Create an environment variable pointing to the Ant home directory (ANT_HOME)
and add the $ANT_HOME/bin directory to your PATH.

Copy junit.jar from the JUnit package into $ANT_HOME/lib/.

You also need to build the dcm4che core library from the source, before
you can build dcm4jboss-ejb.

At first time, connecting to the project CVS repository, you have to login by

>cvs -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che login
CVS password:<enter>

Download the source of the dcm4che core library by

>cvs -z3 -d:pserver:anonymous@cvs.dcm4che.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4che14 

and build the library jar, by running

>ant jar

with the dcm4che14 distribution directory as working directory.

The source of the dcm4jboss-ejb package is available in module dcm4jboss-ejb
in the dcm4che project CVS repository:

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

with the dcm4jboss-ejb distribution directory as working directory builds:

target/common/lib/dcm4jboss/dcm4jboss-jdbc.jar:
Provides database (read) access via JDBC 

target/common/lib/dcm4jboss/dcm4jboss-ejb-client.jar:
Interfaces needed by (remote) EJB clients

target/db2/dcm4jboss-ejb-db2.jar:
ejb-jar with DB2 specific deployment descriptor.

target/hsql/dcm4jboss-ejb-hsql.jar:
ejb-jar with Hypersonic SQL specific deployment descriptor.

target/psql/dcm4jboss-ejb-psql.jar:
ejb-jar with Postgres SQL specific deployment descriptor.
