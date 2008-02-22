/*
 * ShellAdapter.java
 *
 * Created on February 11, 2003, 2:01 PM
 */

package kiyut.swing.shell.event;

/** An abstract adapter class for receiving shell events. 
 * The methods in this class are empty. This class exists as convenience for creating listener objects. 
 * Extend this class to create a ShellEvent listener and override the methods for the events of interest. 
 * (If you implement the KeyListener interface, you have to define all of the methods in it. This abstract class defines null methods for them all, so you can only have to define methods for events you care about.) 
 * @author  tonny
 */
public abstract class ShellAdapter implements ShellListener {
    
    /** invoked after file have been deleted. 
     * @param e a ShellEvent object
     */
    public void shellDeleted(ShellEvent e) {}
    
    /** invoked after file have been copied. 
     * @param e a ShellEvent object
     */
    public void shellCopied(ShellEvent e) {}
    
    /** invoked after file have been moved. 
     * @param e a ShellEvent object
     */
    public void shellMoved(ShellEvent e) {}
}
