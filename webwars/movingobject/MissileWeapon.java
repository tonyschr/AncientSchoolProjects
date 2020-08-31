package webwars.movingobject;

import java.awt.*;

import webwars.*;

public class MissileWeapon extends MovingObject implements WeaponI
{
    private int m_damageCaused;
    private long m_lifetime;
    private int m_angle;

    MissileWeapon(Vec position, Vec initialVelocity, int id, MOList moList)
    {
        super(position, initialVelocity, 4, false, false, id, moList);  

        m_damageCaused=WebWars.MISSILEPOWER;
        m_lifetime=System.currentTimeMillis();
    }

    public boolean move()
    {
        MODescriptor target = m_moList.closestVehicle((MovingObject)this);

        if (target != null && target.id() != id())
        {
            Vec velRelative = target.velocity().minus(this.velocity());
            Vec distance = target.position().minus(this.position());

            float perpFrac = distance.cross(velRelative) /
                distance.magnitude()/velRelative.magnitude();

            Vec perpendicular = velRelative.times(perpFrac);

            if (perpendicular.magnitude() > (float)2)
                turnToward(this.position().plus(velRelative));
            else
                turnToward(target.position());
             
            float mag = (float)0.9;
				 
            Vec acceleration=
                new Vec(mag*(float)Math.sin(angleR()),
-mag*(float)Math.cos(angleR()));

            this.addVelocity(acceleration);
        }

        if (stationary()==false)
        {
            position().add(velocity());
        }

        if(System.currentTimeMillis()-m_lifetime > 4800)
            return false;
        else
        {
            if(this.alive())
                return true;
            else
                return false;
        }
    }

    private void turnToward (Vec target)
    {
        Vec toTarget=new Vec(target);
        Vec forward=new Vec(this.position().x()+(float)Math.sin(angleR()),
                            this.position().y()-(float)Math.cos(angleR()));

        toTarget.subtract(this.position());
        forward.subtract(this.position());

        if (toTarget.x()*forward.y() < forward.x()*toTarget.y()) // cross product
            turnRight();
        else
            turnLeft();
    }

    private void turnRight()
    {
        m_angle++;
        if(m_angle >= WebWars.ANGLEDIVISIONS)
            m_angle = 0;
    }

    private void turnLeft()
    {
        m_angle--;

        if(m_angle < 0)
            m_angle = WebWars.ANGLEDIVISIONS - 1;
    }


    public void paint(Graphics g)
    {
        g.setColor(Color.green);
        g.drawLine((int)position().x(),
                   (int)position().y(),
                   (int)position().x()-(int)(15*Math.sin(angleR())),
                   (int)position().y()+(int)(15*Math.cos(angleR())));
    }

    public int lifetime()
    {
        return(10);
    }

    public boolean tracking()  // does it track me?
    {
        return true;
    }

    synchronized public boolean affect(MovingObject who)
    {
        if (who.stationary()==false)
        {
            Vec deltaPosition = new Vec(this.position());
            deltaPosition.subtract(who.position());
            if (deltaPosition.magnitude() < (this.size() + who.size()) )
            {
                if(id() != who.id())
                {
                    // Collision code here
                    this.die();
                    who.addEnergy(-m_damageCaused);
                    return false;  // This should make bullet disappear
                }
            }
        }

        return true;
    }

    public boolean addEnergy(int power)
    {
        this.die();
        return false;
    }

    public String type()
    {
        return new String("Weapon");
    }

    public int damage()
    {
        return m_damageCaused;
    }

    public float angleR()  // angle in Radians
    {
        return (float)(2*Math.PI*(float)m_angle/WebWars.ANGLEDIVISIONS);
    }

}

