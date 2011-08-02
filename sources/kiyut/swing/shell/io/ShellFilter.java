/*
 * ShellFilter.java
 *
 * Created on April 21, 2002, 9:37 AM
 */

package kiyut.swing.shell.io;

import java.io.File;
import java.util.*;

import javax.swing.filechooser.*;

/** This class filter the files according to supplied filter
 *
 * @author  tonny
 */
public class ShellFilter {
    
    /** filter the files according to the specied FileFilter
     * @param FileFilter
     * @param array of files
     * @return list of filtered file
     */
    public static List<File> filter(FileFilter fileFilter, File[] files) {
        List<File> l = new ArrayList<File>();
        
        for (int i=0; i<files.length;i++) {
            File f = files[i];
            if (fileFilter.accept(f)) {
                l.add(f);
            }
        }
        
        return l;
    }
    
}
