/*
 * PreferencesPane.java
 *
 * Created on January 8, 2004, 9:43 PM
 */

package kiyut.ekspos.prefs;

import javax.swing.*;

/**
 * Abstract implementaion of Preferences Panel
 *
 * @author  Kiyut
 */
public abstract class PreferencesPane extends JPanel {
    /** load Prefences */
    public abstract void loadPrefs();
    
    /** save Prefences */
    public abstract void savePrefs();
}
