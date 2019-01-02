/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class PullToRefreshRecyclerView extends PullToRefreshBase<RecyclerView> {

	public PullToRefreshRecyclerView(Context context) {
		super(context);
	}

	public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshRecyclerView(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshRecyclerView(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}

	@Override
	public final Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
		//RecyclerView scrollView;
	/*	if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			scrollView = new InternalRecyclerViewSDK9(context, attrs);
		} else {
			scrollView = new RecyclerView(context, attrs);
		}
*/     RecyclerView scrollView = new RecyclerView(context, attrs);
		scrollView.setId(R.id.scrollview);
		return scrollView;
	}

	@Override
	protected boolean isReadyForPullStart() {
		RecyclerView.LayoutManager layoutManager=mRefreshableView.getLayoutManager();
		if (layoutManager instanceof StaggeredGridLayoutManager) {
			StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) mRefreshableView.getLayoutManager();
			int column = lm.getColumnCountForAccessibility(null, null);
			int positions[] = new int[column];
			lm.findFirstVisibleItemPositions(positions);
			for (int i = 0; i < positions.length; i++) {
				/**
				 * 判断lastItem的底边到recyclerView顶部的距离
				 * 是否小于recyclerView的高度
				 * 如果小于或等于 说明滚动到了底部
				 */
				// 刚才忘了写判断是否是最后一个item了
				if (positions[i] <= (lm.getItemCount() - column)
						&& lm.findViewByPosition(positions[i]).getTop() >= 0) {

					return true;
				}

			}
		}else if (layoutManager instanceof LinearLayoutManager){
			LinearLayoutManager llm= (LinearLayoutManager) layoutManager;
			int position=llm.findFirstVisibleItemPosition();
			if (position==0){
				View childView=llm.findViewByPosition(llm.findFirstVisibleItemPosition());
				int childTop=childView.getTop();
				int parentTop=mRefreshableView.getTop();
				Log.e("top","childTop:"+childTop+"  parentTop:"+parentTop);
				if (childTop>=parentTop){
					return true;
				}
			}


		}
		return false;
	}

	@Override
	protected boolean isReadyForPullEnd() {
		RecyclerView.LayoutManager layoutManager=mRefreshableView.getLayoutManager();
		if (layoutManager instanceof StaggeredGridLayoutManager) {
			// 获取布局管理器
			StaggeredGridLayoutManager layout =
					(StaggeredGridLayoutManager) mRefreshableView.getLayoutManager();
			// 用来记录lastItem的position
			// 由于瀑布流有多个列 所以此处用数组存储
			int column = layout.getColumnCountForAccessibility(null, null);
			int positions[] = new int[column];
			// 获取lastItem的positions
			layout.findLastVisibleItemPositions(positions);

			ArrayList<Boolean> booleanArrayList = new ArrayList<Boolean>();
			for (int i = 0; i < positions.length; i++) {
				/**
				 * 判断lastItem的底边到recyclerView顶部的距离
				 * 是否小于recyclerView的高度
				 * 如果小于或等于 说明滚动到了底部
				 */
				// 刚才忘了写判断是否是最后一个item了
				int count = layout.getItemCount() - column;
				View child = layout.findViewByPosition(positions[i]);
				if (child == null) {

					//return false;
					booleanArrayList.add(false);
					continue;
				}
				int childBottom = child.getBottom();
				int rvBottom = mRefreshableView.getHeight();
				//Log.e("isReadyForPullEnd","positions[i] "+positions[i] +"count:"+count+"  childBottom:"+childBottom+"  rvBottom:"+rvBottom);
				if (positions[i] >= count
						&& childBottom <= rvBottom) {
					/**
					 * 此处实现你的业务逻辑
					 */
					//return true;
					booleanArrayList.add(true);
				} else {
					booleanArrayList.add(false);
				}

			}
			for (Boolean flag : booleanArrayList) {
				if (!flag) {
					return false;
				}
			}

			return true;
		}else if (layoutManager instanceof LinearLayoutManager){
			LinearLayoutManager llm= (LinearLayoutManager) layoutManager;
			int position=llm.findLastVisibleItemPosition();
			if (position==mRefreshableView.getAdapter().getItemCount()-1){
				int lastVisibleItemPosition=llm.findLastVisibleItemPosition();
				if (lastVisibleItemPosition!= RecyclerView.NO_POSITION){
					int childBottom=llm.findViewByPosition(lastVisibleItemPosition).getBottom();
					int rvBottom = mRefreshableView.getHeight();
					if (childBottom<=rvBottom){
						return true;
					}
				}
			}


		}


		return false;
	}

	@TargetApi(9)
	final class InternalRecyclerViewSDK9 extends RecyclerView {

		public InternalRecyclerViewSDK9(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
				int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

			final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
					scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

			// Does all of the hard work...
			OverscrollHelper.overScrollBy(PullToRefreshRecyclerView.this, deltaX, scrollX, deltaY, scrollY,
					getScrollRange(), isTouchEvent);

			return returnValue;
		}

		/**
		 * Taken from the AOSP ScrollView source
		 */
		private int getScrollRange() {
			int scrollRange = 0;
			if (getChildCount() > 0) {
				View child = getChildAt(0);
				scrollRange = Math.max(0, child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
			}
			return scrollRange;
		}
	}

	public void setAdapter(RecyclerView.Adapter adapter){
		mRefreshableView.setAdapter(adapter);
	}

}
