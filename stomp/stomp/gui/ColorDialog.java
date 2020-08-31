package stomp.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

import stomp.gui.Appearance;

/**
 * Color Dialog Class
 *
 * A stand-alone color picker.  Has sliders for red, green, and blue.
 * Text fields next to the sliders let the user type in specific values
 * as well.
 */
public class ColorDialog extends Frame implements ActionListener,
    AdjustmentListener
{
    //Result can be OK or CANCEL.  Indicates which button the user
    //clicked.
    public static int OK = 1;
    public static int CANCEL = 0;
    private int m_result = 5;

    //Internal representation of the color
    private int m_red;
    private int m_green;
    private int m_blue;

    //Scrollbars for choosing the color
    Scrollbar m_redScroll;
    Scrollbar m_greenScroll;
    Scrollbar m_blueScroll;
    
    //Text fields for choosing the color
    TextField m_redText = new TextField(4);
    TextField m_greenText = new TextField(4);
    TextField m_blueText = new TextField(4);

    //ColorBox gives a preview of the color.
    ColorBox m_colorBox = new ColorBox();

    ActionListener m_actionListener = null;

    /**
     * ColorDialog default constructor.
     *
     * Defaults to 0, 0, 0
     */
    public ColorDialog()
    {
        super("Color Picker");
        m_red = 0;
        m_green = 0;
        m_blue = 0;

        layoutComponents();
    }

    public void addActionListener(ActionListener listener)
    {
        m_actionListener = listener;
    }
    
    /**
     * ColorDialog constructor
     *
     * Parameters indicate which color the dialog will be
     * set to by default.
     * @param red Integer value for red
     * @param green Integer value for green
     * @param blue Integer value for blue
     */
    public ColorDialog(int red, int green, int blue)
    {
        super("Color Picker");
        m_red = red;
        m_green = green;
        m_blue = blue;

        layoutComponents();
    }

    /**
     * ColorDialog constructor
     *
     * Parameters indicate which color the dialog will be
     * set to by default.
     * @param color Color object for default color
     */
    public ColorDialog(Color color)
    {
        super("Color Picker");
        m_red = color.getRed();
        m_green = color.getGreen();
        m_blue = color.getBlue();

        layoutComponents();
    }    

    /**
     * Instantiates and places all of the controls ("Widgets") into
     * the dialog.
     */
    private void layoutComponents()
    {
        //Setup look and values
        setBackground(SystemColor.control);
        setFont(Appearance.getFont());

        m_colorBox.setColor(m_red, m_green, m_blue);
        m_redText.setText("" + m_red);
        m_greenText.setText("" + m_green);
        m_blueText.setText("" + m_blue);

        //Set layout manager to GridBagLayout and create an
        //associated GridBagConstraints for placing controls.
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);    

        //Contain the sliders and number text fields in a single panel
        Panel colorSliders = new Panel();
        
        { // Add components to the panel
            colorSliders.setLayout(new GridBagLayout());

            gbc.gridx = 0;
            gbc.gridy = 0;
            
            m_redScroll = new Scrollbar(Scrollbar.HORIZONTAL,
                                        m_red, 1, 0, 256);
            m_greenScroll = new Scrollbar(Scrollbar.HORIZONTAL,
                                          m_green, 1, 0, 256);
            m_blueScroll = new Scrollbar(Scrollbar.HORIZONTAL,
                                         m_blue, 1, 0, 256);

            //Add this class as a listener for changes in the
            //scrollbar.
            m_redScroll.addAdjustmentListener(this);
            m_greenScroll.addAdjustmentListener(this);
            m_blueScroll.addAdjustmentListener(this);

            //Add this class as a listener to the text fields, too.
            //Note: event is only registered when user hits "return"
            //in the text field.
            m_redText.addActionListener(this);
            m_greenText.addActionListener(this);
            m_blueText.addActionListener(this);

            //Add red slider and text field
            gbc.weightx = 1;
            colorSliders.add(new Label("Red"), gbc);
            gbc.gridx++;
            gbc.weightx = 10;
            colorSliders.add(m_redScroll, gbc);
            gbc.gridx+=2;
            gbc.weightx = 1;
            colorSliders.add(m_redText, gbc);

            //Add green slider and text field
            gbc.gridx = 0;
            gbc.gridy++;
            colorSliders.add(new Label("Green"), gbc);
            gbc.gridx++;
            gbc.weightx = 10;
            colorSliders.add(m_greenScroll, gbc);
            gbc.gridx+=2; 
            gbc.weightx = 1;
            colorSliders.add(m_greenText, gbc);

            //Add blue slider and text field
            gbc.gridx = 0;
            gbc.gridy++;
            colorSliders.add(new Label("Blue"), gbc);
            gbc.gridx++;
            gbc.weightx = 10;
            colorSliders.add(m_blueScroll, gbc);
            gbc.gridx+=2;
            gbc.weightx = 1;
            colorSliders.add(m_blueText, gbc);
        }
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        //Add slider panel to main frame.
        add(colorSliders, gbc);

        //Add color indicator box to main frame.
        gbc.gridy++;
        add(m_colorBox, gbc);

        //Add buttonss to main frame.
        gbc.gridy++;
        gbc.gridwidth = 1;
        
        Button okButton = new Button("Ok");
        add(okButton, gbc);
        okButton.addActionListener(this);

        gbc.gridx++;
        Button cancelButton = new Button("Cancel");
        add(cancelButton, gbc);
        cancelButton.addActionListener(this);

        //Validate the controls and pack to the minimum size.
        validate();
        pack();

        //Pack creates a window with sliders that are too small,
        //make the window a little wider, but not higher.  (The height
        //may change depending on the platform it is run on.)
        setSize(350, getSize().height);
    }

    /**
     * This member is invoked when a button is pressed or data is
     * entered into the text fields.
     *
     * @param e ActionEvent supplied automatically when an event causes
     * this method to be called.
     */
    public void actionPerformed(ActionEvent e)
    {
        //Get the name of the item that caused the action.
        String arg = e.getActionCommand();

        try
        {
            //Grab the red, green, and blue values out of the
            //corresponding text fields.
            m_red = Integer.parseInt(m_redText.getText());
            m_green = Integer.parseInt(m_greenText.getText());
            m_blue = Integer.parseInt(m_blueText.getText());

            //Check to make sure they are within the 0-255 range,
            //if not reset them.
            if(m_red > 255 || m_red < 0)
            {
                m_red = 0;
                m_redText.setText("" + m_red);
            }
            if(m_green > 255 || m_green < 0)
            {
                m_green = 0;
                m_greenText.setText("" + m_green);
            }
            if(m_blue > 255 || m_blue < 0)
            {
                m_blue = 0;
                m_blueText.setText("" + m_blue);
            }

            //Set the sliders to reflect the numbers.
            m_redScroll.setValue(m_red);
            m_greenScroll.setValue(m_green);
            m_blueScroll.setValue(m_blue);
        }
        catch(NumberFormatException exception)
        {
        }

        //Update the color box to show the most recent color.
        m_colorBox.setColor(m_red, m_green, m_blue);
        m_colorBox.repaint();

        //Check to see if user hit "Ok" or "Cancel" and do the
        //corresponding action.
        if("Ok".equals(arg))
        {
            m_result = ColorDialog.OK;
            if(m_actionListener != null)
            {
                m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                 "DialogCloseOk"));
            }
            
            dispose();
        }
        else if("Cancel".equals(arg))
        {
            m_result = ColorDialog.CANCEL;
            dispose();
        }
    }

    /**
     * Gets the color picked.
     *
     * @return Color object representing the color picked.
     */
    public Color getColor()
    {
        return new Color(m_red, m_green, m_blue);
    }

    /**
     * Gets whether the user hit Ok or Cancel.
     *
     * @return ColorDialog.OK if the user hit Ok,
     *         ColorDialog.CANCEL is user hit Cancel.
     */
    public int getResult()
    {
        return m_result;
    }

    /**
     * This member is invoked when any of the scrollbars are modified.
     *
     * @param e AdjustmentEvent object is created when this method is
     * automatically called.
     */
    public void adjustmentValueChanged(AdjustmentEvent e)
    {
        //Check to see which slider was adjusted and update the
        //corresponding text box to the new values.
        if(e.getAdjustable() == m_redScroll)
        {
            m_red = m_redScroll.getValue();
            m_redText.setText("" + m_red);
        }
        else if(e.getAdjustable() == m_greenScroll)
        {
            m_green = m_greenScroll.getValue();
            m_greenText.setText("" + m_green);
        }
        else if(e.getAdjustable() == m_blueScroll)
        {
            m_blue = m_blueScroll.getValue();
            m_blueText.setText("" + m_blue);
        }

        //Update the color box to show the most recent color.
        m_colorBox.setColor(m_red, m_green, m_blue);
        m_colorBox.repaint();
    }

    /**
     * Simple unit-test for checking to see how the dialog works.
     * Also gives a simple usage example.
     */
    public static void main(String args[])
    {
        //Create the dialog.
        ColorDialog dg = new ColorDialog(100, 150, 200);

        //Show it.
        dg.setVisible(true);

        //Keep polling the dialog to see if "Ok" or "Cancel" has
        //been selected yet.
        //
        // IMPORTANT: Normally, modal dialogs will "block" and this
        // isn't necessary.  Since modal dialogs have some problems,
        // this is the easiest solution.  The sleep ensures that
        // it will not be a CPU-hogging busy loop.
        while(dg.getResult() != ColorDialog.OK &&
              dg.getResult() != ColorDialog.CANCEL)
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

        //Show selected color and exit.
        if(dg.getResult() == ColorDialog.OK)
        {
            System.out.println("User picked: " + dg.getColor());
        }
        System.exit(0);
    }
}

/***********************************************************
 * $Log: ColorDialog.java,v $
 * Revision 1.4  1998/04/23 17:33:44  schreine
 * Numeric options dialog, made background of all dialogs correct
 *
 * Revision 1.3  1998/03/03 20:51:23  schreine
 * Added log string
 *
 **********************************************************/
