/*
 * ShellUtilities.java
 *
 * Created on September 9, 2003, 12:24 AM
 */

package kiyut.swing.shell.util;

import java.io.*;

/** Collection of Shell Utilities
 *
 * @author  Kiyut
 */
public class ShellUtilities {
    private static long KB = 1024;
    private static long MB = 1048576;
    private static long GB = 1073741824;
    
    /** Convert byte size into String
     * <p>eg: 1 KB, 1 MB, etc </p>
     * @param size
     * @return string representation of the size
     */
    public static String sizeToString(long size) {
        String str;
        
        if (size == 0) {
            str = "0 KB";
        } else if (size <= KB) {
            str = "1 KB";
        } else if (size <= MB) {
            str = (size/KB) + " KB";
        } else if (size <= GB) {
           str = (size/MB) + " MB";
        } else {
            str = (size/GB) + " GB";
        }
        
        return str;
    }
    
    /** Return hex String of the given bytes
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuffer buf = new StringBuffer();
        
        int i;
        for (i = 0; i < bytes.length; i++) {
            if (((int) bytes[i] & 0xff) < 0x10)
                buf.append("0");
            buf.append(Integer.toString((int) bytes[i] & 0xff, 16));
        }
        
        return buf.toString().toUpperCase();
    }
    
    /** Return file suffix portion of give file.
     * @param file the file to be queried
     * @return suffix (eg: jpg,gif,etc) or null
     */
    public static String getFileSuffix(File file) {
        String suffix = null;
        
        // get suffix
        if (file.isFile()) {
            String filename = file.getName();
            int indexOf = filename.lastIndexOf(".");
            if (indexOf > 0) {
                suffix = filename.substring(indexOf+1).toLowerCase();
            }
            
        }
        return suffix;
    }
}
