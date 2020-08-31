package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import java.awt.*;

import java.util.*;

/**
 * Make a spline surface from at least four selected splines.
 */
public class MakeSplineSurfaceCommand implements Command
{
    private Scene m_scene;
    private SplineSurface m_splineSurf;
    private Command m_deselectAll;
    private Vector m_orderedSelectedVerts;
    private Vector m_orderedSelectedPrims;

    private MakeSplineSurfaceCommand()
    {
    }
    
    public MakeSplineSurfaceCommand(Scene scene)
    {
        m_scene = scene;
        m_deselectAll = new DeselectAllCommand(scene);
    }
    
    public boolean execute()
    {
        boolean changed = false;
        m_orderedSelectedVerts = m_scene.getOrderedSelectedVertices();
        m_orderedSelectedPrims = m_scene.getOrderedSelectedPrimitives();
        
        m_splineSurf = m_scene.makeSplineSurface();
        if(m_splineSurf != null)
        {
            changed = true;
            m_scene.addPrimitive(m_splineSurf);
        }

        //Deselect all of the vertices and validate the scene.
        m_deselectAll.execute();
        m_scene.validateScene();

        return changed;
    }

    /**
     * Cannot unexecute new for now.
     */
    public void unExecute()
    {
        if(m_splineSurf != null)
        {
            m_scene.removePrimitive(m_splineSurf);
        }
        
        m_deselectAll.unExecute();

        for(int i = 0; i < m_orderedSelectedVerts.size(); i++)
        {
            m_scene.select((Vertex)m_orderedSelectedVerts.elementAt(i));
        }        

        for(int i = 0; i < m_orderedSelectedPrims.size(); i++)
        {
            m_scene.select((Primitive)m_orderedSelectedPrims.elementAt(i));
        }        

        m_deselectAll.execute();
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Make Spline Surface";
    }
}
