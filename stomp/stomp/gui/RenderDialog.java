package stomp.gui;

import stomp.*;
import stomp.data3d.*;
import stomp.data3d.file.*;
import stomp.command.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

import stomp.gui.Appearance;

/**
 * This dialog is used for controlling the export to POV.  Users
 * can choose the location of the POV executable, output file, and
 * change POV parameters.
 *
 * When the user chooses Render, this dialog exports to POV and
 * automatically invokes the POV raytracer on the scene.
 */
public class RenderDialog extends Frame implements ActionListener
{
    public static String m_povLocationString = "";
    public static String FOV = "30";
    private static String m_outputFileString = "";
    private static String m_widthString = "320";
    private static String m_heightString = "240";
    private static String m_extraOptionsString = "+D";
    
    private Scene m_scene;
    private TextField m_povLocation = new TextField(30);
    private TextField m_outputFile = new TextField(30);
    private TextField m_width = new TextField(10);
    private TextField m_height = new TextField(10);
    private TextField m_extraOptions = new TextField(70);
    private TextField m_fov = new TextField(10);
    
    /**
     *
     */
    public RenderDialog(Scene scene)
    {
        super("POV Render");
        m_scene = scene;
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
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;

        add(new Label("POV Executable"), gbc);
        gbc.gridx++;
        add(m_povLocation, gbc);
        gbc.gridx++;
        Button changeLocation = new Button("Change Location");
        changeLocation.addActionListener(this);
        add(changeLocation, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(new Label("Ouput Filename"), gbc);
        gbc.gridx++;
        add(m_outputFile, gbc);
        gbc.gridx++;
        Button changeFile = new Button("Change File");
        changeFile.addActionListener(this);
        add(changeFile, gbc);        
        
        gbc.gridx = 0;
        gbc.gridy++;
        add(new Label("Image Width"), gbc);
        gbc.gridx++;
        gbc.gridwidth = 2;
        add(m_width, gbc);

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy++;
        add(new Label("Image Height"), gbc);
        gbc.gridx++;
        gbc.gridwidth = 2;
        add(m_height, gbc);

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy++;
        add(new Label("Field of View"), gbc);
        gbc.gridx++;
        gbc.gridwidth = 2;
        add(m_fov, gbc);
        
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy++;
        add(new Label("POV Options"), gbc);
        gbc.gridx++;
        gbc.gridwidth = 2;
        add(m_extraOptions, gbc);
        
        //Add Ok and Cancel buttons.
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        Button okButton = new Button("Render");
        add(okButton, gbc);
        okButton.addActionListener(this);

        gbc.gridx+=2;
        gbc.gridwidth = 1;
        Button cancelButton = new Button("Cancel");
        add(cancelButton, gbc);
        cancelButton.addActionListener(this);

        //Set the default textfield values
        m_povLocation.setText(m_povLocationString);
        m_outputFile.setText(m_outputFileString);
        m_width.setText(m_widthString);
        m_height.setText(m_heightString);
        m_extraOptions.setText(m_extraOptionsString);
        m_fov.setText(FOV);
        
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
        String button = e.getActionCommand();
        
        if(button.equals("Render"))
        {
            renderFile();
            dispose();
        }
        else if(button.equals("Change Location"))
        {
            System.out.println("Change Location");
            FileDialog fileDialog = new FileDialog(this,
                                                   "Location of POV Executable",
                                                   FileDialog.LOAD);
            fileDialog.setVisible(true);
            
            if(fileDialog.getFile() != null)
            {
                m_povLocation.setText("" + fileDialog.getDirectory() +
                                      fileDialog.getFile());
            }
        }
        else if(button.equals("Change File"))
        {
            System.out.println("Change File");
            FileDialog fileDialog = new FileDialog(this,
                                                   "Output Picture File",
                                                   FileDialog.SAVE);
            fileDialog.setVisible(true);
            
            if(fileDialog.getFile() != null)
            {
                m_outputFile.setText("" + fileDialog.getDirectory() +
                                     fileDialog.getFile());
            }
        }
        else
        {
            dispose();
        }
    }

    public void renderFile()
    {
        m_povLocationString = m_povLocation.getText();
        m_outputFileString = m_outputFile.getText();
        m_widthString = m_width.getText();
        m_heightString = m_height.getText();
        m_extraOptionsString = m_extraOptions.getText();
        FOV = m_fov.getText();
        
        if(m_outputFileString.length() <= 4)
        {
            MessageDialog dialog = new MessageDialog("Invalid output file.");
            dialog.setVisible(true);
            return;
        }

        if(m_povLocationString.length() == 0)
        {
            MessageDialog dialog = new MessageDialog("You must specify the location of the POV executable.");
            dialog.setVisible(true);
            return;
        }
        
        File outFile = new File(m_outputFileString);
        String pureFile = outFile.getName();
        String filePath = m_outputFileString.substring(0, m_outputFileString.length() - pureFile.length());

        String libDir = "";
        if(filePath.length() > 0)
        {
            libDir = "+L" + filePath;
        }
        
        String outputPov = m_outputFileString.substring(0, m_outputFileString.length() - 4) + ".pov";

        POVReaderWriter readerWriter = new POVReaderWriter();
        readerWriter.write(m_scene, outputPov);

        //Execute povray
        try
        {
            Runtime runtime = Runtime.getRuntime();
            String command = "" + m_povLocationString + " +I" + outputPov +
                " +W" + m_widthString + " +H" + m_heightString +
                " +O" + m_outputFileString + " " + libDir + " " +
                m_extraOptionsString;
            System.out.println("Command: " + command);
            Process povRender = runtime.exec(command);
        }
        catch(java.io.IOException e)
        {
            System.out.println("Could not render file!");
        }
    }
}
