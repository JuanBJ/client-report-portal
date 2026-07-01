package com.awportal.dto;

public record BalanceEntry(
    Long accountId,
    double balance,
    Double cashBalance
) {}
