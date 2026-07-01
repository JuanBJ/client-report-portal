package com.awportal.dto;

public record AccountRequest(
    String bucket,
    String owner,
    String accountType,
    String accountNumberLast4,
    boolean isPrivateReserve,
    boolean isInvestmentForTarget
) {}
