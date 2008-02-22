/*
 * ShellListViewSelectionModel.java
 *
 * Created on March 3, 2005, 5:02 PM
 */

package kiyut.swing.shell.shelllistview;

import java.io.*;
import java.util.*;

import javax.swing.*;

/**
 *
 * @author Kiyut
 */
public class ShellListViewSelectionModel extends DefaultListSelectionModel {
    /** model */
    protected ShellListViewModel dataModel;
    
    /** Creates a new instance of ShellListViewSelectionModel */
    public ShellListViewSelectionModel(ShellListViewModel dataModel) {
        this.dataModel = dataModel;
    }
    
    /** Returns the last selected index.
     * @return the selected index or -1 if selection is empty and value is adjusting
     * @see #getLastSelectedFile
     */
    public int getLastSelectedIndex() {
        int i = -1;
        
        if (getValueIsAdjusting() || isSelectionEmpty()) {
            i = -1;
        } else {
            //i = getMaxSelectionIndex();
            i = getLeadSelectionIndex();
        }
        
        return i;
    }
    
    /** Returns the last selected file.
     * @return the selected file or null if selection is empty and value is adjusting
     * @see #getLastSelectedIndex
     */
    public File getLastSelectedFile() {
        int i = getLastSelectedIndex();
        
        if (i == -1) {
            return null;
        }
        
        File file = dataModel.getFile(i);
        return file;
        
        /*if (getValueIsAdjusting()) {
            return null;
        }
        
        if (isSelectionEmpty()) {
            return null;
        }
        
        //int i = getMaxSelectionIndex();
        int i = getLeadSelectionIndex();
        File file = dataModel.getFile(i);
        
        return file;
         */
    }
    
    /** Return a list of selected files
     * @return a list of files or null if selection is empty and value is adjusting
     */
    public List<File> getSelectedFiles() {
        if (getValueIsAdjusting()) {
            return null;
        }
        
        if (isSelectionEmpty()) {
            return null;
        }
        
        List<File> l = new ArrayList<File>();
        for (int i=0; i<dataModel.getRowCount(); i++) {
            if (isSelectedIndex(i)) {
                l.add(dataModel.getFile(i));
            }
        }
        
        return l;
    }
    
    /** Return a list of all files in the current directory
     * @return a list of all files or null if selection is empty and value is adjusting
     */
    public List<File> getAllFiles() {
        if (getValueIsAdjusting()) {
            return null;
        }
        
        if (isSelectionEmpty()) {
            return null;
        }
        
        List<File> l = new ArrayList<File>();
        for (int i=0; i<dataModel.getRowCount(); i++) {
            l.add(dataModel.getFile(i));
        }
        return l;
    }
    
    /**
     * Returns an array of all of the selected indices in increasing
     * order.
     * @return all of the selected indices, in increasing order
     */
    public int[] getSelectedIndices() {
        int iMin = getMinSelectionIndex();
        int iMax = getMaxSelectionIndex();
        
        if ((iMin < 0) || (iMax < 0)) {
            return new int[0];
        }
        
        int[] rvTmp = new int[1+ (iMax - iMin)];
        int n = 0;
        for(int i = iMin; i <= iMax; i++) {
            if (isSelectedIndex(i)) {
                rvTmp[n++] = i;
            }
        }
        int[] rv = new int[n];
        System.arraycopy(rvTmp, 0, rv, 0, n);
        return rv;
    }
    
    /**
     * Selects a set of objects
     * @param indices an array of the indices of the objects to select
     */
    public void setSelectedIndices(int[] indices) {
        clearSelection();
        int size = dataModel.getSize();
        for(int i = 0; i < indices.length; i++) {
            if (indices[i] < size) {
                addSelectionInterval(indices[i], indices[i]);
            }
        }
    }
}
