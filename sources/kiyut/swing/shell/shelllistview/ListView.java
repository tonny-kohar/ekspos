/*
 * ListView.java
 *
 * Created on April 24, 2002, 10:05 AM
 */

package kiyut.swing.shell.shelllistview;

import java.applet.Applet;

import java.awt.*;
import java.awt.event.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.io.File;
import java.io.IOException;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;

import kiyut.swing.shell.event.*;
import kiyut.swing.shell.util.*;

/** <code>ListView</code> is a view for <code>ShellListView</code> which display
 * the <code>ShellListViewModel</code> in a list like view.
 *
 * @author  tonny
 */
public class ListView extends JList implements ViewComponent {
    /** editor remover */
    private PropertyChangeListener      editorRemover = null;
    
    /** the component use for editing cells */
    private Component       editorComp;
    
    /** the editor for the cells */
    protected ListCellEditor cellEditor;
    
    /** Identifies the index of the cell being edited. */
    protected int editingIndex = -1;
    
    /** Constructs a <code>ListView</code> using the given data model
     * @param dataModel data model for this component
     */
    public ListView(ShellListViewModel dataModel) {
        super(dataModel);
        
        ListCellRenderer cellRenderer = new ListCellRenderer(dataModel.getFileSystemView());
        
        cellEditor = new ListCellEditor();
        cellEditor.addCellEditorListener(new javax.swing.event.CellEditorListener() {
            public void editingCanceled(ChangeEvent evt) { ListView.this.editingCanceled(evt); }
            public void editingStopped(ChangeEvent evt) { ListView.this.editingStopped(evt); }
        });
        
        addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                onListSelectionChanged(e);
            }
        });
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(ComponentEvent evt) { onComponentResized(evt); }
        });
        
        setFont(UIManager.getFont("Table.font"));
        setLayoutOrientation(JList.VERTICAL_WRAP);
        setCellRenderer(cellRenderer);
    }
    
    /** {@inheritDoc} */
    public ShellListViewModel getViewModel() {
        return (ShellListViewModel)getModel();
    }
    
    /** set editing index
     *@param index the editing index
     */
    private void setEditingIndex(int index) {
        editingIndex = index;
    }
    
    /** Returns the index of the model that contains the cell currently being edited. 
     * If nothing is being edited, returns -1.
     * @return index or -1
     */
    public int getEditingIndex() {
        return editingIndex;
    }
    
    /** Returns the <code>ListCellEditor</code> used by this component to edit values for the cells.
     * @return <code>ListCellEditor</code> used to edit values for the cells
     */
    public ListCellEditor getCellEditor() {
        return cellEditor;
    }
    
    /** Discards the editor object and frees the real estate it used for
     * cell rendering.
     */
    private void removeEditor() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
        removePropertyChangeListener("permanentFocusOwner", editorRemover);
        editorRemover = null;
        
        ListCellEditor editor = getCellEditor();
        if(editor != null) {
            //editor.removeCellEditorListener(this);
            
            if (editorComp != null) {
                remove(editorComp);
            }
            
            int index = getEditingIndex();
            Rectangle cellRect = getCellBounds(index,index);
            
            setEditingIndex(-1);
            editorComp = null;
            
            if (cellRect != null) { repaint(cellRect); }
        }
    }
    
    
    /** Returns true if a cell is being edited.
     * @return  true if editing a cell
     */
    public boolean isEditing() {
        boolean b = false;
        ShellListViewModel dataModel = (ShellListViewModel)getModel();
        if (dataModel.getState() == ShellListViewModel.EDIT) { b = true; }
        return b;
        
    }
    
    /** start editing cell programatically
     * @param index the index of data model to be edited
     * @return true if editing is started; false otherwise
     * @throws IndexOutOfBoundsException if an invalid index was given
     */
    public boolean editCellAt(int index) {
        boolean b = false;
        
        if (editorRemover == null) {
            KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            editorRemover = new CellEditorRemover(fm);
            fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);
        }
        
        ShellListViewModel dataModel = (ShellListViewModel)getModel();
        
        if ( (index < 0) || (index >= dataModel.getRowCount()) ) {
            throw new IndexOutOfBoundsException("invalid index");
        }
        
        ListSelectionModel selectionModel = getSelectionModel();
        selectionModel.clearSelection();
        selectionModel.setSelectionInterval(index,index);
        setEditingIndex(index);
        
        Object value = dataModel.getFile(index);
        editorComp = cellEditor.getListCellEditorComponent(this, value, true, index);
        editorComp.setBounds(getCellBounds(index,index));
        add(editorComp);
        editorComp.validate();
        editorComp.repaint();
        editorComp.requestFocusInWindow();
        
        b = true;
        return b;
    }
    
    /** Invoked when editing is canceled.
     * @param evt the event received
     * @see DetailView#editingCanceled(ChangeEvent)
     */
    public void editingCanceled(ChangeEvent evt) {
        ShellListViewModel dataModel = (ShellListViewModel)getModel();
        dataModel.setState(ShellListViewModel.BROWSE);
        removeEditor();
    }
    
    /** Invoked when editing is stopped.
     * @param evt the event received
     * @see DetailView#editingStopped(ChangeEvent)
     */
    public void editingStopped(ChangeEvent evt) {
        ShellListViewModel dataModel = (ShellListViewModel)getModel();
        dataModel.setState(ShellListViewModel.BROWSE);
        removeEditor();
    }
    
    //////////////////////////
    // event handling
    /////////////////////////
    
    /** Invoked when the component's size changes.
     * @param e a <code>ComponentEvent</code> object
     */
    private void onComponentResized(ComponentEvent evt) {
        if (getModel().getSize() <= 0) {
            return;
        }
        
        int rowCount = 8; // 8 as default
        Rectangle rect  = getCellBounds(0,0);
        Rectangle size = getVisibleRect();
        double d = size.getHeight()/rect.getHeight();
        if (d > rowCount) {
            rowCount = (int)d;
            rowCount = rowCount - 1;
        }
        setVisibleRowCount(rowCount);
    }
    
    /** Called whenever the value of the selection changes.
     * @param e the event that characterizes the change.
     */
    private void onListSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) { return; }
        getCellEditor().cancelCellEditing();
    }
    
    // This class tracks changes in the keyboard focus state. It is used
    // when the ListView is editing to determine when to cancel the edit.
    // If focus switches to a component outside of the ListView, but in the
    // same window, this will cancel editing.
    public class CellEditorRemover implements PropertyChangeListener {
        KeyboardFocusManager focusManager;
        
        public CellEditorRemover(KeyboardFocusManager fm) {
            this.focusManager = fm;
        }
        
        /** {@inheritDoc} */
        public void propertyChange(PropertyChangeEvent evt) {
            if (!isEditing() || getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) {
                return;
            }
            
            Component c = focusManager.getPermanentFocusOwner();
            while (c != null) {
                if (c == ListView.this) {
                    // focus remains inside the list
                    return;
                } else if ((c instanceof Window) || (c instanceof Applet && c.getParent() == null)) {
                    if (c == SwingUtilities.getRoot(ListView.this)) {
                        if (!getCellEditor().stopCellEditing()) {
                            getCellEditor().cancelCellEditing();
                        }
                    }
                    break;
                }
                c = c.getParent();
            }
        }
    }
    
}
