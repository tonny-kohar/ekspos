/*
 * StatusBarItem.java
 *
 * Created on June 16, 2003, 8:45 PM
 */

package kiyut.swing.statusbar;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 *
 * @author  tonny
 */
public class StatusBarItem extends JLabel {
    private Border border;
    
    /** Creates a new instance of StatusBarItem */
    public StatusBarItem() {
        this("", 75,SwingConstants.CENTER);
    }
    
    public StatusBarItem(String text, int width, int align) {
        java.awt.Color color = getBackground().darker().darker().darker();
        border = new CompoundBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)), 
                new javax.swing.border.CompoundBorder(new javax.swing.border.LineBorder(color), 
                new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 2, 0, 2))));
        
        this.setBorder(border);
        this.setVerticalAlignment(SwingConstants.CENTER);
        //this.setFont(new java.awt.Font("Dialog", 0, 12));
        this.setHorizontalAlignment(align);
        this.setText(text);
        
        Dimension dim = getPreferredSize();
        dim.setSize(width,dim.getHeight());
        setPreferredSize(dim);
        setMinimumSize(dim);
    }
}
