package com.point.api.point.service;

import com.point.api.point.dto.PointAccrualRequest;
import com.point.api.point.dto.PointTransactionResponse;

public interface PointCommandService {

    PointTransactionResponse accrue(PointAccrualRequest request);
}
