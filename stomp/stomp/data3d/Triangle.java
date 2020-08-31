package stomp.data3d;

/**
 * This class is a utility class for things such as triangulation
 * and tesselation.  Simple triangles are much easier to maintain
 * than planes, and planes can be created from these triangles easily.
 */
public class Triangle
{
    public Vertex v0;
    public Vertex v1;
    public Vertex v2;

    public Triangle(Vertex tv1, Vertex tv2, Vertex tv3)
    {
        v0 = (Vertex)tv1.clone();
        v1 = (Vertex)tv2.clone();
        v2 = (Vertex)tv3.clone();
    }
}
