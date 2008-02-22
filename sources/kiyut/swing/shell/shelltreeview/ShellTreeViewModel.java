/*
 * ShellTreeViewModel.java
 *
 * Created on December 26, 2002, 1:02 PM
 */

package kiyut.swing.shell.shelltreeview;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.filechooser.FileSystemView;

import kiyut.swing.shell.io.*;

/** The data model for <code>ShellTreeView</code>
 * The default values for fileHidingEnabled is true. 
 *
 * @version 1.0
 * @author  tonny
 */
public class ShellTreeViewModel extends DefaultTreeModel {
    
    /** the <code>FileSystemView</code> for this model */
    protected FileSystemView fsv;
    
    /** the use file hiding for this model */
    protected boolean useFileHiding;
    
    /** the view mode of <code>ShellTreeViewModel</code> */
    protected int viewMode;
    
    /** Constructs a <code>ShellTreeModel</code> */
    public ShellTreeViewModel() {
        this(FileSystemView.getFileSystemView(),true);
    }
    
    /** Constructs a <code>ShellTreeViewModel</code> using the given <code>FileSystemView</code>.
     * @param fsv FileSystemView
     * @param useFileHiding  the boolean value that determines whether file hiding is turned on
     */
    public ShellTreeViewModel(FileSystemView fsv, boolean useFileHiding) {
        super(null);
        
        this.fsv = fsv;
        this.useFileHiding = useFileHiding;
        
        TreeNode root = new DefaultMutableTreeNode(fsv.getRoots()[0]);
        setRoot(root);
        reload(root);
    }
    
    /** Returns whether the specified node is a leaf node.
     * @param node the node to check
     * @return true if the node is a leaf node 
     */
    public boolean isLeaf(Object node) {
        DefaultMutableTreeNode fileNode = (DefaultMutableTreeNode)node;
        
        File file = (File)fileNode.getUserObject();
        
        boolean b = false;
        if (fsv.isFloppyDrive(file)) {
            b = false;   
        } else  if (!file.isDirectory()) {
            b = true;
        } 
        
        return b;
    }
    
    /** Returns true if hidden files are not shown ; otherwise, returns false.
     * @return the status of the file hiding
     *
     * @see #setFileHidingEnabled(boolean)
     */
    public boolean isFileHidingEnabled() {
        return this.useFileHiding;
    }
    
    /** Sets file hiding on or off. If true, hidden files are not shown. 
     * @param useFileHiding the boolean value that determines whether file hiding is turned on
     *
     * @see #isFileHidingEnabled()
     */
    public void setFileHidingEnabled(boolean useFileHiding) {
        this.useFileHiding = useFileHiding;
    }
    
    /** Returns the current view mode. The default is VM_FILES_AND_DIRECTORIES.
     * @return the type of files to be displayed, one of the following:
     * <ul>
     * <li>ShellTree.VM_DIRECTORIES_ONLY.
     * <li>ShellTree.VM_FILES_AND_DIRECTORIES.
     * </ul>
     * @see #setViewMode(int)
     */
    public int getViewMode() {
        return viewMode;
    }
    
    /** 
     * Sets the view mode.
     * @param viewMode the type of files to be displayed:
     * <ul>
     * <li>ShellTree.VM_DIRECTORIES_ONLY.
     * <li>ShellTree.VM_FILES_AND_DIRECTORIES.
     * </ul>
     * @throws IllegalArgumentException if <code>viewMode</code> is an illegal <code>viewMode</code>
     *
     * @see #getViewMode
     */
    public void setViewMode(int viewMode) throws IllegalArgumentException {
        if ((viewMode == ShellTreeView.VM_DIRECTORIES_ONLY) || (viewMode == ShellTreeView.VM_FILES_AND_DIRECTORIES)) {
            this.viewMode = viewMode;
            reload(root);
        } else {
            throw new IllegalArgumentException("illegal viewMode");
        }
    }
    
    /** Returns the file system view.
     * @return the <code>FileSystemView</code> object
     *
     * @see #setFileSystemView(javax.swing.filechooser.FileSystemView)
     */
    public FileSystemView getFileSystemView() {
        return fsv;
    }
    
    /** Sets the file system view that this model uses for accessing and creating file system resources, 
     * such as finding the floppy drive and getting a list of root drives.
     * @param fsv the new FileSystemView
     * 
     * @see #getFileSystemView
     */
    public void setFileSystemView(FileSystemView fsv) {
        this.fsv = fsv;
    }
    
    /** This sets the user object of the TreeNode identified by path and posts a node changed. 
     * If you use custom user objects in the TreeModel you're going to need to subclass this 
     * and set the user object of the changed node to something meaningful.
     * @param path path to the node that the user has altered
     * @param newValue the new value from the TreeCellEditor
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        // do nothing
    }
    
    /**
     * Invoke this method if you've modified the TreeNodes upon which this
     * model depends.  The model will notify all of its listeners that the
     * model has changed below the node <code>node</code>.
     * @param node The node to reload
     */
    public void reload(TreeNode node) {
        if(node == null) {
            return;
        }
        
        DefaultMutableTreeNode fileNode = (DefaultMutableTreeNode)node;
        File file = (File)fileNode.getUserObject();
        
        // Group the folder & file
        File[] fileArray = fsv.getFiles(file,useFileHiding);
        List<File> dirs = ShellFilter.filter(new DirectoryFilter(fsv),fileArray);
        Collections.sort(dirs);
        List<File> fileList = new ArrayList<File>(dirs);
        
        if (viewMode == ShellTreeView.VM_FILES_AND_DIRECTORIES) {
            List<File> files = ShellFilter.filter(new FileOnlyFilter(fsv),fileArray);
            Collections.sort(files);
            fileList.addAll(files);
        }
        
        fileNode.removeAllChildren();
        
        for(int i=0; i<fileList.size(); i++) {
            File f = fileList.get(i);
            fileNode.add(new DefaultMutableTreeNode(f));
        }
                
        super.reload(node);
    }
}
