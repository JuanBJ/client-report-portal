package com.clientreportportal.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String client1Name;
    private LocalDate client1Dob;
    private String client1SsnLast4;

    private String client2Name;
    private LocalDate client2Dob;
    private String client2SsnLast4;

    private double monthlySalary;
    private double monthlyExpenseBudget;
    private double insuranceDeductiblesTotal;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Liability> liabilities = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuarterlyReport> reports = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClient1Name() { return client1Name; }
    public void setClient1Name(String v) { this.client1Name = v; }

    public LocalDate getClient1Dob() { return client1Dob; }
    public void setClient1Dob(LocalDate v) { this.client1Dob = v; }

    public String getClient1SsnLast4() { return client1SsnLast4; }
    public void setClient1SsnLast4(String v) { this.client1SsnLast4 = v; }

    public String getClient2Name() { return client2Name; }
    public void setClient2Name(String v) { this.client2Name = v; }

    public LocalDate getClient2Dob() { return client2Dob; }
    public void setClient2Dob(LocalDate v) { this.client2Dob = v; }

    public String getClient2SsnLast4() { return client2SsnLast4; }
    public void setClient2SsnLast4(String v) { this.client2SsnLast4 = v; }

    public double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(double v) { this.monthlySalary = v; }

    public double getMonthlyExpenseBudget() { return monthlyExpenseBudget; }
    public void setMonthlyExpenseBudget(double v) { this.monthlyExpenseBudget = v; }

    public double getInsuranceDeductiblesTotal() { return insuranceDeductiblesTotal; }
    public void setInsuranceDeductiblesTotal(double v) { this.insuranceDeductiblesTotal = v; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> v) { this.accounts = v; }

    public List<Liability> getLiabilities() { return liabilities; }
    public void setLiabilities(List<Liability> v) { this.liabilities = v; }

    public List<QuarterlyReport> getReports() { return reports; }
    public void setReports(List<QuarterlyReport> v) { this.reports = v; }
}
