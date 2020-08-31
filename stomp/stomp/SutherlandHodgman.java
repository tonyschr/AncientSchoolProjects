package stomp;

import java.awt.Graphics;
import javax.vecmath.*;

import stomp.data3d.Vertex;

/**
 * SutherlandHodgman clipping algorithm.
 *
 * 2d clipping is used for the viewports.  3d clipping is implemented (z
 * axis only) but unused so far.
 */
public class SutherlandHodgman
{
    public static boolean CLIP = true;
    
    private static double m_yBottom = 0.0;
    private static double m_yTop = 200.0;
    private static double m_xLeft = 0.0;
    private static double m_xRight = 200.0;
    private static double m_zFront = 1;
    private static double m_zBack = -5;

    private static final boolean REJECT = false;
    private static final boolean ACCEPT = true;

    static final boolean clip2d_d(Point2d p0, Point2d p1,
                            double min_boundary, double max_boundary)
    {
        double m;

        if(p0.x < min_boundary)
        {
            if(p1.x < min_boundary)
            {
                return REJECT;
            }
            
            m = (p1.y - p0.y) / (p1.x - p0.x);
            p0.y += m * (min_boundary - p0.x);
            p0.x = min_boundary;

            if(p1.x > max_boundary)
            {
                p1.y += m * (max_boundary - p1.x);
                p1.x = max_boundary;
            }
        }
        else if(p0.x > max_boundary)
        {
            if(p1.x > max_boundary)
            {
                return REJECT;
            }

            m = (p1.y - p0.y) / (p1.x - p0.x);
            p0.y += m * (max_boundary - p0.x);
            p0.x = max_boundary;
            
            if(p1.x < min_boundary)
            {
                p1.y += m * (min_boundary - p1.x);
                p1.x = min_boundary;
            }
        }
        else
        {
            if(p1.x > max_boundary)
            {
                p1.y += (p1.y - p0.y) / (p1.x - p0.x) * (max_boundary - p1.x);
                p1.x = max_boundary;
            }
            else if(p1.x < min_boundary)
            {
                p1.y += (p1.y - p0.y) / (p1.x - p0.x) * (min_boundary - p1.x);
                p1.x = min_boundary;
            }
        }

        return ACCEPT;
    }

    static final boolean clip2d_d2(Point2d p0, Point2d p1,
                          double min_boundary, double max_boundary)
    {
        double m;

        if(p0.y < min_boundary)
        {
            if(p1.y < min_boundary)
            {
                return REJECT;
            }
            
            m = (p1.x - p0.x) / (p1.y - p0.y);
            p0.x += m * (min_boundary - p0.y);
            p0.y = min_boundary;

            if(p1.y > max_boundary)
            {
                p1.x += m * (max_boundary - p1.y);
                p1.y = max_boundary;
            }
        }
        else if(p0.y > max_boundary)
        {
            if(p1.y > max_boundary)
            {
                return REJECT;
            }

            m = (p1.x - p0.x) / (p1.y - p0.y);
            p0.x += m * (max_boundary - p0.y);
            p0.y = max_boundary;
            
            if(p1.y < min_boundary)
            {
                p1.x += m * (min_boundary - p1.y);
                p1.y = min_boundary;
            }
        }
        else
        {
            if(p1.y > max_boundary)
            {
                p1.x += (p1.x - p0.x) / (p1.y - p0.y) * (max_boundary - p1.y);
                p1.y = max_boundary;
            }
            else if(p1.y < min_boundary)
            {
                p1.x += (p1.x - p0.x) / (p1.y - p0.y) * (min_boundary - p1.y);
                p1.y = min_boundary;
            }
        }

        return ACCEPT;
    }

    public static final boolean clip2d(Point2d p0, Point2d p1)
    {
        if(CLIP)
        {
            if(clip2d_d(p0, p1, m_xLeft, m_xRight) == REJECT)
            {
                return REJECT;
            }
            if(clip2d_d2(p0, p1, m_yBottom, m_yTop) == REJECT)
            {
                return REJECT;
            }
        }

        return ACCEPT;
    }

    public static final boolean clip3d_d(Point3d p0, Point3d p1,
                                   double min_boundary, double max_boundary)
    {
        double dx, dy, dz, t;

        if(p0.z < min_boundary)
        {
            if(p1.z < min_boundary)
            {
                return REJECT;
            }

            dx = p1.z - p0.z;
            dy = p1.x - p0.x;
            dz = p1.y - p0.y;
            t = (min_boundary - p0.z) / dx;
            p0.x += dy * t;
            p0.y += dz * t;

            if(p1.z > max_boundary)
            {
                t = (max_boundary - p1.z) / dx;
                p1.x += dy * t;
                p1.y += dz * t;
                p1.z = (float)max_boundary;
            }
        }
        else if(p0.z > max_boundary)
        {
            if(p1.z > max_boundary)
            {
                return REJECT;
            }
            
            dx = p1.z - p0.z;
            dy = p1.x - p0.x;
            dz = p1.y - p0.y;
            t = (max_boundary - p0.z) / dx;
            p0.x += dy * t;
            p0.y += dz * t;
            p0.z = (float)max_boundary;

            if(p1.z < min_boundary)
            {
                t = (min_boundary - p1.z) / dx;
                p1.x += dy * t;
                p1.y += dz * t;
                p1.z = (float)min_boundary;
            }
        }
        else
        {
            if(p1.z > max_boundary)
            {
                t = (max_boundary - p1.z) / (p1.z - p0.z);
                p1.x += (p1.x - p0.x) * t;
                p1.y += (p1.y - p0.y) * t;
                p1.z = (float)max_boundary;
            }
            else if(p1.z < min_boundary)
            {
                t = (min_boundary - p1.z) / (p1.z - p0.z);
                p1.x += (p1.x - p0.x) * t;
                p1.y += (p1.y - p0.y) * t;
                p1.z = (float)min_boundary;
            }
        }

        return ACCEPT;
    }

    /**
     * Only clips near and far z planes
     */
    public static final boolean clip3d(Point3d p0, Point3d p1)
    {
        return clip3d_d(p0, p1, m_zFront, m_zBack);
    }

    public static final boolean pointInBounds(int x, int y)
    {
        if(CLIP)
        {
            if(x < m_xLeft)
            {
                return false;
            }
            if(x > m_xRight)
            {
                return false;
            }
            if(y < m_yBottom)
            {
                return false;
            }
            if(y > m_yTop)
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static final void setClipBounds(double yBottom, double yTop,
                                     double xLeft, double xRight)
    {
        if(yTop > 100 &&
           xRight > 100)
        {
            m_yBottom = yBottom;
            m_yTop = yTop;
            m_xRight = xRight;
            m_xLeft = xLeft;
        }
    }
}
