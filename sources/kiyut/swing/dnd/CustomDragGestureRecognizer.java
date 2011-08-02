/*
 * DragGestureRecognizer.java
 *
 * Created on March 4, 2005, 7:40 PM
 */

package kiyut.swing.dnd;

import java.awt.event.*;
import java.awt.dnd.DragSource;

import javax.swing.*;
import javax.swing.event.*;

/** DragGestureRecognizer for custom Swing Component
 *
 * @author Kiyut
 */
public class CustomDragGestureRecognizer extends MouseInputAdapter {
    /** Armed Event */
    protected MouseEvent armedEvent = null;
    
    /** Drag Threshold, by default it same with DragSource.getDragThreshold */
    protected int threshold = DragSource.getDragThreshold();
    
    /** Creates a new instance of DragGestureRecognizer */
    public CustomDragGestureRecognizer() {
    }
    
    /** {@inheritDoc} */
    public void mousePressed(MouseEvent evt) {
        armedEvent = null;
        
        if (isDragPossible(evt) && getDragAction(evt) != TransferHandler.NONE) {
            armedEvent = evt;
            evt.consume();
        }
    }
    
    /** {@inheritDoc} */
    public void mouseReleased(MouseEvent evt) {
        armedEvent = null;
    }
    
    /** {@inheritDoc} */
    public void mouseDragged(MouseEvent evt) {
        if (armedEvent != null) {
            evt.consume();
            
            int action = getDragAction(evt);
            
            if (action == TransferHandler.NONE) {
                return;
            }
            
            int dx = Math.abs(evt.getX() - armedEvent.getX());
            int dy = Math.abs(evt.getY() - armedEvent.getY());
            if ((dx > threshold) || (dy > threshold)) {
                JComponent c = (JComponent)evt.getSource();
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, armedEvent, action);
                armedEvent = null;
            }
        }
    }
    
    
    /**
     * Determines if the following are true:
     * <ul>
     * <li>the press event is located over a selection
     * <li>the dragEnabled property is true
     * <li>A TranferHandler is installed
     * </ul>
     * <p>
     * This is implemented to check for a TransferHandler.
     * Subclasses should perform the remaining conditions.
     * @param evt MouseEvent
     */
    protected boolean isDragPossible(MouseEvent evt) {
        boolean b = false;
        JComponent c = (JComponent)evt.getSource();
        return (c == null) ? true : (c.getTransferHandler() != null);
    }
    
    /** Determine TransferHandler Action from mouse event 
     * @param evt MouseEvent
     */
    protected int getDragAction(MouseEvent evt) {
        //If they are holding down the control key, COPY rather than MOVE
        int ctrlMask = InputEvent.CTRL_DOWN_MASK;
        int action = ((evt.getModifiersEx() & ctrlMask) == ctrlMask) ?
              TransferHandler.COPY : TransferHandler.MOVE;
        return action;
    }
}
