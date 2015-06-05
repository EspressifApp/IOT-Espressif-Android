package com.espressif.iot.ui.softap_sta_support.help;

import com.espressif.iot.R;
import com.espressif.iot.ui.main.EspActivityAbs;

import android.os.Bundle;

public class SSSHelpActivity extends EspActivityAbs
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.fragment_container);
        
        getFragmentManager().beginTransaction().add(R.id.fragment_container, new SSSHelpFragment()).commit();
        
        setTitle(R.string.esp_help);
    }
}
