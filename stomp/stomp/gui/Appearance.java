package stomp.gui;

import java.awt.*;

/**
 * Appearance class.  This class has only static methods and members and
 * encapsulates some of the global look and feel properties of the program.
 *
 * <p>
 * Use this class to get the default font and associated font metrics
 * such as width and ascent and descent of the font.
 */
public class Appearance
{
    private static Font m_textFont;
    private static Font m_smallFont;
    private static FontMetrics m_fm;
    private static FontMetrics m_smallFm;

    private static int m_fontHeight;
    private static int m_fontAscent;
    private static int m_fontDescent;

    /**
     * Static initializer.  Create default font and set defailt mode
     * values.
     */
    static
    {
        m_textFont = new Font("Helvetica", Font.PLAIN, 12);
        m_smallFont = new Font("Helvetica", Font.PLAIN, 9);
        update();
    }

    /**
     * Recalculate the font metrics based on the current font.
     */
    protected static void update()
    {
        m_fm = java.awt.Toolkit.getDefaultToolkit().getFontMetrics(m_textFont);
        m_smallFm = java.awt.Toolkit.getDefaultToolkit().getFontMetrics(m_smallFont);
        m_fontHeight = m_fm.getHeight();
        m_fontAscent = m_fm.getAscent();
        m_fontDescent = m_fm.getDescent();
    }

    /**
     * Set the font size to a given size.
     *
     * @param size New size of default font.
     */
    public static void setFontSize(int size)
    {
        m_textFont = new Font("Helvetica", Font.PLAIN, size);
        update();
    }
    
    /**
     * @return height of the current font in pixels.
     */
    public static int fontHeight()
    {
        return m_fontHeight;
    }

    /**
     * @return ascent of the current font in pixels.
     */
    public static int fontAscent()
    {
        return m_fontAscent;
    }

    /**
     * @return descent of current font in pixels.
     */
    public static int fontDescent()
    {
        return m_fontDescent;
    }

    /**
     * @return a reference to the default font.
     */
    public static Font getFont()
    {
        return m_textFont;
    }

    public static Font getSmallFont()
    {
        return m_smallFont;
    }
    
    /**
     * @return FontMetrics object of current font.  Can be used for additional
     * calculations (such as width of a string in pixels).
     */
    public static FontMetrics getFontMetrics()
    {
        return m_fm;
    }

    public static FontMetrics getSmallFontMetrics()
    {
        return m_smallFm;
    }
}

/***********************************************************
 * $Log: Appearance.java,v $
 * Revision 1.2  1998/03/03 20:51:22  schreine
 * Added log string
 *
 **********************************************************/
