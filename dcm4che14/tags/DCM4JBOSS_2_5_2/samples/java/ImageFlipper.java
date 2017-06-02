/*
 * @(#)ImageFlipper.java	1.2 00/11/15
 *
 * Copyright 2000 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;

/**
 * Display a list of images in sequence.
 *
 * @version 0.5
 *
 * @author Daniel Rice
 */
public class ImageFlipper implements ActionListener {

    // The list of filenames
    String[] filenames;

    // The current index
    int index = 0;

    // If true, increment continuously
    boolean running = false;

    // If true, display the images
    boolean displaying = true;

    // Print timing information every SAMPLE_SIZE images
    static final int SAMPLE_SIZE = 100;
    int filesRead = 0;
    long fileStartTime;

    JFrame frame;
    JPanel imagePanel;
    JLabel label;
    BufferedImage currentImage;

    JToggleButton runButton;
    JToggleButton displayButton;
    JButton previousButton;
    JButton nextButton;

    public ImageFlipper(String[] filenames) throws IOException, IIOException {
        this.filenames = filenames;
        this.index = 0;

        this.imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(800, 400));
        imagePanel.setLayout(new BorderLayout());
        this.currentImage =
            new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        this.label = new JLabel(new ImageIcon(currentImage));
        imagePanel.add(label, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        
        this.runButton = new JToggleButton("Run");
        runButton.addActionListener(this);
        controlPanel.add(runButton);

        this.displayButton = new JToggleButton("Display", true);
        displayButton.addActionListener(this);
        controlPanel.add(displayButton);

        this.previousButton = new JButton("Previous");
        previousButton.addActionListener(this);
        controlPanel.add(previousButton);

        this.nextButton = new JButton("Next");
        nextButton.addActionListener(this);
        controlPanel.add(nextButton);
        
        this.frame = new JFrame("Image I/O Flipper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(imagePanel, BorderLayout.CENTER);
        frame.getContentPane().add(controlPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocation(100, 100);
        frame.setVisible(true);

        loadImage();

        Timer timer = new Timer(0, this);
        timer.start();
    }

    private void loadImage() {
        String filename = filenames[index];
        try {
            if ((filesRead % SAMPLE_SIZE) == 0) {
                fileStartTime = System.currentTimeMillis();
            }
            this.currentImage = ImageIO.read(new File(filename));
            if (currentImage == null) {
                System.out.println("Error: " + filename + " - couldn't read!");
                return;
            }

            ++filesRead;
            if ((filesRead % SAMPLE_SIZE) == 0) {
                long endTime = System.currentTimeMillis();
                float msec = (float)(endTime - fileStartTime)/SAMPLE_SIZE;
                System.out.println("Average load time = " + msec +
                                   " milliseconds");
            }

            if (displaying) {
                label.setIcon(new ImageIcon(currentImage));
                frame.setTitle("Image I/O Flipper - " + filename);
                imagePanel.repaint();
            }
        } catch (Exception exc) {
            System.out.println("\nError: " + filename +
                               " - exception during read!");
            exc.printStackTrace();
            System.out.println();
        }
    }
    
    public synchronized void actionPerformed(ActionEvent e) {
        if (e.getSource() == runButton) {
            this.running = !this.running;
            previousButton.setEnabled(!running);
            nextButton.setEnabled(!running);
            
            return;
        } else if (e.getSource() == displayButton) {
            this.displaying = !this.displaying;
            loadImage();
            return;
        } else if (e.getSource() == previousButton) {
            --index;
            if (index == -1) {
                index = filenames.length - 1;
            }
            loadImage();
        } else if (e.getSource() == nextButton) {
            ++index;
            if (index == filenames.length) {
                index = 0;
            }
            loadImage();
        } else if (running) {
            ++index;
            if (index == filenames.length) {
                index = 0;
            }
            loadImage();
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("usage: java ImageFlipper file [file ...]");
            System.exit(0);
        }

        try {
            new ImageFlipper(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
