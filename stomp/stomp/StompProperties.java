package stomp;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * StompProperties reads from $HOME/stomp.config and sets a variety of
 * flags throughout the program based on that file.  <p>
 *
 * stomp.config consists of key/value pairs separated by a space.  Keys
 * can be:
 * <ul>
 *   <li> PLUGIN_DIR
 *   <li> UNDO_LEVEL
 *   <li> SUBDIVIDE
 *   <li> VRML_PROGRAM
 *   <li> PREVIEW_PATH
 *   <li> POV_PROGRAM
 *   <li> UPDATE_DELAY
 *   <li> POLYGON_THRESHHOLD
 *   <li> NO_CLIPPING
 * </ul><p>
 * Note: The key value pairs are NOT separated with =
 */
public class StompProperties
{
    Stomp m_stomp;

    /**
     * Constructor.
     */
    public StompProperties(Stomp stomp)
    {
        m_stomp = stomp;
    }

    /**
     * Save the stomp.config file.
     *
     * <p>Not implemented for now.  Don't overwrite users's stomp.config
     * because they may have added extra stuff.
     */
    public void saveProperties()
    {
    }
    
    /**
     * Read the properties from a file in the users's home directory.
     * Match up the key/value pairs.
     */
    public void loadProperties()
    {
        Properties props = System.getProperties();
        String homeDir = props.getProperty("user.home");

        try
        {
            //Tell user where his home directory is...
            System.out.println("Looking for 'stomp.config' in home dir: " +
                               homeDir);

            //Try to read the file.
            FileReader reader = new FileReader(homeDir +
                                               File.separatorChar +
                                               "stomp.config");
            BufferedReader in = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();

            int c;
            c = in.read();
            while(c != -1)
            {
                sb.append((char)c);
                c = in.read();
            }

            StringTokenizer tokens = new StringTokenizer(sb.toString(),
                                                         "\n\r\t, ");
            while(tokens.hasMoreTokens())
            {
                String key = tokens.nextToken();
                String value = tokens.nextToken();

                setStompProperty(key, value);
            }
        }
        catch(IOException e)
        {
            System.out.println("No stomp.config file found, using default settings.");
        }
    }

    /**
     * Set a Stomp property based on the passed in key value pair.
     */
    private void setStompProperty(String key, String value)
    {
        try
        {
            NumberFormat numformat = NumberFormat.getInstance();
            if(key.equals("PLUGIN_DIR"))
            {
                System.out.println("Adding plugins from: " + value);
                m_stomp.addPlugins(value);
            }
            else if(key.equals("UNDO_LEVELS"))
            {
                Mode.STACK_SIZE = numformat.parse(value).intValue();
            }
            else if(key.equals("SUBDIVIDE"))
            {
                Mode.NURB_SUBDIVIDE = numformat.parse(value).intValue();
            }
            else if(key.equals("VRML_PROGRAM"))
            {
                Mode.setPreviewProg(value);
            }
            else if(key.equals("PREVIEW_PATH"))
            {
                Mode.setPreviewPath(value);
            }
            else if(key.equals("POV_PROGRAM"))
            {
                stomp.gui.RenderDialog.m_povLocationString =  value;
            }
            else if(key.equals("UPDATE_DELAY"))
            {
                stomp.gui.ViewContainer.UPDATE_DELAY = numformat.parse(value).intValue();
            }
            else if(key.equals("POLYGON_THRESHHOLD"))
            {
                Mode.DISAPPEAR_THRESHHOLD = numformat.parse(value).intValue();
            }
            else if(key.equals("CLIPPING"))
            {
                if(value.equals("FALSE"))
                {
                    SutherlandHodgman.CLIP = false;
                    System.out.println("Warning: Not performing 2D clipping!");
                }
            }
        }
        catch(ParseException e)
        {
            System.out.println("Error parsing stomp.config file: " + e);
        }
    }
}
