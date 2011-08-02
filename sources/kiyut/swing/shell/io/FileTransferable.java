/*
 * TreeTransferable.java
 *
 * Created on October 22, 2003, 2:16 PM
 */

package kiyut.swing.shell.io;

import java.awt.datatransfer.*;

import java.util.*;

/**
 *
 * @author  Tonny Kohar
 */
public class FileTransferable implements Transferable {
    private List data;
    
    /** Creates a new instance of FileTransferable */
    public FileTransferable(List data) {
        this.data = data;
    }
    
    /** {@inheritDoc} */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException {
        if (flavor.equals(DataFlavor.javaFileListFlavor)) {
            return this.data;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
    
    /** {@inheritDoc} */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.javaFileListFlavor };
    }
    
    /** {@inheritDoc} */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor.equals(DataFlavor.javaFileListFlavor)) {
            return true;
        } 
        return false;
    }
    
}
