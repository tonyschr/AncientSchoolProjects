package stomp.command;

import stomp.*;
import stomp.data3d.*;
import java.util.*;

/**
 * Select a primitive or vertex (depending on the mode) in the
 * scene.
 */
public class HideSelectedCommand implements Command
{
    private Scene m_scene;

    private FastVector m_vertices = new FastVector();
    private FastVector m_orderedSelectedVerts = new FastVector();
    private FastVector m_orderedSelectedPrims = new FastVector();

    private HideSelectedCommand()
    {
    }
    
    public HideSelectedCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_orderedSelectedVerts = m_scene.getOrderedSelectedVertices();
        m_orderedSelectedPrims = m_scene.getOrderedSelectedPrimitives();
        m_vertices = m_scene.getVerticesVector();
        
//         Vertex tempVertex;
//         for(int i = 0; i < m_orderedSelectedVerts.sizeFast(); i++)
//         {
//             tempVertex = (Vertex)m_orderedSelectedVerts.elementAtFast(i);
//             tempVertex.setHidden(true);
//             m_scene.deselect(tempVertex);
//         }

        Primitive tempPrim;
        int indices[];
        for(int i = 0; i < m_orderedSelectedPrims.sizeFast(); i++)
        {
            tempPrim = (Primitive)m_orderedSelectedPrims.elementAtFast(i);
            tempPrim.setHidden(true);
            m_scene.deselect(tempPrim);

            //Hide attached vertices
            indices = tempPrim.getIndices();
            for(int j = 0; j < indices.length; j++)
            {
                ((Vertex)m_vertices.elementAtFast(indices[j])).setHidden(true);
            }
        }

        m_scene.validateScene();
        
        return true;
    }
    
    /**
     */
    public void unExecute()
    {
        m_scene.deselectVertices();
        m_scene.deselectPrimitives();
        
        Primitive tempPrim;
        int indices[];
        for(int i = 0; i < m_orderedSelectedPrims.sizeFast(); i++)
        {
            tempPrim = (Primitive)m_orderedSelectedPrims.elementAtFast(i);
            tempPrim.setHidden(false);
            m_scene.select(tempPrim);

            //Hide attached vertices
            indices = tempPrim.getIndices();
            for(int j = 0; j < indices.length; j++)
            {
                ((Vertex)m_vertices.elementAtFast(indices[j])).setHidden(false);
            }
        }

        m_scene.validateScene();
    }
    
    public String toString()
    {
        return "Select";
    }

}
