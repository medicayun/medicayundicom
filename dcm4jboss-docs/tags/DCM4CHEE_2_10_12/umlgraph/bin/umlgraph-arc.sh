#!/bin/bash

DIRNAME=`dirname $0`
cd $DIRNAME/../lib

mkdir -p ../../target/archive
mkdir ../../target/archive/dot
mkdir ../../target/archive/gif

javadoc -docletpath UmlGraph.jar -doclet gr.spinellis.umlgraph.doclet.UmlGraph \
  -d ../../target/archive/dot -views -private ../src/java/archive/ServiceMBeans.java
  
for f in ../../target/archive/dot/*.dot
do \
BASENAME=`basename $f .dot`
dot -Tps $f | \
gs -q -r360 -dNOPAUSE -sDEVICE=pnm -dDEVICEWIDTH=6000 -dDEVICEHEIGHT=4000 -sOutputFile=-  - -c quit | \
pnmcrop | \
pnmscale 0.25 | \
ppmtogif > ../../target/archive/gif/$BASENAME.gif
done
