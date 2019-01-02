package com.wind.base.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wind.base.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wind on 2017/12/6.
 */

public class WebViewHelper {
    public static final String HTTP_SCHEME = "http";
    public static final String TEL_SCHEME = "tel";
    private static WebViewHelper instance;
    private WebHandler mDefaultHandler;

    private WebViewHelper() {
        mHandlerMap = new HashMap<>();
        mDefaultHandler = new WebHandler() {
            @Override
            public void handle(WebView webView, Uri uri) {
                webView.loadUrl(uri.toString());
            }
        };

    }

    public static WebViewHelper getInstance() {
       /* if (instance == null) {
            synchronized (WebViewHelper.class) {
                if (instance == null) {
                    instance = new WebViewHelper();
                }
            }
        }
        return instance;*/
        return new WebViewHelper();
    }

    private static void fixDirPath() {
        String path = ImageUtil.getWebViewUploadDirPath();

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private static final int REQUEST_CODE_PICK_IMAGE = 0;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private static ValueCallback<Uri> mUploadMsg;
    private static ValueCallback<Uri[]> mUploadMsgLollipop;
    private static Intent mSourceIntent;

    private static void showOptions(final Activity context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setOnCancelListener(new ReOnCancelListener());
        alertDialog.setTitle("操作");
        alertDialog.setItems(R.array.webview_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mSourceIntent = ImageUtil.choosePicture();
                            context.startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);
                        } else {
                            mSourceIntent = ImageUtil.takeBigPicture();
                            context.startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);
                        }
                    }
                }
        );
        alertDialog.show();
    }

    public interface WebHandler {
        void handle(WebView view, Uri uri);
    }

    private Map<String, Map<String, WebHandler>> mHandlerMap;

    private String mScheme;


    public WebViewHelper setCurrentProtocol(String scheme) {
        mScheme = scheme;
        return this;
    }

    public WebViewHelper registerHandler(String name, WebHandler handler) {
        return registerHandler(mScheme, name, handler);
    }

    public WebViewHelper registerHandler(String scheme, String name, WebHandler handler) {
        Map<String, WebHandler> schemeMap = mHandlerMap.get(scheme);
        if (schemeMap == null) {
            schemeMap = new HashMap<>();
            schemeMap.put(name, handler);
            mHandlerMap.put(scheme, schemeMap);
        } else {
            if (schemeMap.containsKey(name)) {
                throw new RuntimeException("already register name:" + name);
            } else
                schemeMap.put(name, handler);
        }
        return this;
    }

    public void inflateWebView(final Activity activity, WebView webView) {
        inflateWebView(activity, webView, null, null);
    }

    public void inflateWebView(final Activity activity, WebView webView, final OnPageLoadingListener onPageLoadingListener) {
        inflateWebView(activity, webView, null, onPageLoadingListener);
    }

    public void inflateWebView(final Activity activity, WebView webView, final ProtocolHandler protocolHandler) {
        inflateWebView(activity, webView, protocolHandler, null);
    }

    public void inflateWebView(final Activity activity, final WebView webView, final ProtocolHandler protocolHandler, final OnPageLoadingListener onPageLoadingListener) {
        fixDirPath();
        //屏蔽掉长按事件 因为webview长按时将会调用系统的复制控件:
        webView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setWebChromeClient(new WebViewHelper.ReWebChomeClient(new WebViewHelper.OpenFileChooserCallBack() {
            @Override
            public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMsg = uploadMsg;
                showOptions(activity);
            }

            @Override
            public void onShowFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    mUploadMsgLollipop = filePathCallback;
                    showOptions(activity);

                }
            }

        }));
        //防止url在浏览器中打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();//接受证书
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                //uri的构成->scheme://authority/path1/path2/path3?query#fragment
                if (protocolHandler != null) {
                    protocolHandler.handle(url);
                } else {

                    Uri uri = Uri.parse(url);
                    String scheme = uri.getScheme();
                    String path = uri.getPath();
                    Map<String, WebHandler> handlerMap = mHandlerMap.get(scheme);
                    if (handlerMap != null) {
                        WebHandler handler = handlerMap.get(path);
                        if (handler != null) {
                            handler.handle(webView, uri);
                        } else {
                            mDefaultHandler.handle(webView, uri);
                        }
                    } else {
                        mDefaultHandler.handle(webView, uri);
                    }
                }

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (onPageLoadingListener != null) {
                    onPageLoadingListener.onPageFinished(view, url);
                }

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (onPageLoadingListener != null) {
                    onPageLoadingListener.onPageStarted(view, url, favicon);
                }
            }
        });
        //Android4.4中WebView无法显示图片解决方案
        //启用js
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkImage(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//android5.0以下都是允许的
            /*
               1,MIXED_CONTENT_NEVER_ALLOW：Webview不允许一个安全的站点（https）去加载非安全的站点内容（http）,
               比如，https网页内容的图片是http链接。强烈建议App使用这种模式，因为这样更安全。
                2,MIXED_CONTENT_ALWAYS_ALLOW：在这种模式下，WebView是可以在一个安全的站点（Https）
                里加载非安全的站点内容（Http）,这是WebView最不安全的操作模式，尽可能地不要使用这种模式。
                3,MIXED_CONTENT_COMPATIBILITY_MODE：在这种模式下，当涉及到混合式内容时,WebView会尝试去兼容最新Web浏览器的风格。
                一些不安全的内容（Http）能被加载到一个安全的站点上（Https），而其他类型的内容将会被阻塞。这些内容的类型是被允许加载还是被阻塞可能会随着版本的不同而改变，并没有明确的定义。这种模式主要用于在App里面不能控制内容的渲染，但是又希望在一个安全的环境下运行。
             */
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.getSettings().setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        webView.getSettings().setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
        webView.getSettings().setAppCacheEnabled(true);//是否使用缓存
        webView.getSettings().setDomStorageEnabled(true);//DOM Storage
        // 设置4.2以后版本支持autoPlay，非用户手势促发
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

    }

    public interface OnPageLoadingListener {
        void onPageStarted(WebView view, String url, Bitmap favicon);

        void onPageFinished(WebView webView, String url);
    }

    public interface ProtocolHandler {
        void handle(String url);
    }

    public static class ReWebChomeClient extends WebChromeClient {

        private OpenFileChooserCallBack mOpenFileChooserCallBack;

        public ReWebChomeClient(OpenFileChooserCallBack openFileChooserCallBack) {
            mOpenFileChooserCallBack = openFileChooserCallBack;
        }

        //For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mOpenFileChooserCallBack.openFileChooserCallBack(uploadMsg, acceptType);
        }

        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        // For Android  > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg, acceptType);
        }

        //For Android 5.0+
        @Override
        @SuppressLint("NewApi")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mOpenFileChooserCallBack.onShowFileChooser(filePathCallback, fileChooserParams);
            return true;
        }


    }

    public interface OpenFileChooserCallBack {
        void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType);

        void onShowFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams);
    }

    private static class ReOnCancelListener implements DialogInterface.OnCancelListener {

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            //重要，否则上传照片按钮只能点击一次。
            if (mUploadMsg != null) {
                mUploadMsg.onReceiveValue(null);
                mUploadMsg = null;
            }
            if (mUploadMsgLollipop != null) {
                mUploadMsgLollipop.onReceiveValue(null);
                mUploadMsgLollipop = null;
            }
        }
    }
}
