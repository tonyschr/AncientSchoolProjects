package stomp.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import stomp.*;
import stomp.data3d.*;
import stomp.Mode;
import stomp.transform.*;
import stomp.view.Grid;

/**
 * Options dialog box.
 *
 * Options dialog box contains settings to change Stomp-wide preferences.
 */
public class Options extends Frame implements ActionListener, ItemListener
{
    private Scene m_scene;
    private Panel m_panel = new Panel();

    private TextField m_previewProg = new TextField(15);
    private TextField m_previewPath = new TextField(15);
    private TextField m_stack = new TextField(10);
    private TextField m_subdivide = new TextField(10);
    private TextField m_disappear = new TextField(10);
    private TextField m_updateDelay = new TextField(10);
    private Checkbox m_gridNumbers = new Checkbox();
    private Checkbox m_2dClipping = new Checkbox();

    /**
     * Options constructor.
     */
    public Options(Scene scene)
    {
        super("Options");

        m_scene = scene;
        setBackground(SystemColor.control);
        setFont(Appearance.getFont());

        setLayout(new GridLayout(9,2));

        add(new Label("VRML preview application"));
        m_previewProg.setText("" + Mode.getPreviewProg());
        add(m_previewProg);
        
        add(new Label("Preview file path"));
        m_previewPath.setText("" + Mode.getPreviewPath());
        add(m_previewPath);
        
        add(new Label("Undo stack size"));
        m_stack.setText("" + Mode.STACK_SIZE);
        add(m_stack);

        add(new Label("Spline subdivide level"));
        m_subdivide.setText("" + Mode.NURB_SUBDIVIDE);
        add(m_subdivide);

        add(new Label("Display Update Delay"));
        m_updateDelay.setText("" + ViewContainer.UPDATE_DELAY);
        add(m_updateDelay);        
        
        add(new Label("Fast Draw Threshhold"));
        m_disappear.setText("" + (int)Mode.DISAPPEAR_THRESHHOLD);
        add(m_disappear);

        add(new Label("Grid Numbers"));
        m_gridNumbers.setState(Grid.GRID_NUMBERS);
        add(m_gridNumbers);

        add(new Label("2D Clipping"));
        m_2dClipping.setState(SutherlandHodgman.CLIP);
        m_2dClipping.addItemListener(this);
        add(m_2dClipping);

        Button ok = new Button("Ok");
        ok.addActionListener(this);
        add(ok);

        Button cancel = new Button("Cancel");
        cancel.addActionListener(this);
        add(cancel);

        validate();
        pack();
    }

    /**
     * Handle clicking on "Ok" and "Cancel" buttons.  If user clicks
     * "Ok", parse all of the text fields and set Stomp's preferences
     * for those items.
     */
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        
        if(cmd.equals("Ok"))
        {
            NumberFormat numFormat = new DecimalFormat();

            try
                {
                int stack = numFormat.parse(m_stack.getText()).intValue();
                int subdivide = numFormat.parse(m_subdivide.getText()).intValue();
                int disappear = numFormat.parse(m_disappear.getText()).intValue();
                int updateDelay = numFormat.parse(m_updateDelay.getText()).intValue();
                if(disappear < 100)
                {
                    disappear = 100;
                }

                Mode.setPreviewPath(m_previewPath.getText());
                Mode.setPreviewProg(m_previewProg.getText());
                Mode.STACK_SIZE = stack;
                Mode.NURB_SUBDIVIDE = subdivide;
                Mode.DISAPPEAR_THRESHHOLD = disappear;
                Grid.GRID_NUMBERS = m_gridNumbers.getState();
                ViewContainer.UPDATE_DELAY = updateDelay;
                SutherlandHodgman.CLIP = m_2dClipping.getState();
            }
            catch(NumberFormatException ex)
            {
            }
            catch(ParseException ex)
            {
            }

            m_scene.validateScene();
            dispose();
        }
        else if(cmd.equals("Cancel"))
        {
            m_scene.validateScene();
            dispose();
        }
    }

    public void itemStateChanged(ItemEvent e)
    {
        if(m_2dClipping.getState() == false)
        {
            MessageDialog dialog =
                new MessageDialog("Warning! Due to bugs on some Java VM's, turning off 2d clipping can cause\nserious errors including system instabilities.\n\nProblems occur when zooming way in or out on an object.\n");
            dialog.setVisible(true);
        }
    }
}
