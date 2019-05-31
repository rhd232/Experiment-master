package com.anitoa.listener;


import com.anitoa.bean.Data;

public interface AnitoaConnectionListener {

    void onConnectSuccess();
    void onConnectCancel();
    void onDoThing();
    void onReceivedData(Data data);
}
