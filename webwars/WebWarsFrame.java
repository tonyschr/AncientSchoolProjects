package webwars;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * This frame class acts as a top-level window in which the applet
 * appears when it's run as a standalone application.
 */
public class WebWarsFrame extends Frame implements WindowListener
{

    public WebWarsFrame(String str)
    {
        super (str);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            dispose();
            System.exit(0);
        }
    }
  
    /** Event handler for when the window is closed.  Exits with
     *  System.exit(0).
     */
    public void windowClosed(WindowEvent e) 
    {
        dispose();
        System.exit(0);
    }

    /**
     *  Event handler for when the window is closed.  Simply exits.
     *
     */
    public void windowDeiconified(WindowEvent e) 
    {
    }

    /** Event handler for when the window is iconified.  Does nothing.
     */
    public void windowIconified(WindowEvent e) 
    {
    }


    /** Event handler for when the window is activated.  Does nothing.
     */
    public void windowActivated(WindowEvent e) 
    {
    }

    /**
     *  Event handler for when the window is deactivated.
     *  Does nothing.
     */
    public void windowDeactivated(WindowEvent e) 
    {
    }

    /**
     *  Event handler for when the window is opened.  Does nothing.
     */
    public void windowOpened(WindowEvent e) 
    {
    }

    /**
     *  Event handler for when the window is closing.
     */
    public void windowClosing(WindowEvent e) 
    {
    }
}
