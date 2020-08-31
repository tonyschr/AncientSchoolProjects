package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import java.awt.*;

import java.util.*;

/**
 * Make a polygon from the selected vertices.
 * TODO: Rename to MakePolygonCommand...
 */
public class MakePlaneCommand implements Command
{
    private Scene m_scene;
    private Polygon3d m_Polygon3d;
    private Command m_deselectAll;
    private FastVector m_orderedSelectedVerts;
    private FastVector m_orderedSelectedPrims;

    private MakePlaneCommand()
    {
    }
    
    public MakePlaneCommand(Scene scene)
    {
        m_scene = scene;
        m_deselectAll = new DeselectAllCommand(scene);
    }
    
    public boolean execute()
    {
        boolean changed = false;
        m_orderedSelectedVerts = m_scene.getOrderedSelectedVertices();
        m_orderedSelectedPrims = m_scene.getOrderedSelectedPrimitives();
        
        m_Polygon3d = m_scene.makePolygon();
        if(m_Polygon3d != null)
        {
            changed = true;
            m_scene.addPrimitive(m_Polygon3d);
        }

        //Deselect all of the vertices and validate the scene.
        m_deselectAll.execute();
        
        m_scene.validateScene();
        return changed;
    }

    /**
     *
     */
    public void unExecute()
    {
        if(m_Polygon3d != null)
        {
            m_scene.removePrimitive(m_Polygon3d);
        }
        m_deselectAll.unExecute();

        for(int i = 0; i < m_orderedSelectedVerts.sizeFast(); i++)
        {
            m_scene.select((Vertex)m_orderedSelectedVerts.elementAtFast(i));
        }

        for(int i = 0; i < m_orderedSelectedPrims.sizeFast(); i++)
        {
            m_scene.select((Primitive)m_orderedSelectedPrims.elementAtFast(i));
        }

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Make Polygon";
    }

}
