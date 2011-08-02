/*
 * WindowManager.java
 *
 * Created on September 11, 2003, 1:49 AM
 */

package kiyut.ekspos;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author  tonny
 */
public class WindowManager {

    private static WindowManager instance; // The single instance
    private JFrame applicationFrame;
    private ViewerWindow viewer;

    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return WindowManager The single instance.
     */
    public static synchronized WindowManager getInstance() {
        return instance;
    }

    public static synchronized void createInstance(JFrame applicationFrame) {
        if (instance == null) {
            instance = new WindowManager(applicationFrame);
        }
    }

    /** Creates a new instance of WindowManager */
    private WindowManager(JFrame applicationFrame) {
        this.applicationFrame = applicationFrame;
    }

    public JFrame getBrowserWindow() {
        return this.applicationFrame;
    }

    /**
     * Open Preferences Window
     */
    public void openPreferencesWindow() {
        PreferencesWindow preferencesWindow = new PreferencesWindow(applicationFrame, true);
        preferencesWindow.pack();
        preferencesWindow.setLocationRelativeTo(applicationFrame);
        preferencesWindow.setVisible(true);
    }

    /**
     * Open TOTD Window
     */
    public void openTOTDWindow() {
        TOTDWindow totd = new TOTDWindow();
        totd.pack();
        // center frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = totd.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        totd.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        totd.setVisible(true);
    }

    /** Open About Window
     */
    public void openAboutWindow() {
        AboutWindow window = new AboutWindow(applicationFrame, true);
        window.pack();
        // center frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = window.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        window.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        window.setVisible(true);
    }

    /** Open ViewerWindow
     * @param files List of files
     * @param index The index of first image to display
     */
    public void openViewerWindow(List<File> files, int index) {
        final List<File> finalFiles = files;
        final int finalIndex = index;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (viewer == null) {
                    viewer = new ViewerWindow();
                }
                viewer.setVisible(true);
                viewer.setFiles(finalFiles);
                viewer.setIndex(finalIndex);
                viewer.refresh();
            }
        });
    }
}