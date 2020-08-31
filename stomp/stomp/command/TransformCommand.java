package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import stomp.transform.*;
//import stomp.anim.*;

import javax.vecmath.*;
import java.awt.*;

/**
 * Transform the vertices or primitives in the scene using the
 * supplied transformation matrix.
 */
public class TransformCommand implements Command
{
    private Scene m_scene;
    private Transformation m_trans;

    private TransformCommand()
    {
    }
    
    public TransformCommand(Scene scene, Transformation trans)
    {
        m_scene = scene;
        m_trans = trans;
    }

    public boolean execute()
    {
        m_scene.transformSelected(m_trans);
        m_scene.validateScene();

        return true;
    }

    public void unExecute()
    {
        m_scene.transformSelected(m_trans.inverse());
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Transform";
    }
}
