package stomp.command;

import stomp.*;
import stomp.data3d.*;
import java.util.*;

/**
 * Select all of the primitives or vertices that are somehow attached
 * to the selected one by searching through the mesh.  Currently very
 * slow!
 */
public class SelectConnectedCommand implements Command
{
    private Scene m_scene;
    private FastVector m_vertices;
    private FastVector m_primitives;
    private FastVector m_selectedPrimitives;
    private FastVector m_selectedVertices;

    private FastVector m_newlySelected;
    private boolean m_changed = false;

    private SelectConnectedCommand()
    {
    }
    
    public SelectConnectedCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_vertices = m_scene.getVerticesVector();
        m_primitives = m_scene.getPrimitivesVector();
        m_selectedVertices = m_scene.getSelectedVertices();
        m_selectedPrimitives = m_scene.getSelectedPrimitives();

        m_newlySelected = new FastVector();
        
        if(m_scene.verticesSelected())
        {
            selectConnectedVertices();
        }
        else if(m_scene.primitivesSelected())
        {
            selectConnectedPrimitives();
        }
        
        m_scene.validateScene();

        return m_changed;
    }

    private void selectConnectedVertices()
    {
        FastVector attachedPolygon3ds = new FastVector();

        boolean found = true;
        
        while(found)
        {    
            //For each selected vertex, get the attached Polygon3ds.
            int vIndex;
            Primitive p;
            for(int i = 0; i < m_selectedVertices.sizeFast(); i++)
            {
                vIndex = m_scene.getIndex((Vertex)m_selectedVertices.elementAtFast(i));
                for(int j = 0; j < m_primitives.sizeFast(); j++)
                {
                    p = (Primitive)m_primitives.elementAtFast(j);
                    if(p.containsIndex(vIndex))
                    {
                        if(!attachedPolygon3ds.contains(p))
                        {
                            attachedPolygon3ds.addElement(p);
                        }
                    }
                }
            }
            
            found = false;
            
            //Now, for all the attached Polygon3ds, get the indices and select
            //them.
            int indices[];
            Vertex v;
            for(int i = 0; i < attachedPolygon3ds.size(); i++)
            {
                p = (Primitive)attachedPolygon3ds.elementAtFast(i);
                indices = p.getIndices();
                
                for(int index = 0; index < indices.length; index++)
                {
                    v = (Vertex)m_vertices.elementAtFast(indices[index]);
                    if(!m_selectedVertices.contains(v))
                    {
                        m_scene.select(v);
                        m_changed = true;
                        m_newlySelected.addElement(v);
                        m_selectedVertices.addElement(v);
                        found = true;
                    }
                }       
            }
        }
    }

    private void selectConnectedPrimitives()
    {
        FastVector attachedPolygon3ds = new FastVector();

        boolean found = true;

        for(int sp = 0; sp < m_selectedPrimitives.size(); sp++)
        {
            Primitive selP = (Primitive)m_selectedPrimitives.elementAtFast(sp);
            int indices[] = selP.getIndices();

            for(int i = 0; i < indices.length; i++)
            {
                m_selectedVertices.addElement((Vertex)m_vertices.elementAtFast(indices[i]));
            }
        }

        while(found)
        {    
            //For each selected vertex, get the attached Polygon3ds.
            for(int i = 0; i < m_selectedVertices.sizeFast(); i++)
            {
                int vIndex = m_scene.getIndex((Vertex)m_selectedVertices.elementAtFast(i));
                for(int j = 0; j < m_primitives.sizeFast(); j++)
                {
                    Primitive p = (Primitive)m_primitives.elementAtFast(j);
                    if(p.containsIndex(vIndex))
                    {
                        if(!attachedPolygon3ds.contains(p))
                        {
                            attachedPolygon3ds.addElement(p);
                        }
                    }
                }
            }
            
            found = false;
            
            //Now, for all the attached Polygon3ds, get the indices and select
            //them.
            for(int i = 0; i < attachedPolygon3ds.sizeFast(); i++)
            {
                Primitive p = (Primitive)attachedPolygon3ds.elementAtFast(i);
                int indices[] = p.getIndices();
                
                for(int index = 0; index < indices.length; index++)
                {
                    Vertex v = (Vertex)m_vertices.elementAtFast(indices[index]);
                    if(!m_selectedVertices.contains(v))
                    {
                        m_changed = true;
                        m_newlySelected.addElement(v);
                        m_selectedVertices.addElement(v);
                        found = true;
                    }
                }       
            }
        }

        //For the selected vertices, get the attached Polygon3ds.
        for(int i = 0; i < m_selectedVertices.sizeFast(); i++)
        {
            Vertex v = (Vertex)m_selectedVertices.elementAtFast(i);
            int vIndex = m_scene.getIndex(v);
            for(int j = 0; j < m_primitives.size(); j++)
            {
                Primitive p = (Primitive)m_primitives.elementAtFast(j);
                if(p.containsIndex(vIndex))
                {
                    if(!m_selectedPrimitives.contains(p))
                    {
                        m_scene.select(p);
                        m_selectedPrimitives.addElement(p);
                    }
                }
            }
        }   
    }
        
    /**
     * Cannot unexecute Select for now.
     */
    public void unExecute()
    {
        for(int i = 0; i < m_newlySelected.size(); i++)
        {
            Object element = m_newlySelected.elementAtFast(i);
            if(element instanceof Primitive)
            {
                m_scene.deselect((Primitive)element);
            }
            else if(element instanceof Vertex)
            {
                m_scene.deselect((Vertex)element);
            }
        }
        
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Select Connected";
    }

}
