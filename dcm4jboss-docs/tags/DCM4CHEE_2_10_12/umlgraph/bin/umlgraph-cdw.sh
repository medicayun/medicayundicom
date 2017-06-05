#!/bin/bash

DIRNAME=`dirname $0`
cd $DIRNAME/../lib

mkdir -p ../../target/cdw
mkdir ../../target/cdw/dot
mkdir ../../target/cdw/gif

javadoc -docletpath UmlGraph.jar -doclet gr.spinellis.umlgraph.doclet.UmlGraph \
  -d ../../target/cdw/dot -views -private ../src/java/cdw/ServiceMBeans.java
  
for f in ../../target/cdw/dot/*.dot
do \
BASENAME=`basename $f .dot`
dot -Tps $f | \
gs -q -r360 -dNOPAUSE -sDEVICE=pnm -dDEVICEWIDTH=6000 -dDEVICEHEIGHT=4000 -sOutputFile=-  - -c quit | \
pnmcrop | \
pnmscale 0.25 | \
ppmtogif > ../../target/cdw/gif/$BASENAME.gif
done
