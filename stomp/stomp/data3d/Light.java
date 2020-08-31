package stomp.data3d;

import stomp.*;
import stomp.data3d.*;
import java.awt.*;
import java.lang.*;
import java.util.*;
import javax.vecmath.*;
import java.awt.event.*;

import stomp.data3d.*;
import stomp.transform.*;
import stomp.gui.Appearance;

/**
 * Abstract class for implementing different light types.
 */
public abstract class Light implements Primitive
{
    protected static final Color HIDDEN = new Color(140, 140, 140);

    //Primitive attributes
    protected boolean m_hidden = false;
    protected boolean m_selected = false;
    protected int[] m_indices;
    protected Group m_group;
    
    public Light(int indices[])
    {
        m_indices = indices;
    }

    public Group getGroup()
    {
        return m_group;
    }

    public void setHidden(boolean hide)
    {
        m_hidden = hide;
    }
    
    public boolean isHidden()
    {
        return m_hidden;
    }
    
    public void setGroup(Group g)
    {
        m_group = g;
    }
    
    public boolean isSelected()
    {
        return m_selected;
    }

    public void setSelected(boolean select)
    {
        m_selected = select;
    }

    public boolean select(FastVector vertices, int mouseX, int mouseY)
    {
        if(m_hidden)
        {
            return false;
        }
        
        for(int i = 0; i < m_indices.length; i++)
        {
            Vertex lightVertex = (Vertex)vertices.elementAtFast(m_indices[i]);
            
            double dist = Math.sqrt( Math.abs(lightVertex.x - mouseX) *
                                     Math.abs(lightVertex.x - mouseX) +
                                     Math.abs(lightVertex.y - mouseY) *
                                     Math.abs(lightVertex.y - mouseY));
            
            if(dist < 10.0)
            {
                return true;
            }
        }

        return false;
    }
    
    public boolean selectRegion(FastVector vertices, int xmin, int xmax,
                                int ymin, int ymax)
    {
        if(m_hidden)
        {
            return false;
        }
        
        for(int i = 0; i < m_indices.length; i++)
        {
            Vertex el = (Vertex)vertices.elementAtFast(m_indices[i]);
            if(el.x > xmin && el.x < xmax &&
               el.y > ymin && el.y < ymax)
            {
                return true;
            }
        }

        return false;
    }

    public void transform(Transformation tr, FastVector fromVertices,
                          FastVector toVertices)
    {
        for(int i = 0; i < m_indices.length; i++)
        {
            tr.transformVertex((Vertex)fromVertices.elementAtFast(m_indices[i]),
                               (Vertex)toVertices.elementAtFast(m_indices[i]));
        }
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
    
    public int[] getIndices()
    {
        int[] indices = new int[m_indices.length];
        System.arraycopy(m_indices, 0, indices, 0, m_indices.length);
        return m_indices;
    }
    
    public void setIndices(int indices[])
    {
        m_indices = new int[indices.length];
        System.arraycopy(indices, 0, m_indices, 0, indices.length);
    }
    
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
     * Dummy implementation.
     */
    public Object clone()
    {
        return this;
    }
}
