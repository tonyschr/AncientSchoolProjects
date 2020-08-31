package stomp.transform;

import java.util.*;

import stomp.data3d.*;

/**
 * Serves as the interface for linear and perhaps non-linear
 * transformations.  This stable class helps guarentee future
 * expandability for this area.
 */
public abstract class Transformation
{
    abstract public Vertex transformVertex(Vertex v);
    abstract public void transformVertex(Vertex from, Vertex to);
    abstract public Transformation inverse();
}
