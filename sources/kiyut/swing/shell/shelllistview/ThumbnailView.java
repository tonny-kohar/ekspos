/*
 * ThumbnailView.java
 *
 * Created on December 18, 2002, 4:29 PM
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

import java.net.URI;

import java.security.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.TooManyListenersException;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;

import org.w3c.dom.*;

import kiyut.imageio.*;
import kiyut.swing.dnd.*;
import kiyut.swing.shell.event.*;
import kiyut.swing.shell.image.ImageUtilities;
import kiyut.swing.shell.util.*;


/** <code>ThumbnailView</code> is a view for <code>ShellListView</code> which display
 * the <code>ShellListViewModel</code> in a thumbnail like view.
 * It loads the image on the background and using <code>ImageIO</code> to load the image progressively.
 * When this component is disabled, it stop the background image loading.
 * The chace directory and content is to meet the http://www.freedesktop.org specification regarding thumbnail cache.
 * <b>important:<b>when setUseCache to true make sure cache directory is valid.
 *
 * @version 1.0
 * @author  tonny
 */
public class ThumbnailView extends JComponent implements Scrollable, ViewComponent {
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
    protected List<ThumbnailCell> cells;
    
    /** the list of file to be loaded */
    protected List<File> loadList;
    
    /** the editor for the cells */
    protected ThumbnailCellEditor cellEditor;
    
    /** Identifies the index of the cell being edited. */
    protected int editingIndex = -1;
    
    /** this component background color */
    protected Color background;
    
    /** this component selection background color */
    protected Color selectionBackground;
    
    /** this component selection foreground color */
    protected Color selectionForeground;
    
    /** this component dragEnabled, initialy false */
    protected boolean dragEnabled = false;
    
    /** the thumbnail thread */
    protected Thread thumbnailThread;
    
    /** boolean indicating flag use cache for thumbnail */
    protected boolean useCache = false;
    
    /** cache directory location */
    protected File cacheDirectory = null;
    
    /** md5 generator for file cache */
    protected MessageDigest md5;
    
    /** ImageReaderWRiterPreferences */
    protected ImageReaderWriterPreferences imageReaderWriterPreferences;
    
    /** Constructs a <code>ThumbnailView</code> using the given data model
     * @param dataModel data model for this component
     */
    public ThumbnailView(ShellListViewModel dataModel) {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception ex) {}
        
        cells = new ArrayList<ThumbnailCell>();
        loadList = Collections.synchronizedList(new ArrayList<File>());
        
        cellEditor = new ThumbnailCellEditor();
        cellEditor.addCellEditorListener(new javax.swing.event.CellEditorListener() {
            public void editingCanceled(ChangeEvent evt) { ThumbnailView.this.editingCanceled(evt); }
            public void editingStopped(ChangeEvent evt) { ThumbnailView.this.editingStopped(evt); }
        });
        
        
        this.dataModel = dataModel;
        dataModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void contentsChanged(ListDataEvent evt) { onContentsChanged(evt); }
            public void intervalAdded(ListDataEvent evt) { onIntervalAdded(evt); }
            public void intervalRemoved(ListDataEvent evt) { onIntervalRemoved(evt); }
        });
        
        // DnD Support
        setTransferHandler(new TransferHandler(""));
        ThumbnailViewDragGestureRecognizer dragRecognizer = new ThumbnailViewDragGestureRecognizer();
        addMouseListener(dragRecognizer);
        addMouseMotionListener(dragRecognizer);
        
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) { calculatePreferredSize(); }
        });
        
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
        
        thumbnailThread = new ThumbnailThread();
        thumbnailThread.start();
    }
    
    /** Overriden to provide add Custom dropTargetListener
     * {@inheritDoc}
     */
    public void setTransferHandler(TransferHandler newHandler) {
        super.setTransferHandler(newHandler);
        try {
            getDropTarget().addDropTargetListener(new ThumbnailViewDropTargetListener());
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
    
    /** Enables or disables this component, depending on the value of the parameter b.
     * An enabled component can respond to user input and generate events.
     * Components are enabled initially by default.
     * When this component is disabled, the background image loading also disabled
     * @param b If true, this component is enabled; otherwise this component is disabled
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean b) {
        if (b == true) {
            synchronized(loadList) {
                loadList.notifyAll();
            }
        }
        super.setEnabled(b);
    }
    
    /**
     * Sets the <code>dragEnabled</code> property,
     * which must be <code>true</code> to enable
     * automatic drag handling (the first part of drag and drop)
     * on this component.
     * The <code>transferHandler</code> property needs to be set
     * to a non-<code>null</code> value for the drag to do
     * anything.  The default value of the <code>dragEnabled</code
     * property is <code>false</code>.
     * @param b the value to set the <code>dragEnabled</code> property to
     * @exception HeadlessException if
     *            <code>b</code> is <code>true</code> and
     *            <code>GraphicsEnvironment.isHeadless()</code>
     *            returns <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see #getDragEnabled
     * @see #setTransferHandler
     * @see TransferHandler
     */
    public void setDragEnabled(boolean b) {
        if (b && GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        dragEnabled = b;
    }
    
    /**
     * Gets the value of the <code>dragEnabled</code> property.
     *
     * @return  the value of the <code>dragEnabled</code> property
     */
    public boolean getDragEnabled() {
        return dragEnabled;
    }
    
    /** refresh the data model */
    protected void refresh() {
        this.removeAll();
        this.repaint();
        
        selectionModel.clearSelection();
        cells.clear();
        loadList.clear();
        
        FileSystemView fsv = dataModel.getFileSystemView();
        
        for (int i=0; i < dataModel.getSize(); i++) {
            File file = (File)dataModel.getFile(i);
            ThumbnailCell cell = createCell(file,fsv);
            
            loadList.add(file);
            cells.add(cell);
            this.add(cell);
        }
        
        // set preferredSize
        calculatePreferredSize();
        revalidate();
        repaint();
        
        synchronized(loadList) {
            loadList.notifyAll();
        }
        
    }
    
    /** Returns an <code>ThumbnailCell</code> instance using the given parameter
     * @param file File
     * @param fsv FileSystemView
     * @return an <code>ThumbailCell</code> instance.
     */
    protected ThumbnailCell createCell(File file,FileSystemView fsv) {
        ThumbnailCell cell = new ThumbnailCell(this);
        updateCell(cell,file,fsv);
        return cell;
    }
    
    /** update the cell using the given parameter.
     * @param cell the cell to be updated
     * @param file File
     * @param fsv FileSystemView
     */
    protected void updateCell(ThumbnailCell cell,File file,FileSystemView fsv) {
        cell.setText(fsv.getSystemDisplayName(file));
        cell.setImageRescale(1.5, 1.5);
        cell.setImage(ImageUtilities.iconToBufferedImage(fsv.getSystemIcon(file)));
    }
    
    /** load the specified <code>File</code> image and register it with the cell
     * @param cell the cell to receive notification of image loading
     * @param file the file to be loaded
     */
    private void loadFileImage(ThumbnailCell cell, File file) {
        if (isEnabled() == false) {
            return;
        }
        
        File imgFile = file;
        File cacheFile = null;
        
        if (useCache == true) {
            // get the file to load whether cached or original file
            md5.update(file.toURI().toString().getBytes());
            byte[] bytes = md5.digest();
            String md5String  = ShellUtilities.bytesToHexString(bytes) + ".png";
            cacheFile = new File(cacheDirectory,md5String);
            try {
                if (isCacheValid(imgFile,cacheFile)) {
                    imgFile = cacheFile;
                }
            } catch (Exception e) {}
        }
        
        ImageReader reader = null;
        BufferedImage img = null;
        try {
            reader = getImageReader(imgFile);
            img = cell.readFileImage(reader, null);
            // to make the loading faster, don't need to load the whole image
            //ImageReadParam param = reader.getDefaultReadParam();
            //param.setSourceProgressivePasses(0,4);
            //cell.readFileImage(reader, param);
        } catch (IOException e) {
        } finally {
            if (reader != null) {
                if (reader.getInput() instanceof ImageInputStream) {
                    try {
                        ((ImageInputStream)reader.getInput()).close();
                    }   catch (Exception e) { }
                }
                reader.dispose();
                reader = null;
            }
        }
        
        if (useCache == true && !imgFile.equals(cacheFile) && img != null) {
            if (!(img.getWidth() <= 128 && img.getHeight() <= 128)) {
                try {
                    BufferedImage smallImg = cell.getImage();
                    writeImageCache(smallImg,cacheFile, file.lastModified(), file.toURI());
                    //ImageIO.write(img,"png",cacheFile);
                } catch (Exception ex) {}
            }
        }
    }
    
    /** determine whether the file has cache and the cache is valid
     * MTime == lastModified and URI = fileURI.
     * It is follow http://www.freedesktop.org thumbnail cache specification.
     * @param file file to check
     * @param cacheFile file cache to compare
     * @return true if valid, otherwise false
     * @throw IOException if io error occur
     */
    private boolean isCacheValid(File file,File cacheFile) throws IOException {
        boolean valid = false;
        
        if (cacheFile.exists() == false) {
            return valid;
        }
        
        ImageInputStream iis = ImageIO.createImageInputStream(cacheFile);
        Iterator readers = ImageIO.getImageReadersBySuffix("png");
        ImageReader reader = (ImageReader)readers.next();
        reader.setInput(iis, true);
        
        String cacheMTime = null;
        String cacheURI = null;
        
        IIOMetadata metadata = reader.getImageMetadata(0);
        IIOMetadataNode root = (IIOMetadataNode)metadata.getAsTree(metadata.getNativeMetadataFormatName());
        NodeList nodeList = root.getElementsByTagName("tEXtEntry");
        for (int i=0; i<nodeList.getLength(); i++) {
            IIOMetadataNode node = (IIOMetadataNode)nodeList.item(i);
            if (node.getAttribute("keyword").equals("Thumb::MTime")) {
                cacheMTime = node.getAttribute("value");
            }
            if (node.getAttribute("keyword").equals("Thumb::URI")) {
                cacheURI = node.getAttribute("value");
            }
        }
        
        try {
            iis.close();
        } catch (Exception e) { }
        
        if (cacheMTime == null || cacheURI == null) {
            return valid;
        }
        
        String mTime = Long.toString(file.lastModified());
        String uri = file.toURI().toString();
        if (cacheURI.equals(uri) && cacheMTime.equals(mTime)) {
            valid = true;
        }
        
        return valid;
    }
    
    /** Write image to cache
     * It is follow http://www.freedesktop.org thumbnail cache specification.
     *@param bi the <code>BufferedImage</code> to write
     *@param cacheFile the cache File
     *@param lastModified the last modified to put into Thumb:MTime
     *@param uri the uri to put into Thumb:URI
     *@throws IOException if io error occur
     */
    private void writeImageCache(BufferedImage bi, File cacheFile, long lastModified, URI uri) throws IOException {
        ImageWriter writer = null;
        if (imageReaderWriterPreferences != null) {
            writer = imageReaderWriterPreferences.getPreferredImageWriterByFormatName("png");
        }
        if (writer == null) {
            Iterator writers = ImageIO.getImageWritersByFormatName("png");
            writer = (ImageWriter)writers.next();
        }
        
        // set up metadata
        IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bi),null);
        Node node = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        Node tEXtNode = new IIOMetadataNode("tEXt");
        node.appendChild(tEXtNode);
        Element entryElt;
        entryElt = new IIOMetadataNode("tEXtEntry");
        entryElt.setAttribute("keyword", "Software");
        entryElt.setAttribute("value", "Kiyut Ekspos Image Viewer");
        tEXtNode.appendChild(entryElt);
        entryElt = new IIOMetadataNode("tEXtEntry");
        entryElt.setAttribute("keyword", "Thumb::MTime");
        entryElt.setAttribute("value", Long.toString(lastModified));
        tEXtNode.appendChild(entryElt);
        entryElt = new IIOMetadataNode("tEXtEntry");
        entryElt.setAttribute("keyword", "Thumb::URI");
        entryElt.setAttribute("value", uri.toString());
        tEXtNode.appendChild(entryElt);
        metadata.mergeTree(metadata.getNativeMetadataFormatName(), node);
        
        // create temp file
        File parentFile = cacheFile.getParentFile();
        File tmpFile = new File(parentFile, "temp" + Runtime.getRuntime().hashCode() + ".png");
        
        // write to temp file
        ImageOutputStream ios = ImageIO.createImageOutputStream(tmpFile);
        writer.setOutput(ios);
        IIOImage iioImage = new IIOImage(bi,null,metadata);
        writer.write(null,iioImage,null);
        ios.close();
        writer.dispose();
        
        // rename temp file to cache file
        tmpFile.renameTo(cacheFile);
    }
    
    /** return the <code>ImageReader</code> use to read the specified <code>file</code>
     * @return the <code>ImageReader</code>
     * @throws IOException If an I/O error occurs
     */
    private ImageReader getImageReader(File file) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(file);
        
        String suffix = ShellUtilities.getFileSuffix(file);
        ImageReader reader = null;
        if (imageReaderWriterPreferences != null) {
            reader = imageReaderWriterPreferences.getPreferredImageReaderBySuffix(suffix);
        }
        if (reader == null) {
            Iterator readers = ImageIO.getImageReadersBySuffix(suffix);
            reader = (ImageReader)readers.next();
        }
        reader.setInput(iis,true);
        
        return reader;
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
            throw new IllegalArgumentException("selectionModel must be non null");
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
    protected void calculatePreferredSize() {
        if ( (cells.size() > 0) && (this.isVisible()) ) {
            ThumbnailCell cell = cells.get(0);
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
    
    /** Returns the <code>ThumbnailCellEditor</code> used by this component to edit values for the cells.
     * @return <code>ThumbnailCellEditor</code> used to edit values for the cells
     */
    public ThumbnailCellEditor getCellEditor() {
        return cellEditor;
    }
    
    /** set editing index
     * @param index the editing index
     * @see #getEditingIndex
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
        JComponent editorComp = cellEditor.getThumbnailCellEditorComponent(this, value, true, index);
        ThumbnailCell cell = cells.get(index);
        cell.setCellEditor(editorComp);
        cell.startCellEditing();
        cell.validate();
        cell.repaint();
        
        b = true;
        
        return b;
    }
    
    /**Discards the editor object and frees the real estate it used for
     * cell rendering.
     */
    public void removeEditor() {
        if ((editingIndex>=0) && (editingIndex<cells.size())) {
            ThumbnailCell cell = cells.get(editingIndex);
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
        int i = 64;
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
    
    /** Sets a flag indicating whether a cache file should be used for storing thumbnail
     * if useCache is true make sure getCacheDirectory is not null by setCacheDirectory
     *@param useCache boolean indicating whether a cache file should be used.
     *@see #isUseCache
     *@see #setCacheDirectory(File)
     *@see #getCacheDirectory
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
    
    /** Returns the current value set by setUseCache.
     *@return true if use cache, otherwise false
     *@see #setUseCache(boolean)
     *@see #setCacheDirectory(File)
     *@see #getCacheDirectory
     */
    public boolean isUseCache() {
        return this.useCache;
    }
    
    /** Sets the directory where cache files are to be created.
     *If getUseCache returns false, this value is ignored.
     *@param cacheDirectory a File specifying a directory.
     *@throw IllegalArgumentException if cacheDirectory is not a directory.
     *@see #setUseCache(boolean)
     *@see #isUseCache
     *@see #getCacheDirectory
     */
    public void setCacheDirectory(File cacheDirectory) {
        if (cacheDirectory.isDirectory() == false) {
            throw new IllegalArgumentException(cacheDirectory.toString() + " is not a directory.");
        }
        
        this.cacheDirectory = cacheDirectory;
    }
    
    /** Returns the current value set by setCacheDirectory, or null if no explicit setting has been made.
     *@return a File indicating the directory where cache files will be created
     *@see #setUseCache(boolean)
     *@see #isUseCache
     *@see #setCacheDirectory(boolean)
     */
    public File getCacheDirectory() {
        return this.cacheDirectory;
    }
    
    /** set ImageReaderWriterPreferences for thumbnail encoding and decoding
     * @param prefs ImageReaderWriterPreferences
     * @see #getImageReaderWriterPreferences(ImageReaderWriterPreferences)
     * @see kiyut.imageio.ImageReaderWriterPreferences
     */
    public void setImageReaderWriterPreferences(ImageReaderWriterPreferences prefs) {
        this.imageReaderWriterPreferences = prefs;
    }
    
    /** Return ImageReaderWriterPreferences or null if not set
     * @return ImageReaderWriterPreferences or null
     * @see #setImageReaderWriterPreferences(ImageReaderWriterPreferences)
     * @see kiyut.imageio.ImageReaderWriterPreferences
     */
    public ImageReaderWriterPreferences getImageReaderWriterPreferences() {
        return imageReaderWriterPreferences;
    }
    
    //////////////////////////
    // event handling
    /////////////////////////
    
    /** Invoked when the mouse button has been clicked (pressed and released) on this component.
     * @param evt a <code>MouseEvent</code> object
     */
    private void onMouseClicked(MouseEvent evt) {
        if (isEnabled() == false) { return; }
        
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
    
    /** Invoked when a key has been pressed on this component
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
        
        Component cell = (Component)cells.get(index);
        MouseEvent mouseEvent = new MouseEvent(this,MouseEvent.MOUSE_CLICKED,
                evt.getWhen(),evt.getModifiers(),cell.getX(),cell.getY(),1,false,MouseEvent.BUTTON1);
        
        onMouseClicked(mouseEvent);
    }
    
    /** Sent when the contents of the data model has changed in
     * a way that's too complex to characterize.
     * For example, this is sent when an item has been replaced.
     * Index0 and index1 bracket the change.
     * @param e a <code>ListDataEvent</code> encapsulating the event information
     */
    private void onContentsChanged(ListDataEvent e) {
        // changeAll
        if ((e.getIndex0()==ShellListViewModel.ALL_INDEX) &&  (e.getIndex1()==ShellListViewModel.ALL_INDEX) ){
            refresh();
            return;
        }
        
        FileSystemView fsv = dataModel.getFileSystemView();
        for(int i=e.getIndex0(); i<=e.getIndex1(); i++) {
            File file = (File)dataModel.getFile(i);
            ThumbnailCell cell = cells.get(i);
            updateCell(cell,file,fsv);
            loadList.add(file);
        }
        
        /*int index = e.getIndex1();
        if (dataModel.getSize() >= index) {
            getSelectionModel().setSelectionInterval(index,index);
        }*/
        
        synchronized(loadList) {
            loadList.notifyAll();
        }
    }
    
    /** Sent after the indices in the index0,index1 interval have been inserted in the data model.
     * The new interval includes both index0 and index1.
     * @param e a <code>ListDataEvent</code> encapsulating the event information
     */
    private void onIntervalAdded(ListDataEvent e) {
        FileSystemView fsv = dataModel.getFileSystemView();
        for(int i=e.getIndex0(); i<=e.getIndex1(); i++) {
            File file = (File)dataModel.getFile(i);
            ThumbnailCell cell = createCell(file,fsv);
            this.add(cell,i);
            cells.add(i, cell);
        }
        calculatePreferredSize();
        revalidate();
        repaint();
    }
    
    /** Sent after the indices in the index0,index1 interval have been removed from the data model.
     * The interval includes both index0 and index1.
     * @param e a <code>ListDataEvent</code> encapsulating the event information
     */
    private void onIntervalRemoved(ListDataEvent e) {
        for(int i=e.getIndex0(); i<=e.getIndex1(); i++) {
            ThumbnailCell cell = cells.remove(i);
            this.remove(cell);
        }
        calculatePreferredSize();
        revalidate();
        repaint();
    }
    
    /** Called whenever the value of the selection changes.
     * @param e the event that characterizes the change.
     */
    private void onListSelectionChanged(ListSelectionEvent e) {
        cellEditor.cancelCellEditing();
        
        if (!selectionModel.isSelectionEmpty()) {
            Component comp = (Component)cells.get(selectionModel.getMaxSelectionIndex());
            scrollRectToVisible(comp.getBounds());
        }
        
        for (int i=0; i<cells.size(); i++) {
            ThumbnailCell cell = cells.get(i);
            if (selectionModel.isSelectedIndex(i)) {
                cell.setCellHasFocus(true);
            } else {
                cell.setCellHasFocus(false);
            }
            cell.repaint();
        }
    }
    
    
    
    
    /** thumbnail thread
     */
    private class ThumbnailThread extends Thread {
        /** run this thread */
        public void run() {
            while(true) {
                try {
                    synchronized(loadList) {
                        while ( (loadList.size()==0) || (!isEnabled()) )
                            loadList.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                while ((loadList.size() > 0) && (isEnabled())) {
                    try {
                        File file = (File)loadList.remove(0);
                        int indexOf = dataModel.indexOf(file);
                        if (indexOf != -1) {
                            ThumbnailCell cell = cells.get(indexOf);
                            if (ImageUtilities.isFileImage(file)) {
                                loadFileImage(cell,file);
                                this.yield();
                            }
                        }
                    } catch(Exception e) {}
                }
            }
        }
    }
    
    /** Property Change Handler for this component */
    private class PropertyChangeHandler implements PropertyChangeListener {
        /** This method gets called when a bound property is changed.
         * @param e  A <code>PropertyChangeEvent</code> object describing the event source
         *          and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            
            if (propertyName.equals("selectionBackground") || propertyName.equals("selectionForeground") ) {
                repaint();
            }
        }
    }
    
    protected class ThumbnailViewDragGestureRecognizer extends CustomDragGestureRecognizer {
        /** {@inheritDoc} */
        protected boolean isDragPossible(MouseEvent evt) {
            if (super.isDragPossible(evt)) {
                ThumbnailView comp = (ThumbnailView)evt.getSource();
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
    
    protected class ThumbnailViewDropTargetListener extends CustomDropTargetListener {
        private int[] selectedIndices;
        
        /** {@inheritDoc} */
        protected void saveComponentState(JComponent comp) {
            ThumbnailView viewComp = (ThumbnailView)comp;
            ShellListViewSelectionModel sm = (ShellListViewSelectionModel)viewComp.getSelectionModel();
            selectedIndices = sm.getSelectedIndices();
        }
        
        /** {@inheritDoc} */
        protected void restoreComponentState(JComponent comp) {
            ThumbnailView viewComp = (ThumbnailView)comp;
            ShellListViewSelectionModel sm = (ShellListViewSelectionModel)viewComp.getSelectionModel();
            sm.setSelectedIndices(selectedIndices);
        }
        
        /** {@inheritDoc} */
        protected void updateInsertionLocation(JComponent comp, Point p) {
            ThumbnailView viewComp = (ThumbnailView)comp;
            Component cell = getComponentAt((int)p.getX(),(int)p.getY());
            int index = cells.indexOf(cell);
            if (index != -1) {
                viewComp.getSelectionModel().setSelectionInterval(index, index);
            }
        }
    }
}
