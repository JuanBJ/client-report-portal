package com.awportal.dto;

import java.time.LocalDate;

public record ClientRequest(
    String client1Name,
    LocalDate client1Dob,
    String client1SsnLast4,
    String client2Name,
    LocalDate client2Dob,
    String client2SsnLast4,
    double monthlySalary,
    double monthlyExpenseBudget,
    double insuranceDeductiblesTotal
) {}
