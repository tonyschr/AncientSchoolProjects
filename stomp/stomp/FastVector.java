package stomp;

import java.util.Vector;

/**
 * FastVector is a simple extension to Java which gets around the
 * fact that the synchronized methods in Vector are very slow.
 */
public class FastVector extends Vector
{
    public FastVector()
    {
        super();
    }

    /**
     * Pull an element out of the array and return it.
     */
    public final Object elementAtFast(int index)
    {
        return elementData[index];
    }

    /**
     * Get the size of the array
     */
    public final int sizeFast()
    {
        return elementCount;
    }
}
