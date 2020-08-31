package stomp.command;

import stomp.*;
import stomp.data3d.*;

import java.util.*;

/**
 * This command has several functions:
 * <ul>
 *   <li> Subdivide quads or triangles into 3 or 4 smaller ones.
 *   <li> Convert a spline to a series of points
 *   <li> Convert a spline surface into polygons.
 * </ul>
 */
public class SubdivideCommand implements Command
{
    public static final int CENTER = 1;
    public static final int NOCENTER = 2;
    
    private Scene m_scene;
    private int m_method;
    private Command m_mergeVerticesCommand;

    private FastVector m_addedElements;
    private FastVector m_removedElements;

    private SubdivideCommand()
    {
    }
    
    public SubdivideCommand(Scene scene, int method)
    {
        m_scene = scene;
        m_method = method;
        m_mergeVerticesCommand = new MergeVerticesCommand(m_scene);
    }
    
    public boolean execute()
    {
        m_addedElements = new FastVector();
        m_removedElements = new FastVector();
        
        if(m_method == NOCENTER)
        {
            subdivideSelected1();
        }
        else if(m_method == CENTER)
        {
            subdivideSelected2();
        }

        m_scene.validateScene();

        return true;
    }

    /**
     * Subdivide selected faces using either triangle or quad
     * subdivision method 1.
     */
    public void subdivideSelected1()
    {
        FastVector primitives = m_scene.getPrimitivesVector();

        Stomp.statusBar.startProgress("Subdividing...",
                                      100.0/primitives.size());
        for(int i = primitives.size()-1; i >=0 ; i--)
        {
            Primitive element = (Primitive)primitives.elementAt(i);
            if(element.isSelected() && element instanceof Polygon3d)
            {
                if(((Polygon3d)element).numIndices() == 3)
                {
                    subdivideTriangle1((Polygon3d)element);
                }
                else if(((Polygon3d)element).numIndices() == 4)
                {
                    subdivideQuad1((Polygon3d)element);
                }
            }
            else if(element.isSelected() && element instanceof SplineSurface)
            {
                subdivideSplineSurface((SplineSurface)element);
                return;  //an evil bypass of the merge vertices.
            }
            else if(element.isSelected() && element instanceof Spline)
            {
                subdivideSpline((Spline)element);
            }
            Stomp.statusBar.incrementProgress();
        }

        m_mergeVerticesCommand.execute();
    }

    /**
     * Subdivide selected faces using either triangle or quad
     * subdivision method 2.
     */
    public void subdivideSelected2()
    {
        FastVector primitives = m_scene.getPrimitivesVector();
        
        Stomp.statusBar.startProgress("Subdividing...",
                                      100.0/primitives.size());
        for(int i = primitives.size()-1; i >=0 ; i--)
        {
            Primitive element = (Primitive)primitives.elementAt(i);
            if(element.isSelected() && element instanceof Polygon3d)
            {
                if(((Polygon3d)element).numIndices() == 3)
                {
                    subdivideTriangle2((Polygon3d)element);
                }
                else if(((Polygon3d)element).numIndices() == 4)
                {
                    subdivideQuad2((Polygon3d)element);
                }
                Stomp.statusBar.incrementProgress();
            }
        }

        m_mergeVerticesCommand.execute();
    }

    /**
     * Subdivide triangular faces using method 1:
     * <pre>
     *      /\                  /\
     *     /  \                /__\
     *    /    \   ----->     /\  /\
     *   /______\            /__\/__\
     * </pre>
     */
    public void subdivideTriangle1(Polygon3d element)
    {
        Surface surf = element.getSurface();
        FastVector primitives = m_scene.getPrimitivesVector();
        FastVector transformedVertices = m_scene.getVerticesVector();

        int indices[] = element.getIndices();
        
        Vertex a = new Vertex();
        Vertex b = new Vertex();
        Vertex c = new Vertex();

        //Find midpoint of each of the three sides of the face.
        a.add((Vertex)transformedVertices.elementAt(indices[0]),
              (Vertex)transformedVertices.elementAt(indices[2]));
        a.scale(.5f);
        
        b.add((Vertex)transformedVertices.elementAt(indices[0]),
              (Vertex)transformedVertices.elementAt(indices[1]));
        b.scale(.5f);
        
        c.add((Vertex)transformedVertices.elementAt(indices[1]),
              (Vertex)transformedVertices.elementAt(indices[2]));
        c.scale(.5f);

        //Add vertices along the edges at the midpoint.
        m_scene.addVertex(a);
        m_scene.addVertex(b);
        m_scene.addVertex(c);
        
        m_addedElements.addElement(a);
        m_addedElements.addElement(b);
        m_addedElements.addElement(c);

        
        int aInd = m_scene.getIndex(a);
        int bInd = m_scene.getIndex(b);
        int cInd = m_scene.getIndex(c);
        
        //Delete the existing Polygon3d.
        primitives.removeElement(element);
        m_removedElements.addElement(element);
        
        //Form the 4 new Polygon3ds resulting from the subdivision
        //and add them to the scene.
        Polygon3d p1 = new Polygon3d(aInd, indices[0], bInd);
        Polygon3d p2 = new Polygon3d(bInd, indices[1], cInd);
        Polygon3d p3 = new Polygon3d(cInd, indices[2], aInd);
        Polygon3d p4 = new Polygon3d(aInd, bInd, cInd);

        p1.setSurface(surf);
        p2.setSurface(surf);
        p3.setSurface(surf);
        p4.setSurface(surf);
        
        m_scene.addPrimitive(p1);
        m_scene.addPrimitive(p2);
        m_scene.addPrimitive(p3);
        m_scene.addPrimitive(p4);

        m_addedElements.addElement(p1);
        m_addedElements.addElement(p2);
        m_addedElements.addElement(p3);
        m_addedElements.addElement(p4);
    }

    /**
     * Subdivide triangular face using method 2:
     * (Create new vertex in the middle, form 3 triangles.
     *<pre>
     *      / \                 /|\
     *     /   \               / | \
     *    /     \   ----->    / ,+, \
     *   /_______\           /.-___-.\
     *</pre>
     */
    public void subdivideTriangle2(Polygon3d element)
    {
        Surface surf = element.getSurface();
        FastVector primitives = m_scene.getPrimitivesVector();
        FastVector transformedVertices = m_scene.getVerticesVector();

        int indices[] = element.getIndices();

        //Create new vertex in center and get its index.
        Vertex newVertex = element.getCenter(transformedVertices);
        m_scene.addVertex(newVertex);
        m_addedElements.addElement(newVertex);
        int newIndex = m_scene.getIndex(newVertex);

        //Delete old Polygon3d.
        primitives.removeElement(element);
        m_removedElements.addElement(element);

        //Form the three resulting Polygon3ds.
        Polygon3d p1 = new Polygon3d(newIndex, indices[0], indices[1]);
        Polygon3d p2 = new Polygon3d(newIndex, indices[1], indices[2]);
        Polygon3d p3 = new Polygon3d(newIndex, indices[2], indices[0]);

        p1.setSurface(surf);
        p2.setSurface(surf);
        p3.setSurface(surf);
        
        m_scene.addPrimitive(p1);
        m_scene.addPrimitive(p2);
        m_scene.addPrimitive(p3);

        m_addedElements.addElement(p1);
        m_addedElements.addElement(p2);
        m_addedElements.addElement(p3);
    }

    /**
     * Subdivide quad face using method 1:
     *<pre>
     *    _____            _____
     *   |     |          |  |  |
     *   |     |  ---->   |--+--|
     *   |_____|          |__|__|
     *</pre>
     */
    public void subdivideQuad1(Polygon3d element)
    {
        Surface surf = element.getSurface();
        FastVector primitives = m_scene.getPrimitivesVector();
        FastVector transformedVertices = m_scene.getVerticesVector();

        int indices[] = element.getIndices();

        //Create new vertex in center.
        Vertex centerVertex = element.getCenter(transformedVertices);
        
        Vertex a = new Vertex();
        Vertex b = new Vertex();
        Vertex c = new Vertex();
        Vertex d = new Vertex();

        //Find midpoint of four edges.
        a.add((Vertex)transformedVertices.elementAt(indices[0]),
              (Vertex)transformedVertices.elementAt(indices[1]));
        a.scale(.5f);
        
        b.add((Vertex)transformedVertices.elementAt(indices[1]),
              (Vertex)transformedVertices.elementAt(indices[2]));
        b.scale(.5f);
        
        c.add((Vertex)transformedVertices.elementAt(indices[2]),
              (Vertex)transformedVertices.elementAt(indices[3]));
        c.scale(.5f);

        d.add((Vertex)transformedVertices.elementAt(indices[3]),
              (Vertex)transformedVertices.elementAt(indices[0]));
        d.scale(.5f);

        //Create vertex in center and along midpoing of four
        //edges.
        m_scene.addVertex(centerVertex);
        m_scene.addVertex(a);
        m_scene.addVertex(b);
        m_scene.addVertex(c);
        m_scene.addVertex(d);

        m_addedElements.addElement(centerVertex);
        m_addedElements.addElement(a);
        m_addedElements.addElement(b);
        m_addedElements.addElement(c);
        m_addedElements.addElement(d);

        int center = m_scene.getIndex(centerVertex);
        int ai = m_scene.getIndex(a);
        int bi = m_scene.getIndex(b);
        int ci = m_scene.getIndex(c);
        int di = m_scene.getIndex(d);

        //Remove origional face.
        primitives.removeElement(element);
        m_removedElements.addElement(element);

        //Create four new Polygon3ds formed by the triangulation.
        Polygon3d p1 = new Polygon3d(di, indices[0], ai, center);
        Polygon3d p2 = new Polygon3d(ai, indices[1], bi, center);
        Polygon3d p3 = new Polygon3d(bi, indices[2], ci, center);
        Polygon3d p4 = new Polygon3d(ci, indices[3], di, center);

        p1.setSurface(surf);
        p2.setSurface(surf);
        p3.setSurface(surf);
        p4.setSurface(surf);
        
        m_scene.addPrimitive(p1);
        m_scene.addPrimitive(p2);
        m_scene.addPrimitive(p3);
        m_scene.addPrimitive(p4);

        m_addedElements.addElement(p1);
        m_addedElements.addElement(p2);
        m_addedElements.addElement(p3);
        m_addedElements.addElement(p4);

    }
    
    /**
     * Subdivide quad face using method 2:
     *<pre>
     *    _____            _____
     *   |     |          |\ | /|
     *   |     |  ---->   | >X< |
     *   |_____|          |/___\|
     *</pre>
     */
    public void subdivideQuad2(Polygon3d element)
    {
        Surface surf = element.getSurface();
        FastVector primitives = m_scene.getPrimitivesVector();
        FastVector transformedVertices = m_scene.getVerticesVector();

        int indices[] = element.getIndices();

        //Create and add center vertex.
        Vertex centerVertex = element.getCenter(transformedVertices);
        
        m_scene.addVertex(centerVertex);
        m_addedElements.addElement(centerVertex);
        int center = m_scene.getIndex(centerVertex);

        //Remove the original Polygon3d.
        primitives.removeElement(element);
        m_removedElements.addElement(element);

        //Create four Polygon3ds that connect from CORNERS of quad face
        //to center.
        Polygon3d p1 = new Polygon3d(indices[0], indices[1], center);
        Polygon3d p2 = new Polygon3d(indices[1], indices[2], center);
        Polygon3d p3 = new Polygon3d(indices[2], indices[3], center);
        Polygon3d p4 = new Polygon3d(indices[3], indices[0], center);

        p1.setSurface(surf);
        p2.setSurface(surf);
        p3.setSurface(surf);
        p4.setSurface(surf);
        
        //Add the new Polygon3ds to the scene.
        m_scene.addPrimitive(p1);
        m_scene.addPrimitive(p2);
        m_scene.addPrimitive(p3);
        m_scene.addPrimitive(p4);

        m_addedElements.addElement(p1);
        m_addedElements.addElement(p2);
        m_addedElements.addElement(p3);
        m_addedElements.addElement(p4);
    }

    public void subdivideSpline(Spline spline)
    {
        m_scene.removePrimitive(spline);
        m_removedElements.addElement(spline);
        
        FastVector worldVertices = m_scene.getVerticesVector();
        FastVector vertices =
            spline.getVerticesAlongPath(.25/((float)Mode.NURB_SUBDIVIDE),
                                        worldVertices);
        int indices[] = spline.getIndices();
        Vertex splineVertices[] = new Vertex[indices.length];

        for(int i = 0; i < indices.length; i++)
        {
            splineVertices[i] = (Vertex)worldVertices.elementAt(indices[i]);
        }
        for(int i = 0; i < splineVertices.length; i++)
        {
            m_scene.removeVertex(splineVertices[i]);
        }
        
        //Add first vertex
        m_scene.addVertex(splineVertices[0]);
        m_addedElements.addElement(splineVertices[0]);
        m_scene.select(splineVertices[0]);

        //Add middle ones
        for(int i = 0; i < vertices.sizeFast(); i++)
        {
            m_scene.addVertex((Vertex)vertices.elementAt(i));
            m_addedElements.addElement(vertices.elementAt(i));
            m_scene.select((Vertex)vertices.elementAt(i));
        }

        //Add last one
        m_scene.addVertex(splineVertices[splineVertices.length-1]);
        m_addedElements.addElement(splineVertices[splineVertices.length-1]);
        m_scene.select(splineVertices[splineVertices.length-1]);        
    }
    
    public void subdivideSplineSurface(SplineSurface element)
    {
        FastVector transformedVertices = m_scene.getVerticesVector();
        
        float maxU = element.getMaxKnotU();
        float maxV = element.getMaxKnotV();
        
        FastVector first_row = new FastVector();
        FastVector second_row = new FastVector();
        int [] firstindices = new int[1];
        int [] secondindices= new int[1];

        //Create vertex in center and along midpoing of four
        //edges.
        boolean first = true;
        for(double i = 0.0; i<=maxU; i+= .25/((float)Mode.NURB_SUBDIVIDE))
        {
            for(double j = 0.0;j<=maxV; j += .25/((float)Mode.NURB_SUBDIVIDE))
            {
                if(first)
                {
                    first_row.addElement(element.paintSpline(transformedVertices,i,j));
                }
                else
                {
                    second_row.addElement(element.paintSpline(transformedVertices,i,j));
                }
            }

            if(first)
            {
                firstindices = new int [first_row.size()];
                secondindices = new int [first_row.size()];

                for(int k =0; k<first_row.size(); k++)
                {
                    m_scene.addVertex((Vertex)first_row.elementAt(k));
                    m_addedElements.addElement((Vertex)first_row.elementAt(k));
                }
                for(int k=0; k<first_row.size();k++)
                {
                    firstindices[k] = m_scene.getIndex((Vertex)first_row.elementAt(k));
                }

                //Closed spline
                if(element.isClosed())
                {
                    int newindices[] = new int[firstindices.length];
                    System.arraycopy(firstindices, 0, newindices, 0,
                                     firstindices.length);
                    Polygon3d cap = new Polygon3d(newindices);
                    cap.flip();
                    m_scene.addPrimitive(cap);
                    m_addedElements.addElement(cap);

                }
                first = false;
            }
            else
            {
                for(int k =0; k<second_row.size(); k++)
                {
                    m_scene.addVertex((Vertex)second_row.elementAt(k));
                    m_addedElements.addElement((Vertex)second_row.elementAt(k));
                }
                for(int k=0; k<second_row.size();k++)
                {
                    secondindices[k] = m_scene.getIndex((Vertex)second_row.elementAt(k));
                }

                Polygon3d p[] = new Polygon3d [second_row.size()-1];
                for(int k=0; k<second_row.size()-1;k++)
                {
                    p[k] = new Polygon3d(firstindices[k], firstindices[k+1],
                                     secondindices[k+1], secondindices[k]);
                }
                for(int k=0; k<second_row.size()-1; k++)
                {
                    m_scene.addPrimitive(p[k]);
                    m_addedElements.addElement(p[k]);
                }

                System.arraycopy(secondindices, 0, firstindices,0, secondindices.length);
                second_row = new FastVector();
            }
        }

        //Closed spline
        if(element.isClosed())
        {
            int newindices[] = new int[secondindices.length];
            System.arraycopy(secondindices, 0, newindices, 0,
                             secondindices.length);
            
            Polygon3d cap = new Polygon3d(newindices);
            m_scene.addPrimitive(cap);
            m_addedElements.addElement(cap);
        }        
    }

    /**
     * NOT YET IMPLEMENTED
     */
    public void unExecute()
    {
        //Delete added primitives
        for(int i = 0; i < m_addedElements.sizeFast(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Primitive)
            {
                m_scene.removePrimitive((Primitive)m_addedElements.elementAtFast(i));
            }
        }

        //Delete added vertices
        for(int i = 0; i < m_addedElements.sizeFast(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Vertex)
            {
                m_scene.removeVertex((Vertex)m_addedElements.elementAtFast(i));
            }
        }

        //Add back removed primitives.
        for(int i = 0; i < m_removedElements.sizeFast(); i++)
        {
            if(m_removedElements.elementAtFast(i) instanceof Primitive &&
               !(m_removedElements.elementAtFast(i) instanceof Spline))
            {
                m_scene.addPrimitive((Primitive)m_removedElements.elementAtFast(i));
                m_scene.select((Primitive)m_removedElements.elementAtFast(i));
            }
        }
        
        //m_mergeVerticesCommand.unExecute();
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Subdivide";
    }
}
