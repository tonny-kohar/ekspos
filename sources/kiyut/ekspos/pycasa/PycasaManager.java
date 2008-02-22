package kiyut.ekspos.pycasa;

import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.PhotoEntry;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import kiyut.ekspos.Application;
import kiyut.swing.shell.util.ShellUtilities;
import pycasa.view.LoginDialog;

/**
 * Pycasa manager
 * 
 */
public class PycasaManager {
    private static PycasaManager instance;       // The single instance
    
    private PycasaController pycasaController;
    private JFrame manageWindow;
    private boolean connected = false;
    
    
    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return Preferences The single instance.
     */
    static synchronized public PycasaManager getInstance() {
        if (instance == null) {
            instance = new PycasaManager();
        }
        return instance;
    }
    
    private PycasaManager() {
        pycasaController = new PycasaController();
    }
    
    public PycasaController getController() {
        return pycasaController;
    }
    
    void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public void openLoginDialog(JFrame owner) {
        // login
        LoginDialog login = new LoginDialog(pycasaController);
        login.setAlwaysOnTop(true);
        login.setLocationRelativeTo(owner);
        login.setVisible(true);
        
        /*while(!login.isSuccess() && login.isVisible()) {
            login.setVisible(true);
            // do not need to display error dialog, it has been handled 
            // by the LoginDialog itself, so just return
        }*/
        
        //connected = login.isSuccess();
    }
    
    public void closeManageWindow() {
        if (manageWindow != null) {
            manageWindow.setVisible(false);
            manageWindow = null;
        }
    }
    
    /** Manage Google Picasa Web Album */
    public void openManageWindow(JFrame owner) {
        if (manageWindow == null) {
            pycasa.view.MainWindow pycasaWindow = new pycasa.view.MainWindow(pycasaController);
            this.manageWindow = pycasaWindow;
            pycasaWindow.setIconImage(Application.getIconImage());
            pycasaWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pycasaWindow.setAlwaysOnTop(true);
            pycasaWindow.setLocationRelativeTo(owner);
        } 
        
        this.manageWindow.setVisible(true);
        
        
    }
    
    /** ** Upload selected to Picasa Web Album */
    public void openAddPhotoWindow(final JFrame owner, List<File> files) {
        // check for empty list
        boolean emptyList = true;
        if (files != null) {
            if (files.size() > 0) {
                emptyList = false;
            }
        }
        
        final List <PhotoEntry> photoEntries = new ArrayList<PhotoEntry>();
        if (!emptyList) {
            ListIterator<File> li = files.listIterator();
            while (li.hasNext()) {
                File file = li.next();
                String suffix = ShellUtilities.getFileSuffix(file);
                if (suffix != null) {
                    if (suffix.equalsIgnoreCase("gif") || suffix.equalsIgnoreCase("jpg") 
                            || suffix.equalsIgnoreCase("png") || suffix.equalsIgnoreCase("bmp")) {
                        PhotoEntry photo = pycasaController.getPhotoEntryFromFile(file);
                        if (photo != null) {
                            photoEntries.add(photo);
                        }
                    }
                }
                
            }
        }
        
        if (photoEntries.size() <= 0 ) {
            JOptionPane.showMessageDialog(owner,"Please select image or images to be uploaded.\nCurrently only support jpg, png, gif, or bmp only.","No Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!connected) {
            openLoginDialog(owner);
        }
        
        if (!connected) {
            return;
        }
        
        // load available albums
        List<AlbumEntry> albums = null;
        try {
            albums = pycasaController.getAlbums();
        } catch (Exception ex) {
            albums = null;
        }
        
        if (albums == null) {
            JOptionPane.showMessageDialog(owner,"Error fetching albums.","Error fetching albums", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // open AddPhoto Dialog
        final PycasaAddPhotoPane addPhotoPane = new PycasaAddPhotoPane();
        addPhotoPane.setAlbums(albums);
        int choice = JOptionPane.showOptionDialog(owner,addPhotoPane,addPhotoPane.getTitle(),JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,null,null);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        } 
        
        // upload selected images
        Runnable run = new Runnable() {
            public void run() {
                int progress = 0;
                int size = photoEntries.size();
                
                ProgressMonitor progressMonitor = new ProgressMonitor(owner, "Uploading to Picasa Web Album", "", 0, size);
                progressMonitor.setMillisToPopup(0);
                progressMonitor.setMillisToDecideToPopup(0);
                
                AlbumEntry album = addPhotoPane.getSelectedAlbum();
                if (addPhotoPane.isNewAlbum()) {
                    progressMonitor.setProgress(progress);
                    progressMonitor.setNote("Create new album: " + album.getTitle().getPlainText());
                    try {
                        pycasaController.insertAlbum(album);
                        List<AlbumEntry> newAlbums = pycasaController.getAlbums();
                        for (int i=0; i<newAlbums.size(); i++) {
                            AlbumEntry tmpAlbum = newAlbums.get(i);
                            if (tmpAlbum.getTitle().getPlainText().equals(album.getTitle().getPlainText())) {
                                album = tmpAlbum;
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        progressMonitor.close();
                        JOptionPane.showMessageDialog(owner, "Unable to create new album.", "Error Upload", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                if (progressMonitor.isCanceled()) {
                    progressMonitor.close();
                    return;
                }

                for (int i = 0; i < photoEntries.size(); i++) {
                    PhotoEntry photo = photoEntries.get(i);
                    try {
                        pycasaController.insert(album, photo);
                        progressMonitor.setProgress(++progress);
                        progressMonitor.setNote("Completed " + (i + 1) + " of " + size);
                    } catch (Exception ex) {
                        String msg = "Could not upload photo: " + photo.getTitle().getPlainText();
                        JOptionPane.showMessageDialog(owner, msg, "Error Upload", JOptionPane.ERROR_MESSAGE);
                        
                        // if error due to album is null
                        if (album == null) {
                            progressMonitor.close();
                            return;
                        }
                    }

                    if (progressMonitor.isCanceled()) {
                        progressMonitor.close();
                    }
                }
            }
        };
        
        Thread uploadThread = new Thread(run);
        uploadThread.start();
    }
}
