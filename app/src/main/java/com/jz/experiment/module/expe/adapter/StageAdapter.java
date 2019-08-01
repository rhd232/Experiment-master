package com.jz.experiment.module.expe.adapter;

import android.app.Activity;

import com.jz.experiment.R;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.adapter.BaseDelegateRecyclerAdapter;

import java.util.List;

public class StageAdapter extends BaseDelegateRecyclerAdapter {
    public static int STAGE_ITEM_WIDTH;
    public StageAdapter(Activity activity,int stageItemWidth) {
        super(activity);
        STAGE_ITEM_WIDTH=stageItemWidth;
    }
    public void setStageItemWidth(int stageItemWidth){
        STAGE_ITEM_WIDTH=stageItemWidth;
    }
    @Override
    protected void addDelegate() {
        manager
                .addDelegate(new StartStageDelegate(mActivity, R.layout.item_stage_start))
                .addDelegate(new CyclingStageDelegate(mActivity,this, R.layout.item_stage_cycling))
                .addDelegate(new EndStageDelegate(mActivity, R.layout.item_stage_end))
                .addDelegate(new MeltingStageDelegate(mActivity, R.layout.item_stage_melting));

    }


    public void buildLink() {

        for (int i = 0; i < getItemCount()-1; i++) {
            Stage stage = (Stage) getItem(i);
            if (stage instanceof CyclingStage){
                CyclingStage cyclingStage= (CyclingStage) stage;
                cyclingStage.setSerialNumber(i);//从数据库中读取出来时需要用到它来给CyclingStage排序
                List<PartStage> partStages=cyclingStage.getChildStages();
                stage=partStages.get(partStages.size()-1);
            }
            Stage next = (Stage) getItem(i +1);
            if (next instanceof CyclingStage){
                CyclingStage cyclingStage= (CyclingStage) next;
                List<PartStage> partStages=cyclingStage.getChildStages();
                next=partStages.get(0);
            }
            stage.setNext(next);

           /* if (stage instanceof CyclingStage){
                CyclingStage cyclingStage= (CyclingStage) stage;
                List<PartStage> partStages=cyclingStage.getChildStages();
                for (int j=0;j<partStages.size();j++){
                    PartStage partStage= partStages.get(j);


                    Stage s=cyclingStage.getNext();
                    if (s instanceof CyclingStage){
                        CyclingStage prevC= (CyclingStage) s;
                        List<PartStage> pPartStages=prevC.getChildStages();
                        s=pPartStages.get(partStages.size()-1);
                    }
                    partStage.setPrev(s);


                }
            }*/

        }
    }

}
