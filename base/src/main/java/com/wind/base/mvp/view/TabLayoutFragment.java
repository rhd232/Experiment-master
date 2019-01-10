package com.wind.base.mvp.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wind.base.R;
import com.wind.base.utils.DisplayUtil;
import com.wind.view.NestedTabLayout;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by wind on 2018/3/14.
 */

public abstract class TabLayoutFragment
        extends Fragment {

    protected TabLayout layout_tab;
    protected ViewPager view_pager;

    protected ContainerPagerAdapter mFragmentAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View contentParent = inflater.inflate(getLayoutRes(), container, false);
        return contentParent;
    }

    public abstract int getLayoutRes();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ButterKnife.bind(this,view);
        layout_tab = view.findViewById(R.id.layout_tab);
        view_pager = view.findViewById(R.id.layout_view_pager);
        mFragmentAdapter = new ContainerPagerAdapter(getChildFragmentManager(),
                getFragments(), getTitles());
        view_pager.setAdapter(mFragmentAdapter);

        layout_tab.setupWithViewPager(view_pager);

        /*layout_tab.post(new Runnable() {
            @Override
            public void run() {
                setUpIndicatorWidth(layout_tab, 0, 0);
            }
        });
        layout_tab.setTabMode(TabLayout.MODE_SCROLLABLE);*/
        layout_tab.setTabMode(TabLayout.MODE_FIXED);
    }

    protected abstract List<Fragment> getFragments();

    public List<String> getTitles() {
        return null;
    }

    public static class ContainerPagerAdapter extends FragmentPagerAdapter {

        public List<Fragment> fragments;
        private List<String> titles;

        public ContainerPagerAdapter(FragmentManager fm,
                                     List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        public ContainerPagerAdapter(FragmentManager fm,
                                     List<Fragment> fragments,
                                     List<String> titles) {
            this(fm, fragments);
            this.titles = titles;
        }
        public List<String>  getTitles(){
            return titles;
        }
        public List<Fragment> getFragments(){
            return fragments;
        }
        public void setFragments(List<Fragment> fragments) {
            this.fragments = fragments;
        }

        public void setTitles(List<String> titles) {
            this.titles = titles;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {

            return fragments==null?0:fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (titles == null) {
                return super.getPageTitle(position);
            } else {
                return titles.get(position);
            }
        }
    }

    private void setUpIndicatorWidth(TabLayout tabLayout, int marginLeft, int marginRight) {
        /*Class<?> tabLayoutClass = tabLayout.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tabLayoutClass.getDeclaredField("mTabStrip");
            tabStrip.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        LinearLayout layout = null;
        try {
            if (tabStrip != null) {
                layout = (LinearLayout) tabStrip.get(tabLayout);
            }
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setPadding(0, 0, 0, 0);
                //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.setMarginStart(DisplayUtil.dip2px(getActivity(), marginLeft));
                    params.setMarginEnd(DisplayUtil.dip2px(getActivity(), marginRight));
                }
                child.setLayoutParams(params);
                child.invalidate();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }*/

            try {
                Field mTabStripField;
                if (tabLayout instanceof NestedTabLayout){
                    mTabStripField = tabLayout.getClass().getSuperclass().getDeclaredField("mTabStrip");
                }else {
                    //拿到tabLayout的mTabStrip属性
                    mTabStripField = tabLayout.getClass().getDeclaredField("mTabStrip");
                }

                mTabStripField.setAccessible(true);

                LinearLayout mTabStrip = (LinearLayout) mTabStripField.get(tabLayout);

                int dp10 = DisplayUtil.dip2px(getContext(), 10);

                for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                    View tabView = mTabStrip.getChildAt(i);

                    //拿到tabView的mTextView属性
                    Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                    mTextViewField.setAccessible(true);

                    TextView mTextView = (TextView) mTextViewField.get(tabView);

                    tabView.setPadding(0, 0, 0, 0);

                    //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                    int width = 0;
                    width = mTextView.getWidth();
                    if (width == 0) {
                        mTextView.measure(0, 0);
                        width = mTextView.getMeasuredWidth();
                    }

                    //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                    params.width = width ;
                    params.leftMargin = dp10;
                    params.rightMargin = dp10;
                    tabView.setLayoutParams(params);

                    tabView.invalidate();
                }

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
    }

    protected void addOnTabSelectedListener(TabLayout.OnTabSelectedListener listener) {
        layout_tab.addOnTabSelectedListener(listener);
    }
}
