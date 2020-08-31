package stomp.data3d;

import java.util.Vector;
import java.awt.*;
import javax.vecmath.*;

import stomp.*;
import stomp.FastVector;
import stomp.data3d.*;
import stomp.view.View;
import stomp.transform.Transformation;

public class Spline implements Primitive, java.io.Serializable
{
    private int[] m_indices;    //Array of integers to index into vertex list.
    
    private Vector3f m_normal = new Vector3f();

    private boolean m_selected;
    private boolean m_hidden;
    private Group m_group;
    
    private Vertex m_center = new Vertex();
    private Vertex m_normalOffset = new Vertex();
    private static final Color m_splineColor = new Color(90, 30, 0);
    protected static final Color HIDDEN = new Color(140, 140, 140);

    //Temporary variables we don't want to keep instantiating
    private Vector3f v1 = new Vector3f();
    private Vector3f v2 = new Vector3f();
    private Vertex[] pts = new Vertex[2];
    private int p = 3;
    private int[]kv;
    private double[] m_basisFuns;

    private boolean m_closed = false;
    private Point2d p0 = new Point2d();
    private Point2d p1 = new Point2d();    
    
    /**
     * Spline constructor.  Constructs a spline with any number of vertices.
     *
     * @param indices Integer array of indices into the vertex list.
     */
    public Spline(int indices[])
    {
        m_indices = indices;

        if(indices[0] == indices[indices.length-1])
        {
            m_closed = true;
        }
        
        pts[0] = new Vertex();
        pts[1] = new Vertex();
        
        int size = m_indices.length;

        m_basisFuns = new double[4];
        kv = new int [size+4];

        for (int i = 0; i < 4; i++)
        {
            kv[i] = 0;
        }
        
        for (int i = 4; i < m_indices.length; i++)
        {
            kv[i] = i-3;
        }
        
        for(int i = m_indices.length; i < m_indices.length+4; i++)
        {
             kv[i] = size - 3;
        }

    }

    public Group getGroup()
    {
        return m_group;
    }

    public void setGroup(Group g)
    {
        m_group = g;
    }
    
    /**
     * Make a copy of the spline.
     *
     * @return copy of the object
     */
    public Object clone()
    {
        int indices[] = new int[m_indices.length];
        System.arraycopy(m_indices, 0, indices, 0, m_indices.length);
        Spline p = new Spline(indices);
        p.setGroup(m_group);
        
        return p;
    }

    /**
     *return the index into the Knot Vector that is just below the
     *current point
     *@param n size of the knot Vector
     *@param u current point
     *Uses the member array kv
     *@return index to knot vector element
     */
    private int FindSpan(int n, double u)
    {
        if (u == kv[n+1])
        {
            return(n);
        }
        
        int low = p;
        int high = n + 1;
        int mid = (low + high)/2;
        
        while(u < kv[mid] || u >= kv[mid + 1])
        {
            if(u < kv[mid])
                high = mid;
            else
                low = mid;
            mid = (low+high)/2;
        }
        
        return mid;
    }

    /**
     *calculate the basis function for the current point, and
     *set the member array m_basisFuns
     *@param i knot index
     *@param u current position on curve
     */
    private void BasisFuns(int i, double u)
    {
        m_basisFuns = new double [p + 1];
        double right[] = new double [kv.length];
        double left[] = new double [kv.length];
        double saved;
        double temp;
        m_basisFuns[0] = 1.0;
        int index;
        for (int j = 1; j <= p; j++)
        {
            index = i+1-j;
            left[j] = u - kv[i + 1 - j];
            right[j] = kv[i + j] - u;
            saved = 0.0;
            for(int r = 0; r < j; r++)
            {
                temp = m_basisFuns[r]/(right[r+1]+left[j-r]);
                m_basisFuns[r] = saved+right[r+1]*temp;
                saved = left[j-r]*temp;
            }
            m_basisFuns[j] = saved;
        }


    }
    /**
     * Return whether the spline is selected.
     *
     * @return true if this spline is selected.
     */
    public boolean isSelected()
    {
        return m_selected;
    }

    public boolean isClosed()
    {
        return m_closed;
    }
    
    /**
     * Set the spline to be selected/unselected
     *
     * @param select true selects the spline, false deselects the spline.
     */
    public void setSelected(boolean select)
    {
        m_selected = select;
    }

    /**
     * Set the spline to be hidden/unhidden.
     *
     * @param hide true hides the spline, false unhides.
     */
    public void setHidden(boolean hide)
    {
        m_hidden = hide;
    }

    public boolean isHidden()
    {
        return m_hidden;
    }
    
    public FastVector getVerticesAlongPath(double step, FastVector coordinateVertices)
    {
        FastVector C = new FastVector();
        
        for(double i = 0.0; i<=kv[kv.length-1]; i+=step)
        {
            int n = (kv.length-1) - p -1;
            int span = FindSpan(n,i);
            BasisFuns(span,i);
            Vertex Cw = new Vertex (0,0,0);
            double oldx;
            double oldy;
            //for the current position on the curve, calculate the
            //point on the curve by summing the control points times
            //the basis functions
            for(int j=0; j<=p; j++)
            {
                Vertex temp =
                    new Vertex((Vertex)coordinateVertices.elementAtFast
                               (m_indices[span-p+j]));
                temp.scale((float)m_basisFuns[j]);
                Cw.add(temp);
            }
            C.addElement(Cw);
        }

        return C;
    }
    
    /**
     * Paint the spline.
     *
     * @param vertices Vector of transformed vertices for the current
     * view.
     * @param g Graphics Context.
     */
    public void paint(FastVector vertices, Graphics g)
    {
        //Select the color depending on whether this is selected or not.
        FastVector C = getVerticesAlongPath(0.25, vertices); //.25 == step
        
        if(m_selected)
        {
            g.setColor(Color.cyan);
            Vertex v1 = (Vertex)C.elementAtFast(0);
            if(SutherlandHodgman.pointInBounds((int)v1.x, (int)v1.y))
            {
                g.drawRect((int)(v1.x - 4), (int)(v1.y - 4), 8, 8);
            }
        }
        else
        {
            if(m_hidden)
            {
                g.setColor(HIDDEN);
            }
            else if(m_group == null)
            {
                g.setColor(m_splineColor);
            }
            else
            {
                g.setColor(m_group.getColor());
            }           
        }
        
        for(int i= 1; i<C.size(); i++)
        {
            p0.x = ((Vertex)C.elementAtFast(i-1)).x;
            p0.y = ((Vertex)C.elementAtFast(i-1)).y;
            p1.x = ((Vertex)C.elementAtFast(i)).x;
            p1.y = ((Vertex)C.elementAtFast(i)).y;

            if(SutherlandHodgman.clip2d(p0, p1))
            {
                g.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
            }
        }
    }
    
    /**
     * Select this spline if the point is inside it.
     *
     * @param vertices Orthogonally projected vertices.
     * @param x X coordinate of mouse click.
     * @param y Y coordinate of mouse click.
     */
    public boolean select(FastVector vertices, int x, int y)
    {
        if(m_hidden)
        {
            return false;
        }
        
        int xs[] = new int[m_indices.length];
        int ys[] = new int[m_indices.length];

        for(int i = 0; i < m_indices.length; i++)
        {
            xs[i] = (int)((Vertex)vertices.elementAtFast(m_indices[i])).x;
            ys[i] = (int)((Vertex)vertices.elementAtFast(m_indices[i])).y;
        }

        Polygon polygon = new Polygon(xs, ys, xs.length);
        
        if(polygon.contains(x, y))
        {
            return true;
        }

        return false;
    }

    /**
     * Get the number of vertices this spline has.
     *
     * @return number of indices in spline.
     */
    public int numIndices()
    {
        return m_indices.length;
    }
    
    /**
     * Select this spline if any of its vertices are within
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
        if(m_hidden)
        {
            return false;
        }
        
        for(int i = 0; i < m_indices.length; i++)
        {
            Vertex el = (Vertex)vertices.elementAtFast(m_indices[0]);
            if(el.x > xmin && el.x < xmax &&
               el.y > ymin && el.y < ymax)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Set this plane's indices to new indices.
     * Should be rarely used.
     */
    public void setIndices(int indices[])
    {
        m_indices = new int[indices.length];
        System.arraycopy(indices, 0, m_indices, 0, indices.length);
    }

    /**
     * Renumber the indices after a point has been deleted.
     *
     * @param afterInd All indices after this index are decremented
     * @return true if this plane is left with less than three vertex
     *         indices and should be deleted.
     */
    public boolean renumberIndices(int afterInd)
    {
        boolean vertexDeleted = false;
        
        //Go through vertices, and mark any that have actually beeN
        //Deleted.  Renumber the rest that occur after that index.
        for(int i = 0; i < m_indices.length; i++)
        {
            if(m_indices[i] == afterInd)
            {
                m_indices[i] = -1;
                vertexDeleted = true;
            }
            if(m_indices[i] > afterInd)
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
            int newKnotVector[] = new int[(m_indices.length-1)+4];
            int size = m_indices.length-1;
            if(newIndices.length < 4)
            {
                return true;
            }
            
            m_basisFuns = new double [4];
            for (int i=0;i<4;i++)
                newKnotVector[i] = 0;
            for (int j=4;j<size;j++)
                newKnotVector[j] = j-3;
            for(int i=size;i<size+4;i++)
                newKnotVector[i] = size - 3;
            
            
            int j = 0;
            for(int i = 0; i < m_indices.length; i++)
            {
                if(m_indices[i] != -1)
                {
                    newIndices[j++] = m_indices[i];
                }
            }
            
            m_indices = newIndices;
            kv = newKnotVector;
        }
        return false;
    }

    public boolean containsIndex(int index)
    {
        for(int i = 0; i < m_indices.length; i++)
        {
            if(m_indices[i] == index)
            {
                return true;
            }
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
     * Get the array of integers of the indices.
     *
     * @return Array of integers which are indices.
     */
    public int[] getIndices()
    {
        //TODO: Return a clone of the array.
        return m_indices;
    }

    /**
     *Get the knot vector
     *
     *@return Array of integers which are the knot vector entries
     */
    public int[] getKnotVector()
    {
        return kv;
    }
    /**
     * Transform the spline's vertices by some transformation.
     *
     * @param tr Transformation to transform points on spline with.
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
        
        //Every time we transform, recompute the normal so it is accurate.
        //         computeNormal(toVertices);
    }
}
