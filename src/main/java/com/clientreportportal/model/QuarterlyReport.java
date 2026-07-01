package com.clientreportportal.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quarterly_reports",
       uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "quarter", "report_year"}))
public class QuarterlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private int quarter;

    @Column(name = "report_year")
    private int year;
    private LocalDate asOfDate;

    private double salarySnapshot;
    private double expenseBudgetSnapshot;
    private double insuranceDeductiblesSnapshot;
    private Double trustPropertyValue;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AccountBalance> accountBalances = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LiabilityBalance> liabilityBalances = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public int getQuarter() { return quarter; }
    public void setQuarter(int v) { this.quarter = v; }

    public int getYear() { return year; }
    public void setYear(int v) { this.year = v; }

    public LocalDate getAsOfDate() { return asOfDate; }
    public void setAsOfDate(LocalDate v) { this.asOfDate = v; }

    public double getSalarySnapshot() { return salarySnapshot; }
    public void setSalarySnapshot(double v) { this.salarySnapshot = v; }

    public double getExpenseBudgetSnapshot() { return expenseBudgetSnapshot; }
    public void setExpenseBudgetSnapshot(double v) { this.expenseBudgetSnapshot = v; }

    public double getInsuranceDeductiblesSnapshot() { return insuranceDeductiblesSnapshot; }
    public void setInsuranceDeductiblesSnapshot(double v) { this.insuranceDeductiblesSnapshot = v; }

    public Double getTrustPropertyValue() { return trustPropertyValue; }
    public void setTrustPropertyValue(Double v) { this.trustPropertyValue = v; }

    public List<AccountBalance> getAccountBalances() { return accountBalances; }
    public void setAccountBalances(List<AccountBalance> v) { this.accountBalances = v; }

    public List<LiabilityBalance> getLiabilityBalances() { return liabilityBalances; }
    public void setLiabilityBalances(List<LiabilityBalance> v) { this.liabilityBalances = v; }
}
