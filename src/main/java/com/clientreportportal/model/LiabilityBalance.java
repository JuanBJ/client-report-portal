package com.clientreportportal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "liability_balances")
public class LiabilityBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private QuarterlyReport report;

    @ManyToOne
    @JoinColumn(name = "liability_id")
    private Liability liability;

    private double balance;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuarterlyReport getReport() { return report; }
    public void setReport(QuarterlyReport report) { this.report = report; }

    public Liability getLiability() { return liability; }
    public void setLiability(Liability liability) { this.liability = liability; }

    public double getBalance() { return balance; }
    public void setBalance(double v) { this.balance = v; }
}
