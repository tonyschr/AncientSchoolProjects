package webwars.movingobject;

import java.awt.*;

import webwars.*;

public class ProxBombWeapon extends MovingObject implements WeaponI
{
    private int m_damageCaused;
    private long m_lifetime;
    private int m_angle;

    ProxBombWeapon(Vec position, Vec initialVelocity, int id, MOList moList)
    {
        super(position, initialVelocity, 15, false, true, id, moList);  
      
        m_damageCaused = WebWars.BOMBPOWER;
        m_lifetime = System.currentTimeMillis();
    }

    public boolean move()
    {
        if (stationary() == false)
        {
            position().add(velocity());
        }

        if(System.currentTimeMillis() - m_lifetime > 3300)
        {
            return false;
        }
        else
        {
            if(this.alive())
                return true;
            else
                return false;
        }
    }

    public void paint(Graphics g)
    {
        // Draw circle here
        g.setColor(new Color((int)(150*Math.random()+100),
                             (int)(150*Math.random()+100),
                             (int)(150*Math.random()+100)));
        g.fillOval((int)position().x()-1,(int)position().y()-1,2,2);
        g.setColor(new Color((int)(150*Math.random()+100),
                             (int)(150*Math.random()+100),
                             (int)(150*Math.random()+100)));
        g.drawOval((int)position().x()-3,(int)position().y()-3,6,6);
    }

    public int lifetime()
    {
        return(10);
    }

    public boolean tracking()
    {
        return false;
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
}

