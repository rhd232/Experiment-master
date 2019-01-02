package com.wind.domain.base;

import com.wind.base.repository.PageRepository;
import com.wind.base.usecase.PageUsecase;
import com.wind.data.list.ListRequest;
import com.wind.data.list.ListResponse;

import javax.inject.Inject;

/**
 * Created by wind on 2018/9/5.
 */

public class ListUsecase extends PageUsecase<ListRequest,ListResponse> {
    @Inject
    public ListUsecase(PageRepository pageRepository) {
        super(pageRepository);
    }
}
