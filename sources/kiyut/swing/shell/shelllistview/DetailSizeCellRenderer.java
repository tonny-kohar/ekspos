/*
 * DetailSizeCellRenderer.java
 *
 * Created on November 28, 2002, 11:21 AM
 */

package kiyut.swing.shell.shelllistview;

import java.io.File;
import java.math.*;

import javax.swing.*;
import javax.swing.table.*;
import kiyut.swing.shell.util.*;

/** it render the size right align and use the bytes format such as KB, MB, GB
 *
 * @version 1.0
 * @author  tonny
 */
public class DetailSizeCellRenderer extends DefaultTableCellRenderer {
    /** Constructs a <code>DetailSizeCellRenderer</code> with all default values */
    public DetailSizeCellRenderer() {
        setHorizontalAlignment(SwingConstants.RIGHT);
    }
    
    /** Sets the <code>String</code> object for the cell being rendered to <code>value</code>.
     * @param value <code>File</code> for this cell
     */
    public void setValue(Object obj) {
        Long l = (Long)obj;
        setText(ShellUtilities.sizeToString(l.longValue()));
    }
}
