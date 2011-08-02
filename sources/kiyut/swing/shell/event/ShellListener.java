/*
 * ShellListener.java
 *
 * Created on February 11, 2003, 1:52 AM
 */

package kiyut.swing.shell.event;

import java.util.*;

/** Defines the interface for an object that listens to changes in a shell
 *
 * @author  tonny
 */
public interface ShellListener extends EventListener {
    /** invoked after file have been deleted. */
    public void shellDeleted(ShellEvent e);
    
    /** invoked after file have been copied. */
    public void shellCopied(ShellEvent e);
    
    /** invoked after file have been moved. */
    public void shellMoved(ShellEvent e);
}
