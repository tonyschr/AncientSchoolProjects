package webwars;

import java.awt.*;
import java.awt.event.*;

import webwars.movingobject.Vehicle;

/**
 * Human control over a vehicle.  Reads keypresses and contains other
 * Player related information
 */
public class Human extends Player implements KeyListener
{
    //Keys for directing control
    private char m_leftKey;
    private char m_rightKey;
    private char m_thrustKey;
    private char m_retroThrustKey;
    private char m_fireKey;
    private char m_changeWeaponKey;

    //Values for whether or not the key is currently being pressed
    private boolean m_leftDown;
    private boolean m_rightDown;
    private boolean m_thrustDown;
    private boolean m_retroThrustDown;
    private boolean m_fireDown;
    private boolean m_changeWeaponDown;

    /**
     * Constructor.
     *
     * @param name Name of human player
     * @param teamNumber Team number (not implemented)
     * @param keys String of keys to use for control.
     * @param vehicle Vehicle for player to drive
     * @param moList Reference to main moving object list.
     */
    public Human(String name, int teamNumber, String keys,
                 Vehicle vehicle, MOList moList)
    {
        super(name,teamNumber,vehicle,moList);
        m_leftKey = keys.charAt(0);
        m_rightKey = keys.charAt(1);
        m_thrustKey = keys.charAt(2);
        m_retroThrustKey = keys.charAt(3);
        m_fireKey = keys.charAt(4);
        m_changeWeaponKey = keys.charAt(5);
    }

    public void keyTyped(KeyEvent e)
    {
    }

    /**
     * If key is pressed, keep track of it by getting that key and setting
     * the value of some keyDown to true.
     *
     * This allows multiple keys to be pressed simultaneously and prevents
     * errors from missed key events.
     *
     * Early Java versions had a bug where this didn't work very well, but
     * it works correctly on all reasonably recent releases.
     */
    public void keyPressed(KeyEvent e)
    {
        char key = e.getKeyChar();

        if(key == m_leftKey)
            m_leftDown = true;
        else if(key == m_rightKey)
            m_rightDown = true;
        else if(key == m_thrustKey)
            m_thrustDown = true;
        else if(key == m_retroThrustKey)
            m_retroThrustDown = true;
        else if(key == m_fireKey)
            m_fireDown = true;
        else if (key == m_changeWeaponKey)
            m_vehicle.changeWeaponType();

    }

    /**
     * If key is released, keep track of it and set some keyDown to false to
     * signal the key has been released
     */
    public void keyReleased(KeyEvent e)
    {
        char key = e.getKeyChar();

        if(key == m_leftKey)
            m_leftDown = false;
        else if(key == m_rightKey)
            m_rightDown = false;
        else if(key == m_thrustKey)
            m_thrustDown = false;
        else if(key == m_retroThrustKey)
            m_retroThrustDown = false;
        else if(key == m_fireKey)
            m_fireDown = false;
    }

    /**
     * Main loop for the player.  This is where the ship responds to
     * the keypresses.  There is a sleep value here which can be
     * changed to alter responsiveness.
     */
    public void run()
    {        
        while(true)
        {
            try
            {
                //Calling the methods directly to avoid the additional
                //level of indirection in the Player methods (a speed issue)
                if(m_leftDown==true)
                    m_vehicle.turnLeft();
                else if(m_rightDown == true)
                    m_vehicle.turnRight();
                if(m_thrustDown == true)
                    m_vehicle.thrust();
                else if(m_retroThrustDown)
                    m_vehicle.retrothrust();
                if(m_fireDown == true)
                    addMO(m_vehicle.fire());

                Thread.sleep(50);
                Thread.yield();
            }
            catch(InterruptedException e)
            {
                stop();
            }
        }
    }
}
