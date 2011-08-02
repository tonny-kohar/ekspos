/*
 * StatusBar.java
 *
 * Created on June 7, 2003, 1:34 PM
 */

package kiyut.swing.statusbar;

//import java.awt.Dimension;
//import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 *
 * @author  tonny
 */
public class StatusBar extends JPanel {
    private List<StatusBarItem> itemList;
    
    /** Creates a new instance of StatusBar */
    public StatusBar() {
        this.setLayout(new java.awt.GridBagLayout());
        itemList = new ArrayList<StatusBarItem>();
    }
    
    /** Appends a statusBarItem to the end of this statusBar. 
     *@param statusBarItem the StatusBarItem to be added 
     */
    public void addItem(StatusBarItem statusBarItem) {
        GridBagConstraints gridBagConstraints;
        
        // add item into container
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = itemList.size();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        this.add(statusBarItem, gridBagConstraints);
        
        // update the prev weightx;
        int pos = itemList.size()-1;
        if (pos >= 0) {
            StatusBarItem prevItem = (StatusBarItem)itemList.get(pos);
            java.awt.GridBagLayout layout = (java.awt.GridBagLayout)getLayout();
            gridBagConstraints = layout.getConstraints(prevItem);
            gridBagConstraints.weightx = 0;
            layout.setConstraints(prevItem, gridBagConstraints);
        }
        
        // add the list
        itemList.add(statusBarItem);
    }
    
    public StatusBarItem removeItem(int pos) {
        super.remove(pos);    // remove from the container
        StatusBarItem removed = itemList.remove(pos);  // remove from the list

        // last item
        if (pos == itemList.size() && (pos > 0)) {
            StatusBarItem prevItem = itemList.get(pos);
            java.awt.GridBagLayout layout = (java.awt.GridBagLayout)getLayout();
            GridBagConstraints gridBagConstraints = layout.getConstraints(prevItem);
            gridBagConstraints.weightx = 1;
            layout.setConstraints(prevItem, gridBagConstraints);
        }
        
        return removed;
    }
    
    public java.util.List<StatusBarItem> getItemList() {
        return itemList;
    }
    
    public StatusBarItem getItem(int pos) {
        return itemList.get(pos);
    }
    
    public int getItemCount() {
        return itemList.size();
    }
}
