package stomp.gui;

import java.awt.*;
import java.awt.event.*;

import stomp.view.*;

/**
 * ViewContainer contains all of the four views (TOP, FRONT, SIDE,
 * and PERSPECTIVE).  This class makes it easy to coordinate updating
 * of the views and also switching exclusively between one or all
 * four views.
 *
 * In the future, this class may be extended to allow users to resize
 * and rearrange the views.
 */
public class ViewContainer extends Panel implements Runnable
{
    public static int UPDATE_DELAY = 100;
    public final static int ALL = 0;
    public final static int TOP = 1;
    public final static int FRONT = 2;
    public final static int SIDE = 3;
    public final static int PERSPECTIVE = 4;
    public final static int CAMERA = 5;
    
    private boolean m_changed = true;
    private int m_viewMode;

    private View m_top;
    private View m_front;
    private View m_side;
    private View m_perspective;
    private View m_preview;
    private View m_camera;

    private Thread m_thread;

    /**
     * Constructor.  Needs all four views, hard coded.
     */
    public ViewContainer(View top, View front, View side, View perspective)
    {
        m_top = top;
        m_front = front;
        m_side = side;
        m_perspective = m_preview = perspective;

        setView(ViewContainer.ALL);
        m_thread = new Thread(this);
        m_thread.setPriority(Thread.MIN_PRIORITY);
        
        m_thread.start();
    }

    public void setCamera(View camera)
    {
        m_camera = camera;
        if(camera == null)
        {
            setPerspective(ViewContainer.PERSPECTIVE);
        }
    }

    public void setPerspective(int mode)
    {
        if(mode == ViewContainer.PERSPECTIVE)
        {
            m_perspective = m_preview;
        }
        else if(mode == ViewContainer.CAMERA)
        {
            if(m_camera != null)
            {
                m_perspective = m_camera;
            }
        }

        setView(m_viewMode);
    }

    public void resetViews()
    {
        m_top.resetView();
        m_front.resetView();
        m_side.resetView();
        m_perspective.resetView();
    }
    
    /**
     * Set the ViewContainer to show a specific one of the four views
     * or all of them.
     *
     * @param view Use static member variables to set the view.
     */
    public void setView(int view)
    {
        m_viewMode = view;
        
        if(view == ViewContainer.ALL)
        {
            removeAll();
            LayoutManager layout = new GridLayout(2,2);
            setLayout(layout);
            add(m_top);
            add(m_perspective);
            add(m_front);
            add(m_side);
            layout.layoutContainer(this);
        }
        else if(view == ViewContainer.TOP)
        {
            removeAll();
            LayoutManager layout = new GridLayout(1,1);
            setLayout(layout);
            add(m_top);
            layout.layoutContainer(this);
        }
        else if(view == ViewContainer.FRONT)
        {
            removeAll();
            LayoutManager layout = new GridLayout(1,1);
            setLayout(layout);
            add(m_front);
            layout.layoutContainer(this);
        }
        else if(view == ViewContainer.SIDE)
        {
            removeAll();
            LayoutManager layout = new GridLayout(1,1);
            setLayout(layout);
            add(m_side);
            layout.layoutContainer(this);
        }
        else if(view == ViewContainer.PERSPECTIVE)
        {
            removeAll();
            LayoutManager layout = new GridLayout(1,1);
            setLayout(layout);
            add(m_perspective);
            layout.layoutContainer(this);
        }

        super.repaint();
        repaint();
    }

    /**
     * Gets the preferred size of the ViewContainer.
     */
    public Dimension getPreferredSize()
    {
        return new Dimension(800, 600);
    }

    /**
     * Gets the minimum size of the ViewContainer.  It won't resize after
     * the window gets smaller than this.
     */
    public Dimension getMinimumSize()
    {
        return new Dimension(400,400);
    }

    /**
     * A change has been made to the viewport.  This is how
     * the lazy-updating works.
     */
    public void setChanged()
    {
        m_changed = true;
    }

    public void forceRepaint()
    {
        repaint();
        //m_changed = false;
//          Graphics g = getGraphics();
//          if(g == null)
//          {
//              return;
//          }
        
//          if(m_viewMode == ALL)
//          {
//              m_top.paint(g);
//              m_front.paint(g);
//              m_side.paint(g);
//              m_perspective.paint(g);
//          }
//          else if(m_viewMode == TOP)
//          {
//              m_top.paint(g);
//          }
//          else if(m_viewMode == FRONT)
//          {
//              m_front.paint(g);
//          }
//          else if(m_viewMode == SIDE)
//          {
//              m_side.paint(g);
//          }
//          else if(m_viewMode == PERSPECTIVE)
//          {
//              m_perspective.paint(g);
//          }
    }
    
    /**
     * Repaint all of the views.
     */
    public void repaint()
    {
        //super.repaint();

        if(m_viewMode == ALL)
        {
            m_thread.yield();
            m_top.repaint();
            m_thread.yield();
            m_front.repaint();
            m_thread.yield();
            m_side.repaint();
            m_thread.yield();
            m_perspective.repaint();
        }
        else if(m_viewMode == TOP)
        {
            m_thread.yield();
            m_top.repaint();
        }
        else if(m_viewMode == FRONT)
        {
            m_thread.yield();
            m_front.repaint();
        }
        else if(m_viewMode == SIDE)
        {
            m_thread.yield();
            m_side.repaint();
        }
        else if(m_viewMode == PERSPECTIVE)
        {
            m_thread.yield();
            m_perspective.repaint();
        }
    }

    /**
     * Run simply loops forever and updates all of the views after a
     * short delay after a change has been made to a specific view.
     */
    public void run()
    {
        //Loop forever.
        while(true)
        {
            try
            {
                if(m_changed)
                {
                    m_changed = false;
                    m_thread.yield();
                    repaint();
                }
                m_thread.yield();
                m_thread.sleep(UPDATE_DELAY); //REDRAW_DELAY, Sleep
            }
            catch(InterruptedException e)
            {
            }
        }
    }
}

