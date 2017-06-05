#!/bin/bash

DIRNAME=`dirname $0`
cd $DIRNAME/../lib
mkdir -p ../../target/graph

for f in ../src/pic/*.pic
do \
BASENAME=`basename $f .pic`
pic2plot -Tps $f | \
gs -q -r360 -dNOPAUSE -sDEVICE=pnm -dDEVICEWIDTH=6000 -dDEVICEHEIGHT=4000 -sOutputFile=-  - -c quit | \
pnmcrop | \
pnmscale 0.25 | \
ppmtogif > ../../target/graph/$BASENAME.gif
done
