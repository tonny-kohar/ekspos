/*
 * DirectoryFilter.java
 *
 * Created on April 21, 2002, 9:43 AM
 */

package kiyut.swing.shell.io;

import java.io.File;

import javax.swing.filechooser.*;

/** Directory Filter
 *
 * @author  tonny
 */
public class DirectoryFilter extends FileFilter {
    private FileSystemView fsv;
    
    /** Creates a new instance of DirectoryFilter 
     * @param fileSystemView
     */
    public DirectoryFilter(FileSystemView fsv) {
        this.fsv = fsv;
    }
    
    /** Whether the given file is accepted by this filter
     * @param file
     * @return true if accepted, otherwise false
     */
    public boolean accept(java.io.File file) {
        return fsv.isTraversable(file).booleanValue();
    }
    
    /** Description of this filter
     * @return description of this filter
     */
    public String getDescription() {
        return "DirectoryFilter";
    }
    
}
