/*
 * AbstractPreferencesOption.java
 *
 * Created on September 22, 2004, 12:24 AM
 */

package kiyut.ekspos.prefs;

/**
 *
 * @author  Kiyut
 */
public abstract class AbstractPreferencesOption implements PreferencesOption {
    
    /** {@inheritDoc} */
    public void load() {};
    
    /** {@inheritDoc} */
    public void save() {};
    
}
