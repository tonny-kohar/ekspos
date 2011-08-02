/*
 * IconCell.java
 *
 * Created on December 16, 2002, 3:17 PM
 */

package kiyut.swing.shell.shelllistview;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Icon Cell that represent a cell in the <code>IconView</code> component
 * 
 * @author Kiyut
 */
public class IconCell extends JComponent {
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
    
    /** icon view for this component */
    protected IconView iconView;
    
    /** Constructs a <code>IconCell</code> and initialize with the given parameter
     * @param iconView an <code>IconView</code> object
     */
    public IconCell(IconView iconView) {
        this.iconView = iconView;
        
        int width = 64;
        
        imagePane = new ImagePane();
        imagePane.setPreferredSize(new Dimension(width,32));
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
        
        label.setFont(iconView.getFont());
        
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = getWidth() - insets.left - insets.right;
        int h = getHeight() - insets.top - insets.bottom;
        
        if (editing) {
            label.setForeground(iconView.getForeground());
            g.setColor(iconView.getBackground());
            g.fillRect(x,y,w,h);
        } else {
            if (cellHasFocus) {
                label.setForeground(iconView.getSelectionForeground());
                g.setColor(iconView.getSelectionBackground());
                g.fillRect(x,y,w,h);
            } else {
                label.setForeground(iconView.getForeground());
                g.setColor(iconView.getBackground());
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
    
    /** set the image rescale
     * @param width the width to which to scale the image.
     * @param height the height to which to scale the image.
     */
    public void setImageRescale(double width, double height) {
        imagePane.imageRescale(width,height);
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
        public void imageRescale(double width, double height) {
            this.scaleWidth = width;
            this.scaleHeight = height;
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
