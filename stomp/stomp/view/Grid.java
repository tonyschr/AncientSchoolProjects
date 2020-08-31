package stomp.view;

import javax.vecmath.*;
import java.awt.*;
import stomp.Mode;
import java.text.*;
import stomp.gui.Appearance;

/**
 * Grid class.  This draws the grid based on the current pan and zoom
 * level, as described in Mode.
 */
public class Grid
{
    public static boolean GRID_NUMBERS = true;
    public static final Font SMALLFONT = Appearance.getSmallFont();
    
    private final Color m_color = new Color(220, 220, 220);
    private final Color m_smallColor = new Color(205, 205, 205);
    private final Color m_numberColor = new Color(0, 0, 0);
    private final Color m_backgroundColor = Color.lightGray;
    private static double m_bigSpacing = 1;
    private NumberFormat m_numFormat;

    //Temporary vars so we don't have to re-instantiate every time
    //the grid is painted!!
    private volatile Dimension viewSize;
    private volatile double zoom, panx, pany, zoom2;
    private volatile double curZoom = 0.0;
    private volatile double oldZoom = 0.0;
    private volatile double yOffset, xOffset, halfx, halfy;
    private volatile double factorX, factorY, modeZoom;
    private volatile boolean swapY;
    private volatile int yPos;
    private volatile Vector3d pan;
    
    /**
     * Constructor.  Does nothing.
     */
    Grid()
    {
        m_numFormat = NumberFormat.getInstance();
        m_numFormat.setMaximumFractionDigits(2);
        m_numFormat.setMinimumFractionDigits(2);
    }

    /**
     * Draw the grid.
     * The grid drawing starts at the center
     * and draw out both ways.  Does not draw beyond the size of
     * the viewport, so it should be very fast and have infinite size.
     *
     * @param g Graphics context.
     * @param view Orthogonal view to draw a grid in.  Must have a reference
     * to it to get the dimensions.
     */
    public void paint(Graphics g, Orthogonal view)
    {
        //Get the size of the view
        viewSize = view.getSize();
        g.setFont(SMALLFONT);

        curZoom = Mode.getZoom();
        
        if(oldZoom != curZoom)
        {
            if(curZoom > 100)
            {
                m_numFormat.setMaximumFractionDigits(4);
                m_numFormat.setMinimumFractionDigits(4);
            }
            else if(curZoom > 1)
            {
                m_numFormat.setMaximumFractionDigits(2);
                m_numFormat.setMinimumFractionDigits(2);
            }
            else
            {
                m_numFormat.setMaximumFractionDigits(0);
                m_numFormat.setMinimumFractionDigits(0);
            }
        }
        
        //Get the pan and view settings from Mode.
        zoom = Mode.getZoom() * m_bigSpacing;
        pan = Mode.getPan();

        //Set the pan x and y values, translating z to either x or y.
        panx = view.m_xDir ? pan.x : pan.z;
        pany = view.m_yDir ? pan.y : pan.z;

        if(oldZoom != curZoom)
        {
            while (zoom < 80)
            {
                m_bigSpacing = m_bigSpacing * 4;
                zoom = curZoom * m_bigSpacing;
            }
            while(zoom > 130)
            {
                m_bigSpacing = m_bigSpacing / 4.0;
                zoom = curZoom * m_bigSpacing;
            }

            zoom2 = curZoom * (m_bigSpacing/4.0);
        }
        
        oldZoom = Mode.getZoom();

        //Calculate the offsets for the grid.
        yOffset = pany - ((int)(pany/zoom) * zoom);
        halfy = viewSize.height/2.0;
        xOffset = panx - ((int)(panx/zoom) * zoom);
        halfx = viewSize.width/2.0;

        //Gray background
        g.setColor (m_backgroundColor);
        g.fillRect (0, 0, viewSize.width, viewSize.height);

        //Draw smaller grid things.
        g.setColor(m_smallColor);        
        //Draw horizontal lines for the grid.
        int width = viewSize.width - 2;
        int height = viewSize.height - 2;
        for(double i = yOffset + halfy; i < viewSize.height; i+=zoom2)
        {
            g.drawLine(2, (int)i, width, (int)i);
        }
        for(double i = yOffset + halfy; i > 0; i-=zoom2)
        {
            g.drawLine(2, (int)i, width, (int)i);
        }

        //Draw vertical lines for the grid.
        for(double i = xOffset + halfx; i < viewSize.width; i+=zoom2)
        {
            g.drawLine((int)i, 2, (int)i, height);
        }
        for(double i = xOffset + halfx; i > 0; i-=zoom2)
        {
            g.drawLine((int)i, 2, (int)i, height);
        }

        
        g.setColor(m_color);        

        //Draw horizontal lines for the grid.
        for(double i = yOffset + halfy; i < viewSize.height; i+=zoom)
        {
            g.drawLine(2, (int)i, width, (int)i);
        }
        for(double i = yOffset + halfy; i > 0; i-=zoom)
        {
            g.drawLine(2, (int)i, width, (int)i);
        }

        //Draw vertical lines for the grid.
        for(double i = xOffset + halfx; i < viewSize.width; i+=zoom)
        {
            g.drawLine((int)i, 2, (int)i, height);
        }
        for(double i = xOffset + halfx; i > 0; i-=zoom)
        {
            g.drawLine((int)i, 2, (int)i, height);
        }

        //Draw the center axis to be brighter white.
        g.setColor(Color.white);
        g.drawLine(2, (int)(halfy + pany),
                   width, (int)(halfy + pany));
        g.drawLine((int)(halfx + panx), 2,
                   (int)(halfx + panx), height);

        if(GRID_NUMBERS)
        {
            //Draw the grid numbers.
            g.setColor(m_numberColor);
            
            factorY = pany + halfy;
            factorX = panx + halfx;

            modeZoom = Mode.getZoom();

            if(zoom < 50)
            {
                zoom *= 2;
            }

            if(view.m_xDir && view.m_zDir)
            {
                swapY = true;
            }
            else
            {
                swapY = false;
            }
            
            //Draw horizontal numbers for the grid.
            for(double i = yOffset + halfy; i < viewSize.height; i+=zoom)
            {
                if(swapY)
                {
                    g.drawString(m_numFormat.format((i-factorY)/Mode.getZoom()),
                                 4, (int)i+4);
                }
                else
                {
                    g.drawString(m_numFormat.format(-(i-factorY)/Mode.getZoom()),
                                 4, (int)i+4);
                }
            }
            for(double i = yOffset + halfy; i > 0; i-=zoom)
            {
                if(swapY)
                {
                    g.drawString(m_numFormat.format((i-factorY)/Mode.getZoom()),
                                 4, (int)i+4);
                }
                else
                {
                    g.drawString(m_numFormat.format(-(i-factorY)/Mode.getZoom()),
                                 4, (int)i+4);
                }
            }
            
            yPos = viewSize.height - 4;
            
            //Draw vertical numbers for the grid.
            for(double i = xOffset + halfx; i < viewSize.width; i+=zoom)
            {
                g.drawString(m_numFormat.format((i-factorX)/modeZoom),
                             (int)i-8, yPos);
            }
            for(double i = xOffset + halfx; i > 0; i-=zoom)
            {
                g.drawString(m_numFormat.format((i-factorX)/modeZoom),
                             (int)i-8, yPos);
            }
        }
    }
}
