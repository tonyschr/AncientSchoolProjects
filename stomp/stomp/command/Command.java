package stomp.command;

/**
 * Command interface.
 *
 * This is the base class for all system commands.  This loosely
 * follows the Command pattern, allowing for undo/redo capability
 * and also distributing functionality to reduce the size of the
 * data3d.Scene class.
 */
public interface Command
{
    /**
     * Execute the command.
     *
     * @param param Object should be either a Scene, Stomp, or ViewContainer
     * object.  Command does not dictate what kinds of objects the
     * concrete command will execute upon, but an uncaught exception
     * will be thrown if the wrong parameter type is supplied.
     *
     * <p>execute returns true if the command actually did something,
     * false if no-op.
     */
    public boolean execute();
    public void unExecute();
    public String toString();
}
