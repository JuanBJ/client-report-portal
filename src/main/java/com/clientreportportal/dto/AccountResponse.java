package com.clientreportportal.dto;

public record AccountResponse(
    Long id,
    String bucket,
    String owner,
    String accountType,
    String accountNumberLast4,
    boolean isPrivateReserve,
    boolean isInvestmentForTarget
) {}
