package webwars;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Dialog for basic user options.
 * Pops up by default when program is first run.
 */
public class DialogWindow extends Frame implements WindowListener,
  ActionListener 
{
    private WebWars m_mainApplet;  //Ref. to applet to send messags to it

    //Choice components for choosing number of computer and human players
    private Choice m_numHumans = new Choice();
    private Choice m_edges = new Choice();
    private Choice m_planets = new Choice();
    private Choice m_asteroids = new Choice();
    private Choice m_rewards = new Choice();

    private Choice m_computer1 = new Choice();
    private Choice m_computer2 = new Choice();
    private Choice m_computer3 = new Choice();
    private Choice m_computer4 = new Choice();

    private Label m_keyL[] = new Label[6]; //Key selection labels

    //Key selection text fields
    private TextField m_p1Key[] = new TextField[6];
    private TextField m_p2Key[] = new TextField[6];
    private TextField m_p1name = new TextField("Player One",12);
    private TextField m_p2name = new TextField("Player Two",12);

    //Default values for keys
    private String m_player1Default = new String("jlik u");
    private String m_player2Default = new String("adwsqr");

    /**
     * Constructor.  Sends messages to WebWars class.
     *
     * @param mainApplet WebWars object controlling the game.
     */
    public DialogWindow(WebWars mainApplet) 
    {
        //Get reference to applet, we want to send messages
        m_mainApplet = mainApplet;

        setBackground(Color.lightGray);

        setLayout(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();

        //Set up what are pretty much global GridBagConstraints.
        gbConstraints.gridx = 0;
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.ipadx = 5;
        gbConstraints.ipady = 5;
        gbConstraints.insets = new Insets(5,5,5,5);
        gbConstraints.gridwidth = 2;
      
        Panel choicePanel = new Panel();
        {
            choicePanel.setLayout(new GridLayout(5,4));  //Grid style layout

            //There is probably a better way to do this, but clone() didn't
            //seem to work here.
            m_computer1.addItem("(None)");
            m_computer1.addItem("Generic");
            m_computer1.addItem("JavaRobot 1");
            m_computer1.addItem("JavaRobot 2");
            m_computer1.addItem("JavaRobot 3");
            m_computer1.addItem("JavaRobot 4");

            m_computer2.addItem("(None)");
            m_computer2.addItem("Generic");
            m_computer2.addItem("JavaRobot 1");
            m_computer2.addItem("JavaRobot 2");
            m_computer2.addItem("JavaRobot 3");
            m_computer2.addItem("JavaRobot 4");

            m_computer3.addItem("(None)");
            m_computer3.addItem("Generic");
            m_computer3.addItem("JavaRobot 1");
            m_computer3.addItem("JavaRobot 2");
            m_computer3.addItem("JavaRobot 3");
            m_computer3.addItem("JavaRobot 4");

            m_computer4.addItem("(None)");
            m_computer4.addItem("Generic");
            m_computer4.addItem("JavaRobot 1");
            m_computer4.addItem("JavaRobot 2");
            m_computer4.addItem("JavaRobot 3");
            m_computer4.addItem("JavaRobot 4");

            //Add 0-2 human players options
            choicePanel.add(new Label("Human Players"));
            for(int i = 0; i < 3; i++)
                m_numHumans.addItem(""+i);
            m_numHumans.select(1);
            choicePanel.add(m_numHumans);

            choicePanel.add(new Label("Computer 1"));
            choicePanel.add(m_computer1);

            choicePanel.add(new Label("Planets"));
            for(int i = 0; i < 4; i++)
                m_planets.addItem(""+i);
            choicePanel.add(m_planets);

            choicePanel.add(new Label("Computer 2"));
            choicePanel.add(m_computer2);

            choicePanel.add(new Label("Asteroids"));
            for(int i = 0; i < 4; i++)
                m_asteroids.addItem(""+i);
            m_asteroids.select(1);
            choicePanel.add(m_asteroids);

            choicePanel.add(new Label("Computer 3"));
            choicePanel.add(m_computer3);

            choicePanel.add(new Label("Rewards"));
            for(int i = 0; i < 11; i++)
                m_rewards.addItem(""+i);
            m_rewards.select(2);
            choicePanel.add(m_rewards);
            
            choicePanel.add(new Label("Computer 4"));
            choicePanel.add(m_computer4);
            
            choicePanel.add(new Label("Edges"));
            m_edges.addItem("Wrap");
            m_edges.addItem("Bounce");
            choicePanel.add(m_edges);

            choicePanel.validate();
        }

        //Add this panel at the top
        gbConstraints.gridy = 0;
        add(choicePanel, gbConstraints);

        //Create a new panel for the keyboard input options
        Panel keyPanel = new Panel();
        {

            keyPanel.setLayout(new GridLayout(8,3));  //Grid style layout
            keyPanel.add(new Label("Function",Label.LEFT));
            keyPanel.add(new Label("Player 1",Label.LEFT));
            keyPanel.add(new Label("Player 2",Label.LEFT));

            keyPanel.add(new Label("Name",Label.LEFT));
            keyPanel.add(m_p1name);
            keyPanel.add(m_p2name);

            m_keyL[0] = new Label("Left:",Label.LEFT);
            m_keyL[1] = new Label("Right:",Label.LEFT);
            m_keyL[2] = new Label("Thrust:",Label.LEFT);
            m_keyL[3] = new Label("RetroThrust:",Label.LEFT);
            m_keyL[4] = new Label("Fire:",Label.LEFT);
            m_keyL[5] = new Label("ChangeWeapon:",Label.LEFT);

            //Add all the controls in a loop
            for(int i = 0; i<6; i++)
            {
                m_p1Key[i] = new TextField(new String("" +
                                           m_player1Default.charAt(i)),3);
                keyPanel.add(m_keyL[i]);
                keyPanel.add(m_p1Key[i]);

                m_p2Key[i] = new TextField(new String("" +
                                           m_player2Default.charAt(i)),3);
                keyPanel.add(m_p2Key[i]);
            }

            keyPanel.validate();
        }

        gbConstraints.gridy = 1;
        add(keyPanel, gbConstraints);

        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.gridwidth = 1;
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 2;
        gbConstraints.weightx = 0;
        Button advancedButton = new Button("Advanced");
        advancedButton.addActionListener(this);
        add(advancedButton, gbConstraints);

        //Ok button for accepting selections.
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 2;
        Button okButton = new Button("Ok");
        okButton.addActionListener(this);
        add(okButton, gbConstraints);
      
        addWindowListener(this);
        doLayout();
    }

    public void windowClosed(WindowEvent event) 
    {
    }

    public void windowDeiconified(WindowEvent event) 
    {
    }

    public void windowIconified(WindowEvent event) 
    {
    }

    public void windowActivated(WindowEvent event) 
    {
    }

    public void windowDeactivated(WindowEvent event) 
    {
    }

    public void windowOpened(WindowEvent event) 
    {
    }

    /**
     * User clicked on the close gadget.  Revert to default setting and
     * start the game.
     */
    public void windowClosing(WindowEvent event) 
    {
        m_mainApplet.addPlayer(0,m_p1name.getText());
        m_mainApplet.setEnvironment(m_edges.getSelectedIndex(),
                                    Integer.parseInt(m_planets.getSelectedItem()),
                                    Integer.parseInt(m_asteroids.getSelectedItem()),
                                    Integer.parseInt(m_rewards.getSelectedItem()));
        dispose();
    }

    /**
     * Check to see if user clicked 'ok' and retrieve all values from fields.
     */
    public void actionPerformed(ActionEvent e) 
    {
        String arg = e.getActionCommand();

        if("Advanced".equals(arg))
        {
            AdvancedWindow advWindow = new AdvancedWindow();

            advWindow.pack();
            advWindow.setVisible(true);
        }
        else if("Ok".equals(arg))
        {
            int numHumans = Integer.parseInt(m_numHumans.getSelectedItem());

            String player1 = new String("");
            String player2 = new String("");
            //Get the new key choices
            for(int i = 0; i<6; i++)
            {
                player1 = player1 + m_p1Key[i].getText().charAt(0);
                player2 = player2 + m_p2Key[i].getText().charAt(0);
            }
	
            //Send key choices back to the applet
            m_mainApplet.setPlayerKeys(player1,player2);
            m_mainApplet.setEnvironment(m_edges.getSelectedIndex(),
                                        Integer.parseInt(m_planets.getSelectedItem()),
                                        Integer.parseInt(m_asteroids.getSelectedItem()),
                                        Integer.parseInt(m_rewards.getSelectedItem()));

            //If user clicked okay, add players and close window.
            if(numHumans > 0)
                m_mainApplet.addPlayer(0,m_p1name.getText());
            if(numHumans > 1)
                m_mainApplet.addPlayer(1,m_p2name.getText());

            m_mainApplet.addComputer(m_computer1.getSelectedIndex()-1);
            m_mainApplet.addComputer(m_computer2.getSelectedIndex()-1);
            m_mainApplet.addComputer(m_computer3.getSelectedIndex()-1);
            m_mainApplet.addComputer(m_computer4.getSelectedIndex()-1);

            dispose();
        }
    }
}

