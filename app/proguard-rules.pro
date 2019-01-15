# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# debugging stack traces.
-optimizationpasses 5
-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
-dontwarn com.alibaba.sdk.**
-keep class com.alivc.**
-dontwarn com.aliyun.demo.**
-dontwarn com.bumptech.glide.**
-dontwarn com.handmark.pulltorefresh.library.**
-dontwarn com.aliyun.vod.**
-dontwarn com.baidu.platform.**
#bean不能混淆
-keep class com.wind.data.** {*;}
-keep class com.wind.base.bean.** {*;}
-keep class com.wind.base.request.** {*;}
-keep class com.wind.base.response.** {*;}
############AndPermissions
-dontwarn com.yanzhenjie.permission.**
############Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
######## 云信
-dontwarn com.netease.**
-keep class com.netease.** {*;}
#如果你使用全文检索插件，需要加入
-dontwarn org.apache.lucene.**
-keep class org.apache.lucene.** {*;}


######## GalleryFinal
-keep class cn.finalteam.galleryfinal.widget.*{*;}
-keep class cn.finalteam.galleryfinal.widget.crop.*{*;}
-keep class cn.finalteam.galleryfinal.widget.zoonview.*{*;}


#==================rxjava==========================

-dontwarn rx.internal.**
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#fastjson
-keep public class * implements java.io.Serializable {
	   public *;
	}
		#-libraryjars libs/fastjson-1.1.40.jar
-keepclassmembers class * implements java.io.Serializable {
	    static final long serialVersionUID;
	    private static final java.io.ObjectStreamField[] serialPersistentFields;
	    private void writeObject(java.io.ObjectOutputStream);
	    private void readObject(java.io.ObjectInputStream);
	    java.lang.Object writeReplace();
	    java.lang.Object readResolve();
}

-dontwarn android.support.**
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }


#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **_ViewBinding { *; }
-keepclasseswithmembernames class * {
        @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
        @butterknife.* <methods>;
}

#eventbus
-keepattributes *Annotation*
-keepclassmembers class * {
       @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
   # Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
       <init>(java.lang.Throwable);
}

######################Pingpp开始
# Ping++ 混淆过滤
-keep class org.json.** {*;}
-dontwarn com.pingplusplus.**
-keep class com.pingplusplus.** {*;}

# 支付宝混淆过滤
-dontwarn com.alipay.**
-keep class com.alipay.** {*;}

# 微信或QQ钱包混淆过滤
-dontwarn  com.tencent.**
-keep class com.tencent.** {*;}

# 银联支付混淆过滤
-dontwarn  com.unionpay.**
-keep class com.unionpay.** {*;}

# 招行一网通混淆过滤
-keepclasseswithmembers class cmb.pb.util.CMBKeyboardFunc {
    public <init>(android.app.Activity);
    public boolean HandleUrlCall(android.webkit.WebView,java.lang.String);
    public void callKeyBoardActivity();
}

# 内部WebView混淆过滤
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
######################Pingpp结束



# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions

######################okhttp####################
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform


######################okio####################
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


#============UMENG======================#
-keep public class com.marryu.R$*{
public static final int *;
}
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep public class javax.**
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}


-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.umeng.weixin.handler.**
-keep class com.umeng.weixin.handler.*
-keep class com.umeng.qq.handler.**
-keep class com.umeng.qq.handler.*
-keep class UMMoreHandler{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
-keep class com.tencent.mm.sdk.** {
   *;
}
-keep class com.tencent.mm.opensdk.** {
   *;
}
-keep class com.tencent.wxop.** {
   *;
}
-keep class com.tencent.mm.sdk.** {
   *;
}
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep class com.kakao.** {*;}
-dontwarn com.kakao.**
-keep public class com.umeng.com.umeng.soexample.R$*{
    public static final int *;
}
-keep public class com.linkedin.android.mobilesdk.R$*{
    public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}
-keep class com.umeng.socialize.impl.ImageImpl {*;}
-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {
   *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class com.linkedin.** { *; }
-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
-keepattributes Signature


#趣拍
-ignorewarnings
-dontwarn okio.**
-dontwarn com.google.common.cache.**
-dontwarn java.nio.file.**
-dontwarn sun.misc.**
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep class okhttp3.** { *; }
-keep class com.bumptech.glide.integration.okhttp3.** { *; }
-keep class com.liulishuo.filedownloader.** { *; }
-keep class java.nio.file.** { *; }
-keep class sun.misc.** { *; }

-keep class com.qu.preview.** { *; }
-keep class com.qu.mp4saver.** { *; }
-keep class com.duanqu.transcode.** { *; }
-keep class com.duanqu.qupai.render.** { *; }
-keep class com.duanqu.qupai.player.** { *; }
-keep class com.duanqu.qupai.audio.** { *; }
-keep class com.aliyun.qupai.encoder.** { *; }
-keep class com.sensetime.stmobile.** { *; }
-keep class com.duanqu.qupai.yunos.** { *; }
-keep class com.aliyun.common.** { *; }
-keep class com.aliyun.jasonparse.** { *; }
-keep class com.aliyun.struct.** { *; }
-keep class com.aliyun.recorder.AliyunRecorderCreator { *; }
-keep class com.aliyun.recorder.supply.** { *; }
-keep class com.aliyun.querrorcode.** { *; }
-keep class com.qu.preview.callback.** { *; }
-keep class com.aliyun.qupaiokhttp.** { *; }
-keep class com.aliyun.crop.AliyunCropCreator { *; }
-keep class com.aliyun.crop.struct.CropParam { *; }
-keep class com.aliyun.crop.supply.** { *; }
-keep class com.aliyun.qupai.editor.pplayer.AnimPlayerView { *; }
-keep class com.aliyun.qupai.editor.impl.AliyunEditorFactory { *; }
-keep interface com.aliyun.qupai.editor.** { *; }
-keep interface com.aliyun.qupai.import_core.AliyunIImport { *; }
-keep class com.aliyun.qupai.import_core.AliyunImportCreator { *; }
-keep class com.aliyun.qupai.encoder.** { *; }
-keep class com.aliyun.leaktracer.** { *;}
-keep class com.duanqu.qupai.adaptive.** { *; }
-keep class com.aliyun.thumbnail.** { *;}
-keep class com.aliyun.demo.importer.media.MediaCache { *;}
-keep class com.aliyun.demo.importer.media.MediaDir { *;}
-keep class com.aliyun.demo.importer.media.MediaInfo { *;}
-keep class com.alivc.component.encoder.**{ *;}
-keep class com.aliyun.log.core.AliyunLogCommon { *;}
-keep class com.aliyun.log.core.AliyunLogger { *;}
-keep class com.aliyun.log.core.AliyunLogParam { *;}
-keep class com.aliyun.log.core.LogService { *;}
-keep class com.aliyun.log.struct.** { *;}
-keep class com.aliyun.demo.publish.SecurityTokenInfo { *; }

-keep class com.aliyun.vod.common.** { *; }
-keep class com.aliyun.vod.jasonparse.** { *; }
-keep class com.aliyun.vod.qupaiokhttp.** { *; }
-keep class com.aliyun.vod.log.core.AliyunLogCommon { *;}
-keep class com.aliyun.vod.log.core.AliyunLogger { *;}
-keep class com.aliyun.vod.log.core.AliyunLogParam { *;}
-keep class com.aliyun.vod.log.core.LogService { *;}
-keep class com.aliyun.vod.log.struct.** { *;}
-keep class com.aliyun.auth.core.**{*;}
-keep class com.aliyun.auth.common.AliyunVodHttpCommon{*;}
-keep class com.alibaba.sdk.android.vod.upload.exception.**{*;}
-keep class com.alibaba.sdk.android.vod.upload.auth.**{*;}
-keep class com.aliyun.auth.model.**{*;}
-keep class component.alivc.com.facearengine.** {*;}
-keep class com.aliyun.editor.NativeEditor{*;}
-keep class com.aliyun.nativerender.BitmapGenerator{*;}
-keep class com.aliyun.editor.EditorCallBack{*;}
-keep enum com.aliyun.editor.TimeEffectType{*;}
-keep enum com.aliyun.editor.EffectType{*;}
-keep class **.R$* { *; }
-keep class com.aliyun.svideo.sdk.internal.common.project.**{*;}
-keep class com.aliyun.svideo.sdk.external.struct.**{*;}
-keep class com.aliyun.sys.AlivcSdkCore{*;}

