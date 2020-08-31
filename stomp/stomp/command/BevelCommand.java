package stomp.command;

import stomp.*;
import stomp.data3d.*;

import java.util.*;
import stomp.transform.*;
import javax.vecmath.*;

/**
 * Command to bevel a selected Polygon.
 *
 * Bevel works by extruding a polygon and then scaling around the center
 * of the polygon.  If the polygon is extremely nonconvex, the center of the
 * The center may be outside of the polygon and the scaling will be slightly
 * incorrect.
 */
public class BevelCommand implements Command
{
    private Scene m_scene;
    private Command m_deselectCommand;
    private FastVector m_addedElements;
    private FastVector m_oldPolygon3ds;

    private BevelCommand()
    {
    }
    
    public BevelCommand(Scene scene)
    {
        m_scene = scene;
        m_deselectCommand = new DeselectAllCommand(m_scene);
    }
    
    public boolean execute()
    {
        boolean changed = false;
        m_addedElements = new FastVector();
        m_oldPolygon3ds = new FastVector();
        FastVector selectedPrimitives = m_scene.getOrderedSelectedPrimitives();
        FastVector transformedVertices = m_scene.getVerticesVector();

        Stomp.statusBar.startProgress("Beveling selected Polygons...",
                                      100.0/selectedPrimitives.size());
        for(int i = 0; i < selectedPrimitives.sizeFast(); i++)
        {
            if(selectedPrimitives.elementAtFast(i) instanceof Polygon3d)
            {
                changed = true;

                //Get the indices to the Polygon3d
                Polygon3d Polygon3d = (Polygon3d)selectedPrimitives.elementAtFast(i);
                Surface surf = Polygon3d.getSurface();
                m_oldPolygon3ds.addElement(Polygon3d);
                
                int indices[] = Polygon3d.getIndices();

                FastVector oldVertices = new FastVector();
                FastVector newVertices = new FastVector();

                Vertex[] normalPoints = Polygon3d.getNormalPoints();

                //Scaling about the normal point causes a bevel
                Scale scale = new Scale();
                scale.setScale(normalPoints[1].x,
                               normalPoints[1].y,
                               normalPoints[1].z,
                               -.10f, -.10f, -.10f);

                //Copy vertices for the Polygon3d into two different vectors.
                for(int j = 0; j < indices.length; j++)
                {
                    Vertex v = (Vertex)transformedVertices.elementAtFast(indices[j]);
                    Vertex v2 = (Vertex)v.clone();
                    m_addedElements.addElement(v2);

                    scale.transformVertex(v2, v2);
                    
                    oldVertices.addElement(v);
                    newVertices.addElement(v2);                    
                }

                //Add vertices for the extruded part.
                int newIndices[] = new int[newVertices.size()];
                for(int j = 0; j < newVertices.size(); j++)
                {
                    m_scene.addVertex((Vertex)newVertices.elementAtFast(j));
                    
                    indices[j] = m_scene.getIndex((Vertex)oldVertices.elementAtFast(j));
                    newIndices[j] = m_scene.getIndex((Vertex)newVertices.elementAtFast(j));
                }

                //Add new Polygon3ds to form the sides of the extrusion.
                for(int j = 0; j < indices.length; j++)
                {
                    int j2 = (j + 1)%indices.length;
                    Polygon3d p = new Polygon3d(indices[j], indices[j2],
                                        newIndices[j2], newIndices[j]);
                    p.setSurface(surf);
                    
                    m_scene.addPrimitive(p);
                    m_addedElements.addElement(p);
                }

                //Add a Polygon3d for the top and flip it to face the
                //right direction.
                Polygon3d newTop = new Polygon3d(newIndices);
                newTop.setSurface(surf);
                Polygon3d.flip();                
                
                m_scene.addPrimitive(newTop);
                m_addedElements.addElement(newTop);
            }

            Stomp.statusBar.incrementProgress();
        }
        
        m_deselectCommand.execute();
        
        m_scene.validateScene();

        return changed;
    }

    public void unExecute()
    {
        //First, delete all the new Polygon3ds
        for(int i = 0; i < m_addedElements.sizeFast(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Polygon3d)
            {
                m_scene.removePrimitive((Primitive)m_addedElements.elementAtFast(i));
            }
        }

        //Then, it's safe to kill all of the vertices.
        for(int i = 0; i < m_addedElements.sizeFast(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Vertex)
            {
                m_scene.removeVertex((Vertex)m_addedElements.elementAtFast(i));
            }
        }

        //        m_scene.addPrimitive(m_oldPolygon3d);

        for(int i = 0; i < m_oldPolygon3ds.sizeFast(); i++)
        {
            ((Polygon3d)m_oldPolygon3ds.elementAtFast(i)).flip();
        }

        m_deselectCommand.unExecute();
        
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Extrude Selected";
    }

}
