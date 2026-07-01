package com.clientreportportal.dto;

public record LiabilityResponse(
    Long id,
    String liabilityType,
    double interestRate
) {}
