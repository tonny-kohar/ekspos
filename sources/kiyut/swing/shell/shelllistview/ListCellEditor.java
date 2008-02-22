/*
 * ListCellEditor.java
 *
 * Created on December 10, 2002, 1:57 PM
 */

package kiyut.swing.shell.shelllistview;

import java.awt.event.*;

import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

/** 
 * The editor for <code>ListView</code> cells. 
 * @version 1.0
 * @author  tonny
 */
public class ListCellEditor extends AbstractCellEditor  {
    /** the editor component */
    protected JTextField         editor;
    
    /** Constructs a <code>ListCellEditor</code> with all default values */
    public ListCellEditor() {
        editor = new JTextField();
        editor.setBorder(UIManager.getBorder("TextField.border"));
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(KeyEvent e) { onKeyPressed(e); }
        });
    }
    
    
     /**
     *  Sets an initial <code>value</code> for the editor.  This will cause
     *  the editor to <code>stopEditing</code> and lose any partially
     *  edited value if the editor is editing when this method is called. <p>
     *
     *  Returns the component that should be added to the client's
     *  <code>Component</code> hierarchy.  Once installed in the client's
     *  hierarchy this component will then be able to draw and receive
     *  user input.
     *
     * @param	list		the <code>JList</code> that is asking the
     *				editor to edit; can be <code>null</code>
     * @param	value		the value of the cell to be edited; it is
     *				up to the specific editor to interpret
     *				and draw the value.  For example, if value is
     *				the string "true", it could be rendered as a
     *				string or it could be rendered as a check
     *				box that is checked.  <code>null</code>
     *				is a valid value
     * @param	isSelected	true if the cell is to be rendered with
     *				highlighting
     * @param	row     	the row of the cell being edited
     * @return	the component for editing
     */
    public JComponent getListCellEditorComponent(JList list,Object value, boolean isSelected, int row) {
        ListView listView = (ListView)list;
        ShellListViewModel dataModel = (ShellListViewModel)listView.getModel();
        FileSystemView fsv = dataModel.getFileSystemView();
        File file = dataModel.getFile(row);
        editor.setText(fsv.getSystemDisplayName(file));
        if (isSelected) { editor.selectAll(); }
        return editor;
    }
    
    /** Returns the value contained in the editor.
     * @return the value contained in the editor
     *
     */
    public Object getCellEditorValue() {
        return editor.getText();
    }
    
    /** Invoked when a key has been pressed.
     * @param e a <code>KeyEvent</code> object
     */
    private void onKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            fireEditingStopped();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            fireEditingCanceled();
        }
    }
    
}
