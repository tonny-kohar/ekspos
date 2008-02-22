package kiyut.ekspos;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import kiyut.ekspos.prefs.GeneralPreferences;
import kiyut.ekspos.prefs.IIOPreferences;
import org.flexdock.docking.state.PersistenceException;
import org.flexdock.perspective.PerspectiveManager;
import org.flexdock.perspective.persist.PersistenceHandler;
import org.flexdock.perspective.persist.Persister;
import org.flexdock.perspective.persist.PerspectiveModel;
import org.flexdock.perspective.persist.xml.XMLPersister;

/** This is the Application Launcher 
 */
public class Ekspos {
    
    /**Main method*/
    public static void main(String[] args) {
        String path = null;
        
        if (args.length > 0 ) {
            path = args[0];
        }
        
        Ekspos ekspos = new Ekspos();
        ekspos.run(path);
    }
    
    /**Construct the application*/
    private Ekspos() {
    }
    
    private void run(String path) {
        initialize();
        
        try {
            path = resolvePath(path);
        } catch (Exception ex) {
            System.err.println("Unable to resolve startup path.\n" + ex.getMessage());
            path = null;
        }
        final String absPath = path;
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                showMainFrame(absPath);
            }
        });
    }
    
    /** Resolve the startup path with the following rule <br>
     *  - if empty set to null <br>
     *  - if absolute leave at it is <br>
     *  - if relative resolve against current dir System.getProperty("user.dir")
     * @return String
     */
    private String resolvePath(String path) throws IOException {
        String newPath = null;
        
        if (path == null) {
            return newPath;
        }
        
        File file = new File(path);
        file = file.getCanonicalFile();
        
        newPath = file.getCanonicalPath();
        
        return newPath;
    }
    
    private void initialize() {
        String osName = System.getProperty("os.name").toUpperCase();
        
        // set default swing bold to false, only for JVM 1.5 or above
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        // set LaF
        LookAndFeel lnf = UIManager.getLookAndFeel();
        if (lnf != null && lnf.getID().equalsIgnoreCase("Metal")) {
            String lnfClassName = null;
            if (osName.startsWith("MAC")) {
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", Application.getName());
                System.setProperty("apple.laf.useScreenMenuBar","true");
                lnfClassName = UIManager.getSystemLookAndFeelClassName();
            } else if (osName.startsWith("WINDOWS")) {
                UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
                lnfClassName = Options.getSystemLookAndFeelClassName();
                Options.setUseNarrowButtons(false);
            } else {
                UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
                lnfClassName = Options.getCrossPlatformLookAndFeelClassName();
                PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
                PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                Options.setUseNarrowButtons(false);
                
                //PlasticLookAndFeel.setMyCurrentTheme(new ExperienceBlueDefaultFont());  // for CJK Font
            }
            
            if (lnfClassName != null) {
                try {
                    UIManager.setLookAndFeel(lnfClassName);
                } catch (Exception ex) {
                    System.err.println("Unable to set LookAndFeel, use default LookAndFeel.\n" + ex.getMessage());
                }
            }
        }
        
        // initialize preferences.
        GeneralPreferences.getInstance();
        IIOPreferences.getInstance();
    }
    
    private void showMainFrame(String path) {
        BrowserWindow frame = new BrowserWindow();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setStartPath(path);
        frame.setVisible(true);

        WindowManager.createInstance(frame);

        // setup the PerspectiveManager
        PerspectiveManager.setPersistenceHandler(new DockingPersistence());
        frame.restoreDocking();
        
        // start tips of the day
        GeneralPreferences prefs = GeneralPreferences.getInstance();
        if (prefs.getBoolean(GeneralPreferences.TOTD_KEY) == true) {
            TOTDWindow totd = new TOTDWindow();
            totd.pack();
            totd.setLocationRelativeTo(frame);
            totd.setVisible(true);
        }
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evt) {
                System.exit(0);
            }
        });

        frame.setViewer(new Viewer() {
            public void view(List<File> files, int index) {
                WindowManager.getInstance().openViewerWindow(files, index);
            }
        });
        
    }
    
    private class DockingPersistence implements PersistenceHandler {
        public boolean store(String persistenceKey, PerspectiveModel perspectiveModel) throws IOException, PersistenceException {
            boolean saved = false;
            
            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream(1024);
            try {
                boolean stored = createDefaultPersister().store(os, perspectiveModel);
                if (stored == true) {
                    byte[] bytes = os.toByteArray();
                    GeneralPreferences prefs = GeneralPreferences.getInstance();
                    prefs.putByteArray(GeneralPreferences.DOCKING_LAYOUT_KEY,bytes);
                    prefs.save();
                    saved = true;
                }
            } finally {
                os.close();
            }
            return saved;
        }
        
        public PerspectiveModel load(String persistenceKey) throws IOException, PersistenceException {
            PerspectiveModel perspectiveModel = null;
            
            GeneralPreferences prefs = GeneralPreferences.getInstance();
            byte[] bytes = prefs.getByteArray(GeneralPreferences.DOCKING_LAYOUT_KEY);
            if (bytes != null) {
                java.io.ByteArrayInputStream is = new java.io.ByteArrayInputStream(bytes);
                try {
                    perspectiveModel = createDefaultPersister().load(is);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
            
            return perspectiveModel;
        }
        
        public Persister createDefaultPersister() {
            return XMLPersister.newDefaultInstance();
        }
    }
}