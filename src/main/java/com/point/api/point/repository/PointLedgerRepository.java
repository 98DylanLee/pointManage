package com.point.api.point.repository;

import com.point.api.point.entity.PointLedger;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {

    Optional<PointLedger> findByPointKey(String pointKey);
}
