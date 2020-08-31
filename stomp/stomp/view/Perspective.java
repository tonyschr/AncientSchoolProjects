package stomp.view;

import stomp.*;
import stomp.data3d.*;
import java.awt.*;
import java.lang.*;
import java.util.*;
import javax.vecmath.*;
import java.awt.event.*;

import stomp.transform.*;
import stomp.gui.Appearance;

/**
 * Perspective view is a 3D wireframe perspective view of the scene.
 *
 * When the user rotates and zooms, the world is rotated and scaled
 * instead of moving the camera.
 */
public class Perspective extends View
{
    public static final Color BACKGROUND = Color.lightGray;
    public static int VERTICES_BUTTON_WIDTH;
    public static int GRIDSIZE = 50;
    public static int GRIDMAX = 50;
    private static Color m_axisColor = new Color(220, 220, 220);

    private FastVector m_transformedVertices = new FastVector();

    protected boolean m_drawVertices = true;
    protected boolean m_drawPrimitives = true;
    
    //Camera parameters
    protected Vector3f m_VRP;
    protected Vector3f m_lookat;
    protected Vector3f m_VUP;
    protected Vector3f m_PRP;

    //Double buffering stuff
    protected Image offScreenImage;
    protected Dimension offScreenSize;
    protected Graphics offScreenGraphics;
    protected Scene m_scene;

    //temp variables
    protected Point m_origin;
    protected Vector3f m_holdVRP;

    //Matrices for doing the projection
    protected Matrix4f m_device;
    protected Matrix4f m_persMatrix;
    protected Matrix4f m_scaleRotate;
    protected Matrix4f m_viewTransform = new Matrix4f();

    //Zoom values
    protected float m_zoom = 2;
    protected float m_holdZoom = m_zoom;

    //Rotate values
    protected float m_rotateY = (float)Math.PI/16.0f;
    protected float m_holdRotateY = m_rotateY;
    protected float m_rotateX = (float)Math.PI/8.0f;
    protected float m_holdRotateX = m_rotateX;
    protected float m_rotateZ = 0;
    protected float m_holdRotateZ = m_rotateZ;

    //Pan values
    protected Vector3f m_pan = new Vector3f(0, -3, 0);
    protected Vector3f m_holdPan = new Vector3f(0, 0, 0);

    //Scale, move, and rotate matrices
    protected transient Matrix4f scale = new Matrix4f();
    protected transient Matrix4f rotate1 = new Matrix4f();
    protected transient Matrix4f rotate2 = new Matrix4f();
    protected transient Matrix4f rotate3 = new Matrix4f();
    protected transient Matrix4f pan = new Matrix4f();
    protected transient Vector3f n = new Vector3f();

    //The 3D axis
    protected Point4f m_transOrigin = new Point4f();
    protected Point4f m_transxAxis = new Point4f();    
    protected Point4f m_transyAxis = new Point4f();
    protected Point4f m_transzAxis = new Point4f();

    //The points for the 3D grid
    protected Point4f m_xPoints1[] = new Point4f[GRIDMAX + 1];
    protected Point4f m_xPoints2[] = new Point4f[GRIDMAX + 1];
    protected Point4f m_zPoints1[] = new Point4f[GRIDMAX + 1];
    protected Point4f m_zPoints2[] = new Point4f[GRIDMAX + 1];
    
    private int m_sceneSize = 0;
    
    double window_lft,window_rght, window_top, window_btm;
    double hold_lft, hold_rght, hold_btm, hold_top;
    protected boolean m_localChange = false;

    private boolean m_localFast = false;
    
    /**
     * Initialize the view and setup initial matrices.
     */
    public Perspective (String s, Scene scene)
    {
        super(s);
        //setup GUI stuff
        VERTICES_BUTTON_WIDTH = Appearance.getSmallFontMetrics().stringWidth("Vertices") + 10;

        //setup viewing parameters    
        m_PRP = new Vector3f (0,0,12);
        m_VRP = new Vector3f (0,0,15);
        m_VUP = new Vector3f (0,1,0);
        m_lookat = new Vector3f (0,0,0);
        m_scene = scene;
        Dimension size = getSize();
        window_lft = -size.width/100.0f;
        window_btm = -size.height/100.0f;
        window_top = size.height/100.0f;
        window_rght = size.width/100.0f;

        //Deterimine points for the view.
        for(int i = 0; i < GRIDMAX + 1; ++i)
        {
            m_xPoints1[i] = new Point4f();
            m_xPoints2[i] = new Point4f();
            m_zPoints1[i] = new Point4f();
            m_zPoints2[i] = new Point4f();
        }
        
        m_persMatrix = setupPerspective();
        m_scaleRotate = setupScaleRotate();
        m_device = makedevice(size.width,size.height);
        m_viewTransform.mul(m_device, m_persMatrix);
        m_viewTransform.mul(m_scaleRotate);

        //Set the initial grid size.  Duplicate from zoom.
        int tempgrid = (int)(30 / m_zoom);
        if(tempgrid < 50)
        {
            GRIDSIZE = (tempgrid/2) * 2;
        }
    }
    
    /**
     * Reset the 3D view to the default values.
     */
    public void resetView()
    {
        m_zoom = 2;
        int tempgrid = (int)(30 / m_zoom);
        if(tempgrid < 50)
        {
            GRIDSIZE = (tempgrid/2) * 2;
        }
        m_holdZoom = m_zoom;
        m_rotateY = (float)Math.PI/16.0f;
        m_holdRotateY = m_rotateY;
        m_rotateX = (float)Math.PI/8.0f;
        m_holdRotateX = m_rotateX;
        m_rotateZ = 0;
        m_holdRotateZ = m_rotateZ;
        m_pan = new Vector3f(0, -3, 0);
        m_holdPan = new Vector3f(0, 0, 0);
        m_persMatrix = setupPerspective();
        m_localChange = true;
        repaint();
    }

    /**
     * Overload update to provide double buffering
     */
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

    /**
     * Draw the view.  Don't recompute the perspective pipeline.
     */
    public void paint(Graphics g)
    {
        Dimension size = getSize();
        int height = Appearance.fontAscent();
        
        //Set the 2d clip bounds to the size of the viewport.
        SutherlandHodgman.setClipBounds(0, size.height,
                                        0, size.width);

        //Draw gray background
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, size.width, size.height);

        if(m_localChange)
        {
            m_scaleRotate = setupScaleRotate();
            m_viewTransform.mul(m_device, m_persMatrix);
            m_viewTransform.mul(m_scaleRotate);
        
            m_localChange = false;
        }

        //Do the transformations
        transformVertices();

        //Draw the grid
        g.setColor(m_axisColor);
        Point3d p0 = new Point3d();
        Point3d p1 = new Point3d();
        for(int i = 0; i < GRIDSIZE+1; ++i)
        {
            g.drawLine((int)m_xPoints1[i].x, (int)m_xPoints1[i].y,
                       (int)m_xPoints2[i].x, (int)m_xPoints2[i].y);
            g.drawLine((int)m_zPoints1[i].x, (int)m_zPoints1[i].y,
                       (int)m_zPoints2[i].x, (int)m_zPoints2[i].y);
        }

        //Draw the axis
        g.setColor(Color.white);
        g.drawLine((int)m_transOrigin.x, (int)m_transOrigin.y,
                   (int)m_transxAxis.x, (int)m_transxAxis.y);
        g.drawLine((int)m_transOrigin.x, (int)m_transOrigin.y,
                   (int)m_transyAxis.x, (int)m_transyAxis.y);
        g.drawLine((int)m_transOrigin.x, (int)m_transOrigin.y,
                   (int)m_transzAxis.x, (int)m_transzAxis.y);

        g.setColor(Color.black);
        
        //Paint the non-selected ones only (speed hazard)
        FastVector primitives = m_scene.getPrimitivesVector();
        Primitive temp;
        int decrement = 1;
        if(m_fast || m_localFast)
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
            int decrement2 = 1;
            if(m_fast || m_localFast)
            {
                decrement2 = 1 + (int)(primitives.sizeFast()/(Mode.DISAPPEAR_THRESHHOLD*2));
            }
            for(int i = selectedPrimitives.sizeFast()-1; i >= 0; i-=decrement2)
            {
                temp = (Primitive)selectedPrimitives.elementAtFast(i);
                temp.paint(m_transformedVertices, g);
                
                if(!m_fast && temp instanceof Polygon3d)
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
//          g.setColor(BACKGROUND);
//          g.fillRect(size.width - VERTICES_BUTTON_WIDTH - 5, 2,
//                     VERTICES_BUTTON_WIDTH, height);
//          g.draw3DRect(size.width - VERTICES_BUTTON_WIDTH - 5, 2,
//                     VERTICES_BUTTON_WIDTH, height, m_drawVertices);
//          g.setFont(Appearance.getSmallFont());
//          g.setColor(Color.black);
//          g.drawString("Vertices", size.width - VERTICES_BUTTON_WIDTH,
//                       height);
    }

    /**
     * Transform the vertices with the perspective projection matrix.
     */
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

//              SutherlandHodgman.clip3d(m_xPoints1[i], m_xPoints2[i]);
//              SutherlandHodgman.clip3d(m_zPoints1[i], m_zPoints2[i]);
        }
    }

    /**
     * Setup the perspective matrix
     */
    Matrix4f setupPerspective()
    {
        float  Back, Front;
        float avrgwidth_window, avrghght_window, shx, shy;
        
        avrgwidth_window = (float)(window_lft+window_rght)/2;
        avrghght_window = (float)(window_top+window_btm)/2;
        
        Front = 1;
        Back = -5;

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

    /**
     * Setup the matrices to scale and rotate the world.
     */
    Matrix4f setupScaleRotate()
    {
        //Create scale and rotation matrices for ZOOM and ROTATE modes
        scale.set(m_zoom);

        rotate1.setIdentity();
        rotate1.m00 = (float)Math.cos(m_rotateY);
        rotate1.m02 = (float)-Math.sin(m_rotateY);
        rotate1.m20 = (float)Math.sin(m_rotateY);
        rotate1.m22 = (float)Math.cos(m_rotateY);

        rotate2.setIdentity();
        rotate2.m11 = (float)Math.cos(m_rotateX);
        rotate2.m12 = (float)-Math.sin(m_rotateX);
        rotate2.m21 = (float)Math.sin(m_rotateX);
        rotate2.m22 = (float)Math.cos(m_rotateX);

        rotate3.setIdentity();
        rotate3.m00 = (float)Math.cos(m_rotateZ);
        rotate3.m01 = (float)-Math.sin(m_rotateZ);
        rotate3.m10 = (float)Math.sin(m_rotateZ);
        rotate3.m11 = (float)Math.cos(m_rotateZ);
        
        pan.setIdentity();
        pan.setTranslation(m_pan);

        rotate1.mul(rotate3);
        rotate2.mul(rotate1);
        scale.mul(rotate2);
        pan.mul(scale);

        return pan; //scale; //scale and rotate world
    }

    /**
     * Create device matrix
     */
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
    
    public void mousePressed(MouseEvent e)
    {
        m_localFast = true;
        m_origin = e.getPoint();
        m_holdZoom = m_zoom;
        
        m_holdRotateY = m_rotateY;
        m_holdRotateX = m_rotateX;
        m_holdRotateZ = m_rotateZ;

        m_holdPan.x = m_pan.x;
        m_holdPan.y = m_pan.y;
    }
    
    public void mouseReleased(MouseEvent e)
    {
        
        m_localFast = false;
        m_localChange = true;
        repaint();
    }
    
    public void mouseDragged(MouseEvent e)
    {
        m_localFast = true;
        Point m_distance = e.getPoint();
        float diffx, diffy;

        if(Mode.getMode() == Mode.ZOOM)
        {
            double tempZoom = m_zoom;
            diffy =  m_origin.y - m_distance.y;

            tempZoom = m_holdZoom * Math.pow(1.01, diffy);
            if(tempZoom < .000001)
            {
                tempZoom = .000001;
            }
            else if(tempZoom > 1000000)
            {
                tempZoom = 1000000;
            }

            m_zoom = (float)tempZoom;
            int tempgrid = (int)(30 / m_zoom);
            if(tempgrid < 50)
            {
                GRIDSIZE = (tempgrid/2) * 2;
            }
            m_localChange = true;
            
        }
        else if(Mode.getMode() == Mode.PAN)
        {
            diffx = m_distance.x - m_origin.x;
            diffy = m_distance.y - m_origin.y;

            float amountY = -diffy/20.0f;
            float amountX = diffx/20.0f;

            m_pan.x = m_holdPan.x + amountX;
            m_pan.y = m_holdPan.y + amountY;

            m_localChange = true;
        }
        else
        {
            diffx = m_distance.x - m_origin.x;
            diffy = m_distance.y - m_origin.y;

            float angleY = -diffx/60.0f;
            float angleX = diffy/60.0f;
            
            if(e.getModifiers() == InputEvent.BUTTON2_MASK ||
               e.getModifiers() == InputEvent.BUTTON3_MASK)
            {
                m_rotateZ = m_holdRotateZ + angleY;
            }
            else
            {  
                m_rotateX = m_holdRotateX + angleX;
                m_rotateY = m_holdRotateY + angleY;
            }
            
            m_localChange = true;
        }
        
        repaint();
    }

    /**
     * When the component is resized, we need to recompute the
     * entire projection pipeline for the new window size so the
     * image does not get squished.
     */
    public void componentResized(ComponentEvent e)
    {
        Dimension size = getSize();
        window_lft = -size.width/100.0f;
        window_btm = -size.height/100.0f;
        window_top = size.height/100.0f;
        window_rght = size.width/100.0f;
        m_persMatrix = setupPerspective();
        m_scaleRotate = setupScaleRotate();
        m_device = makedevice(size.width,size.height);
        m_viewTransform.mul(m_device, m_persMatrix);
        m_viewTransform.mul(m_scaleRotate);
        repaint();
    }

    public void mouseEntered(MouseEvent e)
    {
        super.mouseEntered(e);

        if(Mode.getMode() == Mode.PAN || Mode.getMode() == Mode.ZOOM)
        {
            setCursor(m_moveCursor);
        }
    }

    public void mouseExited(MouseEvent e)
    {
        super.mouseExited(e);
        setCursor(m_pickCursor);
    }
}
