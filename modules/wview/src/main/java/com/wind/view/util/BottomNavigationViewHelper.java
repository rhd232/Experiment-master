package com.wind.view.util;

import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BottomNavigationViewHelper {

    public static void disableShiftMode(BottomNavigationView navigationView) {

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigationView.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);

            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);

                Field itemShiftingMode = itemView.getClass().getDeclaredField("mShiftingMode");
                itemShiftingMode.setAccessible(true);
                itemShiftingMode.setBoolean(itemView, false);
                itemShiftingMode.setAccessible(false);
                /*itemView.setShiftingMode(false);
                itemView.setChecked(itemView.getItemData().isChecked());*/
                Method setCheckMethod = itemView.getClass().getMethod("setChecked", boolean.class);
                setCheckMethod.invoke(itemView, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
