package webwars.movingobject;

import java.awt.*;

import webwars.*;

public class Spinner extends MovingObject
{
    private float m_mass;
    private Image m_planetPicture;
    private int m_angle;
    private Image m_spinnerImages[];
    private long m_rotateTime;

    public Spinner(Vec position,float mass, int radius, Image spinnerImages[],
                   MOList moList)
    {
        super(position, new Vec(0,0), radius, true, true, 0, moList);

        m_mass=mass;
        m_spinnerImages = spinnerImages;
    }

    public float mass()
    {
        return m_mass;
    }

    public void paint(Graphics g)
    {
        g.drawImage(m_spinnerImages[m_angle], (int)position().x()-size(),
                    (int)position().y()-size(),null,null);
    }

    public boolean move()
    {
        if(m_rotateTime < System.currentTimeMillis())
        {
            if(++m_angle > 11)
                m_angle = 0;
            m_rotateTime = System.currentTimeMillis() + 10;
        }
        if (stationary()==false)
        {
            position().add(velocity());
        }
        return true;

    }
 
    public synchronized boolean affect(MovingObject who)
    {
        Vec deltaPos = new Vec(this.position());
        deltaPos.subtract(who.position());
        float deltaPosMag = deltaPos.magnitude(); //do this just once

        if (deltaPosMag > this.size() + who.size())
        {
            float accel=m_mass/(deltaPos.magSq());
            who.addVelocity(new Vec(accel*deltaPos.y()/deltaPosMag,
                                    (-accel)*deltaPos.x()/deltaPosMag));

            // Note that this is just the Planet's gravity function with x & y
            // reversed and x negated.
            return(true);
        }
        else
        {
            // Collision code here
            who.die();
            return(false);
        }

    }

    public void die()
    {
        // Spinners don't die.  What are you, crazy?
    }

    public String type()
    {
        return new String("Planet");
    }
}

