package stomp.view;

import stomp.*;
import stomp.data3d.*;
import java.awt.*;
import java.lang.*;
import java.util.*;
import javax.vecmath.*;
import java.awt.event.*;

import stomp.data3d.*;
import stomp.transform.*;
import stomp.gui.Appearance;

public class CameraView extends View implements Primitive
{
    //Primitive attributes
    private boolean m_hidden = false;
    private boolean m_selected = false;
    private Group m_group;
    
    private int[] m_indices;
    private Vertex m_vertex;
    private Vertex m_lookatVertex;
    private float m_movedX = 0;
    private float m_movedY = 0;
    private float m_movedZ = 0;
    //    private Accumulator m_accumulator = new Accumulator();
    
    //View attributes
    private FastVector m_transformedVertices = new FastVector();

    public static int GRIDSIZE = 50;
    public static int GRIDMAX = 50;
    
    private static Color m_axisColor = new Color(220, 220, 220);

    //Camera viewing parameters
    protected Vector3f m_VRP = new Vector3f(0, 0, 5);
    protected Vector3f m_PRP = new Vector3f(0, 0, 5);
    protected Vector3f m_lookat = new Vector3f(0, 0, 0);
    protected Vector3f m_VUP = new Vector3f(0, 1, 0);
    protected float m_aspectRatio = 1.333f;

    protected Matrix4f m_device;
    protected Image offScreenImage;
    protected Dimension offScreenSize;
    protected Graphics offScreenGraphics;
    protected Scene m_scene;
    protected Point m_origin;

    protected Matrix4f m_persMatrix;
    protected Matrix4f m_viewTransform = new Matrix4f();
    
    protected float m_zoom = 5;

    protected transient Matrix4f scale = new Matrix4f();
    protected transient Matrix4f rotate1 = new Matrix4f();
    protected transient Matrix4f rotate2 = new Matrix4f();
    protected transient Matrix4f pan = new Matrix4f();
    protected transient Vector3f n = new Vector3f();

    protected Point4f m_transOrigin = new Point4f();
    protected Point4f m_transxAxis = new Point4f();    
    protected Point4f m_transyAxis = new Point4f();
    protected Point4f m_transzAxis = new Point4f();

    protected Point4f m_xPoints1[] = new Point4f[GRIDMAX + 1];
    protected Point4f m_xPoints2[] = new Point4f[GRIDMAX + 1];
    protected Point4f m_zPoints1[] = new Point4f[GRIDMAX + 1];
    protected Point4f m_zPoints2[] = new Point4f[GRIDMAX + 1];

    private int m_sceneSize = 0;
    
    double window_lft,window_rght, window_top, window_btm;
    double hold_lft, hold_rght, hold_btm, hold_top;
    protected static boolean m_fast = false;
    protected boolean m_localChange = false;

    public CameraView(Scene scene, Vertex vrp, Vertex lookat)
    {
        super("CAMERA");

        m_scene = scene;
        m_vertex = vrp;
        m_lookatVertex = lookat;
        
        m_VRP.x = m_vertex.x;
        m_VRP.y = m_vertex.y;
        m_VRP.z = m_vertex.z;

        m_lookat.x = m_lookatVertex.x;
        m_lookat.y = m_lookatVertex.y;
        m_lookat.z = m_lookatVertex.z;

        Dimension size = getSize();
        window_lft = -m_aspectRatio;
        window_btm = -1;
        window_top = 1;
        window_rght = m_aspectRatio;
        
        for(int i = 0; i < GRIDMAX + 1; ++i)
        {
            m_xPoints1[i] = new Point4f();
            m_xPoints2[i] = new Point4f();
            m_zPoints1[i] = new Point4f();
            m_zPoints2[i] = new Point4f();
        }

        m_persMatrix = setupPerspective();
        m_device = makedevice(size.width,size.height);
        m_viewTransform.mul(m_device, m_persMatrix);

        //Set the initial grid size.  Duplicate from zoom.
        int tempgrid = (int)(30 / m_zoom);
        if(tempgrid < 50)
        {
            GRIDSIZE = (tempgrid/2) * 2;
        }
    }

    //Add double-buffering to the paint method to allow smooth graphics
    public final synchronized void update (Graphics g) 
    {
        Dimension d = getSize();
        
        if((offScreenImage == null) || (d.width != offScreenSize.width) ||  (d.height != offScreenSize.height)) 
        {
            offScreenImage = createImage(d.width, d.height);
            offScreenSize = d;
            offScreenGraphics = offScreenImage.getGraphics();
        }
        paint(offScreenGraphics);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    public void paint(Graphics g)
    {
        Dimension size = getSize();

        //Set the 2d clip bounds to the size of the viewport.
        SutherlandHodgman.setClipBounds(0, size.height,
                                        0, size.width);
            
        //Gray background
        g.setColor (Color.lightGray);
        g.fillRect (0, 0, size.width, size.height);
        if(m_localChange)
        {
            m_viewTransform.mul(m_device, m_persMatrix);
        
            m_localChange = false;
        }
        transformVertices();

        g.setColor(m_axisColor);
        for(int i = 0; i < GRIDSIZE+1; ++i)
        {
            g.drawLine((int)m_xPoints1[i].x, (int)m_xPoints1[i].y,
                       (int)m_xPoints2[i].x, (int)m_xPoints2[i].y);
            g.drawLine((int)m_zPoints1[i].x, (int)m_zPoints1[i].y,
                       (int)m_zPoints2[i].x, (int)m_zPoints2[i].y);
        }
        
        g.setColor(Color.white);

        g.drawLine((int)m_transOrigin.x, (int)m_transOrigin.y,
                   (int)m_transxAxis.x, (int)m_transxAxis.y);
        g.drawLine((int)m_transOrigin.x, (int)m_transOrigin.y,
                   (int)m_transyAxis.x, (int)m_transyAxis.y);
        g.drawLine((int)m_transOrigin.x, (int)m_transOrigin.y,
                   (int)m_transzAxis.x, (int)m_transzAxis.y);

//         g.setColor(Color.black);
        
//         //Paint the non-selected ones only (speed hazard)
//         FastVector primitives = m_scene.getPrimitivesVector();
//         Primitive temp;
//         for(int i = primitives.sizeFast()-1; i >=0; i--)
//         {
//             temp = (Primitive)primitives.elementAtFast(i);
//             if(!temp.isSelected() && !(temp instanceof CameraView))
//             {
//                 temp.paint(m_transformedVertices, g);
//             }
//         }

//         //Paint the selected ones (speed hazard)
//         Point4f normalTemp = new Point4f();
//         Vertex[] norm;
//         for(int i = primitives.sizeFast()-1; i >=0; i--)
//         {
//             temp = (Primitive)primitives.elementAtFast(i);
//             if(temp.isSelected() && !(temp instanceof CameraView))
//             {
//                 temp.paint(m_transformedVertices, g);
                
//                 if(m_fast == false && temp instanceof Polygon3d)
//                 {
//                     norm = ((Polygon3d)temp).getNormalPoints();

//                     //0
//                     normalTemp.set(norm[0].x, norm[0].y, norm[0].z, 1);
//                     m_viewTransform.transform(normalTemp);
//                     normalTemp.project(normalTemp);
//                     norm[0].set(normalTemp.x, normalTemp.y, normalTemp.z);

//                     //1
//                     normalTemp.set(norm[1].x, norm[1].y, norm[1].z, 1);
//                     m_viewTransform.transform(normalTemp);
//                     normalTemp.project(normalTemp);
//                     norm[1].set(normalTemp.x, normalTemp.y, normalTemp.z);
                    
//                     ((Polygon3d)temp).paintNormal(norm, g);
//                 }
//             }
//         }
        
//         for(int i = m_transformedVertices.sizeFast()-1; i >= 0; i--)
//         {
//             ((Vertex)m_transformedVertices.elementAtFast(i)).paint(g);
//         }
//         Dimension size = getSize();
//         //Gray background
//         g.setColor (Color.lightGray);
//         g.fillRect (0, 0, size.width, size.height);

//         transformVertices();

        //        g.setColor(Color.black);
        
        //Paint the non-selected ones only (speed hazard)
        FastVector primitives = m_scene.getPrimitivesVector();
        Primitive temp;
        int decrement = 1;
        if(m_fast)
        {
            decrement = 1 +
                (int)(primitives.sizeFast()/Mode.DISAPPEAR_THRESHHOLD);
        }

        if(m_scene.primitivesSelected())
        {
            for(int i = primitives.sizeFast()-1; i >=0; i-=decrement)
            {
                temp = (Primitive)primitives.elementAtFast(i);
                if(!temp.isSelected())
                {
                    temp.paint(m_transformedVertices, g);
                }
            }

            FastVector selectedPrimitives = m_scene.getOrderedSelectedPrimitives();
            //Paint the selected ones (speed hazard)
            Point4f normalTemp = new Point4f();
            Vertex[] norm;
            for(int i = selectedPrimitives.sizeFast()-1; i >=0; i--)
            {
                temp = (Primitive)selectedPrimitives.elementAtFast(i);
                //if(temp.isSelected())
                {
                    temp.paint(m_transformedVertices, g);
                    
                    if(m_fast == false && temp instanceof Polygon3d)
                    {
                        norm = ((Polygon3d)temp).getNormalPoints();
                        
                        //0
                        normalTemp.set(norm[0].x, norm[0].y, norm[0].z, 1);
                        m_viewTransform.transform(normalTemp);
                        normalTemp.project(normalTemp);
                        norm[0].set(normalTemp.x, normalTemp.y, normalTemp.z);
                        
                        //1
                        normalTemp.set(norm[1].x, norm[1].y, norm[1].z, 1);
                        m_viewTransform.transform(normalTemp);
                        normalTemp.project(normalTemp);
                        norm[1].set(normalTemp.x, normalTemp.y, normalTemp.z);
                        
                        ((Polygon3d)temp).paintNormal(norm, g);
                    }
                }
            }
        }
        else
        {
            for(int i = primitives.sizeFast()-1; i >=0; i-=decrement)
            {
                ((Primitive)primitives.elementAtFast(i)).paint(m_transformedVertices, g);
            }
        }


        if(m_scene.verticesSelected())
        {
            decrement = 1;
        }
        for(int i = m_transformedVertices.sizeFast()-1; i >= 0; i-=decrement)
        {
            ((Vertex)m_transformedVertices.elementAtFast(i)).paint(g);
        }

        //Hightlight goes over everything!
        super.paint(g);
    }

    public Point3f getPosition()
    {
        return new Point3f(m_VRP.x, m_VRP.y, m_VRP.z);
    }

    public Point3f getLookatPoint()
    {
        return new Point3f(m_lookat.x, m_lookat.y, m_lookat.z);
    }
    
    protected void transformVertices()
    {
        //This will have to change once vertices can be deleted, but
        //that should be easy.  Just trying to keep the size the same,
        //the vertices all get copied over anyway.
        FastVector sceneVertices = m_scene.getVerticesVector();
        int sceneSize = sceneVertices.size();

        if(sceneSize != m_sceneSize)
        {
            while(m_transformedVertices.size() < sceneSize)
            {
                m_transformedVertices.addElement(new Vertex());
            }
            while(m_transformedVertices.size() > sceneSize)
            {
                m_transformedVertices.
                removeElementAt(m_transformedVertices.size()-1);
            }
            m_sceneSize = sceneSize;
        }
        
        Point4f temp = new Point4f();
        Vertex dest, source;
        for(int i = 0; i < sceneSize; ++i)
        {
            //Get the vertex from the destination
            dest = (Vertex)m_transformedVertices.elementAtFast(i);
            source = (Vertex)sceneVertices.elementAtFast(i);
            dest.set(source); //set for properties like selection
            temp.set(source.x, source.y, source.z, 1);
            
            m_viewTransform.transform(temp);
            temp.project(temp);
            dest.set(temp.x, temp.y, temp.z);
        }
        
        //Calculate axis and grid
//         m_transOrigin.set(m_lookat.x, m_lookat.y, m_lookat.z, 1);
//         m_transxAxis.set(m_lookat.x+1, 0, 0, 1);
//         m_transyAxis.set(0, m_lookat.y+1, 0, 1);
//         m_transzAxis.set(0, 0, m_lookat.z+1, 1);
        m_transOrigin.set(0, 0, 0, 1);
        m_transxAxis.set(1, 0, 0, 1);
        m_transyAxis.set(0, 1, 0, 1);
        m_transzAxis.set(0, 0, 1, 1);

        m_viewTransform.transform(m_transOrigin);
        m_viewTransform.transform(m_transxAxis);
        m_viewTransform.transform(m_transyAxis);
        m_viewTransform.transform(m_transzAxis);

        m_transOrigin.project(m_transOrigin);
        m_transxAxis.project(m_transxAxis);
        m_transyAxis.project(m_transyAxis);
        m_transzAxis.project(m_transzAxis);

        int step = GRIDSIZE / 2;
        for(int i = 0; i < GRIDSIZE+1; ++i)
        {
            m_xPoints1[i].set(step-i, 0, step, 1);
            m_xPoints2[i].set(step-i, 0, -step, 1);
            m_zPoints1[i].set(-step, 0, step-i, 1);
            m_zPoints2[i].set(step, 0, step-i, 1);

            m_viewTransform.transform(m_xPoints1[i]);
            m_viewTransform.transform(m_xPoints2[i]);
            m_viewTransform.transform(m_zPoints1[i]);
            m_viewTransform.transform(m_zPoints2[i]);

            m_xPoints1[i].project(m_xPoints1[i]);
            m_xPoints2[i].project(m_xPoints2[i]);
            m_zPoints1[i].project(m_zPoints1[i]);
            m_zPoints2[i].project(m_zPoints2[i]);
        }
    }

    /**
     * 
     */
    Matrix4f setupPerspective()
    {
        float  Back, Front;

        Front = 1;
        Back = -100;

        int width = 320;
        int height = 200;
        
        Matrix4f transform = new Matrix4f();
        transform.setIdentity();
            
        //Create u, v, n
        n = new Vector3f();
        n.sub(m_VRP, m_lookat);
        n.normalize();

        Vector3f u = new Vector3f();
        u.cross(m_VUP, n);
        u.normalize();

        Vector3f v = new Vector3f();
        v.cross(n, u);

        Matrix4f T = new Matrix4f();
        T.setIdentity();
        T.m03 = -m_VRP.x;
        T.m13 = -m_VRP.y;
        T.m23 = -m_VRP.z;
            
        Matrix4f Morient = new Matrix4f();
        Morient.setIdentity();
        Morient.m00 = u.x;
        Morient.m01 = u.y;
        Morient.m02 = u.z;
        Morient.m10 = v.x;
        Morient.m11 = v.y;
        Morient.m12 = v.z;
        Morient.m20 = n.x;
        Morient.m21 = n.y;
        Morient.m22 = n.z;

        Matrix4f Tprp = new Matrix4f();
        Tprp.setIdentity();
        Tprp.m03 = -m_PRP.x;
        Tprp.m13 = -m_PRP.y;
        Tprp.m23 = -m_PRP.z;

        Vector3f DOP = new Vector3f( (float)(window_lft + window_rght)/2.0f,
                                     (float)(window_btm + window_top)/2.0f,
                                     0.0f);

        DOP.sub(m_PRP);

        Matrix4f SHpar = new Matrix4f();
        SHpar.setIdentity();
        SHpar.m02 = -(DOP.x/DOP.z);
        SHpar.m12 = -(DOP.y/DOP.z);

        
        Matrix4f Sper = new Matrix4f();
        Sper.setIdentity();
        Sper.m00 = (float) -( (2*m_PRP.z) / ((window_lft - window_rght) *
                                             (Back - m_PRP.z)));
        Sper.m11 = (float) -( (2*m_PRP.z) / ((window_top - window_btm) *
                                             (Back - m_PRP.z)));
        Sper.m22 = (float) -(1/(Back - m_PRP.z));
        
        //System.out.println("Sper = \n" + Sper);        
        //Find nmin
        double nmin = -( (Front - m_PRP.z)/(Back - m_PRP.z) );

        
        Matrix4f Mp = new Matrix4f();
        Mp.m00 = 1;
        Mp.m11 = 1;
        Mp.m22 = (float)(1/(1 + nmin));
        Mp.m23 = (float)-(nmin/(1 + nmin));
        Mp.m32 = -1;

        //Do the matrix multiplication to compose each of these
        //individual matrices into the transformation matrix.

        transform.mul(Mp);
        transform.mul(Sper);
        transform.mul(SHpar);
        transform.mul(Tprp);
        transform.mul(Morient);
        transform.mul(T);

        //Return the resulting transformation matrix.
        return transform;
    }

    //Create device matrix
    Matrix4f makedevice (int width, int height)
    {
        //Flips the Y coordinate of the point, since X windows defines 0,0 as
        //Top left corner
        Matrix4f Md0 = new Matrix4f();
        Md0.setIdentity();
        Md0.m11= -1;
        Md0.m00= -1;
        
        //Add one to X and Y to make points in the range of
        // 0 to 2, instead of -1 to 1
        Matrix4f Md1 = new Matrix4f();
        Md1.setIdentity();
        Md1.m03 =1;
        Md1.m13=1;

        //Scale the points to fit in the viewport window
        Matrix4f Md2 = new Matrix4f();
        Md2.setIdentity();
        Md2.m00 = width/2;
        Md2.m11 = height/2;
        Md2.m22 = 10;

        //Multiply the matrices together and return the result
        Md1.mul(Md0);
        Md2.mul(Md1);
        return (Md2); 
    }

    public void mouseClicked(MouseEvent e)
    {
        super.mouseClicked(e);
    }

    float m_holdPRPx, m_holdPRPz;
    
    public void mousePressed(MouseEvent e)
    {
        super.mousePressed(e);
        
        m_origin = e.getPoint();
    }
    public void mouseReleased(MouseEvent e)
    {
        m_localChange = true;
        repaint();
    }
    
    public void mouseDragged(MouseEvent e)
    {
    }

    /**
     * When the component is resized, we need to recompute the
     * entire projection pipeline for the new window size so the
     * image does not get squished.
     */
    public void componentResized(ComponentEvent e)
    {
        Dimension size = getSize();
        window_lft = -1.33;//size.width/100.0f;
        window_btm = -1;//size.height/100.0f;
        window_top = 1;//size.height/100.0f;
        window_rght = 1.33;//size.width/100.0f;
        m_persMatrix = setupPerspective();
        m_device = makedevice(size.width,size.height);
        m_viewTransform.mul(m_device, m_persMatrix);
        repaint();
    }

    public void mouseEntered(MouseEvent e)
    {
        super.mouseEntered(e);

        if (Mode.getMode() == Mode.PAN || Mode.getMode() == Mode.ZOOM)
        {
            setCursor(m_moveCursor);
        }
    }

    public void mouseExited(MouseEvent e)
    {
        super.mouseExited(e);
        setCursor(m_pickCursor);
    }

//      public Accumulator getAccumulator()
//      {
//          return m_accumulator;
//      }

    public void moveFromOrigin(float x, float y, float z)
    {
        m_movedX += x;
        m_movedY += y;
        m_movedZ += z;
    }

    public Vector3f backToOrigin()
    {
        return new Vector3f(-m_movedX, -m_movedY, -m_movedZ);
    }
    
    public void rotateFromOrigin(AxisAngle4f axang)
    {
        
    }

    public AxisAngle4f rotateBackToOriginAxisAngle()
    {
        return new AxisAngle4f(0, 0, 0, 0);
    }

    public void rotateFromOrigin(float rx, float ry, float rz)
    {
    }

    public Vector3f rotateBackToOrigin()
    {
        return new Vector3f(0, 0, 0);
    }
    
    public void moveOrigin(float x, float y, float z)
    {
    }
    
    public Group getGroup()
    {
        return m_group;
    }

    public void setHidden(boolean hide)
    {
        m_hidden = hide;
    }

    public boolean isHidden()
    {
        return m_hidden;
    }
    
    public void setGroup(Group g)
    {
        m_group = g;
    }
    
    public boolean isSelected()
    {
        return m_selected;
    }

    public void setSelected(boolean select)
    {
        m_selected = select;
    }

    public boolean select(FastVector vertices, int mouseX, int mouseY)
    {
        Vertex cameraVertex = (Vertex)vertices.elementAtFast(m_indices[0]);

        double dist = Math.sqrt( Math.abs(cameraVertex.x - mouseX) *
                                 Math.abs(cameraVertex.x - mouseX) +
                                 Math.abs(cameraVertex.y - mouseY) *
                                 Math.abs(cameraVertex.y - mouseY));

        if(dist < 12.0)
        {
            return true;
        }

        return false;
    }
    
    public boolean selectRegion(FastVector vertices, int xmin, int xmax,
                                int ymin, int ymax)
    {
        Vertex cameraVertex = (Vertex)vertices.elementAtFast(m_indices[0]);

        if( cameraVertex.x >= xmin &&
            cameraVertex.x <= xmax &&
            cameraVertex.y >= ymin &&
            cameraVertex.y <= ymax)
        {
            return true;
        }

        return false;
    }

    public void paint(FastVector vertices, Graphics g)
    {
        if(m_hidden)
        {
            return;
        }
        
        Vertex cameraVertex = (Vertex)vertices.elementAtFast(m_indices[0]);
        Vertex lookatVertex = (Vertex)vertices.elementAtFast(m_indices[1]);

        m_VRP.x = m_vertex.x;
        m_VRP.y = m_vertex.y;
        m_VRP.z = m_vertex.z;

        m_lookat.x = m_lookatVertex.x;
        m_lookat.y = m_lookatVertex.y;
        m_lookat.z = m_lookatVertex.z;

        m_localChange = true;
        m_persMatrix = setupPerspective();
        repaint();

        if(m_group == null)
        {
            g.setColor(Color.blue);
        }
        else
        {
            g.setColor(m_group.getColor());
        }
            
        g.drawOval((int)lookatVertex.x - 4, (int)lookatVertex.y - 4,
                   8, 8);

        if(m_selected)
        {
            g.setColor(Color.cyan);
        }
        
        g.drawRect((int)cameraVertex.x - 9, (int)cameraVertex.y - 9,
                   18, 18);
        g.drawOval((int)cameraVertex.x - 9, (int)cameraVertex.y - 9,
                   18, 18);
    }
    
    public void transform(Transformation tr, FastVector fromVertices,
                          FastVector toVertices)
    {
        Vertex cameraVertex = (Vertex)toVertices.elementAtFast(m_indices[0]);
        tr.transformVertex((Vertex)fromVertices.elementAtFast(m_indices[0]),
                           (Vertex)toVertices.elementAtFast(m_indices[0]));
    }
    
    public boolean containsIndex(int index)
    {
        return m_indices[0] == index;
    }
    
    public int[] getIndices()
    {
        return m_indices;
    }
    
    public void setIndices(int indices[])
    {
        m_indices = new int[indices.length];
        System.arraycopy(indices, 0, m_indices, 0, indices.length);
    }
    
    public boolean renumberIndices(int afterInd)
    {
        boolean vertexDeleted = false;

        //Go through vertices, and mark any that have actually been
        //Deleted.  Renumber the rest that occur after that index.
        int l = m_indices.length;
        for(int i = 0; i < l; ++i)
        {
            if(m_indices[i] == afterInd)
            {
                m_indices[i] = -1;
                vertexDeleted = true;
            }
            else if(m_indices[i] > afterInd)
            {
                m_indices[i]--;
            }
        }

        //If a vertex has been deleted, create a new array that is
        //one size smaller.  Copy all indices except the one
        //deleted into the new array.
        if(vertexDeleted)
        {
            int newIndices[] = new int[m_indices.length-1];

            int j = 0;
            for(int i = 0; i < m_indices.length; ++i)
            {
                if(m_indices[i] != -1)
                {
                    newIndices[j++] = m_indices[i];
                }
            }

            m_indices = newIndices;
        }
        return false;
    }
    
    public void replaceIndex(int oldIndex, int newIndex)
    {
        for(int i = 0; i < m_indices.length; ++i)
        {
            if(m_indices[i] == oldIndex)
            {
                m_indices[i] = newIndex;
            }
        }
    }
    
    public Object clone()
    {
        CameraView clone = new CameraView(m_scene, m_vertex, m_lookatVertex);
        clone.setIndices(m_indices);
        clone.m_selected = m_selected;
        return clone;
    }
}
