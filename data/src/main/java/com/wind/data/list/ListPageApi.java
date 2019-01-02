package com.wind.data.list;

import java.util.Map;

import rx.Observable;

/**
 * Created by wind on 2018/9/5.
 */

public interface ListPageApi {

     Observable<ListResponse> get(Map<String,String> params);
}
