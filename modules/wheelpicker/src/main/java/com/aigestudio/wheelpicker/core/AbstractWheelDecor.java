package com.aigestudio.wheelpicker.core;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * 选中项装饰物
 */
public abstract class AbstractWheelDecor {
    public abstract void drawDecor(Canvas canvas, Rect rectLast, Rect rectNext, Paint paint);
}