package com.espressif.iot.ui.softap_sta_support.help;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUI;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class SSSHelpFragment extends PreferenceFragment implements IEspHelpUI
{
    private static final String KEY_USE_DEVICE = "esp_sss_help_use_device";
    private static final String KEY_UPGRADE = "esp_sss_help_upgrade";
    private static final String KEY_MESH_CONFIGURE = "esp_sss_help_mesh_configure";
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sss_helps);
        
        Preference p = findPreference(KEY_MESH_CONFIGURE);
        if (p != null)
        {
            getPreferenceScreen().removePreference(p);
        }
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        String key = preference.getKey();
        if (key.equals(KEY_USE_DEVICE))
        {
            finishForResult(RESULT_HELP_SSS_USE_DEVICE);
            return true;
        }
        else if (key.equals(KEY_UPGRADE))
        {
            finishForResult(RESULT_HELP_SSS_UPGRADE);
            return true;
        }
        else if (key.equals(KEY_MESH_CONFIGURE))
        {
            finishForResult(RESULT_HELP_SSS_MESH_CONFIGURE);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private void finishForResult(int resultCode)
    {
        getActivity().setResult(resultCode);
        getActivity().finish();
    }
}
