package stomp.transform;

import javax.vecmath.*;

import stomp.data3d.*;

/**
 * Translation class.  Encapsulates a simple 3d translation (or Move).
 */
public class Translation extends Transformation
{
    protected Matrix4f m_transMatrix;

    /**
     * Translation constructor.  Initialize matrices.
     */
    public Translation()
    {
        m_transMatrix = new Matrix4f();
        m_transMatrix.set(1);
    }

    /**
     * Set the translation amount
     *
     * @param x X amount to translate.
     * @param y Y amount to translate.
     * @param z Z amount to translate.
     */
    public void setTranslation(float x, float y, float z)
    {
        m_transMatrix.setTranslation(new Vector3f(x, y, z));
    }

    public Vector3f getTranslation()
    {
        return new Vector3f(m_transMatrix.m03, m_transMatrix.m13, m_transMatrix.m23);
    }
    
    /**
     * Set the translation amount
     *
     * @param trans Vector3f containing x, y, and z translation amounts.
     */
    public void setTranslation(Vector3f trans)
    {
        m_transMatrix.setTranslation(trans);
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
        Translation m = new Translation();
        m.m_transMatrix.invert(m_transMatrix);
        return m;
    }
}
