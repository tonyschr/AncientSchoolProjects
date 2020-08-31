package stomp.gui;

import stomp.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

import stomp.gui.Appearance;

/**
 * Simply brings up a dialog giving author and copyright information
 * for Stomp3D.  Also shows build number and version.
 */
public class AboutDialog extends Frame implements ActionListener
{
    /**
     */
    public AboutDialog()
    {
        super("About Stomp3D");
        layoutComponents();
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
        gbc.insets = new Insets(0,10,-5,10);

        gbc.gridx = 0;
        gbc.gridy = 0;

        add(new Label("Stomp3D"), gbc);
        gbc.gridy++;
        add(new Label("Version " + Stomp.VERSION
                      + ", build " + Stomp.BUILD), gbc);
        gbc.gridy++;
        add(new Label(""), gbc);
        gbc.gridy++;
        add(new Label("by Tony Schreiner and John Speck"), gbc);
        gbc.gridy++;
        add(new Label("Copyright 1998 Tony Schreiner, John Speck."), gbc);
        gbc.gridy++;
        add(new Label(""), gbc);
        gbc.gridy++;
        add(new Label("Stomp is copyrighted freeware.  You may distribute"),
            gbc);
        gbc.gridy++;
        add(new Label("it to your friends, but you may not modify or sell"),
            gbc);
        gbc.gridy++;
        add(new Label("it or claim it as your own work."), gbc);
        gbc.gridy++;
        add(new Label(""), gbc);
        gbc.gridy++;
        add(new Label("Feel free to e-mail schreine@cs.colostate.edu to"), gbc);
        gbc.gridy++;
        add(new Label("comment on Stomp3D or report bugs."), gbc);

        gbc.gridy++;
        add(new Label(""), gbc);

        gbc.gridy++;
        add(new Label("http://holly.colostate.edu/~tschrein/stomp/Stomp.html"), gbc);
        gbc.gridy++;
        add(new Label("(Web site will move in 1999)"), gbc);
            
        gbc.gridy++;
        gbc.gridy++;
            
        //Add Ok and Cancel buttons.
        gbc.insets = new Insets(5,5,5,5);
            
        Button okButton = new Button("Great!");
        add(okButton, gbc);
        okButton.addActionListener(this);

        //Validate layout and pack to best size.
        validate();
        pack();
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
        dispose();
    }

}
