package webwars;

import java.awt.*;
import java.awt.event.*;

import webwars.movingobject.*;

public class JavaRobot1 extends Computer
{
    private int rechargeGoal = 0;
    private int dodge = 1;

    public JavaRobot1(int teamNumber, Vehicle vehicle, MOList moList)
    {
        super("JavaRobot 1",teamNumber,vehicle,moList);
    }


    public void playermove()
    {
        MODescriptor targetobject = closestMO();
        MODescriptor targetVehicle = closestVehicle();

        if(energy() < 300)
            rechargeGoal = 700;
        else if(energy() > rechargeGoal)
            rechargeGoal = 0;

        if(targetobject.type().equals("Weapon"))
        {            
            if(20 * Math.random() > 19)
                dodge = -dodge;

            if(targetobject.position().minus(position()).magSq() < 10000)
            {

                turnToward(targetobject.position());
                while(weaponType() != 0)
                    changeWeaponType();
                if(energy() > 150)
                    fire();
                turnToward(targetobject.position().plus(velocity()));
                retroThrust();
            }
            else
            {
                while(velocity().magSq() > 1 )
                    slowdown();
            }
        }
        else if(targetobject.type().equals("Asteroid") ||
                targetobject.type().equals("Planet"))
        {
            if(targetobject.position().minus(position()).magSq() < (float)40000)
            {
                turnToward(targetobject.position());
                retroThrust();
            }
            else
            {
                while(velocity().magSq() > 1 )
                    slowdown();
            }
        }
        else if(energy() > rechargeGoal)
        {
            if(targetVehicle.position().minus(position()).magSq() > (float)90000)
            {
                turnToward(targetVehicle.position().plus(targetVehicle.velocity()));
                thrust();
            }
            else
            {
                while(velocity().magSq() > 1 )
                    slowdown();
                turnToward(targetVehicle.position());
                if(10 * Math.random() > 9)
                    changeWeaponType();
                fire();
            }

        }
        else if(targetobject.type().equals("Reward"))
        {
            dodge = 0;
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
