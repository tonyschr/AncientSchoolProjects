package stomp.transform;

import javax.vecmath.*;

import stomp.data3d.*;

/**
 * Shear class encapsulates a 3d shear around a specific point.
 */
public class Shear extends Transformation
{
    public static int XY = 1;
    public static int YZ = 2;
    public static int XZ = 3;

    protected Matrix4f m_transMatrix;
    protected Matrix4f m_translateToOrigin = new Matrix4f();
    protected Matrix4f m_translateBack = new Matrix4f();
    protected Matrix4f m_shear = new Matrix4f();

    /**
     * Shear constructor. Initialize matrix.
     */
    public Shear()
    {
        m_transMatrix = new Matrix4f();
        m_transMatrix.setIdentity();
    }

    /**
     * Set the scale's reference point and amount.
     *
     * @param px X point to shear about.
     * @param py Y point to shear about.
     * @param pz Z point to shear about.
     * @param axis axis to shear on (see static member variables)
     * @param x X amount of shear.
     * @param y Y amount of shear.
     * @param z Z amount of shear.
     */    public void setShear(float px, float py, float pz, int axis,
                         float x, float y, float z)
    {
        m_transMatrix.setIdentity();
        m_shear.setIdentity();

        //Determine shear based on axis.
        if(axis == Shear.XY)
        {
            m_shear.m02 = x;
            m_shear.m12 = y;
        }
        else if(axis == Shear.YZ)
        {
            m_shear.m10 = y;
            m_shear.m20 = z;
        }
        else if(axis == Shear.XZ)
        {
            m_shear.m01 = x;
            m_shear.m21 = z;
        }

        //Translate to origin, shear, translate back.
        m_translateToOrigin.setIdentity();
        m_translateToOrigin.setTranslation(new Vector3f(-px, -py, -pz));
        
        m_translateBack.setIdentity();
        m_translateBack.setTranslation(new Vector3f(px, py, pz));
        
        m_shear.mul(m_translateToOrigin);
        m_transMatrix.mul(m_translateBack, m_shear);
    }

    /**
     * Transform a vertex using this transformation matrix.
     *
     * @param from Vertex to transform.
     * @param to Already existing vertex to transform into.
     */
     public void transformVertex(Vertex from, Vertex to)
    {
        m_transMatrix.transform(from, to);
    }
    
    /**
     * Transform a vertex using this transformation matrix.
     *
     * @param from Vertex to transform.
     * @return new Vertex that has been transformed.
     */
    public Vertex transformVertex(Vertex v)
    {
        Vertex v2 = new Vertex();
        m_transMatrix.transform(v, v2);
        return v2;
    }

    public Transformation inverse()
    {
        Shear m = new Shear();
        m.m_transMatrix.invert(m_transMatrix);

        return m;
    }

}
