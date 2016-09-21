package com.espressif.iot.ui.main;

import android.app.ActionBar;
import android.app.Fragment;

public abstract class EspDrawerFragmentBase extends Fragment {
    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(Fragment fragment, int id);
    }

    protected ActionBar getActionBar() {
        return getActivity().getActionBar();
    }
}
