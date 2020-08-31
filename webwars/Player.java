package webwars;

import webwars.movingobject.*;

/**
 * Class representing any player.  This serves as the superclass for all
 * human and computer players.
 */
public class Player implements Runnable
{
    protected Vehicle m_vehicle;
    protected String m_name;

    private Thread m_Player = null;
    private int m_score;
    private int m_team;
    private MOList m_moList;

    /**
     * Contstructor, create a player.
     *
     * @param name Name of the player
     * @param teamNumber Team number of the player (Not Implemented)
     * @param vehicle Vehicle that the player drives
     * @param moList Main MovingObject List
     */
    public Player(String name, int teamNumber, Vehicle vehicle, MOList moList)
    {
        m_name = name;
        m_team = teamNumber;
        m_vehicle = vehicle;
        m_moList = moList;
        m_vehicle.setName(name);
        resetScore();
    }

    /**
     * Change the team number
     *
     * @param teamNumber team number to change to
     */
    public boolean changeTeam(int teamNumber)
    {
        //May check to see if team is a good number, for now just returns true
        m_team = teamNumber;
        return true;
    }

    /**
     * Increase this player's score by the given amount.  May be
     * negative.
     *
     * @param amount amount to add onto score.
     */
    public void increaseScore(int amount)
    {
        m_score += amount;
    }

    /**
     * Reset the player's score to 0.
     */
    public void resetScore()
    {
        m_score = 0;
    }

    /**
     * Start the thread for the player going
     */
    public void start()
    {
        if (m_Player == null)
        {
            m_Player = new Thread(this);
            m_Player.start();
        }
    }

    public void stop()
    {
        if (m_Player != null)
        {
            m_Player.stop();
            m_Player = null;
        }
    }

    /**
     * Move the player.  Infinite loop with the thread sleeping in
     * between.
     */
    public void run()
    {
        while(true)
        {
            try
            {
                playermove();
                Thread.sleep(50);
                Thread.yield();
            }
            catch(InterruptedException e)
            {
                stop();
            }            
        }
    }

    /**
     * I don't know why this is here, except maybe for weapons.
     * TODO: Check this out.
     */
    public void addMO(MovingObject mo)
    {
        m_moList.add(mo);
    }

    /**
     * Make the vehicle turn left.
     */
    public void turnLeft()
    {
        try
        {
            Thread.sleep(10);
            Thread.yield();
        }
        catch(InterruptedException e)
        {
            stop();
        }            

        m_vehicle.turnLeft();
    }

    /**
     * Make the vehicle turn right.
     */
    public void turnRight()
    {
        try
        {
            Thread.sleep(10);
            Thread.yield();
        }
        catch(InterruptedException e)
        {
            stop();
        }            

        m_vehicle.turnRight();
    }

    /**
     * Make the vehicle thrust
     */
    public void thrust()
    {
        try
        {
            Thread.sleep(10);
            Thread.yield();
        }
        catch(InterruptedException e)
        {
            stop();
        }            
        m_vehicle.thrust();
    }

    /**
     * Make the vehicle reverse thrust.
     */
    public void retroThrust()
    {
        try
        {
            Thread.sleep(10);
            Thread.yield();
        }
        catch(InterruptedException e)
        {
            stop();
        }            
        m_vehicle.retrothrust();
    }

    /**
     * Make the vehicle fire.
     */
    public void fire()
    {
        try
        {
            Thread.sleep(10);
            Thread.yield();
        }
        catch(InterruptedException e)
        {
            stop();
        }            
        addMO(m_vehicle.fire());
    }

    /**
     * Make the vehicle turn towards a specific position
     *
     * @param position Position to turn toward.
     */
    public void turnToward(Vec position)
    {
        m_vehicle.turnToward(position);
    }

    /**
     * Make the vehicle turn away from a specific position
     *
     * @param position Position to turn away from
     */
    public void turnAway(Vec position)
    {
        m_vehicle.turnAway(position);
    }

    /**
     * Change the vehicle's weapon type (next available weapon??)
     *
     */
    public void changeWeaponType()
    {
        m_vehicle.changeWeaponType();
    }

    public MODescriptor moId(int idnum)
    {
        return m_moList.getMODescriptor(idnum);
    }

    public void playermove()
    {
    }

    public Vec position()
    {
        return m_vehicle.position();
    }

    public Vec velocity()
    {
        return m_vehicle.velocity();
    }

    public int energy()
    {
        return m_vehicle.energy();
    }
    
    public MODescriptor closestMO(Vec pos)
    {
        return m_moList.closest(pos);
    }

    public MODescriptor closestMO()
    {
        return m_moList.closest(m_vehicle);
    }

    public MODescriptor closestVehicle()
    {
        return m_moList.closestVehicle(m_vehicle);
    }

    public int weaponType()
    {
        return m_vehicle.weaponType();
    }

    public void slowdown()
    {
        try
        {
            Thread.sleep(20);
            Thread.yield();
        }
        catch(InterruptedException e)
        {
            stop();
        }            
        m_vehicle.turnToward(m_vehicle.position().plus(m_vehicle.velocity()));
        m_vehicle.retrothrust();
    }
}

