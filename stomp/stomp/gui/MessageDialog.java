package stomp.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

import stomp.gui.Appearance;

/**
 * MessageDialog class
 *
 * Use this class to display any generic messages to the user.
 * The message box will have Ok and Cancel buttons.  Lines will be
 * wrapped only if the string passed in has \n's in the string where
 * the line break should appear.
 *
 * <p>
 * In the future, this may be expanded to automatically wrap lines.
 * I didn't think it was particularly needed right now.
 */
public class MessageDialog extends Frame implements ActionListener
{
    //Result can be OK or CANCEL.  Indicates which button the user
    //clicked.
    public static int OK = 1;
    public static int CANCEL = 0;
    
    private String[] m_message;
    private int m_result = 5;

    ActionListener m_actionListener = null;
    
    /**
     * MessageDialoig default constructor.
     *
     * This should ordinarily never be used.  It's supplied since is is
     * a good idea for most classes to have a default constructor.
     */
    public MessageDialog()
    {
        super("Notice");
        m_message = separateLines("Unknown Error");
        layoutComponents();
    }

    /**
     * MessageDialog constructor
     *
     * @param message String to display in message box.
     */
    public MessageDialog(String message)
    {
        super("Notice");
        m_message = separateLines(message);
        layoutComponents();
    }

    public void addActionListener(ActionListener listener)
    {
        m_actionListener = listener;
    }

    /**
     * Place all of the controls in the window.
     */
    private void layoutComponents()
    {
        //set look
        setBackground(SystemColor.control);
        setFont(Appearance.getFont());

        //Set default layout to GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        //Set spacing and make components fill the entitire "grid".
        gbc.fill = GridBagConstraints.BOTH;
        //gbc.insets = new Insets(0,10,-6,10);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;

        //Write each of the lines into the dialog box, one below
        //the other.
        for(int i = 0; i < m_message.length; i++)
        {
            add(new Label(m_message[i]), gbc);
            gbc.gridy++;
        }

        add(new Label(""), gbc);
        gbc.gridy++;

        //Add Ok and Cancel buttons.
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(5,5,5,5);
            
        Button okButton = new Button("Ok");
        add(okButton, gbc);
        okButton.addActionListener(this);

        gbc.gridx++;

        Button cancelButton = new Button("Cancel");
        add(cancelButton, gbc);
        cancelButton.addActionListener(this);

        //Validate layout and pack to best size.
        validate();
        pack();
    }

    /**
     * Takes a string and separates into an array of strings
     * based on linefeeds.
     *
     * @param message String with any number of linefeeds.
     * @return string[] of the separated strings.
     */
    private String[] separateLines(String message)
    {
        StringTokenizer tok = new StringTokenizer(message, "\n");
        String[] lines = new String[tok.countTokens()];
        
        int numtokens = tok.countTokens();
        
        for(int i = 0; i < numtokens; i++)
        {
            lines[i] = new String(tok.nextToken());
        }

        return lines;
    }

    /**
     * Gets whether the user hit Ok or Cancel.
     *
     * @return ColorDialog.OK if the user hit Ok,
     *         ColorDialog.CANCEL is user hit Cancel.
     */
    public int getResult()
    {
        return m_result;
    }

    /**
     * This member is invoked when a button is pressed or data is
     * entered into the text fields.
     *
     * @param e ActionEvent supplied automatically when an event causes
     * this method to be called.
     */
    public void actionPerformed(ActionEvent e)
    {
        //Get the name of the item that caused the action.
        String arg = e.getActionCommand();
        
        //Check to see if user hit "Ok" or "Cancel" and do the
        //corresponding action.
        if("Ok".equals(arg))
        {
            m_result = MessageDialog.OK;

            if(m_actionListener != null)
            {
                m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                 "DialogCloseOk"));
            }
            
            dispose();
        }
        else if("Cancel".equals(arg))
        {
            m_result = MessageDialog.CANCEL;
            dispose();
        }
    }

    public static void main(String args[])
    {
        MessageDialog dg =
            new MessageDialog("Are you sure you want to exit?");

        //Show it.
        dg.setVisible(true);

        //Keep polling the dialog to see if "Ok" or "Cancel" has
        //been selected yet.
        //
        // IMPORTANT: Normally, modal dialogs will "block" and this
        // isn't necessary.  Since modal dialogs have some problems,
        // this is the easiest solution.  The sleep ensures that
        // it will not be a CPU-hogging busy loop.
        while(dg.getResult() != ColorDialog.OK &&
              dg.getResult() != ColorDialog.CANCEL)
        {
            try
            {
                Thread.sleep(200);
            }
            catch(InterruptedException e)
            {
                break;
            }
        }
        
        System.exit(0);
    }
}

/***********************************************************
 * $Log: MessageDialog.java,v $
 * Revision 1.4  1998/04/23 17:33:45  schreine
 * Numeric options dialog, made background of all dialogs correct
 *
 * Revision 1.3  1998/03/03 20:51:24  schreine
 * Added log string
 *
 **********************************************************/
