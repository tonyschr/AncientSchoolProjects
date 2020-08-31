package stomp.data3d;

import java.awt.Color;

/**
 * Surface of a face.  Includes color plus surface qualities
 * such as diffuse, reflection, specular, and transparency.
 */
public class Surface implements java.io.Serializable
{
    private double m_diffuse;
    private double m_specular;
    private double m_reflect;
    private double m_transparent;
    private Color m_color;
    private boolean m_smooth = false;

    /**
     * Surface constructor.
     * Defaults to white surface.
     */
    public Surface()
    {
        m_color = new Color(255, 255, 255);
        m_diffuse = 1.0;
    }

    /**
     * Surface constructor.
     * (all params except color are doubles from 0 to 1)
     *
     * @param color Color of surface
     * @param diff Diffuse value for surface
     * @param spec Specularity of surface
     * @param ref Reflection of surface
     * @param trans Transparency of surface
     */
    public Surface(Color color, double diff, double spec,
                   double ref, double trans)
    {
        m_diffuse = diff;
        m_specular = spec;
        m_reflect = ref;
        m_transparent = trans;
        m_color = color;
    }

    public void setSmooth(boolean smooth)
    {
        m_smooth = smooth;
    }
    
    /**
     * Set the diffuse value (0-1)
     *
     * @param diff Diffuse value
     */
    public void setDiffuse(double diff)
    {
        m_diffuse = diff;
    }

    /**
     * Set the color of the surface.
     *
     * @param c Color to set to.
     */
    public void setColor(Color c)
    {
        m_color = c;
    }

    /**
     * Set the specular value (0-1)
     *
     * @param spec Specular value
     */
    public void setSpecular(double spec)
    {
        m_specular = spec;
    }

    /**
     * Set the reflection value (0-1)
     *
     * @param spec Reflection value
     */
    public void setReflect(double ref)
    {
        m_reflect = ref;
    }

    /**
     * Set the transparency value (0-1)
     *
     * @param spec Transparency value
     */
    public void setTransparent(double trans)
    {
        m_transparent = trans;
    }

    /**
     * Get the color of the surface
     *
     * @return Color of surface
     */
    public Color getColor()
    {
        return m_color;
    }

    public boolean isSmooth()
    {
        return m_smooth;
    }
    
    /**
     * Get the diffuse of the surface
     *
     * @return Diffuse of surface
     */
    public double getDiffuse()
    {
        return m_diffuse;
    }

    /**
     * Get the specular value of the surface
     *
     * @return specular value of surface
     */
    public double getSpecular()
    {
        return m_specular;
    }

    /**
     * Get the reflection value of the surface
     *
     * @return reflection value of surface
     */
    public double getReflect()
    {
        return m_reflect;
    }

    /**
     * Get the transparency of the surface
     *
     * @return transparency value of surface
     */
    public double getTransparent()
    {
        return m_transparent;
    }
}
