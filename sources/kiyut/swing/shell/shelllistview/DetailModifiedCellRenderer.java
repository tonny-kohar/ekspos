/*
 * DetailModifiedCellRenderer.java
 *
 * Created on December 24, 2002, 6:56 PM
 */

package kiyut.swing.shell.shelllistview;

import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileSystemView;

/** Modified date cell renderer
 *
 * @version 1.0
 * @author  tonny
 */
public class DetailModifiedCellRenderer extends DefaultTableCellRenderer {
    DateFormat dateFormatter;
    
    /** Constructs a <code>DetailModifiedCellRenderer</code> */
    public DetailModifiedCellRenderer() {
        dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);

    }
    
    /** Sets the <code>String</code> object for the cell being rendered to <code>value</code>.
     * @param value <code>File</code> for this cell
     */
    public void setValue(Object obj) {
        Date d = (Date)obj;
        setText(dateFormatter.format(d));
    }
}
