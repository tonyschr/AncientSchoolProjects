package stomp.command;

import stomp.*;
import stomp.data3d.*;
import stomp.gui.*;

import java.awt.*;
import java.awt.event.*;
//import java.util.*;

/**
 * Group the selected primitives.  Since groups are primitives themselves,
 * groups can contain other groups.  This command pops up a dialog box
 * to allow the user to control the name of the group and color it will
 * be displayed in.
 */
public class GroupSelectedCommand implements Command, ActionListener,
    ItemListener
{ 
    private Scene m_scene;
    private Group m_group;
    private Color m_color = new Color(0, 200, 0);
    private ColorBox m_colorBox = new ColorBox();
    private ColorDialog m_colorDialog;

    private TextField m_groupNameField = new TextField(15);
    private List m_groupNameList = new List(5);
    private Frame m_dialog;
    private Command m_deselectAll;
    private FastVector m_selectedPrims;
    
    private GroupSelectedCommand()
    {
    }
    
    public GroupSelectedCommand(Scene scene)
    {
        m_scene = scene;
    }
    
    public boolean execute()
    {
        m_deselectAll = new DeselectAllCommand(m_scene);
        m_selectedPrims = new FastVector();
        
        m_dialog = new Frame("Name Group");

        m_dialog.setBackground(SystemColor.control);
        m_dialog.setFont(Appearance.getFont());

        m_dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        gbc.gridwidth = 2;
        Panel groups = createGroupList();
        m_dialog.add(groups, gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        
        gbc.gridwidth = 1;
        Button color = new Button("Color");
        m_dialog.add(color, gbc);
        color.addActionListener(this);

        gbc.gridx++;
        m_dialog.add(m_colorBox, gbc);
        
        //Add buttonss to main frame.
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        
        Button okButton = new Button("Ok");
        m_dialog.add(okButton, gbc);
        okButton.addActionListener(this);

        gbc.gridx++;
        Button cancelButton = new Button("Cancel");
        m_dialog.add(cancelButton, gbc);
        cancelButton.addActionListener(this);

        //Validate the controls and pack to the minimum size.
        m_dialog.validate();
        m_dialog.pack();

        m_dialog.setVisible(true);

        return true;
    }

    private Panel createGroupList()
    {
        Panel groupList = new Panel();

        groupList.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0;
        gbc.gridy = 0;

        fillGroupList();

        gbc.gridwidth = 3;
        groupList.add(m_groupNameList, gbc);
        m_groupNameList.addItemListener(this);
        
        gbc.gridy++;
        gbc.gridwidth = 1;
        
        groupList.add(m_groupNameField, gbc);

        gbc.gridx++;
        Button newGroup = new Button("New");
        newGroup.addActionListener(this);
        groupList.add(newGroup, gbc);

        gbc.gridx++;
        Button deleteGroup = new Button("Delete");
        deleteGroup.addActionListener(this);
        groupList.add(deleteGroup, gbc);

        return groupList;
    }

    private void fillGroupList()
    {
        m_groupNameList.removeAll();
        FastVector primitives = m_scene.getPrimitivesVector();
        int selectIndex = 0;
        for(int i = 0; i < primitives.size(); i++)
        {
            Primitive prim = (Primitive)primitives.elementAtFast(i);
            if(prim instanceof Group)
            {
                m_groupNameList.add(((Group)prim).getName());
            }
        }

    }
    
    public void unExecute()
    {
        m_deselectAll.unExecute();

        for(int i = 0; i < m_selectedPrims.sizeFast(); i++)
        {
            Primitive p = (Primitive)m_selectedPrims.elementAtFast(i);
            p.setGroup(null);
        }

        m_scene.removePrimitive(m_group);
    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        String groupName = m_groupNameField.getText();
        boolean newGroup = true;
        
        if(cmd.equals("Ok"))
        {
            if(groupName.length() > 0)
            {
                groupName = groupName.replace(' ', '_');
                m_group = new Group(groupName, m_color);
                
                FastVector primitives = m_scene.getPrimitivesVector();
                for(int i = 0; i < primitives.size(); i++)
                {
                    Primitive prim = (Primitive)primitives.elementAtFast(i);
                    if(prim instanceof Group)
                    {
                        if( ((Group)prim).getName().equals(groupName))
                        {
                            newGroup = false;
                            m_group = (Group)prim;
                        }
                    }
                }
                
                for(int i = 0; i < primitives.size(); i++)
                {
                    Primitive prim = (Primitive)primitives.elementAtFast(i);
                    if(prim.isSelected())
                    {
                        m_selectedPrims.addElement(prim);
                        m_group.add(prim);
                        //                    prim.setGroup(group);
                    }
                }
                
                if(newGroup)
                {
                    m_scene.addPrimitive(m_group);
                }
            
                m_deselectAll.execute();
            }

            m_scene.validateScene();
            m_dialog.dispose();
        }
        else if(cmd.equals("Color"))
        {
            m_colorDialog = new ColorDialog(m_color);
            m_colorDialog.addActionListener(this);
            m_colorDialog.setVisible(true);
        }
        else if(cmd.equals("New"))
        {
            String currentName = m_groupNameField.getText();
            currentName = currentName.replace(' ', '_');
            if(currentName.length() > 0)
            {
                boolean found = false;
                String names[] = m_groupNameList.getItems();
                for(int n = 0; n < names.length; n++)
                {
                    if(names[n].equals(currentName))
                    {
                        found = true;
                        break;
                    }
                }
                if(!found)
                {
                    m_groupNameList.add(currentName);
                    m_colorBox.setColor(m_color);
                    m_colorBox.repaint();
                }
            }
        }
        else if(cmd.equals("Delete"))
        {
            FastVector primitives = m_scene.getPrimitivesVector();
            for(int i = 0; i < primitives.size(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAtFast(i);
                if(prim instanceof Group)
                {
                    if( ((Group)prim).getName().equals(groupName))
                    {
                        Group groupToDelete = (Group)prim;
                        m_scene.removePrimitive(prim);
                        fillGroupList();
                        m_groupNameField.setText("");
                        for(int j = 0; j < primitives.sizeFast(); j++)
                        {
                            Primitive prim2 = (Primitive)primitives.elementAtFast(j);
                            if(prim2.getGroup() == groupToDelete)
                            {
                                prim2.setGroup(null);
                            }
                        }
                        break;
                    }
                }
            }            
        }
        else if(cmd.equals("DialogCloseOk"))
        {
            m_color = m_colorDialog.getColor();
            m_colorBox.setColor(m_color);
            m_colorBox.repaint();
        }
        else if(cmd.equals("Cancel"))
        {
            m_scene.validateScene();
            m_dialog.dispose();
        }
    }

    public void itemStateChanged(ItemEvent e)
    {
        m_groupNameField.setText(m_groupNameList.getSelectedItem());

        FastVector primitives = m_scene.getPrimitivesVector();
        for(int i = 0; i < primitives.sizeFast(); i++)
        {
            if(primitives.elementAtFast(i) instanceof Group)
            {
                Group g = (Group)primitives.elementAtFast(i);
                if(g.getName().equals(m_groupNameField.getText()));
                {
                    m_color = g.getColor();
                }
            }
        }
        m_colorBox.setColor(m_color);
        m_colorBox.repaint();
    }
}

