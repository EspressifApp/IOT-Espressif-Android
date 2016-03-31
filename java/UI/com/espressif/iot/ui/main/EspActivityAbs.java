package com.espressif.iot.ui.main;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.ui.view.menu.IEspBottomMenu;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public abstract class EspActivityAbs extends Activity
{
    private FrameLayout mContentView;
    
    private LinearLayout mTitleView;
    private LinearLayout mBottomView;
    
    private TextView mTitleTV;
    private ImageView mLeftIcon;
    private ImageView mRightIcon;
    private ViewGroup mTitleContentView;
    
    public static final int InputType_PASSWORD_VISIBLE =
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
    public static final int InputType_PASSWORD_NORMAL =
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
    
    private int mScreenWidth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        super.setContentView(R.layout.esp_activity_abs);
        getActionBar().hide();
        
        mContentView = (FrameLayout)findViewById(R.id.content);
        mBottomView = (LinearLayout)findViewById(R.id.bottom_bar);
        
        mTitleView = (LinearLayout)findViewById(R.id.title_bar);
        mTitleTV = (TextView)findViewById(R.id.title_text);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightIcon = (ImageView)findViewById(R.id.right_icon);
        mTitleContentView = (ViewGroup)findViewById(R.id.title_content);
        
        setTitleLeftIcon(R.drawable.esp_icon_back);
        
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        EspBottomMenu bottomMenu = new EspBottomMenu(this);
        onCreateBottomItems(bottomMenu);
        
        int width = getResources().getDimensionPixelSize(R.dimen.esp_bottom_item_width);
        int height = getResources().getDimensionPixelSize(R.dimen.esp_bottom_item_height);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        
        int visibleItemCount = mScreenWidth / width;
        final List<View> items = bottomMenu.getItemViews();
        if (items.size() <= visibleItemCount)
        {
            // All menus can show on bottom bar
            if (items.size() > 0)
            {
                int margin = (mScreenWidth - (width * items.size())) / items.size() / 2;
                lp.setMargins(margin, 0, margin, 0);
            }
            for (View view : items)
            {
                mBottomView.addView(view, lp);
            }
        }
        else
        {
            // Bottom bar hasn't enough room to show all menus
            for (int i = 0; i < visibleItemCount - 1; i++)
            {
                View item = items.get(i);
                mBottomView.addView(item, lp);
            }
            TextView moreItem = bottomMenu.createMoreOverflowItemView(visibleItemCount);
            mBottomView.addView(moreItem, lp);
        }
        
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
    public void setTitleLeftIcon(int iconId)
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
    public void setTitleRightIcon(int iconId)
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
        
        checkTitleRightIconAndTitleContentView();
    }
    
    /**
     * Set the view on the left side of right title icon
     * 
     * @param view
     */
    public void setTitleContentView(View view)
    {
        setTitleContentView(view, 0, 0, 0, 0);
    }
    
    /**
     * Set the view on the left side of right title icon
     * 
     * @param view
     * @param paddingLeft of the content view
     * @param paddingTop of the content view
     * @param paddingRight of the content view
     * @param paddingBottom of the content view
     */
    public void setTitleContentView(View view, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom)
    {
        mTitleContentView.removeAllViews();
        mTitleContentView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        
        if (view != null)
        {
            mTitleContentView.setVisibility(View.VISIBLE);
            mTitleContentView.addView(view);
        }
        else
        {
            mTitleContentView.setVisibility(View.GONE);
        }
        
        checkTitleRightIconAndTitleContentView();
    }
    
    private void checkTitleRightIconAndTitleContentView() {
        if (mRightIcon.getDrawable() == null) {
            if (mTitleContentView.getVisibility() == View.VISIBLE) {
                mRightIcon.setVisibility(View.GONE);
            } else {
                mRightIcon.setVisibility(View.VISIBLE);
            }
        } else {
            mRightIcon.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Show ESP title bar
     */
    protected void showTitleBar()
    {
        mTitleView.setVisibility(View.VISIBLE);
    }
    
    /**
     * Hide ESP title bar
     */
    protected void hideTitleBar()
    {
        mTitleView.setVisibility(View.GONE);
    }
    
    private class EspBottomMenu implements IEspBottomMenu
    {
        private EspActivityAbs mActivity;
        private List<View> mBottomItems;
        
        public EspBottomMenu(EspActivityAbs activity)
        {
            mActivity = activity;
            mBottomItems = new ArrayList<View>();
        }
        
        @Override
        public View addBottomItem(int itemId, int iconRes)
        {
            return addBottomItem(itemId, iconRes, "");
        }
        
        @Override
        public View addBottomItem(int itemId, int iconRes, int titleId)
        {
            return addBottomItem(itemId, iconRes, mActivity.getString(titleId));
        }
        
        @Override
        public View addBottomItem(int itemId, int iconRes, CharSequence title)
        {
            if (mBottomView.getVisibility() != View.VISIBLE)
            {
                mBottomView.setVisibility(View.VISIBLE);
            }
            
            TextView item = createItemView(itemId, iconRes, title);
            mBottomItems.add(item);
            
            return item;
        }
        
        /**
         * Create normal menu item view
         * 
         * @param itemId
         * @param iconRes
         * @param title
         * @return
         */
        private TextView createItemView(int itemId, int iconRes, CharSequence title)
        {
            TextView item = new TextView(mActivity);
            item.setId(itemId);
            item.setBackgroundResource(R.drawable.esp_activity_icon_background);
            item.setCompoundDrawablesWithIntrinsicBounds(0, iconRes, 0, 0);
            item.setText(title);
            item.setContentDescription(title);
            int padding = getResources().getDimensionPixelSize(R.dimen.esp_bottom_item_padding);
            item.setPadding(padding, padding, padding, padding);
            item.setGravity(Gravity.CENTER_HORIZONTAL);
            item.setTextColor(Color.WHITE);
            item.setSingleLine();
            item.setEllipsize(TextUtils.TruncateAt.END);
            item.setOnClickListener(mBottomItemClickListener);
            item.setOnLongClickListener(mBottomItemLongClickListener);
            
            return item;
        }
        
        /**
         * Create more overflow item view. Set the items can't show on bottom bar in PopupMenu
         * 
         * @param firstMoreItemIndex
         * @return
         */
        public TextView createMoreOverflowItemView(final int firstMoreItemIndex)
        {
            TextView moreItem =
                createItemView(Integer.MAX_VALUE,
                    R.drawable.esp_icon_menu_moreoverflow,
                    mActivity.getString(R.string.esp_activity_bottom_item_more_overflow));
            moreItem.setOnClickListener(new View.OnClickListener()
            {
                
                @Override
                public void onClick(View v)
                {
                    ((PopupMenu)v.getTag()).show();
                }
            });
            
            PopupMenu popupMenu = new PopupMenu(mActivity, moreItem);
            Menu menu = popupMenu.getMenu();
            for (int i = firstMoreItemIndex - 1; i < mBottomItems.size(); i++)
            {
                View item = mBottomItems.get(i);
                menu.add(Menu.NONE, item.getId(), 0, item.getContentDescription());
            }
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    int id = item.getItemId();
                    for (View v : mBottomItems)
                    {
                        if (v.getId() == id)
                        {
                            onBottomItemClick(v, id);
                            return true;
                        }
                    }
                    return false;
                }
            });
            moreItem.setTag(popupMenu);
            
            return moreItem;
        }
        
        public List<View> getItemViews()
        {
            return mBottomItems;
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
    }
    
    /**
     * This function will be used in onCreateOptionsMenu(Menu menu), can addBottomItem here
     */
    protected void onCreateBottomItems(IEspBottomMenu bottomMenu)
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
                onTitleLeftIconClick(mLeftIcon);
            }
            else if (v == mRightIcon)
            {
                onTitleRightIconClick(mRightIcon);
            }
        }
    };
    
    /**
     * Get left icon on title
     * 
     * @return
     */
    protected View getLeftTitleIcon()
    {
        return mLeftIcon;
    }
    
    /**
     * Get right icon on title
     * 
     * @return
     */
    protected View getRightTitleIcon()
    {
        return mRightIcon;
    }
    
    /**
     * The title bar left icon click listener
     */
    protected void onTitleLeftIconClick(View leftIcon)
    {
        onBackPressed();
    }
    
    /**
     * The title bar right icon click listener
     */
    protected void onTitleRightIconClick(View rightIcon)
    {
    }
}
