package com.wind.umengsharelib;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMediaObject;

/**
 * Created by wind on 2018/3/21.
 */

public interface IShare {
    /**
     *
     * @param shareMedia 分享平台
     * @param mediaObject 分享内容
     */
    void startShare(SHARE_MEDIA shareMedia, UMediaObject mediaObject);
}
