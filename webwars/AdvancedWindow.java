package webwars;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Dialog for advanced user options.  Right now, they don't make much sense
 * since they are directly modifying internal timing values in the program.
 */
public class AdvancedWindow extends Frame implements WindowListener,
  ActionListener 
{
    private WebWars m_mainApplet;  //Ref. to applet to send messages to it

    //Sleep times
    private TextField m_sleepTime = new TextField();
    private TextField m_redrawTime = new TextField();

    //Vehicle stuff
    private TextField m_acceleration = new TextField();
    private TextField m_maxSpeed = new TextField();
    private TextField m_rechargeRate = new TextField();
    private TextField m_rechargeAmount = new TextField();
    private TextField m_shipEnergy = new TextField();
  
    //Weapon stuff
    private TextField m_bulletDamage = new TextField();
    private TextField m_missileDamage = new TextField();
    private TextField m_bombDamage = new TextField();
    private TextField m_bombDelay = new TextField();
    private TextField m_bulletDelay = new TextField();
    private TextField m_missileDelay = new TextField();

    /**
     * Constructor.  Create the window.
     */
    public AdvancedWindow() 
    {
        super("Advanced Options");

        //Get reference to applet, we want to send messages
        setBackground(Color.lightGray);

        setLayout(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();

        //Set up what are pretty much global GridBagConstraints.
        gbConstraints.gridx = 0;
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.ipadx = 5;
        gbConstraints.ipady = 5;
        gbConstraints.insets = new Insets(5,5,5,5);
        gbConstraints.gridwidth = 2;

        
        Panel gameSpeedPanel = new Panel();
        {
            gameSpeedPanel.setLayout(new GridLayout(2,2));
	
            gameSpeedPanel.add(new Label("Sleep time"));
            m_sleepTime.setText("" + WebWars.SPEEDFACTOR);
            gameSpeedPanel.add(m_sleepTime);

            gameSpeedPanel.add(new Label("Redraw time"));
            m_redrawTime.setText("" + WebWars.REDRAWFACTOR);
            gameSpeedPanel.add(m_redrawTime);
        }

        Panel shipPanel = new Panel();
        {
            shipPanel.setLayout(new GridLayout(5,2));
	
            shipPanel.add(new Label("Initial Energy"));
            m_shipEnergy.setText("" + WebWars.STARTINGENERGY);
            shipPanel.add(m_shipEnergy);

            shipPanel.add(new Label("Recharge Delay (ms)"));
            m_rechargeRate.setText("" + WebWars.RECHARGERATE);
            shipPanel.add(m_rechargeRate);	

            shipPanel.add(new Label("Recharge Amount"));
            m_rechargeAmount.setText("" + WebWars.RECHARGEAMOUNT);
            shipPanel.add(m_rechargeAmount);	

            shipPanel.add(new Label("Ship Acceleration"));
            m_acceleration.setText("" + WebWars.ACCELERATION);
            shipPanel.add(m_acceleration);

            shipPanel.add(new Label("Max Speed"));
            m_maxSpeed.setText("" + WebWars.SPEEDOFLIGHT);
            shipPanel.add(m_maxSpeed);		
        }

        Panel weaponPanel = new Panel();
        {
            weaponPanel.setLayout(new GridLayout(6,2));
	
            weaponPanel.add(new Label("Bullet Damage"));
            m_bulletDamage.setText("" + WebWars.BULLETPOWER);
            weaponPanel.add(m_bulletDamage);

            weaponPanel.add(new Label("Bullet Delay"));
            m_bulletDelay.setText("" + WebWars.BULLETDELAY);
            weaponPanel.add(m_bulletDelay);

            weaponPanel.add(new Label("Missile Damage"));
            m_missileDamage.setText("" + WebWars.MISSILEPOWER);
            weaponPanel.add(m_missileDamage);

            weaponPanel.add(new Label("Missile Delay"));
            m_missileDelay.setText("" + WebWars.MISSILEDELAY);
            weaponPanel.add(m_missileDelay);	

            weaponPanel.add(new Label("Bomb Damage"));
            m_bombDamage.setText("" + WebWars.BOMBPOWER);
            weaponPanel.add(m_bombDamage);

            weaponPanel.add(new Label("Bomb Delay"));
            m_bombDelay.setText("" + WebWars.BOMBDELAY);
            weaponPanel.add(m_bombDelay);	
        }

        gbConstraints.gridy = 0;
        add(new Label("Caution: Some values are non-intuitive"), gbConstraints);
        
        gbConstraints.gridy++;
        add(new Label("Speed Constants"), gbConstraints);
      
        gbConstraints.gridy++;
        add(gameSpeedPanel, gbConstraints);

        gbConstraints.gridy++;
        add(new Label("Ship Constants"), gbConstraints);
      
        gbConstraints.gridy++;
        add(shipPanel, gbConstraints);

        gbConstraints.gridy++;
        add(new Label("Weapon Constants"), gbConstraints);
      
        gbConstraints.gridy++;
        add(weaponPanel, gbConstraints);

        gbConstraints.gridy++;
        Button okButton = new Button("Ok");
        add(okButton, gbConstraints);
        okButton.addActionListener(this);
        validate();
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

    public void windowClosing(WindowEvent event) 
    {
        dispose();
    }

    /**
     * User updated a text field or clicked "ok"
     */
    public void actionPerformed(ActionEvent e) 
    {
        String arg = e.getActionCommand();
        
        if ("Ok".equals(arg))
        {
            System.out.println("User clicked ok");
            //Set sleep times
            WebWars.SPEEDFACTOR = Integer.parseInt(m_sleepTime.getText());
            WebWars.REDRAWFACTOR = Integer.parseInt(m_redrawTime.getText());

            //Set vehicle stuff
            WebWars.ACCELERATION =
                Double.valueOf(m_acceleration.getText()).doubleValue();
            WebWars.SPEEDOFLIGHT = Integer.parseInt(m_maxSpeed.getText());
            WebWars.RECHARGERATE = Integer.parseInt(m_rechargeRate.getText());
            WebWars.RECHARGEAMOUNT = Integer.parseInt(m_rechargeAmount.getText());
            WebWars.STARTINGENERGY = Integer.parseInt(m_shipEnergy.getText());

            //Set weapon stuff
            WebWars.BULLETPOWER = Integer.parseInt(m_bulletDamage.getText());
            WebWars.MISSILEPOWER = Integer.parseInt(m_missileDamage.getText());
            WebWars.BOMBPOWER = Integer.parseInt(m_bombDamage.getText());
	  
            WebWars.BULLETDELAY = Integer.parseInt(m_bulletDelay.getText());
            WebWars.MISSILEDELAY = Integer.parseInt(m_missileDelay.getText());
            WebWars.BOMBDELAY = Integer.parseInt(m_bombDelay.getText());

            //Close the window
            dispose();
        }
    }
}
