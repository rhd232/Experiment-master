package com.wind.base;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.wind.base.utils.LogUtil;
import com.wind.base.utils.WebViewHelper;
import com.wind.umengsharelib.ShareLayout;

import butterknife.ButterKnife;

/**
 * Created by wind on 2017/3/9.
 */

public abstract class AbsH5Activity extends BaseActivity {

    public static final String ARG_KEY_H5PARAM = "arg_key_h5param";
    protected H5Param mH5Param;
    protected WebView webView;
    protected ShareLayout mShareLayout;
    private WebViewHelper.ProtocolHandler mProtocolHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mH5Param = (H5Param) getIntent().getSerializableExtra(ARG_KEY_H5PARAM);
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
        View content = findViewById(R.id.webcontent);
        if (content != null) {
            if (!mH5Param.isTitlebarOverlay()) {
                ViewGroup.LayoutParams contentLayoutParams = content.getLayoutParams();
                FrameLayout.LayoutParams lp;
                if (contentLayoutParams instanceof FrameLayout.LayoutParams) {
                    lp = (FrameLayout.LayoutParams) contentLayoutParams;
                } else {
                    throw new IllegalArgumentException("webcontent's layoutparams must be FrameLayout.LayoutParams");
                }
//                  int size=getResources().getDimensionPixelSize(R.dimen.action_bar_height);
//                TypedValue tv = new TypedValue();
//                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//                    int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
//                    lp.topMargin = actionBarHeight;
//                }
                lp.topMargin = getResources().getDimensionPixelSize(R.dimen.action_bar_height);

            } else {
                mTitleBar.setLineVisibility(View.GONE);
                mTitleBar.setBackgroundColor(Color.TRANSPARENT);

            }
        }

        View view = findViewById(R.id.webView);
        webView = (WebView) view;
        mShareLayout = (ShareLayout) findViewById(R.id.share_layout);


        mProtocolHandler = getProtocolHandler();
        initView();
        if (mProtocolHandler == null) {
            registerWebHandler();
        }
        loadH5();
    }


    protected void registerWebHandler() {
    }

    /**
     * registerWebHandler互斥，当该方法返回为null时registerWebHandler才有效果
     *
     * @return
     */
    public WebViewHelper.ProtocolHandler getProtocolHandler() {
        return null;
    }

    public int getLayoutRes() {
        return R.layout.activity_h5;
    }

    @Override
    protected void setTitle() {
        super.setTitle();
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


    private void initView() {
        WebViewHelper.getInstance()
                .inflateWebView(this, webView, mProtocolHandler, new WebViewHelper.OnPageLoadingListener() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    }

                    @Override
                    public void onPageFinished(WebView webView, String url) {
                        LogUtil.e("onPageFinished", "url" + url);
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


    protected void onWebPageFinished(WebView webView, String url) {
    }


    public void refresh() {
        if (webView != null) {
            webView.clearCache(true);
            webView.reload();
        }
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

}
