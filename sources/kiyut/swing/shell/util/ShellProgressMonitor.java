/*
 * ShellProgressMonitor.java
 *
 * Created on March 1, 2005, 1:40 PM
 */

package kiyut.swing.shell.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.event.*;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileSystemView;
import kiyut.swing.shell.event.ShellEvent;
import kiyut.swing.shell.event.ShellListener;



/** ShellProgressMonitor is a Shell progress utility for Move,Copy,or Delete files.
 * It will open up a dialog showing the progress of status.
 * User can stop the progress by clicking on the cancel button,
 * however, files which already moved/copied/deleted is not cancelable.
 * On Exception, do not forget to call cancel
 * <br>
 * TODO: add i18n support
 *
 * @author Kiyut
 */
public class ShellProgressMonitor {
    /** COPY */
    public static int COPY = 0;
    /** MOVE */
    public static int MOVE = 1;
    /** DELETE */
    public static int DELETE = 2;
    
    /** the <code>FileSystemView</code> for this component */
    private FileSystemView fsv;
    
    /** a boolean value indicated canceled action */
    private boolean canceled = false;
    
    /** the parent component for this component, use to instantiate Dialog */
    private Component parentComponent;
    
    /** Progress Dialog for this component */
    private ShellProgressDialog progressDialog;
    
    /** an EventListenerList object */
    private EventListenerList listenerList;
    
    
    /** Creates a new instance of ShellProgressMonitor */
    public ShellProgressMonitor(Component parentComponent) {
        this(parentComponent,FileSystemView.getFileSystemView());
    }
    
    /** Constructs a <code>ShellProgressMonitor</code>
     * @param parentComponent determines the Frame in which the dialog is displayed
     * @param fsv FileSystemView
     */
    public ShellProgressMonitor(Component parentComponent, FileSystemView fsv) {
        this.parentComponent = parentComponent;
        this.fsv = fsv;
        
        listenerList = new EventListenerList();
    }
    
    /** Sets the file system view that this model uses for accessing and creating file system resources.
     * @param fsv the new FileSystemView
     */
    public void setFileSystemView(FileSystemView fsv) {
        this.fsv = fsv;
    }
    
    /** Return the progress status is canceled or not
     * @return true or false
     */
    public boolean isCanceled() {
        return canceled;
    }
    
    /** Adds a listener for Shell events.
     * @param l the ShellListener that will be notified on the ShellEvent
     */
    public void addShellListener(ShellListener l) {
        listenerList.add(ShellListener.class, l);
    }
    
    /** Removes ShellListener
     * @param l ShellListener to remove
     */
    public void removeShellListener(ShellListener l) {
        listenerList.remove(ShellListener.class, l);
    }
    
    /** Notifies all listeners that have registered interest for notification on this event type.
     * @param file a file object which has been deleted.
     * @param type ShellEvent Type
     */
    private void fireShellEvent(File file, int type) {
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        ShellEvent shellEvent = new ShellEvent(this,type,file,null);
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ShellListener.class) {
                if (type == ShellEvent.SHELL_DELETED) {
                    ((ShellListener)listeners[i+1]).shellDeleted(shellEvent);
                } else if (type == ShellEvent.SHELL_COPIED) {
                    ((ShellListener)listeners[i+1]).shellCopied(shellEvent);
                } else if (type == ShellEvent.SHELL_MOVED) {
                    ((ShellListener)listeners[i+1]).shellMoved(shellEvent);
                }
            }
        }
    }
    
    /** Cancel the progress */
    public void cancel() {
        canceled = true;
        closeDialog();
    }
    
    /** close the progress dialog
     */
    private void closeDialog() {
        if (progressDialog != null) {
            progressDialog.setVisible(false);
            progressDialog.dispose();
            progressDialog = null;
        }
    }
    
    /** Recursively add file into the List
     */
    private void addRecursive(List<File> fileList, File file) {
        if (canceled == true) { return; }
        
        if (file.isDirectory()) {
            File[] files = fsv.getFiles(file,false);
            for (int i=0; i<files.length; i++) {
                addRecursive(fileList,files[i]);
            }
        }
        fileList.add(file);
    }
    
    /** Start the Progress. It include the sub dir processing.
     * <strong>Important:</strong> the source files must from the base/parent directory
     * <strong>on exception do not forget to call cancel</strong>
     *
     * @param action MOVE, COPY, or DELETE
     * @param sourceList The list of files
     * @param dest The destination directory if action is MOVE or COPY otherwise set to null
     * @throws IOException if an I/O error occurs.
     * @see #cancel
     */
    public void start(int action, List<File> sourceList, File dest) throws IOException {
        if (action != COPY && action != MOVE && action != DELETE) {
            throw new IllegalArgumentException("invalid action");
        }
        
        Window window = (Window)SwingUtilities.getRoot(parentComponent);
        if (window instanceof Frame) {
            progressDialog = new ShellProgressDialog((Frame)window,false);
        } else {
            progressDialog = new ShellProgressDialog((Dialog)window,false);
        }
        
        if (action == COPY) {
            copy(sourceList,dest);
        } else if (action == MOVE) {
            move(sourceList,dest);
        } else if (action == DELETE) {
            delete(sourceList);
        }
        
        closeDialog();
    }
    
    /** Move implementation
     * @param sourceList list of files to be deleted
     * @param dest The destination directory
     * @throws IOException if an I/O error occurs.
     */
    protected void move(List<File> sourceList, File dest) throws IOException {
        progressDialog.setTitle("Move...");
        progressDialog.setMinimum(0);
        progressDialog.setValue(0);
        progressDialog.setFileText("Preparing...");
        progressDialog.setFromText("");
        
        progressDialog.setVisible(true);
        progressDialog.paintImmediately();
        
        canceled = false;
        
        progressDialog.setMaximum(sourceList.size());
        
        boolean b;
        for (int i=0; i<sourceList.size(); i++) {
            if (canceled == true) {
                break;
            }
            File sourceFile = sourceList.get(i);
            File parent = fsv.getParentDirectory(sourceFile);
            progressDialog.setFileText(fsv.getSystemDisplayName(sourceFile));
            if (parent != null) {
                progressDialog.setFromText("From " + fsv.getSystemDisplayName(parent));
            } else {
                progressDialog.setFromText("");
            }
            progressDialog.setValue(i);
            progressDialog.paintImmediately();
            
            File destFile = new File(dest,sourceFile.getName());
            
            if(destFile.exists() && destFile.isDirectory() == false && sourceFile.isDirectory() == false) {
                // open dialog to ask to overwrite
                int choice =  FileReplacePane.showDialog(progressDialog,fsv,destFile,sourceFile);
                if (choice == JOptionPane.CANCEL_OPTION) {
                    canceled = true;
                    break;
                } else if (choice == JOptionPane.NO_OPTION) {
                    continue;
                }
            } 
            
            moveFile(sourceFile,destFile);
            
            fireShellEvent(sourceFile,ShellEvent.SHELL_MOVED);
        }
    }
    
    /** Copy implementation
     * @param sourceList list of files to be deleted
     * @param dest The destination directory
     * @throws IOException if an I/O error occurs.
     */
    protected void copy(List<File> sourceList, File dest) throws IOException {
        progressDialog.setTitle("Copying...");
        progressDialog.setMinimum(0);
        progressDialog.setValue(0);
        progressDialog.setFileText("Preparing...");
        progressDialog.setFromText("");
        
        progressDialog.setVisible(true);
        progressDialog.paintImmediately();
        
        canceled = false;
        
        List<File> baseList = new ArrayList<File>();
        List<File> copyList = new ArrayList<File>();
        for (int i=0; i<sourceList.size(); i++) {
            File file = sourceList.get(i);
            File parent = file.getParentFile();
            if (parent != null) {
                if (baseList.contains(parent) == false) {
                    baseList.add(parent);
                }
            }
            addRecursive(copyList,file);
        }
        
        progressDialog.setMaximum(copyList.size());
        
        boolean b;
        for (int i=0; i<copyList.size(); i++) {
            if (canceled == true) {
                break;
            }
            File sourceFile = copyList.get(i);
            File parent = fsv.getParentDirectory(sourceFile);
            progressDialog.setFileText(fsv.getSystemDisplayName(sourceFile));
            if (parent != null) {
                progressDialog.setFromText("From " + fsv.getSystemDisplayName(parent));
            } else {
                progressDialog.setFromText("");
            }
            progressDialog.setValue(i);
            progressDialog.paintImmediately();
            
            File destFile = resolveDestFile(dest,sourceFile,baseList);
            
            if(destFile.exists() && destFile.isDirectory()==false) {
                // open dialog to ask to overwrite
                int choice =  FileReplacePane.showDialog(progressDialog,fsv,destFile,sourceFile);
                if (choice == JOptionPane.CANCEL_OPTION) {
                    canceled = true;
                    break;
                } else if (choice == JOptionPane.NO_OPTION) {
                    continue;
                }
            } else {
                File parentDir = destFile.getParentFile();
                if (parentDir != null) {
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                }
                if (!sourceFile.isDirectory()) {
                    destFile.createNewFile();
                }
            }
            
            if (!sourceFile.isDirectory()) {
                copyFile(sourceFile,destFile);
            }
            
            if (sourceList.contains(sourceFile)) {
                fireShellEvent(sourceFile,ShellEvent.SHELL_COPIED);
            }
        }
    }
    
    /** Delete implementation
     * @param sourceList list of files to be deleted
     * @throws IOException if an I/O error occurs.
     */
    protected void delete(List<File> sourceList) throws IOException {
        progressDialog.setTitle("Deleting...");
        progressDialog.setMinimum(0);
        progressDialog.setValue(0);
        progressDialog.setFileText("Preparing...");
        progressDialog.setFromText("");
        
        progressDialog.setVisible(true);
        progressDialog.paintImmediately();
        
        canceled = false;
        
        List<File> deleteList = new ArrayList<File>();
        for (int i=0; i<sourceList.size(); i++) {
            addRecursive(deleteList,sourceList.get(i));
        }
        
        progressDialog.setMaximum(deleteList.size());
        
        boolean b;
        for (int i=0; i<deleteList.size(); i++) {
            if (canceled == true) {
                break;
            }
            File file = deleteList.get(i);
            File parent = fsv.getParentDirectory(file);
            progressDialog.setFileText(fsv.getSystemDisplayName(file));
            if (parent != null) {
                progressDialog.setFromText("From " + fsv.getSystemDisplayName(parent));
            } else {
                progressDialog.setFromText("");
            }
            progressDialog.setValue(i);
            progressDialog.paintImmediately();
            
            b = file.delete();
            if (b == false) {
                throw new IOException("Unable to delete " + file);
            }
            
            
            if (sourceList.contains(file)) {
                fireShellEvent(file,ShellEvent.SHELL_DELETED);
            }
        }
    }
    
    private File resolveDestFile(File destFile, File sourceFile, List<File> baseList) {
        File file = null;
        
        String child = sourceFile.getName();
        String path = sourceFile.getAbsolutePath();
        for (int i=0; i < baseList.size(); i++) {
            File baseFile = baseList.get(i);
            if (path.startsWith(baseFile.getAbsolutePath())) {
                child = path.substring(baseFile.getAbsolutePath().length());
                break;
            }
        }
        
        file = new File(destFile,child);
        return file;
    }
    
    private void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if(source != null) { source.close(); }
            if(destination != null) { destination.close(); }
        }
    }
    
    private void moveFile(File sourceFile, File destFile) throws IOException {
        boolean b = sourceFile.renameTo(destFile);
        if (b == false) {
            throw new IOException("Unable to move " + sourceFile);
        }
    }
    
    /** DeleteProgressDialog with progress bar and cancel button
     */
    public class ShellProgressDialog extends JDialog {
        private JLabel fileLabel;
        private JLabel fromLabel;
        private JButton cancelButton;
        private JProgressBar progressBar;
        private JPanel panel;
        
        /** Constructs a <code>ShellProgressDialog</code>
         * @param parent determines the Dialog in which the dialog is displayed
         * @param modal true for a modal dialog, false for one that allows other windows to be active at the same time
         */
        public ShellProgressDialog(Dialog parent, boolean modal) {
            super(parent, modal);
            initComponents();
        }
        
        /** Constructs a <code>ShellProgressDialog</code>
         * @param parent determines the Frame in which the dialog is displayed
         * @param modal true for a modal dialog, false for one that allows other windows to be active at the same time
         */
        public ShellProgressDialog(Frame parent, boolean modal) {
            super(parent, modal);
            initComponents();
        }
        
        /** initialize the component */
        private void initComponents() {
            setTitle("Progress...");
            setResizable(false);
            
            panel = new JPanel();
            panel.setLayout(null);
            panel.setPreferredSize(new Dimension(380,140));
            getContentPane().add(panel,BorderLayout.CENTER);
            
            fileLabel = new JLabel("");
            fileLabel.setBounds(15, 60, 350, 15);
            panel.add(fileLabel);
            
            fromLabel = new JLabel("");
            fromLabel.setBounds(15, 80, 350, 15);
            panel.add(fromLabel);
            
            progressBar = new JProgressBar();
            progressBar.setBounds(15, 100, 250, 14);
            panel.add(progressBar);
            
            JButton cancelButton = new JButton(UIManager.getString("OptionPane.cancelButtonText"));
            cancelButton.setBounds(280, 100, 75, 25);
            cancelButton.setActionCommand("cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });
            cancelButton.requestFocus();
            panel.add(cancelButton);
            
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    closeDialog();
                }
            });
            
            pack();
        }
        
        /** Paint immediately */
        public void paintImmediately() {
            Dimension dim = getPreferredSize();
            panel.paintImmediately(0,0,(int)dim.getWidth(),(int)dim.getHeight());
        }
        
        /** Sets the progress bar's minimum value
         * @param min the new minimum
         */
        public void setMinimum(int min) {
            progressBar.setMinimum(min);
        }
        
        /** Sets the progress bar's maximum value
         * @param max the new maximum
         */
        public void setMaximum(int max) {
            progressBar.setMaximum(max);
        }
        
        /** Sets the progress bar's current value
         * @param n the new value
         */
        public void setValue(int n) {
            progressBar.setValue(n);
        }
        
        /** Sets the from text
         * @param txt the new text
         */
        public void setFromText(String txt) {
            fromLabel.setText(txt);
        }
        
        /** Sets the file text
         * @param txt the file text
         */
        public void setFileText(String txt) {
            fileLabel.setText(txt);
        }
    }
}
