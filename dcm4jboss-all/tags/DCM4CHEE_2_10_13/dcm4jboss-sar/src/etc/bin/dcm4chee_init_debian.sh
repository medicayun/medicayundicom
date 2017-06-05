#! /bin/sh
#
# dcm4chee startup/shutdown for Debian GNU/Linux
#
# Either amend this script for your requirements
# or just ensure that the following variables are set correctly 
# before calling the script

JAVA_HOME=${JAVA_HOME:-"/usr/local/java/j2sdk1.4"}

if [ ! -f "$JAVA_HOME/lib/tools.jar" ]; then
  echo Could not locate JDK in JAVA_HOME : $JAVA_HOME
  exit 1
fi

DCM4CHEE_HOME=${DCM4CHEE_HOME:-"/home/gunter/dcm4chee"}

if [ ! -f "$DCM4CHEE_HOME/bin/run.jar" ]; then
  echo Could not locate dcm4chee in DCM4CHEE_HOME : $DCM4CHEE_HOME
  exit 1
fi

DCM4CHEE_USER=${DCM4CHEE_USER:-"gunter"}

# PID_FILE must be writeable for DCM4CHEE_USER!
PID_FILE=/var/run/dcm4chee.pid

JAVA="$JAVA_HOME/bin/java"
JAVA_OPTS="-server -Xmx100m -Djava.awt.headless=true"
JAVA_CP="$DCM4CHEE_HOME/bin/run.jar:$JAVA_HOME/lib/tools.jar"
ARGS="$JAVA_OPTS -cp $JAVA_CP org.jboss.Main"

case "$1" in
  start)
	echo -n "Starting dcm4chee DICOM Archive "
        cd $DCM4CHEE_HOME/bin;
	start-stop-daemon --start --pidfile $PID_FILE --make-pidfile \
		--background --chuid $DCM4CHEE_USER --exec $JAVA -- $ARGS
	echo "."
	;;
  stop)
	echo -n "Stopping dcm4chee DICOM Archive "
	start-stop-daemon --stop --quiet --pidfile $PID_FILE
	echo "."
	;;
  restart)
	echo -n "Restarting dcm4chee DICOM Archive "
	start-stop-daemon --stop --quiet --pidfile $PID_FILE
	sleep 10
        cd $DCM4CHEE_HOME/bin;
	start-stop-daemon --start --pidfile $PID_FILE --make-pidfile \
		--background --chuid $DCM4CHEE_USER --exec $JAVA -- $ARGS
	echo "."
	;;
  *)
	echo "Usage: $0 {start|stop|restart}" >&2
	exit 1
	;;
esac

exit 0
