package com.awportal.dto;

public record LiabilityResponse(
    Long id,
    String liabilityType,
    double interestRate
) {}
