package com.wind.umengsharelib.bean;

import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * Created by wind on 2016/11/8.
 */

public class ShareInfo implements Serializable {

    private String id;        // 分享的商品id;
    private String spell_id;  // 分享的商品的拼团id
    private String title;
    @JSONField(name = "desc")
    private String content;
    @JSONField(name = "pic")
    private String image_url;
    @JSONField(name = "linkUrl")
    private String share_url;   // 分享链接，需要拼接商品id和拼团id
    @JSONField(name = "snapUrl")
    private String snapshootUrl;
    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setIconUrl(String iconUrl) {
        this.image_url = iconUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.share_url = shareUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getIconUrl() {
        return image_url;
    }

    public String getShareUrl() {
        return share_url ;
    }

    public String getShare_url() {
        return share_url + "?id=" + id
                + (
                TextUtils.isEmpty(spell_id) ? "" : ("&spell_id=" + spell_id)
        );
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getSnapshootUrl() {
        return snapshootUrl+"?id="+id + (
                TextUtils.isEmpty(spell_id) ? "" : ("&spell_id=" + spell_id));
    }

    public void setSnapshootUrl(String snapshootUrl) {
        this.snapshootUrl = snapshootUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpell_id() {
        return spell_id;
    }

    public void setSpell_id(String spell_id) {
        this.spell_id = spell_id;
    }
}
