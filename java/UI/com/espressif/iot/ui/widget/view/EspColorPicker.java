package com.espressif.iot.ui.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class EspColorPicker extends View
{
    private Context mContext;

    /**
     * Vertical color
     */
    private static final int[] mColorsV = new int[] {
        0xFFFF0000,
        0xFFFFFF00,
        0xFF00FF00,
        0xFF00FFFF,
        0xFF0000FF,
        0xFFFF00FF,
        0xFFFF0000,
    };
    /**
     * Horizontal color
     */
    private static final int[] mColorsH = new int[] {
        0xFF000000,
        0x00000000,
        0x00FFFFFF,
        0xFFFFFFFF,
    };
    /**
     * Position of horizontal color
     */
    private static final float[] mPositionsH = new float[] {
        0f,
        0.5f,
        0.5f,
        1f,
    };
    
    private RectF mRectF;
    private Paint mPaintV;
    private Shader mShaderV;
    private Paint mPaintH;
    private Shader mShaderH;
    
    private OnColorChangedListener mColorChangedListener;
    
    private static final float MIN_HEIGHT = 20; // Unit is dip;
    
    private boolean mClicked;
    private float mLineVX;
    private float mLineHY;
    private Paint mLinePaint;
    private float mLineWidth;
    
    public EspColorPicker(Context context)
    {
        super(context);
        init(context);
    }

    public EspColorPicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public EspColorPicker(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context)
    {
        mContext = context;
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        float minHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_HEIGHT, dm);
        setMinimumHeight((int)minHeight);

        mRectF = new RectF();
        mPaintV = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintH = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(0x80000000);
        mLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
    }
    
    public interface OnColorChangedListener
    {
        void onColorChangeStart(View v, int color);
        
        void onColorChanged(View v, int color);
        
        void onColorChangeEnd(View v, int color);
    }
    
    public void setOnColorChangeListener(OnColorChangedListener listener)
    {
        mColorChangedListener = listener;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        
        int width = getWidth();
        int height = getHeight();

        mRectF.left = 0;
        mRectF.top = 0;
        mRectF.right = width;
        mRectF.bottom = height;
        
        if (mShaderV == null)
        {
            mShaderV = new LinearGradient(0, mRectF.top, 0, mRectF.bottom, mColorsV, null, TileMode.MIRROR);
            mPaintV.setShader(mShaderV);
        }
        if (mShaderH == null)
        {
            mShaderH = new LinearGradient(mRectF.left, 0, mRectF.right, 0, mColorsH, mPositionsH, TileMode.MIRROR);
            mPaintH.setShader(mShaderH);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawRect(mRectF, mPaintV);
        canvas.drawRect(mRectF, mPaintH);
        
        if (mClicked)
        {
            float lineWidthOffset = mLineWidth / 2;
            canvas.drawRect(mLineVX - lineWidthOffset, 0, mLineVX + lineWidthOffset, getHeight(), mLinePaint);
            canvas.drawRect(0, mLineHY - lineWidthOffset, getWidth(), mLineHY + lineWidthOffset, mLinePaint);
        }
        
        super.onDraw(canvas);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = mLineVX = event.getX();
        float y = mLineHY = event.getY();
        
        if (mLineVX < mRectF.left)
        {
            mLineVX = mRectF.left;
        }
        else if (mLineVX > mRectF.right)
        {
            mLineVX = mRectF.right;
        }
        
        if (mLineHY < mRectF.top)
        {
            mLineHY = mRectF.top;
        }
        else if (mLineHY > mRectF.bottom)
        {
            mLineHY = mRectF.bottom;
        }
        
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mClicked = true;
                if (mColorChangedListener != null)
                {
                    mColorChangedListener.onColorChangeStart(this, getTouchPointColor(x, y));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mColorChangedListener != null)
                {
                    mColorChangedListener.onColorChanged(this, getTouchPointColor(x, y));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mClicked = false;
                if (mColorChangedListener != null)
                {
                    mColorChangedListener.onColorChangeEnd(this, getTouchPointColor(x, y));
                }
                break;
        }
        
        invalidate();
        
        return true;
    }
    
    private int getTouchPointColor(float x, float y)
    {
        // get Vertical color
        int colorV = interceptColor(mColorsV, y, mRectF.top, mRectF.bottom);
        // get touch point color
        int[] colorsH = {Color.BLACK, colorV, Color.WHITE};
        int color = interceptColor(colorsH, x, mRectF.left, mRectF.right);
        
        return color;
    }
    
    /**
     * Get the color value of the axis
     * @param colors
     * @param axis
     * @param minAxis
     * @param maxAxis
     * @return
     */
    private int interceptColor(int[] colors, float axis, float minAxis, float maxAxis)
    {
        int result;
        
        if (axis < minAxis)
        {
            result = colors[0];
        }
        else if (axis > maxAxis)
        {
            result = colors[colors.length - 1];
        }
        else
        {
            float percent = (axis - minAxis) / maxAxis;
            float position = (colors.length - 1) * percent;
            int start = (int)position;
            int end = start + 1;
            int color0 = colors[start];
            int color1 = colors[end];
            
            int a = ave(Color.alpha(color0), Color.alpha(color1), position - start);
            int r = ave(Color.red(color0), Color.red(color1), position - start);
            int g = ave(Color.green(color0), Color.green(color1), position - start);
            int b = ave(Color.blue(color0), Color.blue(color1), position - start);
            
            result = Color.argb(a, r, g, b);
        }
        
        return result;
    }

    /**
     * Get the value of position p from s to d
     * @param s
     * @param d
     * @param p
     * @return
     */
    private int ave(int s, int d, float p)
    {
        return s + Math.round(p * (d - s));
    }
    
    /**
     * Set the width of the touch point axis line
     * @param value the unit is PX
     */
    public void setLineWidth(float value)
    {
        setLineWidth(TypedValue.COMPLEX_UNIT_PX, value);
    }
    
    /**
     * Set the width of the touch point axis line
     * @param unit @see TypedValue
     * @param value
     */
    public void setLineWidth(int unit, float value)
    {
        mLineWidth = TypedValue.applyDimension(unit, value, mContext.getResources().getDisplayMetrics());
    }
}
