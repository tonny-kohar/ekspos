/*
 * ShellListView.java
 *
 * Created on April 20, 2002, 11:29 PM
 */

package kiyut.swing.shell.shelllistview;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileSystemView;

/** ShellListView is a component which show the file system
 * it have 4 view: detail, list, icon, thumbnail
 *
 * @version 1.0
 * @author  tonny
 */
public class ShellListView extends JComponent {
    /** the detail view style */
    public static final int VS_DETAIL = 0;
    
    /** the list view style */
    public static final int VS_LIST = 1;
    
    /** the icon view style */
    public static final int VS_ICON = 2;
    
    /** the thumbnail view style */
    public static final int VS_THUMBNAIL = 3;
    
    /** the data model of <code>ShellListView</code> */
    protected ShellListViewModel dataModel;
    
    /** the selection model of <code>ShellListView</code> */
    protected ShellListViewSelectionModel selectionModel;
    
    /** the scroll pane of <code>ShellListView</code> */
    protected JScrollPane scrollPane;
    
    /** the detail view of <code>ShellListView</code> */
    protected DetailView detailView;
    
    /** the list view of <code>ShellListView</code> */
    protected ListView listView;
    
    /** the icon view of <code>ShellListView</code> */
    protected IconView iconView;
    
    /** the thumbnail view of <code>ShellListView</code> */
    protected ThumbnailView thumbnailView;
    
    /** the view style of <code>ShellListView</code> */
    protected int viewStyle;
    
    /** Constructs a <code>ShellListView</code> with all default values */
    public ShellListView() {
        this(FileSystemView.getFileSystemView().getHomeDirectory(),FileSystemView.getFileSystemView(),true,VS_DETAIL,true);
    }
    
    /** Constructs a <code>ShellListViewModel</code> and initialize with the passing parameter
     * @param dir start directory
     * @param fsv fileSystemView
     * @param useFileHiding the boolean value that determines whether file hiding is turned on
     * @param viewStyle VS_DETAIL,VS_LIST,VS_ICON,VS_THUMBNAIL
     * @param thumbnailEnabled the boolean value that determines whether the thumbnail view is enabled
     * @see ThumbnailView#setEnabled(boolean)
     */
    public ShellListView(File dir, FileSystemView fsv, boolean useFileHiding, int viewStyle, boolean thumbnailEnabled) {
        ViewMouseHandler viewMouseHandler = new ViewMouseHandler();
        ViewKeyHandler viewKeyHandler = new ViewKeyHandler();
        
        dataModel = new ShellListViewModel(fsv);
        selectionModel = new ShellListViewSelectionModel(dataModel);
        
        detailView = new DetailView(dataModel);
        detailView.setSelectionModel(selectionModel);
        detailView.addMouseListener(viewMouseHandler);
        detailView.addKeyListener(viewKeyHandler);
        
        listView = new ListView(dataModel);
        listView.setSelectionModel(selectionModel);
        listView.addMouseListener(viewMouseHandler);
        listView.addKeyListener(viewKeyHandler);
        
        iconView = new IconView(dataModel);
        iconView.setSelectionModel(selectionModel);
        iconView.addMouseListener(viewMouseHandler);
        iconView.addKeyListener(viewKeyHandler);
        
        thumbnailView = new ThumbnailView(dataModel);
        thumbnailView.setSelectionModel(selectionModel);
        thumbnailView.setEnabled(thumbnailEnabled);
        thumbnailView.addMouseListener(viewMouseHandler);
        thumbnailView.addKeyListener(viewKeyHandler);
        
        dataModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void contentsChanged(ListDataEvent evt) { onContentsChanged(evt); }
            public void intervalAdded(ListDataEvent evt) { /* do nothing */ }
            public void intervalRemoved(ListDataEvent evt) { /* do nothing */ }
        });
        
        scrollPane = new JScrollPane();
        
        setFileHidingEnabled(useFileHiding);
        setViewStyle(viewStyle);
        
        setBackground(UIManager.getColor("List.background"));
        setForeground(UIManager.getColor("List.foreground"));
        setFont(UIManager.getFont("Table.font"));
        scrollPane.getViewport().setBackground(getBackground());
        
        setLayout(new BorderLayout());
        add(scrollPane,BorderLayout.CENTER);
        
        refresh();
        
        //updateUI();
    }
    
    /** {@inheritDoc} */
    /*public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //scrollPane.getViewport().setBackground(getBackground());
    }*/
    
    
    
    /** Returns the <code>ShellListViewModel</code> that provides the data displayed by this component.
     * @return the <code>ShellListViewModel</code> that provides the data displayed by this component.
     */
    public ShellListViewModel getModel() {
        return dataModel;
    }
    
    /** Returns the last selected index.
     * This is a convenience method which delegates to ShellListViewSelectionModel.
     * @return the selected index or -1 if selection is empty and value is adjusting
     * @see #getLastSelectedFile
     */
    public int getLastSelectedIndex() {
        return selectionModel.getLastSelectedIndex();
    }
    
    /** Returns the last selected file.
     * This is a convenience method which delegates to ShellListViewSelectionModel.
     * @return the selected file or null if selection is empty and value is adjusting
     * @see #getLastSelectedIndex
     */
    public File getLastSelectedFile() {
        return selectionModel.getLastSelectedFile();
    }
    
    /** Return a list of selected files
     * This is a convenience method which delegates to ShellListViewSelectionModel.
     * @return a list of files or null if selection is empty and value is adjusting
     */
    public List<File> getSelectedFiles() {
        return selectionModel.getSelectedFiles();
    }
    
    /** Return a list of all files in the current directory
     * This is a convenience method which delegates to ShellListViewSelectionModel.
     * @return a list of all files or null if selection is empty and value is adjusting
     */
    public List<File> getAllFiles() {
        return selectionModel.getAllFiles();
    }
    
    /** refresh the data model
     */
    public void refresh() {
        dataModel.refresh();
    }
    
    /** Return the current view style.
     * @return One of the following constants defined in <code>ShellListView</code>: VS_DETAIL, VS_LIST, VS_ICON, VS_THUMBNAIL
     *
     * @see #setViewStyle(int)
     */
    public int getViewStyle() {
        return this.viewStyle;
    }
    
    /** Sets the view style.
     * @param viewStyle One of the following constants defined in <code>ShellListView</code>: VS_DETAIL, VS_LIST, VS_ICON, VS_THUMBNAIL
     * @throws IllegalArgumentException if <code>viewStyle</code> is an illegal viewStyle
     * @see #getViewStyle()
     */
    public void setViewStyle(int viewStyle) {
        if (viewStyle == VS_DETAIL) {
            scrollPane.setViewportView(detailView);
            this.viewStyle = viewStyle;
        } else if (viewStyle == VS_LIST) {
            scrollPane.setViewportView(listView);
            this.viewStyle = viewStyle;
        } else if (viewStyle == VS_ICON) {
            scrollPane.setViewportView(iconView);
            this.viewStyle = viewStyle;
        } else if (viewStyle == VS_THUMBNAIL) {
            scrollPane.setViewportView(thumbnailView);
            this.viewStyle = viewStyle;
        } else {
            throw new IllegalArgumentException("Illegal viewStyle");
        }
        
        revalidate();
    }
    
    /** Sets the current directory. Passing in <code>null</code> sets the <code>ShellListView</code>
     * to point to the user's default directory.
     * This default depends on the operating system. It is typically the "My Documents" folder on Windows,
     * and the user's home directory on Unix.
     * @param dir - the current directory to point to
     *
     * @see #getCurrentDirectory()
     * @see ShellListViewModel#setCurrentDirectory(java.io.File)
     * @see ShellListViewModel#getCurrentDirectory
     */
    public void setCurrentDirectory(File dir) {
        dataModel.setCurrentDirectory(dir);
    }
    
    /** Returns the current directory.
     * @return the current directory
     *
     * @see #setCurrentDirectory(java.io.File)
     * @see ShellListViewModel#setCurrentDirectory(java.io.File)
     * @see ShellListViewModel#getCurrentDirectory
     */
    public File getCurrentDirectory() {
        return dataModel.getCurrentDirectory();
    }
    
    /** Sets file hiding on or off. If true, hidden files are not shown.
     * @param useFileHiding the boolean value that determines whether file hiding is turned on
     *
     * @see #isFileHidingEnabled()
     * @see ShellListViewModel#setFileHidingEnabled(boolean)
     */
    public void setFileHidingEnabled(boolean useFileHiding) {
        dataModel.setFileHidingEnabled(useFileHiding);
    }
    
    /** Returns true if hidden files are not shown ; otherwise, returns false.
     * @return the status of the file hiding
     *
     * @see #setFileHidingEnabled(boolean)
     * @see ShellListViewModel#isFileHidingEnabled()
     */
    public boolean isFileHidingEnabled() {
        return dataModel.isFileHidingEnabled();
    }
    
    /** Return current ViewComponent
     * @return current ViewComponent
     * @see #getViewStyle()
     * @see #setViewStyle(int)
     */
    public ViewComponent getViewComponent() {
        Component comp = scrollPane.getViewport().getView();
        if (comp == null) { return null; }
        return (ViewComponent)comp;
    }
    
    /** Return the detail view of this component
     * @return detail view
     */
    public DetailView getDetailView() {
        return detailView;
    }
    
    /** Return the list view of this component
     * @return list view
     */
    public ListView getListView() {
        return listView;
    }
    
    /** Return the icon view of this component
     * @return icon view
     */
    public IconView getIconView() {
        return iconView;
    }
    
    /** Return the ThumbnailView view of this component
     * @return thumbnail view
     */
    public ThumbnailView getThumbnailView() {
        return thumbnailView;
    }
    
    /** Traves the data model based on selectionModel.getMaxSelectionIndex()
     */
    protected void traves() {
        int index = selectionModel.getMaxSelectionIndex();
        if ( (index>=0) && (dataModel.isTraversable(index)) ) {
            File file = dataModel.getFile(index);
            selectionModel.setValueIsAdjusting(true);
            dataModel.setCurrentDirectory(file);
            selectionModel.setValueIsAdjusting(false);
            dataModel.refresh();
        }
    }
    
    /** This is only a convenience method to setTransferHandler to each view component.
     * {@inheritDoc}
     */
    @Override
    public void setTransferHandler(TransferHandler newHandler) {
        detailView.setTransferHandler(newHandler);
        listView.setTransferHandler(newHandler);
        iconView.setTransferHandler(newHandler);
        thumbnailView.setTransferHandler(newHandler);
    }
    
    /** This is only a convenience method to setDragEnabled to each view component.
     * @param b true or false
     */
    public void setDragEnabled(boolean b) {
        detailView.setDragEnabled(b);
        listView.setDragEnabled(b);
        iconView.setDragEnabled(b);
        thumbnailView.setDragEnabled(b);
    }
    
    /**
     * Removes a listener from the list that's notified each time a
     * change to the selection model model occurs.
     *
     * @param l the <code>ListSelectionListener</code> to be removed
     */
    public void removeListSelectionListener(ListSelectionListener l) {
        selectionModel.removeListSelectionListener(l);
    }
    
    /**
     * Adds a listener to the list that's notified each time a change
     * to the selection model occurs.
     *
     * @param l the <code>ListSelectionListener</code> to be added
     */
    public void addListSelectionListener(ListSelectionListener l) {
        selectionModel.addListSelectionListener(l);
    }
    
    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be added
     */
    public void addListDataListener(ListDataListener l) {
        dataModel.addListDataListener(l);
    }
    
    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be removed
     */
    public void removeListDataListener(ListDataListener l) {
        dataModel.removeListDataListener(l);
    }
    
    /** Adds a listener to the list that's notified when the each view editor stops, or cancels editing.
     * This is a convenience method that wrap each view cellEditor.
     * @param l CellEditorListener
     */
    public void addCellEditorListener(CellEditorListener l) {
        detailView.getColumnModel().getColumn(0).getCellEditor().addCellEditorListener(l);
        listView.getCellEditor().addCellEditorListener(l);
        iconView.getCellEditor().addCellEditorListener(l);
        thumbnailView.getCellEditor().addCellEditorListener(l);
    }
    
    /** Removes a listener from the list that's notified of each view editor.
     * This is a convenience method that wrap each view cellEditor.
     * @param l CellEditorListener
     */
    public void removeCellEditorListener(CellEditorListener l) {
        detailView.getColumnModel().getColumn(0).getCellEditor().removeCellEditorListener(l);
        listView.getCellEditor().removeCellEditorListener(l);
        iconView.getCellEditor().removeCellEditorListener(l);
        thumbnailView.getCellEditor().removeCellEditorListener(l);
    }
    
    /** Start editing cell programatically. It delegate to the current view
     * @param index the index of data model to be edited
     * @return true if editing is started; false otherwise
     * @throws IndexOutOfBoundsException if an invalid index was given
     */
    public boolean editCellAt(int index) {
        boolean result = false;
        
        dataModel.setState(ShellListViewModel.EDIT);
        switch (viewStyle) {
            case VS_DETAIL:
                result = detailView.editCellAt(index,0);
                break;
            case VS_LIST:
                result = listView.editCellAt(index);
                break;
            case VS_ICON:
                result = iconView.editCellAt(index);
                break;
            case VS_THUMBNAIL:
                result = thumbnailView.editCellAt(index);
                break;
            default:
                result = false;
                break;
        }
        
        return result;
    }
    
    /** Overriden to redirect or forward to each view addMouseListener
     * It is a convenience method that wrap each view addMouseListener.
     * {@inheritDoc}
     */
    public void addMouseListener(MouseListener l) {
        detailView.addMouseListener(l);
        listView.addMouseListener(l);
        iconView.addMouseListener(l);
        thumbnailView.addMouseListener(l);
    }
    
    /** Overriden to redirect or forward to each view removeMouseListener
     * It is a convenience method that wrap each view removeMouseListener.
     * {@inheritDoc}
     */
    public void removeMouseListener(MouseListener l) {
        detailView.removeMouseListener(l);
        listView.removeMouseListener(l);
        iconView.removeMouseListener(l);
        thumbnailView.removeMouseListener(l);
    }
    
    /** Overriden to redirect or forward to each view addKeyListener
     * It is a convenience method that wrap each view addKeyListener.
     * {@inheritDoc}
     */
    public void addKeyListener(KeyListener l) {
        detailView.addKeyListener(l);
        listView.addKeyListener(l);
        iconView.addKeyListener(l);
        thumbnailView.addKeyListener(l);
    }
    
    /** Overriden to redirect or forward to each view removeKeyListener
     * It is a convenience method that wrap each view removeKeyListener.
     * {@inheritDoc}
     */
    public void removeKeyListener(KeyListener l) {
        detailView.removeKeyListener(l);
        listView.removeKeyListener(l);
        iconView.removeKeyListener(l);
        thumbnailView.removeKeyListener(l);
    }
    
    ////////////////////////////////////////////
    // EVENT HANDLER
    ///////////////////////////////////////////
    
    /** Sent when the contents of the data model has changed in
     * a way that's too complex to characterize.
     * For example, this is sent when an item has been replaced.
     * Index0 and index1 bracket the change.
     * @param e a <code>ListDataEvent</code> encapsulating the event information
     */
    private void onContentsChanged(ListDataEvent e) {
        // changeAll
        if ((e.getIndex0()==ShellListViewModel.ALL_INDEX) &&  (e.getIndex1()==ShellListViewModel.ALL_INDEX) ){
            return;
        }
        
        if (e.getIndex0() != e.getIndex1()) {
            return;
        }
        int index = e.getIndex1();
        if (dataModel.getSize() >= index) {
            selectionModel.setSelectionInterval(index,index);
        }
    }
    
    private class ViewMouseHandler extends MouseInputAdapter {
        @Override
        public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2) {
                traves();
            }
        }
        
    }
    
    private class ViewKeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent evt) {
            if (evt.getKeyCode()==KeyEvent.VK_ENTER) {
                traves();
            }
        }
    }
}
