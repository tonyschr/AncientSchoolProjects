package stomp;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import stomp.command.*;
import stomp.gui.*;
import stomp.view.*;
import stomp.data3d.*;
import stomp.data3d.file.*;

import javax.vecmath.Vector3d;

/**
 * GUIListener Class.  Handles all of the basic events including
 * button clicks and menus.
 *
 * Does not handle selection of the lock buttons, that is done
 * in stomp.gui.ButtonBar.
 */
public class GUIListener implements ActionListener, WindowListener
{
    Stomp m_stomp;
    ViewContainer m_viewContainer;
    Scene m_scene;
    Frame m_dialog = null;

    /**
     * Constructor.  Does nothing.
     */
    public GUIListener()
    {
    }

    /**
     * Set the scene to activate commands on.
     */
    public void setScene(Scene scene)
    {
        m_scene = scene;
    }
    
    /**
     * Set the frame for the main application.  Some of the dialogs
     * want this information (although it isn't required).
     */
    public void setStomp(Stomp stomp)
    {
        m_stomp = stomp;
    }

    /**
     * Let this class know about the ViewContainer being used to
     * control the views.
     */
    public void setViewContainer(ViewContainer vc)
    {
        m_viewContainer = vc;
    }

    /**
     * Called when an event is fired.
     *
     * This method handles almost all of the menus and button
     * clicks in STOMP.  This is sort-of crude, but it's simple and
     * gets the job done.
     */
    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();
        if(command == null)
        {
            return;
        }
        
        /*************************************************************
         * MENUS
         ************************************************************/
        if(command.equals("New"))
        {
            CommandExecutor.execute(new NewCommand(m_stomp, m_scene));
            m_viewContainer.resetViews();
        }
        else if(command.equals("Load"))
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new StompReaderWriter(),
                                                        LoadSaveCommand.READ));
        }
        else if(command.equals("Save"))
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new StompReaderWriter(),
                                                        LoadSaveCommand.WRITE));
        }
        else if(command.equals("CS410 Lab"))
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new CS410ReaderWriter(),
                                                        LoadSaveCommand.WRITE));
        }
        else if(command.equals("CS510 GUI"))
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new CS510ReaderWriter(),
                                                        LoadSaveCommand.WRITE));
        }
        else if(command.equals("Wavefront OBJ"))
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new WavefrontReaderWriter(),
                                                        LoadSaveCommand.WRITE));
        }
        else if(command.equals("CS510 GUI ")) //import
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new CS510ReaderWriter(),
                                                        LoadSaveCommand.READ));
        }
        else if(command.equals("CS410 Lab ")) //import
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new CS410ReaderWriter(),
                                                        LoadSaveCommand.READ));
        }
        else if(command.equals("VRML"))
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new VRMLReaderWriter(),
                                                        LoadSaveCommand.WRITE));
        }
        else if(command.equals("POV"))
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new POVReaderWriter(),
                                                        LoadSaveCommand.WRITE));
        }
        else if(command.equals("DirectX"))
        {
            CommandExecutor.execute(new LoadSaveCommand(m_stomp, m_scene,
                                                        new DXReaderWriter(),
                                                        LoadSaveCommand.WRITE));
        }
        else if(command.equals("Quit"))
        {
            stompQuit();
        }
        else if(command.equals("Undo"))
        {
            CommandExecutor.undo();
        }
        else if(command.equals("Redo"))
        {
            CommandExecutor.redo();
        }
        else if(command.equals("Cut"))
        {
            CommandExecutor.execute(new CutCommand(m_scene));
        }
        else if(command.equals("Copy"))
        {
            CommandExecutor.execute(new CopyCommand(m_scene));
        }
        else if(command.equals("Paste"))
        {
            CommandExecutor.execute(new PasteCommand(m_scene));
        }
        else if(command.equals("Options"))
        {
            Options options = new Options(m_scene);
            options.setVisible(true);
        }
        else if(command.equals("Show Statistics"))
        {
            m_scene.showStatistics();
        }
        else if(command.equals("VRML Preview"))
        {
            CommandExecutor.execute(new PreviewCommand(m_stomp, m_scene));
        }
        else if(command.equals("POV Render"))
        {
            RenderDialog dialog = new RenderDialog(m_scene);
            dialog.setVisible(true);
        }
//          else if(command.equals("Animation Panel"))
//          {
//              m_stomp.showAnimPanel();
//          }
        else if(command.equals("Default Views") ||
                command.equals("Def"))
        {
            //No need for a bloated command just to do this.
            Mode.setZoom(40);
            Mode.setPan(new Vector3d(0,0,0));
            m_viewContainer.resetViews();
            m_scene.validateScene();
        }
        else if(command.equals("Perspective"))
        {
            m_viewContainer.setPerspective(ViewContainer.PERSPECTIVE);
        }
        else if(command.equals("Camera"))
        {
            FastVector cameras = m_scene.getCameras();
            if(cameras.sizeFast() > 0)
            {
                m_viewContainer.setCamera((CameraView)cameras.elementAt(0));
            }
            m_viewContainer.setPerspective(ViewContainer.CAMERA);
        }
        else if(command.equals("Print Views"))
        {
            PrintJob pj =
                m_viewContainer.getToolkit().getPrintJob(m_stomp, "Print Stomp Views", null);
            if (pj != null)
            {
                Graphics pg = pj.getGraphics();
                m_viewContainer.printAll(pg);
                pg.dispose();
                pj.end();
            }
        }
        else if(command.equals("Print All"))
        {
            PrintJob pj =
                m_stomp.getToolkit().getPrintJob(null, "Print Screen", null);
            if (pj != null)
            {
                Graphics pg = pj.getGraphics();
                m_stomp.printAll(pg);
                pg.dispose();
                pj.end();
            }
        }
        else if(command.equals("About"))
        {
            AboutDialog dialog = new AboutDialog();
            dialog.setVisible(true);
        }

        /*************************************************************
         * BUTTONS
         ************************************************************/

        //Create ****************************
        else if(command.equals("Add Primitive"))
        {
            m_dialog = new PrimitiveDialog();
            ((PrimitiveDialog)m_dialog).addActionListener(this);
            m_dialog.setVisible(true);
        }
        else if(command.equals("Add Camera"))
        {
            CommandExecutor.execute(new AddCameraCommand(m_scene));
        }
        else if(command.equals("Add Light"))
        {
            CommandExecutor.execute(new AddLightCommand(m_scene,
                                        AddLightCommand.POINT_LIGHT));
        }
        else if(command.equals("Create Polygon"))
        {
            CommandExecutor.execute(new MakePlaneCommand(m_scene));
        }
        else if(command.equals("Create Spline"))
        {
            CommandExecutor.execute(new MakeSplineCommand(m_scene));
        }
        else if(command.equals("NURB Surface"))
        {
            CommandExecutor.execute(new MakeSplineSurfaceCommand(m_scene));
        }
        else if(command.equals("Surface"))
        {
            m_dialog = new SurfaceDialog(m_scene.getSurfaceList(),
                                         m_scene.getSelectedSurface());
            
            ((SurfaceDialog)m_dialog).addActionListener(this);
            m_dialog.setVisible(true);
        }

        //Select ****************************
        else if(command.equals("Deselect All"))
        {
            CommandExecutor.execute(new DeselectAllCommand(m_scene));
        }
        else if(command.equals("Deselect Last"))
        {
            CommandExecutor.execute(new DeselectLastCommand(m_scene));
        }
        else if(command.equals("Select Connected"))
        {
            CommandExecutor.execute(new SelectConnectedCommand(m_scene));
        }
        else if(command.equals("Invert Selection"))
        {
            CommandExecutor.execute(new InvertSelectedCommand(m_scene));
        }
        else if(command.equals("Group Selected"))
        {
            CommandExecutor.execute(new GroupSelectedCommand(m_scene));
        }
        else if(command.equals("Ungroup Selected"))
        {
            CommandExecutor.execute(new UngroupSelectedCommand(m_scene));
        }
        else if(command.equals("Hide Selected"))
        {
            CommandExecutor.execute(new HideSelectedCommand(m_scene));
        }
        else if(command.equals("Unhide All"))
        {
            CommandExecutor.execute(new UnhideAllCommand(m_scene));
        }
        
        //Transform *************************
        else if(command.equals("Extrude"))
        {
            CommandExecutor.execute(new ExtrudeCommand(m_scene));
        }
        else if(command.equals("Spline Extrude"))
        {
            CommandExecutor.execute(new SplineExtrudeCommand(m_scene));
        }
        else if(command.equals("Bevel"))
        {
            CommandExecutor.execute(new BevelCommand(m_scene));
        }
        else if(command.equals("Lathe"))
        {
            CommandExecutor.execute(new LatheYCommand(m_scene));
        }            
        else if(command.equals("Flip"))
        {
            CommandExecutor.execute(new FlipNormalCommand(m_scene));
        }
        else if(command.equals("Triangulate"))
        {
            CommandExecutor.execute(new TriangulateCommand(m_scene));
        }
        else if(command.equals("Subdivide"))
        {
            CommandExecutor.execute(new SubdivideCommand(m_scene,
                                                         SubdivideCommand.NOCENTER));
        }
        else if(command.equals("Subdivide2"))
        {
            CommandExecutor.execute(new SubdivideCommand(m_scene,
                                                         SubdivideCommand.CENTER));
        }
        else if(command.equals("Smooth Triangles"))
        {
            CommandExecutor.execute(new SmoothCommand(m_scene));
        }
        else if(command.equals("Delete Selected"))
        {
            CommandExecutor.execute(new DeleteSelectedCommand(m_scene));
        }
        else if(command.equals("Merge Vertices"))
        {
            CommandExecutor.execute(new MergeVerticesCommand(m_scene));
        }
        else if(command.equals("Join Vertices"))
        {
            CommandExecutor.execute(new JoinVerticesCommand(m_scene));
        }
        
        //Static ****************************
        else if(command.equals("Numeric Options"))
        {
            Numeric dialog = new Numeric(m_scene);
            dialog.setVisible(true);
        }
        
        //Views **************
        else if(command.equals("Quad"))
        {
            m_viewContainer.setView(ViewContainer.ALL);
        }
        else if(command.equals("Top"))
        {
            m_viewContainer.setView(ViewContainer.TOP);
        }
        else if(command.equals("Front"))
        {
            m_viewContainer.setView(ViewContainer.FRONT);
        }
        else if(command.equals("Side"))
        {
            m_viewContainer.setView(ViewContainer.SIDE);
        }
        else if(command.equals("Pers"))
        {
            m_viewContainer.setView(ViewContainer.PERSPECTIVE);
        }
        else if(command.equals("Plugins Location"))
        {
            FileDialog fileDialog = new FileDialog(m_stomp,
                                                   "Plugin Directory",
                                                   FileDialog.LOAD);
            fileDialog.setVisible(true);
            
            if(fileDialog.getDirectory() != null)
            {
                m_stomp.addPlugins(fileDialog.getDirectory());
            }
        }
        
        //Dialogs ***********************************************        
        else if(command.equals("DialogCloseOk"))
        {
            if(m_dialog instanceof PrimitiveDialog)
            {
                int shapeType = 0;
                
                int select = ((PrimitiveDialog)m_dialog).getResult();
                switch(select)
                {
                case PrimitiveDialog.PLANE:
                    shapeType = MakeShapeCommand.PLANE;
                    break;
                case PrimitiveDialog.CUBE:
                    shapeType = MakeShapeCommand.CUBE;
                    break;
                case PrimitiveDialog.SPHERE:
                    shapeType = MakeShapeCommand.SPHERE;
                    break;
                case PrimitiveDialog.TESS_SPHERE:
                    shapeType = MakeShapeCommand.TESS_SPHERE;
                    break;
                case PrimitiveDialog.CONE:
                    shapeType = MakeShapeCommand.CONE;
                    break;
                case PrimitiveDialog.CYLINDER:
                    shapeType = MakeShapeCommand.CYLINDER;
                    break;
                case PrimitiveDialog.SURFACE:
                    shapeType = MakeShapeCommand.SURFACE;
                    break;
                case PrimitiveDialog.CLOSED_SURFACE:
                    shapeType = MakeShapeCommand.CLOSED_SURFACE;
                    break;
                }

                CommandExecutor.execute(new MakeShapeCommand(m_scene,
                                                             shapeType));
            }
            else if(m_dialog instanceof SurfaceDialog)
            {
                m_scene.setSelectedSurface(((SurfaceDialog)m_dialog).getSurface());
            }
        }
        else
        {
            System.out.println("Executing plugin");
            Command comm = m_stomp.getPluginCommand(command);
            if(comm != null)
            {
                System.out.println("Calling execute");
                CommandExecutor.execute(comm);
                m_scene.validateScene();
            }
        }
    }

    /**
     * This event happens when the window is selected
     */
    public void windowActivated(WindowEvent e)
    {
    }

    /**
     * This event happens when the window is closed
     */
    public void windowClosed(WindowEvent e)
    {
    }

    /**
     * This event happens when the user requests the window
     * to be closed.
     */
    public void windowClosing(WindowEvent e)
    {
        stompQuit();
    }

    /**
     * This event happens when the window looses focus
     */
    public void windowDeactivated(WindowEvent e)
    {
    }

    /**
     * This event happens when the window is maximized
     */
    public void windowDeiconified(WindowEvent e)
    {
    }

    /**
     * This event happens when the window is minimized/iconified.
     */
    public void windowIconified(WindowEvent e)
    {
    }

    /**
     * This event happens when the window is opened.
     */
    public void windowOpened(WindowEvent e)
    {
    }

    /**
     * The single exit point for STOMP.  All cleanup code should
     * be here.  Never exit except through this function.
     */
    private void stompQuit()
    {
        m_stomp.dispose();
        System.exit(0);
    }
}







