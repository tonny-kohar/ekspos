package kiyut.ekspos;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import kiyut.ekspos.prefs.IIOPreferences;
import kiyut.swing.shell.image.ImageUtilities;
import kiyut.swing.shell.util.ShellUtilities;

/** ImagePane
 *
 * @author  tonny
 */
public class ImagePane extends JPanel implements IIOReadUpdateListener, IIOReadProgressListener {
    /** an image for this component */
    protected BufferedImage image;
    
    /** the scale with for this component. */
    protected double scaleWidth = 1.0;
    
    /** the scaleHeight for this component. */
    protected double scaleHeight = 1.0;
    
    protected Dimension imageSize;
    
    protected ImageReader imageReader;
    
    protected Thread loadThread;
    
    //protected EventListenerList listenerList;
    
    protected double percentComplete;
    
    protected double repaintProgress = 25;
    
    
    /** Creates a new instance of ImagePane */
    public ImagePane() {
        //listenerList = new EventListenerList();
    }
    
    /** Overriden to display or render the {@code Image}
     * {@inheritDoc}
     * @param g {@inheritDoc}
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);    // paint background
        
        if (image == null) {
            return;
        }
        
        Graphics2D g2 = (Graphics2D) g;
        
        Insets insets = getInsets();
        
        double areaWidth = getWidth() - insets.left - insets.right;
        double areaHeight = getHeight()  - insets.top - insets.bottom;
        
        double x = insets.left;
        double y = insets.top;
        double w = image.getWidth() * scaleWidth;
        double h = image.getHeight() * scaleHeight;
        
        double tmpX = (areaWidth - w)/2;
        double tmpY = (areaHeight - h)/2;
        if (tmpX > x) {
            x = tmpX;
        }
        
        if (tmpY > y) {
            y =tmpY;
        }
        
        //System.err.println("x:" + x + " y:" + y);
        
        g2.drawImage(image,(int)x,(int)y,(int)w,(int)h,this);
    }
    
    /** set the image for this component to display
     * if the value of image is null or empty string, nothing is displayed.
     * @param image the image to display
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        if (image != null) {
            imageSize.width = image.getWidth();
            imageSize.height = image.getHeight();
        }
    }
    
    /** Return Image 
     * @return Image 
     */
    public BufferedImage getImage() {
        return this.image;
    }
    
    /** set the image rescale
     * @param scaleWidth the scaleWidth for the image.
     * @param scaleHeight the scaleHeight for the image.
     */
    public void setImageRescale(double scaleWidth, double scaleHeight) {
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;
    }
    
    /** Set the repaint progress increment value between 0 to 100.
     *The default value is 25
     *@param increment The repaint progress increment
     *@see #getProgressRepaint
     */
    public void setProgressRepaint(double increment) {
        if (increment < 0 || increment > 100) {
            throw new IllegalArgumentException("value must be between 0 - 100");
        }
        this.repaintProgress = increment;
    }
    
    /** Return the repaint progress increment
     * @return repaint progress increment
     * @see #setProgressRepaint(double)
     */
    public double getProgessRepaint() {
        return this.repaintProgress;
    }
    
    /**
     *@see javax.imageio.ImageReader#addIIOReadProgressListener(IIOReadProgressListener)
     */
    public void addIIOReadProgressListener(IIOReadProgressListener listener) {
        listenerList.add(IIOReadProgressListener.class, listener);
    }
    
    /**
     *@see javax.imageio.ImageReader#removeIIOReadProgressListener(IIOReadProgressListener)
     */
    public void removeIIOReadProgressListener(IIOReadProgressListener listener) {
        listenerList.remove(IIOReadProgressListener.class, listener);
    }
    
    /**
     *@see javax.imageio.ImageReader#addIIOReadUpdateListener(IIOReadUpdateListener)
     */
    public void addIIOReadUpdateListener(IIOReadUpdateListener listener) {
        listenerList.add(IIOReadUpdateListener.class, listener);
    }
    
    /**
     *@see javax.imageio.ImageReader#removeIIOReadUpdateListener(IIOReadUpdateListener)
     */
    public void removeIIOReadUpdateListener(IIOReadUpdateListener listener) {
        listenerList.remove(IIOReadUpdateListener.class, listener);
    }
    
    /**
     *@see javax.imageio.ImageReader#addIIOReadWarningListener(IIOReadWarningListener)
     */
    public void addIIOReadWarningListener(IIOReadWarningListener listener) {
        listenerList.add(IIOReadWarningListener.class, listener);
    }
    
    /**
     *@see javax.imageio.ImageReader#removeIIOReadWarningListener(IIOReadWarningListener)
     */
    public void removeIIOReadWarningListener(IIOReadWarningListener listener) {
        listenerList.remove(IIOReadWarningListener.class, listener);
    }
    
    /** Display the File Image
     * @param file <code>File</code>
     */
    public void view(File file) {
        if ( (file==null) || (!ImageUtilities.isFileImage(file)) ) {
            setImage(null);
            repaint();
            return;
        }
        startFileImageLoading(file);
    }
    
    private void startFileImageLoading(File file) {
        
        if (imageReader != null) {
            imageReader.abort();
        }
        
        if (loadThread != null) {
            while (loadThread.isAlive())  {
                try {
                    //Thread.currentThread().wait();
                    loadThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        try {
            image = null;
            imageReader = getImageReader(file);
            Dimension imageDim = new Dimension(imageReader.getWidth(0), imageReader.getHeight(0));
            imageSize = imageDim;
            
            Rectangle rect = SwingUtilities.calculateInnerArea(this,null);
            Dimension size = new Dimension(rect.width,rect.height);
            double scale = ImageUtilities.scaleToFit(imageDim,size);
            scaleWidth = scale;
            scaleHeight = scale;
            
        } catch (IOException e) {}
        
        
        Runnable r = new Runnable() {
            public void run() {
                readImage();
                if (imageReader != null) {
                    imageReader.dispose();
                }
            }
        };
        
        loadThread = new Thread(r);
        loadThread.start();
    }
    
    private void readImage() {
        try {
            imageReader.addIIOReadUpdateListener(this);
            imageReader.addIIOReadProgressListener(this);
            
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==IIOReadUpdateListener.class) {
                    imageReader.addIIOReadUpdateListener((IIOReadUpdateListener)listeners[i+1]);
                } else if (listeners[i]==IIOReadProgressListener.class) {
                    imageReader.addIIOReadProgressListener((IIOReadProgressListener)listeners[i+1]);
                } else if (listeners[i]==IIOReadWarningListener.class) {
                    imageReader.addIIOReadWarningListener((IIOReadWarningListener)listeners[i+1]);
                }
            }
            
            image = imageReader.read(0);
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (imageReader.getInput() instanceof ImageInputStream) {
                try {
                    ((ImageInputStream)imageReader.getInput()).close();
                }   catch (Exception e) {}
            }
            imageReader.removeIIOReadUpdateListener(this);
            imageReader.removeIIOReadProgressListener(this);
            
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==IIOReadUpdateListener.class) {
                    imageReader.removeIIOReadUpdateListener((IIOReadUpdateListener)listeners[i+1]);
                } else if (listeners[i]==IIOReadProgressListener.class) {
                    imageReader.removeIIOReadProgressListener((IIOReadProgressListener)listeners[i+1]);
                } else if (listeners[i]==IIOReadWarningListener.class) {
                    imageReader.removeIIOReadWarningListener((IIOReadWarningListener)listeners[i+1]);
                }
            }
            
            imageReader.dispose();
            imageReader = null;
        }
    }
    
    /** return the <code>ImageReader</code> use to read the specified <code>file</code>
     * @return the <code>ImageReader</code> or null if no suitable reader
     * @throws IOException If an I/O error occurs
     */
    private ImageReader getImageReader(File file) throws IOException {
        // find suitable reader
        String suffix = ShellUtilities.getFileSuffix(file);
        IIOPreferences iioPrefs = IIOPreferences.getInstance();
        ImageReader reader = iioPrefs.getImageReaderWriterPreferences().getPreferredImageReaderBySuffix(suffix);
        if (reader == null) {
            return null;
        }
        
        ImageInputStream iis = ImageIO.createImageInputStream(file);
        reader.setInput(iis,true);
        
        return reader;
    }
    
    /** {@inheritDoc} */
    public void imageComplete(ImageReader source) {
        percentComplete = 100;
        repaint();
    }
    
    /** {@inheritDoc} */
    public void imageProgress(ImageReader source, float percentageDone) {
        if (((percentageDone - percentComplete) >= repaintProgress)  ) {
            //paintImmediately(0,0,getWidth(),getHeight());
            repaint();
            percentComplete = percentageDone;
        }
    }
    
    /** {@inheritDoc} */
    public void imageStarted(ImageReader source, int imageIndex) {
        percentComplete = 0;
    }
    
    /** {@inheritDoc} */
    public void imageUpdate(ImageReader source, BufferedImage theImage, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
        //repaint();
    }
    
    /** {@inheritDoc} */
    public void passComplete(ImageReader source, BufferedImage theImage) {
    }
    
    /** {@inheritDoc} */
    public void passStarted(ImageReader source, BufferedImage theImage, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
        if (pass == 0) {
            this.image = theImage;
            repaint();
        }
    }
    
    /** {@inheritDoc} */
    public void readAborted(ImageReader source) {
    }
    
    /** {@inheritDoc} */
    public void sequenceComplete(ImageReader source) {
    }
    
    /** {@inheritDoc} */
    public void sequenceStarted(ImageReader source, int minIndex) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailComplete(ImageReader source) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailPassComplete(ImageReader source, BufferedImage theThumbnail) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailPassStarted(ImageReader source, BufferedImage theThumbnail, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailProgress(ImageReader source, float percentageDone) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
    }
    
    /** {@inheritDoc} */
    public void thumbnailUpdate(ImageReader source, BufferedImage theThumbnail, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {
    }
    
}
