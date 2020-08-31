package stomp.command;

import stomp.*;
import stomp.data3d.*;

/**
 * Copy selected primitives or vertices into the clipboard.
 *
 * The clipboard is internal, not system-wide.  If the user copies
 * primitives, the vertices will be copied with them.  If the user only
 * copies vertices, the primitives will NOT be copied.
 */
public class CopyCommand implements Command
{
    private Scene m_scene;

    private CopyCommand()
    {
    }
    
    public CopyCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_scene.copySelected();

        return false;
    }

    public void unExecute()
    {
    }

    public String toString()
    {
        return "Copy";
    }
}
