package stomp.command;

import stomp.*;
import stomp.data3d.*;

/**
 * Invert the selected items so that things that were selected are
 * unselected, and things that are unselected get selected.
 */
public class InvertSelectedCommand implements Command
{
    private static final int NONE = 0;
    private static final int VERTICES = 1;
    private static final int PRIMITIVES = 2;
    
    private Scene m_scene;
    private FastVector m_selectedVertices;
    private FastVector m_selectedPrims;
    private int m_whichSelected = NONE;
    
    private InvertSelectedCommand()
    {
    }
    
    public InvertSelectedCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        if(m_scene.verticesSelected())
        {
            m_selectedVertices = m_scene.getSelectedVertices();
            m_whichSelected = VERTICES;

            FastVector vertices = m_scene.getVerticesVector();
            Vertex v;
            for(int i = 0; i < vertices.sizeFast(); ++i)
            {
                v = (Vertex)vertices.elementAtFast(i);
                if(v.isSelected())
                {
                    m_scene.deselect(v);
                }
                else
                {
                    m_scene.select(v);
                }
            }
        }
        else if(m_scene.primitivesSelected())
        {
            m_selectedPrims = m_scene.getSelectedPrimitives();

            FastVector prims = m_scene.getPrimitivesVector();
            Primitive p;
            for(int i = 0; i < prims.sizeFast(); ++i)
            {
                p = (Primitive)prims.elementAtFast(i);
                if(p.isSelected())
                {
                    m_scene.deselect(p);
                }
                else
                {
                    m_scene.select(p);
                }
            }
                
            m_whichSelected = PRIMITIVES;
        }

        m_scene.validateScene();
        return true;
    }

    public void unExecute()
    {
        m_scene.deselectVertices();
        m_scene.deselectPrimitives();

        if(m_whichSelected == VERTICES)
        {
            for(int i = 0; i < m_selectedVertices.sizeFast(); ++i)
            {
                m_scene.select((Vertex)m_selectedVertices.elementAtFast(i));
            }
        }
        else if(m_whichSelected == PRIMITIVES)
        {
            for(int i = 0; i < m_selectedPrims.sizeFast(); ++i)
            {
                m_scene.select((Primitive)m_selectedPrims.elementAtFast(i));
            }
        }

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Invert Selected";
    }
}
