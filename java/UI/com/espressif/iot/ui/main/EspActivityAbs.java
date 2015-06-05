package com.espressif.iot.ui.main;

import com.espressif.iot.R;
import com.espressif.iot.help.statemachine.IEspHelpStateMachine;
import com.espressif.iot.help.ui.IEspHelpUI;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class EspActivityAbs extends Activity implements IEspHelpUI
{
    private FrameLayout mContentView;
    
    private LinearLayout mTitleView;
    private LinearLayout mBottomView;
    
    private TextView mTitleTV;
    private ImageView mLeftIcon;
    private ImageView mRightIcon;
    
    protected FrameLayout mHelpContainer;
    
    protected static IEspHelpStateMachine mHelpMachine;
    
    public static final int HELP_BUTTON_ALL = -1;
    public static final int HELP_BUTTON_EXIT = -2;
    public static final int HELP_BUTTON_RETRY = -3;
    public static final int HELP_BUTTON_NEXT = -4;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        super.setContentView(R.layout.esp_activity_abs);
        getActionBar().hide();
        
        mHelpMachine = EspHelpStateMachine.getInstance();
        
        mContentView = (FrameLayout)findViewById(R.id.content);
        mBottomView = (LinearLayout)findViewById(R.id.bottom_bar);
        
        mTitleView = (LinearLayout)findViewById(R.id.title_bar);
        mTitleTV = (TextView)findViewById(R.id.title_text);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightIcon = (ImageView)findViewById(R.id.right_icon);
        
        setTitleLeftIcon(R.drawable.esp_icon_back);
        
        mHelpContainer = (FrameLayout)findViewById(R.id.help_container);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        onCreateBottomItems(new EspBottomBar(this));
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public void setContentView(int layoutResID)
    {
        mContentView.removeAllViews();
        getLayoutInflater().inflate(layoutResID, mContentView);
    }
    
    @Override
    public void setContentView(View view)
    {
        mContentView.removeAllViews();
        mContentView.addView(view);
    }
    
    @Override
    public void setContentView(View view, LayoutParams params)
    {
        mContentView.removeAllViews();
        mContentView.addView(view, params);
    }
    
    @Override
    public void setTitle(int titleId)
    {
        super.setTitle(titleId);
        
        mTitleTV.setText(titleId);
    }
    
    @Override
    public void setTitle(CharSequence title)
    {
        super.setTitle(title);
        
        mTitleTV.setText(title);
    }
    
    /**
     * Set the icon on title bar left side
     * 
     * @param iconId
     */
    protected void setTitleLeftIcon(int iconId)
    {
        mLeftIcon.setImageResource(iconId);
        if (iconId == 0)
        {
            mLeftIcon.setClickable(false);
        }
        else
        {
            mLeftIcon.setOnClickListener(mIconClickListener);
        }
    }
    
    /**
     * Set the icon on title bar right side
     * 
     * @param iconId
     */
    protected void setTitleRightIcon(int iconId)
    {
        mRightIcon.setImageResource(iconId);
        if (iconId == 0)
        {
            mRightIcon.setClickable(false);
        }
        else
        {
            mRightIcon.setOnClickListener(mIconClickListener);
        }
    }
    
    public void setTitleContentView(View view)
    {
        setTitleContentView(view, 0, 0, 0, 0);
    }
    
    public void setTitleContentView(View view, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom)
    {
        ViewGroup content = (ViewGroup)findViewById(R.id.title_content);
        content.removeAllViews();
        content.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        
        if (view != null)
        {
            content.setVisibility(View.VISIBLE);
            content.addView(view);
        }
        else
        {
            content.setVisibility(View.GONE);
        }
    }
    
    protected void showTitleBar()
    {
        mTitleView.setVisibility(View.VISIBLE);
    }
    
    protected void hideTitleBar()
    {
        mTitleView.setVisibility(View.GONE);
    }
    
    /**
     * Add item to bottom bar
     * 
     * @param itemId
     * @param iconId
     * @param name
     * @return the item View
     */
    private View addBottomItem(int itemId, int iconId, CharSequence name)
    {
        if (mBottomView.getVisibility() != View.VISIBLE)
        {
            mBottomView.setVisibility(View.VISIBLE);
        }
        
        ImageView item = new ImageView(this);
        item.setId(itemId);
        item.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.esp_activity_bottom_bar_height));
        item.setAdjustViewBounds(true);
        item.setScaleType(ScaleType.CENTER_INSIDE);
        item.setImageResource(iconId);
        item.setBackgroundResource(R.drawable.esp_activity_icon_background);
        item.setContentDescription(name);
        item.setOnClickListener(mBottomItemClickListener);
        item.setOnLongClickListener(mBottomItemLongClickListener);
        int padding = getResources().getDimensionPixelSize(R.dimen.esp_activity_bottom_item_padding);
        item.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1);
        mBottomView.addView(item, lp);
        
        return item;
    }
    
    private static class EspBottomBar implements IEspBottomBar
    {
        private EspActivityAbs mActivity;
        
        public EspBottomBar(EspActivityAbs activity)
        {
            mActivity = activity;
        }
        
        @Override
        public View addBottomItem(int itemId, int iconId)
        {
            return mActivity.addBottomItem(itemId, iconId, "");
        }
        
        @Override
        public View addBottomItem(int itemId, int iconId, int nameId)
        {
            return mActivity.addBottomItem(itemId, iconId, mActivity.getString(nameId));
        }
        
        @Override
        public View addBottomItem(int itemId, int iconId, CharSequence name)
        {
            return mActivity.addBottomItem(itemId, iconId, name);
        }
        
    }
    
    private View.OnClickListener mBottomItemClickListener = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            onBottomItemClick(v, v.getId());
        }
    };
    
    private Toast mBottomItemToast;
    
    private View.OnLongClickListener mBottomItemLongClickListener = new View.OnLongClickListener()
    {
        
        @Override
        public boolean onLongClick(View v)
        {
            if (mBottomItemToast != null)
            {
                mBottomItemToast.cancel();
                mBottomItemToast = null;
            }
            CharSequence contentDescription = v.getContentDescription();
            if (!TextUtils.isEmpty(contentDescription))
            {
                mBottomItemToast = Toast.makeText(v.getContext(), contentDescription, Toast.LENGTH_SHORT);
                mBottomItemToast.show();
                return true;
            }
            return false;
        }
    };
    
    /**
     * This function will be used in onCreateOptionsMenu(Menu menu), can addBottomItem here
     */
    protected void onCreateBottomItems(IEspBottomBar bottombar)
    {
    }
    
    /**
     * The bottom item click listener
     * 
     * @param v
     * @param itemId
     */
    protected void onBottomItemClick(View v, int itemId)
    {
    }
    
    private View.OnClickListener mIconClickListener = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            if (v == mLeftIcon)
            {
                onTitleLeftIconClick();
            }
            else if (v == mRightIcon)
            {
                onTitleRightIconClick();
            }
        }
    };
    
    protected View getLeftTitleIcon()
    {
        return mLeftIcon;
    }
    
    protected View getRightTitleIcon()
    {
        return mRightIcon;
    }
    
    /**
     * The title bar left icon click listener
     */
    protected void onTitleLeftIconClick()
    {
        onBackPressed();
    }
    
    /**
     * The title bar right icon click listener
     */
    protected void onTitleRightIconClick()
    {
    }
    
    /**
     * Clear all the Views in help container
     */
    public void clearHelpContainer()
    {
        clearAnimation(mHelpContainer);
        mHelpContainer.removeAllViews();
    }
    
    private void clearAnimation(View view)
    {
        view.clearAnimation();
        if (view instanceof ViewGroup)
        {
            ViewGroup vg = (ViewGroup)view;
            for (int i = 0; i < vg.getChildCount(); i++)
            {
                View child = vg.getChildAt(i);
                clearAnimation(child);
            }
        }
    }
    
    /**
     * Add text in help container
     * 
     * @param hintResId
     */
    public void setHelpHintMessage(int hintResId)
    {
        setHelpHintMessage(getString(hintResId));
    }
    
    /**
     * Add text in help container
     * 
     * @param hint
     */
    public void setHelpHintMessage(CharSequence hint)
    {
        View view = getLayoutInflater().inflate(R.layout.esp_help_hint_content, null);
        TextView hintTextView = (TextView)view.findViewById(R.id.help_hint_text);
        hintTextView.setText(hint);
        
        View btnsContainer = view.findViewById(R.id.help_hint_btn_contariner);
        btnsContainer.findViewById(R.id.help_hint_exit_btn).setOnClickListener(mHelpButtonListener);
        btnsContainer.findViewById(R.id.help_hint_retry_btn).setOnClickListener(mHelpButtonListener);
        btnsContainer.findViewById(R.id.help_hint_next_btn).setOnClickListener(mHelpButtonListener);
        
        FrameLayout.LayoutParams lp =
            new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        lp.setMargins(0, 0, 0, mBottomView.getHeight());
        mHelpContainer.addView(view, lp);
    }
    
    /**
     * Show or hide the hint button
     * 
     * @param whichButton
     * @param visible
     */
    public void setHelpButtonVisible(int whichButton, boolean visible)
    {
        View btnsContainer = findViewById(R.id.help_hint_btn_contariner);
        if (btnsContainer != null)
        {
            View exitBtn = findViewById(R.id.help_hint_exit_btn);
            View retryBtn = findViewById(R.id.help_hint_retry_btn);
            View nextBtn = findViewById(R.id.help_hint_next_btn);
            switch (whichButton)
            {
                case HELP_BUTTON_ALL:
                    int visibility = visible ? View.VISIBLE : View.GONE;
                    exitBtn.setVisibility(visibility);
                    retryBtn.setVisibility(visibility);
                    nextBtn.setVisibility(visibility);
                    break;
                case HELP_BUTTON_EXIT:
                    exitBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
                    break;
                case HELP_BUTTON_RETRY:
                    retryBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
                    break;
                case HELP_BUTTON_NEXT:
                    nextBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
                    break;
            }
        }
    }
    
    /**
     * Get the system status bar height
     * 
     * @return The height of StatusBar
     */
    protected int getStatusBarHeight()
    {
        int[] location = new int[2];
        View top = findViewById(R.id.title_bar);
        top.getLocationInWindow(location);
        
        return location[1];
    }
    
    @Override
    public void onBackPressed()
    {
        if (mHelpMachine.isHelpOn())
        {
            showExitHelpDialog();
        }
        else
        {
            super.onBackPressed();
        }
    }
    
    private void showExitHelpDialog()
    {
        new AlertDialog.Builder(this).setTitle(R.string.esp_help_exit_message)
            .setMessage(R.string.esp_help_exit_message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    mHelpMachine.exit();
                    onExitHelpMode();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }
    
    private View.OnClickListener mHelpButtonListener = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.help_hint_exit_btn:
                    showExitHelpDialog();
                    break;
                case R.id.help_hint_retry_btn:
                    mHelpMachine.retry();
                    onHelpRetryClick();
                    break;
                case R.id.help_hint_next_btn:
                    onHelpNextClick();
                    break;
            }
        }
    };
    
    /**
     * When exit help, run here
     */
    public void onExitHelpMode(){}
    
    /**
     * When click help retry button
     */
    public void onHelpRetryClick(){}
    
    /**
     * When click help next button
     */
    public void onHelpNextClick(){}
    
    /**
     * Set the whole layout dark except param view
     * 
     * @param view
     */
    public void highlightHelpView(View view)
    {
        clearHelpContainer();
        
        int[] location = new int[2];
        view.getLocationInWindow(location);
        
        int statusBarHeight = getStatusBarHeight();
        
        Rect rect =
            new Rect(location[0], location[1] - statusBarHeight, location[0] + view.getWidth(), location[1]
                + view.getHeight() - statusBarHeight);
        
        int bgColor = getResources().getColor(R.color.esp_help_frame_background_dark);
        
        if (rect.top > 0)
        {
            View topView = new View(this);
            topView.setBackgroundColor(bgColor);
            topView.setClickable(true);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, rect.top);
            mHelpContainer.addView(topView, lp);
        }
        
        if (rect.bottom < mHelpContainer.getHeight())
        {
            View bottomView = new View(this);
            bottomView.setBackgroundColor(bgColor);
            bottomView.setClickable(true);
            FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, mHelpContainer.getHeight() - rect.bottom,
                    Gravity.BOTTOM);
            mHelpContainer.addView(bottomView, lp);
        }
        
        if (rect.left > 0)
        {
            View leftView = new View(this);
            leftView.setBackgroundColor(bgColor);
            leftView.setClickable(true);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(rect.left, view.getHeight());
            mHelpContainer.addView(leftView, lp);
            leftView.setY(rect.top);
        }
        
        if (rect.right < mHelpContainer.getWidth())
        {
            View rightView = new View(this);
            rightView.setBackgroundColor(bgColor);
            rightView.setClickable(true);
            FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(mHelpContainer.getWidth() - rect.right, view.getHeight(), Gravity.RIGHT);
            mHelpContainer.addView(rightView, lp);
            rightView.setY(rect.top);
        }
    }
    
    protected void setHelpFrameDark()
    {
        clearHelpContainer();
        
        View view = new View(this);
        view.setClickable(true);
        int bgColor = getResources().getColor(R.color.esp_help_frame_background_dark);
        view.setBackgroundColor(bgColor);
        FrameLayout.LayoutParams lp =
            new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mHelpContainer.addView(view, lp);
    }
}
