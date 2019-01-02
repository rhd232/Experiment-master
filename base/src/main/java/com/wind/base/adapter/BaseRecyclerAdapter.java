package com.wind.base.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wind on 2018/1/2.
 */

public abstract class BaseRecyclerAdapter<T, H extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<H> {

    protected List<T> items;
    protected Activity mActivity;
    private int mLayoutRes;

    public BaseRecyclerAdapter(Activity activity, int layoutRes) {
        this.mActivity = activity;
        this.items = new ArrayList<>();
        this.mLayoutRes = layoutRes;
    }

    @Override
    public H onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mActivity.getLayoutInflater().inflate(mLayoutRes, parent, false);
        return onCreateViewHolder(view);
    }

    public abstract H onCreateViewHolder(View v);

    public T getItem(int position) {
        return items.get(position);
    }

    public List<T> getItems(){
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void replace(List<T> items) {
        this.items.clear();
        addAll(items);
    }

    public void replace(T item) {
        this.items.clear();
        add(item);
    }

    public void addAll(List<T> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void add(int position,T item) {
        this.items.add(position,item);
        notifyDataSetChanged();
    }

    public void add(T item) {
        this.items.add(item);
        notifyDataSetChanged();
    }
    public void clear(){
        this.items.clear();
        notifyDataSetChanged();
    }
    public void remove(int positon){
        this.items.remove(positon);
        notifyDataSetChanged();
    }

}
