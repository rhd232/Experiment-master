package com.wind.base.di;

/**
 * Created by wind on 16/5/18.
 */
public interface HasComponent<C extends DaggerComponent> {
    C getComponent();
}
