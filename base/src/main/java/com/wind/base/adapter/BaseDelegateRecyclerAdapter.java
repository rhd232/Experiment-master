package com.wind.base.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wind on 2018/1/2.
 */

public abstract class BaseDelegateRecyclerAdapter extends RecyclerView.Adapter {

    protected AdapterDelegatesManager<List<DisplayItem>> manager;
    protected List<DisplayItem> items;
    protected Activity mActivity;
    public BaseDelegateRecyclerAdapter(Activity activity){
        manager=new AdapterDelegatesManager<>();
        this.items=new ArrayList<>();
        this.mActivity=activity;
        addDelegate();
    }

    protected abstract void addDelegate();


    @Override
    public int getItemViewType(int position) {
        return manager.getItemViewType(items,position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return manager.onCreateViewHolder(parent,viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        manager.onBindViewHolder(items,position,holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<DisplayItem> getItems() {
        return items;
    }

    public void clear(){
        this.items.clear();
        notifyDataSetChanged();
    }
    public void replace(DisplayItem item) {
        this.items.clear();
        add(item);
    }
    public void remove(int position) {
       this.items.remove(position);
       notifyDataSetChanged();
    }
    public void add(int position, DisplayItem item) {
        this.items.add(position,item);
        notifyDataSetChanged();
    }
    public void replace(List<DisplayItem> items) {
        this.items.clear();
        addAll(items);
    }
    public void add(DisplayItem item){
        this.items.add(item);
        notifyDataSetChanged();
    }
    public void addAll(List<DisplayItem> items){


        this.items.addAll(items);


        notifyDataSetChanged();
    }

    public DisplayItem getItem(int position){
        return items.get(position);
    }

    public int getPosition(DisplayItem target){
        for (int i=0;i<items.size();i++){
            DisplayItem item=items.get(i);
            if (item==target){
                return i;
            }
        }
        return -1;
    }

}
