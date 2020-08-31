package stomp.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import stomp.data3d.*;
import stomp.Mode;
import stomp.transform.*;

/**
 */
public class ValuesDialog extends Frame implements ActionListener
{
    public static int OK = 1;
    public static int CANCEL = 2;
    private int m_result = 5;

    private String m_labels[];
    private TextField m_valueFields[];
    private String m_returnValues[];
    
    /**
     * ValuesDialog constructor.
     */
    public ValuesDialog(String name, String labels[], String defaults[])
    {
        super(name);

        m_labels = labels;

        //Set dialog window options
        setBackground(SystemColor.control);
        setFont(Appearance.getFont());
        setLayout(new GridLayout(m_labels.length+1, 2));

        //Setup TextFields for each value
        m_valueFields = new TextField[m_labels.length];
        m_returnValues = new String[m_labels.length];
        for(int i = 0; i < m_labels.length; i++)
        {
            m_valueFields[i] = new TextField(15);
            add(new Label(m_labels[i]));
            m_valueFields[i].setText("" + defaults[i]);
            add(m_valueFields[i]);
        }

        Button ok = new Button("Ok");
        ok.addActionListener(this);
        add(ok);

        Button cancel = new Button("Cancel");
        cancel.addActionListener(this);
        add(cancel);

        validate();
        pack();
    }

    /**
     * Handle clicking on "Ok" and "Cancel" buttons.
     */
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        
        if(cmd.equals("Ok"))
        {
            for(int i = 0; i < m_returnValues.length; i++)
            {
                m_returnValues[i] = m_valueFields[i].getText();
            }

            m_result = ValuesDialog.OK;
            
            dispose();
        }
        else if(cmd.equals("Cancel"))
        {
            m_result = ValuesDialog.CANCEL;
            
            dispose();
        }
    }

    public String[] getReturnValues()
    {
        return m_returnValues;
    }
    
    public int getResult()
    {
        return m_result;
    }
}


