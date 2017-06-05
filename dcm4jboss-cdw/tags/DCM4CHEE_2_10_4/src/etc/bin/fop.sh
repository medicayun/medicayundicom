#! /bin/sh
#
# Shell script to run FOP

if [ -z "$JAVACMD" ] ; then 
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then 
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD=$JAVA_HOME/jre/sh/java
    else
      JAVACMD=$JAVA_HOME/bin/java
    fi
  else
    JAVACMD=java
  fi
fi
 
if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit
fi

if [ -n "$CLASSPATH" ] ; then
  LOCALCLASSPATH=$CLASSPATH
fi

BIN_DIR=`dirname $0`
LIB_DIR=${BIN_DIR}/../lib
LOCALCLASSPATH=${LIB_DIR}/fop.jar
LOCALCLASSPATH=${LIB_DIR}/avalon-framework-cvs-20020806.jar:$LOCALCLASSPATH
LOCALCLASSPATH=${LIB_DIR}/batik.jar:$LOCALCLASSPATH
LOCALCLASSPATH=${LIB_DIR}/jimi-1.0.jar:$LOCALCLASSPATH
LOCALCLASSPATH=${LIB_DIR}/jai_core.jar:$LOCALCLASSPATH
LOCALCLASSPATH=${LIB_DIR}/jai_codec.jar:$LOCALCLASSPATH

$JAVACMD -classpath "$LOCALCLASSPATH" $FOP_OPTS org.apache.fop.apps.Fop -c ${BIN_DIR}/fopcfg.xml "$@"

