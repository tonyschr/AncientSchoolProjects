package webwars.movingobject;

import java.awt.*;

import webwars.*;

public abstract class MovingObject
{
    private Vec m_position;
    private Vec m_velocity;
    private int m_size;    // radius

    private boolean m_stationary; 
    private boolean m_constant_velocity;

    private boolean m_alive;
    private static int m_idCounter;
    private int m_id;

    protected MOList m_moList;

    public MovingObject(Vec position, Vec velocity, int size, int id,
                        MOList moList)
    {
        if(id == 0)
        {
            m_id = ++m_idCounter;
        }
        else
        {
            m_id = id;
        }
        m_position=new Vec(position);
        m_velocity=new Vec(velocity);
        m_size=size;
        m_stationary=false;
        m_constant_velocity=false;

        m_moList=moList;

        m_alive=true;
    }

    public MovingObject(Vec position, Vec velocity, int size,
                        boolean stationary, boolean constant_velocity, 
                        int id, MOList moList)
    {
        this(position, velocity, size, id, moList);
      
        m_stationary=stationary;
        m_constant_velocity=constant_velocity;
    }

    public boolean move()
    {
        if (m_stationary==false)
        {
            m_position.add(m_velocity);
        }

        return true;
    }

    public void setPosition(Vec position)
    {
        m_position = position;
    }

    public void setVelocity(Vec velocity)
    {
        m_velocity = velocity;
    }

    synchronized public boolean affect(MovingObject who)
    {
        //        if (who.stationary()==false)
        //        {
        //            if (this.position().minus(who.position()).magnitude() 
        //                < who.size() + this.size())
        //            {
        //                // Collision code here
        //            }
        //        }

        return true;
    }

    public void paint(Graphics g)
    {
        // notice that this doesn't do anything.  
        // this should be implemented in subclasses if you want to see them.
    }

    public Vec position()        // where it is
    {
        return m_position;
    }

    public Vec velocity()        // which way it's going
    {
        return m_velocity;
    }

    public int size()
    {
        return m_size;
    }

    public boolean stationary()       // is it a stationary object?
    {
        return m_stationary;
    }

    public int damage()     // will this object damage me if I hit it?
        // negative is bad, positive is good
    {
        return 0;       // needs to be overridden by subclasses
        // unless neutral
    }

    public void addVelocity(Vec acceleration)
    {
        m_velocity.add(acceleration);

        // If you want a cap on the velocity, put it here
        // unless you want it just when you thrust (i.e. slingshots around the 
        // planet enable higher speeds...that would be better in my opinion)
        // in which case put it in Vehicle.thrust.
    }

    public boolean addEnergy(int power)
    {
        // By default, this method does nothing, because MO's don't have energy by
        // default.  For Vehicles, it actually adds energy.  For Weapons,
        // it kills them.

        return true;
    }

    public boolean alive()
    {
        return m_alive;
    }

    public void die()
    {
        m_alive=false;
    }

    public String type()
    {
        return new String("Moving Object");
    }
    
    public int id()
    {
        return m_id;
    }
}

