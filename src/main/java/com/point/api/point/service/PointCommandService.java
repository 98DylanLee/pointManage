package com.point.api.point.service;

import com.point.api.point.dto.PointEarnRequest;
import com.point.api.point.dto.PointEarnResponse;

public interface PointCommandService {

    PointEarnResponse earn(PointEarnRequest request);
}
