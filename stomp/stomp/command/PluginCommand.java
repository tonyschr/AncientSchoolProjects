package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.transform.*;

import java.awt.*;
import java.awt.event.*;

/**
 * PluginCommand is the superclass of all user plugins.
 *
 * To create a plugin, extend PluginCommand and implement the
 * <i>execute</i> and (optional) <i>unexecute</i> methods.  You can
 * use the methods provided in this class in conjunction with any of the
 * public classes found in Stomp to create plugins.
 */
public abstract class PluginCommand implements Command
{
    private Scene m_scene;
    private ActionListener m_guiListener;

    /**
     * This is an initialization method used elsewhere in Stomp.  Do
     * not use it...
     */
    public final void setupPlugin(Scene scene, ActionListener guiListener)
    {
        m_scene = scene;
        m_guiListener = guiListener;
    }

    /**
     * Execute a command.  Simulates a user clicking a button or choosing
     * a menu item corresponding to a command of the supplied name.  Does
     * NOT activate mode buttons.
     *
     * @param commandName String containing the name of the command to
     * execute.  It can consist of any regular button or menu item in
     * Stomp3D.
     */
    public final void executeCommand(String commandName)
    {
        ActionEvent fireEvent = new ActionEvent(this, 0, commandName);
        m_guiListener.actionPerformed(fireEvent);
    }


    /**
     * Add a vertex to the scene at the supplied x, y, and z
     * coordinates.
     */
    public final void addVertex(float x, float y, float z)
    {
        CommandExecutor.execute(new AddVertexCommand(m_scene,
                                                     new Vertex(x, y, z)));
    }

    /**
     * Add the suppled vertex to the scene.
     */
    public final void addVertex(Vertex v)
    {
        CommandExecutor.execute(new AddVertexCommand(m_scene, v));
    }

    public final void addPrimitive(Primitive p)
    {
        m_scene.addPrimitive(p);
    }
    
    /**
     * Make a polygon from the supplied array of vertices.  All of the
     * vertices in the array must already exist in the scene!
     */
    public final Polygon3d makePolygon(Vertex vertices[]) throws IllegalArgumentException
    {
        if(vertices.length < 3)
        {
            throw new IllegalArgumentException("Polygons must have at least 3 vertices");
        }
        
        int indices[] = new int[vertices.length];
        for(int i = 0; i < indices.length; i++)
        {
            indices[i] = m_scene.getIndex(vertices[i]);
            if(indices[i] == -1)
            {
                throw new IllegalArgumentException("Vertex not found in scene.");
            }
        }

        Polygon3d polygon = new Polygon3d(indices);
        m_scene.addPrimitive(polygon);
        return polygon;
    }
    
    /**
     * Move the selected vertices or primitive by the supplied x, y, and z
     * amounts.
     */
    public final void moveSelected(float x, float y, float z)
    {
        Translation trans = new Translation();
        trans.setTranslation(x, y, z);
        CommandExecutor.execute(new TransformCommand(m_scene, trans));
    }

    /**
     * Select a vertex.  It must already exist in the scene.
     */
    public final void select(Vertex v)
    {
        m_scene.select(v);
    }

    /**
     * Select a primitive.  It must already exist in the scene.
     */
    public final void select(Primitive p)
    {
        m_scene.select(p);
    }
    
    /**
     * Deselect a vertex.  It must already exist in the scene.
     */
    public final void deselect(Vertex v)
    {
        m_scene.deselect(v);
    }

    /**
     * Deselect a primitive.  It must already exist in the scene.
     */
    public final void deselect(Primitive p)
    {
        m_scene.deselect(p);
    }

    /**
     * Delete a vertex from the scene.
     */
    public final void deleteVertex(Vertex v)
    {
        m_scene.removeVertex(v);
    }

    /**
     * Delete a primitive from the scene.
     */
    public final void deletePrimitive(Primitive p)
    {
        m_scene.removePrimitive(p);
    }

    /**
     * Select all vertices in the scene.
     */
    public final void selectAllVertices()
    {
        executeCommand("Deselect All");
        FastVector vertices = m_scene.getVerticesVector();

        for(int i = 0; i < vertices.sizeFast(); i++)
        {
            m_scene.select((Vertex)vertices.elementAtFast(i));
        }
    }

    /**
     * Select all primitives in the scene.
     */
    public final void selectAllPrimitives()
    {
        executeCommand("Deselect All");
        FastVector primitives = m_scene.getPrimitivesVector();

        for(int i = 0; i < primitives.sizeFast(); i++)
        {
            m_scene.select((Primitive)primitives.elementAtFast(i));
        }
    }

    /**
     * Rotate the selected vertices or primitives.  Axis-angle
     * rotation.
     *
     * @param pivotX X coord to rotate about
     * @param pivotY Y coord to rotate about
     * @param pivotZ Z coord to rotate about
     * @param axisX X component of axis to rotate around
     * @param axisY Y component of axis to rotate around
     * @param axisZ Z component of axis to rotate around
     * @param angle angle (in degrees) to rotate
     */
    public final void rotateSelected(float pivotX, float pivotY, float pivotZ,
                                     float axisX, float axisY, float axisZ,
                                     float angle)
    {
        angle = (float)((angle / 180) * Math.PI);

        Rotation trans = new Rotation();
        trans.setRotation(pivotX, pivotY, pivotZ,
                          axisX, axisY, axisZ, angle);
        CommandExecutor.execute(new TransformCommand(m_scene,
                                                     trans));
    }

    /**
     * Scale the selected vertices or primitives.  Axis-angle
     * rotation.
     *
     * @param centerX X coord to scale about
     * @param centerY Y coord to scale about
     * @param centerZ Z coord to scale about
     * @param axisX X amount to scale
     * @param axisY Y amount to scale 
     * @param axisZ Z amount to scale
     */
    public final void scaleSelected(float centerX, float centerY, float centerZ,
                                    float scaleX, float scaleY, float scaleZ)
    {
        Scale trans = new Scale();
        trans.setScale(centerX, centerY, centerZ,
                       scaleX, scaleY, scaleZ);
        CommandExecutor.execute(new TransformCommand(m_scene,
                                                     trans));
    }

    /**
     * Get a Vector containing all of the vertices in the scene.
     *
     * Warning: This is provided to allow users to build more creative
     * plugins, but if you use this it is your responsibility to ensure
     * the integrity of the scene!!
     */
    public final FastVector getSceneVertices()
    {
        return m_scene.getVerticesVector();
    }

    /**
     * Get a Vector containing all of the primitives in the scene.
     *
     * Warning: This is provided to allow users to build more creative
     * plugins, but if you use this it is your responsibility to ensure
     * the integrity of the scene!!
     */
    public final FastVector getScenePrimitives()
    {
        return m_scene.getPrimitivesVector();
    }

    public void validateScene()
    {
        m_scene.validateScene();
    }
    
    public abstract boolean execute();
    public abstract void unExecute();
    public abstract String toString();
}
