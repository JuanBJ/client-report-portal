package com.awportal.repository;

import com.awportal.model.QuarterlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuarterlyReportRepository extends JpaRepository<QuarterlyReport, Long> {
    List<QuarterlyReport> findByClientId(Long clientId);
}
