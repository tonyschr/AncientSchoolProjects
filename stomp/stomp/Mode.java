package stomp;

import java.util.*;
import javax.vecmath.*;

/**
 * Mode provides a global point of access for determining the mode.
 * Mode is a singleton.  There is one instance controlled through
 * static methods only.
 */
public class Mode extends java.util.Observable
{
    //Possible modes
    public static final int NONE = 0;
    public static final int VERTEX_SELECT = 1;
    public static final int PRIMITIVE_SELECT = 2;
    public static final int OBJECT_SELECT = 3;
    public static final int REGION_PRIMITIVE_SELECT = 4;
    public static final int REGION_VERTEX_SELECT = 5;
    public static final int TRANSLATE = 6;
    public static final int ROTATE = 7;
    public static final int SCALE = 8;
    public static final int SHEAR = 9;
    public static final int TAPER = 10;
    public static final int ADD_POINTS = 20;
    public static final int ADD_PLANES = 21;
    public static final int ADD_POINTS_FREEDRAW = 22;
    public static final int PAN = 30;
    public static final int ZOOM = 31;

    //Constants
    public static final double ZOOMFACTOR= 1;

    //User options
    public static int NURB_SUBDIVIDE = 1;
    public static int STACK_SIZE = 20;   
    public static float DISAPPEAR_THRESHHOLD = 500;

    //Single instance of mode
    private static Mode m_actualMode;

    //values for the Mode object.
    private int m_mode = NONE;
    private double m_zoom = 40;
    private Vector3d m_pan = new Vector3d();
    private int m_panCount;
    private double m_x, m_y, m_z;

    //Default values for the VRML preview application and path.
    private String m_previewPath = "stomptemp.wrl";
    private String m_previewProg = "explorer.exe";
    
    /**
     * Private constructor.  Mode is a Singleton.  Only one instance
     * of it exists, and that is controlled through Mode's static
     * methods.
     */
    private Mode()
    {
    }

    /**
     * Create the one instance of the object if actual mode is null
     */
    private static void guardMode()
    {
        //If there is no instance of Mode yet, create one.
        if(m_actualMode == null)
        {
            m_actualMode = new Mode();
        }
    }

    public static void resetDefaultMode()
    {
        setZoom(40);
        setPan(new Vector3d(0, 0, 0));
    }
    
    /**
     * Set the current mode
     *
     * @param mode Mode to enable.  Use the public static variables in Mode.
     */
    public static void setMode(int mode)
    {
        guardMode();
        
        m_actualMode.m_mode = mode;
        m_actualMode.setChanged();
        m_actualMode.notifyObservers();
    }

    /**
     * Get the current mode.
     *
     * @return mode, use public static variables to determine the mode.
     */
    public static int getMode()
    {
        guardMode();

        return m_actualMode.m_mode;
    }

    /**
     * Get the single instance of Mode.
     *
     * Use this to set up an observer for mode.
     *
     * @return A reference to the single instance of mode.  Only useful
     * for setting up observers.
     */
    public static Mode getActualMode()
    {
        guardMode();
        
        return m_actualMode;
    }

    /**
     * Get the vector for the current pan.
     *
     * @return Vector3d containing x, y, and z pan amount.
     */
    public static Vector3d getPan()
    {
        guardMode();
        
        return m_actualMode.m_pan;
    }

    /**
     * Set the vector for the current pan.
     *
     * @param pan Vector3d containing x, y, and z pan amount.
     */
    public static void setPan(Vector3d pan)
    {
        guardMode();
        
        m_actualMode.m_pan = pan;
    }

    /**
     * Get the current zoom factor.
     *
     * @return double containing zoom factor.
     */
    public static double getZoom()
    {
        guardMode();
        
        return m_actualMode.m_zoom;
    }

    /**
     * Set the zoom factor.
     *
     * @param zoom zoom factor
     */
    public static void setZoom(double zoom)
    {
        guardMode();
        
        m_actualMode.m_zoom = zoom;
    }

    /**
     * Set the current mouse coordinates to x, y, and z values.
     *
     * Used to print the coordinates in the status bar.
     */
    public static void setMouse(double x, double y, double z)
    {
        guardMode();

        m_actualMode.m_x = x;
        m_actualMode.m_y = y;
        m_actualMode.m_z = z;

        m_actualMode.setChanged();
        m_actualMode.notifyObservers();
    }

    /**
     * Get the X mouse coordinate value.
     */
    public static double getX()
    {
        guardMode();

        return m_actualMode.m_x;
    }

    /**
     * Get the Y mouse coordinate value.
     */
    public static double getY()
    {
        guardMode();

        return m_actualMode.m_y;
    }

    /**
     * Get the Z mouse coordinate value.
     */
    public static double getZ()
    {
        guardMode();

        return m_actualMode.m_z;
    }

    /**
     * Set the system-dependent path for the VRML exporter to write to
     */
    public static void setPreviewPath(String path)
    {
        guardMode();

        m_actualMode.m_previewPath = path;
    }

    /**
     * Set the system-dependent path for the VRML viewing program
     */
    public static void setPreviewProg(String prog)
    {
        guardMode();

        m_actualMode.m_previewProg = prog;
    }

    /**
     * Get the path for the VRML preview file
     */
    public static String getPreviewPath()
    {
        guardMode();

        return m_actualMode.m_previewPath;
    }

    /**
     * Get the path for the VRML preview program
     */
    public static String getPreviewProg()
    {
        guardMode();

        return m_actualMode.m_previewProg;
    }

}
