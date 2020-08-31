package stomp.transform;

import javax.vecmath.*;

import stomp.data3d.*;

/**
 * Rotation class encapsulates a 3D rotation.  The rotation can be
 * axis-angle or a standard rotation.
 */
public class Rotation extends Transformation
{
    public static int XY = 1;
    public static int YZ = 2;
    public static int XZ = 3;
    
    protected Matrix4f m_transMatrix;
    protected Matrix4f m_translateToOrigin = new Matrix4f();
    protected Matrix4f m_translateBack = new Matrix4f();
    protected Matrix4f m_axisAngle = new Matrix4f();
    protected Matrix4f m_rotation = new Matrix4f();
    protected boolean m_groupAxis = false;
    protected float m_axisX;
    protected float m_axisY;
    protected float m_axisZ;
    protected int m_axis;
    protected float m_theta;
    
    /**
     * Rotation constructor.  Initialize matrix.
     */
    public Rotation()
    {
        m_transMatrix = new Matrix4f();
        m_transMatrix.setIdentity();
    }

    public void setAlternativeAxis(float px, float py, float pz)
    {
        m_rotation.setIdentity();
        if(m_axis == Rotation.XY)
        {
            m_rotation.m00 = (float)Math.cos(m_theta);
            m_rotation.m01 = (float)-Math.sin(m_theta);
            m_rotation.m10 = (float)Math.sin(m_theta);
            m_rotation.m11 = (float)Math.cos(m_theta);
        }
        else if(m_axis == Rotation.YZ)
        {
            m_rotation.m11 = (float)Math.cos(m_theta);
            m_rotation.m12 = (float)-Math.sin(m_theta);
            m_rotation.m21 = (float)Math.sin(m_theta);
            m_rotation.m22 = (float)Math.cos(m_theta);
        }
        else if(m_axis == Rotation.XZ)
        {
            m_rotation.m00 = (float)Math.cos(m_theta);
            m_rotation.m02 = (float)-Math.sin(m_theta);
            m_rotation.m20 = (float)Math.sin(m_theta);
            m_rotation.m22 = (float)Math.cos(m_theta);
        }

        m_translateToOrigin.setIdentity();
        m_translateToOrigin.setTranslation(new Vector3f(-px, -py, -pz));
        
        m_translateBack.setIdentity();
        m_translateBack.setTranslation(new Vector3f(px, py, pz));

        m_rotation.mul(m_translateToOrigin); 
        m_transMatrix.mul(m_translateBack, m_rotation);                
    }

    public void useNormalAxis()
    {
        setRotation(m_axisX, m_axisY, m_axisZ, m_axis, m_theta);
    }

    public AxisAngle4f getAxisAngleRotation()
    {
        if(m_axis == Rotation.XY)
        {
            return new AxisAngle4f(0, 0, 1, m_theta);
        }
        if(m_axis == Rotation.YZ)
        {
            return new AxisAngle4f(1, 0, 0, m_theta);
        }
        if(m_axis == Rotation.XZ)
        {
            return new AxisAngle4f(0, 1, 0, m_theta);
        }

        else return new AxisAngle4f(1, 0, 0, 0);
    }
    
    public Vector3f getRotation()
    {
        if(m_axis == Rotation.XY)
        {
            return new Vector3f(0, 0, m_theta);
        }
        else if(m_axis == Rotation.YZ)
        {
            return new Vector3f(m_theta, 0, 0);
        }
        else if(m_axis == Rotation.XZ)
        {
            return new Vector3f(0, m_theta, 0);
        }

        else return new Vector3f(0, 0, 0);
    }
    
    /**
     * Axis-angle rotation around an arbitrary point in 3d space.
     * Can be used to rotate objects in a large variety of ways.
     *
     * @param px x point to rotate about
     * @param py y point to rotate about
     * @param pz z point to rotate about
     * @param ux x coordinate of axis to rotate around
     * @param uy y coordinate of axis to rotate around
     * @param uz z coordinate of axis to rotate around
     * @param theta Angle to rotate
     */
    public void setRotation(float px, float py, float pz,
                            float ux, float uy, float uz,
                            float theta)
    {
        m_transMatrix.setIdentity();

        m_axisAngle.setIdentity();
        m_axisAngle.m00 = (float)(ux * ux + Math.cos(theta) * (1 - ux * ux));
        m_axisAngle.m01 = (float)(ux * uy * (1 - Math.cos(theta)) -
                                uz * Math.sin(theta));
        m_axisAngle.m02 = (float)(uz * ux * (1 - Math.cos(theta)) +
                                uy * Math.sin(theta));
        m_axisAngle.m10 = (float)(ux * uy * (1 - Math.cos(theta)) +
                                uz * Math.sin(theta));
        m_axisAngle.m11 = (float)(uy * uy + Math.cos(theta) * (1 - uy * uy));
        m_axisAngle.m12 = (float)(uy * uz * (1 - Math.cos(theta)) -
                                ux * Math.sin(theta));
        m_axisAngle.m20 = (float)(uz * ux * (1 - Math.cos(theta)) -
                                uy * Math.sin(theta));
        m_axisAngle.m21 = (float)(uy * uz * (1 - Math.cos(theta)) +
                                ux * Math.sin(theta));
        m_axisAngle.m22 = (float)(uz * uz + Math.cos(theta) * (1 - uz * uz));

        m_translateToOrigin.setIdentity();
        m_translateToOrigin.setTranslation(new Vector3f(-px, -py, -pz));
        
        m_translateBack.setIdentity();
        m_translateBack.setTranslation(new Vector3f(px, py, pz));

        m_axisAngle.mul(m_translateToOrigin); 
        m_transMatrix.mul(m_translateBack, m_axisAngle);
    }    
    
    /**
     * Standard rotation on either the XY, XZ, or YZ planes.
     *
     * @param px x point to rotate about
     * @param py y point to rotate about
     * @param pz z point to rotate about
     * @param axis integer, use on of Rotation's static member variables.
     * @param theta Angle to rotate
     */
    public void setRotation(float px, float py, float pz,
                            int axis, float theta)
    {
        m_axisX = px;
        m_axisY = py;
        m_axisZ = pz;
        m_axis = axis;
        m_theta = theta;
        
        m_rotation.setIdentity();
        if(axis == Rotation.XY)
        {
            m_rotation.m00 = (float)Math.cos(theta);
            m_rotation.m01 = (float)-Math.sin(theta);
            m_rotation.m10 = (float)Math.sin(theta);
            m_rotation.m11 = (float)Math.cos(theta);
        }
        else if(axis == Rotation.YZ)
        {
            m_rotation.m11 = (float)Math.cos(theta);
            m_rotation.m12 = (float)-Math.sin(theta);
            m_rotation.m21 = (float)Math.sin(theta);
            m_rotation.m22 = (float)Math.cos(theta);
        }
        else if(axis == Rotation.XZ)
        {
            m_rotation.m00 = (float)Math.cos(theta);
            m_rotation.m02 = (float)-Math.sin(theta);
            m_rotation.m20 = (float)Math.sin(theta);
            m_rotation.m22 = (float)Math.cos(theta);
        }

        m_translateToOrigin.setIdentity();
        m_translateToOrigin.setTranslation(new Vector3f(-px, -py, -pz));
        
        m_translateBack.setIdentity();
        m_translateBack.setTranslation(new Vector3f(px, py, pz));

        m_rotation.mul(m_translateToOrigin); 
        m_transMatrix.mul(m_translateBack, m_rotation);        
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
        Rotation m = new Rotation();
        m.m_transMatrix.invert(m_transMatrix);

        return m;
    }
}
