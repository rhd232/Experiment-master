package com.wind.base.api;

import com.wind.base.request.PageRequest;
import com.wind.base.response.PageResponse;

/**
 * Created by wind on 2017/2/27.
 */

public interface IPageApi<Q extends PageRequest,R extends PageResponse> extends IRetrieveApi<Q,R> {


}
