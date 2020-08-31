package webwars.movingobject;

import webwars.*;

public class MODescriptor
{
    private int m_id;
    private Vec m_position;
    private Vec m_velocity;
    private int m_size;
    private int m_danger;
    private String m_type;
    
    public MODescriptor(int in_id, int in_xpos, int in_ypos, int in_xvel,
                        int in_yvel, int in_size, int in_danger,
                        String in_type)
    {
        m_id = in_id;
        m_position = new Vec(in_xpos, in_ypos);
        m_velocity = new Vec(in_xvel, in_yvel);
        m_size = in_size;
        m_danger = in_danger;
        m_type = in_type;
    }

    public int id()
    {
        return m_id;
    }

    public Vec position()
    {
        return m_position;
    }

    public Vec velocity()
    {
        return m_velocity;
    }

    public int size()
    {
        return m_size;
    }

    public int danger()
    {
        return m_danger;
    }

    public String type()
    {
        return m_type;
    }
}
