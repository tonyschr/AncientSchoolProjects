package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;

import java.awt.*;
import java.util.*;

/**
 * Add vertex to scene.
 */
public class AddVertexCommand implements Command
{
    private Scene m_scene;
    private Vertex m_vertex;
    private Vector m_selectedPrims;

    private AddVertexCommand()
    {
    }

    /**
     * Constructor.
     *
     * @param scene Stomp scene to add vertex to
     * @param v Vertex to add
     */
    public AddVertexCommand(Scene scene, Vertex v)
    {
        m_scene = scene;
        m_vertex = v;
    }

    /**
     * Add the vertex to the scene.
     */
    public boolean execute()
    {
        //Deselect any primitives
        m_selectedPrims = m_scene.getOrderedSelectedPrimitives();
        if(m_selectedPrims.size() > 0)
        {
            m_scene.deselectPrimitives();
        }

        //Add the vertex
        m_scene.addSelectedVertex(m_vertex);
        m_scene.validateScene();

        return true;
    }

    /**
     * Remove the added vertex, restore the selected vertices.
     */
    public void unExecute()
    {
        //Remove the vertex
        m_scene.removeVertex(m_vertex);

        //Re-select any primitives that were deselected.
        for(int i = 0; i < m_selectedPrims.size(); i++)
        {
            m_scene.select((Primitive)m_selectedPrims.elementAt(i));
        }
        
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Add Vertex";
    }
}

