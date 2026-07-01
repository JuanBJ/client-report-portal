package com.clientreportportal.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "liabilities")
public class Liability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private String liabilityType;
    private double interestRate;

    @OneToMany(mappedBy = "liability", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LiabilityBalance> balances = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getLiabilityType() { return liabilityType; }
    public void setLiabilityType(String v) { this.liabilityType = v; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double v) { this.interestRate = v; }

    public List<LiabilityBalance> getBalances() { return balances; }
    public void setBalances(List<LiabilityBalance> v) { this.balances = v; }
}
