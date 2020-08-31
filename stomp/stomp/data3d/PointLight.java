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
 * This Light primitive represents a point light source.
 */
public class PointLight extends Light
{
    public PointLight(int indices[])
    {
        super(indices);
    }

    public void paint(FastVector vertices, Graphics g)
    {
        Vertex lightVertex = (Vertex)vertices.elementAtFast(m_indices[0]);

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
                g.setColor(Color.blue);
            }
            else
            {
                g.setColor(m_group.getColor());
            }            
        }

        if(SutherlandHodgman.pointInBounds((int)lightVertex.x,
                                           (int)lightVertex.y))
        {        
            g.drawLine((int)lightVertex.x - 5, (int)lightVertex.y,
                       (int)lightVertex.x + 5, (int)lightVertex.y);
            g.drawLine((int)lightVertex.x, (int)lightVertex.y - 5,
                       (int)lightVertex.x, (int)lightVertex.y + 5);
            g.drawLine((int)lightVertex.x - 4, (int)lightVertex.y - 4,
                       (int)lightVertex.x + 4, (int)lightVertex.y + 4);
            g.drawLine((int)lightVertex.x + 4, (int)lightVertex.y - 4,
                       (int)lightVertex.x - 4, (int)lightVertex.y + 4);
        }
    }
    
    public Object clone()
    {
        PointLight clone = new PointLight(m_indices);
        clone.m_selected = m_selected;
        clone.setGroup(m_group);
        return clone;
    }
}
