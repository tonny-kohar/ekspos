package kiyut.swing.shell.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JLabel;


/** Image utilitis class. It provides  provides static methods for manipulating Images
 *
 * @version 1.0
 * @author  tonny
 */
public class ImageUtilities extends java.lang.Object {
    
    /** scale the image proportionally to fit the supplied width & height
     * if the image is smaller than width & height then nothing change
     * @param img the image
     * @param width the width to which to scale the image
     * @param height the height to which to scale the image.
     * @param ob the ImageObserver object
     * @return new Image
     */
    public static BufferedImage scaleImageToFit(BufferedImage img, int width, int height,ImageObserver ob) {
        double scale;
            
        Dimension src = new Dimension(img.getWidth(ob),img.getHeight(ob));
        Dimension dest = new Dimension(width,height);
        scale = scaleToFit(src,dest);
        
        double w = img.getWidth(ob) * scale;
        double h = img.getHeight(ob) * scale;
        
        //BufferedImage scaledImage = img.createGraphics().getDeviceConfiguration().createCompatibleImage((int)w,(int)h);
        
        BufferedImage scaledImage = new BufferedImage((int)w,(int)h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(img,0,0,(int)w,(int)h,ob);
        g2.dispose();
        
        return scaledImage;
    }
    
    /** Return the scale value from <code>src</code> to <code>dest</code>
     *@param src source dimension
     *@param dest destination dimension
     *@return the scale value
     */
    public static double scaleToFit(Dimension src, Dimension dest) {
        //System.err.println("------------------------------");
        //System.err.println(src.width + " " + src.height);
        //System.err.println(dest.width + " " + dest.height);
        double srcW = src.getWidth();
        double srcH = src.getHeight();
        double destW = dest.getWidth();
        double destH = dest.getHeight();
        double scale = 1;
        if (srcW > srcH) {
            if (srcW > destW) {
                scale = destW / srcW;
            }
            srcH = srcH * scale;
            if (srcH > destH) {
                scale = scale * (destH / srcH);
            }
        } else {
            if (srcH > destH) {
                scale = destH / srcH;
            }
            srcW = srcW * scale;
            if (srcW > destW ) {
                scale = scale * (destW / srcW);
            }
        }
        
        return scale;
    }
    
    /** Return whether the file is a known image file or not
     * It is use ImageIO to determine the file type
     * @param file the file to be queried
     * @return true if it is know image file type, otherwise false
     */
    public static boolean isFileImage(File file) {
        boolean b = false;
        
        String suffix = getFileSuffix(file);
        
        if (suffix != null) {
            Iterator it = ImageIO.getImageReadersBySuffix(suffix);
            b = it.hasNext();
        }
        
        return b;
    }
    
    /** Return file suffix portion of give file.
     * @param file the file to be queried
     * @return suffix (eg: jpg,gif,etc) or null
     * @deprecated replaced by ShellUtilities.getFileSuffix()
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
    
    /** Returns a <code>BufferedImage</code> representation of the icon. 
     * @param icon the icon
     * @return <code>BufferedImage</code> representation of the icon
     */
    public static BufferedImage iconToBufferedImage(Icon icon) {
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        BufferedImage tempImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tempImage.createGraphics();
        icon.paintIcon(new JLabel(),g2,0,0);
        g2.dispose();
        
        return tempImage;
    }
}
