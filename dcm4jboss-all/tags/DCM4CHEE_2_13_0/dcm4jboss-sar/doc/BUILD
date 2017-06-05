BUILD INSTRUCTIONS FOR DCM4CHEE ARCHIVE:
========================================

In order to build all components of the dcm4chee archive application you have
to download and install:

- Ant 1.7.0 [http://ant.apache.org]

- XDoclet 1.2.3 [http://xdoclet.sourceforge.net]

- JBoss Application Server 4.2.2.GA [http://labs.jboss.com/jbossas/downloads]

- Apache FOP 0.20.5 [http://xmlgraphics.apache.org/fop/]

- Jakarta Cactus 1.7.2 for J2EE 1.3 [http://jakarta.apache.org/cactus/]
(only needed to build test suite for dcm4jboss-ejb - not need to build
application)

The dcm4chee archive also uses

- dcm4che-audit-logger : JMX Service for emitting DICOM Supp95 Audit Trail
  Messages, and
- dcm4che-audit-login : JAAS login module emitting User Authentication message

which sources are not included by dcm4chee, but provided as separate
package dcm4chee-audit at http://sourceforge.net/projects/dcm4che . Follow
the build instructions included in dcm4chee-audit, to build and install these
artefacts in your local Maven Repository (default: ~/.m2/repository) for usage
by following build task for the Audit Record Repository.

Create an environment variable pointing to the Ant home directory (ANT_HOME)
and add the $ANT_HOME/bin directory to your PATH.

The build scripts (dcm4jboss-all/dcm4jboss-xxx/build.xml) assumes following
locations of XDoclet, JBoss, Apache FOP, Jakarta Cactus and your local Maven2
Repository:

xdoclet.home=${user.home}/xdoclet-1.2.3
jboss.home=${user.home}/jboss-4.2.2.GA
fop.home=${user.home}/fop-0.20.5
cactus.home=${user.home}/jakarta-cactus-13-1.7
m2.repos=${user.home}/.m2/repository.

Instead of modifying the build scripts directly, you may adjust these values
to your build environment by renaming file 

dcm4jboss-all/build.properties.default
to
dcm4jboss-all/build.properties

and adjust these values there.

Change the working directory:
>cd dcm4jboss-all/dcm4jboss-build

and invoke:
>ant dist

to build all components and pack them in binary distribution packages:

target/dcm4chee-db2-<version>.zip
target/dcm4chee-hsql-<version>.zip
target/dcm4chee-mssql-<version>.zip
target/dcm4chee-mysql-<version>.zip
target/dcm4chee-oracle-<version>.zip
target/dcm4chee-psql-<version>.zip
.
