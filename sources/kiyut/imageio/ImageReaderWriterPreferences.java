/*
 * ImageReaderWriterPreferences.java
 *
 * Created on October 8, 2004, 11:56 AM
 */

package kiyut.imageio;

import java.io.IOException;
import java.util.*;

import javax.imageio.*;
import javax.imageio.spi.*;

/** ImageReaderWriterPreferences
 *
 * @author  Kiyut
 */
public class ImageReaderWriterPreferences {
    protected Map<String,String> suffixMap;
    protected Map<String,ImageReaderSpi> readerSpiMap;
    
    /** Creates a new instance of ImageReaderWriterPreferences */
    public ImageReaderWriterPreferences() {
        init();
    }
    
    /** initialize */
    private void init() {
        // using TreeMap to get sortedMap based on the key
        suffixMap = new TreeMap<String,String>();
        readerSpiMap = new TreeMap<String,ImageReaderSpi>();
        
        ///////////////////
        // Init suffix
        //////////////////
        String[] formatNames = ImageIO.getReaderFormatNames();
        for (int i=0; i<formatNames.length; i++) {
            Iterator it = ImageIO.getImageReadersByFormatName(formatNames[i]);
            while (it.hasNext()) {
                ImageReader reader = (ImageReader)it.next();
                javax.imageio.spi.ImageReaderSpi spi = reader.getOriginatingProvider();
                if (spi == null) { continue; }
                String[] suffixes = spi.getFileSuffixes();
                for (int j=0; j<suffixes.length; j++) {
                    suffixMap.put(suffixes[j],formatNames[i]);
                }
            }
        }
        
        ////////////////////
        // init reader
        ///////////////////
        Iterator it = suffixMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            
            Iterator<ImageReader> readerIt = ImageIO.getImageReadersByFormatName((String)entry.getValue());
            if (readerIt.hasNext()) {
                ImageReader reader = readerIt.next();
                readerSpiMap.put((String)entry.getValue(),reader.getOriginatingProvider());
            }
        }
    }

    /** Return ImageReaderSpiMap.
     * The key=formatName value=ImageReaderSpi
     * @return ImageReaderSpiMap
     */
    public Map<String,ImageReaderSpi> getPreferredImageReaderSpi() {
        return readerSpiMap;
    }
    
    /** Return preferred ImageReader
     * @param formatName the Format Name
     * @return preferred ImageReader
     * @see javax.imageio.ImageIO
     */
    public synchronized ImageReader getPreferredImageReaderByFormatName(String formatName) {
        ImageReader reader = null;
        ImageReaderSpi spi = readerSpiMap.get(formatName);
        if (spi != null) {
            try {
                reader = spi.createReaderInstance();
            } catch (IOException ex) {}
        }
        return reader;
    }
    
    /** Return preferred ImageReader
     * @param suffix the file suffix
     * @return preferred ImageReader
     * @see javax.imageio.ImageIO
     */
    public synchronized ImageReader getPreferredImageReaderBySuffix(String suffix) {
        ImageReader reader = null;
        String format = suffixMap.get(suffix);
        if (format != null) {
            reader = getPreferredImageReaderByFormatName(format);
        }
        
        return reader;
    }
    
    /** TODO: not implemented yet, return null*/
    public synchronized ImageWriter getPreferredImageWriterByFormatName(String formatName) {
        //throw new RuntimeException("TODO: not implemented yet");
        return null;
    }
    
    /** TODO not implemented yet, return null */
    public synchronized ImageWriter getPreferredImageWriterBySuffix(String suffix) {
        //throw new RuntimeException("TODO: not implemented yet");
        return null;
    }
}
