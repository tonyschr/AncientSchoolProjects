package stomp.command;

import stomp.*;
import stomp.gui.*;
import stomp.data3d.*;
import java.util.*;

import java.awt.event.*;
import java.awt.*;
import java.text.*;
import javax.vecmath.Vector3f;

/**
 * Extrude the selected polygons 1 unit in the direction of the
 * polygon's normal.
 */
public class ExtrudeCommand extends Frame implements Command, ActionListener
{
    private TextField m_extrudeAmount = new TextField(8);
    private TextField m_segments = new TextField(8);    
    private static float m_extrudeAmountNum = 1.0f;
    private static int m_segmentsNum = 4;
    
    private Scene m_scene;
    private Command m_deselectCommand;
    private FastVector m_addedElements;
    private FastVector m_oldPolygon3ds;
    private boolean splines = false;


    private ExtrudeCommand()
    {
    }
    
    public ExtrudeCommand(Scene scene)
    {
        m_scene = scene;
        m_deselectCommand = new DeselectAllCommand(m_scene);
    }

    public boolean execute()
    {
        int numFields = 2;
        FastVector selectedPrimitives = m_scene.getOrderedSelectedPrimitives();
        for(int i = 0; i < selectedPrimitives.sizeFast(); i++)
        {
            if(selectedPrimitives.elementAtFast(i) instanceof Spline)
            {
                splines = true;
                numFields = 3;
            }
        }
        
        removeAll();

        setBackground(SystemColor.control);
        setFont(Appearance.getFont());
        setLayout(new GridLayout(numFields,2));
        
        add(new Label("Amount"));
        add(m_extrudeAmount);
        m_extrudeAmount.setText("" + m_extrudeAmountNum);

        if(splines)
        {
            add(new Label("Segments"));
            add(m_segments);
            m_segments.setText("" + m_segmentsNum);

        }
        
        Button ok = new Button("Ok");
        add(ok);
        ok.addActionListener(this);
        Button cancel = new Button("Cancel");
        add(cancel);
        cancel.addActionListener(this);

        validate();
        pack();
        setVisible(true);

        return true;
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        
        if(cmd.equals("Ok"))
        {
            performExtrude();
        }

        dispose();
    }
    
    public void performExtrude()
    {
        try
        {
            NumberFormat numFormat = new DecimalFormat();
            m_extrudeAmountNum = numFormat.parse(m_extrudeAmount.getText()).floatValue();

            if(splines)
            {
                m_segmentsNum = numFormat.parse(m_segments.getText()).intValue();
                if(m_segmentsNum < 4)
                {
                    m_segmentsNum = 4;
                }
                else if(m_segmentsNum > 30)
                {
                    m_segmentsNum = 30;
                }
            }
        }
        catch(ParseException e)
        {
            return;
        }
        m_addedElements = new FastVector();
        m_oldPolygon3ds = new FastVector();
        //Vector primitives = m_scene.getPrimitivesVector();
        FastVector selectedPrimitives = m_scene.getOrderedSelectedPrimitives();
        FastVector transformedVertices = m_scene.getVerticesVector();

        Stomp.statusBar.startProgress("Extruding selected Polygons...",
                                      100.0/selectedPrimitives.size());
        for(int i = 0; i < selectedPrimitives.size(); i++)
        {
            Primitive prim = (Primitive)selectedPrimitives.elementAt(i);
            if(prim instanceof Polygon3d)
            {
                //Get the indices to the Polygon3d
                Polygon3d poly = (Polygon3d)prim;
                Surface surf = poly.getSurface();
                m_oldPolygon3ds.addElement(poly);
                
                int indices[] = poly.getIndices();

                FastVector oldVertices = new FastVector();
                FastVector newVertices = new FastVector();

                Vector3f extAmount = poly.getNormal();
                extAmount.x = extAmount.x * m_extrudeAmountNum;
                extAmount.y = extAmount.y * m_extrudeAmountNum;
                extAmount.z = extAmount.z * m_extrudeAmountNum;
                
                //Copy vertices for the Polygon3d into two different vectors.
                for(int j = 0; j < indices.length; j++)
                {
                    Vertex v = (Vertex)transformedVertices.elementAtFast(indices[j]);
                    Vertex v2 = (Vertex)v.clone();
                    m_addedElements.addElement(v2);
                    v2.add(extAmount);
                    
                    oldVertices.addElement(v);
                    newVertices.addElement(v2);                    
                }

                //Add vertices for the extruded part.
                int newIndices[] = new int[newVertices.size()];
                for(int j = 0; j < newVertices.size(); j++)
                {
                    m_scene.addVertex((Vertex)newVertices.elementAt(j));
                    
                    indices[j] = m_scene.getIndex((Vertex)oldVertices.elementAtFast(j));
                    newIndices[j] = m_scene.getIndex((Vertex)newVertices.elementAtFast(j));
                }

                //Add new Polygon3ds to form the sides of the extrusion.
                for(int j = 0; j < indices.length; j++)
                {
                    int j2 = (j + 1)%indices.length;
                    Polygon3d p = new Polygon3d(indices[j], indices[j2],
                                        newIndices[j2], newIndices[j]);
                    p.setSurface(surf);
                    
                    m_scene.addPrimitive(p);
                    m_addedElements.addElement(p);
                }

                //Add a Polygon3d for the top and flip it to face the
                //right direction.
                Polygon3d newTop = new Polygon3d(newIndices);
                newTop.setSurface(surf);
                poly.flip();
                
                m_scene.addPrimitive(newTop);
                m_addedElements.addElement(newTop);
            }
            else if(prim instanceof Spline)
            {
                Spline[] splines = new Spline[m_segmentsNum];
                splines[0] = (Spline)prim;
                m_scene.removePrimitive(prim);
                m_oldPolygon3ds.addElement(prim);
                //Get the indices to the Polygon3d
                //Spline spl = (Spline)prim;
                
                int indices[] = splines[0].getIndices();

                Polygon3d tempp = new Polygon3d(indices);
                tempp.computeNormal(m_scene.getVerticesVector());
                
                Vector3f extAmount = tempp.getNormal();
                float ex = extAmount.x = extAmount.x * m_extrudeAmountNum/m_segmentsNum;
                float ey = extAmount.y = extAmount.y * m_extrudeAmountNum/m_segmentsNum;
                float ez = extAmount.z = extAmount.z * m_extrudeAmountNum/m_segmentsNum;

                for(int newsplines = 1; newsplines < m_segmentsNum; newsplines++)
                {
                    FastVector oldVertices = new FastVector();
                    FastVector newVertices = new FastVector();
                    //Copy vertices for the spline into two different vectors.
                    for(int j = 0; j < indices.length; j++)
                    {
                        Vertex v = (Vertex)transformedVertices.elementAtFast(indices[j]);
                        Vertex v2 = (Vertex)v.clone();
                        m_addedElements.addElement(v2);
                        v2.add(extAmount);
                        
                        oldVertices.addElement(v);
                        newVertices.addElement(v2);                    
                    }

                    //Add vertices for the extruded part.
                    int newIndices[] = new int[newVertices.size()];
                    for(int j = 0; j < newVertices.size(); j++)
                    {
                        m_scene.addVertex((Vertex)newVertices.elementAt(j));
                        
                        indices[j] = m_scene.getIndex((Vertex)oldVertices.elementAtFast(j));
                        newIndices[j] = m_scene.getIndex((Vertex)newVertices.elementAtFast(j));
                    }

                    splines[newsplines] = new Spline(newIndices);
                    //Spline nextSpline = new Spline(newIndices);
                    //m_scene.addPrimitive(nextSpline);

                    extAmount.x += ex;
                    extAmount.y += ey;
                    extAmount.z += ez;
                }

                m_scene.addPrimitive(new SplineSurface(splines));
            }

            Stomp.statusBar.incrementProgress();
        }
        
        m_deselectCommand.execute();
        
        m_scene.validateScene();
    }

    /**
     * Cannot fully unexecute Extrude for now.
     */
    public void unExecute()
    {
        //First, delete all the new Polygon3ds
        for(int i = 0; i < m_addedElements.sizeFast(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Primitive)
            {
                m_scene.removePrimitive((Primitive)m_addedElements.elementAtFast(i));
            }
        }

        //Then, it's safe to kill all of the vertices.
        for(int i = 0; i < m_addedElements.sizeFast(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Vertex)
            {
                m_scene.removeVertex((Vertex)m_addedElements.elementAtFast(i));
            }
        }

        //        m_scene.addPrimitive(m_oldPolygon3ds);

        for(int i = 0; i < m_oldPolygon3ds.sizeFast(); i++)
        {
            Primitive p = (Primitive)m_oldPolygon3ds.elementAtFast(i);
            if(p instanceof Polygon3d)
            {
                ((Polygon3d)p).flip();
            }
            else if(p instanceof Spline)
            {
                m_scene.addPrimitive(p);
                m_scene.select(p);
            }
        }

        m_deselectCommand.unExecute();
        
        m_scene.validateScene();
    }

    public String toString()
    {
        return "Extrude Selected";
    }

}
