/*
 * BrowserTransferHandler.java
 *
 * Created on March 4, 2005, 1:42 PM
 */

package kiyut.ekspos;

import java.awt.datatransfer.*;
import java.awt.event.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.tree.*;

import kiyut.swing.shell.io.*;
import kiyut.swing.shell.util.*;
import kiyut.swing.shell.shelllistview.*;
import kiyut.swing.shell.shelltreeview.*;

/**
 * Browser Window DnD Support
 * @author Kiyut
 */
public class BrowserTransferHandler extends TransferHandler {
    protected ResourceBundle bundle = ResourceBundle.getBundle("kiyut.ekspos.BrowserWindow");
    
    /** Destination directory or folder */
    protected File targetFile;
    
    /** Boolean flag indicated clipboardTransfer or not */
    protected boolean clipboardTransfer = false;
    
    /** action requested */
    protected int action;
    
    /** Target Component */
    protected JComponent target;
    
    /** Source Component */
    protected JComponent source;
    
    /** Creates a new instance of BrowserTransferHandler */
    public BrowserTransferHandler() {
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        List<File> fileList = new ArrayList<File>();
        if (c instanceof ViewComponent) {
            ViewComponent sourceComp = (ViewComponent)c;
            fileList.addAll(((ShellListViewSelectionModel)sourceComp.getSelectionModel()).getSelectedFiles());
        } else if (c instanceof ShellTreeView) {
            ShellTreeView sourceComp = (ShellTreeView)c;
            TreePath[] paths = sourceComp.getSelectionPaths();
            if (paths == null || paths.length == 0) { return null; }
            for (int i=0; i<paths.length; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                fileList.add((File)node.getUserObject());
            }
        }
        
        if (fileList.size() == 0) {
            return null;
        }
        
        Collections.sort(fileList);
        Transferable transferable = new FileTransferable(fileList);
        return transferable;
        
    }
    
    @Override
    public int getSourceActions(JComponent comp) {
        return TransferHandler.COPY_OR_MOVE;
    }
    
    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        boolean b = false;
        for (int i=0; i<transferFlavors.length; i++) {
            if (transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
                b = true;
                break;
            }
        }
        return b;
    }
    
    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        clipboardTransfer = true;
        this.action = action;
        super.exportToClipboard(comp,clip,action);
    }
    
    @Override
    public void exportAsDrag(JComponent comp, InputEvent evt, int action) {
        clipboardTransfer = false;
        super.exportAsDrag(comp,evt,action);
    }
    
    @Override
    public boolean importData(JComponent comp, Transferable t) {
        boolean imported = false;
        if (canImport(comp,t.getTransferDataFlavors()) == false) {
            return imported;
        }
        
        List fileList = null;
        try {
            fileList = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException ex) {
            return imported;
        } catch (IOException ex) {
            return imported;
        }
        
        if (fileList == null) {
            return imported;
        }
        
        if (comp instanceof ViewComponent) {
            ViewComponent targetComp = (ViewComponent)comp;
            targetFile = ((ShellListViewSelectionModel)targetComp.getSelectionModel()).getLastSelectedFile();
        } else if (comp instanceof ShellTreeView) {
            ShellTreeView targetComp = (ShellTreeView)comp;
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)targetComp.getLastSelectedPathComponent();
            if (targetNode != null) {
                targetFile = (File)targetNode.getUserObject();
            }
        }
        
        if (targetFile == null) {
            return imported;
        }
        
        if (!targetFile.isDirectory()) {
            targetFile = null;
            return imported;
        }
        
        target = comp;
        if (clipboardTransfer == true) {
            startTransfer(source,target,action,fileList,targetFile);
        }
        
        imported = true;
        return imported;
    }
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        this.action = action;
        if (data == null) { return; }
        
        List fileList = null;
        try {
            fileList = (List)data.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException ex) {
            return;
        } catch (IOException ex) {
            return;
        }
        
        if (fileList == null) {
            return ;
        }
        
        this.source = source;
        
        if (clipboardTransfer == false) {
            startTransfer(source,target,action,fileList,targetFile);
        }
        
        /*final JComponent parentComponent = source;
        final List sourceList = fileList;
         
        if (action == TransferHandler.COPY) {
            ShellProgressMonitor shellProgressMonitor = new ShellProgressMonitor(parentComponent);
            try {
                shellProgressMonitor.start(ShellProgressMonitor.COPY,sourceList,targetFile);
            } catch (Exception ex) {
                String msg = ex.getMessage().trim().length() > 0 ? ex.getMessage() : bundle.getString("message.copyException.text");
                JOptionPane.showMessageDialog(parentComponent, msg, bundle.getString("message.exception.title"), JOptionPane.ERROR_MESSAGE);
                shellProgressMonitor.cancel();
            }
        } else if (action == TransferHandler.MOVE) {
            ShellProgressMonitor shellProgressMonitor = new ShellProgressMonitor(parentComponent);
            try {
                shellProgressMonitor.start(ShellProgressMonitor.MOVE,sourceList,targetFile);
            } catch (Exception ex) {
                String msg = ex.getMessage().trim().length() > 0 ? ex.getMessage() : bundle.getString("message.moveException.text");
                JOptionPane.showMessageDialog(parentComponent, msg, bundle.getString("message.exception.title"), JOptionPane.ERROR_MESSAGE);
                shellProgressMonitor.cancel();
            }
        }*/
    }
    
    protected void startTransfer(JComponent source, JComponent target, int action, List<File> sourceList, File targetFile) {
        if (action == TransferHandler.COPY) {
            ShellProgressMonitor shellProgressMonitor = new ShellProgressMonitor(source);
            try {
                shellProgressMonitor.start(ShellProgressMonitor.COPY,sourceList,targetFile);
            } catch (Exception ex) {
                String msg = ex.getMessage().trim().length() > 0 ? ex.getMessage() : bundle.getString("message.copyException.text");
                JOptionPane.showMessageDialog(source, msg, bundle.getString("message.exception.title"), JOptionPane.ERROR_MESSAGE);
                shellProgressMonitor.cancel();
            }
            
            if (target instanceof ViewComponent) {
                ((ViewComponent)target).getViewModel().refresh();
            } else if (target instanceof ShellTreeView) {
                ShellTreeView treeView = (ShellTreeView)target;
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)treeView.getLastSelectedPathComponent();
                if (targetNode != null) {
                    ((ShellTreeViewModel)treeView.getModel()).reload(targetNode);
                }
            }
        } else if (action == TransferHandler.MOVE) {
            ShellProgressMonitor shellProgressMonitor = new ShellProgressMonitor(source);
            try {
                shellProgressMonitor.start(ShellProgressMonitor.MOVE,sourceList,targetFile);
            } catch (Exception ex) {
                String msg = ex.getMessage().trim().length() > 0 ? ex.getMessage() : bundle.getString("message.moveException.text");
                JOptionPane.showMessageDialog(source, msg, bundle.getString("message.exception.title"), JOptionPane.ERROR_MESSAGE);
                shellProgressMonitor.cancel();
            }
            
            if (target instanceof ViewComponent) {
                ((ViewComponent)target).getViewModel().refresh();
            } else if (target instanceof ShellTreeView) {
                ShellTreeView treeView = (ShellTreeView)target;
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)treeView.getLastSelectedPathComponent();
                if (targetNode != null) {
                    ((ShellTreeViewModel)treeView.getModel()).reload(targetNode);
                }
            }
        }
    }
}
