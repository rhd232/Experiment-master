package com.jz.experiment.util;

import com.anitoa.bean.Data;
import com.anitoa.bean.FlashData;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.listener.AnitoaConnectionListener;
import com.anitoa.listener.SimpleConnectionListener;
import com.anitoa.service.CommunicationService;
import com.anitoa.util.ByteUtil;
import com.jz.experiment.chart.CommData;
import com.wind.base.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class FlashTrimReader {

    private CommunicationService communicationService;
    public FlashTrimReader(CommunicationService communicationService){
        this.communicationService=communicationService;
    }
    private int mReadTrimCount;
    Subscription mReadTrimSubscription;
    public void readTrimDataFromInstrument() {
        if (communicationService==null){
            throw new RuntimeException("CommunicationService is null");
        }
        if (mReadTrimSubscription!=null && !mReadTrimSubscription.isUnsubscribed()){
            mReadTrimSubscription.unsubscribe();
        }
        mReadTrimSubscription=null;
        communicationService.setNotify(mListener);
        mReadTrimSubscription= Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (mReadTrimCount==3){
                            mReadTrimSubscription.unsubscribe();
                            showConnectionTip();
                            return;
                        }
                        if (mReadTrimCount>=2){
                            mReadTrimSubscription.unsubscribe();
                            verifyConnection();
                        }else {
                            mReadTrimCount++;

                            TrimReader.getInstance().ReadTrimDataFromInstrument(communicationService);
                        }

                    }
                });
    }
    //dataposition.dat文件内容
    private String dp_str;
    private AnitoaConnectionListener mListener=new SimpleConnectionListener(){
        @Override
        public void onReceivedData(Data data) {
            if (mReadTrimSubscription==null){
                return;
            }
            mReadTrimSubscription.unsubscribe();
            //TODO 读取trim数据和dataposition数据
            byte[] reveicedBytes = data.getBuffer();
            int statusIndex = 1;
            int status = reveicedBytes[statusIndex];
            //TODO 检查返回的包是否正确
            boolean succ = StatusChecker.checkStatus(status);
            if (!succ) {
                return;
            }
            int cmdIndex = 2;
            int cmd = reveicedBytes[cmdIndex];

            int typeIndex = 4;
            int type = (reveicedBytes[typeIndex]);
            if (cmd == PcrCommand.READ_TRIM_CMD && type == PcrCommand.READ_TRIM_TYPE) {

                //接收到数据
                int length = reveicedBytes[3];
                int chanIndex = reveicedBytes[5];
                int curIndex = reveicedBytes[7];        // 当前是index个回复包，  0 -（total-1）
                int total = reveicedBytes[6];    //总共有多少个回复包
                MoveToEEPBuffer(reveicedBytes, curIndex);
                //header status   cmd  length   type  cIndex  total   curIndex
                //aa      00      04     39      2d    00      16       00     780000040406090602060306040702070307040802080308040906060607060807060707070808060807080809020202030204038b0b1717
                //判断是否读取完毕
                if (curIndex == (total - 1)) {
                    List<Integer> rlist = new ArrayList<>();      // row index
                    List<Integer> clist = new ArrayList<>();      // col index

                    byte[] trim_buff = new byte[2048];

                    for (int j = 0; j < EPKT_SZ; j++) {           // parity not copied
                        trim_buff[j] = EepromBuff[0][j];        // copy first page
                    }

                    int k = 0;

                    char version = (char) trim_buff[k];
                    k++;
                    int sn1 = trim_buff[k];
                    k++;
                    int sn2 = trim_buff[k];
                    k++;

                    int num_channels = trim_buff[k];//通道数
                    k++;
                    int num_wells = trim_buff[k];//孔数
                    k++;
                    int num_pages = trim_buff[k];
                    k++;

                    CommData.KsIndex = num_wells;
                    FlashData.NUM_WELLS=num_wells;
                    FlashData.NUM_CHANNELS=num_channels;

                    //4.13新增
                    CommData.sn1 = sn1;
                    CommData.sn2 = sn2;
                    for (int i = 1; i < num_pages; i++) {
                        for (int j = 0; j < EPKT_SZ; j++) {           // parity not copied
                            trim_buff[i * EPKT_SZ + j] = EepromBuff[i][j];
                        }
                    }

                    for (int i = 0; i < num_channels; i++) {
                        for (int j = 0; j < num_wells; j++) {
                            int n = trim_buff[k];
                            k++;
                            rlist.clear();
                            clist.clear();
                            for (int l = 0; l < n; l++) {
                                int row = trim_buff[k++]; // k++;
                                int col = trim_buff[k];
                                k++;

                                rlist.add(row);
                                clist.add(col);
                            }
                            FlashData.row_index[i][j] = new ArrayList<>(rlist);
                            FlashData.col_index[i][j] = new ArrayList<>(clist);
                        }
                    }
                    dp_str = Buf2String(trim_buff, k);
                    for (int ci = 0; ci < num_channels; ci++) {
                        int index_start = num_pages + ci * NUM_EPKT;
                        k = 0;

                        for (int i = 0; i < NUM_EPKT; i++) {
                            for (int j = 0; j < EPKT_SZ; j++) {           // parity not copied
                                trim_buff[i * EPKT_SZ + j] = EepromBuff[i + index_start][j];
                            }
                        }

                        byte b0 = trim_buff[k];
                        k++;        // Extract chip name
                        byte b1 = trim_buff[k];
                        k++;
                        byte b2 = trim_buff[k];
                        k++;

                        for (int i = 0; i < TRIM_IMAGER_SIZE; i++) {
                            for (int j = 0; j < 6; j++) {
                                FlashData.kbi[ci][i][j] = Buf2Int(trim_buff, k);
                                k += 2;
                            }
                        }

                        for (int i = 0; i < TRIM_IMAGER_SIZE; i++) {
                            FlashData.fpni[ci][0][i] = Buf2Int(trim_buff, k);
                            k += 2;
                            FlashData.fpni[ci][1][i] = Buf2Int(trim_buff, k);
                            k += 2;
                        }

                        FlashData.rampgen[ci] = trim_buff[k];
                        k++;
                        FlashData.range[ci] = trim_buff[k];
                        k++;
                        FlashData.auto_v20[ci][0] = trim_buff[k];
                        k++;
                        FlashData.auto_v20[ci][1] = trim_buff[k];
                        k++;
                        FlashData.auto_v15[ci] = trim_buff[k];
                        k++;
                    }

                    CommData.chan1_rampgen=  FlashData.rampgen[0];
                    CommData.chan2_rampgen=  FlashData.rampgen[1];
                    CommData.chan3_rampgen=  FlashData.rampgen[2];
                    CommData.chan4_rampgen=  FlashData.rampgen[3];

                    CommData.chan1_auto_v15= FlashData.auto_v15[0];
                    CommData.chan2_auto_v15=FlashData.auto_v15[1];
                    CommData.chan3_auto_v15=FlashData.auto_v15[2];
                    CommData.chan4_auto_v15=FlashData.auto_v15[3];

                    CommData.chan1_auto_v20=FlashData.auto_v20[0];
                    CommData.chan2_auto_v20=FlashData.auto_v20[1];
                    CommData.chan3_auto_v20=FlashData.auto_v20[2];
                    CommData.chan4_auto_v20=FlashData.auto_v20[3];

                    CommData.chan1_range=FlashData.range[0];
                    CommData.chan2_range=FlashData.range[1];
                    CommData.chan3_range=FlashData.range[2];
                    CommData.chan4_range=FlashData.range[3];
                    FlashData.flash_loaded = true;

                    FlashData.DATA_DEVICE_TRIM=dp_str;
                    FlashData.flash_inited=true;

                    //
                    communicationService.setNotify(null);
                    if (mOnReadFlashListener!=null){
                        mOnReadFlashListener.onReadFlashSuccess();
                    }
                }
            }
        }
    };
    private OnReadFlashListener mOnReadFlashListener;
    public void setOnReadFlashListener(OnReadFlashListener listener){
        this.mOnReadFlashListener=listener;
    }
    public interface OnReadFlashListener{
        void onReadFlashSuccess();
    }

    private void verifyConnection() {
        PcrCommand cmd=PcrCommand.ofLidAndApaptorStatusCmd();
        byte [] reveicedBytes=communicationService.sendPcrCommandSync(cmd);
        if (reveicedBytes!=null) {
            try{
                int statusIndex = 1;
                int status = reveicedBytes[statusIndex];
                //TODO 检查返回的包是否正确
                boolean succ = StatusChecker.checkStatus(status);
                if (succ){
                    readTrimDataFromInstrument();
                }else {
                    showConnectionTip();
                }
            }catch (Exception e){
                e.printStackTrace();
                showConnectionTip();
            }

        }else {
            showConnectionTip();
        }
    }
    private int Buf2Int(byte[] buff, int k) {
        byte[] x = {buff[k + 1],
                buff[k]};
        int y = ByteUtil.getShort(x);
        //int y = (int) BitConverter.ToInt16(x, 0);

        return y;
    }

    int EPKT_SZ = 52;
    int NUM_EPKT = 4;
    int TRIM_IMAGER_SIZE = 12;
    byte[][] EepromBuff = new byte[16 + 4 * NUM_EPKT][EPKT_SZ + 1];

    private void MoveToEEPBuffer(byte[] inputdatas, int index) {
        byte eeprom_parity = 0;

        for (int i = 0; i < EPKT_SZ + 1; i++) {
            EepromBuff[index][i] = inputdatas[8 + i];

            if (i < EPKT_SZ) {
                eeprom_parity += inputdatas[8 + i];
            } else {
                if (eeprom_parity != inputdatas[8 + i]) {
                    LogUtil.e("Packet parity error!");
                }
            }
        }
    }


    private String Buf2String(byte[] buff, int size)
    {
        //String rstr;
        StringBuilder sBuilder=new StringBuilder();
        //rstr = "Chipdp\r\n";
        sBuilder.append("Chipdp\r\n");
        for (int i = 0; i < size; i++)
        {
            sBuilder.append(buff[i]+" ");
            //String str = String.format("{0} ", buff[i]);
            //rstr += str;
        }
        // rstr += "\r\n";
        sBuilder.append("\r\n");
        return sBuilder.toString();
    }
    private void showConnectionTip(){
        communicationService.setNotify(null);

        if (mOnDeviceDisconnectionListener!=null){
            mOnDeviceDisconnectionListener.onDeviceDisconnected();
        }

        /*mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadingDialogHelper.hideOpLoading();
                AppDialogHelper.showNormalDialog(mActivity, "请检查HID设备连接情况", new AppDialogHelper.DialogOperCallback() {
                    @Override
                    public void onDialogConfirmClick() {

                    }
                });
            }
        });*/
    }

    public void destroy(){
        if (mReadTrimSubscription!=null && !mReadTrimSubscription.isUnsubscribed()){
            mReadTrimSubscription.unsubscribe();
        }
        mReadTrimSubscription=null;
    }


    private OnDeviceDisconnectionListener mOnDeviceDisconnectionListener;
    public void setOnDeviceDisconnectionListener(OnDeviceDisconnectionListener listener){
        mOnDeviceDisconnectionListener=listener;
    }
    public interface OnDeviceDisconnectionListener{
        void onDeviceDisconnected();
    }
}
