/*
 * ThumbnailCell.java
 *
 * Created on December 18, 2002, 4:04 PM
 */

package kiyut.swing.shell.shelllistview;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import java.io.IOException;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.event.*;

import javax.swing.*;
import javax.swing.event.*;

import kiyut.swing.shell.image.ImageUtilities;

/**
 * Thumbnail Cell that represent a cell in the <code>ThumbnailView</code> component
 * @version 1.0
 * @author  tonny
 */
public class ThumbnailCell extends JComponent implements IIOReadUpdateListener, IIOReadProgressListener {
    private static int IMAGE_WIDTH = 128;
    private static int IMAGE_HEIGHT = 128;
    
    /** label for this component */
    protected JLabel label;
    
    /** image pane or icon pane for this component */
    protected ImagePane imagePane;
    
    /** editor for this component */
    protected JComponent editor;
    
    /** cell has focus for this component */
    protected boolean cellHasFocus = false;
    
    /** boolean value for editing status */
    protected boolean editing = false;
    
    /** thumbnail view for this component */
    protected ThumbnailView thumbnailView;
    
    /** Image Loading progress */
    protected float progress = 0;
    
    /** Constructs a <code>ThumbnailCell</code> and initialize with the given parameter
     * @param thumbnailView an <code>ThumbnailView</code> object
     */
    public ThumbnailCell(ThumbnailView thumbnailView) {
        this.thumbnailView = thumbnailView;
        
        int width = 128;
        
        imagePane = new ImagePane();
        imagePane.setPreferredSize(new Dimension(width,128));
        imagePane.setBorder(BorderFactory.createEtchedBorder());
        imagePane.setOpaque(false);
        
        label = new JLabel();
        label.setPreferredSize(new Dimension(width,20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setLabelFor(imagePane);
        label.setText("filename");
        label.setOpaque(false);
        
        this.setLayout(new BorderLayout());
        this.add(imagePane, BorderLayout.CENTER);
        this.add(label, BorderLayout.SOUTH);
    }
    
    /** invoked to draw this component
     * @param g the Graphics context in which to paint
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        label.setFont(thumbnailView.getFont());
        
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = getWidth() - insets.left - insets.right;
        int h = getHeight() - insets.top - insets.bottom;
        
        if (editing) {
            label.setForeground(thumbnailView.getForeground());
            g.setColor(thumbnailView.getBackground());
            g.fillRect(x,y,w,h);
        } else {
            if (cellHasFocus) {
                label.setForeground(thumbnailView.getSelectionForeground());
                g.setColor(thumbnailView.getSelectionBackground());
                g.fillRect(x,y,w,h);
            } else {
                label.setForeground(thumbnailView.getForeground());
                g.setColor(thumbnailView.getBackground());
                g.fillRect(x,y,w,h);
            }
        }
    }
    
    /** set the text for this component to display
     * if the value of text is null or empty string, nothing is displayed.
     * @param text a string to display
     */
    public void setText(String text) {
        label.setText(text);
    }
    
    /** Returns the text string that the label displays.
     * @return a String
     */
    public String getText() {
        return label.getText();
    }
    
    /** set the image for this component to display
     * if the value of image is null or empty string, nothing is displayed.
     * @param image the image to display
     */
    public void setImage(BufferedImage image) {
        imagePane.setImage(image);
    }
    
    /** return the image
     * @return the image
     */
    public BufferedImage getImage() {
        return imagePane.getImage();
    }
    
    /** set the image rescale
     * @param width the width to which to scale the image.
     * @param height the height to which to scale the image.
     */
    public void setImageRescale(double width, double height) {
        imagePane.setImageRescale(width,height);
    }
    
    /** Sets the font for this component.
     * @param font The font to become this component's font.
     */
    public void setFont(Font font) {
        setFont(font);
        label.setFont(font);
    }
    
    /** set cell has focus
     * @param cellHasFocus boolean value to set the focus of this component
     */
    public void setCellHasFocus(boolean cellHasFocus) {
        this.cellHasFocus = cellHasFocus;
    }
    
    /** return whether this component has focus or not
     * @return true if this component has focus; otherwise false
     */
    public boolean isCellHasFocus() {
        return this.cellHasFocus;
    }
    
    /** Sets the editor to used by when this component is edited.
     * @param editor the new cell editor
     */
    public void setCellEditor(JComponent editor) {
        this.editor = editor;
    }
    
    /** remove the cell editor used by this component */
    public void removeCellEditor() {
        this.editor = null;
    }
    
    /** Tells the editor to start editing
     * @return true if editing was started; false otherwise
     */
    public boolean startCellEditing() {
        editing = true;
        editor.setPreferredSize(label.getPreferredSize());
        remove(label);
        add(editor, BorderLayout.SOUTH);
        editor.requestFocusInWindow();
        return true;
    }
    
    /** Tells the editor to stop editing and accept any partially edited value as the value of the editor.
     * @return true if editing was stopped; false otherwise
     */
    public boolean stopCellEditing() {
        editing = false;
        this.remove(editor);
        this.add(label, BorderLayout.SOUTH);
        return true;
    }
    
    /** Read file image contained in the <code>reader</code>
     * and register with this component <code>IIOReadUpdateListener</code>
     * and <code>IIOReadProgressListener</code>
     * @param reader <code>ImageReader</code> use to read the file
     * @param param <code>ImageReadParam</code> for the reader
     * @throws IOException If an I/O error occurs
     */
    public BufferedImage readFileImage(ImageReader reader, ImageReadParam param) throws IOException {
        int w = reader.getWidth(0);
        int h = reader.getHeight(0);
        Dimension size = imagePane.getPreferredSize();
        Dimension dest = new Dimension((int)size.getWidth()-5,(int)size.getHeight()-5);
        Dimension src = new Dimension(w,h);
        double scale = ImageUtilities.scaleToFit(src,dest);
        imagePane.setImageRescale(scale,scale);
        
        reader.addIIOReadUpdateListener(this);
        reader.addIIOReadProgressListener(this);
        
        BufferedImage bi = reader.read(0,param);
        
        reader.removeIIOReadUpdateListener(this);
        reader.removeIIOReadProgressListener(this);
        
        // to converserve the memory, once the loading done,
        // resize the big image into the smaller version
        BufferedImage smallImg = ImageUtilities.scaleImageToFit(bi,IMAGE_WIDTH,IMAGE_HEIGHT,this);
        w = smallImg.getWidth();
        h = smallImg.getHeight();
        size = imagePane.getPreferredSize();
        dest = new Dimension((int)size.getWidth()-5,(int)size.getHeight()-5);
        src = new Dimension(w,h);
        scale = ImageUtilities.scaleToFit(src,dest);
        setImage(smallImg);
        setImageRescale(scale,scale);
        repaint();
        
        return bi;
    }
    
    /** {@inheritDoc} */
    public void imageUpdate(ImageReader source, BufferedImage theImage, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
        //imagePane.paintImmediately(minX,minY,width,height);
        //repaint();
    }
    
    /** {@inheritDoc} */
    public void passComplete(ImageReader source, BufferedImage theImage) {
    }
    
    /** {@inheritDoc} */
    public void passStarted(ImageReader source, BufferedImage theImage, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
        //imagePane.setImage(theImage);
        if (pass == 0) {
            imagePane.setImage(theImage);
            repaint();
        }
    }
    
    /** {@inheritDoc} */
    public void thumbnailPassComplete(ImageReader source, BufferedImage theThumbnail) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailPassStarted(ImageReader source, BufferedImage theThumbnail, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailUpdate(ImageReader source, BufferedImage theThumbnail, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
    }
    
    /** {@inheritDoc} */
    public void imageComplete(ImageReader source) {
        
        
    }
    
    /** {@inheritDoc} */
    public void imageProgress(ImageReader source, float percentageDone) {
        if ((percentageDone - progress) >= 10) {
            repaint();
            progress = percentageDone;
        }
    }
    
    /** {@inheritDoc} */
    public void imageStarted(ImageReader source, int imageIndex) {
        progress = 0;
    }
    
    /** {@inheritDoc} */
    public void readAborted(ImageReader source) {
    }
    
    /** {@inheritDoc} */
    public void sequenceComplete(ImageReader source) {
    }
    
    /** {@inheritDoc} */
    public void sequenceStarted(ImageReader source, int minIndex) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailComplete(ImageReader source) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailProgress(ImageReader source, float percentageDone) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
    }
    
    /** A component for the parent class to render an image */
    private class ImagePane extends JPanel {
        /** an image for this component */
        private BufferedImage image;
        
        /** the scale with for this component. */
        private double scaleWidth = 1.0;
        
        /** the scaleHeight for this component. */
        private double scaleHeight = 1.0;
        
        /** Constructs a <code>ImagePane</code> */
        public ImagePane() { }
        
        /** set the image for this component to display
         * if the value of image is null or empty string, nothing is displayed.
         * @param image the image to display
         */
        public void setImage(BufferedImage image) {
            this.image = image;
        }
        
        /** set the image rescale
         * @param width the width to which to scale the image.
         * @param height the height to which to scale the image.
         */
        public void setImageRescale(double width, double height) {
            this.scaleWidth = width;
            this.scaleHeight = height;
        }
        
        /** return the image
         * @return the image
         */
        public BufferedImage getImage() {
            return image;
        }
        
        /** invoked to draw this component
         * @param g the Graphics context in which to paint
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);    // paint background
            
            if (image == null) {
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g;
            
            double w = image.getWidth() *scaleWidth;
            double h = image.getHeight() *scaleHeight;
            
            Dimension dim = getSize();
            double x = 0;
            double y = 0;
            
            if (dim.getWidth() > w) {
                x = (dim.getWidth() - w)/2;
            }
            
            if (dim.getHeight() > h) {
                y = (dim.getHeight() - h)/2;
            }
            
            g2.drawImage(image,(int)x,(int)y,(int)w,(int)h,this);
        }
    }
    
}
