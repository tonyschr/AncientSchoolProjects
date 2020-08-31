package stomp.data3d;

import java.util.*;
import java.awt.*;
import javax.vecmath.*;

import stomp.data3d.*;
import stomp.view.*;
import stomp.transform.*;
import stomp.*;

/**
 * Group of primitives.  This will allow named grouping of
 * primitives for user convenience.
 */
public class Group implements Primitive
{
    protected static final Color HIDDEN = new Color(140, 140, 140);
    
    protected boolean m_hidden = false;
    protected int m_indices[];
    protected FastVector m_primitives;
    protected String m_name;
    protected boolean m_selected;
    protected Color m_color;
    protected Group m_group;
    //    protected Accumulator m_accumulator = new Accumulator();

    protected float m_movedX = 0;
    protected float m_movedY = 0;
    protected float m_movedZ = 0;
    protected float m_rotateX = 0;
    protected float m_rotateY = 0;
    protected float m_rotateZ = 0;
    
    /**
     * Constructor.
     *
     * @param name Name of the group
     * @param color Color for the group
     */
    public Group(String name, Color color)
    {
        m_color = color;
        m_primitives = new FastVector();
        m_name = name;
    }

    /**
     * Get the parent group
     */
    public Group getGroup()
    {
        return m_group;
    }

//      public Accumulator getAccumulator()
//      {
//          return m_accumulator;
//      }

    public void moveFromOrigin(float x, float y, float z)
    {
        m_movedX += x;
        m_movedY += y;
        m_movedZ += z;
    }

    public void rotateFromOrigin(float rx, float ry, float rz)
    {
        m_rotateX += rx;
        m_rotateY += ry;
        m_rotateZ += rz;
    }

    public void rotateFromOrigin(AxisAngle4f axang)
    {
        
    }

    public AxisAngle4f rotateBackToOriginAxisAngle()
    {
        return new AxisAngle4f(0, 0, 0, 0);
    }

    public Vector3f backToOrigin()
    {
        return new Vector3f(-m_movedX, -m_movedY, -m_movedZ);
    }

    public Vector3f rotateBackToOrigin()
    {
        return new Vector3f(-m_rotateX, -m_rotateY, -m_rotateZ);
    }
    
    public void moveOrigin(float x, float y, float z)
    {
    }
    
    /**
     * Set the parent group
     */
    public void setGroup(Group g)
    {
        m_group = g;
    }

    public void setHidden(boolean hidden)
    {
        m_hidden = true;
    }

    public boolean isHidden()
    {
        return m_hidden;
    }
    
    /**
     * Get the name of this group.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Get the color of this group
     */
    public Color getColor()
    {
        if(m_hidden)
        {
            return HIDDEN;
        }
        
        if(m_selected)
        {
            return Color.cyan;
        }

        if(m_group == null)
        {
            return m_color;
        }
        else
        {
            return m_group.getColor();
        }
    }
    
    /**
     * Make a copy of the group.
     */
    public Object clone()
    {
        Color c = new Color(m_color.getRed(),
                            m_color.getGreen(),
                            m_color.getBlue());
        Group g = new Group(new String(m_name) + "_clone", c);
        g.setGroup(m_group);
        for(int i = 0; i < m_primitives.size(); i++)
        {
            g.add((Primitive)((Primitive)m_primitives.elementAtFast(i)));
        }

        return g;
    }

    public void updateBounds(FastVector vertices)
    {

    }
    
    /**
     * Add a new primitive to the group.
     */
    public void add(Primitive primitive)
    {
        m_primitives.addElement(primitive);
        primitive.setGroup(this);
    }

    public void exchange(Primitive old, Primitive newPrim)
    {
        m_primitives.setElementAt(newPrim, m_primitives.indexOf(old));
    }
    
    /**
     * Check to see if all of the elements in the group are selected.
     *
     * @return true if ALL of the elements are selected, false otherwise.
     */
    public boolean isSelected()
    {
        return m_selected;
    }

    public void setSurface(Surface surf)
    {
        Primitive p;
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            p = (Primitive)m_primitives.elementAtFast(i);
            if(p instanceof Polygon3d)
            {
                ((Polygon3d)p).setSurface(surf);
            }
            else if(p instanceof Group)
            {
                ((Group)p).setSurface(surf);
            }
        }
    }

    public Surface getSurface()
    {
        Primitive first = null;
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            if(m_primitives.elementAtFast(i) instanceof Polygon3d ||
               m_primitives.elementAtFast(i) instanceof Group)
            {
                first = (Primitive)m_primitives.elementAtFast(i);
            }
        }

        if(first != null)
        {
            if(first instanceof Polygon3d)
            {
                return ((Polygon3d)first).getSurface();
            }
            else if(first instanceof Group)
            {
                return ((Group)first).getSurface();
            }
        }

        return null;
    }
    
    /**
     * Set all of the members of the group selected or unselected.
     */
    public void setSelected(boolean select)
    {
        m_selected = select;
    }

    /**
     * Paint bounding box (or something...) for the group.
     *
     * @param vertices Transformed vertices for the current view.
     * @param g Graphics context.
     */
    public void paint(FastVector vertices, Graphics g)
    {
    }

    /**
     * Return whether user clicked on any primitive within the group.
     */
    public boolean select(FastVector vertices, int x, int y)
    {
        if(m_hidden)
        {
            return false;
        }
        
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            if(((Primitive)m_primitives.elementAtFast(i)).select(vertices, x, y))
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Return whether user selected any primitive within the group.
     */
    public boolean selectRegion(FastVector vertices, int xmin, int xmax,
                                int ymin, int ymax)
    {
        if(m_hidden)
        {
            return true;
        }
        
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            if(((Primitive)m_primitives.elementAtFast(i)).selectRegion(vertices,
                                                                 xmin, xmax,
                                                                 ymin, ymax))
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Transform elements in the group.
     */
    public void transform(Transformation tr, FastVector fromVertices,
                          FastVector toVertices)
    {
        Rotation rot = null;
        //Rotations can only rotate about the center of the group
        if(tr instanceof Rotation)
        {
            rot = (Rotation)tr;
            rot.setAlternativeAxis(m_movedX, m_movedY, m_movedZ);
        }

        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            ((Primitive)m_primitives.elementAtFast(i)).transform(tr, fromVertices,
                                                           toVertices);
        }

        if(rot != null)
        {
            rot.useNormalAxis();
        }
    }

    /**
     * Returns whether any of the primitives inside this group contains
     * the requested index.
     */
    public boolean containsIndex(int index)
    {
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            if(((Primitive)m_primitives.elementAtFast(i)).containsIndex(index))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the list of primitives inside of this group
     */
    public FastVector getPrimitives()
    {
        return m_primitives;
    }

    /**
     * Just so that it confines to Primitive interface.  Groups don't have
     * any indices.
     */
    public int[] getIndices()
    {
        return new int[0];
    }

    /**
     * Just so that it confines to Primitive interface.  Groups don't have
     * any indices.
     */
    public void setIndices(int indices[])
    {
    }

    /**
     * Just so that it confines to Primitive interface.  Groups don't have
     * any indices.
     */
    public boolean renumberIndices(int afterInd)
    {
        return false;
    }

    /**
     * Just so that it confines to Primitive interface.  Groups don't have
     * any indices.
     */
    public void replaceIndex(int oldIndex, int newIndex)
    {
    }

    /**
     * Tell the group to release all of it's primitives.  Loops
     * through and takes the primitives out of this group.
     */
    public void releasePrimitives()
    {
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            ((Primitive)m_primitives.elementAtFast(i)).setGroup(null);
        }

        m_primitives = new FastVector();
    }
}
