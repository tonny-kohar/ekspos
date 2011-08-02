/*
 * DefaultDropTargetListener.java
 *
 * Created on March 4, 2005, 9:26 PM
 */

package kiyut.swing.dnd;

import java.awt.*;
import java.awt.dnd.*;

import javax.swing.*;

/** DropTargetListener for custom Swing Component
 * The subclasses are expected to implement the following methods to manage the
 * insertion location via the components selection mechanism.
 * <ul>
 * <li>saveComponentState
 * <li>restoreComponentState
 * <li>restoreComponentStateForDrop
 * <li>updateInsertionLocation
 * </ul>
 *
 * @author Kiyut
 */
public class CustomDropTargetListener extends DropTargetAdapter {
    protected JComponent target;
    protected boolean canImport;
    protected Point lastPosition;
    
    /** Creates a new instance of DefaultDropTargetListener */
    public CustomDropTargetListener() {
    }
    
    /**
     * called to save the state of a component in case it needs to
     * be restored because a drop is not performed.
     */
    protected void saveComponentState(JComponent c) {
    }
    
    /**
     * called to restore the state of a component in case a drop
     * is not performed.
     */
    protected void restoreComponentState(JComponent c) {
    }
    
    /**
     * called to restore the state of a component in case a drop
     * is performed.
     */
    protected void restoreComponentStateForDrop(JComponent c) {
    }
    
    /**
     * called to set the insertion location to match the current
     * mouse pointer coordinates.
     */
    protected void updateInsertionLocation(JComponent c, Point p) {
    }
    
    /** {@inheritDoc} */
    public void dragEnter(DropTargetDragEvent evt) {
        target =(JComponent)evt.getDropTargetContext().getComponent();
        TransferHandler th = target.getTransferHandler();
        canImport = th.canImport(target, evt.getCurrentDataFlavors());
        if (canImport) {
            saveComponentState(target);
            lastPosition = evt.getLocation();
        }
    }
    
    /** {@inheritDoc} */
    public void dragOver(DropTargetDragEvent evt) {
        if (canImport) {
            Point p = evt.getLocation();
            updateInsertionLocation(target, p);
            lastPosition = p;
        }
    }
    
    /** {@inheritDoc} */
    public void dragExit(DropTargetEvent evt) {
        if (canImport) {
            restoreComponentState(target);
        }
    }
    
    /** {@inheritDoc} */
    public void drop(DropTargetDropEvent evt) {
        if (canImport) {
            restoreComponentStateForDrop(target);
        }
    }
}
