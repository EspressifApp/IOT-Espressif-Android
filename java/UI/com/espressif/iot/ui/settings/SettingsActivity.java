package com.espressif.iot.ui.settings;

import com.espressif.iot.R;
import com.espressif.iot.ui.main.EspActivityAbs;

import android.app.FragmentManager;
import android.os.Bundle;

public class SettingsActivity extends EspActivityAbs {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();

        setTitle(R.string.esp_settings);
    }
}
