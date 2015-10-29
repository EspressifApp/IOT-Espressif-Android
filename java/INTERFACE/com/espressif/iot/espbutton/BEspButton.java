package com.espressif.iot.espbutton;

import com.espressif.iot.model.espbutton.EspButton;
import com.espressif.iot.model.espbutton.EspButtonGroup;

public class BEspButton
{
    private BEspButton()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspButton instance = new BEspButton();
    }
    
    public static BEspButton getInstance()
    {
        return InstanceHolder.instance;
    }
    
    public IEspButton allocEspButton()
    {
        IEspButton button = new EspButton();
        return button;
    }
    
    public IEspButtonGroup allocEspButtonGroup()
    {
        IEspButtonGroup group = new EspButtonGroup();
        return group;
    }
}
