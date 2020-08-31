package stomp.data3d;

import java.util.Vector;
import java.awt.*;
import javax.vecmath.*;

import stomp.*;
import stomp.data3d.Primitive;
import stomp.view.View;

/**
 * Vertex class.  A vertex is a point in the scene.
 */
public class Vertex extends Point3f implements Cloneable, java.io.Serializable
{
    protected static final Color HIDDEN = new Color(140, 140, 140);
    private static final Color BLACK = new Color(0, 0, 0);
    private static final Color YELLOW = new Color(255, 255, 0);
    private static final Color RED = new Color(255, 0, 0);
    private boolean m_hidden = false;
    private boolean m_selected;

    /**
     * Constructor.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @param z z coordinate.
     */
    public Vertex(float x, float y, float z)
    {
        super(x, y, z);
    }

    /**
     * Constructor.
     *
     * @param p array of 3 floats for x, y, z coords
     */
    public Vertex(float p[])
    {
        super(p);
    }

    /**
     * Construct a vertex from a point
     *
     * Construct a vertex from another vertex.
     */
    public Vertex(Point3f p1)
    {
        super(p1);
    }

    /**
     * Default constructor.
     */
    public Vertex()
    {
        super();
    }

    /**
     * Set this vertex to the passed in one
     *
     * @param v Vertex to set this one equal to.
     */
    public final void set(Vertex v)
    {
        super.set(v);
        m_hidden = v.m_hidden;
        m_selected = v.m_selected;
    }

    /**
     * Clone this vertex.
     *
     * @return New vertex with same attributes
     */
    public Object clone()
    {
        Vertex temp = new Vertex(x, y, z);
        temp.m_hidden = m_hidden;
        temp.m_selected = m_selected;

        return temp;
    }

    /**
     * Set this vertex's hidden state.
     *
     * @param hide true to hide, false to unhide.
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
     * Return whether this vertex is selected.
     *
     * @return true if selected, false otherwise.
     */
    public final boolean isSelected()
    {
        return m_selected;
    }

    /**
     * Set the vertex's selected state.
     *
     * @param select true to select, false to unselect.
     */
    public final void setSelected(boolean select)
    {
        m_selected = select;
    }

    /**
     * Paint this vertex.
     *
     * @param vertices Unnecessary, but used to be there to conform
     * with Primitive interface.
     * @param g Graphics context.
     */
    public final void paint(Graphics g)
    {
        if(SutherlandHodgman.pointInBounds((int)x, (int)y))
        {
            if(m_selected)
            {
                g.setColor(YELLOW);
                g.fillRect((int)x - 2, (int)y - 2, 5, 5);
                g.setColor(RED);
            }
            else if(m_hidden)
            {
                g.setColor(HIDDEN);
            }
            else
            {
                g.setColor(BLACK);
            }

            g.drawLine((int)x - 1, (int)y, (int)x + 1, (int)y);
            g.drawLine((int)x, (int)y - 1, (int)x, (int)y + 1);
        }
    }

    /**
     * Select the vertex.
     * This method assumes we've already been transformed onto
     * the XY plane in device coordinates!
     *
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     */
    public final boolean select(Vector vertices, int mouseX, int mouseY)
    {
        if(m_hidden)
        {
            return false;
        }
        
        double dist = Math.sqrt( Math.abs(x - mouseX) *
                                 Math.abs(x - mouseX) +
                                 Math.abs(y - mouseY) *
                                 Math.abs(y - mouseY));

        if(dist < 5.0)
        {
            return true;
        }
        
        return false;
    }

    /**
     * Select vertex if it is within the region.
     * This method assumes we've already been transformed onto
     * the XY plane in device coordinates!
     *
     * @param xmin left side of bounding box.
     * @param xmax right side of bounding box.
     * @param ymin bottom side of bounding box.
     * @param ymax top side of bounding box.
     */
    public final boolean selectRegion(Vector vertices, int xmin, int xmax,
                                      int ymin, int ymax)
    {
        if(m_hidden)
        {
            return false;
        }
        
        if( x >= xmin && x <= xmax && y >= ymin && y <= ymax)
        {
            return true;
        }

        return false;
    }
}
