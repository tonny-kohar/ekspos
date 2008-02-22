/*
 * Preferences.java
 *
 * Created on September 10, 2003, 11:49 PM
 */

package kiyut.ekspos.prefs;

import java.util.*;
import java.util.prefs.*;
import kiyut.ekspos.*;

/** Ekspos preferences. It is basicly just simple wrapper for java.util.prefs
 *
 * @author  tonny
 */
public class GeneralPreferences extends AbstractPreferencesOption {
    private static GeneralPreferences instance;       // The single instance
    
    //private static final String PREFERENCES_NODE = "/kiyut/ekspos";
    
    public static final String PREFERENCES_NODE = PreferencesOption.ROOT_NODE + "/General";
    
    /** cache directory */
    public static final String CACHE_DIRECTORY_KEY = "cache_directory";
    
    /** use cache */
    public static final String USE_CACHE_KEY = "use_cache";
    
    /** docking layout */
    public static final String DOCKING_LAYOUT_KEY = "docking_layout";
    
    /** repaint increment */
    public static final String PROGRESS_REPAINT_KEY = "progress_repaint";
    
    /** repaint increment */
    public static final String FIT_TO_WINDOW_KEY = "fit_to_window";
    
    /** TOTD */
    public static final String TOTD_KEY = "totd";
    
    private Map<String,Object> valueMap;
    
    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return Preferences The single instance.
     */
    static synchronized public GeneralPreferences getInstance() {
        if (instance == null) {
            instance = new GeneralPreferences();
        }
        return instance;
    }
    
    /** Creates a new instance of Preferences */
    private GeneralPreferences() {
        valueMap = new HashMap<String,Object>();
        load();
    }
    
    /** {@inheritDoc} */
    public void load() {
        Preferences prefs = Preferences.userRoot().node(PREFERENCES_NODE);

        String str;
        str = CACHE_DIRECTORY_KEY;
        valueMap.put(str,prefs.get(str, System.getProperty("user.home") + "/.thumbnails/normal"));
        
        str = USE_CACHE_KEY;
        valueMap.put(str,new Boolean(prefs.getBoolean(str, true)));
        
        str = PROGRESS_REPAINT_KEY;
        valueMap.put(str, new Double(prefs.getDouble(str,10)));
        
        str = FIT_TO_WINDOW_KEY;
        valueMap.put(str,new Boolean(prefs.getBoolean(str, true)));
        
        str = TOTD_KEY;
        valueMap.put(str,new Boolean(prefs.getBoolean(str, true)));
        
        str = DOCKING_LAYOUT_KEY;
        valueMap.put(str,prefs.getByteArray(str,null));
    }
    
    /** {@inheritDoc} */
    public void save() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node(PREFERENCES_NODE);
        
        String str;
        str = CACHE_DIRECTORY_KEY;
        prefs.put(str, (String)valueMap.get(str));
        
        str = USE_CACHE_KEY;
        prefs.putBoolean(str, ((Boolean)valueMap.get(str)).booleanValue());
        
        str = PROGRESS_REPAINT_KEY;
        prefs.putDouble(str, ((Double)valueMap.get(str)).doubleValue());
        
        str = FIT_TO_WINDOW_KEY;
        prefs.putBoolean(str, ((Boolean)valueMap.get(str)).booleanValue());
        
        str = TOTD_KEY;
        prefs.putBoolean(str, ((Boolean)valueMap.get(str)).booleanValue());
        
        str = DOCKING_LAYOUT_KEY;
        byte[] dockingLayout = (byte[])valueMap.get(str);
        if (dockingLayout != null) {
            prefs.putByteArray(str, dockingLayout);
        }
    }
    
    public void putString(String key, String value) {
        valueMap.put(key,value);
    }
    
    public void putBoolean(String key, boolean value) {
        valueMap.put(key,new Boolean(value));
    }
    
    public void putDouble(String key, double value) {
        valueMap.put(key,new Double(value));
    }
    
    public String getString(String key) {
        return (String)valueMap.get(key);
    }
    
    public boolean getBoolean(String key) {
        return ((Boolean)valueMap.get(key)).booleanValue();
    }
    
    public double getDouble(String key) {
        return ((Double)valueMap.get(key)).doubleValue();
    }
    
    public void putByteArray(String key, byte[] byteArray) {
        valueMap.put(key, byteArray);
    }
    
    public byte[] getByteArray(String key) {
        return (byte[])valueMap.get(key);
    }
}
