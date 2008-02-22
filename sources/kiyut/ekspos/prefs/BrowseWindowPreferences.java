/*
 * BrowseWindowPreferences.java
 *
 * Created on October 9, 2004, 5:06 PM
 */

package kiyut.ekspos.prefs;

import java.awt.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;

/**
 *
 * @author  Kiyut
 */
public class BrowseWindowPreferences extends AbstractPreferencesOption  {
    private static BrowseWindowPreferences instance;       // The single instance
    
    public static final String PREFERENCES_NODE = PreferencesOption.ROOT_NODE + "/BrowseWindow";
    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";
    public static final String EXTENDED_STATE_KEY = "extended_state";
        
    private int x;
    private int y;
    private int width;
    private int height;
    private int extendedState;
    
    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return Preferences The single instance.
     */
    static synchronized public BrowseWindowPreferences getInstance() {
        if (instance == null) {
            instance = new BrowseWindowPreferences();
        }
        return instance;
    }
    
    /** Creates a new instance of BrowseWindowPreferences */
    protected BrowseWindowPreferences() {
        load();
    }
    
    public void load() {
        Preferences prefs = Preferences.userRoot().node(PREFERENCES_NODE);
        x = prefs.getInt(X_KEY,-1);
        y = prefs.getInt(Y_KEY,-1);
        width = prefs.getInt(WIDTH_KEY,800);
        height = prefs.getInt(HEIGHT_KEY,600);
        extendedState = prefs.getInt(EXTENDED_STATE_KEY,JFrame.MAXIMIZED_BOTH);
        
        if (width < 400) {
            width = 400;
        }
        if (height < 400) {
            height = 400;
        }
        
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        if (x >= size.width-48) {
            x = -1;
        }
        if (y >= size.height-48) {
            y = -1;
        }
        if (width > size.width) {
            width = size.width;
        }
        if (height > size.height) {
            height = size.height;
        }
    }
    
    public void save() {
        Preferences prefs = Preferences.userRoot().node(PREFERENCES_NODE);
        prefs.putInt(X_KEY,x);
        prefs.putInt(Y_KEY,y);
        prefs.putInt(WIDTH_KEY,width);
        prefs.putInt(HEIGHT_KEY,height);
        prefs.putInt(EXTENDED_STATE_KEY,extendedState);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }

    public void setBounds(Rectangle rv) {
        setBounds(rv.x,rv.y,rv.width,rv.height);
    }
    
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public int getExtendedState() {
        return extendedState;
    }
    
    public void setExtendedState(int extendedState) {
        this.extendedState = extendedState;
    }
    
}
