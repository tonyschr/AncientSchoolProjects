package stomp.gui;

import stomp.data3d.*;
import stomp.command.MakeShapeCommand;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

/**
 * Primitive Dialog creates a simple button dialog to allow the user to
 * select one of a number of predefined shapes.  The selection is stored
 * in a member variable to allow the owner to retrieve the result.
 */
public class PrimitiveDialog extends Frame implements ActionListener
{
    public static final int SPHERE = 1;
    public static final int CYLINDER = 2;
    public static final int CUBE = 3;
    public static final int PLANE = 4;
    public static final int CONE = 5;
    public static final int SURFACE = 6;
    public static final int CLOSED_SURFACE = 7;
    public static final int TESS_SPHERE = 8;
    public static final int CANCEL = 0;
    
    private int m_result = 5;
    ActionListener m_actionListener = null;

    private TextField m_divisions = new TextField(10);
    private TextField m_sections = new TextField(10);

    //Constructor takes no arguments at this time, only calls the layout
    //method
    public PrimitiveDialog ()
    {
        super("Add Primitive");
        layoutComponents();
    }
    
    /*This is the method that creates and lays out the buttons */
    private void layoutComponents()
    {
        setBackground(SystemColor.control);
        setFont(Appearance.getFont());
      
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets (5,5,5,5);

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;

        Button sphereButton = new Button ("Sphere");
        add (sphereButton, gbc);
        sphereButton.addActionListener (this);

        gbc.gridy++;
        Button sphereButton2 = new Button ("Tesselated Sphere");
        add (sphereButton2, gbc);
        sphereButton2.addActionListener (this);

        gbc.gridy++;
        Button cylinderButton = new Button ("Cylinder");
        add (cylinderButton, gbc);
        cylinderButton.addActionListener (this);

        gbc.gridy++;
        Button coneButton = new Button ("Cone");
        add (coneButton, gbc);
        coneButton.addActionListener (this);

        gbc.gridy++;
        Button cubeButton = new Button ("Cube");
        add (cubeButton, gbc);
        cubeButton.addActionListener (this);

        gbc.gridy++;
        Button planeButton = new Button ("Plane");
        add (planeButton, gbc);
        planeButton.addActionListener (this);

        gbc.gridy++;
        Button surfaceButton = new Button ("Spline Surface");
        add (surfaceButton, gbc);
        surfaceButton.addActionListener (this);

        gbc.gridy++;
        Button surfaceButton2 = new Button ("Closed Spline Surface");
        add (surfaceButton2, gbc);
        surfaceButton2.addActionListener (this);

        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new Label("Sides/Divisions"), gbc);
        gbc.gridx++;
        m_divisions.setText("" + MakeShapeCommand.DIVISIONS);
        add(m_divisions, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(new Label("Sections"), gbc);
        gbc.gridx++;
        m_sections.setText("" + MakeShapeCommand.SECTIONS);
        add(m_sections, gbc);
        
        gbc.gridy++;
        Button cancelButton = new Button ("Cancel");
        add (cancelButton, gbc);
        cancelButton.addActionListener (this);
        
        //Validate the controls and pack to the minimum size.
        validate();
        pack();

        //Pack creates a window with sliders that are too small,
        //make the window a little wider, but not higher.  (The height
        //may change depending on the platform it is run on.)
        setSize(250, getSize().height);

    }

    //Creates a new listener to be registered as the button listener
    public void addActionListener(ActionListener listener)
    {
        m_actionListener = listener;
    }
    //This method is called when an action is performed, and correctly deals
    //with the action
    public void actionPerformed(ActionEvent e)
    {
        String arg = e.getActionCommand(); 
        if ("Cancel".equals (arg))
        {
            m_result = PrimitiveDialog.CANCEL;
            dispose();
        }
        else
        {
            NumberFormat numFormat = new DecimalFormat();

            try
            {
                int divisions = numFormat.parse(m_divisions.getText()).intValue();
                int sections = numFormat.parse(m_sections.getText()).intValue();

                if(divisions < 2)
                {
                    divisions = 2;
                }
                if(sections < 1)
                {
                    sections = 1;
                }
                
                MakeShapeCommand.DIVISIONS = divisions;
                MakeShapeCommand.SECTIONS = sections;

            }
            catch(NumberFormatException ex)
            {
            }
            catch(ParseException ex)
            {
            }
            
            if("Sphere".equals(arg))
            {
                m_result = PrimitiveDialog.SPHERE;
                if(m_actionListener != null)
                {
                    m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                     "DialogCloseOk"));
                }            
                dispose ();
            }
            if("Tesselated Sphere".equals(arg))
            {
                m_result = PrimitiveDialog.TESS_SPHERE;
                if(m_actionListener != null)
                {
                    m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                     "DialogCloseOk"));
                }            
                dispose ();
            }
            else if ("Cylinder".equals (arg))
            {
                m_result = PrimitiveDialog.CYLINDER;
                if(m_actionListener != null)
                {
                    m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                     "DialogCloseOk"));
                }

                dispose();
            }
            else if ("Cone".equals (arg))
            {
                m_result = PrimitiveDialog.CONE;
                if(m_actionListener != null)
                {
                    m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                     "DialogCloseOk"));
                }

                dispose();
            }
            else if ("Cube".equals (arg))
            {
                m_result = PrimitiveDialog.CUBE;
                if(m_actionListener != null)
                {
                    m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                     "DialogCloseOk"));
                }

                dispose();
            }
            else if ("Plane".equals (arg))
            {
                m_result = PrimitiveDialog.PLANE;
                if (m_actionListener != null)
                {
                    m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                     "DialogCloseOk"));
                }
                dispose();
            }
            else if ("Spline Surface".equals (arg))
            {
                m_result = PrimitiveDialog.SURFACE;
                if(m_actionListener != null)
                {
                    m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                     "DialogCloseOk"));
                }

                dispose();
            }
            else if ("Closed Spline Surface".equals (arg))
            {
                m_result = PrimitiveDialog.CLOSED_SURFACE;
                if(m_actionListener != null)
                {
                    m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                     "DialogCloseOk"));
                }

                dispose();
            }
        }   
    }
    //A public method to return the result stored in a private member variable
    
    public int getResult()
    {
        return m_result;
    }

    //A simple main function to allow testing of the dialog
    public static void main (String[] args){
        PrimitiveDialog dlg = new PrimitiveDialog ();
        dlg.setVisible (true);
        while(dlg.getResult() > PrimitiveDialog.PLANE )
        {

            try
                {
                    Thread.sleep(200);
                }
            catch(InterruptedException e)
                {
                    break;
                }

        }
        if (dlg.getResult() <= PrimitiveDialog.PLANE)
        {
            System.out.println("User Picked "+ dlg.getResult());
            System.exit(0);
        }
    }

}





