package stomp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import stomp.gui.*;
//import stomp.anim.*;
import stomp.view.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import stomp.command.*;

/**
 * Stomp3D
 * Schreiner and Speck's Tool for Object Model Production.
 *
 * Copyright 1998 Tony Schreiner and Jon Speck.
 *
 * This is the controlling class for the Stomp application.  This
 * class does the initial setup of the GUI, views, etc.
 */
public class Stomp extends java.awt.Frame
{
    //The current build number.  Controls versioning for files, etc.
    public static final int BUILD = 301;
    public static final String VERSION = "0.6.5 (beta)";

    //StatusBar component.  It is static and public so that a variety
    //of commands can easily access it for the progress bar.
    public static StatusBar statusBar = new StatusBar();

    //Animation window
    //private AnimationPanel m_animPanel;
    //private Animation m_animation;
    
    //ButtonBar component.
    private ButtonBar m_buttonBar;
    
    //Event listener.
    private GUIListener m_guiListener;

    //Plugin support
    Menu m_plugins = new Menu("Plugins");
    FastVector m_pluginCommands = new FastVector();
    FastVector m_pluginNames = new FastVector();
    
    //Views
    private View m_topView;
    private View m_frontView;
    private View m_sideView;
    private View m_perspectiveView;
    private ViewContainer m_viewContainer;

    //Scene
    private Scene m_scene;
    //    SceneReaderWriter m_readerWriter = new StompReaderWriter();
    
    /**
     * Constructor, starts STOMP
     */
    public Stomp()
    {
        super("Stomp3D");
        System.out.println("\nStomp3D\nCopyright 1998, Tony Schreiner and Jon Speck.");
        initializeStomp(new Scene());
    }

    /**
     * Initialize STOMP.  Sets up the user interface and sets
     * the current scene.
     *
     * @param scene Scene to edit.
     */
    public void initializeStomp(Scene scene)
    {
        removeAll();
        if(scene == null)
        {
            scene = new Scene();
        }
        
        //Set this frame's font to Helvetica for standard look
        setFont(Appearance.getFont());
        setLayout(new BorderLayout());
        
        m_guiListener = new GUIListener();

        //Add m_guiListener as a listener for this window.
        addWindowListener(m_guiListener);

        setScene(scene);
        
        setupButtonBar();
        setupMenu();
        setupViews();
        //setupAnimationPanel();
        setupStatusBar();

        m_guiListener.setScene(m_scene);
        m_guiListener.setStomp(this);
        m_guiListener.setViewContainer(m_viewContainer);

        //Load properties from $HOME/stomp.config file
        StompProperties properties = new StompProperties(this);
        properties.loadProperties();
        
        pack();
        repaint();
        setVisible(true);
        repaint();
    }
    
    /**
     * Instantiate the button bar and add it to the frame
     */
    private void setupButtonBar()
    {
        m_buttonBar = new ButtonBar(m_guiListener);
        add(m_buttonBar, BorderLayout.WEST);
        addKeyListener(m_buttonBar);
        m_buttonBar.addKeyListener(m_buttonBar);
    }

//      private void setupAnimationPanel()
//      {
//          m_animation = new Animation(m_scene);
//          m_animPanel = new AnimationPanel(m_animation);
//          //showAnimPanel();
//      }

//      public void showAnimPanel()
//      {
//          m_animPanel.setVisible(true);
//          m_animPanel.toFront();
//      }
    
    /**
     * Instantiate the status bar and add it to the frame
     */
    private void setupStatusBar()
    {
        add(statusBar, BorderLayout.SOUTH);
        Mode.getActualMode().addObserver(statusBar);
    }

    /**
     * Create the menus and add to the frame.
     */
    private void setupMenu()
    {
        MenuBar menuBar = new MenuBar();

        //Create each menu and associated menu items

        //Create File menu
        Menu fileMenu = new Menu("File");
        MenuItem fileNew = new MenuItem("New");
        fileMenu.add(fileNew);
        fileNew.addActionListener(m_guiListener);
        MenuItem fileLoad = new MenuItem("Load");
        fileMenu.add(fileLoad);
        fileLoad.addActionListener(m_guiListener);
        MenuItem fileSave = new MenuItem("Save");
        fileMenu.add(fileSave);
        fileSave.addActionListener(m_guiListener);

        //add export submenu
        Menu export = new Menu("Export");
        MenuItem cs410 = new MenuItem("CS410 Lab");
        export.add(cs410);
        cs410.addActionListener(m_guiListener);
        MenuItem cs510 = new MenuItem("CS510 GUI");
        export.add(cs510);
        cs510.addActionListener(m_guiListener);
        MenuItem alias = new MenuItem("Wavefront OBJ");
        export.add(alias);
        alias.addActionListener(m_guiListener);
        MenuItem vrml = new MenuItem("VRML");
        export.add(vrml);
        vrml.addActionListener(m_guiListener);
        MenuItem pov = new MenuItem("POV");
        export.add(pov);
        pov.addActionListener(m_guiListener);
        MenuItem dx = new MenuItem("DirectX");
        export.add(dx);
        dx.addActionListener(m_guiListener);
        fileMenu.add(export);

        //import submenu
        Menu imp = new Menu("Import");
        MenuItem cs410in = new MenuItem("CS410 Lab ");
        imp.add(cs410in);
        cs410in.addActionListener(m_guiListener);
        MenuItem cs510in = new MenuItem("CS510 GUI ");
        imp.add(cs510in);
        cs510in.addActionListener(m_guiListener);
        fileMenu.add(imp);
        
        MenuItem fileQuit = new MenuItem("Quit");
        fileMenu.add(fileQuit);
        fileQuit.addActionListener(m_guiListener);

        //Edit menu
        Menu editMenu = new Menu("Edit");
        MenuItem undo = new MenuItem("Undo");
        MenuItem redo = new MenuItem("Redo");
        MenuItem editCut = new MenuItem("Cut");
        MenuItem editCopy = new MenuItem("Copy");
        MenuItem editPaste = new MenuItem("Paste");
        editMenu.add(undo);
        undo.addActionListener(m_guiListener);
        editMenu.add(redo);
        redo.addActionListener(m_guiListener);
        editMenu.add(editCut);
        editCut.addActionListener(m_guiListener);
        editMenu.add(editCopy);
        editCopy.addActionListener(m_guiListener);
        editMenu.add(editPaste);
        editPaste.addActionListener(m_guiListener);
        editMenu.addSeparator();
        MenuItem editPreferences = new MenuItem("Options");
        editMenu.add(editPreferences);
        editPreferences.addActionListener(m_guiListener);
        
        //Create View menu
        Menu viewMenu = new Menu("View");
        MenuItem stats = new MenuItem("Show Statistics");
        viewMenu.add(stats);
        stats.addActionListener(m_guiListener);
        MenuItem preview = new MenuItem("VRML Preview");
        viewMenu.add(preview);
        preview.addActionListener(m_guiListener);
        MenuItem render = new MenuItem("POV Render");
        viewMenu.add(render);
        render.addActionListener(m_guiListener);
//          MenuItem anim = new MenuItem("Animation Panel");
//          viewMenu.add(anim);
//          anim.addActionListener(m_guiListener);
        MenuItem defaultZoom = new MenuItem("Default Views");
        viewMenu.add(defaultZoom);
        defaultZoom.addActionListener(m_guiListener);
        Menu fourth = new Menu("Preview Options");
        MenuItem previewView = new MenuItem("Perspective");
        fourth.add(previewView);
        previewView.addActionListener(m_guiListener);
        MenuItem cameraView = new MenuItem("Camera");
        fourth.add(cameraView);
        cameraView.addActionListener(m_guiListener);
        viewMenu.add(fourth);

        //Plugin Menu
        MenuItem setPluginLocation = new MenuItem("Plugins Location");
        m_plugins.add(setPluginLocation);
        setPluginLocation.addActionListener(m_guiListener);
        m_plugins.addSeparator();

        //Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem helpAbout = new MenuItem("About");
        helpMenu.add(helpAbout);
        helpAbout.addActionListener(m_guiListener);

        //Add the menus to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(m_plugins);
        menuBar.setHelpMenu(helpMenu);

        setMenuBar(menuBar);
    }

    /**
     * Instantiate the views and add to the ViewContainer.
     * Add the ViewContainer to the frame.
     */
    private void setupViews()
    {        
        m_topView = new Orthogonal("Top", true, false, true, m_scene);
        m_frontView = new Orthogonal("Front", true, true, false, m_scene);
        m_sideView = new Orthogonal("Side", false, true, true, m_scene);
        m_perspectiveView = new Perspective("Perspective", m_scene);
        
        m_viewContainer = new ViewContainer(m_topView, m_frontView,
                                            m_sideView, m_perspectiveView);
        add(m_viewContainer, BorderLayout.CENTER);
        m_scene.addViewContainer(m_viewContainer);

        m_topView.addKeyListener(m_buttonBar);
        m_sideView.addKeyListener(m_buttonBar);
        m_frontView.addKeyListener(m_buttonBar);
        m_perspectiveView.addKeyListener(m_buttonBar);

    }

    /**
     * Set the scene for Stomp to be editing.
     */
    public void setScene(Scene scene)
    {
        m_scene = scene;
    }

    /**
     * Add plugins to the menu for the specified directory.
     */
    public void addPlugins(String directory)
    {
        File dir = new File(directory);
        if(dir.isDirectory())
        {
            //Consider all files in the directory ending in .class as plugins
            String files[] = dir.list();
            for(int f = 0; f < files.length; f++)
            {
                if(files[f].endsWith(".class"))
                {
                    String pluginName =
                        files[f].substring(0, files[f].length() - 6);
                    addPlugin(pluginName);
                }
            }
        }
    }

    /**
     * Add a plugin with the given class name to Stomp.  Verifies
     * that the class extends PluginCommand and adds it to the menu
     * and list of Plugin classes.
     */
    public void addPlugin(String classname)
    {
        for(int i = 0; i < m_pluginNames.sizeFast(); i++)
        {
            String plName = (String)m_pluginNames.elementAtFast(i);
            if(plName.equals(classname))
            {
                System.out.println("  - Duplicate plugin not added: " +
                                   classname);
                return;
            }
        }
        try
        {
            Class pluginClass = Class.forName(classname);
            Class command = Class.forName("stomp.command.PluginCommand");
            if(command.isAssignableFrom(pluginClass))
            {
                System.out.println("  + plugin: " + classname);
                m_pluginNames.addElement(classname);
                m_pluginCommands.addElement(pluginClass);
                MenuItem newPlugin = new MenuItem(classname);
                m_plugins.add(newPlugin);
                newPlugin.addActionListener(m_guiListener);
            }
        }
        catch(ClassNotFoundException e)
        {
            System.out.println("Class not found! " + e);
            return;
        }
    }

    /**
     * Get a PluginCommand object that corresponds with the name of the
     * plulgin.  Scene keeps a table of the plugin name and its associated
     * class.
     */
    public Command getPluginCommand(String commandName)
    {
        for(int i = 0; i < m_pluginNames.sizeFast(); i++)
        {
            System.out.println("" + m_pluginNames.elementAtFast(i));
            System.out.println("c " + commandName);
            if(((String)m_pluginNames.elementAtFast(i)).equals(commandName))
            {
                try
                {
                    Class commandClass = (Class)m_pluginCommands.elementAtFast(i);
                    PluginCommand command =
                        (PluginCommand)commandClass.newInstance();
                    command.setupPlugin(m_scene, m_guiListener);
                    return command;
                }
                catch(InstantiationException ie)
                {
                    System.out.println("Instantiation: " + ie);
                    return null;
                }
                catch(IllegalAccessException e)
                {
                    System.out.println("Access: " + e);
                    return null;
                }
            }
        }

        return null;
    }
    
    /**
     * Instantiate STOMP.  This is where everything begins.
     */
    public static void main(String args[])
    {
        Stomp stomp = new Stomp();
    }
}
