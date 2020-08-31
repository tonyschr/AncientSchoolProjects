package stomp.command;

import stomp.*;
import stomp.gui.MessageDialog;
import stomp.data3d.*;

import java.util.*;

/**
 * Make a basic shape.  Supported shapes include:
 * <ul>
 *   <li> SPHERE
 *   <li> CONE
 *   <li> CYLINDER
 *   <li> CUBE
 *   <li> PLANE (quad polygon)
 *   <li> SURFACE (flat b-spline surface)
 *   <li> CLOSED_SURFACE (circular b-spline surface)
 * <ul>
 */
public class MakeShapeCommand implements Command
{
    public static final int SPHERE = 1;
    public static final int CONE = 2;
    public static final int CYLINDER = 3;
    public static final int CUBE = 4;
    public static final int PLANE = 5;
    public static final int SURFACE = 6;
    public static final int CLOSED_SURFACE = 7;
    public static final int TESS_SPHERE = 8;
    
    public static int DIVISIONS = 16;
    public static int SECTIONS = 1;
    public static int MAXLEVEL = 3;

    private static final int lowx = -1;
    private static final int hix = 1;
    private static final int lowy = -1;
    private static final int hiy = 1;
    private static final int lowz = -1;
    private static final int hiz = 1;

    private Scene m_scene;
    private int m_shapeKind;
    private FastVector m_addedElements;
    private Command m_mergeVerticesCommand;
    private boolean m_merged = false;
    
    private MakeShapeCommand()
    {
    }
    
    public MakeShapeCommand(Scene scene, int shapeKind)
    {
        m_scene = scene;
        m_shapeKind = shapeKind;
        m_mergeVerticesCommand = new MergeVerticesCommand(m_scene);
        m_merged = false;
    }
    
    public boolean execute()
    {
        boolean changed = false;
        m_addedElements = new FastVector();
        
        switch(m_shapeKind)
        {
        case SPHERE:
            createSphere();
            changed = true;
            m_mergeVerticesCommand.execute();
            m_merged = true;
            break;
        case TESS_SPHERE:
            createTesselatedSphere();
            changed = true;
            m_mergeVerticesCommand.execute();
            m_merged = true;
            break;
        case CONE:
            createCone();
            changed = true;
            m_mergeVerticesCommand.execute();
            m_merged = true;
            break;
        case CYLINDER:
            createCylinder();
            changed = true;
            m_mergeVerticesCommand.execute();
            m_merged = true;
            break;
        case CUBE:
            createCube();
            changed = true;
            break;
        case PLANE:
            createPolygon();
            changed = true;
            break;
        case SURFACE:
            createSurface();
            changed = true;
            break;
        case CLOSED_SURFACE:
            createSplineCage();
            changed = true;
            break;
        }

        if(m_scene.verticesSelected())
        {
            //Select all the vertices that were just added.
            for(int i = 0; i < m_addedElements.sizeFast(); i++)
            {
                if(m_addedElements.elementAtFast(i) instanceof Vertex)
                {
                    m_scene.select((Vertex)m_addedElements.elementAtFast(i));
                }
            }
        }
        else
        {
            //Select all the primitives that were just added.
            for(int i = 0; i < m_addedElements.sizeFast(); i++)
            {
                if(m_addedElements.elementAtFast(i) instanceof Primitive)
                {
                    m_scene.select((Primitive)m_addedElements.elementAtFast(i));
                }
            }
 
        }
        m_scene.validateScene();

        return changed;
    }

    public void unExecute()
    {
        if(m_merged)
        {
            m_mergeVerticesCommand.unExecute();
        }
        
        //First, delete all the new primitives
        for(int i = 0; i < m_addedElements.size(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Primitive)
            {
                m_scene.removePrimitive((Primitive)m_addedElements.elementAt(i));
            }
        }
        
        //Then, it's safe to kill all of the vertices.
        for(int i = 0; i < m_addedElements.size(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Vertex)
            {
                m_scene.removeVertex((Vertex)m_addedElements.elementAtFast(i));
            }
        }
        
        m_scene.validateScene();
    }

    /**
     * Create a "unit size" polygon in the scene.
     */
    private void createPolygon()
    {
        //Create 4 vertices and add them to the m_scene.
        Vertex v1 = new Vertex(lowx, lowy, 0);
        Vertex v2 = new Vertex(hix, lowy, 0);
        Vertex v3 = new Vertex(hix, hiy, 0);
        Vertex v4 = new Vertex(lowx, hiy, 0);
        
        m_scene.addVertex(v1);
        m_scene.addVertex(v2);
        m_scene.addVertex(v3);
        m_scene.addVertex(v4);

        //Add to added elements for undo
        m_addedElements.addElement(v1);
        m_addedElements.addElement(v2);
        m_addedElements.addElement(v3);
        m_addedElements.addElement(v4);
        
        int i1 = m_scene.getIndex(v1);
        int i2 = m_scene.getIndex(v2);
        int i3 = m_scene.getIndex(v3);
        int i4 = m_scene.getIndex(v4);

        //Create the Polygon3d and add it to the m_scene.
        Polygon3d p1 = new Polygon3d(i1, i2, i3, i4);
            
        m_scene.addPrimitive(p1);

        //Add to added elements for undo
        m_addedElements.addElement(p1);
    }

    /**
     * Create a "unit size" cube in the scene.
     */
    private void createCube()
    {
        //Create 8 vertices for the cube and add them to the
        //scene.
        Vertex v1 = new Vertex(lowx, lowy, lowz);
        Vertex v2 = new Vertex(hix, lowy, lowz);
        Vertex v3 = new Vertex(hix, hiy, lowz);
        Vertex v4 = new Vertex(lowx, hiy, lowz);
        Vertex v5 = new Vertex(lowx, lowy, hiz);
        Vertex v6 = new Vertex(hix, lowy, hiz);
        Vertex v7 = new Vertex(hix, hiy, hiz);
        Vertex v8 = new Vertex(lowx, hiy, hiz);

        m_scene.addVertex(v1);
        m_scene.addVertex(v2);
        m_scene.addVertex(v3);
        m_scene.addVertex(v4);
        m_scene.addVertex(v5);
        m_scene.addVertex(v6);
        m_scene.addVertex(v7);
        m_scene.addVertex(v8);
        
        //Add to added elements for undo
        m_addedElements.addElement(v1);
        m_addedElements.addElement(v2);
        m_addedElements.addElement(v3);
        m_addedElements.addElement(v4);
        m_addedElements.addElement(v5);
        m_addedElements.addElement(v6);
        m_addedElements.addElement(v7);
        m_addedElements.addElement(v8);

        int i0 = m_scene.getIndex(v1);
        int i1 = m_scene.getIndex(v2);
        int i2 = m_scene.getIndex(v3);
        int i3 = m_scene.getIndex(v4);
        int i4 = m_scene.getIndex(v5);
        int i5 = m_scene.getIndex(v6);
        int i6 = m_scene.getIndex(v7);
        int i7 = m_scene.getIndex(v8);

        //Front
        Polygon3d p1 = new Polygon3d(i0, i1, i2, i3);

        //Back
        Polygon3d p2 = new Polygon3d(i7, i6, i5, i4);

        //Top
        Polygon3d p3 = new Polygon3d(i2, i6, i7, i3);

        //Bottom
        Polygon3d p4 = new Polygon3d(i1, i0, i4, i5);

        //Left
        Polygon3d p5 = new Polygon3d(i0, i3, i7, i4);

        //Right
        Polygon3d p6 = new Polygon3d(i1, i5, i6, i2);

        //Add the 6 Polygon3ds for the cube the the m_scene.
        m_scene.addPrimitive(p1);
        m_scene.addPrimitive(p2);
        m_scene.addPrimitive(p3);
        m_scene.addPrimitive(p4);
        m_scene.addPrimitive(p5);
        m_scene.addPrimitive(p6);

        //Add for undo
        m_addedElements.addElement(p1);
        m_addedElements.addElement(p2);
        m_addedElements.addElement(p3);
        m_addedElements.addElement(p4);
        m_addedElements.addElement(p5);
        m_addedElements.addElement(p6);
    }

    /**
     * Create sphere.  Creates a sphere by rotating a hemisphere
     * of points around another circle and forming polygons.
     */
    private void createSphere()
    {
        int divisions = DIVISIONS;
        if(divisions % 2 == 1)
        {
            divisions++; //must be even number
        }

        if(divisions <= 2)
        {
            MessageDialog dialog =
                new MessageDialog("Error: Spheres must have at least 3 sides.");
            dialog.setVisible(true);
            return;
        }
        
        int radius = 1;
        int inds[][] = new int[divisions][divisions];
        //boolean zface[] = new boolean[divisions];
        
        int i = 0;
        int j = 0;

        //Spin section of hemispheres.
        float rotate = 0;
        for(int d = 0; d < divisions/2+1; d++)
        {
            double y = radius * Math.cos(rotate);
            double rotateRadius = radius * Math.sin(rotate);
            
            //Hemisphere of vertices
            float angle = 0;
            for(int a = 0; a < divisions; a++)
            {
                double x = rotateRadius * Math.cos(angle);
                double z = rotateRadius * Math.sin(angle);
    
                Vertex v = new Vertex((float)x,
                                      (float)y,
                                      (float)z);
                m_scene.addVertex(v);

                //add undo information
                m_addedElements.addElement(v);

                inds[d][a] = m_scene.getIndex(v);
                angle += 2 * Math.PI/(float)divisions;

            }
            rotate += 2 * Math.PI/(float)divisions;
            i = 0;
            j++;
        }
        
        boolean flip = false;
        for(int s = 0; s < divisions/2; s++)
        {
            if(s > divisions/2)
                flip = true;
            
            for(int n = 0; n < divisions; n++)
            {
                int ind[] = new int[4];
                
                ind[3] = inds[s][n];
                ind[2] = inds[s][(n+1)%divisions];
                ind[1] = inds[(s+1)%(divisions/2+1)][(n+1)%divisions];
                ind[0] = inds[(s+1)%(divisions/2+1)][n];
                
                Polygon3d side = new Polygon3d(ind);
                m_scene.addPrimitive(side);
                
                //Add unto information
                m_addedElements.addElement(side);
            }
        }

        //merge vertices?
    }

    /**
     * This sphere turned out to be really lame.  There isn't currently
     * a button for this kind of sphere because it has a high polygon
     * count.  Also, it seems to be THE WORST CASE for Merge Vertices..
     */
    private void createTesselatedSphere()
    {
        Vertex XPLUS = new Vertex(1, 0, 0);
        Vertex XMIN = new Vertex(-1, 0, 0);
        Vertex YPLUS = new Vertex(0, 1, 0);
        Vertex YMIN = new Vertex(0, -1, 0);
        Vertex ZPLUS = new Vertex(0, 0, 1);
        Vertex ZMIN = new Vertex(0, 0, -1);
        
        Triangle octahedron[] = new Triangle[8];
        octahedron[0] = new Triangle(XPLUS, ZPLUS, YPLUS);
        octahedron[1] = new Triangle(YPLUS, ZPLUS, XMIN);
        octahedron[2] = new Triangle(XMIN, ZPLUS, YMIN);
        octahedron[3] = new Triangle(YMIN, ZPLUS, XPLUS);
        octahedron[4] = new Triangle(XPLUS, YPLUS, ZMIN);
        octahedron[5] = new Triangle(YPLUS, XMIN, ZMIN);
        octahedron[6] = new Triangle(XMIN, YMIN, ZMIN);
        octahedron[7] = new Triangle(YMIN, XPLUS, ZMIN);

        Triangle old[] = octahedron;

        for(int level = 1; level < MAXLEVEL; level++)
        {
            Triangle current[] = new Triangle[old.length * 4];

            for(int i = 0; i < old.length; i++)
            {
                int newindex = 4 * i;
                
                Vertex a = normalize(midpoint(old[i].v0, old[i].v2));
                Vertex b = normalize(midpoint(old[i].v0, old[i].v1));
                Vertex c = normalize(midpoint(old[i].v1, old[i].v2));

                current[newindex + 0] = new Triangle(old[i].v0, b, a);
                current[newindex + 1] = new Triangle(b, old[i].v1, c);
                current[newindex + 2] = new Triangle(a, b, c);
                current[newindex + 3] = new Triangle(a, c, old[i].v2);
            }

            old = current;
        }

        //Create polygons from the triangles that were generated.
        for(int i = 0; i < old.length; i++)
        {
            m_scene.addVertex(old[i].v0);
            m_scene.addVertex(old[i].v1);
            m_scene.addVertex(old[i].v2);

            //add for undo
            m_addedElements.addElement(old[i].v0);
            m_addedElements.addElement(old[i].v1);
            m_addedElements.addElement(old[i].v2);

            int i0 = m_scene.getIndex(old[i].v0);
            int i1 = m_scene.getIndex(old[i].v1);
            int i2 = m_scene.getIndex(old[i].v2);

            Polygon3d p = new Polygon3d(i0, i1, i2);
            m_scene.addPrimitive(p);

            //add for undo
            m_addedElements.addElement(p);
        }
    }

    private Vertex normalize(Vertex p)
    {
        Vertex r = new Vertex();
        r.set(p);
        
        double mag = p.x * p.x + p.y * p.y + p.z * p.z;
        if(mag != 0.0)
        {
            mag = 1.0 / Math.sqrt(mag);
            r.x *= mag;
            r.y *= mag;
            r.z *= mag;
        }

        return r;
    }

    private Vertex midpoint(Vertex v1, Vertex v2)
    {
        Vertex ave = new Vertex();
        ave.add(v1, v2);
        ave.scale(.5f);

        return ave;
    }
    
    /**
     * Create a "unit size" cylinder in the m_scene.
     */
    private void createCylinder()
    {
        int radius = 1;
        int indices[][] = new int[SECTIONS+1][DIVISIONS];

        //Add all the vertices for the m_scene.  Cylinders may have
        //multiple sections.
        for(int s = 0; s < SECTIONS+1; s++)
        {
            //int index = 0;
            float angle = 0;
            for(int index = 0; index < DIVISIONS; index++)
            {
                double x = radius * Math.cos(angle);
                double y = radius * Math.sin(angle);
                
                Vertex v = new Vertex((float)x, (float)y, s);
                m_scene.addVertex(v);

                //add undo information
                m_addedElements.addElement(v);

                indices[s][index] = m_scene.getIndex(v);
                angle += 2 * Math.PI/DIVISIONS;
            }
        }

        //Create top and bottom Polygon3ds
        Polygon3d top = new Polygon3d(indices[0]);
        Polygon3d bottom = new Polygon3d(indices[SECTIONS]);
        
        m_scene.addPrimitive(top); 
        m_scene.addPrimitive(bottom);

        //add undo information
        m_addedElements.addElement(top);
        m_addedElements.addElement(bottom);
        
        //Create Polygon3ds around the outsides of the cylinder.
        for(int s = 0; s < SECTIONS; s++)
        {
            for(int i = 0; i < DIVISIONS; i++)
            {
                int ind[] = new int[4];
                
                ind[3] = indices[s][i];
                ind[2] = indices[s][(i+1)%DIVISIONS];
                ind[1] = indices[s+1][(i+1)%DIVISIONS];
                ind[0] = indices[s+1][i];
                
                Polygon3d side = new Polygon3d(ind);
                m_scene.addPrimitive(side);
                m_addedElements.addElement(side);
            }
        }

        //Flip the bottom Polygon3d.
        bottom.flip();
        bottom.computeNormal(m_scene.getVerticesVector());
    }

    /**
     * Creates a unit size cone in the m_scene
     */
    private void createCone()
    {
        int radius = 1;
        int inds[] = new int[DIVISIONS];

        int i = 0;

        //Add center vertex for the cone.
        Vertex center = new Vertex(0, 0, 1);
        m_scene.addVertex(center);

        //add undo information
        m_addedElements.addElement(center);
        
        int centerInd = m_scene.getIndex(center);

        //Add circle of vertices for base of the cone.
        for(float angle = 0; angle < 2 * Math.PI; angle += 2*Math.PI/DIVISIONS)
        {
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);

            Vertex v = new Vertex((float)x, (float)y, 0);
            m_scene.addVertex(v);

            //undo info
            m_addedElements.addElement(v);
            
            inds[i++] = m_scene.getIndex(v);
        }

        //Create the sides of the cone.
        int last = inds[DIVISIONS-1];
        for(int j = 0; j < DIVISIONS; j++)
        {
            Polygon3d p = new Polygon3d(last, centerInd, inds[j]);
            last = inds[j];
            m_scene.addPrimitive(p);

            //undo info
            m_addedElements.addElement(p);
        }

        //Create the Polygon3d for the bottom of the cone.
        Polygon3d bottom = new Polygon3d(inds);
        m_scene.addPrimitive(bottom);

        //Undo information
        m_addedElements.addElement(bottom);
    }

    public void createSurface()
    {
        if(DIVISIONS < 4 || SECTIONS < 4)
        {
            MessageDialog dialog = new MessageDialog("Surfaces must be at least 4 x 4 patches");
            dialog.setVisible(true);
            return;
        }
        else if(DIVISIONS > 20 || SECTIONS > 20)
        {
            MessageDialog dialog = new MessageDialog("Surfaces can have a maximum of 20 x 20 patches\n(And that many will be very slow.)");
            dialog.setVisible(true);
            return;
        }
        //Splines will be the last 4.
        Spline splines[] = new Spline[SECTIONS];
        Vertex vertices[][] = new Vertex[SECTIONS][DIVISIONS];
        int vertexIndices[][] = new int[SECTIONS][DIVISIONS];

        for(int i = 0; i < SECTIONS; i++)
        {
            for(int j = 0; j < DIVISIONS; j++)
            {
                vertices[i][j] = new Vertex(i, 0, j);
                
                m_scene.addVertex(vertices[i][j]);
                m_addedElements.addElement(vertices[i][j]);
            }
        }

        for(int i = 0; i < SECTIONS; i++)
        {
            for(int j = 0; j < DIVISIONS; j++)
            {
                vertexIndices[i][j] = m_scene.getIndex(vertices[i][j]);
            }
            splines[i] = new Spline(vertexIndices[i]);
        }
        
        SplineSurface surface = new SplineSurface(splines);
        m_scene.addPrimitive(surface);
        m_addedElements.addElement(surface);
    }

    public void createSplineCage()
    {
        if(DIVISIONS < 4 || SECTIONS < 4)
        {
            MessageDialog dialog = new MessageDialog("Surfaces must be at least 4 x 4 patches");
            dialog.setVisible(true);
            return;
        }

        //Splines will be the last 4.
        Spline splines[] = new Spline[SECTIONS];
        Vertex vertices[][] = new Vertex[SECTIONS][DIVISIONS];
        int vertexIndices[][] = new int[SECTIONS][DIVISIONS+1];

        int radius = 1;

        //Add all the vertices for the m_scene.  Cylinders may have
        //multiple sections.
        for(int s = 0; s < SECTIONS; s++)
        {
            //int index = 0;
            float angle = 0;
            for(int index = 0; index < DIVISIONS; index++)
            {
                double x = radius * Math.cos(angle);
                double y = radius * Math.sin(angle);
                
                Vertex v = new Vertex((float)x, (float)y, s);
                m_scene.addVertex(v);
                vertices[s][index] = v;

                //add undo information
                m_addedElements.addElement(v);

                vertexIndices[s][index] = m_scene.getIndex(v);
                angle += 2 * Math.PI/(float)DIVISIONS;
            }
        }

        for(int i = 0; i < SECTIONS; i++)
        {
            for(int j = 0; j < DIVISIONS+1; j++)
            {
                vertexIndices[i][DIVISIONS-j] = m_scene.getIndex(vertices[i][j%DIVISIONS]);
            }
            splines[i] = new Spline(vertexIndices[i]);
        }
        
        SplineSurface surface = new SplineSurface(splines);
        m_scene.addPrimitive(surface);
        m_addedElements.addElement(surface);
    }
}
