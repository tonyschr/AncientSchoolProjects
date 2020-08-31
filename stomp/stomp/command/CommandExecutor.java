package stomp.command;

import stomp.Mode;

import java.util.*;

/**
 * This class manages the command stack.  All commands should be
 * executed through the CommandExecutor so that they can be undone
 * and redone.
 */
public class CommandExecutor
{
    private static final boolean DEBUG = false;
    
    private static Stack m_commands =  new Stack();
    private static Stack m_undoCommands = new Stack();

    /**
     * Execute a command and add push it on the command stack.
     */
    public static final void execute(Command command)
    {
        m_undoCommands.removeAllElements();
        if(DEBUG)
            System.out.println("Executing Command: " + command);            
        boolean undoable = command.execute();

        if(undoable)
        {
            m_commands.push(command);

            if(m_commands.size() > Mode.STACK_SIZE)
            {
                m_commands.removeElementAt(0);
            }
        }
    }

    /**
     * Push a command on the command stack without executing it.
     */
    public static final void addNoExecute(Command command)
    {
        m_undoCommands.removeAllElements();
        if(DEBUG)
            System.out.println("Adding Command: " + command);
        m_commands.push(command); 
    }

    /**
     * Pop a command from the command stack, unexecute it, and
     * push the command on the undo stack.
     */
    public static final void undo()
    {
        if(m_commands.size() > 0)
        {
            Command command = (Command)m_commands.pop();
            command.unExecute();
            m_undoCommands.push(command);
        }
    }

    /**
     * Pop a command from the undo stack, execute it, and
     * push the command back onto the command stack.
     */
    public static final void redo()
    {
        if(m_undoCommands.size() > 0)
        {
            Command command = (Command)m_undoCommands.pop();
            if(DEBUG)
                System.out.println("Re-Doing Command: " + command);
            command.execute();
            m_commands.push(command);
        }
    }

    /**
     * Remove all of the commands from the command and undo stack.
     */
    public static final void clear()
    {
        m_undoCommands.removeAllElements();
        m_commands.removeAllElements();
    }

    /**
     * Returns whether the command stack has a command that can be undone
     */
    public static final boolean canUndo()
    {
        return m_commands.size() > 0;
    }

    /**
     * Returns whether a there are commands on the redo stack that
     * can be redone.
     */
    public static final boolean canRedo()
    {
        return m_undoCommands.size() > 0;
    }
}
