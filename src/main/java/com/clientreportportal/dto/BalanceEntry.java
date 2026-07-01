package com.clientreportportal.dto;

public record BalanceEntry(
    Long accountId,
    double balance,
    Double cashBalance
) {}
