rem -------------------------------------------------------------------------
rem undeploy IHEYr4 Audit Logger Service v2.x
rem required for installation of RFC-3881 Audit Logger Service v3.x
rem -------------------------------------------------------------------------

mkdir ..\dcm4chee-auditlog.old
mkdir ..\dcm4chee-auditlog.old\deploy
move ..\server\default\deploy\dcm4chee-auditlog-service.xml  ..\dcm4chee-auditlog.old\deploy\
