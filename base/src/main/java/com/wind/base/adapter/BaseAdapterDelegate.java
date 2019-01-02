package com.wind.base.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates3.AdapterDelegate;

import java.util.List;

/**
 * Created by wind on 2018/10/11.
 */

public abstract class BaseAdapterDelegate<VH extends RecyclerView.ViewHolder> extends AdapterDelegate<List<DisplayItem>> {

    protected Activity mActivity;
    private int mLayoutRes;
    public BaseAdapterDelegate(Activity activity,int layoutRes){
        mActivity=activity;
        this.mLayoutRes=layoutRes;
    }
    /*@Override
    protected boolean isForViewType(@NonNull List<DisplayItem> items, int position) {

        return false;
    }*/

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View itemView=mActivity.getLayoutInflater().inflate(mLayoutRes,parent,false);
        return onCreateViewHolder(itemView);
    }

    protected abstract VH onCreateViewHolder(View itemView);


}
