package stomp.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import stomp.data3d.*;
import stomp.Mode;
import stomp.transform.*;
import stomp.command.*;

/**
 * This class is a dialog which can display slightly different dialog
 * boxes for the different numeric options dialog box.
 */
public class Numeric extends Frame implements ActionListener
{
    private Scene m_scene;
    private int m_mode; //mode when dialog was created, in case it changes
    private static int m_lastMode = -1;
    private Panel m_panel = new Panel();
    
    private TextField m_xText = new TextField(10);
    private TextField m_yText = new TextField(10);
    private TextField m_zText = new TextField(10);
    private TextField m_axText = new TextField(10);
    private TextField m_ayText = new TextField(10);
    private TextField m_azText = new TextField(10);
    private TextField m_angleText = new TextField(10);

    private static float x = 0;
    private static float y = 0;
    private static float z = 0;
    private static float ax = 0;
    private static float ay = 0;
    private static float az = 0;
    private static float angle = 0;
    
    /**
     * Create the dialog box.  Needs scene to carry out operations after
     * "ok" has been clicked.
     */
    public Numeric(Scene scene)
    {
        //Set up the dialog
        super("Numeric Options");
        setBackground(SystemColor.control);
        setFont(Appearance.getFont());

        m_scene = scene;
        m_mode = Mode.getMode();

        //Set the default values.  Keep the previous settings if user
        //is doing the same thing as the last time, otherwise reset
        if(m_mode != m_lastMode)
        {
            x = 0;
            y = 0;
            z = 0;
            ax = 0;
            ay = 0;
            az = 0;
            angle = 0;
            m_lastMode = m_mode;
        }
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridwidth = 2;

        //Depending on the current mode, show different dialogs.
        if(m_mode == Mode.ADD_POINTS)
        {
            add(new Label("Add Point"), gbc);
            gbc.gridy++;
            addPoints();
        }
        else if(m_mode == Mode.TRANSLATE)
        {
            add(new Label("Translate"), gbc);
            gbc.gridy++;
            translate();
        }
        else if(m_mode == Mode.ROTATE)
        {
            add(new Label("Rotate"), gbc);
            gbc.gridy++;
            rotate();
        }
        else if(m_mode == Mode.SCALE)
        {
            add(new Label("Scale"), gbc);
            gbc.gridy++;
            scale();
        }
        else
        {
            add(new Label("No numeric options for this item."));
        }

        //Add the created panel.
        add(m_panel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;

        //Add ok and cancel buttons
        Button ok = new Button("Ok");
        ok.addActionListener(this);
        add(ok, gbc);

        gbc.gridx++;
        Button cancel = new Button("Cancel");
        cancel.addActionListener(this);
        add(cancel, gbc);

        validate();
        pack();
    }

    /**
     * Add buttons and text entry boxes for creating points
     * by typing in the coordinates.
     */
    private void addPoints()
    {
        m_panel.setLayout(new GridLayout(3, 2));

        m_xText.setText("" + x);
        m_yText.setText("" + y);
        m_zText.setText("" + z);
        
        m_panel.add(new Label("X", Label.CENTER));
        m_panel.add(m_xText);
        m_panel.add(new Label("Y", Label.CENTER));
        m_panel.add(m_yText);
        m_panel.add(new Label("Z", Label.CENTER));
        m_panel.add(m_zText);
    }

    /**
     * Add buttons and text entry boxes for translating points
     * and primitives by typing in the coordinates.
     */
    private void translate()
    {
        m_panel.setLayout(new GridLayout(3, 2));

        m_xText.setText("" + x);
        m_yText.setText("" + y);
        m_zText.setText("" + z);
        
        m_panel.add(new Label("X", Label.CENTER));
        m_panel.add(m_xText);
        m_panel.add(new Label("Y", Label.CENTER));
        m_panel.add(m_yText);
        m_panel.add(new Label("Z", Label.CENTER));
        m_panel.add(m_zText);        
    }

    /**
     * Add buttons and text entry boxes for rotating points
     * and primitives by typing in the coordinates.
     */
    public void rotate()
    {
        m_panel.setLayout(new GridLayout(4, 4));

        m_xText.setText("" + x);
        m_yText.setText("" + y);
        m_zText.setText("" + z);
        m_axText.setText("" + ax);
        m_ayText.setText("" + ay);
        m_azText.setText("" + az);
        m_angleText.setText("" + angle);
        
        m_panel.add(new Label("Center X", Label.CENTER));
        m_panel.add(m_xText);
        m_panel.add(new Label("Axis X", Label.CENTER));
        m_panel.add(m_axText);

        m_panel.add(new Label("Center Y", Label.CENTER));
        m_panel.add(m_yText);
        m_panel.add(new Label("Axis Y", Label.CENTER));
        m_panel.add(m_ayText);
        
        m_panel.add(new Label("Center Z", Label.CENTER));
        m_panel.add(m_zText);
        m_panel.add(new Label("Axis Z", Label.CENTER));
        m_panel.add(m_azText);

        m_panel.add(new Label("Angle", Label.CENTER));
        m_panel.add(m_angleText);
    }

    /**
     * Add buttons and text entry boxes for scaling points
     * and primitives by typing in the coordinates.
     */
    public void scale()
    {
        m_panel.setLayout(new GridLayout(3, 4));

        m_xText.setText("" + x);
        m_yText.setText("" + y);
        m_zText.setText("" + z);
        m_axText.setText("1.0");
        m_ayText.setText("1.0");
        m_azText.setText("1.0");
        
        m_panel.add(new Label("Center X", Label.CENTER));
        m_panel.add(m_xText);
        m_panel.add(new Label("Amount X", Label.CENTER));
        m_panel.add(m_axText);

        m_panel.add(new Label("Center Y", Label.CENTER));
        m_panel.add(m_yText);
        m_panel.add(new Label("Amount Y", Label.CENTER));
        m_panel.add(m_ayText);
        
        m_panel.add(new Label("Center Z", Label.CENTER));
        m_panel.add(m_zText);
        m_panel.add(new Label("Amount Z", Label.CENTER));
        m_panel.add(m_azText);
    }

    /**
     * When 'ok' is clicked, parse the text entry boxes and
     * do the correct operation.  If 'cancel' is clicked, close
     * the dialog box and do nothing.
     */
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        
        if(cmd.equals("Ok"))
        {
            NumberFormat numFormat = new DecimalFormat();
            if(m_mode == Mode.ADD_POINTS)
            {
                try
                {
                    x = numFormat.parse(m_xText.getText()).floatValue();
                    y = numFormat.parse(m_yText.getText()).floatValue();
                    z = numFormat.parse(m_zText.getText()).floatValue();
                    CommandExecutor.execute(new AddVertexCommand(m_scene,
                                                         new Vertex(x, y, z)));
                    
                    //                    m_scene.addVertex(new Vertex(x, y, z));
                }
                catch(NumberFormatException ex)
                {
                }
                catch(ParseException ex)
                {
                }
            }
            else if(m_mode == Mode.TRANSLATE)
            {
                try
                {
                    x = numFormat.parse(m_xText.getText()).floatValue();
                    y = numFormat.parse(m_yText.getText()).floatValue();
                    z = numFormat.parse(m_zText.getText()).floatValue();
                    
                    Translation trans = new Translation();
                    trans.setTranslation(x, y, z);
                    CommandExecutor.execute(new TransformCommand(m_scene,
                                                                 trans));
//                     m_scene.transformSelected(trans);
//                     m_scene.validateScene();
//                     m_scene.updateViews();
                }
                catch(NumberFormatException ex)
                {
                }
                catch(ParseException ex)
                {
                }
            }
            else if(m_mode == Mode.ROTATE)
            {
                try
                {
                    x = numFormat.parse(m_xText.getText()).floatValue();
                    y = numFormat.parse(m_yText.getText()).floatValue();
                    z = numFormat.parse(m_zText.getText()).floatValue();
                    ax = numFormat.parse(m_axText.getText()).floatValue();
                    ay = numFormat.parse(m_ayText.getText()).floatValue();
                    az = numFormat.parse(m_azText.getText()).floatValue();
                    angle = 
                        numFormat.parse(m_angleText.getText()).floatValue();

                    //Convert to radians.
                    float radangle = (float)((angle / 180) * Math.PI);

                    if( (angle < -.001 || angle > .001) &&
                        (ax + ay + az > .05))
                    {
                        Rotation trans = new Rotation();
                        trans.setRotation(x, y, z, ax, ay, az, radangle);
                        CommandExecutor.execute(new TransformCommand(m_scene,
                                                                     trans));
                    }
                }
                catch(NumberFormatException ex)
                {
                }
                catch(ParseException ex)
                {
                }
            }
            else if(m_mode == Mode.SCALE)
            {
                try
                {
                    x = numFormat.parse(m_xText.getText()).floatValue();
                    y = numFormat.parse(m_yText.getText()).floatValue();
                    z = numFormat.parse(m_zText.getText()).floatValue();
                    ax = numFormat.parse(m_axText.getText()).floatValue();
                    ay = numFormat.parse(m_ayText.getText()).floatValue();
                    az = numFormat.parse(m_azText.getText()).floatValue();

                    if((ax < -.001 || ax > .001) &&
                       (ay < -.001 || ay > .001) &&
                       (az < -.001 || az > .001))
                    {
                        Scale trans = new Scale();
                        trans.setScale(x, y, z, ax - 1, ay - 1, az - 1);
                        CommandExecutor.execute(new TransformCommand(m_scene,
                                                                     trans));
                    }
                }
                catch(NumberFormatException ex)
                {
                }
                catch(ParseException ex)
                {
                }
            }

            dispose();
        }
        else if(cmd.equals("Cancel"))
        {
            dispose();
        }
    }
}
