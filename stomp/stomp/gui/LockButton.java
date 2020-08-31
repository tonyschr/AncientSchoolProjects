package stomp.gui;

import java.awt.*;
import java.awt.event.*;

/**
 * LockButton
 * creates a clickable button that maintains a boolean type of state
 * When clicked, listens only for the MOUSE_PRESSED Event, which triggers
 * the state and redraws the button in the down mode.  When clicked a
 * second time, returns the state to the original state, and resets the
 * button to the unpressed mode
 */
public class LockButton extends Canvas implements MouseListener, MouseMotionListener
{
    private final static Color m_unSelected = new Color(0, 0, 0);
    private final static Color m_selected = new Color(255, 255, 255);
    private String m_text;
    private boolean m_state;
    private boolean m_mouseIn = false;
    private Dimension m_size;
    private ActionListener m_actionListener;
    
    public LockButton (String s)
    {
        super();
        m_text = s;
        m_state = false;
        m_size = new Dimension(Appearance.getFontMetrics().stringWidth(m_text)+
                               10, Appearance.fontHeight() + 8);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public LockButton (String s, int width, int height)
    {
        super();
        m_text = s;
        m_state = false;
        m_size = new Dimension(width, height);
        addMouseListener(this);
    }

    public void addActionListener(ActionListener listener)
    {
        m_actionListener = listener;
    }
    
    /**
     *  Draws the button depending on the button's state
     *  Uses system colors to maintain the look of the application.
     */      
    public void paint(Graphics g)
    {
        Dimension size = getSize();
        int x = (size.width -
                 Appearance.getFontMetrics().stringWidth(m_text))/2;
        int y = size.height/2 + Appearance.fontAscent()/2;
        if (!m_state)
        {
            g.setColor (SystemColor.control);
            g.fill3DRect(0, 0, size.width, size.height, true);
        }
        else
        {
            g.setColor ((SystemColor.control).darker());
            g.fill3DRect (0, 0, size.width, size.height, false);
        }
        if(m_mouseIn)
        {
            g.setColor(m_selected);
        }
        else
        {
            g.setColor(m_unSelected);
        }
        g.drawString (m_text, x, y);
    } 

    public boolean isSelected()
    {
        return m_state;
    }

    public void setSelected(boolean select)
    {
        m_state = select;
        repaint();
    }
    
    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
        m_mouseIn = true;
        repaint();
    }

    public void mouseExited(MouseEvent e)
    {
        m_mouseIn = false;
        repaint();
    }

    public void mousePressed(MouseEvent e)
    {
//         m_state = m_state == false;

        if(m_actionListener != null)
        {
            m_actionListener.actionPerformed(new ActionEvent(this, 0, m_text, 0));
        }
        
        repaint();
    }

    public void mouseReleased(MouseEvent e)
    {
    }
    
    public Dimension getPreferredSize()
    {
        return m_size;
    }

    public void mouseDragged(MouseEvent e)
    {
    }
    
    public void mouseMoved(MouseEvent e)
    {
    }

    public Dimension getMinimumSize()
    {
        return m_size;
    }

    public void validate()
    {
        super.validate();
        repaint();
    }
}


