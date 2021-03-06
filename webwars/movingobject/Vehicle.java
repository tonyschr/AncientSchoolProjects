package webwars.movingobject;

import java.awt.*;

import webwars.*;

public class Vehicle extends MovingObject
{
    private int m_angle;
    private int m_curWeaponType;
    public int m_energy;
    private long m_timelastshot;
    private Image m_shipImages[];
    private int m_maxEnergy;
    private String m_name = new String("Dr. NoName");
    private int m_deaths;

    public static int idNumGen;
    private int m_idNum;
    private long m_shieldTime;

    public Vehicle(Vec position, Vec velocity, int angle, 
                   Image shipImages[], int size, int energy, MOList moList)
    {
        super(position, velocity, size, 0, moList);
        m_shipImages=shipImages;
        m_idNum = ++idNumGen;
        m_angle=angle;
        m_energy=m_maxEnergy=energy;

        m_curWeaponType=0;
    }

    public void paint(Graphics g)
    {
        //Draw the guy's energy and number of times killed.
        g.setColor(Color.white);
        int ypos = WebWars.ySize - 15 * m_idNum;
        g.drawString(m_name,10,ypos);
        g.drawString("["+m_deaths+"]",80,ypos);

        //If low on energy, color differently.
        if(m_energy > 700)
            g.setColor(Color.yellow);
        else if(m_energy > 300)
            g.setColor(Color.orange);
        else
            g.setColor(Color.red);

        g.drawString(""+m_energy,115,ypos);

        g.drawImage(m_shipImages[m_angle], (int)position().x()-size(),
                    (int)position().y()-size(),null,null);
      
        long fadeTime = m_shieldTime - System.currentTimeMillis();

        if(fadeTime >0)
        {
            g.setColor(Color.cyan);
            g.drawOval((int)position().x()-size()-2,
                       (int)position().y()-size()-2,
                       2*size()+4, 2*size()+4);
        }
    }

    public void turnRight()
    {
        if(++m_angle >= WebWars.ANGLEDIVISIONS)
            m_angle = 0;
    }

    public void turnLeft()
    {
        if(--m_angle < 0)
            m_angle = WebWars.ANGLEDIVISIONS - 1;
    }

    public float angleR()  // angle in Radians
    {
        return (float)(2*Math.PI*(float)m_angle/WebWars.ANGLEDIVISIONS); 
    }

    public MovingObject fire()
    {
        if(System.currentTimeMillis() - m_timelastshot > 0)
        {
            Vec startPosition = new Vec(position());

            Vec offset = new Vec((float)Math.sin(angleR()),
-(float)Math.cos(angleR()));
            Vec offset2 = new Vec((float)Math.sin(angleR()),
-(float)Math.cos(angleR()));
	  
            // the sin/cos mixup is due to the fact that up is 0 and
            // right turns are an increase in angle.
            offset.multiply((float)WebWars.WEAPONSPEED);
            offset.add(velocity());
            offset2.multiply(velocity().magnitude()+18);
            startPosition.add(offset2);

            MovingObject weaponFired = null;
            switch(m_curWeaponType)
            {
            case 0:
                weaponFired = new ShotWeapon(startPosition, offset,
                                             id(), m_moList);
                m_timelastshot = System.currentTimeMillis() +
                    WebWars.BULLETDELAY;

                m_energy-=(int)WebWars.BULLETPOWER * .1;
                if (m_energy<=0)
                    die();
                break;
            case 1:
                weaponFired = new MissileWeapon(startPosition, offset,
                                                id(), m_moList);
                m_timelastshot = System.currentTimeMillis() +
                    WebWars.MISSILEDELAY;

                m_energy-=(int)WebWars.BULLETPOWER * .2;
                if (m_energy<=0)
                    die();
                break;
            case 2:
                weaponFired = new ProxBombWeapon(startPosition, offset,
                                                 id(), m_moList);
                m_timelastshot = System.currentTimeMillis() +
                    WebWars.BOMBDELAY;

                m_energy-=(int)WebWars.BOMBPOWER * .35;
                if (m_energy<=0)
                    die();
                break;
	      
            }
	  
            return weaponFired;
        }

        return null;
    }

    public int weaponType()
    {
        return m_curWeaponType;
    }

    public void changeWeaponType()
    {
        m_curWeaponType++;
        m_curWeaponType %= WebWars.NUMWEAPONS;
    }

    public void thrust()
    {
        float c=WebWars.SPEEDOFLIGHT;  // speed of "light"
        float mag = (float)WebWars.ACCELERATION *
            (1-this.velocity().magSq()/c/c); 
        //how much thrust, with relativity thrown in to keep you from going
        // too fast.  Note that, as in real life, it takes just as much
        // thrust against your direction to slow you down, because your 
        // thrust cannot change your relative velocity as much as you 
        // approach the speed of light.

        // Cap'n, the ship can't take it any more!
        if (velocity().magSq() > c*c)
            velocity().multiply((float)0.9);
        // this code should never be reached except
        // for round off errors in the above formula

        Vec acceleration =
            new Vec(mag*(float)Math.sin(angleR()),-mag*(float)Math.cos(angleR()));

        this.addVelocity(acceleration);
    }

    public void retrothrust()
    {
        float c=WebWars.SPEEDOFLIGHT;  // speed of "light"
        float mag = (float)WebWars.ACCELERATION *
            (1-this.velocity().magSq()/c/c); 
        //how much thrust, with relativity thrown in to keep you from going
        // too fast.  Note that, as in real life, it takes just as much
        // thrust against your direction to slow you down, because your 
        // thrust cannot change your relative velocity as much as you 
        // approach the speed of light.

        // Cap'n, the ship can't take it any more!
        if (velocity().magSq() > c*c)
            velocity().multiply((float)0.9);
        // this code should never be reached except
        // for round off errors in the above formula

        Vec acceleration=
            new Vec(-mag*(float)Math.sin(angleR()),mag*(float)Math.cos(angleR()));

        this.addVelocity(acceleration);
    }

    public void turnToward (Vec target)
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

    public void turnAway (Vec target)
    {
        Vec fromTarget=new Vec(target);
        Vec forward=new Vec(this.position().x()+(float)Math.sin(angleR()),
                            this.position().y()-(float)Math.cos(angleR()));

        fromTarget.subtract(this.position());
        forward.subtract(this.position());

        if (fromTarget.x()*forward.y() > forward.x()*fromTarget.y()) // cross product
            turnRight();
        else
            turnLeft();
    }

    public int energy()
    {
        return m_energy;
    }

    public boolean addEnergy(int power)
    {
        return addEnergy(power, false);
    }

    public boolean addEnergy(int power, boolean drawShield)
    {
        m_energy+=power;

        if (m_energy<=0)
            die();
    
        // cap on energy 
        if (m_energy>m_maxEnergy)
            m_energy=m_maxEnergy;

        if(power < 0 && drawShield)
            m_shieldTime = System.currentTimeMillis() + 100;
        
        return this.alive();
    }

    public void die()
    {
        int x = (int)(600 * Math.random());
        int y = (int)(400 * Math.random());

        setPosition(new Vec(x,y));
        setVelocity(new Vec(0,0));
        m_deaths++;
        m_energy = WebWars.STARTINGENERGY;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    public String type()
    {
        return new String("Vehicle");
    }
    
}

