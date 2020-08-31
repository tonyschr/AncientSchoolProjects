package webwars;

import java.awt.*;
import java.awt.event.*;

import webwars.movingobject.*;

/**
 * This is a very basic computer player
 * Runs from everything, collects rewards, never fires.
 *
 * Serves as a superclass for all computer players.
 */
public class Computer extends Player
{
    /**
     * Constructor.
     *
     * @param name Computer player's name
     * @param teamNumber Team number (teams not implemented yet)
     * @param vehicle Vehicle for computer to control
     * @param moList Reference to main Moving Object List.
     */
    public Computer(String name, int teamNumber, Vehicle vehicle,
                    MOList moList)
    {
        super(name,teamNumber,vehicle,moList);
    }

    /**
     * The brains of the computer.  This method is called every time the
     * computer player deserves to move.
     */
    public void playermove()
    {
        MODescriptor targetobject = closestMO();

        if(!targetobject.type().equals("Reward"))
        {
            if(targetobject.position().minus(position()).magSq() < 300*300)
            {
                turnAway(targetobject.position());
                thrust();
            }
            else
            {
                while(velocity().magSq() > 1 )
                    slowdown();
            }
        }
        else
        {
            Vec velRelative = targetobject.velocity().minus(velocity());
            Vec distance = targetobject.position().minus(position());

            float perpFrac = distance.cross(velRelative) /
                distance.magnitude()/velRelative.magnitude();

            Vec perpendicular = velRelative.times(perpFrac);

            if (perpendicular.magSq() > (float)(1.5*1.5))
                turnToward(this.position().plus(velRelative));
            else
                turnToward(targetobject.position());

            thrust();
        }
    }

}
