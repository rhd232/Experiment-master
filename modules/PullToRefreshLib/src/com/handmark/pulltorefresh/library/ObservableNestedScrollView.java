package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;

public class ObservableNestedScrollView extends NestedScrollView {
	private ScrollViewListener scrollViewListener = null;

	public ObservableNestedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setScrollViewListener(ScrollViewListener scrollViewListener) {
		this.scrollViewListener = scrollViewListener;
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		if (scrollViewListener != null) {
			scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
		}
	}

	public interface ScrollViewListener {
		void onScrollChanged(ObservableNestedScrollView scrollView, int x, int y, int oldx, int oldy);
	}

	/*@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
		return 0;
	}*/
}
