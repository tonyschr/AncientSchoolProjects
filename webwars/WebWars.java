package webwars;

/************************************************************************
*                                                                        
*   WebWars, Copyright 1997 by:                                          
*                                                                        
*   Keith Buck      buck@holly.colostate.edu                             
*   Tony Schreiner  tschrein@holly.colostate.edu                         
*   Scott Wayland   wayland@cs.colostate.edu                             
*                                                                        
*************************************************************************/


import java.applet.*;
import java.awt.*;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.Vector;
import java.util.Enumeration;

import webwars.movingobject.*;
/**
 * Main webwars applet.
 * <p>
 *
 * <b>Usage:</b>
 * <p>
 * java webwars.WebWars [width height]
 */
public class WebWars extends Applet implements Runnable, MouseListener, KeyListener
{
    //Global constants!  Change these to alter gameplay
    public static int xSize = 600;
    public static int ySize = 400;
    public static int STARTINGENERGY = 1000;
    public static int RECHARGERATE = 1000; //1000 ms
    public static int RECHARGEAMOUNT = 5;  //5 units of energy
    public static int BULLETPOWER = 200;
    public static int MISSILEPOWER = 200;
    public static int BOMBPOWER = 800;
    public static int BULLETDELAY = 300;
    public static int MISSILEDELAY = 700;
    public static int BOMBDELAY = 2000;
    public static int ANGLEDIVISIONS = 24;
    public static int WEAPONSPEED = 7;
    public static int NUMWEAPONS = 3;
    public static int SPEEDFACTOR = 30;
    public static int REDRAWFACTOR = 35;
    public static double ACCELERATION = 1.2;
    public static int SPEEDOFLIGHT = 30;

    private boolean m_gameRunning = false;
    private Image m_ship1Images[] = new Image[24];
    private Image m_ship2Images[] = new Image[24];
    private Image m_ship3Images[] = new Image[24];
    private Image m_planetSize25;
    private Image m_planetSize50;
    private Image m_planetSize100;
    private Image m_planetSize150;
    private Image m_repelPlanet;
    private Image m_rewardImages[] = new Image[12];
    private Image m_spinnerImages[] = new Image[12];
    private Image m_asteroidImages[] = new Image[24];

    private MOList m_moList = new MOList();

    private String m_displayString = new String("Loading Applet...");
    public boolean m_dialogOpen = true;
    private int m_environmentEdges;

    //private int m_computerPlayers = 0;
    private String m_player1Keys = new String("jlik u");
    private String m_player2Keys = new String("adwsqr");

    private Human m_human1;
    private Human m_human2;

    private Vector m_computer = new Vector();

    //Stuff needed for double-buffering
    private Image offScreenImage;
    private Dimension offScreenSize;
    private Graphics offScreenGraphics;
    Dimension d = new Dimension(xSize,ySize);

    private Thread m_WebWars = null;

    private boolean m_fStandAlone = false;

    /**
     * Entry point for the WebWars application.
     */
    public static void main(String args[])
    {
        try
        {
            int xtemp = Integer.parseInt(args[0]);
            int ytemp = Integer.parseInt(args[1]);
            if(xtemp > 300 && ytemp > 300)
            {
                xSize = xtemp;
                ySize = ytemp;
            }
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
        }

        WebWarsFrame frame = new WebWarsFrame("WebWars");
        frame.setVisible(false);
        frame.setSize(xSize+10,ySize+30);

        WebWars applet_WebWars = new WebWars();

        frame.add("Center", applet_WebWars);
        applet_WebWars.m_fStandAlone = true;
        applet_WebWars.init();
        applet_WebWars.start();
        frame.setVisible(true);
    }

    // WebWars Class Constructor
    //--------------------------------------------------------------------------
    public WebWars()
    {
    }

    public void keyTyped(KeyEvent e)
    {
    }

    /**
     *
     */
    public void keyPressed(KeyEvent e)
    {
        char key = e.getKeyChar();

        if(key == 'Q')
        {
            stop();
            start();
        }
    }

    public void keyReleased(KeyEvent e)
    {
    }

    public String getAppletInfo()
    {
        return "WebWars\r\n" + 
            "\r\n" +
            "Copyright 1997, 1998" +
            "Tony Schreiner\r\n" +
            "Keith Buck\r\n" +
            "Scott Wayland\r\n" +
            "";
    }


    /**
     * The init() method is called by the AWT when an applet is first loaded or
     * reloaded.  Override this method to perform whatever initialization your
     * applet needs, such as initializing data structures, loading images or
     * fonts, creating frame windows, setting the layout manager, or adding UI
     * components.
     */
    public void init()
    {
        //This is a global keylistener
        addKeyListener(this);
        
        resize(xSize, ySize);
        setBackground(Color.black);

        appletLoadImages();
    }

    /**
     * Load the images and pause the application/applet until all
     * pictures have been loaded.
     */
    private void appletLoadImages()
    {
        MediaTracker tracker = new MediaTracker(this);
        int mediacounter = 0;

        if(m_fStandAlone == false)
        {
            m_planetSize25 = getImage(getCodeBase(),"images/planet25.gif");
            m_planetSize50 = getImage(getCodeBase(),"images/planet50.gif");
            m_planetSize100 = getImage(getCodeBase(),"images/planet100.gif");
            m_repelPlanet = getImage(getCodeBase(),"images/repel.gif");
        }
        else
        {
            m_planetSize25 = getToolkit().getImage("images/planet25.gif");
            m_planetSize50 = getToolkit().getImage("images/planet50.gif");
            m_planetSize100 = getToolkit().getImage("images/planet100.gif");
            m_repelPlanet = getToolkit().getImage("images/repel.gif");
        }

        tracker.addImage(m_planetSize25,mediacounter++);
        tracker.addImage(m_planetSize50,mediacounter++);
        tracker.addImage(m_planetSize100,mediacounter++);
        tracker.addImage(m_repelPlanet,mediacounter++);

        for(int i = 0; i < 24; i++)
        {
            if(m_fStandAlone == false)
            {
                m_ship1Images[i] = getImage(getCodeBase(),"images/p1ship"+i+".gif");
                m_ship2Images[i] = getImage(getCodeBase(),"images/p2ship"+i+".gif");
                m_ship3Images[i] = getImage(getCodeBase(),"images/p3ship"+i+".gif");
                m_asteroidImages[i] = getImage(getCodeBase(),"images/asteroid"+i+".gif");
            }
            else
            {
                m_ship1Images[i] = getToolkit().getImage("images/p1ship"+i+".gif");
                m_ship2Images[i] = getToolkit().getImage("images/p2ship"+i+".gif");
                m_ship3Images[i] = getToolkit().getImage("images/p3ship"+i+".gif");
                m_asteroidImages[i] = getToolkit().getImage("images/asteroid"+i+".gif");
            }

            tracker.addImage(m_ship1Images[i],mediacounter++);
            tracker.addImage(m_ship2Images[i],mediacounter++);
            tracker.addImage(m_ship3Images[i],mediacounter++);
            tracker.addImage(m_asteroidImages[i],mediacounter++);
        }

        for(int i = 0; i < 12; i++)
        {
            if(m_fStandAlone == false)
            {
                m_rewardImages[i] = getImage(getCodeBase(),"images/reward"+i+".gif");
                m_spinnerImages[i] = getImage(getCodeBase(),"images/spinner"+i+".gif");
            }
            else
            {
                m_rewardImages[i] = getToolkit().getImage("images/reward"+i+".gif");
                m_spinnerImages[i] = getToolkit().getImage("images/spinner"+i+".gif");
            }
            tracker.addImage(m_rewardImages[i],mediacounter++);
            tracker.addImage(m_spinnerImages[i],mediacounter++);
        }
        try
        {
            m_displayString = new String("Loading Images...");
            System.out.println("Loading Images...Please wait...");
            tracker.waitForAll();
            System.out.println("Thank you.");
        }
        catch (InterruptedException e)
        {
            m_displayString = new String("Error loading images!!");
            System.out.println("Error loading images!");
            stop();
        }

    }

    public void destroy()
    {
    }

    //Add double-buffering to the paint method to allow smooth graphics
    public final synchronized void update (Graphics g) 
    {
        if((offScreenImage == null) ||
           (d.width != offScreenSize.width) ||
           (d.height != offScreenSize.height)) 
        {
            offScreenImage = createImage(d.width, d.height);
            offScreenSize = d;
            offScreenGraphics = offScreenImage.getGraphics();
        }
        paint(offScreenGraphics);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    // WebWars Paint Handler
    //--------------------------------------------------------------------------
    public void paint(Graphics g)
    {
        g.setColor(Color.black);
        g.fillRect(0,0,xSize,ySize);
        //g.setColor(Color.white);
        //Commented out on 10/16/97.  Attempting to increase speed.
        //        if(m_moList != null)
        m_moList.paint(g);
        //else
        //g.drawString(m_displayString,100,200);
    }

    /**
     * The start() method is called when the page containing the applet
     * first appears on the screen. The AppletWizard's initial implementation
     * of this method starts execution of the applet's thread.
     */
    public void start()
    {
        m_gameRunning = true;
        
        m_WebWars = new Thread(this);
        m_WebWars.start();
    }
	
    /**
     * The stop() method is called when the page containing the applet is
     * no longer on the screen. The AppletWizard's initial implementation of
     * this method stops execution of the applet's thread.
     */
    public void stop()
    {
        m_gameRunning = false;
        
        if (m_WebWars != null)
        {
            m_WebWars.stop();
            m_WebWars = null;
        }

        if(m_human1 != null)
        {
            m_human1.stop();
            m_human1 = null;
        }
        if(m_human2 != null)
        {
            m_human2.stop();
            m_human2 = null;
        }
        Enumeration computerplayers = m_computer.elements();
        while(computerplayers.hasMoreElements())
        {
            Player player = (Player)computerplayers.nextElement();
            player.stop();
        }
        m_computer = new Vector();
        
        if(m_moList != null)
        {
            m_moList.stop();
            m_moList.erase();
        }

        resetValues();
    }

    private void resetValues()
    {
        Vehicle.idNumGen = 0;
    }
    
    public void run()
    {
        OptionsDialog();

        m_moList.start();

        while (m_gameRunning)
        {
            try
            {
                long startTime = System.currentTimeMillis();
                repaint();
                long sleepTime = WebWars.REDRAWFACTOR -
                    (System.currentTimeMillis() - startTime);
	      
                if(sleepTime > 0)
                {
                    Thread.sleep(sleepTime);
                }

                Thread.sleep(20);
            }
            catch (InterruptedException e)
            {
                stop();
            }
        }
    }

    public void setPlayerKeys(String player1,String player2)
    {
        if(player1.length() >= 6)
            m_player1Keys = player1;
        if(player2.length() >= 6)
            m_player2Keys = player2;
    }

    private void OptionsDialog()
    {
        DialogWindow window = new DialogWindow(this);

        window.pack();
        //window.resize(window.insets().left + window.insets().right  + 600,
        //			 window.insets().top  + window.insets().bottom + 400);

        window.setTitle("WebWars Options");
        window.setVisible(true);
    }

    public void addPlayer(int type, String name)
    {
        int x = (int)(xSize * Math.random());
        int y = (int)(ySize * Math.random());

        if(type == 0) // human player 1
        {

            Vehicle tempVehicle = new Vehicle(new Vec(x,y), new Vec(0,0), 0, m_ship2Images, 10, STARTINGENERGY, m_moList);
            m_moList.add(tempVehicle);
            m_human1 = new Human(name,0,m_player1Keys,tempVehicle,m_moList);
            addKeyListener(m_human1);
            m_human1.start();
        }
        if(type == 1)
        {
            Vehicle tempVehicle = new Vehicle(new Vec(x,y), new Vec(0,0), 0, m_ship3Images, 10, STARTINGENERGY , m_moList);
            m_moList.add(tempVehicle);
            m_human2 = new Human(name,0,m_player2Keys,tempVehicle,m_moList);
            addKeyListener(m_human2);
            m_human2.start();
        }
    }

    public void addComputer(int type)
    {
        if(type >= 0)
        {
            int x = (int)(xSize * Math.random());
            int y = (int)(ySize * Math.random());

            Vehicle tempVehicle = new Vehicle(new Vec(x,y), new Vec(0,0), 0, m_ship1Images, 10, STARTINGENERGY, m_moList);
            m_moList.add(tempVehicle);
            Computer computer;
            switch(type)
            {
            case 1:
                computer = new JavaRobot1(0,tempVehicle,m_moList);
                break;
            case 2:
                computer = new JavaRobot2(0,tempVehicle,m_moList);
                break;
            case 3:
                computer = new JavaRobot3(0,tempVehicle,m_moList);
                break;
            case 4:
                computer = new JavaRobot4(0,tempVehicle,m_moList);
                break;
            default:
                computer = new Computer("Generic",0,tempVehicle,m_moList);
                break;
            }
            m_computer.addElement(computer);
            computer.start();
        }
    }

    public void setEnvironment(int property, int numberPlanets, int numberAsteroids,
                               int numberRewards)
    {
        m_moList.add(new Environment(0, property, m_moList));

        for(int i = 0; i < numberPlanets; i++)
        {
            int x = (int)(xSize * Math.random());
            int y = (int)(ySize * Math.random());
            int gravity =(int) (500 + (2000 * Math.random()));

            int size = (int) (5 * Math.random());
            if(size == 0)
                m_moList.add(new Planet(new Vec(x,y),gravity, 12, m_planetSize25, m_moList));
            else if(size == 1)
                m_moList.add(new Planet(new Vec(x,y),gravity, 25, m_planetSize50, m_moList));
            else if(size == 2)
                m_moList.add(new Planet(new Vec(x,y),gravity, 50, m_planetSize100, m_moList));
            else if(size == 3)
                m_moList.add(new Planet(new Vec(x,y),-gravity, 17, m_repelPlanet, m_moList));			
            else
                m_moList.add(new Spinner(new Vec(x,y),gravity, 25, m_spinnerImages, m_moList));
        }

        for(int i = 0; i < numberRewards; i++)
            m_moList.add(new Reward(m_rewardImages,m_moList));
        for(int i = 0; i < numberAsteroids; i++)
            m_moList.add(new Asteroid(m_asteroidImages,m_moList));
    }

    //Mouse listener stuff
    public void mouseClicked(MouseEvent e)
    {
        requestFocus();
    }
    public void mouseEntered(MouseEvent e)
    {
        requestFocus();
    }
    public void mouseExited(MouseEvent e)
    {
    }
    public void mousePressed(MouseEvent e)
    {
    }
    public void mouseReleased(MouseEvent e)
    {
    }
}
