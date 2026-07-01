package com.clientreportportal.repository;

import com.clientreportportal.model.QuarterlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuarterlyReportRepository extends JpaRepository<QuarterlyReport, Long> {
    List<QuarterlyReport> findByClientId(Long clientId);
    Optional<QuarterlyReport> findByClientIdAndQuarterAndYear(Long clientId, int quarter, int year);
}
