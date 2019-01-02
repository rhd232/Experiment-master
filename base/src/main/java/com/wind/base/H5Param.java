package com.wind.base;

import com.wind.umengsharelib.bean.ShareInfo;

import java.io.Serializable;

/**
 * Created by wind on 2017/3/9.
 */

public class H5Param implements Serializable {
    private boolean titlebarOverlay;//
    private String targetUrl;
    private String title;
    private String rightBtnName;
    //返回到哪个activity
    private Class backTargetClass;

    private boolean startForResult;

    private boolean vipPay;
    private  boolean isOld;

    private int payCode;//支付的是哪个项目
    private ShareInfo shareInfo;

    public H5Param(){

    }
    public H5Param(String targetUrl, String title, String rightBtnName){
        this.targetUrl=targetUrl;
        this.title=title;
        this.rightBtnName=rightBtnName;
    }
    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public Class getBackTargetClass() {
        return backTargetClass;
    }

    public void setBackTargetClass(Class backTargetClass) {
        this.backTargetClass = backTargetClass;
    }

    public boolean isStartForResult() {
        return startForResult;
    }

    public void setStartForResult(boolean startForResult) {
        this.startForResult = startForResult;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRightBtnName() {
        return rightBtnName;
    }

    public void setRightBtnName(String rightBtnName) {
        this.rightBtnName = rightBtnName;
    }


    public static H5Param obtain(String url,String title,String rightBtnName){
        return new H5Param(url,title,rightBtnName);
    }

    public boolean isVipPay() {
        return vipPay;
    }

    public void setVipPay(boolean vipPay) {
        this.vipPay = vipPay;
    }

    public boolean isOld() {
        return isOld;
    }

    public void setOld(boolean old) {
        isOld = old;
    }

    public ShareInfo getShareInfo() {
        return shareInfo;
    }

    public void setShareInfo(ShareInfo shareInfo) {
        this.shareInfo = shareInfo;
    }

    public int getPayCode() {
        return payCode;
    }

    public void setPayCode(int payCode) {
        this.payCode = payCode;
    }

    public boolean isTitlebarOverlay() {
        return titlebarOverlay;
    }

    public void setTitlebarOverlay(boolean titlebarOverlay) {
        this.titlebarOverlay = titlebarOverlay;
    }
}
