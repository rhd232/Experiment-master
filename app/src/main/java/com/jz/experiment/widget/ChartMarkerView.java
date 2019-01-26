package com.jz.experiment.widget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.jz.experiment.R;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;
import com.wind.base.utils.AppUtil;
import com.wind.base.utils.DisplayUtil;
import com.wind.data.expe.bean.ColorfulEntry;

public class ChartMarkerView extends MarkerView {
    ListView lv;
    LinearLayout ll_container;
    MarkerAdapter mAdapter;
    int screenWidth;
    int screenHeight;
    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context the layout resource to use for the MarkerView
     */
    public ChartMarkerView(Context context, OnPointSelectedListener listener) {
        super(context, R.layout.layout_markerview);
        this.listener = listener;
        lv = findViewById(R.id.lv);
        ll_container = findViewById(R.id.ll_container);
        mAdapter=new MarkerAdapter(context,R.layout.item_markerview);
        lv.setAdapter(mAdapter);

        screenWidth=AppUtil.getScreenWidth((Activity) context);
        screenHeight=AppUtil.getScreenHeight((Activity) context);
    }


    @Override
    public void refreshContent(final Entry e,final Highlight highlight) {
        //yValue.setText(e.getY()+"");
        //根据此x值获取其他折线的y值
        listener.onPointSelected(e);
        ll_container.removeAllViews();
        for (int i=0;i<mAdapter.getCount();i++){
            ColorfulEntry item=mAdapter.getItem(i);
            View view=LayoutInflater.from(getContext()).inflate(R.layout.item_markerview,ll_container,false);
            TextView tv_xvalue=view.findViewById(R.id.tv_xvalue);
            TextView tv_yvalue=view.findViewById(R.id.tv_yvalue);
            View indicator=view.findViewById(R.id.indicator);
            tv_xvalue.setText(item.getEntry().getX()+"");
            tv_yvalue.setText(item.getEntry().getY()+"");
            indicator.setBackgroundColor(item.getColor());

            ll_container.addView(view);
        }
        super.refreshContent(e, highlight);




    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        MPPointF offset = getOffset();
        // posY \posX 指的是markerView左上角点在图表上面的位置
        //y轴上markerview超出界面
        if (posY+getHeight()>screenHeight-DisplayUtil.dip2px(getContext(),60)){
            offset.y=-(posY+getHeight()-(screenHeight-DisplayUtil.dip2px(getContext(),60)));
        }
        if (posX<0){
            offset.x=0;
        }

        if (posX+getWidth()>screenWidth){
            offset.x=-(posX+getWidth()-screenWidth);
        }
        return offset;
    }

    public OnPointSelectedListener listener;

    public void setOnPointSelectedListener(OnPointSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnPointSelectedListener {
        void onPointSelected(Entry e);
    }

    public QuickAdapter getAdapter(){
        return mAdapter;
    }

    static class MarkerAdapter extends QuickAdapter<ColorfulEntry>{

        public MarkerAdapter(Context context, int layoutResId) {
            super(context, layoutResId);
        }

        @Override
        protected void convert(BaseAdapterHelper helper, ColorfulEntry item) {
            helper.setText(R.id.tv_xvalue,item.getEntry().getX()+"");
            helper.setText(R.id.tv_yvalue,item.getEntry().getY()+"");

            helper.getView(R.id.indicator).setBackgroundColor(item.getColor());


        }
    }
}
