package stomp.data3d;

import java.util.Vector;
import java.awt.Graphics;

import stomp.FastVector;
import stomp.view.View;
import stomp.transform.Transformation;
import stomp.data3d.Group;

/**
 * Primitive interface.  Primitives are all pieces and parts
 * of objects except vertices, which are handeled separately.
 */
public interface Primitive extends java.io.Serializable
{
    public boolean isSelected();
    public void setSelected(boolean select);
    public void setHidden(boolean hidden);
    public boolean isHidden();
    public boolean select(FastVector vertices, int mouseX, int mouseY);
    public boolean selectRegion(FastVector vertices, int xmin, int xmax,
                                int ymin, int ymax);
    public void paint(FastVector vertices, Graphics g);
    public void transform(Transformation tr, FastVector fromVertices,
                          FastVector toVertices);
    public boolean containsIndex(int index);
    public int[] getIndices();
    public void setIndices(int indices[]);
    public boolean renumberIndices(int afterInd);
    public void replaceIndex(int oldIndex, int newIndex);
    public void setGroup(Group group);
    public Group getGroup();
    public Object clone();
}

