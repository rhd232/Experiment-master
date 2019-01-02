package com.wind.base.utils;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by wind on 2017/3/2.
 */

public class AppCompat {
    public static final String TRANSITION_NAME_DEFAULT = "transition_name_default";

    public static void setTransitionName(View v, String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.setTransitionName(transitionName);
        }
    }



}
