package stomp.command;

import stomp.*;
import stomp.data3d.*;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;

/**
 * Convert all of the polygons in the scene to triangles.
 * Does not yet handle nonconvex polygons, but it will someday...
 */
public class TriangulateCommand implements Command
{
    private Scene m_scene;
    private FastVector m_originalPolygon3ds;
    private FastVector m_addedElements;
    private boolean m_changed = false;

    private TriangulateCommand()
    {
    }
    
    public TriangulateCommand(Scene scene)
    {
        m_scene = scene;
        
    }
    
    public boolean execute()
    {
        m_addedElements = new FastVector();
        m_originalPolygon3ds = new FastVector();

        FastVector primitives = m_scene.getPrimitivesVector();

        //Store original Polygon3ds for Undo
        for(int i = 0; i < primitives.size(); i++)
        {
            Primitive prim = (Primitive)primitives.elementAtFast(i);
            if(prim.isSelected() && prim instanceof Polygon3d)
            {
                m_originalPolygon3ds.addElement(prim);
            }
        }
        
        triangulate();

        m_scene.validateScene();
        
        return m_changed;
    }

    public void triangulate()
    {
        FastVector primitives = m_scene.getPrimitivesVector();
        FastVector transformedVertices = m_scene.getVerticesVector();
        
        //For each primitive...
        Stomp.statusBar.startProgress("Triangulating polygons...",
                                      100.0/primitives.size());
        for(int i = primitives.size()-1; i >=0; i--)
        {
            Primitive prim = (Primitive)primitives.elementAtFast(i);
            if(prim.isSelected() && prim instanceof Polygon3d)
            {
                m_scene.deselect(prim);
                Polygon3d polygon3d = (Polygon3d)prim;
                Surface surf = polygon3d.getSurface();
                Group group = polygon3d.getGroup();
                
                //Get the ordered indices
                int indList[] = polygon3d.getIndices();

                //Skip polygons that are already triangles.
                if(indList.length <= 3)
                {
                    continue;
                }

                //m_scene.removePrimitive((Primitive)primitives.elementAt(i));
                
                //Add from array to vector.
                FastVector indices = new FastVector();
                for(int j = 0; j < indList.length; j++)
                {
                    indices.addElement(new Integer(indList[j]));
                }
                
                //Test each vertex to see if they are convex.  If a vertex is
                //concave, split the polygon3d in two and recurse.  CONCAVE
                //PART NOT YET IMPLEMENTED!
                Vector3f polygon3dNormal = polygon3d.getNormal();
                                                                     
                int test1 = indList[indList.length-1];
                for(int j = 0; j < indList.length; j++)
                {
                    int test2 = indList[j];
                    int test3 = indList[(j+1)%indList.length];

                    if(!convex((Vertex)transformedVertices.elementAtFast(test1),
                               (Vertex)transformedVertices.elementAtFast(test2),
                               (Vertex)transformedVertices.elementAtFast(test3),
                               polygon3dNormal))
                    {
                        //TODO: Somehow split this into two nonconvex polygon3ds.
                        //(note: test2 is the concave point)
                        int v1 = ((Integer)indices.elementAtFast(0)).intValue();
                        int v2 = ((Integer)indices.elementAtFast(1)).intValue();
                        int v3 = ((Integer)indices.elementAtFast(2)).intValue();
                        
//                          Polygon3d p = new Polygon3d(v1, v2, v3);
//                          p.setSurface(surf);
//                          p.setGroup(group);
//                          m_addedElements.addElement(p);
//                          m_changed = true;
//                          m_scene.addPrimitive(p);
//                          indices.removeElementAt(1);
                    }
                        
                    
                }
                
//                  //Do ear-cutting method of convex polygon3d triangulation.
//                  while(indices.size() > 2)
//                  {
//                      int v1 = ((Integer)indices.elementAtFast(0)).intValue();
//                      int v2 = ((Integer)indices.elementAtFast(1)).intValue();
//                      int v3 = ((Integer)indices.elementAtFast(2)).intValue();

//                      {
//                          Polygon3d p = new Polygon3d(v1, v2, v3);
//                          p.setSurface(surf);
//                          p.setGroup(group);
//                          m_addedElements.addElement(p);
//                          m_changed = true;
//                          m_scene.addPrimitive(p);
//                          indices.removeElementAt(1);
//                      }
//                  }
            }

            Stomp.statusBar.incrementProgress();
        }
    }

    public void unExecute()
    {
        //Remove all of the new Polygon3ds that were added
        for(int i = 0; i < m_addedElements.size(); i++)
        {
            m_scene.removePrimitive((Primitive)m_addedElements.elementAtFast(i));
        }

        //Add back the original Polygon3ds.  They should still be indexing the
        //original vertices.
        for(int i = 0; i < m_originalPolygon3ds.size(); i++)
        {
            m_scene.addPrimitive((Primitive)m_originalPolygon3ds.elementAtFast(i));
            m_scene.select((Primitive)m_originalPolygon3ds.elementAtFast(i));
        }

        m_scene.validateScene();
    }

    /**
     * Determine whether a vertex is convex.
     *
     * @param v1 First vertex
     * @param v2 Middle vertex -- the one we are testing
     * @param v2 Third vertex
     * @param Polygon3dNormal normal to the Polygon3d
     */
    private boolean convex(Vertex v1, Vertex v2, Vertex v3,
                           Vector3f polygon3dNormal)
    {
        //Check to see if the normal of this vertex faces the
        //same way was the normal to the Polygon3d.  If it does,
        //this vertex is convex.  If not, it's concave.
        float areaSum = 0;

        Vector3f nvec1 = new Vector3f();
        Vector3f nvec2 = new Vector3f();
        nvec1.sub(v1, v2);
        nvec2.sub(v3, v2);
        
        Vector3f normal = new Vector3f();
        normal.cross(nvec1, nvec2);
        normal.scale(1/normal.length());

        normal.sub(polygon3dNormal);
        
        return normal.length() < .001; //epsilon ==
    }

    public String toString()
    {
        return "Triangulate";
    }
}
