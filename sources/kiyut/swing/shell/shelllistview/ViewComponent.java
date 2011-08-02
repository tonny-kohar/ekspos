/*
 * ViewStyle.java
 *
 * Created on March 3, 2005, 4:22 PM
 */

package kiyut.swing.shell.shelllistview;

import javax.swing.*;

/**
 *
 * @author Kiyut
 */
public interface ViewComponent {
  
    /** Returns the ShellListViewModel that provides the data displayed by this component.
     * @return the <code>ShellListViewModel that provides the data displayed by this component
     */
    public ShellListViewModel getViewModel();
    
    /** Returns the ListSelectionModel that is used to maintain selection state. 
     * @return the object that provides selection state, null if selection is not allowed
     */
    public ListSelectionModel getSelectionModel();
}
