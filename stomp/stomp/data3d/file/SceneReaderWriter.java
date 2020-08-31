package stomp.data3d.file;

import stomp.data3d.*;

/**
 * This is the interface for all file input/output capability
 * for stomp.
 */
public abstract class SceneReaderWriter
{
    abstract public void read(Scene scene, String filename);
    abstract public void write(Scene scene, String filename);
}



