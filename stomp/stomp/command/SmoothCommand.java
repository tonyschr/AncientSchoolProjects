package stomp.command;

import stomp.*;
import stomp.data3d.*;
//import stomp.gui.MessageDialog;

import java.util.*;

public class SmoothCommand implements Command
{
    private Scene m_scene;
    private Command m_deselectCommand;
    private Vector m_addedElements;
    private Polygon3d m_oldPolygon3d;

    private SmoothCommand()
    {
    }
    
    public SmoothCommand(Scene scene)
    {
        m_scene = scene;
        m_deselectCommand = new DeselectAllCommand(m_scene);
    }
    
    public boolean execute()
    {
        
        
        return true;
    }

    /**
     * Cannot fully unexecute Smooth for now.
     */
    public void unExecute()
    {
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Smooth Selected";
    }
 }
