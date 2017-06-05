#!/bin/sh
# -------------------------------------------------------------------------
# undeploy IHEYr4 Audit Logger Service v2.x
# required for installation of RFC-3881 Audit Logger Service v3.x
# -------------------------------------------------------------------------

mkdir ../dcm4chee-auditlog.old
mkdir ../dcm4chee-auditlog.old/deploy
mv ../server/default/deploy/dcm4chee-auditlog-service.xml  ../dcm4chee-auditlog.old/deploy/
