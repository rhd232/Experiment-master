package com.jz.experiment.widget;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.jz.experiment.R;

public class ChartMarkerView extends MarkerView {
    TextView yValue;
    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * the layout resource to use for the MarkerView
     */
    public ChartMarkerView(Context context){
        super(context, R.layout.layout_markerview);

        yValue=findViewById(R.id.tv_yvalue);
    }


    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        yValue.setText(e.getY()+"");
        super.refreshContent(e, highlight);

    }
}
