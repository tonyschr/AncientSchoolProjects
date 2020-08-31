package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.gui.*;

import java.util.*;

/**
 * Deletes selected primitives or vertices from the Polygon3d.  Delete behavior
 * is as follows:  <p>
 *
 * Vertex:
 * <ul>
 *   <li> if it is not attached to anything, delete it and renumber
 *        primitives' indices
 *   <li> if it is attached to a polygon, delete it and make the
 *        polygon have one fewer vertex.  If the Polygon has < 3
 *        vertices, delete it.
 *   <li> if it is attached to another primitive (spline, surface) then
 *        delete that primitive.
 * </ul>
 */
public class DeleteSelectedCommand implements Command
{
    private boolean m_deleteGroups;
    private Scene m_scene;
    private FastVector m_oldPrimitives;
    private FastVector m_oldVertices;
    private FastVector m_oldUnclonedPrimitives;
    private Hashtable m_clonedHash = new Hashtable();
    private FastVector m_deletedGroups = new FastVector();
    
    private FastVector m_orderedSelectedVerts = new FastVector();
    private FastVector m_orderedSelectedPrims = new FastVector();
    
    private DeleteSelectedCommand()
    {
    }
    
    public DeleteSelectedCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_oldPrimitives = new FastVector();
        m_oldVertices = new FastVector();
        m_orderedSelectedVerts = new FastVector();
        m_orderedSelectedPrims = new FastVector();
        m_clonedHash = new Hashtable();
        m_deletedGroups = new FastVector();
        
        //if(m_scene.numSelected() > 0)
        {
            m_orderedSelectedVerts = (FastVector)m_scene.getOrderedSelectedVertices().clone();
            m_orderedSelectedPrims = (FastVector)m_scene.getOrderedSelectedPrimitives().clone();
            
            m_scene.emptyOrderedSelectedVertices();
            m_scene.emptyOrderedSelectedPrimitives();
            
            FastVector vertices = m_scene.getVerticesVector();
            FastVector primitives = m_scene.getPrimitivesVector();

            //Copy all of the original vertices and primitives for undo.
            //Inefficient, but almost necessary since the data structures
            //can be changed so much.
            m_oldVertices = (FastVector)vertices.clone();
            m_oldUnclonedPrimitives = (FastVector)primitives.clone();
            Primitive element, cloned;
            for(int i = 0; i < primitives.sizeFast(); i++)
            {
                element = (Primitive)primitives.elementAtFast(i);
                //Don't clone groups.
                if(!(element instanceof Group))
                {
                    cloned = (Primitive)element.clone();
                    m_oldPrimitives.addElement(cloned);

                    //Add element to hash table with key of original prim
                    //and object as the cloned one.
                    m_clonedHash.put(element, cloned);
                }
                else if(element.isSelected())
                {
                    m_deleteGroups = true;
                    m_deletedGroups.addElement(element);
                }
                else //group was not selected, just pass it along
                     //so it doesn't get lost on undo.
                {
                    m_oldPrimitives.addElement(element);
                }
            }

            //Go through all selected vertices.  For each vertex, renumber
            //all the primitives.  If the primitive contains the vertex,
            //deal with it using the policy described in the class description.
            Vertex v;
            Primitive p, pClone;
            Stomp.statusBar.startProgress("Deleting, step 1...",
                                          100.0/m_orderedSelectedVerts.size());
            for(int verts = m_orderedSelectedVerts.size()-1; verts >=0; verts--)
            {
                v = (Vertex)m_orderedSelectedVerts.elementAtFast(verts);
                int i = m_scene.getIndex(v);
                if(i > -1)
                {
                    for(int j =  primitives.size()-1; j >=0 ; j--)
                    {
                        p = (Primitive)primitives.elementAtFast(j);
                        
                        if(p.renumberIndices(i))
                        {
                            m_scene.removePrimitive((Primitive)primitives.elementAt(j));
                        }
                    }

                    //m_scene.removeVertex((Vertex)vertices.elementAt(i));
                    vertices.removeElementAt(i);
                }

                Stomp.statusBar.incrementProgress();
            }

            //Loop through primitives and delete if they are selected.
            Stomp.statusBar.startProgress("Deleting, step 2...",
                                          100.0/primitives.size());
            //take care of groups!
            for(int i = primitives.size()-1; i >= 0; i--)
            {
                element = (Primitive)primitives.elementAtFast(i);
                if(element.isSelected() && element instanceof Group)
                {
                    ((Group)element).releasePrimitives();
                    primitives.removeElement(element);
                }
                Stomp.statusBar.incrementProgress();
            }
            
            Stomp.statusBar.startProgress("Deleting, step 3...",
                                          100.0/primitives.size());
            for(int i = primitives.size()-1; i >= 0; i--)
            {
                element = (Primitive)primitives.elementAtFast(i);
                if(element.isSelected())
                {
                    primitives.removeElement(element);
                }
                Stomp.statusBar.incrementProgress();
            }

            m_scene.validateScene();
            return true;
        }

        //        return false;
    }

    public void unExecute()
    {
        //Restore all of the vertices
        FastVector vertices = m_scene.getVerticesVector();
        vertices.removeAllElements();
        for(int i = 0; i < m_oldVertices.size(); i++)
        {
            m_scene.addVertex((Vertex)m_oldVertices.elementAtFast(i));
        }

        //Restore all of the primitives
        FastVector primitives = m_scene.getPrimitivesVector();
        primitives.removeAllElements();
        for(int i = 0; i < m_oldPrimitives.size(); i++)
        {
            Primitive p = (Primitive)m_oldPrimitives.elementAtFast(i);
            for(int g = 0; g < m_deletedGroups.sizeFast(); g++)
            {
                //If primitive references a deleted group, remove that
                //reference.
                if(p.getGroup() == m_deletedGroups.elementAtFast(g))
                {
                    p.setGroup(null);
                }
            }
            int indices[] = p.getIndices();
            for(int q = 0; q < indices.length; q++)
            {
                if(indices[q] == -1)
                {
                    System.out.println("Problem in delete selected's undo.");
                }
            }

            m_scene.addPrimitive(p);
        }

        //Restore the selected vertices and primitives
        for(int i = 0; i < m_orderedSelectedVerts.size(); i++)
        {
            m_scene.select((Vertex)m_orderedSelectedVerts.elementAtFast(i));
        }

        Primitive prim;
        for(int i = 0; i < m_orderedSelectedPrims.size(); i++)
        {
            //Lookup the cloned object based on getting the uncloned one.
            prim = (Primitive)m_clonedHash.get(m_orderedSelectedPrims.elementAtFast(i));
            if(prim != null)
            {
                m_scene.select(prim);
            }
        }

        m_scene.validateScene();
    }
}
