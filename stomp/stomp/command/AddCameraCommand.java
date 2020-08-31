package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import stomp.view.*;
import stomp.gui.*;

import java.awt.*;
import java.util.*;

/**
 * Add camera to the scene.  There can only be one camera at a time for
 * now.
 */
public class AddCameraCommand implements Command
{
    private static int m_cameraCount = 0;
    private Scene m_scene;
    private Vertex m_vertex;
    private Vertex m_lookatVertex;
    private CameraView m_cameraView;

    /**
     * No default constructor
     */
    private AddCameraCommand()
    {
    }

    /**
     * Set the camera count to 0.  Only done when performing a "new"
     */
    public static void resetCameraCount()
    {
        m_cameraCount = 0;
    }

    /**
     * Get the number of cameras in the scene.
     */
    public static int numCameras()
    {
        return m_cameraCount;
    }
    
    /**
     * Constructor.
     *
     * @param scene Stomp scene to add camera to
     */
    public AddCameraCommand(Scene scene)
    {
        m_scene = scene;
    }

    public boolean execute()
    {
        if(m_cameraCount++ < 1)
        {
            //Add the vertex for the camera
            m_vertex = new Vertex(0, 0, 5);
            m_scene.addVertex(m_vertex);
            m_lookatVertex = new Vertex(0, 0, 0);
            m_scene.addVertex(m_lookatVertex);
            
            //Get the vertex index
            int vertexIndex[] = new int[2];
            vertexIndex[0] = m_scene.getIndex(m_vertex);
            vertexIndex[1] = m_scene.getIndex(m_lookatVertex);
            
            m_cameraView = new CameraView(m_scene, m_vertex, m_lookatVertex);
            m_cameraView.setIndices(vertexIndex);
            m_scene.addPrimitive(m_cameraView);
        }
        else
        {
            MessageDialog dialog =
                new MessageDialog("Only one camera allowed at a time.");
        }

        m_scene.validateScene();
                
        return true;
    }

    public void unExecute()
    {
        //Remove the vertex
        m_scene.removePrimitive(m_cameraView);
        m_scene.removeVertex(m_vertex);
        m_scene.removeVertex(m_lookatVertex);

        m_cameraCount--;
        
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Add Camera";
    }
}

