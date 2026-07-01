package com.awportal.dto;

public record LiabilityRequest(
    String liabilityType,
    double interestRate
) {}
