#! /bin/sh
#
# dcm4jboss startup/shutdown for Debian GNU/Linux
#
# Either amend this script for your requirements
# or just ensure that the following variables are set correctly 
# before calling the script

JAVA_HOME=${JAVA_HOME:-"/usr/local/java/j2sdk1.4"}

if [ ! -f "$JAVA_HOME/lib/tools.jar" ]; then
  echo Could not locate JDK in JAVA_HOME : $JAVA_HOME
  exit 1
fi

DCM4JBOSS_HOME=${DCM4JBOSS_HOME:-"/home/gunter/dcm4jboss"}

if [ ! -f "$DCM4JBOSS_HOME/bin/run.jar" ]; then
  echo Could not locate dcm4jboss in DCM4JBOSS_HOME : $DCM4JBOSS_HOME
  exit 1
fi

DCM4JBOSS_USER=${DCM4JBOSS_USER:-"gunter"}

# PID_FILE must be writeable for DCM4JBOSS_USER!
PID_FILE=/var/run/dcm4jboss.pid

JAVA="$JAVA_HOME/bin/java"
JAVA_OPTS="-server -Xmx100m -Djava.awt.headless=true"
JAVA_CP="$DCM4JBOSS_HOME/bin/run.jar:$JAVA_HOME/lib/tools.jar"
ARGS="$JAVA_OPTS -cp $JAVA_CP org.jboss.Main -c pacs"

case "$1" in
  start)
	echo -n "Starting dcm4jboss DICOM Archive "
        cd $DCM4JBOSS_HOME/bin;
	start-stop-daemon --start --pidfile $PID_FILE --make-pidfile \
		--background --chuid $DCM4JBOSS_USER --exec $JAVA -- $ARGS
	echo "."
	;;
  stop)
	echo -n "Stopping dcm4jboss DICOM Archive "
	start-stop-daemon --stop --quiet --pidfile $PID_FILE
	echo "."
	;;
  restart)
	echo -n "Restarting dcm4jboss DICOM Archive "
	start-stop-daemon --stop --quiet --pidfile $PID_FILE
	sleep 10
        cd $DCM4JBOSS_HOME/bin;
	start-stop-daemon --start --pidfile $PID_FILE --make-pidfile \
		--background --chuid $DCM4JBOSS_USER --exec $JAVA -- $ARGS
	echo "."
	;;
  *)
	echo "Usage: $0 {start|stop|restart}" >&2
	exit 1
	;;
esac

exit 0
