package stomp.command;

import stomp.*;
import stomp.data3d.*;

import java.util.*;

/**
 * Paste primitives or vertices that are in the clipboard.  Pasted
 * items are automatically selected.
 */
public class PasteCommand implements Command
{
    public Scene m_scene;
    FastVector m_addedElements;
    DeselectAllCommand m_deselect;

    private PasteCommand()
    {
    }
    
    public PasteCommand(Scene scene)
    {
        m_deselect = new DeselectAllCommand(scene);
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_deselect.execute();
        m_addedElements = m_scene.paste();

        for(int i = 0; i < m_addedElements.sizeFast(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Primitive)
            {
                m_scene.select((Primitive)m_addedElements.elementAtFast(i));
            }
        }
        
        m_scene.validateScene();

        if(m_addedElements.size() > 0)
        {
            return true;
        }

        return false;
    }

    public void unExecute()
    {
        //First, delete all the new primitives
        for(int i = 0; i < m_addedElements.size(); i++)
        {
            if(m_addedElements.elementAt(i) instanceof Primitive)
            {
                m_scene.removePrimitive((Primitive)m_addedElements.elementAtFast(i));
            }
        }
        
        //Then, it's safe to kill all of the vertices.
        for(int i = 0; i < m_addedElements.size(); i++)
        {
            if(m_addedElements.elementAt(i) instanceof Vertex)
             {
                 m_scene.removeVertex((Vertex)m_addedElements.elementAt(i));
             }
        }

        m_deselect.unExecute();
                
        m_scene.validateScene();

    }

    public String toString()
    {
        return "Paste";
    }

}
