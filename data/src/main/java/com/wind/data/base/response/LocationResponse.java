package com.wind.data.base.response;

import com.wind.base.response.BaseResponse;
import com.wind.data.base.bean.Location;

/**
 * Created by wind on 2017/3/25.
 */

public class LocationResponse extends BaseResponse {

    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
