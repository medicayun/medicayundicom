#!/bin/sh
# -------------------------------------------------------------------------
# remove Audit Record Repository v2.x
# required for installation of Audit Record Repository v3.x
# -------------------------------------------------------------------------

mkdir ../dcm4chee-arr.old
mkdir ../dcm4chee-arr.old/lib
mkdir ../dcm4chee-arr.old/deploy
mv ../server/default/lib/dcm4chee-arr-ejb-client.jar ../dcm4chee-arr.old/lib/
mv ../server/default/lib/dcm4chee-arr.jar ../dcm4chee-arr.old/lib/
mv ../server/default/deploy/dcm4chee-arr-ejb.jar  ../dcm4chee-arr.old/deploy/
mv ../server/default/deploy/dcm4chee-arr-service.xml  ../dcm4chee-arr.old/deploy/
mv ../server/default/deploy/dcm4chee-arr.war  ../dcm4chee-arr.old/deploy/
