package stomp.data3d.file;

import stomp.data3d.*;
import java.io.*;

/**
 * For platforms that support it, this makes dialogs only display
 * files that have the .sto or .stomp filename extension.
 */
public class StompFileFilter implements FilenameFilter
{
    public boolean accept(File dir, String name)
    {
        return name.endsWith(".stomp") ||
            name.endsWith(".sto");
    }
}
