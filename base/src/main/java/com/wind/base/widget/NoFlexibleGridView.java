package com.wind.base.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 不伸缩的gridview
 * 解决在scrollView中嵌套GridView时，GridView中的内容展示不全的问题
 * @author jxkj-sh
 *
 */
public class NoFlexibleGridView extends GridView{

	public NoFlexibleGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public NoFlexibleGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoFlexibleGridView(Context context) {
		super(context);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		 int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
