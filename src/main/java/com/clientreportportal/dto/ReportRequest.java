package com.clientreportportal.dto;

import java.util.List;

public record ReportRequest(
    int quarter,
    int year,
    Double trustPropertyValue,
    List<BalanceEntry> accountBalances,
    List<LiabilityBalanceEntry> liabilityBalances
) {}
