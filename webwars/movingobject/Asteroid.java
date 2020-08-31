package webwars.movingobject;

import java.awt.*;

import webwars.*;

/** An asteroid is an obstacle for players.  It drifts with constant
 *  velocity across the screen, following the same rules as other
 *  Moving Objects.
 */
public class Asteroid extends MovingObject
{
    public int m_energy;
    private Image m_astImages[];
    private int m_maxEnergy;
    private int m_angle;
    private long m_rotateTime;
    
    /** Asteroid constructor, used to create a new asteroid.  Asteroids
     *  start out at a random position with a random initial rotation
     *  angle.
     *
     *  @param astImages Array of Image objects for the asteroid.  The
     *                   asteroid has 24 images for its rotation.
     *  @param moList Reference to main Moving Object List.
     */
    public Asteroid(Image astImages[], MOList moList)
    {
        super(new Vec((int)(WebWars.xSize * Math.random()),
                      (int)(WebWars.ySize * Math.random())), 
              new Vec((int)(5 * Math.random() + 1),
                      (int)(5 * Math.random() +1)),
              16, false, true, 0, moList);
        
        m_angle = (int)(10 * Math.random());
        m_astImages = astImages;
        m_energy = 500;
    }

    /** Draws the image of this asteroid on the screen.
     *
     *  @param g Graphics object representing the main drawing
     *           are for the Applet or Application.
     */
    public void paint(Graphics g)
    {
        g.drawImage(m_astImages[m_angle], (int)position().x()-size(),
                    (int)position().y()-size(), null, null);
    }

    /** Move method moves the asteroid and causes it to rotate at a
     *  constant speed.  It cycles through 24 images for the rotation.
     */
    public boolean move()
    {
        if(m_rotateTime < System.currentTimeMillis())
        {
            if(++m_angle > 23)
            {
                m_angle = 0;
            }
	 
            m_rotateTime = System.currentTimeMillis() + 85;
        }
      
        //if (stationary()==false) //Asteroid is never stationary?
        //{
        position().add(velocity());
        //}
      
        return true;
    }


    /** Returns the current energy for this object.
     *
     *  @return int containing this object's energy level.
     */
    public int energy()
    {
        return m_energy;
    }

    /** Causes the asteroid to die, mainly if it hits a planet.
     */
    public void die()
    {
        int x = (int)(WebWars.xSize * Math.random());
        int y = (int)(WebWars.ySize * Math.random());
        int xvel = (int)(3 * Math.random());
        int yvel = (int)(3 * Math.random());

        setPosition(new Vec(x,y));
        setVelocity(new Vec(0,0));
        m_energy = 10000;
    }

    /** Affect method for image collision and gravity.
     *
     *  @param who Moving Object to affect.  Causes all objects that can
     *             die to die when they hit the asteroid.
     *
     *  @return boolean value if something was affected???
     */
    synchronized public boolean affect(MovingObject who)
    {
        Vec deltaPos = new Vec(this.position());
        deltaPos.subtract(who.position());
        if (deltaPos.magnitude() < this.size() + who.size())
        {
            // Collision code here
            who.die();
            return(false);
        }

        return true;
    }

    /** Adds energy to the moving object.  Not sure why this is here
     *  for an asteroid...
     *
     *  @param power Amount of energy to add (or subract, if it is negative).
     */
    public boolean addEnergy(int power)
    {
        m_energy+=power;

        if (m_energy<=0)
        {
            die();
        }
      
        return false;
    }

    /** Returns a string containing the type of this Moving Object.
     */
    public String type()
    {
        return new String("Asteroid");
    }
}
