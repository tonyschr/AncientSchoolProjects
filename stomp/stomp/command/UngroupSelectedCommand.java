package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Ungroup the selected group back into its parts.
 */
public class UngroupSelectedCommand implements Command
{ 
    private Scene m_scene;
    private FastVector m_groups;
    private Color m_color;

    private FastVector m_selectedPrims;

    private UngroupSelectedCommand()
    {
    }
    
    public UngroupSelectedCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_groups = new FastVector();
        
        FastVector primitives = m_scene.getPrimitivesVector();

        for(int i = 0; i < primitives.sizeFast(); i++)
        {
            Primitive prim = (Primitive)primitives.elementAtFast(i);

            if(prim instanceof Group && prim.isSelected())
            {
                Group group = (Group)prim;
                group.releasePrimitives();
                m_scene.removePrimitive(group);
            }
        }
        
        m_scene.validateScene();
        return true;
    }
    
    public void unExecute()
    {
        System.out.println("Sorry, can't undo ungroup yet");
    }

}

