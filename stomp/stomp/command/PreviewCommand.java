package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;

/**
 * Preview the scene using the VRML viewer pointed to in the
 * Options dialog box.  Automatically saves to a .wrl and
 * invokes the VRML viewer on the scene.
 */
public class PreviewCommand implements Command
{
    private Stomp m_stomp;
    private Scene m_scene;

    private PreviewCommand()
    {
    }
    
    public PreviewCommand(Stomp stomp, Scene scene)
    {
        m_stomp = stomp;
        m_scene = scene;
    }
    
    public boolean execute()
    {
        try
        {
            SceneReaderWriter rw = new VRMLReaderWriter();
            rw.write(m_scene, Mode.getPreviewPath());
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("" + Mode.getPreviewProg() + " " +
                         Mode.getPreviewPath());
        }
        catch(java.io.IOException e)
        {
        }
        
        return false;
    }

    /**
     * Cannot unexecute preview!
     */
    public void unExecute()
    {
    }

    public String toString()
    {
        return "Preview";
    }

}
