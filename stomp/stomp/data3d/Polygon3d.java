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
 * Polygon3d class.  Represents a simple polygon.
 * Polygons may have any number of vertices.
 */
public class Polygon3d implements Primitive, java.io.Serializable
{
    private static final Color MAGENTA = new Color(255, 0, 255);
    private static final Color BLACK = new Color(0, 0, 0);
    private static final Color BLUE = new Color(0, 0, 255);
    private static final Color HIDDEN = new Color(140, 140, 140);
    
    private int[] m_indices;    //Array of integers to index into vertex list.
    private Surface m_surface;  //Surface of the plane.
    private Group m_group;
    
    private Vector3f m_normal = new Vector3f();

    private boolean m_selected;
    private boolean m_hidden;

    private Vertex m_center = new Vertex();
    private Vertex m_normalOffset = new Vertex();

    //Temporary variables we don't want to keep instantiating
    private Vector3f v1 = new Vector3f();
    private Vector3f v2 = new Vector3f();
    private Vertex[] pts = new Vertex[2];
    private Point2d p0 = new Point2d();
    private Point2d p1 = new Point2d();
    private int xs[];// = new int[m_indices.length];
    private int ys[];// = new int[m_indices.length];

    /**
     * Plane constructor.  Constructs a plane with any number of vertices.
     *
     * @param indices Integer array of indices into the vertex list.
     */
    public Polygon3d(int indices[])
    {
        m_indices = indices;
        pts[0] = new Vertex();
        pts[1] = new Vertex();
        xs = new int[m_indices.length];
        ys = new int[m_indices.length];
    }

    /**
     * Polygon3d constructor.  Construct a plane with 3 vertices.
     *
     * @param v1 first vertex index
     * @param v2 second vertex index
     * @param v3 third vertex index
     */
    public Polygon3d(int v1, int v2, int v3)
    {
        m_indices = new int[3];
        m_indices[0] = v1;
        m_indices[1] = v2;
        m_indices[2] = v3;
        pts[0] = new Vertex();
        pts[1] = new Vertex();
        xs = new int[m_indices.length];
        ys = new int[m_indices.length];
    }

    /**
     * Polygon3d constructor.  Construct a plane with 4 vertices.
     *
     * @param v1 first vertex index
     * @param v2 second vertex index
     * @param v3 third vertex index
     * @param v4 fourth vertex index
     */
    public Polygon3d(int v1, int v2, int v3, int v4)
    {
        m_indices = new int[4];
        m_indices[0] = v1;
        m_indices[1] = v2;
        m_indices[2] = v3;
        m_indices[3] = v4;
        pts[0] = new Vertex();
        pts[1] = new Vertex();
        xs = new int[m_indices.length];
        ys = new int[m_indices.length];
    }
    
    public Group getGroup()
    {
        return m_group;
    }

    public void setGroup(Group g)
    {
        m_group = g;
    }

    public boolean containsVertex(FastVector vertices, Vertex v)
    {
        for(int i = 0; i < m_indices.length; i++)
        {
            xs[i] = (int)((Vertex)vertices.elementAtFast(m_indices[i])).x;
            ys[i] = (int)((Vertex)vertices.elementAtFast(m_indices[i])).y;
        }

        Polygon py = new Polygon(xs, ys, xs.length);
            
        if(py.contains((int)v.x, (int)v.y))
        {
            return true;
        }

        return false;

    }
    
    /**
     * Make a copy of the plane.
     *
     * @return copy of the object
     */
    public Object clone()
    {
        int indices[] = new int[m_indices.length];
        System.arraycopy(m_indices, 0, indices, 0, m_indices.length);
        Polygon3d p = new Polygon3d(indices);
        p.m_surface = m_surface;
        p.m_normal.set(m_normal);
        p.m_center.set(m_center);
        p.m_normalOffset.set(m_normalOffset);
        p.m_hidden = m_hidden;
        p.m_selected = m_selected;
        p.m_group = m_group;
        
        return p;
    }

    /**
     * Get the Polygon3d's surface.
     *
     * @return reference to the Polygon3d's surface.
     */
    public Surface getSurface()
    {
        return m_surface;
    }

    /**
     * Set the Polygon3d's surface.
     *
     * @param Surface to set Polygon3d's surface to.
     */
    public void setSurface(Surface surf)
    {
        m_surface = surf;
    }

    /**
     * Return whether the Polygon3d is selected.
     *
     * @return true if this Polygon3d is selected.
     */
    public boolean isSelected()
    {
        return m_selected;
    }

    /**
     * Set the Polygon3d to be selected/unselected
     *
     * @param select true selects the Polygon3d, false deselects the Polygon3d.
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
    public final void paint(FastVector vertices, Graphics g)
    {
//         if(m_hidden)
//         {
//             return;
//         }
        
        //Select the color depending on whether this is selected or not.
        if(m_selected)
        {
            g.setColor(MAGENTA);
        }
        else
        {
            if(m_hidden)
            {
                g.setColor(HIDDEN);
            }
            else if(m_group == null)
            {
                g.setColor(BLACK);
            }
            else
            {
                g.setColor(m_group.getColor());
            }
        }

        p1.x = ((Vertex)vertices.elementAtFast(m_indices[0])).x;
        p1.y = ((Vertex)vertices.elementAtFast(m_indices[0])).y;

        //Faster, but need to modify clipping to do polygon!!        
        if(!SutherlandHodgman.CLIP)
        {
            //Faster way to draw polygons.  Uses drawPolygon which is
            //only one system function call and often accelerated
            int i = 0;
            for(; i < m_indices.length - 1; i++)
            {
                p0.x = p1.x;
                p0.y = p1.y;
                p1.x = ((Vertex)vertices.elementAtFast(m_indices[i+1])).x;
                p1.y = ((Vertex)vertices.elementAtFast(m_indices[i+1])).y;
                
                xs[i] = (int)p0.x;
                ys[i] = (int)p0.y;
            }
            
            xs[i] = (int)p1.x;
            ys[i] = (int)p1.y;
            
            g.drawPolygon(xs, ys, m_indices.length);
        }
        else
        {
            //Draw the lines of the Polygon3d.
            for(int i = 0; i < m_indices.length - 1; i++)
            {
                p0.x = ((Vertex)vertices.elementAtFast(m_indices[i])).x;
                p0.y = ((Vertex)vertices.elementAtFast(m_indices[i])).y;
                p1.x = ((Vertex)vertices.elementAtFast(m_indices[i+1])).x;
                p1.y = ((Vertex)vertices.elementAtFast(m_indices[i+1])).y;
                
                if(SutherlandHodgman.clip2d(p0, p1))
                {
                    g.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
                }
            }
            
            p0.x = ((Vertex)vertices.elementAtFast(m_indices[0])).x;
            p0.y = ((Vertex)vertices.elementAtFast(m_indices[0])).y;
            p1.x = ((Vertex)vertices.elementAtFast(m_indices[m_indices.length-1])).x;
            p1.y = ((Vertex)vertices.elementAtFast(m_indices[m_indices.length-1])).y;
            
            if(SutherlandHodgman.clip2d(p0, p1))
            {
                g.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
            }
        }
//          else
//          {
//              p0.x = ((Vertex)vertices.elementAtFast(m_indices[m_indices.length-1])).x;
//              p0.y = ((Vertex)vertices.elementAtFast(m_indices[m_indices.length-1])).y;

//              if(SutherlandHodgman.clip2d(p0, p1))
//              {
//                  g.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
//              }

//              //Draw the lines of the Polygon3d.
//              for(int i = 0; i < m_indices.length - 1; i++)
//              {
//                  p0.x = p1.x;//((Vertex)vertices.elementAtFast(m_indices[i])).x;
//                  p0.y = p1.y;//((Vertex)vertices.elementAtFast(m_indices[i])).y;
//                  p1.x = ((Vertex)vertices.elementAtFast(m_indices[i+1])).x;
//                  p1.y = ((Vertex)vertices.elementAtFast(m_indices[i+1])).y;
                
//                  if(SutherlandHodgman.clip2d(p0, p1))
//                  {
//                      g.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
//                  }
//              }
//          }        
    }
    
    /**
     * Select this Polygon3d if the point is inside it.
     *
     * @param vertices Orthogonally projected vertices.
     * @param x X coordinate of mouse click.
     * @param y Y coordinate of mouse click.
     */
    public final boolean select(FastVector vertices, int x, int y)
    {
        if(m_hidden)
        {
            return false;
        }
        
        for(int i = 0; i < m_indices.length; i++)
        {
            xs[i] = (int)((Vertex)vertices.elementAtFast(m_indices[i])).x;
            ys[i] = (int)((Vertex)vertices.elementAtFast(m_indices[i])).y;
        }

        Polygon py = new Polygon(xs, ys, xs.length);
            
        if(py.contains(x, y))
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
    public final boolean selectRegion(FastVector vertices, int xmin, int xmax,
                                      int ymin, int ymax)
    {
        if(m_hidden)
        {
            return false;
        }
        
        Vertex el;
        int count = 0;
        for(int i = 0; i < m_indices.length; i++)
        {
            el = (Vertex)vertices.elementAtFast(m_indices[i]);
            if(el.x > xmin && el.x < xmax &&
               el.y > ymin && el.y < ymax)
            {
                if(++count > 1)
                {
                    return true;
                }
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
            int newIndices[] = new int[m_indices.length-1];
            xs = new int[m_indices.length];
            ys = new int[m_indices.length];
            if(newIndices.length < 3)
            {
                return true;
            }

            
            int j = 0;
            for(int i = 0; i < m_indices.length; i++)
            {
                if(m_indices[i] != -1)
                {
                    newIndices[j++] = m_indices[i];
                }
            }

            m_indices = newIndices;
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
        boolean replaced = false;
        int verticesDeleted = 0;
        for(int i = 0; i < m_indices.length; ++i)
        {
            if(m_indices[i] == oldIndex)
            {
                m_indices[i] = newIndex;
                replaced = true;
            }
        }

        //if(replaced)
        {
            for(int i = 0; i < m_indices.length; ++i)
            {
                for(int j = 1; j < m_indices.length; ++j)
                {
                    if(i != j && m_indices[i] == m_indices[j])
                    {
                        m_indices[i] = -1;
                        ++verticesDeleted;
                    }
                }
            }
        
            //If a vertex has been deleted, create a new array that is
            //one size smaller.  Copy all indices except the one
            //deleted into the new array.
            if(verticesDeleted > 0)
            {
                int newIndices[] = new int[m_indices.length-verticesDeleted];
                int xs[] = new int[newIndices.length];
                int ys[] = new int[newIndices.length];
                
                int j = 0;
                for(int i = 0; i < m_indices.length; i++)
                {
                    if(m_indices[i] != -1)
                    {
                        newIndices[j++] = m_indices[i];
                    }
                }

                m_indices = newIndices;
            }
        }
    }
    
    /**
     * Compute the normal to the Polygon3d.
     *
     * @param vertices Vector of vertices in world coordinates.
     */
    public boolean computeNormal(FastVector vertices)
    {
        int j;
        double A = 0, B = 0, C = 0;

        //Calculate Polygon3d coeffients A, B, and C.
        for(int i = 0; i < m_indices.length; i++)
        {
            j = (i + 1) % m_indices.length;
            A += ( ((Vertex)vertices.elementAtFast(m_indices[i])).z +
                   ((Vertex)vertices.elementAtFast(m_indices[j])).z) *
                ( ((Vertex)vertices.elementAtFast(m_indices[j])).y -
                  ((Vertex)vertices.elementAtFast(m_indices[i])).y);
            B += ( ((Vertex)vertices.elementAtFast(m_indices[i])).x +
                   ((Vertex)vertices.elementAtFast(m_indices[j])).x) *
                ( ((Vertex)vertices.elementAtFast(m_indices[j])).z -
                  ((Vertex)vertices.elementAtFast(m_indices[i])).z);
            C += ( ((Vertex)vertices.elementAtFast(m_indices[i])).y +
                   ((Vertex)vertices.elementAtFast(m_indices[j])).y) *
                ( ((Vertex)vertices.elementAtFast(m_indices[j])).x -
                  ((Vertex)vertices.elementAtFast(m_indices[i])).x);
        }
        
        A /= 2.0;
        B /= 2.0;
        C /= 2.0;

        //Set and normalize the normal.
        m_normal.set((float)A, (float)B, (float)C);
        if(m_normal.length() < .000001)
        {
            return true;
        } 
        m_normal.scale(1/m_normal.length()); //make unit length

        //Find the center of the Polygon3d, so we know how to draw the
        //normal.
        m_center.set(0, 0, 0);
        for(int i = 0; i < m_indices.length; i++)
        {
            Vertex v = (Vertex)vertices.elementAtFast(m_indices[i]);
            m_center.add(v);
        }
        
        m_center.scale((float)(1/(float)m_indices.length));

        //Make a new line from the center to some offset off of
        //the center for drawing the normal.
        m_normalOffset.set(0, 0, 0);
        Vector3f len = new Vector3f();
        len.sub((Vertex)vertices.elementAtFast(m_indices[0]), m_center);
        m_normalOffset.scaleAdd(len.length(),
                                m_normal, m_center);
        return false;
    }

    /**
     * Set this Polygon3d's 3 indices to new indices.
     * Should be rarely used.
     *
     * @param v1 first vertex index
     * @param v2 second vertex index
     * @param v3 third vertex index
     */
    public void setIndices(int v1, int v2, int v3)
    {
        m_indices[0] = v1;
        m_indices[1] = v2;
        m_indices[2] = v3;
    }

    /**
     * Set this Polygon3d's indices to new indices.
     * Should be rarely used.
     */
    public void setIndices(int indices[])
    {
        m_indices = new int[indices.length];
        xs = new int[m_indices.length];
        ys = new int[m_indices.length];
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
     * Get the center vertex on the Polygon3d.
     *
     * @return Vertex that lies in center of Polygon3d.
     */
    public Vertex getCenter(Vector vertices)
    {
        Vertex center = new Vertex();

        //Calculate the center and return it
        center.set(0, 0, 0);
        for(int i = 0; i < m_indices.length; i++)
        {
            Vertex v = (Vertex)vertices.elementAt(m_indices[i]);
            center.add(v);
        }
        
        center.scale((float)(1/(float)m_indices.length));

        return center;
    }

    /**
     * Get the two vertices that define how the normal is drawn.
     *
     * Used *only* for drawing the normal to the Polygon3d..`
     *
     * @return Array of 2 vertices that define the normal.
     */
    public Vertex[] getNormalPoints()
    {
        pts[0].set(m_center);
        pts[1].set(m_normalOffset);

        return pts;
    }

    /**
     * Get the vector which defines the normal to the Polygon3d.
     *
     * @return The vector defining the normal.
     */
    public Vector3f getNormal()
    {
        return m_normal;
    }

    /**
     * Draw the normal.  We expect the passed in vertices for the normal
     * to be after projection.
     */
    public void paintNormal(Vertex[] vert, Graphics g)
    {
        p0.x = vert[0].x;
        p0.y = vert[0].y;
        p1.x = vert[1].x;
        p1.y = vert[1].y;

        if(SutherlandHodgman.clip2d(p0, p1))
        {
            g.setColor(BLUE);
            g.drawLine((int)p0.x, (int)p0.y,
                       (int)p1.x, (int)p1.y);
        }
    }

    /**
     * Flip the Polygon3d so the normal faces the other way.
     */
    public void flip()
    {
        int temp;

        //Simply reverse indices vertices
        for(int i = 0; i < m_indices.length/2; i++)
        {
            temp = m_indices[i];
            m_indices[i] = m_indices[m_indices.length-1 - i];
            m_indices[m_indices.length-1 - i] = temp;
        }
    }

    /**
     * Transform the Polygon3d's vertices by some transformation.
     *
     * @param tr Transformation to transform points on Polygon3d with.
     * @param fromVertices vector of vertices in world coordinates
     * @param toVertices vector of vertices in world coordinates
     */
    public final void transform(Transformation tr, FastVector fromVertices,
                                FastVector toVertices)
    {
        //Loop through all indices, and transform corresponding vertices.
        for(int i = 0; i < m_indices.length; i++)
        {
            tr.transformVertex( (Vertex)fromVertices.elementAtFast(m_indices[i]),
                                (Vertex)toVertices.elementAtFast(m_indices[i]));
        }
        
        //Every time we transform, recompute the normal so it is accurate.
        //computeNormal(toVertices);
    }
}
