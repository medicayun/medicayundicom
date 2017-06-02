/*
 * Copyright (C) 2006 Thomas Hacklaender
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.iftm.dcm4che.services.samples;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.sql.*;
import javax.swing.*;
import javax.swing.event.*;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import de.iftm.dcm4che.services.ConfigProperties;
import de.iftm.dcm4che.services.CDimseService;
import de.iftm.dcm4che.services.StorageService;
import de.iftm.dcm4che.services.StorageServiceAdapter;

import org.dcm4che.dict.*;
import org.dcm4che.data.*;
import org.dcm4che.util.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.net.Dimse;

import org.apache.log4j.*;


/**
 * Sample application to test the implementet service classes.
 *
 * @author Thomas Hacklaender
 * @version 2006-07-01
 */
public class SampleApplication extends javax.swing.JFrame {
    
    /** Initialize logger */
    private static Logger log = Logger.getLogger(SampleApplication.class);
    
    
    /**
     * Creates new form SampleApplication
     */
    public SampleApplication() {
        // Clear all existing Appenders
        BasicConfigurator.resetConfiguration();
        
        // Initialize logging system
        BasicConfigurator.configure();
        
        // Set logging level to INFO
        Logger.getRootLogger().setLevel(Level.INFO);
        
        // Set logging level of QueryRetrieveServiceClass to DEBUG
        //Logger.getLogger("de.iftm.dcm4che.sc.QueryRetrieveServiceClass").setLevel(Level.DEBUG);
        
        // Set logging level of dcm4che to WARN
        Logger.getLogger("org.dcm4cheri.net.FsmImpl").setLevel(Level.WARN);
        Logger.getLogger("org.dcm4cheri.server.ServerImpl").setLevel(Level.WARN);
        
        setLookAndFeel();
        initComponents();
        postInitComponents();
    }
    
    
    /**
     * Any code that should be executeed after initialization of the GUI.
     *<p>The server is started here.
     */
    private void postInitComponents() {
        // Nothing to do
    }
    
    
    /**
     * Sets the Look-And-Feel of the program.
     */
    public void setLookAndFeel() {
        // Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            // UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Centers the frame on the screen.
     */
    public void showCentered() {
        Dimension frameSize;
        Dimension scrSize;
        
        // Frame validieren, die er eine voreingestellte Groe�e besitzen.
        this.validate();
        
        // Frame zentriert zeigen
        frameSize = this.getSize();
        scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((scrSize.width - frameSize.width) / 2, (scrSize.height - frameSize.height) / 2);
        this.setVisible(true);
        this.toFront();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        testPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        cFindBtn = new javax.swing.JButton();
        cEchoBtn = new javax.swing.JButton();
        cStoreBtn = new javax.swing.JButton();
        tslSendReceiveBtn = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        testPanel.setLayout(new java.awt.GridBagLayout());

        testPanel.setMinimumSize(new java.awt.Dimension(640, 320));
        testPanel.setOpaque(false);
        testPanel.setPreferredSize(new java.awt.Dimension(640, 320));
        textArea.setColumns(20);
        textArea.setRows(5);
        scrollPane.setViewportView(textArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 100.0;
        testPanel.add(scrollPane, gridBagConstraints);

        cFindBtn.setText("C-FIND/C-MOVE");
        cFindBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cFindMoveBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 10.0;
        testPanel.add(cFindBtn, gridBagConstraints);

        cEchoBtn.setText("C-ECHO");
        cEchoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cEchoBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 10.0;
        testPanel.add(cEchoBtn, gridBagConstraints);

        cStoreBtn.setText("C-STORE");
        cStoreBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cStoreBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 10.0;
        testPanel.add(cStoreBtn, gridBagConstraints);

        tslSendReceiveBtn.setText("TSL SEND - RECEIVE");
        tslSendReceiveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tslSendReceiveBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 10.0;
        testPanel.add(tslSendReceiveBtn, gridBagConstraints);

        getContentPane().add(testPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");
        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);

        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save As ...");
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText("Help");
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void tslSendReceiveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tslSendReceiveBtnActionPerformed
        ConfigProperties cfgStorageSCP;
        ConfigProperties cfgSaveFilesystem;
        StorageServiceAdapter ssa = null;
        ConfigProperties cfgCDimseService;
        boolean isOpen;
        File imageFile;
        Dataset imageDataset;
        CDimseService cDimseService;
        
        //>>>> Load and modify the configuration properties of the local server
        
        try {
            // Load configuration properties of the server
            cfgStorageSCP = new ConfigProperties(StorageService.class.getResource("resources/StorageService.cfg"));
            
            // Load configuration properties for saving the DICOM objects to the filesystem
            cfgSaveFilesystem = new ConfigProperties(StorageService.class.getResource("resources/SaveFilesystem.cfg"));
            
        } catch (IOException e) {
            log.error("Error while loading configuration properties");
            return;
        }
        
        // Normally the 'dicom' protocol is used. The data are send unencrypted.
        // If you would like to use the encrypted transmission, use the 'dicom-tls'
        // protocol. If you use it, you have to define some other properties, named 'tls_xxx'.
        // dicom-tls: accept TLS connection (offer AES and DES encryption)
        // dicom-tls.aes : accept TLS connection (force AES or DES encryption)
        // dicom-tls.3des: accept TLS connection (force DES encryption)
        // dicom-tls.nodes : accept TLS connection (no encryption)
        // cfgStorageSCP.put("protocol", "dicom");
        cfgStorageSCP.put("protocol", "dicom-tls");
        
        // !!!! By default use parameter given in the configuration file !!!!
        
        // cfgStorageSCP.put("tls-key", "file:/d:/dos/create_cert/tmp/identityDE.p12");
        // cfgStorageSCP.put("tls-keystore-passwd", "secret");
        // cfgStorageSCP.put("tls-key-passwd", "secret");
        // cfgStorageSCP.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/iftmCacert.jks");
        // cfgStorageSCP.put("tls-cacerts_passwd", "secret");
        
        // cfgStorageSCP.put("tls-key", "file:/d:/dos/create_cert/tmp/identityJava.jks");
        // cfgStorageSCP.put("tls-keystore-passwd", "secret");
        // cfgStorageSCP.put("tls-key-passwd", "secret");
        // cfgStorageSCP.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/identityJava.jks");
        // cfgStorageSCP.put("tls-cacerts_passwd", "secret");
        
        // cfgStorageSCP.put("tls-key", "file:/d:/dos/create_cert/tmp/identityJavaSigned.jks");
        // cfgStorageSCP.put("tls-keystore-passwd", "secret");
        // cfgStorageSCP.put("tls-key-passwd", "secret");
        // cfgStorageSCP.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/jstkCacert.jks");
        // cfgStorageSCP.put("tls-cacerts_passwd", "secret");
        
        // The port number of this computer used to receive DICOM objects.
        cfgStorageSCP.put("port", "5104");
        
        // The AE title of this computer. By default this property is not defined, so the receiver accepts connections with any AET set.
        // cfgStorageSCP.put("called-aets", "SERVER");
        // The AE titles of computers which are allowed to call this receiver. By default this property is not defined, which allows any computer to call this receiver.
        // cfgStorageSCP.put("calling-aets", "CLIENT");
        
        cfgSaveFilesystem.put("directory", "./tmp");
        
        // Create and start a new local server
        
        try {
            ssa = new StorageServiceAdapter(cfgStorageSCP, cfgSaveFilesystem);
        } catch (ParseException pe) {
            log.error("Error while parsing the server configuration: " + pe.getMessage());
            return;
        }
        
        try {
            ssa.start();
        } catch (Exception ioe) {
            log.error("Error while starting the server: " + ioe.getMessage());
            return;
        }
        
        
        //>>>> Load the sample image
        
        imageFile = new File("./samples/mr_t2.dcm");
        imageDataset = fileToDataset(imageFile);
        
        // Set new IDs to make the image unique
        
        // Set Patient Name to date and time
        imageDataset.putPN(Tags.PatientName, "N" + (new SimpleDateFormat("HHmmssss")).format(new java.util.Date()));
        // Set Patient ID
        imageDataset.putSH(Tags.PatientID, "ID" + (new SimpleDateFormat("HHmmssss")).format(new java.util.Date()));
        // Set Accession Number
        imageDataset.putSH(Tags.AccessionNumber, "NO" + (new SimpleDateFormat("HHmmssss")).format(new java.util.Date()));
        // SOP Study UID to new value
        imageDataset.putUI(Tags.StudyInstanceUID, UIDGenerator.getInstance().createUID());
        // SOP Series UID to new value
        imageDataset.putUI(Tags.SeriesInstanceUID, UIDGenerator.getInstance().createUID());
        // SOP Instance UID to new value
        imageDataset.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance().createUID());
        
        
        //>>>> Load and modify the configuration properties of the sender
        
        try {
            // Load configuration properties of the server
            cfgCDimseService = new ConfigProperties(StorageService.class.getResource("resources/CDimseService.cfg"));
        } catch (Exception e) {
            log.error("Error while loading configuration properties");
            return;
        }
        
        // DcmURL url = new DcmURL("dicom-tls://ARCHIVE@localhost:104");
        // DcmURL url = new DcmURL("dicom", "ARCHIVE", null, "localhost", 104);
        // Normally the 'dicom' protocol is used. The data are send unencrypted.
        // If you would like to use the encrypted transmission, use the 'dicom-tls'
        // protocol. If you use it, you have to define some other properties, named 'tls_xxx'.
        // dicom-tls: accept TLS connection (offer AES and DES encryption)
        // dicom-tls.aes : accept TLS connection (force AES or DES encryption)
        // dicom-tls.3des: accept TLS connection (force DES encryption)
        // dicom-tls.nodes : accept TLS connection (no encryption)
        // DcmURL url = new DcmURL("dicom", "SERVER", null, "localhost", 5104);
        // DcmURL url = new DcmURL("dicom://SERVER@localhost:5104");
        DcmURL url = new DcmURL("dicom-tls://SERVER@localhost:5104");
        
        // !!!! By default use parameter given in the configuration file !!!!
        
        // cfgCDimseService.put("tls-key", "file:/d:/dos/create_cert/tmp/identityDE.p12");
        // cfgCDimseService.put("tls-keystore-passwd", "secret");
        // cfgCDimseService.put("tls-key-passwd", "secret");
        // cfgCDimseService.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/iftmCacert.jks");
        // cfgCDimseService.put("tls-cacerts_passwd", "secret");
        
        // cfgCDimseService.put("tls-key", "file:/d:/dos/create_cert/tmp/identityJava.jks");
        // cfgCDimseService.put("tls-keystore-passwd", "secret");
        // cfgCDimseService.put("tls-key-passwd", "secret");
        // cfgCDimseService.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/identityJava.jks");
        // cfgCDimseService.put("tls-cacerts_passwd", "secret");
        
        // cfgCDimseService.put("tls-key", "file:/d:/dos/create_cert/tmp/identityJavaSigned.jks");
        // cfgCDimseService.put("tls-keystore-passwd", "secret");
        // cfgCDimseService.put("tls-key-passwd", "secret");
        // cfgCDimseService.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/jstkCacert.jks");
        // cfgCDimseService.put("tls-cacerts_passwd", "secret");
        
        try {
            cDimseService = new CDimseService(cfgCDimseService, url);
        } catch (ParseException e) {
            log.error("Error in constructor");
            return;
        }
        
        // Open association
        try {
            isOpen = cDimseService.aASSOCIATE();
            if (! isOpen) {
                return;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage());
            return;
        }
        
        // Store
        try {
            cDimseService.cSTORE(imageDataset);
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        
        // Release association
        try {
            cDimseService.aRELEASE(true);
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        
        // Stop the local server if currently running
        if (ssa != null) {
            ssa.stop();
            log.info("stop");
        }
        
        log.info(">>>>>>>>>> TSL SEND - RECEIVE finished. <<<<<<<<<<");
    }//GEN-LAST:event_tslSendReceiveBtnActionPerformed
    
    
    /**
     * Code to when pressing the cSTORE button.
     *<p>This test expects a server running on "localhost", listening at port 104, AET = "ARCHIVE".
     */
    private void cStoreBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cStoreBtnActionPerformed
        ConfigProperties cfgCDimseService;
        boolean isOpen;
        File imageFile;
        Dataset imageDataset;
        CDimseService cDimseService;
        
        try {
            // Load configuration properties of the server
            cfgCDimseService = new ConfigProperties(StorageService.class.getResource("resources/CDimseService.cfg"));
        } catch (Exception e) {
            log.error("Error while loading configuration properties");
            return;
        }
        
        //>>>> Load sample image
        
        imageFile = new File("./samples/mr_t2.dcm");
        imageDataset = fileToDataset(imageFile);
        
        //>>>> Set new IDs to make the image unique
        
        // Set Patient Name to date and time
        imageDataset.putPN(Tags.PatientName, "N" + (new SimpleDateFormat("HHmmssss")).format(new java.util.Date()));
        
        // Set Patient ID
        imageDataset.putSH(Tags.PatientID, "ID" + (new SimpleDateFormat("HHmmssss")).format(new java.util.Date()));
        
        // Set Accession Number
        imageDataset.putSH(Tags.AccessionNumber, "NO" + (new SimpleDateFormat("HHmmssss")).format(new java.util.Date()));
        
        // SOP Study UID to new value
        imageDataset.putUI(Tags.StudyInstanceUID, UIDGenerator.getInstance().createUID());
        
        // SOP Series UID to new value
        imageDataset.putUI(Tags.SeriesInstanceUID, UIDGenerator.getInstance().createUID());
        
        // SOP Instance UID to new value
        imageDataset.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance().createUID());
        
        
        //>>>> Modify the configuration properties of the server
        
        // DcmURL url = new DcmURL("dicom-tls://ARCHIVE@localhost:104");
        // DcmURL url = new DcmURL("dicom", "ARCHIVE", null, "localhost", 104);
        // Normally the 'dicom' protocol is used. The data are send unencrypted.
        // If you would like to use the encrypted transmission, use the 'dicom-tls'
        // protocol. If you use it, you have to define some other properties, named 'tls_xxx'.
        // dicom-tls: accept TLS connection (offer AES and DES encryption)
        // dicom-tls.aes : accept TLS connection (force AES or DES encryption)
        // dicom-tls.3des: accept TLS connection (force DES encryption)
        // dicom-tls.nodes : accept TLS connection (no encryption)
        DcmURL url = new DcmURL("dicom://ARCHIVE@localhost:104");
        // DcmURL url = new DcmURL("dicom-tls://ARCHIVE@localhost:104");
        // DcmURL url = new DcmURL("dicom", "ARCHIVE", null, "localhost", 104);
        
        // cfgCDimseService.put("tls-key", tls_key);
        // cfgCDimseService.put("tls-keystore-passwd", tls_key_passwd);
        // cfgCDimseService.put("tls-key-passwd", tls_key_passwd);
        // cfgCDimseService.put("tls-cacerts", tls_cacerts);
        // cfgCDimseService.put("tls-cacerts_passwd", tls_cacerts_passwd);
        
        try {
            cDimseService = new CDimseService(cfgCDimseService, url);
        } catch (ParseException e) {
            log.error("Error in constructor");
            return;
        }
        
        // Open association
        try {
            isOpen = cDimseService.aASSOCIATE();
            if (! isOpen) {
                return;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage());
            return;
        }
        
        // Store
        try {
            cDimseService.cSTORE(imageDataset);
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        
        // Release association
        try {
            cDimseService.aRELEASE(true);
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        
        log.info(">>>>>>>>>> C-STORE finished. <<<<<<<<<<");
    }//GEN-LAST:event_cStoreBtnActionPerformed
    
    
    /**
     * Code to when pressing the cECHO button.
     *<p>This test expects a server running on "localhost", listening at port 104, AET = "ARCHIVE".
     */
    private void cEchoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cEchoBtnActionPerformed
        ConfigProperties cfgCDimseService;
        CDimseService cDimseService;
        boolean isOpen;
        long delay;
        
        // Load configuration properties of the server
        try {
            cfgCDimseService = new ConfigProperties(CDimseService.class.getResource("resources/CDimseService.cfg"));
            //cfgCDimseService = new ConfigProperties(CDimseService.class.getResource("resources/CEcho_CDimseService.cfg"));
        } catch (IOException e) {
            log.error("Error while loading configuration properties");
            return;
        }
        
        //>>>> Modify the configuration properties of the server
        
        // The AE title of this computer. By default this property is not defined, so the receiver accepts connections with any AET set.
        // cfgCDimseService.put("called-aets", "SERVER");
        // The AE titles of computers which are allowed to call this receiver. By default this property is not defined, which allows any computer to call this receiver.
        // cfgCDimseService.put("calling-aets", "CLIENT");
        
        //>>>> Begin of test routines for query/retrieve >>>>>>>>>>>>>>>>>>>>>>
        
        // Normally the 'dicom' protocol is used. The data are send unencrypted.
        // If you would like to use the encrypted transmission, use the 'dicom-tls'
        // protocol. If you use it, you have to define some other properties, named 'tls_xxx'.
        // dicom-tls: accept TLS connection (offer AES and DES encryption)
        // dicom-tls.aes : accept TLS connection (force AES or DES encryption)
        // dicom-tls.3des: accept TLS connection (force DES encryption)
        // dicom-tls.nodes : accept TLS connection (no encryption)
        DcmURL url = new DcmURL("dicom://ARCHIVE@localhost:104");
        // DcmURL url = new DcmURL("dicom-tls://ARCHIVE@localhost:104");
        // DcmURL url = new DcmURL("dicom", "ARCHIVE", null, "localhost", 104);
        
        // cfgCDimseService.put("tls-key", tls_key);
        // cfgCDimseService.put("tls-keystore-passwd", tls_key_passwd);
        // cfgCDimseService.put("tls-key-passwd", tls_key_passwd);
        // cfgCDimseService.put("tls-cacerts", tls_cacerts);
        // cfgCDimseService.put("tls-cacerts_passwd", tls_cacerts_passwd);
        
        try {
            cDimseService = new CDimseService(cfgCDimseService, url);
        } catch (ParseException e) {
            log.error("Error in constructor");
            return;
        }
        
        // Open association
        try {
            isOpen = cDimseService.aASSOCIATE();
            if (! isOpen) {
                return;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage());
            return;
        }
        
        // Echo
        try {
            delay = cDimseService.cECHO();
            log.info("C-ECHO delay: " + String.valueOf(delay));
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        
        // Release association
        try {
            cDimseService.aRELEASE(true);
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        
        log.info(">>>>>>>>>> C-ECHO finished. <<<<<<<<<<");
    }//GEN-LAST:event_cEchoBtnActionPerformed
    
    
    /**
     * Code to when pressing the cFIND/cMOVE button.
     *<p>This test expects a archive running on "localhost", listening at port 104, AET = "ARCHIVE".
     *<p>On the archive a C-MOVE destination must be defined: AET = "SERVER", "localhost", port 5104.
     *<p>This sample program implements the server in method postInitComponents with AET = "SERVER",
     * "localhost", port 5104. When using the build-in configuration file the received DICOM objects
     * are stored in the directory "./tmp".
     */
    private void cFindMoveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cFindMoveBtnActionPerformed
        ConfigProperties cfgStorageSCP;
        ConfigProperties cfgSaveFilesystem;
        StorageServiceAdapter ssa = null;
        ConfigProperties cfgCDimseService;
        boolean isOpen;
        Vector datasetVector;
        CDimseService cDimseService;
        
        
        //>>>> Load and modify the configuration properties of the local server
        
        try {
            // Load configuration properties of the server
            cfgStorageSCP = new ConfigProperties(StorageService.class.getResource("resources/StorageService.cfg"));
            
            // Load configuration properties for saving the DICOM objects to the filesystem
            cfgSaveFilesystem = new ConfigProperties(StorageService.class.getResource("resources/SaveFilesystem.cfg"));
            
        } catch (IOException e) {
            log.error("Error while loading configuration properties");
            return;
        }
        
        // Normally the 'dicom' protocol is used. The data are send unencrypted.
        // If you would like to use the encrypted transmission, use the 'dicom-tls'
        // protocol. If you use it, you have to define some other properties, named 'tls_xxx'.
        // dicom-tls: accept TLS connection (offer AES and DES encryption)
        // dicom-tls.aes : accept TLS connection (force AES or DES encryption)
        // dicom-tls.3des: accept TLS connection (force DES encryption)
        // dicom-tls.nodes : accept TLS connection (no encryption)
        cfgStorageSCP.put("protocol", "dicom");
        // cfgStorageSCP.put("protocol", "dicom-tls");
        
        // !!!! By default use parameter given in the configuration file !!!!
        
        // cfgStorageSCP.put("tls-key", "file:/d:/dos/create_cert/tmp/identityDE.p12");
        // cfgStorageSCP.put("tls-keystore-passwd", "secret");
        // cfgStorageSCP.put("tls-key-passwd", "secret");
        // cfgStorageSCP.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/iftmCacert.jks");
        // cfgStorageSCP.put("tls-cacerts_passwd", "secret");
        
        // cfgStorageSCP.put("tls-key", "file:/d:/dos/create_cert/tmp/identityJava.jks");
        // cfgStorageSCP.put("tls-keystore-passwd", "secret");
        // cfgStorageSCP.put("tls-key-passwd", "secret");
        // cfgStorageSCP.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/identityJava.jks");
        // cfgStorageSCP.put("tls-cacerts_passwd", "secret");
        
        // cfgStorageSCP.put("tls-key", "file:/d:/dos/create_cert/tmp/identityJavaSigned.jks");
        // cfgStorageSCP.put("tls-keystore-passwd", "secret");
        // cfgStorageSCP.put("tls-key-passwd", "secret");
        // cfgStorageSCP.put("tls-cacerts", "file:/d:/dos/create_cert/tmp/jstkCacert.jks");
        // cfgStorageSCP.put("tls-cacerts_passwd", "secret");
        
        // The port number of this computer used to receive DICOM objects.
        cfgStorageSCP.put("port", "5104");
        
        // The AE title of this computer. By default this property is not defined, so the receiver accepts connections with any AET set.
        // cfgStorageSCP.put("called-aets", "SERVER");
        // The AE titles of computers which are allowed to call this receiver. By default this property is not defined, which allows any computer to call this receiver.
        // cfgStorageSCP.put("calling-aets", "CLIENT");
        
        cfgSaveFilesystem.put("directory", "./tmp");
        
        
        //>>>> Create and start a new local server
        
        try {
            ssa = new StorageServiceAdapter(cfgStorageSCP, cfgSaveFilesystem);
        } catch (ParseException pe) {
            log.error("Error while parsing the server configuration: " + pe.getMessage());
            return;
        }
        
        try {
            ssa.start();
        } catch (Exception ioe) {
            log.error("Error while starting the server: " + ioe.getMessage());
            return;
        }
        
        
        //>>>> Load and modify the configuration properties for C-FIND / C-MOVE
        
        try {
            // Load configuration properties for C-DIMSE
            cfgCDimseService = new ConfigProperties(StorageService.class.getResource("resources/CDimseService.cfg"));
            
        } catch (IOException e) {
            log.error("Error while loading configuration properties");
            return;
        }
        
        // Normally the 'dicom' protocol is used. The data are send unencrypted.
        // If you would like to use the encrypted transmission, use the 'dicom-tls'
        // protocol. If you use it, you have to define some other properties, named 'tls_xxx'.
        // dicom-tls: accept TLS connection (offer AES and DES encryption)
        // dicom-tls.aes : accept TLS connection (force AES or DES encryption)
        // dicom-tls.3des: accept TLS connection (force DES encryption)
        // dicom-tls.nodes : accept TLS connection (no encryption)
        // DcmURL url = new DcmURL("dicom", "ARCHIVE", null, "localhost", 104);
        DcmURL url = new DcmURL("dicom://ARCHIVE@localhost:104");
        // DcmURL url = new DcmURL("dicom-tls://ARCHIVE@localhost:104");
        
        cfgCDimseService.put("dest", "SERVER");
        
        // "XXX*" gives no results
        //cfgCDimseService.put("key.PatientName", "X*");
        
        // "N*" gives some results
        cfgCDimseService.put("key.PatientName", "N*");
        
        // "*" gives maximum results
        //cfgCDimseService.put("key.PatientName", "*");
        
        //>>>> Find >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        
        try {
            cDimseService = new CDimseService(cfgCDimseService, url);
        } catch (ParseException e) {
            log.error("Error in constructor");
            return;
        }
        
        // Open association
        try {
            isOpen = cDimseService.aASSOCIATE();
            if (! isOpen) {
                return;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage());
            return;
        }
        
        // Query
        try {
            datasetVector = cDimseService.cFIND();
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        
        // Output query results
        String text = "";
        for (int i = 0; i < datasetVector.size(); i++) {
            try {
                String s = "";
                Dataset ds = (Dataset) datasetVector.elementAt(i);
                s += ds.getString(Tags.PatientName) + ", ";
                s += ds.getString(Tags.PatientBirthDate) + ", ";
                s += ds.getString(Tags.StudyID) + ", ";
                s += ds.getString(Tags.StudyDescription) + ", ";
                s += ds.getString(Tags.SeriesNumber) + ", ";
                s += ds.getString(Tags.InstanceNumber);
                text += s + "\n";
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        textArea.setText(text);
        
        
        //>>>> Find >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        
        // Ask archive to move objects to destination (local server)
        for (int i = 0; i < datasetVector.size(); i++) {
            try {
                Dataset ds = (Dataset) datasetVector.elementAt(i);
                cDimseService.cMOVE(ds);
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        }
        
        // Release association
        try {
            cDimseService.aRELEASE(true);
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        
        // Stop the local server if currently running
        if (ssa != null) {
            ssa.stop();
            log.info("stop");
        }
        
        log.info(">>>>>>>>>> C-FIND/C-MOVE finished. <<<<<<<<<<");
    }//GEN-LAST:event_cFindMoveBtnActionPerformed
    
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    /**
     * The main method exectuted when starting as an application.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SampleApplication app = new SampleApplication();
        app.showCentered();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton cEchoBtn;
    private javax.swing.JButton cFindBtn;
    private javax.swing.JButton cStoreBtn;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JPanel testPanel;
    private javax.swing.JTextArea textArea;
    private javax.swing.JButton tslSendReceiveBtn;
    // End of variables declaration//GEN-END:variables
    
    
    /**
     * Loads a Dataset object from a file.
     *
     * @param f the file containing teh Dataset.
     * @return the read in Dataset.
     */
    private Dataset fileToDataset(File f) {
        BufferedInputStream in = null;
        FileFormat          ff = null;
        DcmParser           parser = null;
        Dataset             ds = null;
        
        try {
            in = new BufferedInputStream(new FileInputStream(f));
        } catch (IOException ioe) {
            log.error("Can't read file: " + f.getPath());
            return null;
        }
        
        try {
            parser = DcmParserFactory.getInstance().newDcmParser(in);
            
            try {
                ff = parser.detectFileFormat();
            } catch (Exception ioe) {
                log.error("Can't detect DICOM file format for file: " + f.getPath());
                // Try next file
                return null;
            }
            
            ds = DcmObjectFactory.getInstance().newDataset();
            
            try {
                ds.readFile(in, ff, -1);
            } catch (IOException ioe) {
                log.error("Can't create Dataset for file: " + f.getPath());
                // Try next file
                return null;
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {}
        }
        
        return ds;
    }
    
}
