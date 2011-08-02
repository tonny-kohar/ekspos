/*
 * DetailNameCellRenderer.java
 *
 * Created on April 22, 2002, 9:30 AM
 */

package kiyut.swing.shell.shelllistview;

import java.io.File;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileSystemView;

/** File with Icon cell renderer, it render the filename with the icon
 *
 * @version 1.0
 * @author  tonny
 */
public class DetailNameCellRenderer extends DefaultTableCellRenderer {
    /** the <code>FileSystemView</code> for this renderer */
    private FileSystemView fsv;
    
    /** Constructs a <code>DetailNameCellRenderer</code>
     * @param fsv FileSystemView
     */
    public DetailNameCellRenderer(FileSystemView fsv) {
        super();
        this.fsv = fsv;
    }
    
    /** Sets the <code>String</code> object for the cell being rendered to <code>value</code>.
     * @param value <code>File</code> for this cell
     */
    public void setValue(Object value) {
        File file = (File)value;
        setText(fsv.getSystemDisplayName(file));
        setIcon(fsv.getSystemIcon(file));
    }
}
