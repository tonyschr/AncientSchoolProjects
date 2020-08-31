package stomp.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import stomp.gui.Appearance;
import stomp.data3d.*;
import stomp.gui.ColorDialog;

/**
 * SurfaceDialog class.  Control surface attributes such as:
 * <ul>
 *   <li> Color
 *   <li> Diffuse
 *   <li> Specularity
 *   <li> Reflection
 *   <li> Transparency
 * </ul>
 */
public class SurfaceDialog extends Frame implements ActionListener,
    AdjustmentListener, ItemListener
{
    //Result can be OK or CANCEL.  Indicates which button the user
    //clicked.
    public static int OK = 1;
    public static int CANCEL = 0;
    private int m_result = 5;

    private Surface m_surface;
    private SurfaceList m_surfaceList;

    //AWT Components
    private Scrollbar m_diffuse;
    private Scrollbar m_specular;
    private Scrollbar m_reflect;
    private Scrollbar m_transparent;

    private TextField m_diffuseText = new TextField(4);
    private TextField m_specularText = new TextField(4);
    private TextField m_reflectText = new TextField(4);
    private TextField m_transparentText = new TextField(4);
    private Checkbox m_smoothCheck = new Checkbox("Smooth shading");

    private TextField m_currentSurfaceName = new TextField(30);

    private List m_surfaceListBox = new List(3);
    
    private ColorBox m_colorBox = new ColorBox();
    private ColorDialog m_colorDialog;

    private ActionListener m_actionListener = null;

    /**
     * SurfaceDialog default constructor.  Creates a new surface.
     */
    public SurfaceDialog(SurfaceList surfaceList, Surface surf)
    {
        super("Surface Attributes");

        m_surfaceList = surfaceList;
        if(surf == null)
        {
            m_surface = surfaceList.getSurface("Default");
        }
        else
        {
            m_surface = surf;
        }

        layoutComponents();
    }

    /**
     * Add a class to listen to actions from this dialog.  This dialog
     * fires an ActionEvent with the message "DialogCloseOk" when
     * it is closed.
     */
    public void addActionListener(ActionListener listener)
    {
        m_actionListener = listener;
    }

    /**
     * Get the surface.  Should only be used when new surfaces are created.
     *
     * @return Surface that was created.
     */
    public Surface getSurface()
    {
        return m_surface;
    }

    /**
     * Lays out the components/widgets.
     */
    private void layoutComponents()
    {
        setBackground(SystemColor.control);
        setFont(Appearance.getFont());

        m_colorBox.setColor(m_surface.getColor());

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        
        Panel propertySliders = createSliders();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        gbc.gridwidth = 2;
        Panel surface = createSurfaceList();
        add(surface, gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        
        gbc.gridwidth = 1;
        Button color = new Button("Color");
        add(color, gbc);
        color.addActionListener(this);

        gbc.gridx++;
        add(m_colorBox, gbc);
        
        gbc.gridwidth = 2;        
        gbc.gridx = 0;
        gbc.gridy++;       

        //Add slider panel to main frame.
        add(propertySliders, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        add(m_smoothCheck, gbc);
        m_smoothCheck.addItemListener(this);
        
        //Add buttonss to main frame.
        gbc.gridy++;
        gbc.gridwidth = 1;
        
        Button okButton = new Button("Ok");
        add(okButton, gbc);
        okButton.addActionListener(this);

        gbc.gridx++;
        Button cancelButton = new Button("Cancel");
        add(cancelButton, gbc);
        cancelButton.addActionListener(this);

        //Validate the controls and pack to the minimum size.
        validate();
        pack();

        //Pack creates a window with sliders that are too small,
        //make the window a little wider, but not higher.  (The height
        //may change depending on the platform it is run on.)
        setSize(440, getSize().height);

    }

    private Panel createSurfaceList()
    {
        Panel surfList = new Panel();

        surfList.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0;
        gbc.gridy = 0;

        Vector surfaces = m_surfaceList.getSurfaceNames();
        int selectIndex = 0;
        for(int i = 0; i < surfaces.size(); i++)
        {
            if(m_surfaceList.getSurface((String)surfaces.elementAt(i)) ==
               m_surface)
            {
                selectIndex = i;
            }
            
            m_surfaceListBox.add((String)surfaces.elementAt(i));
        }
        m_surfaceListBox.select(selectIndex);

        gbc.gridwidth = 3;
        surfList.add(m_surfaceListBox, gbc);
        m_surfaceListBox.addItemListener(this);
        
        gbc.gridy++;
        gbc.gridwidth = 1;
        
        surfList.add(m_currentSurfaceName, gbc);

        gbc.gridx++;
        Button newSurface = new Button("New");
        newSurface.addActionListener(this);
        surfList.add(newSurface, gbc);

        gbc.gridx++;
        Button deleteSurf = new Button("Delete");
        deleteSurf.addActionListener(this);
        surfList.add(deleteSurf, gbc);

        return surfList;
    }
    
    /**
     * Lays out the sliders into a panel.
     */
    private Panel createSliders()
    {
        Panel sliders = new Panel();

        sliders.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0;
        gbc.gridy = 0;

        m_diffuse = new Scrollbar(Scrollbar.HORIZONTAL,
                                  (int)(m_surface.getDiffuse()*100),
                                  1, 0, 101);
        m_specular = new Scrollbar(Scrollbar.HORIZONTAL,
                                   (int)(m_surface.getSpecular()*100),
                                   1, 0, 101);
        m_reflect = new Scrollbar(Scrollbar.HORIZONTAL,
                                  (int)(m_surface.getReflect()*100),
                                  1, 0, 101);
        m_transparent = new Scrollbar(Scrollbar.HORIZONTAL,
                                      (int)(m_surface.getTransparent()*100),
                                      1, 0, 101);

        setValues();

        m_diffuse.addAdjustmentListener(this);
        m_specular.addAdjustmentListener(this);
        m_reflect.addAdjustmentListener(this);
        m_transparent.addAdjustmentListener(this);

        m_diffuseText.addActionListener(this);
        m_specularText.addActionListener(this);
        m_reflectText.addActionListener(this);
        m_transparentText.addActionListener(this);

        gbc.weightx = 1;
        sliders.add(new Label("Diffuse"), gbc);
        gbc.gridx++;
        gbc.weightx = 10;
        sliders.add(m_diffuse, gbc);
        gbc.gridx ++;
        gbc.weightx = 1;
        sliders.add(m_diffuseText, gbc);
        gbc.gridx++;
        sliders.add(new Label("%"), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 1;
        sliders.add(new Label("Specularity"), gbc);
        gbc.gridx++;
        gbc.weightx = 10;
        sliders.add(m_specular, gbc);
        gbc.gridx ++;
        gbc.weightx = 1;
        sliders.add(m_specularText, gbc);
        gbc.gridx++;
        sliders.add(new Label("%"), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 1;
        sliders.add(new Label("Reflectivity"), gbc);
        gbc.gridx++;
        gbc.weightx = 10;
        sliders.add(m_reflect, gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        sliders.add(m_reflectText, gbc);
        gbc.gridx++;
        sliders.add(new Label("%"), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 1;
        sliders.add(new Label("Transparency"), gbc);
        gbc.gridx++;
        gbc.weightx = 10;
        sliders.add(m_transparent, gbc);
        gbc.gridx ++;
        gbc.weightx = 1;
        sliders.add(m_transparentText, gbc);
        gbc.gridx++;
        sliders.add(new Label("%"), gbc);

        return sliders;
    }

    public void itemStateChanged(ItemEvent e)
    {
        if(e.getItemSelectable() == m_surfaceListBox)
        {
            m_surface =
                m_surfaceList.getSurface(m_surfaceListBox.getSelectedItem());
        }
        else
        {
            m_surface.setSmooth(m_smoothCheck.getState());
        }
        setValues();    
        //Set the sliders to reflect the numbers.
        m_diffuse.setValue((int)(m_surface.getDiffuse() * 100));
        m_specular.setValue((int)(m_surface.getSpecular() * 100));
        m_reflect.setValue((int)(m_surface.getReflect() * 100));
        m_transparent.setValue((int)(m_surface.getTransparent() * 100));
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

        try
        {
            //Grab the red, green, and blue values out of the
            //corresponding text fields.
            m_surface.setDiffuse(Integer.parseInt(m_diffuseText.getText()) /
                                 100.0);
            m_surface.setSpecular(Integer.parseInt(m_specularText.getText()) /
                                  100.0);
            m_surface.setReflect(Integer.parseInt(m_reflectText.getText()) /
                                 100.0);
            m_surface.setTransparent(Integer.parseInt(m_transparentText.getText()) /
                                     100.0);

            m_surface.setSmooth(m_smoothCheck.getState());
            
            //Check to make sure they are within the 0-1 range,
            //if not reset them.
            if(m_surface.getDiffuse() > 1 || m_surface.getDiffuse() < 0)
            {
                m_surface.setDiffuse(0);
            }
            if(m_surface.getSpecular() > 1 || m_surface.getSpecular() < 0)
            {
                m_surface.setSpecular(0);
            }
            if(m_surface.getReflect() > 1 || m_surface.getReflect() < 0)
            {
                m_surface.setReflect(0);
            }
            if(m_surface.getTransparent() > 1 || m_surface.getTransparent() < 0)
            {
                m_surface.setTransparent(0);
            }

            setValues();
        }
        catch(NumberFormatException exception)
        {
        }

        //Check to see if user hit "Ok" or "Cancel" and do the
        //corresponding action.
        if("Ok".equals(arg))
        {
            m_result = SurfaceDialog.OK;

            if(m_actionListener != null)
            {
                m_actionListener.actionPerformed(new ActionEvent(this, 0,
                                                                 "DialogCloseOk"));
            }

            dispose();
        }
        else if("Cancel".equals(arg))
        {
            m_result = SurfaceDialog.CANCEL;
            dispose();
        }
        else if("Color".equals(arg))
        {
            m_colorDialog = new ColorDialog(m_surface.getColor());
            m_colorDialog.addActionListener(this);
            m_colorDialog.setVisible(true);
        }
        else if("DialogCloseOk".equals(arg))
        {
            m_surface.setColor(m_colorDialog.getColor());
            m_colorBox.setColor(m_surface.getColor());
            m_colorBox.repaint();
        }
        else if("New".equals(arg))
        {
            String currentName = m_currentSurfaceName.getText();
            currentName = currentName.replace(' ', '_');
            if(currentName.length() > 0)
            {
                if(m_surfaceList.getSurface(currentName) == null)
                {
                    m_surface = new Surface();
                    m_surfaceList.addSurface(currentName,
                                             m_surface);
                    m_surfaceListBox.add(currentName);
                }
            }
        }
        else if("Delete".equals(arg))
        {
            String selected = m_surfaceListBox.getSelectedItem();
            if(!selected.equals("Default"))
            {
                m_surfaceList.deleteSurface(selected);
                m_surfaceListBox.remove(selected);
            }
        }
    }

    /**
     * Based on the internal representation of the surface, updates the sliders
     * and text fields with the newest values.
     */
    private void setValues()
    {
        m_diffuseText.setText("" + (int)(m_surface.getDiffuse() * 100));
        m_specularText.setText("" + (int)(m_surface.getSpecular() * 100));
        m_reflectText.setText("" + (int)(m_surface.getReflect() * 100));
        m_transparentText.setText("" + (int)(m_surface.getTransparent() * 100));
        m_smoothCheck.setState(m_surface.isSmooth());

        //Set the sliders to reflect the numbers.
//         m_diffuse.setValue((int)(m_surface.getDiffuse() * 100));
//         m_specular.setValue((int)(m_surface.getSpecular() * 100));
//         m_reflect.setValue((int)(m_surface.getReflect() * 100));
//         m_transparent.setValue((int)(m_surface.getTransparent() * 100));

        
        m_colorBox.setColor(m_surface.getColor());
        m_colorBox.repaint();
    }
    
    /**
     * This member is invoked when any of the scrollbars are modified.
     *
     * @param e AdjustmentEvent object is created when this method is
     * automatically called.
     */
    public void adjustmentValueChanged(AdjustmentEvent e)
    {
        if(e.getAdjustable() == m_diffuse)
        {
            m_surface.setDiffuse(m_diffuse.getValue() / 100.0);
        }
        else if(e.getAdjustable() == m_specular)
        {
            m_surface.setSpecular(m_specular.getValue() / 100.0);
        }
        else if(e.getAdjustable() == m_reflect)
        {
            m_surface.setReflect(m_reflect.getValue() / 100.0);
        }
        else if(e.getAdjustable() == m_transparent)
        {
            m_surface.setTransparent(m_transparent.getValue() / 100.0);
        }

        setValues();
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
     * Simple unit-test for checking to see how the dialog works.
     * Also gives a simple usage example.
     */
    public static void main(String args[])
    {
        //Create the dialog.
        SurfaceDialog dg = new SurfaceDialog(new SurfaceList(), new Surface());

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
 * $Log: SurfaceDialog.java,v $
 * Revision 1.6  1998/05/04 09:12:31  schreine
 * Commenting, minor changes.
 *
 * Revision 1.5  1998/04/23 17:33:47  schreine
 * Numeric options dialog, made background of all dialogs correct
 *
 * Revision 1.4  1998/03/03 20:51:24  schreine
 * Added log string
 *
 **********************************************************/
