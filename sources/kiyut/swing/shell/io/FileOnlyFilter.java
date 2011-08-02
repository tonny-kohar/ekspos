/*
 * FileOnlyFilter.java
 *
 * Created on April 21, 2002, 10:17 AM
 */

package kiyut.swing.shell.io;

import java.io.File;

import javax.swing.filechooser.*;

/** File Only Filter
 *
 * @author  tonny
 */
public class FileOnlyFilter extends FileFilter {
    private FileSystemView fsv;
    
    /** Creates a new instance of FileOnlyFilter 
     * @param fileSystemView
     */
    public FileOnlyFilter(FileSystemView fsv) {
        this.fsv = fsv;
    }
    
    /** Whether the given file is accepted by this filter
     * @param file
     * @return true if accepted, otherwise false
     */
    public boolean accept(java.io.File file) {
        return !fsv.isTraversable(file).booleanValue();
    }
    
    /** Description of this filter
     * @return description of this filter
     */
    public String getDescription() {
        return "DirectoryFilter";
    }
    
}
