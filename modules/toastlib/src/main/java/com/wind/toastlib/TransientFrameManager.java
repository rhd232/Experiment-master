package com.wind.toastlib;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by wind on 2018/1/19.
 */
 class TransientFrameManager {
    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS = 2750;

    public static final int MSG_DISMISS=1;
    public static final int MSG_TIMEOUT=2;
    private static TransientFrameManager instance;
    private TransientFrameRecord mCurrentRecord;
    private TransientFrameRecord mNextRecord;
    private Handler mHandler;
    private TransientFrameManager(){
        mHandler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_TIMEOUT:
                        handleTimeout((TransientFrameRecord) msg.obj);
                        break;
                }
            }
        };
    }

    private void handleTimeout(TransientFrameRecord record) {
        if (record==mCurrentRecord || record==mNextRecord){
            cancelTransientFrame(record);
        }
    }

    public static TransientFrameManager getInstance(){
        if (instance==null){
            synchronized (TransientFrameManager.class){
                if (instance==null){
                    instance=new TransientFrameManager();
                }
            }
        }
        return instance;
    }

    public void show(int duration,Callback callback){

        if (isCurrentRecordShown(callback)){ //判断即将显示的和正在显示的trasientFrame是否是同一个;
            mCurrentRecord.duration=duration;
            mHandler.removeCallbacksAndMessages(mCurrentRecord);
            scheduleTimeout(mCurrentRecord);
        }else if (isNextRecordShown(callback)){
            mNextRecord.duration=duration;
        }else {
            mNextRecord=new TransientFrameRecord(duration,callback);
        }

        if (mCurrentRecord!=null && cancelTransientFrame(mCurrentRecord)){
            return;
        }else {
            scheduleNextTransientFrame();
        }


    }

    private void scheduleNextTransientFrame() {
        if (mNextRecord!=null){
            mCurrentRecord=mNextRecord;
            mNextRecord=null;
            Callback callback=mCurrentRecord.getCallback().get();
            if (callback!=null){
                callback.show();
            }else {
                mCurrentRecord=null;
            }

        }

    }

    private boolean cancelTransientFrame(TransientFrameRecord record){
        Callback callback=record.getCallback().get();
        if (callback!=null){
            mHandler.removeCallbacksAndMessages(record);
            callback.dismiss();
            return true;
        }
        return false;
    }

    private boolean isNextRecordShown(Callback callback) {
        return mNextRecord!=null && mNextRecord.isShown(callback);
    }

    private void scheduleTimeout(TransientFrameRecord record) {
        if (record.duration== TransientFrame.LENGTH_INDEFINITE){
            return;
        }
        long delay;
        if (record.duration== TransientFrame.LENGTH_SHORT){
            delay=SHORT_DURATION_MS;
        }else {
            delay=LONG_DURATION_MS;
        }
        mHandler.removeCallbacksAndMessages(record);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, record), delay);
    }

    private boolean isCurrentRecordShown(Callback callback) {

        return mCurrentRecord!=null && mCurrentRecord.isShown(callback);
    }

    public void onShown(Callback callback) {
        if (isCurrentRecordShown(callback)) {
            scheduleTimeout(mCurrentRecord);
        }
    }

    public void onDismiss(Callback callback) {
        if (isCurrentRecordShown(callback)) {
            // If the callback is from a Snackbar currently show, remove it and show a new one
            mCurrentRecord = null;
            if (mNextRecord != null) {
                scheduleNextTransientFrame();
            }
        }
    }

    public class TransientFrameRecord{
        private int duration;
        private WeakReference<Callback> callback;
        public TransientFrameRecord(int duration,Callback callback){
            this.callback=new WeakReference<Callback>(callback);
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public WeakReference<Callback> getCallback() {
            return callback;
        }

        public void setCallback(WeakReference<Callback> callback) {
            this.callback = callback;
        }

        public boolean isShown(Callback callback) {

            return callback!=null&&this.callback.get()==callback;
        }
    }
    /**
     * 回调通知显示{@link com.wind.toastlib.TransientFrame}
     */
    public interface Callback{
        void show();
        void dismiss();
    }


}
