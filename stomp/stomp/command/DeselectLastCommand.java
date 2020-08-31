package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import java.awt.*;

import java.util.*;

/**
 * Deselect all of the primitives and vertices in the scene.
 */
public class DeselectLastCommand implements Command
{
    private Scene m_scene;
    private Object m_itemDeselected = null;
    
    private DeselectLastCommand()
    {
    }
    
    public DeselectLastCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        if(m_scene.verticesSelected())
        {
            FastVector vertices = m_scene.getOrderedSelectedVertices();
            m_itemDeselected = vertices.elementAt(vertices.size()-1);
            m_scene.deselect((Vertex)m_itemDeselected);
            m_scene.validateScene();
            
            return true;
        }
        else if(m_scene.primitivesSelected())
        {
            FastVector prims = m_scene.getOrderedSelectedPrimitives();
            m_itemDeselected = prims.elementAt(prims.size()-1);
            m_scene.deselect((Primitive)m_itemDeselected);
            m_scene.validateScene();
            
            return true;
        }
        
        return false;
    }

    /**
     * 
     */
    public void unExecute()
    {
        if(m_itemDeselected instanceof Vertex)
        {
            m_scene.select((Vertex)m_itemDeselected);
        }
        else if(m_itemDeselected instanceof Primitive)
        {
            m_scene.select((Primitive)m_itemDeselected);
        }

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Deselect Last";
    }
}
