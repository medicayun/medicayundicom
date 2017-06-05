BUILD INSTRUCTIONS FOR DCM4CHEE ARCHIVE:
========================================

In order to build all components of the dcm4chee archive application you have
to download and install:

- Ant 1.6.x [http://ant.apache.org]

- XDoclet 1.2.3 [http://xdoclet.sourceforge.net]

- JBoss 4.0.3SP1 [http://labs.jboss.com/jbossas/download]
(you have to fetch a binary package (e.g jboss-4.0.3SP1.zip) and unpack it,
running the installer does not work!)

- Apache FOP 0.20.5 [http://xmlgraphics.apache.org/fop/]

- Jakarta Cactus 1.7 for J2EE 1.3 [http://jakarta.apache.org/cactus/]
(only needed to build test suite for dcm4jboss-ejb - not need to build application)

- [Only Windows] Patch utility for Window 
[e.g. http://gnuwin32.sourceforge.net/packages/patch.htm]
(needed in PATH for build of Oracle and DB2 version) 

Create an environment variable pointing to the Ant home directory (ANT_HOME)
and add the $ANT_HOME/bin directory to your PATH.

The build scripts (dcm4jboss-all/dcm4jboss-xxx/build.xml) assumes following
locations of XDoclet, JBoss, Apache FOP and Jakarta Cactus:

xdoclet.home=${user.home}/xdoclet-1.2.3
jboss.home=${user.home}/jboss-4.0.3SP1
fop.home=${user.home}/fop-0.20.5
cactus.home=${user.home}/jakarta-cactus-13-1.7

Instead of modifying the build script directly, you may adjust these values
to your build environment by renaming file 

dcm4jboss-all/dcm4jboss-xxx/build.properties.default
to
dcm4jboss-all/dcm4jboss-xxx/build.properties

and modify the values in it. If no patch utility is available [Windows]
you have also to disable Oracle and DB2 build by

oracle.nobuild=true
db2.nobuild=true

in

dcm4jboss-all/dcm4jboss-arr/build.properties
and
dcm4jboss-all/dcm4jboss-ejb/build.properties.

Change the working directory:
>cd dcm4jboss-all/dcm4jboss-build

and invoke:
>ant dist

to build all components and pack them in binary distribution packages:

target/dcm4chee-standalone-db2-<version>.zip
target/dcm4chee-standalone-hsql-<version>.zip
target/dcm4chee-standalone-mssql-<version>.zip
target/dcm4chee-standalone-mysql-<version>.zip
target/dcm4chee-standalone-oracle-<version>.zip
target/dcm4chee-standalone-psql-<version>.zip
.
