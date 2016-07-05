package com.espressif.iot.ui.widget.preference;

import com.espressif.iot.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class EspPreferenceCategory extends PreferenceCategory
{
    private ColorStateList mTitleColor;
    
    public EspPreferenceCategory(Context context)
    {
        this(context, null);
    }
    
    public EspPreferenceCategory(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        
        init(context, attrs);
    }
    
    public EspPreferenceCategory(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EspPreference);
        for (int i = a.getIndexCount(); i >= 0; i--)
        {
            int attr = a.getIndex(i);
            switch (attr)
            {
                case R.styleable.EspPreference_titleColor:
                    mTitleColor = a.getColorStateList(attr);
                    break;
            }
        }
        a.recycle();
    }
    
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        if (mTitleColor != null) {
            TextView titleTV = (TextView)view.findViewById(android.R.id.title);
            titleTV.setTextColor(mTitleColor);
        }
    }
    
    public void setTitleColor(int color)
    {
        mTitleColor = ColorStateList.valueOf(color);
        notifyChanged();
    }
    
    public void setTitleColor(ColorStateList colors) {
        if (colors == null) {
            throw new NullPointerException();
        }

        mTitleColor = colors;
        notifyChanged();
    }
}
