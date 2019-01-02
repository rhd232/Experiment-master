package com.wind.base;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;
import com.wind.base.di.BaseMvpComponent;
import com.wind.base.mvp.view.DaggerMvpFragment;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.LogUtil;
import com.wind.base.utils.WebViewHelper;
import com.wind.view.TitleBar;

/**
 * Created by wind on 2018/3/14.
 */

public abstract class AbsH5Fragment<V extends MvpView,P extends MvpPresenter<V>,C extends BaseMvpComponent<V,P>> extends DaggerMvpFragment<V,P,C>
{
    public static final String ARG_KEY_H5PARAM="arg_key_h5param";
    protected H5Param mH5Param;
    protected WebView webView;
    protected TitleBar mTitleBar;
    protected WebViewHelper mWebViewHelper;
    private WebViewHelper.ProtocolHandler mProtocolHandler;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mH5Param= (H5Param) getArguments().getSerializable(ARG_KEY_H5PARAM);
        View content = view.findViewById(R.id.webcontent);
        if (content!=null){
            mTitleBar = view.findViewById(R.id.title_bar);
            if (!mH5Param.isTitlebarOverlay()) {
                ViewGroup.LayoutParams contentLayoutParams=content.getLayoutParams();
                FrameLayout.LayoutParams lp;
                if (contentLayoutParams instanceof  FrameLayout.LayoutParams) {
                    lp = (FrameLayout.LayoutParams)contentLayoutParams;
                }else {
                    throw new IllegalArgumentException("webcontent's layoutparams must be FrameLayout.LayoutParams");
                }
//                //  int size=getResources().getDimensionPixelSize(R.dimen.action_bar_height);
//                TypedValue tv = new TypedValue();
//                if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//                    int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
//                    lp.topMargin = actionBarHeight;
//                }
                lp.topMargin = getResources().getDimensionPixelSize(R.dimen.action_bar_height);

            } else {
                mTitleBar.setLineVisibility(View.GONE);
                mTitleBar.setBackgroundColor(Color.TRANSPARENT);

            }
        }
        if (mTitleBar!=null){
            setTitle();
        }
        webView= view.findViewById(R.id.webView);

        mProtocolHandler=getProtocolHandler();
        initView();
        if (mProtocolHandler==null){
            registerWebHandler();
        }
        loadH5();
    }

    /**
     * registerWebHandler互斥，当该方法返回为null时registerWebHandler才有效果
     * @return
     */
    public WebViewHelper.ProtocolHandler getProtocolHandler(){
        return null;
    }
    /**
     * h5需要调用java时设置
     */
    protected  void registerWebHandler(){}

    public int getLayoutRes() {
        return R.layout.activity_h5;
    }


    protected void setTitle() {
        if (mTitleBar != null) {
            mTitleBar.setTitle(mH5Param.getTitle());
            String rightName = mH5Param.getRightBtnName();
            if (TextUtils.isEmpty(rightName)) {

                mTitleBar.setRightVisibility(View.GONE);
            } else {
                mTitleBar.setRightText(rightName);
                mTitleBar.setRightVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 设置webview相关参数
     */
    private void initView() {
        mWebViewHelper= WebViewHelper.getInstance();
        mWebViewHelper.inflateWebView(getActivity(), webView,mProtocolHandler,new WebViewHelper.OnPageLoadingListener() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    }

                    @Override
                    public void onPageFinished(WebView webView, String url) {
                        LogUtil.e("onPageFinished", "url " + url);
                        if (TextUtils.isEmpty(mH5Param.getTitle()) && !mH5Param.isTitlebarOverlay()) {
                            String title = webView.getTitle();
                            if (title != null && title.length() > 10) {
                            } else {
                                mTitleBar.setTitle(title);
                            }

                        }

                        onWebPageFinished(webView, url);
                    }
                });





    }

    private void loadH5() {
        if (mH5Param.getTargetUrl().startsWith("http")) {
            webView.loadUrl(mH5Param.getTargetUrl());
        } else {
            //网页源码方式加载html
            webView.loadData(mH5Param.getTargetUrl(), "text/html; charset=UTF-8", null);//这种写法可以正确解码
        }
    }

    /**
     * h5页面加载完成回调通知
     * @param webView
     * @param url
     */
    protected void onWebPageFinished(WebView webView, String url){}


    /**
     * 刷新webview
     */
    public void refresh() {
        if (webView != null) {
            webView.clearCache(true);
            webView.reload();
        }
    }

    /**
     * webview善后工作
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
           /* if (webView != null) {//解决Receiver not registered: android.widget.ZoomButtonsController
                webView.getSettings().setBuiltInZoomControls(true);
                webView.setVisibility(View.GONE);// 把destroy()延后
                long timeout = ViewConfiguration.getZoomControlsTimeout();
                System.out.println("time=="+timeout);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mWebView.destroy();
                    }
                }, timeout);
            }*/

            webView.clearHistory();
            webView.clearCache(true);
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }
    /**
     * 按下返回键
     */
    public void onBackPressed() {
        getActivity().setResult(Activity.RESULT_OK);
        ActivityUtil.finish(getActivity());
    }

}
