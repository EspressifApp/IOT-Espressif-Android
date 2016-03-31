package com.espressif.iot.ui.configure;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.device.IEspDeviceNew;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class DeviceBrowserWebFragment extends Fragment {
    private final Logger log = Logger.getLogger(getClass());

    private DeviceBrowserConfigureActivity mActivity;

    private ProgressBar mLoadingPB;
    private WebView mWebView;

    private IEspDeviceNew mDevice;

    public void setDevice(IEspDeviceNew device) {
        mDevice = device;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mWebView = (WebView)inflater.inflate(R.layout.device_browser_web, container, false);
        mWebView.setWebViewClient(new DeviceWebViewClient());
        // The device configure web page is JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        return mWebView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = (DeviceBrowserConfigureActivity)getActivity();
        mLoadingPB = new ProgressBar(mActivity);
        mLoadingPB.setVisibility(View.GONE);
        int progressPadding = getResources().getDimensionPixelSize(R.dimen.esp_activity_ui_progress_padding);
        mActivity.setTitleContentView(mLoadingPB, progressPadding, progressPadding, progressPadding, progressPadding);
        mActivity.setTitleRightIcon(android.R.drawable.ic_menu_rotate);

        // Load device configure web page
        String url = "http://" + EspApplication.sharedInstance().getGateway();
        mWebView.loadUrl(url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mActivity.setTitleRightIcon(0);
        mActivity.setTitleContentView(null);
        if (mDevice != null && !TextUtils.isEmpty(mDevice.getApSsid())) {
            // Connect last connected ssid
            EspBaseApiUtil.enableConnected(mDevice.getApSsid());
        }
    }

    public void onTitleRightIconClick() {
        mWebView.reload();
    }

    private class DeviceWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            log.warn("onReceivedError: " + failingUrl + " , " + errorCode + " , " + description);
            mActivity.setResult(Activity.RESULT_CANCELED);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            log.info("onPageStarted: " + url);
            mActivity.setResult(Activity.RESULT_OK);
            mLoadingPB.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            log.info("onPageFinished: " + url);
            mLoadingPB.setVisibility(View.GONE);
        }
        
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }
}
