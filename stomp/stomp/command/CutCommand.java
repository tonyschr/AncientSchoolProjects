package stomp.command;

import stomp.*;
import stomp.data3d.*;

/**
 * Copy selected primitives or vertices into the clipboard and then
 * delete them from the scene.
 *
 * The clipboard is internal, not system-wide.  If the user copies
 * primitives, the vertices will be copied with them.  If the user only
 * copies vertices, the primitives will NOT be copied.
 */
public class CutCommand implements Command
{
    private Scene m_scene;
    private Command m_deleteSelected;

    private CutCommand()
    {
    }
    
    public CutCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        if(m_scene.numSelected() > 0)
        {
            m_scene.copySelected();
            
            m_deleteSelected = new DeleteSelectedCommand(m_scene);
            m_deleteSelected.execute();
            
            m_scene.validateScene();

            return true;
        }

        return false;
    }

    public void unExecute()
    {
        m_deleteSelected.unExecute();

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Cut";
    }
}
