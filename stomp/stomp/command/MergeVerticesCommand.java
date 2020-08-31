package stomp.command;

import stomp.*;
import stomp.data3d.*;

import java.util.*;
import javax.vecmath.*;

/**
 * Merge all overlapping vertices into one vertex.  This reduces the number
 * of vertices in the scene.
 */
public class MergeVerticesCommand implements Command
{
    private Scene m_scene;
    private FastVector m_oldPrimitives = new FastVector();
    private FastVector m_newPrimitives = new FastVector();
    
    private Command m_deselectAllCommand;
    private Command m_deleteSelectedCommand;

    private MergeVerticesCommand()
    {
    }
    
    public MergeVerticesCommand(Scene scene)
    {
        m_scene = scene;
        m_deselectAllCommand = new DeselectAllCommand(m_scene);
        m_deleteSelectedCommand = new DeleteSelectedCommand(m_scene);
    }
    
    public boolean execute()
    {
        boolean changed = true; //true by default
        int count = 0;
        Vector3f dvec = new Vector3f();
        Vector toRemove = new FastVector();
        m_oldPrimitives = new FastVector();
        m_newPrimitives = new FastVector();
        //m_vertices = new Vector();

        m_deselectAllCommand.execute();

        FastVector vertices = m_scene.getVerticesVector();
        FastVector primitives = m_scene.getPrimitivesVector();
        
        //Loop through all pairs of vertices, if two are the same,
        //loop through primitives and replace reference to duplicate vertex
        //with other vertex.
        //
        // Slow!
        Stomp.statusBar.startProgress("Merging duplicate vertices...",
                                      100.0/vertices.sizeFast());
        for(int i = 0; i < vertices.sizeFast(); i++)
        {
            Vertex v1 = (Vertex)vertices.elementAtFast(i);
            for(int j = i + 1; j < vertices.sizeFast(); j++)
            {
                Vertex v2 = (Vertex)vertices.elementAtFast(j);

                if(v1.epsilonEquals(v2, .00001f))
                {
                    //Go through all primitives and replace indices into
                    //that vertex with indices into the older vertex
                    //at the same coordinate.
                    for(int p = primitives.sizeFast()-1; p >=0; p--)
                    {
                        Primitive prim = (Primitive)primitives.elementAtFast(p);
                        if(prim.containsIndex(j))
                        {
                            //If the primitive is touched, save it as newly
                            //changed and save the clone as the old version.
                            if(!m_newPrimitives.contains(prim))
                            {
                                m_newPrimitives.addElement(prim);
                                m_oldPrimitives.addElement(prim.clone());
                            }
                            prim.replaceIndex(j, i);
                        }
                    }
                    count++;
                    m_scene.select(v2);
                    //                    v2.setSelected(true);
                    //m_vertices.addElement(v2);
                }
            }

            Stomp.statusBar.incrementProgress();
        }
            
        //Duplicates were marked by selecting them, so delete them.
        m_deleteSelectedCommand.execute();

        m_deselectAllCommand.unExecute();

        m_scene.validateScene();
        return changed;
    }

    public void unExecute()
    {
        //undo the deleted vertices
        m_deleteSelectedCommand.unExecute();

        //restore the planes

        //delete the new planes (all that were touched
        for(int i = 0; i < m_newPrimitives.size(); i++)
        {
            m_scene.removePrimitive((Primitive)m_newPrimitives.elementAtFast(i));
        }

        //Add back the old planes (indexing into the old vertices
        for(int i = 0; i < m_newPrimitives.size(); i++)
        {
            m_scene.addPrimitive((Primitive)m_oldPrimitives.elementAtFast(i));
        }
        
        m_scene.validateScene();
    }
}
