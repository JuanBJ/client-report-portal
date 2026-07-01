package com.awportal.dto;

public record CalculatedReport(
    Long reportId,
    Long clientId,
    int quarter,
    int year,
    double inflow,
    double outflow,
    double excessToReserve,
    double privateReserveBalance,
    double investmentBalance,
    double reserveTarget,
    boolean reserveFullyFunded,
    double client1RetirementTotal,
    double client2RetirementTotal,
    double nonRetirementTotal,
    double trustValue,
    double grandTotalNetWorth,
    double liabilitiesTotal
) {}
