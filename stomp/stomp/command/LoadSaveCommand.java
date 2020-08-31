package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import java.awt.*;

/**
 * Command for loading and saving files.  The actual filter used
 * (SceneReaderWriter interface) is passed into the constructor.
 * This class handles opening the file dialog boxes.
 */
public class LoadSaveCommand implements Command
{
    public static final int READ = 1;
    public static final int WRITE = 2;

    private static String m_directory;
    
    private Stomp m_stomp;
    private Scene m_scene;
    private SceneReaderWriter m_readerWriter;
    private int m_mode;

    private LoadSaveCommand()
    {
    }
    
    public LoadSaveCommand(Stomp stomp, Scene scene, SceneReaderWriter rw,
                           int mode)
    {
        m_stomp = stomp;
        m_scene = scene;
        m_readerWriter = rw;
        m_mode = mode;
    }
    
    public boolean execute()
    {
        if(m_mode == WRITE)
        {
            FileDialog fileDialog = new FileDialog(m_stomp,
                                                   "Save File",
                                                   FileDialog.SAVE);
            if(m_directory != null)
            {
                fileDialog.setDirectory(m_directory);
            }
            
            fileDialog.setVisible(true);
            
            if(fileDialog.getFile() != null)
            {
                m_readerWriter.write(m_scene, "" + fileDialog.getDirectory() +
                                     fileDialog.getFile());
                m_directory = fileDialog.getDirectory();
            }
        }
        else if(m_mode == READ)
        {
            FileDialog fileDialog = new FileDialog(m_stomp,
                                                   "Load File",
                                                   FileDialog.LOAD);
            fileDialog.setFilenameFilter(new StompFileFilter());
            if(m_directory != null)
            {
                fileDialog.setDirectory(m_directory);
            }
            
            fileDialog.setVisible(true);
            
            if(fileDialog.getFile() != null)
            {
                m_readerWriter.read(m_scene, "" + fileDialog.getDirectory() +
                                    fileDialog.getFile());
                m_directory = fileDialog.getDirectory();
            }
            
            m_scene.validateScene();
        }
        
        return false;
    }

    /**
     * Cannot unexecute for now.
     */
    public void unExecute()
    {
    }

    public String toString()
    {
        return "Load/Save Command";
    }
}
