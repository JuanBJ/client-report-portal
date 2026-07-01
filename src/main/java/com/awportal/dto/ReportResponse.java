package com.awportal.dto;

import java.time.LocalDate;

public record ReportResponse(
    Long id,
    Long clientId,
    int quarter,
    int year,
    LocalDate asOfDate,
    double salarySnapshot,
    double expenseBudgetSnapshot,
    double insuranceDeductiblesSnapshot,
    Double trustPropertyValue
) {}
