package stomp.transform;

import javax.vecmath.*;

import stomp.data3d.*;

/**
 * Taper class.  Non-linear taper transformation.
 *
 * TAPER IS NOT IMPLEMENTED IN THIS RELEASE OF STOMP.
 */
public class Taper extends Transformation
{
    protected Matrix4f m_transMatrix;
    protected Matrix4f m_translateToOrigin = new Matrix4f();
    protected Matrix4f m_translateBack = new Matrix4f();
    protected Matrix4f m_taper = new Matrix4f();

    protected float m_mousex;
    protected float m_mousey;
    protected float m_mousez;
    protected float m_px, m_py, m_pz;
    protected float m_x, m_y, m_z;
    
    public Taper()
    {
        m_transMatrix = new Matrix4f();
        m_transMatrix.setIdentity();
    }

    public void setTaper(float px, float py, float pz,
                         float cx, float cy, float cz,
                         float x, float y, float z)
    {
        m_mousex = cx;
        m_mousey = cy;
        m_mousez = cz;

        m_px = px;
        m_py = py;
        m_pz = pz;
        
        m_x = x;
        m_y = y;
        m_z = z;
    }

    public void transformVertex(Vertex from, Vertex to)
    {
        m_transMatrix.setIdentity();

        m_transMatrix.transform(from, to);
    }
    
    public Vertex transformVertex(Vertex v)
    {
        Vertex v2 = new Vertex();
        m_transMatrix.transform(v, v2);
        return v2;
    }

    public Transformation inverse()
    {
        Taper m = new Taper();
        m.m_transMatrix.invert(m_transMatrix);

        return m;
    }
}
