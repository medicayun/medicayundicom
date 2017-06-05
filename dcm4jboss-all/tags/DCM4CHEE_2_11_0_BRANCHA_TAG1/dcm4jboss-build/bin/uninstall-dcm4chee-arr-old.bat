rem -------------------------------------------------------------------------
rem remove Audit Record Repository v2.x
rem required for installation of Audit Record Repository v3.x
rem -------------------------------------------------------------------------

mkdir ..\dcm4chee-arr.old
mkdir ..\dcm4chee-arr.old\lib
mkdir ..\dcm4chee-arr.old\deploy
move ..\server\default\lib\dcm4chee-arr-ejb-client.jar ..\dcm4chee-arr.old\lib\
move ..\server\default\lib\dcm4chee-arr.jar ..\dcm4chee-arr.old\lib\
move ..\server\default\deploy\dcm4chee-arr-ejb.jar  ..\dcm4chee-arr.old\deploy\
move ..\server\default\deploy\dcm4chee-arr-service.xml  ..\dcm4chee-arr.old\deploy\
move ..\server\default\deploy\dcm4chee-arr.war  ..\dcm4chee-arr.old\deploy\
