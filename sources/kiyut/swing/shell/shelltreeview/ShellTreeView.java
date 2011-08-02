/*
 * ShellTreeView.java
 *
 * Created on December 26, 2002, 12:58 PM
 */

package kiyut.swing.shell.shelltreeview;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;

import kiyut.swing.shell.event.*;
import kiyut.swing.shell.io.*;
import kiyut.swing.shell.util.*;

/** 
 * ShellTreeView is a component which show the file system in the tree structure.
 * It can be set to show directories only or files and directories
 *
 * @author  tonny
 */
public class ShellTreeView extends JTree {
    /** the directories only view mode */
    public static int VM_DIRECTORIES_ONLY = 0;
    
    /** the files and directories only view mode */
    public static int VM_FILES_AND_DIRECTORIES = 1;
    
    /** Constructs ShellTreeView with default value */
    public ShellTreeView() {
        this(FileSystemView.getFileSystemView(),true);
    }
    
    /** Constructs ShellTreeView using the given <code>FileSystemView</code>.
     * @param fsv FileSystemView
     * @param useFileHiding  the boolean value that determines whether file hiding is turned on
     */
    public ShellTreeView(FileSystemView fsv, boolean useFileHiding) {
        ShellTreeViewCellRenderer renderer = new ShellTreeViewCellRenderer();
        
        ShellTreeViewModel model = new ShellTreeViewModel(fsv,useFileHiding);
        
        TreeCellEditor cellEditor = new ShellTreeViewCellEditor(this,renderer);
        
        addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillExpand(TreeExpansionEvent evt) throws ExpandVetoException {
                onTreeWillExpand(evt);
            }
            public void treeWillCollapse(TreeExpansionEvent evt) throws ExpandVetoException {
            }
        });
        
        setModel(model);
        setRootVisible(true);
        setShowsRootHandles(true);
        putClientProperty("JTree.lineStyle", "Angled");
        setEditable(true);
        setCellRenderer(renderer);
        setCellEditor(cellEditor);
        
        TreeSelectionModel selectionModel = getSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        //setDragEnabled(true);
        //setTransferHandler(new ShellTreeViewTransferHandler());
    }
    
    /** Returns true if hidden files are not shown otherwise false.
     * @return the status of the file hiding
     *
     * @see #setFileHidingEnabled(boolean)
     */
    public boolean isFileHidingEnabled() {
        return ((ShellTreeViewModel)getModel()).isFileHidingEnabled();
    }
    
    /** Sets file hiding on or off. If true, hidden files are not shown.
     * @param useFileHiding the boolean value that determines whether file hiding is turned on
     *
     * @see #isFileHidingEnabled()
     */
    public void setFileHidingEnabled(boolean useFileHiding) {
        ((ShellTreeViewModel)getModel()).setFileHidingEnabled(useFileHiding);
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
        return ((ShellTreeViewModel)getModel()).getViewMode();
    }
    
    /**
     * Sets the view mode.
     * <b>Note:</b> when the view mode change, it will clear the selection and start from root
     * @param viewMode the type of files to be displayed:
     * <ul>
     * <li>ShellTree.VM_DIRECTORIES_ONLY.
     * <li>ShellTree.VM_FILES_AND_DIRECTORIES.
     * </ul>
     * @throws IllegalArgumentException if <code>viewStyle</code> is an illegal viewStyle
     *
     * @see #getViewMode
     */
    public void setViewMode(int viewMode) throws IllegalArgumentException {
        ((ShellTreeViewModel)getModel()).setViewMode(viewMode);
    }
    
    /** Selects the node identified by the specified path.
     * If any component of the path is hidden (under a collapsed node),
     * and getExpandsSelectedPaths is true it is exposed (made viewable).
     * if the file is not found on the treepath nothing will happen
     * @param file the <code>File</code> specifying the node to select
     * @throws IllegalArgumentException if <code>file</code> is <code>null</code>
     */
    public void setSelection(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Cannot set a null value");
        }
        
        ShellTreeViewModel treeModel = ((ShellTreeViewModel)getModel());
        FileSystemView fsv = treeModel.getFileSystemView();
        
        java.util.List<File> l = new ArrayList<File>();
        
        // create the list start from dir position to root
        l.add(file);
        File parent = fsv.getParentDirectory(file);
        while (parent != null) {
            l.add(parent);
            parent = fsv.getParentDirectory(parent);
        }
        
        // reverse the list, so it start with root dir
        Collections.reverse(l);
        
        boolean found = false;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getModel().getRoot();
        DefaultMutableTreeNode childNode;
        for (int i=1;i<l.size();i++) {
            found = false;
            
            // seach in the current node children
            for (int j=0; j<node.getChildCount(); j++) {
                childNode = (DefaultMutableTreeNode)node.getChildAt(j);
                File f = (File)childNode.getUserObject();
                
                if (f.compareTo(l.get(i)) == 0) {
                    node = childNode;
                    if (i < l.size()-1) {
                        expandPath(new TreePath(node.getPath()));
                    }
                    found = true;
                    break;
                }
            }
            
            if (found == false) { break; }
        }
        
        if (found == true) {
            TreePath treePath = new TreePath(node.getPath());
            setSelectionPath(treePath);
            scrollPathToVisible(treePath);
        }
    }
    
    /** Return last selected file or directory
     * @return last selected file or directory or null
     */
    public File getLastSelection() {
        File file = null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
        if (node != null) {
            file = (File)node.getUserObject();
        }
        return file;
    }
    
    
    ////////////////////////////////
    // Event Handler
    ///////////////////////////////
    
    /** Invoked whenever a node in the tree is about to be expanded.
     * @param e <code>TreeExpansionEvent</code>
     * @throws ExpandVetoException
     */
    private void onTreeWillExpand(TreeExpansionEvent evt) throws ExpandVetoException {
        TreeNode node = (TreeNode)evt.getPath().getLastPathComponent();
        ShellTreeViewModel model = (ShellTreeViewModel)getModel();
        model.reload(node);
    }
}
