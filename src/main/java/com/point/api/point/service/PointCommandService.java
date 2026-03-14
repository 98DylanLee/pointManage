package com.point.api.point.service;

import com.point.api.point.dto.PointEarnCancelRequest;
import com.point.api.point.dto.PointEarnCancelResponse;
import com.point.api.point.dto.PointEarnRequest;
import com.point.api.point.dto.PointEarnResponse;
import com.point.api.point.dto.PointUseCancelRequest;
import com.point.api.point.dto.PointUseCancelResponse;
import com.point.api.point.dto.PointUseRequest;
import com.point.api.point.dto.PointUseResponse;

public interface PointCommandService {

    PointEarnResponse earn(PointEarnRequest request);

    PointEarnCancelResponse cancelEarn(PointEarnCancelRequest request);

    PointUseResponse use(PointUseRequest request);

    PointUseCancelResponse cancelUse(PointUseCancelRequest request);
}
