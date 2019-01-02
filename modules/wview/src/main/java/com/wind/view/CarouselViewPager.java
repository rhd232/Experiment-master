package com.wind.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by wind on 2017/3/6.
 */

public class CarouselViewPager extends LinearLayout implements ViewPager.OnPageChangeListener {
    public CarouselViewPager(@NonNull Context context) {
        super(context);
        init(null);
    }

    public CarouselViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CarouselViewPager(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CarouselViewPager(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private ViewPager mViewPager;
    private ImagePagerAdapter mImagePagerAdapter;
    private RecyclerView mRvDots;
    private int mCurrentPosition;
    private DotsAdapter mDotsAdapter;
    private CarouselHandler mHander;

    private int mDotMarginBottom,mDotInterDotMargin,mDotSize;
    private Drawable mDotDrawable;
    private void init(AttributeSet attrs) {
        TypedArray typeArray = getContext().obtainStyledAttributes(attrs, R.styleable.CarouselViewPager);
        mDotMarginBottom=typeArray
                .getDimensionPixelSize(R.styleable.CarouselViewPager_dots_margin_bottom,
                        getContext().getResources().getDimensionPixelSize(R.dimen.wd_carousel_dots_bottom_margin));
        mDotInterDotMargin=typeArray.
                getDimensionPixelSize(R.styleable.CarouselViewPager_dot_inter_dot_margin,
                        getContext().getResources().getDimensionPixelSize(R.dimen.wd_carousel_dot_inter_dot_margin));
        mDotSize=typeArray.
                getDimensionPixelSize(R.styleable.CarouselViewPager_dot_size,
                        getContext().getResources().getDimensionPixelSize(R.dimen.wd_carousel_dot_size));
        mDotDrawable=typeArray.getDrawable(R.styleable.CarouselViewPager_dot_color_selector);
        typeArray.recycle();

        inflate(getContext(), R.layout.wd_layout_carousel, this);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mRvDots = (RecyclerView) findViewById(R.id.rv_dots);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvDots.setLayoutManager(layoutManager);
        mViewPager.addOnPageChangeListener(this);
        mDotsAdapter = new DotsAdapter();
        mRvDots.setAdapter(mDotsAdapter);




        MarginLayoutParams params= (MarginLayoutParams) mRvDots.getLayoutParams();
        params.bottomMargin=mDotMarginBottom;

    }

    public void setImageResIds(List<Integer> resIds) {
        setupPager(resIds);
        mSize=resIds.size();
        startCarousel();
    }


    private int mSize;
    public void setImageResPaths(List<String> items) {
        setupPager(items);
        mSize=items.size();
        startCarousel();

    }
    private void setupPager(List resIds) {

        mImagePagerAdapter = new ImagePagerAdapter(getContext(), resIds);
        mViewPager.setAdapter(mImagePagerAdapter);
        mViewPager.setCurrentItem(mCurrentPosition);
        if (resIds.size()>1)
            mDotsAdapter.replaceAll(resIds);

    }
    public void startCarousel() {
        cancel();
        mHander = new CarouselHandler(getContext(), mSize);
        //启动轮播
        mHander.sendEmptyMessageDelayed(CarouselHandler.MSG_UPDATE_IMAGE, CarouselHandler.MSG_DELAY);
    }

    public boolean isCarouseling(){
        return mHander!=null;
    }
    public void cancel(){
        if (mHander!=null){
            mHander.removeMessages(CarouselHandler.MSG_UPDATE_IMAGE);
            mHander=null;
        }
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
       // LogUtil.e("CarouselViewPager","onPageSelected");
        mCurrentPosition = position;
        mDotsAdapter.notifyDataSetChanged();
        if (mHander==null){
            return;
        }
        mHander.sendMessage(Message.obtain(mHander, CarouselHandler.MSG_PAGE_CHANGED, position, 0));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mHander==null){
            return;
        }
        switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING:
                mHander.sendEmptyMessage(CarouselHandler.MSG_KEEP_SILENT);
                break;
            case ViewPager.SCROLL_STATE_IDLE:
                mHander.sendEmptyMessageDelayed(CarouselHandler.MSG_UPDATE_IMAGE, CarouselHandler.MSG_DELAY);
                break;
            default:
                break;
        }
    }

    private class DotsAdapter extends RecyclerView.Adapter<DotsAdapter.ViewHolder> {

        private List items;

        private DotsAdapter() {
            items = new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.wd_carousel_item_dot, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position == mCurrentPosition) {
                holder.itemView.setActivated(true);
            } else {
                holder.itemView.setActivated(false);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void replaceAll(List resIds) {
            items.clear();
            items.addAll(resIds);
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View itemView) {
                super(itemView);

                MarginLayoutParams params= (MarginLayoutParams) itemView.getLayoutParams();
                params.leftMargin=mDotInterDotMargin;
                params.rightMargin=mDotInterDotMargin;
                params.width=mDotSize;
                params.height=mDotSize;
                if (mDotDrawable!=null)
                    itemView.setBackgroundDrawable(mDotDrawable);
            }
        }
    }

    private class ImagePagerAdapter<T> extends PagerAdapter {
        List<T> items;
        private Context mContext;

        public ImagePagerAdapter(Context context, List<T> items) {
            this.items = items;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container,final int position) {  //这个方法用来实例化页卡
            ImageView iv = new ImageView(mContext);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            T path = items.get(position);

            if (path instanceof String) {
                WdImageLoader.display(mContext, iv, (String) (items.get(position)), R.drawable.placeholder_bg);
            } else {
                WdImageLoader.display(mContext, iv, (Integer) (items.get(position)), R.drawable.placeholder_bg);
            }
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener!=null){
                        mListener.onCarouseItemClick(position);
                    }
                }
            });
            container.addView(iv, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            return iv;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    private OnCarouselItemClickListener mListener;
    public void setOnCarouselItemClickListener(OnCarouselItemClickListener listener){
        this.mListener=listener;
    }
    public interface OnCarouselItemClickListener{
        void onCarouseItemClick(int position);
    }
    private class CarouselHandler extends Handler {

        /**
         * 请求更新显示的View。
         */
        protected static final int MSG_UPDATE_IMAGE = 1;
        /**
         * 请求暂停轮播。
         */
        protected static final int MSG_KEEP_SILENT = 2;
        /**
         * 请求恢复轮播。
         */
        protected static final int MSG_BREAK_SILENT = 3;
        /**
         * 记录最新的页号，当用户手动滑动时需要记录新页号，否则会使轮播的页面出错。
         * 例如当前如果在第一页，本来准备播放的是第二页，而这时候用户滑动到了末页，
         * 则应该播放的是第一页，如果继续按照原来的第二页播放，则逻辑上有问题。
         */
        protected static final int MSG_PAGE_CHANGED = 4;

        //轮播间隔时间
        protected static final long MSG_DELAY = 3000;

        //使用弱引用避免Handler泄露.这里的泛型参数可以不是Activity，也可以是Fragment等
        private WeakReference<Context> weakReference;
        private int length;

        protected CarouselHandler(Context context, int length) {
            weakReference = new WeakReference<Context>(context);
            this.length = length;
        }

        public void startCarouse() {
            sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Context activity = weakReference.get();
            if (activity == null) {
                //Activity已经回收，无需再处理UI了
                return;
            }
            if (length==0){
                return;
            }
            if (msg.what==MSG_UPDATE_IMAGE){
                //检查消息队列并移除未发送的消息，这主要是避免在复杂环境下消息出现重复等问题。
                if (hasMessages(MSG_UPDATE_IMAGE)) {
                    removeMessages(MSG_UPDATE_IMAGE);
                }
            }

            switch (msg.what) {
                case MSG_UPDATE_IMAGE:
                    mCurrentPosition++;
                    mCurrentPosition = mCurrentPosition % length;
                    mViewPager.setCurrentItem(mCurrentPosition);
                    //准备下次播放
                    sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                    break;
                case MSG_KEEP_SILENT:
                    //只要不发送消息就暂停了
                    break;
                case MSG_BREAK_SILENT:
                    sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                    break;
                case MSG_PAGE_CHANGED:
                    //记录当前的页号，避免播放的时候页面显示不正确。
                    mCurrentPosition = msg.arg1;
                    break;
                default:
                    break;
            }
        }
    }


}
