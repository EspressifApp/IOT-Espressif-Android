package com.espressif.iot.ui.main;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.ui.login.LoginActivity;
import com.espressif.iot.ui.view.EspPagerAdapter;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class WelcomeActivity extends Activity
{
    
    private static final Logger log = Logger.getLogger(WelcomeActivity.class);
    
    private Point mScreenSize;
    
    private ImageView mMainIV;
    
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private List<View> mPagerViewList;
    
    private final static String NAME_USE_INFO = "use_info";
    private final static String KEY_LAST_USE_VERSION_CODE = "key_last_use_version_code";
    
    private final static int MSG_SHOW_PAGER = 0x10;
    private final static int MSG_LOGIN = 0x11;
    
    private Handler mHandler;
    
    private SharedPreferences mShared;
    
    private EspApplication mApplication;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.welcome_activity);
        
        mApplication = EspApplication.sharedInstance();
        
        mScreenSize = getScreenSize();
        
        mMainIV = (ImageView)findViewById(R.id.welcome_main_img);
        mPager = (ViewPager)findViewById(R.id.welcome_pager);
        mPagerViewList = new ArrayList<View>();
        initPagerItem();
        mPagerAdapter = new EspPagerAdapter(mPagerViewList);
        mPager.setAdapter(mPagerAdapter);
        
        mHandler = new WelcomeHandler(this);
        mShared = getSharedPreferences(NAME_USE_INFO, Context.MODE_PRIVATE);
        int lastUseVersion = mShared.getInt(KEY_LAST_USE_VERSION_CODE, 0);
        int currentVersion = mApplication.getVersionCode();
        if (currentVersion > lastUseVersion)
        {
            mHandler.sendEmptyMessageDelayed(MSG_SHOW_PAGER, 1000);
        }
        else
        {
            mHandler.sendEmptyMessage(MSG_LOGIN);
        }
    }
    
    private Point getScreenSize()
    {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        log.info("Screen width = " + point.x + " | Screen height = " + point.y);
        return point;
    }
    
    private void initPagerItem()
    {
        LayoutInflater inflater = getLayoutInflater();
        
        for (int i = 0; i < 3; i++)
        {
            ImageView pagerItem = (ImageView)inflater.inflate(R.layout.welcome_pager_item, null);
            switch (i)
            {
                case 0:
                    pagerItem.setImageResource(R.drawable.esp_welcome_pager_src_0);
                    break;
                case 1:
                    pagerItem.setImageResource(R.drawable.esp_welcome_pager_src_1);
                    break;
                case 2:
                    pagerItem.setImageResource(R.drawable.esp_welcome_pager_src_2);
                    pagerItem.setOnClickListener(mToLoginListener);
                    break;
            }
            
            mPagerViewList.add(pagerItem);
        }
    }
    
    private View.OnClickListener mToLoginListener = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            login();
            
            int versionCode = mApplication.getVersionCode();
            mShared.edit().putInt(KEY_LAST_USE_VERSION_CODE, versionCode).commit();
        }
    };
    
    private void showPager()
    {
        final long duration = 800;
        Animation mainOutAnim = new TranslateAnimation(0, -mScreenSize.x, 0, 0);
        mainOutAnim.setDuration(duration);
        mainOutAnim.setAnimationListener(new Animation.AnimationListener()
        {
            
            @Override
            public void onAnimationStart(Animation animation)
            {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }
            
            @Override
            public void onAnimationEnd(Animation animation)
            {
                mMainIV.clearAnimation();
                mMainIV.setVisibility(View.GONE);
            }
        });
        
        Animation pagerInAnim = new TranslateAnimation(mScreenSize.x, 0, 0, 0);
        pagerInAnim.setDuration(duration);
        pagerInAnim.setAnimationListener(new Animation.AnimationListener()
        {
            
            @Override
            public void onAnimationStart(Animation animation)
            {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }
            
            @Override
            public void onAnimationEnd(Animation animation)
            {
                mPager.clearAnimation();
            }
        });
        
        mMainIV.startAnimation(mainOutAnim);
        
        mPager.setVisibility(View.VISIBLE);
        mPager.startAnimation(pagerInAnim);
    }
    
    private void login()
    {
        // Get user data from DB
        BEspUser.getBuilder().getInstance().doActionUserLoginDB();
        SharedPreferences shared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        if (shared.getBoolean(EspStrings.Key.KEY_AUTO_LOGIN,
            false))
        {
            // Auto login, go to device list page
            Intent autoIntent = new Intent(this, EspApplication.getEspUIActivity());
            startActivity(autoIntent);
        }
        else
        {
            // Go to LoginActivity
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }
        
        finish();
    }
    
    private static class WelcomeHandler extends Handler
    {
        private WeakReference<WelcomeActivity> mActivity;
        
        public WelcomeHandler(WelcomeActivity activity)
        {
            mActivity = new WeakReference<WelcomeActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            WelcomeActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            switch (msg.what)
            {
                case MSG_SHOW_PAGER:
                    log.debug("handle show Pager msg");
                    activity.showPager();
                    break;
                case MSG_LOGIN:
                    log.debug("handle Login msg");
                    activity.login();
                    break;
            }
        }
    }
}
