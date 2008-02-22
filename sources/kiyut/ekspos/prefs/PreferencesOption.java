/*
 * PreferencesOption.java
 *
 * Created on September 22, 2004, 12:22 AM
 */

package kiyut.ekspos.prefs;

/**
 * PreferencesOption Interface
 *
 * @author  Kiyut
 */
public interface PreferencesOption {
    /** Preferences ROOT_NODE */
    public static final String ROOT_NODE = "/kiyut/ekspos/prefs";
    
    /** load prefs from datastore */
    public void load();
    
    /** save prefs from datastore */
    public void save();
}
