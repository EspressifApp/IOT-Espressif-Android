package com.espressif.iot.ui.view.menu;

import android.view.View;

public interface IEspBottomMenu
{
    View addBottomItem(int itemId, int iconId);
    
    View addBottomItem(int itemId, int iconId, int nameId);
    
    View addBottomItem(int itemId, int iconId, CharSequence name);
}
