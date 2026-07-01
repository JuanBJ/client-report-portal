package com.awportal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "account_balances")
public class AccountBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private QuarterlyReport report;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private double balance;
    private Double cashBalance;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuarterlyReport getReport() { return report; }
    public void setReport(QuarterlyReport report) { this.report = report; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public double getBalance() { return balance; }
    public void setBalance(double v) { this.balance = v; }

    public Double getCashBalance() { return cashBalance; }
    public void setCashBalance(Double v) { this.cashBalance = v; }
}
