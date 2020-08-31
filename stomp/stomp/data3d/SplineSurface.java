package stomp.data3d;

import java.util.Vector;
import java.awt.*;
import javax.vecmath.*;

import stomp.*;
import stomp.FastVector;
import stomp.data3d.*;
import stomp.view.View;
import stomp.transform.Transformation;

public class SplineSurface implements Primitive
{
    protected static final Color HIDDEN = new Color(140, 140, 140);
    private double STEP = .125;
    private double MESHSTEP = .25;

    private Spline[] m_splines;
    private int[][] m_controlNet;//Array of integers to index into vertex list.
    private Surface m_surface;  //Surface of the plane.
    
    private Vertex[] m_normal = new Vertex[4];

    private boolean m_selected;
    private boolean m_hidden;
    private Group m_group;
    
    private Vertex m_center = new Vertex();
    private Vertex m_normalOffset[] = new Vertex[4];

    //the order of the spline surface (cubic)
    private static final int p = 3;
    //Temporary variables we don't want to keep instantiating
    private Vector3f v1 = new Vector3f();
    private Vector3f v2 = new Vector3f();
    private Vertex[] pts = new Vertex[2];
    private int m_numSplines;
    private int m_sizeSplines;
    private int[]kvu;
    private int[]kvv;
    private int[] corners = new int [4];
    private double[] m_basisFuns;
    double []Nu;
    double []Nv;
    Vertex []tempv;
    Vertex tmp = new Vertex(0, 0, 0);
    double right[];
    double left[];
    Vertex C[];

    private boolean m_closed = false;
    private Point2d p0 = new Point2d();
    private Point2d p1 = new Point2d();
    
    
    /**
     * Splinesurface constructor.
     * Constructs a spline surface with any number of splines.
     *
     * @param splines Spline array.
     */
    public SplineSurface(Spline splines[])
    {
        int tempind[] = splines[0].getIndices();
        int tempind2[] = splines[splines.length-1].getIndices();

        if(tempind.length + tempind2.length > 12)
        {
            STEP *= 2;

            if(tempind.length + tempind2.length > 24)
            {
                STEP *= 2;
            }

            if(tempind.length + tempind2.length > 36)
            {
                STEP *= 2;
            }
        }

        if(tempind[0] == tempind[tempind.length-1] &&
           tempind2[0] == tempind2[tempind.length-1])
        {
            m_closed = true;
        }
        
        m_splines = splines;
        m_numSplines = splines.length;
        m_sizeSplines = splines[1].getIndices().length;
        m_controlNet = new int [m_numSplines][m_sizeSplines];
        for(int i=0; i<splines.length;i++)
        {
            int indices[] = splines[i].getIndices();
            for(int j=0; j<indices.length;j++)
            {
                m_controlNet[i][j] = indices[j];
            }
        }
        corners[0] = m_controlNet[0][0];
        corners[3] = m_controlNet[m_numSplines-1][m_sizeSplines-1];
        corners[1] = m_controlNet[m_numSplines-1][0];
        corners[2] = m_controlNet[0][m_sizeSplines-1];
        kvu = new int [(splines.length)+4];
        kvv = new int [(m_sizeSplines)+4];
        for (int i=0;i<4;i++)
        {
            kvu[i] = 0;
            kvv[i] = 0;
        }

        for (int j=4;j<m_numSplines;j++)
            kvu[j] = j-3;
        for (int j=4;j<m_sizeSplines;j++)
            kvv[j] = j-3;
        for(int i=m_numSplines;i<m_numSplines+4;i++)
            kvu[i] = splines.length - 3;
        for(int i=m_sizeSplines;i<m_sizeSplines+4;i++)
            kvv[i] = m_sizeSplines - 3;
        
        Nu = new double [p + 1];
        Nv = new double [p + 1];
        tempv = new Vertex[p + 1];
        for(int i = 0; i <= p; i++)
        {
            tempv[i] = new Vertex(0, 0, 0);
        }

        int maxlen;
        if(kvv.length > kvu.length)
            maxlen = kvv.length;
        else
            maxlen = kvu.length;
        
        right = new double [maxlen];
        left = new double [maxlen];

        int count = 0;
        double maxlen2 = kvv[kvv.length-1];
        if(kvu[kvu.length-1] > kvv[kvv.length-1])
        {
            maxlen2 = kvu[kvu.length-1];
        }
        for(double j = 0.0; j<=maxlen2; j += STEP)
        {
            count++;
        }

        C = new Vertex[count + 1];
//          for(int i = 0; i < count + 1; i++)
//          {
//              C[i] = new Vertex(0, 0, 0);
//          }
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
     *Compute the normal to the surface corners
     *Used for tessellation of the surface
     */
    public void computeNormal(Vector Vertices)
    {
        Vertex vCorner1 = (Vertex)Vertices.elementAt(corners[1]);
        Vertex vCorner2 = (Vertex)Vertices.elementAt(corners[2]);
        Vertex vCorner0 = (Vertex)Vertices.elementAt(corners[0]);
        Vertex vCorner3 = (Vertex)Vertices.elementAt(corners[3]);
        Vector3f corner1=new Vector3f(vCorner1.x, vCorner1.y, vCorner1.z);
        Vector3f corner2=new Vector3f(vCorner2.x, vCorner2.y, vCorner2.z);
        Vector3f corner0=new Vector3f(vCorner0.x, vCorner0.y, vCorner0.z);
        Vector3f corner3=new Vector3f(vCorner3.x, vCorner3.y, vCorner3.z);
        Vector3f temp1 = new Vector3f();
        Vector3f temp2 = new Vector3f();
        Vector3f temp3 = new Vector3f();
        temp1.sub(corner1, corner0);
        temp2.sub(corner2, corner0);
        temp3.cross(temp1,temp2);
        temp3.normalize();
        m_normal[0] = new Vertex(temp3.x, temp3.y,temp3.z);
        temp1.sub(corner3, corner1);
        temp2.sub(corner0, corner1);
        temp3.cross(temp1,temp2);
        temp3.normalize();
        m_normal[1] = new Vertex(temp3.x,temp3.y, temp3.z);
        temp1.sub(corner0,corner2);
        temp2.sub(corner3, corner2);
        temp3.cross(temp1,temp2);
        temp3.normalize();
        m_normal[2] = new Vertex(temp3.x, temp3.y, temp3.z);
        temp1.sub(corner1, corner3);
        temp2.sub(corner2, corner3);
        temp3.cross(temp1,temp2);
        temp3.normalize();
        m_normal[3] = new Vertex(temp3.x, temp3.y, temp3.z);


        for(int i=0; i< 4; i++)
        {
            m_normalOffset[i] = new Vertex(0,0,0);
            //m_normalOffset[i].set(0, 0, 0);
            Vector3f len = new Vector3f();
            len.sub((Vertex)Vertices.elementAt(m_controlNet[0][0]),
                    (Vertex)Vertices.elementAt(m_controlNet[0][1]));
        
            m_normalOffset[i].set(m_normal[0]);
            m_normalOffset[i].scale(len.length());
        }

        //        m_normal;
    }
    /**
     * Make a copy of the spline Surface.
     *
     * @return copy of the object
     */
    public Object clone()
    {
        Spline []s = new Spline [m_numSplines];
        for(int i = 0; i<m_numSplines; i++)
        {
            int indices[] = new int[m_controlNet[i].length];
            System.arraycopy(m_controlNet[i], 0, indices, 0, m_controlNet[i].length);
            s[i] = new Spline(indices);
        }
        SplineSurface p = new SplineSurface(s); 
        //         p.m_surface = m_surface;
        //         p.m_normal.set(m_normal);
        //         p.m_center.set(m_center);
        //         p.m_normalOffset.set(m_normalOffset);
        p.setGroup(m_group);
        
        return p;
    }

    /**
     *Find the index into the knot vector
     *@param n the size of the knot vector
     *@param u the current position on the curve
     *@param kv the knot vector of the current spline
     *@return index into the knot vector
     */
    private int FindSpan(int n, double u, int[] kv)
    {
        if (u == kv[n+1])
            return(n);
        int low, high;
        int mid;
        low = p;
        high = n+1;
        mid = (low+high)/2;
        while (u < kv[mid] || u>= kv[mid+1])
        {
            if (u < kv[mid])
                high = mid;
            else
                low = mid;
            mid = (low+high)/2;
        }
        return(mid);
    }

    /**
     * calculate the basis functions for the current point on the spline
     * almost identical to the spline object method, but generalized
     * for the two directions
     *@param i the index into the knot vector
     *@param u the current position on the curve
     *@param kv the knot vector of the spline we are calculating
     *@param basis the array into which the basis results are stored
     */
    private void BasisFuns(int i,double u, int[] kv, double []basis)
    {
        double saved;
        double temp;
        basis[0] = 1.0;
        for (int j=1; j<=p; j++)
        {
            int index = i+1-j;
            left[j] = u-kv[i+1-j];
            right[j] = kv[i+j]-u;
            saved = 0.0;
            for( int r=0; r<j; r++)
            {
                temp = basis[r]/(right[r+1]+left[j-r]);
                basis[r] = saved+right[r+1]*temp;
                saved = left[j-r]*temp;
            }
            basis[j] = saved;
        }
    }

    /**
     * Get the plane's surface.
     *
     * @return reference to the plane's surface.
     */
    public Surface getSurface()
    {
        return m_surface;
    }

    /**
     * Set the plane's surface.
     *
     * @param Surface to set plane's surface to.
     */
    public void setSurface(Surface surf)
    {
        m_surface = surf;
    }

    /**
     * Return whether the plane is selected.
     *
     * @return true if this plane is selected.
     */
    public boolean isSelected()
    {
        return m_selected;
    }

    /**
     * Set the plane to be selected/unselected
     *
     * @param select true selects the plane, false deselects the plane.
     */
    public void setSelected(boolean select)
    {
        m_selected = select;
    }

    /**
     * Set the plane to be hidden/unhidden.
     *
     * @param hide true hides the plane, false unhides.
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
     * Calculate the point on the spline surface.
     *
     * @param vertices Vector of transformed vertices for the current
     * view.
     * @param g Graphics Context.
     */
    public Vertex paintSpline(FastVector vertices,double u, double v)
    {
        //Select the color depending on whether this is selected or not.
        //double [] Nu = new double [p+1];
        //double []Nv = new double [p+1];
        int n = (kvu.length-1) - p -1;
        //Get the knot index and basis functions for the u direction
        int uspan = FindSpan(n,u,kvu);
        BasisFuns(uspan,u,kvu,Nu);
        //Get the knot index and basis functions for the v direction
        n = (kvv.length-1) - p - 1;
        int vspan = FindSpan(n,v,kvv);
        BasisFuns(vspan,v,kvv,Nv);
        //Calculate the point on the u spline
        Vertex Sw = new Vertex(0,0,0);
        for(int i=0;i <=p;i++)
        {
            tempv[i].set(0,0,0);
            for(int j=0; j<=p; j++)
            {
                tmp.set(((Vertex)vertices.elementAtFast
                         (m_controlNet[uspan-p+j][vspan-p+i])));
                tmp.scale((float)Nu[j]);
                tempv[i].add(tmp);
            }

            tempv[i].scale((float)Nv[i]);
            Sw.add(tempv[i]);
        }

        return Sw;    
    }
    
    /**
     * Paint the Spline.
     *
     * @param vertices Vector of transformed vertices for the current
     * view.
     * @param g Graphics Context.
     */
    public void paint(FastVector vertices, Graphics g)
    {
        //Select the color depending on whether this is selected or not.
        
        if(m_selected)
        {
            g.setColor(Color.cyan);
        }
        else
        {
            if(m_hidden)
            {
                g.setColor(HIDDEN);
            }
            else if(m_group == null)
            {
                g.setColor(Color.red);
            }
            else
            {
                g.setColor(m_group.getColor());
            }           
        }

        //calculate splines in the V direction
        for(double i = 0.0; i<=kvu[kvu.length-1]; i += MESHSTEP)
        {
            int countC = 0;
            for(double j = 0.0; j<=kvv[kvv.length-1]; j += STEP)
            {
                //Retrieve the next point on the curve
                C[countC] = paintSpline(vertices, i, j);
                countC++;
            }
            for(int k= 1; k< countC/*C.size()*/; k+=1)
            {
                //draw lines to the points calculated on the
                //current spline
                p0.x = C[k-1].x;
                p0.y = C[k-1].y;
                p1.x = C[k].x;
                p1.y = C[k].y;

                if(SutherlandHodgman.clip2d(p0, p1))
                {
                    g.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
                }
            }
        }

        //Calculate splines in the U direction
        for(double i = 0.0; i<=kvv[kvv.length-1]; i += MESHSTEP)
        {
            int countC = 0;
            for(double j =0.0; j<=kvu[kvu.length-1]; j += STEP)
            {
                //Get the next point on the spline
                C[countC] = paintSpline(vertices, j, i);
                countC++;
            }
            for(int k = 1; k < countC; k++)
            {
                p0.x = C[k-1].x;
                p0.y = C[k-1].y;
                p1.x = C[k].x;
                p1.y = C[k].y;

                if(SutherlandHodgman.clip2d(p0, p1))
                {
                    g.drawLine((int)p0.x, (int)p0.y, (int)p1.x, (int)p1.y);
                }
            }
         }
    }
    
    /**
     * Select this SplineSurface if the point is inside it.
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
        
        float minX = 100000;
        float minY = 100000;
        float maxX = -100000;
        float maxY = -100000;

        int xs[] = new int[m_numSplines*m_sizeSplines];
        int ys[] = new int[m_numSplines*m_sizeSplines];

        for(int i = 0; i < m_numSplines; i++)
        {
            for(int j=0; j<m_controlNet[i].length; j++)
            {
                float tempX = ((Vertex)vertices.elementAtFast
                              (m_controlNet[i][j])).x;
                float tempY = ((Vertex)vertices.elementAtFast
                              (m_controlNet[i][j])).y;

                if(tempX > maxX)
                {
                    maxX = tempX;
                }
                else if (tempX < minX)
                {
                    minX = tempX;
                }
                if(tempY > maxY)
                {
                    maxY = tempY;
                }
                else if(tempY < minY)
                {
                    minY = tempY;
                }
            }
        }

        return y > minY && y < maxY && x > minX && x < maxX;

        //return false;
    }

    /**
     * Get the number of vertices this SplineSurface has.
     *
     * @return number of indices in SplineSurface.
     */
    public int numIndices()
    {
        int number = 0;
        for(int i =0; i< m_numSplines;i++)
            number+= m_controlNet[i].length;
        return number;
    }

    public boolean containsIndex(int index)
    {
        for(int i = 0; i < m_numSplines; i++)
        {
            for(int j = 0; j < m_controlNet[i].length; j++)
            {
                if(m_controlNet[i][j] == index)
                {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * Select this SplineSurface if any of its vertices are within
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
        
        for(int i = 0; i < m_numSplines; i++)
        {
            for(int j=0; j<m_sizeSplines; j++)
            {
                Vertex el = (Vertex)vertices.elementAtFast
                    (m_controlNet[i][j]);
                if(el.x > xmin && el.x < xmax &&
                   el.y > ymin && el.y < ymax)
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
     * @return true if this splinesurface is left with less than three vertex
     *         indices and should be deleted.
     */
    public boolean renumberIndices(int afterInd)
    {
        for(int i = 0; i < m_numSplines; i++)
        {
            for(int j = 0; j < m_controlNet[i].length; j++)
            {
                if(m_controlNet[i][j] == afterInd)
                {
                    return true;
                }
                else if(m_controlNet[i][j] > afterInd)
                {
                    m_controlNet[i][j]--;
                }
            }
        }

        return false;
    }

    /**
     * Set this surface's indices to new indices.
     * Should be rarely used.
     */
    public void setIndices(int indices[])
    {
        for(int i = 0; i < m_numSplines; i++)
        {
            for(int j = 0; j < m_sizeSplines; j++)
            {
                 m_controlNet[i][j] = indices[i + m_numSplines * j];
            }
        }
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
        for(int i = 0; i < m_numSplines; i++)
        {
            for(int j = 0; j < m_controlNet[i].length; j++)
            {
                if(m_controlNet[i][j] == oldIndex)
                {
                    m_controlNet[i][j] = newIndex;
                }
            }
        }
    }

    public Spline[] getSplines()
    {
        return m_splines;
    }
    
    /**
     * Get the array of integers of the indices.
     *
     * @return Array of integers which are indices.
     */
    public int[] getIndices()
    {
        int[] indices = new int[m_numSplines * m_sizeSplines];
        
        for(int i = 0; i < m_numSplines; i++)
        {
            for(int j = 0; j < m_sizeSplines; j++)
            {
                indices[i + m_numSplines * j] = m_controlNet[i][j];
            }
        }

        return indices;
    }

    public int getMaxKnotU()
    {
        return kvu[kvu.length-1];
    }
    
    public int getMaxKnotV()
    {
        return kvv[kvv.length-1];
    }

    public boolean isClosed()
    {
        return m_closed;
    }
    
    /**
     * Transform the plane's vertices by some transformation.
     *
     * @param tr Transformation to transform points on plane with.
     * @param fromVertices vector of vertices in world coordinates
     * @param toVertices vector of vertices in world coordinates
     */
    public void transform(Transformation tr, FastVector fromVertices,
                          FastVector toVertices)
    {
        //Loop through all indices, and transform corresponding vertices.
        for(int i = 0; i < m_numSplines; i++)
        {
            for(int j=0;j< m_sizeSplines; j++)
            {
                tr.transformVertex( (Vertex)fromVertices.elementAtFast
                                    (m_controlNet[i][j]),
                                    (Vertex)toVertices.elementAtFast
                                    (m_controlNet[i][j]));
            }
        }
        
        //Every time we transform, recompute the normal so it is accurate.
        //         computeNormal(toVertices);
    }
}






