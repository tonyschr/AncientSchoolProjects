package stomp.view;

import java.math.*;
import stomp.*;
import stomp.data3d.*;
import java.awt.*;
import java.lang.*;
import java.util.*;
import javax.vecmath.*;
import java.awt.event.*;

import stomp.FastVector;
import stomp.transform.*;
import stomp.gui.Appearance;
import stomp.command.*;

/**
 * Orthogonal class
 * Represents a viewport that is an orthogonal projection of the objects
 */
public class Orthogonal extends View implements Observer
{
    //Constants
    private final int DRAWDELAY = 200;  //delay in DRAW_VERTICES mode
    private final Color m_regionBoxColor = new Color(0, 100, 0);

    //Reference back to the scene
    protected Scene m_scene;

    private FastVector m_transformedVertices = new FastVector();
    
    //Temporary variables indicating mode/state
    //    private static boolean m_fast = false;
    protected long m_drawVerticesDelay = 0;
    protected int m_sceneSize = 0;
    protected int m_mode;

    //choices for direction
    protected boolean m_xDir = false;
    protected boolean m_yDir = false;
    protected boolean m_zDir = false;

    //Selection temp variables
    protected Point m_first;
    protected Point m_second;
    protected Point m_secondOld;
    protected boolean m_regionSelect = false;
    Vector4f m_statusMouseCoord = new Vector4f();
    
    //View transformation stuff
    protected Matrix4f projectToWindow;
    protected Matrix4f m_scaleTranslate;
    protected Matrix4f m_inverseMatrix = null;
    Matrix4f m_viewTransform = new Matrix4f();
    protected double m_scale;
    protected Point m_origin;
    protected Point m_magnitude;
    protected Vector3d m_modePan;
    protected double m_modeZoom;
    protected double m_zoomStart;

    //center of window
    protected Vector3f m_cw;

    //The grid
    protected Grid m_grid = new Grid();

    //Transformation for move, rotate, scale, etc.
    protected Transformation m_transformation = null;

    //Stuff needed for double-buffering
    private Image offScreenImage;
    private Dimension offScreenSize;
    private Graphics offScreenGraphics;

    //Add double-buffering to the paint method to allow smooth graphics
    public final synchronized void update (Graphics g) 
    {
        Dimension d = getSize();
        
        if((offScreenImage == null) ||
           (d.width != offScreenSize.width) ||
           (d.height != offScreenSize.height)) 
        {
            offScreenImage = createImage(d.width, d.height);
            offScreenSize = d;
            offScreenGraphics = offScreenImage.getGraphics();
        }
        paint(offScreenGraphics);
        g.drawImage(offScreenImage, 0, 0, null);
    }
    
    /**
     * Orthogonal constructor: creates an orthogonal viewport
     *
     *@param s the descriptive string for this viewport
     *@param x a boolean value that determines if this view shows the x dir
     *@param y a boolean value that determines if this view shows the y dir
     *@param z a boolean value that determines if this view shows the z dir
     * Only two of the above parameters can be true at one time
     *@param scene the scene object, allows the view to communicate with
     * the major data objects of Stomp
     */
    public Orthogonal (String s, boolean x, boolean y, boolean z,
                       Scene scene)
    {
        super(s);
        m_scene = scene;
        Mode.getActualMode().addObserver(this);

        m_xDir = x;
        m_yDir = y;
        m_zDir = z;

        if ( m_xDir && m_yDir && m_zDir)
        {
            System.out.println("Can't have all three directions");
            System.out.println("Defaulting to X Y direction");
            m_zDir = false;
        }
        
        //Create the orthogonal scale matrix, set to identity
        projectToWindow = new Matrix4f();
        projectToWindow.set(1);

        //If the xdir is false, then z must stand in for x, so
        //the scale in the x dir is set to zero
        if(!m_xDir)
        {
            projectToWindow.m00 = 0;
            projectToWindow.m02 = 1;
        }
        
        //If the ydir is false, then z must stand in for y, so
        //the scale in the y dir is set to zero
        if(!m_yDir)
        {
            projectToWindow.m11 = 0;
            projectToWindow.m12 = 1;
        }
        
        //m_scaleTranslate is the scale and translation matrix
        //for zooming and panning
        m_scaleTranslate = new Matrix4f();
        m_inverseMatrix = new Matrix4f();
        m_scaleTranslate.setIdentity();

        m_inverseMatrix.invert(m_scaleTranslate);

        m_origin = new Point();
        m_magnitude = new Point();
    }

    /**
     * Transform all of the vertices from the scene into a local list
     * of vertices.  This need to be as fast as possible.
     */
    private final void transformVertices()
    {
        //This will have to change once vertices can be deleted, but
        //that should be easy.  Just trying to keep the size the same,
        //the vertices all get copied over anyway.
        FastVector sceneVertices = m_scene.getVerticesVector();
        int sceneSize = sceneVertices.sizeFast();

        //Make this scene's # of vertices match Scene
        if(m_sceneSize != sceneSize)
        {
            while(m_transformedVertices.sizeFast() < sceneSize)
            {
                m_transformedVertices.addElement(new Vertex());
            }
            while(m_transformedVertices.sizeFast() > sceneSize)
            {
                m_transformedVertices.removeElementAt(m_transformedVertices.sizeFast()-1);
            }
            m_sceneSize = sceneSize;
        }

        //Calculate the projection matrix
        m_viewTransform.mul(projectToWindow, m_scaleTranslate);

        //Do vertex transformations
        //This must be optimized to be as fast as possible
        Vertex dest;
        for(int i = 0; i < sceneSize; ++i)
        {
            dest = (Vertex)m_transformedVertices.elementAtFast(i);
            dest.set((Vertex)sceneVertices.elementAtFast(i));
            
            m_viewTransform.transform(dest);
        }
    }

    /**
     * makeViewport method sets the m_scaleTranslate matrix
     * @param size  the dimension of the window, used in case
     * the user has enlarged to this window
     */
    private final void makeViewport()
    {
        Dimension size = getSize();
        Vector3d pan = Mode.getPan();
        double x = (m_xDir)?(size.width/2)+pan.x:0;
        double y = (m_yDir)?(size.height/2)+pan.y:0;
        double z = pan.z+((m_zDir)?((m_xDir)?size.height/2:size.width/2):0);

        m_scaleTranslate.set((float)Mode.getZoom());
        m_scaleTranslate.setTranslation(new Vector3f((float)x,
                                                     (float)y,
                                                     (float)z));

        //Swap the Y axis
        m_scaleTranslate.m11 = -m_scaleTranslate.m11;
        m_inverseMatrix.invert(m_scaleTranslate);
    }
    
    /**
     *paint method performs the actual drawing of the window
     *@param g  the graphics context of this view
     */
    public void paint(Graphics g)
    {
        Dimension size = getSize();

        //Set the 2d clip bounds to the size of the viewport.
        SutherlandHodgman.setClipBounds(0, size.height,
                                        0, size.width);
        
        //Draw the grid
        m_grid.paint(g, this);

        makeViewport();
        transformVertices();

        //Draw an "axis" in the lower left of the view
        g.setColor(Color.black);
        g.drawLine(10, size.height - (size.height/10), 10, size.height-10);
        g.drawLine(10, size.height-10, size.height/10, size.height -10);
        g.setFont(Appearance.getFont());
        
        //Depending on the current ortho settings, draw the
        //corresponding axis variable
        if(m_yDir)
        {
            g.drawString ("y",5,size.height - (size.height/10) -
                          Appearance.fontDescent());
        }
        if(m_xDir)
        {
            g.drawString ("x",size.height/10, size.height-5);
        }
        if(m_xDir && m_zDir)
        {
            g.drawString ("z", 5, size.height - (size.height/10)-1);
        }
        else if (m_yDir && m_zDir)
        {
            g.drawString ("z", size.height/10, size.height-5);
        }

        //If the user is selecting a region, draw a selection box
        if(m_regionSelect)
        {
            int lowX = Math.min(m_first.x,m_second.x);
            int highX = Math.max(m_first.x,m_second.x);
            int lowY = Math.min(m_first.y,m_second.y);
            int highY = Math.max(m_first.y,m_second.y);
            //g.setXORMode(m_regionBoxColor);
            g.setColor(m_regionBoxColor);
            g.drawRect(lowX,lowY,highX-lowX,highY-lowY);
            //g.setPaintMode();
        }

        //Paint the non-selected primitives first
        FastVector primitives = m_scene.getPrimitivesVector();
        Primitive temp;

        //Depending on the number of primitives, choose to skip every
        //n (decrement) polygons
        int decrement = 1;
        if(m_fast)
        {
            decrement = 1 +
                (int)(primitives.sizeFast()/Mode.DISAPPEAR_THRESHHOLD);
        }

        //If primitives are selected, we can't ignore checking the
        //temp.isSelected().  If none are selected, optimize it away.
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
            //Paint the selected primitives (speed hazard)
            Vertex[] norm;
            for(int i = selectedPrimitives.sizeFast()-1; i >=0; i--)//=decrement)
            {
                temp = (Primitive)selectedPrimitives.elementAtFast(i);
                temp.paint(m_transformedVertices, g);
                
                if(m_fast == false && temp instanceof Polygon3d)
                {
                    norm = ((Polygon3d)temp).getNormalPoints();
                    
                    m_viewTransform.transform(norm[0]);
                    m_viewTransform.transform(norm[1]);
                    
                    ((Polygon3d)temp).paintNormal(norm, g);
                }
            }
        }
        else //only draw unselected, don't check either.
        {
            for(int i = primitives.sizeFast()-1; i >=0; i-=decrement)
            {
                ((Primitive)primitives.elementAtFast(i)).paint(m_transformedVertices, g);
            }
        }

        //If there are selected vertices, make sure to draw all of them
        //so the user can see what he is doing.
        if(m_scene.verticesSelected())
        {
            decrement = 1;
        }
        
        //Paint the vertices
        for(int i = m_transformedVertices.sizeFast()-1; i >= 0; i-=decrement)
        {
            ((Vertex)m_transformedVertices.elementAtFast(i)).paint(g);
        }

        //Hightlight goes over everything!
        super.paint(g);
    }

    /**
     *update method  called by the Mode object to allow
     *proper cursor depending on current mode
     */
    public void update(Observable o, Object arg)
    {
        m_mode = Mode.getMode();

        if(m_mode == Mode.PRIMITIVE_SELECT)
        {
            setCursor(m_selectCursor);
        }
        else if (m_mode == Mode.VERTEX_SELECT)
        {
            setCursor(m_selectCursor);
        }
        else if (m_mode == Mode.REGION_PRIMITIVE_SELECT ||
                 m_mode == Mode.REGION_VERTEX_SELECT)
        {
            setCursor(m_selectCursor);
        }
        else if (m_mode == Mode.OBJECT_SELECT)
        {
            setCursor(m_pickCursor);
        }
        else if (m_mode == Mode.ADD_POINTS ||
                 m_mode == Mode.ADD_POINTS_FREEDRAW)
        {
            setCursor(m_selectCursor);
        }
        else if (m_mode == Mode.ADD_PLANES)
        {
            setCursor(m_pickCursor);
        }
        else if (m_mode == Mode.PAN || m_mode == Mode.ZOOM)
        {
            setCursor(m_moveCursor);
        }
        else
        {
            setCursor(m_pickCursor);
        }
    }
    /**
     *Highlight the view, since the mouse entered it
     */
    public void mouseEntered(MouseEvent e)
    {
        super.mouseEntered(e);

        update(null, null);
    }

    /**
     *Unhighlight the view
     */
    public void mouseExited(MouseEvent e)
    {
        super.mouseExited(e);
        setCursor(m_pickCursor);
    }

    /**
     *currently not used
     */
    public void mouseClicked(MouseEvent e)
    {
        super.mouseClicked(e);
    }
    
    /**
     *the user has pressed the button, do the right thing depending
     *on current mode
     */
    public void mousePressed(MouseEvent e)
    {
        m_mode = Mode.getMode();
        int modifiers = e.getModifiers();
        
        //If the user is selecting, call the select method of
        //scene with the current point and the transformed
        //vertices of this view
        if(m_mode == Mode.VERTEX_SELECT || m_mode == Mode.PRIMITIVE_SELECT ||
           m_mode == Mode.OBJECT_SELECT)
        {
            CommandExecutor.execute(new SelectCommand(m_scene,
                                                      m_transformedVertices,
                                                      e.getX(), e.getY()));
        }
        //User is adding points
        else if(m_mode == Mode.ADD_POINTS)
        {
            float X,Y,Z;

            //Since the mouse only has two axis, determine which
            //axis the view is displaying, and copy the value into the
            //corresponding element (The mouse click is a ray originating
            //from the click
            X=Y=Z=0;
            if (m_xDir)
            {
                X = e.getX();
            }
            else
            {
                Z = e.getX();
            }
            if (m_yDir)
            {
                Y = e.getY();
            }
            else
            {
                Z = e.getY();
            }
            Vector4f transformMouseCoord = new Vector4f(X,Y,Z,1);

            //pump the resulting vertex back through the tranformation matrix
            //to get the actual world coordinate out
            m_inverseMatrix.transform(transformMouseCoord);
            CommandExecutor.execute(new AddVertexCommand(m_scene,
                                    new Vertex(transformMouseCoord.x,
                                               transformMouseCoord.y,
                                               transformMouseCoord.z)));
        }
        //If the user is selection a region, record the first point
        else if(m_mode == Mode.REGION_PRIMITIVE_SELECT ||
                m_mode == Mode.REGION_VERTEX_SELECT)
        {
            m_first = new Point();
            m_second = new Point();
            m_secondOld = new Point();
            m_first = e.getPoint();
            m_second = m_first;
            m_regionSelect = true;
        }
        //If the user is panning, record the first point for comparison
        else if(m_mode == Mode.PAN)
        {
            m_origin = e.getPoint();
            m_modePan = Mode.getPan();
        }
        //If the user is zooming, record the first point for comparison
        else if(m_mode == Mode.ZOOM)
        {
            m_origin = e.getPoint();
            m_modeZoom = Mode.getZoom();
        }
    }
    /**
     * MouseReleased method  finalizes the change initialized by mousepressed
     *@param e the mouse event passed in by the viewport
     */
    public void mouseReleased(MouseEvent e)
    {
        //If selecting a region, finalize the values of the select box,
        //and pass the results to Scene
        if(m_regionSelect)
        {
            m_second = e.getPoint();
            m_secondOld = m_second;

            //Find min/max bounds of bounding box
            int lowX = Math.min(m_first.x,m_second.x);
            int highX = Math.max(m_first.x,m_second.x);
            int lowY = Math.min(m_first.y,m_second.y);
            int highY = Math.max(m_first.y,m_second.y);

            m_regionSelect = false;
            CommandExecutor.execute(new SelectRegionCommand(m_scene,
                                                            m_transformedVertices,
                                                            lowX, highX,
                                                            lowY, highY));
        }
                
        m_fast = false;

        if(m_transformation != null)
        {
            CommandExecutor.addNoExecute(new TransformCommand(m_scene,
                                                    m_transformation));
        }

        m_transformation = null;

        m_scene.validateScene();
        //repaint();
        update(getGraphics());
    }

    /**
     * mouseDragged   for any modes that require a difference in mouse movement
     *@param e the MouseEvent passed by the view
     */
    public void mouseDragged(MouseEvent e)
    {
        int mode = Mode.getMode();

        //Set the second point of the select box to the current position
        //and redraw to show the current position
        if(m_regionSelect)
        {
            m_secondOld = m_second;
            m_second = e.getPoint();
        }
        //Add points at the current mouse position
        else if(mode == Mode.ADD_POINTS_FREEDRAW)
        {
            if(System.currentTimeMillis() > m_drawVerticesDelay)
            {
                float X,Y,Z;
                //                Dimension size = getSize();
                //makeViewport(size);

                X=Y=Z=0;
                if (m_xDir)
                {
                    X = e.getX();
                }
                else
                {
                    Z = e.getX();
                }
                if (m_yDir)
                {
                    Y = e.getY();
                }
                else
                {
                    Z = e.getY();
                }
                
                Vector4f transformMouseCoord = new Vector4f(X,Y,Z,1);
                m_inverseMatrix.transform(transformMouseCoord);
                CommandExecutor.execute(new AddVertexCommand(m_scene,
                                                new Vertex(
                                                transformMouseCoord.x ,
                                                transformMouseCoord.y,
                                                transformMouseCoord.z)));
                m_drawVerticesDelay = System.currentTimeMillis() + DRAWDELAY;
            }
        }
        //calculate the difference from the first point to determine how
        //much to move the center point
        else if(Mode.getMode() == Mode.PAN)
        {
            m_fast = true;
            int diffx;
            int diffy;
            Vector3d tempPan = new Vector3d(m_modePan);
            m_magnitude = e.getPoint();
            diffx = m_magnitude.x - m_origin.x;
            diffy = m_magnitude.y - m_origin.y;
            tempPan.x += (m_xDir)?diffx:0;
            tempPan.y += (m_yDir)?diffy:0;
            tempPan.z += (m_zDir)?((m_xDir)?diffy:diffx):0;
            //set the global pan variable
            Mode.setPan(tempPan);
        }
        //calculate the difference from the first point to determine how
        //much to zoom in or out
        else if(Mode.getMode() == Mode.ZOOM)
        {
            m_fast = true;
            int diffy;
            double tempZoom = Mode.getZoom();
            double oldZoom = tempZoom;
            m_magnitude = e.getPoint();
            diffy = (m_origin.y - m_magnitude.y);

            tempZoom = m_modeZoom * Math.pow(1.01, diffy);

            if(tempZoom < .00001)
            {
                tempZoom = .00001;
            }
            else if(tempZoom > 1000000)
            {
                tempZoom = 1000000;
            }
            
            double zoomDiff = tempZoom/oldZoom;

            //Set the new pan to zoom around center of window.
            Vector3d pan = Mode.getPan();
            pan.x *= zoomDiff;
            pan.y *= zoomDiff;
            pan.z *= zoomDiff;

            Mode.setZoom(tempZoom);
        }
        else
        {
            transformFromMode(e.getPoint());
        }

        if(!(m_mode == Mode.REGION_PRIMITIVE_SELECT ||
             m_mode == Mode.REGION_VERTEX_SELECT))
        {
            m_scene.updateViews();        
        }
        
        //repaint();
        update(getGraphics());
    }
    
    /**
     *mouseMoved  reports the current position of the mouse for display
     *on the status bar
     */
    public void mouseMoved(MouseEvent e)
    {
        float X=0, Y=0, Z=0;
        if (m_xDir)
        {
            X = e.getX();
        }
        else
        {
            Z = e.getX();
        }
        if (m_yDir)
        {
            Y = e.getY();
        }
        else
        {
            Z = e.getY();
        }

        m_statusMouseCoord.set(X,Y,Z,1);
        m_inverseMatrix.transform(m_statusMouseCoord);
        Mode.setMouse(m_statusMouseCoord.x,
                      m_statusMouseCoord.y,
                      m_statusMouseCoord.z);
    }
    
    /**
     * Finds out what the current mode is and calls the
     * appropriate method
     */
    public final void transformFromMode(Point mousePoint)
    {
        if(Mode.getMode() == Mode.TRANSLATE)
        {
            translate(mousePoint);
        }
        else if(Mode.getMode() == Mode.SCALE)
        {
            scale(mousePoint);
        }
        else if(Mode.getMode() == Mode.ROTATE)
        {
            rotate(mousePoint);
        }
        else if(Mode.getMode() == Mode.TAPER)
        {
            taper(mousePoint);
        }
        else if(Mode.getMode() == Mode.SHEAR)
        {
            shear(mousePoint);
        }
    }
    
    /**
     * Translate vertices/primitives
     *
     * @param mousePoint the second mouse position
     */
    public final void translate(Point mousePoint)
    {
        if(m_transformation != null)
        {
            Point second = mousePoint;
            Vector3f v1, v2;
            if(m_xDir && m_yDir)
            {
                v1 = new Vector3f(m_first.x, m_first.y, 0);
                v2 = new Vector3f(second.x, second.y, 0);
            }
            else if(m_xDir && m_zDir)
            {
                v1 = new Vector3f(m_first.x, 0, m_first.y);
                v2 = new Vector3f(second.x, 0, second.y);
            }
            else //if(m_yDir && m_zDir)
            {
                v1 = new Vector3f(0, m_first.y, m_first.x);
                v2 = new Vector3f(0, second.y, second.x);
            }
                
            v2.sub(v1);
            v2.y = -v2.y;
            
            v2.scale((float)(1/Mode.getZoom()));
            ((Translation)m_transformation).setTranslation(v2);
            m_scene.transformSelected(m_transformation);
        }
        else
        {
            m_first = mousePoint;
            m_transformation = new Translation();
            m_fast = true;
        }
    }

    /**
     * Rotate selected items vertices around a point
     *
     * @param mousePoint the second point
     */
    public final void rotate(Point mousePoint)
    {
        if(m_transformation != null)
        {
            Point second = mousePoint;
            float angle = -(second.x - m_first.x)/45.0f;
            
            if(m_xDir && m_yDir)
            {
                Vector4f transformMouseCoord = new Vector4f(m_first.x,
                                                            m_first.y,
                                                            0, 1);
                m_inverseMatrix.transform(transformMouseCoord);

                ((Rotation)m_transformation).
                    setRotation(transformMouseCoord.x,
                                transformMouseCoord.y,
                                transformMouseCoord.z,
                                Rotation.XY, angle);
            }
            else if(m_xDir && m_zDir)
            {
                Vector4f transformMouseCoord = new Vector4f(m_first.x,
                                                            0,
                                                            m_first.y, 1);
                m_inverseMatrix.transform(transformMouseCoord);

                ((Rotation)m_transformation).
                    setRotation(transformMouseCoord.x,
                                transformMouseCoord.y,
                                transformMouseCoord.z,
                                Rotation.XZ, angle);
            }
            else //if(m_yDir && m_zDir)
            {
                Vector4f transformMouseCoord = new Vector4f(0,
                                                            m_first.y,
                                                            m_first.x, 1);
                m_inverseMatrix.transform(transformMouseCoord);

                ((Rotation)m_transformation).
                    setRotation(transformMouseCoord.x,
                                transformMouseCoord.y,
                                transformMouseCoord.z,
                                Rotation.YZ, angle);
            }

            m_scene.transformSelected(m_transformation);
        }
        else
        {
            m_first = mousePoint;
            m_fast = true;
            m_transformation = new Rotation();
        }

    }
    /**
     *scale method used to determine the amount of scale of the vertices
     *@param mousePoint   compared to m_first to determine the
     *amount of growth or shrinkage of the vertices
     */
    public final void scale(Point mousePoint)
    {
        if(m_transformation != null)
        {
            Point second = mousePoint;
            Vector3f v1, v2;
            if(m_xDir && m_yDir)
            {
                v1 = new Vector3f(m_first.x, m_first.y, 0);
                v2 = new Vector3f(second.x, second.y, 0);
            }
            else if(m_xDir && m_zDir)
            {
                v1 = new Vector3f(m_first.x, 0, m_first.y);
                v2 = new Vector3f(second.x, 0, second.y);
                //v2.z = -v2.z;
            }
            else //if(m_yDir && m_zDir)
            {
                v1 = new Vector3f(0, m_first.y, m_first.x);
                v2 = new Vector3f(0, second.y, second.x);
            }

            v2.sub(v1);
            v2.y = -v2.y;

            //Fix slight
            if(m_xDir && m_zDir)
            {
                v2.z = -v2.z;
            }

            if(v2.length() > .01)
            {
                //                Dimension size = getSize();
                //makeViewport(size);
                //Matrix4f inverse = new Matrix4f();
                //m_inverseMatrix.invert(m_scaleTranslate);
                Vector4f transformMouseCoord = new Vector4f(v1.x,
                                                            v1.y,
                                                            v1.z, 1);
                m_inverseMatrix.transform(transformMouseCoord);

                ((Scale)m_transformation).setScale(transformMouseCoord.x,
                                                   transformMouseCoord.y,
                                                   transformMouseCoord.z,
                                                   v2.x/20.0f,
                                                   v2.y/20.0f,
                                                   v2.z/20.0f);
                
                m_scene.transformSelected(m_transformation);
            }
        }
        else
        {
            m_first = mousePoint;
            m_fast = true;
            m_transformation = new Scale();
        }
    }
    /**
     *shear method calculates the affine shear matrix
     *@param mousePoint used with m_first to skew the vertices in the
     *specified direction
     */
    public final void shear(Point mousePoint)
    {
        if(m_transformation != null)
        {
            Point second = mousePoint;
            Vector3f v1, v2;
            int axis;
            if(m_xDir && m_yDir)
            {
                v1 = new Vector3f(m_first.x, m_first.y, 0);
                v2 = new Vector3f(second.x, second.y, 0);
                axis = Shear.XY;
            }
            else if(m_xDir && m_zDir)
            {
                v1 = new Vector3f(m_first.x, 0, m_first.y);
                v2 = new Vector3f(second.x, 0, second.y);
                axis = Shear.XZ;
            }
            else //if(m_yDir && m_zDir)
            {
                v1 = new Vector3f(0, m_first.y, m_first.x);
                v2 = new Vector3f(0, second.y, second.x);
                axis = Shear.YZ;
            }

            v2.sub(v1);
            v2.y = -v2.y;

            if(v2.length() > .01)
            {
                //Dimension size = getSize();
                //makeViewport(size);
                //Matrix4f inverse = new Matrix4f();
                //m_inverseMatrix.invert(m_scaleTranslate);
                Vector4f transformMouseCoord = new Vector4f(v1.x,
                                                            v1.y,
                                                            v1.z, 1);
                m_inverseMatrix.transform(transformMouseCoord);
                
                ((Shear)m_transformation).setShear(transformMouseCoord.x,
                                                   transformMouseCoord.y,
                                                   transformMouseCoord.z,
                                                   axis,
                                                   v2.x/20.0f,
                                                   v2.y/20.0f,
                                                   v2.z/20.0f);

                m_scene.transformSelected(m_transformation);
            }
        }
        else
        {
            m_first = mousePoint;
            m_fast = true;
            m_transformation = new Shear();
        }
    }

    /**
     *taper   calculates the non affine taper, or keystoning
     *@param mousePoint used with m_first to determine the amount of taper
     */
    public final void taper(Point mousePoint)
    {
        if(m_transformation != null)
        {
            Point second = mousePoint;
            Vector3f v1, v2;
            if(m_xDir && m_yDir)
            {
                v1 = new Vector3f(m_first.x, m_first.y, 0);
                v2 = new Vector3f(second.x, second.y, 0);
            }
            else if(m_xDir && m_zDir)
            {
                v1 = new Vector3f(m_first.x, 0, m_first.y);
                v2 = new Vector3f(second.x, 0, second.y);
            }
            else //if(m_yDir && m_zDir)
            {
                v1 = new Vector3f(0, m_first.y, m_first.x);
                v2 = new Vector3f(0, second.y, second.x);
            }

            Vector3f mouse = new Vector3f();
            mouse.set(v2);
            v2.sub(v1);
            v2.y = -v2.y;

            if(v2.length() > .01)
            {
                //                Dimension size = getSize();
                //makeViewport(size);
                //Matrix4f inverse = new Matrix4f();
                //m_inverseMatrix.invert(m_scaleTranslate);
                Vector4f transformMouseCoord = new Vector4f(v1.x,
                                                            v1.y,
                                                            v1.z, 1);
                Vector4f currentMouseCoord = new Vector4f(mouse.x,
                                                          mouse.y,
                                                          mouse.z, 1);
                m_inverseMatrix.transform(transformMouseCoord);
                m_inverseMatrix.transform(currentMouseCoord);
                ((Taper)m_transformation).setTaper(transformMouseCoord.x,
                                                   transformMouseCoord.y,
                                                   transformMouseCoord.z,
                                                   currentMouseCoord.x,
                                                   currentMouseCoord.y,
                                                   currentMouseCoord.z,
                                                   v2.x/20.0f,
                                                   v2.y/20.0f,
                                                   v2.z/20.0f);
                
                m_scene.transformSelected(m_transformation);
            }
        }
        else
        {
            m_first = mousePoint;
            m_transformation = new Taper();
        }
    }
}
