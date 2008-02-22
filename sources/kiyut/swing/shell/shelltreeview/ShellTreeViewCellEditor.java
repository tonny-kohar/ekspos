/*
 * ShellTreeViewCellEditor.java
 *
 * Created on January 24, 2003, 3:55 PM
 */

package kiyut.swing.shell.shelltreeview;

import java.awt.*;

import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;

/** 
 * The editor for <code>ShellTreeView</code> node. 
 *
 * @version 1.0
 * @author  tonny
 */
public class ShellTreeViewCellEditor extends DefaultTreeCellEditor {
    
    /** Constructs a <code>ShellTreeViewCellEditor</code> 
     * @param tree a JTree object
     * @param renderer a DefaultTreeCellRenderer object
     */
    public ShellTreeViewCellEditor(JTree tree, DefaultTreeCellRenderer renderer)  {
        super(tree,renderer);
    }
 
    /** Configures the editor with the supplied parameter
     * @param tree the JTree that is asking the editor to edit
     * @param value the value of the cell to be edited
     * @param isSelected true is the cell is to be renderer with selection highlighting
     * @param expanded true if the node is expanded
     * @param leaf true if the node is a leaf node
     * @param row the row index of the node being edited
     * @return the component for editing
     */
    public Component getTreeCellEditorComponent(JTree tree,Object value,boolean isSelected,boolean expanded,boolean leaf,int row) {
        FileSystemView fsv = ((ShellTreeViewModel)tree.getModel()).getFileSystemView();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        
        File file = (File)node.getUserObject();
        String name = fsv.getSystemDisplayName(file);
        return super.getTreeCellEditorComponent(tree, name, isSelected,expanded,leaf, row);
    }
                                            
    
}
