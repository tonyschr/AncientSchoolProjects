package stomp.command;

import stomp.*;
import stomp.gui.*;
import stomp.data3d.*;
import stomp.transform.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

/**
 * Lathe.  Spin selected points around any axis and create
 * polygons connecting them.
 * TODO: Rename this class since it is no longer just around the
 * Y axis.
 */
public class LatheYCommand extends Frame implements Command, ActionListener
{
    private static int LATHE_DIVISIONS = 16;

    private TextField m_latheDivisions = new TextField(8);
    private TextField m_xAxisField = new TextField(8);
    private TextField m_yAxisField = new TextField(8);
    private TextField m_zAxisField = new TextField(8);
    
    private Scene m_scene;
    private FastVector m_addedElements = new FastVector();

    private LatheYCommand()
    {
    }
    
    public LatheYCommand(Scene scene)
    {
        super("Lathe (any axis)");
        m_scene = scene;
    }
    
    public boolean execute()
    {
        removeAll();

        setBackground(SystemColor.control);
        setFont(Appearance.getFont());
        setLayout(new GridLayout(5,2));
        
        add(new Label("Subdivisions"));
        add(m_latheDivisions);
        m_latheDivisions.setText("" + LATHE_DIVISIONS);

        add(new Label("X Axis"));
        add(m_xAxisField);
        m_xAxisField.setText("0");
        
        add(new Label("Y Axis"));
        add(m_yAxisField);
        m_yAxisField.setText("1");
        
        add(new Label("Z Axis"));
        add(m_zAxisField);
        m_zAxisField.setText("0");
        
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
            performLathe();
        }

        dispose();
    }
    
    private void performLathe()
    {
        float xAxis, yAxis, zAxis;
        try
        {
            NumberFormat numFormat = new DecimalFormat();
            xAxis = numFormat.parse(m_xAxisField.getText()).floatValue();
            yAxis = numFormat.parse(m_yAxisField.getText()).floatValue();
            zAxis = numFormat.parse(m_zAxisField.getText()).floatValue();
            LATHE_DIVISIONS = numFormat.parse(m_latheDivisions.getText()).intValue();
        }
        catch(ParseException e)
        {
            return;
        }
        
        m_addedElements = new FastVector();
        FastVector vertices = m_scene.getVerticesVector();

        int numSelected = 0;
        for(int i = vertices.size()-1; i >= 0; i--)
        {
            if( ((Vertex)vertices.elementAtFast(i)).isSelected())
            {
                numSelected++;
            }
        }

        int inds[][] = new int[LATHE_DIVISIONS][numSelected];

        int i = 0;
        int j = 0;

        Rotation rotation = new Rotation();

        FastVector selectedVerts = m_scene.getOrderedSelectedVertices();
        
        //First indicies are original points.
        for(int t = 0; t < selectedVerts.size(); t++)
        {
            Vertex v = (Vertex)selectedVerts.elementAtFast(t);
            //if(v.isSelected())
            //{
                inds[j][i++] = m_scene.getIndex(v);
                //}
        }

        j++;
        i = 0;
        
        float rotate = 0;
        for(int d = 1; d < LATHE_DIVISIONS; d++)
        {
            rotate += 2 * Math.PI/LATHE_DIVISIONS;
            Vertex tempNew[] = new Vertex[numSelected];

            rotation.setRotation(0, 0, 0,
                                 xAxis, yAxis, zAxis, rotate);
            
            //Spin the points.
            for(int t = 0; t < numSelected; t++)
            {
                Vertex vold = (Vertex)vertices.elementAtFast(inds[0][t]);
                tempNew[t] = new Vertex();
                rotation.transformVertex(vold, tempNew[t]);
                m_scene.addVertex(tempNew[t]);
                m_addedElements.addElement(tempNew[t]);

                inds[j][t] = m_scene.getIndex(tempNew[t]);
            }
            
            i = 0;
            j++;
        }

        for(int s = 0; s < LATHE_DIVISIONS; s++)
        {
            int num = numSelected;
            for(int n = 0; n < num-1; n++)
            {
                int ind[] = new int[4];
                
                ind[0] = inds[s][n];
                ind[1] = inds[s][(n+1)];
                ind[2] = inds[(s+1)%LATHE_DIVISIONS][(n+1)];
                ind[3] = inds[(s+1)%LATHE_DIVISIONS][n];
                
                Polygon3d side = new Polygon3d(ind);
                m_scene.addPrimitive(side);
                m_addedElements.addElement(side);
            }
        }
        
        m_scene.validateScene();
    }

    public void unExecute()
    {
        //First, delete all the new Polygon3ds
        for(int i = 0; i < m_addedElements.size(); i++)
        {
            if(m_addedElements.elementAtFast(i) instanceof Polygon3d)
            {
                m_scene.removePrimitive((Primitive)m_addedElements.elementAtFast(i));
            }
        }

        //Then, it's safe to kill all of the vertices.
         for(int i = 0; i < m_addedElements.size(); i++)
         {
             if(m_addedElements.elementAtFast(i) instanceof Vertex)
             {
                 m_scene.removeVertex((Vertex)m_addedElements.elementAtFast(i));
               }
         }

        m_scene.validateScene();
    }

    public String toString()
    {
        return "Lathe Y Axis";
    }
}
