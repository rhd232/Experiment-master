package com.wind.view;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by wind on 2017/3/6.
 */

public class WdImageLoader {

    public static void display(Context context, ImageView iv, String url) {
        display(context, iv, url, R.drawable.placeholder_bg, true);
    }

    public static void display(Context context, ImageView iv, String url, boolean hasCache) {
        display(context, iv, url, R.drawable.placeholder_bg, hasCache);
    }

    public static void display(Context context, ImageView iv, String url, @DrawableRes int placeHolderRes) {
        display(context, iv, url, placeHolderRes, null, true);
    }

    public static void display(Context context, ImageView iv, String url, @DrawableRes int placeHolderRes, boolean hasCache) {
        display(context, iv, url, placeHolderRes, null, hasCache);
    }

    public static void display(Context context, ImageView iv, String url, @DrawableRes int placeHolderRes, ImageOption imageOption) {
        display(context, iv, url, placeHolderRes, imageOption, true);
    }

    public static void display(Context context, ImageView iv, String url, @DrawableRes int placeHolderRes, ImageOption imageOption, boolean hasCache) {
        if (url.startsWith("https://")) {
            url = "http://" + url.substring(8);
        }
        if (imageOption != null) {
            if (imageOption instanceof RoundedCornersOption) {
                RoundedCornersOption option = (RoundedCornersOption) imageOption;
                Glide.with(context)
                        .load(url)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .bitmapTransform(new CropSquareTransformation(context), new RoundedCornersTransformation(context, DisplayUtil.dip2px(context, 10), 0))
                        .placeholder(placeHolderRes)
                        .into(iv);
            } else if (imageOption instanceof BlurOption) {
                BlurOption option = (BlurOption) imageOption;
                if (option.isHasRounded()) {
                    Glide.with(context)
                            .load(url)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            //.bitmapTransform(new BlurTransformation(context, 25, 3), new CropSquareTransformation(context), new RoundedCornersTransformation(context, DisplayUtil.dip2px(context, 3), 0))
                            .bitmapTransform(new BlurTransformation(context, 25, 3), new CropSquareTransformation(context))
                            .placeholder(placeHolderRes)
                            .into(iv);
                } else {
                    Glide.with(context)
                            .load(url)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .bitmapTransform(new BlurTransformation(context, 25, 3), new CropSquareTransformation(context))
                            .placeholder(placeHolderRes)
                            .into(iv);
                }

            }
        } else {
            if (hasCache) {
                Glide.with(context)
                        .load(url)
                        .placeholder(placeHolderRes)
                        .into(iv);
            } else {
                Glide.with(context)
                        .load(url)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(placeHolderRes)
                        .into(iv);
            }

        }


    }


    public static void display(Context context, ImageView iv, @IdRes int resId) {
        display(context, iv, resId, 0);
    }

    public static void display(Context context, ImageView iv, @IdRes int resId, @DrawableRes int placeHolderRes) {
        Glide.with(context)
                .load(resId)
                .placeholder(placeHolderRes)
                .into(iv);
    }


    public static class ImageOption {
    }


    public static class RoundedCornersOption extends ImageOption {
        private int radius;

        public RoundedCornersOption(int radius) {
            this.radius = radius;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }
    }

    public static class BlurOption extends ImageOption {
        private boolean hasRounded;

        public BlurOption(boolean hasRounded) {
            this.hasRounded = hasRounded;
        }

        public boolean isHasRounded() {
            return hasRounded;
        }

        public void setHasRounded(boolean hasRounded) {
            this.hasRounded = hasRounded;
        }
    }
}