package stomp;

import java.awt.Graphics;
import javax.vecmath.*;

import stomp.data3d.Vertex;

/**
 * Implementation of LiangBarsky clipping algorith.  I believe this
 * is faster than the SutherlandHodgman algorithm, but there is
 * a subtle bug and I haven't been able to track it down.
 */
public class LiangBarsky
{
    private static double m_yBottom = 0.0;
    private static double m_yTop = 200.0;
    private static double m_xLeft = 0.0;
    private static double m_xRight = 200.0;

    private static double t0;
    private static double t1;
    
    static boolean clip_t(double p, double q)
    {
        boolean accept = true;
        double r;

        if(p < 0.0)
        {
            r = q / p;
            if(r > t1)
            {
                accept = false; //reject
            }
            else if(r > t0)
            {
                t0 = r;
            }
        }
        else if(p > 0.0)
        {
            r = q / p;
            if(r < t0)
            {
                accept = false; //reject
            }
            else if(r < t1)
            {
                t1 = r;
            }
        }
        else if(q < 0.0)
        {
            accept = false;
        }

        return accept;
    }

    static void setClipBounds(double yBottom, double yTop,
                              double xRight, double xLeft)
    {
        m_yBottom = yBottom;
        m_yTop = yTop;
        m_xRight = xRight;
        m_xLeft = xLeft;
    }
    
    public static boolean clip2d(Point2d p0,
                                 Point2d p1)
    {
        t0 = 0.0;
        t1 = 1.0;
        double deltax = p1.x - p0.x;
        double deltay;

        if(clip_t(-deltax, p0.x - m_xLeft))
        {
            if(clip_t(deltax, m_xRight - p0.x))
            {
                deltay = p1.y - p0.y;
                if(clip_t(-deltay, p0.y - m_yBottom))
                {
                    if(clip_t(deltay, m_yTop - p0.y))
                    {
                        if(t1 < 1.0)
                        {
                            p1.x = (p0.x + (t1 * deltax)) + 0.5;
                            p1.y = (p0.y + (t1 * deltay)) + 0.5;
                        }
                        if(t0 > 0.0)
                        {
                            p0.x += t0 * deltax + 0.5;
                            p0.y += t0 * deltay + 0.5;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }

        return true;
    }
}
