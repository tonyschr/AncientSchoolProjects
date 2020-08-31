package stomp.data3d;

import java.util.Vector;
import java.awt.*;
import javax.vecmath.*;

import stomp.*;
import stomp.FastVector;
import stomp.data3d.*;
import stomp.view.View;
import stomp.transform.Transformation;

/**
 * NOT FULLY IMPLEMENTED.  This will someday represent a true sphere rather
 * than just an approximation of one.
 */
public class RealSphere implements Primitive, java.io.Serializable
{
    private static final Color MAGENTA = new Color(255, 0, 255);
    private static final Color BLACK = new Color(0, 0, 0);
    private static final Color BLUE = new Color(0, 0, 255);
    
    private int[] m_indices;    //Array of integers to index into vertex list.
    private Surface m_surface;  //Surface of the sphere.
    private Group m_group;
    
    private boolean m_selected;
    private boolean m_hidden;

    //Temp vars so we don't have to keep reinstantiating them.
    private transient Vertex p0, p1;
    private transient int radius;

    public RealSphere(int centerIndex, int radiusIndex)
    {
        m_indices = new int[2];
        m_indices[0] = centerIndex;
        m_indices[1] = radiusIndex;
    }

    public Group getGroup()
    {
        return m_group;
    }

    public void setGroup(Group g)
    {
        m_group = g;
    }
    
    public Object clone()
    {
        RealSphere sp = new RealSphere(m_indices[0], m_indices[1]);
        sp.m_surface = m_surface;
        sp.m_hidden = m_hidden;
        sp.m_selected = m_selected;
        sp.m_group = m_group;
        
        return sp;
    }

    /**
     * Get the RealSphere's surface.
     *
     * @return reference to the surface.
     */
    public Surface getSurface()
    {
        return m_surface;
    }

    /**
     * Set the RealSphere's surface.
     *
     * @param Surface to set surface to.
     */
    public void setSurface(Surface surf)
    {
        m_surface = surf;
    }

    /**
     * Return whether the RealSphere is selected.
     *
     * @return true if this Polygon3d is selected.
     */
    public boolean isSelected()
    {
        return m_selected;
    }

    /**
     * Set the RealSphere to be selected/unselected
     *
     * @param select true selects the RealSphere, false deselects it.
     */
    public void setSelected(boolean select)
    {
        m_selected = select;
    }

    /**
     * Set the Polygon3d to be hidden/unhidden.
     *
     * @param hide true hides the Polygon3d, false unhides.
     */
    public void setHidden(boolean hide)
    {
        m_hidden = hide;
    }

    public boolean isHidden()
    {
        return m_hidden;
    }
    
    /**
     * Paint the Polygon3d.
     *
     * @param vertices Vector of transformed vertices for the current
     * view.
     * @param g Graphics Context.
     */
    public void paint(FastVector vertices, Graphics g)
    {
        if(m_hidden)
        {
            return;
        }
        
        //Select the color depending on whether this is selected or not.
        if(m_selected)
        {
            g.setColor(MAGENTA);
        }
        else
        {
            if(m_group == null)
            {
                g.setColor(BLACK);
            }
            else
            {
                g.setColor(m_group.getColor());
            }
        }

        p0 = (Vertex)vertices.elementAtFast(m_indices[0]);
        p1 = (Vertex)vertices.elementAtFast(m_indices[1]);
        radius = (int)Math.sqrt( (p0.x - p1.x) * (p0.x - p1.x) +
                                 (p0.y - p1.y) * (p0.y - p1.y));
        
        if(SutherlandHodgman.pointInBounds((int)p0.x, (int)p0.y) &&
           SutherlandHodgman.pointInBounds((int)p1.x, (int)p1.y))
        {
            g.drawOval((int)p0.x - radius, (int)p0.y - radius,
                       (int)p1.x + radius, (int)p1.y + radius);
        }
    }
    
    /**
     * Select this Polygon3d if the point is inside it.
     *
     * @param vertices Orthogonally projected vertices.
     * @param x X coordinate of mouse click.
     * @param y Y coordinate of mouse click.
     */
    public boolean select(FastVector vertices, int x, int y)
    {
        p0 = (Vertex)vertices.elementAtFast(m_indices[0]);

        int dist = (int)Math.sqrt( (p0.x - x) * (p0.x - x) +
                                   (p0.y - y) * (p0.y - y));

        radius = (int)Math.sqrt( (p0.x - p1.x) * (p0.x - p1.x) +
                                 (p0.y - p1.y) * (p0.y - p1.y));

        if(dist <= radius)
        {
            return true;
        }
        
        return false;
    }

    /**
     * Get the number of vertices this Polygon3d has.
     *
     * @return number of indices in Polygon3d.
     */
    public int numIndices()
    {
        return m_indices.length;
    }
    
    /**
     * Select this Polygon3d if any 2 of its vertices are within
     * the bounding box.
     *
     * @param vertices Vector of orthogonally projected vertices
     * @param xmin left of bounding box
     * @param xmax right of bounding box
     * @param ymin bottom of bounding box
     * @param ymax top of bounding box
     */
    public boolean selectRegion(FastVector vertices, int xmin, int xmax,
                                int ymin, int ymax)
    {
        Vertex el;
        int count = 0;
        for(int i = 0; i < m_indices.length; i++)
        {
            el = (Vertex)vertices.elementAtFast(m_indices[i]);
            if(el.x > xmin && el.x < xmax &&
               el.y > ymin && el.y < ymax)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Renumber the indices after a point has been deleted.
     *
     * @param afterInd All indices after this index are decremented
     * @return true if this Polygon3d is left with less than three vertex
     *         indices and should be deleted.
     */
    public boolean renumberIndices(int afterInd)
    {
        boolean vertexDeleted = false;

        //Go through vertices, and mark any that have actually been
        //Deleted.  Renumber the rest that occur after that index.
        int l = m_indices.length;
        for(int i = 0; i < l; i++)
        {
            if(m_indices[i] == afterInd)
            {
                m_indices[i] = -1;
                vertexDeleted = true;
            }
            else if(m_indices[i] > afterInd)
            {
                m_indices[i]--;
            }
        }

        //If a vertex has been deleted, create a new array that is
        //one size smaller.  Copy all indices except the one
        //deleted into the new array.
        if(vertexDeleted)
        {
            return true;
        }

        return false;
    }

    /**
     * Replaces an index to a vertex with another index.
     * Used for merting points.
     *
     * @param oldIndex Index to replace
     * @param newIndex Index that replaces old index.
     */
    public void replaceIndex(int oldIndex, int newIndex)
    {
        for(int i = 0; i < m_indices.length; i++)
        {
            if(m_indices[i] == oldIndex)
            {
                m_indices[i] = newIndex;
            }
        }
    }
    
    /**
     * Set this Polygon3d's indices to new indices.
     * Should be rarely used.
     */
    public void setIndices(int indices[])
    {
        m_indices = new int[indices.length];
        System.arraycopy(indices, 0, m_indices, 0, indices.length);
    }

    /**
     * Get the array of integers of the indices.
     *
     * @return Array of integers which are indices.
     */
    public int[] getIndices()
    {
        int[] indices = new int[m_indices.length];
        System.arraycopy(m_indices, 0, indices, 0, m_indices.length);
        return m_indices;
    }

    public boolean containsIndex(int index)
    {
        int l = m_indices.length;
        for(int i = 0; i < l; i++)
        {
            if(m_indices[i] == index)
            {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Transform the Polygon3d's vertices by some transformation.
     *
     * @param tr Transformation to transform points on Polygon3d with.
     * @param fromVertices vector of vertices in world coordinates
     * @param toVertices vector of vertices in world coordinates
     */
    public void transform(Transformation tr, FastVector fromVertices,
                          FastVector toVertices)
    {
        //Loop through all indices, and transform corresponding vertices.
        for(int i = 0; i < m_indices.length; i++)
        {
            tr.transformVertex( (Vertex)fromVertices.elementAtFast(m_indices[i]),
                                (Vertex)toVertices.elementAtFast(m_indices[i]));
        }
    }
}
