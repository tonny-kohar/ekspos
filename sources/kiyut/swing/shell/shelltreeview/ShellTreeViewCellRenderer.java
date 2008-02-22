/*
 * ShellTreeViewCellRenderer.java
 *
 * Created on January 10, 2003, 10:01 AM
 */

package kiyut.swing.shell.shelltreeview;

import java.awt.*;

import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;

/** File with Icon cell renderer, it render the filename with the icon
 *
 * @version 1.0
 * @author  tonny
 */
public class ShellTreeViewCellRenderer extends DefaultTreeCellRenderer {
    
     /** Constructs a <code>ShellTreeViewCellRenderer</code> */
    public ShellTreeViewCellRenderer() {
        
    }
 
    /** Return a component that has been configured to display the specified value. 
     * @param tree The JTree we're painting.
     * @param value The value returned by data model.
     * @param selected True if the specified node was selected.
     * @param expanded True if the specified node was expanded.
     * @param leaf True if the specified node was leaf node
     * @param row The node index row
     * @param hasFocus True if the specified node has the focus.
     * @return A component whose paint() method will render the specified value.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        FileSystemView fsv = ((ShellTreeViewModel)tree.getModel()).getFileSystemView();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        
        File file = (File)node.getUserObject();
        String name = fsv.getSystemDisplayName(file);
        super.getTreeCellRendererComponent(tree,name,selected,expanded,leaf,row,hasFocus);
        setText(name);
        setIcon(fsv.getSystemIcon(file));
        /*Icon icon = fsv.getSystemIcon(file);
        if (leaf == true) {
            setLeafIcon(icon);
        } else {
            setClosedIcon(icon);
            setOpenIcon(icon);
        }*/
        
        setBackgroundNonSelectionColor(tree.getBackground());
        
        return this;
    }
    
}
