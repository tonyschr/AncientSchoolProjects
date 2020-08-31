package stomp.gui;

import java.awt.*;

/**
 * A simple color box widget.  A component that is simply the
 * color that it is told to be.
 */
public class ColorBox extends Canvas
{
    private int m_red;
    private int m_green;
    private int m_blue;

    /**
     * Default constructor.
     */
    public ColorBox()
    {
    }

    /**
     * Set the color of the text box to a given RGB value.
     *
     * @param red int 0 to 255
     * @param green int 0 to 255
     * @param blue int 0 to 255
     */
    public void setColor(int red, int green, int blue)
    {
        m_red = red;
        m_green = green;
        m_blue = blue;
    }

    public void setColor(Color color)
    {
        m_red = color.getRed();
        m_green = color.getGreen();
        m_blue = color.getBlue();
    }

    /**
     * Draw the colored rectangle.
     */
    public void paint(Graphics g)
    {
        Dimension size = getSize();
        g.setColor(new Color(m_red, m_green, m_blue));
        
        g.fillRect(0, 0, size.width, size.height);
    }

    /**
     * Specifies the minimum size that this component can be.
     */
    public Dimension getMinimumSize()
    {
        return new Dimension(20, 20);
    }

    /**
     * Specifies the size this component would like to be.
     *
     * Defaults to 20 x 20 pixels.  This component will be
     * streched by the layout manager to neatly fit almost
     * anywhere it is put.
     */
    public Dimension getPreferredSize()
    {
        return new Dimension(20, 20);
    }
}

/***********************************************************
 * $Log: ColorBox.java,v $
 * Revision 1.3  1998/03/03 20:51:22  schreine
 * Added log string
 *
 **********************************************************/
