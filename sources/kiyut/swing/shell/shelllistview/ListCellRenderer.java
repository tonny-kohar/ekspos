/*
 * ListNameCellRenderer.java
 *
 * Created on April 24, 2002, 10:39 AM
 */

package kiyut.swing.shell.shelllistview;

import java.io.File;

import java.awt.Component;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

/** File with icon cell renderer, it render the filename with the icon
 *
 * @version 1.0
 * @author  tonny
 */
public class ListCellRenderer extends DefaultListCellRenderer {
    /** the <code>FileSystemView</code> for this renderer */
    private FileSystemView fsv;

    /** Constructs a <code>ListCellRenderer</code>
     * @param fsv FileSystemView
     */
    public ListCellRenderer(FileSystemView fsv) {
        super();
        this.fsv = fsv;
    }
    
    /** Return a component that has been configured to display the specified value. 
     * @param list The JList we're painting.
     * @param value The value returned by data model.
     * @param index The cells index.
     * @param isSelected True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return A component whose paint() method will render the specified value.
     */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        
        File file = (File)value;
        setText(fsv.getSystemDisplayName(file));
        setIcon(fsv.getSystemIcon(file));
        
        return this;
	}

}
