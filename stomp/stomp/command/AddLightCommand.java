package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import stomp.view.*;

import java.awt.*;
import java.util.*;

/**
 * Add a light to the scene.  Currently, the only type of light is
 * POINT_LIGHT.
 */
public class AddLightCommand implements Command
{
    public final static int POINT_LIGHT = 0;
    
    private Scene m_scene;
    private Vertex m_vertex;
    private Light m_light;
    private int m_lightType;
    
    private AddLightCommand()
    {
    }

    /**
     * Constructor.
     *
     * @param scene Stomp scene to add light to
     */
    public AddLightCommand(Scene scene, int lightType)
    {
        m_scene = scene;
        m_lightType = lightType;
    }

    public boolean execute()
    {
        //Add the vertex for the camera
        m_vertex = new Vertex(0, 0, 0);
        m_scene.addVertex(m_vertex);
        
        //Get the vertex index
        int vertexIndex[] = new int[1];
        vertexIndex[0] = m_scene.getIndex(m_vertex);
            
        m_light = new PointLight(vertexIndex);
        m_scene.addPrimitive(m_light);
        
        m_scene.validateScene();
                
        return true;
    }

    public void unExecute()
    {
        //Remove the vertex
        m_scene.removePrimitive(m_light);
        m_scene.removeVertex(m_vertex);

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Add Light";
    }
}

