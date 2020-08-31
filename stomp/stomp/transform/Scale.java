package stomp.transform;

import javax.vecmath.*;

import stomp.data3d.*;

/**
 * Scale class encapsulates a 3d scale around a specific point.
 */
public class Scale extends Transformation
{
    protected Matrix4f m_transMatrix;
    protected Matrix4f m_translateToOrigin = new Matrix4f();
    protected Matrix4f m_translateBack = new Matrix4f();
    protected Matrix4f m_scale = new Matrix4f();

    /**
     * Scale constructor.  Initialize matrix.
     */
    public Scale()
    {
        m_transMatrix = new Matrix4f();
        m_transMatrix.setIdentity();
    }

    /**
     * Set the scale's reference point and amount.
     *
     * @param px X point to scale about.
     * @param py Y point to scale about.
     * @param pz Z point to scale about.
     * @param x X amount of scale.
     * @param y Y amount of scale.
     * @param z Z amount of scale.
     */
    public void setScale(float px, float py, float pz,
                         float x, float y, float z)
    {
        m_transMatrix.setIdentity();

        //Set the scale.
        m_scale.setIdentity();
        m_scale.m00 = x+1;
        m_scale.m11 = y+1;
        m_scale.m22 = z+1;

        //Bound the scale so it can't go negative!
//          if(m_scale.m00 < .0001)
//              m_scale.m00 = (float).0001;
//          if(m_scale.m11 < .0001)
//              m_scale.m11 = (float).0001;
//          if(m_scale.m22 < .0001)
//              m_scale.m22 = (float).0001;

        //Tranlate to origin, scale, translate back.
        m_translateToOrigin.setIdentity();
        m_translateToOrigin.setTranslation(new Vector3f(-px, -py, -pz));
        
        m_translateBack.setIdentity();
        m_translateBack.setTranslation(new Vector3f(px, py, pz));
        
        m_scale.mul(m_translateToOrigin);
        m_transMatrix.mul(m_translateBack, m_scale);
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
        Scale m = new Scale();
        m.m_transMatrix.invert(m_transMatrix);

        return m;
    }

}
