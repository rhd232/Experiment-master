package com.jz.experiment.module.expe.di;

import com.jz.experiment.di.PlaceHolderModule;
import com.jz.experiment.module.expe.HistoryExperimentsFragment;
import com.wind.base.di.DaggerComponent;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;
@Subcomponent(modules = PlaceHolderModule.class)
public interface HistoryExperimentComponent extends DaggerComponent,AndroidInjector<HistoryExperimentsFragment> {

    @Subcomponent.Builder
    public abstract class Builder extends AndroidInjector.Builder<HistoryExperimentsFragment> {

    }
}
