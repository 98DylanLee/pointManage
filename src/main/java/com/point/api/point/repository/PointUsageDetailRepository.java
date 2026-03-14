package com.point.api.point.repository;

import com.point.api.point.entity.PointUsageDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointUsageDetailRepository extends JpaRepository<PointUsageDetail, Long> {

    List<PointUsageDetail> findByPoint_PointIdOrderByUsageDetailIdAsc(Long pointId);
}
