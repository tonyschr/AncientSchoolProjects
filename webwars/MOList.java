package webwars;

import java.util.*;
import java.awt.*;

import webwars.movingobject.*;

/**
 * Moving Object List class.  Contains all of the "Moving Objects" in the
 * game.  Moving Objects may be visible or invisible, and can interact
 * with each other.
 *
 * Moving Objects may also be stationary, etc.
 */
public class MOList implements Runnable
{
    private Thread m_moThread = null;
    private Vector m_moList = new Vector();
    private long m_rechargeTime;

    public MOList()
    {
        m_rechargeTime = System.currentTimeMillis();
    }
    
    public void erase()
    {
        m_moList = new Vector();
    }
    
    /**
     * Add a moving object to the collection
     *
     * @param mo MovingObject to add
     */
    public synchronized void add(MovingObject mo)
    {
        if(mo != null)
        {
            m_moList.addElement(mo);
        }
    }

    /**
     * Delete a moving object from the list
     *
     * @param index Index to delete an object from.  This is the less
     * safe version of delete as changes in the list between querying
     * the index number and deleting it may cause a user to delete the
     * wrong object.
     */
    public synchronized void delete(int index)
    {
        m_moList.removeElementAt(index);
    }

    /**
     * Delete a moving object from the list
     *
     * @param obj MovingObject to delete.
     */
    public synchronized void delete(MovingObject obj)
    {
        m_moList.removeElement(obj);
    }

    /**
     * Moves and does collision detection between all of the moving
     * objects in the game.
     */
    public synchronized void move()
    {
        //moving references outside of loop so we don't have to create
        //them each time (10/16/97).
        MovingObject tempobj = null;
        MovingObject affectobj = null;

        for(int i = 0; i<size(); i++)
        {
            tempobj = (MovingObject)m_moList.elementAt(i);

            if(tempobj.move()==false)
            {
                delete(tempobj);
            }
            else
            {
                if(tempobj instanceof Vehicle &&
                   System.currentTimeMillis() > m_rechargeTime)
                {
                    ((Vehicle)tempobj).addEnergy(WebWars.RECHARGEAMOUNT, false);
                    m_rechargeTime = WebWars.RECHARGERATE + System.currentTimeMillis();
                }
                for(int j = 0; j < size(); j++)
                {
                    if(i != j)
                    {
                        affectobj = (MovingObject)m_moList.elementAt(j);
                        affectobj.affect(tempobj);
                    }
                }
            }
        }		
    }

    /**
     * Paint all of the moving objects on the screen.
     */
    public void paint(Graphics g) 
    { 
        MovingObject tempobj = null;
        for(int i = 0; i<size(); i++)
        {
            tempobj = (MovingObject)m_moList.elementAt(i);
            tempobj.paint(g);
        }		
    }

    /**
     * <b>Utility Function</b>.  Returns the closest moving object to a
     * specified point.
     *
     * @param p The Point
     * @return MODescriptor representing the moving object so that
     * the caller cannot modify the moving object.
     */
    public MODescriptor closest(Vec p)
    {
        float smallestdistSq = 10000000;
        int smallestindex = 0;
        
        for(int i = 1; i<size(); i++)
        {
            MovingObject tempobj = (MovingObject)m_moList.elementAt(i);
            Vec dist = tempobj.position().minus(p);
            float distanceSq = dist.magSq();
            if(distanceSq < smallestdistSq)
            {
                smallestdistSq = distanceSq;
                smallestindex = i;
            }
        }

        MovingObject tempobj = (MovingObject)m_moList.elementAt(smallestindex);

        return new MODescriptor(tempobj.id(),
                                (int)tempobj.position().x(),
                                (int)tempobj.position().y(),
                                (int)tempobj.velocity().x(),
                                (int)tempobj.velocity().y(),
                                tempobj.size(),
                                tempobj.damage(),tempobj.type());
    }

    /**
     * <b>Utility Function</b>.  Returns the closest moving object to a
     * specified moving object
     *
     * @param obj The moving object
     * @return MODescriptor representing the moving object so that
     * the caller cannot modify the moving object.
     */
    public MODescriptor closest (MovingObject obj)
    {
        float smallestdistSq = 10000000;
        int smallestindex = 0;
        
        for(int i = 1; i<size(); i++)
        {
            MovingObject tempobj = (MovingObject)m_moList.elementAt(i);
            if(tempobj.id() != obj.id())
            {
                Vec dist = tempobj.position().minus(obj.position());
                float distanceSq = dist.magSq();
                if(distanceSq < smallestdistSq)
                {
                    smallestdistSq = distanceSq;
                    smallestindex = i;
                }
            }
        }

        MovingObject tempobj = (MovingObject)m_moList.elementAt(smallestindex);

        return new MODescriptor(tempobj.id(),
                                (int)tempobj.position().x(),
                                (int)tempobj.position().y(),
                                (int)tempobj.velocity().x(),
                                (int)tempobj.velocity().y(),
                                tempobj.size(),
                                tempobj.damage(),tempobj.type());
    }

 
    /**
     * <b>Utility Function</b>.  Returns the closest vehicle to a
     * specified moving object
     *
     * @param obj The moving object
     * @return MODescriptor representing the moving object so that
     * the caller cannot modify the moving object.
     */
    public MODescriptor closestVehicle(MovingObject obj)
    {
        float smallestdistSq = 10000000;
        int smallestindex = 0;
        
        for(int i = 1; i<size(); i++)
        {
            MovingObject tempobj = (MovingObject)m_moList.elementAt(i);
            if(tempobj.id() != obj.id() && tempobj.type().compareTo("Vehicle") == 0)
            {
                Vec dist = tempobj.position().minus(obj.position());
                float distanceSq = dist.magSq();
                if(distanceSq < smallestdistSq)
                {
                    smallestdistSq = distanceSq;
                    smallestindex = i;
                }
            }
        }

        MovingObject tempobj = (MovingObject)m_moList.elementAt(smallestindex);

        return new MODescriptor(tempobj.id(),
                                (int)tempobj.position().x(),
                                (int)tempobj.position().y(),
                                (int)tempobj.velocity().x(),
                                (int)tempobj.velocity().y(),
                                tempobj.size(),
                                tempobj.damage(),tempobj.type());
    }

    /**
     * <b>Utility Function</b>.
     *
     * @param objectnum The moving object number to get the descript for
     * @return MODescriptor representing the moving object so that
     * the caller cannot modify the moving object.
     */

    public MODescriptor getMODescriptor(int objectnum)
    {
        for(int i = 1; i<size();i++)
        {
            MovingObject tempobj = (MovingObject)m_moList.elementAt(i);
            if(tempobj.id() == objectnum)
            {
                return new MODescriptor(tempobj.id(),
                                        (int)tempobj.position().x(),
                                        (int)tempobj.position().y(),
                                        (int)tempobj.velocity().x(),
                                        (int)tempobj.velocity().y(),
                                        tempobj.size(),
                                        tempobj.damage(),tempobj.type());
            }
        }
        return null;
    }

    /**
     * Returns the size of the moving object list
     */
    public int size()
    {
        return m_moList.size();
    }

    /**
     * Who knows
     */
    public MovingObject info(int id)
    {
        return null;
    }

    /**
     * Who knows...
     */
    public Environment associatedEnvironment()
    {
        return null;
    }

    /**
     * Start the thread for this object going.
     */
    public void start()
    {
        if (m_moThread == null)
        {
            m_moThread = new Thread(this);
            m_moThread.start();
        }
    }

    /**
     * Stop the thread
     */
    public void stop()
    {
        if (m_moThread != null)
        {
            m_moThread.stop();
            m_moThread = null;
        }
    }

    /**
     * Main loop for the MOList.
     *
     * Inifinite loop calling move()
     */
    public void run()
    {
        while(true)
        {
            long startTime = System.currentTimeMillis();
            try
            {
                move();

                long sleepTime = WebWars.SPEEDFACTOR -
                    (System.currentTimeMillis() - startTime);
	      
                if(sleepTime >0)
                {
                    Thread.sleep(sleepTime);
                }
                else
                {
                    System.out.println("DEBUG: Too slow, factor = " +
                                       sleepTime);
                }

                Thread.sleep(10);
                Thread.yield();
            }
            catch(InterruptedException e)
            {
                stop();
            }
        }
    }
}

