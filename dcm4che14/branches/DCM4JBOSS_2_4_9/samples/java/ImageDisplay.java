/*$Id: ImageDisplay.java 3660 2003-06-22 22:33:03Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class ImageDisplay extends JPanel {

    private static final int PREFERRED_WIDTH = 240;
    private static final int PREFERRED_HEIGHT = 80;

    private JFileChooser filechooser = null;

    // Used only if ImageDisplay is an application 
    private JFrame frame = null;

    // Used only if ImageDisplay is an applet 
    private ImageDisplayApplet applet = null;

    /** Creates a new instance of ImageDisplay */
    public ImageDisplay(ImageDisplayApplet applet) {
        this(applet, null);
    }

    public ImageDisplay(ImageDisplayApplet applet, GraphicsConfiguration gc) {

	this.applet = applet;

        if (applet == null) {
            frame = new JFrame(gc);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

	setLayout(new BorderLayout());

	setPreferredSize(new Dimension(PREFERRED_WIDTH,PREFERRED_HEIGHT));
        
        Action openAction = new AbstractAction("Open File",
                new ImageIcon(getClass().getResource("/open.gif"))) {
            public void actionPerformed(ActionEvent e) {
                if (filechooser == null) {
                    filechooser = new JFileChooser();
                    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                }

                if (filechooser.showOpenDialog(ImageDisplay.this) ==
                        JFileChooser.APPROVE_OPTION) {
                    open(filechooser.getSelectedFile());
                }
            }
        };
        
        JToolBar bar = new JToolBar();
        bar.add(new ToolBarButton(openAction));
        add(bar, BorderLayout.NORTH);

        if(applet == null) {
	    // put ImageDisplay in a frame and show it
	    frame.setTitle("Image Display - Control Panel");
	    frame.getContentPane().add(this, BorderLayout.CENTER);
	    frame.pack();
	    frame.show();
	} 
    }
    
    private void open(File f) {
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(f);
            Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
            ImageReader reader = (ImageReader)iter.next();
            reader.setInput(iis, false);
            JPanel p = new ImageBox(reader);
            //JPanel p = new ImageBox(f);
            JFrame jf = new JFrame("ImageDisplay - Display Panel");
            jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            jf.getContentPane().add(p);
            jf.pack();
            jf.setSize(Math.min(jf.getWidth(),800),
                    Math.min(jf.getHeight(),600));
            jf.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ImageDisplay Main. Called only if we're an application, not an applet.
     */
    public static void main(String[] args) {
    // Create ImageDisplay on the default monitor
	ImageDisplay display = new ImageDisplay(null, GraphicsEnvironment.
                                             getLocalGraphicsEnvironment().
                                             getDefaultScreenDevice().
                                             getDefaultConfiguration());
    }
}

class ToolBarButton extends JButton {
    public ToolBarButton(Action a) {
        super((Icon)a.getValue(Action.SMALL_ICON));
        String toolTip = (String)a.getValue(Action.SHORT_DESCRIPTION);
        if (toolTip == null)
            toolTip = (String)a.getValue(Action.NAME);
        if (toolTip != null)
            setToolTipText(toolTip);
        addActionListener(a);
    }
}