package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.gui.*;
import stomp.transform.*;

import java.util.*;
import javax.vecmath.*;

/**
 * Extrudes a plane along the path of a spline.
 */
public class SplineExtrudeCommand implements Command
{
    private Scene m_scene;
    private Vector m_addedElements;
    private Polygon3d m_oldPolygon3d;

    private SplineExtrudeCommand()
    {
    }
    
    public SplineExtrudeCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_addedElements = new Vector();
        boolean rotate = true;
        Vector selectedPrimitives = m_scene.getSelectedPrimitives();
        Polygon3d polygon3d = null;
        Spline spline = null;

        if(selectedPrimitives.size() != 2)
        {
            MessageDialog dialog = new MessageDialog("Error: Must select one Polygon3d and one Spline.");
            dialog.setVisible(true);
            return false;
        }
        
        for(int i = 0; i < 2; i++)
        {
            if(selectedPrimitives.elementAt(i) instanceof Polygon3d)
            {
                polygon3d = (Polygon3d)selectedPrimitives.elementAt(i);
            }
            else if(selectedPrimitives.elementAt(i) instanceof Spline)
            {
                spline = (Spline)selectedPrimitives.elementAt(i);
            }
        }

        if(polygon3d == null || spline == null)
        {
            MessageDialog dialog = new MessageDialog("Error: Must select one Polygon3d and one Spline.");
            dialog.setVisible(true);
            return false;
        }

        Surface surf = polygon3d.getSurface();
        FastVector vertices = m_scene.getVerticesVector();
        Vector splineVertices =
            spline.getVerticesAlongPath(0.25/(float)Mode.NURB_SUBDIVIDE,
                                        vertices);

        polygon3d.flip();
        polygon3d.computeNormal(vertices);
        
        //Get the Polygon3d's vertices and make a copy.  The vertices are what
        //we will extrude along the spline.
        int polygon3dIndices[] = polygon3d.getIndices();
        Vertex placeVertices[][] = new Vertex[splineVertices.size()][polygon3dIndices.length];
        int placeIndices[][] = new int[splineVertices.size()][polygon3dIndices.length];
        for(int i = 0; i < polygon3dIndices.length; i++)
        {
            //PlaceVertices[0] contains vertices to be copied.
            placeVertices[0][i] = (Vertex)vertices.elementAt(polygon3dIndices[i]);
            placeIndices[0][i] = polygon3dIndices[i];
        }    

        
        for(int i = 1; i < splineVertices.size(); i++)
        {
            // i - (i-1)
            Vertex vertex0 = (Vertex)splineVertices.elementAt(i-1);
            Vertex vertex1 = (Vertex)splineVertices.elementAt(i);
            Vector3f moveAmount = new Vector3f();
            moveAmount.sub(vertex1, vertex0);
            
            //Add the offset to form rings of new vertices along the path.
            for(int j = 0; j < polygon3dIndices.length; j++)
            {
                Vertex v = (Vertex)placeVertices[i-1][j].clone();
                v.add(moveAmount);

                placeVertices[i][j] = v;
                m_scene.addVertex(placeVertices[i][j]);
                m_addedElements.addElement(placeVertices[i][j]);
                placeIndices[i][j] = m_scene.getIndex(placeVertices[i][j]);
            }
        }
        
        if(rotate)
        {
            Rotation rotationX = new Rotation();
            Rotation rotationY = new Rotation();
            Rotation rotationZ = new Rotation();

            float angleX, angleY, angleZ;

            for(int i = 1; i < splineVertices.size(); i++)
            {
                Vertex vertex0 = (Vertex)splineVertices.elementAt(i-1);
                Vertex vertex1 = (Vertex)splineVertices.elementAt(i);
                
                Vector3f v2 = new Vector3f();
                v2.sub(vertex1, vertex0);

                //Recalculate indices, they may have changed.
                for(int j = 0; j < polygon3dIndices.length; j++)
                {
                    placeIndices[i][j] =
                        m_scene.getIndex(placeVertices[i][j]);
                }
                
                Polygon3d temp = new Polygon3d(placeIndices[i]);
                temp.setSurface(surf);
                temp.computeNormal(vertices);
                Vector3f v1 = temp.getNormal();
                Vertex center = temp.getCenter(vertices);
                v1.normalize();
                v2.normalize();
                
                //v1 is normal, v2 is path vector, we want to align them.
                Vector3f axisVector = new Vector3f();
                axisVector.cross(v1, v2);
                axisVector.normalize();

                float angle = v1.angle(v2);

                Rotation rotation = new Rotation();
                rotation.setRotation(center.x, center.y, center.z,
                                     axisVector.x, axisVector.y, axisVector.z,
                                     angle);

                for(int j = 0; j < polygon3dIndices.length; j++)
                {
                    Vertex v = placeVertices[i][j];
                    Vertex oldv = (Vertex)v.clone();
                    rotation.transformVertex(oldv, v);
                }
            }
            //Polygon3d.flip();
            //Polygon3d.computeNormal(vertices);
            m_oldPolygon3d = polygon3d;
        }

        //Create Polygon3ds around the outsides of the cylinder.
        for(int s = 0; s < splineVertices.size()-1; s++)
        {
            for(int i = 0; i < polygon3dIndices.length; i++)
            {
                placeIndices[s][i] = m_scene.getIndex(placeVertices[s][i]);

                int ind[] = new int[4];
                
                ind[3] = placeIndices[s][i];
                ind[2] = placeIndices[s][(i+1)%polygon3dIndices.length];
                ind[1] = placeIndices[s+1][(i+1)%polygon3dIndices.length];
                ind[0] = placeIndices[s+1][i];
                
                Polygon3d side = new Polygon3d(ind);
                side.setSurface(surf);
                m_scene.addPrimitive(side);
                m_addedElements.addElement(side);
                side.flip();
                side.computeNormal(vertices);
            }
        }

        Polygon3d end = new Polygon3d(placeIndices[splineVertices.size()-1]);
        end.setSurface(surf);
        m_scene.addPrimitive(end);
        m_addedElements.addElement(end);

        polygon3d.flip();
        polygon3d.computeNormal(vertices);

        if(!rotate)
        {
            end.flip();
            end.computeNormal(vertices);
        }
        
        m_scene.validateScene();
        return true;
    }

    /**
     */
    public void unExecute()
    {
        //First, delete all the new Polygon3ds
        for(int i = 0; i < m_addedElements.size(); i++)
        {
            if(m_addedElements.elementAt(i) instanceof Primitive)
            {
                m_scene.removePrimitive((Primitive)m_addedElements.elementAt(i));
            }
        }

        //Then, it's safe to kill all of the vertices.
        for(int i = 0; i < m_addedElements.size(); i++)
        {
            if(m_addedElements.elementAt(i) instanceof Vertex)
            {
                m_scene.removeVertex((Vertex)m_addedElements.elementAt(i));
            }
        }

//         if(m_oldPolygon3d != null)
//         {
//             m_oldPolygon3d.flip();
//             Vector vertices = m_scene.getVerticesVector();
//             m_oldPolygon3d.computeNormal(vertices);
//         }
        
        m_scene.validateScene();
    }
    
    public String toString()
    {
        return "Spline Extrude";
    }

}
