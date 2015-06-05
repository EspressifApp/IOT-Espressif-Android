package com.espressif.iot.ui.help;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUI;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class HelpFragment extends PreferenceFragment implements IEspHelpUI
{
    private final static String KEY_CONFIGURE_HELP = "esp_help_configure";
    private final static String KEY_USE_PLUG_HELP = "esp_help_use_plug";
    private final static String KEY_USE_PLUGS_HELP = "esp_help_use_plugs";
    private final static String KEY_USE_LIGHT_HELP = "esp_help_use_light";
    private final static String KEY_USE_HUMITURE_HELP = "esp_help_use_humiture";
    private final static String KEY_USE_FLAMMABLE_HELP = "esp_help_use_flammable";
    private final static String KEY_USE_VOLTAGE_HELP = "esp_help_use_voltage";
    private final static String KEY_USE_REMOTE_HELP = "esp_help_use_remote";
    private final static String KEY_UPGRADE_LOCAL_HELP = "esp_help_upgrade_local";
    private final static String KEY_UPGRADE_ONLINE_HELP = "esp_help_upgrade_online";
    
    private Preference mConfigureHelpPre;
    private Preference mUsePlugHelpPre;
    private Preference mUsePlugsHelpPre;
    private Preference mUseLightHelpPre;
    private Preference mUseHumitureHelpPre;
    private Preference mUseFlammableHelpPre;
    private Preference mUseVoltageHelpPre;
    private Preference mUseRemoteHelpPre;
    private Preference mUpgradeLocalPre;
    private Preference mUpgradeOnlinePre;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.helps);
        
        mConfigureHelpPre = findPreference(KEY_CONFIGURE_HELP);
        mUsePlugHelpPre = findPreference(KEY_USE_PLUG_HELP);
        mUsePlugsHelpPre = findPreference(KEY_USE_PLUGS_HELP);
        mUseLightHelpPre = findPreference(KEY_USE_LIGHT_HELP);
        mUseHumitureHelpPre = findPreference(KEY_USE_HUMITURE_HELP);
        mUseFlammableHelpPre = findPreference(KEY_USE_FLAMMABLE_HELP);
        mUseVoltageHelpPre = findPreference(KEY_USE_VOLTAGE_HELP);
        mUseRemoteHelpPre = findPreference(KEY_USE_REMOTE_HELP);
        mUpgradeLocalPre = findPreference(KEY_UPGRADE_LOCAL_HELP);
        mUpgradeOnlinePre = findPreference(KEY_UPGRADE_ONLINE_HELP);
        
        // Remote has stopped develop
        if (mUseRemoteHelpPre != null)
        {
            getPreferenceScreen().removePreference(mUseRemoteHelpPre);
        }
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        if (preference == mConfigureHelpPre)
        {
            finishForResult(RESULT_HELP_CONFIGURE);
            return true;
        }
        else if (preference == mUsePlugHelpPre)
        {
            finishForResult(RESULT_HELP_USE_PLUG);
            return true;
        }
        else if (preference == mUseLightHelpPre)
        {
            finishForResult(RESULT_HELP_USE_LIGHT);
            return true;
        }
        else if (preference == mUseHumitureHelpPre)
        {
            finishForResult(RESULT_HELP_USE_HUMITURE);
            return true;
        }
        else if (preference == mUseFlammableHelpPre)
        {
            finishForResult(RESULT_HELP_USE_FLAMMABLE);
            return true;
        }
        else if (preference == mUseVoltageHelpPre)
        {
            finishForResult(RESULT_HELP_USE_VOLTAGE);
            return true;
        }
        else if (preference == mUseRemoteHelpPre)
        {
            finishForResult(RESULT_HELP_USE_REMOTE);
            return true;
        }
        else if (preference == mUsePlugsHelpPre)
        {
            finishForResult(RESULT_HELP_USE_PLUGS);
            return true;
        }
        else if (preference == mUpgradeLocalPre)
        {
            finishForResult(RESULT_HELP_UPGRADE_LOCAL);
            return true;
        }
        else if (preference == mUpgradeOnlinePre)
        {
            finishForResult(RESULT_HELP_UPGRADE_ONLINE);
            return true;
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    /**
     * Set result for activity then finish.
     * @param result
     */
    private void finishForResult(int result)
    {
        getActivity().setResult(result);
        getActivity().finish();
    }
}
