package webwars;

public class Vec
{
    private float m_x;
    private float m_y;

    /**
     * Constructor.
     *
     * @param x X value
     * @param y Y value
     */
    public Vec(float x, float y)
    {
        m_x=x;
        m_y=y;
    }

    /**
     * Constructor
     *
     * @param v Previous existing Vec to copy
     */
    public Vec(Vec v)
    {
        m_x=v.m_x;
        m_y=v.m_y;
    }

    /**
     * @return X value of the vector
     */
    public float x()
    {
        return m_x;
    }

    /**
     * @return Y value of the vector
     */
    public float y()
    {
        return m_y;
    }

    /**
     * @return Magnitude of the vector.
     * (Caution: Uses an expensive square root)
     */
    public float magnitude()
    {
        return (float)Math.sqrt(m_x*m_x+m_y*m_y);
    }

    /**
     * Version of magnitude that doesn't do a square root.  Safe for
     * comparing in some circumstances and much more efficient.
     *
     * @return magnitude of the vector
     */
    public float magSq()  // square of magnitude...just for efficiency
    {
        return (float)(m_x*m_x+m_y*m_y);
    }

    //
    // Mathematical Operations.  Vector addition, subtraction,
    // and scalar multiplication
    //

    /**
     * Add another vector to this vector.
     *
     * @param addend Vector to add
     */
    public void add(Vec addend)
    {
        m_x+=addend.x();
        m_y+=addend.y();
    }

    /**
     * Subtract vector from this vector
     *
     * @param addend Vector to subtract
     */
    public void subtract(Vec addend)
    {
        m_x-=addend.x();
        m_y-=addend.y();
    }

    /**
     * Multiply this vector by a scalar.
     *
     * @param factor value to multiply
     */
    public void multiply(float factor)
    {
        m_x*=factor;
        m_y*=factor;
    }

    // 
    // functional mathematical operations.  These do not change the state
    // of the object, they compute and return a new object.
    //

    /**
     * Add this and another vector and return the result.
     */
    public Vec plus(Vec addend)
    {
        Vec result = new Vec(this);
        result.m_x += addend.m_x;
        result.m_y += addend.m_y;
        return result;
    }

    /**
     * Subtract another vector from this and return the result.
     */
    public Vec minus(Vec addend)
    {
        Vec result = new Vec(this);
        result.m_x -= addend.m_x;
        result.m_y -= addend.m_y;
        return result;
    }

    /**
     * Multiply this vector by a scalar and return the result.
     */
    public Vec times(float factor)
    {
        Vec result = new Vec(this);
        result.m_x *= factor;
        result.m_y *= factor;
        return result;
    }

    /**
     * Cross product and return the result.
     */
    public float cross(Vec v)
    {
        return v.m_x * this.m_y - v.m_y * this.m_x;
    }

    /**
     * Dot product and return the result.
     */
    public float dot(Vec v)
    {
        return v.m_x * this.m_x + v.m_y * this.m_y;
    }
}

