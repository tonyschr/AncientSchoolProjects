package stomp.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

import stomp.Mode;

/**
 * Show the current status so the user knows what is happening.
 * This component should be added so that it is at the bottom of
 * the window.
 */
public class StatusBar extends Canvas implements Observer
{
    private String m_modeText = "Welcome to STOMP";
    private NumberFormat m_numberFormat;
    private int m_width;

    //Progress bar
    private double m_progress = 0;
    private double m_progressInc;
    
    //Stuff needed for double-buffering
    private Image offScreenImage;
    private Dimension offScreenSize;
    private Graphics offScreenGraphics;
    private String X, Y, Z;

    /**
     * StatusBar constructor.
     */
    public StatusBar()
    {
        m_numberFormat = NumberFormat.getInstance();
        m_numberFormat.setMaximumFractionDigits(4);
        m_numberFormat.setMinimumFractionDigits(4);
        FontMetrics fm = Appearance.getFontMetrics();
        m_width = fm.stringWidth("X: -00000.0000  Y: -00000.0000  Z: -00000.0000 ");
    }

    //Add double-buffering to the paint method to allow smooth graphics
    public final synchronized void update (Graphics g) 
    {
        Dimension d = getSize();;
        
        if((offScreenImage == null) || (d.width != offScreenSize.width)
           ||  (d.height != offScreenSize.height)) 
        {
            offScreenImage = createImage(d.width, d.height);
            offScreenSize = d;
            offScreenGraphics = offScreenImage.getGraphics();
        }
        paint(offScreenGraphics);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    /**
     * Redraw the status bar.
     */
    public void paint(Graphics g)
    {
        //Get the size of the status bar and draw an depressed box
        //around the border.
        Dimension size = getSize();
        g.setColor(SystemColor.control);
        g.fillRect(0, 0, size.width, size.height);

        g.setColor(SystemColor.controlLtHighlight);
        g.drawLine(size.width-2, 2, size.width-2, size.height-1);
        g.drawLine(2, size.height-1, size.width, size.height-1);

        g.drawLine(m_width, 2, m_width, size.height-1);
        
        g.setColor(SystemColor.controlShadow);
        g.drawLine(2, 2, size.width-2, 2);
        g.drawLine(2, 2, 2, size.height-1);
        
        g.drawLine(m_width+1, 2, m_width+1, size.height-1);

        //Draw the progress bars
        if(m_progress > 0)
        {
            g.setColor(SystemColor.controlLtHighlight);
            g.fillRect(m_width+2, 3, (int)((size.width - m_width) * m_progress/100.0),
                       size.height - 4);
        }
        
        //Draw the text in the status bar.
        g.setColor(Color.black);
        g.setFont(Appearance.getFont());
        
        X = m_numberFormat.format(Mode.getX());
        Y = m_numberFormat.format(Mode.getY());
        Z = m_numberFormat.format(Mode.getZ());
        
        g.drawString("X: " + X +
                     "  Y: " + Y +
                     "  Z: " + Z, 6, Appearance.fontAscent() + 3);
        g.drawString(m_modeText, m_width + 6, Appearance.fontAscent() + 3);
    }

    public Dimension getMinimumSize()
    {
        return new Dimension(200, Appearance.fontHeight() + 10);
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(400, Appearance.fontHeight() + 4);
    }

    public void startProgress(String message, double increment)
    {
        setMessage(message);
        m_progress = 1;
        m_progressInc = increment;
    }

    public void incrementProgress()
    {
        if(m_progress > 0)
        {
            m_progress += m_progressInc;
            if(m_progress >= 100)
            {
                m_progress = 0;
                setMessage("");
            }
            else if((int)m_progress % 5 == 0)
            {
                update(getGraphics());
            }
        }
    }
    
    private void setMessage(String message)
    {
        m_modeText = message;
        update(getGraphics());
    }

    public void update(Observable obj, Object args)
    {
        int mode = Mode.getMode();

        if(mode == Mode.NONE)
        {
            setMessage("");
        }
        else if(mode == Mode.VERTEX_SELECT)
        {
            setMessage("Select Vertices: Click on vertex to select it; click a selected vertex to deselect.");
        }
        else if(mode == Mode.PRIMITIVE_SELECT)
        {
            setMessage("Select Primitive: Click on a primitive to select it.");
        }
        else if(mode == Mode.REGION_VERTEX_SELECT)
        {
            setMessage("Region Select: Draw bounding box to select vertices.");
        }
        else if(mode == Mode.REGION_PRIMITIVE_SELECT)
        {
            setMessage("Region Select: Draw bounding box to select primitives.");
        }
        else if(mode == Mode.TRANSLATE)
        {
            setMessage("Translate: Click and drag mouse to move selected items.");
        }
        else if(mode == Mode.ROTATE)
        {
            setMessage("Rotate: Click and drag mouse to rotate selected items.");
        }
        else if(mode == Mode.SCALE)
        {
            setMessage("Scale: Click and drag mouse to scale selected items.");
        }
        else if(mode == Mode.ADD_POINTS)
        {
            setMessage("Add Points: Click mouse for each vertex you would like to add.");
        }
        else if(mode == Mode.SHEAR)
        {
            setMessage("Shear: Click and drag mouse to shear selected items.");
        }
        else if(mode == Mode.PAN)
        {
            setMessage("Pan: Click and drag mouse to pan within the viewpport.");
        }
        else if(mode == Mode.ZOOM)
        {
            setMessage("Zoom: Click and drag mouse up to zoom in, down to zoom out.");
        }

    }
}
