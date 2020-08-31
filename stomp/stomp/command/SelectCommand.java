package stomp.command;

import stomp.*;
import stomp.data3d.*;
import java.util.*;

/**
 * Select a primitive or vertex (depending on the mode) in the
 * scene.
 */
public class SelectCommand implements Command
{
    private Scene m_scene;
    private FastVector m_vertices;
    private int m_x, m_y;
    private boolean m_changed = false;
    private boolean m_noDeselect = false;

    private FastVector m_orderedSelectedVerts = new FastVector();
    private FastVector m_orderedSelectedPrims = new FastVector();

    private FastVector m_newlySelected = new FastVector();

    private SelectCommand()
    {
    }
    
    public SelectCommand(Scene scene, FastVector vertices, int x, int y)
    {
        m_scene = scene;
        m_vertices = vertices;
        m_x = x;
        m_y = y;
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
        if(Mode.getMode() == Mode.VERTEX_SELECT)
        {
            //Deselect all the primitives, we aren't in that mode anymore.
            m_scene.deselectPrimitives();

            //Assume we are selecting rather than deselecting.
            boolean sel = true;

            //Check all of the vertices within our range.  If any are
            //selected, we're now in deselect mode.
            Vertex element;
            if(m_scene.verticesSelected())
            {
                for(int i = m_vertices.size()-1; i >=0; i--)
                {
                    element = (Vertex)m_vertices.elementAtFast(i);
                    if(element.isSelected() &&
                       element.select(m_vertices, m_x, m_y))
                    {
                        sel = false;
                        break;
                    }
                }
            }
            
            //Select/deselect vertices depending on whether we are
            //in select or deselect mode.
            for(int i = m_vertices.size()-1; i >=0; i--)
            {
                element = (Vertex)m_vertices.elementAtFast(i);
                
                if(element.select(m_vertices, m_x, m_y))
                {
                    if(sel)
                    {
                        m_scene.select((Vertex)transformedVertices.elementAtFast(i));
                        m_newlySelected.addElement(transformedVertices.elementAtFast(i));
                        m_changed = true;
                    }
                    else
                    {
                        m_scene.deselect((Vertex)transformedVertices.elementAtFast(i));
                        m_changed = true;
                    }
                }
            }
        }
        else if(Mode.getMode() == Mode.PRIMITIVE_SELECT)
        {
            //Assume we are in select mode.
            boolean sel = true;

            m_scene.deselectVertices();

            //Check all faces we are clicking within. If any are selected,
            //go into deselect mode.
            Primitive element;
            if(m_scene.primitivesSelected())
            {
                for(int i = primitives.size()-1; i >= 0; i--)
                {
                    element = (Primitive)primitives.elementAtFast(i);
                    if(element.isSelected() &&
                       element.select(m_vertices, m_x, m_y))
                    {
                        sel = false;
                        break;
                    }
                }
            }

            //Loop through all primitives.  If clicking on one,
            //select or deselect it depending on the mode.
            for(int i = primitives.size()-1; i >= 0; i--)
            {
                element = (Primitive)primitives.elementAtFast(i);
                //Make sure that the normal to the plane has been computed
                //Before we select a plane (and reveal its normal!)
//                 if(element instanceof Plane)
//                 {
//                     ((Plane)element).computeNormal(transformedVertices);
//                 }
                if(element.select(m_vertices, m_x, m_y))
                {
                    if(sel)
                    {
                        if(element.getGroup() == null ||
                           element instanceof Group)
                        {
                            m_scene.select(element);
                            m_newlySelected.addElement(element);
                            m_changed = true;
                        }
                    }
                    else
                    {
                        m_scene.deselect(element);
                        m_changed = true;
                    }
                }
            }
        }
    }
    
    /**
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
        return "Select";
    }

}
