package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import java.awt.*;

import java.util.*;

/**
 * Deselect all of the primitives and vertices in the scene.
 */
public class DeselectAllCommand implements Command
{
    private Scene m_scene;
    private FastVector m_orderedSelectedVerts = new FastVector();
    private FastVector m_orderedSelectedPrims = new FastVector();
    
    private DeselectAllCommand()
    {
    }
    
    public DeselectAllCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        if(m_scene.numSelected() > 0)
        {
            //Get the selected vertices and primitives
            m_orderedSelectedVerts = m_scene.getOrderedSelectedVertices();
            m_orderedSelectedPrims = m_scene.getOrderedSelectedPrimitives();
            
//             m_primitives = m_scene.getSelectedPrimitives();
//             m_vertices = m_scene.getSelectedVertices();
            
            m_scene.deselectVertices();
            m_scene.deselectPrimitives();
            
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
        //Restore selected vertices and primitives
        
        for(int i = 0; i < m_orderedSelectedVerts.size(); i++)
        {
            m_scene.select((Vertex)m_orderedSelectedVerts.elementAtFast(i));
        }

        for(int i = 0; i < m_orderedSelectedPrims.size(); i++)
        {
            m_scene.select((Primitive)m_orderedSelectedPrims.elementAtFast(i));
        }

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Deselect All";
    }
}
