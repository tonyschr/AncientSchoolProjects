package stomp.command;

import stomp.*;
import stomp.data3d.*;
import java.util.*;

/**
 * Select all of the vertices or primitives within the region specified
 * by the user using a bounding box.
 */
public class SelectRegionCommand implements Command
{
    private Scene m_scene;
    private FastVector m_vertices;
    private int m_xmin, m_xmax;
    private int m_ymin, m_ymax;
    private boolean m_changed = false;

    private FastVector m_orderedSelectedVerts = new FastVector();
    private FastVector m_orderedSelectedPrims = new FastVector();
    private FastVector m_newlySelected = new FastVector();
    
    private SelectRegionCommand()
    {
    }
    
    public SelectRegionCommand(Scene scene, FastVector vertices, int xmin,
                               int xmax, int ymin, int ymax)
    {
        m_scene = scene;
        m_vertices = vertices;
        m_xmin = xmin;
        m_xmax = xmax;
        m_ymin = ymin;
        m_ymax = ymax;
    }
    
    public boolean execute()
    {
        m_orderedSelectedVerts = m_scene.getOrderedSelectedVertices();
        m_orderedSelectedPrims = m_scene.getOrderedSelectedPrimitives();

        if(m_newlySelected.sizeFast() == 0)
        {
            select();
        }
        else
        {
            for(int i = 0; i < m_newlySelected.sizeFast(); i++)
            {
                if(m_newlySelected.elementAtFast(i) instanceof Vertex)
                {
                    m_scene.select((Vertex)m_newlySelected.elementAtFast(i));
                }
                else
                {
                    m_scene.select((Primitive)m_newlySelected.elementAtFast(i));
                }
            }
        }
        
        m_scene.validateScene();

        return m_changed;
    }

    /**
     * TODO: Is select/unselect exact opposite operations?
     */
    private void select()
    {
        //Get the vector of transformed vertices.
        FastVector transformedVertices = m_scene.getVerticesVector();
        FastVector primitives = m_scene.getPrimitivesVector();

        //Do right thing depending on mode
        if(Mode.getMode() == Mode.REGION_VERTEX_SELECT)
        {
            m_scene.deselectPrimitives();
            
            for(int i = m_vertices.size()-1; i >=0 ; i--)
            {
                Vertex element = (Vertex)m_vertices.elementAtFast(i);
                if(element.selectRegion(m_vertices, m_xmin, m_xmax,
                                        m_ymin, m_ymax))
                {
                    m_scene.select((Vertex)transformedVertices.elementAtFast(i));
                    m_newlySelected.addElement(transformedVertices.elementAtFast(i));
                    m_changed = true;
                }
            }
        }
        else if(Mode.getMode() == Mode.REGION_PRIMITIVE_SELECT)
        {
            m_scene.deselectVertices();
            for(int i = primitives.size()-1; i >=0 ; i--)
            {
                Primitive element = (Primitive)primitives.elementAtFast(i);
                if(element instanceof Polygon3d)
                {
                    ((Polygon3d)element).computeNormal(transformedVertices);
                }
                if(element.selectRegion(m_vertices, m_xmin, m_xmax,
                                        m_ymin, m_ymax))
                {
                    if(element.getGroup() == null ||
                       element instanceof Group)
                    {
                        m_scene.select(element);
                        m_newlySelected.addElement(element);
                        m_changed = true;
                    }
                    //m_scene.select(element);
                    //m_changed = true;
                }
            }
        }
    }
    
    /**
     * 
     */
    public void unExecute()
    {
        m_scene.deselectVertices();
        m_scene.deselectPrimitives();
        
        for(int i = 0; i < m_orderedSelectedVerts.size(); i++)
        {
            m_scene.select((Vertex)m_orderedSelectedVerts.elementAt(i));
        }

        for(int i = 0; i < m_orderedSelectedPrims.size(); i++)
        {
            m_scene.select((Primitive)m_orderedSelectedPrims.elementAt(i));
        }

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Select Region";
    }

}
