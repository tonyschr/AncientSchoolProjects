package stomp.command;

import stomp.*;
import stomp.data3d.*;
import java.util.*;

/**
 * Select a primitive or vertex (depending on the mode) in the
 * scene.
 */
public class UnhideAllCommand implements Command
{
    private Scene m_scene;

    private FastVector m_hiddenElements;

    private UnhideAllCommand()
    {
    }
    
    public UnhideAllCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_hiddenElements = new FastVector();
        
        FastVector vertices = m_scene.getVerticesVector();
        FastVector primitives = m_scene.getPrimitivesVector();

        Vertex tempVertex;
        for(int i = 0; i < vertices.sizeFast(); i++)
        {
            tempVertex = (Vertex)vertices.elementAtFast(i);
            if(tempVertex.isHidden())
            {
                m_hiddenElements.addElement(tempVertex);
                tempVertex.setHidden(false);
            }            
        }

        Primitive tempPrim;
        for(int i = 0; i < primitives.sizeFast(); i++)
        {
            tempPrim = (Primitive)primitives.elementAtFast(i);
            if(tempPrim.isHidden())
            {
                m_hiddenElements.addElement(tempPrim);
                tempPrim.setHidden(false);
            }            
        }

        m_scene.validateScene();
        
        return true;
    }
    
    /**
     */
    public void unExecute()
    {
        for(int i = 0; i < m_hiddenElements.sizeFast(); i++)
        {
            if(m_hiddenElements.elementAtFast(i) instanceof Vertex)
            {
                ((Vertex)m_hiddenElements.elementAtFast(i)).setHidden(true);
            }
            else
            {
                ((Primitive)m_hiddenElements.elementAtFast(i)).setHidden(true);
            }
        }

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Select";
    }

}
