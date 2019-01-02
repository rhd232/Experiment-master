package com.wind.base.api;

import com.wind.base.request.BaseRequest;
import com.wind.base.response.BaseResponse;

import java.util.Map;

import rx.Observable;

/**
 * Created by wind on 2017/2/28.
 */

public interface IItemApi<Q extends BaseRequest,R extends BaseResponse>  extends IRetrieveApi<Q,R> {

}
