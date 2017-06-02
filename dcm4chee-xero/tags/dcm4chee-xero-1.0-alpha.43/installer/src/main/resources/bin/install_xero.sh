#!/bin/sh
#
# Version: MPL 1.1/GPL 2.0/LGPL 2.1
#
# The contents of this file are subject to the Mozilla Public License Version
# 1.1 (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
# http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS IS" basis,
# WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
# for the specific language governing rights and limitations under the
# License.
#
# The Original Code is part of dcm4che, an implementation of DICOM(TM) in
# Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
#
# The Initial Developer of the Original Code is
# William Zimrin, University of Maryland
# Portions created by the Initial Developer are Copyright (C) 2008
# the Initial Developer. All Rights Reserved.

DCM4CHEESERV=$1server/default

if [ ! -d $1 ] 
then
     echo "Please pass DCM4CHEE to install script"
     echo "./install_xero <path-to-dcm4chee-installation-dir>"
else
      if [ -d $DCM4CHEESERV ]
      then
          rm -r $DCM4CHEESERV/lib/jaxb*
          cp *.war $DCM4CHEESERV/deploy
          read -p "Update server.xml, default web service, and log configuration?(y/n) " cont
          if [ $cont = "y" -o $cont = "Y" ]
          then
            echo "Updating server.xml in jboss-web.deployer."
            cp server.xml $DCM4CHEESERV/deploy/jboss-web.deployer/server.xml
            cp index.html default.htm
            cp jboss-log4j.xml $DCM4CHEESERV/conf
            jar uvf $DCM4CHEESERV/deploy/dcm4chee-wado.war index.html default.htm
           else
            echo Not Updating server.xml, default web service, and log configuration.
           fi
      else
          echo Could not locate $DCM4CHEESERV.  Please check that you provided
          echo the path to your DCM4CHEE server.  Also remember to put a final
	  echo slash on the end of the path.
      fi
fi
