package com.espressif.iot.ui;

import android.view.View;

public interface IEspBottomBar
{
    View addBottomItem(int itemId, int iconId);
    
    View addBottomItem(int itemId, int iconId, int nameId);
    
    View addBottomItem(int itemId, int iconId, CharSequence name);
}
