BUILD INSTRUCTIONS FOR DCM4CHE:
===============================

In order to build dcm4che you have to download and install:
- Ant 1.6.x (from http://ant.apache.org)
- JUnit 3.8.x (from http://junit.org)
- Sun's Java Advanced Imaging Image I/O Tools 1.0_01 (from 
  http://java.sun.com/products/java-media/jai/downloads/download-iio-1_0_01.html)

Create an environment variable pointing to the Ant home directory (ANT_HOME)
and add the $ANT_HOME/bin directory to your PATH.

Copy junit.jar from the JUnit package into $ANT_HOME/lib/.

Change the working directory to the root directory of the
dcm4che source distribution.

Run

>ant release

to build all dcm4che components.

Before you can run the JUnit tests by

>ant test

, you need to download the test data, which are only available directly
from the project CVS repository at sourceforge:

At first time, connecting to the project CVS repository, you have to login by

>cvs -d:pserver:anonymous@cvs.sourceforge.net:/cvsroot/dcm4che login
CVS password:<enter>

, before you can checkout the testdata:

>cvs -z3 -d:pserver:anonymous@cvs.sourceforge.net:/cvsroot/dcm4che
  checkout dcm4che14/test/conf/data


