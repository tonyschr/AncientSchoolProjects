package stomp.command;

import stomp.*;
import stomp.data3d.*;

/**
 * Erase the contents of the current scene and return views to their default
 * settings.
 */
public class NewCommand implements Command
{
    private Stomp m_stomp;
    private Scene m_scene;

    private NewCommand()
    {
    }
    
    public NewCommand(Stomp stomp, Scene scene)
    {
        m_stomp = stomp;
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_scene.removeAllContents();
        AddCameraCommand.resetCameraCount();
        CommandExecutor.clear();
        //System.gc();
        m_scene.validateScene();
        //m_stomp.initializeStomp(new Scene());
        return false;
    }

    /**
     * Cannot unexecute new for now.
     */
    public void unExecute()
    {
    }

    public String toString()
    {
        return "New Scene";
    }

}
