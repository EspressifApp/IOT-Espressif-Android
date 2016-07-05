package com.espressif.iot.ui.login;

import java.util.HashMap;

import org.apache.log4j.Logger;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

import com.espressif.iot.R;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspThirdPartyLoginPlat;
import com.espressif.iot.ui.widget.dialog.NoBgDialog;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class LoginThirdPartyDialog implements PlatformActionListener, OnClickListener
{
    private final Logger log = Logger.getLogger(getClass());
    
    private Context mContext;
    
    private Platform mWeibo;
    private Platform mWeChat;
    private Platform mQQ;
    private Platform mFacebook;
    private Platform mTwitter;
    
    private TextView mWeiboTV;
    private TextView mWeChatTV;
    private TextView mQQTV;
    private TextView mFacebookTV;
    private TextView mTwitterTV;
    
    private NoBgDialog mThirdPartyLoginSelectDialog;
    private ProgressDialog mThirdPartyEnteringDialog;
    
    private Handler mHandler = new Handler();
    
    public interface OnLoginListener
    {
        void onLoginComplete(EspLoginResult result);
    }
    
    private OnLoginListener mLoginListener;
    
    public LoginThirdPartyDialog(Context context)
    {
        mContext = context;
        
        mWeibo = ShareSDK.getPlatform(EspThirdPartyLoginPlat.SHARESDK_NAME_WEIBO);
        mWeibo.setPlatformActionListener(this);
        mWeChat = ShareSDK.getPlatform(EspThirdPartyLoginPlat.SHARESDK_NAME_WECHAT);
        mWeChat.setPlatformActionListener(this);
        mQQ = ShareSDK.getPlatform(EspThirdPartyLoginPlat.SHARESDK_NAME_QQ);
        mQQ.setPlatformActionListener(this);
        mFacebook = ShareSDK.getPlatform(EspThirdPartyLoginPlat.SHARESDK_NAME_FACEBOOK);
        mFacebook.setPlatformActionListener(this);
        mTwitter = ShareSDK.getPlatform(EspThirdPartyLoginPlat.SHARESDK_NAME_TWITTER);
        mTwitter.setPlatformActionListener(this);
        
        View content = View.inflate(mContext, R.layout.third_party_login_dialog, null);
        mWeiboTV = (TextView)content.findViewById(R.id.weibo_login);
        mWeiboTV.setOnClickListener(this);
        mWeChatTV = (TextView)content.findViewById(R.id.wechat_login);
        mWeChatTV.setOnClickListener(this);
        mQQTV = (TextView)content.findViewById(R.id.qq_login);
        mQQTV.setOnClickListener(this);
        mFacebookTV = (TextView)content.findViewById(R.id.facebook_login);
        mFacebookTV.setOnClickListener(this);
        mTwitterTV = (TextView)content.findViewById(R.id.twitter_login);
        mTwitterTV.setOnClickListener(this);
        
        mThirdPartyLoginSelectDialog = new NoBgDialog(mContext);
        mThirdPartyLoginSelectDialog.setView(content);
        
        mThirdPartyEnteringDialog = new ProgressDialog(mContext);
        mThirdPartyEnteringDialog.setMessage(mContext.getString(R.string.esp_third_party_authorizing));
        mThirdPartyEnteringDialog.setCanceledOnTouchOutside(false);
    }
    
    public void show()
    {
        if (mThirdPartyEnteringDialog.isShowing())
        {
            return;
        }
        if (!mThirdPartyLoginSelectDialog.isShowing())
        {
            mThirdPartyLoginSelectDialog.show();
        }
    }
    
    public void setOnLoginListener(OnLoginListener listener)
    {
        mLoginListener = listener;
    }
    
    @Override
    public void onClick(View v)
    {
        mThirdPartyLoginSelectDialog.dismiss();
        mThirdPartyEnteringDialog.show();
        
        Platform platform = null;
        
        switch(v.getId())
        {
            case R.id.weibo_login:
                platform = mWeibo;
                break;
            case R.id.wechat_login:
                platform = mWeChat;
                break;
            case R.id.qq_login:
                platform = mQQ;
                break;
            case R.id.facebook_login:
                platform = mFacebook;
                break;
            case R.id.twitter_login:
                platform = mTwitter;
                break;
        }
        
        if (platform.isAuthValid())
        {
            mThirdPartyEnteringDialog.dismiss();
            thirdPartyLogin(platform);
        }
        else
        {
            platform.authorize();
        }
    }
    
    @Override
    public void onComplete(Platform platform, int action, HashMap<String, Object> res)
    {
        mThirdPartyEnteringDialog.dismiss();
        
        final Platform plat = platform;
        
        log.debug("authorize() onComplete : " + platform.getName());
        
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                thirdPartyLogin(plat);
            }
        });
    }
    
    @Override
    public void onCancel(Platform platform, int action)
    {
        mThirdPartyEnteringDialog.dismiss();
        log.debug("authorize() onCancel");
    }

    @Override
    public void onError(Platform platform, int action, Throwable t)
    {
        mThirdPartyEnteringDialog.dismiss();
        final String platName = platform.getName();
        log.debug("authorize() onError " + platName);
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(mContext,
                    mContext.getString(R.string.esp_third_party_get_token_failed, platName),
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void thirdPartyLogin(Platform plat)
    {
        String state = EspThirdPartyLoginPlat.convertShareSDKNameToEspState(plat.getName());
        String token = plat.getDb().getToken();
        String userId = plat.getDb().getUserId();
        
        final EspThirdPartyLoginPlat espPlat = new EspThirdPartyLoginPlat();
        espPlat.setAccessToken(token);
        espPlat.setState(state);
        espPlat.setOpenId(userId);
        
        new LoginTask(mContext)
        {
            @Override
            public EspLoginResult doLogin()
            {
                IEspUser user = BEspUser.getBuilder().getInstance();
                return user.doActionThirdPartyLoginInternet(espPlat);
            }
            
            @Override
            public void loginResult(EspLoginResult result)
            {
                if (mLoginListener != null)
                {
                    mLoginListener.onLoginComplete(result);
                }
            }
        }.execute();
    }
}
