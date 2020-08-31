package stomp.data3d;

import java.util.*;
import javax.vecmath.*;
import java.awt.*;

import stomp.FastVector;
import stomp.gui.*;
import stomp.view.*;
import stomp.Mode;
import stomp.transform.*;

/**
 * Scene is a central class which stores and manipulates the vertices
 * and primitives (ie. polygons) in the scene.
 *
 * <p>
 * Scene contains a few of the algorithms for operating on the
 * data structures.  The reason this is done in scene (for now) is that
 * the individual vertices, planes, etc. do not know about each other.
 * Operations such as subdivision and triangulation are done through
 * command objects which encapsulate undoable functions.
 */
public class Scene implements java.io.Serializable
{
    private FastVector m_primitives = new FastVector();
    private FastVector m_referenceVertices = new FastVector();
    private FastVector m_vertices = new FastVector();
    
    private SurfaceList m_surfaceList;

    private transient ViewContainer m_views;
    private FastVector m_clipVertices = new FastVector();
    private FastVector m_clipPrimitives = new FastVector();

    private FastVector m_orderedSelectedVertices = new FastVector();
    private FastVector m_orderedSelectedPrimitives = new FastVector();

    /**
     * Scene constructor.
     */
    public Scene()
    {
        m_surfaceList = new SurfaceList();

        Mode.resetDefaultMode();
    }

    /**
     * Make a copy of the scene.
     */
    public Object clone()
    {
        Scene newScene = new Scene();

        newScene.m_surfaceList = m_surfaceList;
        newScene.m_views = m_views;
        newScene.m_orderedSelectedVertices = m_orderedSelectedVertices;
        newScene.m_orderedSelectedPrimitives = m_orderedSelectedPrimitives;
        
        for(int i = 0; i < m_vertices.sizeFast(); i++)
        {
            newScene.m_vertices.addElement(m_vertices.elementAtFast(i));
            newScene.m_referenceVertices.addElement(m_referenceVertices.elementAtFast(i));
        }

        for(int i = 0; i < m_primitives.size(); i++)
        {
            newScene.m_primitives.addElement(((Primitive)m_primitives.elementAtFast(i)).clone());
        }

        return newScene;
    }
    
    /************************************************************
     * Functions that violate encapsulation:
     ************************************************************/
    
    /**
     * Get all the primitives in the scene.
     *
     * @return Vector of primitives
     */
    public final FastVector getPrimitivesVector()
    {
        return m_primitives;
    }

    /**
     * Get all the vertices in the scene.
     *
     * @return Vector of Vertices.
     */
    public final FastVector getVerticesVector()
    {
        return m_vertices;
    }

    public final FastVector getLights()
    {
        FastVector cameras = new FastVector();
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            if(m_primitives.elementAtFast(i) instanceof CameraView)
            {
                cameras.addElement(m_primitives.elementAtFast(i));
            }
        }

        return cameras;
    }

    /**
     * Get a list of cameras in the scene.
     */
    public final FastVector getCameras()
    {
        FastVector cameras = new FastVector();
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            if(m_primitives.elementAtFast(i) instanceof CameraView)
            {
                cameras.addElement(m_primitives.elementAtFast(i));
            }
        }

        return cameras;
    }
    
    /**
     * Returns the selected vertices.
     *
     * @return vector of selected vertices.
     */
    public final FastVector getSelectedVertices()
    {
        FastVector selected = new FastVector();
        for(int i = 0; i < m_orderedSelectedVertices.sizeFast(); i++)
        {
            Vertex v = (Vertex)m_orderedSelectedVertices.elementAtFast(i);
            selected.addElement(v);
        }

        return selected;
    }

    /**
     * Returns the selected primitives.
     *
     * @return vector of selected primitives.
     */
    public final FastVector getSelectedPrimitives()
    {
        FastVector selected = new FastVector();
        for(int i = 0; i < m_orderedSelectedPrimitives.sizeFast(); i++)
        {
            selected.addElement(m_orderedSelectedPrimitives.elementAtFast(i));
        }

        return selected;
    }

    /**
     * Remove all elements from the selected vertices list.
     */
    public final void emptyOrderedSelectedVertices()
    {
        m_orderedSelectedVertices.removeAllElements();
    }

    /**
     * Returns the list of selected vertices.
     */
    public final FastVector getOrderedSelectedVertices() 
    {
        FastVector ord = new FastVector();
        for(int i = 0; i < m_orderedSelectedVertices.sizeFast(); i++)
        {
            ord.addElement(m_orderedSelectedVertices.elementAtFast(i));
        }
        
        return  ord;
    }

    /**
     * Remove all elements from the selected Primitives list.
     */
    public final void emptyOrderedSelectedPrimitives()
    {
        m_orderedSelectedPrimitives.removeAllElements();
    }

    /**
     * Get the list of selected primitives.
     */
    public final FastVector getOrderedSelectedPrimitives()
    {
        FastVector ord = new FastVector();
        for(int i = 0; i < m_orderedSelectedPrimitives.size(); i++)
        {
            ord.addElement(m_orderedSelectedPrimitives.elementAtFast(i));
        }
        
        return  ord;
    }

    /**
     * Add a reference to a viewcontainer to the scene.
     * <p>
     * TODO: remove this, since it is out of place.
     */
    public final void addViewContainer(ViewContainer vc)
    {
        m_views = vc;
    }

    
    /**
     * Deselect all the vertices in the plane.
     */
    public final void deselectVertices()
    {
        emptyOrderedSelectedVertices();
        for(int i = m_vertices.size()-1; i >= 0; i--)
        {
            ((Vertex)m_vertices.elementAtFast(i)).setSelected(false);
        }
    }

    /**
     * Deselect all the primitives in the plane.
     */
    public final void deselectPrimitives()
    {
        emptyOrderedSelectedPrimitives();
        for(int i = m_primitives.size()-1; i >= 0; i--)
        {
            ((Primitive)m_primitives.elementAtFast(i)).setSelected(false);
        }
    }

    public final boolean verticesSelected()
    {
        return m_orderedSelectedVertices.sizeFast() > 0;
    }

    public final boolean primitivesSelected()
    {
        return m_orderedSelectedPrimitives.sizeFast() > 0;
    }
    
    /**
     * Get the total number of selected items in the scene.
     *
     * @return number of selected items in the scene.
     */
    public final int numSelected()
    {
        int count = 0;
        
        for(int i = m_vertices.size()-1; i >= 0; i--)
        {
            if( ((Vertex)m_vertices.elementAtFast(i)).isSelected())
            {
                count++;
            }
        }
        
        for(int i = m_primitives.size()-1; i >=0 ; i--)
        {
            if( ((Primitive)m_primitives.elementAtFast(i)).isSelected())
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Get the number of vertices in the scene.
     *
     * @return Number of total vertices in the scene.
     */
    public final int numVertices()
    {
        return m_vertices.size();
    }    

    /**
     * Add a vertex to the scene and then select it
     *
     * @param v Vertex to add.
     */
    public final void addSelectedVertex(Vertex v)
    {
        m_vertices.addElement(v);
        select(v);
    }

    /**
     * Get the index of a specific vertex.
     *
     * @param v Vertex to get index for.
     */
    public final int getIndex(Vertex v)
    {
        return m_vertices.lastIndexOf(v);
    }

    public final Spline makeSpline()
    {
        //Must be 4+ vertices selected to make a plane.
        if(m_orderedSelectedVertices.size() < 4)
        {
            return null;
        }
        else
        {
            //Check to make sure that we have selected vertices
            for(int i = 0; i < m_orderedSelectedVertices.size(); i++)
            {
                if(!(m_orderedSelectedVertices.elementAtFast(i) instanceof Vertex))
                {
                    return null;
                }
            }

            int indices[] = new int[m_orderedSelectedVertices.size()];
            int j = 0;
            for(int i = 0; i < m_orderedSelectedVertices.size(); i++)
            {
                if(getIndex((Vertex)m_orderedSelectedVertices.elementAtFast(i)) > -1)
                {
                    indices[j++] =
                        getIndex((Vertex)m_orderedSelectedVertices.elementAtFast(i));
                }
            }

            emptyOrderedSelectedVertices();

            //Create a new plane with the selected vertices.
            return new Spline(indices);
        }
    }

    /**
     * TODO: Pop up a dialog box on the exception -- splines must have
     * consistent # of vertices.
     */
    public final SplineSurface makeSplineSurface ()
    {
        if(m_orderedSelectedPrimitives.size() < 4)
        {
            MessageDialog dialog = new MessageDialog("Spline Surface must be made of at least 4 splines.");
            dialog.setVisible(true);
            return null;
        }
        else
        {
            //Check to make sure that we have selected vertices
            for(int i = 0; i < m_orderedSelectedPrimitives.size(); i++)
            {
                if(!(m_orderedSelectedPrimitives.elementAtFast(i) instanceof Spline))
                {
                    MessageDialog dialog = new MessageDialog("Only splines can be made into spline surfaces.");
                    dialog.setVisible(true);
                    return null;
                }
            }

            Spline temp[] = new Spline[m_orderedSelectedPrimitives.size()];
            int count = 0;
            for(int i = 0; i<m_orderedSelectedPrimitives.size(); i++)
            {
                if(m_orderedSelectedPrimitives.elementAtFast(i) instanceof Spline)
                {
                    temp[count++] = (Spline)m_orderedSelectedPrimitives.elementAtFast(i);
                }
            }
            if(count>=4)
            {
                //Check that all of the splines have same number of vertices
                int firstIndicesLength = temp[0].getIndices().length;
                for(int i = 0; i < temp.length; i++)
                {
                    if(temp[i].getIndices().length != firstIndicesLength)
                    {
                        MessageDialog dialog = new MessageDialog("All splines must have the same number of vertices.");
                        dialog.setVisible(true);
                        return null;
                    }
                }
                
                emptyOrderedSelectedPrimitives();
                return new SplineSurface(temp);
            }
        }

        return null;
    }
    /**
     * Add a primitive to the scene.
     */
    public final void addPrimitive(Primitive p)
    {
        //If we are adding a plane, compute the normal first and give
        //it the default surface.
        //TODO: Maybe move Surface to Primitive
        if(p instanceof Polygon3d)
        {
            if(((Polygon3d)p).getSurface() == null)
            {
                ((Polygon3d)p).setSurface(m_surfaceList.getSurface("Default"));
            }

            //if true was returned, we should not add!
            //Note: For the plane, the points were probably colinear and
            //therefore with 0 area.
            if(((Polygon3d)p).computeNormal(m_vertices))
            {
                return;
            }
        }
        
        m_primitives.addElement(p);
    }

    /**
     * Get this scene's surface list.
     *
     * @return SurfaceList associated with the scene.
     */
    public final SurfaceList getSurfaceList()
    {
        return m_surfaceList;
    }

    /**
     * Get the first surface of all of the selected items.
     *
     * @return Surface of first selected item.
     */
    public final Surface getSelectedSurface()
    {
        for(int i = 0; i < m_primitives.size(); i++)
        {
            Primitive element = (Primitive)m_primitives.elementAtFast(i);
            if(element.isSelected())
            {
                if(element instanceof Polygon3d)
                {
                    return ((Polygon3d)element).getSurface();
                }
                else if(element instanceof Group)
                {
                    return ((Group)element).getSurface();
                }
            }
        }
        
        return null;
    }

    /**
     * Set all of the selected primitives's surface.
     *
     * @param surf Surface to set selected primitives to.
     */
    public final void setSelectedSurface(Surface surf)
    {
        for(int i = 0; i < m_primitives.size(); i++)
        {
            Primitive element = (Primitive)m_primitives.elementAtFast(i);
            if(element.isSelected())
            {
                if(element instanceof Polygon3d)
                {
                    ((Polygon3d)element).setSurface(surf);
                }
                else if(element instanceof Group)
                {
                    ((Group)element).setSurface(surf);
                }
            }
        }
    }

    /**
     * Copy selected vertices or planes.  Copying planes will copy
     * the corresponding vertices as well.
     */
    public final void copySelected()
    {
        FastVector copiedOriginal = new FastVector();

        boolean triedToCopyGroup = false;
        for(int i = 0; i < m_orderedSelectedPrimitives.sizeFast(); i++)
        {
            if(m_orderedSelectedPrimitives.elementAtFast(i) instanceof Group)
            {
                triedToCopyGroup = true;
                break;
            }
        }

        if(triedToCopyGroup)
        {
            MessageDialog dialog = new MessageDialog("Sorry, copying groups not yet implemented");
            dialog.setVisible(true);
            return;
        }
        
        //Empty clipboard.
        m_clipVertices.removeAllElements();
        m_clipPrimitives.removeAllElements();

        //Copy vertices into clipboard.
        for(int i = 0; i < m_vertices.size(); i++)
        {
            Vertex vertex = (Vertex)m_vertices.elementAtFast(i);
            if(vertex.isSelected())
            {
                m_clipVertices.addElement(vertex.clone());
            }
        }

        //Copy primitives into clipboard.
        for(int i = 0; i < m_primitives.size(); i++)
        {
            Primitive prim = (Primitive)m_primitives.elementAtFast(i);
            if(prim.isSelected())
            {
                int indices[] = prim.getIndices();
                int clipIndices[] = new int[indices.length];
                
                for(int j = 0; j < indices.length; j++)
                {
                    Vertex orig = (Vertex)m_vertices.elementAtFast(indices[j]);
                    Vertex clipv = (Vertex)orig.clone();

                    //Only add vertex if it isn't already copied.
                    if(!copiedOriginal.contains(orig))
                    {
                        m_clipVertices.addElement(clipv);
                        copiedOriginal.addElement(orig);
                    }
                    clipIndices[j] = copiedOriginal.lastIndexOf(orig);
                }
                
                Primitive clipp = (Primitive)prim.clone();
                clipp.setIndices(clipIndices);
                
                m_clipPrimitives.addElement(clipp);
            }
        }
    }

    /**
     * Paste vertices or planes from clipboard into scene.  Pasting planes
     * causes new vertices to be added in the correct place to allow
     * creation of the plane.
     */
    public final FastVector paste()
    {
        FastVector addedElements = new FastVector();
        int lookup[] = new int[m_clipVertices.size()];

        for(int i = 0; i < m_clipVertices.size(); i++)
        {
            Vertex vertex = (Vertex)m_clipVertices.elementAtFast(i);
            Vertex v = (Vertex)vertex.clone();
            m_vertices.addElement(v);
            addedElements.addElement(v);
            v.setSelected(false);
            lookup[i] = getIndex(v);
        }

        //Paste primitives into the scene.
        for(int i = 0; i < m_clipPrimitives.size(); i++)
        {
            Primitive p = (Primitive)m_clipPrimitives.elementAtFast(i);
            Primitive clipPlane = (Primitive)p;
            int clipIndices[] = clipPlane.getIndices();
            int newIndices[] = new int[clipIndices.length];

            for(int j = 0; j < clipIndices.length; j++)
            {
                newIndices[j] = lookup[clipIndices[j]];
            }

            Primitive newplane = (Primitive)clipPlane.clone();
            newplane.setIndices(newIndices);
            
            addPrimitive(newplane);
            newplane.setSelected(false);
            addedElements.addElement(newplane);
        }

        return addedElements;
    }
    
    /**
     * Validate scene copies elements from the vertices
     * into the object's permanent vertices.  It also may renumber
     * indices after points have been deleted and delete faces when
     * one of the three points for the face has been deleted.
     */
    public final void validateScene()
    {
        while(m_referenceVertices.size() < m_vertices.size())
        {
            m_referenceVertices.addElement(new Vertex());
        }

        while(m_referenceVertices.size() > m_vertices.size())
        {
            m_referenceVertices.removeElementAt(0);
        }
        
        //        Vertex temp1, temp2;
        for(int i = 0; i < m_vertices.sizeFast(); i++)
        {
            ((Vertex)m_referenceVertices.elementAtFast(i)).set((Vertex)m_vertices.elementAtFast(i));
        }
        
        for(int i = 0; i < m_orderedSelectedVertices.sizeFast(); i++)
        {
            if(m_vertices.indexOf(m_orderedSelectedVertices.elementAtFast(i)) < 0)
            {
                m_orderedSelectedVertices.removeElementAt(i--);
            }
        }

        for(int i = 0; i < m_orderedSelectedPrimitives.sizeFast(); i++)
        {
            if(m_primitives.indexOf(m_orderedSelectedPrimitives.elementAtFast(i)) < 0)
            {
                m_orderedSelectedPrimitives.removeElementAt(i--);
            }
        }

        //Compute plane normals
        for(int i = 0; i < m_primitives.sizeFast(); i++)
        {
            if(m_primitives.elementAtFast(i) instanceof Polygon3d)
            {
                ((Polygon3d)m_primitives.elementAtFast(i)).computeNormal(m_vertices);
            }
        }

        updateViews();
    }

    /**
     * Make a plane out of all the selected vertices.
     */
    public final Polygon3d makePolygon()
    {
        //Must be 3+ vertices selected to make a plane.
        if(m_orderedSelectedVertices.size() < 3)
        {
            return null;
        }
        else
        {
            //Check to make sure that we have selected vertices
            for(int i = 0; i < m_orderedSelectedVertices.size(); i++)
            {
                if(!(m_orderedSelectedVertices.elementAtFast(i) instanceof Vertex))
                {
                    return null;
                }
            }
             
            int indices[] = new int[m_orderedSelectedVertices.size()];
            int j = 0;
            for(int i = 0; i < m_orderedSelectedVertices.size(); i++)
            {
                if(getIndex((Vertex)m_orderedSelectedVertices.elementAtFast(i)) >= 0)
                {
                    indices[j++] =
                        getIndex((Vertex)m_orderedSelectedVertices.elementAtFast(i));
                }
            }

            emptyOrderedSelectedVertices();

            //Create a new plane with the selected vertices.
            return new Polygon3d(indices);
        }
    }
    
    public final void select(Vertex v)
    {
        v.setSelected(true);
        m_orderedSelectedVertices.addElement(v);
    }

    public final void deselect(Vertex v)
    {
        v.setSelected(false);
        m_orderedSelectedVertices.removeElement(v);
    }
    
    public final void select(Primitive p)
    {
        p.setSelected(true);
        m_orderedSelectedPrimitives.addElement(p);
    }

    public final void deselect(Primitive p)
    {
        p.setSelected(false);
        m_orderedSelectedPrimitives.removeElement(p);
    }
    
    /**
     * Show the statistics for the scene using a message dialog box.
     */
    public final void showStatistics()
    {
        String message
            = new String("Number vertices: " + m_vertices.size() + "\n" +
                         "Number primitives: " + m_primitives.size() + "\n" +
                         "Number selected: " + numSelected());
        MessageDialog dialog = new MessageDialog(message);
        dialog.setVisible(true);
    }
    
    /**
     * Transform the selected vertices or primitives.
     *
     * @param trans Transformation to apply to selected vertices or primitives
     */
    public final void transformSelected(Transformation trans)
    {
        //Loop through vertices and transform if selected.
        if(m_orderedSelectedVertices.sizeFast() > 0)
        {
            Vertex vertex;
            for(int i = m_vertices.sizeFast()-1; i >=0; --i)
            {
                vertex = (Vertex)m_vertices.elementAtFast(i);
                if(vertex.isSelected())
                {
                    trans.transformVertex((Vertex)m_referenceVertices.elementAtFast(i),
                                          vertex);
                }
            }
        }
        else if(m_orderedSelectedPrimitives.sizeFast() > 0)
        {
            //Loop through planes and transform if selected.
            Primitive prim;
            for(int i = m_primitives.size()-1; i >=0; --i)
            {
                prim = (Primitive)m_primitives.elementAtFast(i);
                if(prim.isSelected())
                {
                    prim.transform(trans, m_referenceVertices, m_vertices);
                }
            }
        }
    }

    public final void removePrimitive(Primitive p)
    {
        m_primitives.removeElement(p);
        m_orderedSelectedPrimitives.removeElement(p);
    }
    
    /**
     * Inform all the views that a change has occured and they should
     * update at their leisure.
     */
    public final void updateViews()
    {
        if(m_views != null)
        {
            m_views.setChanged();
        }
    }

    public final void forceRepaint()
    {
//          System.out.println("Force Repaint");
        m_views.forceRepaint();
    }

    public final void addVertex(Vertex v)
    {
        m_vertices.addElement(v);
    }

    public final void removeVertex(Vertex v)
    {
        //Each time we delete a vertex, we need to make sure
        //no primitives are indexing into it.  If they are,
        //renumber the primitive's indices.
        int i = m_vertices.lastIndexOf(v);
        if(i >= 0)
        {
            for(int j =  m_primitives.size()-1; j >=0 ; j--)
            {
                Primitive p = (Primitive)m_primitives.elementAtFast(j);
                boolean del = p.renumberIndices(i);
                if(del)
                {
                    m_primitives.removeElementAt(j);
                }
            }
            m_orderedSelectedVertices.removeElement(v);
            m_vertices.removeElementAt(i);
        }
        
    }

    public final boolean canCopy()
    {
        return numSelected() > 0;
    }

    public final boolean canPaste()
    {
        return m_clipVertices.size() > 0 || m_clipPrimitives.size() > 0;
    }

    public final void removeAllContents()
    {
        Mode.resetDefaultMode();
        stomp.command.CommandExecutor.clear();
        m_primitives.removeAllElements();
        m_referenceVertices.removeAllElements();
        m_vertices.removeAllElements();
        m_surfaceList = new SurfaceList();
        m_clipVertices.removeAllElements();
        m_clipPrimitives.removeAllElements();
        m_orderedSelectedVertices.removeAllElements();
        m_orderedSelectedPrimitives.removeAllElements();
        m_views.setCamera(null);
        m_views.setPerspective(ViewContainer.PERSPECTIVE);
    }
}

