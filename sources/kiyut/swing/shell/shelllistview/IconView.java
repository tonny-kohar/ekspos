/*
 * IconView.java
 *
 * Created on December 14, 2002, 2:36 PM
 */

package kiyut.swing.shell.shelllistview;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.awt.image.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.TooManyListenersException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;

import kiyut.swing.dnd.*;
import kiyut.swing.shell.event.*;
import kiyut.swing.shell.image.ImageUtilities;
import kiyut.swing.shell.util.*;


/** <code>IconView</code> is a view for <code>ShellListView</code> which display
 * the <code>ShellListViewModel</code> in an icon like view.
 *
 * @author  tonny
 */
public class IconView extends JComponent implements Scrollable, ViewComponent {
    /** the <code>PropertyChangeListener for this component */
    private PropertyChangeListener propertyChangeListener;
    
    /** the <code>FlowLayout</code> of this component, used to arrange the cells. */
    private FlowLayout flowLayout;
    
    /** Number of columns to create.*/
    private int columnCount = -1;
    
    /** The <code>ListSelectionModel</code> of this component , used to keep track of index selections. */
    protected ListSelectionModel selectionModel;
    
    /** The <code>ShellListViewModel</code> of the this component. */
    protected ShellListViewModel dataModel;
    
    /** the list of cells */
    protected List<IconCell> cells;
    
    /** the editor for the cells */
    protected IconCellEditor cellEditor;
    
    /** Identifies the index of the cell being edited. */
    protected int editingIndex = -1;
    
    /** this component selection background color */
    protected Color selectionBackground;
    
    /** this component selection foreground color */
    protected Color selectionForeground;
    
    /** this component dragEnabled, initialy false */
    protected boolean dragEnabled = false;
    
    /** Constructs a <code>IconView</code> using the given data model
     * @param dataModel data model for this component
     */
    public IconView(ShellListViewModel dataModel) {
        cells = new ArrayList<IconCell>();
        
        cellEditor = new IconCellEditor();
        cellEditor.addCellEditorListener(new javax.swing.event.CellEditorListener() {
            public void editingCanceled(ChangeEvent evt) { IconView.this.editingCanceled(evt); }
            public void editingStopped(ChangeEvent evt) { IconView.this.editingStopped(evt); }
        });
        
        this.dataModel = dataModel;
        dataModel.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent evt) { onContentsChanged(evt); }
            public void intervalAdded(ListDataEvent evt) { onIntervalAdded(evt); }
            public void intervalRemoved(ListDataEvent evt) { onIntervalRemoved(evt); }
        });
        
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) { calculatePreferredSize(); }
        });
        
        // DnD Support
        setTransferHandler(new TransferHandler(""));
        IconViewDragGestureRecognizer dragRecognizer = new IconViewDragGestureRecognizer();
        addMouseListener(dragRecognizer);
        addMouseMotionListener(dragRecognizer);
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) { onMouseClicked(evt); }
        });
        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) { onKeyPressed(evt); }
        });
        
        propertyChangeListener = new PropertyChangeHandler();
        addPropertyChangeListener(propertyChangeListener);
        
        flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        setPreferredSize(new Dimension(128, 128));
        setLayout(flowLayout);
        //setOpaque(true);
        
        setBackground(UIManager.getColor("List.background"));
        setForeground(UIManager.getColor("List.foreground"));
        setFont(UIManager.getFont("Table.font"));
        setSelectionBackground(UIManager.getColor("List.selectionBackground"));
        setSelectionForeground(UIManager.getColor("List.selectionForeground"));
    }
    
    /** Overriden to provide add Custom dropTargetListener
     * {@inheritDoc} 
     */
    public void setTransferHandler(TransferHandler newHandler) {
        super.setTransferHandler(newHandler);
        try {
            getDropTarget().addDropTargetListener(new IconViewDropTargetListener());
        } catch (TooManyListenersException ex) { }
    }
    
    /**
     * Returns the background color for selected cells.
     *
     * @return the <code>Color</code> used for the background of selected list items
     * @see #setSelectionBackground
     */
    public Color getSelectionBackground() {
        return selectionBackground;
    }
    
    
    /**
     * Sets the background color for selected cells.
     * <p>
     * The default value of this property is defined by the look
     * and feel implementation.
     * <p>
     * This is a JavaBeans bound property.
     *
     * @param selectionBackground  the <code>Color</code> to use for the
     *                             background of selected cells
     * @see #getSelectionBackground
     * @see #setSelectionForeground
     * @see #setForeground
     * @see #setBackground
     * @see #setFont
     */
    public void setSelectionBackground(Color selectionBackground) {
        Color oldValue = this.selectionBackground;
        this.selectionBackground = selectionBackground;
        firePropertyChange("selectionBackground", oldValue, selectionBackground);
    }
    
    /**
     * Returns the Foreground color for selected cells.
     *
     * @return the <code>Color</code> used for the Foreground of
     * selected list items
     * @see #setSelectionForeground(Color)
     */
    public Color getSelectionForeground() {
        return selectionForeground;
    }
    
    
    /**
     * Sets the Foreground color for selected cells.
     * <p>
     * The default value of this property is defined by the look
     * and feel implementation.
     * <p>
     * This is a JavaBeans bound property.
     *
     * @param selectionForeground the <code>Color</code> to use for the
     *                             foreground of selected cells
     * @see #getSelectionBackground
     * @see #setSelectionForeground
     */
    public void setSelectionForeground(Color selectionForeground) {
        Color oldValue = this.selectionForeground;
        this.selectionForeground = selectionForeground;
        firePropertyChange("selectionForeground", oldValue, selectionForeground);
    }
    
    /** {@inheritDoc} */
    public void setDragEnabled(boolean b) {
        if (b && GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        dragEnabled = b;
    }
    
    /** {@inheritDoc} */
    public boolean getDragEnabled() {
        return dragEnabled;
    }
    
    /** refresh the data model */
    protected void refresh() {
        this.removeAll();
        this.repaint();
        selectionModel.clearSelection();
        cells.clear();
        
        FileSystemView fsv = dataModel.getFileSystemView();
        
        for (int i=0; i < dataModel.getSize(); i++) {
            File file = (File)dataModel.getFile(i);
            IconCell cell = createCell(file,fsv);
            
            cells.add(cell);
            this.add(cell);
        }
        
        // set preferredSize
        calculatePreferredSize();
        revalidate();
        repaint();
    }
    
    /** Returns an <code>IconCell</code> instance using the given parameter
     * @param file File
     * @param fsv FileSystemView
     * @return an <code>IconCell</code> instance.
     */
    protected IconCell createCell(File file,FileSystemView fsv) {
        IconCell cell = new IconCell(this);
        updateCell(cell,file,fsv);
        return cell;
    }
    
    /** update the cell using the given parameter.
     * @param cell the cell to be updated
     * @param file File
     * @param fsv FileSystemView
     */
    protected void updateCell(IconCell cell,File file,FileSystemView fsv) {
        cell.setText(fsv.getSystemDisplayName(file));
        cell.setImageRescale(1.5, 1.5);
        cell.setImage(ImageUtilities.iconToBufferedImage(fsv.getSystemIcon(file)));
    }
    
    /** {@inheritDoc} */
    public ShellListViewModel getViewModel() {
        return dataModel;
    }
    
    /**
     * Sets the index selection model for this component to <code>selectionModel</code>
     * and registers for listener notifications from the new selection model.
     *
     * @param   selectionModel        the new selection model
     * @exception IllegalArgumentException      if <code>newModel</code> is <code>null</code>
     * @see     #getSelectionModel()
     */
    public void setSelectionModel(ListSelectionModel selectionModel) {
        if (selectionModel == null) {
            throw new IllegalArgumentException("Cannot set a null SelectionModel");
        }
        
        this.selectionModel = selectionModel;
        this.selectionModel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                onListSelectionChanged(e);
            }
        });
    }
    
    /**
     * Returns the value of the current selection model. The selection
     * model handles the task of making single selections, selections
     * of contiguous ranges, and non-contiguous selections.
     *
     * @return the <code>ListSelectionModel</code> that implements
     *					list selections
     * @see #setSelectionModel(ListSelectionModel)
     * @see ListSelectionModel
     */
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }
    
    /**
     * Returns true if the specified index is selected.
     * This is a convenience method that just delegates to the
     * <code>selectionModel</code>.
     *
     * @param index index to be queried for selection state
     * @return true if the specified index is selected
     * @see ListSelectionModel#isSelectedIndex
     */
    public boolean isSelectedIndex(int index) {
        return getSelectionModel().isSelectedIndex(index);
    }
    
    /** calculates the preferred size to arrange the icon
     */
    private void calculatePreferredSize() {
        if ( (cells.size() > 0) && (this.isVisible()) ) {
            IconCell cell = cells.get(0);
            double width = getVisibleRect().getWidth();
            columnCount = (int)(width / (cell.getPreferredSize().getWidth() + flowLayout.getVgap()));
            if (columnCount > 0) {
                int row = (int)(cells.size()/columnCount) + 1;
                double height = row * (cell.getPreferredSize().getHeight() + flowLayout.getHgap());
                Dimension dim = new Dimension((int)width,(int)height);
                setPreferredSize(dim);
            }
        }
    }
    
    /** Returns true if a cell is being edited.
     * @return  true if editing a cell
     */
    public boolean isEditing() {
        boolean b = false;
        ShellListViewModel dataModel = getViewModel();
        if (dataModel.getState() == ShellListViewModel.EDIT) { b = true; }
        return b;
        
    }
    
    /** Returns the <code>IconCellEditor</code> used by this component to edit values for the cells.
     * @return <code>IconCellEditor</code> used to edit values for the cells
     */
    public IconCellEditor getCellEditor() {
        return cellEditor;
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
    
    /** start editing cell programatically
     * @param index the index of data model to be edited
     * @return true if editing is started; false otherwise
     * @throws IndexOutOfBoundsException if an invalid index was given
     */
    public boolean editCellAt(int index) {
        boolean b = false;
        
        if ( (index < 0) || (index >= dataModel.getRowCount()) ) {
            throw new IndexOutOfBoundsException("invalid index");
        }
        
        selectionModel.clearSelection();
        selectionModel.setSelectionInterval(index,index);
        setEditingIndex(index);
        
        ShellListViewModel dataModel = getViewModel();
        Object value = dataModel.getFile(index);
        JComponent editorComp = cellEditor.getIconCellEditorComponent(this, value, true, index);
        IconCell cell = cells.get(index);
        cell.setCellEditor(editorComp);
        cell.startCellEditing();
        cell.validate();
        cell.repaint();
        
        b = true;
        
        return b;
    }
    
    /** Discards the editor object and frees the real estate it used for
     * cell rendering.
     */
    protected void removeEditor() {
        if ((editingIndex>=0) && (editingIndex<cells.size())) {
            IconCell cell = cells.get(editingIndex);
            cell.stopCellEditing();
            cell.removeCellEditor();
            editingIndex = -1;
            cell.validate();
        }
    }
    
    /** Invoked when editing is canceled.
     * @param evt the event received
     * @see DetailView#editingCanceled(ChangeEvent)
     */
    public void editingCanceled(ChangeEvent evt) {
        ShellListViewModel dataModel = getViewModel();
        dataModel.setState(ShellListViewModel.BROWSE);
        removeEditor();
    }
    
    /** Invoked when editing is stopped.
     * @param evt the event received
     * @see DetailView#editingStopped(ChangeEvent)
     */
    public void editingStopped(ChangeEvent evt) {
        ShellListViewModel dataModel = getViewModel();
        dataModel.setState(ShellListViewModel.BROWSE);
        removeEditor();
    }
    
    /** Returns the preferred size of the viewport for a view component.
     * For example the preferredSize of a JList component is the size
     * required to accommodate all of the cells in its list however the
     * value of preferredScrollableViewportSize is the size required for
     * JList.getVisibleRowCount() rows.   A component without any properties
     * that would effect the viewport size should just return
     * getPreferredSize() here.
     *
     * @return The preferredSize of a JViewport whose view is this Scrollable.
     * @see JViewport#getPreferredSize
     *
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    
    /** Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one block
     * of rows or columns, depending on the value of orientation.
     * <p>
     * Scrolling containers, like JScrollPane, will use this method
     * each time the user requests a block scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "block" increment for scrolling in the specified direction.
     *         This value should always be positive.
     * @see JScrollBar#setBlockIncrement
     *
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        int i = 16;
        return i;
    }
    
    /** Return true if a viewport should always force the height of this
     * Scrollable to match the height of the viewport.  For example a
     * columnar text view that flowed text in left to right columns
     * could effectively disable vertical scrolling by returning
     * true here.
     * <p>
     * Scrolling containers, like JViewport, will use this method each
     * time they are validated.
     *
     * @return True if a viewport should force the Scrollables height to match its own.
     *
     */
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    /** Return true if a viewport should always force the width of this
     * <code>Scrollable</code> to match the width of the viewport.
     * For example a normal
     * text view that supported line wrapping would return true here, since it
     * would be undesirable for wrapped lines to disappear beyond the right
     * edge of the viewport.  Note that returning true for a Scrollable
     * whose ancestor is a JScrollPane effectively disables horizontal
     * scrolling.
     * <p>
     * Scrolling containers, like JViewport, will use this method each
     * time they are validated.
     *
     * @return True if a viewport should force the Scrollables width to match its own.
     *
     */
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }
    
    /** Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one new row
     * or column, depending on the value of orientation.  Ideally,
     * components should handle a partially exposed row or column by
     * returning the distance required to completely expose the item.
     * <p>
     * Scrolling containers, like JScrollPane, will use this method
     * each time the user requests a unit scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "unit" increment for scrolling in the specified direction.
     *         This value should always be positive.
     * @see JScrollBar#setUnitIncrement
     *
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getScrollableBlockIncrement(visibleRect,orientation,direction);
    }
    
    
    //////////////////////////
    // event handling
    /////////////////////////
    
    
    /** Invoked when the mouse button has been clicked (pressed and released) on this component.
     * @param evt a <code>MouseEvent</code> object
     */
    private void onMouseClicked(MouseEvent evt) {
        requestFocusInWindow();
        
        if (evt.getClickCount() > 1) {
            return;
        }
        
        Component cell = getComponentAt(evt.getX(),evt.getY());
        
        if ( (evt.getClickCount() == 1) && (cell != null) ) {
            int index = cells.indexOf(cell);
            if (evt.isShiftDown()) {
                selectionModel.addSelectionInterval(selectionModel.getLeadSelectionIndex(),index);
            } else if (evt.isControlDown()) {
                selectionModel.addSelectionInterval(index,index);
            } else {
                //selectionModel.clearSelection();
                selectionModel.setSelectionInterval(index, index);
            }
        }
    }
    
    /** Invoked when a key has been pressed on a this component
     * @param evt a <code>KeyEvent</code> object
     */
    private void onKeyPressed(KeyEvent evt) {
        ShellListViewModel dataModel = getViewModel();
        if (dataModel.getState()!=ShellListViewModel.BROWSE) { return; }
        
        int index = getSelectionModel().getMaxSelectionIndex();
        int keyCode = evt.getKeyCode();
        if (keyCode==KeyEvent.VK_RIGHT) {
            index = index + 1;
        } else if (keyCode==KeyEvent.VK_LEFT) {
            index = index - 1;
        } else if (keyCode==KeyEvent.VK_UP) {
            index = index - columnCount;
        } else if (keyCode==KeyEvent.VK_DOWN) {
            index = index + columnCount;
        } else {
            return;
        }
        
        if ((index<0) || (index >= cells.size())) {
            return;
        }
        
        IconCell cell = cells.get(index);
        MouseEvent mouseEvent = new MouseEvent(this,MouseEvent.MOUSE_CLICKED,
                evt.getWhen(),evt.getModifiers(),cell.getX(),cell.getY(),1,false,MouseEvent.BUTTON1);
        
        onMouseClicked(mouseEvent);
    }
    
    /** Sent when the contents of the data model has changed in
     * a way that's too complex to characterize.
     * For example, this is sent when an item has been replaced.
     * Index0 and index1 bracket the change.
     * @param evt a <code>ListDataEvent</code> encapsulating the event information
     */
    private void onContentsChanged(ListDataEvent evt) {
        // changeAll
        if ((evt.getIndex0()==ShellListViewModel.ALL_INDEX) &&  (evt.getIndex1()==ShellListViewModel.ALL_INDEX) ){
            refresh();
            return;
        }
        
        FileSystemView fsv = dataModel.getFileSystemView();
        for(int i=evt.getIndex0(); i<=evt.getIndex1(); i++) {
            File file = (File)dataModel.getFile(i);
            IconCell cell = cells.get(i);
            updateCell(cell,file,fsv);
            cell.repaint();
        }
    }
    
    /** Sent after the indices in the index0,index1 interval have been inserted in the data model.
     * The new interval includes both index0 and index1.
     * @param evt a <code>ListDataEvent</code> encapsulating the event information
     */
    private void onIntervalAdded(ListDataEvent evt) {
        FileSystemView fsv = dataModel.getFileSystemView();
        for(int i=evt.getIndex0(); i<=evt.getIndex1(); i++) {
            File file = (File)dataModel.getFile(i);
            IconCell cell = createCell(file,fsv);
            this.add(cell,i);
            cells.add(i, cell);
        }
        calculatePreferredSize();
        revalidate();
        repaint();
    }
    
    /** Sent after the indices in the index0,index1 interval have been removed from the data model.
     * The interval includes both index0 and index1.
     * @param evt a <code>ListDataEvent</code> encapsulating the event information
     */
    private void onIntervalRemoved(ListDataEvent evt) {
        for(int i=evt.getIndex0(); i<=evt.getIndex1(); i++) {
            IconCell cell = cells.remove(i);
            this.remove(cell);
        }
        calculatePreferredSize();
        revalidate();
        repaint();
    }
    
    /** Called whenever the value of the selection changes.
     * @param evt the event that characterizes the change.
     */
    private void onListSelectionChanged(ListSelectionEvent evt) {
        cellEditor.cancelCellEditing();
        
        if (!selectionModel.isSelectionEmpty()) {
            Component comp = (Component)cells.get(selectionModel.getMaxSelectionIndex());
            scrollRectToVisible(comp.getBounds());
        }
        
        for (int i=0; i<cells.size(); i++) {
            IconCell cell = cells.get(i);
            if (selectionModel.isSelectedIndex(i)) {
                cell.setCellHasFocus(true);
            } else {
                cell.setCellHasFocus(false);
            }
            cell.repaint();
        }
    }
    
    /** Property Change Handler for this component */
    protected class PropertyChangeHandler implements PropertyChangeListener {
        /** {@inheritDoc} */
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            
            if (propertyName.equals("selectionBackground") || propertyName.equals("selectionForeground") ) {
                repaint();
            }
        }
    }
    
    protected class IconViewDragGestureRecognizer extends CustomDragGestureRecognizer {
        /** {@inheritDoc} */
        protected boolean isDragPossible(MouseEvent evt) {
            if (super.isDragPossible(evt)) {
                IconView comp = (IconView)evt.getSource();
                if (comp.getDragEnabled()) {
                    Component cell = getComponentAt(evt.getX(),evt.getY());
                    int index = cells.indexOf(cell);
                    if ((index != -1) && comp.isSelectedIndex(index)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    protected class IconViewDropTargetListener extends CustomDropTargetListener {
        private int[] selectedIndices;
        
        /** {@inheritDoc} */
        protected void saveComponentState(JComponent comp) {
            IconView iconView = (IconView)comp;
            ShellListViewSelectionModel sm = (ShellListViewSelectionModel)iconView.getSelectionModel();
            selectedIndices = sm.getSelectedIndices();
        }
        
        /** {@inheritDoc} */
        protected void restoreComponentState(JComponent comp) {
            IconView iconView = (IconView)comp;
            ShellListViewSelectionModel sm = (ShellListViewSelectionModel)iconView.getSelectionModel();
            sm.setSelectedIndices(selectedIndices);
        }
        
        /** {@inheritDoc} */
        protected void updateInsertionLocation(JComponent comp, Point p) {
            IconView iconView = (IconView)comp;
            Component cell = getComponentAt((int)p.getX(),(int)p.getY());
            int index = cells.indexOf(cell);
            if (index != -1) {
                iconView.getSelectionModel().setSelectionInterval(index, index);
            }
        }
    }
}

