package stomp.command;

import stomp.*;
import stomp.data3d.*;

import java.util.*;
import javax.vecmath.*;

/**
 * Join the selected vertices into the first vertex.  Allows loops to be
 * created
 */
public class JoinVerticesCommand implements Command
{
    private Scene m_scene;
    private Vector m_oldPrimitives = new Vector();
    private Vector m_newPrimitives = new Vector();
    private Vector m_vertices = new Vector();
    
    private Command m_deselectAllCommand;
    private Command m_deleteSelectedCommand;

    private JoinVerticesCommand()
    {
    }
    
    public JoinVerticesCommand(Scene scene)
    {
        m_scene = scene;
        m_deselectAllCommand = new DeselectAllCommand(m_scene);
        m_deleteSelectedCommand = new DeleteSelectedCommand(m_scene);
    }
    
    public boolean execute()
    {
        boolean changed = false;
        int count = 0;
        Vector3f dvec = new Vector3f();
        Vector toRemove = new Vector();
        m_oldPrimitives = new Vector();
        m_newPrimitives = new Vector();
        m_vertices = new Vector();

        Vector vertices = m_scene.getVerticesVector();
        Vector primitives = m_scene.getPrimitivesVector();
        Vector selectedVertices = m_scene.getSelectedVertices();

        if(selectedVertices.size() < 2)
        {
            return changed;
        }

        Vertex keepVertex = (Vertex)selectedVertices.elementAt(0);
        int keepIndex = vertices.indexOf(keepVertex);
        m_scene.deselect(keepVertex);
        
        selectedVertices = m_scene.getSelectedVertices();
        //Loop through all selected vertices and merge them into the first.
        for(int i = 0; i < selectedVertices.size(); i++)
        {
            Vertex v1 = (Vertex)selectedVertices.elementAt(i);
            int oldIndex = m_scene.getIndex(v1);

            //Go through all primitives and replace indices into
            //that vertex with indices into the older vertex
            //at the same coordinate.
            for(int p = primitives.size()-1; p >=0; p--)
            {
                Primitive prim = (Primitive)primitives.elementAt(p);
                if(prim.containsIndex(oldIndex))
                {
                    changed = true;
                    //If the primitive is touched, save it as newly
                    //changed and save the clone as the old version.
                    if(!m_newPrimitives.contains(prim))
                    {
                        m_newPrimitives.addElement(prim);
                        m_oldPrimitives.addElement(prim.clone());
                    }
                    prim.replaceIndex(oldIndex, keepIndex);
                }
            }

            m_vertices.addElement(v1);
        }

        m_deleteSelectedCommand.execute();

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
            m_scene.removePrimitive((Primitive)m_newPrimitives.elementAt(i));
        }

        //Add back the old planes (indexing into the old vertices
        for(int i = 0; i < m_newPrimitives.size(); i++)
        {
            m_scene.addPrimitive((Primitive)m_oldPrimitives.elementAt(i));
        }
        
        m_scene.validateScene();
    }
}
