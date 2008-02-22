package kiyut.swing.shell.shelllistview;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import kiyut.swing.shell.io.DirectoryFilter;
import kiyut.swing.shell.io.FileOnlyFilter;
import kiyut.swing.shell.io.ShellFilter;


/** The data model for {@code ShellListView}
 * The default values for fileHidingEnabled is false and sortBy is name.
 * to know when the refresh / change directory occur listen for {@code ListModel.contentsChanged()}
 * where {@code ListDataEvent.getIndex0()} and {@code ListDataEvent.getIndex1()} equal with {@code ShellListViewModel.ALL_INDEX}
 *
 * @version 1.0
 * @author  tonny
 */
public class ShellListViewModel extends AbstractTableModel implements ListModel, Serializable {
    /** the all index */
    public static final int ALL_INDEX       =   TableModelEvent.HEADER_ROW;
    
    /** the name column */
    public static final int NAME            = 0;
    
    /** the size column */
    public static final int SIZE            = 1;
    
    /** the type column */
    public static final int TYPE            = 2;
    
    /** the modified column */
    public static final int MODIFIED        = 3;
    
    /** the browse state */
    public static final int BROWSE          = 0;
    
    /** the edit state */
    public static final int EDIT            = 1;
    
    /** the delete state */
    public static final int DELETE          = 2;
    
    /** the column names for this model */
    protected String[] columnNames = {"Name","Size","Type","Modified"};
    
    /** the data for this model */
    protected List<File> data;
    
    /** list data listener */
    protected EventListenerList listDataListeners; 
    
    /** the <code>FileSystemView</code> for this model */
    protected FileSystemView fsv;
    
    /** the use file hiding for this model */
    protected boolean useFileHiding;
    
    /** the sortBy use by this model */
    protected int sortBy;
    
    /** the state for this model */
    protected int state;
    
    /** the current directory of this model */
    protected File directory;
    
    /** Constructs a <code>ShellListViewModel</code> pointing to the user's default directory. */
    public ShellListViewModel() {
        this(FileSystemView.getFileSystemView());
    }
    
    /** Constructs a <code>ShellListViewModel</code> using the given <code>FileSystemView</code>.
     * @param fsv FileSystemView
     */
    public ShellListViewModel(FileSystemView fsv) {
        this(fsv.getHomeDirectory(),fsv,false,NAME);
    }
    
    /** Constructs a <code>ShellListViewModel</code> using the given directory
     * and <code>FileSystemView</code> and initialize it with <code>showHidden</code>
     * and <code>sortBy</code> parameter
     * @param directory directory to point to
     * @param fsv FileSystemView
     * @param useFileHiding  the boolean value that determines whether file hiding is turned on
     * @param sortBy sort the model using
     */
    public ShellListViewModel(File directory, FileSystemView fsv, boolean useFileHiding, int sortBy) {
        this.fsv = fsv;
        this.directory = directory;
        this.useFileHiding = useFileHiding;
        this.sortBy = sortBy;
        listDataListeners = new EventListenerList();
        
        data = Collections.synchronizedList(new ArrayList<File>());
    }
    
    /** {@inheritDoc} */
    public int getSize() {
        return getRowCount();
    }
    
    /** {@inheritDoc} */
    public int getColumnCount() {
        return columnNames.length;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    /** {@inheritDoc} */
    public int getRowCount() {
        return data.size();
    }
    
    /** {@inheritDoc} */
    public Object getValueAt(int row, int col) {
        Object object = null;
        File file = data.get(row);
        switch(col) {
            case NAME:
                object = file;
                break;
            case SIZE:
                if (fsv.isTraversable(file).booleanValue()) {
                    object= new Long(0);
                } else {
                    object = new Long(file.length());
                }
                break;
            case TYPE:
                String str = fsv.getSystemTypeDescription(file);
                if (str == null) { str = ""; }
                object = str;
                break;
            case MODIFIED:
                object = new Date(file.lastModified());
                break;
        }
        return object;
    }
    
    /**
     * Returns the value at the specified index.
     * <blockquote>
     * <b>Note:</b> Although this method is not deprecated, the preferred
     *    method to use is <code>getFile(int)</code> or <code>getValueAt(int,int)</code>
     * </blockquote>
     * @param      index   the index being queried
     * @return     the value at the specified index
     * @exception  IndexOutOfBoundsException  if an invalid index was given 
     * @see #getFile(int)
     * @see #getValueAt(int,int)
     */
    public Object getElementAt(int index) {
        return getValueAt(index, 0);
    }
    
    /** Return the file at specified index
     * @param index - the index being queried
     * @return file at specified index
     * @throws  IndexOutOfBoundsException  if an invalid index or column was given 
     * @see #getElementAt(int)
     * @see #getValueAt(int,int)
     */
    public File getFile(int index) {
        return (File)getValueAt(index,0);
    }
    
    /** Refresh the data model
     */
    public synchronized void refresh() {
        data.clear();
        
        // Group the folder & file
        File[] fileArray = fsv.getFiles(directory,useFileHiding);
        List<File> dirs = ShellFilter.filter(new DirectoryFilter(fsv),fileArray);
        List<File> files = ShellFilter.filter(new FileOnlyFilter(fsv),fileArray);
        
        switch (sortBy) {
            case NAME:
                Collections.sort(dirs);
                Collections.sort(files);
                break;
            case SIZE:
                Collections.sort(dirs,new FileSizeComparator());
                Collections.sort(files,new FileSizeComparator());
                break;
            case TYPE:
                Collections.sort(dirs,new FileTypeComparator());
                Collections.sort(files,new FileTypeComparator());
                break;
            case MODIFIED:
                Collections.sort(dirs,new FileModifiedComparator());
                Collections.sort(files,new FileModifiedComparator());
                break;
        }
        
        data.addAll(dirs);
        data.addAll(files);
        
        fireTableDataChanged();
        fireContentsChanged(ALL_INDEX,ALL_INDEX);
    }
    
    /** Rename file at specified index with the specied filename.
     * The filename string will be appended with the current directory
     * this model point to
     * @param index the index being queried
     * @param filename the new filename
     * @return true if and only if the renaming succeeded; false otherwise
     * @throws SecurityException If a security manager exists and its SecurityManager.checkDelete(java.lang.String) method denies delete access to the file
     * @exception IndexOutOfBoundsException if an invalid index was given 
     */
    public synchronized boolean renameFile(int index, String filename) throws SecurityException {
        boolean b = false;
        File file = getFile(index);
        File dest = new File(getCurrentDirectory() + File.separator + filename);
        b = file.renameTo(dest);
        if (b == true) {
            data.set(index,dest);
            fireTableRowsUpdated(index,index);
            fireContentsChanged(index,index);
        }
        return b;
    }
    
    /** Removes the element at the specified position in this model. 
     * @param index the index of the element to removed
     * @return Returns the element that was removed from the model.
     */
    public Object remove(int index) {
        Object object = data.remove(index);
        fireTableRowsDeleted(index,index);
        fireIntervalRemoved(index, index);
        return object;
    }
    
    /** Searches for the first occurence of the given argument, testing for equality using the equals method.
     * @param elem an object.
     * @return the index of the first occurrence of the argument in this model; returns -1 if the object is not found.
     */
    public int indexOf(Object elem) {
        return data.indexOf(elem);
    }
    
    /** Returns true if the file (directory) can be visited. Returns false if the directory cannot be traversed.
     * @param index the index being queried
     * @return true if the file/directory can be traversed, otherwise false
     */
    public boolean isTraversable(int index) {
        return fsv.isTraversable(getFile(index)).booleanValue();
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
    
    /** return the sort by value
     * @return the status of sort by
     */
    public int getSortBy() {
        return this.sortBy;
    }
    
    /** Sets the sort by used by this model
     * @param sortBy the value for sorting this model
     */
    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
    }
    
    /** Returns the current directory.
     * @return the current directory
     *
     * @see #setCurrentDirectory(java.io.File)
     */
    public File getCurrentDirectory() {
        return this.directory;
    }
    
    /** Sets the current directory. Passing in <code>null</code> sets the <code>ShellListView</code> 
     * to point to the user's default directory. 
     * This default depends on the operating system. It is typically the "My Documents" folder on Windows, 
     * and the user's home directory on Unix.
     * @param directory the current directory to point to
     *
     * @see #getCurrentDirectory()
     */
    public void setCurrentDirectory(File directory) {
        if (directory == null) { 
            this.directory = fsv.getHomeDirectory(); 
        } else { 
            this.directory = directory; 
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
    
    /** return this model state
     * @return One of the following constants defined in <code>ShellListViewModel</code>: BROWSE, EDIT, DELETE
     *
     * @see #setState(int)
     */
    public int getState() {
        return this.state;
    }
    
    /** Sets this model state
     * @param state One of the following constants defined in <code>ShellListViewModel</code>: BROWSE, EDIT, DELETE
     * 
     * @see #getState()
     */
    public void setState(int state) {
        this.state = state;
    }
    
    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be added
     */
    public void addListDataListener(ListDataListener l) {
        listDataListeners.add(ListDataListener.class, l);
    }
    
    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be removed
     */
    public void removeListDataListener(ListDataListener l) {
        listDataListeners.remove(ListDataListener.class, l);
    }
    
    /** Notifies all listeners that data in the range [index0, index1], inclusive, have been changed.
     * @param index0 one end of the new interval
     * @param index1 the other end of the new interval
     * @see EventListenerList
     */
    public void fireContentsChanged(int index0, int index1) {
        Object[] listeners = listDataListeners.getListenerList();
        ListDataEvent e = null;
        
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index0, index1);
                }
                ((ListDataListener)listeners[i+1]).contentsChanged(e);
            }
        }
    }
    
    /** Notifies all listeners that data in the range [index0, index1], inclusive, have been inserted.
     * @param index0 one end of the new interval
     * @param index1 the other end of the new interval
     * @see EventListenerList
     */
    public void fireIntervalAdded(int index0, int index1) {
        Object[] listeners = listDataListeners.getListenerList();
        ListDataEvent e = null;
        
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index0, index1);
                }
                ((ListDataListener)listeners[i+1]).intervalAdded(e);
            }
        }
    }
    
    /** Notifies all listeners that data in the range [index0, index1], inclusive, have been deleted.
     * @param index0 one end of the new interval
     * @param index1 the other end of the new interval
     * @see EventListenerList
     */
    public void fireIntervalRemoved(int index0, int index1) {
        Object[] listeners = listDataListeners.getListenerList();
        ListDataEvent e = null;
        
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0, index1);
                }
                ((ListDataListener)listeners[i+1]).intervalRemoved(e);
            }
        }
    }
    
    /** use to compare file based on size  */
    private class FileSizeComparator implements Comparator<File> {
        public int compare(File file1, File file2) {
            int comp;
            
            if (file1.length() < file2.length()) {
                comp =  -1;
            } else if (file1.length() == file2.length()) {
                comp = file1.compareTo(file2);
            } else {
                comp = 1;
            }
            
            return comp;
        }
    }
    
    /** use to compare file based on size */
    private class FileTypeComparator implements Comparator<File> {
        public int compare(File file1, File file2) {
            int comp;
            
            String desc1 = fsv.getSystemTypeDescription(file1);
            String desc2 = fsv.getSystemTypeDescription(file2);
            
            if (desc1 == null) { desc1 = ""; }
            if (desc2 == null) { desc2 = ""; }
            
            comp = desc1.compareTo(desc2);
            if (comp == 0) {
                comp = file1.compareTo(file2);
            }
            
            return comp;
        }
        
    }
    
    /** use to compare file based on last modified */
    private class FileModifiedComparator implements Comparator<File> {
        public int compare(File file1, File file2) {
            int comp;
            
            if (file1.lastModified() < file2.lastModified()) {
                comp =  -1;
            } else if (file1.lastModified() == file2.lastModified()) {
                comp = file1.compareTo(file2);
            } else {
                comp =  1;
            }
            
            return comp;
        }
    }
}

