package com.wind.base.mvp.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecyclerView;
import com.hannesdorfmann.mosby.mvp.MvpView;
import com.wind.base.R;
import com.wind.base.adapter.BaseDelegateRecyclerAdapter;
import com.wind.base.bean.EmptyBean;
import com.wind.base.di.BaseMvpComponent;
import com.wind.base.mvp.presenter.ExecutePresenter;
import com.wind.base.recyclerview.decoration.VerticalSpacesItemDecoration;
import com.wind.base.request.BaseRequest;
import com.wind.base.response.PageResponse;
import com.wind.base.utils.DisplayUtil;

public abstract class BasePageFragment<V extends MvpView,P extends ExecutePresenter<V>,C extends BaseMvpComponent<V,P>,PR extends PageResponse>
        extends DaggerMvpFragment<V,P,C>
        implements MvpPageLoadingView<PR> {

    protected PullToRefreshRecyclerView mPrv;
    protected ezy.ui.layout.LoadingLayout mLayoutManager;
    protected BaseDelegateRecyclerAdapter mAdapter;
    private boolean mIsLoading;
    private boolean mCanRefreshWhenEmpty;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLayoutManager = view.findViewById(R.id.layout_loading);
        mLayoutManager.setEmptyImage(R.drawable.empty_icon);
        mLayoutManager.setEmptyText("空空如也~");
        mPrv = view.findViewById(R.id.prv);
        mAdapter = getAdapter();

        setPrvMode(PullToRefreshBase.Mode.BOTH);
        mPrv.getRefreshableView().setLayoutManager(getLayoutManager());
        mPrv.setAdapter(mAdapter);
        mLayoutManager.setRetryListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutManager.showLoading();
                loadFirstPage();
            }
        });


        RecyclerView.ItemDecoration decoration=getItemDecoration();
        mPrv.getRefreshableView().addItemDecoration(decoration);
        mLayoutManager.showLoading();
        mPrv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadFirstPage();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadNextPage();
            }

        });
        //setBackgroundColor(Color.WHITE);
        loadFirstPage();

        View ll_back=view.findViewById(R.id.ll_back);
        if (ll_back!=null){
            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }
        View titleBarLeft=view.findViewById(R.id.titlebar_left);
        if (titleBarLeft!=null){
            titleBarLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }
    }

    public void setPrvMode(PullToRefreshBase.Mode mode) {
        mPrv.setMode(mode);
    }

    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new VerticalSpacesItemDecoration(DisplayUtil.dip2px(getActivity(),8));
    }

    protected void loadFirstPage() {
        if (!mIsLoading){
            mIsLoading=true;
            presenter.execute(buildRequest(true));
        }

    }
    protected void loadNextPage(){
        if (!mIsLoading) {
            mIsLoading = true;
            presenter.execute(buildRequest(false));
        }
    }



    /*protected  void addItemDecoration(RecyclerView.ItemDecoration itemDecoration){
        mPrv.getRefreshableView().addItemDecoration(itemDecoration);
    }
*/

    protected RecyclerView.LayoutManager getLayoutManager() {
        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        return manager;
    }

    public void setBackgroundColor(int color){
        mLayoutManager.setBackgroundColor(color);
    }

    @Override
    public void showEmptyView(boolean isShow) {
        mLayoutManager.showEmpty();
        mIsLoading=false;
    }

    @Override
    public void showErrorView(boolean isShow) {
        mLayoutManager.showError();
        mIsLoading=false;
    }

    @Override
    public void showPageLoadingIndicator() {
    }

    @Override
    public void hidePageLoadingIndicator() {

    }


    protected void setCanRefreshWhenEmpty(boolean canRefreshWhenEmpty){
        this.mCanRefreshWhenEmpty=canRefreshWhenEmpty;
    }
    @Override
    public void showPage(PR response) {
        if (response.isFirstPage()){
            mAdapter.replace(response.getItems());
            if (mCanRefreshWhenEmpty) {
                if (mAdapter.getItemCount() == 0) {
                    mAdapter.add(new EmptyBean());
                }
            }
        }else {
            mAdapter.addAll(response.getItems());
        }
        if (!mCanRefreshWhenEmpty) {
            if (mAdapter.getItemCount() == 0) {
                mLayoutManager.showEmpty();
            } else {
                mLayoutManager.showContent();
            }
        }else {
            mLayoutManager.showContent();
        }
        mIsLoading=false;
        mPrv.onRefreshComplete();
    }
    @Override
    public P createPresenter() {
        return getComponent().presenter();
    }
    protected abstract BaseDelegateRecyclerAdapter getAdapter();

    protected abstract BaseRequest buildRequest(boolean isFirstPage);
}
