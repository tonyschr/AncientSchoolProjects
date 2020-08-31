package webwars.movingobject;

import java.awt.*;

import webwars.*;

public class Reward extends MovingObject
{
    private Image m_rewardImages[];
    private int m_maxEnergy;
    private int m_angle;
    private int m_bonus;
    private long m_rotateTime;

    public Reward(Image rewardImages[], MOList moList)
    {  
        super(new Vec((int)(WebWars.xSize * Math.random()),
                      (int)(WebWars.ySize * Math.random())), 
              new Vec(0,0),
              10, true, true, 0, moList);

        m_angle = (int)(10 * Math.random());
        m_bonus = 100 + (int)(300 * Math.random());
        m_rewardImages=rewardImages;
    }

    public void paint(Graphics g)
    {
        g.drawImage(m_rewardImages[m_angle], (int)position().x()-size(),
                    (int)position().y()-size(), null, null);
    }

    public boolean move()
    {
        if(m_rotateTime < System.currentTimeMillis())
        {
            if(++m_angle > 11)
                m_angle = 0;
            m_rotateTime = System.currentTimeMillis() + 50;
        }
	
        return true;
    }

    public void die()
    {
        int x = (int)(WebWars.xSize * Math.random());
        int y = (int)(WebWars.ySize * Math.random());
        int xvel = (int)(3 * Math.random());
        int yvel = (int)(3 * Math.random());

        setPosition(new Vec(x,y));
        setVelocity(new Vec(0,0));
    }

    synchronized public boolean affect(MovingObject who)
    {
        if (who.stationary()==false)
        {
            Vec deltaPosition = new Vec(this.position());
            deltaPosition.subtract(who.position());
            if (deltaPosition.magnitude() < (this.size() + who.size()) )
            {
                // Collision code here
                this.die();
                who.addEnergy(m_bonus);
                return false;  // This should make bullet disappear
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
        return new String("Reward");
    }

}

