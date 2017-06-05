#!/bin/sh
javah -jni -classpath ../../../lib/mogio.jar se.mog.io.FileSystem
javah -jni -classpath ../../../lib/mogio.jar se.mog.io.UnixFileSystem


gcc -g -fPIC -shared -static-libgcc \
	-I${JAVA_HOME}/include \
	-I${JAVA_HOME}/include/linux \
	UnixFileSystem.c \
	-o libFileSystem.so
