package com.clientreportportal.dto;

public record LiabilityRequest(
    String liabilityType,
    double interestRate
) {}
