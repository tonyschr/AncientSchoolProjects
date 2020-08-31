package stomp.command;

import stomp.*;
import stomp.data3d.*;

import java.util.*;

/**
 * Flip a polygon so that the normal faces the other direction.  Does
 * this by reordering the indices.
 */
public class FlipNormalCommand implements Command
{
    private Scene m_scene;

    private FlipNormalCommand()
    {
    }
    
    public FlipNormalCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        return flip();
    }

    public void unExecute()
    {
        flip();
    }

    private boolean flip()
    {
        boolean changed = false;
        Vector selectedPrimitives = m_scene.getOrderedSelectedPrimitives();
        FastVector transformedVertices = m_scene.getVerticesVector();
        
        for(int i = selectedPrimitives.size()-1; i >=0 ; i--)
        {
            Primitive element = (Primitive)selectedPrimitives.elementAt(i);
            if(element instanceof Polygon3d)
            {
                changed = true;
                ((Polygon3d)element).flip();
                ((Polygon3d)element).computeNormal(transformedVertices);
            }
        }

        m_scene.validateScene();
        return changed;
    }

    public String toString()
    {
        return "Flip Normal";
    }

}
