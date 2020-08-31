package webwars.movingobject;

import java.awt.*;

import webwars.*;

public class Planet extends MovingObject
{
    private float m_mass;
    private Image m_planetPicture;

    public Planet(Vec position,float mass, int radius, Image planetPicture,
                  MOList moList)
    {
        super(position, new Vec(0,0), radius, true, true, 0, moList);

        m_mass=mass;
        m_planetPicture = planetPicture;
    }

    public float mass()
    {
        return m_mass;
    }

    public void paint(Graphics g)
    {
        g.drawImage(m_planetPicture, (int)position().x()-size(),
                    (int)position().y()-size(), null, null);
    }

    public Vec gravity(Vec moPosition)  // returns gravitational acceleration  
        // vector due to this planet object
    {
        Vec deltaPos=new Vec(this.position());
        deltaPos.subtract(moPosition);

        float accel=m_mass/(deltaPos.magSq());       // magnitude of acceleration
        float l_distance = deltaPos.magnitude();       // this part uses similar
        return new Vec(accel*deltaPos.x()/l_distance,  // triangles to split 
                       accel*deltaPos.y()/l_distance); // into components
    }

    public synchronized boolean affect(MovingObject who)
    {
        Vec deltaPos = new Vec(this.position());
        deltaPos.subtract(who.position());
        float deltaPosMag = deltaPos.magnitude(); //do this only once

        if (deltaPosMag > this.size()+who.size())
        {
            float accel=m_mass/(deltaPos.magSq());
            who.addVelocity(new Vec(accel*deltaPos.x()/deltaPosMag,
                                    accel*deltaPos.y()/deltaPosMag));
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
        // Planets don't die.  What are you, crazy?
    }

    public String type()
    {
        return new String("Planet");
    }
}

