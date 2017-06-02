DCM4CHE Xero is a medical image viewer that runs on top of
an existing DCM4CHE system.  It can be installed by running:

install_xero <DCM4CHEE-DIR>

on Windows, or on Unix by copying the war files to your deploy
directory, and enabling single-sign on.  See the install_xero.cmd
file for one way to enable SSO.  You also need to remove the
<DCM4CHEE-DIR>/server/default/lib/jaxb*.jar as these are version 1.0 of
JAXB and Xero requires 2.0.

Then, startup the dcm4chee server again and go to:
http://localhost/xero
to startup the server.  You will need to login.


