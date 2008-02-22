/*
 * IIOPreferences.java
 *
 * Created on September 6, 2004, 4:37 PM
 */

package kiyut.ekspos.prefs;

import java.util.*;
import java.util.prefs.*;

import javax.imageio.*;
import javax.imageio.spi.*;

import kiyut.ekspos.*;
import kiyut.imageio.*;

/** IIOPreferences
 *
 * @author  Kiyut
 */
public class IIOPreferences extends AbstractPreferencesOption {
    private static IIOPreferences instance;  // single instance
    
    public static final String PREFERENCES_NODE = PreferencesOption.ROOT_NODE + "/IIO";
    
    protected static final String VALUE_SEPARATOR = "=";
    protected static final String READER_KEY = "reader";
    
    private ImageReaderWriterPreferences readerWriterPrefs;
    
    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return IIOPreferences The single instance.
     */
    static synchronized public IIOPreferences getInstance() {
        if (instance == null) {
            instance = new IIOPreferences();
        }
        return instance;
    }
    
    /** Creates a new instance of IIOPreferences */
    private IIOPreferences() {
        IIORegistry.getDefaultInstance().registerApplicationClasspathSpis();
        readerWriterPrefs = new ImageReaderWriterPreferences();
        load();
    }
    
    /** {@inheritDoc} */
    public void save() {
        Preferences prefs = Preferences.userRoot().node(PREFERENCES_NODE);
        
        try {
            prefs.clear();
        } catch (BackingStoreException ex) {
            ex.printStackTrace();
        }
        
        ///////////////
        // save reader
        ///////////////
        Map readerMap = readerWriterPrefs.getPreferredImageReaderSpi();
        Iterator it = readerMap.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            ImageReaderSpi spi = (ImageReaderSpi)entry.getValue();
            String value = (String)entry.getKey() + VALUE_SEPARATOR + spi.getClass().getName();
            prefs.put(READER_KEY+Integer.toString(count),value);
            count++;
        }
    }
    
    /** {@inheritDoc} */
    public void load() {
        Preferences prefs = Preferences.userRoot().node(PREFERENCES_NODE);
        
        ///////////////
        // load reader
        ///////////////
        Map<String,ImageReaderSpi> readerMap = readerWriterPrefs.getPreferredImageReaderSpi();
        int count = 0;
        while(true) {
            String value = prefs.get(READER_KEY+Integer.toString(count),null);
            if (value == null) { break; }
            count++;
            
            // split the value
            int index = value.indexOf(VALUE_SEPARATOR);
            if (index == -1) { continue; }
            String format = value.substring(0,index);
            String className = value.substring(index+1);
            
            try {
                Class classDefinition = Class.forName(className);
                ImageReaderSpi spi = (ImageReaderSpi)classDefinition.newInstance();
                readerMap.put(format,spi);
            } catch (Exception ex) {
                System.err.println("unable to load Preferences reader: " + format);
                System.err.println(ex.getMessage());
            }
            
        }
    }
    
    /** Return ImageReaderWriterPreferences.
     * @return ImageReaderWriterPreferences
     */
    public ImageReaderWriterPreferences getImageReaderWriterPreferences() {
        return readerWriterPrefs;
    }
    
    
}
