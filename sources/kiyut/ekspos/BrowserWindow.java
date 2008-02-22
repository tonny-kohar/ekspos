package kiyut.ekspos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import kiyut.ekspos.prefs.BrowseWindowPreferences;
import kiyut.ekspos.prefs.GeneralPreferences;
import kiyut.ekspos.prefs.IIOPreferences;
import kiyut.ekspos.pycasa.PycasaManager;
import kiyut.swing.shell.event.ShellAdapter;
import kiyut.swing.shell.event.ShellEvent;
import kiyut.swing.shell.image.ImageUtilities;
import kiyut.swing.shell.shelllistview.ShellListView;
import kiyut.swing.shell.shelllistview.ShellListViewModel;
import kiyut.swing.shell.shelllistview.ThumbnailView;
import kiyut.swing.shell.shelltreeview.ShellTreeView;
import kiyut.swing.shell.shelltreeview.ShellTreeViewModel;
import kiyut.swing.shell.util.ShellProgressMonitor;
import kiyut.swing.statusbar.StatusBar;
import kiyut.swing.statusbar.StatusBarItem;
import kiyut.swing.util.MenuFactory;
import org.flexdock.docking.DockableFactory;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.perspective.LayoutSequence;
import org.flexdock.perspective.Perspective;
import org.flexdock.perspective.PerspectiveFactory;
import org.flexdock.perspective.PerspectiveManager;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;

/** BrowserWindow
 */
public class BrowserWindow extends JFrame {
    public static final String EXIT_ACTION_COMMAND = "Exit";
    public static final String CUT_ACTION_COMMAND = "Cut";
    public static final String COPY_ACTION_COMMAND = "Copy";
    public static final String PASTE_ACTION_COMMAND = "Paste";
    public static final String RENAME_ACTION_COMMAND = "Rename";
    public static final String DELETE_ACTION_COMMAND = "Delete";
    public static final String PREFERENCES_ACTION_COMMAND = "Preferences";
    public static final String DETAIL_VIEW_ACTION_COMMAND = "DetailView";
    public static final String LIST_VIEW_ACTION_COMMAND = "ListView";
    public static final String ICON_VIEW_ACTION_COMMAND = "IconView";
    public static final String THUMBNAIL_VIEW_ACTION_COMMAND = "ThumbnailView";
    public static final String RELOAD_ACTION_COMMAND = "Reload";
    public static final String RESET_PERSPECTIVE_ACTION_COMMAND = "ResetPerspective";
    public static final String LOCATION_ACTION_COMMAND = "Location";
    public static final String UP_ACTION_COMMAND = "Up";
    public static final String BACK_ACTION_COMMAND = "Back";
    public static final String FORWARD_ACTION_COMMAND = "Forward";
    public static final String HOME_ACTION_COMMAND = "Home";
    public static final String PICASA_LOGIN_ACTION_COMMAND = "PicasaLogin";
    public static final String PICASA_ADD_ACTION_COMMAND = "PicasaAdd";
    public static final String PICASA_MANAGE_ACTION_COMMAND = "PicasaManage";
    public static final String HELP_CONTENT_ACTION_COMMAND = "HelpContent";
    public static final String TOTD_ACTION_COMMAND = "TOTD";
    public static final String ABOUT_ACTION_COMMAND = "About";

    private static final String DEFAULT_PERSPECTIVE = "default.perspective";
    private static final String DOCKING_LIST_VIEW = "listview.view";
    private static final String DOCKING_TREE_VIEW = "treeview.view";
    private static final String DOCKING_PREVIEW_VIEW = "preview.view";
    
    private ResourceBundle bundle = ResourceBundle.getBundle("kiyut.ekspos.BrowserWindow");
    
    private Viewport viewport;
    private ShellTreeView shellTreeView;
    private ShellListView shellListView;
    private ImagePane imagePreviewPane;
    private Viewer viewer;
    
    private ShellProgressMonitor shellProgressMonitor;
    
    private JPopupMenu shellTreeViewPopupMenu;
    private JPopupMenu shellListViewPopupMenu;
    
    private JScrollPane shellTreeViewScroll = new JScrollPane();
    
    private StatusBar statusBar;
    
    private static final int HISTORY_SIZE = 9;
    private List<String> historyList = new ArrayList<String>(HISTORY_SIZE);
    
    private JComboBox locationCombo;
    private List<String> locationList = new ArrayList<String>(HISTORY_SIZE);
    
    private boolean shellListViewInProgress = false;
    private boolean shellTreeViewInProgress = false;
    private boolean backOrForwardInProgress = false;
    private int historyIndex = 0;
    
    /**Construct the frame*/
    public BrowserWindow() {
        setTitle(Application.getLongName());
        setIconImage(Application.getIconImage());
        
        BrowseWindowPreferences prefs = BrowseWindowPreferences.getInstance();
        int x = prefs.getX();
        int y = prefs.getY();
        int width = prefs.getWidth();
        int height = prefs.getHeight();
        
        this.setSize(width,height);
        if (!(x < 0 || y < 0)) {
            this.setBounds(x,y,width,height);
        }
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);
        
        shellTreeView  = new ShellTreeView();
        shellListView = new ShellListView();
        imagePreviewPane = new ImagePane();
        
        TransferHandler transferHandler = new BrowserTransferHandler();
        
        shellTreeView.setTransferHandler(transferHandler);
        shellTreeView.setDragEnabled(true);
        
        shellListView.setTransferHandler(transferHandler);
        shellListView.setDragEnabled(true);
        
        imagePreviewPane.setBorder(new EmptyBorder(3,3,3,3));
        
        shellTreeView.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent evt) {
                shellTreeViewValueChanged(evt);
            }
        });
        
        shellListView.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                shellListViewValueChanged(evt);
            }
        });
        
        shellListView.addListDataListener(new javax.swing.event.ListDataListener() {
            public void contentsChanged(ListDataEvent evt) {
                shellListViewContentsChanged(evt);
            }
            public void intervalAdded(ListDataEvent evt) {}
            public void intervalRemoved(ListDataEvent evt) {}
        });
        
        shellListView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) { shellListViewMouseClicked(evt); }
        });
        
        shellListView.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) { shellListViewKeyPressed(evt); }
        });
        
        
        shellProgressMonitor = new ShellProgressMonitor(this);
        shellProgressMonitor.addShellListener(new ShellAdapter() {
            @Override
            public void shellDeleted(ShellEvent evt) {  shellProgressMonitorShellDeleted(evt); }
            @Override
            public void shellMoved(ShellEvent evt) {  shellProgressMonitorShellMoved(evt); }
            //public void shellCopied(ShellEvent evt) {  shellProgressMonitorShellCopied(evt); }
        });
        
        
        shellTreeViewScroll.setViewportView(shellTreeView);
        shellTreeViewScroll.getViewport().setBackground(shellTreeView.getBackground());
        
        statusBar = new StatusBar();
        StatusBarItem statusBarItem = new StatusBarItem(" ",100,SwingConstants.CENTER);
        statusBar.addItem(statusBarItem);
        statusBarItem = new StatusBarItem(" ",150,SwingConstants.LEFT);
        statusBar.addItem(statusBarItem);
        contentPane.add(statusBar,BorderLayout.SOUTH);
        
        initActions();
        
        restoreFromPreferences();
        
        // need to put inside try..catch because docking is problematic
        try {
            initDocking();
        } catch (Exception ex) {
            // do nothing
        }
        
        //WindowManager.createInstance(this);
        
    }
    
    /** Initialize Actions, MenuBar and ToolBar
     */
    private void initActions() {
        Map<String,Action> actionMap = new HashMap<String,Action>();
        
        // create action
        actionMap.put(EXIT_ACTION_COMMAND,new BrowserAction(EXIT_ACTION_COMMAND));
        actionMap.put(CUT_ACTION_COMMAND,new BrowserAction(CUT_ACTION_COMMAND));
        actionMap.put(COPY_ACTION_COMMAND,new BrowserAction(COPY_ACTION_COMMAND));
        actionMap.put(PASTE_ACTION_COMMAND,new BrowserAction(PASTE_ACTION_COMMAND));
        actionMap.put(RENAME_ACTION_COMMAND,new BrowserAction(RENAME_ACTION_COMMAND));
        actionMap.put(DELETE_ACTION_COMMAND,new BrowserAction(DELETE_ACTION_COMMAND));
        actionMap.put(PREFERENCES_ACTION_COMMAND,new BrowserAction(PREFERENCES_ACTION_COMMAND));
        actionMap.put(DETAIL_VIEW_ACTION_COMMAND,new BrowserAction(DETAIL_VIEW_ACTION_COMMAND));
        actionMap.put(LIST_VIEW_ACTION_COMMAND,new BrowserAction(LIST_VIEW_ACTION_COMMAND));
        actionMap.put(ICON_VIEW_ACTION_COMMAND,new BrowserAction(ICON_VIEW_ACTION_COMMAND));
        actionMap.put(THUMBNAIL_VIEW_ACTION_COMMAND,new BrowserAction(THUMBNAIL_VIEW_ACTION_COMMAND));
        actionMap.put(RELOAD_ACTION_COMMAND,new BrowserAction(RELOAD_ACTION_COMMAND));
        actionMap.put(RESET_PERSPECTIVE_ACTION_COMMAND,new BrowserAction(RESET_PERSPECTIVE_ACTION_COMMAND));
        actionMap.put(LOCATION_ACTION_COMMAND,new BrowserAction(LOCATION_ACTION_COMMAND));
        actionMap.put(UP_ACTION_COMMAND,new BrowserAction(UP_ACTION_COMMAND));
        actionMap.put(BACK_ACTION_COMMAND,new BrowserAction(BACK_ACTION_COMMAND));
        actionMap.put(FORWARD_ACTION_COMMAND,new BrowserAction(FORWARD_ACTION_COMMAND));
        actionMap.put(HOME_ACTION_COMMAND,new BrowserAction(HOME_ACTION_COMMAND));
        actionMap.put(PICASA_LOGIN_ACTION_COMMAND,new BrowserAction(PICASA_LOGIN_ACTION_COMMAND));
        actionMap.put(PICASA_ADD_ACTION_COMMAND,new BrowserAction(PICASA_ADD_ACTION_COMMAND));
        actionMap.put(PICASA_MANAGE_ACTION_COMMAND,new BrowserAction(PICASA_MANAGE_ACTION_COMMAND));
        actionMap.put(HELP_CONTENT_ACTION_COMMAND,new BrowserAction(HELP_CONTENT_ACTION_COMMAND));
        actionMap.put(TOTD_ACTION_COMMAND,new BrowserAction(TOTD_ACTION_COMMAND));
        actionMap.put(ABOUT_ACTION_COMMAND,new BrowserAction(ABOUT_ACTION_COMMAND));
        
        // inititalize action from ResourceBundle
        MenuFactory.configureActionMap(bundle,actionMap);
        
        // initialize menuBar
        JMenuBar menuBar = MenuFactory.createMenuBar(bundle,actionMap);
        setJMenuBar(menuBar);
        
        // initialize navigation toolBar
        JPanel toolBarPane = new JPanel();
        toolBarPane.setLayout(new BorderLayout());
        getContentPane().add(toolBarPane, BorderLayout.NORTH);
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBarPane.add(toolBar,BorderLayout.NORTH);
        
        toolBar.add(createToolBarButton(actionMap.get(BACK_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(FORWARD_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(UP_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(HOME_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(RELOAD_ACTION_COMMAND)));
        toolBar.addSeparator();
        toolBar.add(createToolBarButton(actionMap.get(DETAIL_VIEW_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(LIST_VIEW_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(ICON_VIEW_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(THUMBNAIL_VIEW_ACTION_COMMAND)));
        toolBar.addSeparator();
        toolBar.add(createToolBarButton(actionMap.get(PREFERENCES_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(ABOUT_ACTION_COMMAND)));
        
        // initialize address toolBar
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBarPane.add(toolBar,BorderLayout.SOUTH);
        
        JLabel locationLabel = new JLabel("Location  ");
        
        locationCombo = new JComboBox();
        locationCombo.setEditable(true);
        Action action = actionMap.get(LOCATION_ACTION_COMMAND);
        locationCombo.setActionCommand((String)action.getValue(Action.ACTION_COMMAND_KEY));
        locationCombo.setAction(action);
        
        toolBar.add(locationLabel);
        toolBar.add(locationCombo);
        
        ////////////////////
        // ShellTreeView
        ///////////////////
        shellTreeViewPopupMenu = MenuFactory.createPopupMenu(bundle,actionMap,"ShellTreeViewPopupMenu");
        shellTreeView.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {}
            @Override
            public void mousePressed(MouseEvent evt) { shellTreeViewPopupTrigger(evt); }
            @Override
            public void mouseReleased(MouseEvent evt) { shellTreeViewPopupTrigger(evt); }
        });
        
        shellTreeView.getCellEditor().addCellEditorListener(new javax.swing.event.CellEditorListener() {
            public void editingStopped(ChangeEvent evt) { shellTreeViewEditingStopped(evt); }
            public void editingCanceled(ChangeEvent evt)  { }
        });
        
        
        ////////////////////
        // ShellListView
        ///////////////////
        shellListViewPopupMenu = MenuFactory.createPopupMenu(bundle,actionMap,"ShellListViewPopupMenu");
        shellListView.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {}
            @Override
            public void mousePressed(MouseEvent evt) { shellListViewPopupTrigger(evt); }
            @Override
            public void mouseReleased(MouseEvent evt) { shellListViewPopupTrigger(evt); }
        });
        
        shellListView.addCellEditorListener(new javax.swing.event.CellEditorListener() {
            public void editingStopped(ChangeEvent evt) { shellListViewEditingStopped(evt); }
            public void editingCanceled(ChangeEvent evt)  { }
        });
    }
    
    private JButton createToolBarButton(Action action) {
        JButton button = new JButton(action);
        button.setText("");
        button.setFocusable(false);
        //button.setBorderPainted(false);
        //button.setMargin(new Insets(2,2,2,2));
        return button;
    }
    
    private void initDocking() {
        // create docking view
        JPanel contentPane = (JPanel)getContentPane();
        
        viewport = new Viewport();
        contentPane.add(viewport, BorderLayout.CENTER);

        // setup the DockingManager to work with our application
        DockingManager.setDockableFactory(new ViewFactory());
        //DockingManager.setMainDockingPort(this,"BrowserWindow");
        
        PerspectiveManager.setFactory(new BrowserPerspectiveFactory());
        PerspectiveManager.setRestoreFloatingOnLoad(false);

        PerspectiveManager mgr = PerspectiveManager.getInstance();
        mgr.setCurrentPerspective(DEFAULT_PERSPECTIVE, false);
        mgr.reset(this);
        //DockingManager.restoreLayout();
        
    }
    
    public void restoreDocking() {
        BrowseWindowPreferences framePrefs = BrowseWindowPreferences.getInstance();
        int frameState = framePrefs.getExtendedState();
        
        if (Toolkit.getDefaultToolkit().isFrameStateSupported(frameState) == true) {
            this.setExtendedState(frameState);
            // wait till extendedState really done
            do  {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) { 
                    // do nothing
                }         
            } while (frameState != this.getExtendedState() && this.isValid());
        }

        try {
            DockingManager.loadLayoutModel();
            DockingManager.restoreLayout();
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
        }
        
        if (!(shellTreeView.isDisplayable() && shellListView.isDisplayable() && imagePreviewPane.isDisplayable())) {
            resetPerspective();
            System.err.println("[Ekspos] restoreDocking reset perspective");
            
        }
    }
    
    private void resetPerspective() {
        PerspectiveManager mgr = PerspectiveManager.getInstance();
        mgr.remove(DEFAULT_PERSPECTIVE);
        mgr.setCurrentPerspective(DEFAULT_PERSPECTIVE, false);
        mgr.reset(this);
        
        //DockingManager.restoreLayout();
    }
    
    /** Set value using preferences
     * later don't use this, use listener instead
     */
    public void restoreFromPreferences() {
        GeneralPreferences generalPrefs = GeneralPreferences.getInstance();
        IIOPreferences iioPrefs = IIOPreferences.getInstance();
        
        boolean useCache = generalPrefs.getBoolean(GeneralPreferences.USE_CACHE_KEY);
        ThumbnailView thumbnailView = shellListView.getThumbnailView();
        if (useCache == true) {
            String str = GeneralPreferences.getInstance().getString(GeneralPreferences.CACHE_DIRECTORY_KEY);
            File cacheDirectory = new File(str);
            if (cacheDirectory.exists() == false) {
                cacheDirectory.mkdirs();
            }
            thumbnailView.setCacheDirectory(cacheDirectory);
        }
        thumbnailView.setUseCache(useCache);
        thumbnailView.setImageReaderWriterPreferences(iioPrefs.getImageReaderWriterPreferences());
    }
    
    
    /** Overridden to save the prefs 
     * {@inheritDoc}
     */
    @Override
    protected void processWindowEvent(WindowEvent evt) {
        if (evt.getID() == WindowEvent.WINDOW_CLOSING) {
            exitForm();
        }
        super.processWindowEvent(evt);
    } 
    
    /** Exit */
    private void exitForm() {
        savePrefs();
        setVisible(false);
        dispose();
        //System.exit(0);
    }
    
    private void savePrefs() {
        BrowseWindowPreferences windowPrefs = BrowseWindowPreferences.getInstance();
        windowPrefs.setExtendedState(this.getExtendedState());
        if (this.getExtendedState() != JFrame.NORMAL) {
            windowPrefs.setBounds(-1,-1,760,500);
        } else {
            Rectangle rv = getBounds();
            windowPrefs.setBounds(rv);
        }
        windowPrefs.save();
        
        try {
            DockingManager.storeLayoutModel();
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    /** Set Start Path, by default it is using user.home
     * @param path path
     */
    public void setStartPath(String path) {
        File file = new File(System.getProperty("user.home","."));
        if (path != null) {
            File tmpFile = new File(path);
            if (tmpFile.exists()) {
                file = tmpFile;
            }
        }
        shellTreeView.setSelection(file);
    }
    
    /** Sets the view style.
     * @param viewStyle One of the following constants defined in {@code ShellListView}: VS_DETAIL, VS_LIST, VS_ICON, VS_THUMBNAIL
     * @throws IllegalArgumentException if {@code ViewStyle} is an illegal viewStyle
     */
    public void setViewStyle(int viewStyle) {
        shellListView.setViewStyle(viewStyle);
    }
    
    public void setViewer(Viewer viewer) {
        this.viewer = viewer;
    }
    
    /** Open ViewerWindow */
    protected void openViewerWindow() {
        if (viewer == null) { return; }
        
        List<File> files = shellListView.getSelectedFiles();
        if (files != null) {
            if (files.size() == 1) {
                if (files.get(0).isDirectory()) { return; }
                
                files = shellListView.getAllFiles();
                ListIterator<File> li = files.listIterator();
                while (li.hasNext()) {
                    File file = li.next();
                    if (ImageUtilities.isFileImage(file) == false) {
                        li.remove();
                    }
                }
            }
            
            if (files.size() <= 0) {
                return;
            }
            
            File file = shellListView.getLastSelectedFile();
            int indexOf = files.indexOf(file);
            if (indexOf < 0) {
                indexOf = 0;
            }
            
            final List<File> fileList = files;
            final int index = indexOf;
            
            //WindowManager.getInstance().openViewerWindow(fileList, index);
            viewer.view(fileList, index);
        }
    }
    
    protected synchronized void updateLocationModel(File file) {
        String pathString = file.toString();
        
        // location stuff
        int index = locationList.indexOf(pathString);
        if (index > -1) {
            locationList.remove(index);
            locationList.add(0,pathString);
        } else {
            locationList.add(0,pathString);
            if (locationList.size() > HISTORY_SIZE) {
                locationList.remove(HISTORY_SIZE-1);
            }
        }
        
        locationCombo.removeAllItems();
        //ComboBoxModel model = locationCombo.getModel();
        for (int i=0; i<locationList.size(); i++) {
            locationCombo.addItem(locationList.get(i));
        }
        //locationCombo.setSelectedItem(pathString);
    }
    
    protected synchronized void updateHistory(File file) {
        if (backOrForwardInProgress == true) { return; }
        
        // history stuff
        boolean append = true;
        if (historyList.size() > 0) {
            String str = historyList.get(0);
            if (str.equals(file.toString())) {
                append = false;
            }
        }
        if (append == true) {
            historyList.add(0,file.toString());
        }
        while (historyList.size() > HISTORY_SIZE) {
            historyList.remove(historyList.size()-1);
        }
    }
    
    
    /** Display Error Dialog that the param does not exists
     * @param file File
     */
    protected void displayErrorFileNotExists(File file) {
        Object[] args = {file.toString()};
        JOptionPane.showMessageDialog(this,MessageFormat.format(bundle.getString("message.fileNotExists.text"),args),bundle.getString("message.fileNotExists.title"), JOptionPane.ERROR_MESSAGE);
    }
    
    /** Login to Picasa Web Album */
    private void picasaLogin() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                PycasaManager.getInstance().openLoginDialog(BrowserWindow.this);
            }
        });
    }
    
    /** Upload selected files to Picasa Web Album */
    private void picasaAdd() {
        List<File> files = shellListView.getSelectedFiles();
        if (files != null) {
            if (files.size() == 1) {
                if (files.get(0).isDirectory()) { return; }
            }
            
            ListIterator<File> li = files.listIterator();
            while (li.hasNext()) {
                File file = li.next();
                if (ImageUtilities.isFileImage(file) == false) {
                    li.remove();
                }
            }
        }
        
        final List<File> selectedFiles = files;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                PycasaManager.getInstance().openAddPhotoWindow(BrowserWindow.this, selectedFiles);
            }
        });
    }
    
    /** Manage Picasa Web Album */
    private void picasaManage() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                PycasaManager.getInstance().openManageWindow(BrowserWindow.this);
            }
        });
    }
    
    /////////////////////////////
    // Event Handler
    ////////////////////////////
    private void shellTreeViewValueChanged(TreeSelectionEvent e) {
        if (shellTreeViewInProgress == true) { return; }
        
        shellTreeViewInProgress = true;
        try {
            if (!shellListViewInProgress) {
                File dir = shellTreeView.getLastSelection();
                if (dir != null) {
                    shellListView.setCurrentDirectory(dir);
                    shellListView.refresh();
                }
            }
        } finally {
            shellTreeViewInProgress = false;
        }
    }
    
    private void shellTreeViewPopupTrigger(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            shellTreeViewPopupMenu.show(evt.getComponent(),evt.getX(), evt.getY());
        }
    }
    
    private void shellTreeViewEditingStopped(ChangeEvent evt) {
        TreeCellEditor editor = shellTreeView.getCellEditor();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)shellTreeView.getLastSelectedPathComponent();
        
        File file = (File)node.getUserObject();
        File dest = new File(file.getParent() + File.separator + editor.getCellEditorValue().toString());
        
        boolean success = false;
        try {
            success = file.renameTo(dest);
        } catch (Exception ex) {
            success = false;
        }
        
        if (success == true) {
            node.setUserObject(dest);
            ShellTreeViewModel dataModel = (ShellTreeViewModel)shellTreeView.getModel();
            dataModel.reload(node);
            TreeSelectionModel selectionModel = shellTreeView.getSelectionModel();
            selectionModel.clearSelection();
            selectionModel.setSelectionPath(new TreePath(node.getPath()));
        } else {
            JOptionPane.showMessageDialog(this, bundle.getString("message.renameException.text"), bundle.getString("message.exception.title"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void shellListViewValueChanged(ListSelectionEvent evt) {
        File file = shellListView.getLastSelectedFile();
        imagePreviewPane.setProgressRepaint(GeneralPreferences.getInstance().getDouble(GeneralPreferences.PROGRESS_REPAINT_KEY));
        imagePreviewPane.view(file);
    }
    
    private void shellListViewContentsChanged(ListDataEvent evt) {
        if (shellListViewInProgress == true) { return; }
        
        if (evt.getIndex0() != ShellListViewModel.ALL_INDEX && evt.getIndex1() != ShellListViewModel.ALL_INDEX) {
            return;
        }
        
        shellListViewInProgress = true;
        try {
            // synchronize with folder tree
            File dir = shellListView.getCurrentDirectory();
            if (!shellTreeViewInProgress) {
                shellTreeView.setSelection(dir);
            }
            
            // synchronize with history
            updateLocationModel(dir);
            updateHistory(dir);
            locationCombo.setSelectedItem(dir.toString());
            
        } finally {
            shellListViewInProgress = false;
        }
        
        if (backOrForwardInProgress == false) {
            historyIndex = 0;
        }
    }
    
    private void shellListViewPopupTrigger(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            shellListViewPopupMenu.show(evt.getComponent(),evt.getX(), evt.getY());
        }
    }
    
    private void shellListViewEditingStopped(ChangeEvent evt) {
        CellEditor editor = (CellEditor)evt.getSource();
        int index = shellListView.getLastSelectedIndex();
        if (index >= 0) {
            try {
                shellListView.getModel().renameFile(index, editor.getCellEditorValue().toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, bundle.getString("message.renameException.text"), bundle.getString("message.exception.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void shellListViewMouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            openViewerWindow();
        }
    }
    
    
    public void shellListViewKeyPressed(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            openViewerWindow();
        }
    }
    
    private void historyBack() {
        if (historyList.size() < 1) { return; }
        
        backOrForwardInProgress = true;
        try {
            historyIndex++;
            if (historyIndex < historyList.size()) {
                File file = new File(historyList.get(historyIndex));
                if (file.exists()) {
                    shellListView.setCurrentDirectory(file);
                    shellListView.refresh();
                } else {
                    displayErrorFileNotExists(file);
                }
            } else {
                historyIndex = historyList.size()-1;
            }
        } finally {
            backOrForwardInProgress = false;
        }
        
    }
    
    private void historyForward() {
        if (historyList.size() < 1) { return; }
        backOrForwardInProgress = true;
        try {
            historyIndex--;
            if (historyIndex > -1) {
                File file = new File(historyList.get(historyIndex));
                if (file.exists()) {
                    shellListView.setCurrentDirectory(file);
                    shellListView.refresh();
                } else {
                    displayErrorFileNotExists(file);
                }
            } else {
                historyIndex = 0;
            }
        } finally {
            backOrForwardInProgress = false;
        }
    }
    
    private void upActionPerformed() {
        File dir = shellListView.getCurrentDirectory();
        if (dir != null) {
            dir = dir.getParentFile();
        }
        
        if (dir != null) {
            shellListView.setCurrentDirectory(dir);
            shellListView.refresh();
        } else {
            
        }
    }
    
    private void locationActionPerformed(String str) {
        if (shellListViewInProgress) {
            return;
        }
        File file = new File(str);
        if (file.exists()) {
            shellListView.setCurrentDirectory(file);
            shellListView.refresh();
        } else {
            displayErrorFileNotExists(file);
        }
    }
    
    @SuppressWarnings("static-access")
    private void cutActionPerformed(ActionEvent evt) {
        Container parent = ((Component)evt.getSource()).getParent();
        Action action = null;
        JComponent source = null;
        if (shellTreeViewPopupMenu.equals(parent)) {
            action = shellTreeView.getTransferHandler().getCutAction();
            source = shellTreeView;
        } else {
            action = shellListView.getTransferHandler().getCutAction();
            source = (JComponent)shellListView.getViewComponent();
        }
        ActionEvent newEvent = new ActionEvent(source,ActionEvent.ACTION_PERFORMED,evt.getActionCommand());
        action.actionPerformed(newEvent);
    }
    
    @SuppressWarnings("static-access")
    private void copyActionPerformed(ActionEvent evt) {
        Container parent = ((Component)evt.getSource()).getParent();
        Action action = null;
        JComponent source = null;
        if (shellTreeViewPopupMenu.equals(parent)) {
            action = shellTreeView.getTransferHandler().getCopyAction();
            source = shellTreeView;
        } else {
            action = shellListView.getTransferHandler().getCopyAction();
            source = (JComponent)shellListView.getViewComponent();
        }
        ActionEvent newEvent = new ActionEvent(source,ActionEvent.ACTION_PERFORMED,evt.getActionCommand());
        action.actionPerformed(newEvent);
    }
    
    @SuppressWarnings("static-access")
    private void pasteActionPerformed(ActionEvent evt) {
        Container parent = ((Component)evt.getSource()).getParent();
        Action action = null;
        JComponent source = null;
        if (shellTreeViewPopupMenu.equals(parent)) {
            action = shellTreeView.getTransferHandler().getPasteAction();
            source = shellTreeView;
        } else {
            action = shellListView.getTransferHandler().getPasteAction();
            source = (JComponent)shellListView.getViewComponent();
        }
        ActionEvent newEvent = new ActionEvent(source,ActionEvent.ACTION_PERFORMED,evt.getActionCommand());
        action.actionPerformed(newEvent);
    }
    
    private void renameActionPerformed(ActionEvent evt) {
        //System.err.println(evt.getSource().getClass().toString());
        Container parent = ((Component)evt.getSource()).getParent();
        if (shellTreeViewPopupMenu.equals(parent)) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)shellTreeView.getLastSelectedPathComponent();
            if (node != null) {
                shellTreeView.startEditingAtPath(new TreePath(node.getPath()));
            }
        } else {
            int i = shellListView.getLastSelectedIndex();
            if ( i != -1) {
                shellListView.editCellAt(i);
            }
        }
    }
    
    private void deleteActionPerformed(ActionEvent evt) {
        final List<File> deleteList = new ArrayList<File>();
        Container parent = ((Component)evt.getSource()).getParent();
        if (shellTreeViewPopupMenu.equals(parent)) {
            DefaultMutableTreeNode node = null;
            node = (DefaultMutableTreeNode)shellTreeView.getLastSelectedPathComponent();
            if (node != null) {
                deleteList.add((File)node.getUserObject());
            }
        } else {
            List<File> selectedList = shellListView.getSelectedFiles();
            if (selectedList != null) {
                deleteList.addAll(selectedList);
            }
        }
        
        if (deleteList.size() == 0) {
            return;
        }
        
        int n = JOptionPane.showConfirmDialog(this,bundle.getString("message.confirmDelete.text"),bundle.getString("message.confirmDelete.title"),JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.OK_OPTION) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        shellProgressMonitor.start(ShellProgressMonitor.DELETE,deleteList,null);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(BrowserWindow.this, bundle.getString("message.deleteException.text"), bundle.getString("message.exception.title"), JOptionPane.ERROR_MESSAGE);
                        shellProgressMonitor.cancel();
                    }
                }
            });
        }
    }
    
    private void shellProgressMonitorShellDeleted(ShellEvent evt) {
        // handle shellTreeView
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)shellTreeView.getLastSelectedPathComponent();
        if (node != null) {
            if (node.getUserObject().equals(evt.getSourceFile())) {
                ShellTreeViewModel dataModel = (ShellTreeViewModel)shellTreeView.getModel();
                dataModel.removeNodeFromParent(node);
            }
        }
        
        // handle shellListView
        if (evt.getSourceFile().equals(shellListView.getCurrentDirectory())) {
            //shellListView.refresh();
        } else {
            ShellListViewModel dataModel = shellListView.getModel();
            int i = dataModel.indexOf(evt.getSourceFile());
            if (i != -1) {
                dataModel.remove(i);
            }
        }
    }
    
    private void shellProgressMonitorShellMoved(ShellEvent evt) {
        // handle shellTreeView
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)shellTreeView.getLastSelectedPathComponent();
        if (node != null) {
            if (node.getUserObject().equals(evt.getSourceFile())) {
                ShellTreeViewModel dataModel = (ShellTreeViewModel)shellTreeView.getModel();
                dataModel.removeNodeFromParent(node);
            }
        }
        
        // handle shellListView
        /*if (evt.getSourceFile().equals(shellListView.getCurrentDirectory())) {
            //shellListView.refresh();
        } else {
            ShellListViewModel dataModel = shellListView.getModel();
            int i = dataModel.indexOf(evt.getSourceFile());
            if (i != -1) {
                dataModel.remove(i);
            }
        }*/
    }
    
    
    
    private class BrowserAction extends AbstractAction {
        
        public BrowserAction(String actionCommand) {
            putValue(Action.ACTION_COMMAND_KEY,actionCommand);
        }
        
        /** {@inheritDoc} */
        public void actionPerformed(ActionEvent evt) {
            String command = evt.getActionCommand();
            if (command.equals(EXIT_ACTION_COMMAND)) {
                exitForm();
                
            } else if (command.equals(CUT_ACTION_COMMAND)) {
                cutActionPerformed(evt);
            } else if (command.equals(COPY_ACTION_COMMAND)) {
                copyActionPerformed(evt);
            } else if (command.equals(PASTE_ACTION_COMMAND)) {
                pasteActionPerformed(evt);
            } else if (command.equals(RENAME_ACTION_COMMAND)) {
                renameActionPerformed(evt);
            } else if (command.equals(DELETE_ACTION_COMMAND)) {
                deleteActionPerformed(evt);
            } else if (command.equals(PREFERENCES_ACTION_COMMAND)) {
                WindowManager windowManager = WindowManager.getInstance();
                windowManager.openPreferencesWindow();
                
            } else if (command.equals(BACK_ACTION_COMMAND)) {
                historyBack();
            } else if (command.equals(FORWARD_ACTION_COMMAND)) {
                historyForward();
            } else if (command.equals(UP_ACTION_COMMAND)) {
                upActionPerformed();
            } else if (command.equals(HOME_ACTION_COMMAND)) {
                File home = new File(System.getProperty("user.home","."));
                shellTreeView.setSelection(home);
            } else if (command.equals(RELOAD_ACTION_COMMAND)) {
                shellListView.refresh();
            } else if (command.equals(LOCATION_ACTION_COMMAND)) {
                JComboBox combo = (JComboBox)evt.getSource();
                String str = (String)combo.getSelectedItem();
                if (str != null) {
                    locationActionPerformed(str);
                }
            } else if (command.equals(RESET_PERSPECTIVE_ACTION_COMMAND)) {
                resetPerspective();
                
            } else if (command.equals(DETAIL_VIEW_ACTION_COMMAND)) {
                shellListView.setViewStyle(ShellListView.VS_DETAIL);
            } else if (command.equals(LIST_VIEW_ACTION_COMMAND)) {
                shellListView.setViewStyle(ShellListView.VS_LIST);
            } else if (command.equals(ICON_VIEW_ACTION_COMMAND)) {
                shellListView.setViewStyle(ShellListView.VS_ICON);
            } else if (command.equals(THUMBNAIL_VIEW_ACTION_COMMAND)) {
                shellListView.setViewStyle(ShellListView.VS_THUMBNAIL);
                
            } else if (command.equals(PICASA_LOGIN_ACTION_COMMAND)) {
                picasaLogin();
            } else if (command.equals(PICASA_ADD_ACTION_COMMAND)) {
                picasaAdd();
            } else if (command.equals(PICASA_MANAGE_ACTION_COMMAND)) {
                picasaManage();
                
            } else if (command.equals(HELP_CONTENT_ACTION_COMMAND)) {
                JOptionPane.showMessageDialog(BrowserWindow.this,"Not Implemented yet.");
                System.err.println("...Help Contents: not implemented yet");
            } else if (command.equals(TOTD_ACTION_COMMAND)) {
                WindowManager windowManager = WindowManager.getInstance();
                windowManager.openTOTDWindow();
            } else if (command.equals(ABOUT_ACTION_COMMAND)) {
                WindowManager windowManager = WindowManager.getInstance();
                windowManager.openAboutWindow();
            }
        }
    }

    private class BrowserPerspectiveFactory implements PerspectiveFactory {
        
        public Perspective getPerspective(String persistentId) {
            //System.err.println("getPerspective:" + persistentId);
            if(DEFAULT_PERSPECTIVE.equals(persistentId)) {
                return createDefaultPerspective();
            }
            return null;
	}

        private Perspective createDefaultPerspective() {
            //System.err.println("createDefaultPers");
            Perspective perspective = new Perspective(DEFAULT_PERSPECTIVE, "Default");
            LayoutSequence sequence = perspective.getInitialSequence(true);
			
            sequence.add(DOCKING_LIST_VIEW);
            sequence.add(DOCKING_TREE_VIEW, DOCKING_LIST_VIEW, DockingConstants.WEST_REGION, .3f);
            sequence.add(DOCKING_PREVIEW_VIEW, DOCKING_TREE_VIEW, DockingConstants.SOUTH_REGION, .6f);
            
            
            /*sequence.add(DOCKING_TREE_VIEW);
            sequence.add(DOCKING_LIST_VIEW, DOCKING_TREE_VIEW, DockingConstants.EAST_REGION, .3f);
            sequence.add(DOCKING_PREVIEW_VIEW, DOCKING_TREE_VIEW, DockingConstants.SOUTH_REGION, .6f);
             */

            return perspective;
        }

    }

    private class ViewFactory extends DockableFactory.Stub {
        @Override
        public Component getDockableComponent(String dockableId) {
            //System.err.println("getDockableComponent:" + dockableId);
            if (DOCKING_LIST_VIEW.equals(dockableId)) {
                return createViewComponent(DOCKING_LIST_VIEW,"Folders and Files",shellListView);
            } else if (DOCKING_TREE_VIEW.equals(dockableId)) {
                View view = createViewComponent(DOCKING_TREE_VIEW,"Folders",new JScrollPane(shellTreeView));
                view.addAction(DockingConstants.PIN_ACTION);
                return view;
            } else if (DOCKING_PREVIEW_VIEW.equals(dockableId)) {
                View view = createViewComponent(DOCKING_PREVIEW_VIEW,"Preview",imagePreviewPane);
                view.addAction(DockingConstants.PIN_ACTION);
                return view;
            }
            return null;
        }

        private View createViewComponent(String id, String text, JComponent comp) {
            View view = new View(id, text);
        
            JPanel p = new JPanel();
            p.setBorder(new LineBorder(Color.GRAY, 1));
            p.setLayout(new BorderLayout());
            p.add(comp, BorderLayout.CENTER);
        
            view.setContentPane(p);
        
            return view;
        }

    }
}
