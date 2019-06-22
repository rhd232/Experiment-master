package com.jz.experiment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("BootReceiver:"+intent.getAction());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
           Intent splashI=new Intent(context,SplashActivity.class);
           splashI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           context.startActivity(splashI);
        }
    }
}
