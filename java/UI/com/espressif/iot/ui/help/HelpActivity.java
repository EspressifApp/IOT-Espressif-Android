package com.espressif.iot.ui.help;

import com.espressif.iot.R;
import com.espressif.iot.ui.main.EspActivityAbs;

import android.app.FragmentManager;
import android.os.Bundle;

public class HelpActivity extends EspActivityAbs
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new HelpFragment())
                .commit();

        setTitle(R.string.esp_help);
    }
}
