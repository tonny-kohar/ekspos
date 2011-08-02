package kiyut.ekspos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import kiyut.swing.util.MenuFactory;
import kiyut.ekspos.prefs.*;
import kiyut.swing.statusbar.StatusBar;
import kiyut.swing.statusbar.StatusBarItem;


/** ViewerWindow
 */
public class ViewerWindow extends JFrame {
    private static final String CLOSE_ACTION_COMMAND = "Close";
    private static final String NEXT_ACTION_COMMAND = "Next";
    private static final String PREVIOUS_ACTION_COMMAND = "Previous";
    private static final String RELOAD_ACTION_COMMAND = "Reload";
    private static final String ZOOM_OUT_ACTION_COMMAND = "ZoomOut";
    private static final String ZOOM_IN_ACTION_COMMAND = "ZoomIn";
    private static final String FIT_TO_SCREEN_ACTION_COMMAND = "FitToScreen";
    
    private static ResourceBundle bundle = ResourceBundle.getBundle("kiyut.ekspos.ViewerWindow");
    
    private Map<String,Action> actionMap;
    
    private JScrollBar vBar, hBar;
    private int curX;
    private int curY;
    private List<File> files;
    private int index;
    private double curScale;
    
    private JToolBar toolBar;
    private StatusBar statusBar;
    
    private JScrollPane scrollPane = new JScrollPane();
    private ImagePane imagePane = new ImagePane();
    
    private boolean fitToScreen;
    
    public ViewerWindow() {
        setIconImage(Application.getIconImage());
        imagePane.setBackground(Color.WHITE);
        imagePane.setPreferredSize(new Dimension(500, 500));
        //imagePane.setFocusable(true);
        //imagePane.requestFocusInWindow();
        
        imagePane.addIIOReadProgressListener(new IIOReadProgressListener() {
            public void imageComplete(ImageReader source) { onImageCompleted(source); }
            public void imageProgress(ImageReader source, float percentageDone) { onImageProgress(source,percentageDone); }
            public void imageStarted(ImageReader source, int imageIndex) { onImageStarted(source,imageIndex); }
            public void readAborted(ImageReader source) { }
            public void sequenceComplete(ImageReader source) { }
            public void sequenceStarted(ImageReader source, int minIndex) { }
            public void thumbnailComplete(ImageReader source) { }
            public void thumbnailProgress(ImageReader source, float percentageDone) { }
            public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) { }
        });
        
        scrollPane.setFocusable(true);
        scrollPane.requestFocusInWindow();
        scrollPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) { onScrollPaneMouseDragged(e); }
        });
        
        scrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { onScrollPaneMousePressed(e); }
            @Override
            public void mouseClicked(MouseEvent e) { onScrollPaneMouseClicked(e); }
        });
        
        scrollPane.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { onScrollPaneKeyPressed(e); }
        });
        
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.black);
        scrollPane.setViewportView(imagePane);
        
        vBar = scrollPane.getVerticalScrollBar();
        hBar = scrollPane.getHorizontalScrollBar();
        vBar.setUnitIncrement(20);
        hBar.setUnitIncrement(20);
        
        statusBar = new StatusBar();
        StatusBarItem statusBarItem = new StatusBarItem(" ",75,SwingConstants.CENTER);
        statusBar.addItem(statusBarItem);
        statusBarItem = new StatusBarItem(" ",100,SwingConstants.CENTER);
        statusBar.addItem(statusBarItem);
        statusBarItem = new StatusBarItem(" ",60,SwingConstants.CENTER);
        statusBar.addItem(statusBarItem);
        statusBarItem = new StatusBarItem(" ",100,SwingConstants.LEFT);
        statusBar.addItem(statusBarItem);
        
        //setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initActions();
        
        this.getContentPane().setBackground(Color.black);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(toolBar, BorderLayout.NORTH);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(statusBar,BorderLayout.SOUTH);
        
        files = new ArrayList<File>();
        index = 0;
        
        pack();
    }
    
    /**Overridden so we can exit when window is closed*/
    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            GeneralPreferences.getInstance().putBoolean(GeneralPreferences.FIT_TO_WINDOW_KEY, fitToScreen);
            GeneralPreferences.getInstance().save();
        }
    }
    
    /** Initialize Actions, MenuBar and ToolBar
     */
    private void initActions() {
        actionMap = new HashMap<String,Action>();
        
        // create action
        actionMap.put(CLOSE_ACTION_COMMAND,new ViewerAction(CLOSE_ACTION_COMMAND));
        actionMap.put(PREVIOUS_ACTION_COMMAND,new ViewerAction(PREVIOUS_ACTION_COMMAND));
        actionMap.put(NEXT_ACTION_COMMAND,new ViewerAction(NEXT_ACTION_COMMAND));
        actionMap.put(RELOAD_ACTION_COMMAND,new ViewerAction(RELOAD_ACTION_COMMAND));
        actionMap.put(ZOOM_IN_ACTION_COMMAND,new ViewerAction(ZOOM_IN_ACTION_COMMAND));
        actionMap.put(ZOOM_OUT_ACTION_COMMAND,new ViewerAction(ZOOM_OUT_ACTION_COMMAND));
        actionMap.put(FIT_TO_SCREEN_ACTION_COMMAND,new ViewerAction(FIT_TO_SCREEN_ACTION_COMMAND));
        
        // inititalize action from ResourceBundle
        MenuFactory.configureActionMap(bundle,actionMap);
        
        // initialize menuBar
        JMenuBar menuBar = MenuFactory.createMenuBar(bundle,actionMap);
        setJMenuBar(menuBar);
        
        boolean b = GeneralPreferences.getInstance().getBoolean(GeneralPreferences.FIT_TO_WINDOW_KEY);
        JMenuItem menuItem = MenuFactory.getMenuItem(getJMenuBar().getSubElements(),FIT_TO_SCREEN_ACTION_COMMAND);
        menuItem.setIcon(null);
        ((JCheckBoxMenuItem)menuItem).setSelected(b);
        fitToScreen = b;
        
        // initialize ToolBar
        toolBar = new JToolBar();
        toolBar.setFloatable(true);
        toolBar.setRollover(true);
        
        toolBar.add(createToolBarButton(actionMap.get(PREVIOUS_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(NEXT_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(RELOAD_ACTION_COMMAND)));
        toolBar.addSeparator();
        toolBar.add(createToolBarButton(actionMap.get(ZOOM_IN_ACTION_COMMAND)));
        toolBar.add(createToolBarButton(actionMap.get(ZOOM_OUT_ACTION_COMMAND)));
        toolBar.addSeparator();
        toolBar.add(createToolBarButton(actionMap.get(FIT_TO_SCREEN_ACTION_COMMAND)));
    }
    
    /**
     * Create ToolBar Button
     * @param action <code>Action</code> for the button
     * @return <code>JButton </code>
     */
    private JButton createToolBarButton(Action action) {
        JButton button = new JButton(action);
        button.setText("");
        button.setFocusable(false);
        return button;
    }
    
    /** set Files, it also reset the index to 0
     * @param list of files
     */
    public void setFiles(List<File> files) {
        this.files = files;
        index = 0;
    }
    
    /** add files to the list
     * @param list of files
     */
    public void addFiles(List<File> files) {
        this.files.addAll(files);
    }
    
    /** set the index for list of files to be shown first
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }
    
    /** set FitToScreen
     *@param b true / false
     */
    protected void setFitToScreen(boolean b) {
        fitToScreen = b;
        
        // update MenuItem
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)MenuFactory.getMenuItem(getJMenuBar().getSubElements(),FIT_TO_SCREEN_ACTION_COMMAND);
        if (menuItem.isSelected() != fitToScreen) {
            menuItem.setSelected(fitToScreen);
        }
        
        if (fitToScreen == true) {
            BufferedImage img = imagePane.getImage();
            if (img == null) {
                return;
            }
            int width = img.getWidth();
            int height = img.getHeight();
            imageResized(width,height);
        }
        
        refresh();
    }
    
    /** Image has been resized, adjust the frame size
     *@param width Image width
     *@param height Image height
     */
    protected void imageResized(int width, int height) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
        
        //Point p = scrollPane.getLocation();
        Point p1 = this.getLocationOnScreen();
        Point p2 = scrollPane.getLocationOnScreen();
        int x = (p2.x - p1.x) * 2; // currently it is only frame border
        int y = (p2.y - p1.y) + statusBar.getBounds().height + (x*2); // x*2 = frameBorder
        
        int maxWidth = screenSize.width - (screenInsets.left + screenInsets.right) - x;
        int maxHeight = screenSize.height - (screenInsets.top + screenInsets.bottom) - y;
        
        int state = JFrame.NORMAL;
        if (maxWidth <= width && maxHeight <= height) {
            state = JFrame.MAXIMIZED_BOTH;
        } else if (maxWidth <= width) {
            state = JFrame.MAXIMIZED_HORIZ;
        } else if (maxHeight <= height) {
            state = JFrame.MAXIMIZED_VERT;
        }
        
        if (Toolkit.getDefaultToolkit().isFrameStateSupported(state)) {
            setExtendedState(state);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        
        Dimension imgSize =  new Dimension(width,height);
        Dimension areaSize =  new Dimension(maxWidth,maxHeight);
        Dimension newSize = null;
        
        if (state == JFrame.NORMAL) {
            curScale = 1;
            newSize = imgSize;
        } else {
            if (fitToScreen == true ) {
                curScale = kiyut.swing.shell.image.ImageUtilities.scaleToFit(imgSize,areaSize);
                newSize = areaSize;
                if (state == JFrame.MAXIMIZED_HORIZ) {
                    newSize.height = imgSize.height;
                } else if (state == JFrame.MAXIMIZED_VERT) {
                    newSize.width = imgSize.width;
                }
            } else {
                curScale = 1;
                newSize = imgSize;
            }
        }
        
        final Dimension fNewSize = newSize;
        final int fState = state;
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                imagePane.setImageRescale(curScale,curScale);
        
                if ( fNewSize != null && !imagePane.getSize().equals(fNewSize)) {
                    imagePane.setSize(fNewSize);
                    imagePane.setPreferredSize(fNewSize);
                    imagePane.revalidate();
                }
        
                if (fState == JFrame.NORMAL) {
                    pack();
                }
        
                repaint();
           } 
        });
        
    }
    
    /** Refresh this window/frame
     */
    public void refresh() {
        File file = files.get(index);
        
        setTitle(file.toString() + " - " + Application.getName());
        
        statusBar.getItem(0).setText((index + 1) + "/" + files.size());
        
        curScale = 1;
        imagePane.setProgressRepaint(GeneralPreferences.getInstance().getDouble(GeneralPreferences.PROGRESS_REPAINT_KEY));
        imagePane.view(file);
    }
    
    /** show next image on the list
     */
    public void showNext() {
        if (index < files.size()-1) {
            index++;
        } else {
            index = 0;
        }
        refresh();
        
    }
    
    /** show previous image on the list 
     */
    public void showPrevious() {
        if (index > 0) {
            index--;
        } else {
            index = files.size() - 1;
        }
        refresh();
    }
    
    /** Zoom In / Zoom Out
     * @param bigger true/false
     */
    protected void zoom(boolean bigger) {
        double increment = 0.1;
        double scale = curScale;
        
        if (bigger == true) {
            scale = scale + increment;
        } else {
            scale = scale + (-increment);
        }
        
        if (scale < increment) {
            scale = increment;
            return;
        } else if (scale > 16) {
            scale = 16;
            return;
        }
        
        zoom(scale);
    }
    
    /** Zoom by Scale Factor
     * @param scale the Scale Factor
     */
    protected void zoom(double scale) {
        curScale = scale;
        imagePane.setImageRescale(curScale,curScale);
        //imagePane.repaint();
        
        BufferedImage img = imagePane.getImage();
        if (img == null) {
            return;
        }
        int width = (int)Math.ceil(img.getWidth() * scale);
        int height = (int)Math.ceil(img.getHeight() * scale);
        Dimension imgSize =  new Dimension(width,height);
        imagePane.setSize(imgSize);
        imagePane.setPreferredSize(imgSize);
        
        imagePane.revalidate();
    }
    
    ///////////////////////////////
    // Event Handler
    ///////////////////////////////
    
    protected void onImageStarted(ImageReader source, int imageIndex) {
        int width = 0;
        int height = 0;
        
        try {
            width = source.getWidth(0);
            height = source.getHeight(0);
        } catch (IOException ex) { }
        
        statusBar.getItem(1).setText(width + "x" + height);
        statusBar.getItem(2).setText("0%");
        
        imageResized(width,height);
    }
    
    protected void onImageProgress(ImageReader source, final float percentageDone) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                statusBar.getItem(2).setText(Math.round(percentageDone) + "%");
            }
        });
        
    }
    
    protected void onImageCompleted(ImageReader source) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                statusBar.getItem(2).setText("100%");
            }
        });
    }
    
    protected void onScrollPaneKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER
                || e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            Action action = actionMap.get(CLOSE_ACTION_COMMAND);
            action.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,CLOSE_ACTION_COMMAND));
        } else if (e.getKeyCode()==KeyEvent.VK_SPACE
                || e.getKeyCode()==KeyEvent.VK_PAGE_DOWN) {
            Action action = actionMap.get(NEXT_ACTION_COMMAND);
            action.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,NEXT_ACTION_COMMAND));
        } else if (e.getKeyCode()==KeyEvent.VK_BACK_SPACE
                || e.getKeyCode()==KeyEvent.VK_PAGE_UP) {
            Action action = actionMap.get(PREVIOUS_ACTION_COMMAND);
            action.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,PREVIOUS_ACTION_COMMAND));
        } else if (e.getKeyCode()==KeyEvent.VK_HOME) {
            index = 0;
            refresh();
        } else if (e.getKeyCode()==KeyEvent.VK_END) {
            index = files.size()-1;
            refresh();
        } else if (e.getKeyCode()==KeyEvent.VK_PLUS
                || e.getKeyCode()==KeyEvent.VK_ADD) {
            Action action = actionMap.get(ZOOM_IN_ACTION_COMMAND);
            action.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,ZOOM_IN_ACTION_COMMAND));
        } else if (e.getKeyCode()==KeyEvent.VK_MINUS
                || e.getKeyCode()==KeyEvent.VK_SUBTRACT) {
            Action action = actionMap.get(ZOOM_OUT_ACTION_COMMAND);
            action.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,ZOOM_OUT_ACTION_COMMAND));
        } else if (e.getKeyCode()==KeyEvent.VK_UP){
            vBar.setValue(vBar.getValue() - vBar.getBlockIncrement());
        } else if (e.getKeyCode()==KeyEvent.VK_DOWN){
            vBar.setValue(vBar.getValue() + vBar.getBlockIncrement());
        } else if (e.getKeyCode()==KeyEvent.VK_LEFT){
            hBar.setValue(hBar.getValue() - hBar.getBlockIncrement());
        } else if (e.getKeyCode()==KeyEvent.VK_RIGHT ){
            hBar.setValue(hBar.getValue() + hBar.getBlockIncrement());
        }
        e.consume();
    }
    
    protected void onScrollPaneMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Action action = actionMap.get(CLOSE_ACTION_COMMAND);
            action.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,CLOSE_ACTION_COMMAND));
        }
    }
    
    protected void onScrollPaneMouseDragged(MouseEvent e) {
        final int newX = e.getX();
        final int newY = e.getY();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (curX < newX) {
            hBar.setValue(hBar.getValue() - (newX-curX));
        } else {
            hBar.setValue(hBar.getValue() + (curX-newX));
        }
        
        if (curY < newY) {
            vBar.setValue(vBar.getValue() - (newY-curY));
        } else {
            vBar.setValue(vBar.getValue() + (curY-newY));
        }
        
        curX = newX;
        curY = newY;
            }
        });
        
    }
    
    protected void onScrollPaneMousePressed(MouseEvent e) {
        curX = e.getX();
        curY = e.getY();
    }
    
    private class ViewerAction extends AbstractAction {
        public ViewerAction(String actionCommand) {
            putValue(Action.ACTION_COMMAND_KEY,actionCommand);
        }
        
        /** {@inheritDoc} */
        public void actionPerformed(ActionEvent evt) {
            final ActionEvent fEvt = evt;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    actionPerformedImpl(fEvt);
                }
            });
        }
        
        /** Make it easy to run under EDT */
        private void actionPerformedImpl(ActionEvent evt) {
            String actionCommand = evt.getActionCommand();
            
            if (actionCommand.equals(CLOSE_ACTION_COMMAND)) {
                ViewerWindow.this.dispatchEvent(new WindowEvent(ViewerWindow.this,WindowEvent.WINDOW_CLOSING));
            } else if(actionCommand.equals(PREVIOUS_ACTION_COMMAND)) {
                showPrevious();
            } else if(actionCommand.equals(NEXT_ACTION_COMMAND)) {
                showNext();
            } else if(actionCommand.equals(RELOAD_ACTION_COMMAND)) {
                refresh();
            } else if(actionCommand.equals(ZOOM_OUT_ACTION_COMMAND)) {
                zoom(false);
            } else if(actionCommand.equals(ZOOM_IN_ACTION_COMMAND)) {
                zoom(true);
            } else if(actionCommand.equals(FIT_TO_SCREEN_ACTION_COMMAND)) {
                if (evt.getSource() instanceof JCheckBoxMenuItem) {
                    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)evt.getSource();
                    setFitToScreen(menuItem.isSelected());
                } else {
                    //setFitToScreen(true);
                    setFitToScreen(!fitToScreen);
                }
                
            }
        }
    }
}

