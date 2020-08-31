package stomp.data3d;

import java.util.Vector;

/**
 * SurfaceList is a list of surface names and the
 * associated surfaces.  Used to manage surfaces so that
 * each plane doesn't have to have it's own instance of a surface --
 * it just references into surfaces created in the SurfaceList.
 */
public class SurfaceList implements java.io.Serializable
{
    Vector m_surfaces;
    Vector m_surfaceNames;

    /**
     * SurfaceList constructor.  Creates the default surface.
     */
    public SurfaceList()
    {
        m_surfaces = new Vector();
        m_surfaceNames = new Vector();
        Surface def = new Surface();
        def.setDiffuse(1.0);
        addSurface("Default", def);
    }
    
    /**
     * Add a new surface to the list.
     *
     * @param name Name of the surface
     * @param surface Surface associated with name.
     */
    public void addSurface(String name, Surface surface)
    {
        name = name.replace(' ', '_');

        //If it's a duplicate, don't add it.
        for(int i = 0; i < m_surfaceNames.size(); i++)
        {
            String surfName = (String)m_surfaceNames.elementAt(i);
            //TODO: replace spaces with underscore
            if(surfName.equals(name))
            {
                return;
            }
        }
        
        m_surfaceNames.addElement(name);
        m_surfaces.addElement(surface);
    }

    /**
     * Get a surface from the list using its name
     *
     * @param name Name of surface to get.
     * @return Surface associated with that name.
     */
    public Surface getSurface(String name)
    {
        for(int i = 0; i < m_surfaceNames.size(); i++)
        {
            if( ((String)m_surfaceNames.elementAt(i)).equals(name))
            {
                return (Surface)m_surfaces.elementAt(i);
            }
        }

        return null;
    }

    /**
     * Get the surface name associated with a surface.
     *
     * @param Surface Surface to get name for
     * @return Name of surface.
     */
    public String getSurfaceName(Surface surf)
    {
        int index = m_surfaces.indexOf(surf);

        return (String)m_surfaceNames.elementAt(index);
    }

    public int getSurfaceIndex(Surface surf)
    {
        return m_surfaces.indexOf(surf);
    }

    /**
     * Delete a surface with the given name.
     *
     * @param name Name of surface to delete.
     */
    public void deleteSurface(String name)
    {
        for(int i = 0; i < m_surfaceNames.size(); i++)
        {
            if( ((String)m_surfaceNames.elementAt(i)).equals(name))
            {
                m_surfaceNames.removeElementAt(i);
                m_surfaces.removeElementAt(i);
            }
        }        
    }
    
    /**
     * Get a list of all of the surface names.
     *
     * @return Vector of strings of surface names.
     */
    public Vector getSurfaceNames()
    {
        return m_surfaceNames;
    }
}
