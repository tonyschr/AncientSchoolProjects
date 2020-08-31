package webwars.movingobject;

import java.awt.*;

import webwars.*;

public class Environment extends MovingObject
{
    // for now there is just one type, so this is a dummy arg
    private int m_type;  
    private int m_wrapBounce ;

    synchronized public boolean affect(MovingObject who)
    {
        if(m_wrapBounce == 1)
        {
            if (who.position().x() < who.size())
            {
                who.setVelocity(new Vec(-who.velocity().x(),who.velocity().y()));
                who.setPosition(new Vec(0+who.size(),who.position().y()));
                who.move();
            }
            else if(who.position().x() > WebWars.xSize-who.size())
            {
                who.setVelocity(new Vec(-who.velocity().x(),who.velocity().y()));
                who.setPosition(new Vec(WebWars.xSize-who.size(),who.position().y()));
                who.move();
            }
            if(who.position().y() < who.size())
            {
                who.setVelocity(new Vec(who.velocity().x(),-who.velocity().y()));
                who.setPosition(new Vec(who.position().x(),0+who.size()));
                who.move();            
            }
            else if(who.position().y() > WebWars.ySize-who.size())
            {
                who.setVelocity(new Vec(who.velocity().x(),-who.velocity().y()));
                who.setPosition(new Vec(who.position().x(),WebWars.ySize-who.size()));
                who.move();
            }
        }
        else
        {
            if(who.position().x() <0)
                who.setPosition(new Vec(WebWars.xSize,who.position().y()));
            else if(who.position().x() > WebWars.xSize)
                who.setPosition(new Vec(0,who.position().y()));
            if(who.position().y() <0)
                who.setPosition(new Vec(who.position().x(),WebWars.ySize));
            else if(who.position().y() > WebWars.ySize)
                who.setPosition(new Vec(who.position().x(),0));
        }

        return true;
    }

    public Environment(int type, int wrapBounce, MOList moList)
    {
        super(new Vec(0,0), new Vec(0,0), 0, true, true, 0, moList);
        // this won't do anything until we have different types defined
        m_type=type;
        m_wrapBounce = wrapBounce;
    }

    public void paint(Graphics g)
    {
        // This should draw the background
    }

    public void die()
    {
        // The Environment can't die...
    }

    public String type()
    {
        return new String("Environment");
    }
}

