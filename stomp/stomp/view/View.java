package stomp.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.vecmath.*;

import stomp.FastVector;
import stomp.gui.Appearance;

/**
 * The View class is the superclass for both orthogonal and perspective
 * views.
 */
public abstract class View extends Canvas
implements MouseListener, MouseMotionListener, ComponentListener
{
    protected String m_text;
    protected Cursor m_selectCursor;
    protected Cursor m_pickCursor;
    protected Cursor m_moveCursor;
    protected Cursor m_grabCursor;
    protected Cursor m_northCursor;
    protected Cursor m_southCursor;
    protected Vector m_grid;
     
    protected boolean m_highlighted = false;
    protected static boolean m_changed = true;
    protected static int m_checkOut =4;
    protected static boolean m_fast = false;

    public View (String s)
    {
        m_text = new String(""+ s);
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        m_selectCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        m_pickCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        m_moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        m_grabCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        m_northCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
        m_southCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    }

    public View ()
    {
        m_text = new String ("Default");
        addMouseListener (this);
        addMouseMotionListener (this);
        m_selectCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        m_pickCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        m_moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }

    public void resetView()
    {
    }
    
    public void paint(Graphics g)
    {
        Dimension s = getSize();
        int height = Appearance.fontAscent();

        g.setColor(SystemColor.activeCaption);
        g.fillRect(0, 0, s.width - 1, height + 5);        
        g.setFont(Appearance.getFont());
        g.setColor(SystemColor.activeCaptionText);

        //draw the name of the view
        g.drawString(m_text, 5, height + 2);
        
        g.setColor(Color.black);
        g.drawRect(0, 0, s.width - 1, s.height - 1);
        //g.drawRect(1, 1, s.width - 3, s.height - 3);
        g.drawLine(0, height + 5, s.width - 1, height + 5);
    }
    
    int scaleMouseInput(int modifiers, int value)
    {
        if(modifiers == InputEvent.SHIFT_MASK)
        {
            return (int)(value/6.0);
        }

        return value;
    }

    public void mouseClicked(MouseEvent e)
    {
        requestFocus();
    }
    
    public void mouseEntered(MouseEvent e)
    {
//          m_highlighted = true;
//          ((View)this).paint(getGraphics());
    }
    
    public void mouseExited(MouseEvent e)
    {
//          m_highlighted = false;
//          ((View)this).paint(getGraphics());
    }
    
    public void mousePressed(MouseEvent e)
    {
    }
    
    public void mouseReleased(MouseEvent e)
    {
    }
    
    public void mouseDragged(MouseEvent e)
    {
    }
    
    public void mouseMoved(MouseEvent e)
    {
    }
    
    public Dimension getMinimumSize()
    {
        return new Dimension (200,200);
    }
    
    public Dimension getPreferredSize()
    {
        return new Dimension (400,300);
    }

    public void componentResized(ComponentEvent e)
    {
        m_changed = true;
        m_checkOut = 4;
        repaint();
    }
    
    public void componentMoved(ComponentEvent e)
    {
        m_changed = true;
        m_checkOut = 4;
        repaint();
    }

    public void componentShown(ComponentEvent e)
    {
        m_changed = true;
        m_checkOut = 4;
        repaint();
    }

    public void componentHidden(ComponentEvent e)
    {
        m_changed = true;
        m_checkOut = 4;
        repaint();
    }
}





