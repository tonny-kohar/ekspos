/*
 * ShellEvent.java
 *
 * Created on February 10, 2003, 8:52 PM
 */

package kiyut.swing.shell.event;

import java.io.*;
import java.util.*;

/** An event that characterizes a shell event. 
 * Rename File is equal with SHELL_MOVED
 * For the SHELL_DELETED event the destination file is null
 * 
 *
 * @author  tonny
 */
public class ShellEvent extends EventObject {
    /** Identifies shell delete event */
    public static int SHELL_DELETED = 0;
    
    /** Identifies shell copy event */
    public static int SHELL_COPIED = 1;
    
    /** Identifies shell move event */
    public static int SHELL_MOVED = 2;
    
    /** a source file */
    private File sourceFile;
    
    /** a destination file */
    private File destinationFile;
    
    private int type;
    
    /** Construct a ShellEvent object
     * @param source  the Object that originated the event (typically this)
     * @param type an int specifying the event
     * @param sourceFile a source file
     * @param destinationFile a destination file 
     */
    public ShellEvent(Object source, int type, File sourceFile, File destinationFile) {
        super(source);
        this.sourceFile = sourceFile;
        this.destinationFile = destinationFile;
        this.type = type;
    }
    
    /** Returns the event type
     * @return an int representing the type value
     */
    public int getType() {
        return type;
    }
    
    /** Returns the source file
     * @return the source file
     */
    public File getSourceFile() {
        return sourceFile;
    }
    
    /** Returns the destination file or null for SHELL_DELETED event
     * @return the destination file or null for SHELL_DELETED event
     */
    public File getDestinationFile() {
        return destinationFile;
    }
    
}
