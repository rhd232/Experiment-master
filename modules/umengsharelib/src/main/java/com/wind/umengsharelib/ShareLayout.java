package com.wind.umengsharelib;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.wind.umengsharelib.bean.ShareInfo;

//import io.github.zhitaocai.toastcompat.util.ToastUtil;

/**
 * Created by wind on 16/6/2.
 */
public class ShareLayout extends FrameLayout implements View.OnClickListener {


    ImageView iv_share_copy;
    private String shareUrl;

    public ShareLayout(Context context) {
        super(context);
        init(context);
    }

    public ShareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShareLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {


        View view = inflate(context, R.layout.share_layout, this);
        initView(view);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    private void initView(View view) {
        iv_share_copy = (ImageView) view.findViewById(R.id.iv_share_copy);
        view.findViewById(R.id.iv_share_wx).setOnClickListener(this);
        view.findViewById(R.id.iv_share_wx_circle).setOnClickListener(this);
        view.findViewById(R.id.iv_share_qq).setOnClickListener(this);
        view.findViewById(R.id.iv_share_qq_zone).setOnClickListener(this);
        view.findViewById(R.id.iv_share_sina_weibo).setOnClickListener(this);
        view.findViewById(R.id.iv_share_copy).setOnClickListener(this);
        view.findViewById(R.id.iv_share_cancel).setOnClickListener(this);
    }


    public void onClick(View v) {
        setVisibility(GONE);
        int vid = v.getId();

        if (vid == R.id.iv_share_wx) {
            startShare(SHARE_MEDIA.WEIXIN, mUMImage, mTitle, mWxqqcontent, mTargetUrl,umWeb);

        } else if (vid == R.id.iv_share_wx_circle) {
            startShare(SHARE_MEDIA.WEIXIN_CIRCLE, mUMImage, mTitle, mWxcircleqzonesinacontent, mTargetUrl,umWeb);
        } else if (vid == R.id.iv_share_qq) {
            startShare(SHARE_MEDIA.QQ, mUMImage, mTitle, mWxqqcontent, mTargetUrl,umWeb);
        } else if (vid == R.id.iv_share_qq_zone) {
            startShare(SHARE_MEDIA.QZONE, mUMImage, mTitle, mWxcircleqzonesinacontent, mTargetUrl,umWeb);
        } else if (vid == R.id.iv_share_sina_weibo) {
            startShare(SHARE_MEDIA.SINA, mUMImage, mTitle, mWxcircleqzonesinacontent, mTargetUrl,umWeb);
        } else if (vid == R.id.iv_share_copy) {
            //将分享链接拷贝到剪切板中
            ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clipData = ClipData.newRawUri("shareUrl", Uri.parse(mTargetUrl));
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(getContext(), "分享链接已复制到剪贴板",Toast.LENGTH_SHORT).show();
        }

    }

    private void startShare(SHARE_MEDIA share_media, UMImage image, String title, String content, String targetUrl, UMWeb umWeb) {
        try {
            Log.e("ShareLayout","mTitle:"+title+"---content:"+content);
            ShareAction shareAction = new ShareAction((Activity) getContext());
           /* if (!TextUtils.isEmpty(content))
                shareAction.withText(content);
            if (image!=null)
                shareAction.withMedia(image);*/
            if(umWeb !=null)
                shareAction.withMedia(umWeb);


            shareAction.setPlatform(share_media).setCallback(umShareListener).share();

            // sendPost(share_media);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setCopyBtn(boolean showCopyBtn) {
        if (showCopyBtn) {
            iv_share_copy.setVisibility(VISIBLE);
        } else {
            iv_share_copy.setVisibility(GONE);
        }
    }

    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            Log.d("plat", "platform" + platform);

            //Toast.makeText(MainActivity.this, platform + " 分享成功啦", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
           // ToastUtil.showToast(getContext(),platform + " 分享失败啦"+t.getMessage());
            if (t != null) {
                Log.d("yuelan", "throw:" + t.getMessage());
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            //Toast.makeText(MainActivity.this,platform + " 分享取消了", Toast.LENGTH_SHORT).show();
        }
    };

    private void sendPost(SHARE_MEDIA share_media) {
        String type = "";
        if (share_media == SHARE_MEDIA.WEIXIN) {
            type = "weixin";
        } else if (share_media == SHARE_MEDIA.WEIXIN_CIRCLE) {
            type = "weixin_circle";
        } else if (share_media == SHARE_MEDIA.QQ) {
            type = "qq";
        } else if (share_media == SHARE_MEDIA.QZONE) {
            type = "qzone";
        } else if (share_media == SHARE_MEDIA.SINA) {
            type = "sina";
        }
        /*OkHttpUtils.post()
                .url(MyApi.getUrl("/user/user_share"))
                .addParams("token", MYApplication.getInstance().getLoginUser().getToken())
                .addParams("version", MYApi.getApiVersion())
                .addParams("source", shareType)
                .addParams("source_id", sourceId)
                .addParams("type", type)
                .build().execute(null);*/
    }


    private UMImage mUMImage;
    private String mTitle;
    private String mTargetUrl;
    private String mWxqqcontent;
    private String mWxcircleqzonesinacontent;

    private String shareType;
    private String sourceId;

    private  UMWeb umWeb;
    public void setContent(UMImage urlImage, String title, String targetUrl,
                           String wxqqcontent, String wxcircleqzonesinacontent,
                           String shareType, String sourceId,UMWeb umWeb) {
        this.mUMImage = urlImage;
        this.mTitle = title;
        this.mTargetUrl = targetUrl;
        this.mWxqqcontent = wxqqcontent;
        this.mWxcircleqzonesinacontent = wxcircleqzonesinacontent;

        this.shareType = shareType;
        this.sourceId = sourceId;
        this.umWeb = umWeb;
    }

    public void setContent(UMImage urlImage){
        setContent(urlImage,"","","","","","",umWeb);
    }
    public void setShareInfo(ShareInfo shareInfo) {
        if (shareInfo==null){
            return;
        }

        //mUMImage = new UMImage(getContext(), shareInfo.getIconUrl());
        if (TextUtils.isEmpty(shareInfo.getIconUrl())){
            mUMImage = new UMImage(getContext(), R.drawable.app_logo);
        }else {
            mUMImage = new UMImage(getContext(), shareInfo.getIconUrl());
        }

        mTitle = shareInfo.getTitle();
        mTargetUrl = shareInfo.getShareUrl();
        mWxcircleqzonesinacontent = shareInfo.getContent();
        mWxqqcontent = shareInfo.getContent();

        umWeb=new UMWeb(mTargetUrl);
       /* UMWeb urlImage = new UMWeb(
                "http://www.baidu.com");*/
        umWeb.setTitle(mTitle);
        umWeb.setDescription(mWxqqcontent);
        if (TextUtils.isEmpty(shareInfo.getIconUrl())){
           // mUMImage = new UMImage(getContext(), R.drawable.app_logo);
            umWeb.setThumb(new UMImage(getContext(),R.drawable.app_logo));
        }else {
           // mUMImage = new UMImage(getContext(), shareInfo.getIconUrl());
            umWeb.setThumb(new UMImage(getContext(), shareInfo.getIconUrl()));
        }


    }

}
