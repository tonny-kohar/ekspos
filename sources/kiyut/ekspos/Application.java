package kiyut.ekspos;

import java.awt.Image;
import java.awt.Toolkit;


/**
 * Application Class. This class contains static information about the application
 * eg: name, version. etc
 *
 * @author  Kiyut
 */
public final class Application {

    private String name = "Ekspos";
    private String longName = "Ekspos Image Viewer";
    private String version = "1.0";
    private String buildNumber = "201001251528";
    private Image iconImage;

    private static Application instance;
    static {
        instance = new Application();
    }

    private Application() {
    }
    
     /** Return Application Name
     * @return name
     */
    public static String getName() {
        return instance.name;
    }

    /** Return Application Long Name
     * @return long name
     */
    public static String getLongName() {
        return instance.longName;
    }
    
    /** Convenience method for getting application name and version
     * @return name with version appended
     */
    public static String getNameVersion() {
        return instance.name + " " + instance.version;
    }

    /** Return Application Version
     * @return version
     */
    public static String getVersion() {
        return instance.version;
    }

    /** Return Build Number in the format YYYYMMDDHHMM
     * @return buildNumber
     */
    public static String getBuildNumber() {
        return instance.buildNumber;
    }
    
    /** Return icon image
     * @return image
     */
    public static Image getIconImage() {
        if (instance.iconImage == null) {
            instance.iconImage = Toolkit.getDefaultToolkit().createImage(Application.class.getResource("/kiyut/ekspos/ekspos.png"));
        }
        return instance.iconImage;
    }
}
